# 分布式 Agent 协作架构

## 核心问题

当 CrewAI 的三个 Agent 分别运行在三个独立的 Docker 容器中时：

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│  Container 1 │         │  Container 2 │         │  Container 3 │
│              │         │              │         │              │
│  ┌────────┐  │         │  ┌────────┐  │         │  ┌────────┐  │
│  │Agent A │  │         │  │Agent B │  │         │  │Agent C │  │
│  │        │  │         │  │        │  │         │  │        │  │
│  │CrewAI  │  │         │  │CrewAI  │  │         │  │CrewAI  │  │
│  └────────┘  │         │  └────────┘  │         │  └────────┘  │
└──────────────┘         └──────────────┘         └──────────────┘
      │                         │                         │
      └─────────────────────────┼─────────────────────────┘
                                │
                       ❌ 无法直接通信
                       ❌ CrewAI 假设同进程
                       ❌ 原生 delegation 失效
```

**CrewAI 的限制**：
```python
# CrewAI 原生代码（单进程）
crew = Crew(
    agents=[agent_a, agent_b, agent_c],  # 同一个 Python 对象
    tasks=[task1, task2, task3],
    process=Process.sequential
)

# 当 agent_a 委派任务时，它直接调用：
# agent_b.delegate() - 这是 Python 函数调用
# 但在分布式环境中，agent_b 在另一个容器中！
```

---

## 解决方案：分布式 CrewAI Runtime (Distributed Crew)

### 核心思想

创建一个**消息中间件层**，拦截 CrewAI 的本地调用并转换为分布式消息：

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    分布式 CrewAI 架构                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐            │
│  │  Container 1 │    │  Container 2 │    │  Container 3 │            │
│  │              │    │              │    │              │            │
│  │ ┌──────────┐ │    │ ┌──────────┐ │    │ ┌──────────┐ │            │
│  │ │Agent A   │ │    │ │Agent B   │ │    │ │Agent C   │ │            │
│  │ │          │ │    │ │          │ │    │ │          │ │            │
│  │ │CrewAI    │ │    │ │CrewAI    │ │    │ │CrewAI    │ │            │
│  │ │+ Runtime │ │    │ │+ Runtime │ │    │ │+ Runtime │ │            │
│  │ └────┬─────┘ │    │ └────┬─────┘ │    │ └────┬─────┘ │            │
│  └──────┼───────┘    └──────┼───────┘    └──────┼───────┘            │
│         │                    │                    │                   │
│         └────────────────────┼────────────────────┘                   │
│                              │                                       │
│                              ▼                                       │
│                    ┌─────────────────────┐                          │
│                    │   Message Broker    │                          │
│                    │     (Redis)         │                          │
│                    │                     │                          │
│                    │  crew:crew-001     │ ← 虚拟 Crew 的消息频道    │
│                    │  agent:a           │                          │
│                    │  agent:b           │                          │
│                    │  agent:c           │                          │
│                    └─────────────────────┘                          │
│                              │                                       │
│                              ▼                                       │
│                    ┌─────────────────────┐                          │
│                    │  Crew Coordinator   │ ← 协调分布式 Crew       │
│                    │  (可选/系统级)      │                          │
│                    └─────────────────────┘                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 架构层级

```
┌───────────────────────────────────────────────────────────────────┐
│  应用层：CrewAI Agent (业务逻辑)                                   │
│  - Agent 定义 (role, goal, backstory)                              │
│  - Task 定义                                                       │
└────────────────────────────┬──────────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────────┐
│  分布式 Runtime 层（新增）                                        │
│  - 拦截 CrewAI 的本地调用                                          │
│  - 转换为消息协议                                                 │
│  - 处理跨容器通信                                                 │
└────────────────────────────┬──────────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────────┐
│  消息传输层                                                       │
│  - Redis Pub/Sub                                                  │
│  - 消息路由                                                       │
│  - 可靠性保证                                                     │
└───────────────────────────────────────────────────────────────────┘
```

---

## 核心实现

### 1. 分布式 CrewAI Runtime

```python
# distributed_crew/runtime.py
"""
分布式 CrewAI Runtime
将 CrewAI 的本地调用转换为分布式消息传递
"""

import asyncio
import json
import uuid
from typing import Dict, List, Optional, Any
from dataclasses import dataclass, field
from datetime import datetime
import redis.asyncio as redis
from loguru import logger

# CrewAI 导入（我们需要猴子补丁这些）
from crewai import Agent, Task, Crew
from crewai.agents.agent_builder.base_agent import BaseAgent


@dataclass
class DistributedAgentConfig:
    """分布式 Agent 配置"""
    agent_id: str
    crew_id: str  # 所属的逻辑 Crew
    role: str
    goal: str
    backstory: str
    capabilities: List[str] = field(default_factory=list)
    delegation_enabled: bool = True


@dataclass
class CrewMembership:
    """Crew 成员信息"""
    crew_id: str
    members: Dict[str, DistributedAgentConfig]  # agent_id -> config
    process_type: str = "sequential"  # sequential, hierarchical


class DistributedCrewRuntime:
    """
    分布式 CrewAI Runtime

    核心功能：
    1. 管理 Agent 的 Crew 成员关系
    2. 拦截并转换 CrewAI 的本地调用为分布式消息
    3. 协调跨容器的任务执行和委派
    """

    def __init__(
        self,
        agent_id: str,
        redis_url: str,
        config: DistributedAgentConfig
    ):
        self.agent_id = agent_id
        self.config = config
        self.redis = redis.from_url(redis_url)

        # 虚拟 Crew 的通信频道
        self.crew_channel = f"crew:{config.crew_id}"
        self.agent_channel = f"agent:{agent_id}"

        # 状态管理
        self.current_crew: Optional[CrewMembership] = None
        self.pending_tasks: Dict[str, asyncio.Future] = {}
        self.running = False

    async def join_crew(self, crew_id: str) -> bool:
        """加入一个虚拟 Crew"""
        try:
            # 从 Redis 获取 Crew 定义
            crew_data = await self.redis.get(f"crew:definition:{crew_id}")

            if not crew_data:
                logger.warning(f"Crew {crew_id} 不存在，等待创建...")
                # 可以等待或返回 False
                return False

            crew_info = json.loads(crew_data)
            self.current_crew = CrewMembership(
                crew_id=crew_id,
                members={
                    m_id: DistributedAgentConfig(**m_cfg)
                    for m_id, m_cfg in crew_info["members"].items()
                },
                process_type=crew_info.get("process_type", "sequential")
            )

            # 广播加入事件
            await self._publish_event("agent_joined", {
                "agent_id": self.agent_id,
                "crew_id": crew_id,
                "timestamp": datetime.utcnow().isoformat()
            })

            logger.info(f"Agent {self.agent_id} 已加入 Crew {crew_id}")

            # 启动消息监听
            await self._start_message_listener()

            return True

        except Exception as e:
            logger.error(f"加入 Crew 失败: {e}")
            return False

    async def execute_task(
        self,
        task: Task,
        context: Optional[Dict[str, Any]] = None
    ) -> Any:
        """
        执行任务（分布式版本）

        这是 CrewAI 调用的入口点，我们拦截它并转换为分布式执行
        """
        task_id = str(uuid.uuid4())

        logger.info(f"[{self.agent_id}] 开始执行任务: {task.description}")

        # 检查是否需要委派
        if self.config.delegation_enabled and await self._should_delegate(task):
            return await self._delegate_task(task_id, task, context)

        # 执行任务
        result = await self._execute_locally(task, context)

        # 发送完成事件
        await self._publish_event("task_completed", {
            "task_id": task_id,
            "agent_id": self.agent_id,
            "result": str(result)[:500],
            "timestamp": datetime.utcnow().isoformat()
        })

        return result

    async def _should_delegate(self, task: Task) -> bool:
        """判断是否应该委派任务"""
        # 检查任务是否超出当前 Agent 的能力
        # 可以基于关键词、任务复杂度等判断

        # 简化实现：总是尝试委派（让其他 Agent 竞价）
        if not self.current_crew:
            return False

        return True

    async def _delegate_task(
        self,
        task_id: str,
        task: Task,
        context: Optional[Dict[str, Any]]
    ) -> Any:
        """
        委派任务给 Crew 中的其他 Agent

        这是 CrewAI delegation 的分布式版本
        """
        logger.info(f"[{self.agent_id}] 委派任务: {task.description}")

        # 创建委派消息
        delegation_message = {
            "type": "delegation",
            "delegation_id": task_id,
            "source_agent": self.agent_id,
            "crew_id": self.current_crew.crew_id,
            "task": {
                "description": task.description,
                "expected_output": task.expected_output,
                "context": context
            },
            "timestamp": datetime.utcnow().isoformat(),
            "timeout": 300  # 5分钟超时
        }

        # 广播到 Crew 频道（其他成员会收到）
        await self.redis.publish(
            self.crew_channel,
            json.dumps(delegation_message)
        )

        # 等待其他 Agent 接受委派
        try:
            # 创建 Future 来等待响应
            future = asyncio.Future()
            self.pending_tasks[task_id] = future

            # 等待响应（带超时）
            result = await asyncio.wait_for(future, timeout=300)

            logger.info(f"[{self.agent_id}] 委派完成: {task_id}")
            return result

        except asyncio.TimeoutError:
            logger.warning(f"[{self.agent_id}] 委派超时: {task_id}")
            # 超时后自己执行
            return await self._execute_locally(task, context)
        finally:
            self.pending_tasks.pop(task_id, None)

    async def _execute_locally(
        self,
        task: Task,
        context: Optional[Dict[str, Any]]
    ) -> Any:
        """本地执行任务（调用原始 CrewAI Agent）"""
        # 这里调用原始 CrewAI Agent 的执行逻辑
        # 需要通过猴子补丁或适配器来访问

        logger.info(f"[{self.agent_id}] 本地执行: {task.description}")

        # 实际实现中，这里会调用 CrewAI 的 LLM
        # 简化示例：
        return f"Agent {self.agent_id} 完成了任务: {task.description}"

    async def handle_delegation_request(self, message: Dict[str, Any]):
        """
        处理收到的委派请求

        当其他 Agent 委派任务时被调用
        """
        task_info = message["task"]
        delegation_id = message["delegation_id"]

        logger.info(f"[{self.agent_id}] 收到委派请求: {delegation_id}")

        # 决定是否接受委派
        should_accept = await self._should_accept_delegation(task_info)

        if should_accept:
            # 发送接受响应
            response = {
                "type": "delegation_response",
                "delegation_id": delegation_id,
                "agent_id": self.agent_id,
                "accepted": True,
                "timestamp": datetime.utcnow().isoformat()
            }

            await self.redis.publish(
                f"delegation:{delegation_id}",
                json.dumps(response)
            )

            # 执行任务
            # 创建 Task 对象（简化）
            task = Task(
                description=task_info["description"],
                expected_output=task_info.get("expected_output", "")
            )

            result = await self._execute_locally(task, task_info.get("context"))

            # 发送结果
            result_message = {
                "type": "delegation_result",
                "delegation_id": delegation_id,
                "agent_id": self.agent_id,
                "result": str(result),
                "timestamp": datetime.utcnow().isoformat()
            }

            await self.redis.publish(
                f"delegation:{delegation_id}",
                json.dumps(result_message)
            )

    async def _should_accept_delegation(self, task_info: Dict[str, Any]) -> bool:
        """
        决定是否接受委派

        这是 Agent 自主决策的关键点
        可以基于：
        1. 当前负载
        2. 任务匹配度
        3. 自身能力
        """
        # 简化实现：总是接受
        # 实际中可以让 Agent 用 LLM 判断
        return True

    async def _start_message_listener(self):
        """启动消息监听器"""

        # 订阅多个频道
        pubsub = self.redis.pubsub()

        await pubsub.subscribe(
            self.crew_channel,      # Crew 频道
            self.agent_channel,     # 私有频道
            f"delegation:*"         # 委派响应频道
        )

        async for message in pubsub.listen():
            if not self.running:
                break

            if message['type'] == 'message':
                try:
                    data = json.loads(message['data'])
                    await self._handle_message(data)
                except Exception as e:
                    logger.error(f"处理消息失败: {e}")

    async def _handle_message(self, message: Dict[str, Any]):
        """处理收到的消息"""
        msg_type = message.get("type")

        if msg_type == "delegation":
            # 委派请求
            await self.handle_delegation_request(message)

        elif msg_type == "delegation_response":
            # 委派响应
            delegation_id = message["delegation_id"]
            if delegation_id in self.pending_tasks:
                if message["accepted"]:
                    logger.info(f"[{self.agent_id}] 委派被接受")
                    # 继续等待结果...
                else:
                    # 拒绝，需要找其他 Agent 或自己执行
                    logger.info(f"[{self.agent_id}] 委派被拒绝")

        elif msg_type == "delegation_result":
            # 委派结果
            delegation_id = message["delegation_id"]
            if delegation_id in self.pending_tasks:
                future = self.pending_tasks[delegation_id]
                future.set_result(message["result"])

        elif msg_type == "crew_task":
            # Crew 协调的任务分配
            await self.handle_crew_task(message)

    async def handle_crew_task(self, message: Dict[str, Any]):
        """处理 Crew 协调器分配的任务"""
        task_spec = message["task"]
        task = Task(
            description=task_spec["description"],
            expected_output=task_spec.get("expected_output", "")
        )

        result = await self.execute_task(task, task_spec.get("context"))

        # 发送结果回协调器
        response = {
            "type": "task_result",
            "task_id": message["task_id"],
            "agent_id": self.agent_id,
            "result": str(result),
            "timestamp": datetime.utcnow().isoformat()
        }

        await self.redis.publish(
            f"crew:result:{message['coordinator_id']}",
            json.dumps(response)
        )

    async def _publish_event(self, event_type: str, data: Dict[str, Any]):
        """发布事件"""
        event = {
            "type": event_type,
            "agent_id": self.agent_id,
            "crew_id": self.config.crew_id,
            "timestamp": datetime.utcnow().isoformat(),
            "data": data
        }

        await self.redis.publish(
            f"events:{self.config.crew_id}",
            json.dumps(event)
        )

    async def start(self):
        """启动 Runtime"""
        self.running = True
        logger.info(f"[{self.agent_id}] Distributed Runtime 启动")

    async def stop(self):
        """停止 Runtime"""
        self.running = False
        await self.redis.close()
        logger.info(f"[{self.agent_id}] Distributed Runtime 停止")
```

### 2. Crew 协调器（可选）

```python
# distributed_crew/coordinator.py
"""
Crew 协调器 - 管理分布式 Crew 的执行流程
"""

import asyncio
import json
import uuid
from typing import Dict, List, Optional
from dataclasses import dataclass
import redis.asyncio as redis
from loguru import logger


@dataclass
class DistributedCrewSpec:
    """分布式 Crew 定义"""
    crew_id: str
    name: str
    process_type: str  # sequential, hierarchical
    agents: List[Dict[str, any]]  # Agent 配置列表
    tasks: List[Dict[str, any]]   # Task 定义列表


class CrewCoordinator:
    """
    Crew 协调器

    职责：
    1. 管理 Crew 的定义和成员
    2. 协调任务的顺序执行
    3. 处理层级委派
    """

    def __init__(self, redis_url: str):
        self.redis = redis.from_url(redis_url)
        self.active_crews: Dict[str, DistributedCrewSpec] = {}

    async def create_crew(self, spec: DistributedCrewSpec) -> str:
        """创建一个虚拟 Crew"""
        crew_id = spec.crew_id

        # 保存 Crew 定义到 Redis
        await self.redis.set(
            f"crew:definition:{crew_id}",
            json.dumps({
                "crew_id": crew_id,
                "name": spec.name,
                "process_type": spec.process_type,
                "members": {
                    agent["agent_id"]: agent
                    for agent in spec.agents
                }
            })
        )

        self.active_crews[crew_id] = spec

        logger.info(f"Crew {crew_id} 已创建，包含 {len(spec.agents)} 个 Agent")

        return crew_id

    async def execute_crew(self, crew_id: str, inputs: Dict[str, any]) -> Dict[str, any]:
        """执行 Crew（分布式版本）"""

        crew_spec = self.active_crews.get(crew_id)
        if not crew_spec:
            crew_data = await self.redis.get(f"crew:definition:{crew_id}")
            if not crew_data:
                raise ValueError(f"Crew {crew_id} 不存在")
            # 解析并加载...

        logger.info(f"开始执行 Crew: {crew_id}")

        if crew_spec.process_type == "sequential":
            return await self._execute_sequential(crew_spec, inputs)
        elif crew_spec.process_type == "hierarchical":
            return await self._execute_hierarchical(crew_spec, inputs)
        else:
            raise ValueError(f"不支持的流程类型: {crew_spec.process_type}")

    async def _execute_sequential(
        self,
        crew_spec: DistributedCrewSpec,
        inputs: Dict[str, any]
    ) -> Dict[str, any]:
        """顺序执行流程"""

        results = []
        context = inputs.copy()

        for task_spec in crew_spec.tasks:
            # 找到负责这个任务的 Agent
            agent_id = task_spec.get("agent_id")
            if not agent_id:
                # 自动选择
                agent_id = await self._select_agent_for_task(
                    crew_spec,
                    task_spec,
                    context
                )

            logger.info(f"分配任务 {task_spec['description']} 给 Agent {agent_id}")

            # 发送任务给 Agent
            task_message = {
                "type": "crew_task",
                "coordinator_id": f"coordinator-{uuid.uuid4()}",
                "crew_id": crew_spec.crew_id,
                "task_id": str(uuid.uuid4()),
                "task": task_spec,
                "context": context,
                "timestamp": ...  # datetime
            }

            await self.redis.publish(
                f"agent:{agent_id}",
                json.dumps(task_message)
            )

            # 等待结果
            result = await self._wait_for_result(
                task_message["coordinator_id"],
                task_message["task_id"],
                timeout=300
            )

            results.append(result)
            context["previous_output"] = result

        return {
            "final_output": results[-1] if results else None,
            "all_outputs": results
        }

    async def _execute_hierarchical(
        self,
        crew_spec: DistributedCrewSpec,
        inputs: Dict[str, any]
    ) -> Dict[str, any]:
        """层级执行流程（支持委派）"""

        # 找到 Manager Agent
        manager_id = None
        for agent in crew_spec.agents:
            if agent.get("role") == "Manager" or agent.get("is_manager"):
                manager_id = agent["agent_id"]
                break

        if not manager_id:
            # 使用第一个 Agent 作为 manager
            manager_id = crew_spec.agents[0]["agent_id"]

        logger.info(f"使用 Manager Agent: {manager_id}")

        # 发送初始任务给 Manager
        # Manager 会自己决定如何委派给其他 Agent
        task_message = {
            "type": "crew_task",
            "coordinator_id": f"coordinator-{uuid.uuid4()}",
            "crew_id": crew_spec.crew_id,
            "task_id": str(uuid.uuid4()),
            "task": {
                "description": f"完成以下任务，可以委派给团队成员: {inputs}",
                "expected_output": "最终结果"
            },
            "context": {
                "can_delegate": True,
                "crew_members": [
                    agent["agent_id"] for agent in crew_spec.agents
                    if agent["agent_id"] != manager_id
                ]
            },
            "timestamp": ...
        }

        await self.redis.publish(
            f"agent:{manager_id}",
            json.dumps(task_message)
        )

        # 等待最终结果
        result = await self._wait_for_result(
            task_message["coordinator_id"],
            task_message["task_id"],
            timeout=600  # 层级流程可能需要更长时间
        )

        return {"final_output": result}

    async def _select_agent_for_task(
        self,
        crew_spec: DistributedCrewSpec,
        task_spec: Dict[str, any],
        context: Dict[str, any]
    ) -> str:
        """为任务选择最合适的 Agent"""

        # 简化实现：根据任务关键词匹配 Agent 角色
        task_desc = task_spec["description"].lower()

        for agent in crew_spec.agents:
            role = agent.get("role", "").lower()
            # 如果任务描述包含 Agent 角色的关键词
            if role in task_desc:
                return agent["agent_id"]

        # 默认：返回第一个 Agent
        return crew_spec.agents[0]["agent_id"]

    async def _wait_for_result(
        self,
        coordinator_id: str,
        task_id: str,
        timeout: int = 300
    ) -> str:
        """等待任务结果"""

        result_channel = f"crew:result:{coordinator_id}"
        pubsub = self.redis.pubsub()
        await pubsub.subscribe(result_channel)

        start_time = asyncio.get_event_loop().time()

        async for message in pubsub.listen():
            if message['type'] == 'message':
                data = json.loads(message['data'])

                if data.get("task_id") == task_id:
                    await pubsub.unsubscribe(result_channel)
                    return data.get("result", "")

            # 检查超时
            if asyncio.get_event_loop().time() - start_time > timeout:
                await pubsub.unsubscribe(result_channel)
                raise TimeoutError(f"任务 {task_id} 超时")
```

### 3. Agent 容器启动脚本

```python
# distributed_crew/agent_main.py
"""
单个 Agent 容器的启动入口
"""

import asyncio
import os
import sys
from loguru import logger

from distributed_crew.runtime import DistributedCrewRuntime, DistributedAgentConfig
from crewai import Agent


async def main():
    # Agent 配置（从环境变量读取）
    agent_id = os.getenv("AGENT_ID", "agent-unknown")
    crew_id = os.getenv("CREW_ID", "default-crew")
    redis_url = os.getenv("REDIS_URL", "redis://localhost:6379")

    # 创建 CrewAI Agent
    crewai_agent = Agent(
        role=os.getenv("AGENT_ROLE", "Assistant"),
        goal=os.getenv("AGENT_GOAL", "Help with tasks"),
        backstory=os.getenv("AGENT_BACKSTORY", ""),
        verbose=True,
        allow_delegation=os.getenv("ALLOW_DELEGATION", "true").lower() == "true"
    )

    # 创建分布式配置
    config = DistributedAgentConfig(
        agent_id=agent_id,
        crew_id=crew_id,
        role=crewai_agent.role,
        goal=crewai_agent.goal,
        backstory=crewai_agent.backstory,
        delegation_enabled=crewai_agent.allow_delegation
    )

    # 创建分布式 Runtime
    runtime = DistributedCrewRuntime(
        agent_id=agent_id,
        redis_url=redis_url,
        config=config
    )

    # 启动 Runtime
    await runtime.start()

    # 加入 Crew
    if await runtime.join_crew(crew_id):
        logger.info(f"Agent {agent_id} 已加入 Crew {crew_id}")

        # 保持运行
        try:
            while True:
                await asyncio.sleep(1)
        except KeyboardInterrupt:
            logger.info("收到停止信号")
    else:
        logger.error(f"无法加入 Crew {crew_id}")

    await runtime.stop()


if __name__ == "__main__":
    asyncio.run(main())
```

### 4. Docker Compose 配置

```yaml
# docker-compose.distributed-crew.yml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  # Agent A - Receptionist
  agent-a:
    build:
      context: .
      dockerfile: docker/Dockerfile.distributed-agent
    environment:
      - AGENT_ID=receptionist-001
      - CREW_ID=customer-service-crew
      - AGENT_ROLE=Customer Service Representative
      - AGENT_GOAL=Handle customer inquiries
      - AGENT_BACKSTORY=You are a friendly customer service representative
      - ALLOW_DELEGATION=true
      - REDIS_URL=redis://redis:6379
    depends_on:
      - redis
    restart: unless-stopped

  # Agent B - Specialist
  agent-b:
    build:
      context: .
      dockerfile: docker/Dockerfile.distributed-agent
    environment:
      - AGENT_ID=specialist-001
      - CREW_ID=customer-service-crew
      - AGENT_ROLE=Technical Specialist
      - AGENT_GOAL=Handle technical issues
      - AGENT_BACKSTORY=You are a technical expert
      - ALLOW_DELEGATION=false
      - REDIS_URL=redis://redis:6379
    depends_on:
      - redis
    restart: unless-stopped

  # Agent C - Supervisor
  agent-c:
    build:
      context: .
      dockerfile: docker/Dockerfile.distributed-agent
    environment:
      - AGENT_ID=supervisor-001
      - CREW_ID=customer-service-crew
      - AGENT_ROLE=Supervisor
      - AGENT_GOAL=Oversee quality
      - AGENT_BACKSTORY=You are a quality supervisor
      - ALLOW_DELEGATION=false
      - REDIS_URL=redis://redis:6379
    depends_on:
      - redis
    restart: unless-stopped

  # Crew Coordinator (可选)
  crew-coordinator:
    build:
      context: .
      dockerfile: docker/Dockerfile.coordinator
    environment:
      - REDIS_URL=redis://redis:6379
    depends_on:
      - redis
    restart: unless-stopped
```

---

## 工作流程示例

### 场景：Customer Service Crew（分布式）

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    分布式 Crew 执行流程                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. Crew Coordinator 创建 Crew                                         │
│     ├─ 定义 3 个 Agent (receptionist, specialist, supervisor)          │
│     ├─ 定义任务流程                                                    │
│     └─ 将 Crew 信息保存到 Redis                                        │
│                                                                         │
│  2. 三个 Agent 容器启动                                                │
│     ├─ Agent A (receptionist)                                         │
│     ├─ Agent B (specialist)                                           │
│     └─ Agent C (supervisor)                                           │
│     每个启动后调用 runtime.join_crew(crew_id)                          │
│                                                                         │
│  3. 客户请求到来                                                       │
│     └─ Coordinator 收到请求                                            │
│                                                                         │
│  4. Coordinator 分配第一个任务给 Receptionist                          │
│     ├─ 发送消息到 agent:receptionist-001                               │
│     └─ Receptionist 的 Runtime 收到消息                                │
│                                                                         │
│  5. Receptionist 处理请求                                              │
│     ├─ 发现有技术问题                                                  │
│     ├─ 调用 runtime.delegate_task()                                   │
│     ├─ 发送 delegation 消息到 crew:customer-service-crew               │
│     └─ 等待响应                                                        │
│                                                                         │
│  6. Specialist 收到 delegation 消息                                    │
│     ├─ 判断可以处理                                                    │
│     ├─ 发送 delegation_response (accepted: true)                       │
│     ├─ 执行技术分析                                                    │
│     └─ 发送 delegation_result                                          │
│                                                                         │
│  7. Receptionist 收到结果                                              │
│     ├─ 将结果返回给 Coordinator                                        │
│     └─ 任务完成                                                        │
│                                                                         │
│  8. Coordinator 分配下一个任务给 Supervisor (质量检查)                 │
│     └─ 流程继续...                                                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 消息流示例

```python
# 消息 1: Coordinator → Receptionist (分配任务)
{
    "type": "crew_task",
    "coordinator_id": "coord-001",
    "crew_id": "customer-service-crew",
    "task_id": "task-001",
    "task": {
        "description": "处理客户咨询: 云服务上传问题",
        "expected_output": "问题解决方案"
    },
    "context": {"customer_query": "..."}
}

# 消息 2: Receptionist → Crew (委派请求)
{
    "type": "delegation",
    "delegation_id": "delegate-001",
    "source_agent": "receptionist-001",
    "crew_id": "customer-service-crew",
    "task": {
        "description": "技术问题：文件上传超时",
        "context": {...}
    }
}

# 消息 3: Specialist → Receptionist (接受委派)
{
    "type": "delegation_response",
    "delegation_id": "delegate-001",
    "agent_id": "specialist-001",
    "accepted": true
}

# 消息 4: Specialist → Receptionist (委派结果)
{
    "type": "delegation_result",
    "delegation_id": "delegate-001",
    "agent_id": "specialist-001",
    "result": "问题原因是超时设置过短，建议调整配置..."
}

# 消息 5: Receptionist → Coordinator (任务完成)
{
    "type": "task_result",
    "task_id": "task-001",
    "agent_id": "receptionist-001",
    "result": "已为客户解决上传问题..."
}
```

---

## 总结

### 核心组件

| 组件 | 职责 | 部署位置 |
|------|------|----------|
| **DistributedCrewRuntime** | 拦截 CrewAI 调用、转换消息 | 每个 Agent 容器 |
| **CrewCoordinator** | 管理虚拟 Crew、协调执行 | 独立服务（可选） |
| **Redis** | 消息总线、状态存储 | 独立服务 |
| **Agent 容器** | 运行单个 Agent + Runtime | 多个容器 |

### 关键技术点

1. **消息拦截** - Runtime 拦截 CrewAI 的本地调用
2. **协议转换** - 将 Python 调用转换为 Redis 消息
3. **异步等待** - Future 模式等待跨容器响应
4. **虚拟 Crew** - 逻辑上的 Crew，物理上分散

### 与原生 CrewAI 的兼容性

| 特性 | 原生 CrewAI | 分布式 Crew |
|------|-------------|------------|
| Delegation | ✅ 同进程函数调用 | ✅ Redis 消息 |
| Hierarchical Process | ✅ 本地协调 | ✅ Coordinator |
| Sequential Process | ✅ 本地顺序 | ✅ Coordinator |
| Multi-Agent Chat | ❌ 不支持 | ✅ 可扩展 |
| 跨容器 | ❌ 不支持 | ✅ 完全支持 |
| 框架互操作 | ❌ 仅 CrewAI | ✅ 可扩展 |

这个架构允许你：
- 每个容器运行单个 Agent
- 保持 CrewAI 的编程模型
- 实现真正的分布式协作
- 支持动态添加/移除 Agent

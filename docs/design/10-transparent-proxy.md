# 无侵入式分布式 Agent 架构

## 核心原则

1. **不修改开源框架源码** - CrewAI/LangChain 等保持原样
2. **保留原生协商机制** - Agent 的智能决策完全保留
3. **网络透明化** - Agent 感知不到分布式环境

---

## 为什么之前的方案有问题

### 问题 1：需要修改 Agent 代码

```python
# 之前方案的问题
class DistributedCrewRuntime:
    async def execute_task(self, task):
        # ❌ 需要调用新的 API
        if await self._should_delegate(task):
            return await self._delegate_task(task)
        return await self._execute_locally(task)

# 用户需要改写现有 Agent 代码
```

### 问题 2：破坏原生决策机制

```python
# CrewAI 原生的智能决策
agent_a = Agent(
    role="Researcher",
    goal="Conduct research",
    allow_delegation=True,  # ← Agent 自己决定何时委派
    verbose=True
)

# Agent 内部有复杂的逻辑判断：
# - 当前任务是否匹配我的能力？
# - 我的负载如何？
# - 是否有更合适的 Agent？

# 之前的方案破坏了这个决策过程
```

---

## 新方案：网络透明代理

### 核心思想

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      网络透明代理架构                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  Agent 容器 A (原生 CrewAI 代码，完全无修改)                     │  │
│  │                                                                 │  │
│  │  from crewai import Agent, Task, Crew                          │  │
│  │                                                                 │  │
│  │  # 原生代码，无需修改                                          │  │
│  │  agent_a = Agent(                                             │  │
│  │      role="Manager",                                          │  │
│  │      goal="Oversee the project",                              │  │
│  │      backstory="...",                                         │  │
│  │      allow_delegation=True  # ← 原生委派能力                  │  │
│  │  )                                                            │  │
│  │                                                                 │  │
│  │  # 运行时创建 Crew（可能包含远程 Agent）                      │  │
│  │  crew = Crew(                                                  │  │
│  │      agents=[agent_a, remote_agent_b, remote_agent_c],       │  │
│  │      tasks=[task1, task2, task3]                              │  │
│  │  )                                                            │  │
│  │                                                                 │  │
│  │  crew.kickoff()  # ← 与原生完全一致                           │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                │                                     │
│                                │ Python 方法调用                      │
│                                ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  透明代理层 (Transparent Proxy Layer)                          │  │
│  │                                                                 │  │
│  │  拦截 Python 调用：                                            │  │
│  │  - agent_b.delegate(task)  →  远程调用代理                      │  │
│  │  - agent_b.execute()      →  远程调用代理                      │  │
│  │  - agent_b.tools         →  远程属性代理                       │  │
│  │                                                                 │  │
│  │  对 Agent 完全透明！                                            │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                │                                     │
│                                │ Redis 消息                         │
│                                ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  Agent 容器 B (原生 CrewAI 代码，完全无修改)                     │  │
│  │                                                                 │  │
│  │  agent_b = Agent(                                             │  │
│  │      role="Specialist",                                       │  │
│  │      goal="Handle technical tasks",                           │  │
│  │      allow_delegation=False                                   │  │
│  │  )                                                            │  │
│  │                                                                 │  │
│  │  # agent_b 根本不知道自己被远程调用！                          │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 核心实现

### 1. 透明代理工厂

```python
# transparent_proxy/proxy_factory.py
"""
透明代理工厂
创建远程 Agent 的本地代理，对调用者完全透明
"""

import uuid
import json
import asyncio
from typing import Any, Callable, Optional
import redis.asyncio as redis
from loguru import logger


class RemoteAgentProxy:
    """
    远程 Agent 代理

    看起来像本地 Agent，实际调用转发到远程容器
    """

    def __init__(
        self,
        agent_id: str,
        redis_url: str,
        timeout: int = 300
    ):
        self.agent_id = agent_id
        self.redis_url = redis_url
        self.timeout = timeout

        # 代理 Agent 的属性（延迟加载）
        self._attributes = None

    def __getattr__(self, name: str) -> Any:
        """
        拦截属性访问

        当访问 agent_b.role 时，实际返回远程 Agent 的 role
        """
        if self._attributes and name in self._attributes:
            return self._attributes[name]

        # 从远程获取属性
        attributes = asyncio.run(self._get_remote_attributes())
        if name in attributes:
            return attributes[name]

        # 返回一个可调用代理（假设是方法）
        return RemoteMethodProxy(self.agent_id, name, self.redis_url)

    async def _get_remote_attributes(self) -> dict:
        """从远程获取 Agent 属性"""
        if self._attributes:
            return self._attributes

        r = redis.from_url(self.redis_url)

        # 请求远程 Agent 的属性
        request = {
            "type": "get_attributes",
            "source_agent": "proxy",
            "target_agent": self.agent_id,
            "request_id": str(uuid.uuid4())
        }

        # 发送并等待响应
        await r.publish(f"agent:{self.agent_id}", json.dumps(request))

        # 订阅响应频道
        response_channel = f"agent_response:{self.agent_id}:{request['request_id']}"
        pubsub = r.pubsub()
        await pubsub.subscribe(response_channel)

        async for msg in pubsub.listen():
            if msg['type'] == 'message':
                data = json.loads(msg['data'])
                self._attributes = data['attributes']
                await pubsub.unsubscribe(response_channel)
                await r.close()
                return self._attributes

        await r.close()
        return {}

    def delegate(self, task: Any, context: Optional[dict] = None):
        """
        委派任务到远程 Agent

        这个方法会被 CrewAI 调用！
        """
        logger.info(f"[Proxy] 委派任务到远程 Agent {self.agent_id}")

        # 转发到远程
        result = asyncio.run(self._remote_execute(
            method="delegate",
            args=[task],
            kwargs={"context": context} if context else {}
        ))

        return result

    def execute_task(self, task: Any, context: Optional[dict] = None):
        """
        执行任务

        这个方法也会被 CrewAI 调用！
        """
        logger.info(f"[Proxy] 执行任务到远程 Agent {self.agent_id}")

        return asyncio.run(self._remote_execute(
            method="execute_task",
            args=[task],
            kwargs={"context": context} if context else {}
        ))

    async def _remote_execute(self, method: str, args: list, kwargs: dict) -> Any:
        """远程方法调用"""
        r = redis.from_url(self.redis_url)
        request_id = str(uuid.uuid4())

        request = {
            "type": "method_call",
            "source_agent": "proxy",
            "target_agent": self.agent_id,
            "method": method,
            "args": self._serialize_args(args),
            "kwargs": self._serialize_args(kwargs),
            "request_id": request_id
        }

        # 发送请求
        await r.publish(f"agent:{self.agent_id}", json.dumps(request))

        # 等待响应
        response_channel = f"agent_response:{self.agent_id}:{request_id}"
        pubsub = r.pubsub()
        await pubsub.subscribe(response_channel)

        start_time = asyncio.get_event_loop().time()

        async for msg in pubsub.listen():
            if msg['type'] == 'message':
                data = json.loads(msg['data'])
                await pubsub.unsubscribe(response_channel)
                await r.close()
                return self._deserialize_result(data['result'])

            # 超时检查
            if asyncio.get_event_loop().time() - start_time > self.timeout:
                await pubsub.unsubscribe(response_channel)
                await r.close()
                raise TimeoutError(f"远程调用超时: {method}")

        await r.close()
        raise Exception("没有收到响应")

    def _serialize_args(self, args) -> Any:
        """序列化参数"""
        # 简化实现：处理 CrewAI 的 Task 对象
        if isinstance(args, list) and len(args) > 0:
            if hasattr(args[0], 'description'):
                return [{
                    "description": args[0].description,
                    "expected_output": args[0].expected_output
                }]
        return args

    def _deserialize_result(self, result: Any) -> Any:
        """反序列化结果"""
        # 简化实现
        return result


class RemoteMethodProxy:
    """远程方法代理"""

    def __init__(self, agent_id: str, method_name: str, redis_url: str):
        self.agent_id = agent_id
        self.method_name = method_name
        self.redis_url = redis_url

    def __call__(self, *args, **kwargs):
        """调用远程方法"""
        proxy = RemoteAgentProxy(self.agent_id, self.redis_url)
        return asyncio.run(proxy._remote_execute(
            self.method_name,
            list(args),
            kwargs
        ))


def create_remote_agent(
    agent_id: str,
    redis_url: str = "redis://localhost:6379"
) -> RemoteAgentProxy:
    """
    创建远程 Agent 的本地代理

    使用示例：
        agent_b = create_remote_agent("specialist-001")
        # agent_b 现在可以像本地 Agent 一样使用！
        assert agent_b.role == "Specialist"
        agent_b.delegate(task)  # 转发到远程
    """
    return RemoteAgentProxy(agent_id, redis_url)
```

### 2. 远程 Agent 服务器（无侵入）

```python
# transparent_proxy/agent_server.py
"""
远程 Agent 服务器

在 Agent 容器中运行，接收远程调用请求
不修改 Agent 代码，只提供调用接口
"""

import asyncio
import json
import uuid
from typing import Any
import redis.asyncio as redis
from loguru import logger


class AgentServer:
    """
    Agent 服务器

    接收远程调用请求，转发给本地 Agent
    """

    def __init__(
        self,
        agent_id: str,
        agent: Any,  # 原生 CrewAI Agent 对象
        redis_url: str
    ):
        self.agent_id = agent_id
        self.agent = agent  # 原生 Agent，不修改
        self.redis_url = redis_url
        self.running = False

    async def start(self):
        """启动服务器"""
        self.running = True

        r = redis.from_url(self.redis_url)
        pubsub = r.pubsub()

        # 订阅 Agent 的私有频道
        await pubsub.subscribe(f"agent:{self.agent_id}")

        logger.info(f"[{self.agent_id}] Agent Server 启动")

        async for message in pubsub.listen():
            if not self.running:
                break

            if message['type'] == 'message':
                try:
                    request = json.loads(message['data'])
                    await self._handle_request(r, request)
                except Exception as e:
                    logger.error(f"处理请求失败: {e}")

        await r.close()

    async def _handle_request(self, r: redis, request: dict):
        """处理请求"""
        request_type = request.get("type")
        request_id = request.get("request_id")

        if request_type == "get_attributes":
            # 返回 Agent 属性
            response = {
                "type": "attributes_response",
                "agent_id": self.agent_id,
                "request_id": request_id,
                "attributes": {
                    "role": self.agent.role,
                    "goal": self.agent.goal,
                    "backstory": self.agent.backstory,
                    "allow_delegation": self.agent.allow_delegation,
                    "verbose": self.agent.verbose,
                }
            }

        elif request_type == "method_call":
            # 调用 Agent 方法
            method = request.get("method")
            args = request.get("args", [])
            kwargs = request.get("kwargs", {})

            logger.info(f"[{self.agent_id}] 调用方法: {method}")

            # 调用原生 Agent 的方法（不修改！）
            if method == "delegate":
                result = await self._handle_delegate(args, kwargs)
            elif method == "execute_task":
                result = await self._handle_execute(args, kwargs)
            else:
                # 动态调用
                result = getattr(self.agent, method)(*args, **kwargs)

            response = {
                "type": "method_response",
                "agent_id": self.agent_id,
                "request_id": request_id,
                "result": self._serialize_result(result)
            }

        else:
            response = {
                "type": "error",
                "agent_id": self.agent_id,
                "request_id": request_id,
                "error": f"未知请求类型: {request_type}"
            }

        # 发送响应
        response_channel = f"agent_response:{self.agent_id}:{request_id}"
        await r.publish(response_channel, json.dumps(response))

    async def _handle_delegate(self, args: list, kwargs: dict) -> Any:
        """
        处理委派请求

        关键：调用原生 Agent 的 delegate 方法！
        """
        # 从参数中恢复 Task 对象（简化）
        task = args[0] if args else None

        if task and isinstance(task, dict):
            # 重建 Task 对象
            from crewai import Task
            task = Task(
                description=task.get("description", ""),
                expected_output=task.get("expected_output", "")
            )

        # 调用原生 Agent 的 delegate 方法
        # 这会触发 CrewAI 原生的决策逻辑！
        if hasattr(self.agent, 'delegate'):
            result = self.agent.delegate(task, **kwargs)
        else:
            # 如果没有 delegate 方法，直接执行
            result = await self._execute_with_llm(task)

        return result

    async def _handle_execute(self, args: list, kwargs: dict) -> Any:
        """
        处理执行任务请求
        """
        task = args[0] if args else None

        if task and isinstance(task, dict):
            from crewai import Task
            task = Task(
                description=task.get("description", ""),
                expected_output=task.get("expected_output", "")
            )

        # 执行任务（调用原生 Agent 的 LLM）
        result = await self._execute_with_llm(task)
        return result

    async def _execute_with_llm(self, task) -> str:
        """
        使用 Agent 的 LLM 执行任务

        这里调用 CrewAI 原生的执行逻辑
        """
        # 简化实现：实际会调用 Agent 的 LLM
        logger.info(f"[{self.agent_id}] 执行任务: {task.description}")

        # 这里应该调用 CrewAI Agent 的实际执行逻辑
        # 比如：self.agent.llm.call(...)
        # 简化示例：
        return f"Agent {self.agent_id} 完成了: {task.description}"

    def _serialize_result(self, result: Any) -> Any:
        """序列化结果"""
        if hasattr(result, 'raw'):
            return str(result.raw)
        return str(result)

    def stop(self):
        """停止服务器"""
        self.running = False
```

### 3. Agent 容器主入口（无侵入）

```python
# transparent_proxy/container_main.py
"""
Agent 容器的主入口

完全不需要修改用户现有的 Agent 代码！
"""

import asyncio
import os
import sys
from loguru import logger

from transparent_proxy.agent_server import AgentServer
from transparent_proxy.proxy_factory import create_remote_agent


async def main():
    # Agent 配置
    agent_id = os.getenv("AGENT_ID", "agent-001")
    redis_url = os.getenv("REDIS_URL", "redis://localhost:6379")

    # ============================================================
    #  用户代码区域 - 完全不需要修改！
    # ============================================================

    # 导入 CrewAI（原生）
    from crewai import Agent, Task, Crew

    # 创建 Agent（原生代码）
    agent = Agent(
        role=os.getenv("AGENT_ROLE", "Assistant"),
        goal=os.getenv("AGENT_GOAL", "Help with tasks"),
        backstory=os.getenv("AGENT_BACKSTORY", ""),
        verbose=True,
        allow_delegation=os.getenv("ALLOW_DELEGATION", "true").lower() == "true"
    )

    # ============================================================
    #  框架层 - 启动服务器（不修改 Agent 代码）
    # ============================================================

    # 启动 Agent Server（在后台运行）
    server = AgentServer(agent_id, agent, redis_url)

    # 在后台任务中运行服务器
    server_task = asyncio.create_task(server.start())

    logger.info(f"Agent {agent_id} ({agent.role}) 已启动")

    # 如果这个 Agent 是 Manager，还需要创建 Crew
    if os.getenv("IS_MANAGER", "false").lower() == "true":
        # 创建远程 Agent 的代理
        remote_agent_b = create_remote_agent(
            os.getenv("REMOTE_AGENT_B", "specialist-001"),
            redis_url
        )
        remote_agent_c = create_remote_agent(
            os.getenv("REMOTE_AGENT_C", "supervisor-001"),
            redis_url
        )

        # 创建 Crew（使用远程代理）
        # 注意：这里看起来和原生 CrewAI 完全一样！
        crew = Crew(
            agents=[agent, remote_agent_b, remote_agent_c],
            tasks=[...],
            process=Process.hierarchical  # 保留原生流程
        )

        # 执行 Crew（原生调用）
        result = crew.kickoff(inputs={...})
        logger.info(f"Crew 执行完成: {result}")

    # 保持运行
    try:
        await server_task
    except asyncio.CancelledError:
        logger.info("Agent 停止")
        server.stop()


if __name__ == "__main__":
    asyncio.run(main())
```

### 4. 用户代码示例（完全无修改）

```python
# user_code/customer_service_crew.py
"""
用户代码 - 完全不需要修改！
"""

from crewai import Agent, Task, Crew, Process

# 创建 Agent（原生代码）
manager = Agent(
    role="Customer Service Manager",
    goal="Handle customer inquiries and delegate when needed",
    backstory="You are a friendly manager...",
    allow_delegation=True,  # ← 保留原生委派能力
    verbose=True
)

# 如果要远程部署，只需要在启动时指定
# 不需要修改这里的代码！

def create_crew():
    """创建 Crew"""
    return Crew(
        agents=[manager, specialist, supervisor],
        tasks=[task1, task2, task3],
        process=Process.hierarchical  # ← 保留原生流程
    )

if __name__ == "__main__":
    crew = create_crew()
    result = crew.kickoff(inputs={...})
    print(result)
```

---

## 原生协商机制如何保留

### CrewAI 的决策流程（完全保留）

```
┌─────────────────────────────────────────────────────────────────────────┐
│                  CrewAI 原生决策流程（完整保留）                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Agent A (Manager)                                                     │
│      │                                                                 │
│      │ 收到任务: "客户询问技术问题"                                     │
│      │                                                                 │
│      ▼                                                                 │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  CrewAI 内部决策逻辑（原生，未修改）                            │  │
│  │                                                                 │  │
│  │  1. 分析任务                                                    │  │
│  │     - 这是一个技术问题                                          │  │
│  │     - 超出我的能力范围？                                        │  │
│  │                                                                 │  │
│  │  2. 检查可用 Agent                                             │  │
│  │     - Agent B: Specialist (技术专家)                           │  │
│  │     - Agent C: Supervisor (质量检查)                           │  │
│  │                                                                 │  │
│  │  3. 决策（LLM 驱动）                                           │  │
│  │     - 应该委派给 Agent B                                       │  │
│  │     - 为什么？因为他是技术专家                                 │  │
│  │                                                                 │  │
│  │  4. 执行委派                                                    │  │
│  │     manager.delegate(task, delegate_to=agent_b)                │  │
│  │                                                                 │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│      │                                                                 │
│      │ 委派调用                                                      │
│      ▼                                                                 │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  透明代理层（网络透明）                                          │  │
│  │                                                                 │  │
│  │  agent_b.delegate(task)                                        │  │
│  │       │                                                         │  │
│  │       │ RemoteAgentProxy 拦截                                   │  │
│  │       │                                                         │  │
│  │       ▼                                                         │  │
│  │  发送 Redis 消息到 Agent B 的容器                               │  │
│  │                                                                 │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│      │                                                                 │
│      │ Redis 消息                                                    │
│      ▼                                                                 │
│  Agent B (Specialist) - 在另一个容器                                  │
│      │                                                                 │
│      │ Agent Server 接收消息                                          │
│      │                                                                 │
│      ▼                                                                 │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  Agent B 内部决策逻辑（原生，未修改）                            │  │
│  │                                                                 │  │
│  │  1. 分析任务                                                    │  │
│  │     - 这是一个技术问题                                          │  │
│  │     - 在我的能力范围内 ✓                                        │  │
│  │                                                                 │  │
│  │  2. 决策（LLM 驱动）                                           │  │
│  │     - 我可以处理这个问题                                       │  │
│  │     - 需要调用什么工具？                                        │  │
│  │                                                                 │  │
│  │  3. 执行任务                                                    │  │
│  │     - 使用 LLM 分析问题                                         │  │
│  │     - 调用相关工具                                             │  │
│  │     - 生成解决方案                                              │  │
│  │                                                                 │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│      │                                                                 │
│      │ 返回结果                                                        │
│      ▼                                                                 │
│  Agent A 收到结果，继续处理...                                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 关键点

| 方面 | 原生 CrewAI | 透明代理方案 |
|------|-------------|-------------|
| **Agent 定义** | `Agent(allow_delegation=True)` | ✅ 完全相同 |
| **决策逻辑** | LLM 驱动 | ✅ 完全保留 |
| **委派机制** | `agent.delegate()` | ✅ 透明转发 |
| **工具调用** | `agent.tools` | ✅ 透明代理 |
| **执行流程** | `Process.hierarchical` | ✅ 完全保留 |

---

## 完整示例：Customer Service Crew

### Docker Compose 配置

```yaml
# docker-compose.transparent.yml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  # Manager Agent
  agent-manager:
    build:
      context: .
      dockerfile: docker/Dockerfile.agent
    environment:
      - AGENT_ID=manager-001
      - AGENT_ROLE=Customer Service Manager
      - AGENT_GOAL=Handle customer inquiries and delegate technical issues
      - AGENT_BACKSTORY=You are a friendly manager...
      - ALLOW_DELEGATION=true
      - IS_MANAGER=true  # 这个 Agent 会创建 Crew
      - REMOTE_AGENT_B=specialist-001  # 远程 Agent ID
      - REMOTE_AGENT_C=supervisor-001
      - REDIS_URL=redis://redis:6379
    depends_on:
      - redis
    restart: unless-stopped

  # Specialist Agent
  agent-specialist:
    build:
      context: .
      dockerfile: docker/Dockerfile.agent
    environment:
      - AGENT_ID=specialist-001
      - AGENT_ROLE=Technical Specialist
      - AGENT_GOAL=Handle technical issues
      - AGENT_BACKSTORY=You are a technical expert...
      - ALLOW_DELEGATION=false
      - IS_MANAGER=false
      - REDIS_URL=redis://redis:6379
    depends_on:
      - redis
    restart: unless-stopped

  # Supervisor Agent
  agent-supervisor:
    build:
      context: .
      dockerfile: docker/Dockerfile.agent
    environment:
      - AGENT_ID=supervisor-001
      - AGENT_ROLE=Quality Supervisor
      - AGENT_GOAL=Ensure quality of responses
      - AGENT_BACKSTORY=You are a quality supervisor...
      - ALLOW_DELEGATION=false
      - IS_MANAGER=false
      - REDIS_URL=redis://redis:6379
    depends_on:
      - redis
    restart: unless-stopped
```

---

## 对比：新方案 vs 旧方案

| 方面 | 旧方案（DistributedCrewRuntime） | 新方案（透明代理） |
|------|--------------------------------|-------------------|
| **修改 Agent 代码** | ❌ 需要改写 | ✅ 完全不需要 |
| **保留原生决策** | ⚠️ 部分保留 | ✅ 完全保留 |
| **框架兼容性** | ⚠️ 需要适配 | ✅ 完全兼容 |
| **开发成本** | 高 | 低 |
| **维护成本** | 高（升级需改代码） | 低（升级无影响） |
| **网络透明** | ❌ Agent 感知网络 | ✅ 完全透明 |
| **原生 API** | ❌ 需要学习新 API | ✅ 使用原生 API |

---

## 总结

### 核心优势

1. **零侵入** - 不修改任何开源框架代码
2. **保留智能** - Agent 的原生决策逻辑完全保留
3. **透明代理** - Agent 感知不到分布式环境
4. **渐进迁移** - 可以逐步从单体迁移到分布式

### 技术实现

- **Python 动态代理** - 拦截方法调用
- **Redis 消息传递** - 转发到远程容器
- **异步等待** - Future 模式等待响应
- **序列化/反序列化** - 传递 CrewAI 对象

### 用户代码变化

```python
# 迁移前（单体）
from crewai import Agent, Crew

agent_a = Agent(role="Manager", allow_delegation=True)
agent_b = Agent(role="Specialist")
crew = Crew(agents=[agent_a, agent_b], tasks=[...])
crew.kickoff()

# 迁移后（分布式）
# 代码完全一样！只需要：
# 1. 把 agent_b 放到另一个容器
# 2. 在启动时指定 IS_MANAGER=true 和 REMOTE_AGENT_B
# 3. agent_b = create_remote_agent("specialist-001")

from crewai import Agent, Crew
from transparent_proxy import create_remote_agent

agent_a = Agent(role="Manager", allow_delegation=True)
agent_b = create_remote_agent("specialist-001")  # ← 只改这一行！
crew = Crew(agents=[agent_a, agent_b], tasks=[...])
crew.kickoff()  # 完全一样！
```

### 最小改动原则

- **用户代码**：只改一行（创建远程代理）
- **框架代码**：完全不修改
- **Agent 定义**：完全不修改
- **执行流程**：完全不修改

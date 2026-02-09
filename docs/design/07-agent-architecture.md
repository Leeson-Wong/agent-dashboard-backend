# Agent 通用架构设计方案

本文档回答三个核心问题：
1. Agent 最通用的运行形态
2. 如何常驻并接收消息
3. Agent 之间如何交互

---

## 问题分析：当前架构的局限性

### 当前的一次性执行模式

```
┌─────────────────────────────────────────────────────────────┐
│  当前模式 (story_writer_crew)                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  python main.py                                             │
│       │                                                     │
│       ▼                                                     │
│  创建 Crew → kickoff(inputs) → 执行任务 → 返回结果 → 退出   │
│       │                                                     │
│       ▼                                                     │
│  进程结束                                                   │
│                                                             │
│  ❌ 无法持续接收任务                                         │
│  ❌ 无法与其他 Agent 交互                                    │
│  ❌ 无法水平扩展                                            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 问题 1：Agent 最通用的运行形态

### 解决方案：Docker + 消息队列架构

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        Agent 运行时架构                                   │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐       │
│  │  Docker     │         │  Docker     │         │  Docker     │       │
│  │  Container  │         │  Container  │         │  Container  │       │
│  │             │         │             │         │             │       │
│  │ ┌─────────┐ │         │ ┌─────────┐ │         │ ┌─────────┐ │       │
│  │ │Python   │ │         │ │Node.js  │ │         │ │  Java   │ │       │
│  │ │Agent    │ │         │ │Agent    │ │         │ │Agent    │ │       │
│  │ │+ Plugin │ │         │ │+ Plugin │ │         │ │+ Plugin │ │       │
│  │ └────┬────┘ │         │ └────┬────┘ │         │ └────┬────┘ │       │
│  └──────┼──────┘         └──────┼──────┘         └──────┼──────┘       │
│         │                       │                       │              │
│         └───────────────────────┼───────────────────────┘              │
│                                 ▼                                      │
│                    ┌─────────────────────┐                             │
│                    │   Message Broker    │                             │
│                    │   (Redis/RabbitMQ)  │                             │
│                    │                     │                             │
│                    │  agent:tasks        │ ← 任务队列                   │
│                    │  agent:events      │ ← 事件分发                   │
│                    │  agent:response    │ ← 响应队列                   │
│                    └─────────────────────┘                             │
│                                 │                                      │
│                                 ▼                                      │
│                    ┌─────────────────────┐                             │
│                    │   Monitor Backend   │                             │
│                    │   (Dashboard)       │                             │
│                    └─────────────────────┘                             │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### Docker 镜像分层设计

```
# 通用基础镜像
FROM python:3.11-slim

# 安装监控插件（所有 Agent 共享）
COPY agent-monitor-plugin/ /usr/local/lib/python3.11/site-packages/agent_monitor/
RUN pip install agent-monitor-plugin

# 框架特定层（选择其一）
# CrewAI
RUN pip install crewai

# 或 LangChain
# RUN pip install langchain langchain-openai

# 或 LangGraph
# RUN pip install langgraph

# Agent 代码层
COPY agent_code/ /app/agent/
WORKDIR /app/agent

# 环境变量注入
ENV AGENT_MONITOR_ENABLED=true
ENV AGENT_MONITOR_URL=http://monitor-backend:8080
ENV AGENT_MESSAGE_BROKER=redis://redis:6379
ENV AGENT_ID=${AGENT_ID}

# 启动命令
CMD ["python", "-m", "agent_runtime.server"]
```

### 镜像变体策略

| 镜像名称 | 包含内容 | 大小 | 用途 |
|---------|---------|------|------|
| `agent-base:python` | Python + 监控插件 | ~200MB | 所有 Python Agent |
| `agent-crewai:latest` | base + CrewAI | ~350MB | CrewAI Agent |
| `agent-langchain:latest` | base + LangChain | ~380MB | LangChain Agent |
| `agent-langgraph:latest` | base + LangGraph | ~390MB | LangGraph Agent |
| `agent-base:node` | Node.js + 监控插件 | ~180MB | 所有 JS/TS Agent |
| `agent-base:java` | Java + 监控插件 | ~250MB | 所有 Java Agent |

---

## 问题 2：常驻进程 + 消息接收

### Agent 运行时服务层

创建一个通用的 Agent Runtime，包装任何 Agent 实现：

```python
# agent_runtime/server.py
"""
Agent Runtime Server - 通用 Agent 运行时
支持任何框架的 Agent 作为常驻服务运行
"""

import os
import json
import asyncio
import signal
from typing import Optional, Any
from dataclasses import dataclass
from datetime import datetime

import redis.asyncio as redis
from loguru import logger

# Agent 框架适配器
from agent_runtime.adapters.base import AgentAdapter
from agent_runtime.adapters.crewai_adapter import CrewAIAdapter
from agent_runtime.adapters.langchain_adapter import LangChainAdapter


@dataclass
class AgentMessage:
    """Agent 消息协议（通用）"""
    id: str
    type: str  # 'task', 'event', 'query', 'control'
    source: str  # 发送者 agent_id
    target: str  # 接收者 agent_id (或 'broadcast')
    timestamp: str
    payload: dict
    reply_to: Optional[str] = None  # 用于响应


class AgentRuntime:
    """Agent 运行时 - 使任何 Agent 成为常驻服务"""

    def __init__(self):
        self.agent_id = os.getenv("AGENT_ID", f"agent-{os.getpid()}")
        self.redis_url = os.getenv("AGENT_MESSAGE_BROKER", "redis://localhost:6379")
        self.monitor_url = os.getenv("AGENT_MONITOR_URL")

        # 任务队列
        self.task_queue = f"agent:tasks:{self.agent_id}"
        # 事件订阅频道
        self.event_channel = "agent:events"
        # 响应队列
        self.response_queue = f"agent:response:{self.agent_id}"

        # 框架适配器
        self.adapter: Optional[AgentAdapter] = None
        self.running = False

        # Redis 连接
        self.redis: Optional[redis.Redis] = None

    async def initialize(self):
        """初始化运行时"""
        logger.info(f"[{self.agent_id}] 初始化 Agent Runtime")

        # 连接 Redis
        self.redis = redis.from_url(self.redis_url)

        # 检测并加载 Agent 框架
        self.adapter = self._detect_adapter()
        if not self.adapter:
            raise RuntimeError("无法检测到 Agent 框架")

        logger.info(f"[{self.agent_id}] 使用适配器: {self.adapter.__class__.__name__}")

        # 初始化适配器
        await self.adapter.initialize()

        # 注册 Agent（发送上线事件）
        await self._register_agent()

    def _detect_adapter(self) -> Optional[AgentAdapter]:
        """自动检测 Agent 框架"""
        # 检查 CrewAI
        try:
            from crewai import Agent, Crew
            # 检查当前目录是否有 Crew 定义
            if os.path.exists("crew.py"):
                return CrewAIAdapter(self.agent_id)
        except ImportError:
            pass

        # 检查 LangChain
        try:
            from langchain.agents import AgentExecutor
            if os.path.exists("langchain_agent.py"):
                return LangChainAdapter(self.agent_id)
        except ImportError:
            pass

        # 添加更多框架检测...
        return None

    async def _register_agent(self):
        """向监控系统注册 Agent"""
        registration = {
            "agent_id": self.agent_id,
            "server_id": os.getenv("AGENT_SERVER_ID", "local"),
            "framework": self.adapter.framework_name,
            "language": "python",
            "status": "online",
            "capabilities": self.adapter.get_capabilities(),
            "registered_at": datetime.utcnow().isoformat()
        }

        # 发送到监控后端
        # 同时广播到事件频道
        await self.redis.publish(
            self.event_channel,
            json.dumps({
                "type": "agent_registered",
                "data": registration
            })
        )

        logger.info(f"[{self.agent_id}] Agent 已注册")

    async def start(self):
        """启动运行时"""
        self.running = True

        logger.info(f"[{self.agent_id}] Agent Runtime 启动")

        # 并发运行任务监听和事件监听
        await asyncio.gather(
            self._task_listener(),
            self._event_listener(),
            self._heartbeat_loop()
        )

    async def stop(self):
        """停止运行时"""
        logger.info(f"[{self.agent_id}] 正在停止...")
        self.running = False

        # 注销 Agent
        await self._unregister_agent()

        # 关闭连接
        if self.redis:
            await self.redis.close()

        # 清理适配器
        if self.adapter:
            await self.adapter.cleanup()

    async def _unregister_agent(self):
        """注销 Agent"""
        await self.redis.publish(
            self.event_channel,
            json.dumps({
                "type": "agent_unregistered",
                "data": {
                    "agent_id": self.agent_id,
                    "unregistered_at": datetime.utcnow().isoformat()
                }
            })
        )

    async def _task_listener(self):
        """监听任务队列"""
        logger.info(f"[{self.agent_id}] 任务监听器启动: {self.task_queue}")

        while self.running:
            try:
                # 阻塞式读取任务
                message = await self.redis.blpop(self.task_queue, timeout=5)

                if message:
                    _, data = message
                    task_msg = json.loads(data)

                    logger.info(f"[{self.agent_id}] 收到任务: {task_msg['id']}")

                    # 处理任务
                    await self._handle_task(task_msg)

            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"[{self.agent_id}] 任务处理错误: {e}")

    async def _event_listener(self):
        """监听事件频道"""
        logger.info(f"[{self.agent_id}] 事件监听器启动")

        pubsub = self.redis.pubsub()
        await pubsub.subscribe(self.event_channel)

        async for message in pubsub.listen():
            if not self.running:
                break

            if message['type'] == 'message':
                event = json.loads(message['data'])

                # 过滤自己的事件
                if event.get('data', {}).get('agent_id') == self.agent_id:
                    continue

                await self._handle_event(event)

    async def _handle_task(self, message: AgentMessage):
        """处理收到的任务"""
        try:
            # 发送任务开始事件
            await self._emit_event("task_started", {
                "task_id": message['id'],
                "task_type": message['payload'].get('type')
            })

            # 使用适配器执行任务
            result = await self.adapter.execute(message['payload'])

            # 发送响应
            response = AgentMessage(
                id=message['id'],
                type='task_response',
                source=self.agent_id,
                target=message['source'],
                timestamp=datetime.utcnow().isoformat(),
                payload={
                    "success": True,
                    "result": result
                },
                reply_to=message['id']
            )

            await self.redis.rpush(
                message['reply_to'] or self.response_queue,
                json.dumps(response)
            )

            # 发送任务完成事件
            await self._emit_event("task_completed", {
                "task_id": message['id'],
                "result_summary": str(result)[:200]
            })

        except Exception as e:
            logger.error(f"[{self.agent_id}] 任务执行失败: {e}")

            # 发送错误响应
            error_response = AgentMessage(
                id=message['id'],
                type='task_error',
                source=self.agent_id,
                target=message['source'],
                timestamp=datetime.utcnow().isoformat(),
                payload={
                    "success": False,
                    "error": str(e)
                },
                reply_to=message['id']
            )

            await self.redis.rpush(
                message['reply_to'] or self.response_queue,
                json.dumps(error_response)
            )

    async def _handle_event(self, event: dict):
        """处理收到的事件"""
        event_type = event.get('type')
        event_data = event.get('data', {})

        logger.debug(f"[{self.agent_id}] 收到事件: {event_type}")

        # 转发给适配器
        await self.adapter.handle_event(event_type, event_data)

    async def _emit_event(self, event_type: str, data: dict):
        """发送事件"""
        event = {
            "type": event_type,
            "data": {
                "agent_id": self.agent_id,
                "timestamp": datetime.utcnow().isoformat(),
                **data
            }
        }

        await self.redis.publish(self.event_channel, json.dumps(event))

    async def _heartbeat_loop(self):
        """心跳循环"""
        while self.running:
            await self._emit_event("heartbeat", {
                "status": "online",
                "uptime": "..."  # 可以计算运行时间
            })
            await asyncio.sleep(30)


# 信号处理
runtime: Optional[AgentRuntime] = None


async def main():
    """主函数"""
    global runtime

    runtime = AgentRuntime()
    await runtime.initialize()

    # 启动运行时
    try:
        await runtime.start()
    except asyncio.CancelledError:
        logger.info("收到取消信号")
    finally:
        await runtime.stop()


def signal_handler(signum, frame):
    """信号处理器"""
    logger.info(f"收到信号 {signum}")
    if runtime:
        # 触发停止
        runtime.running = False


if __name__ == "__main__":
    # 注册信号处理
    signal.signal(signal.SIGTERM, signal_handler)
    signal.signal(signal.SIGINT, signal_handler)

    # 运行
    asyncio.run(main())
```

### 框架适配器示例

```python
# agent_runtime/adapters/crewai_adapter.py
"""
CrewAI 框架适配器
将一次性执行的 CrewAI Agent 包装为常驻服务
"""

import sys
import os
from typing import Any, Dict
from loguru import logger

from .base import AgentAdapter


class CrewAIAdapter(AgentAdapter):
    """CrewAI 适配器"""

    def __init__(self, agent_id: str):
        super().__init__(agent_id)
        self.framework_name = "crewai"
        self.crew = None
        self.agents = []

    async def initialize(self):
        """初始化 CrewAI Agent"""
        # 动态导入 Crew 定义
        sys.path.insert(0, os.getcwd())
        from crew import MyCrew

        crew_instance = MyCrew()
        self.crew = crew_instance.crew()
        self.agents = crew_instance.agents

        logger.info(f"[{self.agent_id}] CrewAI 适配器初始化完成")
        logger.info(f"[{self.agent_id}] 已加载 {len(self.agents)} 个 Agent")

    async def execute(self, task_payload: dict) -> Any:
        """执行任务"""
        inputs = task_payload.get('inputs', {})
        task_description = task_payload.get('task', 'Execute task')

        logger.info(f"[{self.agent_id}] 执行任务: {task_description}")

        # 执行 Crew
        result = await self.crew.kickoff_async(inputs=inputs)

        return {
            "output": str(result.raw),
            "raw": result.raw,
            "token_usage": result.token_usage.dict() if hasattr(result, 'token_usage') else {}
        }

    async def handle_event(self, event_type: str, event_data: dict):
        """处理事件"""
        # CrewAI 特定的事件处理
        if event_type == "agent_query":
            # 可以让特定 Agent 处理查询
            pass

    def get_capabilities(self) -> dict:
        """返回 Agent 能力"""
        return {
            "framework": "crewai",
            "agent_count": len(self.agents),
            "agents": [
                {
                    "role": agent.role,
                    "goal": agent.goal,
                    "verbose": agent.verbose
                }
                for agent in self.agents
            ]
        }

    async def cleanup(self):
        """清理资源"""
        logger.info(f"[{self.agent_id}] CrewAI 适配器清理")
```

---

## 问题 3：Agent 之间通用交互

### 统一消息协议

```python
# agent_runtime/protocol/messages.py
"""
Agent 之间通用的消息协议
"""

from enum import Enum
from typing import Optional, Dict, Any, List
from pydantic import BaseModel, Field
from datetime import datetime


class MessageType(str, Enum):
    """消息类型"""
    # 任务相关
    TASK = "task"                    # 发送任务
    TASK_RESPONSE = "task_response"  # 任务响应
    TASK_ERROR = "task_error"        # 任务错误

    # 通信相关
    QUERY = "query"                  # 查询请求
    QUERY_RESPONSE = "query_response"  # 查询响应

    # 协作相关
    DELEGATE = "delegate"            # 委派任务
    PROPOSE = "propose"              # 提议
    ACCEPT = "accept"                # 接受
    REJECT = "reject"                # 拒绝

    # 事件相关
    EVENT = "event"                  # 广播事件
    NOTIFICATION = "notification"    # 通知


class AgentAddress(BaseModel):
    """Agent 地址"""
    agent_id: str
    server_id: Optional[str] = None
    framework: Optional[str] = None


class MessageEnvelope(BaseModel):
    """消息封套（通用包装）"""
    id: str = Field(default_factory=lambda: f"msg-{datetime.now().timestamp()}")
    type: MessageType
    timestamp: str = Field(default_factory=lambda: datetime.utcnow().isoformat())

    # 发送者和接收者
    source: AgentAddress
    target: AgentAddress

    # 消息内容
    payload: Dict[str, Any]

    # 响应处理
    reply_to: Optional[str] = None
    correlation_id: Optional[str] = None

    # 优先级和TTL
    priority: int = Field(default=5, ge=1, le=10)
    ttl: Optional[int] = Field(default=None, description="消息生存时间（秒）")


# 具体消息类型

class TaskMessage(BaseModel):
    """任务消息"""
    task_id: str
    task_type: str
    description: str
    inputs: Dict[str, Any] = Field(default_factory=dict)
    context: Dict[str, Any] = Field(default_factory=dict)
    requirements: Optional[Dict[str, Any]] = None


class DelegateMessage(BaseModel):
    """委派消息"""
    delegation_id: str
    original_task_id: str
    task: TaskMessage
    reason: str
    deadline: Optional[str] = None


class QueryMessage(BaseModel):
    """查询消息"""
    query_id: str
    query_type: str
    parameters: Dict[str, Any] = Field(default_factory=dict)
    filters: Optional[Dict[str, Any]] = None


class AgentCapability(BaseModel):
    """Agent 能力描述"""
    agent_id: str
    framework: str
    capabilities: List[str]
    skills: List[str]
    tools: List[str]
    can_handle: List[str]  # 可以处理的任务类型
```

### 消息路由服务

```python
# agent_runtime/router.py
"""
Agent 消息路由服务
"""

from typing import Optional, List
import redis.asyncio as redis
from loguru import logger


class MessageRouter:
    """消息路由器 - 实现 Agent 之间的通信"""

    def __init__(self, redis_url: str):
        self.redis = redis.from_url(redis_url)
        self.local_cache = {}  # 本地缓存 Agent 注册信息

    async def send_message(
        self,
        envelope: MessageEnvelope,
        timeout: int = 30
    ) -> Optional[MessageEnvelope]:
        """发送消息并等待响应"""
        # 序列化消息
        message_json = envelope.model_dump_json()

        # 根据目标类型选择发送方式
        if envelope.target.agent_id == "broadcast":
            # 广播到事件频道
            await self.redis.publish("agent:events", message_json)
            return None
        else:
            # 点对点发送到目标 Agent 的任务队列
            target_queue = f"agent:tasks:{envelope.target.agent_id}"

            # 如果需要响应，创建响应队列
            response_queue = None
            if envelope.type in [MessageType.TASK, MessageType.QUERY]:
                response_queue = f"agent:response:{envelope.source.agent_id}:{envelope.id}"
                envelope.reply_to = response_queue

            # 发送消息
            await self.redis.rpush(target_queue, message_json)

            # 等待响应
            if response_queue:
                result = await self.redis.blpop(response_queue, timeout=timeout)

                if result:
                    _, response_json = result
                    return MessageEnvelope.model_validate_json(response_json)
                else:
                    logger.warning(f"消息 {envelope.id} 超时")

            return None

    async def discover_agents(
        self,
        framework: Optional[str] = None,
        capabilities: Optional[List[str]] = None
    ) -> List[AgentAddress]:
        """发现可用的 Agent"""
        # 从缓存或注册中心查询
        # 这里简化实现，实际应该有专门的 Agent Registry 服务

        # 从 Redis 中获取所有注册的 Agent
        agents = []

        async for key in self.redis.scan_iter("agent:registry:*"):
            agent_data = await self.redis.get(key)
            if agent_data:
                agent_info = json.loads(agent_data)

                # 过滤
                if framework and agent_info.get('framework') != framework:
                    continue
                if capabilities:
                    agent_caps = agent_info.get('capabilities', [])
                    if not any(c in agent_caps for c in capabilities):
                        continue

                agents.append(AgentAddress(**agent_info))

        return agents

    async def find_best_agent(
        self,
        task_type: str,
        requirements: Optional[Dict[str, Any]] = None
    ) -> Optional[AgentAddress]:
        """查找最适合处理任务的 Agent"""
        candidates = await self.discover_agents()

        # 根据能力匹配和负载选择最佳 Agent
        best_agent = None
        best_score = 0

        for agent in candidates:
            score = self._calculate_agent_score(agent, task_type, requirements)
            if score > best_score:
                best_score = score
                best_agent = agent

        return best_agent

    def _calculate_agent_score(
        self,
        agent: AgentAddress,
        task_type: str,
        requirements: Optional[Dict[str, Any]]
    ) -> float:
        """计算 Agent 匹配分数"""
        # 简化的评分逻辑
        score = 0.0

        # 检查能力匹配
        # agent_capabilities = self._get_agent_capabilities(agent.agent_id)
        # if task_type in agent_capabilities:
        #     score += 10

        # 检查负载
        # load = self._get_agent_load(agent.agent_id)
        # score -= load * 0.1

        return score
```

### Agent 交互示例

```python
# 示例：Agent 之间协作

# Agent A 发起任务到 Agent B
async def agent_a_collaborates_with_agent_b():
    """Agent A 与 Agent B 协作"""
    router = MessageRouter("redis://localhost:6379")

    # 创建任务消息
    task_msg = MessageEnvelope(
        type=MessageType.TASK,
        source=AgentAddress(agent_id="agent-a", framework="crewai"),
        target=AgentAddress(agent_id="agent-b", framework="langchain"),
        payload={
            "task": TaskMessage(
                task_id="task-001",
                task_type="research",
                description="研究最新的 AI 趋势",
                inputs={"topic": "LLM agents"}
            ).model_dump()
        }
    )

    # 发送并等待响应
    response = await router.send_message(task_msg)

    if response:
        logger.info(f"收到响应: {response.payload}")


# Agent 广播事件
async def agent_broadcasts_event():
    """Agent 广播事件"""
    router = MessageRouter("redis://localhost:6379")

    event = MessageEnvelope(
        type=MessageType.EVENT,
        source=AgentAddress(agent_id="agent-a"),
        target=AgentAddress(agent_id="broadcast"),  # 广播
        payload={
            "event_type": "task_completed",
            "data": {
                "task_id": "task-001",
                "result": "..."
            }
        }
    )

    await router.send_message(event)
```

---

## 完整部署方案

### Docker Compose 配置

```yaml
# docker-compose.yml
version: '3.8'

services:
  # 消息队列
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  # 监控后端
  monitor-backend:
    build: ./agent-dashboard-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/agent_monitor
      - SPRING_REDIS_HOST=redis
    depends_on:
      - db
      - redis

  # 数据库
  db:
    image: mysql:8
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=agent_monitor
    volumes:
      - mysql_data:/var/lib/mysql

  # 前端
  monitor-frontend:
    build: ./agent-dashboard-frontend
    ports:
      - "3000:80"
    environment:
      - VITE_API_BASE_URL=http://localhost:8080
      - VITE_WS_URL=ws://localhost:8080/ws

  # CrewAI Agent 示例
  crewai-agent:
    build:
      context: .
      dockerfile: docker/Dockerfile.agent-crewai
    environment:
      - AGENT_ID=crewai-agent-001
      - AGENT_MONITOR_URL=http://monitor-backend:8080
      - AGENT_MESSAGE_BROKER=redis://redis:6379
      - AGENT_SERVER_ID=docker-host-01
    depends_on:
      - redis
      - monitor-backend
    restart: unless-stopped

  # LangChain Agent 示例
  langchain-agent:
    build:
      context: .
      dockerfile: docker/Dockerfile.agent-langchain
    environment:
      - AGENT_ID=langchain-agent-001
      - AGENT_MONITOR_URL=http://monitor-backend:8080
      - AGENT_MESSAGE_BROKER=redis://redis:6379
      - AGENT_SERVER_ID=docker-host-01
    depends_on:
      - redis
      - monitor-backend
    restart: unless-stopped

volumes:
  redis_data:
  mysql_data:
```

### Dockerfile 示例

```dockerfile
# docker/Dockerfile.agent-crewai
FROM python:3.11-slim

# 安装系统依赖
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# 安装监控插件
COPY agent-monitor-plugin/ /tmp/plugin/
RUN cd /tmp/plugin && pip install -e . && rm -rf /tmp/plugin

# 安装 CrewAI
RUN pip install crewai

# 复制 Agent 运行时
COPY agent_runtime/ /app/agent_runtime/
RUN pip install -r /app/agent_runtime/requirements.txt

# 复制 Agent 代码
COPY agents/crewai_example/ /app/agent/
WORKDIR /app/agent

# 启动 Agent Runtime
CMD ["python", "-m", "agent_runtime.server"]
```

---

## 架构总结

### 1. 通用运行形态：Docker 容器

| 优势 | 说明 |
|------|------|
| **语言无关** | Python、Node.js、Java 都可以打包成镜像 |
| **环境隔离** | 每个 Agent 有独立的运行环境 |
| **易于扩展** | 可以快速启动多个实例 |
| **资源控制** | 可以设置 CPU、内存限制 |
| **统一管理** | 使用 Docker/Kubernetes 编排 |

### 2. 常驻 + 消息接收：Agent Runtime

```
┌──────────────────────────────────────────────┐
│         Agent Runtime (通用层)               │
│  ┌────────────────────────────────────────┐  │
│  │  消息队列监听                           │  │
│  │  - 任务队列 (blpop)                    │  │
│  │  - 事件频道 (pubsub)                   │  │
│  │  - 心跳维持                            │  │
│  └────────────────────────────────────────┘  │
│                    │                         │
│                    ▼                         │
│  ┌────────────────────────────────────────┐  │
│  │  框架适配器                             │  │
│  │  - CrewAIAdapter                       │  │
│  │  - LangChainAdapter                    │  │
│  │  - LangGraphAdapter                    │  │
│  └────────────────────────────────────────┘  │
│                    │                         │
│                    ▼                         │
│  ┌────────────────────────────────────────┐  │
│  │  Agent 代码 (一次性执行包装)            │  │
│  │  - story_writer_crew                   │  │
│  │  - customer_service_crew               │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

### 3. Agent 交互：统一消息协议

```
┌─────────────────────────────────────────────────────────────┐
│                   消息流转                                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Agent A (CrewAI)       Redis Broker        Agent B (LC)   │
│      ┌────┐                  │                  ┌────┐       │
│      │发送│ ──────────────────▶│─────────────────▶│接收│       │
│      │消息│   agent:tasks:b    │                  │消息│       │
│      └────┘                  │                  └────┘       │
│      │                        │                  │           │
│      │                        │                  │           │
│      │响应     ◀──────────────│─────────────────│响应       │
│      │   agent:response:a     │                  │           │
│                                                             │
│   事件广播:                                                 │
│      ┌────┐                  │                  ┌────┐       │
│      │发布│ ──────────────────▶─────────────────▶│订阅│       │
│      │事件│    agent:events   │                  │事件│       │
│      └────┘                  │                  └────┘       │
└─────────────────────────────────────────────────────────────┘
```

### 关键技术选型

| 组件 | 技术选择 | 理由 |
|------|---------|------|
| 容器化 | Docker | 通用、成熟 |
| 消息队列 | Redis | 轻量、支持 pubsub |
| 序列化 | JSON | 通用、可读 |
| 监控插件 | agent-monitor-plugin | 已有实现 |
| 框架适配 | Adapter 模式 | 解耦、可扩展 |
| 服务发现 | Redis Registry | 简单、无需额外组件 |

# Agent 分布式运行时 SDK 实现方案

## 项目定位

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        项目职责划分                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │  agent-monitor-plugin (已存在)                                     │ │
│  │  职责：监控和事件上报                                              │ │
│  │  - 拦截 CrewAI 事件                                               │ │
│  │  - 发送到监控后端                                                │ │
│  │  - WebSocket/HTTP 传输                                           │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                          │                                            │
│                          │ 分离的职责                                │
│                          ▼                                            │
│  ┌───────────────────────────────────────────────────────────────────┐ │
│  │  agent-runtime-sdk (新增) ← 这是要实现的！                        │ │
│  │  职责：分布式通信和代理                                            │ │
│  │  - 创建远程 Agent 代理                                            │ │
│  │  - 跨容器消息传递                                                │ │
│  │  - 网络透明化                                                    │ │
│  │  - 与监控插件配合（可选）                                         │ │
│  └───────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 项目结构

```
agent-runtime-sdk/
├── README.md
├── pyproject.toml
├── setup.py
├── requirements.txt
├── agent_runtime/
│   ├── __init__.py
│   ├── proxy/
│   │   ├── __init__.py
│   │   ├── remote_agent.py        # RemoteAgentProxy 类
│   │   ├── method_proxy.py        # RemoteMethodProxy 类
│   │   └── factory.py             # create_remote_agent() 工厂函数
│   ├── server/
│   │   ├── __init__.py
│   │   ├── agent_server.py        # AgentServer 类
│   │   └── handlers.py            # 请求处理器
│   ├── transport/
│   │   ├── __init__.py
│   │   ├── base.py                # Transport 接口
│   │   ├── redis_transport.py     # Redis 实现
│   │   └── http_transport.py      # HTTP 实现（可选）
│   ├── serialization/
│   │   ├── __init__.py
│   │   ├── crewai.py              # CrewAI 对象序列化
│   │   └── langchain.py           # LangChain 对象序列化
│   ├── integration/
│   │   ├── __init__.py
│   │   └── monitor_plugin.py      # 与监控插件集成
│   └── utils/
│       ├── __init__.py
│       └── async_helpers.py
├── examples/
│   ├── distributed_crew.py        # 分布式 Crew 示例
│   └── docker-compose.yml         # Docker Compose 配置
└── tests/
    ├── test_proxy.py
    └── test_server.py
```

---

## 核心代码实现

### 1. 项目配置

```toml
# agent-runtime-sdk/pyproject.toml
[build-system]
requires = ["setuptools>=61.0", "wheel"]
build-backend = "setuptools.build_meta"

[project]
name = "agent-runtime-sdk"
version = "0.1.0"
description = "分布式 Agent 运行时 SDK - 支持跨容器通信"
authors = [
    {name = "Agent Runtime Team"}
]
readme = "README.md"
requires-python = ">=3.8"
license = {text = "MIT"}

dependencies = [
    "redis>=5.0.0",
    "pydantic>=2.0.0",
    "asyncio>=3.11",
]

[project.optional-dependencies]
dev = [
    "pytest>=7.0.0",
    "pytest-asyncio>=0.21.0",
    "black>=23.0.0",
    "mypy>=1.0.0",
]

crewai = [
    "crewai>=0.1.0",
]

langchain = [
    "langchain>=0.1.0",
]

monitor = [
    "agent-monitor-plugin>=0.1.0",
]

[project.entry-points]
"agent_runtime.transports" = [
    "redis = agent_runtime.transport.redis_transport:RedisTransport"
]
```

### 2. RemoteAgentProxy 实现

```python
# agent_runtime/proxy/remote_agent.py
"""
远程 Agent 代理

创建一个看起来像本地 Agent 的代理对象，
实际将所有调用转发到远程容器
"""

import uuid
import json
import asyncio
from typing import Any, Optional, Dict
from loguru import logger

from agent_runtime.transport.base import Transport


class RemoteAgentProxy:
    """
    远程 Agent 代理

    用法：
        agent_b = create_remote_agent("specialist-001")

        # 看起来像本地调用
        role = agent_b.role  # 获取属性
        result = agent_b.delegate(task)  # 调用方法
    """

    def __init__(
        self,
        agent_id: str,
        transport: Transport,
        timeout: int = 300,
        lazy_load: bool = True
    ):
        self.agent_id = agent_id
        self.transport = transport
        self.timeout = timeout
        self.lazy_load = lazy_load

        # 延迟加载的属性
        self._attributes: Optional[Dict[str, Any]] = None
        self._attributes_loaded = False

    def __repr__(self) -> str:
        """字符串表示"""
        if self._attributes_loaded:
            return f"RemoteAgent(id={self.agent_id}, role={self._attributes.get('role', 'Unknown')})"
        return f"RemoteAgent(id={self.agent_id}, loading...)"

    def __getattr__(self, name: str) -> Any:
        """
        拦截属性访问

        策略：
        1. 如果已加载属性，返回本地值
        2. 如果是已知方法，返回 MethodProxy
        3. 否则从远程获取
        """
        # 已加载的属性
        if self._attributes_loaded and self._attributes and name in self._attributes:
            return self._attributes[name]

        # 已知方法（返回可调用代理）
        known_methods = {
            'delegate', 'execute_task', 'execute',
            'chat', 'run', 'kickoff'
        }
        if name in known_methods:
            return RemoteMethodProxy(
                self.agent_id,
                name,
                self.transport,
                self.timeout
            )

        # 从远程获取属性
        if self.lazy_load and not self._attributes_loaded:
            asyncio.run(self._load_attributes())
            if self._attributes and name in self._attributes:
                return self._attributes[name]

        # 返回一个通用方法代理
        return RemoteMethodProxy(
            self.agent_id,
            name,
            self.transport,
            self.timeout
        )

    async def _load_attributes(self) -> None:
        """从远程加载 Agent 属性"""
        if self._attributes_loaded:
            return

        logger.debug(f"[{self.agent_id}] 加载远程属性")

        request = {
            "type": "get_attributes",
            "source_agent": "proxy",
            "target_agent": self.agent_id,
            "request_id": str(uuid.uuid4()),
            "timestamp": ...  # datetime
        }

        response = await self.transport.call(
            self.agent_id,
            request,
            timeout=self.timeout
        )

        if response and response.get("type") == "attributes_response":
            self._attributes = response.get("attributes", {})
            self._attributes_loaded = True
            logger.debug(f"[{self.agent_id}] 属性加载完成: {list(self._attributes.keys())}")
        else:
            logger.warning(f"[{self.agent_id}] 属性加载失败")
            self._attributes = {}
            self._attributes_loaded = True

    def delegate(self, task: Any, context: Optional[Dict] = None) -> Any:
        """
        委派任务（会被 CrewAI 调用）

        这个方法需要同步返回（CrewAI 要求），但内部使用异步
        """
        logger.info(f"[Proxy] 委派任务到 {self.agent_id}")
        return asyncio.run(self._async_delegate(task, context))

    async def _async_delegate(self, task: Any, context: Optional[Dict] = None) -> Any:
        """异步委派实现"""
        # 序列化任务
        serialized_task = self._serialize_task(task)

        request = {
            "type": "method_call",
            "method": "delegate",
            "args": [serialized_task],
            "kwargs": {"context": context} if context else {},
            "request_id": str(uuid.uuid4())
        }

        response = await self.transport.call(
            self.agent_id,
            request,
            timeout=self.timeout
        )

        if response:
            return self._deserialize_result(response.get("result"))
        else:
            raise Exception(f"委派超时: {self.agent_id}")

    def _serialize_task(self, task: Any) -> Dict:
        """序列化任务对象"""
        # 处理 CrewAI Task
        if hasattr(task, 'description'):
            return {
                "description": task.description,
                "expected_output": getattr(task, 'expected_output', ''),
                "agent": getattr(task, 'agent', None),
            }
        # 处理字典
        elif isinstance(task, dict):
            return task
        # 其他类型
        else:
            return {"value": str(task)}

    def _deserialize_result(self, result: Any) -> Any:
        """反序列化结果"""
        # 简化实现，实际需要更复杂的处理
        if isinstance(result, dict):
            if "output" in result:
                return result["output"]
            if "raw" in result:
                return result["raw"]
        return result


class RemoteMethodProxy:
    """远程方法代理"""

    def __init__(
        self,
        agent_id: str,
        method_name: str,
        transport: Transport,
        timeout: int = 300
    ):
        self.agent_id = agent_id
        self.method_name = method_name
        self.transport = transport
        self.timeout = timeout

    def __call__(self, *args, **kwargs):
        """
        调用远程方法

        这个方法需要同步返回，内部使用异步
        """
        logger.debug(f"[Proxy] 调用远程方法: {self.agent_id}.{self.method_name}")
        return asyncio.run(self._async_call(*args, **kwargs))

    async def _async_call(self, *args, **kwargs):
        """异步调用实现"""
        # 序列化参数
        serialized_args = self._serialize_args(args)
        serialized_kwargs = self._serialize_args(kwargs)

        request = {
            "type": "method_call",
            "method": self.method_name,
            "args": serialized_args,
            "kwargs": serialized_kwargs,
            "request_id": str(uuid.uuid4())
        }

        response = await self.transport.call(
            self.agent_id,
            request,
            timeout=self.timeout
        )

        if response:
            return self._deserialize_result(response.get("result"))
        else:
            raise Exception(f"方法调用超时: {self.method_name}")

    def _serialize_args(self, args) -> Any:
        """序列化参数"""
        # 简化实现
        if len(args) == 0:
            return []
        return [self._serialize_value(arg) for arg in args]

    def _serialize_value(self, value: Any) -> Any:
        """序列化单个值"""
        # 处理 CrewAI 对象
        if hasattr(value, 'description'):
            return {
                "description": value.description,
                "expected_output": getattr(value, 'expected_output', ''),
            }
        # 处理字典
        elif isinstance(value, dict):
            return {k: self._serialize_value(v) for k, v in value.items()}
        # 其他
        else:
            return value

    def _deserialize_result(self, result: Any) -> Any:
        """反序列化结果"""
        if isinstance(result, dict) and "raw" in result:
            return result["raw"]
        return result
```

### 3. 工厂函数

```python
# agent_runtime/proxy/factory.py
"""
远程代理工厂函数
"""

from typing import Optional
from agent_runtime.transport.base import Transport
from agent_runtime.transport.redis_transport import RedisTransport
from agent_runtime.proxy.remote_agent import RemoteAgentProxy


def create_remote_agent(
    agent_id: str,
    redis_url: Optional[str] = None,
    transport: Optional[Transport] = None,
    timeout: int = 300,
    **kwargs
) -> RemoteAgentProxy:
    """
    创建远程 Agent 的本地代理

    Args:
        agent_id: 远程 Agent 的 ID
        redis_url: Redis 连接 URL（如果不提供 transport）
        transport: 自定义传输层（可选）
        timeout: 调用超时时间（秒）
        **kwargs: 其他参数

    Returns:
        RemoteAgentProxy: 远程 Agent 代理

    Examples:
        >>> # 使用默认 Redis 传输
        >>> agent_b = create_remote_agent("specialist-001")

        >>> # 使用自定义 Redis URL
        >>> agent_b = create_remote_agent(
        ...     "specialist-001",
        ...     redis_url="redis://remote-host:6379"
        ... )

        >>> # 使用自定义传输层
        >>> from agent_runtime.transport import HTTPTransport
        >>> transport = HTTPTransport("http://agent-server:8080")
        >>> agent_b = create_remote_agent("specialist-001", transport=transport)
    """
    if transport is None:
        if redis_url is None:
            # 尝试从环境变量读取
            import os
            redis_url = os.getenv(
                "AGENT_RUNTIME_REDIS_URL",
                "redis://localhost:6379"
            )
        transport = RedisTransport(redis_url)

    return RemoteAgentProxy(
        agent_id=agent_id,
        transport=transport,
        timeout=timeout,
        **kwargs
    )


# 便捷导入
__all__ = ['create_remote_agent', 'RemoteAgentProxy']
```

### 4. Transport 接口和实现

```python
# agent_runtime/transport/base.py
"""
传输层抽象接口
"""

from abc import ABC, abstractmethod
from typing import Any, Dict, Optional


class Transport(ABC):
    """传输层基类"""

    @abstractmethod
    async def call(
        self,
        target_id: str,
        message: Dict[str, Any],
        timeout: int = 300
    ) -> Optional[Dict[str, Any]]:
        """
        发送调用请求并等待响应

        Args:
            target_id: 目标 Agent ID
            message: 消息内容
            timeout: 超时时间（秒）

        Returns:
            响应内容，如果超时返回 None
        """
        pass

    @abstractmethod
    async def publish(self, channel: str, message: Dict[str, Any]) -> None:
        """发布消息（不需要响应）"""
        pass

    @abstractmethod
    async def subscribe(self, channel: str, callback) -> None:
        """订阅频道"""
        pass
```

```python
# agent_runtime/transport/redis_transport.py
"""
Redis 传输层实现
"""

import uuid
import json
import asyncio
from typing import Any, Dict, Optional, Callable
import redis.asyncio as redis
from loguru import logger

from agent_runtime.transport.base import Transport


class RedisTransport(Transport):
    """Redis 传输层"""

    def __init__(
        self,
        redis_url: str = "redis://localhost:6379",
        encoding: str = "utf-8"
    ):
        self.redis_url = redis_url
        self.encoding = encoding
        self._redis: Optional[redis.Redis] = None

    def _get_redis(self) -> redis.Redis:
        """获取 Redis 连接（懒加载）"""
        if self._redis is None:
            self._redis = redis.from_url(self.redis_url, encoding=self.encoding)
        return self._redis

    async def call(
        self,
        target_id: str,
        message: Dict[str, Any],
        timeout: int = 300
    ) -> Optional[Dict[str, Any]]:
        """
        发送调用请求并等待响应

        使用 Redis Pub/Sub 模式：
        1. 发布请求到 agent:{target_id}
        2. 订阅响应频道 agent_response:{target_id}:{request_id}
        3. 等待响应或超时
        """
        request_id = message.get("request_id", str(uuid.uuid4()))

        # 添加 request_id（如果没有）
        message["request_id"] = request_id

        r = self._get_redis()

        # 订阅响应频道
        response_channel = f"agent_response:{target_id}:{request_id}"
        pubsub = r.pubsub()
        await pubsub.subscribe(response_channel)

        # 发布请求
        request_channel = f"agent:{target_id}"
        await r.publish(request_channel, json.dumps(message))

        logger.debug(f"[Transport] 发送请求到 {target_id}: {message.get('method')}")

        # 等待响应
        start_time = asyncio.get_event_loop().time()

        async for msg in pubsub.listen():
            if msg['type'] == 'message':
                await pubsub.unsubscribe(response_channel)

                data = json.loads(msg['data'])
                logger.debug(f"[Transport] 收到响应: {data.get('type')}")
                return data

            # 超时检查
            if asyncio.get_event_loop().time() - start_time > timeout:
                await pubsub.unsubscribe(response_channel)
                logger.warning(f"[Transport] 请求超时: {request_id}")
                return None

        return None

    async def publish(self, channel: str, message: Dict[str, Any]) -> None:
        """发布消息"""
        r = self._get_redis()
        await r.publish(channel, json.dumps(message))

    async def subscribe(self, channel: str, callback: Callable) -> None:
        """订阅频道"""
        r = self._get_redis()
        pubsub = r.pubsub()
        await pubsub.subscribe(channel)

        async for msg in pubsub.listen():
            if msg['type'] == 'message':
                data = json.loads(msg['data'])
                await callback(data)
```

### 5. AgentServer 实现

```python
# agent_runtime/server/agent_server.py
"""
Agent 服务器 - 在每个 Agent 容器中运行

接收远程调用请求，转发给本地 Agent
"""

import asyncio
import uuid
from typing import Any, Dict
import redis.asyncio as redis
from loguru import logger

from agent_runtime.transport.base import Transport
from agent_runtime.transport.redis_transport import RedisTransport


class AgentServer:
    """
    Agent 服务器

    在后台运行，监听远程调用请求
    """

    def __init__(
        self,
        agent_id: str,
        agent: Any,  # 原生 Agent 对象（CrewAI Agent 等）
        transport: Transport = None,
        redis_url: str = None
    ):
        self.agent_id = agent_id
        self.agent = agent  # 原生 Agent，不修改！

        # 传输层
        if transport:
            self.transport = transport
        else:
            if redis_url is None:
                import os
                redis_url = os.getenv("AGENT_RUNTIME_REDIS_URL", "redis://localhost:6379")
            self.transport = RedisTransport(redis_url)

        self.running = False
        self._server_task = None

    async def start(self, background: bool = True):
        """
        启动服务器

        Args:
            background: 是否在后台运行（默认 True）
        """
        self.running = True

        if background:
            # 在后台任务中运行
            self._server_task = asyncio.create_task(self._run())
            logger.info(f"[{self.agent_id}] Agent Server 启动（后台模式）")
        else:
            # 直接运行
            logger.info(f"[{self.agent_id}] Agent Server 启动")
            await self._run()

    async def stop(self):
        """停止服务器"""
        self.running = False
        if self._server_task:
            self._server_task.cancel()
            try:
                await self._server_task
            except asyncio.CancelledError:
                pass
        logger.info(f"[{self.agent_id}] Agent Server 停止")

    async def _run(self):
        """服务器主循环"""
        r = self.transport._get_redis()
        pubsub = r.pubsub()

        # 订阅 Agent 的私有频道
        await pubsub.subscribe(f"agent:{self.agent_id}")

        logger.info(f"[{self.agent_id}] 开始监听频道: agent:{self.agent_id}")

        async for message in pubsub.listen():
            if not self.running:
                break

            if message['type'] == 'pmessage' or message['type'] == 'message':
                try:
                    data = message.get('data')
                    if data:
                        request = json.loads(data)
                        await self._handle_request(r, request)
                except Exception as e:
                    logger.error(f"[{self.agent_id}] 处理请求失败: {e}")

    async def _handle_request(self, r: redis, request: Dict[str, Any]):
        """处理请求"""
        request_type = request.get("type")
        request_id = request.get("request_id")

        logger.debug(f"[{self.agent_id}] 收到请求: {request_type}")

        if request_type == "get_attributes":
            await self._handle_get_attributes(r, request_id)

        elif request_type == "method_call":
            await self._handle_method_call(r, request, request_id)

        else:
            await self._send_error(r, request_id, f"未知请求类型: {request_type}")

    async def _handle_get_attributes(self, r: redis, request_id: str):
        """处理获取属性请求"""
        # 获取 Agent 属性（原生）
        attributes = {
            "role": self.agent.role,
            "goal": self.agent.goal,
            "backstory": getattr(self.agent, 'backstory', ''),
            "allow_delegation": getattr(self.agent, 'allow_delegation', False),
            "verbose": getattr(self.agent, 'verbose', False),
        }

        response = {
            "type": "attributes_response",
            "agent_id": self.agent_id,
            "request_id": request_id,
            "attributes": attributes
        }

        response_channel = f"agent_response:{self.agent_id}:{request_id}"
        await r.publish(response_channel, json.dumps(response))

    async def _handle_method_call(self, r: redis, request: Dict[str, Any], request_id: str):
        """处理方法调用请求"""
        method = request.get("method")
        args = request.get("args", [])
        kwargs = request.get("kwargs", {})

        logger.info(f"[{self.agent_id}] 调用方法: {method}")

        try:
            # 反序列化参数
            deserialized_args = await self._deserialize_args(args)
            deserialized_kwargs = await self._deserialize_args(kwargs)

            # 调用原生 Agent 的方法
            if hasattr(self.agent, method):
                method_func = getattr(self.agent, method)
                result = method_func(*deserialized_args, **deserialized_kwargs)
            else:
                # 方法不存在
                raise AttributeError(f"Agent 没有 '{method}' 方法")

            # 序列化结果
            serialized_result = await self._serialize_result(result)

            # 发送响应
            response = {
                "type": "method_response",
                "agent_id": self.agent_id,
                "request_id": request_id,
                "result": serialized_result
            }

            response_channel = f"agent_response:{self.agent_id}:{request_id}"
            await r.publish(response_channel, json.dumps(response))

            logger.info(f"[{self.agent_id}] 方法调用完成: {method}")

        except Exception as e:
            logger.error(f"[{self.agent_id}] 方法调用失败: {e}")
            await self._send_error(r, request_id, str(e))

    async def _deserialize_args(self, args: Any) -> Any:
        """反序列化参数"""
        # 处理 CrewAI Task
        if isinstance(args, list):
            from crewai import Task
            deserialized = []
            for arg in args:
                if isinstance(arg, dict) and 'description' in arg:
                    # 重建 Task 对象
                    deserialized.append(Task(
                        description=arg['description'],
                        expected_output=arg.get('expected_output', '')
                    ))
                else:
                    deserialized.append(arg)
            return deserialized
        return args

    async def _serialize_result(self, result: Any) -> Any:
        """序列化结果"""
        # 处理 CrewAI Output
        if hasattr(result, 'raw'):
            return {
                "raw": str(result.raw),
                "output": str(result.raw)
            }
        return str(result)

    async def _send_error(self, r: redis, request_id: str, error: str):
        """发送错误响应"""
        response = {
            "type": "error",
            "agent_id": self.agent_id,
            "request_id": request_id,
            "error": error
        }

        response_channel = f"agent_response:{self.agent_id}:{request_id}"
        await r.publish(response_channel, json.dumps(response))
```

### 6. 与监控插件集成（可选）

```python
# agent_runtime/integration/monitor_plugin.py
"""
与监控插件集成

可以在 AgentServer 中集成监控插件，自动上报事件
"""

from typing import Optional


class MonitoredAgentServer(AgentServer):
    """
    带监控的 Agent 服务器

    在处理远程调用时，自动发送监控事件
    """

    def __init__(
        self,
        agent_id: str,
        agent: Any,
        transport=None,
        redis_url: str = None,
        monitor_plugin=None
    ):
        super().__init__(agent_id, agent, transport, redis_url)
        self.monitor_plugin = monitor_plugin

    async def _handle_method_call(self, r, request, request_id):
        """处理方法调用（带监控）"""
        method = request.get("method")

        # 发送监控事件
        if self.monitor_plugin:
            await self._emit_monitor_event("method_call_started", {
                "method": method,
                "remote_call": True
            })

        # 调用父类方法
        await super()._handle_method_call(r, request, request_id)

        # 发送完成事件
        if self.monitor_plugin:
            await self._emit_monitor_event("method_call_completed", {
                "method": method
            })

    async def _emit_monitor_event(self, event_type: str, data: dict):
        """发送监控事件"""
        if self.monitor_plugin:
            # 使用监控插件的传输层
            from agent_monitor.protocol.unified_event import MonitorEvent
            from agent_monitor.protocol.unified_event import (
                EventSource, EventMetadata
            )

            event = MonitorEvent(
                source=EventSource(
                    server_id=self._get_server_id(),
                    agent_id=self.agent_id,
                    framework=self._get_framework(),
                    language="python"
                ),
                event={
                    "type": event_type,
                    "data": data
                },
                metadata=EventMetadata(
                    hostname=self._get_hostname()
                )
            )

            # 发送到监控后端
            self.monitor_plugin.transport.send(event.to_dict())

    def _get_server_id(self) -> str:
        """获取服务器 ID"""
        import os
        return os.getenv("AGENT_SERVER_ID", "unknown")

    def _get_framework(self) -> str:
        """获取框架名称"""
        if hasattr(self.agent, '__class__'):
            return "crewai"
        return "unknown"

    def _get_hostname(self) -> str:
        """获取主机名"""
        import socket
        return socket.gethostname()
```

### 7. 使用示例

```python
# examples/distributed_crew.py
"""
分布式 Crew 示例

展示如何使用 agent-runtime-sdk 创建分布式 Agent
"""

import asyncio
import os
from crewai import Agent, Task, Crew, Process
from loguru import logger

# 导入运行时 SDK
from agent_runtime.proxy import create_remote_agent
from agent_runtime.server.agent_server import AgentServer


async def main():
    # Agent 配置
    agent_id = os.getenv("AGENT_ID", "manager-001")
    redis_url = os.getenv("REDIS_URL", "redis://localhost:6379")

    # ============================================================
    #  创建本地 Agent（完全原生代码）
    # ============================================================

    manager = Agent(
        role="Customer Service Manager",
        goal="Handle customer inquiries and delegate technical issues",
        backstory="You are a friendly manager with 5 years of experience...",
        verbose=True,
        allow_delegation=True  # ← 保留原生委派能力
    )

    # ============================================================
    #  启动 Agent Server（后台运行）
    # ============================================================

    server = AgentServer(agent_id, manager, redis_url=redis_url)
    await server.start(background=True)

    logger.info(f"Agent {agent_id} ({manager.role}) 已启动")

    # ============================================================
    #  如果是 Manager，创建远程 Agent 代理并运行 Crew
    # ============================================================

    if os.getenv("IS_MANAGER", "false").lower() == "true":
        # 创建远程 Agent 代理（看起来像本地 Agent！）
        specialist = create_remote_agent(
            os.getenv("SPECIALIST_ID", "specialist-001"),
            redis_url=redis_url
        )

        supervisor = create_remote_agent(
            os.getenv("SUPERVISOR_ID", "supervisor-001"),
            redis_url=redis_url
        )

        # 创建 Crew（完全原生代码！）
        task1 = Task(
            description="Handle customer inquiry about API timeout",
            expected_output="Resolution for the customer"
        )

        crew = Crew(
            agents=[manager, specialist, supervisor],
            tasks=[task1],
            process=Process.hierarchical,  # ← 保留原生流程
            verbose=True
        )

        # 执行 Crew（完全原生调用！）
        result = crew.kickoff(inputs={"customer_query": "API keeps timing out"})

        logger.info(f"Crew 执行完成: {result}")

    # 保持运行
    try:
        while True:
            await asyncio.sleep(1)
    except KeyboardInterrupt:
        logger.info("停止 Agent")
        await server.stop()


if __name__ == "__main__":
    asyncio.run(main())
```

---

## 总结

### 项目定位

| 项目 | 职责 | 状态 |
|------|------|------|
| **agent-monitor-plugin** | 监控和事件上报 | ✅ 已实现 |
| **agent-runtime-sdk** | 分布式通信和代理 | 🆕 新增 |
| **agent-dashboard-backend** | 监控后端 | ✅ 已实现 |
| **agent-dashboard-frontend** | 监控前端 | ✅ 已实现 |

### agent-runtime-sdk 的核心组件

| 组件 | 文件 | 职责 |
|------|------|------|
| **RemoteAgentProxy** | `proxy/remote_agent.py` | 远程 Agent 代理 |
| **AgentServer** | `server/agent_server.py` | 接收远程调用 |
| **Transport** | `transport/base.py` | 传输层抽象 |
| **RedisTransport** | `transport/redis_transport.py` | Redis 实现 |
| **工厂函数** | `proxy/factory.py` | `create_remote_agent()` |

### 与监控插件的关系

```
┌─────────────────────────────────────────────────────────────┐
│  Agent 容器                                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  用户代码（CrewAI Agent）                              │ │
│  └───────────────────────────────────────────────────────┘ │
│                          │                                  │
│          ┌───────────────┴──────────────┐                  │
│          ▼                              ▼                  │
│  ┌──────────────────┐        ┌──────────────────┐         │
│  │ AgentServer      │        │ 监控插件         │         │
│  │ (处理远程调用)  │        │ (发送监控事件)  │         │
│  └──────────────────┘        └──────────────────┘         │
│          │                              │                  │
│          └───────────────┬──────────────┘                  │
│                          ▼                                  │
│                    ┌─────────┐                                │
│                    │  Redis  │                                │
│                    └─────────┘                                │
└─────────────────────────────────────────────────────────────┘
```

### 安装和使用

```bash
# 安装
pip install agent-runtime-sdk

# 基础使用（仅运行时）
from agent_runtime import create_remote_agent

agent_b = create_remote_agent("specialist-001")

# 集成监控
pip install agent-runtime-sdk[monitor]

from agent_runtime import MonitoredAgentServer
# ... 自动上报监控事件
```

这就是 `transparent_proxy` 的完整实现方案！它是一个独立的 SDK，与监控插件分离但可以集成使用。

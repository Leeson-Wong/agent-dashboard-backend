# Agent 分布式监控系统 - 架构设计文档

> **版本**: v1.0
> **日期**: 2025-02-06
> **状态**: 设计阶段
> **核心理念**: 面向未来的分布式监控架构

---

## 一、项目愿景

构建一个**通用的、分布式的 Agent 监控平台**，能够：

1. **零侵入集成** - 不修改原始 Agent 代码
2. **跨框架支持** - 支持 CrewAI、LangGraph、AutoGen 等所有主流框架
3. **分布式部署** - Agent 可运行在任意服务器
4. **实时监控** - 实时掌握 Agent 状态、活动和关系
5. **可扩展架构** - 支持未来新框架、新传输方式的扩展

---

## 二、整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      分布式 Agent 集群                            │
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │  Server A    │    │  Server B    │    │  Server C    │      │
│  │              │    │              │    │              │      │
│  │  ┌────────┐  │    │  ┌────────┐  │    │  ┌────────┐  │      │
│  │  │CrewAI  │  │    │  │LangGrp │  │    │  │AutoGen │  │      │
│  │  │Agent 1 │  │    │  │Agent 3 │  │    │  │Agent 5 │  │      │
│  │  └───┬────┘  │    │  └───┬────┘  │    │  └───┬────┘  │      │
│  │      │       │    │      │       │    │      │       │      │
│  │  ┌───▼────┐  │    │  ┌───▼────┐  │    │  ┌───▼────┐  │      │
│  │  │Plugin  │◄─┼────┼──┤Plugin  │◄─┼────┼──┤Plugin  │  │      │
│  │  └───┬────┘  │    │  └───┬────┘  │    │  └───┬────┘  │      │
│  └──────┼───────┘    └──────┼───────┘    └──────┼───────┘      │
│         │                   │                   │              │
└─────────┼───────────────────┼───────────────────┼──────────────┘
          │                   │                   │
          │         ┌─────────▼─────────┐         │
          │         │   Message Queue    │         │
          │         │  (Kafka/NATS/Jet) │         │
          │         │   - 事件总线       │         │
          │         │   - 消息持久化     │         │
          │         │   - 支持重试       │         │
          │         └─────────┬─────────┘         │
          │                   │                   │
          │         ┌─────────▼───────────────────▼───────┐
          │         │         事件收集器集群              │
          │         │  ┌──────────┐  ┌──────────┐       │
          │         │  │Collector │  │Collector │       │
          │         │  │  Node 1  │  │  Node 2  │  ...  │
          │         │  └────┬─────┘  └────┬─────┘       │
          │         └───────┼────────────┼──────────────┘
          │                 │            │
          ▼                 ▼            ▼
┌─────────────────────────────────────────────────────────┐
│                    核心服务层                             │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 状态聚合服务  │  │ 关系图谱服务 │  │ 告警服务     │  │
│  │ - Agent状态  │  │ - Neo4j存储 │  │ - 异常检测   │  │
│  │ - 活动追踪  │  │ - 图查询    │  │ - 通知推送   │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
│         │                 │                 │
└─────────┼─────────────────┼─────────────────┼───────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────┐
│                    存储层                                 │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ PostgreSQL   │  │    Neo4j     │  │  ClickHouse  │  │
│  │ - Agent元数据│  │ - 关系图谱   │  │ - 时序事件   │  │
│  │ - 配置数据   │  │ - 协作网络   │  │ - 性能指标   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────┐
│                    API 网关                               │
│  - GraphQL API   - WebSocket API   - REST API           │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                    前端看板                               │
│  - Agent 列表    - 关系图谱     - 实时事件流             │
└─────────────────────────────────────────────────────────┘
```

---

## 三、核心设计原则

### 3.1 插件与网络层解耦

**插件不应该关心：**
- ❌ 监控服务在哪里
- ❌ 如何重连
- ❌ 如何批量发送
- ❌ 如何处理网络故障

**插件只负责：**
- ✅ 捕获事件
- ✅ 标准化格式
- ✅ 发送到本地代理（UDP/Unix Socket）

### 3.2 轻量级本地代理

每个服务器运行一个轻量级代理：

```python
# agent-monitor-agent/agent.py
"""
Agent Monitor Agent - 本地事件采集器

功能：
1. 接收本地所有 Agent 插件的事件
2. 批量缓存
3. 智能重连
4. 压缩传输
"""

import asyncio
import msgpack
from typing import List

class MonitorAgent:
    def __init__(self, remote_url: str):
        self.remote_url = remote_url
        self.event_buffer: List[dict] = []
        self.buffer_size = 100  # 批量发送
        self.flush_interval = 5  # 5秒强制刷新

    async def start(self):
        """启动本地监听服务"""
        # 启动 UDP 服务器接收插件事件
        await self._start_udp_server()

        # 启动定时刷新任务
        asyncio.create_task(self._auto_flush())

    async def receive_event(self, event: dict):
        """接收插件事件"""
        self.event_buffer.append(event)

        if len(self.event_buffer) >= self.buffer_size:
            await self._flush()

    async def _flush(self):
        """批量发送到远程"""
        if not self.event_buffer:
            return

        # 压缩
        payload = msgpack.packb(self.event_buffer)

        # 发送
        try:
            async with aiohttp.ClientSession() as session:
                await session.post(
                    f"{self.remote_url}/api/events/batch",
                    data=payload,
                    headers={"Content-Type": "application/msgpack"}
                )
        except Exception as e:
            # 失败时缓存到磁盘
            await self._persist_to_disk()

        self.event_buffer.clear()
```

### 3.3 统一事件协议

```typescript
// 协议版本化，支持未来扩展
interface MonitorEventV1 {
  protocol: "agent-monitor"     // 协议标识
  version: "1.0"                // 协议版本
  timestamp: ISO8601            // 事件时间
  source: {                     // 事件源
    server_id: string           // 服务器唯一ID
    agent_id: string            // Agent ID
    framework: string           // 框架名称
    process_id: number          // 进程ID
  }
  event: {                      // 事件内容
    type: string                // 事件类型
    data: any                   // 事件数据
  }
  metadata: {                   // 元数据
    hostname: string
    ip_address: string
    tags?: Record<string, string>
  }
}
```

---

## 四、分层插件架构

### 4.1 架构层次

```
┌─────────────────────────────────────────────────────────┐
│                   Agent 框架层                            │
│  (CrewAI / LangGraph / AutoGen / ...)                   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              层级 1: 框架适配器                            │
│  - CrewAIAdapter                                        │
│  - LangGraphAdapter                                     │
│  - AutoGenAdapter                                       │
│  职责：捕获框架事件，转换为统一格式                       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              层级 2: 协议层                                │
│  - MonitorEventV1                                       │
│  - 事件验证器                                            │
│  职责：定义统一事件格式，确保兼容性                       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              层级 3: 传输适配器                            │
│  - UDPTransport (本地)                                   │
│  - UnixSocketTransport (本地 IPC)                        │
│  - KafkaTransport (直连消息队列)                         │
│  职责：抽象网络层，支持多种传输方式                       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              层级 4: 本地代理（可选）                     │
│  - MonitorAgent                                         │
│  职责：批量缓存、压缩、智能重连                           │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              层级 5: 远程服务                             │
│  - Message Queue → Collector → Aggregator → Storage     │
└─────────────────────────────────────────────────────────┘
```

### 4.2 框架适配器示例

```python
# agent_monitor/plugins/crewai_adapter.py
class CrewAIAdapter:
    """CrewAI 框架适配器"""

    def __init__(self, transport):
        self.server_id = self._get_server_id()
        self.transport = transport

    def capture_events(self):
        """捕获 CrewAI 事件"""
        from crewai.events import crewai_event_bus
        from crewai.events.types import AgentExecutionStartedEvent

        @crewai_event_bus.on(AgentExecutionStartedEvent)
        def on_event(source, event):
            # 转换为统一格式
            normalized = {
                "protocol": "agent-monitor",
                "version": "1.0",
                "timestamp": event.timestamp.isoformat(),
                "source": {
                    "server_id": self.server_id,
                    "agent_id": event.agent.id,
                    "framework": "crewai",
                    "process_id": os.getpid()
                },
                "event": {
                    "type": "agent_online",
                    "data": {
                        "role": event.agent.role,
                        "goal": event.agent.goal
                    }
                },
                "metadata": {
                    "hostname": socket.gethostname(),
                    "ip_address": self._get_local_ip()
                }
            }

            # 发送到传输层（不直接发往远程）
            self.transport.send(normalized)
```

### 4.3 传输适配器接口

```python
# agent_monitor/transports/base.py
class TransportAdapter(ABC):
    """传输适配器基类"""

    @abstractmethod
    async def send(self, event: dict):
        """发送事件"""
        pass

    @abstractmethod
    async def flush(self):
        """刷新缓冲区"""
        pass

# agent_monitor/transports/udp.py
class UDPTransport(TransportAdapter):
    """UDP 传输 - 最低延迟"""

    def __init__(self, host="127.0.0.1", port=9999):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.address = (host, port)

    async def send(self, event: dict):
        """发送到本地监控代理"""
        payload = json.dumps(event).encode()
        self.sock.sendto(payload, self.address)

# agent_monitor/transports/kafka.py
class KafkaTransport(TransportAdapter):
    """Kafka 传输 - 直接发送到消息队列"""

    def __init__(self, bootstrap_servers, topic="agent-events"):
        from kafka import KafkaProducer
        self.producer = KafkaProducer(
            bootstrap_servers=bootstrap_servers,
            value_serializer=lambda v: json.dumps(v).encode()
        )
        self.topic = topic

    async def send(self, event: dict):
        """直接发送到 Kafka（适用于大规模部署）"""
        await self.producer.send_and_wait(self.topic, event)
```

---

## 五、插件集成方式

### 5.1 零侵入方式（推荐）

```bash
# 1. 安装插件
pip install agent-monitor-plugin

# 2. 启用监控
export AGENT_MONITOR_ENABLED=true

# 3. 运行 Agent（自动监控，无需修改代码）
python my_crewai_app.py
```

### 5.2 代码集成方式

```python
# my_app.py
from agent_monitor import init_monitor

# 初始化监控（会自动检测框架）
monitor = init_monitor(
    server_id="my-server",
    transport="udp"
)

# 正常使用 CrewAI
crew = Crew(agents=[...], tasks=[...])
crew.kickoff()

# 监控自动运行，无需额外代码
```

### 5.3 配置文件方式

```toml
# ~/.agent-monitor/config.toml
[general]
server_id = "prod-server-01"
environment = "production"

[transport]
type = "udp"  # 或 "kafka"、"unix_socket"

[udp]
host = "127.0.0.1"
port = 9999

[monitor_service]
# 如果是直连模式（跳过本地代理）
url = "https://monitor.example.com"
api_key = "sk_xxx"

[buffer]
batch_size = 100
flush_interval = 5
max_disk_cache = "1GB"
```

---

## 六、插件项目结构

```
agent-monitor-plugin/
├── setup.py                    # 包配置
├── agent_monitor/
│   ├── __init__.py
│   ├── config/                 # 配置管理
│   │   ├── __init__.py
│   │   └── loader.py           # 配置加载器
│   │
│   ├── plugins/                # 框架适配器
│   │   ├── __init__.py
│   │   ├── base.py             # 插件基类
│   │   ├── crewai_plugin.py
│   │   ├── langgraph_plugin.py
│   │   ├── autogen_plugin.py
│   │   └── registry.py         # 插件注册表
│   │
│   ├── transports/             # 传输适配器
│   │   ├── __init__.py
│   │   ├── base.py
│   │   ├── udp.py
│   │   ├── unix_socket.py
│   │   └── kafka.py            # 可选依赖
│   │
│   ├── protocol/               # 统一协议
│   │   ├── __init__.py
│   │   ├── event_v1.py         # 事件格式定义
│   │   └── validator.py        # 验证器
│   │
│   ├── utils/                  # 工具
│   │   ├── __init__.py
│   │   ├── hostname.py         # 获取主机信息
│   │   └── cache.py            # 磁盘缓存
│   │
│   └── cli/                    # 命令行工具
│       ├── __init__.py
│       └── agent.py            # agent-monitor-agent 命令
│
├── tests/
│   ├── test_plugins/
│   ├── test_transports/
│   └── integration/
│
└── README.md
```

---

## 七、远程服务架构

### 7.1 事件收集器

```python
# collector-service/collector.py
"""
事件收集器 - 从消息队列接收事件，分发到处理服务
"""

class EventCollector:
    def __init__(self, kafka_brokers):
        self.consumer = KafkaConsumer(
            "agent-events",
            bootstrap_servers=kafka_brokers,
            group_id="collector-group"
        )

    async def start(self):
        """启动收集器"""
        async for message in self.consumer:
            event = json.loads(message.value)

            # 分发到处理服务
            await self.dispatch(event)

    async def dispatch(self, event):
        """根据事件类型分发"""
        if event["event"]["type"] == "agent_online":
            await self.state_aggregator.agent_online(event)
        elif event["event"]["type"] == "agent_working":
            await self.state_aggregator.agent_working(event)
        elif event["event"]["type"] == "agent_relationship":
            await self.relationship_builder.add_relationship(event)
```

### 7.2 状态聚合服务

```python
# state-aggregator-service/aggregator.py
"""
状态聚合服务 - 维护 Agent 状态
"""

class StateAggregator:
    def __init__(self, db_pool):
        self.db = db_pool

    async def agent_online(self, event):
        """Agent 上线"""
        await self.db.execute("""
            INSERT INTO agent_states (agent_id, server_id, status, last_seen)
            VALUES ($1, $2, 'online', NOW())
            ON CONFLICT (agent_id) DO UPDATE
            SET status = 'online', last_seen = NOW()
        """, event["source"]["agent_id"], event["source"]["server_id"])

    async def get_all_agents(self):
        """获取所有 Agent 状态"""
        return await self.db.fetch("SELECT * FROM agent_states")
```

### 7.3 关系图谱服务

```python
# relationship-service/builder.py
"""
关系图谱服务 - 构建 Agent 关系网络
"""

class RelationshipBuilder:
    def __init__(self, neo4j_driver):
        self.driver = neo4j_driver

    async def add_relationship(self, event):
        """添加 Agent 关系"""
        data = event["event"]["data"]

        with self.driver.session() as session:
            session.run("""
                MERGE (a:Agent {id: $from_agent})
                MERGE (b:Agent {id: $to_agent})
                MERGE (a)-[r:DELEGATED_TO]->(b)
                SET r.last_interaction = datetime()
                SET r.count = coalesce(r.count, 0) + 1
            """, from_agent=data["from"], to_agent=data["to"])

    async def get_agent_network(self, agent_id, depth=2):
        """获取 Agent 的关系网络"""
        with self.driver.session() as session:
            result = session.run("""
                MATCH (a:Agent {id: $id})-[r*1..{depth}]-(b:Agent)
                RETURN a, r, b
            """, id=agent_id, depth=depth)
            return result.data()
```

---

## 八、数据存储设计

### 8.1 PostgreSQL - Agent 元数据

```sql
-- Agent 状态表
CREATE TABLE agent_states (
    agent_id VARCHAR(255) PRIMARY KEY,
    server_id VARCHAR(255) NOT NULL,
    framework VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,  -- online, offline, error
    current_activity JSONB,
    capabilities JSONB,
    last_seen TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 服务器表
CREATE TABLE servers (
    server_id VARCHAR(255) PRIMARY KEY,
    hostname VARCHAR(255),
    ip_address INET,
    environment VARCHAR(50),
    last_heartbeat TIMESTAMP,
    status VARCHAR(20)
);

-- 索引
CREATE INDEX idx_agent_status ON agent_states(status);
CREATE INDEX idx_agent_server ON agent_states(server_id);
```

### 8.2 Neo4j - 关系图谱

```cypher
// Agent 节点
CREATE (agent:Agent {
    id: "agent-123",
    framework: "crewai",
    role: "researcher",
    server: "server-1"
})

// 关系类型
// - DELEGATED_TO: 委派关系
// - COLLABORATED_WITH: 协作关系
// - REPORTED_TO: 报告关系
CREATE (a1:Agent {id: "agent-1"})
CREATE (a2:Agent {id: "agent-2"})
CREATE (a1)-[:DELEGATED_TO {count: 5, last_at: datetime()}]->(a2)
```

### 8.3 ClickHouse - 时序事件

```sql
-- 事件表
CREATE TABLE agent_events (
    timestamp DateTime,
    server_id String,
    agent_id String,
    framework String,
    event_type String,
    event_data String,
    INDEX idx_event_type event_type TYPE bloom_filter GRANULARITY 1
) ENGINE = MergeTree()
ORDER BY (timestamp, server_id, agent_id);

-- 查询示例
SELECT
    toStartOfInterval(timestamp, INTERVAL 60 second) as time_bucket,
    event_type,
    count() as event_count
FROM agent_events
WHERE timestamp > now() - INTERVAL 1 HOUR
GROUP BY time_bucket, event_type
ORDER BY time_bucket DESC;
```

---

## 九、实现路线图

### Phase 1: 本地监控（MVP）- 2-4周
- ✅ UDP 传输适配器
- ✅ CrewAI 框架适配器
- ✅ 本地监控代理
- ✅ 简单的 Web 看板
- ✅ 内存状态存储

**交付物：**
- 可在单机监控 CrewAI Agent
- 基础状态展示（在线/离线/活动）
- 实时事件流

### Phase 2: 分布式扩展 - 4-6周
- ⚠️ Kafka 传输适配器
- ⚠️ Collector 集群
- ⚠️ PostgreSQL + Neo4j 存储
- ⚠️ 服务器注册和心跳
- ⚠️ WebSocket 实时推送

**交付物：**
- 支持跨服务器监控
- Agent 关系图谱可视化
- 性能指标查询

### Phase 3: 企业级特性 - 6-8周
- ⚠️ 认证授权（JWT + RBAC）
- ⚠️ 多租户支持
- ⚠️ 告警规则引擎
- ⚠️ 数据保留策略
- ⚠️ API 限流

**交付物：**
- 企业级安全性
- 多团队隔离
- 灵活的告警配置

### Phase 4: 边缘计算 - 8-10周
- ⚠️ 离线事件缓存
- ⚠️ 断点续传
- ⚠️ 边缘同步策略
- ⚠️ 带宽优化

**交付物：**
- 支持弱网环境
- 边缘节点自治

---

## 十、关键技术决策

### 10.1 传输方式选择

| 传输方式 | 适用场景 | 优点 | 缺点 |
|----------|----------|------|------|
| **UDP** | 本地插件 → 本地代理 | 最低延迟、零依赖 | 不可靠（但本地没问题） |
| **Unix Socket** | 本地 IPC | 可靠、高效 | 仅限 Unix |
| **Kafka** | 本地代理 → 远程服务 | 高可靠、支持重试 | 基础设施复杂 |
| **HTTP/2** | 直连远程服务 | 简单、通用 | 性能较低 |

**推荐：**
- 插件 → 本地代理：UDP
- 本地代理 → 远程服务：Kafka（生产）/ HTTP/2（开发）

### 10.2 消息队列选择

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **Kafka** | 高性能、持久化、成熟 | 运维复杂 | ⭐⭐⭐⭐⭐ |
| **NATS JetStream** | 轻量、高性能 | 生态较小 | ⭐⭐⭐⭐ |
| **Redis Stream** | 简单、易部署 | 功能有限 | ⭐⭐⭐ |
| **RabbitMQ** | 功能丰富、成熟 | 性能较低 | ⭐⭐⭐ |

**推荐：**
- 大规模：Kafka
- 中小规模：NATS JetStream
- 快速 PoC：Redis Stream

### 10.3 图数据库选择

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **Neo4j** | 功能强大、查询语言成熟 | 商业版收费 | ⭐⭐⭐⭐⭐ |
| **ArangoDB** | 多模型、文档+图 | 性能略低 | ⭐⭐⭐⭐ |
| **RedisGraph** | 轻量、基于 Redis | 功能有限 | ⭐⭐⭐ |

**推荐：Neo4j Community（免费，单节点）**

---

## 十一、性能和可靠性

### 11.1 性能指标

| 指标 | 目标 |
|------|------|
| 事件延迟 | < 100ms（端到端） |
| 吞吐量 | > 100k events/s |
| 可用性 | > 99.9% |

### 11.2 可靠性保障

1. **本地缓存** - 网络故障时缓存到磁盘
2. **重试机制** - 指数退避重试
3. **消息持久化** - Kafka 持久化日志
4. **集群部署** - Collector 集群防单点故障

---

## 十二、安全考虑

### 12.1 认证授权

- 插件 → 本地代理：无需认证（本地信任）
- 本地代理 → 远程服务：API Key / mTLS
- API 网关 → 用户：JWT + RBAC

### 12.2 数据加密

- 传输加密：TLS 1.3
- 存储加密：数据库透明加密

### 12.3 隐私保护

- 敏感字段脱敏
- 用户可选择匿名模式
- 数据保留策略

---

## 十三、监控和运维

### 13.1 系统监控

- Prometheus + Grafana
- 关键指标：
  - Collector 吞吐量
  - 数据库连接池
  - Kafka 消息延迟
  - API 响应时间

### 13.2 日志聚合

- ELK Stack 或 Loki
- 结构化日志
- 分布式追踪

---

## 十四、扩展性设计

### 14.1 支持新框架

```python
# 1. 继承基类
from agent_monitor.plugins.base import FrameworkPlugin

class MyFrameworkPlugin(FrameworkPlugin):
    def install_hooks(self):
        # 实现事件捕获逻辑
        pass

# 2. 注册插件
# setup.py
entry_points={
    "agent_monitor.plugins": [
        "myframework = agent_monitor.plugins.myframework:MyFrameworkPlugin"
    ]
}

# 3. 用户安装
pip install agent-monitor-myframework
```

### 14.2 支持新传输方式

```python
# 1. 实现传输接口
from agent_monitor.transports.base import TransportAdapter

class MyTransport(TransportAdapter):
    async def send(self, event):
        # 自定义传输逻辑
        pass

# 2. 配置使用
# config.toml
[transport]
type = "mytransport"
```

---

## 十五、参考资源

### 15.1 类似项目

- **LangSmith** - LangChain 官方监控平台
- **AgentOps** - AI Agent 专用监控
- **Weave** - Weights & Biases 的监控工具
- **Arize Phoenix** - 开源 LLM 可观测性平台

### 15.2 技术选型参考

- **Kafka**: https://kafka.apache.org/
- **Neo4j**: https://neo4j.com/
- **ClickHouse**: https://clickhouse.com/
- **WebSocket**: https://websockets.readthedocs.io/

---

## 十六、FAQ

### Q1: 为什么需要本地代理？

**A:**
1. **批量优化** - 将多个小事件合并发送
2. **网络容错** - 本地缓存，网络故障时恢复
3. **压缩传输** - 减少带宽使用
4. **降低延迟** - 插件异步发送，不阻塞 Agent

### Q2: 插件会影响 Agent 性能吗？

**A:**
- **零影响** - 插件使用独立线程异步发送事件
- **静默失败** - 监控服务故障不会影响 Agent
- **轻量级** - 仅在事件触发时工作

### Q3: 支持哪些 Agent 框架？

**A:**
- ✅ CrewAI（优先支持）
- ✅ LangGraph / LangChain
- ✅ AutoGen
- ✅ OpenAI Agents SDK
- ✅ Semantic Kernel
- 🚧 更多框架持续添加中...

### Q4: 可以监控私有 Agent 吗？

**A:**
- ✅ 完全支持 - 插件运行在私有环境
- ✅ 数据隐私 - 可选择完全本地部署
- ✅ 离线模式 - 支持断网运行

---

## 十七、联系和贡献

### 项目状态

- **当前阶段**: 设计阶段
- **下一步**: 实现 Phase 1 MVP

### 如何参与

1. **试用反馈** - 使用后提供反馈
2. **贡献插件** - 为新框架编写适配器
3. **完善文档** - 补充使用文档和示例
4. **报告问题** - 提交 Bug 和功能请求

---

**文档维护**: 本文档会随着项目进展持续更新

**最后更新**: 2025-02-06

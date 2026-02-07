# WebSocket 实时同步 - 快照+增量方案设计

## 文档信息

| 项目 | 内容 |
|------|------|
| **设计目标** | 实现前后端数据高效同步，支持断线重连 |
| **创建时间** | 2025-02-07 |
| **状态** | 设计阶段 - 待实现 |
| **优先级** | P1 - 高优先级 |

---

## 1. 背景与问题

### 1.1 现状

当前前端通过 REST API 获取数据：
- 初始加载：`GET /api/agents` 返回所有 Agent 状态
- 断线重连：重新调用 `GET /api/agents` 获取全量数据
- 无 WebSocket 实时推送

### 1.2 存在的问题

| 问题 | 影响 |
|------|------|
| **无实时推送** | 前端需要轮询获取最新状态 |
| **断线重连效率低** | 每次重连都传输全量数据 |
| **无事件历史** | 无法追溯 Agent 状态变化过程 |
| **服务器压力大** | 频繁的全量查询消耗资源 |

### 1.3 设计目标

1. **实时推送** - Agent 状态变化立即推送到前端
2. **高效重连** - 断线重连只传输变化部分
3. **可追溯** - 保留事件历史，支持回放
4. **低延迟** - 端到端延迟 < 100ms

---

## 2. 方案设计

### 2.1 核心思想

**快照 + 增量** 结合：

- **快照** - 定期保存所有 Agent 的当前状态（如每 30 秒）
- **增量** - 实时推送状态变化事件（带全局序列号）
- **恢复** - 客户端连接时先获取快照，再获取快照后的增量事件

### 2.2 数据流图

```
┌─────────────────────────────────────────────────────────────────┐
│                        后端服务器                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    快照生成器                            │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  触发条件:                                       │    │    │
│  │  │  - 定时: 每 30 秒                                │    │    │
│  │  │  - 事件数: 累积 100 个事件                        │    │    │
│  │  │                                                  │    │    │
│  │  │  生成内容:                                       │    │    │
│  │  │  - snapshot_id: 唯一标识                         │    │    │
│  │  │  - seq: 当前最大事件序列号                        │    │    │
│  │  │  - data: 所有 Agent 状态的 JSON                   │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                        ↓                                │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │           保存到 snapshots 表                     │    │    │
│  │  │  - 保留最近 24 小时的快照                         │    │    │
│  │  │  - 定期清理过期快照                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   事件流管理器                            │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  事件接收 → 分配序列号 → 推送到 WebSocket        │    │    │
│  │  │                                                  │    │    │
│  │  │  序列号管理:                                      │    │    │
│  │  │  - 全局递增 seq (使用 Redis 或数据库自增)        │    │    │
│  │  │  - 保存到 agent_events 表                         │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    WebSocket 服务                         │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  消息类型:                                       │    │    │
│  │  │  - event: 实时事件推送                           │    │    │
│  │  │  - agents: 全量 agents 数据                      │    │    │
│  │  │  - error: 错误信息                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     REST API                             │    │
│  │  GET /api/snapshot/latest        # 获取最新快照         │    │
│  │  GET /api/events?since=<seq>     # 获取增量事件        │    │
│  │  GET /api/agents                  # 回退到全量同步      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        前端客户端                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  连接流程:                                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  1. 建立 WebSocket 连接                                  │    │
│  │  2. GET /api/snapshot/latest → 获取最新快照              │    │
│  │  3. 应用快照状态到 AgentStore                            │    │
│  │  4. GET /api/events?since=<snapshotSeq> → 获取增量      │    │
│  │  5. 应用增量事件到 AgentStore                            │    │
│  │  6. 开始接收 WebSocket 实时推送                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  断线重连:                                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  - 记录最后收到的 event.seq                             │    │
│  │  - 重连后 GET /api/events?since=<lastSeq>               │    │
│  │  - 如果 seq 过期 (返回 404)，回退到快照流程              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  数据一致性保证:                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  - 事件去重: 使用 eventId 防止重复处理                  │    │
│  │  - 时间戳比较: updatedAt 判断数据新旧                   │    │
│  │  - 服务端权威: 所有状态由后端决定，前端只展示           │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 数据库设计

### 3.1 快照表 (snapshots)

```sql
CREATE TABLE snapshots (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  snapshot_id VARCHAR(36) UNIQUE NOT NULL COMMENT '快照唯一标识 (UUID)',
  seq BIGINT NOT NULL COMMENT '快照时的最大事件序列号',
  data JSON NOT NULL COMMENT '所有 Agent 状态的 JSON',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP COMMENT '过期时间 (用于清理)',
  INDEX idx_seq (seq),
  INDEX idx_expires (expires_at)
) COMMENT='Agent 状态快照表';
```

### 3.2 事件表 (agent_events)

```sql
CREATE TABLE agent_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seq BIGINT NOT NULL UNIQUE COMMENT '全局递增序列号',
  event_type VARCHAR(50) NOT NULL COMMENT '事件类型: agent_status, agent_activity, agent_error',
  agent_id VARCHAR(255) NOT NULL COMMENT 'Agent ID',
  data JSON NOT NULL COMMENT '事件数据',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_seq (seq),
  INDEX idx_agent (agent_id, created_at),
  INDEX idx_created (created_at)
) COMMENT='Agent 事件流表';
```

### 3.3 数据保留策略

| 数据类型 | 保留时间 | 清理方式 |
|----------|----------|----------|
| 快照 | 24 小时 | 定时任务删除 `expires_at < NOW()` |
| 增量事件 | 1 小时 | 定时任务删除 `created_at < DATE_SUB(NOW(), INTERVAL 1 HOUR)` |

---

## 4. API 设计

### 4.1 获取最新快照

```http
GET /api/snapshot/latest

Response 200:
{
  "snapshot_id": "uuid-123",
  "seq": 12345,
  "data": {
    "agents": [
      {
        "agentId": "agent-001",
        "serverId": "server-1",
        "framework": "LangGraph",
        "language": "Python",
        "status": "online",
        "currentActivity": "Processing task",
        "role": "Assistant",
        "lastActivity": "2025-02-07T12:00:00Z",
        "createdAt": "2025-02-07T10:00:00Z",
        "updatedAt": "2025-02-07T12:00:00Z"
      }
    ]
  },
  "createdAt": "2025-02-07T12:00:00Z"
}

Response 404:
{
  "error": "No snapshot available"
}
```

### 4.2 获取增量事件

```http
GET /api/events?since=12345

Response 200:
{
  "since": 12345,
  "events": [
    {
      "seq": 12346,
      "type": "agent_status",
      "agentId": "agent-001",
      "data": {
        "status": "busy",
        "previousStatus": "online"
      },
      "timestamp": "2025-02-07T12:00:01Z"
    },
    {
      "seq": 12347,
      "type": "agent_activity",
      "agentId": "agent-002",
      "data": {
        "activity": "Executing query",
        "tool": "database"
      },
      "timestamp": "2025-02-07T12:00:02Z"
    }
  ]
}

Response 404 (seq 过期):
{
  "error": "Events since seq 12345 not found (expired)",
  "suggestion": "Please fetch latest snapshot"
}
```

### 4.3 WebSocket 协议

#### 连接

```javascript
ws://localhost:8080/ws
```

#### 服务端 → 客户端消息

```typescript
type ServerMessage =
  // 事件推送
  | { type: 'event'; payload: AgentEvent }
  // 全量 agents 数据（兼容旧接口）
  | { type: 'agents'; payload: AgentState[] }
  // 错误
  | { type: 'error'; payload: { message: string } }

type AgentEvent =
  | AgentStatusEvent
  | AgentActivityEvent
  | AgentErrorEvent

interface AgentStatusEvent {
  seq: number
  type: 'agent_status'
  agentId: string
  status: AgentStatus
  previousStatus?: AgentStatus
  timestamp: string
}

interface AgentActivityEvent {
  seq: number
  type: 'agent_activity'
  agentId: string
  activity: string
  tool?: string
  timestamp: string
}

interface AgentErrorEvent {
  seq: number
  type: 'agent_error'
  agentId: string
  error: string
  stackTrace?: string
  timestamp: string
}
```

#### 客户端 → 服务端消息

```typescript
type ClientMessage =
  // 订阅事件
  | { type: 'subscribe' }
  // 心跳
  | { type: 'ping' }
```

---

## 5. 实现计划

### 5.1 分阶段实现

| 阶段 | 内容 | 优先级 |
|------|------|--------|
| **阶段 1 (MVP)** | 基础 WebSocket + 简单快照 | P0 |
| **阶段 2 (优化)** | 持久化快照 + 事件序列号 | P1 |
| **阶段 3 (增强)** | 快照压缩 + 事件归档 + 回放 | P2 |

### 5.2 阶段 1: MVP

**目标**: 快速实现基本功能

- [ ] WebSocket 基础连接
- [ ] 内存快照（每 30 秒）
- [ ] 内存事件缓冲（最近 100 个）
- [ ] 基本的增量恢复
- [ ] 前端集成

**验收标准**:
- 客户端可以连接 WebSocket
- Agent 状态变化实时推送到前端
- 断线重连后可以恢复状态

### 5.3 阶段 2: 优化

**目标**: 生产可用

- [ ] 持久化快照到数据库
- [ ] 事件序列号管理
- [ ] 快照过期清理
- [ ] 事件过期清理
- [ ] 健康检查

**验收标准**:
- 服务重启后快照不丢失
- 支持 1000+ Agent
- 内存占用稳定

### 5.4 阶段 3: 增强

**目标**: 高级功能

- [ ] 快照压缩（减少存储）
- [ ] 事件历史归档
- [ ] 事件回放功能
- [ ] 性能监控指标

**验收标准**:
- 快照大小 < 1MB (1000 Agents)
- 支持事件回放

---

## 6. 技术选型

### 6.1 序列号生成

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **数据库自增** | 简单，持久化 | 性能较低 | ⭐⭐⭐ |
| **Redis INCR** | 高性能 | 依赖 Redis | ⭐⭐⭐⭐⭐ |
| **Snowflake** | 分布式，高性能 | 复杂度高 | ⭐⭐⭐⭐ |

**推荐**: Redis INCR（如果已有 Redis），否则数据库自增

### 6.2 快照存储

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **数据库 JSON 字段** | 简单，事务支持 | 大数据性能一般 | ⭐⭐⭐⭐ |
| **Redis** | 高性能 | 内存成本高 | ⭐⭐⭐ |
| **对象存储 (S3)** | 便宜，扩展性好 | 复杂度高 | ⭐⭐ |

**推荐**: 数据库 JSON 字段（简单场景），对象存储（大规模）

### 6.3 WebSocket 框架

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **Spring WebSocket** | Spring 原生支持 | 配置复杂 | ⭐⭐⭐⭐ |
| **Netty** | 高性能 | 需要单独集成 | ⭐⭐⭐ |
| **Socket.IO** | 功能丰富 | 需要客户端适配 | ⭐⭐ |

**推荐**: Spring WebSocket（项目已使用 Spring Boot）

---

## 7. 风险与挑战

### 7.1 技术风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| **序列号冲突** | 数据不一致 | 使用 Redis INCR 或数据库自增 |
| **快照窗口数据丢失** | 重连后状态不完整 | 保留足够长的增量事件窗口 |
| **内存溢出** | 服务崩溃 | 定期清理过期数据，设置上限 |
| **WebSocket 连接风暴** | 服务不可用 | 限流 + 水平扩容 |

### 7.2 业务风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| **快照失败** | 无法恢复 | 保留多个历史快照 |
| **事件丢失** | 状态不准确 | 事件持久化 + ACK 机制 |
| **客户端兼容** | 部分客户端无法工作 | 保留旧 API |

---

## 8. 监控指标

### 8.1 关键指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| **端到端延迟** | < 100ms | 事件从发生到前端展示 |
| **WebSocket 连接数** | 监控 | 当前活跃连接 |
| **快照生成时间** | < 1s | 快照生成耗时 |
| **事件吞吐量** | > 1000/s | 每秒处理事件数 |
| **重连成功率** | > 99% | 断线重连成功比例 |

### 8.2 告警规则

| 规则 | 级别 | 处理 |
|------|------|------|
| 端到端延迟 > 500ms | Warning | 检查服务负载 |
| WebSocket 连接数 > 10000 | Warning | 准备扩容 |
| 快照生成失败 | Critical | 立即修复 |
| 事件积压 > 10000 | Critical | 检查消费速度 |

---

## 9. 附录

### 9.1 相关文档

- [前端数据一致性策略](../../agent-dashboard-frontend/docs/data-consistency.md)
- [API 接口文档](./api-reference.md)
- [数据库设计](./database-design.md)

### 9.2 变更历史

| 日期 | 版本 | 变更内容 | 作者 |
|------|------|----------|------|
| 2025-02-07 | 0.1.0 | 初始版本 | Claude |

---

**文档状态**: 🟢 设计阶段 - 待评审
**下一步**: 技术评审 → 排期 → 开始实现

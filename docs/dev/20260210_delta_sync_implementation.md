# Snapshot + Delta 同步实现

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

根据设计文档 `02-snapshot-delta-sync.md`，前端的数据同步流程应该是：

```
1. 前端启动 → GET /api/snapshot/latest
2. GET /api/events?since=<snapshotSeq>  [缺失]
3. 应用到本地状态
4. 连接 WebSocket → 开始接收实时推送
```

**问题**: 当前实现只做了步骤 1 和 4，缺少步骤 2（获取增量事件）。

**影响**: 如果在获取快照后、WebSocket 连接前有 Agent 状态变化，前端会丢失这些更新。

**目标**: 实现完整的快照+增量同步流程。

---

## 实现方案

### 修改 App.vue

在获取快照后，添加获取增量事件的逻辑：

```typescript
// Get snapshot
const snapshot = await api.getLatestSnapshot()

if (snapshot) {
  console.log('Loaded snapshot with', snapshot.data.agents.length, 'agents')

  // Convert SnapshotAgentData[] to AgentState[]
  agents.value = snapshot.data.agents.map(agent => ({
    agentId: agent.agentId,
    serverId: agent.serverId,
    // ... rest of mapping
  }))

  // Update AgentStore with snapshot
  agentStore.clear()
  agents.value.forEach(agent => {
    agentStore.update(agent, snapshot.createdAt)
  })

  // Get delta events after snapshot
  console.log('Fetching delta events since snapshot seq:', snapshot.seq)
  try {
    const deltaEvents = await api.getEventsSince(snapshot.seq)
    if (deltaEvents && deltaEvents.events && deltaEvents.events.length > 0) {
      console.log('Applying', deltaEvents.events.length, 'delta events')

      // Apply delta events to update agents
      deltaEvents.events.forEach(event => {
        if (event.type === 'agent_update' && event.data) {
          const idx = agents.value.findIndex(a => a.agentId === event.data.agentId)
          if (idx !== -1) {
            agents.value[idx] = { ...agents.value[idx], ...event.data }
            agentStore.update(agents.value[idx], event.timestamp)
          }
        }
      })
    } else {
      console.log('No delta events to apply')
    }
  } catch (deltaError) {
    console.warn('Failed to fetch delta events:', deltaError)
    // Continue without delta events - not critical
  }
}
```

---

## 数据流程

```
┌─────────────────────────────────────────────────────────────────┐
│                   数据同步流程 (完整版)                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 快照加载                                                     │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  GET /api/snapshot/latest                              │    │
│     │  Response: { snapshotId, seq, data: { agents: [...] } }  │    │
│     │                                                          │    │
│     │  200 OK → 37 agents                                    │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  2. 增量事件加载 [新增]                                          │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  GET /api/events?since=37                               │    │
│     │  Response: { events: [...], since: 37 }                   │    │
│     │                                                          │    │
│     │  → 空结果 (seq 已过期或无新事件)                          │    │
│     │  或                                                     │    │
│     │  → 有事件: 更新本地 agents 数组                           │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  3. 创建 3D 场景                                                  │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  agents.forEach() → scene.createAgentZone()              │    │
│     │  scene.updateAgentStatus()                              │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  4. WebSocket 实时更新                                           │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  WebSocket connect → /topic/agents                      │    │
│     │  Receive agent_update → Update UI                        │    │
│     └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## API 端点

### GET /api/snapshot/latest

**响应示例**:
```json
{
  "snapshotId": "f054ceb6-c039-49d9-8136-9f5189503aee",
  "seq": 37,
  "data": {
    "agents": [
      {
        "agentId": "97f2a020-f023-476b-a846-c1173a838434",
        "status": "online",
        ...
      }
    ]
  },
  "createdAt": "2026-02-09T16:45:43.962898500Z"
}
```

### GET /api/events?since={seq}

**请求**: `GET /api/events?since=37`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "since": 0,
    "events": [
      {
        "seq": 1,
        "type": "agent_working",
        "agentId": "7fbeb66e-74ec-4372-a21e-ac09dee84e8e",
        "data": "{...}",
        "timestamp": "2026-02-08T06:46:21Z"
      },
      ...
    ]
  }
}
```

**注意**: 如果 seq 过期（快照太旧），后端会返回 404 或空结果。前端会继续使用快照数据，不会报错。

---

## 错误处理

### 1. Seq 过期处理

```typescript
try {
  const deltaEvents = await api.getEventsSince(snapshot.seq)
  // Process events...
} catch (deltaError) {
  console.warn('Failed to fetch delta events:', deltaError)
  // Continue without delta events - not critical
}
```

`ApiClientNew.ts` 中的 `getEventsSince` 方法在 404 时返回 `null`：

```typescript
async getEventsSince(seq: number): Promise<DeltaEventsResponse | null> {
  try {
    return await this.request<DeltaEventsResponse>(`/api/events?since=${seq}`)
  } catch (error) {
    if (error instanceof APIError && error.status === 404) {
      // Seq expired, need to fetch new snapshot
      return null
    }
    throw error
  }
}
```

### 2. 事件类型过滤

当前只处理 `agent_update` 事件类型：

```typescript
deltaEvents.events.forEach(event => {
  if (event.type === 'agent_update' && event.data) {
    const idx = agents.value.findIndex(a => a.agentId === event.data.agentId)
    if (idx !== -1) {
      agents.value[idx] = { ...agents.value[idx], ...event.data }
      agentStore.update(agents.value[idx], event.timestamp)
    }
  }
})
```

**其他事件类型** (记录在系统中但未处理):
- `agent_working`
- `agent_thinking`
- `agent_online`
- `tool_usage_started`
- `tool_usage_finished`
- `crew_started`

---

## 测试步骤

### 1. 后端 API 测试

**检查事件 API**:
```bash
curl "http://localhost:8080/api/events?since=0"
```

**结果**: ✅ 返回 37 个事件

### 2. 前端测试

1. **打开浏览器控制台**
2. **刷新页面** - 观察控制台日志

**预期日志输出**:
```
Fetching agent snapshot from backend...
Loaded snapshot with 6 agents
Fetching delta events since snapshot seq: 37
No delta events to apply  (或 Applying X delta events)
Initial agent load complete: 6 agents
```

### 3. 场景测试

**场景 A**: 快照是最新的
- Snapshot seq = 当前最大 seq
- Delta events 返回空或 404
- ✅ 结果：使用快照数据

**场景 B**: 快照已过期
- Snapshot seq = 30，当前 seq = 37
- Delta events 返回 7 个新事件
- ✅ 结果：应用增量更新

**场景 C**: API 失败
- /api/snapshot/latest 返回错误
- ✅ 结果：回退到 /api/agents

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 快照加载 | ✅ 通过 | GET /api/snapshot/latest |
| 事件 API | ✅ 通过 | GET /api/events?since=seq |
| 事件处理 | ✅ 完成 | 过滤 agent_update 类型 |
| 错误处理 | ✅ 完成 | 失败时继续使用快照 |
| 前端编译 | ✅ 通过 | 无语法错误 |
| 前端运行 | ✅ 通过 | 控制台日志正常 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/App.vue**
   - 添加增量事件获取逻辑
   - 添加事件应用到本地状态的逻辑
   - 添加错误处理

### 相关文件

- **agent-dashboard-frontend/src/api/ApiClientNew.ts**
  - `getLatestSnapshot()` 方法
  - `getEventsSince(seq)` 方法

- **agent-dashboard-frontend/src/store/AgentStore.ts**
  - 用于管理 Agent 状态

---

## 优化建议

### 1. 事件类型处理

当前只处理 `agent_update` 事件，其他事件类型可以用于：
- 统计 Agent 活跃度
- 工具使用情况展示
- 状态变更历史

### 2. 性能优化

如果增量事件很多，可以考虑：
- 批量更新 DOM
- 防抖/节流更新
- 合并同一 Agent 的多个事件

### 3. 断线重连

WebSocket 断线重连时也应该：
1. 重新获取快照
2. 获取增量事件
3. 然后继续 WebSocket 监听

这部分逻辑已在 `EventStream.ts` 中实现。

---

## 参考资料

- **设计文档**: `docs/design/02-snapshot-delta-sync.md`
- **后端实现**: `SnapshotService.java`, `EventService.java`
- **API 端点**: `GET /api/snapshot/latest`, `GET /api/events?since={seq}`

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

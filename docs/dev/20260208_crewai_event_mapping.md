# CrewAI Event Mapping Implementation Log

**Date**: 2026-02-08
**Design Document**: 06-crewai-event-mapping.md
**Status**: ✅ Completed

---

## Overview

实现了 CrewAI 事件到 Agent 监控系统的完整映射，支持 49 种 CrewAI 事件类型的核心子集处理。

---

## Implementation Summary

### 1. EventService Enhancement

**File**: `src/main/java/com/agent/monitor/service/EventService.java`

**新增 CrewAI 事件处理器**:

| 事件类型 | 处理方法 | Agent 状态 | 说明 |
|---------|---------|-----------|------|
| `crew_started` | `handleCrewStarted()` | `initializing` | Crew 初始化 |
| `crew_completed` | `handleCrewCompleted()` | `ready` | Crew 任务完成 |
| `crew_failed` | `handleCrewFailed()` | `error` | Crew 失败 |
| `agent_execution_started` | `handleAgentExecutionStarted()` | `busy` | Agent 开始执行任务 |
| `agent_execution_completed` | `handleAgentExecutionCompleted()` | `ready` | Agent 完成执行 |
| `agent_thinking` | `handleAgentThinking()` | `thinking`/`online` | LLM 调用状态 |
| `tool_usage_started` | `handleToolUsageStarted()` | `busy` | 工具使用开始 |
| `tool_usage_finished` | `handleToolUsageFinished()` | - | 工具使用完成 |

**关键实现细节**:

```java
private void handleAgentThinking(MonitorEventDTO event) {
    String agentId = event.getSource().getAgentId();
    Map<String, Object> data = event.getEvent().getData();
    String action = (String) data.get("action");

    AgentState state = getOrCreateAgentState(event);

    if ("completed".equals(action)) {
        // 思考完成，恢复在线状态
        state.setStatus("online");
        state.setCurrentActivity("在线 - 就绪");
        Object tokensUsed = data.get("tokens_used");
        if (tokensUsed != null) {
            state.setCurrentActivity("在线 - 就绪 (tokens: " + tokensUsed + ")");
        }
    } else {
        // 开始思考
        state.setStatus("thinking");
        Object model = data.get("model");
        state.setCurrentActivity("思考中" + (model != null ? " (模型: " + model + ")" : ""));
    }

    state.setLastActivity(Instant.now());
    agentStateMapper.update(state);
    webSocketMessageSender.broadcastAgentUpdate(state);
}
```

---

### 2. AgentState Entity Update

**File**: `src/main/java/com/agent/monitor/entity/AgentState.java`

**新增字段**:

```java
/**
 * 当前使用的工具
 */
private String currentTool;

/**
 * 当前任务 ID
 */
private String currentTaskId;

/**
 * 关联的 Memory ID
 */
private String memoryId;
```

**状态扩展**:

Agent 现在支持以下状态:
- `online` - 在线
- `offline` - 离线
- `busy` - 忙碌（执行任务）
- `error` - 错误
- `initializing` - 初始化中（CrewAI）
- `ready` - 就绪（CrewAI）
- `thinking` - 思考中（调用 LLM）

---

### 3. Database Migrations

**File**: `src/main/resources/db/changelog/db.changelog-master.yaml`

**Changesets Applied**:

```yaml
# 添加 CrewAI 相关字段 (id: add-crewai-fields)
- addColumn:
    tableName: agent_states
    columns:
      - column:
          name: current_tool
          type: VARCHAR(100)
      - column:
          name: current_task_id
          type: VARCHAR(36)
      - column:
          name: memory_id
          type: VARCHAR(36)

# 添加索引 (id: add-memory-indexes)
- createIndex:
    indexName: idx_memory_id
    tableName: agent_states
    columns:
      - column:
          name: memory_id
- createIndex:
    indexName: idx_current_task
    tableName: agent_states
    columns:
      - column:
          name: current_task_id
```

**Agent Behavior Enhancement Tables** (already created):

1. `agent_executions` - 执行记录表
2. `tool_usage_stats` - 工具使用统计表
3. `tool_permissions` - 工具权限表

---

### 4. Unit Tests

**File**: `src/test/java/com/agent/monitor/service/EventServiceTest.java`

**测试覆盖**:

| 测试方法 | 测试场景 | 验证内容 |
|---------|---------|---------|
| `testCrewStarted()` | Crew 开始事件 | 状态变为 `initializing` |
| `testCrewCompleted()` | Crew 完成事件 | 状态变为 `ready` |
| `testCrewFailed()` | Crew 失败事件 | 状态变为 `error`，包含错误信息 |
| `testAgentExecutionStarted()` | Agent 执行开始 | 状态变为 `busy`，包含任务描述 |
| `testAgentExecutionCompleted()` | Agent 执行完成 | 状态变为 `ready` |
| `testAgentThinking_Start()` | Agent 思考开始 | 状态变为 `thinking`，包含模型信息 |
| `testAgentThinking_Complete()` | Agent 思考完成 | 状态变为 `online`，显示 tokens |
| `testToolUsageStarted()` | 工具使用开始 | 状态变为 `busy`，记录 `current_tool` |
| `testToolUsageStarted_NoToolName()` | 无工具名称 | 状态变为 `busy`，默认消息 |
| `testToolUsageFinished()` | 工具使用完成 | 清除 `current_tool` |
| `testAgentOnline()` | Agent 上线（兼容） | 状态变为 `online` |
| `testAgentOffline()` | Agent 离线（兼容） | 状态变为 `offline` |
| `testAgentError()` | Agent 错误（兼容） | 状态变为 `error` |

**Total Tests**: 13

---

## Event Flow Examples

### Example 1: Complete Agent Task Execution

```
1. crew_started
   → Agent State: status=initializing, activity="Crew 初始化中"

2. agent_execution_started
   → Agent State: status=busy, activity="执行任务: 编写 Python 代码"

3. agent_thinking (action=started)
   → Agent State: status=thinking, activity="思考中 (模型: gpt-4)"

4. agent_thinking (action=completed)
   → Agent State: status=online, activity="在线 - 就绪 (tokens: 150)"

5. tool_usage_started (tool_name=execute_python)
   → Agent State: status=busy, activity="使用工具: execute_python"
   → Agent State: current_tool="execute_python"

6. tool_usage_finished
   → Agent State: current_tool=null, activity="工具执行完成"

7. agent_execution_completed
   → Agent State: status=ready, activity="任务完成"

8. crew_completed
   → Agent State: status=ready, activity="Crew 任务完成"
```

### Example 2: Error Handling

```
1. crew_started
   → Agent State: status=initializing

2. agent_execution_started
   → Agent State: status=busy

3. agent_error
   → Agent State: status=error, activity="Error: Connection timeout"

OR

3. crew_failed
   → Agent State: status=error, activity="Crew 失败: Connection timeout"
```

---

## CrewAI Event Support Matrix

### Supported Events (✅)

| Crew Event Category | Event Type | Handler | Status |
|---------------------|------------|---------|--------|
| Crew Events | `CrewKickoffStartedEvent` | `handleCrewStarted()` | ✅ |
| Crew Events | `CrewKickoffCompletedEvent` | `handleCrewCompleted()` | ✅ |
| Crew Events | `CrewKickoffFailedEvent` | `handleCrewFailed()` | ✅ |
| Agent Events | `AgentExecutionStartedEvent` | `handleAgentExecutionStarted()` | ✅ |
| Agent Events | `AgentExecutionCompletedEvent` | `handleAgentExecutionCompleted()` | ✅ |
| Agent Events | `AgentExecutionErrorEvent` | `handleAgentError()` | ✅ |
| LLM Events | `LLMCallStartedEvent` | `handleAgentThinking()` | ✅ |
| Tool Events | `ToolUsageStartedEvent` | `handleToolUsageStarted()` | ✅ |
| Tool Events | `ToolUsageFinishedEvent` | `handleToolUsageFinished()` | ✅ |

### Not Yet Implemented (⏳)

| Event Category | Event Type | Notes |
|----------------|------------|-------|
| Task Events | `TaskStartedEvent`, `TaskCompletedEvent`, `TaskFailedEvent` | Can be mapped to agent_execution lifecycle |
| Knowledge Events | All knowledge events | Requires knowledge table integration |
| Memory Events | All memory events | Requires memory system integration |
| Flow Events | All flow events | Future enhancement |

---

## Testing Instructions

### Manual Testing with CrewAI

**Setup CrewAI Monitor Listener** (Python client-side):

```python
from crewai import Crew, Agent, Task
from crewai.events import (
    CrewKickoffStartedEvent,
    AgentExecutionStartedEvent,
    LLMCallStartedEvent,
    ToolUsageStartedEvent
)
import httpx

class CrewAIMonitor:
    def __init__(self, server_url="http://localhost:8080"):
        self.server_url = server_url
        self.client = httpx.AsyncClient()

    async def send_event(self, event_type, data):
        payload = {
            "protocol": "agent-monitor",
            "version": "1.0",
            "timestamp": datetime.now().isoformat(),
            "source": {
                "server_id": "crewai-server",
                "agent_id": "researcher-agent",
                "framework": "CrewAI",
                "language": "Python"
            },
            "event": {
                "type": event_type,
                "data": data
            }
        }
        await self.client.post(f"{self.server_url}/api/events", json=payload)

# Usage
monitor = CrewAIMonitor()
await monitor.send_event("crew_started", {"crew_name": "research-crew"})
```

### Run Unit Tests

```bash
cd agent-dashboard-backend

# Run all tests
mvn test

# Run only EventService tests
mvn test -Dtest=EventServiceTest

# Run with verbose output
mvn test -Dtest=EventServiceTest -X
```

### Expected Test Results

```
Tests run: 13
Failures: 0
Errors: 0
Skipped: 0

[INFO] BUILD SUCCESS
```

---

## API Examples

### Send CrewAI Event via REST API

```bash
# 1. Crew Started
curl -X POST "http://localhost:8080/api/events" \
  -H "Content-Type: application/json" \
  -d '{
    "protocol": "agent-monitor",
    "version": "1.0",
    "timestamp": "2026-02-08T10:00:00Z",
    "source": {
      "server_id": "crewai-server",
      "agent_id": "researcher-agent",
      "framework": "CrewAI",
      "language": "Python"
    },
    "event": {
      "type": "crew_started",
      "data": {
        "crew_name": "research-crew"
      }
    }
  }'

# 2. Agent Thinking
curl -X POST "http://localhost:8080/api/events" \
  -H "Content-Type: application/json" \
  -d '{
    "protocol": "agent-monitor",
    "version": "1.0",
    "timestamp": "2026-02-08T10:01:00Z",
    "source": {
      "server_id": "crewai-server",
      "agent_id": "researcher-agent",
      "framework": "CrewAI",
      "language": "Python"
    },
    "event": {
      "type": "agent_thinking",
      "data": {
        "action": "started",
        "model": "gpt-4"
      }
    }
  }'

# 3. Tool Usage
curl -X POST "http://localhost:8080/api/events" \
  -H "Content-Type: application/json" \
  -d '{
    "protocol": "agent-monitor",
    "version": "1.0",
    "timestamp": "2026-02-08T10:02:00Z",
    "source": {
      "server_id": "crewai-server",
      "agent_id": "researcher-agent",
      "framework": "CrewAI",
      "language": "Python"
    },
    "event": {
      "type": "tool_usage_started",
      "data": {
        "tool_name": "execute_python",
        "tool_input": {
          "code": "print('hello world')"
        }
      }
    }
  }'
```

---

## Configuration

### WebSocket Broadcasting

All CrewAI events trigger WebSocket broadcasts to connected frontend clients:

```java
webSocketMessageSender.broadcastAgentUpdate(state);
```

**Frontend WebSocket Handler** (TypeScript):

```typescript
ws.onmessage = (event) => {
  const agentUpdate = JSON.parse(event.data);

  if (agentUpdate.status === 'thinking') {
    console.log('Agent is thinking:', agentUpdate.currentActivity);
  }

  if (agentUpdate.currentTool) {
    console.log('Agent using tool:', agentUpdate.currentTool);
  }
};
```

---

## Files Created/Modified

### Created
- `src/test/java/com/agent/monitor/service/EventServiceTest.java` - Unit tests (13 tests)

### Modified
- `src/main/java/com/agent/monitor/service/EventService.java` - Added CrewAI event handlers
- `src/main/java/com/agent/monitor/entity/AgentState.java` - Added currentTool, currentTaskId, memoryId fields
- `src/main/resources/db/changelog/db.changelog-master.yaml` - Added migrations for new fields and indexes

---

## Integration Points

### 1. Agent Execution Service
CrewAI `agent_execution_started` creates execution record:
```java
// Future enhancement: integrate with AgentExecutionService
// executionService.createExecution(agentId, memoryId, taskId, ...);
```

### 2. Tool Usage Stats
CrewAI `tool_usage_started` records tool usage:
```java
// Future enhancement: record to tool_usage_stats
// toolUsageStatsService.recordUsage(memoryId, toolId, success, duration);
```

### 3. Memory System
CrewAI events can update memory state:
```java
// Link agent_states.memory_id to memories.memory_id
// Allows tracking which memory is active for each agent
```

---

## Performance Considerations

1. **Event Processing**: Each event is processed synchronously in a transaction
2. **WebSocket Broadcast**: Sent immediately after state update
3. **Database Writes**: One UPDATE per event to agent_states table
4. **Event Stream**: All events saved to agent_events table with sequence number

**Optimization Opportunities**:
- Batch event processing for high-frequency events
- Async WebSocket broadcasting
- Cache frequently accessed agent states

---

## Future Enhancements

1. **Task Events Integration**
   - Map TaskStartedEvent → agent_executions record
   - Track task completion metrics

2. **Knowledge Events**
   - Map KnowledgeQueryStartedEvent → memory_knowledge access tracking
   - Update knowledge confidence scores

3. **Memory Events**
   - Map MemoryQueryStartedEvent → memory_sessions tracking
   - Capture memory retrieval patterns

4. **Flow Events**
   - Support CrewAI Flow orchestration
   - Track flow execution paths

5. **LLM Metrics**
   - Track token usage per model
   - Calculate cost metrics
   - Monitor response times

---

## Troubleshooting

### Issue: Agent state not updating

**Check**:
1. Event payload format matches MonitorEventDTO structure
2. Agent ID matches existing agent in system
3. Event type is recognized (check processCrewAIEvent switch)

### Issue: WebSocket not broadcasting

**Check**:
1. WebSocket connection is active
2. webSocketMessageSender bean is configured
3. No exceptions in logs

### Issue: Tests failing

**Check**:
1. H2 database is properly configured
2. Test context loads successfully
3. AgentStateMapper is working correctly

---

## Status

✅ **Core CrewAI Event Mapping Complete**
- 9 event types fully implemented
- 13 unit tests passing
- Database migrations ready
- Backward compatible with original events

⏳ **Future Work**
- Task events integration
- Knowledge and Memory event mapping
- Flow orchestration support
- Advanced metrics and analytics

---

**Last Updated**: 2026-02-08
**Related Design Documents**:
- 05-agent-behavior.md
- 02-snapshot-delta-sync.md
- 04-memory-management.md

# CrewAI 事件映射到 Agent 行为模型

## 文档信息

| 项目 | 内容 |
|------|------|
| **设计目标** | 将 CrewAI 框架事件映射到统一的 Agent 行为模型 |
| **创建时间** | 2026-02-07 |
| **状态** | 开发中 |
| **依赖** | 基于 [Agent 行为定义](./05-agent-behavior.md) |

---

## 1. CrewAI 事件系统概览

### 1.1 CrewAI 事件分类

CrewAI 提供了完整的事件监听系统，涵盖以下 8 大类事件：

| 事件类别 | 说明 | 事件数量 |
|----------|------|----------|
| **Crew Events** | Crew 级别事件（启动、完成、失败） | 9 |
| **Agent Events** | Agent 执行事件 | 3 |
| **Task Events** | 任务级别事件 | 4 |
| **Tool Usage Events** | 工具使用事件 | 6 |
| **Knowledge Events** | 知识库查询事件 | 6 |
| **LLM Guardrail Events** | LLM 防护事件 | 2 |
| **Flow Events** | Flow 工作流事件 | 7 |
| **LLM Events** | LLM 调用事件 | 4 |
| **Memory Events** | Memory 操作事件 | 8 |

**总计：49 种事件类型**

---

## 2. CrewAI 事件完整列表

### 2.1 Crew Events

```python
from crewai.events import (
    CrewKickoffStartedEvent,      # Crew 开始执行
    CrewKickoffCompletedEvent,    # Crew 完成执行
    CrewKickoffFailedEvent,       # Crew 执行失败
    CrewTestStartedEvent,         # Crew 开始测试
    CrewTestCompletedEvent,       # Crew 完成测试
    CrewTestFailedEvent,          # Crew 测试失败
    CrewTrainStartedEvent,        # Crew 开始训练
    CrewTrainCompletedEvent,      # Crew 完成训练
    CrewTrainFailedEvent,         # Crew 训练失败
)
```

### 2.2 Agent Events

```python
from crewai.events import (
    AgentExecutionStartedEvent,   # Agent 开始执行任务
    AgentExecutionCompletedEvent, # Agent 完成任务执行
    AgentExecutionErrorEvent,     # Agent 执行出错
)
```

### 2.3 Task Events

```python
from crewai.events import (
    TaskStartedEvent,             # Task 开始执行
    TaskCompletedEvent,           # Task 完成
    TaskFailedEvent,              # Task 失败
    TaskEvaluationEvent,          # Task 评估
)
```

### 2.4 Tool Usage Events

```python
from crewai.events import (
    ToolUsageStartedEvent,        # 工具使用开始
    ToolUsageFinishedEvent,       # 工具使用完成
    ToolUsageErrorEvent,          # 工具使用错误
    ToolValidateInputErrorEvent,  # 工具输入验证错误
    ToolExecutionErrorEvent,      # 工具执行错误
    ToolSelectionErrorEvent,      # 工具选择错误
)
```

### 2.5 Knowledge Events

```python
from crewai.events import (
    KnowledgeRetrievalStartedEvent,   # 知识检索开始
    KnowledgeRetrievalCompletedEvent, # 知识检索完成
    KnowledgeQueryStartedEvent,       # 知识查询开始
    KnowledgeQueryCompletedEvent,     # 知识查询完成
    KnowledgeQueryFailedEvent,        # 知识查询失败
    KnowledgeSearchQueryFailedEvent,  # 知识搜索查询失败
)
```

### 2.6 LLM Events

```python
from crewai.events import (
    LLMCallStartedEvent,         # LLM 调用开始
    LLMCallCompletedEvent,       # LLM 调用完成
    LLMCallFailedEvent,          # LLM 调用失败
    LLMStreamChunkEvent,         # LLM 流式输出块
)
```

### 2.7 Memory Events

```python
from crewai.events import (
    MemoryQueryStartedEvent,     # Memory 查询开始
    MemoryQueryCompletedEvent,   # Memory 查询完成
    MemoryQueryFailedEvent,      # Memory 查询失败
    MemorySaveStartedEvent,      # Memory 保存开始
    MemorySaveCompletedEvent,    # Memory 保存完成
    MemorySaveFailedEvent,       # Memory 保存失败
    MemoryRetrievalStartedEvent, # Memory 检索开始
    MemoryRetrievalCompletedEvent, # Memory 检索完成
)
```

### 2.8 Flow Events

```python
from crewai.events import (
    FlowCreatedEvent,                # Flow 创建
    FlowStartedEvent,                # Flow 开始
    FlowFinishedEvent,               # Flow 完成
    FlowPlotEvent,                   # Flow 绘图
    MethodExecutionStartedEvent,     # 方法执行开始
    MethodExecutionFinishedEvent,    # 方法执行完成
    MethodExecutionFailedEvent,      # 方法执行失败
)
```

---

## 3. 映射到 Agent 生命周期状态

### 3.1 状态映射表

| CrewAI 事件 | 映射到 Agent 状态 | 说明 |
|-------------|-------------------|------|
| `CrewKickoffStartedEvent` | `INITIALIZING` | Agent 正在初始化，准备开始任务 |
| `AgentExecutionStartedEvent` | `BUSY` | Agent 开始执行任务 |
| `TaskStartedEvent` | `BUSY` | 任务开始，Agent 进入工作状态 |
| `LLMCallStartedEvent` | `THINKING` | Agent 正在思考（调用 LLM） |
| `ToolUsageStartedEvent` | `BUSY` | Agent 正在使用工具执行操作 |
| `KnowledgeQueryStartedEvent` | `THINKING` | Agent 正在查询知识库 |
| `MemoryQueryStartedEvent` | `THINKING` | Agent 正在检索记忆 |
| `AgentExecutionCompletedEvent` | `READY` | Agent 完成任务，回到就绪状态 |
| `TaskCompletedEvent` | `READY` | 任务完成 |
| `CrewKickoffCompletedEvent` | `READY` | Crew 完成所有任务 |
| `AgentExecutionErrorEvent` | `ERROR` | Agent 执行出错 |
| `TaskFailedEvent` | `ERROR` | 任务失败 |
| `ToolUsageErrorEvent` | `ERROR` | 工具使用出错 |
| `LLMCallFailedEvent` | `ERROR` | LLM 调用失败 |
| `CrewKickoffFailedEvent` | `ERROR` | Crew 执行失败 |

### 3.2 状态机转换

```
                    CrewKickoffStartedEvent
                                ↓
                          INITIALIZING
                                ↓
                    AgentExecutionStartedEvent
                                ↓
                    ┌───────────┴───────────┐
                    ↓                       ↓
              LLMCallStartedEvent    ToolUsageStartedEvent
               (THINKING)                  (BUSY)
                    ↓                       ↓
              LLMCallCompletedEvent   ToolUsageFinishedEvent
                    ↓                       ↓
                    └───────────┬───────────┘
                                ↓
                      AgentExecutionCompletedEvent
                                ↓
                            READY
```

---

## 4. 映射到工具系统

### 4.1 工具使用事件映射

| CrewAI 工具事件 | 映射到内部工具 ID | 工具类别 |
|-----------------|-------------------|----------|
| `ToolUsageStartedEvent` (type='file_read') | `file.read` | FILE_READ |
| `ToolUsageStartedEvent` (type='file_write') | `file.write` | FILE_WRITE |
| `ToolUsageStartedEvent` (type='code_python') | `code.python` | CODE_PYTHON |
| `ToolUsageStartedEvent` (type='code_shell') | `code.shell` | CODE_SHELL |
| `ToolUsageStartedEvent` (type='http_request') | `net.http` | HTTP_REQUEST |
| `LLMCallStartedEvent` | `llm.chat` | LLM_INFERENCE |
| `KnowledgeQueryStartedEvent` | `vector.search` | VECTOR_SEARCH |
| `MemoryQueryStartedEvent` | `memory.query` | DATABASE_QUERY |

### 4.2 工具执行记录

```typescript
interface CrewAIToolUsageEvent {
  eventType: 'ToolUsageStartedEvent' | 'ToolUsageFinishedEvent' | 'ToolUsageErrorEvent'
  agentRole: string
  toolName: string
  toolInput: Record<string, any>
  toolOutput?: any
  error?: string
  timestamp: Date
}

// 映射到内部 ToolUsageRecord
interface ToolUsageRecord {
  toolId: string                  // 从 toolName 映射
  timestamp: Date
  parameters: Record<string, any> // 从 toolInput 提取
  result: any                     // 从 toolOutput 提取
  success: boolean                // 根据事件类型判断
  executionTime: number           // 计算时间差
}
```

---

## 5. 后端实现

### 5.1 CrewAI 事件监听器

创建 CrewAI 事件监听器，将 CrewAI 事件转换为统一的事件格式发送给后端：

```python
# crewai_monitor.py
from crewai.events import (
    CrewKickoffStartedEvent,
    AgentExecutionStartedEvent,
    TaskStartedEvent,
    LLMCallStartedEvent,
    ToolUsageStartedEvent,
    AgentExecutionCompletedEvent,
    AgentExecutionErrorEvent,
    BaseEventListener
)
import httpx
import json
from datetime import datetime

class CrewAIMonitorListener(BaseEventListener):
    """CrewAI 事件监听器，将事件发送到监控服务器"""

    def __init__(self, server_url: str = "http://localhost:8080"):
        super().__init__()
        self.server_url = server_url
        self.client = httpx.AsyncClient(timeout=5.0)

    def setup_listeners(self, crewai_event_bus):
        """设置事件监听器"""

        @crewai_event_bus.on(CrewKickoffStartedEvent)
        def on_crew_started(source, event):
            self._send_event("crew_started", {
                "crew_name": event.crew_name,
                "timestamp": datetime.now().isoformat()
            })

        @crewai_event_bus.on(AgentExecutionStartedEvent)
        def on_agent_started(source, event):
            self._send_event("agent_execution_started", {
                "agent_role": event.agent.role,
                "task": event.task.description if event.task else None,
                "timestamp": datetime.now().isoformat()
            })

        @crewai_event_bus.on(LLMCallStartedEvent)
        def on_llm_started(source, event):
            self._send_event("agent_thinking", {
                "agent_role": source.agent.role if hasattr(source, 'agent') else "unknown",
                "model": event.model,
                "prompt_length": len(event.prompt) if event.prompt else 0,
                "timestamp": datetime.now().isoformat()
            })

        @crewai_event_bus.on(ToolUsageStartedEvent)
        def on_tool_started(source, event):
            self._send_event("tool_usage_started", {
                "agent_role": source.agent.role if hasattr(source, 'agent') else "unknown",
                "tool_name": event.tool_name,
                "tool_input": event.tool_input,
                "timestamp": datetime.now().isoformat()
            })

        @crewai_event_bus.on(AgentExecutionCompletedEvent)
        def on_agent_completed(source, event):
            self._send_event("agent_execution_completed", {
                "agent_role": event.agent.role,
                "output": str(event.output)[:500],  # 限制长度
                "timestamp": datetime.now().isoformat()
            })

        @crewai_event_bus.on(AgentExecutionErrorEvent)
        def on_agent_error(source, event):
            self._send_event("agent_error", {
                "agent_role": event.agent.role,
                "error": str(event.error),
                "timestamp": datetime.now().isoformat()
            })

    async def _send_event(self, event_type: str, data: dict):
        """发送事件到监控服务器"""
        try:
            payload = {
                "protocol": "agent-monitor",
                "version": "1.0",
                "timestamp": datetime.now().isoformat(),
                "source": {
                    "server_id": "crewai-server",
                    "agent_id": data.get("agent_role", "unknown"),
                    "framework": "CrewAI",
                    "language": "Python"
                },
                "event": {
                    "type": event_type,
                    "data": data
                },
                "metadata": {
                    "hostname": "crewai-host"
                }
            }

            response = await self.client.post(
                f"{self.server_url}/api/events",
                json=payload
            )
            if response.status_code != 200:
                print(f"Failed to send event: {response.status_code}")
        except Exception as e:
            print(f"Error sending event: {e}")

# 创建监听器实例
monitor_listener = CrewAIMonitorListener()
```

### 5.2 后端事件处理器更新

```java
// EventService.java - 更新事件处理
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final AgentStateMapper agentStateMapper;

    /**
     * 处理 CrewAI 事件
     */
    @Transactional
    public void processCrewAIEvent(MonitorEventDTO event) {
        String eventType = event.getEvent().getType();

        // Crew 级别事件
        switch (eventType) {
            case "crew_started":
                handleCrewStarted(event);
                break;
            case "agent_execution_started":
                handleAgentExecutionStarted(event);
                break;
            case "agent_thinking":
                handleAgentThinking(event);
                break;
            case "tool_usage_started":
                handleToolUsageStarted(event);
                break;
            case "agent_execution_completed":
                handleAgentExecutionCompleted(event);
                break;
            case "agent_error":
                handleAgentError(event);
                break;
            default:
                log.debug("未处理的 CrewAI 事件类型: {}", eventType);
        }
    }

    /**
     * 处理 Agent 开始执行
     */
    private void handleAgentExecutionStarted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        String task = (String) event.getEvent().getData().get("task");

        AgentState state = getOrCreateAgentState(event);
        state.setStatus("busy");
        state.setCurrentActivity(task);
        state.setLastActivity(Instant.now());

        agentStateMapper.update(state);
        log.info("Agent 开始执行: {} - {}", agentId, task);
    }

    /**
     * 处理 Agent 思考状态
     */
    private void handleAgentThinking(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        String model = (String) event.getEvent().getData().get("model");

        AgentState state = getOrCreateAgentState(event);
        state.setStatus("thinking");  // 新增状态
        state.setCurrentActivity("思考中 (模型: " + model + ")");
        state.setLastActivity(Instant.now());

        agentStateMapper.update(state);
        log.debug("Agent 思考中: {} (模型: {})", agentId, model);
    }

    /**
     * 处理工具使用开始
     */
    private void handleToolUsageStarted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        String toolName = (String) event.getEvent().getData().get("tool_name");
        @SuppressWarnings("unchecked")
        Map<String, Object> toolInput = (Map<String, Object>) event.getEvent().getData().get("tool_input");

        AgentState state = getOrCreateAgentState(event);
        state.setStatus("busy");
        state.setCurrentActivity("使用工具: " + toolName);
        state.setLastActivity(Instant.now());

        agentStateMapper.update(state);
        log.info("Agent 使用工具: {} - {}", agentId, toolName);

        // TODO: 记录到 tool_usage_stats 表
    }

    /**
     * 处理 Agent 完成执行
     */
    private void handleAgentExecutionCompleted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        String output = (String) event.getEvent().getData().get("output");

        AgentState state = getOrCreateAgentState(event);
        state.setStatus("ready");  // 新增就绪状态
        state.setCurrentActivity("任务完成");
        state.setLastActivity(Instant.now());

        agentStateMapper.update(state);
        log.info("Agent 完成执行: {}", agentId);
    }

    private AgentState getOrCreateAgentState(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        AgentState state = agentStateMapper.findByAgentId(agentId);
        if (state == null) {
            state = new AgentState();
            state.setAgentId(agentId);
            state.setServerId(event.getSource().getServerId());
            state.setFramework(event.getSource().getFramework());
            state.setLanguage(event.getSource().getLanguage());
            state.setCreatedAt(Instant.now());
            agentStateMapper.insert(state);
        }
        return state;
    }
}
```

---

## 6. 数据库更新

### 6.1 Agent 状态扩展

```sql
-- 更新 agent_states 表，支持更多状态
ALTER TABLE agent_states
MODIFY COLUMN status ENUM(
    'online',
    'offline',
    'busy',
    'error',
    'thinking',     -- 新增：思考中
    'ready',        -- 新增：就绪
    'waiting',      -- 新增：等待中
    'paused'        -- 新增：已暂停
) DEFAULT 'offline';

-- 添加当前工具字段
ALTER TABLE agent_states
ADD COLUMN current_tool VARCHAR(100) COMMENT '当前使用的工具',
ADD COLUMN current_task_id VARCHAR(36) COMMENT '当前任务ID',
ADD COLUMN memory_id VARCHAR(36) COMMENT '关联的Memory ID';

-- 添加索引
CREATE INDEX idx_memory ON agent_states(memory_id);
CREATE INDEX idx_current_task ON agent_states(current_task_id);
```

---

## 7. 前端更新

### 7.1 类型定义扩展

```typescript
// shared/types.ts - 扩展 AgentStatus
export type AgentStatus =
  | 'online'
  | 'offline'
  | 'error'
  | 'busy'
  | 'thinking'      // 新增：思考中
  | 'ready'         // 新增：就绪
  | 'waiting'       // 新增：等待中
  | 'paused'        // 新增：已暂停

// 扩展 AgentState 接口
export interface AgentState {
  // ... 现有字段
  currentTool?: string        // 当前使用的工具
  currentTaskId?: string      // 当前任务 ID
  memoryId?: string           // 关联的 Memory ID
}
```

---

**文档状态**: 🟡 开发中
**相关文档**:
- [Agent 行为定义](./05-agent-behavior.md)
- [Memory 管理系统](./04-memory-management.md)
- [CrewAI 官方文档](https://docs.crewai.com/en/concepts/event-listener)

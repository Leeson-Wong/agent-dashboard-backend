# 主流 Agent 框架 Hook 系统统一清单

> 本文档整理了所有主流 Agent 框架的 Hook/事件系统，为开发**统一监控看板**提供参考。

---

## 一、框架 Hook 系统对比矩阵

| 框架 | Hook 机制 | 在线状态 | 当前活动 | Agent 关系 | 推荐集成方式 |
|------|-----------|----------|----------|------------|--------------|
| **CrewAI** | 事件总线 + 装饰器 | ✅ | ✅ | ✅ A2A 协议 | 事件监听器 |
| **LangGraph** | Callback Handlers | ✅ | ✅ | ⚠️ 节点依赖 | 回调处理器 |
| **AutoGen** | Event System | ⚠️ | ✅ | ✅ 对话流 | 事件订阅 |
| **OpenAI Agents** | Hooks API | ✅ | ✅ | ⚠️ Handoff | 生命周期钩子 |
| **Phidata** | Git-style Hooks | ✅ | ✅ | ❌ | Runner Hooks |
| **Semantic Kernel** | Pre/Post Hooks | ✅ | ✅ | ✅ 多 Agent | 函数拦截 |
| **Vercel AI SDK** | Tool Lifecycle Hooks | ✅ | ✅ | ❌ | onBefore/After |
| **Dust** | Webhook Endpoints | ✅ | ✅ | ⚠️ MCP | Webhook 接收 |
| **Rivet** | Visual Debugging | ✅ | ✅ | ✅ 节点连接 | 可视化 API |
| **AgentOps** | 遥测追踪 | ✅ | ✅ | ⚠️ 会话关联 | TraceListener |

---

## 二、详细 Hook 清单

### 1. CrewAI - 事件总线模式

#### 核心文件
```
lib/crewai/src/crewai/events/
├── event_bus.py              # 事件总线（单例）
├── base_events.py            # 基础事件类
├── event_listener.py         # 监听器基类
└── types/
    ├── agent_events.py       # Agent 生命周期
    ├── task_events.py        # 任务事件
    ├── crew_events.py        # Crew 执行
    ├── llm_events.py         # LLM 调用
    ├── tool_usage_events.py  # 工具使用
    ├── a2a_events.py         # Agent-to-Agent 协作
    ├── flow_events.py        # Flow 工作流
    └── memory_events.py      # 内存操作
```

#### 监控 "哪些 Agent 在线"

```python
# Agent 创建事件
@crewai_event_bus.on(AgentExecutionStartedEvent)
def on_agent_online(source, event):
    """Agent 上线"""
    return {
        'agent_id': event.agent.id,
        'agent_role': event.agent.role,
        'agent_goal': event.agent.goal,
        'timestamp': event.timestamp,
        'status': 'online'
    }

@crewai_event_bus.on(AgentExecutionCompletedEvent)
def on_agent_offline(source, event):
    """Agent 离线"""
    return {
        'agent_id': event.agent.id,
        'status': 'offline',
        'result': event.output.raw
    }
```

#### 监控 "Agent 在做什么"

```python
@crewai_event_bus.on(TaskStartedEvent)
def on_task_start(source, event):
    """任务开始"""
    return {
        'agent_id': event.task.agent.id,
        'task_id': event.task.id,
        'task_description': event.task.description,
        'expected_output': event.task.expected_output,
        'status': 'working'
    }

@crewai_event_bus.on(ToolUsageStartedEvent)
def on_tool_use(source, event):
    """工具使用"""
    return {
        'agent_id': event.agent_key,
        'tool_name': event.tool_name,
        'tool_args': event.tool_args,
        'action': 'using_tool'
    }
```

#### 监控 "Agent 关系"

```python
# A2A 协作事件
@crewai_event_bus.on(A2ADelegationStartedEvent)
def on_delegation(source, event):
    """Agent 委派任务给另一个 Agent"""
    return {
        'from_agent': event.source_agent_id,
        'to_agent': event.a2a_agent_name,
        'delegation_type': 'task_handoff',
        'timestamp': event.timestamp
    }

@crewai_event_bus.on(A2AConversationStartedEvent)
def on_conversation_start(source, event):
    """多轮对话开始"""
    return {
        'participants': event.participating_agents,
        'conversation_id': event.conversation_id,
        'relationship': 'collaboration'
    }
```

#### 关键事件类型

**Agent 生命周期：**
- `AgentExecutionStartedEvent` - 上线
- `AgentExecutionCompletedEvent` - 离线
- `AgentExecutionErrorEvent` - 错误

**当前活动：**
- `TaskStartedEvent` / `TaskCompletedEvent` - 任务状态
- `ToolUsageStartedEvent` / `ToolUsageFinishedEvent` - 工具调用
- `LLMCallStartedEvent` / `LLMCallCompletedEvent` - LLM 调用

**Agent 关系：**
- `A2ADelegationStartedEvent` - 委派关系
- `A2AConversationStartedEvent` - 协作关系
- `A2AMessageSentEvent` / `A2AResponseReceivedEvent` - 消息流

---

### 2. LangGraph / LangChain - Callback Handlers

#### 核心接口
```python
from langchain_core.callbacks import BaseCallbackHandler

class AgentMonitorHandler(BaseCallbackHandler):
    def on_llm_start(self, serialized, prompts, **kwargs):
        """LLM 调用开始"""
        pass

    def on_llm_end(self, response, **kwargs):
        """LLM 调用结束"""
        pass

    def on_chain_start(self, serialized, inputs, **kwargs):
        """链（Agent）开始"""
        pass

    def on_chain_end(self, outputs, **kwargs):
        """链（Agent）结束"""
        pass

    def on_tool_start(self, serialized, input_str, **kwargs):
        """工具开始"""
        pass

    def on_tool_end(self, output, **kwargs):
        """工具结束"""
        pass
```

#### 监控 "在线状态"

```python
class AgentStatusHandler(BaseCallbackHandler):
    def on_chain_start(self, serialized, inputs, **kwargs):
        agent_name = serialized.get('name', 'unknown')
        # Agent 上线
        self.monitor.agent_online(agent_name, inputs)

    def on_chain_end(self, outputs, **kwargs):
        agent_name = serialized.get('name', 'unknown')
        # Agent 离线
        self.monitor.agent_offline(agent_name, outputs)
```

#### 监控 "当前活动"

```python
def on_tool_start(self, serialized, input_str, **kwargs):
    tool_name = serialized.get('name', 'unknown')
    # Agent 正在使用工具
    self.monitor.agent_activity(tool_name, 'using_tool')

def on_llm_start(self, serialized, prompts, **kwargs):
    # Agent 正在思考
    self.monitor.agent_activity('thinking', prompts[0])
```

#### 监控 "Agent 关系"

```python
# LangGraph 节点依赖
def on_graph_node_start(self, node_id, **kwargs):
    # 节点（Agent）之间的依赖关系
    self.monitor.agent_dependency(node_id, kwargs.get('source_node'))
```

---

### 3. AutoGen - Event System

#### 事件类型
```python
from autogen_agentchat import Event

class AutoGenMonitor:
    def on_agent_message(self, sender, recipient, message):
        """消息事件"""
        return {
            'from_agent': sender.name,
            'to_agent': recipient.name,
            'message_type': type(message).__name__,
            'timestamp': datetime.now()
        }

    def on_agent_response(self, agent, response):
        """响应事件"""
        return {
            'agent_id': agent.name,
            'response': response,
            'status': 'responding'
        }
```

---

### 4. OpenAI Agents SDK - Hooks API

#### 生命周期钩子
```typescript
interface AgentHooks {
  // Agent 生命周期
  onAgentStart?: (agent: Agent) => Promise<void>
  onAgentEnd?: (agent: Agent, result: RunResult) => Promise<void>
  onAgentError?: (agent: Agent, error: Error) => Promise<void>

  // 工具执行
  onToolStart?: (tool: Tool, input: any) => Promise<void>
  onToolEnd?: (tool: Tool, input: any, output: any) => Promise<void>

  // Handoff（Agent 交接）
  onHandoff?: (from: Agent, to: Agent) => Promise<void>
}
```

#### 监控示例
```typescript
const hooks: AgentHooks = {
  onAgentStart: async (agent) => {
    monitor.agentOnline(agent.id, agent.name)
  },
  onAgentEnd: async (agent, result) => {
    monitor.agentOffline(agent.id, result.finalOutput)
  },
  onHandoff: async (from, to) => {
    monitor.agentHandoff(from.id, to.id)
  }
}
```

---

### 5. Semantic Kernel - Pre/Post Hooks

#### 函数拦截器
```python
from semantic_kernel import Kernel

class KernelHook:
    async def on_before_function_execution(self, context):
        """函数执行前"""
        return {
            'agent_id': context.agent_id,
            'function': context.function_name,
            'status': 'executing'
        }

    async def on_after_function_execution(self, context, result):
        """函数执行后"""
        return {
            'agent_id': context.agent_id,
            'function': context.function_name,
            'result': result,
            'status': 'completed'
        }
```

---

### 6. Phidata - Git-style Hooks

#### Runner Hooks
```python
from phi.agent import Agent

class PhidataMonitor:
    def on_agent_run_start(self, agent: Agent, input_data: dict):
        """Agent 开始运行"""
        monitor.record_event({
            'agent_id': agent.id,
            'agent_name': agent.name,
            'status': 'online',
            'input': input_data
        })

    def on_agent_run_end(self, agent: Agent, output: dict):
        """Agent 运行结束"""
        monitor.record_event({
            'agent_id': agent.id,
            'status': 'offline',
            'output': output
        })

    def on_message(self, agent: Agent, message: str):
        """消息事件"""
        monitor.record_event({
            'agent_id': agent.id,
            'activity': 'messaging',
            'message': message
        })
```

---

### 7. Vercel AI SDK - Tool Lifecycle Hooks

#### 工具生命周期
```typescript
import { useChat } from 'ai/react'

const { hooks } = useChat({
  onToolStart: ({ toolName }) => {
    monitor.agentActivity('tool_start', toolName)
  },
  onToolEnd: ({ toolName, result }) => {
    monitor.agentActivity('tool_end', { toolName, result })
  }
})
```

---

### 8. Dust - Webhook Endpoints

#### Webhook 事件接收
```typescript
// Dust Agent 暴露 Webhook 端点
app.post('/webhook/agent/:agentId', (req, res) => {
  const { agentId } = req.params
  const event = req.body

  switch (event.type) {
    case 'agent.activated':
      monitor.agentOnline(agentId)
      break
    case 'agent.task.started':
      monitor.agentActivity(agentId, event.task)
      break
    case 'agent.task.completed':
      monitor.agentActivityComplete(agentId, event.result)
      break
  }
})
```

---

### 9. Rivet - Visual Debugging

#### 可视化 API
```typescript
// Rivet 提供实时状态查询
const rivetMonitor = await rivet.getMonitor()

// 获取所有活跃 Agent
const activeAgents = await rivetMonitor.getActiveAgents()

// 监听节点执行
rivet.on('nodeExecute', (data) => {
  monitor.agentActivity(data.nodeId, data.execution)
})

// 监听节点连接
rivet.on('nodeConnection', (from, to) => {
  monitor.agentRelationship(from, to)
})
```

---

### 10. AgentOps - 遥测追踪

#### TraceListener 集成
```python
from agent_ops import TraceListener

class AgentOpsMonitor(TraceListener):
    def on_trace(self, trace_event):
        """接收所有追踪事件"""
        return {
            'event_type': trace_event.type,
            'agent_id': trace_event.agent_id,
            'timestamp': trace_event.timestamp,
            'data': trace_event.event_data
        }

# 通用事件映射
EVENT_MAPPING = {
    'agent_execution_started': 'agent_online',
    'agent_execution_completed': 'agent_offline',
    'task_started': 'agent_working',
    'tool_usage_started': 'agent_using_tool',
    'llm_call_started': 'agent_thinking'
}
```

---

## 三、统一监控看板 - 数据模型设计

### 1. Agent 状态模型

```typescript
interface AgentState {
  id: string                      // Agent 唯一标识
  name: string                    // Agent 名称/角色
  framework: string               // 所属框架
  status: 'online' | 'offline' | 'error'  // 在线状态
  currentActivity: {               // 当前活动
    type: 'idle' | 'thinking' | 'using_tool' | 'task_executing'
    description: string
    startTime: Date
    metadata?: any
  }
  capabilities: string[]          // 能力列表（可用工具）
  relationships: AgentRelationship[]  // Agent 关系
  metrics: AgentMetrics           // 性能指标
  lastSeen: Date                  // 最后活跃时间
}

interface AgentRelationship {
  type: 'delegate' | 'collaborate' | 'report' | 'depend'
  targetAgentId: string
  strength: number                // 交互频率权重
  lastInteraction: Date
}
```

### 2. 监控事件模型

```typescript
interface MonitorEvent {
  eventId: string
  timestamp: Date
  framework: string
  agentId: string
  eventType: string               // 事件类型
  eventData: any                  // 事件数据
  parentEventId?: string          // 父事件（构建事件树）
  source: 'crewai' | 'langgraph' | 'autogen' | ...
}
```

### 3. 统一事件适配器

```python
class UnifiedEventAdapter:
    """将各框架事件转换为统一格式"""

    def __init__(self):
        self.framework_adapters = {
            'crewai': self._adapt_crewai_event,
            'langgraph': self._adapt_langgraph_event,
            'autogen': self._adapt_autogen_event,
            # ...
        }

    def adapt_event(self, framework: str, raw_event: Any) -> MonitorEvent:
        adapter = self.framework_adapters.get(framework)
        if adapter:
            return adapter(raw_event)
        raise ValueError(f"Unsupported framework: {framework}")

    def _adapt_crewai_event(self, event: BaseEvent) -> MonitorEvent:
        """适配 CrewAI 事件"""
        return MonitorEvent(
            event_id=event.event_id,
            timestamp=event.timestamp,
            framework='crewai',
            agent_id=getattr(event, 'agent_id', None),
            event_type=event.type,
            event_data=event.model_dump(),
            parent_event_id=event.parent_event_id
        )
```

---

## 四、集中看板架构建议

### 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    统一监控看板 UI                            │
│  (Agent 列表 | 关系图谱 | 实时事件流 | 性能仪表盘)            │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                  事件聚合服务                                 │
│  - 事件标准化                                               │
│  - 状态聚合计算                                             │
│  - 关系图谱构建                                             │
│  - 实时推送 (WebSocket)                                     │
└────────────────┬────────────────────────────────────────────┘
                 │
    ┌────────────┼────────────┬────────────┬────────────┐
    ▼            ▼            ▼            ▼            ▼
┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐
│ CrewAI │  │LangGph │  │AutoGen │  │ Phidata│  │ Other  │
│Listener│  │Handler │  │ Monitor│  │  Hook  │  │ Monitor│
└────────┘  └────────┘  └────────┘  └────────┘  └────────┘
    │            │            │            │            │
    ▼            ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────┐
│              Agent 框架实例（运行时）                         │
└─────────────────────────────────────────────────────────────┘
```

### 核心组件

#### 1. 事件收集器
```python
class EventCollector:
    """收集所有框架的事件"""

    def __init__(self):
        self.listeners = []
        self.event_queue = asyncio.Queue()

    async def start(self):
        """启动所有监听器"""
        for listener in self.listeners:
            await listener.start(self.event_queue)

    async def process_events(self):
        """处理事件流"""
        while True:
            event = await self.event_queue.get()
            normalized = self.adapter.adapt_event(event.framework, event)
            await self.storage.save(normalized)
            await self.websocket.broadcast(normalized)
```

#### 2. 状态聚合器
```python
class StateAggregator:
    """聚合 Agent 状态"""

    def __init__(self):
        self.agent_states: Dict[str, AgentState] = {}

    async def update_agent_state(self, event: MonitorEvent):
        """根据事件更新 Agent 状态"""
        agent_id = event.agent_id

        if event.event_type == 'agent_online':
            self.agent_states[agent_id] = AgentState(
                id=agent_id,
                status='online',
                lastSeen=event.timestamp
            )

        elif event.event_type == 'agent_working':
            self.agent_states[agent_id].currentActivity = {
                type: 'task_executing',
                description: event.event_data.task_description,
                startTime: event.timestamp
            }

        elif event.event_type == 'agent_offline':
            self.agent_states[agent_id].status = 'offline'
```

#### 3. 关系图谱构建器
```python
class RelationshipBuilder:
    """构建 Agent 关系图谱"""

    def __init__(self):
        self.graph = networkx.DiGraph()

    async def update_relationship(self, event: MonitorEvent):
        """更新 Agent 关系"""
        if event.event_type == 'agent_delegation':
            from_agent = event.event_data.from_agent
            to_agent = event.event_data.to_agent

            self.graph.add_edge(from_agent, to_agent,
                type='delegate',
                timestamp=event.timestamp
            )

    def get_agent_neighbors(self, agent_id: str, depth: int = 2):
        """获取 Agent 的邻居"""
        return nx.single_source_shortest_path_length(
            self.graph, agent_id, cutoff=depth
        )
```

---

## 五、实现优先级建议

### Phase 1: 基础监控（MVP）
1. ✅ CrewAI 集成（最完善的事件系统）
2. ✅ LangGraph 集成（使用最广泛）
3. ✅ 基础状态展示（在线/离线/工作）
4. ✅ 实时事件流

### Phase 2: 关系图谱
5. ✅ Agent 依赖关系可视化
6. ✅ 交互热度分析
7. ✅ 协作路径追踪

### Phase 3: 高级功能
8. ⚠️ 性能指标聚合
9. ⚠️ 异常检测和告警
10. ⚠️ 多框架统一管理

---

## 六、关键代码示例

### CrewAI 集成示例

```python
# crewai_monitor.py
from crewai.events import crewai_event_bus, BaseEventListener
from crewai.events.types import (
    AgentExecutionStartedEvent,
    AgentExecutionCompletedEvent,
    TaskStartedEvent,
    A2ADelegationStartedEvent
)

class CrewAIMonitor(BaseEventListener):
    def __init__(self, event_collector):
        self.collector = event_collector

    def setup_listeners(self, crewai_event_bus):
        @crewai_event_bus.on(AgentExecutionStartedEvent)
        def on_agent_online(source, event):
            self.collector.push(MonitorEvent(
                framework='crewai',
                agent_id=event.agent.id,
                event_type='agent_online',
                event_data={
                    'role': event.agent.role,
                    'goal': event.agent.goal
                },
                timestamp=event.timestamp
            ))

        @crewai_event_bus.on(TaskStartedEvent)
        def on_task_start(source, event):
            self.collector.push(MonitorEvent(
                framework='crewai',
                agent_id=event.task.agent.id,
                event_type='agent_working',
                event_data={
                    'task': event.task.description,
                    'expected_output': event.task.expected_output
                },
                timestamp=event.timestamp
            ))

        @crewai_event_bus.on(A2ADelegationStartedEvent)
        def on_delegation(source, event):
            self.collector.push(MonitorEvent(
                framework='crewai',
                agent_id=event.source_agent_id,
                event_type='agent_relationship',
                event_data={
                    'relationship_type': 'delegate',
                    'target_agent': event.a2a_agent_name
                },
                timestamp=event.timestamp
            ))
```

---

## 七、参考资料

### CrewAI
- 事件系统文档：`lib/crewai/src/crewai/events/`
- A2A 协议：`lib/crewai/src/crewai/events/types/a2a_events.py`

### LangGraph
- 回调处理器：`langchain_core.callbacks.BaseCallbackHandler`
- 流式事件：`astream_events()` API

### AgentOps
- 遥测标准：OpenTelemetry 兼容
- 批量追踪：`TraceBatch` 数据结构

---

## 八、下一步行动

1. **创建统一事件适配器** - 定义标准化事件格式
2. **实现 CrewAI 监听器** - 作为参考实现
3. **开发 WebSocket 推送服务** - 实时更新看板
4. **构建关系图谱可视化** - 使用 D3.js 或 Cytoscape.js
5. **添加性能指标** - 执行时间、成功率等

---

**生成时间：** 2025-02-06
**版本：** v1.0

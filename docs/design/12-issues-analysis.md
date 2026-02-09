# Agent Monitor 项目问题分析记录

生成时间: 2025-02-09

---

## 项目概览

该项目由三个子项目组成：

1. **agent-monitor-plugin** - Python 监控插件，拦截 CrewAI 事件并发送到后端
2. **agent-dashboard-backend** - Java Spring Boot 后端，接收事件并提供 API
3. **agent-dashboard-frontend** - TypeScript 前端，3D 可视化 Agent 状态

---

## 🔴 核心问题 1：WebSocket 协议不兼容

### 问题描述

前端使用原生 WebSocket 协议，后端使用 STOMP 协议 + SockJS，导致无法建立连接。

### 详细信息

| 组件 | 当前实现 | 代码位置 |
|------|----------|----------|
| **前端** | 原生 WebSocket (`new WebSocket(url)`) | `agent-dashboard-frontend/src/api/EventStream.ts:66` |
| **后端** | STOMP 协议 + SockJS 备用 | `agent-dashboard-backend/src/main/java/com/agent/monitor/websocket/WebSocketConfig.java:14-31` |

### 代码证据

**前端** (`main.ts:22`):
```typescript
const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'
```

**后端** (`WebSocketConfig.java`):
```java
@Configuration
@EnableWebSocketMessageBroker  // 启用 STOMP 消息代理
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // 需要 SockJS 客户端
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

### 影响范围

- 前端无法与后端建立 WebSocket 连接
- 实时更新功能不可用
- 前端会降级到轮询或模拟数据模式

---

## 🔴 核心问题 2：消息格式不匹配

### 问题描述

后端发送的 STOMP 消息格式与前端期望的 WebSocket 消息格式不一致。

### 代码证据

**后端发送** (`WebSocketMessageSender.java:28-38`):
```java
public void broadcastAgentUpdate(AgentState agentState, Long seq) {
    Map<String, Object> message = new HashMap<>();
    message.put("type", "agent_update");        // ← 后端类型
    message.put("data", agentState);
    message.put("timestamp", Instant.now().toString());
    if (seq != null) {
        message.put("seq", seq);
    }
    messagingTemplate.convertAndSend("/topic/agents", message);  // STOMP 主题
}
```

**前端期望** (`types.ts:134-139`):
```typescript
export type ServerMessage =
  | { type: 'event'; payload: AgentEvent }        // ← 前端类型
  | { type: 'agents'; payload: AgentState[] }
  | { type: 'stats'; payload: AgentStatsResponse }
  | { type: 'connected' }
  | { type: 'error'; payload: { message: string } }
```

### 类型差异

| 后端类型 | 前端类型 | 说明 |
|---------|---------|------|
| `agent_update` | `agents` / `event` | 类型名称不同 |
| `data` | `payload` | 字段名不同 |
| 包含 `seq` | 无 `seq` 字段 | 后端有序列号，前端未处理 |

---

## 🔴 核心问题 3：事件类型映射不一致

### 问题描述

Python 插件、后端处理、前端类型定义之间存在事件类型映射断层。

### 代码证据

**Python 插件发送** (`crewai_plugin.py`):
```python
event={
    "type": "agent_online",           # CrewAI 事件
    "data": {"role": event.agent.role, ...}
}
event={
    "type": "crew_started",           # Crew 级别事件
    "data": {"crew_name": event.crew_name, ...}
}
event={
    "type": "agent_thinking",         # 思考状态
    "data": {"action": "thinking", ...}
}
event={
    "type": "tool_usage_started",     # 工具使用
    "data": {"tool_name": event.tool_name, ...}
}
```

**后端处理** (`EventService.java:68-114`):
```java
// 后端将 CrewAI 事件映射到 AgentState 状态更新
private void processCrewAIEvent(MonitorEventDTO event, Long seq) {
    switch (eventType) {
        case "crew_started":
            state.setStatus("initializing");
            break;
        case "agent_thinking":
            state.setStatus("thinking");
            break;
        case "tool_usage_started":
            state.setStatus("busy");
            break;
    }
    webSocketMessageSender.broadcastAgentUpdate(state, seq);  // 发送统一格式
}
```

**前端期望** (`types.ts:67-95`):
```typescript
export type EventType = 'agent_status' | 'agent_activity' | 'agent_error'

export interface AgentStatusEvent extends BaseEvent {
    type: 'agent_status'      // ← 前端不识别 CrewAI 事件
    status: AgentStatus
}

export interface AgentActivityEvent extends BaseEvent {
    type: 'agent_activity'
    activity: string
}
```

### 映射断层

```
Python 插件          后端处理               前端类型
─────────────────    ──────────────────    ──────────────
agent_online    →   AgentState 状态更新  →   ??? (无直接映射)
crew_started    →   status="initializing"→   ???
agent_thinking  →   status="thinking"    →   ???
tool_usage_*    →   status="busy"        →   ???
```

---

## 📊 数据流分析

### 正常流程（预期）

```
┌──────────────────┐   HTTP POST      ┌──────────────────┐
│ Python Plugin    │  /events/batch   │  Java Backend    │
│ (CrewAI events)  │ ───────────────▶ │ (EventService)   │
└──────────────────┘                  └────────┬─────────┘
                                              │
                                              │ STOMP
                                              │ /topic/agents
                                              ▼
                                     ┌──────────────────┐
                                     │ TypeScript Front │
                                     │ (EventStream)    │
                                     └──────────────────┘
```

### 当前流程（实际）

```
┌──────────────────┐   HTTP POST      ┌──────────────────┐
│ Python Plugin    │  /events/batch   │  Java Backend    │
│ (CrewAI events)  │ ───────────────▶ │ (EventService)   │
│                  │                  │                  │
│ ✅ 工作正常       │                  │ ✅ 工作正常       │
└──────────────────┘                  └────────┬─────────┘
                                              │
                                              │ STOMP 消息
                                              │ /topic/agents
                                              │ ❌ 前端无法接收
                                              │    (协议不兼容)
                                              ▼
                                     ┌──────────────────┐
                                     │ TypeScript Front │
                                     │ (原生 WebSocket) │
                                     │ ❌ 连接失败      │
                                     └──────────────────┘
```

---

## 🔍 其他发现

### 1. 前端降级机制

前端有模拟数据降级逻辑 (`main.ts:336-382`):

```typescript
if (USE_MOCK_DATA) {
    console.log('⚠️  Using mock data (VITE_USE_MOCK_DATA=true)')
    initMockData()
}
// ...
} catch (error) {
    console.error('❌ Failed to initialize:', error)
    // 降级到模拟数据
    initMockData()
}
```

### 2. 前端期望的 WebSocket 消息流程

`EventStream.ts:112-124`:
```typescript
this.ws.onopen = () => {
    // 订阅事件
    this.send({ type: 'subscribe' })

    // 请求初始数据
    this.send({ type: 'get_agents' })

    this.onConnect?.()
}
```

但后端 STOMP 不处理这些消息类型。

### 3. 状态管理存在但无法使用

`AgentStore.ts` 有完整的状态管理逻辑：
- 全量同步 (`syncAll`)
- 增量更新 (`update`)
- 事件去重
- 时间戳比较

但无法接收 WebSocket 更新导致这些逻辑无法发挥作用。

---

## 🛠️ 修复方案建议

### 方案 A：修改前端使用 STOMP 协议（推荐）

**优点：**
- 后端已配置完整的 STOMP 支持
- Spring WebSocket STOMP 是成熟方案
- 改动相对较小

**需要修改：**
1. 安装 `@stomp/stompjs` 和 `sockjs-client`
2. 重写 `EventStream.ts` 使用 Stomp.js
3. 调整消息处理逻辑

### 方案 B：修改后端使用原生 WebSocket

**优点：**
- 前端代码不需要改动

**缺点：**
- 需要重写 WebSocket 配置
- 失去 STOMP 的消息代理功能
- 改动较大

### 方案 C：创建 WebSocket 兼容层

**优点：**
- 可以同时支持两种协议

**缺点：**
- 增加系统复杂度
- 维护成本高

---

## 📝 待确认信息

- [ ] 后端是否必须使用 STOMP 协议？
- [ ] 前端是否可以引入新依赖？
- [ ] 是否需要保持向后兼容？
- [ ] 其他框架（LangChain, LangGraph）的支持情况

---

## 🔗 相关文件清单

### 前端
- `agent-dashboard-frontend/src/api/EventStream.ts` - WebSocket 客户端
- `agent-dashboard-frontend/src/api/ApiClient.ts` - REST API 客户端
- `agent-dashboard-frontend/src/store/AgentStore.ts` - 状态管理
- `agent-dashboard-frontend/shared/types.ts` - 类型定义
- `agent-dashboard-frontend/src/main.ts` - 主入口

### 后端
- `agent-dashboard-backend/src/main/java/com/agent/monitor/websocket/WebSocketConfig.java` - WebSocket 配置
- `agent-dashboard-backend/src/main/java/com/agent/monitor/websocket/WebSocketMessageSender.java` - 消息发送
- `agent-dashboard-backend/src/main/java/com/agent/monitor/service/EventService.java` - 事件处理
- `agent-dashboard-backend/src/main/java/com/agent/monitor/controller/EventController.java` - HTTP 接口

### 插件
- `agent-monitor-plugin/agent_monitor/protocol/unified_event.py` - 统一事件协议
- `agent-monitor-plugin/agent_monitor/plugins/crewai_plugin.py` - CrewAI 插件
- `agent-monitor-plugin/agent_monitor/transports/direct.py` - HTTP 传输

---

## 🏗️ 架构层面的问题（新发现）

### 问题 4：当前 Agent 是一次性执行模式，无法常驻

**代码位置**: `agent-crewAI/prac/story_writer_crew/src/story_writer_crew/main.py`

**当前模式**:
```python
# main.py:12-38
def run():
    """运行故事创作 Crew"""
    inputs = {'theme': '时间旅行与遗憾'}

    story_crew = StoryWriterCrew()
    result = story_crew.crew().kickoff(inputs=inputs)  # ← 执行一次

    # 输出结果后进程退出
    print(result.raw)
```

**问题**:
- ❌ Agent 执行一次任务后退出
- ❌ 无法持续接收新任务
- ❌ 无法作为服务运行
- ❌ 无法与其他 Agent 协作

### 问题 5：缺少 Agent 间通信机制

**现状**:
- 当前每个 Crew 是独立运行的
- 没有 Agent 之间的消息传递机制
- 没有服务发现和注册机制
- 无法实现跨 Agent 的任务协作

**需要补充**:
```
┌─────────────┐         ┌─────────────┐
│ Agent A     │         │ Agent B     │
│ (CrewAI)    │         │ (LangChain) │
└──────┬──────┘         └──────┬──────┘
       │                       │
       │    ❌ 无法直接通信     │
       │                       │
       ▼                       ▼
   (独立进程)              (独立进程)
```

### 问题 6：缺少容器化部署方案

**当前部署方式**:
- 直接在本地运行 Python 脚本
- 使用虚拟环境隔离依赖
- 没有统一的运行时环境

**问题**:
- 不同语言框架的 Agent 难以统一管理
- 无法实现资源隔离和限制
- 扩展和部署困难

---

## 🎯 架构演进建议

### 推荐方案：Docker + 消息队列 + Agent Runtime

详见 `AGENT_ARCHITECTURE_DESIGN.md` 完整设计文档。

**核心组件**:

1. **Docker 容器化** - 统一运行环境
2. **Agent Runtime** - 将一次性 Agent 包装为常驻服务
3. **消息队列** - 实现 Agent 间通信
4. **统一消息协议** - 跨框架通信标准

**架构图**:
```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Docker     │    │   Docker     │    │   Docker     │
│   Python     │    │   Node.js    │    │   Java       │
│   Agent      │    │   Agent      │    │   Agent      │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           ▼
                  ┌─────────────────┐
                  │  Message Broker │
                  │   (Redis)       │
                  └─────────────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │  Monitor        │
                  │  Backend        │
                  └─────────────────┘
```

---

## 📚 相关文档

- `AGENT_CREATION_FLOW.md` - Agent 创建流程
- `AGENT_ARCHITECTURE_DESIGN.md` - 完整架构设计方案

---

*此文档由代码分析自动生成，待人工验证确认*

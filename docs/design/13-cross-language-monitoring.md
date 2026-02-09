# 跨语言 Agent 监控方案

> **版本**: v1.0
> **日期**: 2025-02-06
> **核心问题**: 不同语言实现的 Agent（Python CrewAI vs TypeScript OpenClaw）需要不同的监控方式

---

## 一、问题背景

### 1.1 现实情况

| Agent 框架 | 编程语言 | 监控方式 |
|-----------|---------|---------|
| **CrewAI** | Python | 事件总线、sys.setprofile |
| **LangGraph** | Python | 回调处理器 |
| **AutoGen** | Python | 事件系统 |
| **OpenClaw** | **TypeScript** | **需要完全不同的方案** ⚠️ |
| **自建 Agent** | 可能是任何语言 | **需要通用方案** ⚠️ |

### 1.2 核心挑战

1. **监控机制不同**
   - Python: `sys.setprofile`、AST 转换、猴子补丁
   - TypeScript: Proxy、Async Hooks、Babel 转换
   - Rust: 宏系统、Trait
   - Go: 接口包装

2. **运行时差异**
   - Python: 解释执行，动态性强
   - TypeScript: 编译到 JavaScript，V8 引擎
   - Rust: 编译型，性能优先

3. **异步模型不同**
   - Python: asyncio、事件循环
   - TypeScript: Promise、async/await
   - Rust: async/await、Future

---

## 二、跨语言监控架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│              多语言 Agent 环境                                │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Python      │  │  TypeScript  │  │   Rust       │      │
│  │  Agent       │  │  Agent       │  │   Agent      │      │
│  │              │  │              │  │              │      │
│  │ CrewAI       │  │ OpenClaw     │  │ Custom Agent │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │              │
└─────────┼──────────────────┼──────────────────┼──────────────┘
          │                  │                  │
          │         ┌────────▼──────────────────▼───────┐      │
          │         │      语言无关的通信协议            │      │
          │         │    (HTTP/WebSocket/gRPC)          │      │
          │         └──────────────────────────────────┘      │
          │                  │                              │
          ▼                  ▼                              │
┌─────────────────────────────────────────────────────────┐   │
│              统一监控服务                                  │   │
│          (无论 Agent 用什么语言，都用相同 API)             │   │
└─────────────────────────────────────────────────────────┘   │
```

### 2.2 核心设计原则

**关键思想：统一协议 + 语言特定实现**

1. **统一协议** - 所有语言发送相同格式的事件
2. **语言特定插件** - 每种语言有专门的监控实现
3. **统一 API** - 用户使用体验一致
4. **自动检测** - 自动选择最佳监控方案

---

## 三、统一监控协议

### 3.1 事件格式定义

```typescript
// protocol/unified-event.ts
/**
 * 统一监控事件协议
 * 所有语言的 Agent 都使用这个格式
 */

export interface UnifiedMonitorEvent {
  // 协议标识
  protocol: "agent-monitor";
  version: "1.0";

  // 时间戳 (ISO 8601)
  timestamp: string;

  // 事件源信息
  source: {
    server_id: string;        // 服务器唯一标识
    agent_id: string;         // Agent ID
    framework: string;        // 框架名称 (crewai, openclaw, etc)
    language: Language;       // 编程语言
    process_id?: number;      // 进程 ID (可选)
  };

  // 事件内容
  event: {
    type: EventType;
    data: Record<string, any>;
  };

  // 元数据
  metadata: {
    hostname: string;
    ip_address?: string;
    tags?: Record<string, string>;
  };
}

/**
 * 支持的编程语言
 */
export type Language =
  | "python"
  | "typescript"
  | "javascript"
  | "rust"
  | "go"
  | "java"
  | "cpp"
  | "csharp";

/**
 * 事件类型（跨语言统一）
 */
export type EventType =
  // Agent 生命周期
  | "agent_online"        // Agent 启动
  | "agent_offline"       // Agent 关闭
  | "agent_error"         // 发生错误

  // Agent 活动
  | "agent_working"       // Agent 工作中
  | "agent_thinking"      // LLM 思考中
  | "agent_using_tool"    // 使用工具

  // Agent 关系
  | "agent_relationship"  // Agent 关系变化

  // 通用方法调用（语言无关）
  | "method_call"         // 方法调用
  | "method_return"       // 方法返回
  | "method_error"        // 方法错误

  // LLM 调用
  | "llm_call_start"      // LLM 调用开始
  | "llm_call_end"        // LLM 调用结束
  | "llm_stream_chunk"    // 流式输出块

  // 异步操作
  | "async_operation_start"  // 异步操作开始
  | "async_operation_end";   // 异步操作结束
```

### 3.2 Python 实现

```python
# protocol/unified_event.py
from typing import Any
from pydantic import BaseModel
from datetime import datetime
from enum import Enum

class Language(str, Enum):
    python = "python"
    typescript = "typescript"
    javascript = "javascript"
    rust = "rust"
    go = "go"
    java = "java"

class EventType(str, Enum):
    agent_online = "agent_online"
    agent_offline = "agent_offline"
    agent_working = "agent_working"
    agent_thinking = "agent_thinking"
    agent_using_tool = "agent_using_tool"
    agent_relationship = "agent_relationship"
    method_call = "method_call"
    method_return = "method_return"
    llm_call_start = "llm_call_start"
    llm_call_end = "llm_call_end"

class EventSource(BaseModel):
    server_id: str
    agent_id: str
    framework: str
    language: Language
    process_id: int = None

class EventMetadata(BaseModel):
    hostname: str
    ip_address: str = None
    tags: dict[str, str] = None

class MonitorEvent(BaseModel):
    protocol: str = "agent-monitor"
    version: str = "1.0"
    timestamp: datetime
    source: EventSource
    event: dict
    metadata: EventMetadata

    def to_dict(self) -> dict:
        """转换为字典（用于 JSON 序列化）"""
        return {
            "protocol": self.protocol,
            "version": self.version,
            "timestamp": self.timestamp.isoformat(),
            "source": self.source.dict(),
            "event": self.event,
            "metadata": self.metadata.dict()
        }
```

### 3.3 Rust 实现

```rust
// protocol/src/unified_event.rs
use serde::{Deserialize, Serialize};
use chrono::{DateTime, Utc};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum Language {
    Python,
    TypeScript,
    JavaScript,
    Rust,
    Go,
    Java,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum EventType {
    AgentOnline,
    AgentOffline,
    AgentWorking,
    AgentThinking,
    AgentUsingTool,
    AgentRelationship,
    MethodCall,
    MethodReturn,
    LlmCallStart,
    LlmCallEnd,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct EventSource {
    pub server_id: String,
    pub agent_id: String,
    pub framework: String,
    pub language: Language,
    pub process_id: Option<u32>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct EventMetadata {
    pub hostname: String,
    pub ip_address: Option<String>,
    pub tags: Option<std::collections::HashMap<String, String>>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UnifiedMonitorEvent {
    pub protocol: String,
    pub version: String,
    pub timestamp: DateTime<Utc>,
    pub source: EventSource,
    pub event: serde_json::Value,
    pub metadata: EventMetadata,
}

impl UnifiedMonitorEvent {
    pub fn new(
        agent_id: String,
        framework: String,
        language: Language,
        event_type: &str,
        data: serde_json::Value,
    ) -> Self {
        Self {
            protocol: "agent-monitor".to_string(),
            version: "1.0".to_string(),
            timestamp: Utc::now(),
            source: EventSource {
                server_id: get_server_id(),
                agent_id,
                framework,
                language,
                process_id: std::process::id().into(),
            },
            event: serde_json::json!({
                "type": event_type,
                "data": data
            }),
            metadata: EventMetadata {
                hostname: gethostname::gethostname().to_string_lossy().to_string(),
                ip_address: None,
                tags: None,
            },
        }
    }
}
```

---

## 四、TypeScript/JavaScript 监控方案

### 4.1 方案 1：Proxy 拦截（推荐）

**原理：** 使用 ES6 Proxy 拦截对象操作

```typescript
// monitor/plugins/typescript/proxy-interceptor.ts
/**
 * TypeScript Agent 监控插件 - 基于 Proxy
 */

interface AgentMonitorConfig {
  agentId: string;
  framework: string;
  monitorUrl: string;
}

class TSProxyMonitor {
  private monitorUrl: string;
  private serverId: string;

  constructor(monitorUrl: string) {
    this.monitorUrl = monitorUrl;
    this.serverId = this.getServerId();
  }

  /**
   * 包装 Agent 对象，拦截所有方法调用
   */
  wrapAgent<T extends Record<string, any>>(
    agent: T,
    config: AgentMonitorConfig
  ): T {
    const self = this;

    // 创建 Proxy，拦截所有操作
    return new Proxy(agent, {
      get(target, prop, receiver) {
        const value = Reflect.get(target, prop, receiver);

        // 如果是方法，包装它
        if (typeof value === "function") {
          return function (...args: any[]) {
            // 🔍 方法调用前
            self.sendEvent({
              protocol: "agent-monitor",
              version: "1.0",
              timestamp: new Date().toISOString(),
              source: {
                server_id: self.serverId,
                agent_id: config.agentId,
                framework: config.framework,
                language: "typescript",
              },
              event: {
                type: "method_call",
                data: {
                  method: String(prop),
                  args: JSON.stringify(args).slice(0, 500),
                },
              },
              metadata: {
                hostname: require("os").hostname(),
              },
            });

            // 执行原始方法
            const result = value.apply(target, args);

            // 如果是 Promise，等待完成
            if (result instanceof Promise) {
              return result
                .then((res) => {
                  // 🔍 Promise 完成
                  self.sendEvent({
                    ...self.baseEvent(config),
                    event: {
                      type: "method_return",
                      data: {
                        method: String(prop),
                        result: JSON.stringify(res).slice(0, 500),
                      },
                    },
                    metadata: { hostname: require("os").hostname() },
                  });
                  return res;
                })
                .catch((err) => {
                  // 🔍 Promise 错误
                  self.sendEvent({
                    ...self.baseEvent(config),
                    event: {
                      type: "method_error",
                      data: {
                        method: String(prop),
                        error: err.message,
                      },
                    },
                    metadata: { hostname: require("os").hostname() },
                  });
                  throw err;
                });
            }

            // 🔍 方法返回（同步）
            self.sendEvent({
              ...self.baseEvent(config),
              event: {
                type: "method_return",
                data: {
                  method: String(prop),
                  result: JSON.stringify(result).slice(0, 500),
                },
              },
              metadata: { hostname: require("os").hostname() },
            });

            return result;
          };
        }

        return value;
      },
    });
  }

  private baseEvent(config: AgentMonitorConfig) {
    return {
      protocol: "agent-monitor" as const,
      version: "1.0",
      timestamp: new Date().toISOString(),
      source: {
        server_id: this.serverId,
        agent_id: config.agentId,
        framework: config.framework,
        language: "typescript" as const,
      },
    };
  }

  private sendEvent(event: UnifiedMonitorEvent) {
    // 异步发送，不阻塞主流程
    fetch(`${this.monitorUrl}/api/events`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(event),
    }).catch(() => {
      // 静默失败，不影响 Agent
    });
  }

  private getServerId(): string {
    return (
      process.env.AGENT_SERVER_ID ||
      require("os").hostname() ||
      "unknown"
    );
  }
}

// 导出便捷函数
export function instrumentAgent<T extends Record<string, any>>(
  agent: T,
  config: AgentMonitorConfig
): T {
  const monitor = new TSProxyMonitor(config.monitorUrl);
  return monitor.wrapAgent(agent, config);
}
```

### 4.2 OpenClaw 集成示例

```typescript
// examples/openclaw-monitor.ts
/**
 * OpenClaw Agent 监控集成示例
 */

import { Agent } from "@openclaw/core";
import { instrumentAgent } from "@agent-monitor/typescript";

// 创建 OpenClaw Agent
const agent = new Agent({
  name: "research-agent",
  instructions: "You are a research assistant",
});

// 🔧 一行代码启用监控
const monitoredAgent = instrumentAgent(agent, {
  agentId: "research-agent-1",
  framework: "openclaw",
  monitorUrl: process.env.AGENT_MONITOR_URL || "http://localhost:8000",
});

// 正常使用，所有行为自动监控
await monitoredAgent.chat("研究最新的 AI 框架");
// ↑ 这个调用会自动发送事件：
// 1. method_call (chat 开始)
// 2. method_return (chat 完成)
// 3. 内部的 LLM 调用也会被捕获

// 更多示例
await monitoredAgent.run({
  command: "search",
  args: ["Agent monitoring tools"],
});
```

### 4.3 方案 2：Async Hooks（Node.js 异步追踪）

```typescript
// monitor/plugins/typescript/async-hooks.ts
/**
 * 基于 Node.js Async Hooks 的监控
 * 追踪异步操作链
 */

import { createHook, AsyncResource } from "async_hooks";

interface AsyncContext {
  agentId: string;
  operation: string;
  startTime: number;
  parentResourceId: number;
}

const asyncContexts = new Map<number, AsyncContext>();

export function installAsyncHooks(monitorUrl: string) {
  const hook = createHook({
    init(asyncId, type, triggerAsyncId, resource) {
      // 记录异步资源创建
      const parent = asyncContexts.get(triggerAsyncId);

      if (parent) {
        asyncContexts.set(asyncId, {
          agentId: parent.agentId,
          operation: `${parent.operation}/${type}`,
          startTime: Date.now(),
          parentResourceId: triggerAsyncId,
        });
      }
    },

    before(asyncId) {
      // 异步操作开始
      const context = asyncContexts.get(asyncId);
      if (context) {
        sendEvent(monitorUrl, {
          type: "async_operation_start",
          agentId: context.agentId,
          operation: context.operation,
        });
      }
    },

    after(asyncId) {
      // 异步操作完成
      const context = asyncContexts.get(asyncId);
      if (context) {
        const duration = Date.now() - context.startTime;
        sendEvent(monitorUrl, {
          type: "async_operation_end",
          agentId: context.agentId,
          operation: context.operation,
          duration,
        });
      }
    },

    destroy(asyncId) {
      // 清理
      asyncContexts.delete(asyncId);
    },
  });

  hook.enable();
  console.log("✅ Async Hooks 监控已启用");
}

function sendEvent(monitorUrl: string, data: any) {
  // 发送到监控服务
  fetch(`${monitorUrl}/api/events`, {
    method: "POST",
    body: JSON.stringify(data),
  }).catch(() => {});
}

// 使用示例
installAsyncHooks("http://localhost:8000");
```

### 4.4 方案 3：Babel/AST 转换（编译时注入）

```javascript
// babel-plugin-agent-monitor.js
/**
 * Babel 插件 - 编译时自动注入监控代码
 */

module.exports = function ({ types: t }) {
  return {
    name: "agent-monitor-instrumentation",
    visitor: {
      // 拦截所有函数调用
      CallExpression(path, state) {
        // 检查是否是 Agent 方法
        if (isAgentMethod(path)) {
          // 在调用前注入监控代码
          path.insertBefore(
            t.expressionStatement(
              t.callExpression(t.identifier("__monitorAgentCall"), [
                t.stringLiteral(path.node.callee.name),
                t.arrayExpression(path.node.arguments),
              ])
            )
          );

          // 在调用后注入监控代码
          path.insertAfter(
            t.expressionStatement(
              t.callExpression(t.identifier("__monitorAgentReturn"), [
                t.identifier(path.node.callee.name),
              ])
            )
          );
        }
      },
    },
  };
};

// 使用 .babelrc
{
  "plugins": [
    ["./babel-plugin-agent-monitor", {
      "monitorUrl": "http://localhost:8000",
      "framework": "openclaw"
    }]
  ]
}
```

---

## 五、Python 监控方案（回顾）

### 5.1 CrewAI 事件监听

```python
# monitor/plugins/python/crewai_plugin.py
from crewai.events import crewai_event_bus
from crewai.events.types import AgentExecutionStartedEvent

class CrewAIPlugin:
    def install(self):
        @crewai_event_bus.on(AgentExecutionStartedEvent)
        def on_agent_start(source, event):
            self.send_event({
                "protocol": "agent-monitor",
                "version": "1.0",
                "timestamp": event.timestamp.isoformat(),
                "source": {
                    "server_id": self.server_id,
                    "agent_id": event.agent.id,
                    "framework": "crewai",
                    "language": "python"
                },
                "event": {
                    "type": "agent_online",
                    "data": {
                        "role": event.agent.role,
                        "goal": event.agent.goal
                    }
                }
            })
```

### 5.2 sys.setprofile（通用方案）

```python
# monitor/plugins/python/profiler.py
import sys

class UniversalPythonMonitor:
    def start(self):
        sys.setprofile(self._profile_func)

    def _profile_func(self, frame, event, arg):
        if event == "call":
            # 捕获所有 Python 函数调用
            func_name = frame.f_code.co_name
            filename = frame.f_code.co_filename

            if self._is_agent_function(filename, func_name):
                self.send_event({
                    "event_type": "function_call",
                    "function": func_name,
                    "file": filename
                })
```

---

## 六、多语言项目结构

```
agent-monitor-plugins/
├── python/                          # Python 插件
│   ├── setup.py
│   ├── agent_monitor/
│   │   ├── __init__.py
│   │   ├── plugins/
│   │   │   ├── base.py
│   │   │   ├── crewai_plugin.py
│   │   │   ├── langgraph_plugin.py
│   │   │   └── autogen_plugin.py
│   │   ├── core/
│   │   │   ├── profiler.py          # sys.setprofile
│   │   │   ├── monkey_patch.py      # 猴子补丁
│   │   │   └── import_hook.py       # Import Hook
│   │   └── protocol/
│   │       └── unified_event.py     # 统一协议
│   ├── tests/
│   └── README.md
│
├── typescript/                      # TypeScript 插件
│   ├── package.json
│   ├── src/
│   │   ├── index.ts
│   │   ├── proxy-interceptor.ts     # Proxy 拦截
│   │   ├── async-hooks.ts           # Async Hooks
│   │   ├── babel-plugin.ts          # Babel 插件
│   │   └── openclaw-adapter.ts      # OpenClaw 适配器
│   ├── tsconfig.json
│   ├── tests/
│   └── README.md
│
├── rust/                            # Rust 插件
│   ├── Cargo.toml
│   └── src/
│       ├── lib.rs
│       └── macro.rs                # 过程宏
│
├── go/                              # Go 插件
│   ├── go.mod
│   └── src/
│       └── interceptor.go
│
├── protocol/                        # 统一协议定义
│   ├── unified-event.ts             # TypeScript
│   ├── unified-event.py             # Python
│   ├── unified-event.rs             # Rust
│   └── README.md
│
└── docs/
    ├── architecture.md
    └── api.md
```

---

## 七、各语言监控方案对比

### 7.1 技术对比

| 语言 | 监控方式 | 侵入性 | 性能开销 | 可靠性 | 实现难度 |
|------|---------|--------|---------|--------|---------|
| **Python** | sys.setprofile / 事件总线 | 低 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **TypeScript** | Proxy / Async Hooks | 低 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **JavaScript** | Proxy / WeakRef | 低 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Rust** | 宏 / Trait 拦截 | 中 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Go** | 接口包装 | 中 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Java** | AOP / ByteBuddy | 中 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

### 7.2 功能对比

| 功能 | Python | TypeScript | Rust | Go |
|------|--------|-----------|------|-----|
| **方法调用拦截** | ✅ | ✅ | ✅ | ✅ |
| **异步追踪** | ⚠️ asyncio | ✅ Async Hooks | ✅ Future | ✅ goroutine |
| **LLM 调用监控** | ✅ | ✅ | ✅ | ✅ |
| **工具使用监控** | ✅ | ✅ | ✅ | ✅ |
| **Agent 关系追踪** | ✅ | ⚠️ 需框架支持 | ⚠️ 需框架支持 | ⚠️ 需框架支持 |
| **性能分析** | ✅ cProfile | ✅ V8 Inspector | ✅ perf | ✅ pprof |

---

## 八、使用示例

### 8.1 Python Agent 使用

```bash
# 1. 安装插件
pip install agent-monitor-python

# 2. 启用监控
export AGENT_MONITOR_ENABLED=true
export AGENT_MONITOR_URL=http://localhost:8000

# 3. 运行 Agent（无需修改代码）
python my_crewai_app.py
```

### 8.2 TypeScript Agent 使用

```bash
# 1. 安装插件
npm install @agent-monitor/typescript

# 2. 启用监控
export AGENT_MONITOR_URL=http://localhost:8000

# 3. 在代码中启用（只需一行）
import { instrumentAgent } from "@agent-monitor/typescript";

const agent = new OpenClawAgent({...});
const monitoredAgent = instrumentAgent(agent, {
  agentId: "my-agent",
  framework: "openclaw",
  monitorUrl: process.env.AGENT_MONITOR_URL
});

# 正常使用
await monitoredAgent.chat("Hello");
```

---

## 九、统一监控服务设计

### 9.1 API 接口

无论什么语言的 Agent，都使用相同的 API：

```typescript
// POST /api/events
// 接收所有语言的事件

interface EventRequest {
  events: UnifiedMonitorEvent[];  // 批量接收
}

interface EventResponse {
  success: boolean;
  received: number;
  errors?: string[];
}

// GET /api/agents
// 获取所有 Agent 状态

interface AgentState {
  agent_id: string;
  framework: string;
  language: Language;
  status: "online" | "offline" | "error";
  last_activity: string;
}

// GET /api/agents/:id/relationships
// 获取 Agent 关系图谱

interface AgentRelationship {
  from_agent: string;
  to_agent: string;
  type: "delegates" | "collaborates" | "reports_to";
  strength: number;
}
```

### 9.2 事件处理流程

```
┌────────────────┐
│  Python Agent  │
└────────┬───────┘
         │ HTTP POST
         ▼
┌─────────────────────────────────┐
│      API Gateway                │
│  - 验证事件格式                  │
│  - 协议转换（如需要）            │
│  - 负载均衡                      │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│      Message Queue               │
│  (Kafka / NATS / Redis)          │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│      Event Processor             │
│  - 状态聚合                      │
│  - 关系图谱构建                  │
│  - 告警检测                      │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│      Storage                     │
│  - PostgreSQL (元数据)           │
│  - Neo4j (关系图谱)              │
│  - ClickHouse (时序数据)         │
└─────────────────────────────────┘
```

---

## 十、实现路线图

### Phase 1: MVP（Python + TypeScript）

**目标：** 支持最流行的两种语言

**Python 插件：**
- ✅ CrewAI 事件监听
- ✅ LangGraph 回调
- ✅ sys.setprofile 通用方案

**TypeScript 插件：**
- ✅ Proxy 拦截
- ✅ OpenClaw 集成示例
- ✅ Async Hooks 追踪

**统一服务：**
- ✅ 统一事件协议
- ✅ API 网关
- ✅ 基础状态存储

**时间：** 4-6 周

### Phase 2: 扩展到其他语言

**Rust 插件：**
- 宏系统实现方法拦截
- Trait 拦截

**Go 插件：**
- 接口包装

**Java 插件：**
- ByteBuddy 字节码增强

**时间：** 6-8 周

### Phase 3: 企业级特性

- 多租户支持
- RBAC 权限控制
- 高可用部署
- 性能优化

**时间：** 8-10 周

---

## 十一、关键技术决策

### 11.1 语言选择

**为什么优先支持 Python 和 TypeScript？**

1. **Python (CrewAI, LangGraph, AutoGen)**
   - ✅ Agent 框架最成熟
   - ✅ 市场占有率最高
   - ✅ 社区最活跃

2. **TypeScript (OpenClaw, Vercel AI SDK)**
   - ✅ Web/Node.js 生态
   - ✅ 性能更好
   - ✅ 前后端统一

3. **其他语言（后续）**
   - Rust: 性能敏感场景
   - Go: 云原生场景
   - Java: 企业场景

### 11.2 传输协议

| 协议 | 优点 | 缺点 | 推荐场景 |
|------|------|------|---------|
| **HTTP/2** | 简单、通用 | 性能一般 | 通用场景 |
| **WebSocket** | 双向通信 | 连接管理复杂 | 实时性要求高 |
| **gRPC** | 高性能、强类型 | 复杂 | 大规模部署 |
| **UDP** | 最低延迟 | 不可靠 | 本地插件 → 代理 |

**推荐：**
- 插件 → 本地代理：UDP
- 本地代理 → 远程服务：HTTP/2
- 前端 ← 服务：WebSocket

---

## 十二、测试策略

### 12.1 跨语言测试

```yaml
# 测试矩阵
test_matrix:
  languages:
    - python:
        frameworks: [crewai, langgraph, autogen]
    - typescript:
        frameworks: [openclaw, vercel-ai-sdk]

  scenarios:
    - agent_lifecycle:
        - agent_online
        - agent_working
        - agent_offline
    - tool_usage:
        - tool_call
        - tool_result
    - llm_calls:
        - llm_start
        - llm_streaming
        - llm_end
```

### 12.2 集成测试

```python
# tests/integration/test_cross_language.py
def test_python_and_typescript_agents():
    """
    测试 Python 和 TypeScript Agent 的事件
    能被统一服务处理
    """

    # 1. 启动 Python Agent
    python_agent = CrewAIExample()
    python_agent.run()

    # 2. 启动 TypeScript Agent
    typescript_agent = OpenClawExample()
    typescript_agent.run()

    # 3. 验证事件格式统一
    events = monitor_service.get_all_events()

    for event in events:
        assert event["protocol"] == "agent-monitor"
        assert event["version"] == "1.0"
        assert "source" in event
        assert "event" in event

    # 4. 验证状态聚合
    agents = monitor_service.get_all_agents()

    python_agent_state = next(
        a for a in agents if a["agent_id"] == "python-agent-1"
    )
    assert python_agent_state["language"] == "python"
    assert python_agent_state["status"] == "online"

    typescript_agent_state = next(
        a for a in agents if a["agent_id"] == "ts-agent-1"
    )
    assert typescript_agent_state["language"] == "typescript"
    assert typescript_agent_state["status"] == "online"
```

---

## 十三、最佳实践

### 13.1 插件开发

1. **统一协议优先**
   - 所有插件发送相同格式的事件
   - 避免语言特定字段

2. **异步发送**
   - 不阻塞 Agent 执行
   - 使用独立线程/协程

3. **静默失败**
   - 监控服务故障不影响 Agent
   - 失败时本地缓存

4. **性能友好**
   - 批量发送
   - 数据压缩
   - 采样策略（高频事件）

### 13.2 错误处理

```typescript
// 统一的错误处理策略
function sendEventWithRetry(event: UnifiedMonitorEvent, maxRetries = 3) {
  let retries = 0;

  async function attemptSend(): Promise<boolean> {
    try {
      await fetch(`${MONITOR_URL}/api/events`, {
        method: "POST",
        body: JSON.stringify(event),
      });
      return true;
    } catch (error) {
      retries++;

      if (retries >= maxRetries) {
        // 失败时缓存到本地
        await cacheToDisk(event);
        return false;
      }

      // 指数退避
      await sleep(Math.pow(2, retries) * 1000);
      return attemptSend();
    }
  }

  // 异步执行，不阻塞
  attemptSend();
}
```

---

## 十四、常见问题

### Q1: 不同语言的 Agent 如何协作监控？

**A:** 通过统一的事件协议，无论什么语言的 Agent，都发送相同格式的事件到监控服务。

### Q2: 如何处理语言特定的功能？

**A:** 在 `event.data` 中使用语言特定的字段，但保持 `event.type` 跨语言统一。

### Q3: 性能开销如何？

**A:**
- Python 事件监听：几乎无开销（< 1%）
- Python sys.setprofile：中等开销（5-10%）
- TypeScript Proxy：低开销（< 2%）
- 整体影响：通常 < 5%

### Q4: 可以监控第三方 Agent 吗？

**A:** 可以，只要：
1. Agent 是用支持的语言编写
2. 可以加载监控插件
3. 或者通过 Sidecar 模式监控

---

## 十五、参考资源

### 15.1 Python 监控技术
- sys.setprofile: https://docs.python.org/3/library/sys.html#sys.setprofile
- AST 转换: https://docs.python.org/3/library/ast.html
- 猴子补丁: Python 动态特性

### 15.2 TypeScript 监控技术
- Proxy: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Proxy
- Async Hooks: https://nodejs.org/api/async_hooks.html
- Babel 插件: https://babeljs.io/docs/en/plugins

### 15.3 相关项目
- OpenTelemetry: 多语言可观测性标准
- Jaeger: 分布式追踪
- Prometheus: 监控系统

---

**文档维护**: 随项目进展持续更新

**最后更新**: 2025-02-06

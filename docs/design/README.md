# 设计文档导航

本目录包含分布式 Agent 监控系统的所有架构和设计文档。

## 📁 文档分类

### 📐 已有核心设计文档（01-06）

这些文档描述了 MVP 阶段的系统设计：

1. **[系统概览](01-system-overview.md)** - 系统整体架构
2. **[快照增量同步](02-snapshot-delta-sync.md)** - 数据同步机制
3. **[内存优先架构](03-memory-first-architecture.md)** - 内存架构设计
4. **[内存管理](04-memory-management.md)** - 内存管理策略
5. **[Agent 行为](05-agent-behavior.md)** - Agent 行为分析
6. **[CrewAI 事件映射](06-crewai-event-mapping.md)** - CrewAI 事件映射

### 🆕 分布式架构设计（07-17）

这些文档描述了扩展到分布式 Agent 的架构设计：

7. **[Agent 通用架构](07-agent-architecture.md)** - 通用运行形态、常驻进程、Agent 交互
8. **[编排和治理](08-orchestration.md)** - 部署架构、任务委派治理模式
9. **[分布式 CrewAI](09-distributed-crew.md)** - 分布式 CrewAI 架构（侵入式 Runtime）
10. **[透明代理](10-transparent-proxy.md)** - 无侵入式代理架构（**推荐方案**）
11. **[运行时 SDK](11-runtime-sdk.md)** - agent-runtime-sdk 实现方案
12. **[问题分析](12-issues-analysis.md)** - WebSocket 协议等问题分析
13. **[跨语言监控](13-cross-language-monitoring.md)** - 跨语言监控方案
14. **[分布式监控](17-distributed-monitoring.md)** - 分布式监控架构

### 🆕 部署和容器设计（14-18）

这些文档描述了部署方案和容器技术选型：

14. **[Agent 创建流程](14-agent-creation-flow.md)** - 完整的 Agent 创建流程
15. **[容器方案对比](15-container-alternatives.md)** - 容器技术全面对比
16. **[容器决策记录](16-container-decision.md)** - 容器方案决策记录
17. **[部署方案](../deployment-and-dataflow.md)** - 部署和数据流设计

---

## 🗺️ 架构演进路径

```
┌─────────────────────────────────────────────────────────────────┐
│  MVP 阶段 (已完成)                                            │
├─────────────────────────────────────────────────────────────────┤
│  • 单机 CrewAI 监控                                             │
│  • 内存优先架构                                                 │
│  • HTTP 事件上报                                                  │
│  • H2 内存数据库                                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  分布式阶段 (设计阶段)                                          │
├─────────────────────────────────────────────────────────────────┤
│   • 多容器 Agent 部署                                           │
│  • 透明代理通信 (推荐方案)                                       │
│  • Redis 消息队列                                               │
│  • Agent 编排和治理                                               │
│  • agent-runtime-sdk                                              │
│  • 跨语言框架支持                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📖 推荐阅读顺序

### 新手入门

1. 先阅读 **[项目概览](../OVERVIEW.md)** 了解整体
2. 再阅读 **[系统概览](01-system-overview.md)** 了解基础架构
3. 然后阅读 **[透明代理](10-transparent-proxy.md)** 了解推荐方案

### 架构深入

1. **[Agent 通用架构](07-agent-architecture.md)** - 运行形态和交互
2. **[编排和治理](08-orchestration.md)** - 部署和治理模式
3. **[运行时 SDK](11-runtime-sdk.md)** - SDK 实现细节

### 部署决策

1. **[Agent 创建流程](14-agent-creation-flow.md)** - 如何创建 Agent
2. **[容器方案对比](15-container-alternatives.md)** - 技术选型
3. **[容器决策记录](16-container-decision.md)** - 决策依据

---

## 🔗 相关文档

### 开发文档

- [开发文档索引](../dev/README.md)
- [开发日志索引](../dev/20260208_final_summary.md)

### 研究文档

- [Agent Hooks 研究](../research/agent-hooks.md)
- [跨语言监控研究](13-cross-language-monitoring.md)

### 问题记录

- [WebSocket 问题分析](12-issues-analysis.md)
- [CrewAI 事件映射](06-crewai-event-mapping.md)

---

**最后更新**: 2025-02-09

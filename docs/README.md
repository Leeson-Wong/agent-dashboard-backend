# Agent Dashboard Backend - 文档中心

> 分布式 Agent 监控系统 | 完整技术文档

---

## 快速导航

| 文档类型 | 说明 | 链接 |
|---------|------|------|
| **项目概览** | 项目简介、快速开始、技术栈 | [OVERVIEW.md](OVERVIEW.md) |
| **设计文档** | 架构设计、技术方案、决策记录 | [design/](design/) |
| **开发文档** | 开发日志、实现细节、测试指南 | [dev/](dev/) |
| **研究文档** | 技术调研、方案对比 | [research/](research/) |

---

## 文档结构

```
docs/
├── OVERVIEW.md                 # 项目概览和快速开始
├── README.md                   # 本文档
│
├── design/                     # 设计文档
│   ├── README.md               # 设计文档导航
│   ├── 01-06 *.md              # MVP 阶段设计
│   └── 07-17 *.md              # 分布式阶段设计
│
├── dev/                        # 开发文档
│   ├── README.md               # 开发文档导航
│   └── 20260208_*.md           # 开发日志（按日期）
│
└── research/                   # 研究文档
    └── agent-hooks.md          # Agent Hooks 技术调研
```

---

## 新手入门指南

### 第一次接触本项目？

建议按以下顺序阅读：

1. **[项目概览](OVERVIEW.md)** - 了解项目背景和技术栈
2. **[系统概览](design/01-system-overview.md)** - 理解基础架构
3. **[透明代理](design/10-transparent-proxy.md)** - 了解推荐方案（分布式核心）

### 想了解分布式架构？

1. **[Agent 通用架构](design/07-agent-architecture.md)** - 运行形态和交互
2. **[编排和治理](design/08-orchestration.md)** - 部署和治理模式
3. **[运行时 SDK](design/11-runtime-sdk.md)** - SDK 实现细节

### 想参与开发？

1. **[开发日志索引](dev/20260208_final_summary.md)** - 查看 MVP 完成总结
2. **[生产就绪清单](dev/20260208_production_readiness_checklist.md)** - 生产部署前检查

### 想了解技术选型？

1. **[容器方案对比](design/15-container-alternatives.md)** - Docker vs Podman vs Systemd
2. **[容器决策记录](design/16-container-decision.md)** - 决策依据
3. **[Agent Hooks 研究](research/agent-hooks.md)** - 主流框架 Hook 机制

---

## 架构演进

### MVP 阶段（已完成）

```
单机 CrewAI 监控
├── 内存优先架构
├── HTTP 事件上报
└── H2 内存数据库
```

**相关文档**: [01-06](design/README.md#已有核心设计文档01-06)

### 分布式阶段（设计中）

```
多容器 Agent 部署
├── 透明代理通信
├── Redis 消息队列
├── Agent 编排和治理
└── agent-runtime-sdk
```

**相关文档**: [07-17](design/README.md#分布式架构设计07-17)

---

## 按场景查找文档

### 场景 1: 我想快速启动项目

→ 阅读 [项目概览 - 快速开始](OVERVIEW.md#快速开始)

### 场景 2: 我想了解 Agent 如何创建

→ 阅读 [Agent 创建流程](design/14-agent-creation-flow.md)

### 场景 3: 我想选择容器方案

→ 阅读 [容器方案对比](design/15-container-alternatives.md) 和 [容器决策记录](design/16-container-decision.md)

### 场景 4: 我想了解分布式通信

→ 阅读 [透明代理设计](design/10-transparent-proxy.md)

### 场景 5: 我想了解跨语言监控

→ 阅读 [跨语言监控方案](design/13-cross-language-monitoring.md)

### 场景 6: 我想了解 WebSocket 问题

→ 阅读 [问题分析](design/12-issues-analysis.md)

---

## 项目里程碑

| 阶段 | 状态 | 交付物 | 文档 |
|------|------|--------|------|
| **MVP** | ✅ 完成 | 单机监控 | [01-06](design/) |
| **分布式设计** | 🔄 进行中 | 架构设计 | [07-17](design/) |
| **SDK 实现** | ⏳ 待开始 | agent-runtime-sdk | [11](design/11-runtime-sdk.md) |
| **生产部署** | ⏳ 待开始 | 容器化部署 | [14-16](design/) |

---

## 贡献指南

### 文档规范

- 设计文档使用编号前缀（01-17）
- 开发文档使用日期前缀（YYYYMMDD）
- Markdown 格式，使用中文

### 更新文档

修改文档时请确保：
1. 保持文档之间的引用链接正确
2. 更新相应 README 中的索引
3. 在文档底部注明最后更新日期

---

## 外部链接

- **GitHub 仓库**: (待填写)
- **Issue 跟踪**: (待填写)
- **API 文档**: (待填写)

---

**最后更新**: 2025-02-09
**文档版本**: 1.0.0

# 设计文档汇总说明

本文档记录了分布式 Agent 监控系统的所有设计文档，并说明它们在项目结构中的位置。

## 新增设计文档汇总

### 架构设计类 (适合 docs/design/)

| 文档 | 说明 | 建议位置 |
|------|------|----------|
| `AGENT_ARCHITECTURE_DESIGN.md` | Agent 通用架构设计：运行形态、常驻进程、Agent 交互 | `docs/design/07-agent-architecture.md` |
| `AGENT_ORCHESTRATION_DESIGN.md` | 部署架构、任务委派治理模式 | `docs/design/08-orchestration.md` |
| `DISTRIBUTED_CREW_DESIGN.md` | 分布式 CrewAI 架构（侵入式 Runtime） | `docs/design/09-distributed-crew.md` |
| `TRANSPARENT_PROXY_DESIGN.md` | 无侵入式代理架构（推荐方案） | `docs/design/10-transparent-proxy.md` |

### 运行时和通信类 (适合 docs/design/)

| 文档 | 说明 | 建议位置 |
|------|------|----------|
| `RUNTIME_SDK_IMPLEMENTATION.md` | agent-runtime-sdk 实现方案 | `docs/design/11-runtime-sdk.md` |
| `ISSUES_ANALYSIS.md` | WebSocket 协议等问题分析 | `docs/design/12-issues-analysis.md` |
| `distributed-monitor-architecture.md` | 分布式监控架构 | `docs/design/13-distributed-monitoring.md` |
| `cross-language-monitoring.md` | 跨语言监控 | `docs/design/14-cross-language-monitoring.md` |

### 容器和部署类 (适合 docs/design/)

| 文档 | 说明 | 建议位置 |
|------|------|----------|
| `AGENT_CREATION_FLOW.md` | Agent 创建完整流程 | `docs/design/15-agent-creation-flow.md` |
| `CONTAINER_ALTERNATIVES.md` | 容器方案全面对比 | `docs/design/16-container-alternatives.md` |
| `CONTAINER_DECISION_RECORD.md` | 容器方案决策记录 | `docs/design/17-container-decision.md` |
| `deployment-and-dataflow.md` | 部署和数据流 | `docs/design/18-deployment.md` |

### 项目总结类 (适合 docs/)

| 文档 | 说明 | 建议位置 |
|------|------|----------|
| `PROJECT_SUMMARY.md` | 项目整体总结 | `docs/README.md` 或 `docs/OVERVIEW.md` |

### 研究文档 (适合 docs/research/)

| 文档 | 说明 | 建议位置 |
|------|------|----------|
| `agent_hooks_research.md` | Agent Hooks 研究 | `docs/research/agent-hooks.md` |

---

## 建议的文档组织结构

```
agent-dashboard-backend/docs/
├── README.md                           # 概览和导航
├── design/                             # 设计文档
│   ├── 01-system-overview.md          # 已存在
│   ├── 02-snapshot-delta-sync.md       # 已存在
│   ├── 03-memory-first-architecture.md  # 已存在
│   ├── 04-memory-management.md        # 已存在
│   ├── 05-agent-behavior.md           # 已存在
│   ├── 06-crewai-event-mapping.md     # 已存在
│   ├── 07-agent-architecture.md       # 新增：通用架构
│   ├── 08-orchestration.md             # 新增：编排和治理
│   ├── 09-distributed-crew.md          # 新增：分布式CrewAI
│   ├── 10-transparent-proxy.md        # 新增：透明代理
│   ├── 11-runtime-sdk.md              # 新增：运行时SDK
│   ├── 12-issues-analysis.md           # 新增：问题分析
│   ├── 13-distributed-monitoring.md    # 新增：分布式监控
│   ├── 14-cross-language-monitoring.md # 新增：跨语言监控
│   ├── 15-agent-creation-flow.md      # 新增：创建流程
│   ├── 16-container-alternatives.md    # 新增：容器方案
│   ├── 17-container-decision.md       # 新增：容器决策
│   ├── 18-deployment.md                # 新增：部署方案
│   └── README.md                         # 设计文档导航
├── dev/                                # 开发文档
│   ├── (保留所有现有文件)
│   └── README.md                         # 开发文档导航
├── research/                            # 研究文档（新增）
│   ├── agent-hooks.md                    # Agent Hooks研究
│   └── README.md                         # 研究文档导航
└── OVERVIEW.md                          # 项目整体概览（新增）
```

---

## 执行计划

需要将以下文档复制/移动到对应位置：

### 复制到 docs/design/ (7个新文档)

1. `AGENT_ARCHITECTURE_DESIGN.md` → `docs/design/07-agent-architecture.md`
2. `AGENT_ORCHESTRATION_DESIGN.md` → `docs/design/08-orchestration.md`
3. `DISTRIBUTED_CREW_DESIGN.md` → `docs/design/09-distributed-crew.md`
4. `TRANSPARENT_PROXY_DESIGN.md` → `docs/design/10-transparent-proxy.md`
5. `RUNTIME_SDK_IMPLEMENTATION.md` → `docs/design/11-runtime-sdk.md`
6. `ISSUES_ANALYSIS.md` → `docs/design/12-issues-analysis.md`
7. `cross-language-monitoring.md` → `docs/design/13-cross-language-monitoring.md`
8. `AGENT_CREATION_FLOW.md` → `docs/design/14-agent-creation-flow.md`
9. `CONTAINER_ALTERNATIVES.md` → `docs/design/15-container-alternatives.md`
10. `CONTAINER_DECISION_RECORD.md` → `docs/design/16-container-decision.md`
11. `distributed-monitor-architecture.md` → `docs/design/17-distributed-monitoring.md`

### 复制到 docs/

12. `PROJECT_SUMMARY.md` → `docs/OVERVIEW.md`

### 复制到 docs/research/ (新建目录)

13. `agent_hooks_research.md` → `docs/research/agent-hooks.md`

---

## 需要创建的导航文件

1. **docs/design/README.md** - 设计文档导航
2. **docs/dev/README.md** - 开发文档导航（如不存在）
3. **docs/research/README.md** - 研究文档导航
4. **docs/OVERVIEW.md** - 项目整体概览

---

## 说明

- **保留现有文档**：所有已存在的 docs/design/ 和 docs/dev/ 文件保持不变
- **新增文档**：按编号顺序添加，避免与现有编号冲突
- **交叉引用**：更新相关文档中的相互引用路径
- **索引更新**：创建导航文件，便于查找相关文档

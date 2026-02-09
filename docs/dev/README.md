# 开发文档索引

本目录包含 Agent 监控系统的开发日志、实现细节和测试指南。

---

## 📁 文档分类

### 📊 开发总览

| 文档 | 说明 | 日期 |
|------|------|------|
| **[MVP 完成总结](20260208_final_summary.md)** | MVP 阶段所有功能完成情况 | 2025-02-08 |
| **[项目完成评估](20260208_project_completion_assessment.md)** | 功能完成度、技术债务、改进建议 | 2025-02-08 |
| **[生产就绪清单](20260208_production_readiness_checklist.md)** | 上线前需要检查的项目 | 2025-02-08 |

---

### 🏗️ 架构实现

#### 快照增量同步

| 文档 | 说明 |
|------|------|
| **[后端实现](20260208_snapshot_delta_sync_backend.md)** | SnapshotService、DeltaService 实现 |
| **[Part 1](20260208_snapshot_delta_sync_part1.md)** | 需求分析、数据模型设计 |
| **[Part 2](20260208_snapshot_delta_sync_part2.md)** | 服务接口定义、实现逻辑 |
| **[Part 3](20260208_snapshot_delta_sync_part3.md)** | API Controller、前端集成 |
| **[测试指南](20260208_snapshot_delta_sync_testing.md)** | 单元测试、集成测试用例 |

#### 内存管理

| 文档 | 说明 |
|------|------|
| **[Part 1](20260208_memory_management_part1.md)** | 内存分析、优化策略 |
| **[Part 2](20260208_memory_management_part2.md)** | 内存限制实现、数据清理 |
| **[Part 3](20260208_memory_management_part3.md)** | 监控指标、告警机制 |

#### Agent 行为分析

| 文档 | 说明 |
|------|------|
| **[Part 1](20260208_agent_behavior_part1.md)** | 行为追踪设计、数据模型 |
| **[Part 2](20260208_agent_behavior_part2.md)** | 状态机设计、行为分类 |
| **[Part 3](20260208_agent_behavior_part3.md)** | 统计聚合、查询接口 |
| **[Part 4](20260208_agent_behavior_part4.md)** | 可视化支持、实时更新 |
| **[测试指南](20260208_agent_behavior_testing_guide.md)** | 测试用例、验证方法 |

#### CrewAI 事件映射

| 文档 | 说明 |
|------|------|
| **[事件映射](20260208_crewai_event_mapping.md)** | CrewAI 事件到 AgentState 的映射规则 |

---

### 🐛 问题修复

| 文档 | 问题 | 解决方案 |
|------|------|----------|
| **[Liquibase 重复列修复](20260208_liquibase_duplicate_column_fix.md)** | agent_id 列重复创建 | 修改 changelog |
| **[Spring Context 修复](20260208_spring_context_fix.md)** | AgentStateRepository 注入失败 | 检查组件扫描 |
| **[临时文件清理](20260208_temporary_files_cleanup.md)** | 测试产生临时文件 | 添加清理钩子 |
| **[测试修复](20260208_test_fixes.sql.md)** | SQL 测试数据问题 | 修复测试数据 |

---

### 🧪 测试指南

| 文档 | 说明 |
|------|------|
| **[测试执行指南](20260208_test_execution_guide.md)** | 如何运行单元测试、集成测试 |

---

### 🔧 功能实现

| 文档 | 功能 |
|------|------|
| **[工具使用统计实现](20260208_tool_usage_stats_implementation.md)** | ToolUsageStatsService |

---

## 📖 阅读建议

### 新加入开发者

1. 先阅读 **[MVP 完成总结](20260208_final_summary.md)** 了解整体进度
2. 再阅读 **[项目完成评估](20260208_project_completion_assessment.md)** 了解技术债务
3. 然后根据感兴趣的功能深入阅读对应文档

### 了解特定功能

| 如果你想了解... | 阅读这些文档 |
|----------------|--------------|
| 快照增量同步机制 | [snapshot_delta_sync 系列](#快照增量同步) |
| 内存管理策略 | [memory_management 系列](#内存管理) |
| Agent 行为分析 | [agent_behavior 系列](#agent-行为分析) |
| CrewAI 事件处理 | [crewai_event_mapping](#crewai-事件映射) |
| 如何运行测试 | [测试执行指南](20260208_test_execution_guide.md) |
| 上线前检查 | [生产就绪清单](20260208_production_readiness_checklist.md) |

### 修复 Bug

1. 查看 **[问题修复](#-问题修复)** 部分
2. 搜索类似问题的解决方案
3. 参考 **[测试执行指南](20260208_test_execution_guide.md)** 运行测试

---

## 🔗 相关文档

### 设计文档

- [设计文档导航](../design/README.md)
- [系统概览](../design/01-system-overview.md)
- [快照增量同步设计](../design/02-snapshot-delta-sync.md)

### 研究文档

- [Agent Hooks 研究](../research/agent-hooks.md)

---

## 📝 开发日志规范

### 命名规则

```
YYYYMMDD_功能描述_部分.md
```

示例：
- `20260208_snapshot_delta_sync_part1.md`
- `20260208_memory_management_part2.md`

### 文档模板

```markdown
# 标题

> **日期**: YYYY-MM-DD
> **作者**: XXX
> **状态**: 进行中/已完成

## 背景
描述为什么需要这个功能/修复

## 实现方案
详细描述实现细节

## 代码示例
关键代码片段

## 测试
如何测试这个功能

## 备注
其他需要注意的事项
```

---

## 🎯 当前开发状态

| 模块 | 状态 | 完成度 |
|------|------|--------|
| 快照增量同步 | ✅ 完成 | 100% |
| 内存管理 | ✅ 完成 | 100% |
| Agent 行为分析 | ✅ 完成 | 100% |
| CrewAI 事件映射 | ✅ 完成 | 100% |
| 单元测试 | ✅ 完成 | 100% |
| 集成测试 | ✅ 完成 | 100% |

---

**最后更新**: 2025-02-09

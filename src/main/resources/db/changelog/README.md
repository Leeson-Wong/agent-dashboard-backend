# 预设数据说明

本文档说明了 Agent Dashboard 的预设数据初始化。

## 概述

预设数据包括:
- **Agent Templates**: 预定义的 Agent 配置模板
- **Memories**: 示例 Memory 记录

## Agent Templates

预设的 Agent 模板覆盖了三个主流 Agent 框架:

### CrewAI 模板

| Template ID | 名称 | 类别 | 角色 |
|------------|------|------|------|
| `crewai-researcher` | CrewAI 研究员 | research | 高级研究员 |
| `crewai-writer` | CrewAI 作家 | content | 技术作家 |
| `crewai-analyst` | CrewAI 数据分析师 | analysis | 数据分析师 |

### LangChain 模板

| Template ID | 名称 | 类别 | 角色 |
|------------|------|------|------|
| `langchain-assistant` | LangChain 智能助手 | assistant | AI 智能助手 |
| `langchain-coder` | LangChain 代码助手 | development | 程序员 |

### AutoGen 模板

| Template ID | 名称 | 类别 | 角色 |
|------------|------|------|------|
| `autogen-manager` | AutoGen 项目经理 | management | 项目经理 |
| `autogen-reviewer` | AutoGen 代码审查员 | development | 代码审查员 |

## Memories

预设的 Memory 记录包括:

| Memory ID | 名称 | 角色 |
|-----------|------|------|
| `mem-researcher-001` | 研究员 Agent 记忆 | 高级研究员 |
| `mem-writer-001` | 作家 Agent 记忆 | 技术作家 |
| `mem-coder-001` | 程序员 Agent 记忆 | 程序员 |

## 应用预设数据

### 使用 Liquibase

预设数据 changeset 使用 `context: dev` 标记，默认只在开发环境应用。

```bash
# 开发环境 - 会应用预设数据
mvn liquibase:update

# 生产环境 - 跳过预设数据
mvn liquibase:update -Dliquibase.contexts=prod
```

### 手动执行 SQL

如果需要手动执行，可以导出 SQL 并在数据库中运行:

```bash
mvn liquibase:updateSQL
```

生成的 SQL 文件位于 `target/liquibase/migrate.sql`

## 自定义预设数据

### 添加新模板

1. 创建新的 changeset 文件: `YYYYMMDD-add-custom-templates.yaml`
2. 添加 insert 语句到 `agent_templates` 表
3. 在 `db.changelog-master.yaml` 中引用新文件

### 添加新 Memory

1. 创建新的 changeset 文件: `YYYYMMDD-add-custom-memories.yaml`
2. 添加 insert 语句到 `memories` 表
3. 在 `db.changelog-master.yaml` 中引用新文件

## 模板字段说明

### AgentTemplate

| 字段 | 类型 | 说明 |
|------|------|------|
| template_id | VARCHAR(36) | 唯一标识符 |
| name | VARCHAR(255) | 模板名称 |
| type | VARCHAR(50) | 框架类型 (CrewAI, LangChain, AutoGen) |
| category | VARCHAR(50) | 类别 (research, content, development, etc.) |
| role | TEXT | Agent 角色 |
| goal | TEXT | Agent 目标 |
| backstory | TEXT | Agent 背景故事 |
| persona | TEXT | Agent 人格特征 |
| skills | JSON | 技能列表 |
| tools | JSON | 工具列表 |
| default_temperature | DECIMAL(3,2) | 默认温度参数 (0.0-1.0) |
| default_max_tokens | INT | 默认最大 token 数 |
| enabled | BOOLEAN | 是否启用 |

### Memory

| 字段 | 类型 | 说明 |
|------|------|------|
| memory_id | VARCHAR(36) | 唯一标识符 |
| name | VARCHAR(255) | Memory 名称 |
| type | VARCHAR(50) | 类型 (standard, enhanced, etc.) |
| status | VARCHAR(20) | 状态 (active, inactive) |
| role | TEXT | 角色 |
| persona | TEXT | 人格特征 |
| goal | TEXT | 目标 |
| backstory | TEXT | 背景故事 |
| experiences | JSON | 经验数据 |
| knowledge | JSON | 知识库数据 |
| skills | JSON | 技能数据 |
| relationships | JSON | 关系图数据 |

## 注意事项

1. **Context 属性**: 预设数据使用 `context: dev`，生产环境不会自动应用
2. **ID 唯一性**: 确保 template_id 和 memory_id 全局唯一
3. **JSON 格式**: skills, tools, experiences 等字段必须是有效的 JSON 数组或对象
4. **时间格式**: 时间戳使用 ISO 8601 格式 (如: `2025-01-01T00:00:00Z`)

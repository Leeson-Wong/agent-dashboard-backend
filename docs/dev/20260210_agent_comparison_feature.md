# Agent 对比视图功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

用户需要比较两个 Agent 的配置和状态差异，以便理解它们的区别并做出决策。Agent 对比视图提供了并排对比功能，清晰显示两个 Agent 之间的差异。

## 需求分析

### 核心需求

1. **并排对比** - 两个 Agent 属性并排显示
2. **差异高亮** - 自动标识不同的属性
3. **相似度计算** - 计算两个 Agent 的相似度百分比
4. **过滤模式** - 仅查看差异或查看全部属性
5. **导出报告** - 导出对比报告为 JSON

### 技术要求

- 使用 AgentState 类型中实际存在的属性
- 支持属性值的格式化显示
- 提供友好的 UI 和交互

## 实现方案

### 1. 创建对比 Composable

**文件**: `src/composables/useAgentComparison.ts`

#### 核心数据结构

```typescript
export interface ComparisonItem {
  label: string
  key: string
  valueA?: string | number | boolean
  valueB?: string | number | boolean
  type?: 'text' | 'boolean' | 'number' | 'array' | 'object'
  different?: boolean
}

export type ActivityFilter = 'all' | 'info' | 'success' | 'warning' | 'error'
```

#### 主要功能

**1. 设置对比 Agent**

```typescript
const setAgents = (a: AgentState | null, b: AgentState | null): void => {
  agentA.value = a
  agentB.value = b
  isComparing.value = a !== null && b !== null
}
```

**2. 对比项目**

基于 AgentState 类型，对比以下属性：
- Agent ID
- 服务器 ID
- 状态
- 框架
- 语言
- 角色
- 当前活动
- 当前工具
- 当前任务
- Memory ID
- 收藏状态
- 创建时间
- 最后活动时间

```typescript
const comparisonItems = computed((): ComparisonItem[] => {
  if (!agentA.value || !agentB.value) {
    return []
  }

  const a = agentA.value
  const b = agentB.value

  return [
    {
      label: 'Agent ID',
      key: 'agentId',
      valueA: a.agentId,
      valueB: b.agentId,
      type: 'text',
      different: a.agentId !== b.agentId
    },
    {
      label: '状态',
      key: 'status',
      valueA: a.status,
      valueB: b.status,
      type: 'text',
      different: a.status !== b.status
    },
    // ... 更多属性
  ]
})
```

**3. 差异过滤**

```typescript
// Get differences only
const differences = computed((): ComparisonItem[] => {
  return comparisonItems.value.filter(item => item.different)
})

// Get difference count
const differenceCount = computed((): number => {
  return differences.value.length
})
```

**4. 相似度计算**

```typescript
// Get similarity percentage
const similarityPercentage = computed((): number => {
  if (comparisonItems.value.length === 0) return 0
  const sameCount = comparisonItems.value.filter(item => !item.different).length
  return Math.round((sameCount / comparisonItems.value.length) * 100)
})
```

**5. 值格式化**

```typescript
// Format value for display
const formatValue = (value: string | number | boolean | undefined, type?: string): string => {
  if (value === undefined || value === null) return 'N/A'
  if (type === 'boolean') return value ? '是' : '否'
  if (type === 'number') return String(value)
  return String(value)
}

// Get value display style
const getValueStyle = (value: string | number | boolean | undefined, type?: string) => {
  if (type === 'boolean') {
    return value === true ? 'color: #22c55e;' : value === false ? 'color: #ef4444;' : ''
  }
  return ''
}
```

### 2. 创建对比视图组件

**文件**: `src/components/AgentComparison.vue`

#### 组件结构

```
AgentComparison (Modal)
├── Header
│   ├── Title ("Agent 对比")
│   ├── Stats
│   │   ├── Similarity Badge
│   │   └── Difference Count
│   └── Close Button
├── Content
│   ├── Selection Bar
│   │   ├── Agent A Info
│   │   ├── VS Badge
│   │   └── Agent B Info
│   ├── Filter Tabs
│   │   ├── All Items
│   │   └── Differences Only
│   └── Comparison Table
│       ├── Header Row
│       │   ├── Property
│       │   ├── Agent A Value
│       │   ├── Divider (= / ≠)
│       │   └── Agent B Value
│       └── Data Rows
└── Footer
    ├── Close Button
    └── Export Button
```

#### 功能特性

1. **Agent 信息显示**
   - 显示两个对比 Agent 的角色名称
   - VS 标识显示对比关系

2. **过滤标签**
   - 全部属性：显示所有对比项目
   - 仅差异：只显示不同的属性

3. **对比表格**
   - 并排显示两个 Agent 的属性值
   - 差异指示器：= 表示相同，≠ 表示不同
   - 差异行高亮显示

4. **相似度徽章**
   - 高相似度（≥80%）：绿色
   - 中等相似度（50-79%）：黄色
   - 低相似度（<50%）：红色

5. **导出功能**
   - 导出对比报告为 JSON 文件

#### 组件实现

```vue
<template>
  <Transition name="modal">
    <div v-if="isOpen && (agentA || agentB)" class="comparison-overlay">
      <div class="comparison-container">
        <div class="comparison-header">
          <h2>Agent 对比</h2>
          <div class="header-stats">
            <span class="similarity-badge" :class="getSimilarityClass()">
              相似度: {{ similarityPercentage }}%
            </span>
            <span v-if="differenceCount > 0" class="difference-count">
              {{ differenceCount }} 项差异
            </span>
          </div>
        </div>

        <div class="comparison-content">
          <!-- Selection Bar -->
          <div class="selection-bar">
            <div class="selection-side">
              <span>Agent A:</span>
              <span>{{ agentA?.role || agentA?.agentId }}</span>
            </div>
            <div class="selection-divider">
              <span class="vs-badge">VS</span>
            </div>
            <div class="selection-side">
              <span>Agent B:</span>
              <span>{{ agentB?.role || agentB?.agentId }}</span>
            </div>
          </div>

          <!-- Filter Tabs -->
          <div class="filter-tabs">
            <button :class="{ active: viewMode === 'all' }" @click="viewMode = 'all'">
              全部 ({{ comparisonItems.length }})
            </button>
            <button :class="{ active: viewMode === 'differences' }" @click="viewMode = 'differences'">
              仅差异 ({{ differenceCount }})
            </button>
          </div>

          <!-- Comparison Table -->
          <table class="comparison-table">
            <thead>
              <tr>
                <th>属性</th>
                <th>{{ agentA?.role || agentA?.agentId }}</th>
                <th></th>
                <th>{{ agentB?.role || agentB?.agentId }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in displayItems" :key="item.key" :class="{ 'is-different': item.different }">
                <td>{{ item.label }}</td>
                <td>{{ formatValue(item.valueA, item.type) }}</td>
                <td>{{ item.different ? '≠' : '=' }}</td>
                <td>{{ formatValue(item.valueB, item.type) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </Transition>
</template>
```

### 3. 集成到 Agent 列表面板

**文件**: `src/components/AgentListPanel.vue`

#### 添加对比按钮

```vue
<button
  class="compare-btn"
  :disabled="selectedAgentIds.size < 2"
  @click="openComparison"
>
  ⚖️ 对比 ({{ selectedAgentIds.size }})
</button>
```

#### 添加对比状态和方法

```typescript
// State
const showComparison = ref(false)

// Get comparison agents (first 2 selected)
const comparisonAgents = computed(() => {
  const ids = Array.from(selectedAgentIds.value).slice(0, 2)
  return ids.map(id => props.agents.find(a => a.agentId === id))
    .filter((a): a is AgentState => a !== undefined)
})

// Open comparison
const openComparison = (): void => {
  if (selectedAgentIds.value.size < 2) {
    success('请选择至少 2 个 Agent 进行对比')
    return
  }
  showComparison.value = true
}

// Close comparison
const closeComparison = (): void => {
  showComparison.value = false
}
```

#### 添加对比组件到模板

```vue
<AgentComparison
  :is-open="showComparison"
  :agent-a="comparisonAgents[0] || null"
  :agent-b="comparisonAgents[1] || null"
  @close="closeComparison"
/>
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **对比显示** ✅
   - 选择 2 个 Agent 后可以打开对比视图
   - Agent 信息正确显示在顶部

2. **属性对比** ✅
   - 所有 AgentState 属性都正确显示
   - 值格式化正确（日期、布尔值等）

3. **差异标识** ✅
   - 相同属性显示 = 符号
   - 不同属性显示 ≠ 符号
   - 差异行高亮显示

4. **相似度计算** ✅
   - 相似度百分比计算正确
   - 徽章颜色根据相似度正确显示

5. **过滤模式** ✅
   - 全部模式显示所有属性
   - 仅差异模式只显示不同的属性

6. **导出功能** ✅
   - 导出 JSON 格式的对比报告
   - 文件名包含时间戳

## 样式实现

### 对比按钮

```css
.compare-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.compare-btn:hover:not(:disabled) {
  background: rgba(139, 92, 246, 0.25);
  border-color: rgba(139, 92, 246, 0.4);
}

.compare-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
```

### 对比表格

```css
.comparison-table {
  width: 100%;
  border-collapse: collapse;
}

.comparison-table tbody tr.is-different {
  background: rgba(251, 191, 36, 0.05);
}

.diff-indicator {
  font-size: 16px;
  color: #f59e0b;
  font-weight: bold;
}

.same-indicator {
  font-size: 16px;
  color: #22c55e;
  opacity: 0.5;
}
```

### 相似度徽章

```css
.similarity-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
}

.similarity-badge.high {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.similarity-badge.medium {
  background: rgba(251, 191, 36, 0.2);
  color: #f59e0b;
}

.similarity-badge.low {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}
```

## 技术要点

### 1. 类型安全的属性访问

使用 AgentState 类型中实际存在的属性：

```typescript
const comparisonItems = computed((): ComparisonItem[] => {
  return [
    {
      label: '状态',
      key: 'status',
      valueA: a.status,
      valueB: b.status,
      type: 'text',
      different: a.status !== b.status
    },
    // 只使用存在的属性
  ]
})
```

### 2. 可选值处理

使用 `|| 'N/A'` 处理可选属性：

```typescript
{
  label: '角色',
  key: 'role',
  valueA: a.role || 'N/A',
  valueB: b.role || 'N/A',
  type: 'text',
  different: (a.role || 'N/A') !== (b.role || 'N/A')
}
```

### 3. 布尔值格式化

布尔值转换为中文显示：

```typescript
const formatValue = (value: unknown, type?: string): string => {
  if (value === undefined || value === null) return 'N/A'
  if (type === 'boolean') return value ? '是' : '否'
  return String(value)
}
```

### 4. 相似度计算

基于相同属性数量计算百分比：

```typescript
const similarityPercentage = computed((): number => {
  if (comparisonItems.value.length === 0) return 0
  const sameCount = comparisonItems.value.filter(item => !item.different).length
  return Math.round((sameCount / comparisonItems.value.length) * 100)
})
```

### 5. 导出报告

生成包含差异详情的 JSON 报告：

```typescript
const exportComparison = (): void => {
  const report = {
    timestamp: new Date().toISOString(),
    agentA: agentA.value?.agentId,
    agentB: agentB.value?.agentId,
    similarityPercentage: similarityPercentage.value,
    differenceCount: differenceCount.value,
    differences: differences.value.map(item => ({
      property: item.label,
      valueA: item.valueA,
      valueB: item.valueB
    }))
  }

  const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `agent-comparison-${Date.now()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
```

## 文件变更

### 新增文件

1. **src/composables/useAgentComparison.ts** (~230 行)
   - 对比状态管理
   - 属性对比逻辑
   - 差异过滤
   - 相似度计算
   - 值格式化

2. **src/components/AgentComparison.vue** (~370 行)
   - 对比视图 UI 组件
   - 过滤标签
   - 对比表格
   - 导出功能

### 修改文件

1. **src/components/AgentListPanel.vue**
   - 导入 AgentComparison 组件
   - 添加对比按钮
   - 添加对比状态和方法
   - 添加对比按钮样式

## 使用说明

### 基本使用

```vue
<template>
  <!-- Select 2 agents, then click compare button -->
  <AgentComparison
    :is-open="showComparison"
    :agent-a="agentA"
    :agent-b="agentB"
    @close="closeComparison"
  />
</template>

<script setup lang="ts">
import { useAgentComparison } from './composables/useAgentComparison'

const {
  agentA,
  agentB,
  setAgents,
  similarityPercentage,
  differenceCount
} = useAgentComparison()

// Set agents for comparison
setAgents(agent1, agent2)
</script>
```

## 已知限制

1. **属性限制** - 只能对比 AgentState 类型中定义的属性
2. **仅支持 2 个** - 目前只支持对比 2 个 Agent
3. **无深度对比** - 不支持嵌套对象或数组的深度对比

## 未来改进

1. **多 Agent 对比** - 支持同时对比多个 Agent（3 个或更多）
2. **深度对比** - 支持 Memory 等嵌套对象的对比
3. **历史对比** - 保存对比历史记录
4. **图表可视化** - 使用雷达图等图表可视化差异
5. **自定义属性** - 允许用户选择要对比的属性

## 总结

Agent 对比视图功能成功实现了：

✅ **并排对比** - 两个 Agent 属性并排显示
✅ **差异高亮** - 自动标识不同的属性
✅ **相似度计算** - 计算相似度百分比
✅ **过滤模式** - 仅查看差异或全部
✅ **导出报告** - 导出对比报告
✅ **友好 UI** - 清晰的对比界面和交互
✅ **类型安全** - 使用正确的 AgentState 属性

该功能为用户提供了强大的 Agent 对比分析能力，便于理解 Agent 之间的差异和相似性。

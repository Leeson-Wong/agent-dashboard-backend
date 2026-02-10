# Agent Sort 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，用户可能需要按不同字段对 Agent 列表进行排序：
- 按 Agent ID 排序（默认）
- 按角色名称排序
- 按状态排序
- 按最后活动时间排序
- 按框架或语言排序

## 需求分析

### 核心需求

1. **多字段排序** - 支持按多个字段排序
2. **升序/降序** - 可切换排序顺序
3. **持久化** - 保存用户的排序偏好
4. **可视化控制** - 下拉菜单选择排序选项
5. **组合过滤** - 与现有过滤器协同工作

## 实现方案

### 1. 创建 Agent Sort Composable

**文件**: `src/composables/useAgentSort.ts`

#### 核心功能

```typescript
export type SortField = 'agentId' | 'role' | 'status' | 'lastActivity' | 'framework' | 'language'
export type SortOrder = 'asc' | 'desc'

export function useAgentSort() {
  const sortField = ref<SortField>('agentId')
  const sortOrder = ref<SortOrder>('asc')

  // Sort agents
  const sortAgents = (agents: AgentState[]): AgentState[] => {
    return [...agents].sort((a, b) => {
      let comparison = 0

      switch (sortField.value) {
        case 'agentId':
          comparison = a.agentId.localeCompare(b.agentId)
          break
        case 'lastActivity':
          comparison = new Date(a.lastActivity).getTime() - new Date(b.lastActivity).getTime()
          break
        // ... other fields
      }

      return sortOrder.value === 'asc' ? comparison : -comparison
    })
  }
}
```

### 2. 创建 SortSelector 组件

**文件**: `src/components/SortSelector.vue`

#### 组件结构

```
SortSelector
├── Sort Button
│   ├── Icon (🔀)
│   ├── Current Sort Label
│   └── Arrow
└── Dropdown Menu
    ├── Sort Fields Section
    │   ├── Agent ID
    │   ├── Role
    │   ├── Status
    │   ├── Last Activity
    │   ├── Framework
    │   └── Language
    ├── Sort Order Section
    │   ├── Ascending (↑)
    │   └── Descending (↓)
    └── Reset Button
```

### 3. 集成到主应用

**文件**: `src/App.vue`

#### 应用排序逻辑

```typescript
import { useAgentSort } from './composables/useAgentSort'
import SortSelector from './components/SortSelector.vue'

// Sort composable
const { sortAgents } = useAgentSort()

// Filtered and sorted agents
const filteredAgents = computed(() => {
  let result = agents.value

  // Apply tag filter first
  result = filterAgents(result)

  // Then apply quick filter
  if (quickFilter.value) {
    // ... filter logic
  }

  // Finally apply sort
  result = sortAgents(result)

  return result
})
```

#### 添加到模板

```vue
<div class="quick-filter-bar">
  <QuickFilterButtons
    :agents="agents"
    v-model="quickFilter"
    @filter-change="handleQuickFilterChange"
  />
  <SortSelector />
</div>
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (264 modules)

### 功能测试

1. **排序字段** ✅
   - Agent ID、角色、状态、最后活动、框架、语言
   - 默认按 Agent ID 升序

2. **排序顺序** ✅
   - 升序 (A-Z, 旧到新)
   - 降序 (Z-A, 新到旧)

3. **持久化** ✅
   - 刷新页面后保持排序偏好

4. **组合过滤** ✅
   - 与标签过滤协同
   - 与快速过滤协同

## 排序字段

| 字段 | 说明 | 升序 | 降序 |
|------|------|------|------|
| Agent ID | Agent 标识符 | A-Z | Z-A |
| 角色 | Agent 角色名称 | A-Z | Z-A |
| 状态 | Agent 状态 | online → offline | offline → online |
| 最后活动 | 最后活动时间 | 旧 → 新 | 新 → 旧 |
| 框架 | Agent 框架 | A-Z | Z-A |
| 语言 | 编程语言 | A-Z | Z-A |

## 使用示例

```typescript
import { useAgentSort } from './composables/useAgentSort'

const {
  sortField,
  sortOrder,
  sortOption,
  setSortField,
  setSortOrder,
  setSort,
  sortAgents
} = useAgentSort()

// Sort by last activity (newest first)
setSort('lastActivity', 'desc')

// Sort agents
const sorted = sortAgents(agents)

// Toggle sort order
toggleSortOrder()
```

## 技术要点

### 1. 排序算法

使用 JavaScript 原生 `sort()` 方法：

```typescript
const sortAgents = (agents: AgentState[]): AgentState[] => {
  return [...agents].sort((a, b) => {
    const comparison = // ... compare logic
    return sortOrder.value === 'asc' ? comparison : -comparison
  })
}
```

### 2. localStorage 持久化

```typescript
const saveSortPreferences = (): void => {
  const prefs = { field: sortField.value, order: sortOrder.value }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs))
}
```

### 3. Click Outside 处理

```typescript
const handleClickOutside = (event: MouseEvent): void => {
  const target = event.target as HTMLElement
  if (!target.closest('.sort-selector')) {
    closeDropdown()
  }
}
```

## 文件变更

### 新增文件

1. **src/composables/useAgentSort.ts** (~140 行)
   - 排序状态管理
   - localStorage 持久化
   - 多字段排序逻辑

2. **src/components/SortSelector.vue** (~210 行)
   - 排序选择器 UI
   - 下拉菜单
   - 字段和顺序选择

### 修改文件

1. **src/App.vue**
   - 导入 useAgentSort 和 SortSelector
   - 添加排序到 filteredAgents 逻辑
   - 添加 SortSelector 到模板

## 使用说明

### 更改排序字段

1. 点击快速过滤栏中的 🔀 排序按钮
2. 从下拉菜单选择排序字段
3. Agent 列表自动更新

### 更改排序顺序

1. 打开排序下拉菜单
2. 选择升序 (↑) 或降序 (↓)

### 重置排序

点击下拉菜单底部的 "🔄 重置排序" 按钮恢复默认排序。

## 总结

Agent Sort 功能成功实现了：

✅ **多字段排序** - 支持按 6 个字段排序
✅ **升序/降序** - 可切换排序顺序
✅ **持久化** - 保存用户的排序偏好
✅ **可视化控制** - 下拉菜单选择排序选项
✅ **组合过滤** - 与现有过滤器协同工作

该功能为用户提供了灵活的 Agent 列表排序方式，可以按需组织和查看 Agent。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

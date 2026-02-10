# Filter Persistency 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，用户经常设置特定的过滤器（快速过滤、标签过滤、排序等）来查看感兴趣的 Agent。每次刷新页面后，这些设置都会丢失，用户需要重新设置，这很不方便。

过滤器持久化功能可以：
- 自动保存过滤器设置到 localStorage
- 刷新页面后自动恢复过滤器状态
- 提供更流畅的用户体验
- 减少重复操作

## 需求分析

### 核心需求

1. **自动保存** - 过滤器改变时自动保存到 localStorage
2. **自动恢复** - 页面加载时自动恢复过滤器状态
3. **完整持久化** - 保存快速过滤、标签过滤、排序设置
4. **一键清除** - 提供清除持久化状态的功能
5. **命令面板集成** - 通过命令面板快速清除持久化状态

## 实现方案

### 1. 创建 Filter Persistency Composable

**文件**: `src/composables/useFilterPersistency.ts` (~175 lines)

#### 核心接口

```typescript
export interface FilterState {
  quickFilter?: string
  selectedTags: string[]
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}
```

#### 主要功能

```typescript
export function useFilterPersistency(
  quickFilter: Ref<string | undefined>,
  selectedTags: Ref<string[]>,
  sortField: Ref<string | undefined>,
  sortOrder: Ref<'asc' | 'desc'>,
  options: {
    enabled?: boolean
    onSave?: (state: FilterState) => void
    onRestore?: (state: FilterState) => void
  } = {}
)
```

#### 自动保存机制

```typescript
// Auto-save on changes
if (enabled) {
  watch(
    [quickFilter, selectedTags, sortField, sortOrder],
    () => {
      saveState()
    },
    { deep: true }
  )
}
```

#### 自动恢复机制

```typescript
// Restore state on mount
onMounted(() => {
  if (enabled) {
    restoreState()
  }
})
```

#### 辅助函数

```typescript
// Load filter state from localStorage
export const loadFilterState = (): FilterState => { ... }

// Save filter state to localStorage
export const saveFilterState = (state: FilterState): void => { ... }

// Clear filter state from localStorage
export const clearFilterState = (): void => { ... }
```

### 2. 集成到主应用

**文件**: `src/App.vue`

#### 导入 composable

```typescript
import { useFilterPersistency } from './composables/useFilterPersistency'
```

#### 更新 useAgentSort 解构

```typescript
const {
  sortField,
  sortOrder,
  sortAgents
} = useAgentSort()
```

#### 初始化 filter persistency

```typescript
// Filter persistency - automatically saves and restores filter state
const {
  clearPersistedState,
  restoreState,
  saveState
} = useFilterPersistency(
  quickFilter,
  selectedTags,
  sortField,
  sortOrder,
  {
    enabled: true,
    onRestore: (state) => {
      console.log('Filter state restored:', state)
    },
    onSave: (state) => {
      console.log('Filter state saved:', state)
    }
  }
)
```

#### 更新 handleClearAllFilters

```typescript
const handleClearAllFilters = (): void => {
  // Clear quick filter
  clearQuickFilter()
  // Clear tag filter
  if (selectedTags.value) {
    selectedTags.value = []
  }
  // Clear persisted state
  clearPersistedState()
  info('已清除所有过滤器和持久化状态')
}
```

### 3. 添加命令面板命令

**文件**: `src/App.vue`

```typescript
{
  id: 'clear-all-filters',
  label: '清除所有过滤器',
  description: '清除所有过滤器和持久化状态',
  icon: '🗑️',
  category: '数据操作',
  keywords: ['clear', 'reset', 'filter', 'clean'],
  action: () => handleClearAllFilters()
}
```

## 功能特性

### 持久化的过滤器

| 过滤器 | 持久化键 | 恢复逻辑 |
|--------|----------|----------|
| **快速过滤** | `quickFilter` | 恢复到 activeFilter |
| **标签过滤** | `selectedTags` | 恢复到 selectedTags 数组 |
| **排序字段** | `sortField` | 恢复到 sortField |
| **排序顺序** | `sortOrder` | 恢复到 sortOrder (asc/desc) |

### 存储结构

```json
{
  "quickFilter": "online",
  "selectedTags": ["crewai", "python"],
  "sortField": "lastActivity",
  "sortOrder": "desc"
}
```

### 使用场景

1. **刷新页面** - 过滤器设置自动恢复
2. **关闭浏览器** - 下次打开时恢复设置
3. **清除过滤器** - 通过 🗑️ 按钮或命令面板清除
4. **重新开始** - 清除持久化状态回到默认视图

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (266 modules)

### 功能测试

1. **自动保存** ✅
   - 设置快速过滤器
   - 检查 localStorage 中有保存的数据

2. **自动恢复** ✅
   - 刷新页面
   - 过滤器设置自动恢复

3. **组合过滤器持久化** ✅
   - 设置快速过滤 + 标签过滤 + 排序
   - 刷新页面
   - 所有设置正确恢复

4. **清除持久化** ✅
   - 点击 🗑️ 按钮
   - localStorage 被清除
   - 所有过滤器重置

5. **命令面板** ✅
   - 按 Ctrl+Shift+P 打开命令面板
   - 搜索 "clear all"
   - 执行命令清除所有过滤器

## 使用说明

### 自动持久化

过滤器设置会自动保存，无需手动操作：

1. 点击快速过滤按钮 → 自动保存
2. 选择标签 → 自动保存
3. 更改排序 → 自动保存

### 清除持久化

有两种方式清除持久化状态：

1. **使用快速过滤栏的 🗑️ 按钮**
   - 点击快速过滤栏右侧的 🗑️ 按钮
   - 清除所有过滤器和持久化状态

2. **使用命令面板**
   - 按 `Ctrl+Shift+P` 打开命令面板
   - 搜索 "清除所有过滤器" 或 "clear all"
   - 执行命令

### localStorage 检查

在浏览器开发者工具中：

```
localStorage.getItem('agent-dashboard-filter-state')
```

## 文件变更

### 新增文件

1. **src/composables/useFilterPersistency.ts** (~175 lines)
   - FilterState 接口定义
   - loadFilterState 函数
   - saveFilterState 函数
   - clearFilterState 函数
   - useFilterPersistency composable

### 修改文件

1. **src/App.vue**
   - 导入 useFilterPersistency
   - 更新 useAgentSort 解构以包含 sortField 和 sortOrder
   - 初始化 filter persistency
   - 更新 handleClearAllFilters 以清除持久化状态
   - 添加命令面板命令 "清除所有过滤器"

## 总结

Filter Persistency 功能成功实现了：

✅ **自动保存** - 过滤器改变时自动保存到 localStorage
✅ **自动恢复** - 页面加载时自动恢复过滤器状态
✅ **完整持久化** - 保存快速过滤、标签过滤、排序设置
✅ **一键清除** - 提供清除持久化状态的功能
✅ **命令面板集成** - 通过命令面板快速清除持久化状态

该功能显著提升了用户体验，用户不再需要在每次刷新页面后重新设置过滤器。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

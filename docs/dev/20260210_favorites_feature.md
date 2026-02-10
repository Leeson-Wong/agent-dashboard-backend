# Favorites 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，用户可能需要标记某些 Agent 为收藏/常用，以便：
- 快速访问重要的 Agent
- 过滤显示收藏的 Agent
- 个性化 Agent 列表

## 需求分析

### 核心需求

1. **收藏/取消收藏** - 切换 Agent 的收藏状态
2. **持久化存储** - 使用 localStorage 保存收藏状态
3. **收藏过滤** - 快速过滤只显示收藏的 Agent
4. **状态同步** - 收藏状态在多个组件间同步
5. **导入导出** - 支持收藏列表的导入导出

## 实现方案

### 1. 创建 Favorites Composable

**文件**: `src/composables/useFavoriteAgents.ts`

#### 核心功能

```typescript
export function useFavoriteAgents() {
  // Global favorites store (Set of agent IDs)
  const favoritesStore = ref<Set<string>>(new Set())

  // Auto-save to localStorage on changes
  watch(favoritesStore, () => {
    saveFavorites()
  }, { deep: true })

  // Methods
  const isFavorite = (agentId: string): boolean => {
    return favoritesStore.value.has(agentId)
  }

  const toggleFavorite = (agentId: string): void => {
    if (favoritesStore.value.has(agentId)) {
      favoritesStore.value.delete(agentId)
    } else {
      favoritesStore.value.add(agentId)
    }
  }

  const getFavoriteIds = (): string[] => {
    return Array.from(favoritesStore.value)
  }

  const exportFavorites = (): string => {
    const favorites = getFavoriteIds()
    return JSON.stringify(favorites, null, 2)
  }

  const importFavorites = (json: string): boolean => {
    // Import favorites from JSON
  }
}
```

### 2. 集成到 AgentDetailPanel

**文件**: `src/components/AgentDetailPanel.vue`

#### 同步收藏状态

```typescript
import { useFavoriteAgents } from '../composables/useFavoriteAgents'

const { toggleFavorite: toggleFavoriteLocalStorage, isFavorite: isLocalStorageFavorite } = useFavoriteAgents()

// Toggle favorite from keyboard (F key)
const toggleFavoriteShortcut = (): void => {
  if (props.agent) {
    const newStatus = !props.agent.isFavorite
    props.agent.isFavorite = newStatus

    // Also sync with localStorage
    toggleFavoriteLocalStorage(props.agent.agentId)

    success(newStatus ? '已添加到收藏' : '已取消收藏')
  }
}

// Sync favorite status from localStorage when agent changes
watch(() => props.agent?.agentId, (agentId) => {
  if (agentId && props.agent) {
    // Update isFavorite based on localStorage
    props.agent.isFavorite = isLocalStorageFavorite(agentId)
  }
}, { immediate: true })
```

### 3. 添加收藏过滤器

**文件**: `src/components/QuickFilterButtons.vue`

#### 添加收藏过滤按钮

```vue
<script setup lang="ts">
import { useFavoriteAgents } from '../composables/useFavoriteAgents'

const { getFavoriteIds } = useFavoriteAgents()

const filters = computed<QuickFilter[]>(() => {
  const agents = props.agents
  const favoriteIds = getFavoriteIds()

  return [
    // ... other filters
    {
      id: 'favorites',
      label: '收藏',
      icon: '⭐',
      description: '只显示收藏的 Agent',
      count: agents.filter(a => favoriteIds.includes(a.agentId) || a.isFavorite).length,
      predicate: (agent) => favoriteIds.includes(agent.agentId) || agent.isFavorite
    },
    // ... other filters
  ]
})
</script>
```

### 4. 更新 QuickFilter Composable

**文件**: `src/composables/useQuickFilter.ts`

#### 添加收藏过滤器类型

```typescript
export type QuickFilterType = 'all' | 'favorites' | 'online' | 'offline' | 'thinking' | 'errors' | 'paused'

const BUILTIN_FILTERS: QuickFilterDefinition[] = [
  {
    id: 'all',
    label: '全部',
    icon: '📋',
    description: '显示所有 Agent',
    predicate: () => true
  },
  {
    id: 'favorites',
    label: '收藏',
    icon: '⭐',
    description: '只显示收藏的 Agent',
    predicate: (agent) => agent.isFavorite === true
  },
  // ... other filters
]
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (260 modules)

### 功能测试

1. **收藏切换** ✅
   - 按 F 键切换收藏状态
   - ⭐ 按钮显示收藏状态

2. **localStorage 持久化** ✅
   - 刷新页面后收藏状态保持
   - 切换 Agent 后正确加载收藏状态

3. **收藏过滤** ✅
   - 点击 ⭐ 收藏按钮显示收藏的 Agent
   - 计数正确显示收藏数量

4. **状态同步** ✅
   - AgentDetailPanel 和 QuickFilterButtons 同步
   - agent.isFavorite 和 localStorage 同步

## 使用示例

### 基础使用

```typescript
import { useFavoriteAgents } from './composables/useFavoriteAgents'

const {
  isFavorite,
  toggleFavorite,
  addFavorite,
  removeFavorite,
  getFavoriteIds,
  exportFavorites,
  importFavorites
} = useFavoriteAgents()

// Check if agent is favorited
if (isFavorite('agent-123')) {
  console.log('Agent is favorited')
}

// Toggle favorite status
toggleFavorite('agent-123')

// Get all favorite IDs
const favorites = getFavoriteIds()

// Export favorites
const json = exportFavorites()

// Import favorites
importFavorites(json)
```

## 技术要点

### 1. 双向同步

- `agent.isFavorite` (后端/内存状态)
- localStorage (前端持久化)
- 两者保持同步

### 2. 全局状态管理

使用全局 `ref` 和 `Set` 管理收藏：

```typescript
const favoritesStore = ref<Set<string>>(new Set())
```

### 3. 自动保存

使用 `watch` 监听变化并自动保存：

```typescript
watch(favoritesStore, () => {
  saveFavorites()
}, { deep: true })
```

## 文件变更

### 新增文件

1. **src/composables/useFavoriteAgents.ts** (~100 行)
   - 收藏状态管理
   - localStorage 持久化
   - 导入导出功能

### 修改文件

1. **src/components/AgentDetailPanel.vue**
   - 导入 useFavoriteAgents
   - 同步收藏状态到 localStorage
   - 添加 watch 同步从 localStorage 加载状态

2. **src/components/QuickFilterButtons.vue**
   - 导入 useFavoriteAgents
   - 添加收藏过滤器

3. **src/composables/useQuickFilter.ts**
   - 添加 'favorites' 到 QuickFilterType
   - 添加收藏过滤器定义

## 使用说明

### 收藏 Agent

1. 打开 Agent 详情面板
2. 按 F 键或点击 ⭐ 按钮收藏/取消收藏
3. 收藏状态自动保存

### 过滤收藏的 Agent

在快速过滤栏点击 ⭐ 收藏按钮，只显示收藏的 Agent。

## 总结

Favorites 功能成功实现了：

✅ **收藏/取消收藏** - 切换 Agent 的收藏状态
✅ **持久化存储** - 使用 localStorage 保存收藏状态
✅ **收藏过滤** - 快速过滤只显示收藏的 Agent
✅ **状态同步** - 收藏状态在多个组件间同步
✅ **导入导出** - 支持收藏列表的导入导出
✅ **双向同步** - agent.isFavorite 和 localStorage 同步

该功能为用户提供了便捷的收藏管理方式，可以快速访问和过滤重要的 Agent。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

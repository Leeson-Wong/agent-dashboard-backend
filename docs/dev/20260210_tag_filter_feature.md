# 标签过滤功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

Agent 系统已经支持在 AgentDetailPanel 中为每个 Agent 添加标签，但缺少基于标签的快速过滤功能。用户需要能够通过标签快速筛选 Agent，以便更好地组织和管理大量 Agent。

## 需求分析

### 核心需求

1. **标签收集** - 自动扫描所有 Agent 的标签并统计使用次数
2. **快速过滤** - 点击标签即可快速筛选 Agent
3. **视觉反馈** - 显示选中状态和过滤计数
4. **多标签过滤** - 支持同时选择多个标签（OR 逻辑）
5. **紧凑界面** - 可折叠的紧凑显示模式

### 技术要求

- 与现有的 AgentDetailPanel 标签功能兼容
- 实时更新标签列表
- 不影响现有功能
- 响应式布局

## 实现方案

### 1. 创建标签过滤 Composable

**文件**: `src/composables/useTagFilter.ts`

#### 核心数据结构

```typescript
export interface TagWithCount {
  tag: string
  count: number
  color: string
}

export function useTagFilter() {
  const selectedTags = ref<Set<string>>(new Set())
  const allTags = ref<TagWithCount[]>([])
  // ...
}
```

#### 主要功能

**1. 更新标签列表**

```typescript
const updateTags = (agents: Array<{ tags?: string }>): void => {
  const tagCounts: Record<string, number> = {}

  agents.forEach(agent => {
    if (!agent.tags) return

    try {
      const tags = typeof agent.tags === 'string' ? JSON.parse(agent.tags) : agent.tags
      if (Array.isArray(tags)) {
        tags.forEach(tag => {
          tagCounts[tag] = (tagCounts[tag] || 0) + 1
        })
      }
    } catch {
      // Invalid JSON, skip
    }
  })

  allTags.value = Object.entries(tagCounts)
    .map(([tag, count]) => ({
      tag,
      count,
      color: getTagColor(tag)
    }))
    .sort((a, b) => b.count - a.count)
}
```

**2. 过滤 Agent**

```typescript
const filterAgents = <T extends { tags?: string }>(agents: T[]): T[] => {
  if (selectedTags.value.size === 0) return agents

  return agents.filter(agent => {
    if (!agent.tags) return false

    try {
      const tags = typeof agent.tags === 'string' ? JSON.parse(agent.tags) : agent.tags
      if (!Array.isArray(tags)) return false

      // Check if agent has any of the selected tags
      return tags.some(tag => selectedTags.value.has(tag))
    } catch {
      return false
    }
  })
}
```

**3. 标签颜色生成**

```typescript
const getTagColor = (tag: string): string => {
  const colors = ['blue', 'green', 'purple', 'orange', 'pink', 'cyan', 'red', 'yellow']
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}
```

### 2. 创建标签过滤组件

**文件**: `src/components/TagFilterPanel.vue`

#### 组件结构

```
TagFilterPanel (Fixed position: bottom-right)
├── Toggle Button (显示/隐藏)
│   ├── Icon (🏷️)
│   └── Selected Count
└── Content (展开状态)
    ├── Header
    │   ├── Title
    │   └── Clear Button
    ├── Tags List
    │   └── Tag Items
    │       ├── Tag Name
    │       ├── Count
    │       └── Selection State
    └── Selected Summary (可选)
```

#### 组件实现

```vue
<template>
  <div class="tag-filter-panel" :class="{ 'is-collapsed': isCollapsed }">
    <button class="filter-toggle" @click="toggleCollapsed">
      <span class="toggle-icon">🏷️</span>
      <span v-if="isCollapsed" class="toggle-count">{{ selectedCount }}</span>
    </button>

    <Transition name="expand">
      <div v-if="!isCollapsed" class="filter-container">
        <div class="filter-header">
          <span class="filter-title">标签过滤</span>
          <button v-if="isFilterActive" class="clear-btn" @click="handleClearFilters">
            清除 ({{ selectedCount }})
          </button>
        </div>

        <div v-if="allTags.length > 0" class="tags-list">
          <div
            v-for="item in allTags"
            :key="item.tag"
            :class="['tag-item', `color-${item.color}`, { 'is-selected': isTagSelected(item.tag) }]"
            @click="handleTagClick(item.tag)"
          >
            <span class="tag-name">{{ item.tag }}</span>
            <span class="tag-count">{{ item.count }}</span>
          </div>
        </div>

        <div v-else class="empty-state">
          <span class="empty-icon">🏷️</span>
          <span class="empty-text">暂无标签</span>
          <span class="empty-hint">给 Agent 添加标签后即可在此过滤</span>
        </div>
      </div>
    </Transition>
  </div>
</template>
```

#### 组件 Props 和 Events

```typescript
interface Props {
  agents?: Array<{ tags?: string }>
  selectedTags?: Set<string>
  allTags?: TagWithCount[]
  selectedCount?: number
}

const emit = defineEmits<{
  toggleTag: [tag: string]
  clearFilters: []
}>()
```

### 3. 集成到主应用

**文件**: `src/App.vue`

#### 添加状态管理

```typescript
// Tag filter
const {
  selectedTags,
  allTags,
  selectedCount,
  updateTags,
  toggleTag,
  isTagSelected,
  clearFilters,
  filterAgents
} = useTagFilter()

// Filtered agents (for AgentListPanel)
const filteredAgents = computed(() => {
  return filterAgents(agents.value)
})
```

#### 更新模板

```vue
<AgentListPanel :agents="filteredAgents" />

<TagFilterPanel
  :agents="agents"
  :selected-tags="selectedTags"
  :all-tags="allTags"
  :selected-count="selectedCount"
  @toggle-tag="toggleTag"
  @clear-filters="clearFilters"
/>
```

#### 监听 Agents 变化

```typescript
watch(agents, (newAgents) => {
  if (performanceMonitor.value) {
    performanceMonitor.value.setAgentCount(newAgents.length)
  }
  // Update tag filter
  updateTags(newAgents)
}, { immediate: true })
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **标签收集** ✅
   - 正确解析 Agent 标签（JSON 格式）
   - 统计每个标签的使用次数
   - 按使用次数排序显示

2. **标签过滤** ✅
   - 点击标签切换选中状态
   - Agent 列表实时更新
   - 多标签过滤（OR 逻辑）

3. **清除过滤** ✅
   - 清除按钮显示选中数量
   - 点击清除恢复所有 Agent

4. **空状态** ✅
   - 无标签时显示友好提示

5. **紧凑模式** ✅
   - 收起时显示小按钮和数量
   - 展开时显示完整标签列表

## 样式实现

### 标签颜色

```css
.tag-item.is-selected {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.4);
}

/* Different colors for different tags */
.tag-item.color-blue.is-selected { /* ... */ }
.tag-item.color-green.is-selected { /* ... */ }
.tag-item.color-purple.is-selected { /* ... */ }
/* etc. */
```

### 定位

```css
.tag-filter-panel {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 850;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .tag-filter-panel {
    bottom: 140px;
    right: 10px;
    left: 10px;
  }
}
```

### 标签列表

```css
.tags-list {
  padding: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 300px;
  overflow-y: auto;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(100, 116, 139, 0.2);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-item:hover {
  background: rgba(51, 65, 85, 0.5);
  border-color: rgba(100, 116, 139, 0.4);
}
```

## 技术要点

### 1. JSON 标签解析

Agent 的 `tags` 字段存储为 JSON 字符串，需要解析：

```typescript
const tags = typeof agent.tags === 'string' ? JSON.parse(agent.tags) : agent.tags
```

### 2. 过滤逻辑

使用 OR 逻辑 - Agent 只要包含任一选中的标签就会显示：

```typescript
return tags.some(tag => selectedTags.value.has(tag))
```

### 3. 颜色一致性

使用哈希算法确保同一标签始终获得相同颜色：

```typescript
let hash = 0
for (let i = 0; i < tag.length; i++) {
  hash = tag.charCodeAt(i) + ((hash << 5) - hash)
}
return colors[Math.abs(hash) % colors.length]
```

### 4. 状态管理

状态在 App.vue 的 useTagFilter 中管理，通过 props 和 events 与组件通信：

```typescript
// App.vue
const { selectedTags, allTags, toggleTag, clearFilters } = useTagFilter()

// TagFilterPanel
props: ['selectedTags', 'allTags']
emits: ['toggleTag', 'clearFilters']
```

## 文件变更

### 新增文件

1. **src/composables/useTagFilter.ts** (~120 行)
   - 标签过滤状态管理
   - 标签收集和统计
   - Agent 过滤逻辑
   - 颜色生成

2. **src/components/TagFilterPanel.vue** (~380 行)
   - 标签过滤 UI 组件
   - 标签列表显示
   - 选中状态管理
   - 清除功能

### 修改文件

1. **src/App.vue**
   - 导入 TagFilterPanel 组件和 useTagFilter composable
   - 添加标签过滤状态管理
   - 创建 filteredAgents 计算属性
   - 监听 agents 变化并更新标签
   - 传递过滤后的 agents 到 AgentListPanel

## 使用说明

### 基本使用

标签过滤功能会自动：

1. 扫描所有 Agent 的标签
2. 统计每个标签的使用次数
3. 按使用次数排序显示

### 过滤操作

1. **点击标签** - 切换选中状态，Agent 列表实时过滤
2. **清除过滤** - 点击"清除"按钮恢复所有 Agent
3. **多标签过滤** - 可同时选择多个标签（OR 逻辑）

### 快捷键

无（暂无快捷键支持）

## 与现有功能的集成

### AgentDetailPanel 标签功能

标签过滤功能与 AgentDetailPanel 中的标签编辑功能完全兼容：

1. 在 AgentDetailPanel 中添加/删除标签
2. 标签过滤面板自动更新
3. 过滤结果实时反映

### Agent 列表

过滤后的 Agent 通过 `filteredAgents` 传递给 AgentListPanel：

```vue
<AgentListPanel :agents="filteredAgents" />
```

## 已知限制

1. **JSON 格式依赖** - 标签必须是有效的 JSON 数组格式
2. **OR 逻辑** - 当前只支持 OR 逻辑，不支持 AND 逻辑
3. **颜色数量** - 只有 8 种预定义颜色，可能重复
4. **无快捷键** - 暂不支持键盘快捷键操作

## 未来改进

1. **AND 逻辑** - 支持同时满足所有标签的过滤
2. **标签分组** - 支持标签分类管理
3. **快捷键** - 添加键盘快捷键支持
4. **标签历史** - 记录常用过滤组合
5. **标签搜索** - 在标签列表中搜索特定标签

## 总结

标签过滤功能成功实现了：

✅ **自动收集** - 扫描所有 Agent 标签并统计
✅ **快速过滤** - 点击标签即可筛选 Agent
✅ **视觉反馈** - 选中状态和过滤计数
✅ **多标签** - 支持同时选择多个标签
✅ **紧凑界面** - 可折叠的紧凑显示
✅ **实时更新** - 标签变化时自动更新列表
✅ **友好提示** - 空状态提示用户添加标签

该功能极大地增强了 Agent 管理能力，让用户能够快速定位和筛选特定类型的 Agent。

# 快速过滤按钮功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，用户经常需要快速过滤 Agent 列表以查看特定状态的 Agent。快速过滤按钮提供了：

1. **一键过滤** - 单击按钮即可快速过滤
2. **计数显示** - 显示每个过滤器的匹配数量
3. **视觉反馈** - 清晰的激活状态指示
4. **组合过滤** - 与标签过滤协同工作

## 需求分析

### 核心需求

1. **预定义过滤器** - 常用过滤条件（全部、在线、离线、运行中、有错误、已暂停）
2. **计数显示** - 显示每个过滤器匹配的 Agent 数量
3. **切换功能** - 点击按钮切换过滤状态，再次点击清除
4. **清除按钮** - 快速清除所有活动过滤
5. **组合过滤** - 与现有标签过滤系统协同工作

### 技术要求

- 使用 Composable 模式管理过滤状态
- 与标签过滤系统协同工作
- 响应式设计适配移动设备
- v-model 支持双向绑定

## 实现方案

### 1. 创建快速过滤 Composable

**文件**: `src/composables/useQuickFilter.ts`

#### 核心数据结构

```typescript
export type QuickFilterType = 'all' | 'online' | 'offline' | 'thinking' | 'errors' | 'paused'

export interface QuickFilterDefinition {
  id: QuickFilterType
  label: string
  icon: string
  description: string
  predicate: (agent: AgentState) => boolean
}
```

#### 内置过滤器定义

```typescript
const BUILTIN_FILTERS: QuickFilterDefinition[] = [
  {
    id: 'all',
    label: '全部',
    icon: '📋',
    description: '显示所有 Agent',
    predicate: () => true
  },
  {
    id: 'online',
    label: '在线',
    icon: '🟢',
    description: '只显示在线 Agent',
    predicate: (agent) => agent.status === 'online' || agent.status === 'ready'
  },
  {
    id: 'offline',
    label: '离线',
    icon: '⚫',
    description: '只显示离线 Agent',
    predicate: (agent) => agent.status === 'offline' || agent.status === 'stopped'
  },
  {
    id: 'thinking',
    label: '运行中',
    icon: '🔄',
    description: '只显示运行中的 Agent',
    predicate: (agent) => agent.status === 'thinking' || agent.status === 'busy'
  },
  {
    id: 'errors',
    label: '有错误',
    icon: '❌',
    description: '只显示有错误的 Agent',
    predicate: (agent) => agent.status === 'error'
  },
  {
    id: 'paused',
    label: '已暂停',
    icon: '⏸️',
    description: '只显示已暂停的 Agent',
    predicate: (agent) => agent.status === 'paused'
  }
]
```

#### 主要功能

**1. 过滤状态管理**：

```typescript
const activeFilter = ref<QuickFilterType | undefined>(undefined)
const customFilter = ref<((agent: AgentState) => boolean) | undefined>(undefined)

const hasActiveFilter = computed(() => {
  return activeFilter.value !== undefined || customFilter.value !== undefined
})
```

**2. 过滤逻辑**：

```typescript
const filterPredicate = computed(() => {
  if (customFilter.value) return customFilter.value
  if (activeFilter.value) {
    const filter = BUILTIN_FILTERS.find(f => f.id === activeFilter.value)
    return filter?.predicate
  }
  return undefined
})

const filteredAgents = computed(() => {
  if (!filterPredicate.value) return agents.value
  return agents.value.filter(filterPredicate.value)
})
```

**3. 过滤计数**：

```typescript
const filterCounts = computed(() => {
  return BUILTIN_FILTERS.reduce((counts, filter) => {
    counts[filter.id] = agents.value.filter(filter.predicate).length
    return counts
  }, {} as Record<QuickFilterType, number>)
})
```

**4. 过滤操作**：

```typescript
const setFilter = (filterId: QuickFilterType): void => {
  // If clicking the same filter, clear it
  if (activeFilter.value === filterId) {
    clearFilter()
    return
  }
  activeFilter.value = filterId
  customFilter.value = undefined
}

const setCustomFilter = (predicate: (agent: AgentState) => boolean): void => {
  customFilter.value = predicate
  activeFilter.value = undefined
}

const clearFilter = (): void => {
  activeFilter.value = undefined
  customFilter.value = undefined
}

const toggleFilter = (filterId: QuickFilterType): void => {
  if (activeFilter.value === filterId) {
    clearFilter()
  } else {
    setFilter(filterId)
  }
}
```

### 2. 创建快速过滤按钮组件

**文件**: `src/components/QuickFilterButtons.vue`

#### 组件结构

```
QuickFilterButtons
├── Filter Buttons (动态生成)
│   ├── Icon
│   ├── Label
│   └── Count Badge
└── Clear Button (有活动过滤时显示)
```

#### 核心实现

**过滤按钮**：

```vue
<button
  v-for="filter in filters"
  :key="filter.id"
  class="filter-btn"
  :class="{ active: activeFilter === filter.id }"
  @click="selectFilter(filter.id)"
  :title="filter.description"
>
  <span class="filter-icon">{{ filter.icon }}</span>
  <span class="filter-label">{{ filter.label }}</span>
  <span v-if="filter.count !== undefined" class="filter-count">
    {{ filter.count }}
  </span>
</button>
```

**清除按钮**：

```vue
<Transition name="fade">
  <button
    v-if="hasActiveFilter"
    class="clear-btn"
    @click="clearFilter"
    title="清除过滤器"
  >
    ✕
  </button>
</Transition>
```

**过滤器定义（组件内）**：

```typescript
const filters = computed<QuickFilter[]>(() => {
  const agents = props.agents

  return [
    {
      id: 'all',
      label: '全部',
      icon: '📋',
      description: '显示所有 Agent',
      count: agents.length,
      predicate: () => true
    },
    {
      id: 'online',
      label: '在线',
      icon: '🟢',
      description: '只显示在线 Agent',
      count: agents.filter(a => a.status === 'online' || a.status === 'ready').length,
      predicate: (agent) => agent.status === 'online' || agent.status === 'ready'
    },
    // ... other filters
  ]
})
```

**v-model 支持**：

```typescript
const activeFilter = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
  }
})
```

### 3. 集成到主应用

**文件**: `src/App.vue`

**导入 Composable**：

```typescript
import { useQuickFilter, type QuickFilterType } from './composables/useQuickFilter'

// Quick filter
const {
  activeFilter: quickFilter,
  hasActiveFilter: hasQuickFilter,
  filteredAgents: quickFilteredAgents,
  setFilter: setQuickFilter,
  clearFilter: clearQuickFilter,
  toggleFilter: toggleQuickFilter,
  getAllFilters
} = useQuickFilter(ref(agents))
```

**组合过滤逻辑**：

```typescript
// Filtered agents (for AgentListPanel) - combines tag filter and quick filter
const filteredAgents = computed(() => {
  let result = agents.value

  // Apply tag filter first
  result = filterAgents(result)

  // Then apply quick filter
  if (quickFilter.value) {
    const allFilters = getAllFilters()
    const filter = allFilters.find(f => f.id === quickFilter.value)
    if (filter) {
      result = result.filter(filter.predicate)
    }
  }

  return result
})
```

**添加到模板**：

```vue
<div class="quick-filter-bar">
  <QuickFilterButtons
    :agents="agents"
    v-model="quickFilter"
    @filter-change="handleQuickFilterChange"
  />
</div>
```

### 4. 样式实现

**过滤按钮样式**：

```css
.filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: rgba(30, 41, 59, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.filter-btn:hover {
  background: rgba(51, 65, 85, 0.8);
  border-color: rgba(100, 116, 139, 0.5);
  color: #e2e8f0;
}

.filter-btn.active {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.5);
  color: #3b82f6;
}
```

**计数徽章样式**：

```css
.filter-count {
  font-size: 10px;
  background: rgba(100, 116, 139, 0.2);
  padding: 2px 5px;
  border-radius: 8px;
  min-width: 18px;
  text-align: center;
}

.filter-btn.active .filter-count {
  background: rgba(59, 130, 246, 0.2);
}
```

**清除按钮样式**：

```css
.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 6px;
  color: #f87171;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: rgba(239, 68, 68, 0.3);
  border-color: rgba(239, 68, 68, 0.5);
  color: #fca5a5;
}
```

**快速过滤栏样式**：

```css
.quick-filter-bar {
  pointer-events: auto;
  padding: 8px 16px;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(100, 116, 139, 0.2);
}
```

**响应式设计**：

```css
@media (max-width: 768px) {
  .quick-filter-buttons {
    gap: 4px;
  }

  .filter-btn {
    padding: 5px 8px;
    font-size: 11px;
  }

  .filter-icon {
    font-size: 12px;
  }

  .filter-label {
    display: none; /* Hide label on mobile, show only icon and count */
  }

  .filter-count {
    font-size: 9px;
    padding: 1px 4px;
    min-width: 16px;
  }
}
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (255 modules)

```
✓ 255 modules transformed.
✓ built in 4.29s
```

### 功能测试

1. **过滤按钮** ✅
   - 点击按钮应用对应过滤器
   - 激活状态正确显示（蓝色高亮）
   - 再次点击清除过滤器

2. **计数显示** ✅
   - 每个按钮显示匹配的 Agent 数量
   - 计数正确更新
   - 激活时计数徽章高亮

3. **过滤器功能** ✅
   - "全部" - 显示所有 Agent
   - "在线" - 只显示在线 Agent
   - "离线" - 只显示离线 Agent
   - "运行中" - 只显示运行中 Agent
   - "有错误" - 只显示错误状态 Agent
   - "已暂停" - 只显示已暂停 Agent

4. **清除按钮** ✅
   - 有活动过滤时显示
   - 点击清除所有过滤
   - 淡入淡出动画流畅

5. **组合过滤** ✅
   - 与标签过滤协同工作
   - 两个过滤条件都生效
   - 清除任一过滤正确更新结果

6. **响应式设计** ✅
   - 移动设备上隐藏标签
   - 只显示图标和计数
   - 按钮大小自适应

## 过滤器列表

| 过滤器 | 图标 | 说明 | 状态检查 |
|--------|------|------|----------|
| 全部 | 📋 | 显示所有 Agent | 无条件 |
| 在线 | 🟢 | 在线或就绪 | `status === 'online' \|\| 'ready'` |
| 离线 | ⚫ | 离线或已停止 | `status === 'offline' \|\| 'stopped'` |
| 运行中 | 🔄 | 思考中或繁忙 | `status === 'thinking' \|\| 'busy'` |
| 有错误 | ❌ | 错误状态 | `status === 'error'` |
| 已暂停 | ⏸️ | 已暂停 | `status === 'paused'` |

## 使用示例

### 基础使用

```vue
<script setup lang="ts">
import QuickFilterButtons from './components/QuickFilterButtons.vue'
import { ref } from 'vue'

const agents = ref<AgentState[]>([...])
const quickFilter = ref<string>()

const handleFilterChange = (filter: any) => {
  console.log('Filter changed:', filter)
}
</script>

<template>
  <QuickFilterButtons
    :agents="agents"
    v-model="quickFilter"
    @filter-change="handleFilterChange"
  />
</template>
```

### 使用 Composable

```typescript
import { useQuickFilter } from './composables/useQuickFilter'

const {
  activeFilter,
  hasActiveFilter,
  filteredAgents,
  setFilter,
  clearFilter,
  toggleFilter
} = useQuickFilter(ref(agents))

// Set a filter
setFilter('online')

// Toggle a filter
toggleFilter('errors')

// Clear filter
clearFilter()

// Get filtered agents
const onlineAgents = filteredAgents.value
```

### 自定义过滤器

```typescript
const {
  setCustomFilter
} = useQuickFilter(ref(agents))

// Set a custom filter
setCustomFilter((agent) => {
  return agent.framework === 'CrewAI' && agent.status === 'online'
})
```

## 技术要点

### 1. Composable 模式

```typescript
export function useQuickFilter(agents: Ref<AgentState[]>) {
  const activeFilter = ref<QuickFilterType | undefined>(undefined)

  const filteredAgents = computed(() => {
    if (!filterPredicate.value) return agents.value
    return agents.value.filter(filterPredicate.value)
  })

  return {
    activeFilter,
    filteredAgents,
    // ... other exports
  }
}
```

### 2. v-model 双向绑定

```typescript
const activeFilter = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
  }
})
```

### 3. 组合过滤逻辑

```typescript
const filteredAgents = computed(() => {
  let result = agents.value

  // Apply tag filter first
  result = filterAgents(result)

  // Then apply quick filter
  if (quickFilter.value) {
    const filter = allFilters.find(f => f.id === quickFilter.value)
    if (filter) {
      result = result.filter(filter.predicate)
    }
  }

  return result
})
```

### 4. 动态计数

```typescript
const filters = computed<QuickFilter[]>(() => {
  const agents = props.agents

  return [
    {
      id: 'online',
      label: '在线',
      count: agents.filter(a => a.status === 'online' || a.status === 'ready').length,
      predicate: (agent) => agent.status === 'online' || agent.status === 'ready'
    },
    // ...
  ]
})
```

## 已知限制

1. **内置过滤器** - 当前只支持预定义的 6 种过滤器
2. **单选过滤** - 同时只能使用一个快速过滤器
3. **状态检查** - 依赖 Agent.status，无法检查其他属性
4. **过滤器顺序** - 过滤器顺序固定，无法自定义

## 未来改进

1. **自定义过滤器** - 允许用户创建和保存自定义过滤器
2. **多选过滤** - 支持同时选择多个快速过滤器
3. **过滤器组合** - 支持复杂的 AND/OR 组合逻辑
4. **过滤器历史** - 记录最近使用的过滤器
5. **过滤器预设** - 保存常用的过滤器组合
6. **高级过滤** - 支持更多过滤条件（框架、语言等）
7. **过滤器编辑** - 允许用户编辑内置过滤器
8. **过滤器分享** - 支持导出和导入过滤器配置

## 文件变更

### 新增文件

1. **src/composables/useQuickFilter.ts** (~150 行)
   - 快速过滤状态管理
   - 过滤器定义
   - 过滤逻辑实现

2. **src/components/QuickFilterButtons.vue** (~230 行)
   - 过滤按钮 UI
   - 计数徽章
   - 清除按钮

### 修改文件

1. **src/App.vue**
   - 导入 QuickFilterButtons 组件
   - 导入 useQuickFilter composable
   - 添加快速过滤栏到模板
   - 更新 filteredAgents 逻辑以组合标签过滤和快速过滤
   - 添加快速过滤栏样式

## 使用说明

### 快速过滤

点击快速过滤栏中的按钮：
- 📋 全部 - 显示所有 Agent
- 🟢 在线 - 只显示在线 Agent
- ⚫ 离线 - 只显示离线 Agent
- 🔄 运行中 - 只显示运行中 Agent
- ❌ 有错误 - 只显示有错误 Agent
- ⏸️ 已暂停 - 只显示已暂停 Agent

### 清除过滤

点击红色 ✕ 按钮清除所有快速过滤，或再次点击当前激活的过滤器按钮。

### 与标签过滤组合

快速过滤与标签过滤可以同时使用：
- 先选择标签（如：CrewAI、Python）
- 再选择快速过滤器（如：在线）
- 结果：只显示在线的 CrewAI Python Agent

## 总结

快速过滤按钮功能成功实现了：

✅ **预定义过滤器** - 6 个常用过滤条件
✅ **计数显示** - 实时显示匹配的 Agent 数量
✅ **切换功能** - 点击切换/清除过滤器
✅ **清除按钮** - 快速清除所有过滤
✅ **组合过滤** - 与标签过滤协同工作
✅ **v-model 支持** - 双向绑定
✅ **响应式设计** - 适配移动设备
✅ **平滑动画** - 过渡效果流畅
✅ **视觉反馈** - 清晰的激活状态

该功能为用户提供了快速、直观的 Agent 过滤方式，显著提升了用户浏览和查找特定 Agent 的效率。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

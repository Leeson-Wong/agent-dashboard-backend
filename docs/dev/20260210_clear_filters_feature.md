# Clear All Filters Button 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，用户可能会同时激活多个过滤器（快速过滤、标签过滤等）。当需要查看所有 Agent 时，用户需要分别清除每个过滤器，这很不方便。

一键清除所有过滤功能可以：
- 快速重置所有过滤器到初始状态
- 提供更好的用户体验
- 减少操作步骤

## 需求分析

### 核心需求

1. **一键清除** - 单次点击清除所有过滤器
2. **智能显示** - 只在有激活的过滤器时显示按钮
3. **视觉区分** - 与单独清除按钮有明显的视觉区分
4. **完整清除** - 清除快速过滤、标签过滤等所有过滤器

## 实现方案

### 1. 增强 QuickFilterButtons 组件

**文件**: `src/components/QuickFilterButtons.vue`

#### 添加新 Props

```typescript
const props = defineProps<{
  agents: AgentState[]
  modelValue?: string // Active filter ID (v-model)
  hasTagFilter?: boolean // Whether tag filter is active
  hasQuickFilter?: boolean // Whether quick filter is active
}>()
```

#### 添加新事件

```typescript
const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
  'filter-change': [filter: QuickFilter | undefined]
  'clear-all': [] // Event to clear all filters
}>()
```

#### 添加清除所有过滤器方法

```typescript
// Clear all filters (including tag filter and quick filter)
const clearAllFilters = (): void => {
  clearFilter()
  emit('clear-all')
}
```

#### 添加清除所有过滤器按钮

```vue
<!-- Clear all filters button -->
<Transition name="fade">
  <button
    v-if="hasActiveFilter || hasTagFilter || hasQuickFilter"
    class="clear-all-btn"
    @click="clearAllFilters"
    title="清除所有过滤"
  >
    🗑️
  </button>
</Transition>
```

#### 添加按钮样式

```css
.clear-all-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: rgba(245, 158, 11, 0.2);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 6px;
  color: #fbbf24;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-all-btn:hover {
  background: rgba(245, 158, 11, 0.3);
  border-color: rgba(245, 158, 11, 0.5);
  color: #fde047;
}
```

### 2. 集成到主应用

**文件**: `src/App.vue`

#### 添加 hasTagFilter 计算属性

```typescript
// Computed property to check if tag filter is active
const hasTagFilter = computed(() => {
  return selectedTags.value && selectedTags.value.length > 0
})
```

#### 添加清除所有过滤器处理函数

```typescript
// Handle clear all filters
const handleClearAllFilters = (): void => {
  // Clear quick filter
  clearQuickFilter()
  // Clear tag filter
  if (selectedTags.value) {
    selectedTags.value = []
  }
  info('已清除所有过滤器')
}
```

#### 更新 QuickFilterButtons 绑定

```vue
<QuickFilterButtons
  :agents="agents"
  v-model="quickFilter"
  :has-tag-filter="hasTagFilter"
  :has-quick-filter="hasQuickFilter"
  @filter-change="handleQuickFilterChange"
  @clear-all="handleClearAllFilters"
/>
```

## 功能特性

### 两个清除按钮的区别

| 按钮 | 图标 | 功能 | 颜色 |
|------|------|------|------|
| **清除快速过滤** | ✕ | 仅清除快速过滤器 | 红色 |
| **清除所有过滤** | 🗑️ | 清除所有过滤器（快速过滤 + 标签过滤） | 橙色 |

### 显示逻辑

- **清除快速过滤** - 只在有激活的快速过滤器时显示
- **清除所有过滤** - 在有任何激活的过滤器时显示（快速过滤、标签过滤等）

### 视觉设计

- 两个按钮有明显的颜色区分
- 都使用 28x28 像素的方形按钮
- 使用过渡动画，显示/隐藏更平滑

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (265 modules)

### 功能测试

1. **快速过滤清除** ✅
   - 点击快速过滤按钮后，✕ 按钮出现
   - 点击 ✕ 仅清除快速过滤

2. **标签过滤清除** ✅
   - 选择标签后，🗑️ 按钮出现
   - 点击 🗑️ 清除所有过滤

3. **组合过滤清除** ✅
   - 同时激活快速过滤和标签过滤
   - 两个按钮都显示
   - 点击 🗑️ 清除所有过滤器

## 使用说明

### 使用方法

1. 激活任意过滤器（快速过滤或标签过滤）
2. 点击快速过滤栏右侧的 🗑️ 按钮
3. 所有过滤器被清除，显示所有 Agent

### 按钮布局

从左到右：
1. 快速过滤按钮（📋 🕐 ⭐ 🟢 ⚫ 🔄 ❌ ⏸️）
2. 排序选择器（🔀）
3. 清除快速过滤按钮（✕）- 仅在快速过滤激活时显示
4. 清除所有过滤按钮（🗑️）- 在任何过滤激活时显示

## 文件变更

### 修改文件

1. **src/components/QuickFilterButtons.vue** (~330 行)
   - 添加 hasTagFilter 和 hasQuickFilter props
   - 添加 clear-all 事件
   - 添加 clearAllFilters 方法
   - 添加清除所有过滤器按钮
   - 添加 .clear-all-btn 样式

2. **src/App.vue**
   - 添加 hasTagFilter 计算属性
   - 添加 handleClearAllFilters 方法
   - 更新 QuickFilterButtons 绑定

## 总结

Clear All Filters 功能成功实现了：

✅ **一键清除** - 单次点击清除所有过滤器
✅ **智能显示** - 只在有激活的过滤器时显示
✅ **视觉区分** - 橙色 vs 红色，清晰区分
✅ **完整清除** - 清除快速过滤和标签过滤
✅ **平滑过渡** - 使用 Transition 组件实现动画

该功能为用户提供了快速重置过滤器的便捷方式，提升了用户体验。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

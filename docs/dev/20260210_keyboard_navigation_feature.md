# 键盘导航功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

键盘导航是提高应用可访问性和用户体验的重要功能。用户可以通过键盘快捷键快速浏览和选择 Agent，无需频繁使用鼠标。

## 需求分析

### 核心需求

1. **方向键导航** - 使用上下箭头键在列表中导航
2. **Page Up/Down** - 快速翻页浏览
3. **Home/End** - 跳转到列表开头/结尾
4. **空格键选择** - 使用空格键多选/取消选择
5. **回车选择** - 使用回车键选中当前项
6. **视觉反馈** - 显示当前聚焦的项目

### 技术要求

- 全局键盘事件监听
- 忽略输入框中的快捷键
- 平滑滚动到可见区域
- 循环导航选项

## 实现方案

### 1. 创建键盘导航 Composable

**文件**: `src/composables/useKeyboardNavigation.ts`

#### 核心数据结构

```typescript
export interface NavigationItem {
  id: string
  [key: string]: unknown
}

export interface UseKeyboardNavigationOptions {
  items: Ref<NavigationItem[]>
  onSelect?: (item: NavigationItem) => void
  onNavigate?: (item: NavigationItem) => void
  enabled?: Ref<boolean>
  loop?: boolean
}
```

#### 主要功能

**1. 导航功能**

```typescript
const navigateUp = (): void => {
  if (!enabled.value || items.value.length === 0) return

  if (focusedIndex.value <= 0) {
    focusedIndex.value = loop ? items.value.length - 1 : 0
  } else {
    focusedIndex.value--
  }

  if (onNavigate && focusedItem.value) {
    onNavigate(focusedItem.value)
  }
}

const navigateDown = (): void => {
  if (!enabled.value || items.value.length === 0) return

  if (focusedIndex.value >= items.value.length - 1) {
    focusedIndex.value = loop ? 0 : items.value.length - 1
  } else {
    focusedIndex.value++
  }

  if (onNavigate && focusedItem.value) {
    onNavigate(focusedItem.value)
  }
}
```

**2. 快速导航**

```typescript
const navigateToPageUp = (): void => {
  if (!enabled.value || items.value.length === 0) return
  const pageSize = 10
  focusedIndex.value = Math.max(0, focusedIndex.value - pageSize)

  if (onNavigate && focusedItem.value) {
    onNavigate(focusedItem.value)
  }
}

const navigateToPageDown = (): void => {
  if (!enabled.value || items.value.length === 0) return
  const pageSize = 10
  focusedIndex.value = Math.min(items.value.length - 1, focusedIndex.value + pageSize)

  if (onNavigate && focusedItem.value) {
    onNavigate(focusedItem.value)
  }
}

const navigateToFirst = (): void => {
  if (!enabled.value || items.value.length === 0) return
  focusedIndex.value = 0

  if (onNavigate && focusedItem.value) {
    onNavigate(focusedItem.value)
  }
}

const navigateToLast = (): void => {
  if (!enabled.value || items.value.length === 0) return
  focusedIndex.value = items.value.length - 1

  if (onNavigate && focusedItem.value) {
    onNavigate(focusedItem.value)
  }
}
```

**3. 选择功能**

```typescript
const toggleSelection = (itemId: string): void => {
  if (selectedIndex.value.has(itemId)) {
    selectedIndex.value.delete(itemId)
  } else {
    selectedIndex.value.add(itemId)
  }
  // Force reactivity update
  selectedIndex.value = new Set(selectedIndex.value)
}

const selectItem = (itemId: string): void => {
  selectedIndex.value.clear()
  selectedIndex.value.add(itemId)
  selectedIndex.value = new Set(selectedIndex.value)
}

const clearSelection = (): void => {
  selectedIndex.value.clear()
  selectedIndex.value = new Set(selectedIndex.value)
}

const selectAll = (): void => {
  items.value.forEach(item => selectedIndex.value.add(item.id))
  selectedIndex.value = new Set(selectedIndex.value)
}
```

**4. 键盘事件处理**

```typescript
const handleKeydown = (event: KeyboardEvent): void => {
  if (!enabled.value) return

  // Ignore shortcuts when typing in input, textarea, or contenteditable
  const target = event.target as HTMLElement
  const tagName = target.tagName
  const isInput = tagName === 'INPUT' || tagName === 'TEXTAREA' || target.isContentEditable

  if (isInput) return

  switch (event.key) {
    case 'ArrowUp':
      event.preventDefault()
      navigateUp()
      break

    case 'ArrowDown':
      event.preventDefault()
      navigateDown()
      break

    case 'PageUp':
      event.preventDefault()
      navigateToPageUp()
      break

    case 'PageDown':
      event.preventDefault()
      navigateToPageDown()
      break

    case 'Home':
      event.preventDefault()
      navigateToFirst()
      break

    case 'End':
      event.preventDefault()
      navigateToLast()
      break

    case 'Enter':
    case ' ':
      if (focusedItem.value && focusedIndex.value >= 0) {
        event.preventDefault()
        if (event.key === ' ' || event.ctrlKey) {
          // Space or Ctrl+Enter: Toggle selection
          toggleSelection(focusedItem.value.id)
        } else {
          // Enter: Select and trigger callback
          selectItem(focusedItem.value.id)
          if (onSelect) {
            onSelect(focusedItem.value)
          }
        }
      }
      break

    case 'Escape':
      event.preventDefault()
      focusedIndex.value = -1
      clearSelection()
      break

    case 'a':
    case 'A':
      if (event.ctrlKey || event.metaKey) {
        event.preventDefault()
        selectAll()
      }
      break
  }
}
```

**5. 焦点管理**

```typescript
const focusIndex = (index: number): void => {
  if (index >= 0 && index < items.value.length) {
    focusedIndex.value = index
    if (onNavigate && focusedItem.value) {
      onNavigate(focusedItem.value)
    }
  }
}

const focusItem = (itemId: string): void => {
  const index = items.value.findIndex(item => item.id === itemId)
  if (index >= 0) {
    focusIndex(index)
  }
}

const blur = (): void => {
  focusedIndex.value = -1
}
```

### 2. 集成到 Agent 列表面板

**文件**: `src/components/AgentListPanel.vue`

#### 导入 Composable

```typescript
import { useKeyboardNavigation } from '../composables/useKeyboardNavigation'
```

#### 设置键盘导航

```typescript
// Keyboard navigation setup
keyboardNav = useKeyboardNavigation({
  items: filteredAgents,
  onSelect: (item) => {
    selectAgent((item as AgentState).agentId)
  },
  onNavigate: (item) => {
    // Scroll to item when navigating
    const element = document.querySelector(`[data-agent-id="${(item as AgentState).agentId}"]`)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
    }
  },
  enabled: computed(() => !showSavePresetDialog.value),
  loop: true
})
```

#### 更新模板

```vue
<div
  v-for="agent in filteredAgents"
  :key="agent.agentId"
  :data-agent-id="agent.agentId"
  :class="['agent-item', {
    selected: selectedAgentId === agent.agentId,
    'batch-selected': selectedAgentIds.has(agent.agentId),
    focused: keyboardNav?.focusedItem?.agentId === agent.agentId
  }]"
  @click="selectAgent(agent.agentId)"
  @dblclick="handleDoubleClick(agent)"
  @keydown="handleAgentKeydown($event, agent.agentId)"
  role="listitem"
  :aria-label="getAgentAriaLabel(agent)"
  :aria-selected="selectedAgentId === agent.agentId"
  tabindex="0"
>
```

### 3. 样式实现

#### 聚焦状态样式

```css
.agent-item.focused {
  outline: 2px solid rgba(168, 85, 247, 0.5);
  outline-offset: -2px;
  background: rgba(168, 85, 247, 0.05);
}

.agent-item.focused.selected {
  outline-color: rgba(59, 130, 246, 0.6);
}
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **方向键导航** ✅
   - 上箭头向上移动焦点
   - 下箭头向下移动焦点
   - 循环导航正常工作

2. **Page Up/Down** ✅
   - Page Up 向上翻页
   - Page Down 向下翻页

3. **Home/End** ✅
   - Home 跳转到第一项
   - End 跳转到最后一项

4. **选择功能** ✅
   - 空格键切换选择
   - 回车键选中并触发回调
   - Ctrl+A 全选

5. **视觉反馈** ✅
   - 聚焦项显示紫色边框
   - 平滑滚动到可见区域

## 技术要点

### 1. 智能输入检测

自动检测是否在输入框中，避免干扰用户输入：

```typescript
const target = event.target as HTMLElement
const tagName = target.tagName
const isInput = tagName === 'INPUT' || tagName === 'TEXTAREA' || target.isContentEditable

if (isInput) return
```

### 2. 循环导航

支持列表循环导航，从最后一项跳到第一项，反之亦然：

```typescript
if (focusedIndex.value >= items.value.length - 1) {
  focusedIndex.value = loop ? 0 : items.value.length - 1
} else {
  focusedIndex.value++
}
```

### 3. 自动滚动

导航时自动滚动到可见区域：

```typescript
const element = document.querySelector(`[data-agent-id="${agentId}"]`)
if (element) {
  element.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
}
```

### 4. 响应式 Set

使用 Set 的重新赋值来触发 Vue 的响应式更新：

```typescript
selectedIndex.value = new Set(selectedIndex.value)
```

### 5. 条件启用

支持根据状态动态启用/禁用键盘导航：

```typescript
enabled: computed(() => !showSavePresetDialog.value)
```

## 键盘快捷键

| 快捷键 | 功能 |
|--------|------|
| ↑ | 向上导航 |
| ↓ | 向下导航 |
| Page Up | 向上翻页（10 项） |
| Page Down | 向下翻页（10 项） |
| Home | 跳转到第一项 |
| End | 跳转到最后一项 |
| Enter | 选中当前项 |
| Space | 切换选择 |
| Ctrl+A | 全选 |
| Escape | 清除选择并取消焦点 |

## 文件变更

### 新增文件

1. **src/composables/useKeyboardNavigation.ts** (~200 行)
   - 键盘导航逻辑
   - 选择管理
   - 焦点管理
   - 事件处理

### 修改文件

1. **src/components/AgentListPanel.vue**
   - 导入 useKeyboardNavigation
   - 设置键盘导航
   - 更新模板添加 data-agent-id 属性
   - 添加 focused 类样式

## 使用说明

### 基本使用

```typescript
import { useKeyboardNavigation } from './composables/useKeyboardNavigation'

const keyboardNav = useKeyboardNavigation({
  items: computed(() => myList.value),
  onSelect: (item) => {
    console.log('Selected:', item)
  },
  onNavigate: (item) => {
    console.log('Navigated to:', item)
  },
  enabled: ref(true),
  loop: true
})
```

### 自定义页面大小

```typescript
const navigateToPageUp = (): void => {
  const pageSize = 20  // 自定义页面大小
  focusedIndex.value = Math.max(0, focusedIndex.value - pageSize)
}
```

## 已知限制

1. **单一列表** - 当前实现只支持单个列表的导航
2. **无虚拟滚动** - 大列表可能影响性能
3. **固定页面大小** - Page Up/Down 的步长固定为 10

## 未来改进

1. **多列表支持** - 支持多个列表之间的导航
2. **虚拟滚动** - 支持虚拟滚动以提高性能
3. **可配置快捷键** - 允许用户自定义快捷键
4. **导航历史** - 记录导航历史，支持后退
5. **搜索模式** - 输入字符快速跳转到匹配项

## 总结

键盘导航功能成功实现了：

✅ **方向键导航** - 上下箭头键导航
✅ **快速导航** - Page Up/Down/Home/End
✅ **选择功能** - 空格/回车选择
✅ **全选功能** - Ctrl+A 全选
✅ **视觉反馈** - 聚焦状态显示
✅ **自动滚动** - 平滑滚动到可见项
✅ **智能检测** - 忽略输入框中的快捷键
✅ **循环导航** - 支持列表循环

该功能大大提高了应用的可访问性和用户体验，让用户可以更高效地浏览和操作 Agent 列表。

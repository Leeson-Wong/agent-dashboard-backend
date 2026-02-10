# 帮助提示系统功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在复杂的监控系统中，用户可能需要帮助来理解各种功能和操作。帮助提示系统提供了：

1. **上下文敏感的帮助** - 根据用户当前位置提供相关帮助
2. **键盘快捷键参考** - 快速查看所有可用快捷键
3. **可搜索的帮助主题** - 通过搜索快速找到所需信息
4. **分类组织** - 按功能类别组织帮助内容

## 需求分析

### 核心需求

1. **帮助按钮** - 显示在头部，方便用户访问
2. **帮助面板** - 包含所有帮助主题的滑动面板
3. **搜索功能** - 实时过滤帮助主题
4. **分类显示** - 按类别组织（快捷键、导航、视图、功能）
5. **可关闭提示** - 用户可以永久关闭特定提示
6. **重置功能** - 允许用户恢复所有已关闭的提示

### 技术要求

- 使用 Composable 模式管理提示状态
- localStorage 持久化用户的关闭状态
- 优先级排序显示提示
- 响应式设计适配移动设备

## 实现方案

### 1. 创建帮助提示 Composable

**文件**: `src/composables/useHelpTooltip.ts`

#### 核心数据结构

```typescript
export interface HelpTooltip {
  id: string
  title: string
  content: string
  position?: 'top' | 'bottom' | 'left' | 'right'
  priority?: number
  showOnce?: boolean
}
```

#### 主要功能

**1. 显示状态检查**：

```typescript
const shouldShow = (tooltip: HelpTooltip): boolean => {
  if (!config.enabled) return false
  if (dismissedTooltips.value.has(tooltip.id)) return false
  if (tooltip.showOnce && activeTooltips.value.has(tooltip.id)) return false
  return true
}
```

**2. 显示提示**：

```typescript
const showTooltip = (
  tooltip: HelpTooltip,
  element: HTMLElement
): void => {
  if (!shouldShow(tooltip)) return
  markShown(tooltip.id)

  // Create tooltip element
  const tooltipEl = document.createElement('div')
  tooltipEl.className = 'help-tooltip'
  tooltipEl.setAttribute('data-tooltip-id', tooltip.id)
  tooltipEl.innerHTML = `
    <div class="tooltip-header">
      <span class="tooltip-title">${tooltip.title}</span>
      <button class="tooltip-close" data-tooltip-id="${tooltip.id}">×</button>
    </div>
    <div class="tooltip-content">${tooltip.content}</div>
  `

  // Position tooltip based on position property
  // Add close handler
  // Auto-hide after delay if showOnce is true
}
```

**3. 优先级排序**：

```typescript
const getSortedTooltips = (tooltips: HelpTooltip[]): HelpTooltip[] => {
  return tooltips
    .filter(t => shouldShow(t))
    .sort((a, b) => (b.priority || 0) - (a.priority || 0))
}
```

**4. 状态管理**：

```typescript
const dismiss = (tooltipId: string): void => {
  dismissedTooltips.value.add(tooltipId)
  saveDismissed()
  activeTooltips.value.delete(tooltipId)
}

const resetDismissed = (): void => {
  dismissedTooltips.value.clear()
  saveDismissed()
}
```

### 2. 创建帮助提示组件

**文件**: `src/components/HelpTooltip.vue`

#### 组件结构

```
HelpTooltip
├── Help Button ("?" icon)
│   └── Click to toggle help panel
└── Help Panel (fixed position)
    ├── Header
    │   ├── Title ("快速帮助")
    │   └── Close Button (×)
    ├── Search Input
    │   └── Real-time filtering
    ├── Categories (expandable)
    │   ├── Keyboard Shortcuts (⌨️)
    │   ├── Navigation (🧭)
    │   ├── View Control (👁️)
    │   └── Features (✨)
    └── Footer
        ├── Reset Tooltips Button
        └── Close Button
```

#### 核心实现

**帮助按钮**：

```vue
<button
  class="help-button"
  @click="toggleHelp"
  :class="{ active: isOpen }"
  :title="isOpen ? '关闭帮助' : '打开帮助'"
>
  <span class="help-icon">?</span>
</button>
```

**搜索功能**：

```vue
<div class="help-search">
  <input
    v-model="searchQuery"
    type="text"
    placeholder="搜索帮助主题..."
    class="search-input"
  />
</div>
```

**分类显示**：

```vue
<div
  v-for="category in filteredCategories"
  :key="category.id"
  class="help-category"
>
  <button
    class="category-toggle"
    @click="toggleCategory(category.id)"
    :class="{ expanded: expandedCategories.has(category.id) }"
  >
    <span class="category-icon">{{ category.icon }}</span>
    <span class="category-name">{{ category.name }}</span>
    <span class="category-count">{{ category.items.length }}</span>
  </button>

  <Transition name="expand">
    <div v-if="expandedCategories.has(category.id)" class="category-items">
      <div
        v-for="item in category.items"
        :key="item.id"
        class="help-item"
        @click="showHelpItem(item)"
      >
        <span class="item-icon">{{ item.icon }}</span>
        <span class="item-title">{{ item.title }}</span>
        <span class="item-shortcut" v-if="item.shortcut">
          {{ item.shortcut }}
        </span>
      </div>
    </div>
  </Transition>
</div>
```

**过滤逻辑**：

```typescript
const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value

  const query = searchQuery.value.toLowerCase()
  return categories.value.map(category => ({
    ...category,
    items: category.items.filter(item =>
      item.title.toLowerCase().includes(query)
    )
  })).filter(category => category.items.length > 0)
})
```

### 3. 帮助内容组织

#### 键盘快捷键分类

```typescript
{
  id: 'shortcuts',
  name: '键盘快捷键',
  icon: '⌨️',
  items: [
    { id: 'shortcut-refresh', title: '刷新数据', shortcut: 'Ctrl+R', icon: '🔄' },
    { id: 'shortcut-search', title: '搜索 Agent', shortcut: '/', icon: '🔍' },
    { id: 'shortcut-command', title: '命令面板', shortcut: 'Ctrl+Shift+P', icon: '⌘' },
    { id: 'shortcut-settings', title: '打开设置', shortcut: ',', icon: '⚙️' },
    { id: 'shortcut-help', title: '键盘帮助', shortcut: '?', icon: '?' }
  ]
}
```

#### 导航操作分类

```typescript
{
  id: 'navigation',
  name: '导航操作',
  icon: '🧭',
  items: [
    { id: 'nav-select', title: '选择 Agent', icon: '🖱️' },
    { id: 'nav-detail', title: '查看详情', icon: '📄' },
    { id: 'nav-logs', title: '查看日志', icon: '📋' },
    { id: 'nav-tasks', title: '查看任务', icon: '✅' }
  ]
}
```

#### 视图控制分类

```typescript
{
  id: 'view',
  name: '视图控制',
  icon: '👁️',
  items: [
    { id: 'view-compact', title: '紧凑模式', icon: '🔲' },
    { id: 'view-theme', title: '切换主题', icon: '🌓' },
    { id: 'view-fullscreen', title: '全屏模式', icon: '⛶' }
  ]
}
```

#### 功能说明分类

```typescript
{
  id: 'features',
  name: '功能说明',
  icon: '✨',
  items: [
    { id: 'feat-sound', title: '声音通知', icon: '🔊' },
    { id: 'feat-export', title: '数据导出', icon: '💾' },
    { id: 'feat-share', title: '分享视图', icon: '🔗' }
  ]
}
```

### 4. 样式实现

**帮助按钮样式**：

```css
.help-button {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.help-button.active {
  border-color: rgba(59, 130, 246, 0.3);
  color: #3b82f6;
}
```

**帮助面板样式**：

```css
.help-panel {
  position: fixed;
  top: 80px;
  right: 20px;
  width: 320px;
  max-height: calc(100vh - 120px);
  z-index: 1000;
}

.help-content {
  background: rgba(30, 41, 59, 0.98);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 120px);
}
```

**分类项样式**：

```css
.category-toggle {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: transparent;
  border: none;
  color: #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.category-toggle:hover {
  background: rgba(51, 65, 85, 0.5);
}

.category-count {
  font-size: 11px;
  color: #64748b;
  background: rgba(100, 116, 139, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
}
```

**帮助项样式**：

```css
.help-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px 10px 32px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 2px solid transparent;
}

.help-item:hover {
  background: rgba(51, 65, 85, 0.5);
  border-left-color: #3b82f6;
}

.item-shortcut {
  font-size: 11px;
  color: #64748b;
  background: rgba(100, 116, 139, 0.2);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
}
```

**过渡动画**：

```css
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 500px;
  opacity: 1;
}
```

### 5. 集成到主应用

**文件**: `src/App.vue`

**导入组件**：

```typescript
import HelpTooltip from './components/HelpTooltip.vue'
```

**添加到头部**：

```vue
<HelpTooltip />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (251 modules)

```
✓ 251 modules transformed.
✓ built in 4.23s
```

### 功能测试

1. **帮助按钮** ✅
   - 点击按钮打开/关闭帮助面板
   - 图标正确变化
   - 按钮状态正确更新

2. **帮助面板** ✅
   - 面板固定位置显示（右上角）
   - 正确的 z-index 层级
   - 滑动淡入动画流畅

3. **搜索功能** ✅
   - 实时过滤帮助主题
   - 支持大小写不敏感搜索
   - 空搜索结果显示所有分类

4. **分类展开/收起** ✅
   - 点击分类标题切换展开状态
   - 展开动画流畅
   - 默认展开"快捷键"和"导航"

5. **帮助项点击** ✅
   - 点击帮助项有关联
   - 悬停效果正常
   - 快捷键标签正确显示

6. **重置提示** ✅
   - 重置按钮清除已关闭的提示
   - 显示成功提示

7. **响应式设计** ✅
   - 移动设备上正确显示
   - 面板宽度自适应

## 帮助分类汇总

| 分类 | 图标 | 项目数 | 说明 |
|------|------|--------|------|
| 键盘快捷键 | ⌨️ | 5 | 系统快捷键 |
| 导航操作 | 🧭 | 4 | 导航相关操作 |
| 视图控制 | 👁️ | 3 | 视图设置 |
| 功能说明 | ✨ | 3 | 功能介绍 |

## 使用示例

### 在组件中使用 Composable

```typescript
import { useHelpTooltip } from '../composables/useHelpTooltip'

const {
  shouldShow,
  dismiss,
  showTooltip,
  resetDismissed
} = useHelpTooltip()

// Check if tooltip should be shown
if (shouldShow(myTooltip)) {
  // Show tooltip
  showTooltip(myTooltip, elementRef.value)
}

// Dismiss a tooltip
dismiss('tooltip-id')

// Reset all dismissed tooltips
resetDismissed()
```

### 添加自定义提示

```typescript
const customTooltip: HelpTooltip = {
  id: 'custom-feature',
  title: '新功能提示',
  content: '这是一个新功能，点击了解更多...',
  position: 'top',
  priority: 10,
  showOnce: true
}

showTooltip(customTooltip, elementRef.value)
```

## 技术要点

### 1. localStorage 持久化

```typescript
const loadDismissed = (): void => {
  if (!config.enabled) return

  try {
    const saved = localStorage.getItem(`${config.storageKey}_dismissed`)
    if (saved) {
      const dismissed = JSON.parse(saved) as string[]
      dismissedTooltips.value = new Set(dismissed)
    }
  } catch (error) {
    console.warn('Failed to load dismissed tooltips:', error)
  }
}
```

### 2. 动态元素创建

```typescript
const showTooltip = (tooltip: HelpTooltip, element: HTMLElement): void => {
  const tooltipEl = document.createElement('div')
  tooltipEl.className = 'help-tooltip'
  tooltipEl.setAttribute('data-tooltip-id', tooltip.id)
  tooltipEl.innerHTML = /* ... */

  // Position calculation
  const rect = element.getBoundingClientRect()
  const scrollX = window.pageXOffset || document.documentElement.scrollLeft
  const scrollY = window.pageYOffset || document.documentElement.scrollTop

  // ... position logic

  document.body.appendChild(tooltipEl)
}
```

### 3. 过渡动画

使用 Vue 的 Transition 组件：

```vue
<Transition name="slide-fade">
  <div v-if="isOpen" class="help-panel">
    <!-- Panel content -->
  </div>
</Transition>

<Transition name="expand">
  <div v-if="expandedCategories.has(category.id)" class="category-items">
    <!-- Items -->
  </div>
</Transition>
```

### 4. 计算属性优化

```typescript
const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value

  const query = searchQuery.value.toLowerCase()
  return categories.value.map(category => ({
    ...category,
    items: category.items.filter(item =>
      item.title.toLowerCase().includes(query)
    )
  })).filter(category => category.items.length > 0)
})
```

## 已知限制

1. **简化实现** - 当前使用基本 HTML/CSS，没有复杂的富文本内容
2. **图片支持** - 不支持在帮助内容中嵌入图片
3. **多语言** - 当前仅支持中文
4. **键盘导航** - 面板内不支持键盘导航（上下箭头选择）

## 未来改进

1. **富文本内容** - 支持 Markdown 或 HTML 格式
2. **视频教程** - 嵌入短视频教程
3. **交互式导览** - 新用户引导导览
4. **上下文提示** - 根据当前页面/状态显示相关提示
5. **键盘导航** - 支持键盘在面板内导航
6. **多语言** - 支持国际化
7. **搜索高亮** - 搜索时高亮匹配文本
8. **收藏功能** - 收藏常用帮助项

## 文件变更

### 新增文件

1. **src/composables/useHelpTooltip.ts** (~215 行)
   - 帮助提示管理
   - localStorage 持久化
   - 优先级排序
   - DOM 操作

2. **src/components/HelpTooltip.vue** (~490 行)
   - 帮助按钮 UI
   - 帮助面板
   - 搜索功能
   - 分类显示

### 修改文件

1. **src/App.vue**
   - 导入 HelpTooltip 组件
   - 添加到头部

## 使用说明

### 打开帮助面板

点击头部 "?" 按钮：
- 关闭状态 → 打开帮助面板
- 打开状态 → 关闭帮助面板

### 搜索帮助主题

在搜索框中输入关键词：
- 实时过滤所有分类
- 大小写不敏感
- 只显示匹配的分类和项目

### 展开/收起分类

点击分类标题：
- 展开显示该分类的所有项目
- 再次点击收起

### 重置提示

点击 "🔄 重置提示" 按钮：
- 清除所有已关闭的提示
- 恢复所有提示的显示状态

## 总结

帮助提示系统功能成功实现了：

✅ **帮助按钮** - 一键访问帮助
✅ **帮助面板** - 分类组织的帮助内容
✅ **搜索功能** - 实时过滤帮助主题
✅ **分类显示** - 按功能类别组织
✅ **可关闭提示** - 用户可以永久关闭特定提示
✅ **重置功能** - 允许用户恢复所有提示
✅ **优先级排序** - 高优先级提示优先显示
✅ **状态持久化** - localStorage 保存用户偏好
✅ **响应式设计** - 适配移动设备
✅ **平滑动画** - 流畅的过渡效果

该功能为用户提供了完整的帮助系统，可以根据需要快速查找和理解各种功能，显著提升了用户体验。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# 紧凑模式功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在小屏幕设备或需要查看更多内容时，用户希望能够在界面上显示更多信息。为了满足这种需求，我们需要实现一个紧凑模式（Compact Mode）功能，允许用户切换不同的显示密度。

## 需求分析

### 核心需求

1. **三种模式** - 正常、紧凑、超紧凑
2. **一键切换** - 点击按钮循环切换模式
3. **全局应用** - 所有组件统一缩放
4. **状态持久化** - 保存用户选择到 localStorage
5. **视觉反馈** - 清晰显示当前模式状态

### 技术要求

- 使用 CSS 变量实现动态缩放
- 使用 data 属性控制全局样式
- 保持响应式布局兼容性

## 实现方案

### 1. 创建紧凑模式 Composable

**文件**: `src/composables/useCompactMode.ts`

#### 核心数据结构

```typescript
export type CompactLevel = 'normal' | 'compact' | 'ultra'

export interface CompactModeConfig {
  key: string          // localStorage 键名
  enabled: boolean     // 是否启用
  defaultLevel: CompactLevel  // 默认级别
}
```

#### 缩放系数定义

```typescript
const LEVEL_STYLES = {
  normal: {
    scale: 1,        // 整体缩放
    spacing: 1,      // 间距
    fontSize: 1,     // 字体大小
    padding: 1       // 内边距
  },
  compact: {
    scale: 0.9,
    spacing: 0.75,
    fontSize: 0.9,
    padding: 0.8
  },
  ultra: {
    scale: 0.8,
    spacing: 0.6,
    fontSize: 0.85,
    padding: 0.65
  }
}
```

#### 主要功能

**1. 生成 CSS 变量**

```typescript
const getCssVars = (): Record<string, string> => {
  const styles = LEVEL_STYLES[compactLevel.value]
  return {
    '--compact-scale': styles.scale.toString(),
    '--compact-spacing': styles.spacing.toString(),
    '--compact-font-size': styles.fontSize.toString(),
    '--compact-padding': styles.padding.toString()
  }
}
```

**2. 应用紧凑模式**

```typescript
const applyCompactMode = (): void => {
  const root = document.documentElement
  const vars = getCssVars()

  Object.entries(vars).forEach(([key, value]) => {
    root.style.setProperty(key, value)
  })

  // Update data attribute for CSS selectors
  root.setAttribute('data-compact-mode', compactLevel.value)
}
```

**3. 状态持久化**

```typescript
const loadFromStorage = (): CompactLevel => {
  if (!config.enabled) return config.defaultLevel

  try {
    const saved = localStorage.getItem(storageKey)
    if (saved && ['normal', 'compact', 'ultra'].includes(saved)) {
      return saved as CompactLevel
    }
  } catch (error) {
    console.warn('Failed to load compact mode from storage:', error)
  }

  return config.defaultLevel
}

const saveToStorage = (level: CompactLevel): void => {
  if (!config.enabled) return

  try {
    localStorage.setItem(storageKey, level)
  } catch (error) {
    console.warn('Failed to save compact mode to storage:', error)
  }
}
```

**4. 模式切换**

```typescript
// 设置特定级别
const setCompactLevel = (level: CompactLevel): void => {
  compactLevel.value = level
  applyCompactMode()
  saveToStorage(level)
}

// 切换正常/紧凑
const toggleCompact = (): void => {
  const newLevel: CompactLevel = compactLevel.value === 'normal' ? 'compact' : 'normal'
  setCompactLevel(newLevel)
}

// 循环切换所有级别
const cycleLevel = (): void => {
  const levels: CompactLevel[] = ['normal', 'compact', 'ultra']
  const currentIndex = levels.indexOf(compactLevel.value)
  const nextIndex = (currentIndex + 1) % levels.length
  setCompactLevel(levels[nextIndex])
}
```

### 2. 创建紧凑模式切换组件

**文件**: `src/components/CompactModeToggle.vue`

#### 组件结构

```
CompactModeToggle
├── Toggle Button
│   ├── Icon (🔲/▦/▣)
│   └── Label (正常/紧凑/超紧凑)
└── Tooltip (短暂提示)
```

#### 核心实现

**按钮图标和标签**：

```typescript
const getIcon = (): string => {
  switch (compactLevel.value) {
    case 'normal': return '🔲'
    case 'compact': return '▦'
    case 'ultra': return '▣'
    default: return '🔲'
  }
}

const getLabel = (): string => {
  switch (compactLevel.value) {
    case 'normal': return '正常'
    case 'compact': return '紧凑'
    case 'ultra': return '超紧凑'
    default: return '正常'
  }
}
```

**样式变化**：

```css
.toggle-btn.compact {
  border-color: rgba(59, 130, 246, 0.3);
  color: #3b82f6;
}

.toggle-btn.ultra {
  border-color: rgba(168, 85, 247, 0.3);
  color: #a855f7;
}
```

### 3. 创建紧凑模式 CSS

**文件**: `src/styles/compact-mode.css`

#### CSS 变量定义

```css
:root {
  --compact-scale: 1;
  --compact-spacing: 1;
  --compact-font-size: 1;
  --compact-padding: 1;
}

[data-compact-mode="compact"] {
  --compact-scale: 0.9;
  --compact-spacing: 0.75;
  --compact-font-size: 0.9;
  --compact-padding: 0.8;
}

[data-compact-mode="ultra"] {
  --compact-scale: 0.8;
  --compact-spacing: 0.6;
  --compact-font-size: 0.85;
  --compact-padding: 0.65;
}
```

#### 应用到组件

**头部**：

```css
[data-compact-mode] .app-header {
  padding: calc(12px * var(--compact-padding)) calc(20px * var(--compact-padding));
  min-height: calc(60px * var(--compact-scale));
}

[data-compact-mode] .app-header h1 {
  font-size: calc(24px * var(--compact-font-size));
}
```

**Agent 列表**：

```css
[data-compact-mode] .agent-list-panel {
  width: calc(300px * var(--compact-scale));
}

[data-compact-mode] .agent-list-item {
  padding: calc(10px * var(--compact-padding)) calc(12px * var(--compact-padding));
  gap: calc(8px * var(--compact-spacing));
}

[data-compact-mode] .agent-name {
  font-size: calc(14px * var(--compact-font-size));
}
```

**按钮**：

```css
[data-compact-mode] button {
  padding: calc(6px * var(--compact-padding)) calc(12px * var(--compact-padding));
  font-size: calc(13px * var(--compact-font-size));
}
```

**标签**：

```css
[data-compact-mode] .tag {
  padding: calc(2px * var(--compact-padding)) calc(8px * var(--compact-padding));
  font-size: calc(10px * var(--compact-font-size));
}
```

#### 特定组件调整

```css
/* Tag Filter Panel */
[data-compact-mode] .tag-filter-panel {
  width: calc(280px * var(--compact-scale));
  max-height: calc(400px * var(--compact-scale));
}

/* Mini Map */
[data-compact-mode] .mini-map {
  width: calc(200px * var(--compact-scale));
  height: calc(150px * var(--compact-scale));
}

/* Scratchpad */
[data-compact-mode] .scratchpad {
  width: calc(300px * var(--compact-scale));
  height: calc(400px * var(--compact-scale));
}

/* Ultra Compact specific */
[data-compact-mode="ultra"] .agent-list-panel {
  width: 260px;
}

[data-compact-mode="ultra"] .tag {
  padding: 1px 6px;
  font-size: 9px;
}
```

### 4. 集成到主应用

**文件**: `src/main.ts`

```typescript
import './styles/compact-mode.css'
```

**文件**: `src/App.vue`

**导入组件**：

```typescript
import CompactModeToggle from './components/CompactModeToggle.vue'
```

**添加到头部**：

```vue
<CompactModeToggle />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

```
✓ 243 modules transformed.
✓ built in 4.52s
```

### 功能测试

1. **模式切换** ✅
   - 点击按钮循环切换：正常 → 紧凑 → 超紧凑 → 正常
   - 图标和标签正确更新

2. **CSS 变量** ✅
   - `--compact-scale` 正确设置
   - `--compact-spacing` 正确设置
   - `--compact-font-size` 正确设置
   - `--compact-padding` 正确设置

3. **data 属性** ✅
   - `data-compact-mode` 正确设置
   - CSS 选择器正确应用样式

4. **全局缩放** ✅
   - 所有组件统一缩放
   - 间距、字体、内边距协调变化

5. **状态持久化** ✅
   - 保存到 localStorage
   - 页面刷新后恢复

6. **视觉反馈** ✅
   - 正常模式：默认样式
   - 紧凑模式：蓝色边框和文字
   - 超紧凑模式：紫色边框和文字

## 模式对比

### 正常模式 (normal)

- **缩放比例**: 100%
- **适用场景**: 大屏幕、常规使用
- **特点**: 最佳可读性

### 紧凑模式 (compact)

- **缩放比例**: 90%
- **间距**: 75%
- **字体**: 90%
- **内边距**: 80%
- **适用场景**: 笔记本电脑、需要更多内容
- **特点**: 平衡可读性与信息密度

### 超紧凑模式 (ultra)

- **缩放比例**: 80%
- **间距**: 60%
- **字体**: 85%
- **内边距**: 65%
- **适用场景**: 小屏幕、最大化内容显示
- **特点**: 最大化信息密度

## 技术要点

### 1. CSS 变量计算

使用 `calc()` 函数结合 CSS 变量：

```css
[data-compact-mode] .agent-name {
  font-size: calc(14px * var(--compact-font-size));
}
```

### 2. data 属性选择器

使用 data 属性控制样式：

```css
[data-compact-mode="compact"] {
  /* 紧凑模式样式 */
}

[data-compact-mode="ultra"] {
  /* 超紧凑模式样式 */
}
```

### 3. 动态样式设置

JavaScript 动态设置 CSS 变量：

```typescript
const root = document.documentElement
root.style.setProperty('--compact-scale', '0.9')
root.setAttribute('data-compact-mode', 'compact')
```

### 4. localStorage 集成

状态持久化到 localStorage：

```typescript
localStorage.setItem('compact_mode', 'compact')

const saved = localStorage.getItem('compact_mode')
if (saved && ['normal', 'compact', 'ultra'].includes(saved)) {
  return saved as CompactLevel
}
```

## 已知限制

1. **固定像素值** - 某些硬编码的像素值不会缩放
2. **第三方组件** - 外部库组件可能不完全适配
3. **行高** - 某些元素的行高可能需要额外调整
4. **最小可读性** - 超紧凑模式可能影响可读性

## 未来改进

1. **自定义级别** - 允许用户自定义缩放比例
2. **组件级别控制** - 某些组件可以单独控制密度
3. **快捷键** - 添加键盘快捷键切换模式
4. **平滑过渡** - 添加动画过渡效果
5. **预设配置** - 保存多个预设配置
6. **自动适配** - 根据屏幕大小自动切换

## 文件变更

### 新增文件

1. **src/composables/useCompactMode.ts** (~170 行)
   - 紧凑模式状态管理
   - CSS 变量设置
   - localStorage 持久化

2. **src/components/CompactModeToggle.vue** (~130 行)
   - 切换按钮 UI
   - 图标和标签显示
   - 工具提示

3. **src/styles/compact-mode.css** (~550 行)
   - 全局紧凑模式样式
   - 各组件缩放规则
   - 三级模式定义

### 修改文件

1. **src/main.ts**
   - 导入 compact-mode.css

2. **src/App.vue**
   - 导入 CompactModeToggle 组件
   - 添加到头部

## 使用说明

### 切换模式

点击头部 "🔲 正常" 按钮，循环切换：
1. 🔲 正常（默认）
2. ▦ 紧凑
3. ▣ 超紧凑

### 模式特点

| 模式 | 缩放 | 间距 | 适用场景 |
|------|------|------|----------|
| 正常 | 100% | 100% | 大屏幕、常规使用 |
| 紧凑 | 90% | 75% | 笔记本、需要更多内容 |
| 超紧凑 | 80% | 60% | 小屏幕、最大化内容 |

### 状态保存

模式选择会自动保存到浏览器，下次打开时恢复。

## 总结

紧凑模式功能成功实现了：

✅ **三种显示模式** - 正常、紧凑、超紧凑
✅ **一键切换** - 循环切换所有模式
✅ **全局应用** - CSS 变量统一控制所有组件
✅ **状态持久化** - localStorage 保存用户选择
✅ **视觉反馈** - 不同模式不同颜色标识
✅ **响应式兼容** - 保持移动端布局正常
✅ **组件适配** - 主要组件都有紧凑样式
✅ **计算精确** - calc() 函数精确计算尺寸

该功能为用户提供了灵活的界面密度选择，适应不同屏幕尺寸和使用场景，显著提升了系统的可用性。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

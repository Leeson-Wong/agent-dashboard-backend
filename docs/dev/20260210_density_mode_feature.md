# Display Density Mode 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

不同用户有不同的视觉偏好和屏幕空间需求。有些用户希望在有限的空间内看到更多 Agent，而有些用户则希望有更宽松舒适的视觉体验。

显示密度模式功能可以：
- 提供多种显示密度选择
- 适应不同屏幕尺寸和用户偏好
- 保持用户选择的持久化
- 提供更好的用户体验

## 需求分析

### 核心需求

1. **三种模式** - 紧凑、舒适、宽松三种显示模式
2. **模式切换** - 提供快速切换按钮
3. **持久化** - 保存用户选择到 localStorage
4. **视觉差异** - 每种模式有明显不同的视觉效果
5. **自适应** - 根据密度调整字体、间距等

## 实现方案

### 1. 创建 Display Density Composable

**文件**: `src/composables/useDisplayDensity.ts` (~130 lines)

#### 核心类型

```typescript
export type DisplayDensity = 'comfortable' | 'compact' | 'spacious'
```

#### 密度描述

```typescript
export const DENSITY_DESCRIPTIONS: Record<DisplayDensity, string> = {
  comfortable: '舒适模式 - 适合大多数情况',
  compact: '紧凑模式 - 显示更多内容',
  spacious: '宽松模式 - 更舒适的视觉体验'
}
```

#### 主要功能

```typescript
export function useDisplayDensity() {
  // State
  const currentDensity: Ref<DisplayDensity>
  const isComfortable: ComputedRef<boolean>
  const isCompact: ComputedRef<boolean>
  const isSpacious: ComputedRef<boolean>

  // Computed
  const densityClass: ComputedRef<string> // Returns 'density-compact', etc.
  const densityDescription: ComputedRef<string>
  const densityIcon: ComputedRef<string>

  // Methods
  const setDensity: (density: DisplayDensity) => void
  const cycleDensity: () => void // Cycle through densities
  const resetDensity: () => void
}
```

#### 自动持久化

```typescript
// Load from localStorage
const loadDensity = (): DisplayDensity => {
  const stored = localStorage.getItem(STORAGE_KEY)
  return stored || DEFAULT_DENSITY
}

// Save to localStorage on change
watch(currentDensity, (newDensity) => {
  saveDensity(newDensity)
})
```

### 2. 创建 Density Mode Selector 组件

**文件**: `src/components/DensityModeSelector.vue` (~230 lines)

#### 功能特性

```vue
<template>
  <div class="density-mode-selector">
    <button @click="cycleDensity" :title="densityDescription">
      <span class="density-icon">{{ densityIcon }}</span>
      <span class="density-label">{{ densityLabel }}</span>
      <span class="density-arrow">▾</span>
    </button>

    <!-- Dropdown Menu -->
    <Transition name="dropdown">
      <div v-if="isOpen" class="density-dropdown">
        <div
          v-for="mode in densityModes"
          :key="mode.value"
          class="density-option"
          @click="selectDensity(mode.value)"
        >
          <span class="option-icon">{{ mode.icon }}</span>
          <span class="option-label">{{ mode.label }}</span>
          <span class="option-description">{{ mode.description }}</span>
        </div>
      </div>
    </Transition>
  </div>
</template>
```

#### 模式定义

| 模式 | 图标 | 标签 | 描述 |
|------|------|------|------|
| **compact** | 📐 | 紧凑 | 显示更多内容 |
| **comfortable** | 📏 | 舒适 | 适合大多数情况 |
| **spacious** | 📏 | 宽松 | 更舒适的视觉体验 |

### 3. 集成到 App.vue

**文件**: `src/App.vue`

#### 添加组件到头部

```vue
<DensityModeSelector />
```

#### 添加导入

```typescript
import DensityModeSelector from './components/DensityModeSelector.vue'
```

### 4. 应用到 AgentListPanel

**文件**: `src/components/AgentListPanel.vue`

#### 添加密度类

```vue
<div
  :class="['agent-item', densityClass, {
    selected: selectedAgentId === agent.agentId
  }]"
>
```

#### 添加导入和初始化

```typescript
import { useDisplayDensity } from '../composables/useDisplayDensity'

const { densityClass } = useDisplayDensity()
```

### 5. 密度模式样式

**文件**: `src/components/AgentListPanel.vue` - 样式部分

#### 紧凑模式

```css
.agent-item.density-compact {
  padding: 6px 12px;
  gap: 8px;
  min-height: 40px;
}

.agent-item.density-compact .agent-name {
  font-size: 13px;
}

.agent-item.density-compact .agent-status-indicator {
  width: 8px;
  height: 8px;
}
```

#### 舒适模式（默认）

```css
.agent-item.density-comfortable {
  padding: 10px 16px;
  gap: 12px;
  min-height: 56px;
}
```

#### 宽松模式

```css
.agent-item.density-spacious {
  padding: 16px 20px;
  gap: 16px;
  min-height: 72px;
}

.agent-item.density-spacious .agent-name {
  font-size: 15px;
}

.agent-item.density-spacious .agent-status-indicator {
  width: 12px;
  height: 12px;
}
```

## 功能特性

### 三种密度模式对比

| 特性 | 紧凑 | 舒适 | 宽松 |
|------|------|------|------|
| **内边距** | 6px 12px | 10px 16px | 16px 20px |
| **最小高度** | 40px | 56px | 72px |
| **元素间距** | 8px | 12px | 16px |
| **字体大小** | 13px | 14px（默认） | 15px |
| **状态指示器** | 8px | 10px | 12px |

### 用户交互

| 方式 | 操作 |
|------|------|
| **按钮点击** | 循环切换密度模式 |
| **下拉菜单** | 显示所有模式供选择 |
| **自动保存** | 选择后自动保存到 localStorage |
| **自动恢复** | 刷新页面后恢复上次选择 |

### 视觉反馈

- **当前模式** - 下拉菜单中显示 ✓ 标记
- **悬停效果** - 菜单项悬停时高亮
- **平滑过渡** - 密度变化时有动画效果

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (279 modules)

### 功能测试

1. **模式切换** ✅
   - 点击按钮循环切换
   - 下拉菜单选择具体模式

2. **视觉效果** ✅
   - 紧凑模式 - 更小间距和字体
   - 舒适模式 - 默认外观
   - 宽松模式 - 更大间距和字体

3. **持久化** ✅
   - 选择后保存到 localStorage
   - 刷新页面后恢复

4. **响应式** ✅
   - 不同屏幕尺寸正确显示
   - 移动端适配良好

## 使用说明

### 快速切换

1. 点击头部工具栏的密度模式按钮（📏 舒适）
2. 按钮会循环切换：紧凑 → 舒适 → 宽松 → 紧凑

### 精确选择

1. 点击密度模式按钮打开下拉菜单
2. 选择想要的密度模式
3. 模式立即应用并自动保存

### 模式选择建议

| 场景 | 推荐模式 |
|------|----------|
| **大屏幕，想看到更多 Agent** | 紧凑 |
| **一般使用** | 舒适 |
| **演示或大屏幕** | 宽松 |
| **小屏幕笔记本** | 紧凑 |

## 文件变更

### 新增文件

1. **src/composables/useDisplayDensity.ts** (~130 lines)
   - DisplayDensity 类型定义
   - 密度描述和图标
   - useDisplayDensity composable
   - localStorage 持久化

2. **src/components/DensityModeSelector.vue** (~230 lines)
   - 密度模式选择器组件
   - 下拉菜单
   - 模式列表和图标

### 修改文件

1. **src/App.vue**
   - 导入 DensityModeSelector
   - 添加到头部工具栏

2. **src/components/AgentListPanel.vue**
   - 导入 useDisplayDensity
   - 初始化 composable
   - 应用 densityClass 到 agent-item
   - 添加三种密度模式的样式

## CSS 类名

生成的密度类名会自动应用到 Agent 列表项：

- `.density-compact` - 紧凑模式
- `.density-comfortable` - 舒适模式
- `.density-spacious` - 宽松模式

其他组件也可以使用这些类名来适配密度模式：

```vue
<div :class="['my-component', densityClass]">
  <!-- 内容会根据当前密度调整 -->
</div>
```

## 未来扩展

### 潜在增强

1. **全局密度** - 应用到整个应用，不只是 Agent 列表
2. **自定义密度** - 允许用户自定义密度值
3. **快捷键** - 添加键盘快捷键切换密度
4. **动画预览** - 切换时显示动画预览
5. **记住每页设置** - 不同页面记住不同的密度设置

## 总结

Display Density Mode 功能成功实现了：

✅ **三种模式** - 紧凑、舒适、宽松三种显示模式
✅ **模式切换** - 提供快速切换按钮和下拉菜单
✅ **持久化** - 保存用户选择到 localStorage
✅ **视觉差异** - 每种模式有明显不同的视觉效果
✅ **自适应** - 根据密度调整字体、间距、指示器大小

该功能为用户提供了灵活的显示选项，适应不同的使用场景和偏好，显著改善了用户体验。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

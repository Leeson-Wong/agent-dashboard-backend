# Agent 列表加载骨架屏

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

Agent 列表在加载过程中存在空白状态，用户体验不佳。通过添加骨架屏（Skeleton Loading）效果，可以在数据加载时提供更好的视觉反馈，减少用户感知的等待时间。

**目标**:
1. 实现 Agent 列表加载时的骨架屏效果
2. 添加流畅的闪烁动画和渐变效果
3. 保持骨架屏与实际内容布局一致
4. 确保加载状态切换平滑

---

## 实现方案

### 前端实现

#### 1. 添加 loading 属性

**文件**: `src/components/AgentListPanel.vue`

```typescript
import { ref, computed, watch, onMounted, onUnmounted, withDefaults } from 'vue'

interface Props {
  agents: AgentState[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})
```

#### 2. 骨架屏模板结构

```vue
<div :class="['agent-list', `agent-list-${viewMode}`, { 'has-selection': selectedAgentIds.size > 0 }]">
  <!-- Loading Skeleton -->
  <div v-if="loading" class="skeleton-list">
    <div v-for="i in 6" :key="i" class="skeleton-item">
      <div class="skeleton-checkbox"></div>
      <div class="skeleton-indicator"></div>
      <div class="skeleton-content">
        <div class="skeleton-name"></div>
        <div class="skeleton-meta">
          <div></div>
          <div></div>
        </div>
        <div class="skeleton-activity"></div>
      </div>
    </div>
  </div>

  <!-- Actual Agent List -->
  <template v-else>
    <div v-for="agent in filteredAgents" :key="agent.agentId" ...>
      <!-- Agent items -->
    </div>
  </template>

  <!-- Empty State -->
  <div v-if="!loading && filteredAgents.length === 0" class="empty-state">
    <!-- Empty state content -->
  </div>
</div>
```

#### 3. 骨架屏 CSS 样式

```css
/* Skeleton Loading Styles */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px;
}

.skeleton-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(100, 116, 139, 0.2);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

/* Shimmer effect */
.skeleton-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.05),
    transparent
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.skeleton-checkbox {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  background: rgba(100, 116, 139, 0.3);
}

.skeleton-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(100, 116, 139, 0.3);
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-name {
  width: 60%;
  height: 14px;
  border-radius: 4px;
  background: rgba(100, 116, 139, 0.3);
}

.skeleton-meta {
  display: flex;
  gap: 8px;
}

.skeleton-meta > div {
  height: 12px;
  border-radius: 4px;
  background: rgba(100, 116, 139, 0.3);
}

.skeleton-meta > div:first-child {
  width: 40%;
}

.skeleton-meta > div:last-child {
  width: 30%;
}

.skeleton-activity {
  width: 80%;
  height: 12px;
  border-radius: 4px;
  background: rgba(100, 116, 139, 0.3);
}

/* Pulse animation for skeleton items */
.skeleton-item {
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
```

---

## 技术细节

### 骨架屏布局

骨架屏完全模拟实际 Agent 列表的布局结构：

| 骨架元素 | 对应的实际元素 | 尺寸 |
|---------|--------------|------|
| `skeleton-checkbox` | 选择框复选框 | 16px × 16px |
| `skeleton-indicator` | 状态指示器 | 10px × 10px |
| `skeleton-name` | Agent 名称 | 60% 宽度 |
| `skeleton-meta` | 框架/语言标签 | 40% + 30% 宽度 |
| `skeleton-activity` | 活动信息 | 80% 宽度 |

### 动画效果

**Shimmer 动画**（闪烁效果）:
- 使用 `::after` 伪元素创建渐变层
- 从左到右移动渐变效果
- 动画时长: 1.5 秒
- 无限循环

**Pulse 动画**（脉冲效果）:
- 控制整个骨架项的透明度
- 在 1 和 0.7 之间变化
- 动画时长: 1.5 秒
- 使用 `ease-in-out` 缓动函数

### 状态管理

使用 Vue 的 `v-if` / `v-else` 控制状态切换：

```vue
<!-- 加载状态 -->
<div v-if="loading">骨架屏</div>

<!-- 正常状态 -->
<template v-else>实际列表</template>

<!-- 空状态 -->
<div v-if="!loading && filteredAgents.length === 0">空状态提示</div>
```

---

## UI 效果

### 骨架屏结构

```
┌─────────────────────────────────────────────────────┐
│ ☐  ●  Agent 名称占位符                               │
│       ┌──────┐ ┌─────┐                             │
│       │框架  │ │语言 │                             │
│       └──────┘ └─────┘                             │
│       活动信息占位符                                 │
│                                                     │
│  ↓ 闪烁动画从左到右移动                              │
│  ░░░░░░░░▓▓▓▓▓▓▓▓▓░░░░░░░░                          │
│                                                     │
├─────────────────────────────────────────────────────┤
│ ☐  ●  Agent 名称占位符                               │
│       ┌──────┐ ┌─────┐                             │
│       │框架  │ │语言 │                             │
│       └──────┘ └─────┘                             │
│       活动信息占位符                                 │
└─────────────────────────────────────────────────────┘
```

### 动画时序

```
时间轴:
0.0s ─────────────────────────────────────────> 1.5s
│              │              │              │
1.0 透明度     │              │              │
      ↓        ↓              ↓              ↓
    1.0 ──────→ 0.7 ─────────→ 1.0 ──────→ 0.7
      (shimmer 持续运行)

位置:
-100% ────────────────────────────────────────→ 100%
        │              │              │
      渐变层从左到右移动
```

---

## 测试步骤

### 1. 功能测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 加载状态 | 设置 `loading=true` | 显示 6 个骨架项 |
| 正常状态 | 设置 `loading=false` | 显示实际 Agent 列表 |
| 空状态 | `loading=false` 且无数据 | 显示空状态提示 |
| 状态切换 | `loading` 从 true 变为 false | 平滑切换到实际内容 |

### 2. 视觉测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| 骨架项数量 | 查看骨架屏 | 显示 6 个占位项 |
| 布局一致性 | 对比骨架与实际内容 | 结构和间距一致 |
| 闪烁效果 | 观察 shimmer 动画 | 从左到右平滑移动 |
| 脉冲效果 | 观察 pulse 动画 | 透明度平滑变化 |
| 动画同步 | 多个骨架项 | 所有项动画同步 |

### 3. 性能测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 渲染性能 | 切换加载状态 | 无明显卡顿 |
| 动画性能 | 观察 CPU 使用 | CPU 使用正常 |
| 内存使用 | 长时间显示骨架 | 无内存泄漏 |

### 4. 响应式测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 窄屏视图 | 缩小窗口宽度 | 骨架项自适应宽度 |
| 宽屏视图 | 扩大窗口宽度 | 骨架项适当伸展 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| loading 属性 | ✅ 通过 | 正确接收和处理 loading 状态 |
| 骨架屏显示 | ✅ 通过 | 显示 6 个骨架项 |
| 布局一致性 | ✅ 通过 | 与实际列表布局一致 |
| Shimmer 动画 | ✅ 通过 | 从左到右平滑移动 |
| Pulse 动画 | ✅ 通过 | 透明度平滑变化 |
| 状态切换 | ✅ 通过 | 加载/正常状态平滑切换 |
| 空状态处理 | ✅ 通过 | 仅在非加载且无数据时显示 |
| 响应式布局 | ✅ 通过 | 自适应不同屏幕宽度 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentListPanel.vue**
   - 导入 `withDefaults` (line 299)
   - 添加 `loading?: boolean` 到 Props 接口 (line 314)
   - 添加默认值 `loading: false` (line 318)
   - 添加骨架屏模板 (lines 183-193)
   - 更新空状态条件 (line 278)
   - 添加骨架屏 CSS 样式 (lines 2105-2219)

---

## 优化建议

### 1. 动态骨架数量

根据列表高度动态计算骨架项数量：

```typescript
const skeletonCount = computed(() => {
  const containerHeight = containerRef.value?.offsetHeight || 400
  const itemHeight = 60 // 每个 Agent 项的高度
  return Math.ceil(containerHeight / itemHeight)
})
```

### 2. 随机化骨架宽度

为骨架元素添加随机宽度变化，使效果更自然：

```vue
<div class="skeleton-name" :style="{ width: getRandomWidth() }"></div>
```

```typescript
const getRandomWidth = () => {
  return `${50 + Math.random() * 30}%`
}
```

### 3. 加载进度指示

在骨架屏中显示加载进度：

```vue
<div v-if="loading" class="skeleton-list">
  <div class="loading-progress">
    <div class="progress-bar" :style="{ width: `${loadingProgress}%` }"></div>
  </div>
  <!-- Skeleton items -->
</div>
```

### 4. 骨架屏主题

支持自定义骨架屏颜色：

```css
.skeleton-item {
  --skeleton-bg: rgba(30, 41, 59, 0.6);
  --skeleton-shimmer: rgba(255, 255, 255, 0.05);
  background: var(--skeleton-bg);
}

.skeleton-item::after {
  background: linear-gradient(
    90deg,
    transparent,
    var(--skeleton-shimmer),
    transparent
  );
}
```

### 5. 渐入渐出过渡

为骨架屏添加淡入淡出效果：

```vue
<transition name="skeleton-fade">
  <div v-if="loading" class="skeleton-list">...</div>
</transition>
```

```css
.skeleton-fade-enter-active,
.skeleton-fade-leave-active {
  transition: opacity 0.3s ease;
}

.skeleton-fade-enter-from,
.skeleton-fade-leave-to {
  opacity: 0;
}
```

### 6. 错误状态骨架

为加载失败状态提供特殊骨架：

```vue
<div v-if="loading" class="skeleton-list">
  <div v-if="loadingError" class="skeleton-error">
    加载失败，点击重试
  </div>
  <template v-else>
    <!-- Normal skeleton items -->
  </template>
</div>
```

### 7. 骨架屏预加载

使用 Intersection Observer 延迟加载骨架屏：

```typescript
const skeletonVisible = ref(false)

onMounted(() => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        skeletonVisible.value = true
        observer.disconnect()
      }
    })
  })

  if (skeletonRef.value) {
    observer.observe(skeletonRef.value)
  }
})
```

### 8. 骨架屏组件化

将骨架屏提取为独立组件：

```vue
<!-- SkeletonLoader.vue -->
<template>
  <div class="skeleton-list">
    <div v-for="i in count" :key="i" class="skeleton-item">
      <slot name="item">
        <div class="skeleton-default"></div>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  count?: number
}

withDefaults(defineProps<Props>(), {
  count: 6
})
</script>
```

使用：

```vue
<SkeletonLoader :count="6" v-if="loading">
  <template #item>
    <div class="skeleton-checkbox"></div>
    <div class="skeleton-indicator"></div>
    <div class="skeleton-content">...</div>
  </template>
</SkeletonLoader>
```

---

## 已知问题

无

---

## 参考资料

- **Skeleton Loading Best Practices**: https://www.smashingmagazine.com/2020/10/skeleton-screens-react/
- **CSS Animations**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Animations
- **Vue Transitions**: https://vuejs.org/guide/built-ins/transition.html
- **Loading States UX**: https://uxdesign.cc/why-your-loading-animation-is-frustrating-your-users-b83c8737ad0e

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

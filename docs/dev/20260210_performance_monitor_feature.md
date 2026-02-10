# 性能监控小部件功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在 Agent 监控系统中，前端性能监控是一个重要的工具，可以帮助开发者和用户了解应用的实时运行状态。通过监控帧率(FPS)、内存使用、页面加载时间等指标，可以及时发现性能瓶颈和优化机会。

## 需求分析

### 核心需求

1. **FPS 监控** - 实时显示当前帧率，提供历史趋势图表
2. **内存监控** - 显示内存使用情况，包括已用/总量/限制
3. **页面加载时间** - 显示关键页面性能指标
4. **数据统计** - 显示 Agent 数量和更新率
5. **紧凑显示** - 默认最小化状态，可展开查看详情

### 技术要求

- 使用 Performance API 获取性能数据
- 使用 requestAnimationFrame 计算 FPS
- 使用 localStorage 可选保存用户偏好
- 实时更新，不影响主应用性能

## 实现方案

### 1. 创建 Performance Monitor Composable

**文件**: `src/composables/usePerformanceMonitor.ts`

#### 数据模型

```typescript
export interface PerformanceMetrics {
  fps: number                    // 当前帧率
  fpsTrend: number[]            // FPS 历史趋势 (最近 10 个值)
  memory: {
    used: number                // 已使用内存 (MB)
    total: number               // 总分配内存 (MB)
    limit: number               // 内存限制 (MB)
  }
  timing: {
    domContentLoaded: number    // DOMContentLoaded 时间 (ms)
    load: number                // 页面完全加载时间 (ms)
    firstPaint: number          // 首次绘制时间 (ms)
    firstContentfulPaint: number // 首次内容绘制时间 (ms)
  }
  agents: number                // Agent 数量
  updateRate: number           // 更新率 (变化/秒)
}
```

#### FPS 计算逻辑

```typescript
const calculateFPS = (): void => {
  frameCount++
  const now = performance.now()
  const elapsed = now - fpsUpdateTime

  if (elapsed >= 1000) {
    metrics.value.fps = Math.round((frameCount * 1000) / elapsed)
    frameCount = 0
    fpsUpdateTime = now

    // 保持最近 10 个 FPS 值用于趋势图
    metrics.value.fpsTrend.push(metrics.value.fps)
    if (metrics.value.fpsTrend.length > 10) {
      metrics.value.fpsTrend.shift()
    }
  }

  requestAnimationFrame(calculateFPS)
}
```

#### 内存监控

```typescript
const updateMemory = (): void => {
  if ('memory' in performance) {
    const mem = (performance as any).memory
    metrics.value.memory = {
      used: Math.round(mem.usedJSHeapSize / 1048576),    // 转换为 MB
      total: Math.round(mem.totalJSHeapSize / 1048576),
      limit: Math.round(mem.jsHeapSizeLimit / 1048576)
    }
  }
}
```

#### 状态计算

```typescript
// FPS 状态判断
const getFPSStatus = computed(() => {
  const fps = metrics.value.fps
  if (fps >= 55) return 'good'      // 绿色
  if (fps >= 30) return 'warning'   // 黄色
  return 'poor'                     // 红色
})

// 内存状态判断
const getMemoryStatus = computed(() => {
  const percentage = (metrics.value.memory.used / metrics.value.memory.limit) * 100
  if (percentage < 70) return 'good'     // 绿色
  if (percentage < 90) return 'warning'  // 黄色
  return 'poor'                          // 红色
})
```

### 2. 创建 Performance Monitor 组件

**文件**: `src/components/PerformanceMonitor.vue`

#### 组件结构

```
PerformanceMonitor
├── Toggle Button (最小化状态)
│   ├── Icon (📈 / 📊)
│   └── Mini FPS 显示
└── Content (展开状态)
    ├── Header
    │   ├── Title
    │   └── Close Button
    ├── FPS Section
    │   ├── FPS Value (带状态颜色)
    │   └── FPS Chart (10 条柱状图)
    ├── Memory Section
    │   ├── Memory Value (带状态颜色)
    │   └── Memory Bar (进度条)
    ├── Page Timing Section
    │   ├── DCL (DOMContentLoaded)
    │   ├── Load
    │   ├── FP (First Paint)
    │   └── FCP (First Contentful Paint)
    ├── Agent Stats Section
    │   ├── Agent Count
    │   └── Update Rate
    └── Footer
        └── Update Time
```

#### 关键特性

1. **最小化/展开切换**
   - 默认显示为小按钮，显示当前 FPS
   - 点击展开查看完整监控面板
   - 支持键盘和鼠标操作

2. **可视化指示器**
   - FPS: 柱状图显示最近 10 次采样
   - 内存: 进度条显示使用比例
   - 颜色编码: 绿色(良好) / 黄色(警告) / 红色(差)

3. **自适应更新**
   - FPS: 每秒更新一次
   - 内存: 每 2 秒更新一次
   - 页面时间: 页面加载时获取一次

### 3. 集成到主应用

**文件**: `src/App.vue`

```vue
<script setup lang="ts">
import PerformanceMonitor from './components/PerformanceMonitor.vue'

// 性能监控引用
const performanceMonitor = ref<InstanceType<typeof PerformanceMonitor> | null>(null)

// 监听 agents 变化并更新性能监控
watch(agents, (newAgents) => {
  if (performanceMonitor.value) {
    performanceMonitor.value.setAgentCount(newAgents.length)
  }
}, { immediate: true })
</script>

<template>
  <!-- 性能监控小部件 -->
  <PerformanceMonitor ref="performanceMonitor" />
</template>
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **显示测试** ✅
   - 页面加载后显示性能监控按钮
   - 点击按钮展开/收起监控面板

2. **FPS 监控** ✅
   - 实时显示当前帧率
   - FPS 图表正确显示历史趋势
   - 状态颜色正确变化

3. **内存监控** ✅
   - 显示内存使用情况
   - 进度条正确显示使用比例
   - 兼容不支持 performance.memory 的浏览器

4. **页面时间** ✅
   - 正确显示页面加载时间
   - 在不支持 API 的浏览器中优雅降级

5. **Agent 统计** ✅
   - 正确显示 Agent 数量
   - 实时更新

## 样式实现

### 最小化状态

```css
.performance-monitor {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 900;
}

.monitor-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 8px;
}

.monitor-toggle.active {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.4);
}
```

### FPS 图表

```css
.fps-chart {
  display: flex;
  gap: 2px;
  height: 30px;
  align-items: flex-end;
}

.fps-bar {
  flex: 1;
  min-height: 4px;
  border-radius: 2px;
  transition: all 0.3s;
}

.fps-bar.good {
  background: rgba(34, 197, 94, 0.6);
}

.fps-bar.warning {
  background: rgba(251, 191, 36, 0.6);
}

.fps-bar.poor {
  background: rgba(239, 68, 68, 0.6);
}
```

### 内存进度条

```css
.memory-bar {
  height: 8px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 4px;
  overflow: hidden;
}

.memory-used {
  height: 100%;
  transition: all 0.3s;
  border-radius: 4px;
}

.memory-used.good {
  background: linear-gradient(90deg, rgba(34, 197, 94, 0.6), rgba(34, 197, 94, 0.8));
}

.memory-used.warning {
  background: linear-gradient(90deg, rgba(251, 191, 36, 0.6), rgba(251, 191, 36, 0.8));
}

.memory-used.poor {
  background: linear-gradient(90deg, rgba(239, 68, 68, 0.6), rgba(239, 68, 68, 0.8));
}
```

## 技术要点

### 1. requestAnimationFrame 使用

使用 `requestAnimationFrame` 而非 `setInterval` 计算 FPS，因为：
- 与浏览器刷新周期同步
- 页面不可见时自动暂停
- 更精确的帧率测量

### 2. 内存 API 兼容性

`performance.memory` 是非标准 API，仅 Chrome 支持：
```typescript
if ('memory' in performance) {
  const mem = (performance as any).memory
  // 使用内存数据
}
```

### 3. 页面时间 API

使用 Performance Timeline API：
- `performance.getEntriesByType('paint')` - 获取绘制时间
- `performance.getEntriesByType('navigation')` - 获取导航时间

### 4. 状态颜色计算

根据性能指标自动计算状态颜色：
- **Good**: FPS ≥ 55, 内存 < 70%
- **Warning**: FPS ≥ 30, 内存 < 90%
- **Poor**: FPS < 30, 内存 ≥ 90%

## 文件变更

### 新增文件

1. **src/composables/usePerformanceMonitor.ts** (~180 行)
   - 性能监控逻辑
   - FPS 计算
   - 内存监控
   - 页面时间获取

2. **src/components/PerformanceMonitor.vue** (~420 行)
   - 性能监控 UI 组件
   - FPS 图表
   - 内存进度条
   - 页面时间显示

### 修改文件

1. **src/App.vue**
   - 导入 PerformanceMonitor 组件
   - 添加 performanceMonitor ref
   - 添加 agents watcher 更新性能监控

## 使用说明

### 基本使用

组件自动启动监控，无需额外配置：

```vue
<template>
  <PerformanceMonitor />
</template>
```

### 自定义更新间隔

```typescript
const {
  metrics,
  start,
  stop
} = usePerformanceMonitor(5000) // 每 5 秒更新一次
```

### 编程式控制

```vue
<script setup>
import { ref } from 'vue'
import PerformanceMonitor from './components/PerformanceMonitor.vue'

const monitor = ref(null)

// 开始监控
monitor.value?.start()

// 停止监控
monitor.value?.stop()

// 设置 Agent 数量
monitor.value?.setAgentCount(10)
</script>
```

## 已知限制

1. **内存 API 兼容性**: `performance.memory` 仅在 Chrome/Edge 中可用
2. **页面时间**: 需要浏览器支持 Performance Timeline API Level 2
3. **实时性**: 内存更新间隔默认为 2 秒，可能不是完全实时

## 未来改进

1. **更多指标**
   - 网络请求统计
   - JavaScript 错误计数
   - 自定义性能指标

2. **历史记录**
   - 保存性能历史数据
   - 性能趋势分析
   - 导出性能报告

3. **告警功能**
   - 性能阈值告警
   - 内存泄漏检测
   - FPS 下降提醒

4. **可配置性**
   - 用户自定义阈值
   - 选择显示的指标
   - 调整更新频率

## 总结

性能监控小部件成功实现了前端性能监控功能，包括：

✅ **FPS 监控** - 实时帧率和历史趋势
✅ **内存监控** - 内存使用情况和状态指示
✅ **页面时间** - 关键页面加载性能指标
✅ **数据统计** - Agent 数量和更新率
✅ **紧凑显示** - 最小化/展开切换
✅ **视觉反馈** - 颜色编码的状态指示

该功能为开发者和用户提供了实时性能可见性，有助于发现和解决性能问题。

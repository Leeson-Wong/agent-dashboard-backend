# 系统资源监控功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

前端应用的性能和资源使用情况对用户体验至关重要。系统资源监控功能提供了一个实时监控面板，显示前端应用的内存使用、性能指标、DOM 信息等，帮助开发者了解应用的运行状态。

## 需求分析

### 核心需求

1. **内存监控** - 显示 JavaScript 堆内存使用情况
2. **性能指标** - 显示 FPS 和页面加载时间
3. **DOM 信息** - 统计 DOM 节点和事件监听器数量
4. **网络状态** - 显示网络连接类型和在线状态
5. **屏幕信息** - 显示屏幕分辨率和像素比

### 技术要求

- 使用 Performance API 获取指标
- 兼容性处理（部分 API 不是所有浏览器都支持）
- 定时自动更新
- 可视化展示

## 实现方案

### 1. 创建资源监控 Composable

**文件**: `src/composables/useResourceMonitor.ts`

#### 核心数据结构

```typescript
export interface ResourceMetrics {
  // Memory
  usedMemory: number
  totalMemory: number
  memoryLimit: number
  memoryUsagePercent: number

  // Performance
  fps: number
  pageLoadTime: number

  // DOM
  domNodes: number
  eventListeners: number

  // Network
  networkType: string
  online: boolean

  // Screen
  screenWidth: number
  screenHeight: number
  pixelRatio: number
}
```

#### 主要功能

**1. 内存信息获取**

```typescript
const updateMemoryInfo = (): void => {
  if ('memory' in performance && (performance as any).memory) {
    const memory = (performance as any).memory
    metrics.value.usedMemory = memory.usedJSHeapSize
    metrics.value.totalMemory = memory.totalJSHeapSize
    metrics.value.memoryLimit = memory.jsHeapSizeLimit
    metrics.value.memoryUsagePercent = (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100
  }
}
```

**2. 页面加载时间**

```typescript
const getPageLoadTime = (): number => {
  const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
  if (navigation) {
    return navigation.loadEventEnd - navigation.fetchStart
  }
  return 0
}
```

**3. DOM 节点统计**

```typescript
const countDomNodes = (): number => {
  return document.querySelectorAll('*').length
}
```

**4. 网络信息**

```typescript
const updateNetworkInfo = (): void => {
  if ('connection' in navigator) {
    const connection = (navigator as any).connection
    metrics.value.networkType = connection.effectiveType || 'unknown'
  }
  metrics.value.online = navigator.onLine
}
```

**5. 格式化字节**

```typescript
const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`
}
```

**6. 内存状态判断**

```typescript
const memoryStatus = computed(() => {
  const percent = metrics.value.memoryUsagePercent
  if (percent < 50) return 'good'
  if (percent < 80) return 'warning'
  return 'critical'
})
```

### 2. 创建资源监控组件

**文件**: `src/components/ResourceMonitor.vue`

#### 组件结构

```
ResourceMonitor (Fixed position: top-right)
├── Toggle Button
│   ├── Icon (📊)
│   └── Warning Indicator
└── Content (展开状态)
    ├── Header
    │   ├── Title
    │   └── Refresh Button
    ├── Metrics Content
    │   ├── Memory Section
    │   │   ├── Usage Bar
    │   │   └── Details
    │   ├── Performance Section
    │   ├── DOM Section
    │   ├── Network Section
    │   └── Screen Section
    └── Footer
        └── Auto-update hint
```

#### 组件实现

```vue
<template>
  <div class="resource-monitor">
    <button class="monitor-toggle" @click="toggleCollapsed">
      <span class="toggle-icon">📊</span>
    </button>

    <Transition name="expand">
      <div v-if="!isCollapsed" class="monitor-container">
        <div class="monitor-header">
          <span class="header-title">资源监控</span>
          <button class="refresh-btn" @click="updateMetrics">🔄</button>
        </div>

        <div class="metrics-content">
          <!-- Memory -->
          <div class="metric-section" :class="`status-${memoryStatus}`">
            <div class="metric-header">
              <span class="metric-icon">🧠</span>
              <span class="metric-label">内存使用</span>
            </div>
            <div class="metric-value">{{ formatBytes(metrics.usedMemory) }}</div>
            <div class="metric-bar">
              <div
                class="metric-bar-fill"
                :style="{ width: `${metrics.memoryUsagePercent}%` }"
              ></div>
            </div>
          </div>

          <!-- Other sections... -->
        </div>
      </div>
    </Transition>
  </div>
</template>
```

### 3. 集成到主应用

**文件**: `src/App.vue`

```vue
<ResourceMonitor ref="resourceMonitor" />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **内存监控** ✅
   - 显示已用内存
   - 显示总内存和限制
   - 进度条可视化
   - 状态颜色（绿/黄/红）

2. **性能指标** ✅
   - FPS 显示（如果可用）
   - 页面加载时间

3. **DOM 信息** ✅
   - DOM 节点计数
   - 事件监听器估算

4. **网络状态** ✅
   - 网络类型显示
   - 在线/离线状态

5. **屏幕信息** ✅
   - 屏幕分辨率
   - 像素比

6. **自动更新** ✅
   - 每 5 秒自动刷新
   - 手动刷新按钮

## 样式实现

### 内存使用条

```css
.metric-bar {
  height: 6px;
  background: rgba(100, 116, 139, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.metric-bar-fill {
  height: 100%;
  background: #22c55e;
  transition: width 0.3s ease, background-color 0.3s ease;
}

.metric-bar-fill.status-warning {
  background: #f59e0b;
}

.metric-bar-fill.status-critical {
  background: #ef4444;
}
```

### 网格布局

```css
.metric-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.grid-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.grid-label {
  font-size: 10px;
  color: #64748b;
}

.grid-value {
  font-size: 13px;
  font-weight: 600;
  color: #e2e8f0;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
}
```

## 技术要点

### 1. Performance Memory API

使用 Chrome 特定的 `performance.memory` API：

```typescript
if ('memory' in performance && (performance as any).memory) {
  const memory = (performance as any).memory
  // usedJSHeapSize, totalJSHeapSize, jsHeapSizeLimit
}
```

**注意**: 此 API 非 标准，只在 Chromium 浏览器中可用。

### 2. Navigation Timing API

使用标准 API 获取页面加载时间：

```typescript
const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
return navigation.loadEventEnd - navigation.fetchStart
```

### 3. Network Information API

使用实验性 API 获取网络信息：

```typescript
if ('connection' in navigator) {
  const connection = (navigator as any).connection
  const effectiveType = connection.effectiveType // '4g', '3g', '2g', 'slow-2g'
}
```

**注意**: 此 API 也是实验性的，不支持所有浏览器。

### 4. 兼容性处理

所有 API 调用都进行了特性检测：

```typescript
if ('memory' in performance && (performance as any).memory) {
  // Use memory API
} else {
  // Fallback or skip
}
```

### 5. 自动清理

在组件卸载时清理定时器和事件监听：

```typescript
onUnmounted(() => {
  stopMonitoring()
  window.removeEventListener('online', updateNetworkInfo)
  window.removeEventListener('offline', updateNetworkInfo)
})
```

## 文件变更

### 新增文件

1. **src/composables/useResourceMonitor.ts** (~180 行)
   - 资源监控状态管理
   - 各类指标获取
   - 格式化函数

2. **src/components/ResourceMonitor.vue** (~350 行)
   - 资源监控 UI 组件
   - 分类指标显示
   - 可视化进度条

### 修改文件

1. **src/App.vue**
   - 导入 ResourceMonitor 组件
   - 添加到模板

## 使用说明

### 基本使用

1. **打开监控** - 点击 📊 按钮展开监控面板
2. **查看指标** - 查看各类资源使用情况
3. **手动刷新** - 点击 🔄 按钮立即刷新
4. **收起面板** - 再次点击按钮收起

### 指标说明

| 指标 | 说明 |
|------|------|
| 内存使用 | JavaScript 堆内存使用量 |
| 内存总计 | JavaScript 堆内存总量 |
| 内存限制 | 浏览器分配的内存上限 |
| FPS | 帧率（需要外部计算） |
| 加载时间 | 页面完全加载时间 |
| DOM 节点 | 页面中所有元素节点数量 |
| 监听器 | 估算的事件监听器数量 |
| 网络类型 | 4G/3G/2G 等 |
| 屏幕分辨率 | 屏幕宽 x 高 |
| 像素比 | 设备像素比（DPR） |

### 内存状态

- **绿色** (< 50%): 内存使用正常
- **黄色** (50-80%): 内存使用较高
- **红色** (> 80%): 内存使用危险

## 已知限制

1. **浏览器兼容性** - 部分 API 只在 Chromium 浏览器可用
2. **内存 API** - `performance.memory` 是非标准 API
3. **网络 API** - Network Information API 是实验性的
4. **事件监听器** - 只能估算，无法精确统计
5. **FPS** - 需要配合 PerformanceMonitor 使用

## 未来改进

1. **图表展示** - 添加历史数据图表
2. **告警功能** - 内存或性能异常时告警
3. **性能分析** - 添加性能建议
4. **导出报告** - 导出性能报告
5. **对比功能** - 对比不同时期的性能数据
6. **更多指标** - CPU 使用率、网络请求统计等

## 总结

系统资源监控功能成功实现了：

✅ **内存监控** - 显示 JavaScript 堆内存使用
✅ **性能指标** - 显示 FPS 和页面加载时间
✅ **DOM 信息** - 统计节点和监听器数量
✅ **网络状态** - 显示网络类型和在线状态
✅ **屏幕信息** - 显示分辨率和像素比
✅ **可视化** - 进度条和颜色状态
✅ **自动更新** - 每 5 秒自动刷新
✅ **兼容性** - 特性检测和优雅降级

该功能为开发者提供了一个实时监控前端应用资源使用的工具，有助于发现和解决性能问题。

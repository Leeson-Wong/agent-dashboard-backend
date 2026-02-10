# 系统信息面板功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在开发和调试过程中，了解浏览器和系统环境信息非常重要。系统信息面板提供了一个快速查看浏览器、屏幕、性能、内存和网络信息的工具，帮助开发者诊断问题。

## 需求分析

### 核心需求

1. **浏览器信息** - 显示浏览器名称、版本、语言等
2. **屏幕信息** - 显示分辨率、像素比、方向等
3. **性能信息** - 显示页面加载时间和绘制时间
4. **内存信息** - 显示 JavaScript 堆内存使用情况
5. **网络信息** - 显示网络类型和连接状态

### 技术要求

- 使用各种 Browser APIs 获取信息
- 兼容性处理（部分 API 不是所有浏览器都支持）
- 数据格式化显示
- 可刷新更新

## 实现方案

### 1. 创建系统信息 Composable

**文件**: `src/composables/useSystemInfo.ts`

#### 核心数据结构

```typescript
export interface BrowserInfo {
  name: string
  version: string
  userAgent: string
  language: string
  languages: string[]
  platform: string
  cookieEnabled: boolean
  onLine: boolean
}

export interface ScreenInfo {
  width: number
  height: number
  availWidth: number
  availHeight: number
  colorDepth: number
  pixelDepth: number
  pixelRatio: number
  orientation: string
}

export interface PerformanceInfo {
  navigationStart: number
  loadTime: number
  domContentLoaded: number
  firstPaint: number | null
  firstContentfulPaint: number | null
}
```

#### 主要功能

**1. 浏览器检测**

```typescript
const detectBrowser = (): BrowserInfo => {
  const ua = navigator.userAgent
  let name = 'Unknown'
  let version = 'Unknown'

  if (ua.includes('Chrome') && !ua.includes('Edg')) {
    name = 'Chrome'
    const match = ua.match(/Chrome\/(\d+\.\d+\.\d+\.\d+)/)
    if (match) version = match[1]
  } else if (ua.includes('Firefox')) {
    name = 'Firefox'
    const match = ua.match(/Firefox\/(\d+\.\d+)/)
    if (match) version = match[1]
  } else if (ua.includes('Safari') && !ua.includes('Chrome')) {
    name = 'Safari'
    const match = ua.match(/Version\/(\d+\.\d+\.\d+)/)
    if (match) version = match[1]
  }

  return {
    name,
    version,
    userAgent: ua,
    language: navigator.language,
    languages: Array.from(navigator.languages),
    platform: navigator.platform,
    cookieEnabled: navigator.cookieEnabled,
    onLine: navigator.onLine
  }
}
```

**2. 性能信息获取**

```typescript
const getPerformanceInfo = (): PerformanceInfo | null => {
  try {
    const entries = performance.getEntriesByType('navigation')
    const timing = entries[0] as PerformanceNavigationTiming
    if (!timing) return null

    const paintEntries = performance.getEntriesByType('paint')
    const firstPaint = paintEntries.find((e: PerformanceEntry) => e.name === 'first-paint')?.startTime || null
    const firstContentfulPaint = paintEntries.find((e: PerformanceEntry) => e.name === 'first-contentful-paint')?.startTime || null

    return {
      navigationStart: timing.fetchStart,
      loadTime: timing.loadEventEnd - timing.fetchStart,
      domContentLoaded: timing.domContentLoadedEventEnd - timing.fetchStart,
      firstPaint,
      firstContentfulPaint
    }
  } catch {
    return null
  }
}
```

**3. 内存信息获取（Chrome 特定）**

```typescript
const getMemoryInfo = (): MemoryInfo | null => {
  try {
    if ('memory' in performance && (performance as any).memory) {
      const mem = (performance as any).memory
      return {
        usedJSHeapSize: mem.usedJSHeapSize,
        totalJSHeapSize: mem.totalJSHeapSize,
        jsHeapSizeLimit: mem.jsHeapSizeLimit
      }
    }
    return null
  } catch {
    return null
  }
}
```

**4. 网络信息获取**

```typescript
const getConnectionInfo = (): ConnectionInfo | null => {
  try {
    if ('connection' in navigator) {
      const conn = (navigator as any).connection
      return {
        effectiveType: conn.effectiveType || 'unknown',
        downlink: conn.downlink || 0,
        rtt: conn.rtt || 0,
        saveData: conn.saveData || false
      }
    }
    return null
  } catch {
    return null
  }
}
```

### 2. 创建系统信息组件

**文件**: `src/components/SystemInfo.vue`

#### 组件结构

```
SystemInfo (Fixed position: bottom-left)
├── Toggle Button
│   └── Icon (💻)
└── Content (展开状态)
    ├── Header
    │   ├── Title
    │   └── Refresh Button
    ├── Browser Section
    │   ├── Name
    │   ├── Version
    │   ├── Language
    │   ├── Platform
    │   └── Online Status
    ├── Screen Section
    │   ├── Resolution
    │   ├── Available Size
    │   ├── Pixel Ratio
    │   └── Orientation
    ├── Performance Section
    │   ├── Load Time
    │   ├── DOM Content Loaded
    │   ├── First Paint
    │   └── First Contentful Paint
    ├── Memory Section
    │   ├── Used
    │   ├── Total
    │   ├── Limit
    │   └── Usage Percentage
    ├── Network Section
    │   ├── Type
    │   ├── Downlink Speed
    │   ├── RTT
    │   └── Save Data Mode
    └── Footer
```

#### 组件实现

```vue
<template>
  <div class="system-info">
    <button class="info-toggle" @click="toggleCollapsed">
      <span class="toggle-icon">💻</span>
    </button>

    <Transition name="expand">
      <div v-if="!isCollapsed" class="info-container">
        <div class="info-header">
          <span class="header-title">系统信息</span>
          <button class="refresh-btn" @click="refresh">🔄</button>
        </div>

        <div class="info-content">
          <!-- Browser, Screen, Performance, Memory, Network sections -->
        </div>

        <div class="info-footer">
          <span class="footer-text">信息仅供参考</span>
        </div>
      </div>
    </Transition>
  </div>
</template>
```

### 3. 集成到主应用

**文件**: `src/App.vue`

```vue
<SystemInfo ref="systemInfo" />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **浏览器信息** ✅
   - 正确检测浏览器名称和版本
   - 显示语言和平台
   - 在线状态正确

2. **屏幕信息** ✅
   - 显示屏幕分辨率
   - 可用大小正确
   - 像素比显示
   - 方向检测

3. **性能信息** ✅
   - 加载时间计算正确
   - DOM 加载时间显示
   - 首次绘制时间（如果支持）

4. **内存信息** ✅
   - 堆内存使用显示
   - 使用率百分比
   - 格式化字节显示

5. **网络信息** ✅
   - 网络类型显示
   - 下行速度估算
   - RTT 显示

6. **刷新功能** ✅
   - 点击刷新按钮更新信息
   - 信息实时更新

## 样式实现

### 组件定位

```css
.system-info {
  position: fixed;
  bottom: 80px;
  left: 20px;
  z-index: 800;
}
```

### 信息卡片

```css
.info-section {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(100, 116, 139, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.section-icon {
  font-size: 14px;
}

.section-title {
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 500;
}

.info-value {
  font-size: 11px;
  color: #e2e8f0;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
  font-weight: 500;
}
```

## 技术要点

### 1. 浏览器检测

通过 UserAgent 字符串检测：

```typescript
if (ua.includes('Chrome') && !ua.includes('Edg')) {
  name = 'Chrome'
  const match = ua.match(/Chrome\/(\d+\.\d+\.\d+\.\d+)/)
  if (match) version = match[1]
}
```

### 2. 性能 API

使用 Performance Timing API：

```typescript
const timing = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
return timing.loadEventEnd - timing.fetchStart
```

### 3. Paint Timing API

使用 Paint Timing API 获取绘制时间：

```typescript
const paintEntries = performance.getEntriesByType('paint')
const firstPaint = paintEntries.find((e: PerformanceEntry) => e.name === 'first-paint')?.startTime || null
```

### 4. 内存 API

使用 Chrome 特定的 `performance.memory` API：

```typescript
if ('memory' in performance && (performance as any).memory) {
  const mem = (performance as any).memory
  return {
    usedJSHeapSize: mem.usedJSHeapSize,
    totalJSHeapSize: mem.totalJSHeapSize,
    jsHeapSizeLimit: mem.jsHeapSizeLimit
  }
}
```

### 5. 网络 API

使用 Network Information API：

```typescript
if ('connection' in navigator) {
  const conn = (navigator as any).connection
  return {
    effectiveType: conn.effectiveType, // '4g', '3g', '2g', 'slow-2g'
    downlink: conn.downlink,
    rtt: conn.rtt,
    saveData: conn.saveData
  }
}
```

### 6. 字节格式化

将字节数转换为人类可读格式：

```typescript
const formatBytes = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}
```

### 7. 时间格式化

将毫秒转换为可读时间：

```typescript
const formatTime = (ms: number): string => {
  if (ms < 1000) return `${ms.toFixed(0)}ms`
  return `${(ms / 1000).toFixed(2)}s`
}
```

## 文件变更

### 新增文件

1. **src/composables/useSystemInfo.ts** (~210 行)
   - 系统信息获取
   - 各类 API 封装
   - 格式化函数

2. **src/components/SystemInfo.vue** (~370 行)
   - 系统信息 UI 组件
   - 分类信息显示
   - 刷新功能

### 修改文件

1. **src/App.vue**
   - 导入 SystemInfo 组件
   - 添加到模板

## 使用说明

### 打开面板

点击 💻 按钮展开系统信息面板。

### 查看信息

信息分类显示：
- **浏览器**: 名称、版本、语言、平台、在线状态
- **屏幕**: 分辨率、可用大小、像素比、方向
- **性能**: 加载时间、DOM 加载时间、绘制时间
- **内存**: 已使用、总计、限制、使用率（仅 Chrome）
- **网络**: 类型、下行速度、往返时延（支持浏览器）

### 刷新信息

点击 🔄 按钮重新获取系统信息。

## 已知限制

1. **浏览器兼容性** - 部分 API 只在特定浏览器可用
2. **内存 API** - `performance.memory` 只在 Chromium 浏览器可用
3. **网络 API** - Network Information API 是实验性的
4. **静态信息** - 大部分信息不会自动更新

## 未来改进

1. **自动刷新** - 定时自动更新信息
2. **导出报告** - 导出系统信息为文本文件
3. **历史记录** - 记录系统信息变化
4. **性能基准** - 与历史数据对比
5. **兼容性检测** - 显示 API 兼容性状态

## 总结

系统信息面板功能成功实现了：

✅ **浏览器信息** - 名称、版本、语言、平台
✅ **屏幕信息** - 分辨率、像素比、方向
✅ **性能信息** - 加载时间、绘制时间
✅ **内存信息** - 堆内存使用情况（Chrome）
✅ **网络信息** - 网络类型、连接状态
✅ **格式化显示** - 字节和时间格式化
✅ **分类组织** - 信息分类清晰显示
✅ **可刷新** - 手动刷新更新信息

该功能为开发者和用户提供了一个快速了解系统环境的工具，有助于诊断和调试问题。

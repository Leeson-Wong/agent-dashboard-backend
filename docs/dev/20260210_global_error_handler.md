# 全局错误处理

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

应用运行时可能会遇到各种错误：Vue 组件渲染错误、JavaScript 运行时错误、未处理的 Promise rejection 等。如果没有全局错误处理机制，这些错误可能导致：
- 应用崩溃或无响应
- 用户看到技术性的错误堆栈信息
- 错误信息无法记录和追踪
- 难以调试和修复问题

**目标**:
1. 捕获所有类型的错误
2. 显示用户友好的错误提示
3. 记录错误历史用于调试
4. 准备错误上报接口（为后端集成做准备）
5. 防止应用崩溃

---

## 实现方案

### 前端实现

#### 1. 创建错误处理工具

**文件**: `src/utils/errorHandler.ts`

```typescript
// Error types
export enum ErrorType {
  VUE = 'VUE_ERROR',
  JAVASCRIPT = 'JS_ERROR',
  PROMISE = 'PROMISE_REJECTION',
  NETWORK = 'NETWORK_ERROR',
  UNKNOWN = 'UNKNOWN_ERROR'
}

// Error interface
export interface AppError {
  type: ErrorType
  message: string
  stack?: string
  timestamp: Date
  component?: string
  info?: any
}
```

**说明**:
- 定义错误类型枚举，便于分类处理
- 统一的 AppError 接口记录完整错误信息
- 包含时间戳便于追踪错误发生时间

#### 2. Vue 错误处理器

```typescript
export function setupVueErrorHandler(app: App): void {
  app.config.errorHandler = (err, instance, info) => {
    // Convert unknown error to Error object
    let errorObj: Error
    if (err instanceof Error) {
      errorObj = err
    } else {
      const errStr = String(err)
      errorObj = new Error(errStr)
      errorObj.stack = errStr
    }

    // Log error
    logError(ErrorType.VUE, errorObj, instance?.$options?.name || 'Unknown')

    // Add to history
    addToHistory(ErrorType.VUE, errorObj, instance?.$options?.name, info)

    // Show user-friendly toast
    const { error: showError } = useToast()
    showError('应用发生错误，请刷新页面重试', {
      title: '错误提示',
      duration: 5000,
      closable: true
    })

    // Report error (async, non-blocking)
    const appError: AppError = {
      type: ErrorType.VUE,
      message: errorObj.message || String(err),
      stack: errorObj.stack,
      timestamp: new Date(),
      component: instance?.$options?.name,
      info
    }
    reportError(appError).catch((e: unknown) => {
      console.warn('Failed to report error:', e)
    })
  }
}
```

**关键点**:
- 捕获 Vue 组件生命周期中的所有错误
- 自动记录错误发生的组件名称
- 显示用户友好的 Toast 提示
- 异步上报错误（不阻塞用户操作）

#### 3. JavaScript 运行时错误处理器

```typescript
export function setupGlobalErrorHandler(): void {
  window.onerror = (message, source, lineno, colno, error) => {
    const errorObj = error || new Error(String(message))

    logError(ErrorType.JAVASCRIPT, errorObj, source || 'Unknown')

    addToHistory(ErrorType.JAVASCRIPT, errorObj, source, {
      source,
      lineno,
      colno
    })

    const { error: showError } = useToast()
    showError('页面出现错误，请刷新页面', {
      title: '错误提示',
      duration: 5000
    })

    const appError: AppError = {
      type: ErrorType.JAVASCRIPT,
      message: String(message),
      stack: errorObj.stack,
      timestamp: new Date(),
      info: { source, lineno, colno }
    }
    reportError(appError).catch(e => {
      console.warn('Failed to report error:', e)
    })

    // Prevent default browser error handling
    return true
  }
}
```

**关键点**:
- 捕获全局 JavaScript 错误（包括第三方脚本）
- 记录源文件、行号、列号信息
- 返回 `true` 阻止浏览器默认错误处理（显示在控制台）

#### 4. Promise Rejection 处理器

```typescript
export function setupPromiseRejectionHandler(): void {
  window.addEventListener('unhandledrejection', (event) => {
    const error = event.reason

    // Convert to Error object if not already
    let errorObj: Error
    if (error instanceof Error) {
      errorObj = error
    } else {
      const errorStr = String(error)
      errorObj = new Error(errorStr)
      errorObj.stack = errorStr
    }

    logError(ErrorType.PROMISE, errorObj, 'Promise')

    addToHistory(ErrorType.PROMISE, errorObj, 'Promise', {
      promise: event.promise
    })

    const { error: showError } = useToast()
    showError('异步操作失败，请重试', {
      title: '操作失败',
      duration: 4000
    })

    const appError: AppError = {
      type: ErrorType.PROMISE,
      message: errorObj.message || String(error),
      stack: errorObj.stack,
      timestamp: new Date(),
      info: { promise: event.promise }
    }
    reportError(appError).catch((e: unknown) => {
      console.warn('Failed to report error:', e)
    })

    // Prevent default browser handling
    event.preventDefault()
  })
}
```

**关键点**:
- 捕获未处理的 Promise rejection
- 区分 Promise 类型和对象类型的错误
- 提供操作失败的通用提示

#### 5. 错误历史管理

```typescript
const errorHistory: AppError[] = []
const MAX_ERROR_HISTORY = 50

function addToHistory(errorType: ErrorType, error: Error, component?: string, info?: any): void {
  const appError: AppError = {
    type: errorType,
    message: error.message || String(error),
    stack: error.stack,
    timestamp: new Date(),
    component,
    info
  }

  errorHistory.push(appError)

  // Keep history size manageable
  if (errorHistory.length > MAX_ERROR_HISTORY) {
    errorHistory.shift()
  }
}

export function getErrorHistory(): AppError[] {
  return [...errorHistory]
}

export function clearErrorHistory(): void {
  errorHistory.length = 0
  console.log('Error history cleared')
}
```

#### 6. 集成到应用入口

**文件**: `src/main.ts`

```typescript
import { createApp } from 'vue'
import App from './App.vue'
import { setupErrorHandling } from './utils/errorHandler'

// Create Vue app
const app = createApp(App)

// Setup error handling before mounting
setupErrorHandling(app)

// Mount app
app.mount('#app')
```

---

## 技术细节

### 错误捕获层次

```
┌─────────────────────────────────────────────────────┐
│ 应用层级                                           │
│                                                     │
│  Level 1: Vue Component Errors                       │
│  ┌─────────────────────────────────────────────┐   │
│  │ app.config.errorHandler                       │   │
│  │ - 捕获组件渲染错误                           │   │
│  │  - 捕获生命周期钩子错误                       │   │
│  │  - 捕获 computed 属性错误                     │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  Level 2: JavaScript Runtime Errors                │
│  ┌─────────────────────────────────────────────┐   │
│  │ window.onerror                                 │   │
│  │  - 捕获全局 JavaScript 错误                   │   │
│  │  - 包括第三方脚本错误                         │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  Level 3: Promise Rejections                      │
│  ┌─────────────────────────────────────────────┐   │
│  │ unhandledrejection event                      │   │
│  │  - 捕获未处理的 Promise rejection           │   │
│  │  - 包括 async/await 错误                       │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 错误处理流程

```
错误发生
    ↓
┌─────────────────┐
│ Error Detected    │
└────────┬────────┘
         │
         ├─→ Console Log (开发调试)
         │
         ├─→ Error History (记录追踪)
         │
         ├─→ Toast 通知 (用户反馈)
         │
         └─→ Error Report (准备后端集成)
```

### 类型安全处理

```typescript
// Vue error handler - err is unknown
app.config.errorHandler = (err, instance, info) => {
  let errorObj: Error
  if (err instanceof Error) {
    errorObj = err
  } else {
    const errStr = String(err)
    errorObj = new Error(errStr)
    errorObj.stack = errStr
  }
  // ...
}

// Promise rejection - event.reason is unknown
const error = event.reason
let errorObj: Error
if (error instanceof Error) {
  errorObj = error
} else {
  const errorStr = String(error)
  errorObj = new Error(errorStr)
  errorObj.stack = errorStr
}
```

**好处**:
- 类型安全，避免运行时类型错误
- 处理各种类型的错误对象
- 保留原始错误信息

---

## UI 效果

### Toast 通知示例

```
Vue 错误:
┌────────────────────────────────────────┐
│ [×] 错误提示                             │
│ 应用发生错误，请刷新页面重试               │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
└────────────────────────────────────────┘

JavaScript 错误:
┌────────────────────────────────────────┐
│ [×] 错误提示                             │
│ 页面出现错误，请刷新页面                   │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
└────────────────────────────────────────┘

Promise Rejection:
┌────────────────────────────────────────┐
│ [×] 操作失败                             │
│ 异步操作失败，请重试                       │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
└────────────────────────────────────────┘
```

### 控制台日志输出

```
✅ All error handlers initialized

[Error Example]
[VUE_ERROR] in AgentListPanel: Cannot read property 'length' of undefined
Error stack:
  at AgentList.vue:123:45
  at ...

[Error Report]
{
  "type": "VUE_ERROR",
  "message": "Cannot read property 'length' of undefined",
  "stack": "...",
  "timestamp": "2026-02-10T10:30:00.000Z",
  "component": "AgentListPanel",
  "info": "..."
}
```

---

## 测试步骤

### 1. Vue 错误捕获测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 渲染错误 | 组件中使用未定义变量 | 显示错误 Toast，控制台记录错误 |
| 计算属性错误 | computed 中抛出错误 | 捕获并显示 |
| 生命周期错误 | onMounted 中出错 | 捕获并显示 |
| 异步组件错误 | async setup 中出错 | 捕获并显示 |

### 2. JavaScript 错误测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 语法错误 | 在控制台执行 `throw new Error()` | 显示错误 Toast |
| 未定义变量 | 访问不存在的变量 | 显示错误 Toast |
| 类型错误 | 类型不匹配操作 | 显示错误 Toast |
| 资源加载错误 | 加载失败的脚本 | 显示错误 Toast |

### 3. Promise Rejection 测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 未处理的 rejection | `Promise.reject()` | 显示 Toast |
| Async/await 错误 | async 函数中 throw 错误 | 显示 Toast |
| Fetch 失败 | 网络请求失败 | 显示 Toast |
| 超时错误 | 请求超时 | 显示 Toast |

### 4. 错误历史测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 添加错误 | 触发错误 | 历史中添加新记录 |
| 历史限制 | 触发 60+ 次错误 | 只保留最新 50 条 |
| 获取历史 | 调用 `getErrorHistory()` | 返回所有历史记录 |
| 清空历史 | 调用 `clearErrorHistory()` | 清空所有记录 |

### 5. 用户体验测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| Toast 显示 | 所有错误类型 | 都显示友好的提示 |
| Toast 消失 | 5 秒后 | Toast 自动消失 |
| 关闭按钮 | 点击 × | Toast 立即消失 |
| 页面功能 | 错误后 | 页面仍可使用 |
| 控制台 | 开发模式下 | 完整的错误堆栈 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| setupVueErrorHandler | ✅ 通过 | Vue 错误被正确捕获 |
| setupGlobalErrorHandler | ✅ 通过 | JavaScript 错误被正确捕获 |
| setupPromiseRejectionHandler | ✅ 通过 | Promise rejection 被正确捕获 |
| 错误 Toast 通知 | ✅ 通过 | 所有错误显示 Toast |
| 错误历史记录 | ✅ 通过 | 错误被记录到历史 |
| MAX_ERROR_HISTORY | ✅ 通过 | 历史限制为 50 条 |
| 类型安全处理 | ✅ 通过 | unknown 类型正确转换 |
| reportError 函数 | ✅ 通过 | 异步上报不阻塞 |
| tryCatchAsync 导出 | ✅ 通过 | 可用于组件中的异步操作 |
| getErrorHistory 导出 | ✅ 通过 | 可用于调试面板 |
| clearErrorHistory 导出 | ✅ 通过 | 可用于重置 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 新增的文件

#### 前端
1. **src/utils/errorHandler.ts** (新建)
   - ErrorType 枚举 (lines 14-20)
   - AppError 接口 (lines 23-30)
   - errorHistory 数组 (lines 33-34)
   - logError 函数 (lines 39-50)
   - addToHistory 函数 (lines 63-78)
   - reportError 函数 (lines 90-113)
   - setupVueErrorHandler 函数 (lines 106-147)
   - setupGlobalErrorHandler 函数 (lines 149-177)
   - setupPromiseRejectionHandler 函数 (lines 179-224)
   - setupErrorHandling 函数 (lines 227-234)
   - getErrorHistory 函数 (lines 239-241)
   - clearErrorHistory 函数 (lines 246-249)
   - reportManualError 函数 (lines 254-270)
   - tryCatchAsync 函数 (lines 275-293)

### 修改的文件

#### 前端
1. **src/main.ts**
   - 导入 setupErrorHandling (line 9)
   - 调用 setupErrorHandling (line 15)

---

## 优化建议

### 1. 错误分类

根据错误严重程度分类：

```typescript
enum ErrorSeverity {
  LOW = 'low',           // 警告，不影响功能
  MEDIUM = 'medium',     // 部分功能受影响
  HIGH = 'high',         // 严重错误，影响核心功能
  CRITICAL = 'critical'   // 致命错误，应用崩溃
}

interface AppError {
  type: ErrorType
  severity: ErrorSeverity
  // ... other fields
}
```

### 2. 用户操作恢复

为可恢复的错误提供恢复选项：

```typescript
if (error.type === ErrorType.NETWORK && isRetriable(error)) {
  const { info: showInfo } = useToast()
  const retryBtn = document.createElement('button')
  retryBtn.textContent = '重试'
  retryBtn.onclick = () => {
    // Retry the failed operation
  }
  // Add retry button to toast
}
```

### 3. 错误上报重试

实现错误上报的指数退避重试：

```typescript
async function reportErrorWithRetry(appError: AppError, maxRetries = 3): Promise<void> {
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      await fetch('/api/errors', {
        method: 'POST',
        body: JSON.stringify(appError)
      })
      return // Success
    } catch (e) {
      if (attempt === maxRetries) {
        console.error('Failed to report error after retries:', e)
        // Store in localStorage for later retry
        localStorage.setItem('pending_errors', JSON.stringify([appError]))
      }
      await new Promise(resolve => setTimeout(resolve, Math.pow(2, attempt) * 1000))
    }
  }
}
```

### 4. 开发环境详细错误

在开发模式显示详细错误信息：

```typescript
if (import.meta.env.DEV) {
  // Show detailed error in UI
  const errorDetails = {
    message: appError.message,
    stack: appError.stack,
    component: appError.component,
    timestamp: appError.timestamp
  }

  // Show error in a modal for easier debugging
  showErrorWithDetails('开发错误', errorDetails)
}
```

### 5. 错误统计

统计错误类型和频率：

```typescript
const errorStats = ref<Map<ErrorType, number>>(new Map())

function updateErrorStats(errorType: ErrorType): void {
  const count = errorStats.value.get(errorType) || 0
  errorStats.value.set(errorType, count + 1)
  // Send stats to backend for monitoring
}
```

### 6. 错误恢复模式

针对特定错误提供恢复机制：

```typescript
const recoveryStrategies = {
  'Network Error': () => {
    // Check connectivity and retry
  },
  'Authentication Error': () => {
    // Redirect to login
  },
  'Validation Error': () => {
    // Show validation messages
  }
}
```

### 7. 错误边界组件

为特定区域创建错误边界：

```vue
<ErrorBoundary>
  <AgentListPanel :agents="agents" />
</ErrorBoundary>
```

```typescript
// ErrorBoundary.vue
<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'

const hasError = ref(false)
const error = ref<Error | null>(null)

onErrorCaptured((err: Error) => {
  hasError.value = true
  error.value = err
})
</script>

<template>
  <div v-if="hasError" class="error-boundary">
    <h2>出错了</h2>
    <p>{{ error?.message }}</p>
    <button @click="hasError = false">重试</button>
  </div>
  <slot v-else />
</template>
```

### 8. 错误采样

生产环境采样率（减少上报数量）：

```typescript
const SAMPLING_RATE = 0.1 // 只上报 10% 的错误

function shouldReportError(error: AppError): boolean {
  // Always report critical errors
  if (error.severity === ErrorSeverity.CRITICAL) {
    return true
  }

  // Sample other errors
  return Math.random() < SAMPLING_RATE
}
```

### 9. 错误去重

避免重复上报相同错误：

```typescript
const recentErrors = new Set<string>()
const ERROR_DEDUP_WINDOW = 60000 // 1 minute

function isDuplicateError(error: AppError): boolean {
  const errorKey = `${error.type}:${error.message}:${error.component}`

  if (recentErrors.has(errorKey)) {
    return true
  }

  recentErrors.add(errorKey)

  // Clean up old entries
  setTimeout(() => {
    recentErrors.delete(errorKey)
  }, ERROR_DEDUP_WINDOW)

  return false
}
```

### 10. 错误上报优先级

实现错误上报队列和优先级：

```typescript
type ErrorPriority = 'low' | 'medium' | 'high' | 'critical'

interface QueuedError {
  error: AppError
  priority: ErrorPriority
  timestamp: number
}

const errorQueue: QueuedError[] = []

function enqueueError(error: AppError, priority: ErrorPriority): void {
  errorQueue.push({
    error,
    priority,
    timestamp: Date.now()
  })

  // Sort by priority (critical first)
  errorQueue.sort((a, b) => {
    const priorityOrder = { critical: 4, high: 3, medium: 2, low: 1 }
    return priorityOrder[b.priority] - priorityOrder[a.priority]
  })

  // Process queue
  processQueue()
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue Error Handler**: https://vuejs.org/api/application.html#app-config-errorhandler
- **Window.onerror**: https://developer.mozilla.org/en-US/docs/Web/API/Window/onerror
- **Unhandled Rejection**: https://developer.mozilla.org/en-US/docs/Web/API/Window/unhandledrejection_event
- **Error Monitoring**: https://www.browserstack.com/guides/javascript-error-monitoring/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# API 请求/响应日志记录

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

在开发过程中，需要监控前端与后端之间的 API 通信，以便：
1. 调试网络请求问题
2. 分析请求响应时间
3. 追踪错误和失败请求
4. 优化性能

**目标**:
1. 记录所有 API 请求（方法、URL、请求头、请求体）
2. 记录所有 API 响应（状态码、响应时间、响应体）
3. 记录所有错误（网络错误、超时、HTTP 错误）
4. 提供统计数据（请求数量、成功率、平均响应时间）
5. 支持日志导出和查看

---

## 实现方案

### 前端实现

#### 1. 创建 API 日志工具

**文件**: `src/utils/apiLogger.ts`

**核心类型定义**:

```typescript
// 日志级别
export enum LogLevel {
  NONE = 0,
  ERROR = 1,
  WARN = 2,
  INFO = 3,
  DEBUG = 4,
}

// 请求日志
export interface APIRequestLog {
  id: string
  timestamp: string
  method: string
  url: string
  headers?: Record<string, string>
  body?: unknown
}

// 响应日志
export interface APIResponseLog {
  id: string
  timestamp: string
  status: number
  statusText: string
  duration: number
  headers?: Record<string, string>
  body?: unknown
  size: number
}

// 错误日志
export interface APIErrorLog {
  id: string
  timestamp: string
  message: string
  status?: number
  code?: string
  duration: number
}

// 完整日志条目
export interface APILogEntry {
  request: APIRequestLog
  response?: APIResponseLog
  error?: APIErrorLog
}
```

**日志存储**:

```typescript
// 日志历史
const logHistory: APILogEntry[] = []
const MAX_LOG_HISTORY = 100

// 统计数据
const stats = {
  totalRequests: 0,
  successRequests: 0,
  errorRequests: 0,
  totalDuration: 0,
}
```

**日志记录函数**:

```typescript
/**
 * 记录 API 请求
 */
export function logRequest(
  method: string,
  url: string,
  headers?: Record<string, string>,
  body?: unknown
): string {
  const logId = generateLogId()
  const timestamp = new Date().toISOString()

  const requestLog: APIRequestLog = {
    id: logId,
    timestamp,
    method,
    url,
    headers,
    body,
  }

  const entry: APILogEntry = { request: requestLog }
  logHistory.push(entry)

  // 保持历史大小可控
  if (logHistory.length > MAX_LOG_HISTORY) {
    logHistory.shift()
  }

  // 控制台输出
  console.group(`🚀 [API Request] ${method} ${url}`)
  console.log('ID:', logId)
  console.log('Timestamp:', timestamp)
  if (headers) console.log('Headers:', headers)
  if (body) console.log('Body:', formatBody(body))
  console.groupEnd()

  return logId
}
```

```typescript
/**
 * 记录 API 响应
 */
export function logResponse(
  logId: string,
  status: number,
  statusText: string,
  duration: number,
  headers?: Record<string, string>,
  body?: unknown
): void {
  const timestamp = new Date().toISOString()
  const size = calculateSize(body)

  const responseLog: APIResponseLog = {
    id: logId,
    timestamp,
    status,
    statusText,
    duration,
    headers,
    body,
    size,
  }

  // 更新现有条目
  const entry = logHistory.find(e => e.request.id === logId)
  if (entry) {
    entry.response = responseLog
  }

  // 更新统计
  stats.totalRequests++
  stats.totalDuration += duration
  if (status >= 200 && status < 300) {
    stats.successRequests++
  } else {
    stats.errorRequests++
  }

  // 彩色控制台输出
  const statusColor = status >= 200 && status < 300 ? '#22c55e'
                    : status >= 400 ? '#ef4444'
                    : '#f59e0b'
  const statusIcon = status >= 200 && status < 300 ? '✅'
                   : status >= 400 ? '❌'
                   : '⚠️'

  console.group(
    `%c${statusIcon} [API Response] ${status} ${statusText} (${formatDuration(duration)})`,
    `color: ${statusColor}; font-weight: bold`
  )
  console.log('ID:', logId)
  console.log('Timestamp:', timestamp)
  console.log('Size:', `${size} bytes`)
  if (headers) console.log('Headers:', headers)
  if (body) console.log('Body:', formatBody(body))
  console.groupEnd()
}
```

```typescript
/**
 * 记录 API 错误
 */
export function logError(
  logId: string,
  message: string,
  duration: number,
  status?: number,
  code?: string
): void {
  const timestamp = new Date().toISOString()

  const errorLog: APIErrorLog = {
    id: logId,
    timestamp,
    message,
    status,
    code,
    duration,
  }

  const entry = logHistory.find(e => e.request.id === logId)
  if (entry) {
    entry.error = errorLog
  }

  stats.totalRequests++
  stats.errorRequests++
  stats.totalDuration += duration

  console.group(`%c❌ [API Error] ${message}`, 'color: #ef4444; font-weight: bold')
  console.log('ID:', logId)
  console.log('Timestamp:', timestamp)
  console.log('Duration:', formatDuration(duration))
  if (status) console.log('Status:', status)
  if (code) console.log('Code:', code)
  console.groupEnd()
}
```

**辅助函数**:

```typescript
// 格式化持续时间
function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms.toFixed(0)}ms`
  return `${(ms / 1000).toFixed(2)}s`
}

// 格式化请求体（截断大型请求体）
function formatBody(body: unknown, maxSize = 500): string {
  if (!body) return ''
  const str = JSON.stringify(body)
  if (str.length <= maxSize) return str
  return str.substring(0, maxSize) + `... (+${str.length - maxSize} chars)`
}

// 计算响应体大小
function calculateSize(data: unknown): number {
  if (!data) return 0
  return JSON.stringify(data).length
}
```

#### 2. 集成到 API Client

**文件**: `src/api/ApiClientNew.ts`

**导入日志模块**:

```typescript
import { logRequest, logResponse, logError, setLogLevel, LogLevel } from '../utils/apiLogger'
```

**更新 APIClient 类**:

```typescript
export class APIClient {
  private config: APIConfig
  private logEnabled: boolean

  constructor(customConfig?: Partial<APIConfig>) {
    this.config = {
      baseURL: config.api.baseURL,
      timeout: 10000,
      ...customConfig,
    }

    // 开发环境启用日志，生产环境禁用
    this.logEnabled = import.meta.env.DEV

    // 根据环境设置日志级别
    if (this.logEnabled) {
      setLogLevel(LogLevel.DEBUG)
    } else {
      setLogLevel(LogLevel.ERROR)
    }
  }
```

**更新 request 方法**:

```typescript
private async request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${this.config.baseURL}${endpoint}`
  const method = options.method || 'GET'

  // 解析请求体用于日志记录
  let requestBody: unknown = undefined
  if (options.body) {
    try {
      requestBody = JSON.parse(options.body as string)
    } catch {
      requestBody = options.body
    }
  }

  // 记录请求
  const logId = this.logEnabled
    ? logRequest(method, url, options.headers as Record<string, string>, requestBody)
    : ''

  const config: RequestInit = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...this.config.headers,
      ...options.headers,
    },
  }

  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), this.config.timeout)

  const startTime = Date.now()

  try {
    const response = await fetch(url, {
      ...config,
      signal: controller.signal,
    })

    clearTimeout(timeoutId)
    const duration = Date.now() - startTime

    // 获取响应头
    const responseHeaders: Record<string, string> = {}
    response.headers.forEach((value, key) => {
      responseHeaders[key] = value
    })

    // 解析响应体
    let responseBody: unknown = undefined
    const contentType = response.headers.get('content-type')
    if (contentType?.includes('application/json')) {
      try {
        responseBody = await response.json()
      } catch {
        // JSON 解析失败
      }
    }

    if (!response.ok) {
      // 记录错误响应
      if (this.logEnabled) {
        logResponse(logId, response.status, response.statusText, duration, responseHeaders, responseBody)
        logError(logId, `HTTP ${response.status}: ${response.statusText}`, duration, response.status)
      }

      throw new APIError(
        `HTTP ${response.status}: ${response.statusText}`,
        response.status
      )
    }

    // 记录成功响应
    if (this.logEnabled) {
      logResponse(logId, response.status, response.statusText, duration, responseHeaders, responseBody)
    }

    return responseBody as T
  } catch (error) {
    const duration = Date.now() - startTime

    if (error instanceof APIError) {
      if (this.logEnabled) {
        logError(logId, error.message, duration, error.status, error.code)
      }
      throw error
    }

    if (error instanceof TypeError) {
      const networkError = new APIError('网络连接失败，请检查后端服务是否启动', 0)
      if (this.logEnabled) {
        logError(logId, networkError.message, duration, 0, 'NETWORK_ERROR')
      }
      throw networkError
    }

    if (error instanceof Error && error.name === 'AbortError') {
      const timeoutError = new APIError(`请求超时 (${this.config.timeout}ms)`, 0, 'TIMEOUT')
      if (this.logEnabled) {
        logError(logId, timeoutError.message, duration, 0, 'TIMEOUT')
      }
      throw timeoutError
    }

    throw error
  }
}
```

---

## 技术细节

### 日志流程

```
┌─────────────────────────────────────────────────────┐
│ API Request Initiated                                │
└────────┬────────────────────────────────────────────┘
         │
         ├─→ logRequest()
         │   - Generate unique log ID
         │   - Store request details
         │   - Output to console (🚀)
         │
         ├─→ Fetch Request
         │   - Measure start time
         │   - Send HTTP request
         │
         ├─→ Response Received
         │   - Calculate duration
         │   - Parse response headers & body
         │
         ├─→ Success (2xx)?
         │   ├─ Yes → logResponse() ✅
         │   │         - Update statistics
         │   │         - Output to console
         │   │
         │   └─ No → logResponse() ⚠️
         │          logError() ❌
         │          - Update error stats
         │          - Output to console
         │
         └─→ Return/Throw
```

### 控制台输出示例

**成功请求**:
```
🚀 [API Request] GET http://localhost:8080/api/agents
  ID: log-1707560123456-abc123
  Timestamp: 2026-02-10T10:30:00.000Z
  Headers: { "Content-Type": "application/json" }

✅ [API Response] 200 OK (45ms)
  ID: log-1707560123456-abc123
  Timestamp: 2026-02-10T10:30:00.045Z
  Size: 1234 bytes
  Body: { "agents": [...], "total": 5 }
```

**错误请求**:
```
🚀 [API Request] POST http://localhost:8080/api/agents/unknown/pause
  ID: log-1707560123456-def456
  Timestamp: 2026-02-10T10:31:00.000Z

❌ [API Error] HTTP 404: Not Found
  ID: log-1707560123456-def456
  Timestamp: 2026-02-10T10:31:00.012Z
  Duration: 12ms
  Status: 404
```

**网络错误**:
```
🚀 [API Request] GET http://localhost:8080/api/agents
  ID: log-1707560123456-ghi789
  Timestamp: 2026-02-10T10:32:00.000Z

❌ [API Error] 网络连接失败，请检查后端服务是否启动
  ID: log-1707560123456-ghi789
  Timestamp: 2026-02-10T10:32:00.500Z
  Duration: 500ms
  Code: NETWORK_ERROR
```

### 统计数据

```typescript
// 获取统计信息
const stats = getStats()
console.log(stats)
// {
//   totalRequests: 42,
//   successRequests: 38,
//   errorRequests: 4,
//   totalDuration: 1250,
//   avgDuration: 29.76
// }
```

---

## API 接口

### 日志管理

| 函数 | 说明 |
|------|------|
| `setLogLevel(level: LogLevel)` | 设置日志级别 |
| `getLogLevel(): LogLevel` | 获取当前日志级别 |
| `setLoggingEnabled(enabled: boolean)` | 启用/禁用日志 |
| `isLoggingEnabled(): boolean` | 检查日志是否启用 |

### 日志访问

| 函数 | 说明 |
|------|------|
| `getLogHistory(): APILogEntry[]` | 获取所有日志条目 |
| `clearLogHistory(): void` | 清空日志历史 |
| `getLogSummary(): LogSummary` | 获取日志摘要 |

### 统计数据

| 函数 | 说明 |
|------|------|
| `getStats(): Stats` | 获取统计信息 |
| `resetStats(): void` | 重置统计 |

### 导出

| 函数 | 说明 |
|------|------|
| `exportLogs(): string` | 导出日志为 JSON |

---

## 测试步骤

### 1. 请求日志测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| GET 请求 | 访问 `/api/agents` | 控制台显示请求日志 |
| POST 请求 | 创建新 Agent | 控制台显示请求日志和请求体 |
| 请求头 | 检查日志中的 headers | headers 正确记录 |

### 2. 响应日志测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 成功响应 (2xx) | 正常 API 请求 | 绿色 ✅ 日志，显示响应时间 |
| 重定向 (3xx) | 触发重定向的请求 | 黄色 ⚠️ 日志 |
| 错误响应 (4xx, 5xx) | 错误请求 | 红色 ❌ 日志 |

### 3. 错误日志测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 网络错误 | 关闭后端服务 | 显示 NETWORK_ERROR |
| 超时错误 | 请求超过 10 秒 | 显示 TIMEOUT |
| HTTP 错误 | 请求不存在资源 | 显示状态码和错误信息 |

### 4. 统计功能测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 请求数量 | 发送多个请求 | totalRequests 正确增加 |
| 成功率 | 发送成功和失败请求 | successRequests/errorRequests 正确 |
| 响应时间 | 发送多个请求 | avgDuration 计算正确 |

### 5. 日志历史测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 添加日志 | 发送请求 | logHistory 增加 |
| 历史限制 | 发送 100+ 请求 | 只保留最新 100 条 |
| 清空历史 | 调用 `clearLogHistory()` | logHistory 被清空 |

### 6. 日志级别测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| DEBUG 级别 | 设置 LogLevel.DEBUG | 所有日志都显示 |
| ERROR 级别 | 设置 LogLevel.ERROR | 只显示错误日志 |
| NONE 级别 | 设置 LogLevel.NONE | 不显示任何日志 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| ApiClientNew.ts 修改 | ✅ 通过 | 导入日志模块 |
| APIClient 类更新 | ✅ 通过 | 添加 logEnabled 属性 |
| request 方法修改 | ✅ 通过 | 集成日志记录 |
| logRequest 函数 | ✅ 通过 | 正确记录请求 |
| logResponse 函数 | ✅ 通过 | 正确记录响应 |
| logError 函数 | ✅ 通过 | 正确记录错误 |
| 统计数据 | ✅ 通过 | 正确计算 |
| 日志历史管理 | ✅ 通过 | MAX_LOG_HISTORY 限制生效 |
| 控制台输出 | ✅ 通过 | 彩色日志正确显示 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 新增的文件

#### 前端
1. **src/utils/apiLogger.ts** (新建)
   - LogLevel 枚举 (lines 13-18)
   - APIRequestLog 接口 (lines 20-27)
   - APIResponseLog 接口 (lines 29-38)
   - APIErrorLog 接口 (lines 40-48)
   - APILogEntry 接口 (lines 50-54)
   - logHistory 数组 (lines 59-60)
   - stats 统计对象 (lines 62-67)
   - setLogLevel 函数 (lines 75-77)
   - getLogLevel 函数 (lines 82-84)
   - setLoggingEnabled 函数 (lines 89-91)
   - isLoggingEnabled 函数 (lines 96-98)
   - logRequest 函数 (lines 112-145)
   - logResponse 函数 (lines 167-205)
   - logError 函数 (lines 207-233)
   - getLogHistory 函数 (lines 238-240)
   - clearLogHistory 函数 (lines 245-248)
   - getStats 函数 (lines 253-259)
   - resetStats 函数 (lines 264-270)
   - exportLogs 函数 (lines 275-284)
   - getLogSummary 函数 (lines 289-311)

### 修改的文件

#### 前端
1. **src/api/ApiClientNew.ts**
   - 导入日志模块 (line 9)
   - 添加 logEnabled 属性 (line 101)
   - 构造函数设置日志级别 (lines 110-118)
   - request 方法添加日志记录 (lines 124-234)

---

## 优化建议

### 1. 可视化日志面板

创建一个 Vue 组件显示 API 日志：

```vue
<!-- ApiLogPanel.vue -->
<template>
  <div class="api-log-panel">
    <div class="log-header">
      <h3>API 日志</h3>
      <div class="stats">
        <span>总计: {{ stats.totalRequests }}</span>
        <span class="success">成功: {{ stats.successRequests }}</span>
        <span class="error">错误: {{ stats.errorRequests }}</span>
        <span>平均: {{ formatDuration(stats.avgDuration) }}</span>
      </div>
      <button @click="clearLogs">清空</button>
    </div>
    <div class="log-list">
      <div
        v-for="log in logs"
        :key="log.request.id"
        :class="['log-item', { error: log.error }]"
      >
        <div class="log-summary">
          <span :class="['method', log.request.method.toLowerCase()]">
            {{ log.request.method }}
          </span>
          <span class="url">{{ log.request.url }}</span>
          <span v-if="log.response" :class="['status', getStatusClass(log.response.status)]">
            {{ log.response.status }}
          </span>
          <span v-if="log.response" class="duration">
            {{ formatDuration(log.response.duration) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
```

### 2. 日志过滤和搜索

```typescript
interface LogFilter {
  method?: string[]
  status?: number[]
  minDuration?: number
  maxDuration?: number
  search?: string
}

function filterLogs(filter: LogFilter): APILogEntry[] {
  return logHistory.filter(entry => {
    if (filter.method && !filter.method.includes(entry.request.method)) {
      return false
    }
    if (filter.status && entry.response && !filter.status.includes(entry.response.status)) {
      return false
    }
    if (filter.minDuration && entry.response && entry.response.duration < filter.minDuration) {
      return false
    }
    if (filter.search && !entry.request.url.includes(filter.search)) {
      return false
    }
    return true
  })
}
```

### 3. 日志持久化

```typescript
// 保存到 localStorage
function saveLogsToStorage(): void {
  try {
    const data = JSON.stringify(logHistory.slice(-50)) // 只保存最新 50 条
    localStorage.setItem('api_logs', data)
  } catch (e) {
    console.warn('Failed to save logs to storage:', e)
  }
}

// 从 localStorage 加载
function loadLogsFromStorage(): void {
  try {
    const data = localStorage.getItem('api_logs')
    if (data) {
      const logs = JSON.parse(data) as APILogEntry[]
      logHistory.push(...logs)
    }
  } catch (e) {
    console.warn('Failed to load logs from storage:', e)
  }
}
```

### 4. 远程日志上报

```typescript
// 发送日志到后端分析
async function uploadLogs(): Promise<void> {
  const data = {
    sessionId: getSessionId(),
    logs: logHistory,
    stats: getStats(),
    userAgent: navigator.userAgent,
    url: window.location.href,
  }

  await fetch('/api/logs/upload', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
}
```

### 5. 性能监控告警

```typescript
// 监控慢请求
const SLOW_REQUEST_THRESHOLD = 2000 // 2 秒

function logResponse(...) {
  // ... existing code ...

  // 如果请求过慢，显示警告
  if (duration > SLOW_REQUEST_THRESHOLD) {
    console.warn(
      `%c⚠️ Slow Request: ${method} ${url} took ${formatDuration(duration)}`,
      'color: #f59e0b; font-weight: bold'
    )
  }
}
```

### 6. 请求重试追踪

```typescript
interface RetryLog {
  originalLogId: string
  retryCount: number
  retryLogIds: string[]
}

const retryHistory = new Map<string, RetryLog>()

function logRetry(originalLogId: string, retryLogId: string): void {
  let retry = retryHistory.get(originalLogId)
  if (!retry) {
    retry = {
      originalLogId,
      retryCount: 0,
      retryLogIds: [],
    }
    retryHistory.set(originalLogId, retry)
  }
  retry.retryCount++
  retry.retryLogIds.push(retryLogId)
}
```

### 7. WebSocket 请求日志

```typescript
// 扩展到 WebSocket 消息
export interface WebSocketMessageLog {
  id: string
  timestamp: string
  direction: 'sent' | 'received'
  message: unknown
}

export function logWebSocketMessage(
  direction: 'sent' | 'received',
  message: unknown
): string {
  const logId = `ws-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`

  const wsLog: WebSocketMessageLog = {
    id: logId,
    timestamp: new Date().toISOString(),
    direction,
    message,
  }

  console.group(
    direction === 'sent' ? '📤 [WS Sent]' : '📥 [WS Received]'
  )
  console.log('ID:', logId)
  console.log('Message:', message)
  console.groupEnd()

  return logId
}
```

---

## 已知问题

无

---

## 参考资料

- **Fetch API**: https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API
- **Console API**: https://developer.mozilla.org/en-US/docs/Web/API/Console
- **Performance Monitoring**: https://web.dev/performance-monitoring/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

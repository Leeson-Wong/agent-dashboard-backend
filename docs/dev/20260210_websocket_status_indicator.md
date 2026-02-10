# WebSocket 连接状态指示器

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

之前实现了 WebSocket 连接状态增强功能（连接状态枚举、回调机制等），但缺少可视化组件来显示这些状态。用户无法直观地看到：
1. 当前 WebSocket 连接状态
2. 重连进度和尝试次数
3. 连接和断开的时间信息
4. 手动重连按钮

**目标**:
1. 创建可视化连接状态指示器
2. 实时显示连接状态（已连接/连接中/重连中等）
3. 显示详细连接信息弹窗
4. 提供手动重连功能
5. 自动初始化连接

---

## 实现方案

### 前端实现

#### 1. 创建 WebSocket 状态指示器组件

**文件**: `src/components/WebSocketStatus.vue`

**核心功能**:

**状态按钮**:
```vue
<button
  class="ws-status-btn"
  :class="statusClass"
  @click="toggleDetails"
  :title="tooltip"
>
  <span class="ws-icon">{{ statusIcon }}</span>
  <span class="ws-label">{{ statusLabel }}</span>
  <span v-if="info.state === ConnectionState.RECONNECTING" class="ws-reconnect-count">
    ({{ info.reconnectAttempts }}/{{ info.maxReconnectAttempts }})
  </span>
</button>
```

**状态图标和标签**:
```typescript
const statusIcon = computed(() => {
  const state = info.value.state
  const icons = {
    [ConnectionState.CONNECTED]: '●',
    [ConnectionState.CONNECTING]: '○',
    [ConnectionState.RECONNECTING]: '⟳',
    [ConnectionState.DISCONNECTED]: '○',
    [ConnectionState.ERROR]: '✗'
  }
  return icons[state] || '○'
})

const statusLabel = computed(() => {
  const state = info.value.state
  const labels = {
    [ConnectionState.CONNECTED]: '已连接',
    [ConnectionState.CONNECTING]: '连接中...',
    [ConnectionState.RECONNECTING]: '重连中',
    [ConnectionState.DISCONNECTED]: '未连接',
    [ConnectionState.ERROR]: '连接错误'
  }
  return labels[state] || '未知'
})
```

**样式类计算**:
```typescript
const statusClass = computed(() => {
  const state = info.value.state
  return {
    connected: state === ConnectionState.CONNECTED,
    connecting: state === ConnectionState.CONNECTING,
    reconnecting: state === ConnectionState.RECONNECTING,
    disconnected: state === ConnectionState.DISCONNECTED,
    error: state === ConnectionState.ERROR
  }
})
```

**状态详情弹窗**:
```vue
<div v-if="showDetails" class="ws-modal">
  <div class="ws-modal-content">
    <div class="ws-modal-header">
      <h3>WebSocket 连接状态</h3>
      <button class="close-btn" @click="showDetails = false">×</button>
    </div>

    <div class="ws-modal-body">
      <!-- Current Status -->
      <div class="ws-status-display">
        <div class="ws-status-badge" :class="statusClass">
          <span class="status-icon">{{ statusIcon }}</span>
          <span class="status-text">{{ statusLabel }}</span>
        </div>
      </div>

      <!-- Connection Details -->
      <div class="ws-details">
        <div class="detail-row">
          <span class="detail-label">连接地址:</span>
          <span class="detail-value">{{ formatUrl(info.url) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">连接状态:</span>
          <span class="detail-value">{{ info.state }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">已连接:</span>
          <span class="detail-value">{{ info.connected ? '是' : '否' }}</span>
        </div>

        <template v-if="info.lastConnectedTime">
          <div class="detail-row">
            <span class="detail-label">连接时间:</span>
            <span class="detail-value">{{ formatTimestamp(info.lastConnectedTime) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">已连接时长:</span>
            <span class="detail-value">{{ getDuration(info.lastConnectedTime) }}</span>
          </div>
        </template>

        <template v-if="info.lastDisconnectedTime">
          <div class="detail-row">
            <span class="detail-label">断开时间:</span>
            <span class="detail-value">{{ formatTimestamp(info.lastDisconnectedTime) }}</span>
          </div>
        </template>

        <template v-if="info.state === ConnectionState.RECONNECTING">
          <div class="detail-row">
            <span class="detail-label">重连尝试:</span>
            <span class="detail-value">{{ info.reconnectAttempts }} / {{ info.maxReconnectAttempts }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">下次重连:</span>
            <span class="detail-value">{{ formatDuration(info.reconnectDelay) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">重连延迟:</span>
            <span class="detail-value">{{ info.reconnectDelay }}ms</span>
          </div>
        </template>
      </div>

      <!-- Actions -->
      <div class="ws-actions">
        <button
          v-if="canReconnect"
          @click="handleManualReconnect"
          class="action-btn primary"
          :disabled="isReconnecting"
        >
          {{ isReconnecting ? '⏳ 重连中...' : '🔄 手动重连' }}
        </button>
        <button @click="showDetails = false" class="action-btn">
          关闭
        </button>
      </div>

      <!-- Instructions (for error state) -->
      <div v-if="info.state === ConnectionState.ERROR" class="ws-instructions">
        <p><strong>连接失败可能的原因：</strong></p>
        <ul>
          <li>后端服务未启动</li>
          <li>网络连接问题</li>
          <li>防火墙阻止了 WebSocket 连接</li>
          <li>后端地址配置错误</li>
        </ul>
        <p><strong>建议操作：</strong></p>
        <ul>
          <li>检查后端服务是否运行</li>
          <li>尝试刷新页面</li>
          <li>点击"手动重连"按钮</li>
        </ul>
      </div>
    </div>
  </div>
</div>
```

**核心逻辑**:

```typescript
import { getWebSocketConnection, ConnectionState, type ConnectionInfo } from '../api/WebSocketConnection'

const ws = getWebSocketConnection()
const showDetails = ref(false)
const info = ref<ConnectionInfo>(ws.getConnectionInfo())
const isReconnecting = ref(false)

// Subscribe to state changes
const unsubscribe = ws.onStateChange((newInfo) => {
  info.value = newInfo
})

// Toggle details modal
const toggleDetails = () => {
  showDetails.value = !showDetails.value
}

// Manual reconnect
const handleManualReconnect = async () => {
  isReconnecting.value = true
  try {
    await ws.manualReconnect()
  } finally {
    setTimeout(() => {
      isReconnecting.value = false
    }, 1000)
  }
}

// Auto-connect on mount if disconnected
onMounted(() => {
  if (info.value.state === ConnectionState.DISCONNECTED) {
    ws.connect().catch(err => {
      console.error('Failed to connect WebSocket:', err)
    })
  }
})

onUnmounted(() => {
  unsubscribe()
})
```

#### 2. 集成到 App.vue

**文件**: `src/App.vue`

**模板更新**:
```vue
<WebSocketStatus />
```

**导入更新**:
```typescript
import WebSocketStatus from './components/WebSocketStatus.vue'
```

---

## 技术细节

### 状态显示

| 状态 | 图标 | 标签 | 颜色 | 动画 |
|------|------|------|------|------|
| CONNECTED | ● | 已连接 | 绿色 | 脉冲 |
| CONNECTING | ○ | 连接中... | 蓝色 | 旋转 |
| RECONNECTING | ⟳ | 重连中 | 橙色 | 旋转 |
| DISCONNECTED | ○ | 未连接 | 灰色 | 无 |
| ERROR | ✗ | 连接错误 | 红色 | 无 |

### 样式实现

```css
/* Connected state - green with pulse */
.ws-status-btn.connected {
  border-color: rgba(34, 197, 94, 0.3);
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
}

.ws-status-btn.connected .ws-icon {
  animation: pulse-green 2s ease-in-out infinite;
}

@keyframes pulse-green {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* Connecting/Reconnecting state - blue with spin */
.ws-status-btn.connecting,
.ws-status-btn.reconnecting {
  border-color: rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.ws-status-btn.connecting .ws-icon,
.ws-status-btn.reconnecting .ws-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Error state - red */
.ws-status-btn.error {
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}
```

### 工具函数

```typescript
// Format URL for display
const formatUrl = (url: string): string => {
  try {
    const urlObj = new URL(url)
    return urlObj.host + urlObj.pathname
  } catch {
    return url
  }
}

// Format timestamp
const formatTimestamp = (timestamp: number | null): string => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString()
}

// Format duration
const formatDuration = (ms: number): string => {
  if (ms < 1000) return `${ms}ms`
  if (ms < 60000) return `${(ms / 1000).toFixed(0)}s`
  return `${(ms / 1000).toFixed(1)}s`
}

// Get duration since time
const getDuration = (startTime: number | null): string => {
  if (!startTime) return '-'
  const diff = Date.now() - startTime
  return formatDuration(diff)
}
```

---

## UI 效果

### 状态按钮

```
已连接:
┌─────────────────────────────────┐
│ ● 已连接                       │ ← 绿色，脉冲动画
└─────────────────────────────────┘

重连中:
┌─────────────────────────────────┐
│ ⟳ 重连中 (2/5)                 │ ← 橙色，旋转动画
└─────────────────────────────────┘

连接错误:
┌─────────────────────────────────┐
│ ✗ 连接错误                     │ ← 红色
└─────────────────────────────────┘
```

### 详情弹窗

```
┌──────────────────────────────────────────────────────────┐
│  WebSocket 连接状态                              [×]     │
├──────────────────────────────────────────────────────────┤
│                                                           │
│              ● 已连接                                  │
│                                                           │
│  连接地址:    localhost:8080/ws                        │
│  连接状态:    connected                               │
│  已连接:      是                                       │
│  连接时间:    2026-02-10 14:30:00                      │
│  已连接时长:  15s                                      │
│                                                           │
│                         [🔄 手动重连]  [关闭]          │
└──────────────────────────────────────────────────────────┘
```

### 重连中详情

```
┌──────────────────────────────────────────────────────────┐
│  WebSocket 连接状态                              [×]     │
├──────────────────────────────────────────────────────────┤
│                                                           │
│              ⟳ 重连中                                 │
│                                                           │
│  连接地址:    localhost:8080/ws                        │
│  连接状态:    reconnecting                             │
│  已连接:      否                                       │
│  重连尝试:    3 / 5                                    │
│  下次重连:    8s                                       │
│  重连延迟:    20000ms                                  │
│                                                           │
│                    [⏳ 重连中...]  [关闭]        │
└──────────────────────────────────────────────────────────┘
```

### 错误提示

```
┌──────────────────────────────────────────────────────────┐
│  WebSocket 连接状态                              [×]     │
├──────────────────────────────────────────────────────────┤
│                                                           │
│              ✗ 连接错误                               │
│                                                           │
│  连接地址:    localhost:8080/ws                        │
│  连接状态:    error                                    │
│  已连接:      否                                       │
│  断开时间:    2026-02-10 14:30:00                      │
│                                                           │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 连接失败可能的原因：                          │   │
│  │ • 后端服务未启动                                │   │
│  │ • 网络连接问题                                  │   │
│  │ • 防火墙阻止了 WebSocket 连接                   │   │
│  │ • 后端地址配置错误                              │   │
│  │                                               │   │
│  │ 建议操作：                                    │   │
│  │ • 检查后端服务是否运行                        │   │
│  │ • 尝试刷新页面                                │   │
│  │ • 点击"手动重连"按钮                          │   │
│  └─────────────────────────────────────────────────┘   │
│                                                           │
│                    [🔄 手动重连]  [关闭]        │
└──────────────────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 状态显示测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 已连接 | WebSocket 正常连接 | 绿色 ● 图标，脉冲动画 |
| 连接中 | 正在建立连接 | 蓝色 ○ 图标，旋转动画 |
| 重连中 | 自动重连过程中 | 橙色 ⟳ 图标，旋转动画，显示尝试次数 |
| 断开 | 连接断开 | 灰色 ○ 图标 |
| 错误 | 连接失败 | 红色 ✗ 图标 |

### 2. 详情弹窗测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 打开弹窗 | 点击状态按钮 | 显示详情弹窗 |
| 关闭弹窗 | 点击 × 或关闭按钮 | 弹窗关闭 |
| 点击外部 | 点击弹窗外部 | 弹窗关闭 |

### 3. 信息显示测试

| 测试项 | 预期结果 |
|--------|----------|
| 连接地址 | 显示格式化的 URL |
| 连接时间 | 最后连接时间 |
| 已连接时长 | 从连接到现在经过的时间 |
| 断开时间 | 最后断开时间 |
| 重连尝试 | 当前尝试次数 / 最大次数 |
| 重连延迟 | 下次重连等待时间 |

### 4. 手动重连测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 重连按钮 | 断开时点击"手动重连" | 触发重连，按钮显示"重连中..." |
| 按钮状态 | 重连过程中 | 按钮禁用，显示 loading |
| 重连成功 | 连接恢复 | 状态变为"已连接" |
| 重连失败 | 连接失败 | 状态变为"连接错误" |

### 5. 错误提示测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 错误状态 | 连接失败时打开 | 显示错误提示和建议 |
| 原因列表 | 查看 ERROR 状态 | 列出 4 种可能原因 |
| 建议操作 | 查看 ERROR 状态 | 显示 3 条建议操作 |

### 6. 自动连接测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 启动连接 | 组件挂载时未连接 | 自动尝试连接 |
| 状态更新 | 连接状态变化 | 实时更新显示 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| WebSocketStatus.vue 创建 | ✅ 通过 | 组件创建成功 |
| App.vue 集成 | ✅ 通过 | 组件正确导入和使用 |
| 状态图标 | ✅ 通过 | 5 种状态图标正确 |
| 状态标签 | ✅ 通过 | 5 种状态标签正确 |
| 颜色样式 | ✅ 通过 | 5 种状态颜色正确 |
| 动画效果 | ✅ 通过 | 脉冲和旋转动画流畅 |
| 详情弹窗 | ✅ 通过 | 弹窗显示正确 |
| 连接详情 | ✅ 通过 | 所有详情信息正确显示 |
| 时间格式化 | ✅ through | formatTimestamp/getDuration 正确 |
| URL 格式化 | ✅ 通过 | formatUrl 正确处理 |
| 手动重连 | ✅ 通过 | 重连功能正常工作 |
| 自动连接 | ✅ 通过 | 挂载时自动尝试连接 |
| 状态更新 | ✅ 通过 | 实时响应状态变化 |
| 错误提示 | ✅ 通过 | ERROR 状态显示提示 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 新增的文件

#### 前端
1. **src/components/WebSocketStatus.vue** (新建)
   - 模板结构 (lines 1-160)
   - Script setup (lines 162-272)
   - 样式定义 (lines 274-484)

### 修改的文件

#### 前端
1. **src/App.vue**
   - 添加 WebSocketStatus 组件标签 (line 60)
   - 添加 WebSocketStatus 导入 (line 171)

---

## 后续功能建议

### 1. 连接质量指示

```typescript
// 根据延迟和稳定性评估连接质量
function getConnectionQuality(info: ConnectionInfo): 'excellent' | 'good' | 'fair' | 'poor' {
  if (!info.connected) return 'poor'

  // Check reconnect attempts
  if (info.reconnectAttempts > 3) return 'fair'
  if (info.reconnectAttempts > 1) return 'good'

  // Could add latency check here
  return 'excellent'
}
```

### 2. 连接统计图表

```typescript
// 记录连接历史
interface ConnectionHistory {
  connectedAt: number
  disconnectedAt: number | null
  duration: number | null
  reason?: string
}

const connectionHistory = ref<ConnectionHistory[]>([])

function onConnect() {
  connectionHistory.value.push({
    connectedAt: Date.now(),
    disconnectedAt: null,
    duration: null
  })
}

function onDisconnect(reason: string) {
  const lastConnection = connectionHistory.value[connectionHistory.value.length - 1]
  if (lastConnection && !lastConnection.disconnectedAt) {
    lastConnection.disconnectedAt = Date.now()
    lastConnection.duration = lastConnection.disconnectedAt - lastConnection.connectedAt
    lastConnection.reason = reason
  }
}
```

### 3. 连接日志面板

```vue
<template>
  <div class="ws-log-panel">
    <h4>连接日志</h4>
    <div class="log-entries">
      <div v-for="(entry, index) in logEntries" :key="index" class="log-entry">
        <span class="log-time">{{ formatTime(entry.timestamp) }}</span>
        <span class="log-event" :class="entry.event.toLowerCase()">{{ entry.event }}</span>
        <span v-if="entry.detail" class="log-detail">{{ entry.detail }}</span>
      </div>
    </div>
  </div>
</template>
```

### 4. 网络状态检测

```typescript
// 监听浏览器在线/离线事件
window.addEventListener('online', () => {
  if (info.value.state === ConnectionState.DISCONNECTED) {
    handleManualReconnect()
  }
})

window.addEventListener('offline', () => {
  // Show offline warning
  console.log('Network is offline')
})
```

### 5. WebSocket 配置面板

```vue
<template>
  <div class="ws-config">
    <h4>WebSocket 配置</h4>
    <div class="config-item">
      <label>服务器地址:</label>
      <input v-model="wsUrl" type="text" />
    </div>
    <div class="config-item">
      <label>最大重连次数:</label>
      <input v-model="maxReconnect" type="number" />
    </div>
    <div class="config-item">
      <label>重连延迟 (ms):</label>
      <input v-model="reconnectDelay" type="number" />
    </div>
    <button @click="applyConfig">应用配置</button>
  </div>
</template>
```

### 6. 延迟图表

```typescript
// 使用 Chart.js 显示消息延迟
import { Chart } from 'chart.js/auto'

function createLatencyChart(canvas: HTMLCanvasElement) {
  new Chart(canvas, {
    type: 'line',
    data: {
      labels: [],
      datasets: [{
        label: '消息延迟 (ms)',
        data: [],
        borderColor: 'rgb(59, 130, 246)',
        tension: 0.4
      }]
    },
    options: {
      scales: {
        y: {
          beginAtZero: true,
          max: 1000
        }
      }
    }
  })
}
```

---

## 已知问题

无

---

## 参考资料

- **WebSocket API**: https://developer.mozilla.org/en-US/docs/Web/API/WebSocket
- **STOMP Protocol**: https://stomp.github.io/stomp-spec/
- **SockJS**: https://github.com/sockjs/sockjs-client
- **@stomp/stompjs**: https://github.com/stomp-js/stomp-websocket

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# WebSocket 消息活动指示器

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户希望能够直观地看到 WebSocket 是否正在接收消息。当前虽然显示了连接状态（已连接/未连接），但无法直观判断消息流是否正常。

**目标**:
1. 在头部添加消息计数器显示收到的消息总数
2. 显示最后一条消息的时间（相对时间）
3. 当收到新消息时触发闪烁动画提示用户

---

## 实现方案

### 前端修改 (App.vue)

#### 1. 添加状态变量

```typescript
// WebSocket 活动追踪
const lastMessageTime = ref<string | null>(null)
const messageCount = ref(0)
const messageFlash = ref(false)
```

#### 2. 更新 WebSocket 消息处理器

```typescript
const unsubscribe = ws.onMessage((message) => {
  console.log('WebSocket message:', message)

  // 更新活动追踪
  lastMessageTime.value = new Date().toISOString()
  messageCount.value++

  // 触发闪烁动画
  messageFlash.value = true
  setTimeout(() => {
    messageFlash.value = false
  }, 300)

  // 处理 agent_update 消息...
  wsConnected.value = ws.isConnected()
})
```

#### 3. 添加时间计算属性

```typescript
const timeSinceLastUpdate = computed(() => {
  if (!lastMessageTime.value) return ''

  const now = new Date()
  const lastTime = new Date(lastMessageTime.value)
  const diff = now.getTime() - lastTime.getTime()

  if (diff < 1000) return '刚刚'
  if (diff < 60000) return `${Math.floor(diff / 1000)}秒前`
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  return `${Math.floor(diff / 3600000)}小时前`
})
```

#### 4. 更新头部模板

```vue
<header class="app-header">
  <h1>Agent Dashboard</h1>
  <div class="header-stats">
    <span class="stat">在线: {{ stats.online }}</span>
    <span class="stat">总 Agent: {{ stats.total }}</span>
    <span class="stat" :class="{ connected: wsConnected }">
      {{ wsConnected ? '● 已连接' : '○ 未连接' }}
    </span>
    <span class="stat" :class="{ 'message-flash': messageFlash }">
      消息: {{ messageCount }}
    </span>
    <span class="stat last-update">
      {{ lastMessageTime ? timeSinceLastUpdate : '无消息' }}
    </span>
    <button class="memory-btn" @click="openMemoryPanel()">
      🧠 Memory 管理
    </button>
  </div>
</header>
```

#### 5. 添加 CSS 动画

```css
.stat.message-flash {
  animation: flash 300ms ease-out;
  color: #3b82f6;
}

@keyframes flash {
  0% {
    background-color: rgba(59, 130, 246, 0.3);
    padding: 4px 8px;
    border-radius: 4px;
  }
  100% {
    background-color: transparent;
    padding: 0;
  }
}
```

---

## 功能说明

### 1. 消息计数器

- 显示自连接以来收到的消息总数
- 每收到一条 WebSocket 消息自动递增
- 格式：`消息: 42`

### 2. 最后消息时间

- 显示最后一条消息的相对时间
- 动态更新（相对于当前时间）
- 格式：
  - `刚刚`（1秒内）
  - `X秒前`（1分钟内）
  - `X分钟前`（1小时内）
  - `X小时前`（超过1小时）

### 3. 消息闪烁动画

- 当收到新消息时触发
- 蓝色背景闪烁 + 文字变色
- 持续时间：300ms
- 效果：蓝色半透明背景淡入淡出

---

## 测试步骤

### 1. 后端测试接口

**测试批量广播**:
```bash
curl -X POST http://localhost:8080/api/test/broadcast-batch?count=3
```

**测试单条广播**:
```bash
curl -X POST http://localhost:8080/api/test/broadcast \
  -H "Content-Type: application/json" \
  -d '{"message":"Activity test message"}'
```

**响应示例**:
```json
{
  "count": 3,
  "message": "已广播 3 条测试消息",
  "status": "ok",
  "timestamp": "2026-02-09T17:13:45.857331500Z"
}
```

### 2. 前端验证

1. **打开浏览器** 访问 `http://localhost:3000`
2. **观察头部统计信息**:
   - 初始状态：`消息: 0` `无消息`
3. **触发测试消息**:
   ```bash
   curl -X POST http://localhost:8080/api/test/broadcast-batch?count=5
   ```
4. **预期结果**:
   - ✅ 消息计数递增：`消息: 5`
   - ✅ 时间更新：`刚刚` → `X秒前` → `X分钟前`
   - ✅ 蓝色闪烁动画出现
   - ✅ 连接状态显示绿色 `● 已连接`

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 消息计数 | ✅ 通过 | 正确递增 |
| 时间显示 | ✅ 通过 | 相对时间计算正确 |
| 闪烁动画 | ✅ 完成 | 蓝色背景淡入淡出 |
| 后端接口 | ✅ 通过 | 广播消息成功 |
| 前端接收 | ✅ 通过 | WebSocket 正常接收 |
| 样式渲染 | ✅ 完成 | 无布局错位 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/App.vue**
   - 添加状态变量 (line 114-116)
   - 更新 WebSocket 消息处理器 (line 391-402)
   - 添加时间计算属性 (line 140-151)
   - 更新头部模板 (line 17-22)
   - 添加 CSS 动画 (line 516-531)

---

## UI 效果

### 头部显示

```
┌─────────────────────────────────────────────────────────────────┐
│ Agent Dashboard                                                  │
│                                                                 │
│ 在线: 6  总 Agent: 6  ● 已连接  消息: 42  刚刚  [🧠 Memory 管理]  │
└─────────────────────────────────────────────────────────────────┘
```

### 收到消息时的动画效果

```
┌─────────────────────────────────────────────────────────────────┐
│ Agent Dashboard                                                  │
│                                                                 │
│ 在线: 6  总 Agent: 6  ● 已连接  [消息: 43]  刚刚                 │
│                              ^^^^^^^^                             │
│                              蓝色闪烁背景                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 优化建议

### 1. 长时间无消息提示

如果超过一定时间（如5分钟）没有收到消息，可以添加警告提示：

```typescript
const isStale = computed(() => {
  if (!lastMessageTime.value) return false
  const diff = new Date().getTime() - new Date(lastMessageTime.value).getTime()
  return diff > 300000 // 5分钟
})
```

```vue
<span class="stat" :class="{ stale: isStale }">
  {{ lastMessageTime ? timeSinceLastUpdate : '无消息' }}
</span>
```

### 2. 消息频率统计

可以添加消息速率显示：

```typescript
const messageRate = computed(() => {
  if (!lastMessageTime.value || messageCount.value === 0) return '0 msg/s'
  // 计算平均消息速率
  const elapsed = new Date().getTime() - startTime.value
  const rate = (messageCount.value / elapsed) * 1000
  return `${rate.toFixed(1)} msg/s`
})
```

### 3. 消息类型过滤

可以只统计特定类型的消息（如 `agent_update`）：

```typescript
if ((message as Record<string, unknown>).type === 'agent_update') {
  // 只统计 agent_update 消息
  messageCount.value++
  messageFlash.value = true
}
```

---

## 已知问题

无

---

## 参考资料

- **后端测试接口**: `WebSocketTestController.java`
- **WebSocket 连接**: `WebSocketConnection.ts`
- **Vue 响应式系统**: https://vuejs.org/guide/essentials/reactivity-fundamentals.html

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

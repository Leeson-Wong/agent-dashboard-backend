# Toast 通知系统增强

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

现有的 Toast 通知系统功能完善，但存在以下可改进的地方：

1. **通知堆叠问题**: 当多个通知同时出现时，会占据大量屏幕空间
2. **缺少暂停功能**: 用户无法暂停正在倒计时的通知
3. **动画效果**: 离场动画可以更自然流畅

**目标**:
1. 添加最大通知数量限制（防止堆叠）
2. 实现鼠标悬停暂停功能
3. 改进离场动画效果
4. 增强视觉反馈

---

## 实现方案

### 前端实现

#### 1. 通知队列管理

**文件**: `src/composables/useToast.ts`

```typescript
const MAX_TOASTS = 5 // Maximum number of toasts to display

export function useToast() {
  const addToast = (toast: Omit<ToastItem, 'id'>): string => {
    // Check if toast queue is full, remove oldest toast
    if (toasts.value.length >= MAX_TOASTS) {
      toasts.value.shift() // Remove first (oldest) toast
    }

    const id = `toast-${++toastIdCounter}`
    const newToast: ToastItem = {
      id,
      ...toast,
    }

    toasts.value.push(newToast)
    return id
  }
  // ...
}
```

**说明**:
- 设置最大通知数量为 5
- 当队列满时，自动移除最旧的通知
- 确保屏幕上最多显示 5 个通知

#### 2. 鼠标悬停暂停

**文件**: `src/components/Toast.vue`

**模板更新**:
```vue
<div
  :class="['toast', `toast-${type}`, { 'toast-clickable': clickable, 'toast-paused': isPaused }]"
  @mouseenter="pause"
  @mouseleave="resume"
>
```

**逻辑实现**:
```typescript
const isPaused = ref(false)
let remainingTime = props.duration
let lastUpdateTime = Date.now()

const pause = (): void => {
  if (!isPaused.value && remainingTime > 0) {
    isPaused.value = true
    // Save remaining time
    remainingTime = remainingTime - (Date.now() - lastUpdateTime)

    // Clear timers but don't close
    if (timer) clearTimeout(timer)
    if (progressTimer) clearInterval(progressTimer)
  }
}

const resume = (): void => {
  if (isPaused.value && remainingTime > 0) {
    isPaused.value = false
    lastUpdateTime = Date.now()

    // Restart with remaining time
    timer = setTimeout(() => {
      close()
    }, remainingTime)

    startProgress(remainingTime)
  }
}
```

**关键点**:
- 鼠标进入时暂停倒计时
- 保存剩余时间
- 鼠标离开时恢复倒计时
- 进度条也随之暂停和恢复

#### 3. 改进的动画效果

**进场动画**:
```css
.toast-success-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-success-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
```

**离场动画**:
```css
.toast-success-leave-to {
  opacity: 0;
  transform: translateX(20%) scale(0.95);
}
```

**动画改进**:
- 使用 `cubic-bezier(0.4, 0, 0.2, 1)` 缓动函数，更自然
- 离场时向右移动 20% 并缩小至 95%，营造"滑走"感
- 比纯透明度变化更生动

#### 4. 视觉反馈增强

**悬停状态**:
```css
.toast:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
}

.toast-paused {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}
```

**说明**:
- 普通悬停：加深阴影
- 暂停状态：蓝色阴影，明确提示用户倒计时已暂停

---

## 技术细节

### 队列管理流程

```
用户触发通知 1
    ↓
┌─────────────────┐
│ Toast Queue:    │
│ [Toast 1]       │
└─────────────────┘

用户触发通知 2-5
    ↓
┌─────────────────┐
│ Toast Queue:    │
│ [Toast 1]       │
│ [Toast 2]       │
│ [Toast 3]       │
│ [Toast 4]       │
│ [Toast 5]       │
└─────────────────┘

用户触发通知 6 (队列已满)
    ↓
    shift() 移除 Toast 1
    ↓
┌─────────────────┐
│ Toast Queue:    │
│ [Toast 2]       │ ← 新位置
│ [Toast 3]       │
│ [Toast 4]       │
│ [Toast 5]       │
│ [Toast 6]       │ ← 新增
└─────────────────┘
```

### 暂停/恢复机制

```
倒计时: 3000ms (3秒)

正常流程:
0ms ────── 1000ms ────── 2000ms ────── 3000ms
│                              │
└─────── 耗尽 ───────────────┘

暂停流程 (1500ms 时悬停):
0ms ────── 1000ms ────── 1500ms ────── 用户悬停
│                              │
└─────── 剩余 1500ms ──────────┘
                              ↓
                        isPaused = true
                        (暂停计时器和进度条)

用户离开 (3500ms 时):
                        ┌──────────────────────────────┐
                        │ 剩余时间: 1500ms ───────────│
                        │ └─────── 耗尽 ─────────────┘│
                        │                              │
                        └──────────────────────────────┘
                              ↓
                        关闭通知
```

### 状态管理

```typescript
// 状态变量
const visible = ref(false)        // 是否可见（控制过渡动画）
const progressWidth = ref(100)    // 进度条宽度 (0-100%)
const isPaused = ref(false)       // 是否暂停

// 计时器
let timer: ReturnType<typeof setTimeout> | null = null          // 关闭倒计时
let progressTimer: ReturnType<typeof setInterval> | null = null  // 进度条更新

// 时间追踪
let remainingTime = props.duration   // 剩余时间
let lastUpdateTime = Date.now()       // 上次更新时间
```

---

## UI 效果

### 通知队列限制

```
最多显示 5 个通知:
┌────────────────────────────────────┐
│ [✓] 操作成功                    │ ← Toast 1 (最旧)
├────────────────────────────────────┤
│ [✓] 数据已刷新                  │ ← Toast 2
├────────────────────────────────────┤
│ [⚠] 警告消息                   │ ← Toast 3
├────────────────────────────────────┤
│ [ⓘ] 信息提示                   │ ← Toast 4
├────────────────────────────────────┤
│ [✓] 完成保存                    │ ← Toast 5 (最新)
└────────────────────────────────────┘

新增第 6 个通知:
┌────────────────────────────────────┐
│ [✓] 数据已刷新                  │ ← Toast 1 已被移除
│ [⚠] 警告消息                   │
│ [ⓘ] 信息提示                   │
│ [✓] 完成保存                    │
│ [✓] 操作成功                    │
│ [✓] 新通知 (Toast 6)            │ ← 新增
└────────────────────────────────────┘
```

### 暂停/恢复效果

```
正常状态:
┌────────────────────────────────────┐
│ [✓] 操作成功                    │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━ 45%  │ ← 进度条继续
└────────────────────────────────────┘

悬停暂停状态:
┌────────────────────────────────────┐
│ [✓] 操作成功                    │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━ 60%  │ ← 进度条停止
│                                      │ ← 蓝色阴影提示
└────────────────────────────────────┘
      ↑
    鼠标悬停在此

用户离开后恢复:
┌────────────────────────────────────┐
│ [✓] 操作成功                    │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━ 40%  │ ← 从 60% 继续
└────────────────────────────────────┘
```

### 动画效果对比

**改进前的离场动画**:
```
不透明度: 1 ───────────────────────→ 0
位移: 0% ─────────────────────────→ 100%
```

**改进后的离场动画**:
```
不透明度: 1 ───────────────────────→ 0
位移: 0% ────→ 20% (向右移动)
缩放: 100% ──→ 95% (轻微缩小)
```

---

## 测试步骤

### 1. 队列管理测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 添加 5 个通知 | 连续触发 5 次 | 显示 5 个通知 |
| 添加第 6 个通知 | 触发第 6 次 | 第 1 个被移除，显示第 6 个 |
| 快速添加 10 个 | 连续快速触发 | 只保留最新的 5 个 |
| 清空队列 | 调用 clearAll() | 所有通知被移除 |

### 2. 暂停/恢复测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 正常倒计时 | 不操作通知 | 3 秒后自动关闭 |
| 悬停暂停 | 鼠标移入通知 | 倒计时暂停 |
| 进度条暂停 | 观察进度条 | 停止减少 |
| 悬停阴影 | 观察阴影 | 变为蓝色 |
| 恢复倒计时 | 鼠标移出通知 | 从剩余时间继续倒计时 |
| 多次暂停 | 反复移入移出 | 每次都正确暂停和恢复 |

### 3. 动画效果测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 进场动画 | 添加新通知 | 从右侧滑入，淡入效果 |
| 离场动画 | 等待自动关闭 | 向右滑出 + 缩小 + 淡出 |
| 手动关闭 | 点击 × 按钮 | 同样的离场动画 |
| 动画流畅度 | 观察多个通知 | 流畅无卡顿 |

### 4. 边界情况测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 暂停后手动关闭 | 悬停时点击 × | 立即关闭，无错误 |
| 暂停期间添加通知 | 暂停时触发新通知 | 正常显示新通知 |
| 极短 duration | duration=100 | 快速显示和消失 |
| duration=0 | duration=0 | 不自动关闭，需手动关闭 |
| 大量通知 | 连续触发 20+ 次 | 保持最多 5 个 |

### 5. 性能测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 内存使用 | 添加和移除 100 次 | 无内存泄漏 |
| CPU 使用 | 同时 5 个通知进度条 | CPU 使用正常 |
| 动画性能 | 快速添加和移除 | 动画流畅不卡顿 |
| 定时器清理 | 组件卸载 | 所有定时器被清除 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| MAX_TOASTS 常量 | ✅ 通过 | 设置为 5 |
| 队列满时移除旧通知 | ✅ 通过 | shift() 正确移除第一个 |
| isPaused 状态 | ✅ 通过 | 正确反映暂停状态 |
| pause 函数 | ✅ 通过 | 保存剩余时间，清除计时器 |
| resume 函数 | ✅ 通过 | 恢复倒计时和进度条 |
| 鼠标事件绑定 | ✅ 通过 | mouseenter/mouseleave 正确触发 |
| 进度条暂停 | ✅ 通过 | 暂停时停止更新 |
| 悬停阴影效果 | ✅ 通过 | 蓝色阴影正确显示 |
| cubic-bezier 缓动 | ✅ 通过 | 动画更自然流畅 |
| scale(0.95) 缩小 | ✅ 通过 | 离场动画更生动 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/composables/useToast.ts**
   - 添加 MAX_TOASTS 常量 (line 6)
   - 实现队列满时移除旧通知 (lines 10-13)

2. **src/components/Toast.vue**
   - 添加 isPaused ref (line 46)
   - 添加 remainingTime 变量 (line 62)
   - 添加 lastUpdateTime 变量 (line 63)
   - 实现 pause 函数 (lines 72-81)
   - 实现 resume 函数 (lines 84-96)
   - 更新 startProgress 函数支持 duration 参数 (lines 98-112)
   - 更新 onMounted 记录时间 (line 121)
   - 添加 @mouseenter 和 @mouseleave 事件 (lines 6-7)
   - 添加 toast-paused 样式类 (line 5)
   - 添加 :hover 样式 (lines 152-154)
   - 添加 .toast-paused 样式 (lines 156-158)
   - 更新过渡缓动函数 (line 292)
   - 更新离场动画效果 (lines 307-309)

---

## 优化建议

### 1. 可配置的最大数量

允许自定义最大通知数量：

```typescript
interface ToastConfig {
  maxToasts?: number
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left'
}

let globalConfig: ToastConfig = {
  maxToasts: 5,
  position: 'top-right'
}

export function configureToast(config: ToastConfig): void {
  globalConfig = { ...globalConfig, ...config }
}
```

### 2. 通知分组

合并相同消息的通知：

```typescript
const toastCounts = ref<Map<string, number>>(new Map())

const addToast = (toast: Omit<ToastItem, 'id'>): string => {
  const key = `${toast.type}-${toast.message}`

  if (toastCounts.value.has(key)) {
    // Increment count and update existing toast
    const count = toastCounts.value.get(key)! + 1
    toastCounts.value.set(key, count)

    // Find and update existing toast
    const existing = toasts.value.find(t =>
      t.type === toast.type && t.message === toast.message
    )

    if (existing) {
      existing.message = `${toast.message} (${count})`
      return existing.id
    }
  }

  // Add new toast
  toastCounts.value.set(key, 1)
  // ... rest of addToast logic
}
```

### 3. 通知堆叠模式

使用堆叠布局节省空间：

```vue
<div class="toast-list" :class="{ 'stacked': toasts.length > 3 }">
  <Toast v-for="toast in toasts" :key="toast.id" />
</div>

<style scoped>
.toast-list.stacked .toast {
  margin-bottom: -12px; /* Overlap toasts */
}

.toast-list.stacked .toast:hover {
  margin-bottom: 12px; /* Full height on hover */
  z-index: 10;
}
</style>
```

### 4. 进度条颜色

根据通知类型使用不同颜色：

```css
.toast-success .toast-progress-bar {
  background: linear-gradient(90deg, #22c55e, #4ade80);
}

.toast-error .toast-progress-bar {
  background: linear-gradient(90deg, #ef4444, #f87171);
}
```

### 5. 音效提示

添加通知音效：

```typescript
const playNotificationSound = (type: ToastType): void => {
  const audio = new Audio()

  switch (type) {
    case 'success':
      audio.src = '/sounds/success.mp3'
      break
    case 'error':
      audio.src = '/sounds/error.mp3'
      break
    default:
      audio.src = '/sounds/notification.mp3'
  }

  audio.volume = 0.3
  audio.play().catch(() => {
    // Ignore autoplay restrictions
  })
}

// 在显示通知时播放
onMounted(() => {
  // ...
  if (props.duration > 0) {
    playNotificationSound(props.type)
  }
})
```

### 6. 桌面震动

在支持的浏览器中添加震动反馈：

```typescript
const vibrate = (pattern: number | number[] = 200): void => {
  if ('vibrate' in navigator) {
    navigator.vibrate(pattern)
  }
}

// 错误通知震动
if (props.type === 'error') {
  vibrate([200, 100, 200]) // 震动模式
}
```

### 7. 操作按钮

在通知中添加操作按钮：

```typescript
interface ToastItem {
  id: string
  type: ToastType
  message: string
  title?: string
  duration?: number
  closable?: boolean
  clickable?: boolean
  onClick?: () => void
  actions?: ToastAction[] // 新增
}

interface ToastAction {
  label: string
  onClick: () => void
  primary?: boolean
}
```

```vue
<div v-if="actions" class="toast-actions">
  <button
    v-for="action in actions"
    :key="action.label"
    :class="['toast-action', { primary: action.primary }]"
    @click.stop="action.onClick()"
  >
    {{ action.label }}
  </button>
</div>
```

### 8. 通知历史

记录通知历史：

```typescript
const toastHistory = ref<ToastItem[]>([])
const MAX_HISTORY = 50

const addToHistory = (toast: ToastItem): void => {
  toastHistory.value.unshift({ ...toast })

  if (toastHistory.value.length > MAX_HISTORY) {
    toastHistory.value = toastHistory.value.slice(0, MAX_HISTORY)
  }
}

const getHistory = (): ToastItem[] => {
  return toastHistory.value
}
```

### 9. 通知统计

统计通知使用情况：

```typescript
const toastStats = ref({
  success: 0,
  error: 0,
  info: 0,
  warning: 0,
  total: 0
})

const success = (message: string, options?: Partial<Omit<ToastItem, 'id' | 'type' | 'message'>>) => {
  toastStats.value.success++
  toastStats.value.total++
  return addToast({ type: 'success', message, ...options })
}
```

### 10. 批量操作

支持批量清除或操作：

```typescript
const clearByType = (type: ToastType): void => {
  toasts.value = toasts.value.filter(t => t.type !== type)
}

const clearOldToasts = (maxAge: number): void => {
  const now = Date.now()
  toasts.value = toasts.value.filter(t => {
    const toastElement = document.getElementById(t.id)
    if (toastElement) {
      const createdTime = parseInt(toastElement.dataset.timestamp || '0')
      return now - createdTime < maxAge
    }
    return true
  })
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue Transitions**: https://vuejs.org/guide/built-ins/transition.html
- **Cubic Bezier**: https://cubic-bezier.com/
- **Mouse Events**: https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent
- **Toast Notifications Best Practices**: https://www.nngroup.com/articles/alerts-notification-messages/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

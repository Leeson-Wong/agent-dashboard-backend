# Toast 通知系统

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户在执行操作时需要反馈，比如暂停 Agent、导出日志等。当前只有控制台日志，用户在界面上看不到操作结果。

**目标**:
1. 创建可复用的 Toast 通知组件
2. 支持多种类型：成功、错误、信息、警告
3. 自动消失，可手动关闭
4. 显示进度条指示剩余时间

---

## 实现方案

### 1. Toast 组件

**文件**: `src/components/Toast.vue`

功能:
- 显示单个 Toast 通知
- 支持四种类型：`success`, `error`, `info`, `warning`
- 自动消失，可配置持续时间
- 可手动关闭
- 显示进度条
- 进入/离开动画

**Props**:
```typescript
interface Props {
  id: string
  type: ToastType
  message: string
  title?: string
  duration?: number      // 默认 3000ms
  closable?: boolean     // 默认 true
  clickable?: boolean    // 默认 false
  onClose?: () => void
}
```

**UI 设计**:
- 左侧带图标的圆角方块
- 不同类型用不同颜色
- 右上角关闭按钮（可配置）
- 底部进度条显示剩余时间

### 2. ToastContainer 组件

**文件**: `src/components/ToastContainer.vue`

功能:
- 管理多个 Toast 通知
- 固定在右上角
- 垂直排列多个通知
- 响应式布局

**Props**:
```typescript
interface Props {
  toasts: ToastItem[]
}

interface Emits {
  (e: 'remove', id: string): void
}
```

### 3. useToast Composable

**文件**: `src/composables/useToast.ts`

功能:
- 管理 Toast 状态
- 提供便捷方法
- 全局单例模式

**API**:
```typescript
const {
  toasts,      // Toast 列表
  addToast,    // 添加 Toast
  removeToast, // 删除 Toast
  clearAll,    // 清空所有
  success,     // 成功通知
  error,       // 错误通知
  info,        // 信息通知
  warning,     // 警告通知
} = useToast()
```

**使用示例**:
```typescript
// 简单用法
success('操作成功')

// 带标题
error('操作失败', { title: '错误' })

// 自定义持续时间
info('正在处理...', { duration: 5000 })

// 不自动关闭
warning('请注意', { duration: 0 })

// 点击事件
const toast = useToast()
toast.success('可点击', {
  clickable: true,
  onClick: () => console.log('clicked')
})
```

---

## 集成到 App.vue

### 1. 导入组件和 composable

```typescript
import ToastContainer from './components/ToastContainer.vue'
import { useToast, toastList } from './composables/useToast'

// 获取 removeToast 方法
const { removeToast } = useToast()
```

### 2. 添加 ToastContainer 到模板

```vue
<template>
  <div class="app">
    <!-- ... 其他内容 ... -->

    <!-- Toast Container -->
    <ToastContainer :toasts="toastList" @remove="removeToast" />
  </div>
</template>
```

### 3. 在各功能中添加通知

#### Agent 暂停/恢复

```typescript
const pauseAgent = async (agentId: string): Promise<void> => {
  const { success, error } = useToast()

  try {
    const api = getAPIClient()
    const agent = agents.value.find(a => a.agentId === agentId)
    if (!agent) return

    if (agent.status === 'paused') {
      await api.resumeAgent(agentId)
      success(`Agent ${agent.role || agentId} 已恢复运行`)
    } else {
      await api.pauseAgent(agentId)
      success(`Agent ${agent.role || agentId} 已暂停`)
    }
  } catch (err) {
    error('操作失败，请稍后重试')
  }
}
```

#### 日志导出

```typescript
const exportLogs = (logsToExport: LogEntry[]): void => {
  const { success } = useToast()

  // ... 导出逻辑 ...

  success('日志已导出')
}
```

#### Agent 状态变化

```typescript
// WebSocket 消息处理
if (oldStatus !== newStatus) {
  const { info, warning } = useToast()

  if (newStatus === 'online' || newStatus === 'ready') {
    info(`Agent ${agentState.role || agentState.agentId} 已上线`)
  } else if (newStatus === 'offline') {
    info(`Agent ${agentState.role || agentState.agentId} 已离线`)
  } else if (newStatus === 'error') {
    warning(`Agent ${agentState.role || agentState.agentId} 发生错误`)
  }
}
```

---

## UI 效果

### Toast 样式

```
┌─────────────────────────────────────┐
│ ✓  操作成功                      ×  │
│                                     │
│ ━━━━━━━━━━━━━━━━━━━━━━━            │ ← 进度条
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ✕  操作失败                      ×  │
│                                     │
│ ━━━━━━━━━━━━━                      │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ⓘ  信息提示                      ×  │
│                                     │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━      │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ⚠  警告                          ×  │
│                                     │
│ ━━━━━━━━━━━━━━━                   │
└─────────────────────────────────────┘
```

### 位置布局

```
┌─────────────────────────────────────────────────────────────┐
│  Header                                                     │
│                                                              │
│                    ┌─────────────────────────┐               │
│                    │ ✓ 操作成功            × │               │
│                    └─────────────────────────┘               │
│                    ┌─────────────────────────┐               │
│                    │ ✓ 已暂停 Agent         × │               │
│                    └─────────────────────────┘               │
│                                                              │
│  Main Content                                                │
└─────────────────────────────────────────────────────────────┘
     ↑
     固定在右上角 (z-index: 9999)
```

---

## 测试步骤

### 1. 组件测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **在浏览器控制台测试**:
```javascript
// 从全局获取 useToast (需要在某处暴露它)
// 或者通过实际操作触发

// 测试成功通知
// 点击暂停 Agent 按钮 → 应该显示成功 Toast

// 测试导出日志
// 打开日志面板 → 点击导出 → 应该显示"日志已导出"
```

### 2. 功能验证

| 操作 | 预期 Toast |
|------|-----------|
| 暂停 Agent | `✓ Agent [角色] 已暂停` |
| 恢复 Agent | `✓ Agent [角色] 已恢复运行` |
| 导出日志 | `✓ 日志已导出` |
| Agent 上线 | `ⓘ Agent [角色] 已上线` |
| Agent 离线 | `ⓘ Agent [角色] 已离线` |
| Agent 错误 | `⚠ Agent [角色] 发生错误` |
| 操作失败 | `✕ 操作失败，请稍后重试` |

### 3. 交互测试

1. **自动消失**: Toast 应在 3 秒后自动消失
2. **手动关闭**: 点击 × 按钮应立即关闭
3. **进度条**: 底部进度条应显示剩余时间
4. **多个 Toast**: 多个通知应垂直排列
5. **动画**: 进入和离开应有滑入/滑出动画

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| Toast 组件渲染 | ✅ 通过 | 正确显示 |
| ToastContainer 渲染 | ✅ 通过 | 正确排列 |
| 四种类型样式 | ✅ 完成 | 成功/错误/信息/警告 |
| 自动消失 | ✅ 完成 | 3秒后消失 |
| 手动关闭 | ✅ 完成 | 点击 × 按钮 |
| 进度条显示 | ✅ 完成 | 显示剩余时间 |
| 进入/离开动画 | ✅ 完成 | 滑入/滑出效果 |
| 多个 Toast 排列 | ✅ 完成 | 垂直堆叠 |
| 暂停/恢复通知 | ✅ 完成 | 集成到 App.vue |
| 日志导出通知 | ✅ 完成 | 集成到 App.vue |
| Agent 状态通知 | ✅ 完成 | WebSocket 触发 |

---

## 文件清单

### 新增文件

1. **agent-dashboard-frontend/src/components/Toast.vue**
   - 单个 Toast 通知组件
   - 支持四种类型
   - 自动/手动关闭
   - 进度条动画

2. **agent-dashboard-frontend/src/components/ToastContainer.vue**
   - Toast 容器组件
   - 管理多个 Toast
   - 固定定位

3. **agent-dashboard-frontend/src/composables/useToast.ts**
   - Toast 管理 composable
   - 全局状态管理
   - 便捷方法

### 修改文件

1. **agent-dashboard-frontend/src/App.vue**
   - 导入 ToastContainer 和 useToast
   - 添加 ToastContainer 到模板
   - 在 pauseAgent 中添加通知
   - 在 exportLogs 中添加通知
   - 在 WebSocket handler 中添加状态通知

---

## CSS 动画

### Toast 进入动画

```css
.toast-success-enter-active,
.toast-error-enter-active,
.toast-info-enter-active,
.toast-warning-enter-active {
  transition: all 0.3s ease;
}

.toast-success-enter-from,
.toast-error-enter-from,
.toast-info-enter-from,
.toast-warning-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
```

### Toast 离开动画

```css
.toast-success-leave-active,
.toast-error-leave-active,
.toast-info-leave-active,
.toast-warning-leave-active {
  transition: all 0.3s ease;
}

.toast-success-leave-to,
.toast-error-leave-to,
.toast-info-leave-to,
.toast-warning-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
```

### 进度条动画

```css
.toast-progress-bar {
  height: 100%;
  background: currentColor;
  transition: width 0.05s linear;
}
```

---

## 优化建议

### 1. Toast 位置配置

允许用户配置 Toast 显示位置：

```typescript
interface ToastOptions {
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left' | 'top-center' | 'bottom-center'
}
```

### 2. 最大 Toast 数量

限制同时显示的 Toast 数量，避免堆叠过多：

```typescript
const maxToasts = 5

const addToast = (toast: Omit<ToastItem, 'id'>): string => {
  // ...

  if (toasts.value.length >= maxToasts) {
    toasts.value.shift() // 移除最旧的
  }

  // ...
}
```

### 3. 音效反馈

为不同类型的 Toast 添加音效：

```typescript
const playSound = (type: ToastType) => {
  const audio = new Audio(`/sounds/${type}.mp3`)
  audio.play()
}
```

### 4. 持久化重要通知

允许某些重要通知持久化，直到用户手动关闭：

```typescript
warning('重要警告', {
  duration: 0,        // 不自动消失
  persistent: true,   // 标记为持久化
  closable: true
})
```

---

## 已知问题

无

---

## 参考资料

- **Vue Transitions**: https://vuejs.org/guide/built-ins/transition.html
- **Vue Composables**: https://vuejs.org/guide/reusability/composables.html

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# 页面标题动态更新

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要在浏览器标签页中快速查看Agent状态，无需切换到应用窗口。动态更新的页面标题可以提供即时可见的状态信息。

**目标**:
1. 在页面标题中显示WebSocket连接状态
2. 显示Agent在线/总数统计
3. 当状态变化时自动更新标题

---

## 实现方案

### 前端修改 (App.vue)

#### 1. 添加 pageTitle computed

```typescript
// Page title - dynamic update
const pageTitle = computed(() => {
  const { online, total } = stats
  const status = wsConnected.value ? '●' : '○'
  return `(${status} ${online}/${total}) Agent Dashboard`
})
```

#### 2. 添加 watch 监听

```typescript
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'

// Update document title when relevant state changes
watch([pageTitle, selectedAgent], ([newTitle]) => {
  document.title = newTitle
}, { immediate: true })
```

---

## 功能说明

### 标题格式

```
(连接状态 在线/总数) Agent Dashboard
```

- **连接已连接**: `(● 6/6) Agent Dashboard`
- **连接未连接**: `(○ 6/6) Agent Dashboard`
- **0个Agent**: `(● 0/0) Agent Dashboard`

### 状态图标

| 状态 | 图标 | 说明 |
|------|------|------|
| 已连接 | ● | WebSocket 连接正常 |
| 未连接 | ○ | WebSocket 未连接 |

### 自动更新场景

标题会在以下情况自动更新：

1. **WebSocket 连接状态变化**
   - 连接成功: `○` → `●`
   - 连接断开: `●` → `○`

2. **Agent 数量变化**
   - 新Agent上线: `(● 5/6)` → `(● 6/6)`
   - Agent下线: `(● 6/6)` → `(● 5/6)`

3. **Agent 在线状态变化**
   - Agent上线: `(● 5/6)` → `(● 6/6)`
   - Agent下线: `(● 6/6)` → `(● 5/6)`

---

## UI 效果

### 浏览器标签页

```
┌─────────────────────────────────────────────────────────────┐
│ (● 6/6) Agent Dashboard        ×                        │  ← 标签页标题
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  应用内容                                                    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 标题变化示例

```
初始加载:   (○ 0/0) Agent Dashboard
连接成功:   (● 6/6) Agent Dashboard
Agent上线:  (● 7/7) Agent Dashboard
连接断开:   (○ 7/7) Agent Dashboard
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **观察标签页标题**:
   - 初始加载时应显示: `(○ 0/0) Agent Dashboard`
   - WebSocket连接成功后应变为: `(● 6/6) Agent Dashboard`

3. **测试动态更新**:
   - 暂停一个Agent → 标题应更新为 `(● 5/6) Agent Dashboard`
   - 恢复Agent → 标题应更新为 `(● 6/6) Agent Dashboard`

4. **测试连接状态**:
   - 停止后端服务 → 标题应变为 `(○ 6/6) Agent Dashboard`
   - 重启后端 → 标题应变为 `(● 6/6) Agent Dashboard`

### 2. 多标签页测试

| 场景 | 预期结果 |
|------|----------|
| 打开多个标签页 | 每个标签页标题独立更新 |
| 切换到其他应用 | 标题保持最新状态 |
| 返回标签页 | 标题显示当前实际状态 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 初始标题显示 | ✅ 通过 | 显示 (○ 0/0) |
| 连接后更新 | ✅ 通过 | 显示 (● 6/6) |
| Agent数量变化 | ✅ 通过 | 统计数字正确更新 |
| 连接状态变化 | ✅ 通过 | 图标正确切换 |
| 自动更新 | ✅ 完成 | 无需手动刷新 |
| 多标签页 | ✅ 通过 | 每个标签独立 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/App.vue**
   - 导入 watch (line 128)
   - 添加 pageTitle computed (line 209-214)
   - 添加 watch 监听 (line 216-219)

---

## 技术细节

### Watch 配置

```typescript
watch([pageTitle, selectedAgent], ([newTitle]) => {
  document.title = newTitle
}, { immediate: true })
```

- **immediate: true**: 组件加载时立即执行一次，设置初始标题
- **依赖项**: pageTitle（依赖stats和wsConnected）
- **自动响应**: 任何依赖项变化都会触发标题更新

### 响应式链

```
wsConnected ────┐
                 ├──> pageTitle ──> document.title
stats ───────────┘

其中:
  wsConnected 变化 → 触发 pageTitle 更新
  stats 变化 → 触发 pageTitle 更新
  pageTitle 更新 → 触发 watch → 更新 document.title
```

---

## 优化建议

### 1. 标题简化

当所有Agent都在线时，可以简化标题：

```typescript
const pageTitle = computed(() => {
  const { online, total } = stats
  const status = wsConnected.value ? '●' : '○'

  if (online === total && total > 0) {
    return `(${status} ${total} Agents) Dashboard`
  }
  return `(${status} ${online}/${total}) Agent Dashboard`
})
```

### 2. 添加错误指示

当有Agent处于错误状态时，显示警告：

```typescript
const pageTitle = computed(() => {
  const { online, total } = stats
  const status = wsConnected.value ? '●' : '○'

  // Check for errors
  const hasErrors = agents.value.some(a => a.status === 'error')
  const indicator = hasErrors ? '⚠' : ''

  return `(${status} ${online}/${total}${indicator}) Agent Dashboard`
})
```

### 3. 显示选中Agent

当选中Agent时，在标题中显示：

```typescript
watch([pageTitle, selectedAgent], ([newTitle]) => {
  if (selectedAgent.value) {
    const agent = agents.value.find(a => a.agentId === selectedAgent.value)
    const name = agent?.role || agent?.agentId || 'Agent'
    document.title = `${newTitle} - ${name}`
  } else {
    document.title = newTitle
  }
}, { immediate: true })
```

### 4. 页面可见性优化

只在页面可见时更新标题，节省性能：

```typescript
watch(pageTitle, (newTitle) => {
  if (document.visibilityState === 'visible') {
    document.title = newTitle
  }
})
```

---

## 已知问题

无

---

## 参考资料

- **Document.title API**: https://developer.mozilla.org/en-US/docs/Web/API/Document/title
- **Vue Watch**: https://vuejs.org/guide/essentials/watchers.html

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

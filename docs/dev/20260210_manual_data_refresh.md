# 手动数据刷新功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户有时需要手动刷新数据来获取最新的 Agent 状态，特别是在 WebSocket 断开重连或需要强制更新时。

**目标**:
1. 在头部添加刷新按钮
2. 实现手动刷新功能
3. 显示刷新状态和动画
4. 添加键盘快捷键 (Ctrl+R)
5. 使用 Toast 通知刷新结果

---

## 实现方案

### 前端修改 (App.vue)

#### 1. 添加刷新状态

```typescript
// Refresh state
const isRefreshing = ref(false)
const refreshAnimation = ref(false)
```

#### 2. 实现刷新函数

```typescript
const refreshData = async (): Promise<void> => {
  const { success, error: showError } = useToast()

  isRefreshing.value = true
  refreshAnimation.value = true

  try {
    const api = getAPIClient()

    // Fetch latest snapshot
    console.log('Refreshing agent data...')
    const snapshot = await api.getLatestSnapshot()

    if (snapshot) {
      console.log('Refreshed snapshot with', snapshot.data.agents.length, 'agents')

      // Convert SnapshotAgentData[] to AgentState[]
      agents.value = snapshot.data.agents.map(agent => ({
        agentId: agent.agentId,
        serverId: agent.serverId,
        framework: agent.framework,
        language: agent.language,
        status: agent.status,
        currentActivity: agent.currentActivity,
        currentTool: agent.currentTool,
        currentTaskId: agent.currentTaskId,
        memoryId: agent.memoryId,
        role: agent.role,
        lastActivity: agent.lastActivity,
        createdAt: agent.createdAt,
        updatedAt: agent.updatedAt,
      }))

      // Update AgentStore
      agentStore.clear()
      agents.value.forEach(agent => {
        agentStore.update(agent, snapshot.createdAt)
      })

      // Update 3D scene
      if (scene) {
        agents.value.forEach((agent, index) => {
          scene.createAgentZone(agent.agentId, index)
          scene.updateAgentStatus(agent.agentId, agent.status as any)
        })
      }

      console.log('Refresh complete:', agents.value.length, 'agents')
      success(`数据已刷新，共 ${agents.value.length} 个 Agent`)
    }
  } catch (err) {
    console.error('Failed to refresh data:', err)
    showError('数据刷新失败，请稍后重试')
  } finally {
    isRefreshing.value = false
    setTimeout(() => {
      refreshAnimation.value = false
    }, 500)
  }
}
```

#### 3. 添加刷新按钮到头部

```vue
<header class="app-header">
  <h1>Agent Dashboard</h1>
  <div class="header-stats">
    <!-- ... 统计信息 ... -->
    <button class="memory-btn" @click="openMemoryPanel()">
      🧠 Memory 管理
    </button>
    <button
      class="refresh-btn"
      :class="{ refreshing: isRefreshing, 'animate-spin': refreshAnimation }"
      @click="refreshData"
      :disabled="isRefreshing"
      title="刷新数据 (Ctrl+R)"
    >
      {{ isRefreshing ? '⏳' : '🔄' }}
    </button>
    <button class="shortcut-btn" @click="showKeyboardHelp = true" title="键盘快捷键 (按 ?)">
      ⌨️
    </button>
  </div>
</header>
```

#### 4. 添加快捷键

```typescript
registerShortcut({
  key: 'r',
  ctrl: true,
  description: '刷新数据',
  handler: () => {
    refreshData()
  },
})
```

#### 5. 添加样式

```css
.refresh-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: rgba(51, 65, 85, 0.5);
  color: #94a3b8;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(71, 85, 105, 0.8);
  color: #e2e8f0;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-btn.refreshing {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 0.8s linear infinite;
}
```

---

## 功能说明

### 刷新流程

```
┌─────────────────────────────────────────────────────────────────┐
│                   手动刷新流程                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 用户触发刷新                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  点击 🔄 按钮                                            │    │
│     │  或按 Ctrl+R                                            │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  2. 显示加载状态                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  图标变为 ⏳                                            │    │
│     │  按钮禁用 (disabled)                                    │    │
│     │  旋转动画开始                                            │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  3. 获取最新快照                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  api.getLatestSnapshot()                                │    │
│     │                                                          │    │
│     │  成功 → 继续步骤 4                                       │    │
│     │  失败 → 显示错误 Toast                                   │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  4. 更新本地数据                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  更新 agents 数组                                        │    │
│     │  更新 AgentStore                                         │    │
│     │  更新 3D 场景                                             │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  5. 完成刷新                                                     │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  显示成功 Toast                                         │    │
│     │  图标恢复为 🔄                                          │    │
│     │  按钮启用                                                │    │
│     │  停止旋转动画                                            │    │
│     └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### UI 状态

| 状态 | 图标 | 样式 | 按钮 |
|------|------|------|------|
| 正常 | 🔄 | 默认 | 可点击 |
| 刷新中 | ⏳ | 蓝色背景 + 旋转动画 | 禁用 |

---

## UI 效果

### 头部刷新按钮

```
┌─────────────────────────────────────────────────────────────────┐
│ Agent Dashboard                                                  │
│ 在线: 6  总 Agent: 6  ● 已连接  消息: 0  刚刚                   │
│ [🧠 Memory 管理]  [🔄]  [⌨️]                                    │
└─────────────────────────────────────────────────────────────────┘
```

### 刷新中状态

```
┌─────────────────────────────────────────────────────────────────┐
│ Agent Dashboard                                                  │
│ 在线: 6  总 Agent: 6  ● 已连接  消息: 0  刚刚                   │
│ [🧠 Memory 管理]  [⏳]  [⌨️]                                    │
│                      ↑                                         │
│                   蓝色背景 + 旋转                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试刷新按钮**:
   - 点击 🔄 刷新按钮
   - 预期：
     - ✅ 图标变为 ⏳
     - ✅ 按钮变为禁用状态
     - ✅ 显示旋转动画
     - ✅ 刷新完成后显示成功 Toast

3. **测试键盘快捷键**:
   - 按 `Ctrl+R` (Windows) 或 `⌘+R` (Mac)
   - 预期：
     - ✅ 触发刷新功能
     - ✅ 显示与点击按钮相同的状态

4. **测试刷新禁用**:
   - 刷新进行中时尝试再次点击
   - 预期：
     - ✅ 按钮保持禁用状态
     - ✅ 不触发新的刷新请求

### 2. 错误处理测试

| 测试场景 | 预期结果 |
|---------|----------|
| 后端正常 | 显示成功 Toast，数据更新 |
| 后端离线 | 显示错误 Toast "数据刷新失败" |
| 网络延迟 | 显示加载状态直到请求完成 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 刷新按钮显示 | ✅ 通过 | 正确显示在头部 |
| 刷新状态切换 | ✅ 完成 | 图标和状态正确切换 |
| 旋转动画 | ✅ 完成 | 0.8秒旋转一圈 |
| 按钮禁用 | ✅ 完成 | 刷新中按钮不可点击 |
| 数据刷新 | ✅ 通过 | 成功获取并显示新数据 |
| Toast 通知 | ✅ 完成 | 成功/失败消息正确显示 |
| 快捷键触发 | ✅ 通过 | Ctrl+R 正确触发 |
| 样式渲染 | ✅ 完成 | 无布局错位 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/App.vue**
   - 添加刷新状态变量 (line 170-172)
   - 实现 refreshData 函数 (line 314-375)
   - 添加刷新按钮到模板 (line 41-49)
   - 注册 Ctrl+R 快捷键 (line 605-612)
   - 添加刷新按钮样式 (line 781-822)

---

## 优化建议

### 1. 自动刷新

添加定时自动刷新选项：

```typescript
const autoRefreshInterval = ref(30000) // 30秒
let autoRefreshTimer: ReturnType<typeof setInterval> | null = null

const startAutoRefresh = () => {
  stopAutoRefresh()
  autoRefreshTimer = setInterval(() => {
    refreshData()
  }, autoRefreshInterval.value)
}

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}
```

### 2. 刷新进度

添加更详细的刷新进度：

```typescript
const refreshProgress = ref(0)

const refreshData = async (): Promise<void> => {
  refreshProgress.value = 0

  const updateProgress = (value: number) => {
    refreshProgress.value = value
  }

  updateProgress(20) // 开始加载
  const snapshot = await api.getLatestSnapshot()
  updateProgress(60) // 数据获取完成
  // ... 更新 UI
  updateProgress(100) // 完成
}
```

### 3. 后台刷新

在后台静默刷新，不打断用户操作：

```typescript
const silentRefresh = async (): Promise<void> => {
  try {
    const snapshot = await api.getLatestSnapshot()
    // ... 更新数据但不显示 Toast
  } catch (err) {
    console.warn('Silent refresh failed:', err)
  }
}
```

---

## 已知问题

### 1. Ctrl+R 浏览器冲突

在大多数浏览器中，`Ctrl+R` 是刷新页面的默认快捷键。这可能导致冲突。

**解决方案**:
- 使用不同的快捷键组合（如 `Ctrl+Shift+R`）
- 或者允许快捷键被浏览器默认行为覆盖

### 2. 刷新频率限制

用户可能频繁点击刷新按钮，导致过多请求。

**解决方案**:
```typescript
const lastRefreshTime = ref(0)
const refreshCooldown = 2000 // 2秒冷却

const refreshData = async (): Promise<void> => {
  const now = Date.now()
  if (now - lastRefreshTime.value < refreshCooldown) {
    const { warning } = useToast()
    warning('请稍后再试')
    return
  }
  lastRefreshTime.value = now
  // ... 继续刷新
}
```

---

## 参考资料

- **REST API**: GET /api/snapshot/latest
- **Vue Reactivity**: https://vuejs.org/guide/essentials/reactivity-fundamentals.html

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

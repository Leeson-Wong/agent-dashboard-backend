# 自动刷新功能实现

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户设置面板已经添加了"自动刷新"和"刷新间隔"的 UI 控件，但实际的自动刷新逻辑尚未实现。用户需要能够配置系统自动刷新 Agent 数据，而无需手动点击刷新按钮。

**问题**:
1. 设置面板中的"自动刷新"开关没有实际功能
2. "刷新间隔"选择器不生效
3. 用户需要手动刷新才能获取最新数据

**目标**:
1. 实现自动刷新功能，根据用户配置的间隔自动刷新数据
2. 支持动态调整刷新间隔
3. 正确处理刷新状态（避免刷新冲突）
4. 在组件卸载时正确清理定时器

---

## 实现方案

### 前端实现

#### 修改 App.vue

**文件**: `src/App.vue`

**新增状态变量**:
```typescript
// Auto-refresh state
let autoRefreshTimer: ReturnType<typeof setInterval> | null = null
const userSettings = ref<UserSettingsType | null>(null)
```

**新增函数**:

**启动自动刷新**:
```typescript
const startAutoRefresh = (interval: number): void => {
  // 先停止现有定时器
  stopAutoRefresh()
  console.log('Starting auto-refresh with interval:', interval)
  // 创建新的定时器
  autoRefreshTimer = setInterval(() => {
    // 仅在当前没有刷新操作时执行
    if (!isRefreshing.value) {
      console.log('Auto-refreshing data...')
      refreshData()
    }
  }, interval)
}
```

**停止自动刷新**:
```typescript
const stopAutoRefresh = (): void => {
  if (autoRefreshTimer) {
    console.log('Stopping auto-refresh')
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}
```

**加载用户设置**:
```typescript
const loadUserSettings = (): void => {
  const saved = localStorage.getItem('userSettings')
  if (saved) {
    try {
      userSettings.value = JSON.parse(saved)
      // 如果启用了自动刷新，启动定时器
      if (userSettings.value?.autoRefresh && userSettings.value?.refreshInterval) {
        startAutoRefresh(userSettings.value.refreshInterval)
      }
    } catch (e) {
      console.error('Failed to parse user settings:', e)
    }
  }
}
```

**更新设置变更处理**:
```typescript
const handleSettingsChange = (settings: UserSettingsType): void => {
  const { success } = useToast()
  console.log('Settings changed:', settings)

  // 更新用户设置引用
  userSettings.value = settings

  // 应用调试模式
  config.debugMode = settings.debugMode

  // 应用自动刷新
  if (settings.autoRefresh && settings.refreshInterval) {
    startAutoRefresh(settings.refreshInterval)
    console.log('Auto-refresh enabled with interval:', settings.refreshInterval)
  } else {
    stopAutoRefresh()
    console.log('Auto-refresh disabled')
  }

  // 应用 WS 日志
  // This would require updating the WebSocket connection

  success('设置已保存')
}
```

**生命周期更新**:

**onMounted**:
```typescript
onMounted(async () => {
  if (!sceneContainer.value) return

  // 首先加载用户设置（包括自动刷新）
  loadUserSettings()

  // Create 3D scene
  scene = new AgentScene(sceneContainer.value)
  // ...
})
```

**onUnmounted**:
```typescript
onUnmounted(() => {
  if (scene) {
    scene.dispose()
    scene = null
  }
  if (ws) {
    ws.disconnect()
  }
  teardownGlobalKeyboard(handleKeydown)
  // 清理自动刷新定时器
  stopAutoRefresh()
})
```

---

## 技术细节

### 刷新冲突处理

自动刷新逻辑会在执行前检查 `isRefreshing.value` 状态，避免在手动刷新过程中触发自动刷新：

```typescript
if (!isRefreshing.value) {
  console.log('Auto-refreshing data...')
  refreshData()
}
```

### 定时器管理

使用全局变量 `autoRefreshTimer` 存储定时器 ID，确保：
1. 启动新定时器前先停止旧的（避免多个定时器同时运行）
2. 组件卸载时清理定时器（避免内存泄漏）

### 设置持久化

用户设置存储在 `localStorage` 中，页面刷新后会自动加载并应用自动刷新配置。

---

## 工作流程

### 初始化流程

```
1. onMounted 触发
   ↓
2. loadUserSettings() 从 localStorage 读取设置
   ↓
3. 检查 autoRefresh 和 refreshInterval
   ↓
4. 如果启用 → startAutoRefresh(interval)
   ↓
5. 创建 setInterval 定时器
```

### 设置变更流程

```
1. 用户在设置面板修改配置
   ↓
2. @settings-change 事件触发
   ↓
3. handleSettingsChange(settings) 被调用
   ↓
4. 检查 settings.autoRefresh
   ├─ true → startAutoRefresh(settings.refreshInterval)
   └─ false → stopAutoRefresh()
   ↓
5. 显示"设置已保存"提示
```

### 卸载清理流程

```
1. onUnmounted 触发
   ↓
2. stopAutoRefresh() 清理定时器
   ↓
3. clearInterval(autoRefreshTimer)
   ↓
4. autoRefreshTimer = null
```

---

## UI 效果

### 设置面板

```
┌────────────────────────────────────────────────────────────┐
│  数据设置                                                  │
│                                                            │
│  自动刷新                                [●] ON            │
│                                                            │
│  刷新间隔          [5 秒          ▼]                       │
│                    • 5 秒                                  │
│                    • 10 秒                                 │
│                    • 30 秒                                 │
│                    • 60 秒                                 │
└────────────────────────────────────────────────────────────┘
```

### 自动刷新行为

| 用户操作 | 系统行为 |
|---------|---------|
| 启用自动刷新 + 选择 10 秒 | 每 10 秒自动刷新数据 |
| 禁用自动刷新 | 停止自动刷新 |
| 修改刷新间隔 | 停止旧定时器，启动新定时器 |
| 手动刷新时 | 跳过本次自动刷新（避免冲突） |
| 刷新页面 | 自动恢复自动刷新状态 |

---

## 测试步骤

### 1. 启用自动刷新测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 启用 5 秒刷新 | 打开自动刷新，选择 5 秒 | 每 5 秒数据自动更新 |
| 启用 30 秒刷新 | 打开自动刷新，选择 30 秒 | 每 30 秒数据自动更新 |
| 启用 60 秒刷新 | 打开自动刷新，选择 60 秒 | 每 60 秒数据自动更新 |

### 2. 禁用自动刷新测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 禁用刷新 | 关闭自动刷新开关 | 定时器停止，不再自动刷新 |

### 3. 刷新间隔变更测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 缩短间隔 | 从 60 秒改为 5 秒 | 立即应用新间隔 |
| 延长间隔 | 从 5 秒改为 60 秒 | 立即应用新间隔 |

### 4. 刷新冲突测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 手动刷新时 | 在手动刷新期间 | 跳过自动刷新 |

### 5. 持久化测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 刷新页面 | 启用自动刷新后刷新 | 自动恢复自动刷新 |
| 关闭重开 | 启用自动刷新后关闭重开 | 自动恢复自动刷新 |

### 6. 清理测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 组件卸载 | 导航离开页面 | 定时器被清理 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 自动刷新变量声明 | ✅ 通过 | autoRefreshTimer 和 userSettings 正确声明 |
| startAutoRefresh 函数 | ✅ 通过 | 正确创建定时器 |
| stopAutoRefresh 函数 | ✅ 通过 | 正确清理定时器 |
| loadUserSettings 函数 | ✅ 通过 | 正确加载和启动自动刷新 |
| handleSettingsChange 更新 | ✅ 通过 | 正确处理自动刷新设置 |
| onMounted 集成 | ✅ 通过 | 页面加载时调用 loadUserSettings |
| onUnmounted 清理 | ✅ 通过 | 卸载时清理定时器 |
| 刷新冲突处理 | ✅ 通过 | 检查 isRefreshing 状态 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/App.vue**
   - 添加自动刷新状态变量 (lines 216-218)
   - 添加 startAutoRefresh 函数 (lines 466-476)
   - 添加 stopAutoRefresh 函数 (lines 478-484)
   - 添加 loadUserSettings 函数 (lines 486-500)
   - 更新 handleSettingsChange 函数 (lines 502-526)
   - 更新 onMounted (line 532-533)
   - 更新 onUnmounted (lines 855-856)

---

## 后续功能建议

### 1. 智能刷新间隔

根据数据活跃度自动调整刷新间隔：

```typescript
const getOptimalInterval = (baseInterval: number): number => {
  const activityLevel = calculateActivityLevel()
  if (activityLevel === 'high') return baseInterval * 0.5  // 活跃时加快
  if (activityLevel === 'low') return baseInterval * 2    // 不活跃时减慢
  return baseInterval
}
```

### 2. 后台暂停刷新

页面不可见时暂停自动刷新：

```typescript
document.addEventListener('visibilitychange', () => {
  if (document.hidden && autoRefreshTimer) {
    stopAutoRefresh()
  } else if (!document.hidden && userSettings.value?.autoRefresh) {
    startAutoRefresh(userSettings.value.refreshInterval)
  }
})
```

### 3. 网络状态感知

网络断开时暂停刷新，恢复后重新开始：

```typescript
window.addEventListener('online', () => {
  if (userSettings.value?.autoRefresh) {
    startAutoRefresh(userSettings.value.refreshInterval)
  }
})

window.addEventListener('offline', () => {
  stopAutoRefresh()
})
```

### 4. 刷新失败重试

自动刷新失败时增加重试间隔：

```typescript
let consecutiveFailures = 0

const startAutoRefresh = (interval: number): void => {
  stopAutoRefresh()
  autoRefreshTimer = setInterval(async () => {
    if (!isRefreshing.value) {
      try {
        await refreshData()
        consecutiveFailures = 0
      } catch (e) {
        consecutiveFailures++
        if (consecutiveFailures > 3) {
          stopAutoRefresh()
          showError('自动刷新失败次数过多，已停止')
        }
      }
    }
  }, interval)
}
```

### 5. 刷新进度提示

在界面显示下次刷新倒计时：

```vue
<div class="refresh-status">
  <span v-if="autoRefreshEnabled">
    下次刷新: {{ nextRefreshCountdown }}秒
  </span>
</div>
```

---

## 已知问题

无

---

## 参考资料

- **Vue 3 onMounted**: https://vuejs.org/api/lifecycle-cycle.html#onmounted
- **Vue 3 onUnmounted**: https://vuejs.org/api/lifecycle-cycle.html#onunmounted
- **setInterval**: https://developer.mozilla.org/en-US/docs/Web/API/setInterval
- **localStorage**: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

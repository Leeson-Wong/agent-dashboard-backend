# 加载状态管理

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

在实现骨架屏功能后，发现 AgentListPanel 组件的 loading 属性没有与实际数据加载流程关联。用户在数据加载时看不到骨架屏效果，因为 loading 状态始终为 false。

**目标**:
1. 在 App.vue 中添加 loading 状态管理
2. 在数据加载时设置 loading=true
3. 在数据加载完成后设置 loading=false
4. 将 loading 状态传递给 AgentListPanel 组件
5. 确保初始加载和手动刷新都能正确显示骨架屏

---

## 实现方案

### 前端实现

#### 1. 添加 loading 状态

**文件**: `src/App.vue`

在 reactive variables 部分添加 loading ref：

```typescript
// Refresh state
const isRefreshing = ref(false)
const refreshAnimation = ref(false)

// Loading state for agent list
const loading = ref(true)
```

**说明**:
- 初始值设为 `true`，确保首次加载时显示骨架屏
- 与 `isRefreshing` 分离，`isRefreshing` 仅用于控制刷新按钮状态

#### 2. 更新 onMounted 生命周期钩子

在 `onMounted` 中管理初始加载状态：

```typescript
onMounted(async () => {
  if (!sceneContainer.value) return

  // ... (scene initialization)

  // Load agents from backend API
  try {
    const api = getAPIClient()
    const snapshot = await api.getLatestSnapshot()

    if (snapshot) {
      // ... (process agents)
      console.log('Initial agent load complete:', agents.value.length, 'agents')
      // Loading complete
      isLoading.value = false
      loading.value = false  // ✅ 新增
    }
  } catch (error) {
    console.error('Failed to load agents from backend:', error)

    if (config.development.useMockData) {
      // ... (load mock data)
      // Loading complete with mock data
      isLoading.value = false
      loading.value = false  // ✅ 新增
    } else {
      // No mock data fallback - show error
      loadingError.value = error instanceof Error ? error.message : '无法连接到后端服务'
      loading.value = false  // ✅ 新增
    }
  }
})
```

**更新点**:
1. 在成功加载数据后设置 `loading.value = false`
2. 在加载 mock 数据后设置 `loading.value = false`
3. 在错误情况下也设置 `loading.value = false`（确保不会一直显示骨架屏）

#### 3. 更新 refreshData 函数

在手动刷新时管理 loading 状态：

```typescript
const refreshData = async (): Promise<void> => {
  const { success, error: showError } = useToast()

  isRefreshing.value = true
  refreshAnimation.value = true
  loading.value = true  // ✅ 新增：显示骨架屏

  try {
    const api = getAPIClient()

    // Fetch latest snapshot
    const snapshot = await api.getLatestSnapshot()

    if (snapshot) {
      // ... (process agents)
      success(`数据已刷新，共 ${agents.value.length} 个 Agent`)
    }
  } catch (err) {
    console.error('Failed to refresh data:', err)
    showError('数据刷新失败，请稍后重试')
  } finally {
    isRefreshing.value = false
    loading.value = false  // ✅ 新增：隐藏骨架屏
    setTimeout(() => {
      refreshAnimation.value = false
    }, 500)
  }
}
```

**更新点**:
1. 在函数开始时设置 `loading.value = true`
2. 在 finally 块中设置 `loading.value = false`
3. 确保无论成功或失败都会重置 loading 状态

#### 4. 更新模板

将 loading 状态传递给 AgentListPanel：

```vue
<template>
  <div class="app">
    <!-- ... -->

    <!-- Agent List Panel (Left) -->
    <AgentListPanel
      :agents="agents"
      :loading="loading"
      @select-agent="select-agent"
    />

    <!-- ... -->
  </div>
</template>
```

---

## 技术细节

### 状态管理对比

| 状态变量 | 用途 | 初始值 | 控制的UI |
|---------|------|--------|----------|
| `isLoading` | 初始加载遮罩层 | `true` | 全屏加载遮罩 |
| `loading` | Agent列表骨架屏 | `true` | AgentListPanel 骨架屏 |
| `isRefreshing` | 刷新按钮状态 | `false` | 刷新按钮图标和禁用状态 |
| `refreshAnimation` | 刷新动画 | `false` | 刷新按钮旋转动画 |

### 状态生命周期

```
应用启动
    ↓
isLoading = true (显示全屏遮罩)
loading = true (显示骨架屏)
    ↓
数据加载中...
    ↓
isLoading = false (隐藏全屏遮罩)
loading = false (隐藏骨架屏，显示实际内容)
    ↓
用户点击刷新按钮
    ↓
loading = true (显示骨架屏)
isRefreshing = true (禁用刷新按钮)
    ↓
数据刷新中...
    ↓
loading = false (隐藏骨架屏)
isRefreshing = false (启用刷新按钮)
```

### 错误处理

在所有错误路径中都正确设置 `loading.value = false`：

```typescript
try {
  // 加载数据
  loading.value = false  // 成功
} catch (error) {
  if (useMockData) {
    loading.value = false  // 使用 mock 数据
  } else {
    loading.value = false  // 显示错误
  }
}
```

这确保了：
- 不会出现骨架屏一直显示的问题
- 用户始终能看到当前状态的反馈

---

## UI 效果

### 加载时序图

```
┌─────────────────────────────────────────────────────┐
│ 应用启动                                            │
│ ┌─────────────────────────────────────────────────┐│
│ │ 全屏加载遮罩 (isLoading)                        ││
│ │ "正在初始化系统..."                             ││
│ │ ┌─────────────────────────────────────────────┐││
│ │ │ Agent 列表骨架屏 (loading)                  │││
│ │ │ ┌─────────────────────────────────────────┐│││
│ │ │ │ ☐ ● ▃▃▃▃▃▃ ▃▃ ▃▃▃▃▃▃▃▃                ││││
│ │ │ │ ☐ ● ▃▃▃▃▃▃ ▃▃ ▃▃▃▃▃▃▃▃                ││││
│ │ │ │ ☐ ● ▃▃▃▃▃▃ ▃▃ ▃▃▃▃▃▃▃▃                ││││
│ │ │ └─────────────────────────────────────────┘│││
│ │ └─────────────────────────────────────────────┘││
│ └─────────────────────────────────────────────────┘│
│                         ↓                           │
│ 数据加载完成                                       │
│ ┌─────────────────────────────────────────────────┐│
│ │ Agent 列表 (实际内容)                           ││
│ │ ┌─────────────────────────────────────────┐││
│ │ │ ☐ ● agent-001 在线                      │││
│ │ │ ☐ ● agent-002 思考中                    │││
│ │ │ ☐ ● agent-003 忙碌                      │││
│ │ └─────────────────────────────────────────┘││
│ └─────────────────────────────────────────────────┘│
│                         ↓                           │
│ 用户点击刷新按钮 🔄                                │
│ ┌─────────────────────────────────────────────────┐│
│ │ Agent 列表骨架屏 (loading=true)                 ││
│ │ 刷新按钮: ⏳ (isRefreshing=true)               ││
│ │ ┌─────────────────────────────────────────┐││
│ │ │ ☐ ● ▃▃▃▃▃▃ ▃▃ ▃▃▃▃▃▃▃▃                │││
│ │ │ ☐ ● ▃▃▃▃▃▃ ▃▃ ▃▃▃▃▃▃▃▃                │││
│ │ │ ☐ ● ▃▃▃▃▃▃ ▃▃ ▃▃▃▃▃▃▃▃                │││
│ │ └─────────────────────────────────────────┘││
│ └─────────────────────────────────────────────────┘│
│                         ↓                           │
│ 数据刷新完成                                       │
│ ┌─────────────────────────────────────────────────┐│
│ │ Agent 列表 (更新后的内容)                       ││
│ │ 刷新按钮: 🔄 (isRefreshing=false)              ││
│ └─────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────┘
```

### 状态转换表

| 场景 | isLoading | loading | isRefreshing | 显示内容 |
|------|-----------|---------|--------------|----------|
| 应用启动 | `true` | `true` | `false` | 全屏遮罩 + 骨架屏 |
| 加载完成 | `false` | `false` | `false` | 实际 Agent 列表 |
| 点击刷新 | `false` | `true` | `true` | 骨架屏 + 禁用刷新按钮 |
| 刷新完成 | `false` | `false` | `false` | 更新的 Agent 列表 |
| 加载失败（无mock） | `false` | `false` | `false` | 错误提示 |
| 加载失败（有mock） | `false` | `false` | `false` | Mock 数据 |

---

## 测试步骤

### 1. 初始加载测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 冷启动 | 打开应用 | 先显示全屏遮罩和骨架屏，然后显示实际数据 |
| 网络延迟 | 使用慢速网络 | 骨架屏显示时间延长，用户感知良好 |
| 空数据 | 后端返回空列表 | 骨架屏消失，显示空状态提示 |

### 2. 手动刷新测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 正常刷新 | 点击刷新按钮 | 显示骨架屏，刷新按钮变为 ⏳，完成后恢复 |
| 快速连续刷新 | 连续多次点击刷新 | 第一次刷新完成后才能再次触发 |
| 刷新失败 | 断开网络后刷新 | 显示骨架屏 → 显示错误提示 → 骨架屏消失 |

### 3. 状态同步测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| loading 与 isLoading | 同时监控两个状态 | isLoading 先结束，loading 后结束 |
| loading 与 isRefreshing | 刷新时监控状态 | 两个状态同时开始，同时结束 |
| 错误处理 | 加载失败时 | 所有状态都正确重置为 false |

### 4. 边界情况测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 刷新时切换选择 | 刷新中点击其他 Agent | 骨架屏保持显示，切换操作正常 |
| 刷新时使用过滤器 | 刷新中修改筛选条件 | 骨架屏保持显示，刷新后应用新筛选 |
| 极速刷新完成 | 使用 mock 数据（快速） | 骨架屏闪烁时间很短（<100ms） |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| loading 状态初始化 | ✅ 通过 | 初始值为 true |
| onMounted loading 管理 | ✅ 通过 | 成功路径正确设置 loading=false |
| onMounted 错误处理 | ✅ 通过 | 错误路径正确设置 loading=false |
| mock 数据 loading | ✅ 通过 | mock 数据加载后正确设置 loading=false |
| refreshData loading 管理 | ✅ 通过 | 开始时设置 loading=true |
| refreshData finally 块 | ✅ 通过 | 结束时正确设置 loading=false |
| AgentListPanel 传递 | ✅ 通过 | :loading 绑定正确 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/App.vue**
   - 添加 `loading` ref (line 208)
   - 更新 `onMounted` 成功路径 (line 552)
   - 更新 `onMounted` mock 数据路径 (line 600)
   - 更新 `onMounted` 错误路径 (line 604)
   - 更新 `refreshData` 开始 (line 394)
   - 更新 `refreshData` finally 块 (line 446)
   - 更新模板绑定 (line 67)

---

## 优化建议

### 1. 防抖处理

防止快速连续刷新导致闪烁：

```typescript
import { debounce } from 'lodash-es'

const refreshData = debounce(async (): Promise<void> => {
  // ... 刷新逻辑
}, 300)
```

### 2. 最小显示时间

确保骨架屏至少显示一定时间，避免闪烁：

```typescript
const minSkeletonTime = 500  // 最小显示 500ms

const refreshData = async (): Promise<void> => {
  const startTime = Date.now()
  loading.value = true

  try {
    // ... 加载数据
  } finally {
    const elapsed = Date.now() - startTime
    const remaining = Math.max(0, minSkeletonTime - elapsed)

    setTimeout(() => {
      loading.value = false
    }, remaining)
  }
}
```

### 3. 渐进式加载

先显示部分数据，然后逐步加载更多：

```typescript
const loading = ref<'initial' | 'partial' | 'done'>('initial')

// 先加载前 10 个
const partialAgents = await api.getAgents({ limit: 10 })
agents.value = partialAgents
loading.value = 'partial'

// 再加载剩余的
const allAgents = await api.getAgents()
agents.value = allAgents
loading.value = 'done'
```

### 4. 加载进度

在骨架屏中显示加载进度：

```typescript
const loadingProgress = ref(0)
const loading = ref(true)

const refreshData = async (): Promise<void> => {
  loadingProgress.value = 0
  loading.value = true

  try {
    const api = getAPIClient()
    loadingProgress.value = 20

    const snapshot = await api.getLatestSnapshot()
    loadingProgress.value = 60

    // 处理数据...
    loadingProgress.value = 100
  } finally {
    setTimeout(() => {
      loading.value = false
      loadingProgress.value = 0
    }, 300)
  }
}
```

模板中显示进度：

```vue
<AgentListPanel
  :agents="agents"
  :loading="loading"
  :loading-progress="loadingProgress"
  @select-agent="selectAgent"
/>
```

### 5. 错误重试

在骨架屏中添加重试按钮：

```vue
<div v-if="loading" class="skeleton-list">
  <div v-if="loadingError" class="skeleton-error">
    <div class="error-message">加载失败</div>
    <button class="btn-retry" @click="refreshData">重试</button>
  </div>
  <template v-else>
    <!-- 骨架项 -->
  </template>
</div>
```

### 6. 骨架屏缓存

缓存上次的骨架状态，避免重复渲染：

```typescript
const skeletonCache = ref<number>(6)

const refreshData = async (): Promise<void> => {
  // 使用缓存的骨架数量
  skeletonCache.value = agents.value.length || 6
  loading.value = true

  try {
    // ... 加载数据
  } finally {
    loading.value = false
  }
}
```

### 7. 智能预加载

在空闲时预加载数据：

```typescript
import { useIdleCallback } from '@vueuse/core'

const { start } = useIdleCallback(async () => {
  if (!loading.value && agents.value.length > 0) {
    console.log('Preloading agents in background...')
    await refreshData()
  }
}, { timeout: 5000 })

// 在适当的时候启动预加载
onMounted(() => {
  start()
})
```

### 8. 状态机模式

使用状态机管理复杂加载逻辑：

```typescript
type LoadingState =
  | { status: 'idle' }
  | { status: 'loading'; startTime: number }
  | { status: 'success' }
  | { status: 'error'; error: Error }

const loadingState = ref<LoadingState>({ status: 'idle' })

const isLoading = computed(() => loadingState.value.status === 'loading')
const loadingError = computed(() =>
  loadingState.value.status === 'error' ? loadingState.value.error : null
)

const refreshData = async (): Promise<void> => {
  loadingState.value = { status: 'loading', startTime: Date.now() }

  try {
    // ... 加载数据
    loadingState.value = { status: 'success' }
  } catch (error) {
    loadingState.value = { status: 'error', error: error as Error }
  }
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue 3 Reactivity**: https://vuejs.org/guide/essentials/reactivity-fundamentals.html
- **Loading State Management**: https://kentcdodds.com/blog/stop-using-isloading
- **Skeleton Screen UX**: https://www.smashingmagazine.com/2020/10/skeleton-screens-react/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

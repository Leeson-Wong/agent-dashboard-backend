# 初始加载状态功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

应用启动时会从后端获取快照和增量事件，这个过程需要一定时间。当前没有显示加载状态，用户可能会看到空白页面或不完整的内容，体验不佳。

**目标**:
1. 在应用初始化期间显示加载动画
2. 如果加载失败，显示错误信息
3. 提供重试按钮让用户可以重新加载

---

## 实现方案

### 前端修改 (App.vue)

#### 1. 添加加载状态变量

```typescript
// Initial loading state
const isLoading = ref(true)
const loadingError = ref<string | null>(null)
```

#### 2. 添加加载遮罩到模板

```vue
<!-- Initial Loading Overlay -->
<Transition name="fade">
  <div v-if="isLoading" class="loading-overlay">
    <div class="loading-content">
      <div class="loading-spinner"></div>
      <div class="loading-text">正在初始化系统...</div>
      <div v-if="loadingError" class="loading-error">
        <div class="error-icon">⚠️</div>
        <div class="error-message">{{ loadingError }}</div>
        <button class="btn-retry" @click="retryLoad">重新加载</button>
      </div>
    </div>
  </div>
</Transition>
```

#### 3. 更新数据加载逻辑

```typescript
onMounted(async () => {
  // ...

  try {
    const api = getAPIClient()
    const snapshot = await api.getLatestSnapshot()

    // ... 加载数据逻辑 ...

    console.log('Initial agent load complete:', agents.value.length, 'agents')
    // Loading complete
    isLoading.value = false
  } catch (error) {
    console.error('Failed to load agents from backend:', error)

    // Check if mock data is enabled
    if (config.development.useMockData) {
      // ... 使用 mock data ...
      isLoading.value = false
    } else {
      // No mock data fallback - show error
      loadingError.value = error instanceof Error ? error.message : '无法连接到后端服务'
    }
  }
})
```

#### 4. 添加重试函数

```typescript
const retryLoad = (): void => {
  loadingError.value = null
  isLoading.value = true
  // Reload the page to retry initialization
  window.location.reload()
}
```

#### 5. 添加 CSS 样式

```css
/* Loading Overlay */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(8px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
}

.loading-content {
  text-align: center;
  padding: 40px;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(148, 163, 184, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  color: #94a3b8;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.loading-error {
  margin-top: 24px;
  padding: 20px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
}

/* Fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
```

---

## 功能说明

### 1. 加载状态

- **显示时机**: 应用启动时（`isLoading = true`）
- **隐藏时机**: 数据加载成功后（`isLoading = false`）
- **视觉效果**:
  - 全屏深色半透明遮罩
  - 居中显示旋转动画
  - "正在初始化系统..." 提示文字

### 2. 错误状态

- **显示条件**: 数据加载失败且未启用 mock data
- **视觉效果**:
  - ⚠️ 警告图标
  - 红色错误信息框
  - 显示具体错误消息

### 3. 重试功能

- **按钮**: "重新加载"
- **行为**: 刷新页面，重新初始化应用
- **样式**: 红色半透明背景，悬停时变深

### 4. 淡入淡出动画

- 使用 Vue `<Transition>` 组件
- 300ms 淡入淡出效果
- 平滑的视觉过渡

---

## UI 效果

### 正常加载状态

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                                                                 │
│                     ╭────────╮                                  │
│                    ╏  ◌ ◌ ◌ ╏                                   │
│                    ╏   ◌    ╏     正在初始化系统...               │
│                    ╏  ◌ ◌ ◌ ╏                                   │
│                     ╰────────╯                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 错误状态

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                                                                 │
│                     ╭────────╮                                  │
│                    ╏  ◌ ◌ ◌ ╏                                   │
│                    ╏   ◌    ╏     正在初始化系统...               │
│                    ╏  ◌ ◌ ◌ ╏                                   │
│                     ╰────────╯                                  │
│                                                                 │
│                    ┌─────────────┐                               │
│                    │  ⚠️         │                               │
│                    │ 无法连接到   │                               │
│                    │ 后端服务     │                               │
│                    │ [重新加载]  │                               │
│                    └─────────────┘                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 正常加载测试

1. **确保后端运行**:
   ```bash
   curl http://localhost:8080/api/snapshot/latest
   ```

2. **打开浏览器** 访问 `http://localhost:3000`

3. **预期结果**:
   - ✅ 显示加载动画和"正在初始化系统..."
   - ✅ 数据加载完成后，动画淡出
   - ✅ 正常显示应用界面

### 2. 错误状态测试

1. **停止后端服务** 或使用无效的 API URL

2. **刷新浏览器页面**

3. **预期结果**:
   - ✅ 显示加载动画
   - ✅ 几秒后显示错误提示
   - ✅ 显示"重新加载"按钮

3. **点击"重新加载"按钮**:
   - ✅ 页面刷新，重新尝试加载

### 3. Mock Data Fallback

1. **启用 mock data** (在 `config/env.ts`):
   ```typescript
   useMockData: true
   ```

2. **使用无效的 API URL**

3. **预期结果**:
   - ✅ 使用 mock 数据
   - ✅ 不显示错误状态
   - ✅ 正常显示应用

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 加载动画显示 | ✅ 通过 | 旋转动画正常 |
| 加载文字显示 | ✅ 通过 | "正在初始化系统..." |
| 数据加载后隐藏 | ✅ 通过 | 成功后淡出 |
| 错误状态显示 | ✅ 通过 | 显示错误信息 |
| 重试按钮功能 | ✅ 通过 | 刷新页面 |
| Mock data fallback | ✅ 通过 | 使用模拟数据 |
| CSS 样式渲染 | ✅ 完成 | 无布局错位 |
| 淡入淡出动画 | ✅ 完成 | 平滑过渡 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/App.vue**
   - 添加加载状态变量 (line 127-129)
   - 添加加载遮罩模板 (line 6-19)
   - 添加重试函数 (line 272-277)
   - 更新数据加载逻辑 (line 357-358, 404-409)
   - 添加 CSS 样式 (line 612-698)

---

## 代码流程

```
┌─────────────────────────────────────────────────────────────────┐
│                   应用启动流程 (新增加载状态)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 组件挂载 (onMounted)                                         │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  isLoading = true                                       │    │
│     │  显示加载遮罩                                           │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  2. 获取快照                                                     │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  api.getLatestSnapshot()                                │    │
│     │                                                          │    │
│     │  成功 → 继续步骤 3                                       │    │
│     │  失败 → 检查 mock data                                   │    │
│     │    - 有 mock data → 使用 mock data                       │    │
│     │    - 无 mock data → 显示错误                             │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  3. 获取增量事件                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  api.getEventsSince(snapshot.seq)                       │    │
│     │                                                          │    │
│     │  应用增量事件到 agents 数组                              │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  4. 创建 3D 场景                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  scene.createAgentZone()                                │    │
│     │  scene.updateAgentStatus()                              │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  5. 加载完成                                                     │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  isLoading = false                                      │    │
│     │  隐藏加载遮罩 (淡出动画)                                 │    │
│     │  显示应用界面                                            │    │
│     └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 优化建议

### 1. 加载进度提示

如果加载时间较长，可以显示更详细的进度：

```typescript
const loadingProgress = ref([
  { text: '连接到服务器...', done: false },
  { text: '加载 Agent 数据...', done: false },
  { text: '创建 3D 场景...', done: false },
  { text: '建立实时连接...', done: false },
])
```

### 2. 加载超时处理

添加超时机制：

```typescript
const loadingTimeout = setTimeout(() => {
  if (isLoading.value) {
    loadingError.value = '加载超时，请检查网络连接'
  }
}, 30000) // 30秒超时
```

### 3. Skeleton Screen

使用骨架屏代替旋转动画，提供更好的视觉连续性：

```vue
<div v-if="isLoading" class="loading-skeleton">
  <div class="skeleton-header"></div>
  <div class="skeleton-list"></div>
  <div class="skeleton-detail"></div>
</div>
```

---

## 已知问题

无

---

## 参考资料

- **Vue Transitions**: https://vuejs.org/guide/built-ins/transition.html
- **CSS Animations**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Animations

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

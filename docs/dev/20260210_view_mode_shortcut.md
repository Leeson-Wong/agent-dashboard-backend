# 视图模式切换快捷键

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要在列表视图和网格视图之间快速切换。虽然界面已经提供了切换按钮，但添加键盘快捷键可以让操作更加便捷，特别是对于频繁切换视图的用户。

**目标**:
1. 添加 "G" 键快捷键切换视图模式
2. 自动保存切换后的偏好到 localStorage
3. 提供视觉反馈

---

## 实现方案

### 前端修改 (AgentListPanel.vue)

#### 添加键盘快捷键注册

```typescript
registerShortcut({
  key: 'g',
  description: '切换视图模式',
  handler: () => {
    viewMode.value = viewMode.value === 'list' ? 'grid' : 'list'
  },
})
```

---

## 功能说明

### 快捷键行为

| 当前视图 | 按 "G" 键 | 结果 |
|---------|----------|------|
| 列表视图 | 按 G | 切换到网格视图 |
| 网格视图 | 按 G | 切换到列表视图 |

### 自动保存

由于之前已实现 `viewMode` 的 watch 监听器，切换会自动保存到 localStorage：

```typescript
watch(viewMode, (newMode) => {
  localStorage.setItem('agentListViewMode', newMode)
})
```

### 与按钮协同

- **快捷键**: 快速切换，适合键盘用户
- **按钮**: 鼠标操作，直观可见
- **两者**: 互补协同，提供多种操作方式

---

## UI 效果

### 列表视图

```
┌─────────────────────────────────────────────┐
│ Agent 状态 [6/6]                             │
│ 在线: 4  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ [按状态 ▼] [所有框架 ▼]                     │
│ [按状态 ▼] [↑] [▦]                         │  ← 按 G 切换
│                                             │
│ ● 在线 - Agent-001                          │
│ ● 在线 - Agent-002                          │
│   CrewAI Python       刚刚                   │
│   正在执行任务                              │
│                                             │
│ ⚪ 忙碌 - Agent-003                          │
│   LangChain Python    5分钟前               │
│   使用工具中                                │
└─────────────────────────────────────────────┘
```

### 按 "G" 切换到网格视图

```
┌─────────────────────────────────────────────┐
│ Agent 状态 [6/6]                             │
│ 在线: 4  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ [按状态 ▼] [所有框架 ▼]                     │
│ [按状态 ▼] [↑] [☰]                         │  ← 按 G 切回
│                                             │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│ │●        │ │●        │ │⚪        │       │
│ │Agent-001│ │Agent-002│ │Agent-003 │       │
│ │[⋯]     │ │[⋯]     │ │[⋯]     │       │
│ └─────────┘ └─────────┘ └─────────┘       │
│                                             │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│ │●        │ │...      │ │...      │       │
│ └─────────┘ └─────────┘ └─────────┘       │
└─────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试列表到网格**:
   - 确保当前为列表视图（默认）
   - 按 "G" 键
   - 预期：切换到网格视图，按钮变为 ☰

3. **测试网格到列表**:
   - 按 "G" 键
   - 预期：切换回列表视图，按钮变为 ▦

4. **测试连续切换**:
   - 连续按 "G" 键 3 次
   - 预期：网格 → 列表 → 网格

5. **测试快捷键帮助**:
   - 按 ⌨️ (或 Ctrl+/) 打开快捷键帮助
   - 预期：显示 "G - 切换视图模式"

6. **测试偏好保存**:
   - 切换到网格视图
   - 刷新页面 (F5)
   - 预期：保持网格视图

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 列表 → 网格 | ✅ 通过 | 正确切换到网格视图 |
| 网格 → 列表 | ✅ 通过 | 正确切换回列表视图 |
| 连续切换 | ✅ 通过 | 每次正确切换 |
| 按钮图标同步 | ✅ 通过 | 按钮图标正确更新 |
| localStorage 保存 | ✅ 通过 | 通过 watch 自动保存 |
| 快捷键帮助显示 | ✅ 通过 | 帮助中显示该快捷键 |
| 输入框中按 G | ✅ 通过 | 正确跳过，不触发 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加 "G" 键快捷键注册 (line 446-452)

---

## 技术细节

### 响应式切换

直接切换 ref 值，Vue 的响应式系统自动更新 UI：

```typescript
viewMode.value = viewMode.value === 'list' ? 'grid' : 'list'
```

### Watch 触发

切换会触发已注册的 watch 监听器，自动保存到 localStorage：

```typescript
watch(viewMode, (newMode) => {
  localStorage.setItem('agentListViewMode', newMode)
})
```

### 条件渲染

模板中的条件渲染会根据 `viewMode` 值自动更新：

```vue
<div :class="['agent-list', `agent-list-${viewMode}`]">
```

```vue
<button class="view-mode-btn" @click="...">
  {{ viewMode === 'list' ? '▦' : '☰' }}
</button>
```

### 快捷键冲突处理

由于之前已修复 useKeyboard 的输入框检测，在输入框中按 "G" 不会触发快捷键。

---

## 优化建议

### 1. Toast 提示

切换时显示 Toast 通知：

```typescript
const { info } = useToast()

registerShortcut({
  key: 'g',
  description: '切换视图模式',
  handler: () => {
    const newMode = viewMode.value === 'list' ? 'grid' : 'list'
    viewMode.value = newMode
    info(`已切换到${newMode === 'list' ? '列表' : '网格'}视图`, { duration: 1500 })
  },
})
```

### 2. 多方向切换

支持 Shift+G 反向切换：

```typescript
registerShortcut({
  key: 'g',
  shift: true,
  description: '反向切换视图模式',
  handler: () => {
    // 与无 Shift 相同，或反向逻辑
    viewMode.value = viewMode.value === 'list' ? 'grid' : 'list'
  },
})
```

### 3. 切换动画

添加视图切换动画：

```vue
<Transition :name="`view-${viewMode}`" mode="out-in">
  <div :class="['agent-list', `agent-list-${viewMode}`]">
    <!-- agents -->
  </div>
</Transition>
```

```css
.view-list-enter-active,
.view-grid-enter-active {
  transition: all 0.3s;
}

.view-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.view-grid-enter-from {
  opacity: 0;
  transform: scale(0.95);
}
```

### 4. 记住每种视图的滚动位置

分别保存列表和网格视图的滚动位置：

```typescript
const scrollPositions = ref({
  list: 0,
  grid: 0,
})

watch(viewMode, (newMode, oldMode) => {
  // 保存当前滚动位置
  const container = document.querySelector('.agent-list')
  if (container) {
    scrollPositions.value[oldMode] = container.scrollTop
  }
})

onMounted(() => {
  // 恢复滚动位置
  const container = document.querySelector('.agent-list')
  if (container) {
    container.scrollTop = scrollPositions.value[viewMode.value]
  }
})
```

### 5. 视图模式统计

统计每种视图模式的使用次数：

```typescript
const viewModeStats = ref({
  list: 0,
  grid: 0,
})

watch(viewMode, (newMode) => {
  viewModeStats.value[newMode]++
  localStorage.setItem('agentViewModeStats', JSON.stringify(viewModeStats.value))
})
```

---

## 已知问题

无

---

## 参考资料

- **Vue Reactivity**: https://vuejs.org/guide/essentials/reactivity-fundamentals.html
- **localStorage**: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

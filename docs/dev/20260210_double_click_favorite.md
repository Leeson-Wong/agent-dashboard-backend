# 双击收藏功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户收藏 Agent 需要准确定位并点击小的收藏按钮，操作不够便捷。通过添加双击收藏功能，用户可以直接双击 Agent 列表项快速收藏，提高操作效率。

**目标**:
1. 支持双击 Agent 列表项切换收藏状态
2. 双击后同时选中 Agent
3. 保持单击选择功能不变
4. 复用收藏按钮动画效果

---

## 实现方案

### 前端实现

#### 1. 添加双击事件监听

**文件**: `src/components/AgentListPanel.vue`

```vue
<div
  v-for="agent in filteredAgents"
  :key="agent.agentId"
  :class="['agent-item', { selected: selectedAgentId === agent.agentId }]"
  @click="selectAgent(agent.agentId)"
  @dblclick="handleDoubleClick(agent)"
>
```

#### 2. 添加双击处理函数

```typescript
// Handle double click on agent item
const handleDoubleClick = async (agent: AgentState): Promise<void> => {
  await toggleFavorite(agent)
  // Also select the agent
  selectAgent(agent.agentId)
}
```

---

## 功能说明

### 操作方式

| 操作 | 效果 |
|------|------|
| 单击 Agent | 选中 Agent，显示详情 |
| 双击 Agent | 切换收藏状态 + 选中 Agent |
| 点击收藏按钮 | 切换收藏状态（不选中） |

### 交互逻辑

```
双击事件:
  ↓
1. 切换收藏状态 (调用 toggleFavorite)
  ↓
2. 收藏按钮动画触发
  ↓
3. 选中 Agent (调用 selectAgent)
  ↓
4. 显示详情面板
```

### 事件冲突处理

| 事件 | 处理方式 |
|------|----------|
| 单击 | 正常选择 Agent |
| 双击 | 双击事件在单击后触发，两个事件都执行 |
| 双击速度 | 依赖系统默认双击速度 |

---

## UI 效果

### 双击未收藏的 Agent

```
单击: 选中 Agent (背景变蓝)
双击: 选中 Agent + 收藏动画 + 星标变黄
```

### 双击已收藏的 Agent

```
单击: 选中 Agent (背景变蓝)
双击: 选中 Agent + 取消收藏动画 + 星标变灰
```

### 动画序列

```
双击时刻 (0ms):
  ● 按钮缩小到 0.95 倍
  ● 按钮放大到 1.3 倍
  ● 星标旋转 360°
  ● 粒子向外扩散
  ● Agent 背景变蓝（选中）
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3002`

2. **测试双击收藏**:
   - 双击未收藏的 Agent 列表项
   - 预期: Agent 被收藏（星标变黄），同时被选中

3. **测试双击取消收藏**:
   - 双击已收藏的 Agent 列表项
   - 预期: Agent 取消收藏（星标变灰），同时被选中

4. **测试单击选择**:
   - 单击 Agent 列表项
   - 预期: 仅选中 Agent，收藏状态不变

5. **测试双击后详情显示**:
   - 双击 Agent
   - 预期: 详情面板显示该 Agent

### 2. 交互测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 快速双击 | 快速连续双击 | 触发双击事件 |
| 慢速双击 | 慢速双击 | 触发双击事件 |
| 双击复选框 | 双击复选框 | 正常工作 |
| 双击收藏按钮 | 双击收藏按钮 | 切换收藏+选中 |
| 双击其他区域 | 双击 Agent 其他区域 | 正常工作 |

### 3. 动画测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| 收藏动画 | 双击未收藏 | 完整动画播放 |
| 取消收藏动画 | 双击已收藏 | 完整动画播放 |
| 动画时长 | 约 0.6 秒 | 正常播放 |
| 按钮状态 | 动画后 | 正确显示状态 |

### 4. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 网络延迟 | 模拟慢速网络 | 动画正常播放 |
| 快速切换 | 连续双击多个 | 每次都触发 |
| 同时操作 | 边浏览边双击 | 正常工作 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 双击收藏 | ✅ 通过 | 未收藏→收藏 |
| 双击取消收藏 | ✅ 通过 | 收藏→未收藏 |
| 单击选择 | ✅ 通过 | 仅选中，不变收藏 |
| 双击选中 | ✅ 通过 | 同时选中 |
| 动画播放 | ✅ 通过 | 收藏动画正常 |
| 详情显示 | ✅ 通过 | 详情正确显示 |
| 事件冲突 | ✅ 通过 | 单击和双击协调 |
| 性能表现 | ✅ 通过 | 无延迟 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentListPanel.vue**
   - 添加 `@dblclick` 事件监听 (line 187)
   - 添加 `handleDoubleClick` 函数 (line 582-586)

---

## 技术细节

### Vue 事件处理

Vue 3 同时支持 `@click` 和 `@dblclick`:

```vue
<div @click="selectAgent(id)" @dblclick="handleDoubleClick(agent)">
```

双击事件会在两次单击后触发，单击事件也会正常触发。

### 异步函数调用

双击处理函数是异步的，等待收藏 API 完成:

```typescript
const handleDoubleClick = async (agent: AgentState): Promise<void> => {
  await toggleFavorite(agent)  // 等待 API 完成
  selectAgent(agent.agentId)     // 然后选中
}
```

### 事件冒泡

双击事件会冒泡，但在此场景中没有负面影响。如果需要阻止冒泡，可以使用:

```typescript
const handleDoubleClick = async (agent: AgentState, event: Event): Promise<void> => {
  event.preventDefault()
  event.stopPropagation()
  // ...
}
```

### 系统双击速度

双击检测由浏览器处理，通常配置为:
- 第一次点击和第二次点击间隔 < 500ms
- 具体时间取决于用户系统设置

### 函数复用

双击处理复用了已有的 `toggleFavorite` 函数，避免代码重复:

```typescript
await toggleFavorite(agent)  // 复用已有函数
```

---

## 优化建议

### 1. 可配置双击操作

允许用户选择双击行为:

```typescript
interface DoubleClickAction {
  key: 'favorite' | 'select' | 'details' | 'copy'
  label: string
}

const doubleClickAction = ref<DoubleClickAction['key']>('favorite')

const handleDoubleClick = async (agent: AgentState): Promise<void> => {
  switch (doubleClickAction.value) {
    case 'favorite':
      await toggleFavorite(agent)
      break
    case 'select':
      selectAgent(agent.agentId)
      break
    // ...
  }
}
```

### 2. 视觉反馈

双击时添加临时视觉反馈:

```css
.agent-item:active {
  background: rgba(59, 130, 246, 0.1);
}

.agent-item.double-clicking {
  animation: double-click-flash 0.3s ease-out;
}

@keyframes double-click-flash {
  0% { background: transparent; }
  50% { background: rgba(59, 130, 246, 0.2); }
  100% { background: transparent; }
}
```

### 3. 长按收藏

添加长按作为收藏的替代方式:

```typescript
let longPressTimer: number | null = null

const handleMouseDown = (agent: AgentState): void => {
  longPressTimer = window.setTimeout(() => {
    toggleFavorite(agent)
  }, 500)
}

const handleMouseUp = (): void => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}
```

### 4. 手势支持

添加移动端手势支持:

```typescript
const handleSwipe = (agent: AgentState, direction: 'left' | 'right'): void => {
  if (direction === 'right') {
    toggleFavorite(agent)
  }
}
```

### 5. 快捷键支持

添加键盘快捷键快速收藏:

```typescript
registerShortcut({
  key: 'f',
  description: '收藏当前 Agent',
  handler: () => {
    if (selectedAgentId.value) {
      const agent = props.agents.find(a => a.agentId === selectedAgentId.value)
      if (agent) {
        toggleFavorite(agent)
      }
    }
  },
})
```

### 6. 批量收藏

支持批量收藏多个 Agent:

```typescript
const batchFavorite = (agentIds: string[], isFavorite: boolean): Promise<void> => {
  const promises = agentIds.map(id => {
    const agent = props.agents.find(a => a.agentId === id)
    return agent ? updateFavorite(id, isFavorite) : Promise.resolve()
  })

  await Promise.all(promises)
  success(`已${isFavorite ? '收藏' : '取消收藏'} ${agentIds.length} 个 Agent`)
}
```

### 7. 收藏提示

首次使用时显示功能提示:

```vue
<div v-if="showDoubleClickHint" class="double-click-hint">
  💡 提示: 双击 Agent 可快速收藏
  <button @click="dismissHint">知道了</button>
</div>
```

### 8. 收藏历史

记录收藏/取消收藏历史:

```typescript
const favoriteHistory = ref<Array<{
  agentId: string
  action: 'favorite' | 'unfavorite'
  timestamp: number
}>>([])

const recordFavoriteAction = (agent: AgentState, action: 'favorite' | 'unfavorite'): void => {
  favoriteHistory.value.unshift({
    agentId: agent.agentId,
    action,
    timestamp: Date.now()
  })

  if (favoriteHistory.value.length > 100) {
    favoriteHistory.value.pop()
  }
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue Events**: https://vuejs.org/guide/essentials/event-handling.html
- **Mouse Events**: https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent
- **dblclick Event**: https://developer.mozilla.org/en-US/docs/Web/API/Element/dblclick_event

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

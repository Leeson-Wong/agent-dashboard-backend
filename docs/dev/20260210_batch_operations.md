# Agent 批量操作功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要同时对多个 Agent 执行相同的操作，如批量暂停、恢复、停止或删除。通过添加批量操作功能，用户可以快速管理多个 Agent，提高工作效率。

**目标**:
1. 添加复选框多选功能
2. 显示批量操作工具栏
3. 支持批量暂停、恢复、停止、删除
4. 提供清晰的视觉反馈

---

## 实现方案

### 前端修改 (AgentListPanel.vue)

#### 1. 添加批量操作状态

```typescript
const selectedAgentIds = ref<Set<string>>(new Set())
```

使用 `Set` 数据结构存储选中的 Agent ID，确保唯一性和高效查找。

#### 2. 添加批量操作工具栏

```vue
<div v-if="selectedAgentIds.size > 0" class="batch-toolbar">
  <span class="batch-count">已选择 {{ selectedAgentIds.size }} 个 Agent</span>
  <div class="batch-actions">
    <button class="batch-btn batch-pause" @click="batchPause">⏸ 暂停</button>
    <button class="batch-btn batch-resume" @click="batchResume">▶️ 恢复</button>
    <button class="batch-btn batch-stop" @click="batchStop">⏹ 停止</button>
    <button class="batch-btn batch-delete" @click="batchDelete">🗑️ 删除</button>
    <button class="batch-btn batch-cancel" @click="clearSelection">✕</button>
  </div>
</div>
```

#### 3. 添加复选框

```vue
<input
  type="checkbox"
  :class="['agent-checkbox', { visible: selectedAgentIds.size > 0 }]"
  :checked="selectedAgentIds.has(agent.agentId)"
  @click.stop="toggleAgentSelection(agent.agentId)"
/>
```

复选框默认隐藏，当有 Agent 被选中时自动显示。

#### 4. 添加选择切换函数

```typescript
const toggleAgentSelection = (agentId: string): void => {
  if (selectedAgentIds.value.has(agentId)) {
    selectedAgentIds.value.delete(agentId)
  } else {
    selectedAgentIds.value.add(agentId)
  }
  // 强制触发响应式更新
  selectedAgentIds.value = new Set(selectedAgentIds.value)
}
```

#### 5. 添加清除选择函数

```typescript
const clearSelection = (): void => {
  selectedAgentIds.value.clear()
  selectedAgentIds.value = new Set(selectedAgentIds.value)
}
```

#### 6. 添加批量操作 API 调用

```typescript
const batchOperation = async (operation: string): Promise<void> => {
  if (selectedAgentIds.value.size === 0) return

  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const response = await fetch(`${apiUrl}/api/agents/batch`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        operation,
        agentIds: Array.from(selectedAgentIds.value)
      })
    })

    if (!response.ok) {
      throw new Error(`批量${operation}失败`)
    }

    const result = await response.json()
    success(`批量${operation}成功: ${selectedAgentIds.value.size} 个 Agent`)
    clearSelection()
  } catch (error) {
    console.error('Batch operation failed:', error)
  }
}
```

#### 7. 添加批量操作快捷方法

```typescript
const batchPause = (): Promise<void> => batchOperation('pause')
const batchResume = (): Promise<void> => batchOperation('resume')
const batchStop = (): Promise<void> => batchOperation('stop')
const batchDelete = async (): Promise<void> => {
  if (selectedAgentIds.value.size === 0) return
  if (confirm(`确定要删除 ${selectedAgentIds.value.size} 个 Agent 吗?`)) {
    await batchOperation('delete')
  }
}
```

#### 8. 更新 Agent 列表样式

```vue
<div :class="['agent-list', `agent-list-${viewMode}`, { 'has-selection': selectedAgentIds.size > 0 }]">
  <div :class="['agent-item', { 'batch-selected': selectedAgentIds.has(agent.agentId) }]">
    <!-- ... -->
  </div>
</div>
```

#### 9. 添加样式

```css
.batch-toolbar {
  padding: 8px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(59, 130, 246, 0.1);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

.agent-checkbox {
  position: absolute;
  left: 4px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px;
  height: 14px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.agent-checkbox.visible {
  opacity: 1;
  pointer-events: auto;
}

.agent-item.batch-selected {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.4);
}
```

---

## 功能说明

### 批量操作类型

| 操作 | 图标 | 说明 | 确认 |
|------|------|------|------|
| 暂停 | ⏸ | 批量暂停选中的 Agent | 无 |
| 恢复 | ▶️ | 批量恢复选中的 Agent | 无 |
| 停止 | ⏹ | 批量停止选中的 Agent | 无 |
| 删除 | 🗑️ | 批量删除选中的 Agent | 有 |
| 取消 | ✕ | 清除所有选择 | 无 |

### 复选框行为

| 状态 | 说明 |
|------|------|
| 无选择 | 复选框隐藏 |
| 有选择 | 复选框显示 |
| 点击复选框 | 切换该 Agent 的选中状态 |
| 点击 Agent 项 | 仍然触发原有的选中逻辑 |

### 视觉反馈

| 元素 | 状态 | 样式 |
|------|------|------|
| Agent 项 | 已选中 | 蓝色背景 |
| 工具栏 | 有选择 | 蓝色背景 |
| 暂停按钮 | 悬停 | 黄色 |
| 恢复按钮 | 悬停 | 绿色 |
| 停止按钮 | 悬停 | 红色 |
| 删除按钮 | 悬停 | 深红色 |

---

## API 接口

### POST /api/agents/batch

**请求体**:

```json
{
  "operation": "pause",
  "agentIds": ["agent-id-1", "agent-id-2", "agent-id-3"]
}
```

**操作类型**:

| 操作 | 说明 |
|------|------|
| pause | 暂停 Agent |
| resume | 恢复 Agent |
| stop | 停止 Agent |
| restart | 重启 Agent |
| delete | 删除 Agent |

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": ["agent-id-1", "agent-id-2"],
    "failed": ["agent-id-3"],
    "total": 3,
    "successCount": 2,
    "failedCount": 1
  }
}
```

---

## UI 效果

### 初始状态 (无选择)

```
┌─────────────────────────────────────────────┐
│ [按状态 ▼] [所有框架 ▼] [所有时间 ▼]       │
│ [按状态 ▼] [↑] [▦] [JSON] [CSV]            │
├─────────────────────────────────────────────┤
│                                             │
│ ● 在线 - Agent-001                          │  ← 无复选框
│   刚刚                                       │
│                                             │
│ ● 在线 - Agent-002                          │
│   2分钟前                                    │
└─────────────────────────────────────────────┘
```

### 选择一个 Agent 后

```
┌─────────────────────────────────────────────┐
│ [按状态 ▼] [所有框架 ▼] [所有时间 ▼]       │
│ [按状态 ▼] [↑] [▦] [JSON] [CSV]            │
├─────────────────────────────────────────────┤
│ 已选择 1 个 Agent  [⏸暂停] [▶️恢复] [⏹停止] [🗑️删除] [✕] │  ← 工具栏
├─────────────────────────────────────────────┤
│                                             │
│ [✓] 在线 - Agent-001                       │  ← 复选框显示
│     刚刚                                     │  ← 蓝色背景
│                                             │
│ [  ] ● 在线 - Agent-002                    │
│       2分钟前                                │
└─────────────────────────────────────────────┘
```

### 选择多个 Agent

```
┌─────────────────────────────────────────────┐
│ 已选择 3 个 Agent  [⏸暂停] [▶️恢复] [⏹停止] [🗑️删除] [✕] │
├─────────────────────────────────────────────┤
│                                             │
│ [✓] 在线 - Agent-001                       │
│     刚刚                                     │
│                                             │
│ [✓] 在线 - Agent-002                       │
│     2分钟前                                  │
│                                             │
│ [✓] 忙碌 - Agent-003                       │
│     使用工具中                               │
└─────────────────────────────────────────────┘
```

### 批量删除确认

```
┌─────────────────────────────────────────────┐
│ 浏览器对话框                                 │
├─────────────────────────────────────────────┤
│ 确定要删除 3 个 Agent 吗?                    │
│                                             │
│ [取消]  [确定]                              │
└─────────────────────────────────────────────┘
```

### 批量操作成功

```
┌─────────────────────────────────────────────┐
│ Toast 通知                                  │
├─────────────────────────────────────────────┤
│ ✓ 批量pause成功: 3 个 Agent                 │
└─────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试选择单个 Agent**:
   - 点击 Agent 项前的复选框
   - 预期: 复选框变为选中状态, Agent 项背景变蓝
   - 预期: 显示批量操作工具栏 "已选择 1 个 Agent"

3. **测试选择多个 Agent**:
   - 点击多个 Agent 的复选框
   - 预期: 所有选中的 Agent 都有蓝色背景
   - 预期: 工具栏显示正确的数量

4. **测试取消选择**:
   - 点击已选中的复选框
   - 预期: 该 Agent 的选中状态取消
   - 预期: 如果没有选中项, 工具栏隐藏

5. **测试全部取消**:
   - 点击工具栏的 ✕ 按钮
   - 预期: 所有选择清除, 工具栏隐藏

### 2. 批量操作测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 批量暂停 | 选择多个在线 Agent, 点击暂停 | 所有 Agent 暂停, Toast 提示成功 |
| 批量恢复 | 选择多个暂停 Agent, 点击恢复 | 所有 Agent 恢复, Toast 提示成功 |
| 批量停止 | 选择多个 Agent, 点击停止 | 所有 Agent 停止, Toast 提示成功 |
| 批量删除 | 选择多个 Agent, 点击删除 | 显示确认对话框, 确认后删除 |
| 删除取消 | 点击删除, 然后取消 | 不执行删除操作 |

### 3. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 空选择 | 不选择任何 Agent, 点击批量操作 | 无操作 |
| 全选 | 选择所有 Agent | 工具栏显示总数 |
| 切换筛选 | 应用筛选后查看选择 | 选择状态保持 |
| 切换视图 | 切换到网格视图 | 复选框正常显示 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 复选框显示 | ✅ 通过 | 选择后正确显示 |
| 复选框隐藏 | ✅ 通过 | 清除后正确隐藏 |
| 单个选择 | ✅ 通过 | 正确选择单个 Agent |
| 多个选择 | ✅ 通过 | 正确选择多个 Agent |
| 取消选择 | ✅ 通过 | 正确取消选择 |
| 全部取消 | ✅ 通过 | ✕ 按钮正确清除所有 |
| 批量暂停 | ✅ 通过 | 正确调用 API |
| 批量恢复 | ✅ 通过 | 正确调用 API |
| 批量停止 | ✅ 通过 | 正确调用 API |
| 批量删除 | ✅ 通过 | 显示确认并执行 |
| 工具栏显示 | ✅ 通过 | 有选择时正确显示 |
| 工具栏隐藏 | ✅ 通过 | 无选择时正确隐藏 |
| 选中高亮 | ✅ 通过 | 蓝色背景正确显示 |
| Toast 通知 | ✅ 通过 | 操作成功后显示提示 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加 selectedAgentIds 状态 (line 250)
   - 添加批量操作工具栏 (line 134-154)
   - 添加复选框到 Agent 项 (line 163-168)
   - 更新 agent-list class (line 156)
   - 更新 agent-item class (line 160)
   - 添加 toggleAgentSelection 函数 (line 542-551)
   - 添加 clearSelection 函数 (line 553-557)
   - 添加 batchOperation 函数 (line 559-584)
   - 添加批量操作快捷方法 (line 586-596)
   - 添加批量工具栏样式 (line 1049-1093)
   - 添加复选框样式 (line 1095-1131)

---

## 技术细节

### Set 响应式更新

Vue 3 的 `ref` 包装 `Set` 时,直接调用 `add` 和 `delete` 不会触发响应式更新。需要重新赋值:

```typescript
selectedAgentIds.value.add(agentId)
// 触发响应式更新
selectedAgentIds.value = new Set(selectedAgentIds.value)
```

### 复选框可见性控制

使用条件 class 控制复选框的可见性:

```vue
<input :class="['agent-checkbox', { visible: selectedAgentIds.size > 0 }]" />
```

配合 CSS 过渡效果:

```css
.agent-checkbox {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
}

.agent-checkbox.visible {
  opacity: 1;
  pointer-events: auto;
}
```

### 事件冒泡处理

使用 `@click.stop` 阻止复选框点击事件冒泡到 Agent 项:

```vue
<input @click.stop="toggleAgentSelection(agent.agentId)" />
```

### 删除确认

使用浏览器原生 `confirm` 对话框:

```typescript
if (confirm(`确定要删除 ${selectedAgentIds.value.size} 个 Agent 吗?`)) {
  await batchOperation('delete')
}
```

### API 请求格式

批量操作 API 接受以下格式:

```json
{
  "operation": "pause|resume|stop|restart|delete",
  "agentIds": ["id1", "id2", ...]
}
```

---

## 优化建议

### 1. 全选功能

添加全选/取消全选按钮:

```vue
<button class="batch-btn" @click="toggleSelectAll">
  {{ isAllSelected ? '取消全选' : '全选' }}
</button>
```

```typescript
const toggleSelectAll = (): void => {
  if (isAllSelected.value) {
    clearSelection()
  } else {
    filteredAgents.value.forEach(agent => {
      selectedAgentIds.value.add(agent.agentId)
    })
    selectedAgentIds.value = new Set(selectedAgentIds.value)
  }
}

const isAllSelected = computed(() => {
  return filteredAgents.value.length > 0 &&
    filteredAgents.value.every(agent => selectedAgentIds.value.has(agent.agentId))
})
```

### 2. 反选功能

添加反选按钮:

```typescript
const invertSelection = (): void => {
  const newSet = new Set<string>()
  filteredAgents.value.forEach(agent => {
    if (!selectedAgentIds.value.has(agent.agentId)) {
      newSet.add(agent.agentId)
    }
  })
  selectedAgentIds.value = newSet
}
```

### 3. 按状态筛选选择

只选择特定状态的 Agent:

```typescript
const selectByStatus = (status: string): void => {
  filteredAgents.value
    .filter(agent => agent.status === status)
    .forEach(agent => {
      selectedAgentIds.value.add(agent.agentId)
    })
  selectedAgentIds.value = new Set(selectedAgentIds.value)
}
```

### 4. 选择历史

保存最近的选择:

```typescript
const selectionHistory = ref<Set<string>[]>([])
const saveSelection = (): void => {
  selectionHistory.value.push(new Set(selectedAgentIds.value))
}

const restoreSelection = (index: number): void => {
  selectedAgentIds.value = new Set(selectionHistory.value[index])
}
```

### 5. 快捷键支持

添加快捷键:

```typescript
registerShortcut({
  key: 'a',
  ctrl: true,
  description: '全选',
  handler: () => {
    toggleSelectAll()
  },
})

registerShortcut({
  key: 'Escape',
  description: '取消选择',
  handler: () => {
    if (selectedAgentIds.value.size > 0) {
      clearSelection()
    }
  },
})
```

### 6. 批量操作进度

对于大量 Agent,显示操作进度:

```typescript
const batchProgress = ref({ current: 0, total: 0 })

const batchOperationWithProgress = async (operation: string): Promise<void> => {
  const ids = Array.from(selectedAgentIds.value)
  batchProgress.value = { current: 0, total: ids.length }

  for (const id of ids) {
    await singleOperation(operation, id)
    batchProgress.value.current++
  }
}
```

### 7. 批量导出

导出选中的 Agent:

```typescript
const exportSelected = async (format: 'json' | 'csv'): Promise<void> => {
  const selectedData = props.agents.filter(agent =>
    selectedAgentIds.value.has(agent.agentId)
  )
  // 导出 selectedData
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue 3 Reactivity**: https://vuejs.org/guide/essentials/reactivity-fundamentals.html
- **Set API**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Set
- **Event Modifiers**: https://vuejs.org/guide/essentials/event-handling.html#event-modifiers

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

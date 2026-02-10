# 批量操作功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

当用户需要管理多个 Agent 时，逐个操作非常繁琐。批量操作功能允许用户一次性对多个选中的 Agent 执行相同操作，大大提高了管理效率。

## 需求分析

### 核心需求

1. **多选支持** - 用户可以选中多个 Agent
2. **批量操作** - 支持暂停、恢复、删除、导出等批量操作
3. **进度显示** - 显示批量操作的进度和状态
4. **结果反馈** - 显示操作结果（成功/失败详情）
5. **确认机制** - 危险操作需要用户确认

### 技术要求

- 提供 API 调用封装
- 支持并发控制和错误处理
- 友好的用户界面
- 防止误操作

## 实现方案

### 1. 创建批量操作 Composable

**文件**: `src/composables/useBatchOperations.ts`

#### 核心数据结构

```typescript
export type BatchAction = 'pause' | 'resume' | 'delete' | 'export' | 'update'

export interface BatchOperation {
  action: BatchAction
  agentIds: string[]
  options?: Record<string, unknown>
}

export interface BatchResult {
  success: boolean
  agentId: string
  error?: string
}

export interface BatchProgress {
  total: number
  completed: number
  failed: number
  results: BatchResult[]
}
```

#### 主要功能

**1. 执行批量操作**

```typescript
export async function executeBatchOperation(
  operation: BatchOperation,
  agents: AgentState[],
  onProgress?: (progress: BatchProgress) => void
): Promise<BatchProgress> {
  const results: BatchResult[] = []
  let completed = 0
  let failed = 0
  const total = operation.agentIds.length

  // 顺序处理每个 Agent
  for (const agentId of operation.agentIds) {
    try {
      const agent = agents.find(a => a.agentId === agentId)
      if (!agent) {
        throw new Error(`Agent ${agentId} not found`)
      }

      await executeAction(operation.action, agent, operation.options)
      results.push({ success: true, agentId })
      completed++
    } catch (error) {
      results.push({
        success: false,
        agentId,
        error: error instanceof Error ? error.message : 'Unknown error'
      })
      failed++
    }

    // 报告进度
    if (onProgress) {
      onProgress({ total, completed, failed, results })
    }
  }

  return { total, completed, failed, results }
}
```

**2. 执行单个操作**

```typescript
async function executeAction(
  action: BatchAction,
  agent: AgentState,
  options?: Record<string, unknown>
): Promise<void> {
  const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

  switch (action) {
    case 'pause':
      await fetch(`${apiUrl}/api/agents/${agent.agentId}/pause`, {
        method: 'POST'
      })
      break

    case 'resume':
      await fetch(`${apiUrl}/api/agents/${agent.agentId}/resume`, {
        method: 'POST'
      })
      break

    case 'delete':
      await fetch(`${apiUrl}/api/agents/${agent.agentId}`, {
        method: 'DELETE'
      })
      break

    // ... 其他操作
  }
}
```

**3. 辅助函数**

```typescript
// 获取操作显示名称
export function getActionDisplayName(action: BatchAction): string {
  const names: Record<BatchAction, string> = {
    pause: '暂停',
    resume: '恢复',
    delete: '删除',
    export: '导出',
    update: '更新'
  }
  return names[action]
}

// 获取操作图标
export function getActionIcon(action: BatchAction): string {
  const icons: Record<BatchAction, string> = {
    pause: '⏸️',
    resume: '▶️',
    delete: '🗑️',
    export: '📥',
    update: '✏️'
  }
  return icons[action]
}

// 检查是否为危险操作
export function isDestructiveAction(action: BatchAction): boolean {
  return action === 'delete'
}

// 检查是否需要确认
export function requiresConfirmation(action: BatchAction, count: number): boolean {
  return isDestructiveAction(action) || count > 5
}

// 获取确认消息
export function getConfirmationMessage(action: BatchAction, count: number): string {
  const actionName = getActionDisplayName(action)
  const suffix = count > 1 ? ` ${count} 个 Agent` : ''

  if (isDestructiveAction(action)) {
    return `⚠️ 确定要${actionName}${suffix}吗？此操作不可撤销！`
  }

  return `确定要${actionName}${suffix}吗？`
}
```

### 2. 创建批量操作面板组件

**文件**: `src/components/BatchOperationsPanel.vue`

#### 组件功能

1. **显示选中数量** - 显示当前选中的 Agent 数量
2. **操作按钮** - 提供批量操作按钮
3. **进度条** - 显示操作进度
4. **结果摘要** - 显示操作结果摘要
5. **确认对话框** - 危险操作的确认
6. **错误详情** - 显示失败操作的详细错误信息

#### 组件结构

```
BatchOperationsPanel (固定在底部)
├── Panel Content
│   ├── Info
│   │   ├── Selection Count
│   │   └── Clear Selection Button
│   ├── Action Buttons
│   │   ├── Pause (disabled if not all online)
│   │   ├── Resume (disabled if not all offline)
│   │   ├── Export
│   │   └── Delete (destructive)
│   ├── Progress Section (when processing)
│   │   ├── Progress Text
│   │   └── Progress Bar
│   └── Results Summary (after completion)
│       └── Success/Partial Message
├── Confirmation Dialog (when needed)
│   ├── Dialog Message
│   ├── Agent List (preview)
│   └── Confirm/Cancel Buttons
└── Errors Detail Dialog (when there are errors)
    ├── Errors List
    └── Close Button
```

#### 可用操作配置

```typescript
const availableActions = computed(() => {
  const selectedList = Array.from(props.selectedIds)
  const allOnline = selectedList.every(id => {
    const agent = props.agents.find(a => a.agentId === id)
    return agent?.status === 'online' || agent?.status === 'ready'
  })

  const allOffline = selectedList.every(id => {
    const agent = props.agents.find(a => a.agentId === id)
    return agent?.status === 'offline' || agent?.status === 'paused'
  })

  return [
    {
      id: 'pause' as BatchAction,
      label: '暂停',
      icon: '⏸️',
      description: '暂停选中的 Agent',
      destructive: false,
      disabled: !allOnline  // 只有全部在线时才能暂停
    },
    {
      id: 'resume' as BatchAction,
      label: '恢复',
      icon: '▶️',
      description: '恢复选中的 Agent',
      destructive: false,
      disabled: !allOffline  // 只有全部离线时才能恢复
    },
    {
      id: 'export' as BatchAction,
      label: '导出',
      icon: '📥',
      description: '导出选中的 Agent',
      destructive: false,
      disabled: false
    },
    {
      id: 'delete' as BatchAction,
      label: '删除',
      icon: '🗑️',
      description: '删除选中的 Agent',
      destructive: true,
      disabled: false
    }
  ]
})
```

#### 确认对话框

```vue
<Transition name="modal">
  <div v-if="showConfirmDialog" class="confirm-dialog-overlay">
    <div class="confirm-dialog">
      <div class="dialog-header">
        <h3>确认操作</h3>
      </div>
      <div class="dialog-body">
        <p>{{ confirmMessage }}</p>
        <div class="dialog-agents">
          <div v-for="id in selectedAgentIds" :key="id">
            {{ getAgentName(id) }}
          </div>
        </div>
      </div>
      <div class="dialog-footer">
        <button @click="cancelAction">取消</button>
        <button
          :class="{ 'is-destructive': isDestructive }"
          @click="confirmAction"
        >
          {{ isDestructive ? '确认删除' : '确认' }}
        </button>
      </div>
    </div>
  </div>
</Transition>
```

### 3. 集成到 Agent 列表面板

**文件**: `src/components/AgentListPanel.vue`

#### 修改内容

**1. 导入组件**
```typescript
import BatchOperationsPanel from './BatchOperationsPanel.vue'
```

**2. 添加到模板**
```vue
<template>
  <div class="agent-list-panel">
    <!-- ... 其他内容 ... -->

    <!-- Batch Operations Panel -->
    <BatchOperationsPanel
      :agents="filteredAgents"
      :selected-ids="selectedAgentIds"
      @clear-selection="selectedAgentIds.clear()"
      @operation-complete="handleBatchOperationComplete"
    />
  </div>
</template>
```

**3. 处理操作完成**
```typescript
const handleBatchOperationComplete = (results: unknown): void => {
  const result = results as { completed: number; failed: number }
  if (result.failed === 0) {
    success(`成功完成 ${result.completed} 个操作`)
  } else {
    success(`完成 ${result.completed} 个，失败 ${result.failed} 个`)
  }
}
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **多选功能** ✅
   - 可以选中多个 Agent
   - 显示正确的选中数量

2. **批量操作** ✅
   - 暂停/恢复按钮根据状态正确启用/禁用
   - 操作按钮响应正确

3. **进度显示** ✅
   - 进度条正确显示
   - 进度文本实时更新

4. **确认对话框** ✅
   - 危险操作显示确认对话框
   - 对话框显示将要操作的 Agent 列表

5. **结果反馈** ✅
   - 成功时显示成功消息
   - 失败时显示详情按钮

## 样式实现

### 批量操作面板

```css
.batch-operations-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(30, 41, 59, 0.98);
  border-top: 1px solid rgba(100, 116, 139, 0.4);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  padding: 16px 24px;
}

.panel-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 24px;
}
```

### 操作按钮

```css
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: rgba(51, 65, 85, 0.8);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  color: #cbd5e1;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.is-destructive:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.5);
  color: #fca5a5;
}

.action-btn.is-disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
```

### 进度条

```css
.progress-bar {
  height: 6px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.8), rgba(59, 130, 246, 1));
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-fill.has-errors {
  background: linear-gradient(90deg, rgba(251, 191, 36, 0.8), rgba(239, 68, 68, 0.8));
}
```

### 确认对话框

```css
.confirm-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.confirm-dialog {
  background: rgba(30, 41, 59, 0.98);
  border: 1px solid rgba(100, 116, 139, 0.4);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  max-width: 500px;
  width: 100%;
}

.btn-confirm.is-destructive {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
  color: #f87171;
}
```

## 技术要点

### 1. 顺序处理 vs 并发处理

当前实现使用顺序处理，确保不会因并发请求导致问题：

```typescript
// 顺序处理 - 当前实现
for (const agentId of operation.agentIds) {
  await executeAction(operation.action, agent, options)
}

// 并发处理 - 未来改进
const promises = operation.agentIds.map(agentId =>
  executeAction(operation.action, agent, options)
)
await Promise.all(promises)
```

### 2. 进度回调

通过回调函数实时更新进度：

```typescript
await executeBatchOperation(
  operation,
  agents,
  (progress) => {
    progress.value = progress  // 更新 UI
  }
)
```

### 3. 智能按钮状态

根据 Agent 状态动态启用/禁用按钮：

```typescript
const allOnline = selectedList.every(id => {
  const agent = props.agents.find(a => a.agentId === id)
  return agent?.status === 'online' || agent?.status === 'ready'
})

// 暂停按钮只在全部在线时可用
disabled: !allOnline
```

### 4. 滑入动画

使用 Vue Transition 实现面板滑入效果：

```css
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
```

## 文件变更

### 新增文件

1. **src/composables/useBatchOperations.ts** (~150 行)
   - 批量操作执行逻辑
   - 进度跟踪
   - 辅助函数

2. **src/components/BatchOperationsPanel.vue** (~450 行)
   - 批量操作面板 UI
   - 确认对话框
   - 错误详情对话框
   - 进度显示

### 修改文件

1. **src/components/AgentListPanel.vue**
   - 导入 BatchOperationsPanel 组件
   - 添加到模板
   - 添加操作完成处理函数

## 使用说明

### 基本使用

组件会自动根据选中的 Agent 显示：

```vue
<template>
  <BatchOperationsPanel
    :agents="agents"
    :selected-ids="selectedIds"
    @clear-selection="handleClear"
    @operation-complete="handleComplete"
  />
</template>
```

### 编程式使用

```typescript
import { executeBatchOperation } from './composables/useBatchOperations'

const result = await executeBatchOperation(
  {
    action: 'pause',
    agentIds: ['agent1', 'agent2', 'agent3']
  },
  agents,
  (progress) => {
    console.log(`Progress: ${progress.completed}/${progress.total}`)
  }
)

console.log(`Completed: ${result.completed}, Failed: ${result.failed}`)
```

## 已知限制

1. **顺序处理** - 当前为顺序处理，大批量操作可能较慢
2. **API 依赖** - 需要后端支持批量操作 API
3. **无重试机制** - 失败的操作不会自动重试

## 未来改进

1. **并发控制** - 支持并发处理，提高性能
2. **重试机制** - 失败操作自动重试
3. **操作队列** - 支持操作队列和调度
4. **撤销功能** - 支持撤销某些操作
5. **更多操作** - 支持更多批量操作类型

## 总结

批量操作功能成功实现了：

✅ **多选支持** - 选中多个 Agent 进行操作
✅ **批量操作** - 暂停、恢复、删除、导出
✅ **进度显示** - 实时显示操作进度
✅ **结果反馈** - 显示成功/失败详情
✅ **确认机制** - 危险操作需要确认
✅ **智能按钮** - 根据状态启用/禁用
✅ **友好界面** - 固定底部的操作面板

该功能大大提高了用户管理多个 Agent 的效率。

# Agent Status History Timeline 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，了解 Agent 的状态变化历史对于调试和分析非常重要。状态历史时间线功能可以：

- 可视化显示 Agent 的状态变化轨迹
- 帮助诊断 Agent 行为问题
- 分析状态变化频率和持续时间
- 提供历史趋势分析

## 需求分析

### 核心需求

1. **状态追踪** - 记录每次 Agent 状态变化
2. **可视化时间线** - 以时间线形式展示状态历史
3. **统计信息** - 显示状态变化次数、平均持续时间等
4. **过滤选项** - 支持查看全部、最近、今天的记录
5. **清除功能** - 允许清除历史记录

## 实现方案

### 1. 创建 Status History Composable

**文件**: `src/composables/useAgentStatusHistory.ts` (~185 lines)

#### 核心接口

```typescript
export interface StatusHistoryEntry {
  timestamp: Date
  status: AgentStatus
  previousStatus?: AgentStatus
  duration?: number // Duration in milliseconds since previous status
  metadata?: {
    reason?: string
    source?: string
  }
}
```

#### 全局状态存储

```typescript
const statusHistoryStore = ref<Map<string, StatusHistoryEntry[]>>(new Map())
```

#### 主要功能

```typescript
export function useAgentStatusHistory(agentId: string) {
  // Status history for this agent
  const history = computed<StatusHistoryEntry[]>(() => {
    return getAgentStatusHistory(agentId)
  })

  // Status change statistics
  const statistics = computed(() => {
    return {
      totalChanges: entries.length,
      statusCounts: statusCounts,
      averageDuration: averageDuration,
      firstStatus: entries[0].status,
      lastStatus: entries[entries.length - 1].status,
      timeRange: { start, end }
    }
  })

  // Get history within time range
  const getHistoryInRange = (startDate: Date, endDate: Date): StatusHistoryEntry[]

  // Get history for specific status
  const getHistoryForStatus = (status: AgentStatus): StatusHistoryEntry[]

  // Get recent history (last N entries)
  const getRecentHistory = (count: number = 10): StatusHistoryEntry[]

  // Format duration for display
  const formatDuration = (ms?: number): string
}
```

#### 辅助函数

```typescript
// Add status change entry to history
export const addStatusChange = (
  agentId: string,
  newStatus: AgentStatus,
  previousStatus?: AgentStatus,
  metadata?: StatusHistoryEntry['metadata']
): void

// Clear status history for an agent
export const clearAgentStatusHistory = (agentId: string): void
```

### 2. 创建 Status Timeline 组件

**文件**: `src/components/StatusTimeline.vue` (~410 lines)

#### 功能特性

```vue
<template>
  <div class="status-timeline">
    <!-- Header with view mode selector and clear button -->
    <div class="timeline-header">
      <h3>状态历史</h3>
      <div class="timeline-controls">
        <select v-model="viewMode">
          <option value="all">全部</option>
          <option value="recent">最近 10 条</option>
          <option value="today">今天</option>
        </select>
        <button @click="handleClear">清除</button>
      </div>
    </div>

    <!-- Statistics -->
    <div class="timeline-stats">
      <div class="stat-item">总变化次数</div>
      <div class="stat-item">平均持续时间</div>
    </div>

    <!-- Timeline entries -->
    <div class="timeline-entries">
      <div v-for="entry in displayedHistory" class="timeline-entry">
        <div class="timeline-marker">
          <div class="timeline-dot"></div>
          <div class="timeline-line"></div>
        </div>
        <div class="timeline-content">
          <div class="entry-status">{{ entry.status }}</div>
          <div class="entry-time">{{ formatTime(entry.timestamp) }}</div>
          <div class="entry-transition">从 {{ previousStatus }} 转变</div>
          <div class="entry-duration">持续时间: {{ duration }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
```

#### 视觉设计

- **时间线节点** - 彩色圆点表示不同状态
- **连接线** - 渐变线连接状态变化
- **状态颜色**:
  - 在线 (online) - 绿色
  - 离线 (offline) - 灰色
  - 错误 (error) - 红色
  - 忙碌/思考中 (busy/thinking) - 橙色
  - 已暂停 (paused) - 紫色

### 3. 集成到 Agent Detail Panel

**文件**: `src/components/AgentDetailPanel.vue`

#### 添加 Status Timeline 组件

```vue
<!-- Status History Timeline -->
<div class="section">
  <StatusTimeline
    :agent-id="agent.agentId"
    @clear-history="handleClearStatusHistory"
  />
</div>
```

#### 添加导入和处理函数

```typescript
import { clearAgentStatusHistory } from '../composables/useAgentStatusHistory'
import StatusTimeline from './StatusTimeline.vue'

const handleClearStatusHistory = (): void => {
  if (props.agent) {
    clearAgentStatusHistory(props.agent.agentId)
    success('状态历史记录已清除')
  }
}
```

## 功能特性

### 查看模式

| 模式 | 说明 |
|------|------|
| **全部** | 显示所有历史记录（最多 100 条） |
| **最近 10 条** | 显示最近的 10 条状态变化 |
| **今天** | 显示今天的状态变化 |

### 统计信息

| 指标 | 说明 |
|------|------|
| **总变化次数** | 状态变化的总次数 |
| **平均持续时间** | 每个状态的平均持续时间 |

### 时间显示

- **刚刚** - 小于 1 分钟
- **X 秒前** - 小于 1 分钟
- **X 分钟前** - 小于 1 小时
- **X 小时前** - 小于 24 小时
- **完整日期时间** - 超过 24 小时

### 持续时间格式

- **X 秒** - 小于 1 分钟
- **X 分钟 Y 秒** - 小于 1 小时
- **X 小时 Y 分钟** - 小于 24 小时
- **X 天 Y 小时** - 超过 24 小时

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (270 modules)

### 功能测试

1. **状态追踪** ✅
   - 状态变化时自动记录
   - 最多保存 100 条记录

2. **时间线显示** ✅
   - 时间线正确显示状态变化
   - 节点和连接线渲染正确

3. **过滤模式** ✅
   - 全部模式显示所有记录
   - 最近 10 条正确过滤
   - 今天模式正确过滤

4. **统计信息** ✅
   - 总变化次数正确计算
   - 平均持续时间正确显示

5. **清除功能** ✅
   - 点击清除按钮删除历史
   - 显示成功提示

## 使用说明

### 查看状态历史

1. 点击 Agent 查看详情
2. 滚动到"状态历史"部分
3. 选择查看模式（全部/最近 10 条/今天）
4. 浏览状态变化时间线

### 清除历史记录

1. 在状态历史面板中
2. 点击"清除"按钮
3. 确认清除

### 自动记录

状态历史会在 Agent 状态变化时自动记录，无需手动操作。

## 集成指南

### 手动添加状态变化

```typescript
import { addStatusChange } from './composables/useAgentStatusHistory'

addStatusChange(
  agentId,
  'online',      // 新状态
  'offline',     // 旧状态
  {              // 可选元数据
    reason: '连接恢复',
    source: 'websocket'
  }
)
```

### 监听状态变化

```typescript
watch(() => agent.status, (newStatus, oldStatus) => {
  if (newStatus !== oldStatus) {
    addStatusChange(agent.agentId, newStatus, oldStatus)
  }
})
```

## 文件变更

### 新增文件

1. **src/composables/useAgentStatusHistory.ts** (~185 lines)
   - StatusHistoryEntry 接口定义
   - 状态历史存储和管理
   - useAgentStatusHistory composable
   - 辅助函数（addStatusChange, clearAgentStatusHistory）

2. **src/components/StatusTimeline.vue** (~410 lines)
   - 时间线可视化组件
   - 多种查看模式
   - 统计信息显示
   - 状态特定颜色

### 修改文件

1. **src/components/AgentDetailPanel.vue**
   - 导入 StatusTimeline 组件
   - 导入 clearAgentStatusHistory 函数
   - 添加 StatusTimeline 到详情面板
   - 添加 handleClearStatusHistory 处理函数

## 未来扩展

### 潜在增强

1. **图表视图** - 添加状态持续时间图表
2. **导出功能** - 导出状态历史为 CSV/JSON
3. **搜索过滤** - 按状态类型搜索
4. **状态对比** - 对比多个 Agent 的状态历史
5. **告警规则** - 基于状态历史设置告警

## 总结

Agent Status History Timeline 功能成功实现了：

✅ **状态追踪** - 记录每次 Agent 状态变化
✅ **可视化时间线** - 以时间线形式展示状态历史
✅ **统计信息** - 显示状态变化次数、平均持续时间
✅ **过滤选项** - 支持查看全部、最近、今天的记录
✅ **清除功能** - 允许清除历史记录

该功能为用户提供了强大的状态历史分析工具，帮助诊断和理解 Agent 行为。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

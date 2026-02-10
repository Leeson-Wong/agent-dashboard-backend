# 实时活动源功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

用户需要实时了解系统中发生的事件和活动。实时活动源提供了一个集中的界面来显示所有 Agent 相关的事件，包括状态变化、任务完成、错误等。

## 需求分析

### 核心需求

1. **实时显示** - 显示最新的系统活动事件
2. **事件分类** - 按类型（信息/成功/警告/错误）分类显示
3. **事件过滤** - 支持按类型过滤事件
4. **持久化** - 事件数据持久化到 localStorage
5. **时间分组** - 按日期（今天/昨天/更早）分组显示
6. **自动清理** - 支持清理旧事件

### 技术要求

- 使用 localStorage 持久化事件
- 支持最大事件数量限制
- 提供友好的时间显示（相对时间）
- 动画效果和过渡

## 实现方案

### 1. 创建活动源 Composable

**文件**: `src/composables/useActivityFeed.ts`

#### 核心数据结构

```typescript
export interface ActivityEvent {
  id: string
  type: 'info' | 'success' | 'warning' | 'error'
  agentId?: string
  agentName?: string
  title: string
  message: string
  timestamp: number
  icon?: string
  category?: string
}

export type ActivityFilter = 'all' | 'info' | 'success' | 'warning' | 'error'
```

#### 主要功能

**1. 添加事件**

```typescript
const addEvent = (event: Omit<ActivityEvent, 'id' | 'timestamp'>): void => {
  const newEvent: ActivityEvent = {
    ...event,
    id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    timestamp: Date.now(),
    icon: event.icon || getDefaultIcon(event.type)
  }

  events.value.unshift(newEvent)

  // Keep only max events
  if (events.value.length > maxEvents.value) {
    events.value = events.value.slice(0, maxEvents.value)
  }

  saveEvents()
}
```

**2. 便捷方法**

```typescript
const addInfoEvent = (title: string, message: string, agentId?: string, agentName?: string, category?: string): void => {
  addEvent({ type: 'info', title, message, agentId, agentName, category })
}

const addSuccessEvent = (title: string, message: string, agentId?: string, agentName?: string, category?: string): void => {
  addEvent({ type: 'success', title, message, agentId, agentName, category })
}

const addWarningEvent = (title: string, message: string, agentId?: string, agentName?: string, category?: string): void => {
  addEvent({ type: 'warning', title, message, agentId, agentName, category })
}

const addErrorEvent = (title: string, message: string, agentId?: string, agentName?: string, category?: string): void => {
  addEvent({ type: 'error', title, message, agentId, agentName, category })
}
```

**3. 过滤和分组**

```typescript
// Filtered events
const filteredEvents = computed(() => {
  if (filter.value === 'all') {
    return events.value
  }
  return events.value.filter(e => e.type === filter.value)
})

// Events grouped by date
const groupedEvents = computed(() => {
  const groups: Record<string, ActivityEvent[]> = {}

  filteredEvents.value.forEach(event => {
    const date = new Date(event.timestamp)
    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)

    let groupKey = ''

    if (date.toDateString() === today.toDateString()) {
      groupKey = '今天'
    } else if (date.toDateString() === yesterday.toDateString()) {
      groupKey = '昨天'
    } else {
      groupKey = date.toLocaleDateString('zh-CN', {
        month: 'long',
        day: 'numeric'
      })
    }

    if (!groups[groupKey]) {
      groups[groupKey] = []
    }
    groups[groupKey].push(event)
  })

  return groups
})
```

**4. 时间格式化**

```typescript
const formatTimestamp = (timestamp: number): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays < 7) return `${diffDays} 天前`

  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
```

**5. 持久化**

```typescript
// Save events to localStorage
const saveEvents = (): void => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      events: events.value,
      filter: filter.value
    }))
  } catch (e) {
    console.error('Failed to save activity feed:', e)
  }
}

// Load events from localStorage
const loadEvents = (): void => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      events.value = parsed.events || []
      filter.value = parsed.filter || 'all'
    }
  } catch (e) {
    console.error('Failed to load activity feed:', e)
  }
}
```

### 2. 创建活动源组件

**文件**: `src/components/ActivityFeed.vue`

#### 组件结构

```
ActivityFeed
├── Header
│   ├── Title & Count
│   ├── Filter Buttons (All/Info/Success/Warning/Error)
│   ├── Clear Buttons (Auto Clear/Clear All)
│   └── Collapse Button
└── Content
    ├── Empty State
    └── Event Groups
        ├── Group Header (Date)
        └── Event Items
            ├── Icon
            ├── Content
            │   ├── Title & Time
            │   ├── Message
            │   └── Agent Info
            └── Remove Button
```

#### 功能特性

1. **过滤按钮** - 按类型过滤事件
2. **事件计数** - 显示每种类型的事件数量
3. **折叠功能** - 支持折叠/展开
4. **清理功能** - 清除旧事件或全部清除
5. **相对时间** - 友好的时间显示
6. **动画效果** - 列表项进入动画

#### 组件实现

```vue
<template>
  <div class="activity-feed" :class="{ 'is-collapsed': isCollapsed }">
    <div class="feed-header">
      <div class="header-left">
        <h3 class="feed-title">活动源</h3>
        <span class="feed-count">{{ eventCounts.all }}</span>
      </div>
      <div class="header-actions">
        <button
          v-for="type in ['info', 'success', 'warning', 'error']"
          :key="type"
          class="filter-btn"
          :class="{ active: filter === type }"
          @click="filter = type"
        >
          <span class="filter-icon">{{ getTypeIcon(type) }}</span>
          <span v-if="eventCounts[type] > 0" class="filter-count">{{ eventCounts[type] }}</span>
        </button>
        <!-- ... more buttons ... -->
      </div>
    </div>

    <div class="feed-content">
      <div v-for="(events, date) in groupedEvents" :key="date" class="feed-group">
        <div class="group-header">{{ date }}</div>
        <div class="group-events">
          <div
            v-for="event in events"
            :key="event.id"
            class="feed-item"
            :class="`type-${event.type}`"
          >
            <div class="item-icon">{{ event.icon }}</div>
            <div class="item-content">
              <div class="item-header">
                <span class="item-title">{{ event.title }}</span>
                <span class="item-time">{{ formatTimestamp(event.timestamp) }}</span>
              </div>
              <div v-if="event.message" class="item-message">
                {{ event.message }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
```

### 3. 集成到主应用

**文件**: `src/App.vue`

#### 添加组件

```vue
<template>
  <div class="app">
    <!-- ... other components ... -->

    <!-- Activity Feed -->
    <div class="activity-feed-wrapper">
      <ActivityFeed />
    </div>
  </div>
</template>

<script setup lang="ts">
import ActivityFeed from './components/ActivityFeed.vue'
</script>

<style scoped>
.activity-feed-wrapper {
  position: fixed;
  top: 80px;
  right: 20px;
  width: 350px;
  max-width: calc(100vw - 40px);
  z-index: 800;
}

@media (max-width: 768px) {
  .activity-feed-wrapper {
    top: auto;
    bottom: 80px;
    right: 20px;
    width: 300px;
  }
}
</style>
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **事件显示** ✅
   - 事件按时间倒序显示
   - 事件类型图标正确显示

2. **事件过滤** ✅
   - 按类型过滤正常工作
   - 过滤按钮显示正确状态

3. **时间分组** ✅
   - 今天/昨天/更早分组正确
   - 分组标题显示正确

4. **时间格式化** ✅
   - 相对时间显示（刚刚、X分钟前）
   - 较早时间显示日期

5. **折叠功能** ✅
   - 折叠/展开正常工作
   - 动画流畅

6. **清理功能** ✅
   - 清除旧事件
   - 清除全部事件

7. **持久化** ✅
   - 事件保存到 localStorage
   - 页面刷新后数据保留

## 样式实现

### 活动源容器

```css
.activity-feed {
  background: rgba(30, 41, 59, 0.98);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 600px;
}
```

### 事件项样式

```css
.feed-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(100, 116, 139, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.feed-item.type-info {
  border-left: 3px solid #60a5fa;
}

.feed-item.type-success {
  border-left: 3px solid #22c55e;
}

.feed-item.type-warning {
  border-left: 3px solid #f59e0b;
}

.feed-item.type-error {
  border-left: 3px solid #ef4444;
}
```

### 过滤按钮

```css
.filter-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: transparent;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn.active {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.4);
  color: #60a5fa;
}
```

### 动画效果

```css
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
```

## 技术要点

### 1. 唯一 ID 生成

使用时间戳 + 随机字符串生成唯一 ID：

```typescript
id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
```

### 2. 最大事件限制

使用 `unshift` 添加新事件到开头，然后截断：

```typescript
events.value.unshift(newEvent)
if (events.value.length > maxEvents.value) {
  events.value = events.value.slice(0, maxEvents.value)
}
```

### 3. 日期分组

根据时间戳判断日期类型：

```typescript
if (date.toDateString() === today.toDateString()) {
  groupKey = '今天'
} else if (date.toDateString() === yesterday.toDateString()) {
  groupKey = '昨天'
} else {
  groupKey = date.toLocaleDateString('zh-CN', {
    month: 'long',
    day: 'numeric'
  })
}
```

### 4. 相对时间计算

计算时间差并返回友好的字符串：

```typescript
const diffMs = now.getTime() - date.getTime()
const diffMins = Math.floor(diffMs / 60000)
const diffHours = Math.floor(diffMs / 3600000)
const diffDays = Math.floor(diffMs / 86400000)
```

### 5. 过滤计数

实时计算每种类型的事件数量：

```typescript
const eventCounts = computed(() => {
  return {
    all: events.value.length,
    info: events.value.filter(e => e.type === 'info').length,
    success: events.value.filter(e => e.type === 'success').length,
    warning: events.value.filter(e => e.type === 'warning').length,
    error: events.value.filter(e => e.type === 'error').length
  }
})
```

## 文件变更

### 新增文件

1. **src/composables/useActivityFeed.ts** (~260 行)
   - 活动源状态管理
   - 事件添加/删除/清理
   - 过滤和分组逻辑
   - 时间格式化
   - 持久化功能

2. **src/components/ActivityFeed.vue** (~280 行)
   - 活动源 UI 组件
   - 过滤按钮
   - 事件列表显示
   - 折叠/展开功能

### 修改文件

1. **src/App.vue**
   - 导入 ActivityFeed 组件
   - 添加到模板
   - 添加定位样式

## 使用说明

### 基本使用

```vue
<template>
  <ActivityFeed />
</template>

<script setup lang="ts">
import { useActivityFeed } from './composables/useActivityFeed'

const { addInfoEvent, addSuccessEvent, addWarningEvent, addErrorEvent } = useActivityFeed()

// 添加事件
addInfoEvent('Agent 已启动', 'Agent agent-001 已成功启动', 'agent-001', 'Agent 1', 'agent-started')
addSuccessEvent('任务完成', '任务 task-123 已完成', 'agent-001', 'Agent 1', 'task-completed')
addWarningEvent('内存使用警告', 'Agent 内存使用超过 80%', 'agent-002', 'Agent 2', 'memory-warning')
addErrorEvent('连接失败', '无法连接到数据库', '', '', 'connection-error')
</script>
```

### 编程式使用

```typescript
import { useActivityFeed } from './composables/useActivityFeed'

const activityFeed = useActivityFeed()

// 添加单个事件
activityFeed.addEvent({
  type: 'success',
  title: '操作成功',
  message: 'Agent 已成功创建',
  agentId: 'agent-123',
  agentName: 'Test Agent',
  category: 'agent-created'
})

// 批量添加事件
activityFeed.addEvents([
  { type: 'info', title: '事件1', message: '消息1' },
  { type: 'success', title: '事件2', message: '消息2' }
])

// 清除旧事件（7天前）
activityFeed.clearOldEvents(7 * 24 * 60 * 60 * 1000)

// 清除所有事件
activityFeed.clearEvents()
```

## 已知限制

1. **本地存储限制** - localStorage 有大小限制（通常 5-10MB）
2. **单页面限制** - 活动源只在当前页面可见
3. **无后端同步** - 事件不与其他设备/标签页同步

## 未来改进

1. **后端集成** - 与后端 WebSocket 集成，实时接收事件
2. **事件聚合** - 相似事件聚合显示，避免刷屏
3. **事件详情** - 点击事件查看详细信息
4. **导出功能** - 导出活动日志
5. **声音提醒** - 重要事件声音提示
6. **桌面通知** - 浏览器桌面通知集成

## 总结

实时活动源功能成功实现了：

✅ **事件显示** - 实时显示系统活动事件
✅ **事件分类** - 按类型（信息/成功/警告/错误）分类
✅ **事件过滤** - 支持按类型过滤
✅ **时间分组** - 按日期分组显示
✅ **相对时间** - 友好的时间显示
✅ **持久化** - localStorage 持久化
✅ **自动清理** - 清除旧事件功能
✅ **折叠功能** - 支持折叠/展开
✅ **动画效果** - 流畅的列表动画

该功能为用户提供了实时了解系统活动的能力，增强了系统的可观测性和用户体验。

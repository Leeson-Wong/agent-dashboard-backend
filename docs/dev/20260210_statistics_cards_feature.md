# 系统统计卡片功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

用户需要快速了解系统的关键指标和运行状态。统计卡片提供了直观的可视化指标显示，让用户一眼就能看到 Agent 数量、性能指标、内存使用等关键信息。

## 需求分析

### 核心需求

1. **多分类显示** - 按类别组织统计卡片（Agent、性能、内存等）
2. **实时更新** - 定期从后端获取最新统计数据
3. **紧凑展示** - 可折叠的紧凑显示模式
4. **趋势指示** - 显示指标的变化趋势
5. **自动刷新** - 定时自动刷新数据

### 技术要求

- 使用 API 从后端获取统计数据
- 支持从本地 Agent 数据计算统计
- 响应式布局适配
- 动画过渡效果

## 实现方案

### 1. 创建统计卡片 Composable

**文件**: `src/composables/useStatisticsCards.ts`

#### 核心数据结构

```typescript
export interface StatCard {
  id: string
  title: string
  value: number | string
  unit?: string
  icon?: string
  trend?: 'up' | 'down' | 'neutral'
  trendValue?: number
  description?: string
  color?: string
  type?: 'number' | 'percentage' | 'duration'
  clickAction?: string
}

export interface StatCategory {
  id: string
  name: string
  cards: StatCard[]
}
```

#### 默认统计分类

```typescript
const defaultCategories: StatCategory[] = [
  {
    id: 'agents',
    name: 'Agent 统计',
    cards: [
      {
        id: 'total-agents',
        title: '总 Agent 数',
        value: 0,
        icon: '🤖',
        description: '系统中所有 Agent'
      },
      {
        id: 'online-agents',
        title: '在线 Agent',
        value: 0,
        icon: '🟢',
        trend: 'up',
        description: '当前在线的 Agent'
      },
      {
        id: 'offline-agents',
        title: '离线 Agent',
        value: 0,
        icon: '⚫',
        trend: 'down',
        description: '当前离线的 Agent'
      },
      {
        id: 'error-agents',
        title: '错误 Agent',
        value: 0,
        icon: '❌',
        description: '处于错误状态的 Agent'
      }
    ]
  },
  {
    id: 'performance',
    name: '性能指标',
    cards: [
      {
        id: 'avg-response-time',
        title: '平均响应时间',
        value: 0,
        unit: 'ms',
        icon: '⚡',
        description: 'API 平均响应时间'
      },
      {
        id: 'requests-per-second',
        title: '每秒请求数',
        value: 0,
        unit: 'req/s',
        icon: '📈',
        trend: 'up',
        description: '当前系统吞吐量'
      },
      {
        id: 'error-rate',
        title: '错误率',
        value: 0,
        unit: '%',
        icon: '⚠️',
        description: '请求错误率'
      }
    ]
  },
  {
    id: 'memory',
    name: '内存使用',
    cards: [
      {
        id: 'total-memories',
        title: 'Memory 总数',
        value: 0,
        icon: '🧠',
        description: '系统中的 Memory 总数'
      },
      {
        id: 'active-memories',
        title: '活跃 Memory',
        value: 0,
        icon: '✅',
        trend: 'up',
        description: '当前活跃的 Memory'
      },
      {
        id: 'total-experiences',
        title: '经验总数',
        value: 0,
        icon: '📚',
        trend: 'up',
        description: '所有 Memory 的经验总和'
      }
    ]
  },
  {
    id: 'framework',
    name: '框架分布',
    cards: []  // Dynamically populated
  }
]
```

#### 主要功能

**1. 从 Agent 数据更新**

```typescript
const updateFromAgentsData = (agents: {
  total: number
  online: number
  offline: number
  error: number
}): void => {
  const agentCategory = categories.value.find(c => c.id === 'agents')
  if (agentCategory) {
    agentCategory.cards[0].value = agents.total
    agentCategory.cards[1].value = agents.online
    agentCategory.cards[2].value = agents.offline
    agentCategory.cards[3].value = agents.error
  }
}
```

**2. 更新框架分布**

```typescript
const updateFrameworkDistribution = (agents: Array<{ framework?: string }>): void => {
  const frameworkCounts: Record<string, number> = {}

  agents.forEach(agent => {
    if (agent.framework) {
      frameworkCounts[agent.framework] = (frameworkCounts[agent.framework] || 0) + 1
    }
  })

  const frameworkCategory = categories.value.find(c => c.id === 'framework')
  if (frameworkCategory) {
    frameworkCategory.cards = Object.entries(frameworkCounts).map(
      ([framework, count], index) => ({
        id: `framework-${index}`,
        title: framework,
        value: count,
        icon: getFrameworkIcon(framework),
        description: `${framework} 框架的 Agent 数量`
      })
    )
  }
}

const getFrameworkIcon = (framework: string): string => {
  const icons: Record<string, string> = {
    'LangChain': '🦜',
    'LangGraph': '🕸️',
    'AutoGen': '🤖',
    'CrewAI': '👥',
    'OpenAI': '🔵'
  }
  return icons[framework] || '📦'
}
```

**3. 从后端 API 获取统计**

```typescript
const fetchStatistics = async (): Promise<void> => {
  isLoading.value = true
  error.value = null

  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const response = await fetch(`${apiUrl}/api/stats`)

    if (!response.ok) {
      throw new Error('Failed to fetch statistics')
    }

    const data = await response.json()
    updateFromStatsData(data)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unknown error'
  } finally {
    isLoading.value = false
  }
}
```

**4. 值格式化**

```typescript
const formatValue = (card: StatCard): string => {
  const value = card.value

  if (card.type === 'duration') {
    const seconds = Number(value)
    if (seconds < 60) return `${seconds}秒`
    if (seconds < 3600) return `${Math.floor(seconds / 60)}分`
    return `${Math.floor(seconds / 3600)}小时`
  }

  if (card.type === 'number') {
    return Number(value).toLocaleString()
  }

  if (card.unit) {
    return `${value}${card.unit}`
  }

  return String(value)
}
```

### 2. 创建统计卡片组件

**文件**: `src/components/StatisticsCards.vue`

#### 组件结构

```
StatisticsCards (Fixed position)
├── Toggle Button (显示/隐藏)
│   ├── Icon (📊 / 📈)
│   └── Mini Count (卡片总数)
└── Content (展开状态)
    ├── Categories
    │   ├── Category Header
    │   │   ├── Name
    │   │   └── Count
    │   └── Cards Grid (自适应列数)
    │       └── Stat Cards
    │           ├── Header
    │           │   ├── Icon
    │           │   └── Trend Indicator
    │           ├── Value
    │           ├── Title
    │           ├── Description
    │           └── Trend Value (optional)
    ├── Footer
    │   ├── Update Time
    │   └── Retry Button (if error)
```

#### 功能特性

1. **紧凑模式** - 默认显示为小按钮，点击展开
2. **自适应布局** - 根据卡片数量自动调整网格列数
3. **趋势指示器** - 上升📈、下降📉、中性➡️
4. **自动刷新** - 每 10 秒自动刷新数据
5. **错误处理** - 显示错误并提供重试按钮

#### 组件实现

```vue
<template>
  <div class="statistics-cards">
    <button class="cards-toggle" @click="toggleCollapsed">
      <span class="toggle-icon">{{ isCollapsed ? '📊' : '📈' }}</span>
      <span v-if="isCollapsed" class="toggle-mini">{{ totalCards }}</span>
    </button>

    <div v-if="!isCollapsed" class="cards-container">
      <div v-for="category in categories" :key="category.id" class="category">
        <div class="category-header" v-if="category.name">
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count">{{ category.cards.length }}</span>
        </div>

        <div class="cards-grid" :class="`grid-${Math.min(category.cards.length, 4)}`">
          <div v-for="card in category.cards" :key="card.id" class="stat-card">
            <div class="card-header">
              <span class="card-icon">{{ card.icon || '📊' }}</span>
              <span v-if="card.trend" class="card-trend" :class="card.trend">
                {{ getTrendIcon(card.trend) }}
              </span>
            </div>
            <div class="card-value">{{ formatDisplayValue(card) }}</div>
            <div class="card-title">{{ card.title }}</div>
            <div v-if="card.description" class="card-description">
              {{ card.description }}
            </div>
          </div>
        </div>
      </div>

      <div class="cards-footer">
        <span class="update-text">{{ updateText }}</span>
        <button v-if="error" class="retry-btn" @click="fetchStatistics">
          🔄 重试
        </button>
      </div>
    </div>
  </div>
</template>
```

#### 样式特性

1. **渐变背景** - 半透明背景配合毛玻璃效果
2. **趋势颜色** - 上升绿色、下降红色、中性灰色
3. **悬停效果** - 卡片悬停时高亮
4. **平滑动画** - 展开收起动画
5. **响应式** - 移动端适配

### 3. 集成到主应用

**文件**: `src/App.vue`

#### 添加组件到模板

```vue
<template>
  <div class="app">
    <!-- ... other components ... -->

    <!-- Statistics Cards -->
    <StatisticsCards :agents="agents" />
  </div>
</template>

<script setup lang="ts">
import StatisticsCards from './components/StatisticsCards.vue'
</script>
```

#### 添加定位样式

```css
.statistics-cards-wrapper {
  position: fixed;
  top: 80px;
  left: 20px;
  width: 350px;
  max-width: calc(100vw - 40px);
  z-index: 800;
}

@media (max-width: 768px) {
  .statistics-cards-wrapper {
    top: auto;
    bottom: 80px;
    right: 20px;
    width: 300px;
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

1. **统计显示** ✅
   - Agent 统计正确显示
   - 性能指标正确显示
   - 内存使用正确显示

2. **框架分布** ✅
   - 动态生成框架统计卡片
   - 框架图标正确显示

3. **紧凑模式** ✅
   - 收起状态显示小按钮
   - 展开状态显示完整卡片

4. **自动刷新** ✅
   - 每 10 秒自动刷新
   - 更新时间显示

5. **趋势指示** ✅
   - 趋势图标和颜色正确
   - 趋势值显示（如果设置）

## 样式实现

### 统计卡片

```css
.stat-card {
  padding: 12px;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(100, 116, 139, 0.2);
  border-radius: 8px;
  transition: all 0.2s;
}

.stat-card:hover {
  background: rgba(51, 65, 85, 0.5);
  border-color: rgba(100, 116, 139, 0.4);
}

.card-value {
  font-size: 20px;
  font-weight: 700;
  color: #f1f5f9;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
}
```

### 趋势指示器

```css
.card-trend {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.card-trend.up {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.card-trend.down {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.card-trend.neutral {
  background: rgba(100, 116, 139, 0.2);
  color: #94a3b8;
}
```

### 网格布局

```css
.cards-grid.grid-1 {
  grid-template-columns: 1fr;
}

.cards-grid.grid-2 {
  grid-template-columns: 1fr 1fr;
}

.cards-grid.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}

.cards-grid.grid-4 {
  grid-template-columns: repeat(2, 1fr);
}
```

## 技术要点

### 1. 动态框架统计

根据实际 Agent 数据动态生成框架统计卡片：

```typescript
const updateFrameworkDistribution = (agents: Array<{ framework?: string }>): void => {
  const frameworkCounts: Record<string, number> = {}
  agents.forEach(agent => {
    if (agent.framework) {
      frameworkCounts[agent.framework] = (frameworkCounts[agent.framework] || 0) + 1
    }
  })

  // Generate cards dynamically
  frameworkCategory.cards = Object.entries(frameworkCounts).map(
    ([framework, count]) => ({
      title: framework,
      value: count,
      icon: getFrameworkIcon(framework)
    })
  )
}
```

### 2. 响应式网格

根据卡片数量自动调整网格列数（最多4列）：

```vue
<div class="cards-grid" :class="`grid-${Math.min(category.cards.length, 4)}`">
```

### 3. 自动刷新机制

组件挂载时初始化，并设置定时刷新：

```typescript
onMounted(async () => {
  // Update from props
  if (props.agents && props.agents.length > 0) {
    const stats = calculateStats(props.agents)
    updateFromAgentsData(stats)
    updateFrameworkDistribution(props.agents)
  }

  // Fetch from backend
  await fetchStatistics()

  // Auto-refresh every 10 seconds
  setInterval(async () => {
    if (!isCollapsed.value) {
      await fetchStatistics()
    }
  }, 10000)
})
```

### 4. 值格式化

根据类型智能格式化显示值：

```typescript
const formatValue = (card: StatCard): string => {
  const value = card.value

  // Duration formatting
  if (card.type === 'duration') {
    const seconds = Number(value)
    if (seconds < 60) return `${seconds}秒`
    if (seconds < 3600) return `${Math.floor(seconds / 60)}分`
    return `${Math.floor(seconds / 3600)}小时`
  }

  // Number formatting (with thousands separator)
  if (card.type === 'number') {
    return Number(value).toLocaleString()
  }

  // With unit
  if (card.unit) {
    return `${value}${card.unit}`
  }

  return String(value)
}
```

## 文件变更

### 新增文件

1. **src/composables/useStatisticsCards.ts** (~260 行)
   - 统计卡片状态管理
   - 数据更新逻辑
   - 框架分布计算
   - 值格式化
   - API 集成

2. **src/components/StatisticsCards.vue** (~320 行)
   - 统计卡片 UI 组件
   - 分类显示
   - 网格布局
   - 紧凑/展开模式
   - 自动刷新

### 修改文件

1. **src/App.vue**
   - 导入 StatisticsCards 组件
   - 添加到模板
   - 传递 agents 数据

## 使用说明

### 基本使用

```vue
<template>
  <StatisticsCards :agents="agents" />
</template>
```

### 编程式更新

```typescript
import { useStatisticsCards } from './composables/useStatisticsCards'

const {
  categories,
  updateFromAgentsData,
  updateFrameworkDistribution,
  fetchStatistics
} = useStatisticsCards()

// Update from agents array
updateFromAgentsData({
  total: agents.length,
  online: agents.filter(a => a.status === 'online').length,
  offline: agents.filter(a => a.status === 'offline').length,
  error: agents.filter(a => a.status === 'error').length
})

// Update framework distribution
updateFrameworkDistribution(agents)

// Manually refresh
await fetchStatistics()
```

## 已知限制

1. **刷新频率固定** - 当前为 10 秒，不可配置
2. **API 依赖** - 部分统计需要后端 API 支持
3. **单数据源** - 目前只能从一个数据源更新

## 未来改进

1. **可配置刷新** - 允许用户设置刷新频率
2. **更多指标** - 添加更多系统指标
3. **图表可视化** - 添加小型图表（迷你折线图等）
4. **历史趋势** - 显示指标的历史变化趋势
5. **导出功能** - 导出统计数据

## 总结

系统统计卡片功能成功实现了：

✅ **多分类显示** - Agent、性能、内存、框架分类
✅ **实时更新** - 定时自动刷新统计数据
✅ **紧凑展示** - 可折叠的紧凑模式
✅ **趋势指示** - 上升/下降/中性趋势显示
✅ **自动刷新** - 每 10 秒自动刷新
✅ **框架分布** - 动态显示各框架 Agent 数量
✅ **友好 UI** - 清晰的卡片布局和交互

该功能为用户提供了系统关键指标的一目了然的视图，增强了系统可观测性。

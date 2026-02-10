# Agent 详情面板增强

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

Agent 详情面板需要显示更多有用的统计信息,帮助用户快速了解 Agent 的运行状况和活动情况。

**目标**:
1. 添加运行统计信息
2. 显示状态指示器
3. 显示距离上次活动时间
4. 改进空值处理

---

## 实现方案

### 前端修改 (AgentDetailPanel.vue)

#### 1. 添加运行统计部分

```vue
<!-- Runtime Statistics -->
<div class="section">
  <h3>运行统计</h3>
  <div class="stats-grid">
    <div class="stat-card">
      <div class="stat-icon">⏱️</div>
      <div class="stat-info">
        <div class="stat-label">运行时长</div>
        <div class="stat-value">{{ runtimeDuration }}</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">📊</div>
      <div class="stat-info">
        <div class="stat-label">当前状态</div>
        <div class="stat-value status-indicator" :class="agent.status">
          {{ statusText }}
        </div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">🕐</div>
      <div class="stat-info">
        <div class="stat-label">距离上次活动</div>
        <div class="stat-value">{{ timeSinceLastActivity }}</div>
      </div>
    </div>
  </div>
</div>
```

#### 2. 添加运行时长计算

```typescript
const runtimeDuration = computed(() => {
  if (!props.agent) return '-'
  const created = new Date(props.agent.createdAt)
  const now = new Date()
  const diff = now.getTime() - created.getTime()

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (days > 0) return `${days}天 ${hours}小时`
  if (hours > 0) return `${hours}小时 ${minutes}分钟`
  if (minutes > 0) return `${minutes}分钟`
  return '不到 1 分钟'
})
```

#### 3. 添加距离上次活动时间计算

```typescript
const timeSinceLastActivity = computed(() => {
  if (!props.agent) return '-'
  const lastActivity = new Date(props.agent.lastActivity)
  const now = new Date()
  const diff = now.getTime() - lastActivity.getTime()

  if (diff < 1000) return '刚刚'
  if (diff < 60000) return `${Math.floor(diff / 1000)}秒前`
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
})
```

#### 4. 添加统计卡片样式

```css
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(100, 116, 139, 0.2);
  border-radius: 8px;
}

.stat-value.status-indicator {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.stat-value.status-indicator.online {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
}

.stat-value.status-indicator.busy {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}
```

#### 5. 改进空值处理

```vue
<div class="value">{{ agent.framework || '-' }}</div>
<div class="value">{{ agent.language || '-' }}</div>
```

---

## 功能说明

### 统计信息

| 统计项 | 说明 | 示例 |
|--------|------|------|
| 运行时长 | Agent 创建后运行的时间 | 2天 5小时 |
| 当前状态 | Agent 当前状态 | 在线 (带颜色) |
| 距上次活动 | 最后活动到现在的间隔 | 3分钟前 |

### 时间格式化规则

| 时间范围 | 显示格式 |
|---------|---------|
| < 1 秒 | 刚刚 |
| < 1 分钟 | X秒前 |
| < 1 小时 | X分钟前 |
| < 1 天 | X小时前 |
| ≥ 1 天 | X天前 |

### 运行时长规则

| 时间范围 | 显示格式 |
|---------|---------|
| < 1 分钟 | 不到 1 分钟 |
| < 1 小时 | X分钟 |
| < 1 天 | X小时 X分钟 |
| ≥ 1 天 | X天 X小时 |

### 状态颜色

| 状态 | 颜色 | 背景色 |
|------|------|--------|
| online | 绿色 #22c55e | rgba(34, 197, 94, 0.2) |
| ready | 蓝色 #3b82f6 | rgba(59, 130, 246, 0.2) |
| busy | 橙色 #f59e0b | rgba(245, 158, 11, 0.2) |
| thinking | 紫色 #8b5cf6 | rgba(139, 92, 246, 0.2) |
| offline | 灰色 #6b7280 | rgba(100, 116, 139, 0.2) |
| error | 红色 #ef4444 | rgba(239, 68, 68, 0.2) |
| paused | 紫色 #a855f7 | rgba(168, 85, 247, 0.2) |

---

## UI 效果

### 详情面板 - 运行统计

```
┌─────────────────────────────────────────┐
│ 专业小说作家                        [在线] │
│ ─────────────────────────────────────────│
│                                             │
│ 框架    语言    Agent ID     服务器         │
│ CrewAI  Python  97f2a020...  DESKTOP-...   │
│                                             │
│ 运行统计                                    │
│ ─────────────────────────────────────────│
│ ⏱️ 运行时长                                 │
│    2小时 15分钟                              │
│                                             │
│ 📊 当前状态                                 │
│    [在线]                                   │
│                                             │
│ 🕐 距离上次活动                             │
│    3分钟前                                  │
│                                             │
│ 当前活动                                    │
│ ⚡ 正在撰写小说第3章                         │
│                                             │
│ 时间线                                      │
│ • 最后活动   2026-02-10 18:05:32          │
│ • 创建时间   2026-02-10 16:00:00          │
│                                             │
│ [查看日志] [任务历史] [暂停]               │
└─────────────────────────────────────────┘
```

### 空值处理

当 `framework` 或 `language` 为空时显示 `-`:

``│ 框架    语言    Agent ID     服务器         │
│ -       -       97f2a020...  DESKTOP-...   │
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **选择 Agent**:
   - 点击列表中的任意 Agent
   - 预期: 显示详情面板

3. **检查运行统计**:
   - 查看 "运行时长" 显示
   - 预期: 显示正确的时间
   - 查看 "当前状态" 显示
   - 预期: 显示状态标签,带颜色
   - 查看 "距离上次活动" 显示
   - 预期: 显示相对时间

### 2. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 新 Agent | 选择刚创建的 Agent | 显示 "不到 1 分钟" |
| 长时间运行 | 选择运行数天的 Agent | 显示 "X天 X小时" |
| 无活动 | 长时间未活动 | 显示 "X天前" |
| 空框架/语言 | Agent 无框架信息 | 显示 "-" |
| 无选择 | 未选择 Agent | 显示空状态 |

### 3. 状态颜色测试

| 状态 | 预期颜色 |
|------|---------|
| 在线 | 绿色 |
| 忙碌 | 橙色 |
| 思考中 | 紫色 |
| 离线 | 灰色 |
| 错误 | 红色 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 运行时长显示 | ✅ 通过 | 正确计算和显示 |
| 状态指示器 | ✅ 通过 | 状态带颜色显示 |
| 距离上次活动 | ✅ 通过 | 相对时间正确 |
| 空值处理 | ✅ 通过 | 显示 "-" |
| 新 Agent | ✅ 通过 | 显示 "不到 1 分钟" |
| 长时间运行 | ✅ 通过 | 显示天和小时 |
| 统计卡片样式 | ✅ 完成 | 网格布局美观 |
| 响应式布局 | ✅ 完成 | 卡片宽度自适应 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentDetailPanel.vue**
   - 添加运行统计部分 (line 35-63)
   - 添加 runtimeDuration computed (line 173-188)
   - 添加 activePercentage computed (line 190-199)
   - 添加 updateCount computed (line 201-208)
   - 添加 timeSinceLastActivity computed (line 210-222)
   - 添加 stats-grid 样式 (line 363-368)
   - 添加 stat-card 样式 (line 370-378)
   - 添加 stat-icon 样式 (line 380-383)
   - 添加 stat-info 样式 (line 385-388)
   - 添加 stat-label 样式 (line 390-394)
   - 添加 stat-value 样式 (line 396-403)
   - 添加 status-indicator 样式 (line 405-417)
   - 更新空值处理 (line 19, 23)

---

## 技术细节

### 时间差计算

使用 `Date.getTime()` 获取毫秒级时间戳进行计算:

```typescript
const diff = now.getTime() - created.getTime()
```

### 时间单位换算

| 单位 | 毫秒数 |
|------|--------|
| 1 秒 | 1000 ms |
| 1 分钟 | 60,000 ms |
| 1 小时 | 3,600,000 ms |
| 1 天 | 86,400,000 ms |

### 取模运算

使用取模运算获取剩余时间:

```typescript
const days = Math.floor(diff / (1000 * 60 * 60 * 24))
const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
```

### 条件渲染

使用 `||` 运算符提供默认值:

```vue
<div class="value">{{ agent.framework || '-' }}</div>
```

---

## 优化建议

### 1. 实时更新

使用定时器实时更新统计信息:

```typescript
let updateTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  updateTimer = setInterval(() => {
    // 触发响应式更新
  }, 1000)
})

onUnmounted(() => {
  if (updateTimer) {
    clearInterval(updateTimer)
  }
})
```

### 2. 活动图表

添加活动时间线图表:

```vue
<canvas ref="chartCanvas" class="activity-chart"></canvas>
```

```typescript
import { Chart } from 'chart.js/auto'

const chart = new Chart(chartCanvas.value, {
  type: 'line',
  data: {
    labels: timeLabels,
    datasets: [{
      label: '活跃度',
      data: activityData,
    }],
  },
})
```

### 3. 更多统计

添加更多有用统计:

```typescript
const totalTasksCompleted = computed(() => {
  // 从任务历史获取
})

const averageResponseTime = computed(() => {
  // 计算平均响应时间
})

const errorRate = computed(() => {
  // 计算错误率
})
```

### 4. 对比视图

显示与平均值的对比:

```vue
<div class="comparison">
  <div class="comparison-label">比平均活跃度高</div>
  <div class="comparison-value">+15%</div>
</div>
```

### 5. 趋势指示

显示趋势箭头:

```typescript
const trend = computed(() => {
  // 计算活动趋势
  const recent = getActivityRecent()
  const previous = getActivityPrevious()
  if (recent > previous) return '↑'
  if (recent < previous) return '↓'
  return '→'
})
```

---

## 已知问题

无

---

## 参考资料

- **Date getTime()**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getTime
- **CSS Grid**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Grid_Layout
- **Vue Computed**: https://vuejs.org/guide/essentials/computed.html

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

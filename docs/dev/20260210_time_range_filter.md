# 时间范围筛选

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要快速找到最近活跃的 Agent。通过添加时间范围筛选器，用户可以只查看在最近 5 分钟、1 小时、24 小时或 7 天内活跃的 Agent。这对于监控当前正在运行的任务或最近的活动非常有用。

**目标**:
1. 添加时间范围下拉筛选器
2. 支持多个时间范围选项
3. 根据最后活动时间过滤 Agent
4. 与其他筛选器协同工作

---

## 实现方案

### 前端修改 (AgentListPanel.vue)

#### 1. 添加时间范围状态

```typescript
const timeRangeFilter = ref('') // '' = all, '5m', '1h', '24h', '7d'
```

#### 2. 添加时间范围下拉框

```vue
<select v-model="timeRangeFilter" class="filter-select">
  <option value="">所有时间</option>
  <option value="5m">最近5分钟</option>
  <option value="1h">最近1小时</option>
  <option value="24h">最近24小时</option>
  <option value="7d">最近7天</option>
</select>
```

#### 3. 实现时间范围过滤逻辑

```typescript
// Apply time range filter
if (timeRangeFilter.value) {
  const now = Date.now()
  const ranges: Record<string, number> = {
    '5m': 5 * 60 * 1000,      // 5 minutes
    '1h': 60 * 60 * 1000,     // 1 hour
    '24h': 24 * 60 * 60 * 1000, // 24 hours
    '7d': 7 * 24 * 60 * 60 * 1000, // 7 days
  }
  const cutoffTime = ranges[timeRangeFilter.value] || 0
  filtered = filtered.filter(agent => {
    const activityTime = new Date(agent.lastActivity).getTime()
    return now - activityTime <= cutoffTime
  })
}
```

#### 4. 添加标签映射函数

```typescript
const getTimeRangeLabel = (timeRange: string): string => {
  const labels: Record<string, string> = {
    '5m': '最近5分钟',
    '1h': '最近1小时',
    '24h': '最近24小时',
    '7d': '最近7天',
  }
  return labels[timeRange] || timeRange
}
```

#### 5. 更新活动筛选标签

```vue
<div v-if="timeRangeFilter" class="filter-tag">
  <span class="tag-label">时间:</span>
  <span class="tag-value">{{ getTimeRangeLabel(timeRangeFilter) }}</span>
  <button class="tag-remove" @click="timeRangeFilter = ''" title="清除时间筛选">×</button>
</div>
```

#### 6. 更新清除筛选函数

```typescript
const clearFilters = (): void => {
  searchQuery.value = ''
  statusFilter.value = ''
  frameworkFilter.value = ''
  timeRangeFilter.value = ''
}
```

---

## 功能说明

### 时间范围选项

| 选项 | 时间窗口 | 说明 |
|------|---------|------|
| 所有时间 | 无限制 | 显示所有 Agent |
| 最近5分钟 | 5 分钟 | 显示 5 分钟内活跃的 Agent |
| 最近1小时 | 1 小时 | 显示 1 小时内活跃的 Agent |
| 最近24小时 | 24 小时 | 显示 24 小时内活跃的 Agent |
| 最近7天 | 7 天 | 显示 7 天内活跃的 Agent |

### 过滤逻辑

时间范围基于 Agent 的 `lastActivity` 字段：

```typescript
now - activityTime <= cutoffTime
```

只保留在时间窗口内有活动的 Agent。

### 与其他筛选协同

时间范围筛选与其他筛选器（搜索、状态、框架）组合使用：

```
最终结果 = 搜索结果 ∩ 状态筛选 ∩ 框架筛选 ∩ 时间筛选
```

---

## UI 效果

### 筛选下拉框

```
┌─────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────┐ │
│ │ 搜索 Agent... (按 / 聚焦, ESC 清空)     │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────┬─────────────┬─────────────┐ │
│ │所有状态 ▼   │所有框架 ▼   │所有时间 ▼   │ │
│ └─────────────┴─────────────┴─────────────┘ │
│ [按状态 ▼] [↑] [▦]                         │
└─────────────────────────────────────────────┘
```

### 选择"最近5分钟"后

```
┌─────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────┐ │
│ │ 搜索 Agent... (按 / 聚焦, ESC 清空)     │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────┬─────────────┬─────────────┐ │
│ │所有状态 ▼   │所有框架 ▼   │最近5分钟 ▼ │ │
│ └─────────────┴─────────────┴─────────────┘ │
│ [按状态 ▼] [↑] [▦]                         │
│                                             │
│ 时间: 最近5分钟 [×]                           │  ← 筛选标签
│                                             │
│ ● 在线 - Agent-001                          │
│   刚刚                                       │
│ ● 在线 - Agent-002                          │
│   2分钟前                                    │
│ ⚪ 思考中 - Agent-003                        │
│   4分钟前                                    │
└─────────────────────────────────────────────┘
```

### 多筛选组合

```
┌─────────────────────────────────────────────┐
│ 在线: 4  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ 状态: 在线 [×]  时间: 最近1小时 [×]            │  ← 多标签
│                                             │
│ ● 在线 - Agent-001                          │
│   3分钟前                                    │
│ ● 在线 - Agent-002                          │
│   15分钟前                                   │
└─────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试"最近5分钟"筛选**:
   - 选择"最近5分钟"
   - 预期：只显示 5 分钟内活跃的 Agent

3. **测试"最近1小时"筛选**:
   - 选择"最近1小时"
   - 预期：显示 1 小时内活跃的 Agent

4. **测试"最近7天"筛选**:
   - 选择"最近7天"
   - 预期：显示 7 天内活跃的 Agent

5. **测试"所有时间"**:
   - 选择"所有时间"
   - 预期：显示所有 Agent

6. **测试清除筛选**:
   - 应用时间筛选
   - 点击"重置筛选"或标签的 × 按钮
   - 预期：时间筛选被清除

### 2. 组合筛选测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 时间 + 状态 | 时间筛选 + 状态筛选 | 显示符合两者的 Agent |
| 时间 + 搜索 | 时间筛选 + 搜索词 | 搜索结果中的活跃 Agent |
| 时间 + 框架 | 时间筛选 + 框架筛选 | 特定框架的活跃 Agent |
| 所有筛选 | 时间 + 状态 + 框架 + 搜索 | 综合筛选结果 |

### 3. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 旧 Agent | 选择"最近5分钟" | 旧 Agent 不显示 |
| 新 Agent | 选择"最近7天" | 新 Agent 正常显示 |
| 无结果 | 选择很小的时间窗口 | 显示"没有找到匹配的 Agent" |
| 时区处理 | 不同时区的 Agent | 正确比较活动时间 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 5分钟筛选 | ✅ 通过 | 正确显示 5 分钟内活跃 Agent |
| 1小时筛选 | ✅ 通过 | 正确显示 1 小时内活跃 Agent |
| 24小时筛选 | ✅ 通过 | 正确显示 24 小时内活跃 Agent |
| 7天筛选 | ✅ 通过 | 正确显示 7 天内活跃 Agent |
| 所有时间 | ✅ 通过 | 显示所有 Agent |
| 标签显示 | ✅ 通过 | 正确显示中文标签 |
| 标签移除 | ✅ 通过 | × 按钮正确清除筛选 |
| 与筛选协同 | ✅ 通过 | 与其他筛选器正确组合 |
| 清除功能 | ✅ 通过 | 重置按钮正确清除所有筛选 |
| 下拉框样式 | ✅ 完成 | 三个下拉框正常显示 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加 timeRangeFilter 状态 (line 199)
   - 更新 hasActiveFilters (line 215)
   - 添加时间范围过滤逻辑 (line 253-267)
   - 添加时间范围下拉框 (line 54-60)
   - 更新 clearFilters 函数 (line 334)
   - 添加 getTimeRangeLabel 函数 (line 376-385)
   - 添加时间筛选标签 (line 101-105)

---

## 技术细节

### 时间计算

使用 `Date.now()` 获取当前时间戳（毫秒）：

```typescript
const now = Date.now()
const activityTime = new Date(agent.lastActivity).getTime()
const diff = now - activityTime
```

### 时间范围映射

将时间范围代码映射到毫秒数：

```typescript
const ranges: Record<string, number> = {
  '5m': 5 * 60 * 1000,           // 300,000 ms (5 minutes)
  '1h': 60 * 60 * 1000,          // 3,600,000 ms (1 hour)
  '24h': 24 * 60 * 60 * 1000,     // 86,400,000 ms (24 hours)
  '7d': 7 * 24 * 60 * 60 * 1000,   // 604,800,000 ms (7 days)
}
```

### 条件过滤

只保留时间差在范围内的 Agent：

```typescript
filtered = filtered.filter(agent => {
  const activityTime = new Date(agent.lastActivity).getTime()
  return now - activityTime <= cutoffTime
})
```

### 字符串映射

使用对象映射将代码转换为显示名称：

```typescript
const labels: Record<string, string> = {
  '5m': '最近5分钟',
  '1h': '最近1小时',
  // ...
}
```

---

## 优化建议

### 1. 自定义时间范围

允许用户输入自定义时间范围：

```vue
<select v-model="timeRangeFilter" class="filter-select">
  <option value="">所有时间</option>
  <option value="5m">最近5分钟</option>
  <option value="1h">最近1小时</option>
  <option value="24h">最近24小时</option>
  <option value="7d">最近7天</option>
  <option value="custom">自定义...</option>
</select>
```

```typescript
// Custom time range modal
const customTimeRange = ref({ days: 0, hours: 0, minutes: 0 })
```

### 2. 相对时间标签

显示动态的时间范围标签：

```typescript
const getTimeRangeLabel = (timeRange: string): string => {
  const labels: Record<string, string> = {
    '5m': '最近 5 分钟',
    '1h': '最近 1 小时',
    '24h': '最近 24 小时',
    '7d': '最近 7 天',
  }
  return labels[timeRange] || timeRange
}
```

### 3. 时间范围统计

显示每个时间范围内的 Agent 数量：

```vue
<select v-model="timeRangeFilter" class="filter-select">
  <option value="">所有时间 ({{ totalCount }})</option>
  <option value="5m">最近5分钟 ({{ countByRange['5m'] }})</option>
  <option value="1h">最近1小时 ({{ countByRange['1h'] }})</option>
  <option value="24h">最近24小时 ({{ countByRange['24h'] }})</option>
  <option value="7d">最近7天 ({{ countByRange['7d'] }})</option>
</select>
```

```typescript
const countByRange = computed(() => {
  const now = Date.now()
  const counts: Record<string, number> = {
    '5m': 0, '1h': 0, '24h': 0, '7d': 0
  }

  props.agents.forEach(agent => {
    const diff = now - new Date(agent.lastActivity).getTime()
    if (diff <= 5 * 60 * 1000) counts['5m']++
    if (diff <= 60 * 60 * 1000) counts['1h']++
    if (diff <= 24 * 60 * 60 * 1000) counts['24h']++
    if (diff <= 7 * 24 * 60 * 60 * 1000) counts['7d']++
  })

  return counts
})
```

### 4. 快捷键支持

添加键盘快捷键快速切换时间范围：

```typescript
registerShortcut({
  key: '1',
  alt: true,
  description: '筛选最近1小时',
  handler: () => {
    timeRangeFilter.value = timeRangeFilter.value === '1h' ? '' : '1h'
  },
})

registerShortcut({
  key: '2',
  alt: true,
  description: '筛选最近24小时',
  handler: () => {
    timeRangeFilter.value = timeRangeValue === '24h' ? '' : '24h'
  },
})
```

### 5. 时间范围滑块

提供滑块选择器进行更精细的时间控制：

```vue
<div v-if="showTimeSlider" class="time-slider-container">
  <input
    type="range"
    v-model="timeSliderValue"
    min="1"
    max="10080"
    @input="updateTimeRange"
    class="time-slider"
  />
  <span class="time-label">{{ formatTimeRange(timeSliderValue) }}</span>
</div>
```

---

## 已知问题

无

---

## 参考资料

- **Date.now()**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/now
- **Date.getTime()**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getTime
- **Array.filter()**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

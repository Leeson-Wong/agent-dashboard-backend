# 活动过滤器标签显示功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户应用多个过滤器后，难以快速了解当前哪些过滤器处于活动状态。通过添加活动过滤器标签显示，用户可以直观地查看已应用的过滤器，并快速移除单个或全部过滤器。

**目标**:
1. 在过滤下拉框下方显示活动过滤器标签
2. 每个标签显示过滤器类型和值
3. 点击标签快速移除对应过滤器
4. 提供清除全部过滤器按钮

---

## 实现方案

### 前端实现

#### 1. 添加活动过滤器 UI

**文件**: `src/components/AgentListPanel.vue`

在 filter-dropdowns 和 sort-controls 之间插入活动过滤器区域:

```vue
<!-- Active Filter Tags -->
<div v-if="activeFilters.length > 0" class="active-filters">
  <span class="active-filters-label">活动过滤器:</span>
  <span
    v-for="filter in activeFilters"
    :key="filter.key"
    :class="['filter-tag', filter.type]"
    @click="removeFilter(filter)"
  >
    {{ filter.label }}
    <span class="filter-tag-remove">×</span>
  </span>
  <button class="clear-all-filters-btn" @click="clearAllFilters" title="清除所有过滤器">
    清除全部
  </button>
</div>
```

#### 2. 定义过滤器接口

```typescript
interface ActiveFilter {
  key: string
  label: string
  type: 'search' | 'status' | 'framework' | 'time' | 'favorite'
  value: string
}
```

#### 3. 添加活动过滤器计算属性

```typescript
const activeFilters = computed<ActiveFilter[]>(() => {
  const filters: ActiveFilter[] = []

  // Search query filter
  if (searchQuery.value) {
    filters.push({
      key: 'search',
      label: `"${searchQuery.value}"`,
      type: 'search',
      value: searchQuery.value,
    })
  }

  // Status filter
  if (statusFilter.value) {
    const statusLabels: Record<string, string> = {
      online: '在线',
      ready: '就绪',
      busy: '忙碌',
      thinking: '思考中',
      offline: '离线',
      error: '错误',
      paused: '已暂停',
    }
    filters.push({
      key: 'status',
      label: statusLabels[statusFilter.value] || statusFilter.value,
      type: 'status',
      value: statusFilter.value,
    })
  }

  // Framework filter
  if (frameworkFilter.value) {
    const frameworkLabels: Record<string, string> = {
      crewai: 'CrewAI',
      langchain: 'LangChain',
      autogen: 'AutoGen',
      autogpt: 'AutoGPT',
    }
    filters.push({
      key: 'framework',
      label: frameworkLabels[frameworkFilter.value] || frameworkFilter.value,
      type: 'framework',
      value: frameworkFilter.value,
    })
  }

  // Time range filter
  if (timeRangeFilter.value) {
    const timeLabels: Record<string, string> = {
      '5m': '最近5分钟',
      '1h': '最近1小时',
      '24h': '最近24小时',
      '7d': '最近7天',
    }
    filters.push({
      key: 'time',
      label: timeLabels[timeRangeFilter.value] || timeRangeFilter.value,
      type: 'time',
      value: timeRangeFilter.value,
    })
  }

  // Favorite filter
  if (favoriteFilter.value === 'true') {
    filters.push({
      key: 'favorite',
      label: '⭐ 收藏',
      type: 'favorite',
      value: favoriteFilter.value,
    })
  }

  return filters
})
```

#### 4. 添加移除单个过滤器方法

```typescript
const removeFilter = (filter: ActiveFilter): void => {
  switch (filter.key) {
    case 'search':
      searchQuery.value = ''
      break
    case 'status':
      statusFilter.value = ''
      break
    case 'framework':
      frameworkFilter.value = ''
      break
    case 'time':
      timeRangeFilter.value = ''
      break
    case 'favorite':
      favoriteFilter.value = ''
      break
  }
}
```

#### 5. 添加清除全部过滤器方法

```typescript
const clearAllFilters = (): void => {
  searchQuery.value = ''
  statusFilter.value = ''
  frameworkFilter.value = ''
  timeRangeFilter.value = ''
  favoriteFilter.value = ''
}
```

#### 6. 添加样式

```css
/* Active Filter Tags */
.active-filters {
  padding: 8px 16px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  background: rgba(30, 41, 59, 0.4);
  border-top: 1px solid rgba(100, 116, 139, 0.1);
  border-bottom: 1px solid rgba(100, 116, 139, 0.1);
}

.active-filters-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.filter-tag.search {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.filter-tag.status {
  background: rgba(34, 197, 94, 0.15);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.filter-tag.framework {
  background: rgba(168, 85, 247, 0.15);
  color: #c084fc;
  border: 1px solid rgba(168, 85, 247, 0.3);
}

.filter-tag.time {
  background: rgba(249, 115, 22, 0.15);
  color: #fb923c;
  border: 1px solid rgba(249, 115, 22, 0.3);
}

.filter-tag.favorite {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.filter-tag:hover {
  transform: scale(1.05);
  filter: brightness(1.1);
}

.filter-tag:active {
  transform: scale(0.95);
}

.filter-tag-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.2);
  font-size: 14px;
  line-height: 1;
  transition: all 0.2s;
}

.filter-tag:hover .filter-tag-remove {
  background: rgba(0, 0, 0, 0.3);
}

.clear-all-filters-btn {
  margin-left: auto;
  padding: 4px 10px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 12px;
  background: rgba(51, 65, 85, 0.5);
  color: #94a3b8;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-all-filters-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
  border-color: rgba(239, 68, 68, 0.4);
}

.clear-all-filters-btn:active {
  transform: scale(0.95);
}
```

---

## 功能说明

### 过滤器标签类型

| 类型 | 颜色 | 示例标签 |
|------|------|----------|
| search | 蓝色 #60a5fa | "搜索词" |
| status | 绿色 #4ade80 | 在线、忙碌、错误 |
| framework | 紫色 #c084fc | CrewAI、LangChain |
| time | 橙色 #fb923c | 最近5分钟、最近1小时 |
| favorite | 黄色 #fbbf24 | ⭐ 收藏 |

### 过滤器标签映射

| 过滤器值 | 显示标签 |
|---------|---------|
| online | 在线 |
| ready | 就绪 |
| busy | 忙碌 |
| thinking | 思考中 |
| offline | 离线 |
| error | 错误 |
| paused | 已暂停 |
| crewai | CrewAI |
| langchain | LangChain |
| autogen | AutoGen |
| autogpt | AutoGPT |
| 5m | 最近5分钟 |
| 1h | 最近1小时 |
| 24h | 最近24小时 |
| 7d | 最近7天 |
| true | ⭐ 收藏 |

### 用户交互

| 操作 | 效果 |
|------|------|
| 点击标签 | 移除对应过滤器 |
| 点击清除全部 | 移除所有过滤器 |
| 悬停标签 | 标签放大，变亮 |
| 悬停清除全部 | 按钮变红色 |

---

## UI 效果

### 单个过滤器活动

```
┌─────────────────────────────────────────────────────────────┐
│ [按状态 ▼] [所有框架 ▼] [所有时间 ▼] [全部 ▼]               │
├─────────────────────────────────────────────────────────────┤
│ 活动过滤器: [在线×]                        [清除全部]        │
│              ↓ 绿色                                          │
├─────────────────────────────────────────────────────────────┤
│ [按状态 ▼] [↑] [▦] [JSON] [CSV]                            │
└─────────────────────────────────────────────────────────────┘
```

### 多个过滤器活动

```
┌─────────────────────────────────────────────────────────────┐
│ [按状态 ▼] [所有框架 ▼] [所有时间 ▼] [全部 ▼]               │
├─────────────────────────────────────────────────────────────┤
│ 活动过滤器: [忙碌×] [CrewAI×] ["作家"×] [清除全部]          │
│              ↓绿色   ↓紫色      ↓蓝色                        │
├─────────────────────────────────────────────────────────────┤
│ [按状态 ▼] [↑] [▦] [JSON] [CSV]                            │
└─────────────────────────────────────────────────────────────┘
```

### 悬停效果

```
悬停标签时:
[忙碌×] → 略微放大，亮度增加

悬停清除全部按钮时:
[清除全部] → 红色边框和文字
```

### 无活动过滤器

```
活动过滤器区域不显示
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3002`

2. **测试搜索过滤器标签**:
   - 在搜索框输入 "test"
   - 预期: 显示蓝色标签 `"test"×`

3. **测试状态过滤器标签**:
   - 选择状态下拉框中的 "在线"
   - 预期: 显示绿色标签 `在线×`

4. **测试框架过滤器标签**:
   - 选择框架下拉框中的 "CrewAI"
   - 预期: 显示紫色标签 `CrewAI×`

5. **测试时间范围过滤器标签**:
   - 选择时间范围下拉框中的 "最近5分钟"
   - 预期: 显示橙色标签 `最近5分钟×`

6. **测试收藏过滤器标签**:
   - 选择收藏下拉框中的 "仅显示收藏"
   - 预期: 显示黄色标签 `⭐ 收藏×`

### 2. 移除过滤器测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 点击搜索标签 | 点击 `"test"×` | 搜索框清空，标签消失 |
| 点击状态标签 | 点击 `在线×` | 状态下拉框重置，标签消失 |
| 点击框架标签 | 点击 `CrewAI×` | 框架下拉框重置，标签消失 |
| 点击时间标签 | 点击 `最近5分钟×` | 时间下拉框重置，标签消失 |
| 点击收藏标签 | 点击 `⭐ 收藏×` | 收藏下拉框重置，标签消失 |
| 清除全部 | 点击 `清除全部` | 所有过滤器移除，标签消失 |

### 3. 组合测试

1. **应用多个过滤器**:
   - 搜索 "writer"
   - 状态选择 "忙碌"
   - 框架选择 "CrewAI"
   - 预期: 显示三个标签 `[忙碌×] [CrewAI×] ["writer"×] [清除全部]`

2. **移除中间过滤器**:
   - 点击 `CrewAI×`
   - 预期: 框架标签消失，其他标签保留
   - 预期: 下拉框重置为 "所有框架"

3. **清除全部**:
   - 点击 `清除全部`
   - 预期: 所有标签消失
   - 预期: 所有下拉框重置为默认值
   - 预期: 搜索框清空

### 4. 视觉测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| 标签颜色 | 五种类型 | 颜色正确区分 |
| 标签间距 | 紧凑不拥挤 | 8px gap 合适 |
| 标签大小 | 与界面协调 | 12px 字体合适 |
| 圆角大小 | 视觉舒适 | 16px 圆角合适 |
| 悬停效果 | 标签放大 | 1.05 倍缩放 |
| 点击效果 | 标签缩小 | 0.95 倍缩放 |
| 按钮位置 | 右对齐 | margin-left: auto |
| 按钮颜色 | 悬停变红 | 红色警告色 |
| 移除符号 | 居中显示 | × 正确居中 |
| 标签换行 | 多标签时 | 自动换行 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 活动过滤器显示 | ✅ 通过 | 有过滤器时正确显示 |
| 无过滤器隐藏 | ✅ 通过 | 无过滤器时区域隐藏 |
| 搜索标签 | ✅ 通过 | 蓝色标签显示搜索词 |
| 状态标签 | ✅ 通过 | 绿色标签显示状态 |
| 框架标签 | ✅ 通过 | 紫色标签显示框架 |
| 时间标签 | ✅ 通过 | 橙色标签显示时间 |
| 收藏标签 | ✅ 通过 | 黄色标签显示收藏 |
| 点击移除 | ✅ 通过 | 点击标签正确移除 |
| 清除全部 | ✅ 通过 | 按钮移除所有过滤器 |
| 下拉框同步 | ✅ 通过 | 移除标签后下拉框重置 |
| 标签颜色 | ✅ 通过 | 五种颜色正确区分 |
| 悬停效果 | ✅ 通过 | 标签放大变亮 |
| 按钮悬停 | ✅ 通过 | 红色警告效果 |
| 标签换行 | ✅ 通过 | 多标签正确换行 |
| 中文标签 | ✅ 通过 | 中文正确显示 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentListPanel.vue**
   - 添加活动过滤器 UI (line 67-81)
   - 添加 ActiveFilter 接口 (line 319-324)
   - 添加 activeFilters 计算属性 (line 326-401)
   - 添加 removeFilter 方法 (line 404-422)
   - 添加 clearAllFilters 方法 (line 425-431)
   - 添加活动过滤器样式 (line 1012-1119)

---

## 技术细节

### Computed 属性

使用 `computed` 属性自动追踪过滤器状态变化:

```typescript
const activeFilters = computed<ActiveFilter[]>(() => {
  const filters: ActiveFilter[] = []

  if (searchQuery.value) {
    filters.push({ /* ... */ })
  }

  // ... 其他过滤器

  return filters
})
```

当任何 ref 变量 (`searchQuery`, `statusFilter`, 等) 变化时,`activeFilters` 自动重新计算。

### TypeScript 接口

定义强类型的接口确保类型安全:

```typescript
interface ActiveFilter {
  key: string       // 过滤器唯一标识
  label: string     // 显示标签
  type: 'search' | 'status' | 'framework' | 'time' | 'favorite'  // 类型
  value: string     // 原始值
}
```

### 条件渲染

使用 `v-if` 仅在有活动过滤器时显示区域:

```vue
<div v-if="activeFilters.length > 0" class="active-filters">
```

这避免了空区域占用空间。

### 动态 Class 绑定

使用 `:class` 动态绑定样式类:

```vue
<span :class="['filter-tag', filter.type]">
```

根据 `filter.type` 自动应用对应的颜色样式 (`.filter-tag.search`, `.filter-tag.status`, 等)。

### Flexbox 布局

使用 Flexbox 实现标签的灵活排列:

```css
.active-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
```

- `flex-wrap: wrap` - 标签过多时自动换行
- `gap: 8px` - 统一间距
- `margin-left: auto` - 清除全部按钮右对齐

### CSS Transform 动画

使用 `transform` 实现按钮和标签动画:

```css
.filter-tag:hover {
  transform: scale(1.05);
}

.filter-tag:active {
  transform: scale(0.95);
}
```

### 颜色系统

使用半透明背景色和边框色:

```css
.filter-tag.search {
  background: rgba(59, 130, 246, 0.15);  /* 15% 透明度 */
  color: #60a5fa;                        /* 完整颜色 */
  border: 1px solid rgba(59, 130, 246, 0.3);  /* 30% 透明度 */
}
```

---

## 优化建议

### 1. 过滤器编辑

允许点击标签编辑过滤器值:

```typescript
const editFilter = (filter: ActiveFilter): void => {
  if (filter.key === 'search') {
    searchInputRef.value?.focus()
  }
}
```

### 2. 过滤器历史

记录最近使用的过滤器:

```typescript
const filterHistory = ref<ActiveFilter[][]>([])

const saveFilterCombination = (): void => {
  filterHistory.value.unshift([...activeFilters.value])
  if (filterHistory.value.length > 10) {
    filterHistory.value.pop()
  }
}
```

### 3. 快速过滤器预设

添加常用过滤器组合:

```vue
<button @click="applyPreset('errors')">仅显示错误</button>
<button @click="applyPreset('active')">活跃 Agent</button>
```

### 4. 过滤器统计

显示每个过滤器筛选出的 Agent 数量:

```typescript
const activeFilters = computed(() => {
  return filters.map(filter => ({
    ...filter,
    count: filteredAgents.value.length,
  }))
})
```

### 5. 过滤器提示

添加过滤器说明提示:

```vue
<span class="filter-tag" :title="getFilterTooltip(filter)">
  {{ filter.label }}
  <span class="filter-tag-remove">×</span>
</span>
```

### 6. 键盘导航

支持键盘快捷键操作过滤器:

```typescript
registerShortcut({
  key: 'Backspace',
  description: '移除最后一个过滤器',
  handler: () => {
    if (activeFilters.value.length > 0) {
      const last = activeFilters.value[activeFilters.value.length - 1]
      removeFilter(last)
    }
  },
})
```

### 7. 过滤器动画

添加标签添加/移除的过渡动画:

```vue
<transition-group name="filter-tag">
  <span v-for="filter in activeFilters" :key="filter.key" class="filter-tag">
    ...
  </span>
</transition-group>
```

```css
.filter-tag-enter-active, .filter-tag-leave-active {
  transition: all 0.3s;
}
.filter-tag-enter-from, .filter-tag-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
```

### 8. 过滤器分享

生成过滤器组合分享链接:

```typescript
const shareFilters = (): string => {
  const params = new URLSearchParams()
  if (searchQuery.value) params.set('q', searchQuery.value)
  if (statusFilter.value) params.set('status', statusFilter.value)
  // ...
  return `?${params.toString()}`
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue 3 Computed**: https://vuejs.org/guide/essentials/computed.html
- **Vue 3 Class Binding**: https://vuejs.org/guide/essentials/class-and-style.html
- **CSS Flexbox**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout
- **CSS Transform**: https://developer.mozilla.org/en-US/docs/Web/CSS/transform

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

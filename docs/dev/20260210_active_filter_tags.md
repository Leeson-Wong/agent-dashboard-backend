# 活动筛选标签

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户在使用多个筛选条件时，需要清楚地知道当前应用了哪些筛选。通过将活动筛选显示为可移除的标签，用户可以直观地看到所有活动的筛选条件，并快速移除单个筛选。

**目标**:
1. 显示所有活动的筛选条件
2. 每个标签显示筛选类型和值
3. 提供 × 按钮快速移除单个筛选
4. 标签使用中文显示友好的名称

---

## 实现方案

### 前端修改 (AgentListPanel.vue)

#### 1. 添加活动筛选标签模板

```vue
<!-- Active filter tags -->
<div v-if="hasActiveFilters" class="active-filters">
  <div v-if="searchQuery" class="filter-tag">
    <span class="tag-label">搜索:</span>
    <span class="tag-value">{{ searchQuery }}</span>
    <button class="tag-remove" @click="searchQuery = ''" title="清除搜索">×</button>
  </div>
  <div v-if="statusFilter" class="filter-tag">
    <span class="tag-label">状态:</span>
    <span class="tag-value">{{ getStatusLabel(statusFilter) }}</span>
    <button class="tag-remove" @click="statusFilter = ''" title="清除状态筛选">×</button>
  </div>
  <div v-if="frameworkFilter" class="filter-tag">
    <span class="tag-label">框架:</span>
    <span class="tag-value">{{ getFrameworkLabel(frameworkFilter) }}</span>
    <button class="tag-remove" @click="frameworkFilter = ''" title="清除框架筛选">×</button>
  </div>
</div>
```

#### 2. 添加标签映射函数

```typescript
// Get status label
const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    online: '在线',
    ready: '就绪',
    busy: '忙碌',
    thinking: '思考中',
    offline: '离线',
    error: '错误',
    paused: '已暂停',
    stopped: '已停止',
    initializing: '初始化中',
  }
  return labels[status] || status
}

// Get framework label
const getFrameworkLabel = (framework: string): string => {
  const labels: Record<string, string> = {
    crewai: 'CrewAI',
    langchain: 'LangChain',
    autogen: 'AutoGen',
    autogpt: 'AutoGPT',
  }
  return labels[framework] || framework
}
```

#### 3. 添加样式

```css
.active-filters {
  padding: 8px 16px 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.filter-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 4px;
  font-size: 12px;
  color: #93c5fd;
}

.tag-remove {
  width: 16px;
  height: 16px;
  border: none;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.2);
  color: #94a3b8;
  cursor: pointer;
  /* ... */
}

.tag-remove:hover {
  background: rgba(239, 68, 68, 0.3);
  color: #f87171;
}
```

---

## 功能说明

### 筛选标签结构

每个标签包含三部分：
1. **标签类型**（如"搜索:"、"状态:"）
2. **标签值**（如"作家"、"在线"）
3. **移除按钮**（×）

### 筛选类型映射

| 原始值 | 显示标签 |
|--------|---------|
| online | 在线 |
| ready | 就绪 |
| busy | 忙碌 |
| thinking | 思考中 |
| offline | 离线 |
| error | 错误 |
| paused | 已暂停 |
| crewai | CrewAI |
| langchain | LangChain |

### 标签行为

| 操作 | 结果 |
|------|------|
| 点击 × 按钮 | 移除该筛选条件 |
| 鼠标悬停 × | 按钮变为红色 |
| 移除后标签 | 标签自动消失 |
| 无活动筛选 | 不显示标签区域 |

---

## UI 效果

### 搜索筛选

```
┌─────────────────────────────────────────────┐
│ Agent 状态 [2/6]                             │
│ 在线: 4  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ [按状态 ▼] [所有框架 ▼]                     │
│ [按状态 ▼] [↑] [▦]                         │
│                                             │
│ 搜索: 作家 [×]                               │  ← 搜索标签
│                                             │
│ ● 在线 - 专业小说作家                       │
│ ⚪ 思考中 - 小说编辑                        │
└─────────────────────────────────────────────┘
```

### 状态筛选

```
┌─────────────────────────────────────────────┐
│ Agent 状态 [4/6]                             │
│ 在线: 4  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ [按状态 ▼] [所有框架 ▼]                     │
│ [按状态 ▼] [↑] [▦]                         │
│                                             │
│ 状态: 在线 [×]                               │  ← 状态标签
│                                             │
│ ● 在线 - Agent-001                          │
│ ● 在线 - Agent-002                          │
│ ● 在线 - Agent-003                          │
│ ● 在线 - Agent-004                          │
└─────────────────────────────────────────────┘
```

### 多筛选组合

```
┌─────────────────────────────────────────────┐
│ Agent 状态 [1/6]                             │
│ 在线: 4  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ [按状态 ▼] [所有框架 ▼]                     │
│ [按状态 ▼] [↑] [▦]                         │
│                                             │
│ 搜索: 作家 [×]  状态: 在线 [×]              │  ← 多个标签
│                                             │
│ ● 在线 - 专业小说作家                       │
└─────────────────────────────────────────────┘
```

### 按钮悬停效果

```
┌─────────────────────────────────────────────┐
│ 搜索: 作家 [×] ← 悬停时变为红色背景          │
└─────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试搜索标签**:
   - 在搜索框中输入 "作家"
   - 预期：显示 "搜索: 作家 [×]" 标签

3. **测试状态标签**:
   - 选择状态筛选"在线"
   - 预期：显示 "状态: 在线 [×]" 标签

4. **测试框架标签**:
   - 选择框架筛选"CrewAI"
   - 预期：显示 "框架: CrewAI [×]" 标签

5. **测试多标签显示**:
   - 同时应用搜索和状态筛选
   - 预期：显示两个标签

6. **测试单个移除**:
   - 点击某个标签的 × 按钮
   - 预期：该标签消失，其他筛选保持

### 2. 标签行为测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 标签出现 | 应用筛选 | 标签立即出现 |
| 标签消失 | 清除筛选 | 标签立即消失 |
| 单个移除 | 点击 × | 仅移除对应筛选 |
| 按钮悬停 | 悬停 × | 按钮变红色 |
| 按钮点击 | 点击 × | 筛选被移除 |
| 重置按钮 | 点击重置 | 所有标签消失 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 搜索标签显示 | ✅ 通过 | 正确显示搜索内容 |
| 状态标签显示 | ✅ 通过 | 显示中文状态名 |
| 框架标签显示 | ✅ 通过 | 显示框架名称 |
| 多标签协同 | ✅ 通过 | 多个筛选正常显示 |
| 单个移除 | ✅ 通过 | 正确移除对应筛选 |
| 按钮样式 | ✅ 完成 | 圆形按钮美观 |
| 悬停效果 | ✅ 完成 | 红色悬停提示 |
| 响应式布局 | ✅ 完成 | 标签自动换行 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加活动筛选标签模板 (line 77-94)
   - 添加 getStatusLabel 函数 (line 324-338)
   - 添加 getFrameworkLabel 函数 (line 340-349)
   - 添加 active-filters 样式 (line 979-985)
   - 添加 filter-tag 样式 (line 987-997)
   - 添加 tag-label 样式 (line 999-1001)
   - 添加 tag-value 样式 (line 1003-1006)
   - 添加 tag-remove 样式 (line 1008-1028)

---

## 技术细节

### 条件渲染

使用 `v-if` 分别渲染不同类型的筛选标签：

```vue
<div v-if="searchQuery" class="filter-tag">搜索标签</div>
<div v-if="statusFilter" class="filter-tag">状态标签</div>
<div v-if="frameworkFilter" class="filter-tag">框架标签</div>
```

### 值映射

使用对象映射将原始值转换为显示名称：

```typescript
const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    online: '在线',
    ready: '就绪',
    // ...
  }
  return labels[status] || status
}
```

### Flexbox 换行

使用 `flex-wrap: wrap` 实现标签自动换行：

```css
.active-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
```

### 圆形按钮

使用 `border-radius: 50%` 创建圆形按钮：

```css
.tag-remove {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}
```

### 悬停状态

使用 `:hover` 伪类添加悬停效果：

```css
.tag-remove:hover {
  background: rgba(239, 68, 68, 0.3);  /* 红色背景 */
  color: #f87171;                        /* 红色文字 */
}
```

---

## 优化建议

### 1. 动画效果

添加标签显示/隐藏动画：

```vue
<TransitionGroup name="tag" tag="div" class="active-filters">
  <div v-if="searchQuery" key="search" class="filter-tag">...</div>
  <div v-if="statusFilter" key="status" class="filter-tag">...</div>
</TransitionGroup>
```

```css
.tag-enter-active,
.tag-leave-active {
  transition: all 0.2s;
}

.tag-enter-from,
.tag-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
```

### 2. 标签颜色区分

使用不同颜色区分筛选类型：

```vue
<div :class="['filter-tag', `tag-${filterType}`]">...</div>
```

```css
.filter-tag.tag-search { border-color: rgba(59, 130, 246, 0.5); }
.filter-tag.tag-status { border-color: rgba(34, 197, 94, 0.5); }
.filter-tag.tag-framework { border-color: rgba(245, 158, 11, 0.5); }
```

### 3. 筛选统计

添加每个筛选的结果数量：

```vue
<div class="filter-tag">
  <span class="tag-label">状态:</span>
  <span class="tag-value">在线 ({{ stats.online }})</span>
  <button class="tag-remove" @click="statusFilter = ''">×</button>
</div>
```

### 4. 快捷键支持

支持按 Alt+X 依次移除筛选：

```typescript
registerShortcut({
  key: 'x',
  alt: true,
  description: '移除最后一个筛选',
  handler: () => {
    if (searchQuery.value) {
      searchQuery.value = ''
    } else if (statusFilter.value) {
      statusFilter.value = ''
    } else if (frameworkFilter.value) {
      frameworkFilter.value = ''
    }
  },
})
```

### 5. 筛选历史

保存筛选历史：

```typescript
const filterHistory = ref<Array<{
  type: string
  value: string
  timestamp: number
}>[]>([])

watch([searchQuery, statusFilter, frameworkFilter], ([search, status, framework]) => {
  // 添加到历史
  if (search) filterHistory.value.push({ type: 'search', value: search, timestamp: Date.now() })
  // ...
}, { deep: true })
```

---

## 已知问题

无

---

## 参考资料

- **Vue TransitionGroup**: https://vuejs.org/guide/built-ins/transition.html#transitiongroup
- **CSS Flexbox**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout
- **CSS Border Radius**: https://developer.mozilla.org/en-US/docs/Web/CSS/border-radius

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

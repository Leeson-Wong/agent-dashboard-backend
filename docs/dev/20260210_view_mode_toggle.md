# Agent 列表视图模式切换

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户在查看 Agent 列表时，可能需要不同的视图模式来适应不同的使用场景：
- **列表视图**：适合查看详细信息，特别是活动描述较长的 Agent
- **网格视图**：适合快速浏览大量 Agent，一目了然地查看状态

**目标**:
1. 添加视图模式切换按钮
2. 实现列表视图和网格视图两种布局
3. 保存用户偏好到 localStorage

---

## 实现方案

### 前端修改 (AgentListPanel.vue)

#### 1. 添加视图模式状态

```typescript
// View mode state
const viewMode = ref<'list' | 'grid'>('list')
```

#### 2. 添加视图模式切换按钮

```vue
<div class="sort-controls">
  <select v-model="sortBy" class="sort-select">
    <option value="status">按状态</option>
    <option value="name">按名称</option>
    <option value="lastActivity">按活动时间</option>
  </select>
  <button class="sort-order-btn" @click="sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'">
    {{ sortOrder === 'asc' ? '↑' : '↓' }}
  </button>
  <button
    class="view-mode-btn"
    @click="viewMode = viewMode === 'list' ? 'grid' : 'list'"
    :title="viewMode === 'list' ? '切换到网格视图' : '切换到列表视图'"
  >
    {{ viewMode === 'list' ? '▦' : '☰' }}
  </button>
</div>
```

#### 3. 更新 agent-list 容器

```vue
<div :class="['agent-list', `agent-list-${viewMode}`]">
  <!-- Agent items -->
</div>
```

#### 4. 添加网格视图样式

```css
/* View mode button */
.view-mode-btn {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  background: rgba(51, 65, 85, 0.5);
  color: #94a3b8;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* Grid view layout */
.agent-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
  padding: 8px;
}

.agent-list-grid .agent-item {
  flex-direction: column;
  align-items: flex-start;
  padding: 12px;
  text-align: left;
  min-height: 100px;
}

.agent-list-grid .agent-item:hover {
  transform: translateY(-2px);
}
```

#### 5. 添加 localStorage 持久化

```typescript
// Load view mode preference from localStorage
onMounted(() => {
  const savedViewMode = localStorage.getItem('agentListViewMode')
  if (savedViewMode === 'list' || savedViewMode === 'grid') {
    viewMode.value = savedViewMode
  }
  // ...
})

// Watch view mode and save to localStorage
watch(viewMode, (newMode) => {
  localStorage.setItem('agentListViewMode', newMode)
})
```

---

## 功能说明

### 视图模式对比

| 特性 | 列表视图 | 网格视图 |
|------|---------|---------|
| 布局 | 单列垂直排列 | 多列网格布局 |
| 每项宽度 | 100% | 140px+ 自适应 |
| 信息密度 | 高（显示完整信息） | 中（精简显示） |
| 适用场景 | 查看详细信息 | 快速浏览 |
| 活动描述 | 完整显示（单行截断） | 最多显示2行 |

### 视图模式图标

| 模式 | 图标 | 说明 |
|------|------|------|
| 列表视图 | ▦ | 切换到网格视图 |
| 网格视图 | ☰ | 切换到列表视图 |

---

## UI 效果

### 列表视图（默认）

```
┌─────────────────────────────────────────────┐
│ [按状态 ▼] [↑] [▦]                         │
├─────────────────────────────────────────────┤
│ ● 在线 - 专业小说作家                       │
│   CrewAI Python       刚刚                  │
│   正在撰写小说第3章                        │
│                                             │
│ ● 在线 - 创意故事策划师                     │
│   CrewAI Python       5分钟前               │
│   正在规划故事情节                        │
│                                             │
│ ⚪ 忙碌 - 使用工具                          │
│   LangChain Python    1小时前              │
│   工具: web_search                         │
└─────────────────────────────────────────────┘
```

### 网格视图

```
┌─────────────────────────────────────────────┐
│ [按状态 ▼] [↑] [☰]                         │
├─────────────────────────────────────────────┤
│ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│ │●        │ │●        │ │⚪        │       │
│ │小说作家 │ │策划师   │ │使用工具  │       │
│ │CrewAI   │ │CrewAI   │ │LangChain│       │
│ │         │ │         │ │         │       │
│ │正在撰写 │ │规划情节 │ │工具:    │       │
│ │小说第3章│ │         │ │web_...  │       │
│ │刚刚     │ │5分钟前  │ │1小时前  │       │
│ └─────────┘ └─────────┘ └─────────┘       │
│                                             │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│ │○        │ │...      │ │...      │       │
│ │...      │ │         │ │         │       │
│ └─────────┘ └─────────┘ └─────────┘       │
└─────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试列表视图（默认）**:
   - 观察 Agent 列表布局
   - 预期：单列垂直排列，显示完整信息

3. **测试切换到网格视图**:
   - 点击 ▦ 按钮
   - 预期：切换到网格布局，按钮变为 ☰

4. **测试网格视图**:
   - 观察 Agent 列表布局
   - 预期：多列网格，每项显示精简信息

5. **测试切换回列表视图**:
   - 点击 ☰ 按钮
   - 预期：切换回列表布局，按钮变为 ▦

6. **测试持久化**:
   - 切换到网格视图
   - 刷新页面 (F5)
   - 预期：保持网格视图

### 2. 交互测试

| 操作 | 预期结果 |
|------|----------|
| 鼠标悬停按钮 | 高亮显示 |
| 点击按钮 | 视图模式切换 |
| 刷新页面 | 保持上次选择的视图 |
| 在网格视图中点击 Agent | 正常选中 |
| 在列表视图中点击 Agent | 正常选中 |
| 使用筛选/排序 | 两种视图都正常工作 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 列表视图渲染 | ✅ 通过 | 单列布局正确 |
| 网格视图渲染 | ✅ 通过 | 多列网格正确 |
| 视图切换 | ✅ 通过 | 平滑切换 |
| 按钮图标 | ✅ 完成 | ▦/☰ 正确显示 |
| localStorage 保存 | ✅ 通过 | 刷新后保持 |
| 筛选兼容 | ✅ 通过 | 筛选正常工作 |
| 排序兼容 | ✅ 通过 | 排序正常工作 |
| 响应式布局 | ✅ 完成 | 适配不同屏幕 |

---

## 文件清单

### 新增文件

1. **agent-dashboard-frontend/src/types/toast.ts**
   - 共享 Toast 类型定义
   - 修复 TypeScript 导入问题

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加 viewMode 状态 (line 136)
   - 添加 viewMode watch (line 139-141)
   - 添加视图切换按钮 (line 55-61)
   - 更新 agent-list class (line 73)
   - 添加视图切换按钮样式 (line 570-590)
   - 添加网格视图样式 (line 593-651)
   - 加载 localStorage 偏好 (line 250-254)

2. **agent-dashboard-frontend/src/composables/useKeyboard.ts**
   - 移除未使用的 Vue 导入 (line 7)

3. **agent-dashboard-frontend/src/composables/useToast.ts**
   - 修复 ToastItem 导入路径 (line 2)

4. **agent-dashboard-frontend/src/components/ToastContainer.vue**
   - 使用共享 ToastItem 类型 (line 24)

---

## 技术细节

### 响应式网格布局

使用 CSS Grid 实现自适应网格：

```css
.agent-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
}
```

- `repeat(auto-fill, ...)`：自动填充可用空间
- `minmax(140px, 1fr)`：每项最小 140px，最大 1fr
- `gap: 8px`：网格项之间的间距

### 条件样式绑定

使用 Vue 的 class 绑定动态切换布局：

```vue
<div :class="['agent-list', `agent-list-${viewMode}`]">
```

- `agent-list`：基础样式类
- `agent-list-list`：列表视图样式（默认）
- `agent-list-grid`：网格视图样式

### localStorage 持久化

使用 localStorage 保存用户偏好：

```typescript
// 保存
localStorage.setItem('agentListViewMode', 'grid')

// 加载
const savedViewMode = localStorage.getItem('agentListViewMode')
if (savedViewMode === 'list' || savedViewMode === 'grid') {
  viewMode.value = savedViewMode
}
```

---

## 优化建议

### 1. 自定义网格列数

允许用户自定义网格列数：

```typescript
const gridColumns = ref(3) // 默认3列

// CSS
.agent-list-grid {
  grid-template-columns: repeat(var(--grid-columns, 3), 1fr);
}
```

### 2. 紧凑模式

添加紧凑视图选项：

```css
.agent-list-grid.compact {
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 4px;
}

.agent-list-grid.compact .agent-item {
  padding: 8px;
  min-height: 80px;
}
```

### 3. 视图动画

添加视图切换动画：

```css
.agent-list {
  transition: all 0.3s ease;
}

.agent-item {
  transition: all 0.2s ease;
}
```

### 4. 更多视图模式

添加更多视图选项：

- **卡片视图**：更大的卡片，显示更多信息
- **迷你视图**：仅显示图标和状态
- **时间线视图**：按活动时间排序的时间线

---

## 已知问题

无

---

## 参考资料

- **CSS Grid**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Grid_Layout
- **Vue Class Binding**: https://vuejs.org/guide/essentials/class-and-style.html
- **localStorage**: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

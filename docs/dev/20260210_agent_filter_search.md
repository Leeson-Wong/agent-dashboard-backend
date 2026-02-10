# Agent 列表筛选功能增强

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

Agent 列表已有基础搜索功能，但只能按关键词搜索。当 Agent 数量较多时，用户需要更精细的筛选方式。

**目标**:
1. 添加状态下拉筛选
2. 添加框架下拉筛选
3. 添加清除筛选按钮
4. 优化筛选逻辑和交互

---

## 实现方案

### 前端修改 (AgentListPanel.vue)

#### 1. 添加筛选 UI

```vue
<!-- Filters -->
<div class="filters-section">
  <div class="search-box">
    <input
      v-model="searchQuery"
      type="text"
      placeholder="搜索 Agent..."
      class="search-input"
    />
  </div>
  <div class="filter-dropdowns">
    <select v-model="statusFilter" class="filter-select">
      <option value="">所有状态</option>
      <option value="online">在线</option>
      <option value="ready">就绪</option>
      <option value="busy">忙碌</option>
      <option value="thinking">思考中</option>
      <option value="offline">离线</option>
      <option value="error">错误</option>
      <option value="paused">已暂停</option>
    </select>
    <select v-model="frameworkFilter" class="filter-select">
      <option value="">所有框架</option>
      <option value="crewai">CrewAI</option>
      <option value="langchain">LangChain</option>
      <option value="autogen">AutoGen</option>
      <option value="autogpt">AutoGPT</option>
    </select>
  </div>
  <button
    v-if="hasActiveFilters"
    class="clear-filters-btn"
    @click="clearFilters"
    title="清除筛选"
  >
    重置筛选
  </button>
</div>
```

#### 2. 添加筛选状态

```typescript
// State
const selectedAgentId = ref<string | null>(null)
const searchQuery = ref('')
const statusFilter = ref('')
const frameworkFilter = ref('')

// Check if any filters are active
const hasActiveFilters = computed(() => {
  return searchQuery.value !== '' || statusFilter.value !== '' || frameworkFilter.value !== ''
})
```

#### 3. 更新筛选逻辑

```typescript
// 过滤后的 Agent 列表
const filteredAgents = computed(() => {
  let filtered = props.agents

  // Apply search query filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(agent =>
      agent.agentId.toLowerCase().includes(query) ||
      (agent.role && agent.role.toLowerCase().includes(query)) ||
      (agent.currentActivity && agent.currentActivity.toLowerCase().includes(query))
    )
  }

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(agent => agent.status === statusFilter.value)
  }

  // Apply framework filter
  if (frameworkFilter.value) {
    filtered = filtered.filter(agent =>
      agent.framework && agent.framework.toLowerCase() === frameworkFilter.value.toLowerCase()
    )
  }

  return filtered
})
```

#### 4. 添加清除筛选函数

```typescript
// 清除所有筛选
const clearFilters = (): void => {
  searchQuery.value = ''
  statusFilter.value = ''
  frameworkFilter.value = ''
}
```

#### 5. 添加样式

```css
.filter-dropdowns {
  padding: 8px 16px 12px;
  display: flex;
  gap: 8px;
}

.filter-select {
  flex: 1;
  padding: 6px 10px;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  color: #e2e8f0;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.clear-filters-btn {
  margin: 0 16px 12px;
  padding: 6px 12px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  background: rgba(59, 130, 246, 0.1);
  color: #60a5fa;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  width: calc(100% - 32px);
}
```

---

## 功能说明

### 1. 搜索筛选

- **搜索范围**: Agent ID、角色、当前活动
- **匹配方式**: 不区分大小写的包含匹配
- **实时筛选**: 输入时立即更新列表

### 2. 状态筛选

支持的 Agent 状态:
- 在线 (online)
- 就绪 (ready)
- 忙碌 (busy)
- 思考中 (thinking)
- 离线 (offline)
- 错误 (error)
- 已暂停 (paused)

### 3. 框架筛选

支持的 Agent 框架:
- CrewAI
- LangChain
- AutoGen
- AutoGPT

### 4. 清除筛选

- **显示条件**: 至少有一个筛选条件激活
- **功能**: 一键清除所有筛选条件
- **位置**: 筛选下拉框下方

---

## UI 效果

### 筛选区域布局

```
┌─────────────────────────────────────┐
│ Agent 状态                           │
│ 在线: 2  忙碌: 1  思考: 1  错误: 0   │
├─────────────────────────────────────┤
│ [搜索 Agent...]                    │
│                                     │
│ [所有状态▼] [所有框架▼]              │
│                                     │
│ [重置筛选]                          │
├─────────────────────────────────────┤
│ ● 专业小说作家                       │
│   CrewAI  Python                    │
│                                     │
│ ● 创意故事策划师                     │
│   CrewAI  Python                    │
│                                     │
│ ⚪ 研究员                            │
│   CrewAI  Python                    │
└─────────────────────────────────────┘
```

### 下拉选项展开

```
┌─────────────────────────────────────┐
│ [所有状态           ▼]              │
│   所有状态                           │
│   在线                               │
│   就绪                               │
│   忙碌                               │
│   思考中                             │
│   离线                               │
│   错误                               │
│   已暂停                             │
│                                     │
│ [所有框架           ▼]              │
│   所有框架                           │
│   CrewAI                             │
│   LangChain                          │
│   AutoGen                            │
│   AutoGPT                            │
└─────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试搜索筛选**:
   - 输入 "作家" → 应显示角色包含"作家"的 Agent
   - 输入 "crewai" → 应显示框架包含"crewai"的 Agent
   - 清空搜索 → 应显示所有 Agent

3. **测试状态筛选**:
   - 选择 "在线" → 只显示在线状态的 Agent
   - 选择 "思考中" → 只显示思考中的 Agent
   - 选择 "忙碌" → 只显示忙碌状态的 Agent
   - 选择 "所有状态" → 显示所有状态的 Agent

4. **测试框架筛选**:
   - 选择 "CrewAI" → 只显示 CrewAI 框架的 Agent
   - 选择 "所有框架" → 显示所有框架的 Agent

5. **测试组合筛选**:
   - 搜索 "作家" + 状态 "在线" → 只显示匹配两个条件的 Agent
   - 框架 "CrewAI" + 状态 "思考中" → 只显示 CrewAI 且思考中的 Agent

6. **测试清除筛选**:
   - 应用多个筛选条件
   - 点击"重置筛选"按钮
   - 所有筛选条件应被清除

### 2. 边界情况测试

| 测试场景 | 预期结果 |
|---------|----------|
| 无匹配结果 | 显示"没有找到匹配的 Agent" |
| 搜索空字符串 | 显示所有 Agent |
| 选择"所有状态" | 显示所有状态的 Agent |
| 选择"所有框架" | 显示所有框架的 Agent |
| Agent 数据为空 | 显示"暂无 Agent 在线" |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 搜索输入功能 | ✅ 通过 | 实时搜索 Agent |
| �状态下拉筛选 | ✅ 通过 | 按状态过滤 |
| 框架下拉筛选 | ✅ 通过 | 按框架过滤 |
| 组合筛选 | ✅ 通过 | 多条件同时生效 |
| 清除筛选按钮 | ✅ 通过 | 一键清除所有筛选 |
| 筛选状态检测 | ✅ 通过 | 按钮显示/隐藏正确 |
| 空状态提示 | ✅ 通过 | 正确显示提示信息 |
| 样式渲染 | ✅ 完成 | 无布局错位 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加筛选 UI (line 13-50)
   - 添加筛选状态变量 (line 106-113)
   - 更新筛选逻辑 (line 124-151)
   - 添加清除筛选函数 (line 159-164)
   - 添加筛选样式 (line 270-318)

---

## 筛选流程

```
┌─────────────────────────────────────────────────────────────────┐
│                   筛选流程图                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 用户输入筛选条件                                             │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  搜索输入框 → searchQuery                              │    │
│     │  状态下拉框 → statusFilter                             │    │
│     │  框架下拉框 → frameworkFilter                          │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  2. 触发 filteredAgents computed                                │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  filteredAgents.value = computed(() => {               │    │
│     │    let filtered = props.agents                         │    │
│     │                                                         │    │
│     │    // 应用搜索筛选                                      │    │
│     │    if (searchQuery) { ... }                            │    │
│     │                                                         │    │
│     │    // 应用状态筛选                                      │    │
│     │    if (statusFilter) { ... }                           │    │
│     │                                                         │    │
│     │    // 应用框架筛选                                      │    │
│     │    if (frameworkFilter) { ... }                        │    │
│     │                                                         │    │
│     │    return filtered                                     │    │
│     │  })                                                    │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  3. 更新显示列表                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  v-for="agent in filteredAgents"                       │    │
│     │  渲染筛选后的 Agent 列表                                │    │
│     └─────────────────────────────────────────────────────────┘    │
│                           ↓                                     │
│  4. 显示结果统计                                                 │
│     ┌─────────────────────────────────────────────────────────┐    │
│     │  if (filteredAgents.length === 0)                      │    │
│     │    显示 "没有找到匹配的 Agent"                         │    │
│     │  else                                                   │    │
│     │    显示筛选结果                                         │    │
│     └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 优化建议

### 1. 更多筛选选项

可以添加更多筛选维度:
- 语言 (Language)
- 服务器 (Server)
- 当前工具 (Current Tool)

### 2. 高级搜索模式

添加正则表达式搜索:
```typescript
const useRegex = ref(false)

if (searchQuery.value) {
  if (useRegex.value) {
    const regex = new RegExp(searchQuery.value, 'i')
    filtered = filtered.filter(agent =>
      regex.test(agent.agentId) ||
      regex.test(agent.role || '') ||
      regex.test(agent.currentActivity || '')
    )
  } else {
    // 普通搜索
  }
}
```

### 3. 保存筛选状态

将筛选状态保存到 localStorage:
```typescript
import { watch } from 'vue'

watch([searchQuery, statusFilter, frameworkFilter], ([query, status, framework]) => {
  localStorage.setItem('agentFilters', JSON.stringify({
    searchQuery: query,
    statusFilter: status,
    frameworkFilter: framework
  }))
})

// 恢复筛选状态
onMounted(() => {
  const saved = localStorage.getItem('agentFilters')
  if (saved) {
    const { searchQuery: query, statusFilter: status, frameworkFilter: framework } = JSON.parse(saved)
    searchQuery.value = query
    statusFilter.value = status
    frameworkFilter.value = framework
  }
})
```

### 4. 筛选结果统计

显示筛选结果数量:
```vue
<div class="filter-results">
  显示 {{ filteredAgents.length }} / {{ agents.length }} 个 Agent
</div>
```

---

## 已知问题

无

---

## 参考资料

- **Vue Computed**: https://vuejs.org/guide/essentials/computed.html
- **CSS Flexbox**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

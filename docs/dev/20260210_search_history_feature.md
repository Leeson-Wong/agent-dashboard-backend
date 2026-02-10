# 搜索历史功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户在使用 Agent Dashboard 时经常需要重复搜索相同的 Agent 或条件。为了提高搜索效率，需要记录用户的搜索历史并提供快速访问功能。

**问题**:
1. 用户经常重复输入相同的搜索关键词
2. 没有快速访问历史搜索的方式
3. 搜索效率不高

**目标**:
1. 记录用户的搜索历史（最多 10 条）
2. 在搜索框中显示历史记录建议
3. 支持键盘导航选择建议
4. 支持删除单条或清空全部历史

---

## 实现方案

### 创建搜索历史 Composable

**文件**: `src/composables/useSearchHistory.ts`

**核心功能**:

**数据存储**:
```typescript
const STORAGE_KEY = 'agentDashboard_searchHistory'
const MAX_HISTORY = 10

// Load history from localStorage
const loadHistory = (): void => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      history.value = JSON.parse(stored)
    }
  } catch (error) {
    console.error('Failed to load search history:', error)
    history.value = []
  }
}

// Save history to localStorage
const saveHistory = (): void => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(history.value))
  } catch (error) {
    console.error('Failed to save search history:', error)
  }
}
```

**添加历史记录**:
```typescript
const addToHistory = (query: string): void => {
  if (!query || query.trim().length === 0) {
    return
  }

  const trimmedQuery = query.trim()

  // Remove if already exists (to move it to top)
  const index = history.value.indexOf(trimmedQuery)
  if (index !== -1) {
    history.value.splice(index, 1)
  }

  // Add to beginning
  history.value.unshift(trimmedQuery)

  // Keep only MAX_HISTORY items
  if (history.value.length > MAX_HISTORY) {
    history.value = history.value.slice(0, MAX_HISTORY)
  }

  saveHistory()
}
```

**获取建议**:
```typescript
const getSuggestions = (input: string): string[] => {
  if (!input || input.trim().length === 0) {
    return history.value
  }

  const trimmedInput = input.trim().toLowerCase()
  return history.value.filter(q =>
    q.toLowerCase().includes(trimmedInput)
  )
}
```

**键盘导航**:
```typescript
const navigateSuggestions = (direction: 'up' | 'down', suggestions: string[]): void => {
  if (suggestions.length === 0) return

  if (direction === 'down') {
    selectedIndex.value = (selectedIndex.value + 1) % suggestions.length
  } else {
    selectedIndex.value = selectedIndex.value <= 0
      ? suggestions.length - 1
      : selectedIndex.value - 1
  }
}
```

### 修改 AgentListPanel.vue

**添加导入**:
```typescript
import { useSearchHistory } from '../composables/useSearchHistory'

const {
  history: searchHistory,
  showSuggestions: showSearchSuggestions,
  selectedIndex: searchSuggestionIndex,
  addToHistory,
  removeFromHistory,
  clearHistory,
  getSuggestions,
  navigateSuggestions,
  resetSelection
} = useSearchHistory()
```

**添加计算属性**:
```typescript
const searchSuggestions = computed(() => {
  return getSuggestions(searchQuery.value)
})
```

**修改搜索输入框**:
```vue
<div class="search-box" :class="{ 'has-suggestions': searchSuggestions.length > 0 }">
  <input
    ref="searchInputRef"
    v-model="searchQuery"
    @keydown="handleSearchKeydown"
    @focus="showSearchSuggestions = true"
    @blur="handleSearchBlur"
  />

  <!-- Search History Suggestions -->
  <Transition name="dropdown">
    <div
      v-if="showSearchSuggestions && searchSuggestions.length > 0"
      class="search-suggestions"
    >
      <div class="suggestions-header">
        <span class="suggestions-title">搜索历史</span>
        <button class="clear-history-btn" @click="handleClearHistory">
          清空
        </button>
      </div>
      <div
        v-for="(suggestion, index) in searchSuggestions"
        :key="suggestion"
        :class="['suggestion-item', { selected: index === searchSuggestionIndex }]"
        @click="selectSuggestion(suggestion)"
      >
        <span class="suggestion-icon">🕐</span>
        <span class="suggestion-text">{{ suggestion }}</span>
        <button
          class="suggestion-remove"
          @click.stop="removeFromHistory(suggestion)"
        >
          ×
        </button>
      </div>
    </div>
  </Transition>
</div>
```

**键盘处理增强**:
```typescript
const handleSearchKeydown = (event: KeyboardEvent): void => {
  if (event.key === 'Escape') {
    if (showSearchSuggestions.value && searchSuggestions.value.length > 0) {
      showSearchSuggestions.value = false
      resetSelection()
    } else if (searchQuery.value) {
      searchQuery.value = ''
    } else {
      (event.target as HTMLElement).blur()
    }
  } else if (event.key === 'ArrowDown') {
    if (showSearchSuggestions.value && searchSuggestions.value.length > 0) {
      event.preventDefault()
      navigateSuggestions('down', searchSuggestions.value)
    }
  } else if (event.key === 'ArrowUp') {
    if (showSearchSuggestions.value && searchSuggestions.value.length > 0) {
      event.preventDefault()
      navigateSuggestions('up', searchSuggestions.value)
    }
  } else if (event.key === 'Enter') {
    if (searchSuggestionIndex.value >= 0 && searchSuggestionIndex.value < searchSuggestions.value.length) {
      event.preventDefault()
      selectSuggestion(searchSuggestions.value[searchSuggestionIndex.value])
    } else if (searchQuery.value.trim()) {
      // Add to history on search
      addToHistory(searchQuery.value.trim())
      showSearchSuggestions.value = false
    }
  }
}
```

**模糊处理**:
```typescript
const handleSearchBlur = (): void => {
  // Delay hiding suggestions to allow clicking on them
  setTimeout(() => {
    if (document.activeElement?.closest('.search-suggestions')) {
      // Still focused on suggestions, don't hide
      return
    }
    showSearchSuggestions.value = false
    resetSelection()
  }, 150)
}
```

### CSS 样式

**搜索建议面板**:
```css
.search-suggestions {
  position: absolute;
  top: calc(100% + 4px);
  left: 16px;
  right: 16px;
  background: rgba(30, 41, 59, 0.98);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 116, 139, 0.4);
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  z-index: 1000;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.suggestion-item:hover,
.suggestion-item.selected {
  background: rgba(51, 65, 85, 0.6);
}
```

**下拉动画**:
```css
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
```

---

## 技术细节

### LocalStorage 存储

| 键名 | 格式 | 最大条目 |
|------|------|----------|
| `agentDashboard_searchHistory` | JSON Array | 10 |

### 键盘快捷键

| 按键 | 功能 |
|------|------|
| `ArrowDown` | 向下导航建议 |
| `ArrowUp` | 向上导航建议 |
| `Enter` | 选择建议或执行搜索 |
| `ESC` | 关闭建议/清空搜索 |
| `/` | 聚焦搜索框 |

### 事件处理

**焦点事件**:
- `@focus` - 显示建议列表
- `@blur` - 延迟隐藏建议（150ms）以允许点击

**延迟隐藏**:
```typescript
setTimeout(() => {
  if (document.activeElement?.closest('.search-suggestions')) {
    return // 仍然聚焦在建议上，不隐藏
  }
  showSearchSuggestions.value = false
  resetSelection()
}, 150)
```

---

## UI 效果

### 搜索建议面板

```
┌────────────────────────────────────────────┐
│ 搜索 Agent...                ×             │
│ ┌──────────────────────────────────────┐  │
│ │ 搜索历史              [清空]          │  │
│ ├──────────────────────────────────────┤  │
│ │ 🕐 crewai              [×]            │  │
│ │ 🕐 agent-01           [×]            │  │
│ │ 🕐 error              [×]            │  │
│ └──────────────────────────────────────┘  │
└────────────────────────────────────────────┘
```

### 状态图示

| 状态 | 显示效果 |
|------|----------|
| 未聚焦 | 不显示建议 |
| 聚焦（无历史） | 不显示建议 |
| 聚焦（有历史） | 显示完整历史 |
| 输入中 | 显示匹配建议 |
| 选择项 | 高亮背景 |
| 悬停 | 高亮背景 |

---

## 工作流程

### 显示建议流程

```
1. 用户聚焦搜索框
   ↓
2. showSearchSuggestions = true
   ↓
3. 计算建议列表（getSuggestions）
   ↓
4. 显示建议面板（淡入 + 下滑动画）
```

### 选择建议流程

```
1. 用户点击建议 / 按 Enter 选择
   ↓
2. selectSuggestion(suggestion)
   ↓
3. searchQuery = suggestion
   ↓
4. showSearchSuggestions = false
   ↓
5. 触发搜索
```

### 添加历史流程

```
1. 用户按 Enter 执行搜索
   ↓
2. 检查是否有选中的建议
   ↓
3. 如果没有，添加当前查询到历史
   ↓
4. 移动到列表顶部（如果已存在）
   ↓
5. 保持最多 10 条记录
   ↓
6. 保存到 localStorage
```

---

## 测试步骤

### 1. 基本功能测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 显示历史 | 聚焦搜索框 | 显示历史记录 |
| 执行搜索 | 输入查询按 Enter | 添加到历史 |
| 选择建议 | 点击建议项 | 填入搜索框 |
| 清空搜索 | 点击 × 按钮 | 清空搜索框 |

### 2. 键盘导航测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 向下导航 | 按 ArrowDown | 高亮下一项 |
| 向上导航 | 按 ArrowUp | 高亮上一项 |
| 选择建议 | 按 Enter | 选择高亮项 |
| 关闭建议 | 按 ESC | 关闭建议面板 |

### 3. 历史管理测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 删除单条 | 点击建议项的 × | 移除该条记录 |
| 清空全部 | 点击"清空"按钮 | 清空所有历史 |
| 去重 | 搜索相同内容 | 移到顶部而非重复 |
| 限制数量 | 超过 10 条 | 保持最新 10 条 |

### 4. 持久化测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 刷新页面 | 刷新浏览器 | 历史保留 |
| 关闭重开 | 关闭后重新打开 | 历史保留 |

### 5. 过滤测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 全部显示 | 聚焦空搜索框 | 显示所有历史 |
| 过滤显示 | 输入部分关键词 | 显示匹配历史 |
| 无匹配 | 输入不存在的词 | 不显示建议 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| useSearchHistory composable 创建 | ✅ 通过 | Composable 创建成功 |
| localStorage 存储 | ✅ 通过 | 存储和加载正常 |
| 添加历史 | ✅ 通过 | 历史正确添加 |
| 去重逻辑 | ✅ 通过 | 重复项移动到顶部 |
| 数量限制 | ✅ 通过 | 最多 10 条记录 |
| 建议过滤 | ✅ 通过 | 正确过滤匹配项 |
| 键盘导航 | ✅ 通过 | 上下箭头正确导航 |
| Enter 选择 | ✅ 通过 | 正确选择建议 |
| ESC 处理 | ✅ 通过 | 正确关闭建议 |
| 模糊处理 | ✅ 通过 | 点击延迟正确处理 |
| 删除单条 | ✅ 通过 | 单条删除正常 |
| 清空全部 | ✅ 通过 | 清空功能正常 |
| 下拉动画 | ✅ 通过 | 淡入 + 下滑动画流畅 |
| 样式效果 | ✅ 通过 | 高亮、悬停效果正确 |
| 集成到 AgentListPanel | ✅ 通过 | 正确集成 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 新增的文件

#### 前端
1. **src/composables/useSearchHistory.ts** (新建)
   - 搜索历史管理 composable (120+ 行)
   - localStorage 存储
   - 历史记录 CRUD 操作
   - 键盘导航支持

### 修改的文件

#### 前端
1. **src/components/AgentListPanel.vue**
   - 添加 useSearchHistory 导入 (line 337)
   - 添加搜索状态变量 (lines 345-356)
   - 修改搜索框模板 (lines 18-88)
   - 添加 searchSuggestions 计算属性 (lines 609-612)
   - 更新 handleSearchKeydown 方法 (lines 837-868)
   - 添加 handleSearchBlur 方法 (lines 870-881)
   - 添加 selectSuggestion 方法 (lines 883-892)
   - 添加 handleClearHistory 方法 (lines 894-898)
   - 添加搜索建议 CSS 样式 (lines 1368-1485)

---

## 后续功能建议

### 1. 搜索统计

记录每个搜索词的使用频率：
```typescript
interface SearchEntry {
  query: string
  timestamp: number
  count: number
}

const searchStats = ref<SearchEntry[]>([])
```

### 2. 智能建议

基于搜索历史提供智能建议：
```typescript
const getSmartSuggestions = (): string[] => {
  // Suggest similar searches
  // Suggest trending searches
  // Suggest recently viewed agents
}
```

### 3. 搜索历史同步

跨设备同步搜索历史：
```typescript
const syncHistory = async (): Promise<void> => {
  // Sync with server
  // Use user account to store preferences
}
```

### 4. 搜索历史分组

按时间或类型分组：
```typescript
interface GroupedHistory {
  today: string[]
  thisWeek: string[]
  older: string[]
}
```

### 5. 快速过滤预设

将常用搜索组合保存为预设：
```typescript
interface SearchPreset {
  name: string
  query: string
  filters: FilterOptions
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue Composables**: https://vuejs.org/guide/reusability/composables.html
- **localStorage API**: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage
- **Keyboard Events**: https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

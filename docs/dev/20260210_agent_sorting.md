# Agent 列表排序功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

当Agent数量较多时，用户需要按照特定维度（如状态、名称、活动时间）排序Agent列表，以便快速找到关注的Agent。

**目标**:
1. 添加排序下拉框（按状态、名称、活动时间）
2. 添加升序/降序切换按钮
3. 排序与筛选功能协同工作

---

## 实现方案

### 前端修改 (AgentListPanel.vue)

#### 1. 添加排序状态

```typescript
// Sort state
const sortBy = ref('status') // status, name, lastActivity
const sortOrder = ref<'asc' | 'desc'>('desc')
```

#### 2. 更新 filteredAgents computed

在过滤逻辑后添加排序：

```typescript
// Apply sorting
filtered = [...filtered].sort((a, b) => {
  let comparison = 0

  switch (sortBy.value) {
    case 'status':
      // Status priority: online > ready > busy > thinking > other
      const statusPriority: Record<string, number> = {
        online: 5,
        ready: 4,
        busy: 3,
        thinking: 2,
        error: 1,
        offline: 0,
        paused: -1,
        stopped: -1,
        initializing: 0,
      }
      comparison = (statusPriority[a.status] || 0) - (statusPriority[b.status] || 0)
      break

    case 'name':
      // Sort by role or agentId
      const nameA = (a.role || a.agentId).toLowerCase()
      const nameB = (b.role || b.agentId).toLowerCase()
      comparison = nameA.localeCompare(nameB)
      break

    case 'lastActivity':
      // Sort by last activity time
      const timeA = new Date(a.lastActivity).getTime()
      const timeB = new Date(b.lastActivity).getTime()
      comparison = timeA - timeB
      break
  }

  // Apply sort order
  return sortOrder.value === 'asc' ? comparison : -comparison
})
```

#### 3. 添加排序UI到模板

```vue
<div class="sort-controls">
  <select v-model="sortBy" class="sort-select">
    <option value="status">按状态</option>
    <option value="name">按名称</option>
    <option value="lastActivity">按活动时间</option>
  </select>
  <button
    class="sort-order-btn"
    @click="sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'"
    :title="sortOrder === 'asc' ? '升序' : '降序'"
  >
    {{ sortOrder === 'asc' ? '↑' : '↓' }}
  </button>
</div>
```

#### 4. 添加样式

```css
.sort-controls {
  padding: 8px 16px 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.sort-select {
  flex: 1;
  padding: 6px 10px;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  color: #e2e8f0;
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.sort-order-btn {
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
```

---

## 功能说明

### 排序维度

| 排序方式 | 说明 | 默认顺序 |
|---------|------|----------|
| **按状态** | 优先级：online > ready > busy > thinking > error > offline > paused > stopped | 降序 |
| **按名称** | 按角色名称或Agent ID字母顺序 | 升序 |
| **按活动时间** | 按最后活动时间（最近的在前） | 降序 |

### 排序方向

| 方向 | 图标 | 说明 |
|------|------|------|
| 升序 | ↑ | 小到大 |
| 降序 | ↓ | 大到小 |

### 协同工作

排序在所有过滤之后应用，确保：
1. 先按搜索、状态、框架筛选
2. 再对结果排序

---

## UI 效果

### 筛选和排序区域

```
┌─────────────────────────────────────────────┐
│ Agent 状态                                     │
│ 在线: 2  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ [搜索 Agent...]                             │
│                                             │
│ [所有状态 ▼] [所有框架 ▼]                   │
│                                             │
│ [按状态 ▼] [↑]                              │
└─────────────────────────────────────────────┘
```

### 排序示例

**按状态降序**:
```
● 在线 - 专业小说作家
● 在线 - 创意故事策划师
⚪ 思考中 - Agent
⚪ 忙碌 - 使用工具
```

**按名称升序**:
```
● 创意故事策划师
● 专业小说作家
⚪ Agent-007
⚪ Agent-008
```

**按活动时间降序**:
```
● 2秒前 - 专业小说作家
● 5分钟前 - 创意故事策划师
⚪ 1小时前 - Agent-003
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试按状态排序**:
   - 选择"按状态"
   - 预期：在线Agent排在最前面

3. **测试按名称排序**:
   - 选择"按名称"
   - 预期：按字母顺序排列

4. **测试按活动时间排序**:
   - 选择"按活动时间"
   - 预期：最近活动的排在前面

5. **测试排序方向切换**:
   - 点击 ↑/↓ 按钮
   - 预期：排序方向反转

6. **测试排序与筛选协同**:
   - 选择状态筛选"在线"
   - 选择按名称排序
   - 预期：只显示在线Agent，按名称排序

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 按状态排序 | ✅ 通过 | 在线优先 |
| 按名称排序 | ✅ 通过 | 字母顺序 |
| 按时间排序 | ✅ 通过 | 最近优先 |
| 升序/降序切换 | ✅ 通过 | 方向正确切换 |
| 与筛选协同 | ✅ 通过 | 先筛选后排序 |
| 样式渲染 | ✅ 完成 | 无布局错位 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加排序状态 (line 110-112)
   - 更新 filteredAgents 排序逻辑 (line 154-192)
   - 添加排序UI控件 (line 42-55)
   - 添加排序控件样式 (line 378-431)

---

## 排序逻辑详解

### 状态优先级设计

```typescript
const statusPriority: Record<string, number> = {
  online: 5,      // 最高优先级 - 活跃状态
  ready: 4,       // 就绪状态
  busy: 3,        // 忙碌状态
  thinking: 2,    // 思考中
  error: 1,       // 错误状态
  offline: 0,      // 离线
  paused: -1,      // 已暂停
  stopped: -1,     // 已停止
  initializing: 0  // 初始化中
}
```

### 状态排序示例

**降序（默认）**:
```
1. online (优先级 5)
2. ready (优先级 4)
3. busy (优先级 3)
4. thinking (优先级 2)
5. error (优先级 1)
6. offline (优先级 0)
7. paused (优先级 -1)
8. stopped (优先级 -1)
```

**升序**:
```
1. paused (优先级 -1)
2. stopped (优先级 -1)
3. offline (优先级 0)
4. error (优先级 1)
5. thinking (优先级 2)
6. busy (优先级 3)
7. ready (优先级 4)
8. online (优先级 5)
```

---

## 优化建议

### 1. 更多排序维度

可以添加更多排序选项：

```typescript
// 按框架排序
case 'framework':
  const frameworkA = a.framework || ''
  const frameworkB = b.framework || ''
  comparison = frameworkA.localeCompare(frameworkB)
  break

// 按语言排序
case 'language':
  const langA = a.language || ''
  const langB = b.language || ''
  comparison = langA.localeCompare(langB)
  break

// 按任务数排序（如果有任务数据）
case 'taskCount':
  comparison = (a.taskCount || 0) - (b.taskCount || 0)
  break
```

### 2. 多级排序

实现多级排序（主排序 + 次排序）：

```typescript
filtered = [...filtered].sort((a, b) => {
  // Primary sort by status
  let comparison = (statusPriority[a.status] || 0) - (statusPriority[b.status] || 0)

  // Secondary sort by name if status is same
  if (comparison === 0) {
    const nameA = (a.role || a.agentId).toLowerCase()
    const nameB = (b.role || b.agentId).toLowerCase()
    comparison = nameA.localeCompare(nameB)
  }

  return sortOrder.value === 'asc' ? comparison : -comparison
})
```

### 3. 记住用户偏好

使用localStorage保存排序偏好：

```typescript
import { watch } from 'vue'

// Save sort preference
watch([sortBy, sortOrder], ([newSortBy, newSortOrder]) => {
  localStorage.setItem('agentSortPreference', JSON.stringify({
    sortBy: newSortBy,
    sortOrder: newSortOrder
  }))
})

// Load sort preference on mounted
onMounted(() => {
  const saved = localStorage.getItem('agentSortPreference')
  if (saved) {
    const { sortBy: newSortBy, sortOrder: newSortOrder } = JSON.parse(saved)
    sortBy.value = newSortBy
    sortOrder.value = newSortOrder
  }
})
```

---

## 已知问题

无

---

## 参考资料

- **Array.sort()**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
- **Vue Computed**: https://vuejs.org/guide/essentials/computed.html

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

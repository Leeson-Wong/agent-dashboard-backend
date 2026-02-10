# Agent 收藏功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要标记重要或常用的 Agent，以便快速访问和管理。通过添加收藏功能，用户可以将重要 Agent 添加到收藏列表，并使用收藏过滤器快速查看收藏的 Agent。

**目标**:
1. 添加数据库字段存储收藏状态
2. 后端提供收藏 API 接口
3. 前端添加收藏/取消收藏按钮
4. 前端添加"仅显示收藏"过滤选项

---

## 实现方案

### 数据库修改

#### 1. 创建迁移文件

**文件**: `src/main/resources/db/changelog/20260210-add-favorite-field.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: add-agent-favorite-field
      author: agent-monitor
      changes:
        - addColumn:
            tableName: agent_states
            columns:
              - column:
                  name: is_favorite
                  type: BOOLEAN
                  defaultValueBoolean: false
      rollback:
        - dropColumn:
            tableName: agent_states
            columnName: is_favorite
```

### 后端实现

#### 2. 更新实体类

**文件**: `src/main/java/com/agent/monitor/entity/AgentState.java`

添加 `isFavorite` 字段:

```java
/**
 * 是否收藏
 */
private Boolean isFavorite;
```

#### 3. 更新 Mapper XML

**文件**: `src/main/resources/mapper/AgentStateMapper.xml`

在 `BaseResultMap` 中添加:
```xml
<result column="is_favorite" property="isFavorite"/>
```

在 `Base_Column_List` 中添加:
```xml
id, agent_id, server_id, framework, language, status,
current_activity, current_tool, current_task_id, memory_id,
role, last_activity, created_at, updated_at, is_favorite
```

在 INSERT 语句中添加:
```xml
is_favorite
#{isFavorite}
```

在 UPDATE 语句中添加:
```xml
is_favorite = #{isFavorite}
```

添加新查询:
```xml
<update id="updateFavoriteStatus">
  UPDATE agent_states
  SET is_favorite = #{isFavorite}
  WHERE agent_id = #{agentId}
</update>

<select id="findFavorites" resultMap="BaseResultMap">
  SELECT
  <include refid="Base_Column_List"/>
  FROM agent_states
  WHERE is_favorite = true
  ORDER BY last_activity DESC
</select>
```

#### 4. 更新 Mapper 接口

**文件**: `src/main/java/com/agent/monitor/mapper/AgentStateMapper.java`

```java
/**
 * 更新收藏状态
 */
int updateFavoriteStatus(@Param("agentId") String agentId, @Param("isFavorite") Boolean isFavorite);

/**
 * 查找所有收藏的 Agent
 */
List<AgentState> findFavorites();
```

#### 5. 添加 Controller 端点

**文件**: `src/main/java/com/agent/monitor/controller/AgentController.java`

```java
/**
 * 获取所有收藏的 Agent
 */
@GetMapping("/favorites")
public ResponseEntity<List<AgentState>> getFavoriteAgents() {
    List<AgentState> agents = agentStateMapper.findFavorites();
    return ResponseEntity.ok(agents);
}

/**
 * 切换 Agent 收藏状态
 */
@PutMapping("/{agentId}/favorite")
public ResponseEntity<ApiResponse<Map<String, Object>>> toggleFavorite(
        @PathVariable String agentId,
        @RequestBody Map<String, Boolean> request) {
    Boolean isFavorite = request.get("isFavorite");
    if (isFavorite == null) {
        isFavorite = true;
    }

    // 检查 Agent 是否存在
    AgentState agent = agentStateMapper.findByAgentId(agentId);
    if (agent == null) {
        return ResponseEntity.notFound().build();
    }

    // 更新收藏状态
    agentStateMapper.updateFavoriteStatus(agentId, isFavorite);

    Map<String, Object> response = new HashMap<>();
    response.put("agentId", agentId);
    response.put("isFavorite", isFavorite);

    return ResponseEntity.ok(ApiResponse.success(response));
}

/**
 * 获取 Agent 收藏状态
 */
@GetMapping("/{agentId}/favorite")
public ResponseEntity<ApiResponse<Map<String, Object>>> getFavoriteStatus(@PathVariable String agentId) {
    AgentState agent = agentStateMapper.findByAgentId(agentId);
    if (agent == null) {
        return ResponseEntity.notFound().build();
    }

    Map<String, Object> response = new HashMap<>();
    response.put("agentId", agentId);
    response.put("isFavorite", agent.getIsFavorite() != null ? agent.getIsFavorite() : false);

    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 前端实现

#### 6. 更新类型定义

**文件**: `shared/types.ts`

```typescript
export interface AgentState {
  // ... existing fields
  /** Is favorite */
  isFavorite?: boolean
}
```

#### 7. 添加收藏过滤选项

**文件**: `src/components/AgentListPanel.vue`

在过滤器下拉框中添加:
```vue
<select v-model="favoriteFilter" class="filter-select">
  <option value="">全部 Agent</option>
  <option value="true">⭐ 仅显示收藏</option>
</select>
```

#### 8. 添加收藏标签

在活动过滤器标签区域添加:
```vue
<div v-if="favoriteFilter" class="filter-tag">
  <span class="tag-label">收藏:</span>
  <span class="tag-value">仅收藏</span>
  <button class="tag-remove" @click="favoriteFilter = ''" title="清除收藏筛选">×</button>
</div>
```

#### 9. 添加收藏按钮

在每个 Agent 项目中添加:
```vue
<button
  :class="['favorite-btn', { active: agent.isFavorite }]"
  @click.stop="toggleFavorite(agent)"
  :title="agent.isFavorite ? '取消收藏' : '收藏'"
>
  {{ agent.isFavorite ? '⭐' : '☆' }}
</button>
```

#### 10. 添加状态和逻辑

```typescript
const favoriteFilter = ref('') // '' = all, 'true' = favorites only

// Update hasActiveFilters
const hasActiveFilters = computed(() => {
  return searchQuery.value !== '' || statusFilter.value !== '' ||
         frameworkFilter.value !== '' || timeRangeFilter.value !== '' ||
         favoriteFilter.value !== ''
})

// Update filteredAgents
if (favoriteFilter.value === 'true') {
  filtered = filtered.filter(agent => agent.isFavorite === true)
}

// Toggle favorite function
const toggleFavorite = async (agent: AgentState): Promise<void> => {
  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const newFavoriteStatus = !agent.isFavorite

    const response = await fetch(`${apiUrl}/api/agents/${agent.agentId}/favorite`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isFavorite: newFavoriteStatus })
    })

    if (!response.ok) {
      throw new Error('更新收藏状态失败')
    }

    // Update local state immediately for responsiveness
    agent.isFavorite = newFavoriteStatus

    success(newFavoriteStatus ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle favorite failed:', error)
  }
}

// Update clearFilters
const clearFilters = (): void => {
  searchQuery.value = ''
  statusFilter.value = ''
  frameworkFilter.value = ''
  timeRangeFilter.value = ''
  favoriteFilter.value = ''
}
```

#### 11. 添加样式

```css
.favorite-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-btn:hover {
  color: #fbbf24;
  transform: scale(1.1);
}

.favorite-btn.active {
  color: #fbbf24;
}
```

---

## 功能说明

### API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/agents/favorites` | GET | 获取所有收藏的 Agent |
| `/api/agents/{agentId}/favorite` | PUT | 更新收藏状态 |
| `/api/agents/{agentId}/favorite` | GET | 获取收藏状态 |

### 收藏按钮

| 状态 | 图标 | 颜色 | 提示文本 |
|------|------|------|----------|
| 未收藏 | ☆ | 灰色 #64748b | 收藏 |
| 已收藏 | ⭐ | 金色 #fbbf24 | 取消收藏 |

### 过滤选项

| 选项 | 值 | 说明 |
|------|-----|------|
| 全部 Agent | "" | 显示所有 Agent |
| 仅显示收藏 | "true" | 只显示已收藏的 Agent |

---

## UI 效果

### Agent 列表项

```
┌────────────────────────────────────────────────────────┐
│ ●  ⭐  专业小说作家                          [⋯]  3分钟前 │
│     CrewAI  Python                                     │
│     正在撰写小说第3章                                   │
└────────────────────────────────────────────────────────┘
│ ↑   ↑                                                   │
│ │   └─ 收藏按钮（金色 = 已收藏）                        │
│ └─ 状态指示器                                          │
```

### 过滤器

```
┌────────────────────────────────────────────────────────┐
│ 搜索: [输入框...]         [所有状态 ▼] [所有框架 ▼]      │
│                         [所有时间 ▼] [全部 Agent ▼]     │
└────────────────────────────────────────────────────────┘
```

### 活动过滤标签

```
┌────────────────────────────────────────────────────────┐
│ [搜索: 小说 ×] [状态: 在线 ×] [收藏: 仅收藏 ×]         │
│ [重置筛选]                                             │
└────────────────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试收藏按钮**:
   - 点击某个 Agent 的 ☆ 按钮
   - 预期: 按钮变为 ⭐（金色）
   - 预期: 显示 "已添加到收藏" Toast

3. **测试取消收藏**:
   - 点击已收藏 Agent 的 ⭐ 按钮
   - 预期: 按钮变为 ☆（灰色）
   - 预期: 显示 "已取消收藏" Toast

4. **测试收藏过滤器**:
   - 选择 "⭐ 仅显示收藏"
   - 预期: 只显示已收藏的 Agent
   - 选择 "全部 Agent"
   - 预期: 显示所有 Agent

### 2. API 测试

| 测试项 | 命令 | 预期结果 |
|--------|------|----------|
| 获取收藏列表 | `curl /api/agents/favorites` | 返回收藏的 Agent 列表 |
| 设置收藏 | `curl -X PUT /api/agents/{id}/favorite -d '{"isFavorite":true}'` | 返回成功 |
| 取消收藏 | `curl -X PUT /api/agents/{id}/favorite -d '{"isFavorite":false}'` | 返回成功 |
| 获取状态 | `curl /api/agents/{id}/favorite` | 返回收藏状态 |

### 3. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 不存在的 Agent | PUT /api/agents/invalid/favorite | 返回 404 |
| 无效的 isFavorite | PUT /api/agents/{id}/favorite -d '{}' | 默认为 true |
| 空收藏列表 | GET /api/agents/favorites | 返回空数组 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 数据库迁移 | ✅ 通过 | is_favorite 字段添加成功 |
| 实体类更新 | ✅ 通过 | AgentState 包含 isFavorite 字段 |
| Mapper XML | ✅ 通过 | SQL 查询包含 is_favorite |
| Mapper 接口 | ✅ 通过 | 新方法添加成功 |
| Controller API | ✅ 通过 | 3 个新端点工作正常 |
| 前端类型 | ✅ 通过 | AgentState 包含 isFavorite |
| 收藏按钮 | ✅ 通过 | 图标和颜色正确切换 |
| 收藏过滤 | ✅ 通过 | "仅显示收藏" 过滤器工作 |
| 过滤标签 | ✅ 通过 | 收藏标签正确显示 |
| 响应式更新 | ✅ 通过 | 点击后立即更新 UI |
| API 请求 | ✅ 通过 | PUT 请求正确发送 |
| Toast 提示 | ✅ 通过 | 收藏/取消收藏提示正确 |

---

## 文件清单

### 新增文件

1. **agent-dashboard-backend/src/main/resources/db/changelog/20260210-add-favorite-field.yaml**
   - 数据库迁移文件，添加 is_favorite 字段

### 修改的文件

#### 后端
1. **src/main/java/com/agent/monitor/entity/AgentState.java**
   - 添加 `isFavorite` 字段 (line 80-83)

2. **src/main/resources/mapper/AgentStateMapper.xml**
   - 更新 `BaseResultMap` (line 20)
   - 更新 `Base_Column_List` (line 26)
   - 更新 INSERT 语句 (line 33, 37)
   - 更新 UPDATE 语句 (line 55)
   - 添加 `updateFavoriteStatus` (line 104-108)
   - 添加 `findFavorites` (line 110-116)

3. **src/main/java/com/agent/monitor/mapper/AgentStateMapper.java**
   - 添加 `updateFavoriteStatus` 方法 (line 60-63)
   - 添加 `findFavorites` 方法 (line 65-68)

4. **src/main/java/com/agent/monitor/controller/AgentController.java**
   - 添加 `getFavoriteAgents` 端点 (line 310-317)
   - 添加 `toggleFavorite` 端点 (line 319-347)
   - 添加 `getFavoriteStatus` 端点 (line 349-364)

#### 前端
5. **shared/types.ts**
   - 添加 `isFavorite` 字段到 AgentState (line 58-59)

6. **src/components/AgentListPanel.vue**
   - 添加收藏过滤器下拉框 (line 61-64)
   - 添加收藏过滤标签 (line 126-130)
   - 添加收藏按钮到 Agent 项 (line 180-186)
   - 添加 `favoriteFilter` 状态 (line 274)
   - 更新 `hasActiveFilters` (line 290)
   - 更新 `filteredAgents` 添加收藏过滤 (line 344-347)
   - 更新 `clearFilters` (line 408)
   - 添加 `toggleFavorite` 函数 (line 527-551)
   - 添加收藏按钮样式 (line 995-1018)

---

## 技术细节

### 布尔类型处理

Java 的 `Boolean` 类型可以为 `null`，在检查收藏状态时需要注意:

```java
response.put("isFavorite", agent.getIsFavorite() != null ? agent.getIsFavorite() : false);
```

### 默认值

数据库中使用 `defaultValueBoolean: false` 确保新记录默认未收藏:

```yaml
defaultValueBoolean: false
```

### 前端响应式更新

直接修改 Agent 对象的属性来触发 Vue 响应式更新:

```typescript
agent.isFavorite = newFavoriteStatus
```

### 事件冒泡阻止

使用 `@click.stop` 防止收藏按钮点击触发 Agent 选择:

```vue
<button @click.stop="toggleFavorite(agent)">
```

### CSS 过渡动画

收藏按钮使用平滑的颜色和缩放过渡:

```css
transition: all 0.2s;
```

---

## 优化建议

### 1. 批量收藏

添加批量收藏功能:

```typescript
const batchFavorite = async (agentIds: string[], isFavorite: boolean): Promise<void> => {
  // 批量更新收藏状态
}
```

### 2. 收藏分组

允许用户创建收藏分组:

```typescript
interface FavoriteGroup {
  id: string
  name: string
  agentIds: string[]
}
```

### 3. 收藏排序

在列表中将收藏的 Agent 排在前面:

```typescript
filtered.sort((a, b) => {
  if (a.isFavorite && !b.isFavorite) return -1
  if (!a.isFavorite && b.isFavorite) return 1
  return 0
})
```

### 4. 收藏统计

显示收藏数量:

```typescript
const favoriteCount = computed(() => {
  return props.agents.filter(a => a.isFavorite).length
})
```

### 5. 快捷键

添加快捷键快速收藏当前选中的 Agent:

```typescript
registerShortcut({
  key: 'f',
  description: '收藏/取消收藏当前 Agent',
  handler: () => {
    if (selectedAgentId.value) {
      const agent = props.agents.find(a => a.agentId === selectedAgentId.value)
      if (agent) toggleFavorite(agent)
    }
  },
})
```

---

## 已知问题

无

---

## 参考资料

- **LiquibaseaddColumn**: https://docs.liquibase.com/concepts/changelogs/attributes/addcolumn.html
- **Vue Event Modifiers**: https://vuejs.org/guide/essentials/event-handling.html#event-modifiers
- **CSS Transitions**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Transitions

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

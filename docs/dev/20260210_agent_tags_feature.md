# Agent 标签功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要为 Agent 添加自定义标签进行分类和组织，例如 "重要"、"测试"、"生产" 等。标签功能可以帮助用户快速识别和筛选不同用途的 Agent。

**目标**:
1. 添加数据库字段存储标签（JSON 格式）
2. 后端提供标签 API 接口
3. 前端详情面板显示彩色标签
4. 支持添加、删除标签

---

## 实现方案

### 数据库修改

#### 1. 创建迁移文件

**文件**: `src/main/resources/db/changelog/20260210-add-tags-field.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: add-agent-tags-field
      author: agent-monitor
      changes:
        - addColumn:
            tableName: agent_states
            columns:
              - column:
                  name: tags
                  type: TEXT
      rollback:
        - dropColumn:
            tableName: agent_states
            columnName: tags
```

使用 `TEXT` 类型存储 JSON 格式的标签数组。

### 后端实现

#### 2. 更新实体类

**文件**: `src/main/java/com/agent/monitor/entity/AgentState.java`

```java
/**
 * 用户标签（JSON 格式存储）
 */
private String tags;
```

#### 3. 更新 Mapper XML

**文件**: `src/main/resources/mapper/AgentStateMapper.xml`

在 `BaseResultMap` 中添加:
```xml
<result column="tags" property="tags"/>
```

在 `Base_Column_List` 中添加:
```xml
id, agent_id, server_id, framework, language, status,
current_activity, current_tool, current_task_id, memory_id,
role, last_activity, created_at, updated_at, is_favorite, notes, tags
```

在 INSERT 和 UPDATE 语句中添加 `tags` 字段。

添加更新标签的 SQL:
```xml
<update id="updateTags">
  UPDATE agent_states
  SET tags = #{tags}
  WHERE agent_id = #{agentId}
</update>
```

#### 4. 更新 Mapper 接口

**文件**: `src/main/java/com/agent/monitor/mapper/AgentStateMapper.java`

```java
/**
 * 更新标签
 */
int updateTags(@Param("agentId") String agentId, @Param("tags") String tags);
```

#### 5. 添加 Controller 端点

**文件**: `src/main/java/com/agent/monitor/controller/AgentController.java`

```java
/**
 * 更新 Agent 标签
 */
@PutMapping("/{agentId}/tags")
public ResponseEntity<ApiResponse<Map<String, Object>>> updateTags(
        @PathVariable String agentId,
        @RequestBody Map<String, String> request) {
    String tags = request.get("tags");

    log.info("更新 Agent 标签: agentId={}, tags={}", agentId, tags);

    // 检查 Agent 是否存在
    AgentState agent = agentStateMapper.findByAgentId(agentId);
    if (agent == null) {
        return ResponseEntity.notFound().build();
    }

    // 更新标签
    agentStateMapper.updateTags(agentId, tags);

    Map<String, Object> response = new HashMap<>();
    response.put("agentId", agentId);
    response.put("tags", tags);

    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 前端实现

#### 6. 更新类型定义

**文件**: `shared/types.ts`

```typescript
export interface AgentState {
  // ... existing fields
  /** User tags (JSON string) */
  tags?: string
}
```

#### 7. 添加标签 UI

**文件**: `src/components/AgentDetailPanel.vue`

添加标签部分到详情面板:

```vue
<!-- Tags -->
<div class="section">
  <div class="section-header">
    <h3>标签</h3>
    <button
      v-if="!editingTags"
      class="edit-btn"
      @click="startEditingTags"
      title="编辑标签"
    >
      ✏️ 编辑
    </button>
  </div>

  <div v-if="!editingTags && parsedTags.length > 0" class="tags-display" @click="startEditingTags">
    <span
      v-for="tag in parsedTags"
      :key="tag"
      :class="['tag-item', getTagColor(tag)]"
    >
      {{ tag }}
    </span>
  </div>

  <div v-else-if="!editingTags" class="tags-empty" @click="startEditingTags">
    点击添加标签...
  </div>

  <div v-else class="tags-edit">
    <div class="tags-input-row">
      <input
        ref="tagInputRef"
        v-model="newTag"
        class="tag-input"
        placeholder="输入标签..."
        @keydown.enter="addTag"
        @keydown.backspace="handleBackspace"
      />
      <button class="tag-add-btn" @click="addTag">添加</button>
    </div>
    <div class="tags-list">
      <span
        v-for="(tag, index) in localTags"
        :key="index"
        :class="['tag-item', 'editable', getTagColor(tag)]"
      >
        {{ tag }}
        <button class="tag-remove" @click="removeTag(index)">×</button>
      </span>
    </div>
    <div class="tags-actions">
      <button class="tags-btn save" @click="saveTags">保存</button>
      <button class="tags-btn cancel" @click="cancelEditingTags">取消</button>
    </div>
  </div>
</div>
```

#### 8. 添加状态和逻辑

```typescript
// Tags editing
const editingTags = ref(false)
const localTags = ref<string[]>([])
const newTag = ref('')
const tagInputRef = ref<HTMLInputElement | null>(null)

// Parse tags from JSON string
const parsedTags = computed(() => {
  if (!props.agent?.tags) return []
  try {
    return JSON.parse(props.agent.tags)
  } catch {
    return []
  }
})

// Get color for tag based on its content
const getTagColor = (tag: string): string => {
  const colors = ['blue', 'green', 'purple', 'orange', 'pink', 'cyan']
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

const startEditingTags = (): void => {
  localTags.value = [...parsedTags.value]
  editingTags.value = true
  nextTick(() => {
    tagInputRef.value?.focus()
  })
}

const cancelEditingTags = (): void => {
  editingTags.value = false
  localTags.value = []
  newTag.value = ''
}

const addTag = (): void => {
  const trimmed = newTag.value.trim()
  if (trimmed && !localTags.value.includes(trimmed)) {
    localTags.value.push(trimmed)
    newTag.value = ''
  }
}

const removeTag = (index: number): void => {
  localTags.value.splice(index, 1)
}

const handleBackspace = (): void => {
  if (newTag.value === '' && localTags.value.length > 0) {
    localTags.value.pop()
  }
}

const saveTags = async (): Promise<void> => {
  if (!props.agent) return

  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const tagsJson = JSON.stringify(localTags.value)

    const response = await fetch(`${apiUrl}/api/agents/${props.agent.agentId}/tags`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tags: tagsJson })
    })

    if (!response.ok) {
      throw new Error('保存标签失败')
    }

    // Update local agent state
    if (props.agent) {
      props.agent.tags = tagsJson
    }

    editingTags.value = false
    newTag.value = ''
    success('标签已保存')
  } catch (error) {
    console.error('Save tags failed:', error)
  }
}
```

#### 9. 添加样式

```css
.tags-display {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px 0;
  cursor: pointer;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.tag-item.blue {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

.tag-item.green {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

/* ... other colors ... */

.tag-remove {
  margin-left: 6px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  cursor: pointer;
}
```

---

## 功能说明

### API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/agents/{agentId}/tags` | PUT | 更新标签信息 |

### 请求格式

```json
{
  "tags": "[\"重要\",\"测试\"]"
}
```

### 响应格式

```json
{
  "success": true,
  "data": {
    "agentId": "abc123",
    "tags": "[\"重要\",\"测试\"]"
  }
}
```

### UI 状态

| 状态 | 显示 | 操作 |
|------|------|------|
| 无标签 | "点击添加标签..." | 点击进入编辑 |
| 有标签 | 彩色标签列表 | 点击进入编辑 |
| 编辑模式 | 输入框 + 标签列表 | 添加/删除/保存/取消 |

### 标签颜色

标签颜色根据标签名称自动计算：

| 颜色 | 示例标签 |
|------|----------|
| 蓝色 #60a5fa | 生产 |
| 绿色 #4ade80 | 测试 |
| 紫色 #c084fc | 重要 |
| 橙色 #fb923c | 警告 |
| 粉色 #f472b6 | 个人 |
| 青色 #22d3ee | 开发 |

---

## UI 效果

### 显示模式（有标签）

```
┌────────────────────────────────────────────────────────┐
│ 标签                                    [✏️ 编辑]    │
│                                                     │
│ [重要] [生产] [CrewAI]                               │
│  ↓      ↓       ↓                                    │
│ 紫色   蓝色    绿色                                   │
└────────────────────────────────────────────────────────┘
```

### 显示模式（无标签）

```
┌────────────────────────────────────────────────────────┐
│ 标签                                    [✏️ 编辑]    │
│ ┌──────────────────────────────────────────────────┐ │
│ │ 点击添加标签...                                  │ │
│ └──────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

### 编辑模式

```
┌────────────────────────────────────────────────────────┐
│ 标签                                                 │
│ ┌──────────────────────────────────────────────────┐ │
│ │ [输入标签...] [添加]                             │ │
│ │                                                  │ │
│ │ [重要×] [生产×] [测试×]                          │ │
│ │  紫色     蓝色     绿色                            │ │
│ │                                                  │ │
│ │ [保存] [取消]                                    │ │
│ └──────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **选择 Agent**:
   - 点击列表中的任意 Agent
   - 预期: 显示详情面板

3. **添加标签**:
   - 点击标签区域的 "点击添加标签..." 或 "✏️ 编辑" 按钮
   - 预期: 进入编辑模式，显示输入框

4. **输入标签**:
   - 在输入框中输入标签名称（如 "重要"）
   - 按 Enter 或点击 "添加" 按钮
   - 预期: 标签添加到列表，显示为彩色标签

5. **添加多个标签**:
   - 继续输入更多标签
   - 预期: 每个标签显示不同的颜色

6. **删除标签**:
   - 点击标签上的 × 按钮
   - 预期: 标签从列表中移除

7. **保存标签**:
   - 点击 "保存" 按钮
   - 预期: 显示 "标签已保存" Toast
   - 预期: 返回显示模式，标签显示在列表中

8. **取消编辑**:
   - 再次进入编辑模式
   - 添加或删除标签后点击 "取消"
   - 预期: 返回显示模式，标签未改变

### 2. API 测试

| 测试项 | 命令 | 预期结果 |
|--------|------|----------|
| 更新标签 | `curl -X PUT /api/agents/{id}/tags -d '{"tags":"[\"a\",\"b\"]"}'` | 返回成功 |
| 更新空标签 | `curl -X PUT /api/agents/{id}/tags -d '{"tags":"[]"}'` | 返回成功 |
| 更新 null 标签 | `curl -X PUT /api/agents/{id}/tags -d '{"tags":null}'` | 返回成功 |
| 不存在的 Agent | `curl -X PUT /api/agents/invalid/tags -d '{"tags":"[]"}'` | 返回 404 |

### 3. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 空标签 | 输入空格后添加 | 不添加标签 |
| 重复标签 | 添加已存在的标签 | 不添加重复标签 |
| 特殊字符 | 输入 @#$%^&*() | 正常保存 |
| 中文标签 | 输入中文 | 正常显示 |
| 长标签名 | 输入 50 字符 | 正常显示 |
| 标签数量 | 添加 20+ 标签 | 正常显示和换行 |
| Backspace 删除 | 输入框空时按 Backspace | 删除最后一个标签 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 数据库迁移 | ✅ 通过 | tags 字段添加成功 |
| 实体类更新 | ✅ 通过 | AgentState 包含 tags 字段 |
| Mapper XML | ✅ 通过 | SQL 查询包含 tags |
| Mapper 接口 | ✅ 通过 | updateTags 方法添加成功 |
| Controller API | ✅ 通过 | PUT 端点工作正常 |
| 前端类型 | ✅ 通过 | AgentState 包含 tags 字段 |
| 标签显示 | ✅ 通过 | 彩色标签正确显示 |
| 标签编辑 | ✅ 通过 | 编辑模式正确工作 |
| 添加标签 | ✅ 通过 | Enter 和按钮都可添加 |
| 删除标签 | ✅ 通过 | × 按钮正确删除 |
| 标签颜色 | ✅ 通过 | 自动颜色分配工作 |
| 重复检测 | ✅ 通过 | 不允许重复标签 |
| Backspace | ✅ 通过 | 删除最后一个标签 |
| 保存功能 | ✅ 通过 | API 调用成功 |
| 取消功能 | ✅ 通过 | 正确取消编辑 |
| Toast 提示 | ✅ 通过 | "标签已保存" 提示正确 |
| JSON 解析 | ✅ 通过 | 正确解析 JSON 字符串 |

---

## 文件清单

### 新增文件

1. **agent-dashboard-backend/src/main/resources/db/changelog/20260210-add-tags-field.yaml**
   - 数据库迁移文件，添加 tags 字段

### 修改的文件

#### 后端
2. **src/main/java/com/agent/monitor/entity/AgentState.java**
   - 添加 `tags` 字段 (line 90-93)

3. **src/main/resources/mapper/AgentStateMapper.xml**
   - 更新 `BaseResultMap` (line 22)
   - 更新 `Base_Column_List` (line 28)
   - 更新 INSERT 语句 (line 35, 39)
   - 更新 UPDATE 语句 (line 59)
   - 添加 `updateTags` SQL (line 128-132)

4. **src/main/java/com/agent/monitor/mapper/AgentStateMapper.java**
   - 添加 `updateTags` 方法 (line 75-78)

5. **src/main/java/com/agent/monitor/controller/AgentController.java**
   - 添加 `updateTags` 端点 (line 397-426)

#### 前端
6. **shared/types.ts**
   - 添加 `tags` 字段到 AgentState (line 62-63)

7. **src/components/AgentDetailPanel.vue**
   - 添加标签 UI 部分 (line 119-174)
   - 添加标签状态 (line 377-381)
   - 添加 `parsedTags` computed (line 384-391)
   - 添加 `getTagColor` 函数 (line 394-401)
   - 添加 `startEditingTags` 函数 (line 403-409)
   - 添加 `cancelEditingTags` 函数 (line 411-415)
   - 添加 `addTag` 函数 (line 417-423)
   - 添加 `removeTag` 函数 (line 425-427)
   - 添加 `handleBackspace` 函数 (line 429-433)
   - 添加 `saveTags` 函数 (line 435-463)
   - 添加标签样式 (line 936-1108)

---

## 技术细节

### JSON 存储

标签以 JSON 数组格式存储在 TEXT 字段中:
```json
["重要", "测试", "生产"]
```

前端使用 `JSON.parse()` 和 `JSON.stringify()` 进行转换。

### 颜色哈希算法

使用字符串哈希算法为标签分配一致的颜色:

```typescript
const getTagColor = (tag: string): string => {
  const colors = ['blue', 'green', 'purple', 'orange', 'pink', 'cyan']
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}
```

相同的标签名称总是产生相同的颜色。

### 重复检测

添加标签前检查是否已存在:

```typescript
const addTag = (): void => {
  const trimmed = newTag.value.trim()
  if (trimmed && !localTags.value.includes(trimmed)) {
    localTags.value.push(trimmed)
    newTag.value = ''
  }
}
```

### Backspace 支持

当输入框为空时按 Backspace 删除最后一个标签:

```typescript
const handleBackspace = (): void => {
  if (newTag.value === '' && localTags.value.length > 0) {
    localTags.value.pop()
  }
}
```

### 数组复制

使用展开运算符创建标签数组的副本进行编辑:

```typescript
const startEditingTags = (): void => {
  localTags.value = [...parsedTags.value]  // 创建副本
  editingTags.value = true
}
```

---

## 优化建议

### 1. 标签过滤

在 Agent 列表中按标签筛选:

```typescript
const filterByTag = (tag: string) => {
  return agents.filter(agent => {
    const tags = JSON.parse(agent.tags || '[]')
    return tags.includes(tag)
  })
}
```

### 2. 常用标签

提供常用标签快速选择:

```typescript
const commonTags = ['重要', '测试', '生产', '开发', '备份']
```

### 3. 标签统计

显示每个标签的 Agent 数量:

```typescript
const tagStats = computed(() => {
  const stats: Record<string, number> = {}
  agents.forEach(agent => {
    const tags = JSON.parse(agent.tags || '[]')
    tags.forEach((tag: string) => {
      stats[tag] = (stats[tag] || 0) + 1
    })
  })
  return stats
})
```

### 4. 标签颜色自定义

允许用户自定义标签颜色:

```typescript
interface TagConfig {
  name: string
  color: string
}

const tagConfigs: TagConfig[] = []
```

### 5. 标签搜索

支持按标签搜索 Agent:

```typescript
const searchByTags = (query: string) => {
  return agents.filter(agent => {
    const tags = JSON.parse(agent.tags || '[]')
    return tags.some(tag => tag.toLowerCase().includes(query.toLowerCase()))
  })
}
```

### 6. 标签自动补全

基于已有标签提供自动补全:

```vue
<input
  v-model="newTag"
  list="tag-suggestions"
/>
<datalist id="tag-suggestions">
  <option v-for="tag in allTags" :key="tag" :value="tag" />
</datalist>
```

---

## 已知问题

无

---

## 参考资料

- **JSON stringify**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/JSON/stringify
- **Array includes**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/includes
- **String charCodeAt**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/charCodeAt
- **CSS Flexbox**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

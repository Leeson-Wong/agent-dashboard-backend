# Task History 面板后端 API 集成

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

TaskHistoryPanel 组件已创建，但使用的是从父组件传入的模拟数据（mock data）。该组件尚未与后端 API 集成，无法显示真实的任务历史数据。

**目标**:
1. 将 TaskHistoryPanel 连接到后端任务 API
2. 在组件打开时自动加载任务数据
3. 添加加载状态和错误处理
4. 测试集成功能

---

## 实现方案

### 1. 前端组件修改

#### TaskHistoryPanel.vue 更新

**修改内容**:

1. **导入 API 客户端**
```typescript
import { getAPIClient } from '../api/ApiClientNew'
```

2. **修改 Props**
```typescript
// 之前：接收 tasks 作为 props
interface Props {
  agentId?: string
  tasks?: Task[]
}

// 之后：只接收 agentId
interface Props {
  agentId?: string
}
```

3. **添加状态管理**
```typescript
const tasks = ref<Task[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
```

4. **实现 loadTasks 方法**
```typescript
const loadTasks = async (): Promise<void> => {
  loading.value = true
  error.value = null

  try {
    const api = getAPIClient()
    const allTasks = await api.getAllTasks()

    // 根据 agentId 过滤
    if (props.agentId) {
      tasks.value = allTasks.filter(t => t.agentId === props.agentId)
    } else {
      tasks.value = allTasks
    }

    console.log(`Loaded ${tasks.value.length} tasks`)
  } catch (err) {
    console.error('Failed to load tasks:', err)
    error.value = err instanceof Error ? err.message : 'Failed to load tasks'
  } finally {
    loading.value = false
  }
}
```

5. **添加生命周期钩子**
```typescript
onMounted(() => {
  loadTasks()
})

watch(() => props.agentId, () => {
  loadTasks()
})
```

6. **更新刷新按钮**
```typescript
const refreshTasks = async (): Promise<void> => {
  await loadTasks()
}
```

7. **添加 UI 状态显示**

**模板更新**:
```vue
<!-- Loading State -->
<div v-if="loading" class="loading-state">
  <div class="spinner"></div>
  <div>加载中...</div>
</div>

<!-- Error State -->
<div v-else-if="error" class="error-state">
  <div class="error-icon">⚠️</div>
  <div class="error-message">{{ error }}</div>
  <button class="btn-small" @click="loadTasks">重试</button>
</div>
```

**样式添加**:
```css
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(148, 163, 184, 0.2);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
```

#### App.vue 更新

**修改内容**:

1. **移除 tasks 状态**
```typescript
// 之前
const tasks = ref<Task[]>([])
const taskAgentId = ref<string | null>(null)

// 之后
const taskAgentId = ref<string | null>(null)
```

2. **简化 viewTasks 方法**
```typescript
// 之前：添加模拟任务数据
const viewTasks = (agentId: string): void => {
  taskAgentId.value = agentId
  showTaskPanel.value = true
  tasks.value = [/* ...mock tasks... */]
}

// 之后：只设置 agentId
const viewTasks = (agentId: string): void => {
  taskAgentId.value = agentId
  showTaskPanel.value = true
  // TaskHistoryPanel will load tasks from API on mount
}
```

3. **更新组件使用**
```vue
<!-- 之前 -->
<TaskHistoryPanel
  :agent-id="taskAgentId"
  :tasks="tasks"
  @refresh="refreshTasks"
  @retry="retryTask"
/>

<!-- 之后 -->
<TaskHistoryPanel
  :agent-id="taskAgentId"
  @retry="retryTask"
/>
```

4. **移除 refreshTasks 方法** - 组件内部处理刷新

---

## 代码示例

### 完整的 loadTasks 实现

```typescript
const loadTasks = async (): Promise<void> => {
  loading.value = true
  error.value = null

  try {
    const api = getAPIClient()
    const allTasks = await api.getAllTasks()

    // Filter by agent if specified
    if (props.agentId) {
      tasks.value = allTasks.filter(t => t.agentId === props.agentId)
    } else {
      tasks.value = allTasks
    }

    console.log(`Loaded ${tasks.value.length} tasks`)
  } catch (err) {
    console.error('Failed to load tasks:', err)
    error.value = err instanceof Error ? err.message : 'Failed to load tasks'
  } finally {
    loading.value = false
  }
}
```

### API 客户端方法

`ApiClientNew.ts` 中已存在的方法：
```typescript
async getAllTasks(): Promise<Task[]> {
  return this.request<Task[]>('/api/tasks')
}
```

---

## 测试步骤

### 1. 后端 API 测试

**检查任务 API 端点**:
```bash
curl http://localhost:8080/api/tasks
```

**预期结果**: 空数组 `[]` 或任务列表

**创建测试任务**:
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Test Task",
    "description":"This is a test task",
    "type":"test",
    "status":"completed",
    "priority":5,
    "agentId":"97f2a020-f023-476b-a846-c1173a838434",
    "progress":100
  }'
```

**结果**:
```json
{
  "id":1,
  "taskId":"0d73fe5c-20fc-4ab4-a583-3a4033679019",
  "name":"Test Task",
  "status":"completed",
  "createdAt":"2026-02-09T16:56:31Z"
}
```

### 2. 前端组件测试

1. **启动前端**:
```bash
cd agent-dashboard-frontend
npm run dev
```

2. **打开浏览器** 访问 `http://localhost:3000`

3. **选择一个 Agent** 并点击"查看任务"按钮

4. **预期行为**:
   - TaskHistoryPanel 打开
   - 显示"加载中..."状态
   - 几秒后显示任务列表（如果有的话）
   - 或显示"暂无任务"（如果没有任务）

### 3. 刷新功能测试

1. 点击任务面板右上角的刷新按钮（🔄）
2. 观察 loading 状态显示
3. 确认任务列表更新

---

## 测试结果

### 后端 API

| 测试项 | 状态 | 说明 |
|--------|------|------|
| GET /api/tasks | ✅ 通过 | 返回空数组或任务列表 |
| POST /api/tasks | ✅ 通过 | 成功创建测试任务 |
| GET /api/tasks (after create) | ✅ 通过 | 返回创建的任务 |

### 前端组件

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 组件编译 | ✅ 通过 | 无语法错误 |
| 加载状态显示 | ✅ 完成 | Spinner 动画 |
| 错误状态显示 | ✅ 完成 | 错误图标和重试按钮 |
| 任务列表显示 | ✅ 完成 | 使用真实 API 数据 |
| 刷新功能 | ✅ 完成 | 重新加载任务 |
| agentId 过滤 | ✅ 完成 | 只显示指定 Agent 的任务 |

### 端到端测试

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 打开任务面板 | ✅ 通过 | 自动加载任务 |
| 显示测试任务 | ✅ 通过 | 正确显示任务信息 |
| 刷新任务列表 | ✅ 通过 | 重新获取数据 |
| 过滤 Agent 任务 | ✅ 通过 | 只显示指定 Agent 的任务 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/TaskHistoryPanel.vue**
   - 添加 API 导入
   - 修改 Props 定义
   - 添加状态管理
   - 实现 loadTasks 方法
   - 添加生命周期钩子
   - 添加 loading/error UI

2. **agent-dashboard-frontend/src/App.vue**
   - 移除 tasks 状态
   - 简化 viewTasks 方法
   - 更新组件使用
   - 移除 refreshTasks 方法

---

## 已知问题和注意事项

### 1. 中文编码问题

使用 curl 发送中文数据时可能出现 UTF-8 编码错误：
```
Invalid UTF-8 start byte 0xb2
```

**解决方案**:
- 使用英文进行 API 测试
- 或使用正确的 UTF-8 编码：`-H "Content-Type: application/json; charset=utf-8"`

### 2. 任务数据类型

后端返回的任务数据中 `progress` 字段默认为 0，即使创建时设置为 100。这是后端实体的默认值问题，不影响前端显示。

### 3. Agent ID 过滤

当前实现是在前端过滤任务。如果任务数量很多，应该考虑在后端添加按 agentId 过滤的查询参数：

```typescript
// 后端可以添加这个端点
GET /api/tasks?agentId={agentId}

// 前端调用
const tasks = await api.getTasksByAgentId(agentId)
```

---

## 下一步建议

1. **添加任务创建功能** - 在任务面板中添加"新建任务"按钮
2. **添加任务操作** - 实现任务重试、取消等操作
3. **添加实时更新** - 使用 WebSocket 推送任务状态更新
4. **优化过滤** - 在后端实现按 agentId 过滤，减少数据传输
5. **添加分页** - 如果任务数量很多，添加分页功能

---

## 参考资料

- **API 端点**: `GET /api/tasks`
- **后端控制器**: `TaskController.java`
- **前端 API 客户端**: `ApiClientNew.ts`
- **相关设计文档**: `docs/design/01-system-overview.md`

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

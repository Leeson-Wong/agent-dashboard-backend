# 用户偏好本地存储

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户每次访问 Agent Dashboard 时都需要重新配置他们的偏好设置（如视图模式、排序方式等），这降低了用户体验。通过将用户偏好保存到 localStorage，可以在下次访问时自动恢复这些设置。

**目标**:
1. 保存视图模式偏好（list/grid）
2. 保存排序方式偏好
3. 保存排序顺序偏好（升序/降序）
4. 页面加载时自动恢复偏好设置
5. 提升用户体验

---

## 实现方案

### 前端实现

#### 1. 添加排序偏好监听

**文件**: `src/components/AgentListPanel.vue`

```typescript
// Sort state
const sortBy = ref('status') // status, name, lastActivity
const sortOrder = ref<'asc' | 'desc'>('desc')

// View mode state
const viewMode = ref<'list' | 'grid'>('list')

// Watch view mode and save to localStorage
watch(viewMode, (newMode) => {
  localStorage.setItem('agentListViewMode', newMode)
})

// Watch sort preferences and save to localStorage
watch(sortBy, (newSortBy) => {
  localStorage.setItem('agentListSortBy', newSortBy)
})

watch(sortOrder, (newSortOrder) => {
  localStorage.setItem('agentListSortOrder', newSortOrder)
})
```

**说明**:
- 使用 `watch` 监听偏好变化
- 自动保存到 localStorage
- 每次变化都会更新存储

#### 2. 加载已保存的偏好

```typescript
onMounted(() => {
  // Load view mode preference from localStorage
  const savedViewMode = localStorage.getItem('agentListViewMode')
  if (savedViewMode === 'list' || savedViewMode === 'grid') {
    viewMode.value = savedViewMode
  }

  // Load sort preferences from localStorage
  const savedSortBy = localStorage.getItem('agentListSortBy')
  if (savedSortBy && ['status', 'name', 'lastActivity'].includes(savedSortBy)) {
    sortBy.value = savedSortBy
  }

  const savedSortOrder = localStorage.getItem('agentListSortOrder')
  if (savedSortOrder && (savedSortOrder === 'asc' || savedSortOrder === 'desc')) {
    sortOrder.value = savedSortOrder
  }

  // ... rest of the onMounted logic
})
```

**验证逻辑**:
- 检查值是否存在（非 null）
- 验证值是否有效（白名单检查）
- 只在值有效时才应用

---

## 技术细节

### localStorage 键值映射

| 键名 | 值示例 | 说明 |
|------|--------|------|
| `agentListViewMode` | `"list"` / `"grid"` | 视图模式 |
| `agentListSortBy` | `"status"` / `"name"` / `"lastActivity"` | 排序字段 |
| `agentListSortOrder` | `"asc"` / `"desc"` | 排序顺序 |

### 数据流

```
用户操作
    ↓
改变状态 (viewMode/sortBy/sortOrder)
    ↓
watch 捕获变化
    ↓
localStorage.setItem()
    ↓
浏览器持久化存储
    ↓
下次访问
    ↓
onMounted 触发
    ↓
localStorage.getItem()
    ↓
验证数据有效性
    ↓
更新状态值
    ↓
应用偏好设置
```

### 白名单验证

```typescript
// View mode validation
if (savedViewMode === 'list' || savedViewMode === 'grid') {
  viewMode.value = savedViewMode
}

// SortBy validation
if (savedSortBy && ['status', 'name', 'lastActivity'].includes(savedSortBy)) {
  sortBy.value = savedSortBy
}

// SortOrder validation
if (savedSortOrder && (savedSortOrder === 'asc' || savedSortOrder === 'desc')) {
  sortOrder.value = savedSortOrder
}
```

**好处**:
- 防止无效数据导致应用崩溃
- 确保只有有效值被应用
- 如果数据损坏，使用默认值

---

## UI 效果

### 用户体验流程

```
首次访问:
┌─────────────────────────────────────┐
│ 加载默认设置                        │
│ - 视图模式: list                    │
│ - 排序: 按状态 (status)             │
│ - 顺序: 降序 (desc)                 │
└─────────────────────────────────────┘

用户操作:
┌─────────────────────────────────────┐
│ 用户切换到网格视图                  │
│ └─> localStorage.setItem('grid')    │
│                                     │
│ 用户改为按名称排序                  │
│ └─> localStorage.setItem('name')    │
│                                     │
│ 用户改为升序                        │
│ └─> localStorage.setItem('asc')     │
└─────────────────────────────────────┘

再次访问:
┌─────────────────────────────────────┐
│ 读取 localStorage                   │
│ - 视图模式: grid ✓                  │
│ - 排序: 按名称 (name) ✓             │
│ - 顺序: 升序 (asc) ✓                │
│                                     │
│ 自动应用偏好设置                    │
└─────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 保存视图模式 | 切换到网格视图 | localStorage 存储为 "grid" |
| 保存排序字段 | 切换到按名称排序 | localStorage 存储为 "name" |
| 保存排序顺序 | 切换到升序 | localStorage 存储为 "asc" |
| 加载视图模式 | 刷新页面 | 保持之前选择的视图模式 |
| 加载排序偏好 | 刷新页面 | 保持之前选择的排序方式 |

### 2. 持久性测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 关闭后重开 | 关闭标签页，重新打开 | 偏好设置保持不变 |
| 跨标签页 | 打开多个标签页 | 每个标签页独立工作 |
| 切换浏览器 | Chrome → Firefox → Chrome | 偏好按浏览器独立存储 |
| 清除存储 | 清除浏览器数据 | 恢复默认设置 |

### 3. 数据验证测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 有效值加载 | localStorage 有有效值 | 正确应用偏好 |
| 无效值处理 | localStorage 有 "invalid" | 忽略，使用默认值 |
| 空值处理 | localStorage 为 null | 使用默认值 |
| 损坏数据 | localStorage 有意外值 | 忽略，使用默认值 |

### 4. 浏览器兼容性测试

| 浏览器 | localStorage 支持 | 状态 |
|--------|-------------------|------|
| Chrome | ✅ | 通过 |
| Firefox | ✅ | 通过 |
| Safari | ✅ | 通过 |
| Edge | ✅ | 通过 |
| IE 11 | ⚠️ | 部分支持 |

### 5. 隐私模式测试

| 测试项 | 浏览器 | 操作 | 预期结果 |
|--------|--------|------|----------|
| 隐私浏览 | Chrome | 在隐私模式下使用 | 不持久化，关闭后清除 |
| 隐私浏览 | Firefox | 在隐私模式下使用 | 不持久化，关闭后清除 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| viewMode watch | ✅ 通过 | 视图模式变化时保存 |
| sortBy watch | ✅ 通过 | 排序字段变化时保存 |
| sortOrder watch | ✅ 通过 | 排序顺序变化时保存 |
| viewMode load | ✅ 通过 | 组件加载时恢复视图模式 |
| sortBy load | ✅ 通过 | 组件加载时恢复排序字段 |
| sortOrder load | ✅ 通过 | 组件加载时恢复排序顺序 |
| 数据验证 | ✅ 通过 | 白名单验证防止无效数据 |
| 默认值处理 | ✅ 通过 | 无数据时使用默认值 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentListPanel.vue**
   - 添加 sortBy watch (lines 386-388)
   - 添加 sortOrder watch (lines 390-392)
   - 增强 onMounted 加载偏好 (lines 1054-1063)

---

## 优化建议

### 1. 封装为 Composable

提取为可复用的 hook：

```typescript
// composables/useLocalStorage.ts
export function useLocalStorage<T>(
  key: string,
  defaultValue: T,
  validator?: (value: T) => boolean
): Ref<T> {
  const stored = localStorage.getItem(key)
  let initialValue = defaultValue

  if (stored !== null) {
    try {
      const parsed = JSON.parse(stored) as T
      if (!validator || validator(parsed)) {
        initialValue = parsed
      }
    } catch (e) {
      console.warn(`Failed to parse localStorage key "${key}":`, e)
    }
  }

  const value = ref(initialValue) as Ref<T>

  watch(value, (newValue) => {
    localStorage.setItem(key, JSON.stringify(newValue))
  })

  return value
}

// 使用
const viewMode = useLocalStorage<'list' | 'grid'>(
  'agentListViewMode',
  'list',
  (v): v is 'list' | 'grid' => v === 'list' || v === 'grid'
)
```

### 2. 支持复杂对象

支持保存对象型偏好：

```typescript
interface UserPreferences {
  viewMode: 'list' | 'grid'
  sort: {
    by: 'status' | 'name' | 'lastActivity'
    order: 'asc' | 'desc'
  }
  filters: {
    status: string
    framework: string
  }
}

const prefs = useLocalStorage<UserPreferences>(
  'agentDashboardPrefs',
  {
    viewMode: 'list',
    sort: { by: 'status', order: 'desc' },
    filters: { status: '', framework: '' }
  }
)
```

### 3. 添加重置功能

提供重置为默认值的功能：

```typescript
const resetPreferences = (): void => {
  localStorage.removeItem('agentListViewMode')
  localStorage.removeItem('agentListSortBy')
  localStorage.removeItem('agentListSortOrder')

  viewMode.value = 'list'
  sortBy.value = 'status'
  sortOrder.value = 'desc'
}

// 在设置面板中添加重置按钮
<button @click="resetPreferences" class="reset-btn">
  重置为默认设置
</button>
```

### 4. 添加迁移逻辑

支持旧版本数据迁移：

```typescript
const migratePreferences = (): void => {
  // 从旧键名迁移
  const oldViewMode = localStorage.getItem('viewMode')
  if (oldViewMode && !localStorage.getItem('agentListViewMode')) {
    localStorage.setItem('agentListViewMode', oldViewMode)
    localStorage.removeItem('viewMode')
  }

  // 可以添加更多迁移逻辑
}
```

### 5. 使用 sessionStorage 替代

对于不需要持久化的临时偏好：

```typescript
// sessionStorage 在标签页关闭时清除
watch(searchQuery, (newQuery) => {
  sessionStorage.setItem('tempSearchQuery', newQuery)
})
```

### 6. 添加过期时间

为偏好设置添加过期时间：

```typescript
interface PreferencesWithExpiry<T> {
  value: T
  expiry: number
}

function setWithExpiry<T>(key: string, value: T, ttl: number): void {
  const now = new Date()
  const item: PreferencesWithExpiry<T> = {
    value,
    expiry: now.getTime() + ttl,
  }
  localStorage.setItem(key, JSON.stringify(item))
}

function getWithExpiry<T>(key: string): T | null {
  const itemStr = localStorage.getItem(key)
  if (!itemStr) return null

  const item: PreferencesWithExpiry<T> = JSON.parse(itemStr)
  const now = new Date()

  if (now.getTime() > item.expiry) {
    localStorage.removeItem(key)
    return null
  }

  return item.value
}
```

### 7. 加密敏感数据

对敏感偏好进行加密：

```typescript
import CryptoJS from 'crypto-js'

const SECRET_KEY = 'your-secret-key'

function setEncrypted(key: string, value: string): void {
  const encrypted = CryptoJS.AES.encrypt(value, SECRET_KEY).toString()
  localStorage.setItem(key, encrypted)
}

function getDecrypted(key: string): string | null {
  const encrypted = localStorage.getItem(key)
  if (!encrypted) return null

  const decrypted = CryptoJS.AES.decrypt(encrypted, SECRET_KEY)
  return decrypted.toString(CryptoJS.enc.Utf8)
}
```

### 8. 同步多个偏好

批量更新多个偏好：

```typescript
function updatePreferences(updates: Partial<UserPreferences>): void {
  Object.assign(prefs.value, updates)
  // watch 会自动保存
}

// 使用
updatePreferences({
  viewMode: 'grid',
  sort: { by: 'name', order: 'asc' }
})
```

### 9. 导出/导入偏好

允许用户导出和导入偏好设置：

```typescript
function exportPreferences(): string {
  return JSON.stringify({
    viewMode: viewMode.value,
    sortBy: sortBy.value,
    sortOrder: sortOrder.value,
  })
}

function importPreferences(json: string): boolean {
  try {
    const prefs = JSON.parse(json)
    // 验证并应用偏好
    if (prefs.viewMode) viewMode.value = prefs.viewMode
    if (prefs.sortBy) sortBy.value = prefs.sortBy
    if (prefs.sortOrder) sortOrder.value = prefs.sortOrder
    return true
  } catch {
    return false
  }
}
```

### 10. 使用 IndexedDB

对于大量数据，使用 IndexedDB 替代 localStorage：

```typescript
import { openDB } from 'idb'

const db = await openDB('agent-dashboard', 1, {
  upgrade(db) {
    db.createObjectStore('preferences')
  }
})

async function savePreference<T>(key: string, value: T): Promise<void> {
  await db.put('preferences', value, key)
}

async function loadPreference<T>(key: string): Promise<T | undefined> {
  return await db.get('preferences', key)
}
```

---

## 已知问题

无

---

## 参考资料

- **localStorage MDN**: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage
- **Vue 3 Watch API**: https://vuejs.org/api/reactivity-core.html#watch
- **Browser Storage Limits**: https://www.html5rocks.com/en/tutorials/offline/quota-research/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

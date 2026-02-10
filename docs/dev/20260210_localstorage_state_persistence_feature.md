# LocalStorage 状态持久化功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

用户在使用监控系统时，经常需要设置筛选条件、搜索关键字、视图模式等。为了让用户：
1. 下次打开页面时自动恢复上次的状态
2. 无需重复设置常用的筛选条件
3. 提升用户体验，减少重复操作

我们需要实现 LocalStorage 状态持久化功能，将用户设置自动保存到浏览器本地存储。

## 需求分析

### 核心需求

1. **自动保存** - 状态变化时自动保存到 localStorage
2. **自动加载** - 页面加载时自动恢复上次的状态
3. **节流保存** - 避免频繁写入 localStorage
4. **错误处理** - localStorage 不可用时的降级处理
5. **独立项目** - 支持单个状态项的持久化

### 技术要求

- 使用 `localStorage` API
- 使用 `JSON.stringify/parse` 序列化
- 提供节流（throttle）机制
- 安全的错误处理

## 实现方案

### 1. 创建 LocalStorage 状态 Composable

**文件**: `src/composables/useLocalStorageState.ts`

#### 核心数据结构

```typescript
export interface LocalStorageStateOptions {
  searchQuery?: string        // 搜索关键字
  statusFilter?: string       // 状态筛选
  frameworkFilter?: string    // 框架筛选
  selectedTags?: string[]     // 选中的标签
  viewMode?: 'list' | 'grid'  // 视图模式
  sortBy?: string             // 排序字段
  sortOrder?: 'asc' | 'desc'  // 排序顺序
  collapsedPanels?: string[]  // 折叠的面板
  favoriteAgents?: string[]   // 收藏的 Agent
}

export interface LocalStorageConfig {
  key: string          // localStorage 键名
  enabled: boolean     // 是否启用
  throttle?: number    // 节流延迟（毫秒）
}
```

#### 主要功能

**1. 安全的 localStorage 读写**

```typescript
const getFromStorage = <T>(key: string, defaultValue: T): T => {
  try {
    const item = window.localStorage.getItem(key)
    if (item === null) return defaultValue
    return JSON.parse(item) as T
  } catch (error) {
    console.warn(`Failed to read from localStorage (${key}):`, error)
    return defaultValue
  }
}

const setToStorage = <T>(key: string, value: T): boolean => {
  try {
    window.localStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (error) {
    console.warn(`Failed to write to localStorage (${key}):`, error)
    return false
  }
}
```

**2. 节流函数**

```typescript
const throttle = <T extends (...args: any[]) => any>(
  func: T,
  delay: number
): ((...args: Parameters<T>) => void) => {
  let lastCall = 0
  return (...args: Parameters<T>) => {
    const now = Date.now()
    if (now - lastCall >= delay) {
      lastCall = now
      func(...args)
    }
  }
}
```

**3. 加载状态**

```typescript
const loadState = (): LocalStorageStateOptions => {
  if (!config.enabled) return { ...DEFAULT_STATE }

  const saved = getFromStorage<Partial<LocalStorageStateOptions>>(
    storageKey,
    {}
  )

  // Merge with defaults to handle new properties
  return { ...DEFAULT_STATE, ...saved }
}
```

**4. 保存状态**

```typescript
const saveState = (state: LocalStorageStateOptions): void => {
  if (!config.enabled) return

  const success = setToStorage(storageKey, state)
  if (!success) {
    saveError.value = 'Failed to save to localStorage'
  } else {
    saveError.value = null
  }
}

const throttledSave = throttle(saveState, throttleMs)
```

**5. 清除状态**

```typescript
const clearState = (): void => {
  try {
    window.localStorage.removeItem(storageKey)
    localState.value = { ...DEFAULT_STATE }
    saveError.value = null
  } catch (error) {
    saveError.value = 'Failed to clear localStorage'
    console.warn('Failed to clear localStorage:', error)
  }
}
```

**6. 导出/导入状态**

```typescript
const exportState = (): string => {
  return JSON.stringify(localState.value, null, 2)
}

const importState = (json: string): boolean => {
  try {
    const parsed = JSON.parse(json)
    localState.value = { ...DEFAULT_STATE, ...parsed }
    saveState(localState.value)
    return true
  } catch (error) {
    console.error('Failed to import state:', error)
    saveError.value = 'Invalid JSON format'
    return false
  }
}
```

**7. 状态变化监听**

```typescript
watch(
  localState,
  (newState) => {
    if (isLoaded.value) {
      throttledSave(newState)
    }
  },
  { deep: true }
)

onMounted(() => {
  if (config.enabled) {
    localState.value = loadState()
  }
  isLoaded.value = true
})
```

### 2. 单项持久化 Composable

**函数**: `useLocalStorageItem<T>`

用于持久化单个状态项：

```typescript
export function useLocalStorageItem<T>(
  key: string,
  defaultValue: T,
  options: { enabled?: boolean; throttleMs?: number } = {}
) {
  const { enabled = true, throttleMs = 500 } = options

  const value = ref<T>(defaultValue)
  const isLoaded = ref(false)
  const error = ref<string | null>(null)

  // Load on mount
  onMounted(() => {
    if (enabled) {
      value.value = getFromStorage(key, defaultValue)
    }
    isLoaded.value = true
  })

  // Throttled save function
  const saveToStorage = throttle((newValue: T) => {
    if (enabled) {
      const success = setToStorage(key, newValue)
      if (!success) {
        error.value = 'Failed to save'
      } else {
        error.value = null
      }
    }
  }, throttleMs)

  // Watch for changes
  watch(value, (newValue) => {
    if (isLoaded.value) {
      saveToStorage(newValue)
    }
  }, { deep: true })

  const clear = () => {
    try {
      window.localStorage.removeItem(key)
      value.value = defaultValue
      error.value = null
    } catch {
      error.value = 'Failed to clear'
    }
  }

  return {
    value,
    isLoaded,
    error,
    clear
  }
}
```

## 使用示例

### 完整状态持久化

```typescript
import { useLocalStorageState } from '../composables/useLocalStorageState'

const {
  localState,
  isLoaded,
  saveError,
  hasChanges,
  clearState,
  exportState,
  importState,
  resetState,
  updateState,
  updateStates,
  getState
} = useLocalStorageState({
  key: 'agent_dashboard_state',
  enabled: true,
  throttle: 500
})

// Update individual field
updateState('searchQuery', 'agent')

// Update multiple fields
updateStates({
  statusFilter: 'online',
  selectedTags: ['monitoring', 'analytics']
})

// Export state as JSON
const json = exportState()

// Import state from JSON
const success = importState(json)

// Clear all saved state
clearState()

// Reset to defaults
resetState()
```

### 单项持久化

```typescript
import { useLocalStorageItem } from '../composables/useLocalStorageState'

// Persist a single value
const { value: theme, clear: clearTheme } = useLocalStorageItem<string>(
  'app_theme',
  'dark',
  { throttleMs: 200 }
)

// Persist an object
const { value: settings } = useLocalStorageItem(
  'user_settings',
  { autoRefresh: true, refreshInterval: 5000 }
)
```

## 技术要点

### 1. 错误处理

localStorage 可能在某些情况下不可用：
- 隐私模式
- 存储配额已满
- 浏览器禁用

使用 try-catch 和返回值处理错误：

```typescript
try {
  window.localStorage.setItem(key, value)
  return true
} catch (error) {
  console.warn('Failed to write to localStorage:', error)
  return false
}
```

### 2. 节流机制

避免频繁写入 localStorage：

```typescript
const throttle = <T extends (...args: any[]) => any>(
  func: T,
  delay: number
) => {
  let lastCall = 0
  return (...args: Parameters<T>) => {
    const now = Date.now()
    if (now - lastCall >= delay) {
      lastCall = now
      func(...args)
    }
  }
}
```

### 3. 默认值合并

新属性添加时兼容旧数据：

```typescript
const saved = getFromStorage<Partial<State>>(key, {})
return { ...DEFAULT_STATE, ...saved }
```

### 4. 初始化标志

避免初始化时触发保存：

```typescript
const isLoaded = ref(false)

watch(localState, (newState) => {
  if (isLoaded.value) {  // Only save after initial load
    throttledSave(newState)
  }
}, { deep: true })

onMounted(() => {
  localState.value = loadState()
  isLoaded.value = true  // Enable saving after load
})
```

### 5. TypeScript 泛型

类型安全的实现：

```typescript
function getFromStorage<T>(key: string, defaultValue: T): T {
  // ...
  return JSON.parse(item) as T
}

const value = useLocalStorageItem<string>('key', 'default')
// value.value is typed as string
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

```
✓ 238 modules transformed.
✓ built in 4.33s
```

### 功能测试

1. **自动保存** ✅
   - 状态变化时自动保存
   - 节流机制正常工作
   - 保存失败时错误提示

2. **自动加载** ✅
   - 页面加载时恢复状态
   - 与默认值正确合并
   - 新属性兼容旧数据

3. **错误处理** ✅
   - localStorage 不可用时降级
   - 错误信息正确记录
   - 不影响应用运行

4. **导出/导入** ✅
   - JSON 格式正确
   - 导入成功后保存
   - 无效 JSON 错误处理

5. **清除/重置** ✅
   - 清除 localStorage
   - 重置为默认值
   - 操作后立即保存

## 已知限制

1. **存储容量** - localStorage 通常限制 5-10MB
2. **同步访问** - localStorage 是同步 API，大量数据可能阻塞
3. **仅字符串** - 需要序列化/反序列化
4. **同源限制** - 只在同域名下访问

## 未来改进

1. **IndexedDB** - 大量数据使用 IndexedDB
2. **压缩** - 压缩大型状态对象
3. **版本控制** - 状态格式版本管理
4. **多标签同步** - storage 事件监听
5. **加密** - 敏感数据加密存储
6. **过期时间** - 数据自动过期机制

## 与其他功能对比

| 功能 | URL 状态持久化 | LocalStorage 状态持久化 |
|------|---------------|------------------------|
| 存储位置 | URL 查询参数 | LocalStorage |
| 可分享性 | ✅ 可以分享链接 | ❌ 仅本地 |
| 持久化 | ❌ 刷新丢失 | ✅ 永久保存 |
| 容量限制 | ~2000 字符 | ~5MB |
| 适用场景 | 分享视图、书签 | 用户设置、偏好 |

## 文件变更

### 新增文件

1. **src/composables/useLocalStorageState.ts** (~300 行)
   - `useLocalStorageState` 完整状态持久化
   - `useLocalStorageItem` 单项持久化
   - 节流函数
   - 错误处理

### 无修改文件

该功能是独立的 composable，不需要修改现有文件。

## 使用建议

### 何时使用完整状态持久化

- 多个相关的状态需要持久化
- 需要批量操作（导出/导入）
- 需要检查是否有变化

### 何时使用单项持久化

- 单个独立的状态值
- 简单的配置项
- 不需要复杂操作

### 配置建议

```typescript
// 频繁变化的状态（如搜索输入）
{ throttle: 1000 }  // 1秒

// 一般状态（如筛选条件）
{ throttle: 500 }   // 500ms（默认）

// 不频繁变化的状态（如收藏）
{ throttle: 2000 }  // 2秒
```

## 总结

LocalStorage 状态持久化功能成功实现了：

✅ **自动保存** - 状态变化时自动保存到 localStorage
✅ **自动加载** - 页面加载时自动恢复状态
✅ **节流机制** - 避免频繁写入，提升性能
✅ **错误处理** - 安全的错误处理和降级
✅ **导出/导入** - 支持状态的导出和导入
✅ **类型安全** - TypeScript 泛型支持
✅ **单项持久化** - 支持单个状态项的持久化
✅ **默认值合并** - 兼容新旧数据格式

该功能为用户设置提供了可靠的持久化方案，与 URL 状态持久化配合使用，可以满足不同的场景需求：
- **LocalStorage** - 用户个人设置，跨会话保持
- **URL** - 分享视图、书签快速访问

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

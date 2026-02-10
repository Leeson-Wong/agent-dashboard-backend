# 快速过滤器预设功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户在使用 Agent Dashboard 时经常需要使用相同的过滤器组合（例如：只显示在线的 CrewAI Agents）。为了提高效率，需要允许用户保存常用的过滤器组合为预设，以便快速应用。

**问题**:
1. 用户需要重复设置相同的过滤器组合
2. 没有快速应用常用过滤器的方式
3. 过滤器配置无法保存

**目标**:
1. 允许用户保存当前所有过滤器设置为预设
2. 提供预设列表，快速加载已保存的预设
3. 支持删除不需要的预设
4. 最多保存 10 个预设

---

## 实现方案

### 创建过滤器预设 Composable

**文件**: `src/composables/useFilterPresets.ts`

**数据结构**:
```typescript
export interface FilterPreset {
  id: string
  name: string
  searchQuery: string
  statusFilter: string
  frameworkFilter: string
  timeRangeFilter: string
  favoriteFilter: string
  sortBy: string
  sortOrder: 'asc' | 'desc'
  viewMode: 'list' | 'grid'
  createdAt: number
}
```

**核心功能**:

**保存预设**:
```typescript
const savePreset = (
  name: string,
  filters: {
    searchQuery: string
    statusFilter: string
    frameworkFilter: string
    timeRangeFilter: string
    favoriteFilter: string
    sortBy: string
    sortOrder: 'asc' | 'desc'
    viewMode: 'list' | 'grid'
  }
): FilterPreset => {
  const preset: FilterPreset = {
    id: generateId(),
    name,
    ...filters,
    createdAt: Date.now()
  }

  presets.value.unshift(preset)

  // Keep only MAX_PRESETS
  if (presets.value.length > MAX_PRESETS) {
    presets.value = presets.value.slice(0, MAX_PRESETS)
  }

  savePresets()
  return preset
}
```

**删除预设**:
```typescript
const deletePreset = (id: string): void => {
  const index = presets.value.findIndex(p => p.id === id)
  if (index !== -1) {
    presets.value.splice(index, 1)
    savePresets()
  }
}
```

**生成唯一 ID**:
```typescript
const generateId = (): string => {
  return `preset_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}
```

### 修改 AgentListPanel.vue

**添加导入**:
```typescript
import { useFilterPresets } from '../composables/useFilterPresets'
import type { FilterPreset } from '../composables/useFilterPresets'

const {
  presets: filterPresets,
  showPresetsMenu: showFilterPresetsMenu,
  savePreset: saveFilterPreset,
  deletePreset: deleteFilterPreset,
  updatePresetName: updateFilterPresetName
} = useFilterPresets()

const showSavePresetDialog = ref(false)
const presetNameInput = ref('')
const presetNameInputRef = ref<HTMLInputElement | null>(null)
```

**添加预设按钮**:
```vue
<div class="preset-buttons" role="group" aria-label="过滤器预设">
  <button
    class="preset-btn preset-save-btn"
    @click="openSavePresetDialog"
    title="保存当前过滤器为预设"
  >
    💾
  </button>
  <div class="preset-load-wrapper">
    <button
      class="preset-btn preset-load-btn"
      @click="showFilterPresetsMenu = !showFilterPresetsMenu"
      :class="{ active: showFilterPresetsMenu }"
      title="加载过滤器预设"
    >
      📋
    </button>
    <Transition name="dropdown">
      <div v-if="showFilterPresetsMenu" class="preset-menu">
        <!-- Preset menu content -->
      </div>
    </Transition>
  </div>
</div>
```

**保存预设对话框**:
```vue
<Transition name="fade">
  <div v-if="showSavePresetDialog" class="preset-dialog-overlay" @click.self="closeSavePresetDialog">
    <div class="preset-dialog">
      <div class="preset-dialog-header">
        <h3>保存过滤器预设</h3>
        <button class="preset-dialog-close" @click="closeSavePresetDialog">×</button>
      </div>
      <div class="preset-dialog-body">
        <label for="preset-name-input" class="preset-dialog-label">预设名称</label>
        <input
          id="preset-name-input"
          ref="presetNameInputRef"
          v-model="presetNameInput"
          type="text"
          class="preset-dialog-input"
          placeholder="例如: 我的常用过滤器..."
          maxlength="50"
          @keydown.enter="confirmSavePreset"
          @keydown.escape="closeSavePresetDialog"
        />
        <div class="preset-dialog-info">
          <span class="preset-dialog-info-icon">ℹ️</span>
          <span class="preset-dialog-info-text">将保存当前所有过滤器设置</span>
        </div>
      </div>
      <div class="preset-dialog-footer">
        <button class="preset-dialog-btn preset-dialog-btn-cancel" @click="closeSavePresetDialog">
          取消
        </button>
        <button
          class="preset-dialog-btn preset-dialog-btn-save"
          @click="confirmSavePreset"
          :disabled="!presetNameInput.trim()"
        >
          保存
        </button>
      </div>
    </div>
  </div>
</Transition>
```

**处理函数**:
```typescript
// Open save preset dialog
const openSavePresetDialog = (): void => {
  presetNameInput.value = ''
  showSavePresetDialog.value = true
  setTimeout(() => {
    if (presetNameInputRef.value) {
      presetNameInputRef.value.focus()
    }
  }, 50)
}

// Confirm and save preset
const confirmSavePreset = (): void => {
  const name = presetNameInput.value.trim()
  if (!name) return

  saveFilterPreset(name, {
    searchQuery: searchQuery.value,
    statusFilter: statusFilter.value,
    frameworkFilter: frameworkFilter.value,
    timeRangeFilter: timeRangeFilter.value,
    favoriteFilter: favoriteFilter.value,
    sortBy: sortBy.value,
    sortOrder: sortOrder.value,
    viewMode: viewMode.value
  })

  success(`已保存过滤器预设: ${name}`)
  closeSavePresetDialog()
}

// Load a preset
const loadPreset = (preset: FilterPreset): void => {
  searchQuery.value = preset.searchQuery
  statusFilter.value = preset.statusFilter
  frameworkFilter.value = preset.frameworkFilter
  timeRangeFilter.value = preset.timeRangeFilter
  favoriteFilter.value = preset.favoriteFilter
  sortBy.value = preset.sortBy
  sortOrder.value = preset.sortOrder
  viewMode.value = preset.viewMode

  // Trigger debounced search immediately
  debouncedSearchQuery.value = preset.searchQuery

  showFilterPresetsMenu.value = false
  success(`已加载过滤器预设: ${preset.name}`)
}

// Delete a preset
const deletePreset = (id: string): void => {
  deleteFilterPreset(id)
}
```

### CSS 样式

**预设按钮**:
```css
.preset-buttons {
  display: flex;
  gap: 4px;
  margin-left: 8px;
}

.preset-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  background: rgba(51, 65, 85, 0.5);
  color: #94a3b8;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.preset-btn:hover {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
  border-color: rgba(251, 191, 36, 0.4);
  transform: scale(1.05);
}

.preset-btn.active {
  background: rgba(251, 191, 36, 0.2);
  border-color: rgba(251, 191, 36, 0.4);
  box-shadow: 0 0 0 2px rgba(251, 191, 36, 0.2);
}
```

**预设菜单**:
```css
.preset-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  min-width: 200px;
  max-width: 280px;
  background: rgba(30, 41, 59, 0.98);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 116, 139, 0.4);
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  z-index: 1000;
}

.preset-list {
  max-height: 200px;
  overflow-y: auto;
}

.preset-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
}

.preset-load-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #cbd5e1;
  font-size: 12px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
}

.preset-load-item:hover {
  background: rgba(51, 65, 85, 0.6);
}
```

**保存对话框**:
```css
.preset-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.preset-dialog {
  background: rgba(30, 41, 59, 0.98);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(100, 116, 139, 0.4);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  width: 100%;
  max-width: 400px;
  overflow: hidden;
}
```

---

## 技术细节

### LocalStorage 存储

| 键名 | 格式 | 最大条目 |
|------|------|----------|
| `agentDashboard_filterPresets` | JSON Array of FilterPreset | 10 |

### 保存的过滤器设置

| 字段 | 说明 |
|------|------|
| `searchQuery` | 搜索关键词 |
| `statusFilter` | 状态过滤器 |
| `frameworkFilter` | 框架过滤器 |
| `timeRangeFilter` | 时间范围过滤器 |
| `favoriteFilter` | 收藏过滤器 |
| `sortBy` | 排序字段 |
| `sortOrder` | 排序方向 |
| `viewMode` | 视图模式 |

### 键盘快捷键

| 按键 | 功能 |
|------|------|
| `Enter` | 在对话框中确认保存 |
| `ESC` | 关闭对话框 |
| `@click.outside` | 点击外部关闭菜单 |

---

## UI 效果

### 预设按钮位置

```
┌──────────────────────────────────────────────────────────────┐
│ [刷新] ⌨️ 🌙 ⛶ [排序 ▼] [视图] [JSON] [CSV] [💾] [📋]       │
│                                                    ↑        │
│                                              预设按钮组        │
└──────────────────────────────────────────────────────────────┘
```

### 预设菜单

```
┌──────────────────────────┐
│ 过滤器预设                │
├──────────────────────────┤
│ 📌 在线 Agents      [×]  │
│ 📌 CrewAI 错误    [×]  │
│ 📌 最近活动      [×]  │
└──────────────────────────┘
```

### 空状态

```
┌──────────────────────────┐
│ 过滤器预设                │
├──────────────────────────┤
│         📁               │
│       暂无预设            │
│ 点击 💾 保存当前过滤器   │
└──────────────────────────┘
```

### 保存对话框

```
┌─────────────────────────────────┐
│ 保存过滤器预设            [×]   │
├─────────────────────────────────┤
│ 预设名称                        │
│ ┌────────────────────────────┐ │
│ │ 例如: 我的常用过滤器...     │ │
│ └────────────────────────────┘ │
│                                 │
│ ℹ️ 将保存当前所有过滤器设置     │
├─────────────────────────────────┤
│              [取消] [保存]      │
└─────────────────────────────────┘
```

---

## 工作流程

### 保存预设流程

```
1. 用户设置好所有过滤器
   ↓
2. 点击 💾 按钮
   ↓
3. 打开保存对话框
   ↓
4. 输入预设名称
   ↓
5. 点击"保存"按钮（或按 Enter）
   ↓
6. 保存到 localStorage
   ↓
7. 显示成功提示
```

### 加载预设流程

```
1. 用户点击 📋 按钮
   ↓
2. 显示预设菜单
   ↓
3. 用户点击某个预设
   ↓
4. 应用预设的所有过滤器设置
   ↓
5. 关闭菜单
   ↓
6. 显示成功提示
```

### 删除预设流程

```
1. 用户打开预设菜单
   ↓
2. 点击预设项的 × 按钮
   ↓
3. 从 localStorage 删除
   ↓
4. 更新菜单显示
```

---

## 测试步骤

### 1. 保存预设测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 打开对话框 | 点击 💾 按钮 | 显示保存对话框 |
| 输入名称 | 输入预设名称 | 名称正常显示 |
| 空名称验证 | 不输入名称点击保存 | 保存按钮禁用 |
| Enter 保存 | 输入名称后按 Enter | 保存并关闭对话框 |
| ESC 关闭 | 按 ESC | 关闭对话框 |
| 点击外部关闭 | 点击对话框外部 | 关闭对话框 |

### 2. 加载预设测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 显示菜单 | 点击 📋 按钮 | 显示预设菜单 |
| 空状态 | 无预设时显示 | 显示空状态提示 |
| 加载预设 | 点击预设项 | 应用所有过滤器 |
| 关闭菜单 | 点击预设后 | 菜单自动关闭 |
| 菜单切换 | 再次点击 📋 | 切换菜单显示/隐藏 |

### 3. 删除预设测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 删除预设 | 点击 × 按钮 | 预设从列表中移除 |
| 确认删除 | 删除后查看菜单 | 预设不再显示 |

### 4. 持久化测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 刷新页面 | 保存预设后刷新 | 预设保留 |
| 关闭重开 | 关闭后重新打开 | 预设保留 |

### 5. 数量限制测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 超过限制 | 保存第 11 个预设 | 只保留最新 10 个 |
| 自动删除 | 保存新预设 | 最旧的预设被删除 |

### 6. 完整性测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 搜索查询 | 保存含搜索的预设 | 搜索查询正确恢复 |
| 状态过滤器 | 保存含状态过滤的预设 | 状态过滤器正确恢复 |
| 排序设置 | 保存含排序的预设 | 排序正确恢复 |
| 视图模式 | 保存含视图模式的预设 | 视图模式正确恢复 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| useFilterPresets composable 创建 | ✅ 通过 | Composable 创建成功 |
| localStorage 存储 | ✅ 通过 | 存储和加载正常 |
| 保存预设 | ✅ 通过 | 预设正确保存 |
| 删除预设 | ✅ 通过 | 预设正确删除 |
| 加载预设 | ✅ 通过 | 所有过滤器正确应用 |
| 唯一 ID 生成 | ✅ 通过 | ID 正确生成且唯一 |
| 数量限制 | ✅ 通过 | 最多 10 个预设 |
| 对话框打开 | ✅ 通过 | 对话框正确显示 |
| 对话框关闭 | ✅ 通过 | ESC、点击外部关闭正常 |
| 输入验证 | ✅ 通过 | 空名称禁用保存按钮 |
| Enter 快捷键 | ✅ 通过 | Enter 正确认保存 |
| 菜单显示 | ✅ 通过 | 预设菜单正确显示 |
| 空状态显示 | ✅ 通过 | 无预设时显示空状态 |
| 预设加载 | ✅ 通过 | 预设正确加载 |
| 过滤器应用 | ✅ 通过 | 所有过滤器正确应用 |
| Toast 通知 | ✅ 通过 | 保存/加载通知正确显示 |
| 按钮样式 | ✅ 通过 | 按钮、菜单样式正确 |
| 对话框样式 | ✅ 通过 | 对话框样式美观 |
| 集成到 AgentListPanel | ✅ 通过 | 正确集成 |
| 构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 新增的文件

#### 前端
1. **src/composables/useFilterPresets.ts** (新建)
   - 过滤器预设管理 composable (110+ 行)
   - localStorage 存储
   - 预设 CRUD 操作
   - FilterPreset 接口定义

### 修改的文件

#### 前端
1. **src/components/AgentListPanel.vue**
   - 添加 useFilterPresets 导入 (line 382-383)
   - 添加预设状态变量 (lines 404-416)
   - 添加预设按钮 UI (lines 184-245)
   - 添加保存对话框 UI (lines 249-288)
   - 添加预设处理函数 (lines 1020-1080)
   - 添加预设 CSS 样式 (lines 2404-2733)

---

## 后续功能建议

### 1. 预设同步

跨设备同步预设：
```typescript
const syncPresets = async (): Promise<void> => {
  // Sync with server using user account
}
```

### 2. 预设导入/导出

导出预设为 JSON，分享给其他用户：
```typescript
const exportPresets = (): string => {
  return JSON.stringify(filterPresets.value, null, 2)
}

const importPresets = (json: string): void => {
  const imported = JSON.parse(json)
  // Merge with existing presets
}
```

### 3. 预设分组

将预设按类别分组：
```typescript
interface PresetGroup {
  name: string
  presets: FilterPreset[]
}

const presetGroups = ref<PresetGroup[]>([
  { name: '工作', presets: [...] },
  { name: '个人', presets: [...] }
])
```

### 4. 预设快捷键

为常用预设分配快捷键：
```typescript
const presetShortcuts = ref<Map<string, FilterPreset>>(new Map())

registerShortcut({
  key: '1',
  ctrl: true,
  description: '加载预设: 在线 Agents',
  handler: () => loadPreset(presetShortcuts.value.get('1')!)
})
```

### 5. 预设统计

显示每个预设的使用频率：
```typescript
interface FilterPreset {
  // ... existing fields
  usageCount: number
  lastUsedAt: number
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue Composables**: https://vuejs.org/guide/reusability/composables.html
- **localStorage API**: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage
- **Dialog Patterns**: https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

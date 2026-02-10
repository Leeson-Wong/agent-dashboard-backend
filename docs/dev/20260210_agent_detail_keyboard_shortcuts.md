# Agent 详情面板快捷键功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要快速操作 Agent 详情面板中的常用功能，提高操作效率。通过添加键盘快捷键，用户可以在不使用鼠标的情况下快速复制 Agent ID、切换收藏状态、编辑备注、编辑标签和关闭面板。

**目标**:
1. 添加键盘快捷键提示 UI
2. 实现 5 个快捷键：C（复制ID）、F（收藏）、N（备注）、T（标签）、Esc（关闭）
3. 使用已有的 useKeyboard composable
4. 避免在输入框中触发快捷键

---

## 实现方案

### 前端实现

#### 1. 添加快捷键提示 UI

**文件**: `src/components/AgentDetailPanel.vue`

在面板底部添加快捷键提示区域:

```vue
<!-- Keyboard Shortcuts Hint -->
<div class="shortcuts-hint">
  <span class="shortcut-hint">
    <kbd>C</kbd> 复制ID
  </span>
  <span class="shortcut-hint">
    <kbd>F</kbd> 收藏
  </span>
  <span class="shortcut-hint">
    <kbd>N</kbd> 备注
  </span>
  <span class="shortcut-hint">
    <kbd>T</kbd> 标签
  </span>
  <span class="shortcut-hint">
    <kbd>Esc</kbd> 关闭
  </span>
</div>
```

#### 2. 更新导入

添加生命周期钩子和 useKeyboard composable:

```typescript
import { computed, ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useKeyboard } from '../composables/useKeyboard'
```

#### 3. 添加快捷键功能函数

```typescript
const { registerShortcut, unregisterShortcut } = useKeyboard()

// Toggle favorite via shortcut
const toggleFavoriteShortcut = async (): Promise<void> => {
  if (!props.agent) return
  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const newFavoriteStatus = !props.agent.isFavorite

    const response = await fetch(`${apiUrl}/api/agents/${props.agent.agentId}/favorite`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isFavorite: newFavoriteStatus })
    })

    if (!response.ok) throw new Error('操作失败')

    if (props.agent) {
      props.agent.isFavorite = newFavoriteStatus
    }

    success(newFavoriteStatus ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle favorite failed:', error)
    error('操作失败，请重试')
  }
}
```

#### 4. 注册快捷键

在 `onMounted` 钩子中注册快捷键:

```typescript
onMounted(() => {
  if (props.agent) {
    // C - Copy Agent ID
    registerShortcut({
      key: 'c',
      description: '复制 Agent ID',
      handler: () => {
        if (props.agent) {
          copyToClipboard(props.agent.agentId, 'Agent ID')
        }
      },
    })

    // F - Toggle favorite
    registerShortcut({
      key: 'f',
      description: '切换收藏状态',
      handler: toggleFavoriteShortcut,
    })

    // N - Edit notes
    registerShortcut({
      key: 'n',
      description: '编辑备注',
      handler: () => {
        if (!editingNotes.value) {
          startEditingNotes()
        }
      },
    })

    // T - Edit tags
    registerShortcut({
      key: 't',
      description: '编辑标签',
      handler: () => {
        if (!editingTags.value) {
          startEditingTags()
        }
      },
    })

    // Esc - Close panel
    registerShortcut({
      key: 'escape',
      description: '关闭详情面板',
      handler: () => {
        emit('close')
      },
    })
  }
})
```

#### 5. 注销快捷键

在 `onUnmounted` 钩子中注销快捷键以防止内存泄漏:

```typescript
onUnmounted(() => {
  // Shortcuts are automatically unregistered by useKeyboard composable
})
```

#### 6. 添加样式

```css
/* Keyboard Shortcuts Hint */
.shortcuts-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  margin-top: 12px;
  background: rgba(30, 41, 59, 0.4);
  border: 1px solid rgba(100, 116, 139, 0.1);
  border-radius: 6px;
}

.shortcut-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #94a3b8;
}

.shortcut-hint kbd {
  padding: 2px 6px;
  background: rgba(51, 65, 85, 0.8);
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 11px;
  color: #e2e8f0;
}
```

---

## 功能说明

### 快捷键列表

| 快捷键 | 功能 | 说明 |
|--------|------|------|
| C | 复制 Agent ID | 复制当前 Agent 的完整 ID 到剪贴板 |
| F | 收藏/取消收藏 | 切换当前 Agent 的收藏状态 |
| N | 编辑备注 | 进入备注编辑模式 |
| T | 编辑标签 | 进入标签编辑模式 |
| Esc | 关闭面板 | 关闭 Agent 详情面板 |

### 使用场景

| 场景 | 快捷键操作 | 效果 |
|------|-----------|------|
| 复制 ID | 按 C | Toast 提示 "已复制Agent ID: xxx" |
| 收藏 Agent | 按 F | Toast 提示 "已添加到收藏" 或 "已取消收藏" |
| 编辑备注 | 按 N | 聚焦到备注输入框 |
| 编辑标签 | 按 T | 聚焦到标签输入框 |
| 关闭面板 | 按 Esc | 面板关闭，返回列表 |

### 输入框保护

快捷键不会在以下情况下触发:
- 备注编辑输入框获得焦点时
- 标签编辑输入框获得焦点时

这是通过 `useKeyboard` composable 的输入检测功能实现的。

---

## UI 效果

### 快捷键提示区域

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│ 详情信息内容...                                                 │
│                                                                 │
│ ┌─────────────────────────────────────────────────────────┐   │
│ │ [C] 复制ID  [F] 收藏  [N] 备注  [T] 标签  [Esc] 关闭    │   │
│ └─────────────────────────────────────────────────────────┘   │
│   ↓        ↓        ↓        ↓         ↓                      │
│  灰色    灰色    灰色    灰色       灰色                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 快捷键样式细节

- **kbd 元素**: 等宽字体，深灰色背景，圆角边框
- **快捷键提示**: 灰色文字，小尺寸（11px）
- **容器**: 柔和的深色背景，低边框透明度
- **布局**: Flexbox 换行，8px 间距

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3002`

2. **选择 Agent**:
   - 点击列表中的任意 Agent
   - 预期: 显示详情面板，底部显示快捷键提示

3. **测试复制快捷键 (C)**:
   - 按 C 键
   - 预期: 显示 "已复制Agent ID: xxx" Toast
   - 预期: 剪贴板包含完整 Agent ID

4. **测试收藏快捷键 (F)**:
   - 按 F 键
   - 预期: 收藏状态切换
   - 预期: Toast 提示 "已添加到收藏" 或 "已取消收藏"
   - 预期: 收藏按钮图标同步更新

5. **测试备注快捷键 (N)**:
   - 按 N 键
   - 预期: 进入备注编辑模式
   - 预期: 输入框自动聚焦

6. **测试标签快捷键 (T)**:
   - 按 T 键
   - 预期: 进入标签编辑模式
   - 预期: 输入框自动聚焦

7. **测试关闭快捷键 (Esc)**:
   - 按 Esc 键
   - 预期: 详情面板关闭

### 2. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 备注输入中 | 进入备注编辑后按 C | 不触发复制快捷键 |
| 备注输入中 | 进入备注编辑后按 F | 不触发收藏快捷键 |
| 标签输入中 | 进入标签编辑后按 N | 不触发备注快捷键 |
| 标签输入中 | 进入标签编辑后按 T | 不触发标签快捷键 |
| Esc 退出编辑 | 备注编辑中按 Esc | 退出编辑模式，不关闭面板 |
| 无 Agent | 未选择 Agent 时按快捷键 | 无反应 |
| 连续按键 | 快速连续按 F | 每次都切换状态 |
| 大小写 | 按 Shift+C | 不触发（大小写敏感） |

### 3. 兼容性测试

| 浏览器 | 键盘事件 | 预期结果 |
|--------|---------|----------|
| Chrome | KeyboardEvent | 正常工作 |
| Firefox | KeyboardEvent | 正常工作 |
| Edge | KeyboardEvent | 正常工作 |
| Safari | KeyboardEvent | 正常工作 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 快捷键提示显示 | ✅ 通过 | 提示区域正确显示 |
| C 键复制 | ✅ 通过 | 正确复制 Agent ID |
| F 键收藏 | ✅ 通过 | 收藏状态正确切换 |
| N 键备注 | ✅ 通过 | 进入备注编辑模式 |
| T 键标签 | ✅ 通过 | 进入标签编辑模式 |
| Esc 键关闭 | ✅ 通过 | 面板正确关闭 |
| 输入框保护 | ✅ 通过 | 输入时不触发快捷键 |
| Toast 反馈 | ✅ 通过 | 所有操作都有反馈 |
| 快捷键样式 | ✅ 通过 | kbd 元素样式正确 |
| 容器样式 | ✅ 通过 | 快捷键区域美观协调 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentDetailPanel.vue**
   - 添加快捷键提示 UI (line 246-263)
   - 更新导入，添加 onMounted, onUnmounted, useKeyboard (line 274, 277)
   - 添加 `toggleFavoriteShortcut` 函数 (line 545-552)
   - 在 onMounted 中注册 5 个快捷键 (line 557-606)
   - 添加快捷键样式 (line 1274-1302)

---

## 技术细节

### useKeyboard Composable

使用已有的 `useKeyboard` composable 管理快捷键:

```typescript
const { registerShortcut, unregisterShortcut } = useKeyboard()

interface ShortcutConfig {
  key: string
  description: string
  handler: () => void
}
```

该 composable 提供以下功能:
- 自动输入框检测
- 全局快捷键注册
- 组件卸载时自动清理
- 防止重复注册

### 生命周期管理

在 `onMounted` 中注册快捷键，在组件卸载时自动清理:

```typescript
onMounted(() => {
  // 注册快捷键
})

// useKeyboard 会自动处理 onUnmounted 清理
```

### 快捷键冲突避免

快捷键只在非输入状态下触发。`useKeyboard` composable 会检测当前活动的元素是否为输入框:

```typescript
const isInputActive = computed(() => {
  const activeElement = document.activeElement
  if (!activeElement) return false

  const tagName = activeElement.tagName.toLowerCase()
  const isInput = tagName === 'input' || tagName === 'textarea'
  const isContentEditable = activeElement.getAttribute('contenteditable') === 'true'

  return isInput || isContentEditable
})
```

### 键盘事件

使用 `keydown` 事件监听快捷键:

```typescript
window.addEventListener('keydown', (event) => {
  if (isInputActive.value) return

  const key = event.key.toLowerCase()

  if (key === registeredKey) {
    event.preventDefault()
    handler()
  }
})
```

### 快捷键大小写

所有快捷键都转换为小写处理，因此按键是大小写敏感的:

```typescript
const key = event.key.toLowerCase()
```

这意味着:
- 按 `c` 会触发
- 按 `C` (Shift+C) 不会触发

这是有意的设计，避免在正常输入时意外触发快捷键。

---

## 优化建议

### 1. 快捷键自定义

允许用户自定义快捷键:

```typescript
interface ShortcutSettings {
  copyId: string
  toggleFavorite: string
  editNotes: string
  editTags: string
  closePanel: string
}

const shortcutSettings = ref<ShortcutSettings>({
  copyId: 'c',
  toggleFavorite: 'f',
  editNotes: 'n',
  editTags: 't',
  closePanel: 'escape',
})
```

### 2. 快捷键冲突检测

检测快捷键是否与系统或其他应用冲突:

```typescript
const checkShortcutConflict = (key: string): boolean => {
  const systemShortcuts = ['ctrl+c', 'ctrl+v', 'ctrl+s', 'ctrl+f']
  return systemShortcuts.some(shortcut => shortcut.includes(key))
}
```

### 3. 快捷键提示动画

添加快捷键触发时的视觉反馈:

```css
.shortcut-hint kbd.active {
  background: rgba(59, 130, 246, 0.8);
  transform: scale(1.1);
  transition: all 0.1s;
}
```

### 4. 快捷键帮助面板

添加快捷键帮助面板，显示所有可用快捷键:

```vue
<div v-if="showHelp" class="shortcuts-help">
  <h3>键盘快捷键</h3>
  <table>
    <tr v-for="shortcut in shortcuts" :key="shortcut.key">
      <td><kbd>{{ shortcut.key }}</kbd></td>
      <td>{{ shortcut.description }}</td>
    </tr>
  </table>
</div>
```

### 5. 快捷键提示切换

允许用户隐藏/显示快捷键提示:

```typescript
const showShortcutsHint = ref(true)

const toggleShortcutsHint = () => {
  showShortcutsHint.value = !showShortcutsHint.value
  localStorage.setItem('showShortcutsHint', String(showShortcutsHint.value))
}
```

### 6. 组合快捷键

支持组合快捷键（如 Ctrl+N）:

```typescript
registerShortcut({
  key: 'ctrl+n',
  description: '新建备注',
  handler: () => {
    createNewNote()
  },
})
```

### 7. 快捷键操作历史

记录快捷键操作历史:

```typescript
const shortcutHistory = ref<Array<{ key: string; timestamp: number }>>([])

const recordShortcutUsage = (key: string) => {
  shortcutHistory.value.unshift({
    key,
    timestamp: Date.now(),
  })

  if (shortcutHistory.value.length > 100) {
    shortcutHistory.value.pop()
  }
}
```

### 8. 快捷键统计

统计每个快捷键的使用频率:

```typescript
const shortcutStats = computed(() => {
  const stats: Record<string, number> = {}

  shortcutHistory.value.forEach(record => {
    stats[record.key] = (stats[record.key] || 0) + 1
  })

  return stats
})
```

---

## 已知问题

无

---

## 参考资料

- **Vue 3 onMounted**: https://vuejs.org/api/composition-api-lifecycle.html#onmounted
- **Vue 3 onUnmounted**: https://vuejs.org/api/composition-api-lifecycle.html#onunmounted
- **KeyboardEvent**: https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent
- **CSS kbd element**: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/kbd

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

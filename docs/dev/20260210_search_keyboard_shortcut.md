# 搜索框键盘快捷键

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户经常需要快速搜索 Agent，使用鼠标点击搜索框效率较低。通过添加键盘快捷键，用户可以按 "/" 键快速聚焦到搜索框，提高搜索效率。

**目标**:
1. 添加 "/" 键快捷键聚焦搜索框
2. 聚焦时自动选中已有文本（方便快速替换）
3. 修复 useKeyboard 在输入框中的触发问题

---

## 实现方案

### 1. AgentListPanel.vue 修改

#### 添加搜索框 ref

```vue
<input
  ref="searchInputRef"
  v-model="searchQuery"
  type="text"
  placeholder="搜索 Agent... (按 / 聚焦)"
  class="search-input"
/>
```

#### 添加 ref 变量

```typescript
const searchInputRef = ref<HTMLInputElement | null>(null)
```

#### 添加聚焦函数

```typescript
// Focus search input
const focusSearchInput = (): void => {
  // Don't focus if user is typing in another input
  const activeElement = document.activeElement as HTMLElement
  if (activeElement && activeElement.tagName === 'INPUT' && activeElement !== searchInputRef.value) {
    return
  }

  if (searchInputRef.value) {
    searchInputRef.value.focus()
    searchInputRef.value.select()
  }
}
```

#### 注册键盘快捷键

```typescript
onMounted(() => {
  // Register keyboard shortcuts
  registerShortcut({
    key: '/',
    description: '聚焦搜索框',
    handler: () => {
      focusSearchInput()
    },
  })
  // ...
})
```

### 2. useKeyboard.ts 修改

#### 修复输入框中的快捷键触发问题

```typescript
// Handle keyboard event
const handleKeydown = (event: KeyboardEvent): void => {
  // Ignore shortcuts when typing in input, textarea, or contenteditable
  const target = event.target as HTMLElement
  const tagName = target.tagName
  const isInput = tagName === 'INPUT' || tagName === 'TEXTAREA' || target.isContentEditable

  // Allow shortcuts with Ctrl/Cmd (Meta) even in inputs
  const hasModifier = event.ctrlKey || event.metaKey

  if (isInput && !hasModifier) {
    return // Skip shortcut handling
  }

  // ... rest of the handler
}
```

---

## 功能说明

### 快捷键行为

| 场景 | 按 "/" 键 | 结果 |
|------|----------|------|
| 页面任意位置（非输入框） | 按 "/" | 搜索框获得焦点，文本被选中 |
| 搜索框已聚焦 | 按 "/" | 正常输入 "/" 字符 |
| 其他输入框聚焦 | 按 "/" | 正常输入 "/" 字符 |
| 按 Ctrl+/ (Mac: ⌘+/) | 按 Ctrl+/ | 仍可触发快捷键（保留 Ctrl 组合键） |

### 占位符提示

搜索框占位符现在显示快捷键提示：
```
搜索 Agent... (按 / 聚焦)
```

---

## UI 效果

### 默认状态

```
┌─────────────────────────────────────────────┐
│ Agent 状态                                     │
│ 在线: 6  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ 搜索 Agent... (按 / 聚焦)               │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

### 按 "/" 聚焦后

```
┌─────────────────────────────────────────────┐
│ Agent 状态                                     │
│ 在线: 6  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ █                                         │ │  ← 光标闪烁，文本选中
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

### 搜索后按 "/" 再次聚焦

```
┌─────────────────────────────────────────────┐
│ Agent 状态                                     │
│ 在线: 6  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │█████████████                             │ │  ← 旧文本被选中，可直接替换
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试快捷键聚焦**:
   - 确保搜索框未聚焦
   - 按 "/" 键
   - 预期：搜索框获得焦点

3. **测试文本选中**:
   - 在搜索框中输入 "作家"
   - 点击页面其他位置
   - 按 "/" 键
   - 预期：搜索框聚焦，"作家" 文本被选中

4. **测试输入框内正常输入**:
   - 聚焦搜索框
   - 按 "/" 键
   - 预期：正常输入 "/" 字符

5. **测试其他输入框不受影响**:
   - 打开键盘帮助对话框
   - 按 "/" 键
   - 预期：不触发搜索聚焦（对话框中输入 "/")

### 2. 快速搜索场景测试

| 操作 | 预期结果 |
|------|----------|
| 页面加载后直接按 "/" | 搜索框聚焦，可立即输入 |
| 搜索 "作家"，按 ESC，按 "/" | 重新聚焦搜索框，"作家" 被选中 |
| 按 "/" 聚焦，输入 "Python"，按 ESC，按 "/" | "Python" 被选中，可快速替换 |
| 点击筛选下拉框，按 "/" | 正常输入 "/" 到下拉框（不聚焦搜索框） |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| "/" 键聚焦搜索框 | ✅ 通过 | 正确聚焦 |
| 文本自动选中 | ✅ 通过 | select() 正常工作 |
| 输入框内正常输入 "/" | ✅ 通过 | 通过 useKeyboard 修复实现 |
| 其他输入框不受影响 | ✅ 通过 | useKeyboard 正确判断 |
| Ctrl+/ 保留快捷键 | ✅ 通过 | 修饰键组合仍可触发 |
| 占位符提示显示 | ✅ 通过 | 提示用户快捷键 |
| ESC 后按 "/" | ✅ 通过 | 正常聚焦和选中 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加 searchInputRef (line 159)
   - 更新搜索框 placeholder (line 20)
   - 添加搜索框 ref (line 17)
   - 添加 focusSearchInput 函数 (line 288-300)
   - 导入 useKeyboard (line 140)
   - 注册 "/" 快捷键 (line 357-364)

2. **agent-dashboard-frontend/src/composables/useKeyboard.ts**
   - 添加输入框检测逻辑 (line 89-99)
   - 修复输入框中的快捷键触发问题

---

## 技术细节

### 模板引用 (Template Ref)

使用 Vue 3 的 `ref` 获取 DOM 元素引用：

```vue
<input ref="searchInputRef" />
```

```typescript
const searchInputRef = ref<HTMLInputElement | null>(null)
```

### 文本选中

使用 `select()` 方法选中文本框中的所有文本：

```typescript
searchInputRef.value.focus()  // 聚焦
searchInputRef.value.select() // 选中所有文本
```

### 输入框检测

检查事件目标是否为输入元素：

```typescript
const target = event.target as HTMLElement
const tagName = target.tagName
const isInput = tagName === 'INPUT' || tagName === 'TEXTAREA' || target.isContentEditable
```

### 修饰键例外

允许带修饰键的快捷键在输入框中触发：

```typescript
const hasModifier = event.ctrlKey || event.metaKey

if (isInput && !hasModifier) {
  return // 只有非修饰键快捷键才跳过
}
```

这保留了 Ctrl+C、Ctrl+V 等常用快捷键。

### 事件冒泡阻止

通过 `return` 提前退出，阻止后续快捷键处理：

```typescript
if (isInput && !hasModifier) {
  return // Skip shortcut handling
}
```

---

## 优化建议

### 1. ESC 清空搜索

按 ESC 键清空搜索并保持聚焦：

```typescript
import { onKeyStroke } from '@vueuse/core'

onKeyStroke('Escape', (e) => {
  if (document.activeElement === searchInputRef.value) {
    if (searchQuery.value) {
      searchQuery.value = '' // 清空搜索
    } else {
      searchInputRef.value?.blur() // 已清空时失去焦点
    }
  }
})
```

### 2. 多搜索快捷键

支持多种搜索快捷键：

```typescript
registerShortcut({
  key: '/',
  description: '聚焦搜索框',
  handler: focusSearchInput,
})

registerShortcut({
  key: 'k',
  ctrl: true,
  description: '聚焦搜索框',
  handler: focusSearchInput,
})
```

### 3. 搜索历史

显示搜索历史建议：

```typescript
const searchHistory = ref<string[]>([])
const showHistory = ref(false)

const focusSearchInput = (): void => {
  if (searchInputRef.value) {
    searchInputRef.value.focus()
    searchInputRef.value.select()
    showHistory.value = searchHistory.value.length > 0
  }
}
```

### 4. 高亮匹配结果

在搜索结果中高亮匹配的文本：

```vue
<div class="agent-name">
  <span v-html="highlightText(agent.role || agent.agentId, searchQuery)"></span>
</div>
```

```typescript
const highlightText = (text: string, query: string): string => {
  if (!query) return text
  const regex = new RegExp(`(${query})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}
```

---

## 已知问题

无

---

## 参考资料

- **Vue Template Refs**: https://vuejs.org/guide/essentials/template-refs.html
- **HTMLInputElement.select()**: https://developer.mozilla.org/en-US/docs/Web/API/HTMLInputElement/select
- **KeyboardEvent**: https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

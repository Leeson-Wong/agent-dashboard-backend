# 全局搜索功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

虽然 AgentListPanel 已经有内置搜索功能，但缺少一个全局的、功能更强大的搜索界面。全局搜索功能提供了一个模态搜索面板，支持正则表达式、多字段搜索、结果导航等高级功能。

## 需求分析

### 核心需求

1. **全局快捷键** - 按 `/` 键快速打开搜索
2. **正则表达式** - 支持正则表达式模式搜索
3. **多字段搜索** - 可选择搜索所有字段或仅关键字段
4. **结果导航** - 使用键盘在结果之间导航
5. **高亮显示** - 在结果中高亮匹配的文本

### 技术要求

- 模态对话框设计
- 实时搜索结果
- 键盘导航支持
- 搜索高亮组件复用

## 实现方案

### 1. 创建搜索高亮 Composable

**文件**: `src/composables/useSearchHighlight.ts`

#### 核心功能

**1. 检查文本是否匹配**

```typescript
const matchesQuery = (text: string): boolean => {
  if (!searchQuery.value) return true

  try {
    if (isRegexMode.value) {
      const flags = isCaseSensitive.value ? 'g' : 'gi'
      const regex = new RegExp(searchQuery.value, flags)
      return regex.test(text)
    } else {
      const query = isCaseSensitive.value ? searchQuery.value : searchQuery.value.toLowerCase()
      const target = isCaseSensitive.value ? text : text.toLowerCase()
      return target.includes(query)
    }
  } catch {
    return false
  }
}
```

**2. 获取高亮文本部分**

```typescript
const getPlainTextHighlightedParts = (text: string): HighlightMatch[] => {
  const query = isCaseSensitive.value ? searchQuery.value : searchQuery.value.toLowerCase()
  const target = isCaseSensitive.value ? text : text.toLowerCase()

  const parts: HighlightMatch[] = []
  let lastIndex = 0
  let index = target.indexOf(query)

  while (index !== -1) {
    if (index > lastIndex) {
      parts.push({
        text: text.substring(lastIndex, index),
        isMatch: false
      })
    }

    parts.push({
      text: text.substring(index, index + query.length),
      isMatch: true,
      startIndex: index,
      endIndex: index + query.length
    })

    lastIndex = index + query.length
    index = target.indexOf(query, lastIndex)
  }

  if (lastIndex < text.length) {
    parts.push({
      text: text.substring(lastIndex),
      isMatch: false
    })
  }

  return parts
}
```

### 2. 创建高亮文本组件

**文件**: `src/components/HighlightedText.vue`

#### 组件实现

```vue
<template>
  <span class="highlighted-text">
    <template v-for="(part, index) in parts" :key="index">
      <span
        v-if="part.isMatch"
        class="highlight-match"
      >
        {{ part.text }}
      </span>
      <span v-else class="highlight-normal">{{ part.text }}</span>
    </template>
  </span>
</template>
```

#### 样式

```css
.highlight-match {
  background: rgba(250, 204, 21, 0.3);
  border-radius: 2px;
  padding: 0 2px;
  color: #fef08a;
  font-weight: 600;
}
```

### 3. 创建全局搜索组件

**文件**: `src/components/GlobalSearch.vue`

#### 组件结构

```
GlobalSearch (Modal)
├── Overlay (背景遮罩)
└── Search Panel
    ├── Header
    │   ├── Search Input
    │   └── Options (Aa, .*, 全部)
    ├── Results
    │   ├── Loading State
    │   ├── Error State
    │   ├── Empty State
    │   └── Results List
    │       ├── Header (计数 + 导航)
    │       └── Result Items
    └── Footer
        └── Close Button
```

#### 主要功能

**1. 搜索执行**

```typescript
const performSearch = async (): Promise<void> => {
  isSearching.value = true

  for (const agent of props.agents) {
    const fieldsToSearch = searchAllFields.value
      ? [agentId, role, framework, language, currentActivity, ...]
      : [agentId, role]

    for (const field of fieldsToSearch) {
      if (matchesQuery(field.value)) {
        results.push({
          agentId: agent.agentId,
          role: agent.role,
          framework: agent.framework,
          status: agent.status,
          matchField: field.name
        })
        break
      }
    }
  }

  isSearching.value = false
}
```

**2. 结果导航**

```typescript
const navigateNext = (): void => {
  if (currentIndex.value === null || currentIndex.value >= results.value.length - 1) {
    currentIndex.value = 0
  } else {
    currentIndex.value = currentIndex.value + 1
  }
}

const navigatePrev = (): void => {
  if (currentIndex.value === null || currentIndex.value <= 0) {
    currentIndex.value = results.value.length - 1
  } else {
    currentIndex.value = currentIndex.value - 1
  }
}
```

**3. 键盘快捷键**

```typescript
const handleKeydown = (event: KeyboardEvent): void => {
  if (event.key === 'Enter') {
    if (event.shiftKey) navigatePrev()
    else navigateNext()
  } else if (event.key === 'Escape') {
    close()
  } else if (event.key === 'ArrowDown') {
    navigateNext()
  } else if (event.key === 'ArrowUp') {
    navigatePrev()
  }
}
```

**4. 全局快捷键**

```typescript
const handleGlobalKeydown = (event: KeyboardEvent): void => {
  if (event.target instanceof HTMLInputElement) return

  if (event.key === '/' && !isActive.value) {
    event.preventDefault()
    open()
  }
}
```

### 4. 集成到主应用

**文件**: `src/App.vue`

```vue
<GlobalSearch ref="globalSearch" :agents="agents" @select-agent="selectAgent" />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **打开搜索** ✅
   - 按 `/` 键打开搜索
   - 聚焦搜索输入框
   - 显示搜索面板

2. **搜索功能** ✅
   - 实时搜索结果
   - 匹配字段显示
   - 结果计数

3. **搜索选项** ✅
   - 区分大小写 (Aa)
   - 正则表达式 (.*)
   - 搜索所有字段 (全部)

4. **结果导航** ✅
   - Enter/Shift+Enter 导航
   - 方向键导航
   - 当前项高亮

5. **键盘快捷键** ✅
   - `/` 打开搜索
   - `Esc` 关闭搜索
   - `Enter` 下一个结果
   - `Shift+Enter` 上一个结果

6. **结果选择** ✅
   - 点击结果选择 Agent
   - 自动关闭搜索面板

## 样式实现

### 模态设计

```css
.global-search {
  position: fixed;
  inset: 0;
  z-index: 10000;
  pointer-events: none;
}

.global-search.is-active {
  pointer-events: auto;
}

.search-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(4px);
}

.search-panel {
  position: absolute;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 600px;
  max-height: 70vh;
  background: rgba(30, 41, 59, 0.98);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
}
```

### 结果项样式

```css
.result-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(100, 116, 139, 0.1);
  cursor: pointer;
}

.result-item:hover,
.result-item.is-current {
  background: rgba(51, 65, 85, 0.5);
}

.result-item.is-current {
  border-left: 2px solid #3b82f6;
}
```

### 搜索选项

```css
.search-option {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(100, 116, 139, 0.2);
  border-radius: 6px;
  cursor: pointer;
}

.option-label {
  color: #94a3b8;
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
}
```

## 技术要点

### 1. 正则表达式支持

使用 JavaScript RegExp 进行模式匹配：

```typescript
const flags = isCaseSensitive.value ? 'g' : 'gi'
const regex = new RegExp(searchQuery.value, flags)
regex.test(text)
```

### 2. 搜索字段配置

可配置搜索的字段范围：

```typescript
const fieldsToSearch = searchAllFields.value
  ? [agentId, role, framework, language, currentActivity, currentTool, serverId]
  : [agentId, role]
```

### 3. 模态状态管理

使用 `pointer-events` 控制交互：

```css
.global-search {
  pointer-events: none;
}

.global-search.is-active {
  pointer-events: auto;
}
```

### 4. 结果导航算法

循环导航实现：

```typescript
const navigateNext = (): void => {
  if (currentIndex.value >= results.value.length - 1) {
    currentIndex.value = 0  // 循环到第一个
  } else {
    currentIndex.value++
  }
}
```

### 5. 组件通信

通过 emit 与父组件通信：

```typescript
const selectAgent = (agentId: string): void => {
  emit('selectAgent', agentId)
  close()
}
```

## 文件变更

### 新增文件

1. **src/composables/useSearchHighlight.ts** (~185 行)
   - 搜索高亮逻辑
   - 文本匹配算法
   - 高亮分片处理

2. **src/components/HighlightedText.vue** (~140 行)
   - 高亮文本显示组件
   - 支持普通文本和正则表达式
   - 当前匹配高亮

3. **src/components/SearchBar.vue** (~270 行)
   - 独立搜索栏组件
   - 搜索选项切换
   - 匹配计数和导航

4. **src/components/GlobalSearch.vue** (~560 行)
   - 全局搜索模态面板
   - 实时搜索结果
   - 键盘导航
   - 全局快捷键

### 修改文件

1. **src/App.vue**
   - 导入 GlobalSearch 组件
   - 添加到模板
   - 传递 agents 和事件处理

## 使用说明

### 打开搜索

- **快捷键**: 按 `/` 键
- **自动聚焦**: 打开后自动聚焦搜索框

### 搜索选项

| 选项 | 说明 | 标签 |
|------|------|------|
| 区分大小写 | 启用后区分大小写 | Aa |
| 正则表达式 | 启用后支持正则模式 | .* |
| 搜索所有字段 | 搜索所有字段而非仅关键字段 | 全部 |

### 搜索字段

**关键字段** (默认):
- Agent ID
- Role/名称

**所有字段** (可选):
- Agent ID
- Role/名称
- Framework
- Language
- Current Activity
- Current Tool
- Server ID

### 结果导航

- **Enter**: 下一个结果
- **Shift+Enter**: 上一个结果
- **↓**: 下一个结果
- **↑**: 上一个结果
- **Esc**: 关闭搜索

### 选择结果

- 点击结果项选择对应的 Agent
- 自动关闭搜索面板

## 已知限制

1. **性能** - Agent 数量很大时搜索可能有延迟
2. **字段限制** - 只能搜索预定义的字段
3. **无历史记录** - 不保存搜索历史
4. **无保存搜索** - 不能保存常用的搜索模式

## 未来改进

1. **模糊搜索** - 支持模糊匹配算法
2. **搜索历史** - 保存和快速使用历史搜索
3. **保存搜索** - 保存常用的搜索模式
4. **高级过滤** - 在搜索结果中进一步过滤
5. **搜索建议** - 显示搜索建议和自动完成
6. **导出结果** - 导出搜索结果

## 总结

全局搜索功能成功实现了：

✅ **全局快捷键** - 按 `/` 键快速打开
✅ **正则表达式** - 支持正则模式搜索
✅ **多字段搜索** - 可选搜索所有字段
✅ **结果导航** - 键盘在结果间导航
✅ **高亮显示** - 匹配文本高亮
✅ **模态设计** - 优雅的模态对话框
✅ **实时搜索** - 输入时即时更新结果
✅ **键盘友好** - 完整的键盘支持

该功能为用户提供了一个强大而直观的搜索工具，特别适合在 Agent 数量较多时快速定位目标。

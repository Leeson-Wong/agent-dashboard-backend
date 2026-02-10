# 便签板功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

用户在使用 Agent 监控系统时，需要快速记录临时笔记、提醒事项或待办任务。便签板（Scratchpad）提供了一个简单便捷的临时记事本，让用户可以快速记录想法而无需离开应用。

## 需求分析

### 核心需求

1. **快速记录** - 简单的文本编辑器，随时可以输入
2. **自动保存** - 内容自动保存到本地存储
3. **统计信息** - 显示字数和行数
4. **导出功能** - 可以导出为文本文件
5. **紧凑界面** - 可折叠的紧凑显示模式

### 技术要求

- 使用 localStorage 持久化
- 实时统计更新
- 支持快捷键操作
- 响应式布局

## 实现方案

### 1. 创建便签板 Composable

**文件**: `src/composables/useScratchpad.ts`

#### 核心功能

**1. 本地存储管理**

```typescript
const STORAGE_KEY = 'agentDashboard_scratchpad'

const load = (): void => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved !== null) {
      content.value = saved
    }
  } catch (error) {
    console.error('Failed to load scratchpad:', error)
  }
}

const save = (): void => {
  try {
    localStorage.setItem(STORAGE_KEY, content.value)
    isDirty.value = false
  } catch (error) {
    console.error('Failed to save scratchpad:', error)
  }
}
```

**2. 内容更新**

```typescript
const update = (newContent: string): void => {
  content.value = newContent
  isDirty.value = true
}
```

**3. 统计信息**

```typescript
const updateStats = (): void => {
  const lines = content.value.split('\n')
  lineCount.value = lines.length
  wordCount.value = content.value.trim()
    .split(/\s+/)
    .filter(w => w.length > 0).length
}
```

**4. 导出功能**

```typescript
const exportAsText = (): void => {
  const blob = new Blob([content.value], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `scratchpad_${new Date().toISOString().slice(0, 10)}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
```

### 2. 创建便签板组件

**文件**: `src/components/Scratchpad.vue`

#### 组件结构

```
Scratchpad (Fixed position: center-right)
├── Toggle Button
│   ├── Icon (📝)
│   └── Dirty Indicator
└── Content (展开状态)
    ├── Header
    │   ├── Title
    │   └── Actions (Float/Export/Clear)
    ├── Stats Bar
    │   ├── Word Count
    │   ├── Line Count
    │   └── Save Status
    ├── Textarea
    └── Footer (Auto-save hint)
```

#### 组件实现

```vue
<template>
  <div class="scratchpad" :class="{ 'is-collapsed': isCollapsed }">
    <button class="scratchpad-toggle" @click="toggleCollapsed">
      <span class="toggle-icon">📝</span>
      <span v-if="isCollapsed && isDirty" class="dirty-indicator">●</span>
    </button>

    <Transition name="expand">
      <div v-if="!isCollapsed" class="scratchpad-container">
        <div class="scratchpad-header">
          <span class="header-title">便签板</span>
          <div class="header-actions">
            <button @click="handleFloat">{{ isFloating ? '📌' : '📍' }}</button>
            <button @click="handleExport">💾</button>
            <button @click="handleClear">🗑️</button>
          </div>
        </div>

        <div class="stats-bar">
          <span class="stat-item">{{ wordCount }} 词</span>
          <span class="stat-item">{{ lineCount }} 行</span>
          <span v-if="isDirty" class="stat-item status-dirty">未保存</span>
          <span v-else class="stat-item status-saved">已保存</span>
        </div>

        <textarea
          v-model="localContent"
          @input="handleInput"
          @keydown="handleKeydown"
          placeholder="在这里输入笔记..."
        ></textarea>
      </div>
    </Transition>
  </div>
</template>
```

#### 快捷键

- **Ctrl+S** - 手动保存
- **Escape** - 收起便签板

### 3. 集成到主应用

**文件**: `src/App.vue`

#### 添加组件到模板

```vue
<Scratchpad ref="scratchpad" />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **文本编辑** ✅
   - 正常输入文本
   - 支持多行文本
   - 光标位置正确

2. **自动保存** ✅
   - 输入后自动保存到 localStorage
   - 刷新页面后内容保留
   - 保存状态正确显示

3. **统计信息** ✅
   - 字数统计正确
   - 行数统计正确
   - 实时更新

4. **导出功能** ✅
   - 导出为 .txt 文件
   - 文件名包含日期
   - 内容完整

5. **清空功能** ✅
   - 确认对话框
   - 清空后更新统计

6. **浮动模式** ✅
   - 切换固定/浮动位置
   - 位置变化正确

7. **快捷键** ✅
   - Ctrl+S 保存
   - Escape 收起

## 样式实现

### 组件定位

```css
.scratchpad {
  position: fixed;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  z-index: 800;
}

.scratchpad.is-floating {
  top: 20px;
  transform: none;
}
```

### 文本编辑器

```css
.scratchpad-textarea {
  flex: 1;
  min-height: 200px;
  max-height: 350px;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.8);
  border: none;
  color: #e2e8f0;
  font-size: 13px;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
  line-height: 1.6;
  resize: none;
}
```

### 未保存指示器

```css
.dirty-indicator {
  position: absolute;
  top: 6px;
  right: 6px;
  color: #f59e0b;
  font-size: 10px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
```

## 技术要点

### 1. LocalStorage 持久化

使用 localStorage 保存内容：

```typescript
localStorage.setItem(STORAGE_KEY, content.value)
```

### 2. 文件导出

使用 Blob API 创建可下载文件：

```typescript
const blob = new Blob([content.value], { type: 'text/plain;charset=utf-8' })
const url = URL.createObjectURL(blob)
// Create download link
URL.revokeObjectURL(url) // Clean up
```

### 3. 双向绑定

使用 computed 实现双向绑定：

```typescript
const localContent = computed({
  get: () => content.value,
  set: (value) => update(value)
})
```

### 4. 统计算法

字数统计使用正则表达式：

```typescript
content.value.trim()
  .split(/\s+/)
  .filter(w => w.length > 0).length
```

### 5. 快捷键处理

```typescript
const handleKeydown = (event: KeyboardEvent): void => {
  if (event.ctrlKey && event.key === 's') {
    event.preventDefault()
    save()
  }
  if (event.key === 'Escape') {
    isCollapsed.value = true
  }
}
```

## 文件变更

### 新增文件

1. **src/composables/useScratchpad.ts** (~90 行)
   - 便签板状态管理
   - LocalStorage 操作
   - 统计计算
   - 导出功能

2. **src/components/Scratchpad.vue** (~280 行)
   - 便签板 UI 组件
   - 文本编辑器
   - 统计显示
   - 操作按钮

### 修改文件

1. **src/App.vue**
   - 导入 Scratchpad 组件
   - 添加到模板

## 使用说明

### 基本使用

1. **打开便签板** - 点击 📝 按钮
2. **输入笔记** - 在文本框中输入内容
3. **自动保存** - 输入时自动保存到本地
4. **查看统计** - 顶部显示字数和行数
5. **关闭便签板** - 点击按钮或按 Escape

### 高级功能

1. **浮动模式** - 点击 📌 按钮切换位置
2. **导出** - 点击 💾 按钮导出为文本文件
3. **清空** - 点击 🗑️ 按钮清空内容（需确认）

### 快捷键

- **Ctrl+S** - 手动保存
- **Escape** - 收起便签板

## 已知限制

1. **纯文本** - 只支持纯文本，不支持富文本格式
2. **单文件** - 只有一个便签板，不支持多个
3. **本地存储** - 内容存储在浏览器 localStorage，有大小限制（通常 5-10MB）
4. **无版本历史** - 不支持撤销/重做功能

## 未来改进

1. **Markdown 支持** - 支持 Markdown 渲染
2. **多个便签** - 支持创建多个独立的便签
3. **云同步** - 支持跨设备同步
4. **搜索功能** - 在便签中搜索关键词
5. **标签分类** - 为便签添加标签
6. **提醒功能** - 设置定时提醒
7. **模板** - 保存常用的便签模板

## 总结

便签板功能成功实现了：

✅ **快速记录** - 简单的文本编辑器
✅ **自动保存** - 内容自动保存到本地存储
✅ **统计信息** - 显示字数和行数
✅ **导出功能** - 导出为文本文件
✅ **紧凑界面** - 可折叠的紧凑显示
✅ **浮动模式** - 支持切换位置
✅ **快捷键** - Ctrl+S 保存，Escape 收起
✅ **持久化** - 刷新页面后内容保留

该功能为用户提供了一个便捷的临时记事本，让用户可以在监控 Agent 的同时快速记录想法和待办事项。

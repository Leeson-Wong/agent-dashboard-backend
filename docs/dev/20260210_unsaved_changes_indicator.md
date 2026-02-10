# 未保存更改的视觉提示功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户在编辑 Agent 的备注或标签时，可能会忘记保存内容就切换到其他 Agent 或关闭面板。这导致编辑的内容丢失。通过添加未保存更改的视觉提示，可以提醒用户有内容尚未保存，避免数据丢失。

**目标**:
1. 检测备注和标签的未保存更改
2. 显示 "未保存" 徽章提醒用户
3. 为有未保存更改的输入框添加视觉指示（黄色边框和背景）
4. 添加脉冲动画增强提醒效果
5. 在保存或取消后自动移除提示

---

## 实现方案

### 前端实现

#### 1. 添加未保存更改检测逻辑

**文件**: `src/components/AgentDetailPanel.vue`

**备注未保存检测**:

```typescript
// Check if notes have unsaved changes
const hasUnsavedNotes = computed(() => {
  if (!props.agent) return false
  return editingNotes.value && localNotes.value !== (props.agent.notes || '')
})
```

**标签未保存检测**:

```typescript
// Check if tags have unsaved changes
const hasUnsavedTags = computed(() => {
  if (!props.agent) return false
  if (!editingTags.value) return false

  const originalTags = parsedTags.value.sort()
  const currentTags = [...localTags.value].sort()

  if (originalTags.length !== currentTags.length) return true
  return !originalTags.every((tag, index) => tag === currentTags[index])
})
```

**综合未保存更改检测**:

```typescript
// Check if there are any unsaved changes
const hasUnsavedChanges = computed(() => {
  return hasUnsavedNotes.value || hasUnsavedTags.value
})
```

#### 2. 更新模板添加视觉提示

**备注部分**:

```vue
<!-- Notes Section -->
<div class="section">
  <div class="section-header">
    <h3>备注</h3>
    <div class="section-header-actions">
      <span v-if="hasUnsavedNotes" class="unsaved-badge">未保存</span>
      <button
        v-if="!editingNotes"
        class="edit-btn"
        @click="startEditingNotes"
        title="编辑备注 (按 N 键)"
      >
        ✏️ 编辑
      </button>
    </div>
  </div>

  <div v-if="!editingNotes" class="notes-display" @click="startEditingNotes">
    <div v-if="agent.notes" class="notes-content">{{ agent.notes }}</div>
    <div v-else class="notes-empty">点击添加备注...</div>
  </div>

  <div v-else class="notes-edit">
    <textarea
      ref="notesTextareaRef"
      v-model="localNotes"
      :class="['notes-textarea', { 'has-unsaved-changes': hasUnsavedNotes }]"
      placeholder="输入备注信息... (按 Ctrl+Enter 保存)"
      rows="4"
      @keydown.ctrl.enter="saveNotes"
    ></textarea>
    <div class="notes-actions">
      <button class="notes-btn save" @click="saveNotes">保存</button>
      <button class="notes-btn cancel" @click="cancelEditingNotes">取消</button>
    </div>
  </div>
</div>
```

**标签部分**:

```vue
<!-- Tags Section -->
<div class="section">
  <div class="section-header">
    <h3>标签</h3>
    <div class="section-header-actions">
      <span v-if="hasUnsavedTags" class="unsaved-badge">未保存</span>
      <button
        v-if="!editingTags"
        class="edit-btn"
        @click="startEditingTags"
        title="编辑标签 (按 T 键)"
      >
        ✏️ 编辑
      </button>
    </div>
  </div>

  <div v-if="!editingTags && parsedTags.length > 0" class="tags-display" @click="startEditingTags">
    <span v-for="tag in parsedTags" :key="tag" :class="['tag-item', getTagColor(tag)]">
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
        :class="['tag-input', { 'has-unsaved-changes': hasUnsavedTags }]"
        placeholder="输入标签... (按 Enter 添加)"
        @keydown.enter="addTag"
        @keydown.backspace="handleBackspace"
      />
      <button class="tag-add-btn" @click="addTag">添加</button>
    </div>
    <div class="tags-list">
      <span v-for="(tag, index) in localTags" :key="index" :class="['tag-item', 'editable', getTagColor(tag)]">
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

#### 3. 添加视觉提示样式

```css
/* Section Header Actions */
.section-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Unsaved Badge */
.unsaved-badge {
  padding: 2px 8px;
  background: rgba(245, 158, 11, 0.2);
  border: 1px solid rgba(245, 158, 11, 0.4);
  color: #f59e0b;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  animation: pulse-warning 2s ease-in-out infinite;
}

@keyframes pulse-warning {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* Notes Textarea with Unsaved Changes */
.notes-textarea.has-unsaved-changes {
  border-color: rgba(245, 158, 11, 0.6);
  background: rgba(245, 158, 11, 0.05);
}

.notes-textarea.has-unsaved-changes:focus {
  border-color: rgba(245, 158, 11, 0.8);
  box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.1);
}

/* Tag Input with Unsaved Changes */
.tag-input.has-unsaved-changes {
  border-color: rgba(245, 158, 11, 0.6);
  background: rgba(245, 158, 11, 0.05);
}

.tag-input.has-unsaved-changes:focus {
  border-color: rgba(245, 158, 11, 0.8);
  box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.1);
}
```

---

## 功能说明

### 视觉指示

| 元素 | 未保存状态 | 已保存/未编辑状态 |
|------|-----------|------------------|
| 备注徽章 | 显示 "未保存" 黄色徽章 | 不显示 |
| 标签徽章 | 显示 "未保存" 黄色徽章 | 不显示 |
| 备注输入框 | 黄色边框和淡黄色背景 | 默认边框和背景 |
| 标签输入框 | 黄色边框和淡黄色背景 | 默认边框和背景 |
| 焦点状态 | 橙黄色高亮和阴影 | 蓝色高亮和阴影 |

### 检测逻辑

| 操作 | hasUnsavedNotes | hasUnsavedTags |
|------|-----------------|----------------|
| 开始编辑 | false (内容相同) | false (内容相同) |
| 修改内容 | true | true |
| 保存 | false | false |
| 取消 | false | false |
| 恢复原内容 | false | false |

### 备注检测

```typescript
localNotes.value !== (props.agent.notes || '')
```

- 比较本地编辑的内容与 Agent 的原始备注
- 仅在编辑模式 (`editingNotes = true`) 时检测
- 保存后 `localNotes` 同步到 `agent.notes`

### 标签检测

```typescript
const originalTags = parsedTags.value.sort()
const currentTags = [...localTags.value].sort()

if (originalTags.length !== currentTags.length) return true
return !originalTags.every((tag, index) => tag === currentTags[index])
```

- 比较原始标签和本地标签（排序后）
- 检查数量和内容
- 顺序不重要（排序后比较）

---

## UI 效果

### 未保存状态显示

```
┌─────────────────────────────────────────────────────┐
│ 备注                                     [未保存] [✏️ 编辑] │
│ ┌─────────────────────────────────────────────────┐│
│ │ 这是一个测试备注...                             ││
│ │ ↑ 黄色边框 + 淡黄色背景                       ││
│ │                                                 ││
│ └─────────────────────────────────────────────────┘│
│                                                     │
│ [保存] [取消]                                       │
└─────────────────────────────────────────────────────┘
```

### 已保存状态显示

```
┌─────────────────────────────────────────────────────┐
│ 备注                                              [✏️ 编辑] │
│ ┌─────────────────────────────────────────────────┐│
│ │ 这是一个测试备注...                             ││
│ │ ↑ 默认边框和背景                               ││
│ │                                                 ││
│ └─────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────┘
```

### 徽章样式

```
┌─────────────────────────────────────────┐
│ [未保存]                                  │
│  ↑ 黄色背景 (#f59e0b)                   │
│  ↑ 黄色边框                              │
│  ↑ 脉冲动画 (2秒循环)                    │
│  ↑ 11px 字体                             │
└─────────────────────────────────────────┘
```

### 输入框焦点对比

| 状态 | 边框颜色 | 背景颜色 | 阴影 |
|------|---------|---------|------|
| 未保存焦点 | rgba(245, 158, 11, 0.8) | rgba(245, 158, 11, 0.05) | 0 0 0 2px rgba(245, 158, 11, 0.1) |
| 已保存焦点 | #3b82f6 | rgba(15, 23, 42, 1) | 无 |

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3001`

2. **测试备注未保存提示**:
   - 选择一个 Agent 查看详情
   - 点击 "备注" 旁边的 "✏️ 编辑" 按钮
   - 在文本框中输入一些文字
   - 预期: 标题旁边显示 "未保存" 黄色徽章
   - 预期: 文本框显示黄色边框和淡黄色背景

3. **测试备注保存后清除提示**:
   - 点击 "保存" 按钮
   - 预期: "未保存" 徽章消失
   - 预期: 文本框恢复正常边框和背景

4. **测试备注取消后清除提示**:
   - 编辑备注
   - 点击 "取消" 按钮
   - 预期: "未保存" 徽章消失
   - 预期: 文本框恢复正常边框和背景

5. **测试标签未保存提示**:
   - 点击 "标签" 旁边的 "✏️ 编辑" 按钮
   - 添加一个新标签
   - 预期: 标题旁边显示 "未保存" 黄色徽章
   - 预期: 输入框显示黄色边框和淡黄色背景

6. **测试标签保存后清除提示**:
   - 点击 "保存" 按钮
   - 预期: "未保存" 徽章消失
   - 预期: 输入框恢复正常边框和背景

### 2. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 空白编辑 | 开始编辑但不修改内容 | 不显示 "未保存" 徽章 |
| 恢复原内容 | 修改后恢复到原内容 | "未保存" 徽章消失 |
| 切换 Agent | 有未保存更改时切换 Agent | 提示保留在原状态 |
| 快速编辑 | 快速输入并删除 | 正确检测更改状态 |
| 多次编辑 | 多次编辑和保存 | 每次都正确显示/隐藏 |
| 标签排序 | 添加标签顺序不同 | 不影响未保存检测 |

### 3. 视觉测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| 徽章位置 | 在编辑按钮左侧 | 正确对齐 |
| 徽章颜色 | 黄色 (#f59e0b) | 清晰可见 |
| 徽章动画 | 脉冲动画 | 2 秒循环，自然流畅 |
| 边框颜色 | 橙黄色 (rgba(245, 158, 11, 0.6)) | 明显但不刺眼 |
| 背景颜色 | 淡黄色 (rgba(245, 158, 11, 0.05)) | 轻微着色 |
| 焦点阴影 | 橙黄色光晕 | 清晰的焦点指示 |
| 文字清晰度 | 徽章内文字 | 11px，清晰可读 |

### 4. 交互测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 输入文字 | 在备注中输入 | 实时检测，徽章显示 |
| 删除文字 | 删除备注内容 | 实时检测，徽章显示 |
| 添加标签 | 添加新标签 | 实时检测，徽章显示 |
| 删除标签 | 删除标签 | 实时检测，徽章显示 |
| 保存操作 | 点击保存 | 徽章和边框立即消失 |
| 取消操作 | 点击取消 | 徽章和边框立即消失 |
| Ctrl+Enter | 按快捷键保存 | 徽章和边框立即消失 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 备注未保存徽章显示 | ✅ 通过 | 修改后正确显示 |
| 备注未保存边框显示 | ✅ 通过 | 黄色边框和背景正确 |
| 备注保存后清除 | ✅ 通过 | 保存后立即消失 |
| 备注取消后清除 | ✅ 通过 | 取消后立即消失 |
| 标签未保存徽章显示 | ✅ 通过 | 修改后正确显示 |
| 标签未保存边框显示 | ✅ 通过 | 黄色边框和背景正确 |
| 标签保存后清除 | ✅ 通过 | 保存后立即消失 |
| 标签取消后清除 | ✅ 通过 | 取消后立即消失 |
| 空白编辑不显示 | ✅ 通过 | 无修改时不显示徽章 |
| 恢复原内容清除 | ✅ 通过 | 恢复原值后徽章消失 |
| 标签顺序检测 | ✅ 通过 | 排序后正确比较 |
| 脉冲动画 | ✅ 通过 | 2 秒循环流畅 |
| 焦点样式 | ✅ 通过 | 橙黄色高亮 |
| 实时检测 | ✅ 通过 | 输入时实时响应 |
| 同时编辑 | ✅ 通过 | 备注和标签独立检测 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentDetailPanel.vue**
   - 添加 `hasUnsavedNotes` 计算属性 (line 424-427)
   - 添加 `hasUnsavedTags` 计算属性 (line 486-495)
   - 添加 `hasUnsavedChanges` 计算属性 (line 498-500)
   - 更新备注部分模板 (line 127-163)
   - 更新标签部分模板 (line 165-223)
   - 添加 `.section-header-actions` 样式 (line 1138-1142)
   - 添加 `.unsaved-badge` 样式 (line 1144-1152)
   - 添加 `@keyframes pulse-warning` 动画 (line 1155-1162)
   - 添加 `.notes-textarea.has-unsaved-changes` 样式 (line 1240-1248)
   - 添加 `.tag-input.has-unsaved-changes` 样式 (line 1382-1390)

---

## 技术细节

### Vue 3 计算属性

使用 `computed()` 创建响应式的计算属性:

```typescript
const hasUnsavedNotes = computed(() => {
  if (!props.agent) return false
  return editingNotes.value && localNotes.value !== (props.agent.notes || '')
})
```

**优点**:
- 自动追踪依赖
- 响应式更新
- 性能优化（缓存）

### 条件样式绑定

使用 Vue 的 `:class` 绑定动态类名:

```vue
:class="['notes-textarea', { 'has-unsaved-changes': hasUnsavedNotes }]"
```

当 `hasUnsavedNotes` 为 `true` 时，添加 `has-unsaved-changes` 类。

### CSS 动画

使用 `@keyframes` 创建脉冲动画:

```css
@keyframes pulse-warning {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
```

**特点**:
- 无限循环 (`infinite`)
- 2 秒持续时间
- 缓动函数 (`ease-in-out`)

### 数组比较

标签比较使用排序后逐项比较:

```typescript
const originalTags = parsedTags.value.sort()
const currentTags = [...localTags.value].sort()

if (originalTags.length !== currentTags.length) return true
return !originalTags.every((tag, index) => tag === currentTags[index])
```

**步骤**:
1. 排序原始标签数组
2. 排序本地标签数组（创建副本）
3. 比较长度
4. 逐项比较内容

### 视觉反馈设计

**颜色选择**:
- 警告色: 橙黄色 (#f59e0b)
- 不太刺眼，但足够引起注意
- 与整体暗色调协调

**动画效果**:
- 脉冲: 吸引注意力
- 不太快 (2秒)，避免干扰
- 透明度变化 (1 → 0.7 → 1)

**边框和背景**:
- 边框: rgba(245, 158, 11, 0.6) - 60% 不透明度
- 背景: rgba(245, 158, 11, 0.05) - 5% 不透明度
- 焦点阴影: 0 0 0 2px rgba(245, 158, 11, 0.1)

---

## 优化建议

### 1. 离开面板警告

当用户有未保存更改时尝试切换 Agent 或关闭面板，显示警告对话框:

```typescript
const handleBeforeLeave = () => {
  if (hasUnsavedChanges.value) {
    return '您有未保存的更改，确定要离开吗？'
  }
}

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeLeave)
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeLeave)
})
```

### 2. 自动保存

添加自动保存选项:

```typescript
const autoSaveEnabled = ref(true)
const autoSaveDelay = 2000 // 2 seconds

let autoSaveTimer: number | null = null

watch(localNotes, () => {
  if (!autoSaveEnabled.value) return

  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = window.setTimeout(() => {
    saveNotes()
  }, autoSaveDelay)
})
```

### 3. 草稿持久化

将未保存的更改存储到 localStorage:

```typescript
const saveDraft = () => {
  if (props.agent && hasUnsavedChanges.value) {
    localStorage.setItem(`draft-${props.agent.agentId}`, JSON.stringify({
      notes: localNotes.value,
      tags: localTags.value
    }))
  }
}

const loadDraft = () => {
  if (props.agent) {
    const draft = localStorage.getItem(`draft-${props.agent.agentId}`)
    if (draft) {
      const { notes, tags } = JSON.parse(draft)
      localNotes.value = notes
      localTags.value = tags
    }
  }
}
```

### 4. 更改计数

显示未保存更改的数量:

```vue
<span v-if="unsavedChangesCount > 0" class="unsaved-badge">
  未保存 ({{ unsavedChangesCount }})
</span>
```

```typescript
const unsavedChangesCount = computed(() => {
  let count = 0
  if (hasUnsavedNotes.value) count++
  if (hasUnsavedTags.value) count++
  return count
})
```

### 5. 视觉增强

添加更多视觉提示:

```css
/* 添加小红点指示器 */
.unsaved-indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 添加波浪效果 */
.unsaved-badge::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 4px;
  animation: ripple 1.5s ease-out infinite;
}

@keyframes ripple {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
```

### 6. 快捷键保存

添加更多快捷键:

```typescript
registerShortcut({
  key: 'ctrl+s',
  description: '保存更改',
  handler: (e) => {
    e.preventDefault()
    if (hasUnsavedNotes.value) saveNotes()
    if (hasUnsavedTags.value) saveTags()
  },
})
```

### 7. 批量操作提示

当有多个未保存更改时显示列表:

```vue
<div v-if="hasUnsavedChanges" class="unsaved-summary">
  <div class="unsaved-title">未保存的更改:</div>
  <ul class="unsaved-list">
    <li v-if="hasUnsavedNotes">备注</li>
    <li v-if="hasUnsavedTags">标签</li>
  </ul>
  <button @click="saveAll">全部保存</button>
</div>
```

### 8. 状态持久化

记住用户的自动保存偏好:

```typescript
const userPrefs = ref({
  autoSave: true,
  autoSaveDelay: 2000,
  warnOnLeave: true
})

// Load from localStorage
onMounted(() => {
  const saved = localStorage.getItem('user-prefs')
  if (saved) {
    userPrefs.value = JSON.parse(saved)
  }
})

// Save to localStorage
watch(userPrefs, (prefs) => {
  localStorage.setItem('user-prefs', JSON.stringify(prefs))
}, { deep: true })
```

---

## 已知问题

无

---

## 参考资料

- **Vue 3 Computed**: https://vuejs.org/guide/essentials/computed.html
- **CSS Animations**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Animations
- **localStorage API**: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage
- **beforeunload Event**: https://developer.mozilla.org/en-US/docs/Web/API/Window/beforeunload_event

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

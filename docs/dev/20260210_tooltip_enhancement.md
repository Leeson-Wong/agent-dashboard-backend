# 操作按钮工具提示增强

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

Agent 详情面板的操作按钮缺少详细的工具提示，用户需要猜测按钮的功能或通过点击才知道效果。通过增强工具提示，添加键盘快捷键提示和功能说明，可以显著提升用户体验和操作效率。

**目标**:
1. 为所有操作按钮添加详细的工具提示
2. 在工具提示中显示键盘快捷键
3. 为输入框添加快捷键提示
4. 为可点击区域添加操作提示

---

## 实现方案

### 前端实现

#### 1. 关闭按钮工具提示

**文件**: `src/components/AgentDetailPanel.vue`

```vue
<button class="close-btn" @click="$emit('close')" title="关闭面板 (按 Esc 键)">×</button>
```

#### 2. 复制按钮工具提示

```vue
<!-- Agent ID 复制按钮 -->
<button
  class="copy-mini-btn"
  @click="copyToClipboard(agent.agentId, 'Agent ID')"
  title="复制 Agent ID (按 C 键)"
>📋</button>

<!-- 框架复制按钮 -->
<button
  v-if="agent.framework"
  class="copy-mini-btn"
  @click="copyToClipboard(agent.framework, '框架')"
  title="复制框架信息"
>📋</button>

<!-- 语言复制按钮 -->
<button
  v-if="agent.language"
  class="copy-mini-btn"
  @click="copyToClipboard(agent.language, '语言')"
  title="复制语言信息"
>📋</button>
```

#### 3. 编辑按钮工具提示

```vue
<!-- 编辑备注按钮 -->
<button
  v-if="!editingNotes"
  class="edit-btn"
  @click="startEditingNotes"
  title="编辑备注 (按 N 键)"
>
  ✏️ 编辑
</button>

<!-- 编辑标签按钮 -->
<button
  v-if="!editingTags"
  class="edit-btn"
  @click="startEditingTags"
  title="编辑标签 (按 T 键)"
>
  ✏️ 编辑
</button>
```

#### 4. 操作按钮工具提示

```vue
<!-- Actions -->
<div class="actions">
  <button class="action-btn primary" @click="viewLogs" title="查看 Agent 运行日志">查看日志</button>
  <button class="action-btn" @click="viewTasks" title="查看 Agent 任务执行历史">任务历史</button>
  <button class="action-btn danger" @click="pauseAgent" :title="isPaused ? '恢复 Agent 运行' : '暂停 Agent 运行'">
    {{ isPaused ? '恢复' : '暂停' }}
  </button>
</div>
```

#### 5. 输入框占位符增强

```vue
<!-- 备注输入框 -->
<textarea
  ref="notesTextareaRef"
  v-model="localNotes"
  class="notes-textarea"
  placeholder="输入备注信息... (按 Ctrl+Enter 保存)"
  rows="4"
  @keydown.ctrl.enter="saveNotes"
></textarea>

<!-- 标签输入框 -->
<input
  ref="tagInputRef"
  v-model="newTag"
  class="tag-input"
  placeholder="输入标签... (按 Enter 添加)"
  @keydown.enter="addTag"
  @keydown.backspace="handleBackspace"
/>
```

#### 6. 保存/取消按钮工具提示

```vue
<!-- 备注操作按钮 -->
<div class="notes-actions">
  <button class="notes-btn save" @click="saveNotes" title="保存备注 (按 Ctrl+Enter)">保存</button>
  <button class="notes-btn cancel" @click="cancelEditingNotes" title="取消编辑 (按 Esc)">取消</button>
</div>

<!-- 标签操作按钮 -->
<div class="tags-actions">
  <button class="tags-btn save" @click="saveTags" title="保存标签修改">保存</button>
  <button class="tags-btn cancel" @click="cancelEditingTags" title="取消编辑 (按 Esc)">取消</button>
</div>
```

#### 7. 可点击区域工具提示

```vue
<!-- 备注显示区域 -->
<div v-if="!editingNotes" class="notes-display" @click="startEditingNotes" title="点击编辑备注 (按 N 键)">
  <div v-if="agent.notes" class="notes-content">{{ agent.notes }}</div>
  <div v-else class="notes-empty">点击添加备注...</div>
</div>

<!-- 标签显示区域 -->
<div v-if="!editingTags && parsedTags.length > 0" class="tags-display" @click="startEditingTags" title="点击编辑标签 (按 T 键)">
  <span v-for="tag in parsedTags" :key="tag" :class="['tag-item', getTagColor(tag)]">
    {{ tag }}
  </span>
</div>

<!-- 空标签区域 -->
<div v-else-if="!editingTags" class="tags-empty" @click="startEditingTags" title="点击添加标签 (按 T 键)">
  点击添加标签...
</div>

<!-- Memory 区域 -->
<div class="memory-box clickable" @click="$emit('viewMemory', agent.memoryId)" title="点击查看 Memory 详情">
  <div class="memory-icon">🧠</div>
  <div class="memory-info">
    <div class="memory-id monospace">{{ agent.memoryId.slice(0, 16) }}...</div>
    <div class="memory-hint">点击查看详情 →</div>
  </div>
</div>
```

#### 8. 其他交互元素工具提示

```vue
<!-- 标签添加按钮 -->
<button class="tag-add-btn" @click="addTag" title="添加标签 (按 Enter 键)">添加</button>

<!-- 标签删除按钮 -->
<button class="tag-remove" @click="removeTag(index)" title="删除标签">×</button>
```

---

## 功能说明

### 工具提示映射

| 元素 | 工具提示 | 快捷键 |
|------|---------|--------|
| 关闭按钮 × | 关闭面板 (按 Esc 键) | Esc |
| 编辑备注 | 编辑备注 (按 N 键) | N |
| 编辑标签 | 编辑标签 (按 T 键) | T |
| 复制 Agent ID | 复制 Agent ID (按 C 键) | C |
| 复制框架 | 复制框架信息 | - |
| 复制语言 | 复制语言信息 | - |
| 复制服务器 ID | 复制服务器 ID | - |
| 查看日志 | 查看 Agent 运行日志 | - |
| 任务历史 | 查看 Agent 任务执行历史 | - |
| 暂停/恢复 | 暂停/恢复 Agent 运行 | - |
| 备注保存 | 保存备注 (按 Ctrl+Enter) | Ctrl+Enter |
| 备注取消 | 取消编辑 (按 Esc) | Esc |
| 标签保存 | 保存标签修改 | - |
| 标签取消 | 取消编辑 (按 Esc) | Esc |
| 标签添加 | 添加标签 (按 Enter 键) | Enter |
| 标签删除 | 删除标签 | - |
| 备注区域 | 点击编辑备注 (按 N 键) | N |
| 标签区域 | 点击编辑标签 (按 T 键) | T |
| Memory 区域 | 点击查看 Memory 详情 | - |

### 工具提示规范

| 类型 | 格式 | 示例 |
|------|------|------|
| 有快捷键的按钮 | "操作描述 (按 X 键)" | "编辑备注 (按 N 键)" |
| 仅描述的按钮 | "功能描述" | "查看 Agent 运行日志" |
| 输入框 | "输入提示... (按 X 快捷键)" | "输入标签... (按 Enter 添加)" |
| 可点击区域 | "点击操作描述 (按 X 键)" | "点击编辑备注 (按 N 键)" |

---

## UI 效果

### 工具提示示例

```
┌─────────────────────────────────────────────────────┐
│ [×] ← 悬停显示: "关闭面板 (按 Esc 键)"              │
│                                                     │
│ Agent 详情                           [✏️ 编辑]      │
│                                                  ↑ 悬停显示:     │
│                                        "编辑备注 (按 N 键)"    │
│                                                     │
│ ┌─────────────────────────────────────────────────┐│
│ │ 备注                           [📋]             ││
│ │                                 ↑ 悬停显示:       ││
│ │                    "复制备注信息"                  ││
│ │                                                  ││
│ │ 点击此处可编辑备注...                            ││
│ │ ↑ 悬停整个区域显示: "点击编辑备注 (按 N 键)"      ││
│ └─────────────────────────────────────────────────┘│
│                                                     │
│ ┌─────────────────────────────────────────────────┐│
│ │ 输入备注信息... (按 Ctrl+Enter 保存)             ││
│ │ ↑ 占位符已包含快捷键提示                         ││
│ │                                                  ││
│ │ [保存] [取消]                                    ││
│ │  ↑ 悬停显示:          ↑ 悬停显示:                ││
│ │ "保存备注 (按 Ctrl+Enter)" "取消编辑 (按 Esc)"    ││
│ └─────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────┘
```

### 快捷键提示区域

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│ 快捷键提示:                                         │
│ ┌─────┬─────┬─────┬─────┬─────┐                   │
│ │  C  │  F  │  N  │  T  │ Esc │                   │
│ │复制ID│收藏 │备注 │标签 │关闭 │                   │
│ └─────┴─────┴─────┴─────┴─────┘                   │
│                                                     │
│ 注: 按钮工具提示中也包含这些快捷键信息              │
└─────────────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3001`

2. **测试关闭按钮**:
   - 悬停在右上角的 × 按钮
   - 预期: 显示 "关闭面板 (按 Esc 键)"

3. **测试编辑按钮**:
   - 悬停在备注的 ✏️ 编辑按钮
   - 预期: 显示 "编辑备注 (按 N 键)"
   - 悬停在标签的 ✏️ 编辑按钮
   - 预期: 显示 "编辑标签 (按 T 键)"

4. **测试复制按钮**:
   - 悬停在 Agent ID 旁的 📋 按钮
   - 预期: 显示 "复制 Agent ID (按 C 键)"
   - 悬停在框架旁的 📋 按钮
   - 预期: 显示 "复制框架信息"
   - 悬停在语言旁的 📋 按钮
   - 预期: 显示 "复制语言信息"

5. **测试操作按钮**:
   - 悬停在 "查看日志" 按钮
   - 预期: 显示 "查看 Agent 运行日志"
   - 悬停在 "任务历史" 按钮
   - 预期: 显示 "查看 Agent 任务执行历史"
   - 悬停在 "暂停/恢复" 按钮
   - 预期: 显示 "暂停 Agent 运行" 或 "恢复 Agent 运行"

### 2. 输入框提示测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 备注输入框 | 查看占位符 | "输入备注信息... (按 Ctrl+Enter 保存)" |
| 标签输入框 | 查看占位符 | "输入标签... (按 Enter 添加)" |
| 备注保存 | 悬停保存按钮 | "保存备注 (按 Ctrl+Enter)" |
| 备注取消 | 悬停取消按钮 | "取消编辑 (按 Esc)" |
| 标签保存 | 悬停保存按钮 | "保存标签修改" |
| 标签取消 | 悬停取消按钮 | "取消编辑 (按 Esc)" |

### 3. 可点击区域测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 备注显示区 | 悬停空白区域 | "点击编辑备注 (按 N 键)" |
| 标签显示区 | 悬停标签区域 | "点击编辑标签 (按 T 键)" |
| 空标签区 | 悬停空标签提示 | "点击添加标签 (按 T 键)" |
| Memory 区域 | 悬停 Memory 卡片 | "点击查看 Memory 详情" |

### 4. 标签操作测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 添加按钮 | 悬停 "添加" 按钮 | "添加标签 (按 Enter 键)" |
| 删除按钮 | 悬停标签上的 × | "删除标签" |

### 5. 工具提示行为测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| 显示延迟 | 悬停按钮 | 约 0.5-1 秒后显示 |
| 消失延迟 | 移开鼠标 | 立即消失 |
| 位置正确 | 各个按钮 | 在按钮附近显示 |
| 文字完整 | 长提示 | 完整显示所有文字 |
| 样式一致 | 所有提示 | 浏览器默认样式一致 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 关闭按钮工具提示 | ✅ 通过 | 显示 "关闭面板 (按 Esc 键)" |
| 编辑备注工具提示 | ✅ 通过 | 显示 "编辑备注 (按 N 键)" |
| 编辑标签工具提示 | ✅ 通过 | 显示 "编辑标签 (按 T 键)" |
| 复制 Agent ID 工具提示 | ✅ 通过 | 显示 "复制 Agent ID (按 C 键)" |
| 复制框架工具提示 | ✅ 通过 | 显示 "复制框架信息" |
| 复制语言工具提示 | ✅ 通过 | 显示 "复制语言信息" |
| 查看日志工具提示 | ✅ 通过 | 显示 "查看 Agent 运行日志" |
| 任务历史工具提示 | ✅ 通过 | 显示 "查看 Agent 任务执行历史" |
| 暂停/恢复工具提示 | ✅ 通过 | 根据状态动态显示 |
| 备注输入框占位符 | ✅ 通过 | 包含 Ctrl+Enter 提示 |
| 标签输入框占位符 | ✅ 通过 | 包含 Enter 提示 |
| 备注保存工具提示 | ✅ 通过 | 显示 "保存备注 (按 Ctrl+Enter)" |
| 备注取消工具提示 | ✅ 通过 | 显示 "取消编辑 (按 Esc)" |
| 标签保存工具提示 | ✅ 通过 | 显示 "保存标签修改" |
| 标签取消工具提示 | ✅ 通过 | 显示 "取消编辑 (按 Esc)" |
| 备注区域工具提示 | ✅ 通过 | 显示 "点击编辑备注 (按 N 键)" |
| 标签区域工具提示 | ✅ 通过 | 显示 "点击编辑标签 (按 T 键)" |
| 空标签区域工具提示 | ✅ 通过 | 显示 "点击添加标签 (按 T 键)" |
| Memory 区域工具提示 | ✅ 通过 | 显示 "点击查看 Memory 详情" |
| 标签添加工具提示 | ✅ 通过 | 显示 "添加标签 (按 Enter 键)" |
| 标签删除工具提示 | ✅ 通过 | 显示 "删除标签" |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentDetailPanel.vue**
   - 更新关闭按钮工具提示 (line 25)
   - 更新框架复制按钮工具提示 (line 38)
   - 更新语言复制按钮工具提示 (line 50)
   - 更新 Agent ID 复制按钮工具提示 (line 61)
   - 更新编辑备注按钮工具提示 (line 134)
   - 更新备注区域工具提示 (line 140)
   - 更新备注输入框占位符 (line 150)
   - 更新备注保存/取消按钮工具提示 (line 155-156)
   - 更新编辑标签按钮工具提示 (line 170)
   - 更新标签显示区域工具提示 (line 176)
   - 更新空标签区域工具提示 (line 186)
   - 更新标签输入框占位符 (line 196)
   - 更新标签添加按钮工具提示 (line 200)
   - 更新标签删除按钮工具提示 (line 209)
   - 更新标签保存/取消按钮工具提示 (line 213-214)
   - 更新 Memory 区域工具提示 (line 222)
   - 更新操作按钮工具提示 (line 254-256)

---

## 技术细节

### HTML title 属性

使用浏览器原生的 `title` 属性实现工具提示:

```vue
<button title="关闭面板 (按 Esc 锭)">×</button>
```

**优点**:
- 无需额外 CSS 或 JavaScript
- 浏览器原生支持
- 自动处理显示/隐藏逻辑
- 自动定位

**限制**:
- 样式由浏览器控制，无法自定义
- 显示延迟由浏览器决定
- 不支持富文本内容

### 动态工具提示

使用 Vue 的 `:title` 绑定实现动态内容:

```vue
<button :title="isPaused ? '恢复 Agent 运行' : '暂停 Agent 运行'">
  {{ isPaused ? '恢复' : '暂停' }}
</button>
```

根据状态自动更新工具提示内容。

### 输入框占位符提示

在 `placeholder` 属性中包含快捷键信息:

```vue
<textarea placeholder="输入备注信息... (按 Ctrl+Enter 保存)"></textarea>
<input placeholder="输入标签... (按 Enter 添加)" />
```

用户在输入时就能看到快捷键提示。

### 工具提示设计模式

**按钮工具提示格式**:
- 有快捷键: `操作描述 (按 X 键)`
- 无快捷键: `功能描述`

**可点击区域工具提示格式**:
- `点击操作描述 (按 X 键)`

**输入框占位符格式**:
- `输入提示... (按 X 快捷键)`

---

## 优化建议

### 1. 自定义工具提示组件

使用 Tippy.js 或类似库创建更强大的工具提示:

```vue
<template>
  <button v-tippy="'关闭面板 (按 Esc 键)'">×</button>
</template>

<script setup>
import { directive } from 'vue-tippy'
import 'tippy.js/dist/tippy.css'
</script>
```

### 2. 键盘快捷键显示

在工具提示中使用 `<kbd>` 标签风格:

```vue
<button title="关闭面板 (按 <kbd>Esc</kbd> 键)">×</button>
```

### 3. 快捷键提示徽章

在按钮旁边显示快捷键徽章:

```vue
<button class="action-btn">
  查看日志
  <span class="kbd-badge">?</span>
</button>
```

### 4. 工具提示动画

添加淡入淡出动画:

```css
[title] {
  position: relative;
}

[title]:hover::after {
  content: attr(title);
  position: absolute;
  background: rgba(0, 0, 0, 0.9);
  color: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  white-space: nowrap;
  animation: tooltip-fade-in 0.2s ease-out;
}

@keyframes tooltip-fade-in {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### 5. 工具提示分组

为相关操作提供分组工具提示:

```vue
<div title="备注操作: 点击编辑 (N), Ctrl+Enter 保存, Esc 取消">
  <!-- 备注区域 -->
</div>
```

### 6. 上下文相关提示

根据用户状态显示不同的提示:

```vue
<button :title="getDynamicTooltip()">
  {{ buttonText }}
</button>

<script setup>
const getDynamicTooltip = () => {
  if (isFirstVisit.value) {
    return '编辑备注 (按 N 键) - 首次使用提示'
  }
  return '编辑备注 (按 N 键)'
}
</script>
```

### 7. 快捷键冲突检测

检测并提示快捷键冲突:

```typescript
const shortcuts = {
  close: 'Esc',
  editNotes: 'N',
  editTags: 'T',
  copyId: 'C'
}

const detectConflicts = () => {
  const used = new Set<string>()
  for (const [action, key] of Object.entries(shortcuts)) {
    if (used.has(key)) {
      console.warn(`快捷键冲突: ${key} 被多次使用`)
    }
    used.add(key)
  }
}
```

### 8. 工具提示国际化

支持多语言工具提示:

```vue
<button :title="$t('tooltip.close', { key: 'Esc' })">×</button>
```

```i18n
{
  "tooltip": {
    "close": "关闭面板 (按 {key} 键)",
    "editNotes": "编辑备注 (按 {key} 键)"
  }
}
```

---

## 已知问题

无

---

## 参考资料

- **HTML title Attribute**: https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/title
- **Tooltip Best Practices**: https://www.nngroup.com/articles/tooltip-guidelines/
- **Keyboard Shortcuts UX**: https://alistapart.com/article/neverusewarning/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

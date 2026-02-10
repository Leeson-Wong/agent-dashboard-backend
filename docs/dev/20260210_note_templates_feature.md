# Quick Note Templates 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在使用 Agent 笔记功能时，用户经常需要添加结构化的笔记，如 Bug 报告、任务完成记录、会议记录等。每次手动编写这些结构化内容很耗时。

快速笔记模板功能可以：
- 提供预定义的笔记模板
- 支持自定义模板创建
- 自动填充日期时间等变量
- 提高笔记编写效率

## 需求分析

### 核心需求

1. **内置模板** - 提供常用的笔记模板
2. **模板分类** - 按类别组织模板
3. **变量替换** - 支持日期时间等动态变量
4. **自定义模板** - 允许用户创建自定义模板
5. **模板持久化** - 保存自定义模板到 localStorage

## 实现方案

### 1. 创建 Note Templates Composable

**文件**: `src/composables/useNoteTemplates.ts` (~385 lines)

#### 核心接口

```typescript
export interface NoteTemplate {
  id: string
  name: string
  content: string
  category: string
  isBuiltIn: boolean
}
```

#### 内置模板

| ID | 名称 | 分类 | 内容 |
|----|------|------|------|
| `bug-report` | 🐛 Bug 报告 | 问题 | 问题描述、重现步骤、期望行为、环境信息 |
| `task-completed` | ✅ 任务完成 | 任务 | 完成时间、执行结果、性能数据、备注 |
| `optimization` | ⚡ 性能优化 | 优化 | 问题描述、优化方案、预期收益、实施计划 |
| `investigation` | 🔍 问题调查 | 调查 | 问题描述、调查过程、发现、结论、后续行动 |
| `meeting-notes` | 📝 会议记录 | 记录 | 时间、参与者、议题、讨论要点、决策、行动项 |
| `debug-session` | 🔧 调试会话 | 调试 | 时间、问题、尝试的解决方案、结果、下一步 |

#### 主要功能

```typescript
export function useNoteTemplates() {
  // All templates
  const allTemplates: ComputedRef<NoteTemplate[]>

  // Categories
  const categories: ComputedRef<string[]>

  // Templates grouped by category
  const templatesByCategory: ComputedRef<Record<string, NoteTemplate[]>>

  // Built-in templates
  const builtinTemplates: ComputedRef<NoteTemplate[]>

  // Custom templates
  const customTemplatesList: ComputedRef<NoteTemplate[]>

  // Methods
  const create: (template: Omit<NoteTemplate, 'id' | 'isBuiltIn'>) => NoteTemplate
  const update: (id: string, updates: Partial<NoteTemplate>) => boolean
  const remove: (id: string) => boolean
  const apply: (template: NoteTemplate) => string
  const getTemplateById: (id: string) => NoteTemplate | undefined
  const getTemplatesByCategory: (category: string) => NoteTemplate[]
}
```

#### 变量替换

```typescript
export const applyTemplate = (template: NoteTemplate): string => {
  let content = template.content

  const now = new Date()
  const datetime = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })

  content = content.replace(/\{\{datetime\}\}/g, datetime)
  content = content.replace(/\{\{date\}\}/g, date)
  content = content.replace(/\{\{time\}\}/g, time)

  return content
}
```

#### 支持的变量

| 变量 | 说明 | 示例 |
|------|------|------|
| `{{datetime}}` | 当前日期时间 | 2026/02/10 14:30 |
| `{{date}}` | 当前日期 | 2026/02/10 |
| `{{time}}` | 当前时间 | 14:30 |

### 2. 集成到 Agent Notes 组件

**文件**: `src/components/AgentNotes.vue`

#### 添加模板选择器

```vue
<div class="template-selector">
  <select
    v-model="selectedTemplateId"
    class="template-select"
    @change="applyTemplate"
  >
    <option value="">选择模板...</option>
    <optgroup
      v-for="(templates, category) in templatesByCategory"
      :key="category"
      :label="category"
    >
      <option
        v-for="template in templates"
        :key="template.id"
        :value="template.id"
      >
        {{ template.name }}
      </option>
    </optgroup>
  </select>
</div>
```

#### 添加模板逻辑

```typescript
// Note templates
const {
  templatesByCategory,
  getTemplateById,
  apply: applyTemplateContent
} = useNoteTemplates()
const selectedTemplateId = ref<string>('')

const applyTemplate = (): void => {
  if (!selectedTemplateId.value) return

  const template = getTemplateById(selectedTemplateId.value)
  if (template) {
    const content = applyTemplateContent(template)
    // Append or replace based on current note content
    if (editingNote.value.trim()) {
      editingNote.value = editingNote.value + '\n\n' + content
    } else {
      editingNote.value = content
    }
  }

  // Reset template selection
  selectedTemplateId.value = ''
}
```

## 功能特性

### 模板分类

| 分类 | 内置模板数 | 说明 |
|------|-----------|------|
| **问题** | 1 | Bug 报告 |
| **任务** | 1 | 任务完成记录 |
| **优化** | 1 | 性能优化建议 |
| **调查** | 1 | 问题调查记录 |
| **记录** | 1 | 会议记录 |
| **调试** | 1 | 调试会话记录 |

### 模板应用行为

- **空白笔记** - 直接替换为模板内容
- **已有内容** - 在现有内容后追加模板内容
- **变量替换** - 自动替换 `{{datetime}}` 等变量

### 自定义模板

用户可以创建自定义模板：
- 保存到 localStorage
- 可以编辑和删除
- 不能修改内置模板

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (271 modules)

### 功能测试

1. **内置模板** ✅
   - 模板列表正确显示
   - 分类正确组织
   - 内容正确加载

2. **模板应用** ✅
   - 空白笔记正确替换
   - 已有内容正确追加
   - 变量正确替换

3. **自定义模板** ✅
   - 可以创建自定义模板
   - 保存到 localStorage
   - 刷新后保持

4. **UI 交互** ✅
   - 下拉选择器样式正确
   - 选项分组清晰
   - 选择后自动应用

## 使用说明

### 使用模板

1. 打开 Agent 笔记编辑模式
2. 点击"选择模板..."下拉框
3. 选择需要的模板
4. 模板内容自动填充到编辑器
5. 根据需要编辑内容
6. 保存笔记

### 创建自定义模板

```typescript
import { useNoteTemplates } from './composables/useNoteTemplates'

const { create } = useNoteTemplates()

create({
  name: '我的模板',
  content: '## 我的模板\n\n创建时间: {{datetime}}\n\n内容...',
  category: '自定义'
})
```

### 模板变量

在模板内容中使用以下变量：

```
{{datetime}} - 完整日期时间
{{date}}     - 仅日期
{{time}}     - 仅时间
```

## 文件变更

### 新增文件

1. **src/composables/useNoteTemplates.ts** (~385 lines)
   - NoteTemplate 接口定义
   - 6 个内置模板
   - 模板管理功能
   - useNoteTemplates composable

### 修改文件

1. **src/components/AgentNotes.vue**
   - 导入 useNoteTemplates
   - 添加模板选择器 UI
   - 添加模板应用逻辑
   - 添加模板选择器样式

## 未来扩展

### 潜在增强

1. **模板编辑器** - 可视化编辑自定义模板
2. **模板导入导出** - 支持模板的导入导出
3. **更多变量** - 支持 Agent ID、角色等变量
4. **模板预览** - 在选择前预览模板内容
5. **模板搜索** - 快速搜索模板
6. **团队共享** - 支持团队共享模板库

## 总结

Quick Note Templates 功能成功实现了：

✅ **内置模板** - 提供常用的笔记模板
✅ **模板分类** - 按类别组织模板
✅ **变量替换** - 支持日期时间等动态变量
✅ **自定义模板** - 允许用户创建自定义模板
✅ **模板持久化** - 保存自定义模板到 localStorage

该功能显著提高了笔记编写效率，用户可以快速创建结构化的笔记内容。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

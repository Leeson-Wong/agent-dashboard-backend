# Agent 列表标签显示功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户添加的 Agent 标签只能在详情面板中查看，需要打开每个 Agent 才能看到标签，不够直观。在 Agent 列表中直接显示标签可以让用户快速识别和筛选 Agent。

**目标**:
1. 在 Agent 列表项中显示彩色标签
2. 标签颜色与详情面板保持一致
3. 仅在有标签时显示标签区域
4. 支持列表和网格两种视图模式

---

## 实现方案

### 前端实现

#### 1. 更新列表项模板

**文件**: `src/components/AgentListPanel.vue`

在 agent-info 中添加标签显示区域:

```vue
<div class="agent-info">
  <div class="agent-name">{{ agent.role || agent.agentId }}</div>
  <div class="agent-meta">
    <span class="framework">{{ agent.framework }}</span>
    <span class="language">{{ agent.language }}</span>
  </div>
  <div v-if="agent.currentActivity" class="agent-activity">
    {{ agent.currentActivity }}
  </div>
  <div v-if="agent.currentTool" class="agent-tool">
    工具: {{ agent.currentTool }}
  </div>
  <div v-if="parsedAgentTags(agent).length > 0" class="agent-tags">
    <span
      v-for="tag in parsedAgentTags(agent)"
      :key="tag"
      :class="['agent-tag', getAgentTagColor(tag)]"
    >
      {{ tag }}
    </span>
  </div>
</div>
```

#### 2. 添加标签解析函数

```typescript
// Parse agent tags from JSON string
const parsedAgentTags = (agent: AgentState): string[] => {
  if (!agent.tags) return []
  try {
    return JSON.parse(agent.tags)
  } catch {
    return []
  }
}
```

#### 3. 添加颜色计算函数

```typescript
// Get color for agent tag
const getAgentTagColor = (tag: string): string => {
  const colors = ['blue', 'green', 'purple', 'orange', 'pink', 'cyan']
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}
```

#### 4. 添加样式

```css
/* Agent tags */
.agent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.agent-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 500;
  white-space: nowrap;
}

.agent-tag.blue {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

.agent-tag.green {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

/* ... other colors ... */
```

---

## 功能说明

### 标签显示规则

| 条件 | 显示 |
|------|------|
| 有标签 | 显示彩色标签列表 |
| 无标签 | 不显示标签区域 |
| 标签数量 | 全部显示，自动换行 |

### 标签颜色

与详情面板使用相同的颜色算法，确保一致性:

| 颜色 | 示例标签 |
|------|----------|
| 蓝色 #60a5fa | 生产 |
| 绿色 #4ade80 | 测试 |
| 紫色 #c084fc | 重要 |
| 橙色 #fb923c | 警告 |
| 粉色 #f472b6 | 个人 |
| 青色 #22d3ee | 开发 |

---

## UI 效果

### 列表视图

```
┌─────────────────────────────────────────────────────────────────┐
│ ● ⭐  专业小说作家                          [⋯]  3分钟前       │
│    CrewAI  Python                                                   │
│    正在撰写小说第3章                                                 │
│    [重要] [生产] [CrewAI]                                          │
│    ↓      ↓       ↓                                               │
│   紫色   蓝色    绿色                                              │
│                                                                   │
│ ●     数据分析师                            [⋯]  1小时前       │
│    LangChain  Python                                                 │
│    [测试] [开发]                                                    │
│    ↓      ↓                                                         │
│   绿色   紫色                                                       │
└─────────────────────────────────────────────────────────────────┘
```

### 网格视图

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ ● ⭐            │  │ ●               │  │ ●               │
│ 专业小说作家     │  │ 数据分析师      │  │ 测试助手        │
│ CrewAI Python   │  │ LangChain Py    │  │ AutoGen JS      │
│                 │  │                 │  │                 │
│ [重要] [生产]   │  │ [测试]          │  │                 │
│                 │  │                 │  │                 │
│ 3分钟前          │  │ 1小时前         │  │ 2天前           │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **查看无标签 Agent**:
   - 预期: Agent 列表项中不显示标签区域

3. **查看有标签 Agent**:
   - 预期: Agent 列表项中显示彩色标签
   - 预期: 标签颜色与详情面板一致

4. **多个标签显示**:
   - 预期: 多个标签横向排列
   - 预期: 标签过多时自动换行

5. **切换视图模式**:
   - 切换到网格视图
   - 预期: 标签正常显示

6. **列表滚动**:
   - 滚动列表查看更多 Agent
   - 预期: 标签显示正常，无错位

### 2. 边界测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 空标签 | 无标签的 Agent | 不显示标签区域 |
| 单个标签 | 只有一个标签 | 正常显示 |
| 多个标签 | 5+ 个标签 | 正常换行 |
| 长标签名 | 标签名很长 | 正常显示 |
| 特殊字符 | 标签含特殊字符 | 正常显示 |
| JSON 错误 | 无效的 tags JSON | 不显示标签（容错） |
| 颜色一致性 | 与详情面板对比 | 颜色完全一致 |

### 3. 视觉测试

| 测试项 | 检查点 | 预期结果 |
|--------|--------|----------|
| 标签大小 | 与文本协调 | 10px 字体合适 |
| 标签间距 | 紧凑不拥挤 | 4px gap 合适 |
| 圆角大小 | 视觉舒适 | 8px 合适 |
| 背景透明度 | 不遮挡内容 | 0.2 透明度合适 |
| 文字颜色 | 与背景对比 | 可读性好 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 标签显示 | ✅ 通过 | 有标签时正确显示 |
| 空标签处理 | ✅ 通过 | 无标签时不显示区域 |
| 标签解析 | ✅ 通过 | JSON 正确解析 |
| 错误处理 | ✅ 通过 | JSON 错误时返回空数组 |
| 颜色计算 | ✅ 通过 | 颜色与详情面板一致 |
| 标签换行 | ✅ 通过 | 多标签正确换行 |
| 列表视图 | ✅ 通过 | 标签正常显示 |
| 网格视图 | ✅ 通过 | 标签正常显示 |
| 样式协调 | ✅ 通过 | 与列表项协调 |
| 文本截断 | ✅ 通过 | 长标签名完整显示 |

---

## 文件清单

### 修改的文件

#### 前端
1. **src/components/AgentListPanel.vue**
   - 添加标签显示区域到 agent-item (line 200-208)
   - 添加 `parsedAgentTags` 函数 (line 658-665)
   - 添加 `getAgentTagColor` 函数 (line 667-675)
   - 添加 `agent-tags` 样式 (line 1093-1098)
   - 添加 `agent-tag` 基础样式 (line 1100-1107)
   - 添加 6 种颜色变体样式 (line 1109-1137)

---

## 技术细节

### 条件渲染

使用 `v-if` 仅在有标签时显示标签区域:

```vue
<div v-if="parsedAgentTags(agent).length > 0" class="agent-tags">
```

这避免了空标签区域占用空间。

### 函数式标签解析

使用函数而不是 computed 属性，因为需要在模板中传递不同的 agent:

```typescript
const parsedAgentTags = (agent: AgentState): string[] => {
  if (!agent.tags) return []
  try {
    return JSON.parse(agent.tags)
  } catch {
    return []
  }
}
```

### 颜色哈希算法

使用字符串哈希算法确保相同标签产生相同颜色:

```typescript
let hash = 0
for (let i = 0; i < tag.length; i++) {
  hash = tag.charCodeAt(i) + ((hash << 5) - hash)
}
return colors[Math.abs(hash) % colors.length]
```

### Flexbox 换行

使用 `flex-wrap: wrap` 实现标签自动换行:

```css
.agent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
```

### 小尺寸标签

列表中的标签比详情面板中更小，适合紧凑显示:

```css
.agent-tag {
  padding: 2px 6px;  /* 详情面板: 4px 10px */
  font-size: 10px;   /* 详情面板: 12px */
  border-radius: 8px; /* 详情面板: 12px */
}
```

---

## 优化建议

### 1. 标签筛选

添加按标签筛选 Agent 的功能:

```typescript
const tagFilter = ref('')

const filteredByTag = computed(() => {
  if (!tagFilter.value) return filteredAgents

  return filteredAgents.filter(agent => {
    const tags = parsedAgentTags(agent)
    return tags.includes(tagFilter.value)
  })
})
```

### 2. 标签统计

显示每个标签的 Agent 数量:

```typescript
const tagStats = computed(() => {
  const stats: Record<string, number> = {}
  agents.forEach(agent => {
    const tags = parsedAgentTags(agent)
    tags.forEach(tag => {
      stats[tag] = (stats[tag] || 0) + 1
    })
  })
  return stats
})
```

### 3. 标签云视图

添加标签云视图显示所有标签:

```vue
<div class="tag-cloud">
  <span
    v-for="(count, tag) in tagStats"
    :key="tag"
    :class="['tag-cloud-item', getAgentTagColor(tag)]"
    :style="{ fontSize: 12 + count * 2 + 'px' }"
  >
    {{ tag }} ({{ count }})
  </span>
</div>
```

### 4. 快速标签筛选

点击标签快速筛选:

```typescript
const filterByTag = (tag: string): void => {
  tagFilter.value = tagFilter.value === tag ? '' : tag
}
```

### 5. 标签编辑

在列表中直接编辑标签:

```vue
<button
  class="edit-tags-btn"
  @click="startEditingTags(agent)"
  title="编辑标签"
>
  ✏️
</button>
```

### 6. 标签拖拽排序

支持拖拽标签调整优先级:

```typescript
import { useDraggable } from '@vueuse/core'

const { isDragging } = useDraggable(tagElement, {
  onDragEnd: (position) => {
    // 更新标签顺序
  }
})
```

---

## 已知问题

无

---

## 参考资料

- **JSON.parse**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/JSON/parse
- **String charCodeAt**: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/charCodeAt
- **Flexbox flex-wrap**: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flexible_Box_Layout

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

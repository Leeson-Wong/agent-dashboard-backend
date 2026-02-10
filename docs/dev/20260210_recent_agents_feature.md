# Recent Agents 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，用户经常需要在多个 Agent 之间切换查看。最近访问功能可以：
- 快速返回刚才查看的 Agent
- 记住工作流程中的常用 Agent
- 提高工作效率

## 需求分析

### 核心需求

1. **记录访问历史** - 记录最近查看的 Agent
2. **限制数量** - 最多保存 10 个最近访问
3. **持久化** - localStorage 保存访问历史
4. **快速过滤** - 一键过滤只显示最近访问的 Agent
5. **自动去重** - 已存在的 Agent 移到前面

## 实现方案

### 1. 创建 Recent Agents Composable

**文件**: `src/composables/useRecentAgents.ts`

#### 核心功能

```typescript
const MAX_RECENT = 10

export function useRecentAgents() {
  const recentAgents = ref<string[]>([])

  const addRecent = (agentId: string): void => {
    // Remove if already exists
    const index = recentAgents.value.indexOf(agentId)
    if (index !== -1) {
      recentAgents.value.splice(index, 1)
    }

    // Add to front
    recentAgents.value.unshift(agentId)

    // Keep only MAX_RECENT
    if (recentAgents.value.length > MAX_RECENT) {
      recentAgents.value = recentAgents.value.slice(0, MAX_RECENT)
    }

    saveRecentAgents()
  }

  const isRecent = (agentId: string): boolean => {
    return recentAgents.value.includes(agentId)
  }
}
```

### 2. 集成到主应用

**文件**: `src/App.vue`

#### 添加到 selectAgent 方法

```typescript
import { useRecentAgents } from './composables/useRecentAgents'

const { addRecent: addToRecent } = useRecentAgents()

const selectAgent = (agentId: string): void => {
  selectedAgentId.value = agentId
  if (scene) {
    scene.setFocusedZone(agentId)
  }
  // Add to recent agents
  addToRecent(agentId)
}
```

### 3. 添加到快速过滤

**文件**: `src/components/QuickFilterButtons.vue`

#### 添加"最近访问"过滤器

```vue
<script setup lang="ts">
import { useRecentAgents } from '../composables/useRecentAgents'

const { recentAgents, isRecent } = useRecentAgents()

const filters = computed<QuickFilter[]>(() => {
  const agents = props.agents

  return [
    // ... other filters
    {
      id: 'recent',
      label: '最近访问',
      icon: '🕐',
      description: '只显示最近访问的 Agent',
      count: agents.filter(a => isRecent(a.agentId)).length,
      predicate: (agent) => isRecent(agent.agentId)
    },
    // ... other filters
  ]
})
</script>
```

**文件**: `src/composables/useQuickFilter.ts`

#### 更新 QuickFilterType

```typescript
export type QuickFilterType = 'all' | 'favorites' | 'recent' | 'online' | 'offline' | 'thinking' | 'errors' | 'paused'
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (265 modules)

### 功能测试

1. **记录访问** ✅
   - 点击 Agent 自动记录
   - 最多保存 10 个

2. **快速过滤** ✅
   - 🕐 按钮显示最近访问的 Agent
   - 计数正确显示

3. **持久化** ✅
   - 刷新页面后保持

## 使用说明

### 使用方法

1. 点击任意 Agent 查看详情
2. Agent 自动添加到"最近访问"
3. 点击快速过滤栏的 🕐 按钮查看最近访问的 Agent

### 过滤按钮顺序

从左到右：
1. 📋 全部
2. 🕐 最近访问
3. ⭐ 收藏
4. 🟢 在线
5. ⚫ 离线
6. 🔄 运行中
7. ❌ 有错误
8. ⏸️ 已暂停

## 文件变更

### 新增文件

1. **src/composables/useRecentAgents.ts** (~90 行)
   - 最近访问状态管理
   - localStorage 持久化

### 修改文件

1. **src/App.vue**
   - 导入 useRecentAgents
   - 在 selectAgent 中添加记录逻辑

2. **src/components/QuickFilterButtons.vue**
   - 导入 useRecentAgents
   - 添加"最近访问"过滤器

3. **src/composables/useQuickFilter.ts**
   - 添加 'recent' 到 QuickFilterType
   - 添加 'recent' 过滤器定义

## 总结

Recent Agents 功能成功实现了：

✅ **记录访问历史** - 记录最近查看的 Agent
✅ **限制数量** - 最多保存 10 个
✅ **持久化** - localStorage 保存
✅ **快速过滤** - 一键过滤
✅ **自动去重** - 已存在的移到前面
✅ **组合过滤** - 与其他过滤协同

该功能为用户提供了快速返回最近查看 Agent 的便捷方式。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

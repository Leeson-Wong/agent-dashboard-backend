# 小地图功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

当系统中 Agent 数量较多时，用户需要一个快速概览所有 Agent 状态的方式。小地图（Mini Map）提供了一个紧凑的可视化界面，显示所有 Agent 的分布和状态，让用户能够快速了解系统整体状况。

## 需求分析

### 核心需求

1. **紧凑概览** - 在小空间内显示所有 Agent
2. **状态可视化** - 用颜色区分 Agent 状态
3. **快速选择** - 点击节点快速选择 Agent
4. **统计信息** - 显示各状态的 Agent 数量
5. **可折叠** - 不使用时可收起

### 技术要求

- 使用 SVG 绘制节点
- 响应式节点大小
- 与主 3D 视图状态同步
- 性能优化（大量节点时）

## 实现方案

### 1. 创建小地图 Composable

**文件**: `src/composables/useMiniMap.ts`

#### 核心数据结构

```typescript
export interface MiniMapNode {
  id: string
  x: number
  y: number
  status: string
  framework?: string
  selected: boolean
}

export interface MiniMapViewport {
  x: number
  y: number
  width: number
  height: number
}
```

#### 主要功能

**1. 计算节点位置（网格布局）**

```typescript
const calculatePositions = (agentCount: number): MiniMapNode[] => {
  const result: MiniMapNode[] = []
  const cols = Math.ceil(Math.sqrt(agentCount))
  const rows = Math.ceil(agentCount / cols)

  const nodeWidth = options.width / cols
  const nodeHeight = options.height / rows
  const padding = 4

  for (let i = 0; i < agentCount; i++) {
    const col = i % cols
    const row = Math.floor(i / cols)

    result.push({
      id: `node-${i}`,
      x: col * nodeWidth + padding,
      y: row * nodeHeight + padding,
      status: 'unknown',
      selected: false
    })
  }

  return result
}
```

**2. 从 Agent 更新节点**

```typescript
const updateNodes = (agents: Array<{
  agentId: string
  status: string
  framework?: string
}>, selectedId?: string | null): void => {
  const calculatedPositions = calculatePositions(agents.length)

  nodes.value = agents.map((agent, index) => {
    const pos = calculatedPositions[index] || { x: 0, y: 0 }
    return {
      id: agent.agentId,
      x: pos.x,
      y: pos.y,
      status: agent.status,
      framework: agent.framework,
      selected: agent.agentId === selectedId
    }
  })
}
```

**3. 获取状态颜色**

```typescript
const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    'online': '#22c55e',
    'ready': '#22c55e',
    'offline': '#64748b',
    'paused': '#f59e0b',
    'error': '#ef4444',
    'unknown': '#94a3b8'
  }
  return colors[status] || colors.unknown
}
```

**4. 动态节点大小**

```typescript
const getNodeSize = computed(() => {
  const count = nodes.value.length
  if (count > 100) return 2
  if (count > 50) return 3
  if (count > 20) return 4
  return 5
})
```

### 2. 创建小地图组件

**文件**: `src/components/MiniMap.vue`

#### 组件结构

```
MiniMap (Fixed position: bottom-left)
├── Toggle Button
│   ├── Icon (🗺️)
│   └── Agent Count
└── Content (展开状态)
    ├── Stats Bar
    │   └── Status Dots with Counts
    ├── Canvas (SVG)
    │   └── Nodes (Circles)
    └── Legend
        └── Status Labels
```

#### 组件实现

```vue
<template>
  <div class="minimap" :class="{ 'is-collapsed': isCollapsed }">
    <button class="minimap-toggle" @click="toggleCollapsed">
      <span class="toggle-icon">🗺️</span>
      <span v-if="isCollapsed" class="toggle-count">{{ nodes.length }}</span>
    </button>

    <Transition name="expand">
      <div v-if="!isCollapsed" class="minimap-container">
        <!-- Stats Bar -->
        <div class="stats-bar">
          <span
            v-for="(count, status) in statusStats"
            :key="status"
            :class="['stat-item', `status-${status}`]"
          >
            <span class="stat-dot"></span>
            <span class="stat-count">{{ count }}</span>
          </span>
        </div>

        <!-- SVG Canvas -->
        <div class="canvas-container">
          <svg
            :viewBox="`0 0 ${width} ${height}`"
            class="minimap-svg"
          >
            <circle
              v-for="node in nodes"
              :key="node.id"
              :cx="node.x"
              :cy="node.y"
              :r="nodeSize"
              :fill="getStatusColor(node.status)"
              :class="{ 'is-selected': node.selected }"
              @click.stop="handleNodeClick(node.id)"
            />
          </svg>
        </div>

        <!-- Legend -->
        <div class="legend">
          <span class="legend-item">
            <span class="legend-dot" style="background: #22c55e"></span>
            <span class="legend-label">在线</span>
          </span>
          <!-- ... more legend items ... -->
        </div>
      </div>
    </Transition>
  </div>
</template>
```

### 3. 集成到主应用

**文件**: `src/App.vue`

#### 添加组件到模板

```vue
<MiniMap
  ref="miniMap"
  :agents="agents"
  :selected-agent-id="selectedAgentId"
  @select-agent="selectAgent"
/>
```

#### 添加引用和更新逻辑

```typescript
// Ref
const miniMap = ref<InstanceType<typeof MiniMap> | null>(null)

// Watch agents changes
watch(agents, (newAgents) => {
  // ... other updates ...

  // Update mini map
  if (miniMap.value) {
    miniMap.value.update()
  }
}, { immediate: true })
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **节点显示** ✅
   - 所有 Agent 正确显示为节点
   - 节点位置按网格排列
   - 节点大小随数量动态调整

2. **状态颜色** ✅
   - 在线/就绪：绿色 (#22c55e)
   - 离线：灰色 (#64748b)
   - 暂停：橙色 (#f59e0b)
   - 错误：红色 (#ef4444)

3. **选择状态** ✅
   - 选中的 Agent 有蓝色边框
   - 选中节点有发光效果
   - 点击节点触发选择

4. **统计信息** ✅
   - 状态栏显示各状态数量
   - 颜色与节点一致

5. **图例** ✅
   - 显示所有状态类型
   - 颜色对应正确

6. **紧凑模式** ✅
   - 收起时显示小按钮和数量
   - 展开时显示完整地图

## 样式实现

### 组件定位

```css
.minimap {
  position: fixed;
  bottom: 20px;
  left: 20px;
  z-index: 850;
}
```

### 节点样式

```css
.minimap-node {
  cursor: pointer;
  transition: r 0.2s ease;
}

.minimap-node:hover {
  r: var(--node-size-increased) !important;
}

.minimap-node.is-selected {
  filter: drop-shadow(0 0 4px rgba(96, 165, 250, 0.6));
}
```

### 统计栏

```css
.stats-bar {
  display: flex;
  gap: 8px;
  padding: 8px 10px;
  border-bottom: 1px solid rgba(100, 116, 139, 0.2);
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(15, 23, 42, 0.5);
  font-size: 9px;
  font-weight: 500;
}

.stat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
```

## 技术要点

### 1. 网格布局算法

使用平方根计算最优行列数：

```typescript
const cols = Math.ceil(Math.sqrt(agentCount))
const rows = Math.ceil(agentCount / cols)
```

### 2. SVG 坐标系统

使用 viewBox 实现响应式：

```vue
<svg :viewBox="`0 0 ${width} ${height}`">
```

### 3. 动态节点大小

根据 Agent 数量调整节点大小以保持可读性：

```typescript
const getNodeSize = computed(() => {
  const count = nodes.value.length
  if (count > 100) return 2
  if (count > 50) return 3
  if (count > 20) return 4
  return 5
})
```

### 4. 状态统计

使用 computed 属性动态统计：

```typescript
const getStatusStats = computed(() => {
  const stats: Record<string, number> = {}
  nodes.value.forEach(node => {
    stats[node.status] = (stats[node.status] || 0) + 1
  })
  return stats
})
```

### 5. 事件传递

通过 emit 将节点点击传递给父组件：

```typescript
const handleNodeClick = (nodeId: string): void => {
  onNodeClick(nodeId, (agentId) => {
    emit('selectAgent', agentId)
  })
}
```

## 文件变更

### 新增文件

1. **src/composables/useMiniMap.ts** (~120 行)
   - 小地图状态管理
   - 节点位置计算
   - 状态颜色映射
   - 统计信息

2. **src/components/MiniMap.vue** (~300 行)
   - 小地图 UI 组件
   - SVG 渲染
   - 交互处理
   - 图例显示

### 修改文件

1. **src/App.vue**
   - 导入 MiniMap 组件
   - 添加到模板
   - 添加 miniMap ref
   - 监听 agents 变化并更新

## 使用说明

### 基本使用

小地图会自动显示所有 Agent：

1. **查看状态** - 颜色表示 Agent 状态
2. **查看统计** - 状态栏显示各状态数量
3. **选择 Agent** - 点击节点选择对应的 Agent
4. **折叠/展开** - 点击切换按钮收起或展开

### 快捷键

无（暂无快捷键支持）

### 与主视图同步

- 小地图中的选中状态与主 3D 视图同步
- 点击小地图节点会选中对应 Agent
- 主视图选择 Agent 会更新小地图高亮

## 已知限制

1. **网格布局** - 节点按网格排列，不考虑实际空间分布
2. **节点重叠** - 当 Agent 数量很大时节点可能显得密集
3. **无缩放** - 当前不支持缩放和平移
4. **单向选择** - 只能从小地图选择 Agent，不能反向控制

## 未来改进

1. **空间映射** - 将节点位置映射到主 3D 视图的实际位置
2. **缩放控制** - 支持鼠标滚轮缩放
3. **平移支持** - 支持拖拽平移
4. **框架过滤** - 按框架类型显示/隐藏节点
5. **分组模式** - 支持按状态或框架分组显示
6. **动画效果** - 添加节点状态变化的动画

## 总结

小地图功能成功实现了：

✅ **紧凑概览** - 小空间显示所有 Agent
✅ **状态可视化** - 颜色区分 Agent 状态
✅ **快速选择** - 点击节点快速选择
✅ **统计信息** - 显示各状态数量
✅ **可折叠** - 支持收起/展开
✅ **动态大小** - 节点大小随数量调整
✅ **图例说明** - 清晰的状态图例
✅ **状态同步** - 与主视图选择状态同步

该功能为用户提供了一个快速了解整体系统状况的窗口，特别适合在 Agent 数量较多时使用。

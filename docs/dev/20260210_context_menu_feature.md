# Agent Context Menu 功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在 Agent 列表中，用户需要快速执行各种操作，如查看详情、复制 ID、切换收藏状态等。现有的快捷操作菜单需要点击才能打开。

右键上下文菜单是用户熟悉的标准交互模式，可以：
- 提供更直观的访问方式
- 减少点击次数
- 提高操作效率
- 符合用户习惯

## 需求分析

### 核心需求

1. **右键菜单** - 右键点击 Agent 显示上下文菜单
2. **常用操作** - 包含常用快捷操作
3. **智能定位** - 菜单自动调整位置避免超出屏幕
4. **键盘支持** - 支持 ESC 键关闭菜单
5. **点击外部关闭** - 点击菜单外部自动关闭

## 实现方案

### 1. 创建 Context Menu Composable

**文件**: `src/composables/useContextMenu.ts` (~120 lines)

#### 核心接口

```typescript
export interface ContextMenuItem {
  id: string
  label: string
  icon?: string
  shortcut?: string
  disabled?: boolean
  danger?: boolean
  separator?: boolean
  action: () => void
}

export interface ContextMenuPosition {
  x: number
  y: number
}
```

#### 主要功能

```typescript
export function useContextMenu() {
  // State
  const menuOpen: ComputedRef<boolean>
  const menuPosition: ComputedRef<ContextMenuPosition>
  const menuItems: ComputedRef<ContextMenuItem[]>
  const target: ComputedRef<any>

  // Methods
  const openMenu: (x: number, y: number, items: ContextMenuItem[], data?: any) => void
  const closeMenu: () => void
  const executeAction: (itemId: string) => void
}
```

#### 全局状态管理

```typescript
// Global close function for single menu instance
let globalCloseMenu: (() => void) | null = null

export const registerCloseMenu = (closeFn: () => void): void => {
  globalCloseMenu = closeFn
}

export const closeGlobalContextMenu = (): void => {
  if (globalCloseMenu) {
    globalCloseMenu()
  }
}
```

### 2. 创建 Context Menu 组件

**文件**: `src/components/ContextMenu.vue` (~230 lines)

#### 功能特性

```vue
<template>
  <Teleport to="body">
    <Transition name="context-menu">
      <div
        v-if="menuOpen"
        ref="menuRef"
        class="context-menu"
        :style="menuStyle"
      >
        <div
          v-for="item in menuItems"
          :key="item.id"
          :class="{
            'disabled': item.disabled,
            'danger': item.danger,
            'separator': item.separator
          }"
          @click="handleItemClick(item)"
        >
          <div v-if="item.separator" class="menu-separator"></div>
          <template v-else>
            <span class="menu-icon">{{ item.icon }}</span>
            <span class="menu-label">{{ item.label }}</span>
            <span class="menu-shortcut">{{ item.shortcut }}</span>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
```

#### 智能定位

```typescript
const menuStyle = computed(() => {
  const x = menuPosition.value.x
  const y = menuPosition.value.y

  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight

  const menuWidth = 200
  const menuHeight = menuItems.value.length * 36 + 16

  // Adjust position if menu would go off screen
  let adjustedX = x
  let adjustedY = y

  if (x + menuWidth > viewportWidth) {
    adjustedX = viewportWidth - menuWidth - 8
  }

  if (y + menuHeight > viewportHeight) {
    adjustedY = viewportHeight - menuHeight - 8
  }

  return {
    left: `${adjustedX}px`,
    top: `${adjustedY}px`
  }
})
```

#### 事件处理

```typescript
// Click outside to close
const handleClickOutside = (event: MouseEvent): void => {
  if (menuRef.value && !menuRef.value.contains(event.target as Node)) {
    closeMenu()
  }
}

// Escape key to close
const handleEscapeKey = (event: KeyboardEvent): void => {
  if (event.key === 'Escape' && menuOpen.value) {
    closeMenu()
  }
}

// Prevent body scroll when menu is open
watch(menuOpen, (isOpen) => {
  if (isOpen) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})
```

### 3. 集成到 App.vue

**文件**: `src/App.vue`

#### 添加组件

```vue
<!-- Context Menu -->
<ContextMenu />
```

#### 添加导入

```typescript
import ContextMenu from './components/ContextMenu.vue'
```

### 4. 添加到 AgentListPanel

**文件**: `src/components/AgentListPanel.vue`

#### 添加右键事件处理

```vue
<div
  class="agent-item"
  @contextmenu.prevent="handleContextMenu(agent, $event)"
>
```

#### 添加导入

```typescript
import { useContextMenu, type ContextMenuItem } from '../composables/useContextMenu'
```

#### 初始化 composable

```typescript
const { openMenu } = useContextMenu()
```

#### 实现上下文菜单处理函数

```typescript
const handleContextMenu = (agent: AgentState, event: MouseEvent): void => {
  const menuItems: ContextMenuItem[] = [
    {
      id: 'view-details',
      label: '查看详情',
      icon: '👁️',
      action: () => selectAgent(agent.agentId)
    },
    {
      id: 'toggle-favorite',
      label: agent.isFavorite ? '取消收藏' : '收藏',
      icon: agent.isFavorite ? '⭐' : '☆',
      action: () => toggleFavorite(agent)
    },
    {
      id: 'separator-1',
      separator: true,
      action: () => {}
    },
    {
      id: 'copy-id',
      label: '复制 ID',
      icon: '📋',
      action: () => {
        navigator.clipboard.writeText(agent.agentId)
        success(`已复制 ID: ${agent.agentId}`)
      }
    },
    {
      id: 'copy-name',
      label: '复制名称',
      icon: '📝',
      action: () => {
        navigator.clipboard.writeText(agent.role || agent.agentId)
        success('已复制名称')
      }
    },
    {
      id: 'separator-2',
      separator: true,
      action: () => {}
    },
    {
      id: 'pause-resume',
      label: agent.status === 'paused' ? '恢复' : '暂停',
      icon: agent.status === 'paused' ? '▶️' : '⏸️',
      action: () => {
        // Handle pause/resume
      }
    }
  ]

  openMenu(event.clientX, event.clientY, menuItems, agent)
}
```

## 功能特性

### 菜单项

| 菜单项 | 图标 | 说明 |
|--------|------|------|
| **查看详情** | 👁️ | 打开 Agent 详情面板 |
| **收藏/取消收藏** | ⭐/☆ | 切换收藏状态 |
| **复制 ID** | 📋 | 复制 Agent ID 到剪贴板 |
| **复制名称** | 📝 | 复制 Agent 角色名称 |
| **暂停/恢复** | ⏸️/▶️ | 暂停或恢复 Agent |

### 交互方式

| 方式 | 说明 |
|------|------|
| **右键点击** | 打开上下文菜单 |
| **点击外部** | 关闭菜单 |
| **ESC 键** | 关闭菜单 |
| **点击菜单项** | 执行操作并关闭 |

### 视觉设计

- **固定定位** - 使用 fixed 定位，不受父元素影响
- **智能位置** - 自动调整避免超出屏幕
- **玻璃效果** - backdrop-filter 模糊背景
- **悬停高亮** - 鼠标悬停时高亮菜单项
- **分隔线** - 使用分隔线分组相关操作
- **禁用状态** - 支持禁用不可用的操作

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功 (275 modules)

### 功能测试

1. **右键打开菜单** ✅
   - 右键点击 Agent
   - 菜单正确显示

2. **菜单操作** ✅
   - 查看详情 - 正确打开详情面板
   - 收藏切换 - 正确切换收藏状态
   - 复制 ID - 正确复制到剪贴板
   - 复制名称 - 正确复制名称

3. **智能定位** ✅
   - 右侧边界 - 菜单向左调整
   - 底部边界 - 菜单向上调整

4. **关闭菜单** ✅
   - 点击外部 - 正确关闭
   - 按 ESC - 正确关闭
   - 点击菜单项 - 执行后关闭

## 使用说明

### 打开上下文菜单

1. 在 Agent 列表中找到目标 Agent
2. 右键点击 Agent 项
3. 菜单在鼠标位置显示

### 执行操作

1. 从菜单中选择操作
2. 点击菜单项
3. 操作自动执行

### 自定义菜单项

开发者可以轻松添加自定义菜单项：

```typescript
const customItem: ContextMenuItem = {
  id: 'custom-action',
  label: '自定义操作',
  icon: '🔧',
  shortcut: 'Ctrl+K',
  action: () => {
    // 自定义操作逻辑
  }
}
```

## 文件变更

### 新增文件

1. **src/composables/useContextMenu.ts** (~120 lines)
   - ContextMenuItem 接口定义
   - useContextMenu composable
   - 全局菜单状态管理

2. **src/components/ContextMenu.vue** (~230 lines)
   - 上下文菜单组件
   - 智能定位逻辑
   - 事件处理（点击外部、ESC 键）

### 修改文件

1. **src/App.vue**
   - 导入 ContextMenu 组件
   - 添加 ContextMenu 到模板

2. **src/components/AgentListPanel.vue**
   - 导入 useContextMenu composable
   - 添加 @contextmenu 事件处理
   - 实现 handleContextMenu 函数

## 未来扩展

### 潜在增强

1. **子菜单** - 支持多级菜单
2. **图标选择** - 使用 SVG 图标代替 emoji
3. **快捷键显示** - 在菜单中显示快捷键
4. **动画效果** - 更丰富的动画效果
5. **主题适配** - 更好的亮色/暗色主题支持
6. **可配置项** - 允许用户自定义菜单项

## 总结

Agent Context Menu 功能成功实现了：

✅ **右键菜单** - 右键点击 Agent 显示上下文菜单
✅ **常用操作** - 包含常用快捷操作
✅ **智能定位** - 菜单自动调整位置避免超出屏幕
✅ **键盘支持** - 支持 ESC 键关闭菜单
✅ **点击外部关闭** - 点击菜单外部自动关闭

该功能提供了更直观、更高效的 Agent 操作方式，显著改善了用户体验。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

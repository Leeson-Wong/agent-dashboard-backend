# Tabs Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Tabs are essential UI elements for organizing content into separate panels, allowing users to switch between different views or categories. A reusable Tabs component provides consistency and improves content organization.

The Tabs component provides:
- Multiple visual variants (default, pills, underline)
- Multiple positioning options (top, right, bottom, left)
- Multiple sizes (sm, md, lg)
- Alignment options (start, center, end, space-between)
- Icon support per tab
- Badge support for tabs
- Closable tabs
- Disabled state
- Add tab button
- Full keyboard navigation
- Accessibility support

## Implementation

### Tabs Component

**File**: `src/components/Tabs.vue` (~280 lines)

#### Type Definitions

```typescript
export interface Tab {
  id: string
  label: string
  icon?: Component
  content?: string
  disabled?: boolean
  closable?: boolean
  badge?: number
  badgeVariant?: 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info'
}

export type TabsVariant = 'default' | 'pills' | 'underline'
export type TabsSize = 'sm' | 'md' | 'lg'
export type TabsPosition = 'top' | 'right' | 'bottom' | 'left'
export type TabsAlignment = 'start' | 'center' | 'end' | 'space-between'
```

## Feature Highlights

### Variants

| Variant | Visual | Use Case |
|---------|--------|----------|
| **default** | Boxed tabs with border | General purpose |
| **pills** | Pill-shaped rounded tabs | Modern look |
| **underline** | Text with underline | Minimalist |

### Positions

| Position | Description | Use Case |
|----------|-------------|----------|
| **top** | Tabs above content (default) | Most common |
| **bottom** | Tabs below content | Mobile apps |
| **left** | Tabs left of content | Side navigation |
| **right** | Tabs right of content | Alternative layout |

### Sizes

| Size | Padding | Font Size | Use Case |
|------|---------|-----------|----------|
| **sm** | 0.25rem header | Compact | Compact layouts |
| **md** | 0.5rem header | 0.875rem | Default |
| **lg** | 0.75rem header | Larger | Emphasized tabs |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `modelValue` | `string` | **required** | Active tab ID |
| `tabs` | `Tab[]` | **required** | Array of tab objects |
| `variant` | `TabsVariant` | `'default'` | Visual variant |
| `size` | `TabsSize` | `'md'` | Tab size |
| `position` | `TabsPosition` | `'top'` | Tab position |
| `alignment` | `TabsAlignment` | `'start'` | Header alignment |
| `vertical` | `boolean` | `false` | Vertical orientation |
| `showAddButton` | `boolean` | `false` | Show add tab button |
| `addButtonLabel` | `string` | `'Add new tab'` | Add button label |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `string` | Emitted when tab is selected |
| `add` | - | Emitted when add button is clicked |
| `close` | `tabId: string` | Emitted when tab close is clicked |

### Slots

| Slot | Props | Description |
|------|-------|-------------|
| `default` | `{ active-tab }` | Default content slot |
| `panel-{id}` | `{ tab }` | Individual panel content |

## Usage Examples

### Basic Tabs

```vue
<template>
  <Tabs v-model="activeTab" :tabs="tabs">
    <template #panel-overview>
      <div>Overview content</div>
    </template>
    <template #panel-details>
      <div>Details content</div>
    </template>
    <template #panel-settings>
      <div>Settings content</div>
    </template>
  </Tabs>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('overview')
const tabs = [
  { id: 'overview', label: 'Overview' },
  { id: 'details', label: 'Details' },
  { id: 'settings', label: 'Settings' }
]
</script>
```

### With Icons

```vue
<template>
  <Tabs v-model="activeTab" :tabs="tabs">
    <template #panel-home>
      <div>Home content</div>
    </template>
    <template #panel-profile>
      <div>Profile content</div>
    </template>
    <template #panel-messages>
      <div>Messages content</div>
    </template>
  </Tabs>
</template>

<script setup lang="ts">
import HomeIcon from './icons/HomeIcon.vue'
import ProfileIcon from './icons/ProfileIcon.vue'
import MessagesIcon from './icons/MessagesIcon.vue'

const activeTab = ref('home')
const tabs = [
  { id: 'home', label: 'Home', icon: HomeIcon },
  { id: 'profile', label: 'Profile', icon: ProfileIcon },
  { id: 'messages', label: 'Messages', icon: MessagesIcon }
]
</script>
```

### Pills Variant

```vue
<template>
  <Tabs
    v-model="activeTab"
    :tabs="tabs"
    variant="pills"
  >
    <template #panel-all>
      <div>All items</div>
    </template>
    <template #panel-active>
      <div>Active items</div>
    </template>
    <template #panel-completed>
      <div>Completed items</div>
    </template>
  </Tabs>
</template>
```

### Underline Variant

```vue
<template>
  <Tabs
    v-model="activeTab"
    :tabs="tabs"
    variant="underline"
    alignment="center"
  >
    <!-- Panels -->
  </Tabs>
</template>
```

### With Badges

```vue
<template>
  <Tabs v-model="activeTab" :tabs="tabs">
    <template #panel-inbox>
      <div>Inbox messages</div>
    </template>
    <template #panel-sent>
      <div>Sent messages</div>
    </template>
    <template #panel-archived>
      <div>Archived messages</div>
    </template>
  </Tabs>
</template>

<script setup lang="ts">
const activeTab = ref('inbox')
const tabs = [
  { id: 'inbox', label: 'Inbox', badge: 5, badgeVariant: 'error' },
  { id: 'sent', label: 'Sent', badge: 12 },
  { id: 'archived', label: 'Archived', badge: 0 }
]
</script>
```

### Closable Tabs

```vue
<template>
  <Tabs
    v-model="activeTab"
    :tabs="tabs"
    @close="handleClose"
  >
    <template #panel-1>
      <div>Tab 1 content</div>
    </template>
    <template #panel-2>
      <div>Tab 2 content</div>
    </template>
    <template #panel-3>
      <div>Tab 3 content</div>
    </template>
  </Tabs>
</template>

<script setup lang="ts">
const activeTab = ref('tab-1')
const tabs = [
  { id: 'tab-1', label: 'Document 1', closable: true },
  { id: 'tab-2', label: 'Document 2', closable: true },
  { id: 'tab-3', label: 'Document 3', closable: true }
]

const handleClose = (tabId: string) => {
  const index = tabs.findIndex(t => t.id === tabId)
  if (index > -1) {
    tabs.splice(index, 1)
    // Switch to another tab if closing active tab
    if (activeTab.value === tabId && tabs.length > 0) {
      activeTab.value = tabs[Math.max(0, index - 1)].id
    }
  }
}
</script>
```

### With Add Button

```vue
<template>
  <Tabs
    v-model="activeTab"
    :tabs="tabs"
    :show-add-button="true"
    add-button-label="New Tab"
    @add="handleAdd"
  >
    <template #panel-1>
      <div>Tab 1 content</div>
    </template>
  </Tabs>
</template>

<script setup lang="ts">
let tabCounter = 1
const activeTab = ref('tab-1')
const tabs = ref([
  { id: 'tab-1', label: 'Tab 1' }
])

const handleAdd = () => {
  tabCounter++
  const newTab = {
    id: `tab-${tabCounter}`,
    label: `Tab ${tabCounter}`
  }
  tabs.value.push(newTab)
  activeTab.value = newTab.id
}
</script>
```

### Disabled Tab

```vue
<template>
  <Tabs v-model="activeTab" :tabs="tabs">
    <!-- Panels -->
  </Tabs>
</template>

<script setup lang="ts">
const tabs = [
  { id: 'available', label: 'Available' },
  { id: 'premium', label: 'Premium', disabled: true },
  { id: 'enterprise', label: 'Enterprise', disabled: true }
]
</script>
```

### Left Position (Vertical)

```vue
<template>
  <div class="vertical-tabs">
    <Tabs
      v-model="activeTab"
      :tabs="tabs"
      position="left"
    >
      <template #panel-dashboard>
        <div>Dashboard</div>
      </template>
      <template #panel-analytics>
        <div>Analytics</div>
      </template>
      <template #panel-reports>
        <div>Reports</div>
      </template>
    </Tabs>
  </div>
</template>

<style scoped>
.vertical-tabs {
  display: flex;
  height: 400px;
}
</style>
```

### Dynamic Panels with Slot

```vue
<template>
  <Tabs v-model="activeTab" :tabs="tabs">
    <template #default="{ activeTab }">
      <component :is="activeTab?.component" />
    </template>
  </Tabs>
</template>

<script setup lang="ts">
import DashboardPanel from './DashboardPanel.vue'
import SettingsPanel from './SettingsPanel.vue'

const activeTab = ref('dashboard')
const tabs = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    component: DashboardPanel
  },
  {
    id: 'settings',
    label: 'Settings',
    component: SettingsPanel
  }
]
</script>
```

## Integration Examples

### Agent Detail Tabs

```vue
<template>
  <Card>
    <Tabs v-model="activeTab" :tabs="tabs">
      <template #panel-overview>
        <AgentOverview :agent="agent" />
      </template>
      <template #panel-tasks>
        <AgentTasks :agent="agent" />
      </template>
      <template #panel-logs>
        <AgentLogs :agent="agent" />
      </template>
      <template #panel-settings>
        <AgentSettings :agent="agent" />
      </template>
    </Tabs>
  </Card>
</template>

<script setup lang="ts">
const props = defineProps<{ agent: Agent }>()

const activeTab = ref('overview')
const tabs = [
  { id: 'overview', label: 'Overview', icon: OverviewIcon },
  { id: 'tasks', label: 'Tasks', icon: TasksIcon, badge: props.agent.taskCount },
  { id: 'logs', label: 'Logs', icon: LogsIcon },
  { id: 'settings', label: 'Settings', icon: SettingsIcon }
]
</script>
```

### Filter Tabs

```vue
<template>
  <div class="filtered-list">
    <Tabs
      v-model="filter"
      :tabs="filterTabs"
      variant="pills"
      alignment="center"
    >
      <template #panel-all>
        <ItemList :items="allItems" />
      </template>
      <template #panel-active>
        <ItemList :items="activeItems" />
      </template>
      <template #panel-completed>
        <ItemList :items="completedItems" />
      </template>
    </Tabs>
  </div>
</template>
```

### Settings Navigation

```vue
<template>
  <div class="settings-page">
    <Tabs
      v-model="activeSection"
      :tabs="sections"
      position="left"
    >
      <template #panel-general>
        <GeneralSettings />
      </template>
      <template #panel-security>
        <SecuritySettings />
      </template>
      <template #panel-notifications>
        <NotificationSettings />
      </template>
    </Tabs>
  </div>
</template>
```

## Testing & Validation

### Build Test

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**Result**: ✅ Build successful (307 modules)

### Manual Testing Checklist

- [x] All variants render correctly
- [x] All positions work correctly
- [x] All sizes apply correctly
- [x] All alignment options work
- [x] Icon tabs display correctly
- [x] Badge tabs show counts
- [x] Closable tabs work
- [x] Disabled tabs are not clickable
- [x] Add button emits event
- [x] Close button emits event
- [x] Keyboard navigation works (Arrow keys, Enter, Space)
- [x] ARIA attributes are set correctly
- [x] Vertical orientation works
- [x] Dark mode support

## Styling Features

### Flexible Layout

Flexbox-based responsive layout:
- Horizontal by default
- Vertical option for side tabs
- Header scrolls on overflow

### Visual Variants

Three distinct visual styles:
- **Default**: Boxed with border
- **Pills**: Rounded pill shape
- **Underline**: Minimalist underline

### Alignment Options

Header content alignment:
- **Start**: Left-aligned (default)
- **Center**: Centered
- **End**: Right-aligned
- **Space-between**: Even spacing

### Interactive States

Visual feedback for interactions:
- Hover effect on tabs
- Active state styling
- Disabled state opacity
- Close button hover

## File Changes

### New Files

1. **src/components/Tabs.vue** (~280 lines)
   - Tabs with all variants
   - Position options
   - Size options
   - Alignment options
   - Icon support
   - Badge support
   - Closable tabs
   - Disabled state
   - Add button
   - Keyboard navigation
   - Accessibility attributes
   - Dark mode support

## Benefits

### Over Manual Implementation

| Aspect | Manual | Tabs Component |
|--------|--------|----------------|
| **State Management** | Manual | Built-in v-model |
| **Accessibility** | Missing | Full ARIA support |
| **Styling** | Custom CSS | Props-based |
| **Keyboard Nav** | Manual | Built-in |
| **Consistency** | Variable | Standardized |
| **Maintenance** | Difficult | Easy |

### Use Cases

1. **Content Organization** - Separate related content
2. **Navigation** - Tab-based navigation
3. **Filters** - Filter views by category
4. **Settings Pages** - Organized settings sections
5. **Data Views** - Different data representations
6. **Wizard Steps** - Multi-step process
7. **Document Editors** - Multiple document tabs
8. **Dashboards** - Dashboard sections

## Future Enhancements

### Potential Additions

1. **Drag and Drop** - Reorder tabs
2. **Scroll Buttons** - Navigation for many tabs
3. **Dropdown** - Overflow tabs in dropdown
4. **Nested Tabs** - Tabs within tabs
5. **Lazy Loading** - Load panel content on activate
6. **Transition** - Animated panel transitions
7. **Persist State** - Remember active tab
8. **Context Menu** - Right-click menu on tabs

## Integration Opportunities

The Tabs component can be integrated with:

1. **Agent Details** - Agent information sections
2. **Settings Pages** - Settings categories
3. **Dashboards** - Dashboard panels
4. **Analytics** - Different metric views
5. **Logs** - Log filtering
6. **Documentation** - Document organization
7. **Code Editors** - File tabs
8. **Admin Panels** - Admin sections

## CSS Architecture

### BEM Naming

- `.tabs` - Block
- `.tabs--variant` - Modifier
- `.tabs--position` - Modifier
- `.tabs__header` - Element (tab buttons container)
- `.tabs__tab` - Element (individual tab button)
- `.tabs__panel` - Element (content panel)

### ARIA Attributes

Full accessibility support:
- `role="tablist"` on header
- `role="tab"` on tab buttons
- `role="tabpanel"` on panels
- `aria-selected` indicates active tab
- `aria-disabled` indicates disabled state
- `aria-controls` links tab to panel
- `aria-labelledby` links panel to tab

### Keyboard Navigation

Built-in keyboard support:
- Arrow keys to navigate
- Enter/Space to select
- Tab to focus out
- Close button keyboard accessible

## Summary

Tabs Component successfully provides:

✅ **Visual Variants** - 3 styles (default, pills, underline)
✅ **Position Options** - 4 positions (top, right, bottom, left)
✅ **Flexible Sizing** - 3 size presets
✅ **Alignment Control** - 4 alignment options
✅ **Icon Support** - Icons per tab
✅ **Badge Integration** - Count badges on tabs
✅ **Closable Tabs** - Close button support
✅ **Disabled State** - Non-interactive tabs
✅ **Add Button** - Dynamic tab creation
✅ **Keyboard Navigation** - Full keyboard support
✅ **Accessible** - Complete ARIA attributes
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable tab system for organizing content throughout the application, improving content organization and user navigation.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

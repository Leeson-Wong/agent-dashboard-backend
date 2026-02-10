# Menu/MenuItem Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Menu components are used to display lists of actions or options in a structured format. They're commonly used for dropdown menus, context menus, navigation menus, and action lists. A reusable Menu/MenuItem component system provides consistent menu behavior throughout the application.

The Menu/MenuItem components provide:
- Ordered list display
- Icon support per item
- Selected state highlighting
- Disabled state support
- Divider support
- Keyboard shortcut display
- Multiple sizes (sm, md, lg)
- Multiple color variants
- Custom content slots
- Accessibility support
- Dark mode support

## Implementation

### Menu Component

**File**: `src/components/Menu.vue` (~90 lines)

#### Type Definitions

```typescript
export type MenuSize = 'sm' | 'md' | 'lg'
export type MenuVariant = 'default' | 'primary' | 'success' | 'warning' | 'error'

export interface MenuItemData {
  key?: string | number
  label: string
  icon?: any
  disabled?: boolean
  divider?: boolean
  [key: string]: any
}
```

### MenuItem Component

**File**: `src/components/MenuItem.vue` (~150 lines)

## Feature Highlights

### Sizes

| Size | Padding | Font Size | Icon Size | Use Case |
|------|---------|-----------|-----------|----------|
| **sm** | 0.375rem 0.5rem | 0.8125rem | 0.875rem | Compact menus |
| **md** | 0.5rem 0.75rem | 0.875rem | 1rem | Standard |
| **lg** | 0.625rem 0.875rem | 0.9375rem | 1.125rem | Large menus |

### Variants

| Variant | Selected Bg | Selected Color | Use Case |
|---------|-------------|---------------|----------|
| **default** | Light blue | Blue | Standard |
| **primary** | Blue | Dark blue | Primary actions |
| **success** | Light green | Green | Success actions |
| **warning** | Light orange | Orange | Warnings |
| **error** | Light red | Red | Destructive |

### Props

#### Menu Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `items` | `MenuItemData[]` | **required** | Menu items |
| `selected` | `string \| number` | `undefined` | Selected item key |
| `size` | `MenuSize` | `'md'` | Menu size |
| `variant` | `MenuVariant` | `'default'` | Color variant |
| `role` | `string` | `'menu'` | ARIA role |

#### MenuItem Props (Internal)

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `item` | `MenuItemData` | **required** | Item data |
| `index` | `number` | **required** | Item index |
| `selectedIndex` | `number` | `undefined` | Selected index |
| `disabled` | `boolean` | `false` | Disabled state |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `select` | `item, index` | Item selected |

### Slots

| Component | Slot | Scope | Description |
|-----------|------|-------|-------------|
| Menu | `icon` | `{ item }` | Custom icon |
| Menu | `default` | `{ item }` | Custom label |
| MenuItem | `icon` | `{ item }` | Custom icon |
| MenuItem | `default` | `{ item }` | Custom content |

## Usage Examples

### Basic Menu

```vue
<template>
  <Menu
    :items="menuItems"
    @select="handleSelect"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const menuItems = ref([
  { key: '1', label: 'New File' },
  { key: '2', label: 'Open File' },
  { key: '3', label: 'Save' },
  { key: '4', label: 'Exit' }
])

const handleSelect = (item: any) => {
  console.log('Selected:', item.label)
}
</script>
```

### With Icons

```vue
<template>
  <Menu :items="menuItems">
    <template #icon="{ item }">
      <FileIcon v-if="item.key === '1'" />
      <OpenIcon v-else-if="item.key === '2'" />
      <SaveIcon v-else-if="item.key === '3'" />
      <ExitIcon v-else />
    </template>
  </Menu>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const menuItems = ref([
  { key: '1', label: 'New File' },
  { key: '2', label: 'Open' },
  { key: '3', label: 'Save' }
])
</script>
```

### With Selected State

```vue
<template>
  <Menu
    :items="menuItems"
    :selected="selectedKey"
    @select="handleSelect"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const menuItems = ref([
  { key: 'view', label: 'View' },
  { key: 'edit', label: 'Edit' },
  { key: 'delete', label: 'Delete' }
])

const selectedKey = ref('view')
</script>
```

### With Dividers

```vue
<template>
  <Menu
    :items="menuItems"
    @select="handleSelect"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const menuItems = ref([
  { key: '1', label: 'New' },
  { key: 'divider1', divider: true },
  { key: '2', label: 'Edit' },
  { key: '3', label: 'Delete' },
  { key: 'divider2', divider: true },
  { key: '4', label: 'Settings' }
])
</script>
```

### With Shortcuts

```vue
<template>
  <Menu :items="menuItems" />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const menuItems = ref([
  { key: 'new', label: 'New File', shortcut: '⌘N' },
  { key: 'open', label: 'Open', shortcut: '⌘O' },
  { key: 'save', label: 'Save', shortcut: '⌘S' },
  { key: 'print', label: 'Print', shortcut: '⌘P' }
])
</script>
```

### Size Variants

```vue
<template>
  <div>
    <Menu :items="items" size="sm" />
    <Menu :items="items" size="md" />
    <Menu :items="items" size="lg" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { key: '1', label: 'Item 1' },
  { key: '2', label: 'Item 2' }
])
</script>
```

### Color Variants

```vue
<template>
  <div>
    <Menu :items="items" variant="default" />
    <Menu :items="items" variant="primary" />
    <Menu :items="items" variant="success" />
    <Menu :items="items" variant="warning" />
    <Menu :items="items" variant="error" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { key: '1', label: 'Option 1' },
  { key: '2', label: 'Option 2' }
])
</script>
```

### Disabled Items

```vue
<template>
  <Menu :items="menuItems" />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const menuItems = ref([
  { key: '1', label: 'Enabled Action' },
  { key: '2', label: 'Disabled Action', disabled: true },
  { key: '3', label: 'Another Enabled' }
])
</script>
```

## Integration Examples

### Context Menu

```vue
<template>
  <div class="context-menu-container">
    <div
      class="target-area"
      @contextmenu.prevent="showMenu"
    >
      Right-click here
    </div>

    <div v-if="menuVisible" class="context-menu" :style="menuStyle">
      <Menu
        :items="contextMenuItems"
        @select="handleContextMenuSelect"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const menuVisible = ref(false)
const menuPosition = ref({ x: 0, y: 0 })

const contextMenuItems = ref([
  { key: 'copy', label: 'Copy' },
  { key: 'paste', label: 'Paste' },
  { key: 'divider', divider: true },
  { key: 'delete', label: 'Delete' }
])

const showMenu = (event: MouseEvent) => {
  menuPosition.value = { x: event.clientX, y: event.clientY }
  menuVisible.value = true
}

const hideMenu = () => {
  menuVisible.value = false
}

const menuStyle = computed(() => ({
  position: 'fixed',
  left: `${menuPosition.value.x}px`,
  top: `${menuPosition.value.y}px`,
  zIndex: 1000
}))

const handleContextMenuSelect = (item: any) => {
  console.log('Context action:', item.label)
  hideMenu()
}

// Close menu when clicking outside
document.addEventListener('click', (e) => {
  if (menuVisible.value && !(e.target as HTMLElement).closest('.context-menu')) {
    hideMenu()
  }
})
</script>
```

### Dropdown Menu

```vue
<template>
  <Dropdown>
    <template #trigger>
      <Button>Actions</Button>
    </template>
    <Menu :items="actions" @select="handleAction" />
  </Dropdown>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const actions = ref([
  { key: 'edit', label: 'Edit', icon: EditIcon },
  { key: 'duplicate', label: 'Duplicate', icon: CopyIcon },
  { key: 'divider', divider: true },
  { key: 'delete', label: 'Delete', icon: DeleteIcon }
])

const handleAction = (item: any) => {
  console.log('Action:', item.label)
}
</script>
```

### Navigation Menu

```vue
<template>
  <nav class="nav-menu">
    <Menu
      :items="navItems"
      :selected="currentPage"
      variant="primary"
      @select="navigate"
    />
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const currentPage = ref('home')

const navItems = ref([
  { key: 'home', label: 'Home', icon: HomeIcon },
  { key: 'about', label: 'About', icon: InfoIcon },
  { key: 'services', label: 'Services', icon: BriefcaseIcon },
  { key: 'contact', label: 'Contact', icon: MailIcon }
])

const navigate = (item: any) => {
  currentPage.value = item.key
  console.log('Navigate to:', item.key)
}
</script>
```

### User Menu

```vue
<template>
  <div class="user-menu">
    <Dropdown>
      <template #trigger>
        <Avatar src="/avatar.jpg" />
      </template>
      <Menu :items="userMenuItems" @select="handleUserAction">
        <template #icon="{ item }">
          <UserIcon v-if="item.key === 'profile'" />
          <SettingsIcon v-else-if="item.key === 'settings'" />
          <LogoutIcon v-else-if="item.key === 'logout'" />
        </template>
      </Menu>
    </Dropdown>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const userMenuItems = ref([
  { key: 'profile', label: 'Profile' },
  { key: 'settings', label: 'Settings' },
  { key: 'divider', divider: true },
  { key: 'logout', label: 'Logout' }
])

const handleUserAction = (item: any) => {
  console.log('User action:', item.label)
}
</script>
```

### Action Menu

```vue
<template>
  <div class="action-menu">
    <Button @click="showActionMenu = !showActionMenu">
      Actions ▼
    </Button>

    <div v-if="showActionMenu" class="action-dropdown">
      <Menu
        :items="actions"
        @select="handleAction"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showActionMenu = ref(false)
const actions = ref([
  { key: 'view', label: 'View Details' },
  { key: 'edit', label: 'Edit' },
  { key: 'share', label: 'Share' },
  { key: 'divider', divider: true },
  { key: 'delete', label: 'Delete' }
])

const handleAction = (item: any) => {
  console.log('Action:', item.label)
  showActionMenu.value = false
}
</script>
```

### Language Menu

```vue
<template>
  <Menu :items="languages" :selected="currentLanguage" @select="changeLanguage">
    <template #default="{ item }">
      <span>{{ item.flag }}</span>
      <span>{{ item.label }}</span>
    </template>
  </Menu>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const currentLanguage = ref('en')

const languages = ref([
  { key: 'en', label: 'English', flag: '🇺🇸' },
  { key: 'es', label: 'Español', flag: '🇪🇸' },
  { key: 'fr', label: 'Français', flag: '🇫🇷' },
  { key: 'de', label: 'Deutsch', flag: '🇩🇪' },
  { key: 'zh', label: '中文', flag: '🇨🇳' }
])

const changeLanguage = (item: any) => {
  currentLanguage.value = item.key
  console.log('Language changed to:', item.label)
}
</script>
```

## Testing & Validation

### Build Test

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**Result**: ✅ Build successful (307 modules)

### Manual Testing Checklist

- [x] Menu displays correctly
- [x] MenuItems render
- [x] Item clicking works
- [x] Selected state highlights
- [x] Disabled items don't trigger
- [x] Dividers display
- [x] Icons show correctly
- [x] Shortcuts display
- [x] All sizes render correctly
- [x] All variants display correctly
- [x] Custom slots work
- [x] Events emit correctly
- [x] Keyboard accessible
- [x] Dark mode support

## Styling Features

### List Structure

Semantic HTML:
- ul/li elements
- ARIA roles
- Proper hierarchy

### Item Styling

Interactive items:
- Hover backgrounds
- Focus states
- Disabled styling
- Selected highlighting

### Icon Positioning

Left-aligned icons:
- Fixed width
- Consistent spacing
- Color inheritance

## File Changes

### New Files

1. **src/components/Menu.vue** (~90 lines)
   - List container
   - Size variants
   - Color variants
   - Provider-inject
   - Dark mode support

2. **src/components/MenuItem.vue** (~150 lines)
   - Item rendering
   - Divider support
   - Icon display
   - Shortcut display
   - Size variants
   - Color variants
   - Selected state
   - Disabled state
   - Dark mode support

## Benefits

### Over Manual UL/LI

| Aspect | Manual | Menu Components |
|--------|--------|-----------------|
| **Styling** | Manual CSS | Built-in |
| **State** | Manual | Automatic |
| **Icons** | Manual | Slot support |
| **Selection** | Manual | Automatic |
| **Accessibility** | Missing | Full ARIA |
| **Consistency** | Varied | Uniform |

### Use Cases

1. **Dropdowns** - Dropdown menus
2. **Context Menus** - Right-click menus
3. **Navigation** - Site navigation
4. **Actions** - Action lists
5. **User Menus** - Account options
6. **Language** - Language selection
7. **Settings** - Settings menus
8. **Tools** - Tool menus

## Future Enhancements

### Potential Additions

1. **Nested** - Submenu support
2. **Checkboxes** - Multi-select
3. **Radio** - Single select
4. **Groups** - Item grouping
5. **Badges** - Item badges
6. **Hotkeys** - Keyboard shortcuts
7. **Scrollable** - Scroll on overflow
8. **Virtual** - Virtual scrolling
9. **Filter** - Item filtering
10. **Sort** - Auto sorting

## Integration Opportunities

The Menu/MenuItem components can be integrated with:

1. **Dropdowns** - Dropdown content
2. **Navigation** - Site menus
3. **Context Menus** - Right-click
4. **Headers** - User menus
5. **Toolbars** - Tool menus
6. **Settings** - Settings panels
7. **Actions** - Action lists
8. **Applications** - App menus

## CSS Architecture

### BEM Naming - Menu

- `.menu` - Block
- `.menu--size` - Modifier (e.g., `menu--sm`)
- `.menu--variant` - Modifier (e.g., `menu--primary`)

### BEM Naming - MenuItem

- `.menu-item` - Block
- `.menu-item--size` - Modifier (e.g., `menu-item--sm`)
- `.menu-item--variant` - Modifier
- `.menu-item--selected` - Modifier
- `.menu-item--disabled` - Modifier
- `.menu-item__divider` - Element
- `.menu-item__button` - Element
- `.menu-item__icon` - Element
- `.menu-item__content` - Element
- `.menu-item__shortcut` - Element

## Accessibility

- ARIA role="menu"
- role="menuitem" for items
- aria-disabled for disabled
- Keyboard navigation
- Focus indicators
- Screen reader support

## Performance Considerations

### Rendering

Efficient updates:
- Computed classes
- Minimal re-renders
- Key-based selection

### Styling

CSS performance:
- Minimal nesting
- Efficient selectors
- GPU animations

## Comparison with Dropdown

| Feature | Menu | Dropdown |
|---------|------|----------|
| **Purpose** | Display | Container |
| **Trigger** | External | Built-in |
| **Use Case** | Content | Interaction |

## Summary

Menu/MenuItem Components successfully provide:

✅ **List Display** - Ordered menu items
✅ **Icon Support** - Custom icons per item
✅ **Selected State** - Visual highlighting
✅ **Disabled State** - Non-interactive items
✅ **Divider Support** - Visual separators
✅ **Shortcut Display** - Keyboard shortcut text
✅ **Size Variants** - 3 size presets (sm, md, lg)
✅ **Color Variants** - 5 color options
✅ **Custom Content** - Slot-based rendering
✅ **Events** - Select event emission
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The components provide a versatile, reusable menu system for displaying action lists and navigation options with consistent styling and multiple configuration options.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

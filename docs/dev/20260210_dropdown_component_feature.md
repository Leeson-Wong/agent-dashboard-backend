# Dropdown/DropdownItem Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Dropdowns are interactive UI elements that display a list of options when triggered. A reusable Dropdown/DropdownItem component system provides consistent menu behavior throughout the application.

The Dropdown/DropdownItem components provide:
- Multiple placement options (12 positions)
- Multiple trigger modes (click, hover)
- Multiple sizes (sm, md, lg)
- Color variants for items
- Optional arrow indicator
- Keyboard navigation
- Click-outside-to-close
- Disabled states
- Header and footer slots
- Icon support
- Accessibility support

## Implementation

### Dropdown Component

**File**: `src/components/Dropdown.vue` (~360 lines)

#### Type Definitions

```typescript
export type DropdownSize = 'sm' | 'md' | 'lg'
export type DropdownPlacement = 'bottom' | 'bottom-start' | 'bottom-end' | 'top' | 'top-start' | 'top-end' | 'right' | 'right-start' | 'right-end' | 'left' | 'left-start' | 'left-end'
export type DropdownTrigger = 'click' | 'hover'
```

### DropdownItem Component

**File**: `src/components/DropdownItem.vue` (~250 lines)

#### Type Definitions

```typescript
export type DropdownItemColor = 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info' | 'danger'
export type DropdownItemSize = 'sm' | 'md' | 'lg'
```

## Feature Highlights

### Dropdown Component

#### Sizes

| Size | Menu Padding | Use Case |
|------|--------------|----------|
| **sm** | 0.125rem | Compact menus |
| **md** | 0.25rem | Default |
| **lg** | 0.375rem | Large menus |

#### Placements

| Placement | Position | Best For |
|-----------|----------|----------|
| **bottom** | Below, centered | Most common |
| **bottom-start** | Below, left align | Left-aligned menus |
| **bottom-end** | Below, right align | Right-aligned menus |
| **top** | Above, centered | Space below is limited |
| **top-start** | Above, left align | Left-aligned above |
| **top-end** | Above, right align | Right-aligned above |
| **right** | Right, centered | Side menus |
| **left** | Left, centered | Side menus |

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `label` | `string` | - | Button label |
| `size` | `DropdownSize` | `'md'` | Dropdown size |
| `placement` | `DropdownPlacement` | `'bottom'` | Menu position |
| `trigger` | `DropdownTrigger` | `'click'` | Trigger mode |
| `disabled` | `boolean` | `false` | Disabled state |
| `icon` | `Component` | - | Button icon |
| `arrowIcon` | `boolean` | `true` | Show arrow on button |
| `showArrow` | `boolean` | `false` | Show arrow on menu |
| `buttonVariant` | `ButtonVariant` | `'outline'` | Button style |
| `closeOnClick` | `boolean` | `true` | Close on item click |
| `closeOnOutsideClick` | `boolean` | `true` | Close on outside click |
| `offset` | `number` | `4` | Distance from trigger |
| `minWidth` | `number` | `150` | Minimum menu width |

#### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `open` | - | Menu opened |
| `close` | - | Menu closed |
| `toggle` | `isOpen: boolean` | Menu toggled |

#### Slots

| Slot | Description |
|------|-------------|
| `default` | Dropdown items |
| `trigger` | Custom trigger |
| `header` | Menu header |
| `footer` | Menu footer |

### DropdownItem Component

#### Sizes

| Size | Padding | Font Size |
|------|---------|-----------|
| **sm** | 0.375rem 0.5rem | 0.8125rem |
| **md** | 0.5rem 0.75rem | 0.875rem |
| **lg** | 0.625rem 0.875rem | 0.9375rem |

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `title` | `string` | - | Item title |
| `description` | `string` | - | Item description |
| `shortcut` | `string` | - | Keyboard shortcut |
| `icon` | `Component` | - | Item icon |
| `trailingIcon` | `Component` | - | Trailing icon |
| `color` | `DropdownItemColor` | `'default'` | Color variant |
| `size` | `DropdownItemSize` | `'md'` | Item size |
| `disabled` | `boolean` | `false` | Disabled state |
| `divided` | `boolean` | `false` | Show top border |
| `tag` | `string` | `'button'` | HTML tag |

#### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `click` | `Event` | Item clicked |

#### Slots

| Slot | Description |
|------|-------------|
| `default` | Item content |
| `title` | Title content |
| `description` | Description content |
| `trailing` | Trailing content |

## Usage Examples

### Basic Dropdown

```vue
<template>
  <Dropdown label="Open Menu">
    <DropdownItem>Option 1</DropdownItem>
    <DropdownItem>Option 2</DropdownItem>
    <DropdownItem>Option 3</DropdownItem>
  </Dropdown>
</template>
```

### With Icons

```vue
<template>
  <Dropdown label="Actions" :icon="MoreIcon">
    <DropdownItem :icon="EditIcon">Edit</DropdownItem>
    <DropdownItem :icon="CopyIcon">Copy</DropdownItem>
    <DropdownItem :icon="DeleteIcon" color="danger">Delete</DropdownItem>
  </Dropdown>
</template>

<script setup lang="ts">
import MoreIcon from './icons/MoreIcon.vue'
import EditIcon from './icons/EditIcon.vue'
import CopyIcon from './icons/CopyIcon.vue'
import DeleteIcon from './icons/DeleteIcon.vue'
</script>
```

### Different Placements

```vue
<template>
  <Dropdown label="Bottom" placement="bottom">
    <DropdownItem>Item 1</DropdownItem>
  </Dropdown>

  <Dropdown label="Top" placement="top">
    <DropdownItem>Item 1</DropdownItem>
  </Dropdown>

  <Dropdown label="Right" placement="right">
    <DropdownItem>Item 1</DropdownItem>
  </Dropdown>

  <Dropdown label="Left" placement="left">
    <DropdownItem>Item 1</DropdownItem>
  </Dropdown>
</template>
```

### With Header and Footer

```vue
<template>
  <Dropdown label="Settings">
    <template #header>
      Account Settings
    </template>

    <DropdownItem>Profile</DropdownItem>
    <DropdownItem>Security</DropdownItem>
    <DropdownItem>Billing</DropdownItem>

    <template #footer>
      Signed in as user@example.com
    </template>
  </Dropdown>
</template>
```

### Item with Description

```vue
<template>
  <Dropdown label="Share">
    <DropdownItem
      title="Email"
      description="Share via email"
      :icon="MailIcon"
    />
    <DropdownItem
      title="Twitter"
      description="Share on Twitter"
      :icon="TwitterIcon"
    />
    <DropdownItem
      title="Copy Link"
      description="Copy to clipboard"
      :icon="LinkIcon"
      shortcut="⌘K"
    />
  </Dropdown>
</template>
```

### Hover Trigger

```vue
<template>
  <Dropdown
    label="Hover me"
    trigger="hover"
  >
    <DropdownItem>Item 1</DropdownItem>
    <DropdownItem>Item 2</DropdownItem>
  </Dropdown>
</template>
```

### Divided Items

```vue
<template>
  <Dropdown label="Menu">
    <DropdownItem>Profile</DropdownItem>
    <DropdownItem>Settings</DropdownItem>

    <DropdownItem divided>Help</DropdownItem>
    <DropdownItem divided>Logout</DropdownItem>
  </Dropdown>
</template>
```

### Color Variants

```vue
<template>
  <Dropdown label="Actions">
    <DropdownItem color="primary">Primary Action</DropdownItem>
    <DropdownItem color="success">Success Action</DropdownItem>
    <DropdownItem color="warning">Warning Action</DropdownItem>
    <DropdownItem color="error">Error Action</DropdownItem>
  </Dropdown>
</template>
```

### With Shortcuts

```vue
<template>
  <Dropdown label="File">
    <DropdownItem shortcut="⌘N">New</DropdownItem>
    <DropdownItem shortcut="⌘O">Open</DropdownItem>
    <DropdownItem shortcut="⌘S">Save</DropdownItem>
    <DropdownItem divided shortcut="⌘P">Print</DropdownItem>
  </Dropdown>
</template>
```

### Custom Trigger

```vue
<template>
  <Dropdown>
    <template #trigger>
      <Button variant="solid" color="primary">
        <Avatar :name="userName" size="sm" />
        <span>{{ userName }}</span>
      </Button>
    </template>

    <DropdownItem>Profile</DropdownItem>
    <DropdownItem>Settings</DropdownItem>
    <DropdownItem color="danger">Logout</DropdownItem>
  </Dropdown>
</template>

<script setup lang="ts">
import Avatar from './Avatar.vue'
import Button from './Button.vue'

const userName = ref('John Doe')
</script>
```

### Sizes

```vue
<template>
  <Dropdown label="Small" size="sm">
    <DropdownItem size="sm">Small Item</DropdownItem>
  </Dropdown>

  <Dropdown label="Medium" size="md">
    <DropdownItem size="md">Medium Item</DropdownItem>
  </Dropdown>

  <Dropdown label="Large" size="lg">
    <DropdownItem size="lg">Large Item</DropdownItem>
  </Dropdown>
</template>
```

## Integration Examples

### User Menu

```vue
<template>
  <Dropdown>
    <template #trigger>
      <Button variant="ghost">
        <Avatar :name="user.name" size="sm" />
        <span>{{ user.name }}</span>
      </Button>
    </template>

    <template #header>
      <div class="user-info">
        <div>{{ user.name }}</div>
        <div class="email">{{ user.email }}</div>
      </div>
    </template>

    <DropdownItem :icon="UserIcon">Profile</DropdownItem>
    <DropdownItem :icon="SettingsIcon">Settings</DropdownItem>
    <DropdownItem :icon="BillingIcon">Billing</DropdownItem>

    <DropdownItem divided :icon="HelpIcon">Help</DropdownItem>
    <DropdownItem divided :icon="LogoutIcon" color="danger">Logout</DropdownItem>
  </Dropdown>
</template>

<script setup lang="ts">
const user = ref({
  name: 'John Doe',
  email: 'john@example.com'
})
</script>
```

### Action Menu

```vue
<template>
  <Dropdown label="Actions" :icon="MoreIcon" button-variant="ghost">
    <DropdownItem :icon="EditIcon" @click="edit">Edit</DropdownItem>
    <DropdownItem :icon="CopyIcon" @click="duplicate">Duplicate</DropdownItem>
    <DropdownItem :icon="ArchiveIcon" @click="archive">Archive</DropdownItem>

    <DropdownItem divided :icon="DeleteIcon" color="danger" @click="remove">Delete</DropdownItem>
  </Dropdown>
</template>

<script setup lang="ts">
const edit = () => console.log('Edit')
const duplicate = () => console.log('Duplicate')
const archive = () => console.log('Archive')
const remove = () => console.log('Delete')
</script>
```

### Filter Menu

```vue
<template>
  <Dropdown label="Filter" :icon="FilterIcon">
    <template #header>
      Filter by Status
    </template>

    <DropdownItem
      v-for="filter in filters"
      :key="filter.id"
      :title="filter.label"
      :icon="filter.active ? CheckIcon : undefined"
      @click="toggleFilter(filter.id)"
    />

    <template #footer>
      <Button size="sm" @click="clearFilters">Clear All</Button>
    </template>
  </Dropdown>
</template>

<script setup lang="ts">
const filters = ref([
  { id: 1, label: 'Active', active: true },
  { id: 2, label: 'Inactive', active: false },
  { id: 3, label: 'Pending', active: false }
])

const toggleFilter = (id: number) => {
  const filter = filters.value.find(f => f.id === id)
  if (filter) filter.active = !filter.active
}

const clearFilters = () => {
  filters.value.forEach(f => f.active = false)
}
</script>
```

### Share Menu

```vue
<template>
  <Dropdown label="Share" :icon="ShareIcon">
    <DropdownItem
      :icon="MailIcon"
      title="Email"
      description="Send via email"
      @click="shareViaEmail"
    />
    <DropdownItem
      :icon="TwitterIcon"
      title="Twitter"
      description="Share on Twitter"
      @click="shareOnTwitter"
    />
    <DropdownItem
      :icon="LinkIcon"
      title="Copy Link"
      description="Copy to clipboard"
      shortcut="⌘C"
      @click="copyLink"
    />
  </Dropdown>
</template>

<script setup lang="ts">
const shareViaEmail = () => console.log('Share via email')
const shareOnTwitter = () => console.log('Share on Twitter')
const copyLink = () => console.log('Copy link')
</script>
```

### Sort Menu

```vue
<template>
  <Dropdown label="Sort" :icon="SortIcon">
    <DropdownItem
      v-for="option in sortOptions"
      :key="option.value"
      :title="option.label"
      :trailing-icon="currentSort === option.value ? CheckIcon : undefined"
      @click="setSort(option.value)"
    />
  </Dropdown>
</template>

<script setup lang="ts">
const sortOptions = ref([
  { label: 'Name (A-Z)', value: 'name-asc' },
  { label: 'Name (Z-A)', value: 'name-desc' },
  { label: 'Date (Newest)', value: 'date-desc' },
  { label: 'Date (Oldest)', value: 'date-asc' }
])

const currentSort = ref('name-asc')

const setSort = (value: string) => {
  currentSort.value = value
}
</script>
```

### Language Selector

```vue
<template>
  <Dropdown :label="currentLanguage" placement="bottom-end">
    <DropdownItem
      v-for="lang in languages"
      :key="lang.code"
      :title="lang.name"
      :trailing-icon="currentLanguage === lang.name ? CheckIcon : undefined"
      @click="setLanguage(lang)"
    />
  </Dropdown>
</template>

<script setup lang="ts">
const languages = ref([
  { code: 'en', name: 'English' },
  { code: 'es', name: 'Español' },
  { code: 'fr', name: 'Français' },
  { code: 'de', name: 'Deutsch' }
])

const currentLanguage = ref('English')

const setLanguage = (lang: any) => {
  currentLanguage.value = lang.name
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

- [x] All placements position correctly
- [x] All sizes render correctly
- [x] Click trigger opens/closes
- [x] Hover trigger works
- [x] Click outside closes
- [x] Item click emits event
- [x] Disabled state prevents interaction
- [x] Close on click works
- [x] Header and footer display
- [x] Icons display correctly
- [x] Arrow indicator shows/hides
- [x] Keyboard navigation (Enter, Escape)
- [x] Divided items show border
- [x] Color variants apply
- [x] Shortcuts display
- [x] Dark mode support

## Styling Features

### Position Calculation

Fixed positioning:
- Calculates based on trigger position
- Adjusts for scroll offset
- Supports 12 placement options
- Automatic arrow positioning

### Teleport to Body

Menu renders to body:
- Avoids z-index issues
- Prevents overflow clipping
- Smooth animations

### Transition Animation

Scale and fade:
- Smooth enter/leave
- Scale from 0.95 to 1
- Opacity from 0 to 1

## File Changes

### New Files

1. **src/components/Dropdown.vue** (~360 lines)
   - Dropdown container with placements
   - Size options
   - Trigger modes
   - Arrow indicator
   - Header/footer slots
   - Click outside handling
   - Keyboard navigation
   - Dark mode support

2. **src/components/DropdownItem.vue** (~250 lines)
   - Individual menu item
   - Size options
   - Color variants
   - Icon support
   - Description support
   - Shortcut display
   - Divided option
   - Disabled state
   - Dark mode support

## Benefits

### Over Native Select

| Aspect | Native Select | Dropdown Component |
|--------|---------------|-------------------|
| **Styling** | Limited | Full control |
| **Content** | Text only | Rich content |
| **Icons** | No | Yes |
| **Placement** | Fixed | Flexible |
| **Accessibility** | Basic | Enhanced |
| **Animations** | None | Smooth |
| **Headers/Footer** | No | Yes |
| **Customization** | Limited | Unlimited |

### Use Cases

1. **Actions Menus** - Action lists
2. **User Menus** - User options
3. **Settings** - Setting groups
4. **Filters** - Filter options
5. **Sort** - Sort options
6. **Share** - Share options
7. **Export** - Export formats
8. **Language** - Language selection

## Future Enhancements

### Potential Additions

1. **Submenus** - Nested dropdowns
2. **Keyboard Navigation** - Arrow keys
3. **Virtual Scroll** - For long lists
4. **Search** - Filter items
5. **Checkboxes** - Multi-select
6. **Radio Groups** - Single select
7. **Icons** - Custom icons
8. **Badges** - Item badges
9. **Loading State** - Async items
10. **Groups** - Item groups

## Integration Opportunities

The Dropdown component can be integrated with:

1. **Navigation** - Menu components
2. **Toolbars** - Action menus
3. **Tables** - Row actions
4. **Cards** - Card actions
5. **Headers** - User menu
6. **Forms** - Select alternatives
7. **Filters** - Filter controls
8. **Search** - Sort options

## CSS Architecture

### BEM Naming - Dropdown

- `.dropdown` - Block
- `.dropdown--open` - Modifier
- `.dropdown--disabled` - Modifier
- `.dropdown__trigger` - Element
- `.dropdown__menu` - Element
- `.dropdown__menu--placement` - Modifier
- `.dropdown__menu--size` - Modifier
- `.dropdown__arrow` - Element
- `.dropdown__header` - Element
- `.dropdown__items` - Element
- `.dropdown__footer` - Element

### BEM Naming - DropdownItem

- `.dropdown-item` - Block
- `.dropdown-item--size` - Modifier (e.g., `dropdown-item--sm`)
- `.dropdown-item--color` - Modifier (e.g., `dropdown-item--primary`)
- `.dropdown-item--disabled` - Modifier
- `.dropdown-item--divided` - Modifier
- `.dropdown-item__icon` - Element
- `.dropdown-item__content` - Element
- `.dropdown-item__title` - Element
- `.dropdown-item__description` - Element
- `.dropdown-item__text` - Element
- `.dropdown-item__trailing` - Element
- `.dropdown-item__shortcut` - Element

## Accessibility

- ARIA roles (menu, menuitem)
- Keyboard navigation (Enter, Escape, Space)
- Focus management
- Tab index
- Screen reader support

## Performance Considerations

### Teleport Overhead

Minimal performance cost:
- Only mounted when open
- Clean unmount on close
- Efficient DOM updates

### Position Calculation

Optimized calculations:
- Uses getBoundingClientRect
- Cached calculations
- Updates on scroll/resize

## Comparison with Select

| Feature | Select | Dropdown |
|---------|--------|----------|
| **Form** | Native form element | Custom component |
| **Mobile** | Native picker | Custom UI |
| **Styling** | Limited | Full control |
| **Content** | Text only | Any content |
| **Complexity** | Simple | Flexible |
| **Use Case** | Forms | Actions/Menus |

## Summary

Dropdown/DropdownItem Components successfully provide:

✅ **Multiple Placements** - 12 position options
✅ **Trigger Modes** - Click and hover
✅ **Flexible Sizing** - 3 size presets (sm, md, lg)
✅ **Color Variants** - 7 color options (default + 6 colors)
✅ **Rich Content** - Title, description, icons
✅ **Keyboard Support** - Enter, Escape, Space
✅ **Click Outside** - Auto-close behavior
✅ **Header/Footer** - Custom slots
✅ **Divided Items** - Visual separation
✅ **Disabled States** - Non-interactive items
✅ **Custom Trigger** - Slot override
✅ **Arrow Indicator** - Visual cue
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The components provide a versatile, reusable dropdown system for menus, actions, and option selection throughout the application.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

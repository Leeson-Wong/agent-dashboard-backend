# Chip Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Chips (also known as tags or labels) are compact elements that represent attributes, categories, or actions. A reusable Chip component provides consistent visual representation of metadata throughout the application.

The Chip component provides:
- Multiple variants (default, solid, outline, soft)
- Multiple sizes (xs, sm, md, lg)
- Multiple color schemes (default, primary, success, warning, error, info)
- Optional icon support (left or right)
- Optional avatar support
- Closable/removable option
- Disabled state
- Accessibility support

## Implementation

### Chip Component

**File**: `src/components/Chip.vue` (~290 lines)

#### Type Definitions

```typescript
export type ChipSize = 'xs' | 'sm' | 'md' | 'lg'
export type ChipVariant = 'default' | 'solid' | 'outline' | 'soft'
export type ChipColor = 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info'
```

## Feature Highlights

### Sizes

| Size | Padding | Font Size | Use Case |
|------|---------|-----------|----------|
| **xs** | 0.125rem 0.375rem | 0.6875rem | Compact lists |
| **sm** | 0.25rem 0.5rem | 0.75rem | Dense content |
| **md** | 0.375rem 0.625rem | 0.875rem | Default |
| **lg** | 0.5rem 0.75rem | 0.9375rem | Emphasized tags |

### Variants

| Variant | Background | Border | Use Case |
|---------|------------|--------|----------|
| **default** | Light gray | Gray | General purpose |
| **solid** | Solid color | Solid | High emphasis |
| **outline** | Transparent | Colored | Subtle |
| **soft** | Tinted | Light | Muted |

### Colors

| Color | Solid | Outline | Soft |
|-------|-------|---------|------|
| **primary** | Blue | Blue border | Light blue |
| **success** | Green | Green border | Light green |
| **warning** | Orange | Orange border | Light orange |
| **error** | Red | Red border | Light red |
| **info** | Cyan | Cyan border | Light cyan |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `label` | `string` | - | Chip text |
| `size` | `ChipSize` | `'md'` | Chip size |
| `variant` | `ChipVariant` | `'default'` | Chip variant |
| `color` | `ChipColor` | `'default'` | Chip color |
| `icon` | `Component` | - | Icon component |
| `iconRight` | `boolean` | `false` | Icon on right |
| `avatar` | `string \| Component` | - | Avatar image or component |
| `closable` | `boolean` | `false` | Show close button |
| `closeIcon` | `Component` | X icon | Custom close icon |
| `closeAriaLabel` | `string` | `'Remove'` | Close button aria-label |
| `disabled` | `boolean` | `false` | Disabled state |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `close` | - | Emitted when close clicked |

### Slots

| Slot | Description |
|------|-------------|
| `default` | Chip content (falls back to label) |

## Usage Examples

### Basic Chip

```vue
<template>
  <Chip label="Default" />
  <Chip label="Primary" color="primary" variant="solid" />
  <Chip label="Success" color="success" variant="solid" />
  <Chip label="Warning" color="warning" variant="solid" />
  <Chip label="Error" color="error" variant="solid" />
</template>
```

### Sizes

```vue
<template>
  <Chip label="Extra Small" size="xs" />
  <Chip label="Small" size="sm" />
  <Chip label="Medium" size="md" />
  <Chip label="Large" size="lg" />
</template>
```

### Variants

```vue
<template>
  <Chip label="Default" variant="default" />
  <Chip label="Solid" variant="solid" color="primary" />
  <Chip label="Outline" variant="outline" color="primary" />
  <Chip label="Soft" variant="soft" color="primary" />
</template>
```

### With Icons

```vue
<template>
  <Chip label="With Icon" :icon="CheckIcon" />
  <Chip label="Icon Right" :icon="ArrowIcon" :icon-right="true" />
</template>

<script setup lang="ts">
import CheckIcon from './icons/CheckIcon.vue'
import ArrowIcon from './icons/ArrowIcon.vue'
</script>
```

### With Avatar

```vue
<template>
  <Chip label="John Doe" :avatar="'https://i.pravatar.cc/150?img=1'" />
  <Chip label="Jane Smith" :avatar="UserAvatar" />
</template>

<script setup lang="ts">
import UserAvatar from './UserAvatar.vue'
</script>
```

### Closable Chips

```vue
<template>
  <div>
    <Chip
      v-for="tag in tags"
      :key="tag"
      :label="tag"
      closable
      @close="removeTag(tag)"
    />
  </div>
</template>

<script setup lang="ts">
const tags = ref(['React', 'Vue', 'Angular', 'Svelte'])

const removeTag = (tag: string) => {
  tags.value = tags.value.filter(t => t !== tag)
}
</script>
```

### Disabled Chip

```vue
<template>
  <Chip label="Disabled" disabled />
  <Chip label="Disabled Closable" closable disabled />
</template>
```

### Color Chips

```vue
<template>
  <Chip label="Primary" color="primary" variant="solid" />
  <Chip label="Success" color="success" variant="solid" />
  <Chip label="Warning" color="warning" variant="solid" />
  <Chip label="Error" color="error" variant="solid" />
  <Chip label="Info" color="info" variant="solid" />
</template>
```

### Outline Chips

```vue
<template>
  <Chip label="Primary" color="primary" variant="outline" />
  <Chip label="Success" color="success" variant="outline" />
  <Chip label="Warning" color="warning" variant="outline" />
  <Chip label="Error" color="error" variant="outline" />
  <Chip label="Info" color="info" variant="outline" />
</template>
```

### Soft Chips

```vue
<template>
  <Chip label="Primary" color="primary" variant="soft" />
  <Chip label="Success" color="success" variant="soft" />
  <Chip label="Warning" color="warning" variant="soft" />
  <Chip label="Error" color="error" variant="soft" />
  <Chip label="Info" color="info" variant="soft" />
</template>
```

## Integration Examples

### Category Tags

```vue
<template>
  <div class="product-card">
    <h2>{{ product.name }}</h2>
    <div class="tags">
      <Chip
        v-for="category in product.categories"
        :key="category"
        :label="category"
        size="sm"
        color="primary"
        variant="soft"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const product = ref({
  name: 'Wireless Headphones',
  categories: ['Electronics', 'Audio', 'Wireless']
})
</script>
```

### Status Indicators

```vue
<template>
  <div class="status-row">
    <span>Status:</span>
    <Chip
      :label="status"
      :color="statusColor"
      variant="solid"
      size="sm"
    />
  </div>
</template>

<script setup lang="ts">
const status = ref('active')
const statusColor = computed(() => {
  const colors = {
    active: 'success',
    pending: 'warning',
    inactive: 'error',
    draft: 'info'
  }
  return colors[status.value] || 'default'
})
</script>
```

### Filter Tags

```vue
<template>
  <div class="filters">
    <span class="filter-label">Active filters:</span>
    <Chip
      v-for="filter in activeFilters"
      :key="filter.id"
      :label="filter.label"
      closable
      @close="removeFilter(filter.id)"
      color="primary"
      variant="outline"
    />
    <button v-if="activeFilters.length" @click="clearAll">
      Clear all
    </button>
  </div>
</template>

<script setup lang="ts">
const activeFilters = ref([
  { id: 1, label: 'Price: $0-$100' },
  { id: 2, label: 'Brand: Apple' },
  { id: 3, label: 'In Stock' }
])

const removeFilter = (id: number) => {
  activeFilters.value = activeFilters.value.filter(f => f.id !== id)
}

const clearAll = () => {
  activeFilters.value = []
}
</script>
```

### User Tags

```vue
<template>
  <div class="user-list">
    <div
      v-for="user in users"
      :key="user.id"
      class="user-item"
    >
      <Chip
        :label="user.name"
        :avatar="user.avatar"
        :closable="user.canRemove"
        @close="removeUser(user.id)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const users = ref([
  { id: 1, name: 'Alice', avatar: 'https://i.pravatar.cc/150?img=1', canRemove: true },
  { id: 2, name: 'Bob', avatar: 'https://i.pravatar.cc/150?img=2', canRemove: true },
  { id: 3, name: 'Charlie', avatar: 'https://i.pravatar.cc/150?img=3', canRemove: false }
])

const removeUser = (id: number) => {
  users.value = users.value.filter(u => u.id !== id)
}
</script>
```

### Skill Tags

```vue
<template>
  <div class="skills">
    <Chip
      v-for="skill in skills"
      :key="skill.name"
      :label="skill.name"
      :color="skill.level"
      :variant="skill.level"
      size="sm"
    />
  </div>
</template>

<script setup lang="ts">
const skills = ref([
  { name: 'JavaScript', level: 'primary' as const },
  { name: 'TypeScript', level: 'success' as const },
  { name: 'Python', level: 'warning' as const },
  { name: 'Rust', level: 'error' as const },
  { name: 'Go', level: 'info' as const }
])
</script>
```

### Priority Labels

```vue
<template>
  <div class="task-priority">
    <Chip
      :label="task.priority"
      :color="priorityColor"
      variant="solid"
      size="xs"
    />
  </div>
</template>

<script setup lang="ts">
const task = ref({
  priority: 'High',
  title: 'Fix critical bug'
})

const priorityColor = computed(() => {
  const colors: Record<string, any> = {
    Critical: 'error',
    High: 'warning',
    Medium: 'info',
    Low: 'default'
  }
  return colors[task.value.priority] || 'default'
})
</script>
```

### Feature Badges

```vue
<template>
  <div class="feature-card">
    <h3>{{ feature.name }}</h3>
    <div class="badges">
      <Chip
        v-if="feature.new"
        label="New"
        color="success"
        variant="solid"
        size="xs"
      />
      <Chip
        v-if="feature.beta"
        label="Beta"
        color="warning"
        variant="solid"
        size="xs"
      />
      <Chip
        v-if="feature.pro"
        label="Pro"
        color="primary"
        variant="solid"
        size="xs"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const feature = ref({
  name: 'Advanced Analytics',
  new: true,
  beta: false,
  pro: true
})
</script>
```

### Multi-Select Input

```vue
<template>
  <div class="multi-select">
    <div class="selected-items">
      <Chip
        v-for="item in selectedItems"
        :key="item.id"
        :label="item.label"
        closable
        @close="removeItem(item.id)"
      />
    </div>
    <input
      v-model="search"
      type="text"
      placeholder="Add item..."
      @focus="showDropdown = true"
    />
  </div>
</template>

<script setup lang="ts">
const search = ref('')
const showDropdown = ref(false)
const selectedItems = ref([
  { id: 1, label: 'Item 1' },
  { id: 2, label: 'Item 2' }
])

const removeItem = (id: number) => {
  selectedItems.value = selectedItems.value.filter(i => i.id !== id)
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

- [x] All sizes render correctly
- [x] All variants display correctly
- [x] All colors apply correctly
- [x] Icon displays on left
- [x] Icon displays on right with iconRight
- [x] Avatar image displays
- [x] Avatar component displays
- [x] Close button appears when closable
- [x] Close event fires
- [x] Close button hover effect works
- [x] Disabled state prevents interaction
- [x] Slot content overrides label
- [x] Hover effects work
- [x] Dark mode support
- [x] Responsive text wrapping

## Styling Features

### Flex Layout

Inline-flex layout:
- Centers items vertically
- Consistent gap spacing
- Proper spacing with icons/avatar

### Border Radius

Rounded corners:
- 0.375rem (6px) default
- Consistent across all sizes
- Smooth visual appearance

### Close Button

Inline close button:
- Hover opacity change
- Hover background tint
- Focus outline for accessibility
- Prevents event bubbling

### Avatar Support

Flexible avatar display:
- String URL renders as img
- Component renders inline
- Circular masking
- Size-relative sizing

## File Changes

### New Files

1. **src/components/Chip.vue** (~290 lines)
   - Chip with all variants
   - Size options
   - Color schemes
   - Icon support
   - Avatar support
   - Closable functionality
   - Disabled state
   - Dark mode support

## Benefits

### Over Manual Chips

| Aspect | Manual | Chip Component |
|--------|--------|----------------|
| **Consistency** | Variable | Standardized |
| **Styling** | Manual CSS | Props-based |
| **Colors** | Manual classes | Built-in |
| **Close Handler** | Manual code | Event emission |
| **Accessibility** | Missing | ARIA support |
| **Maintenance** | Difficult | Easy |
| **Responsive** | Manual | Automatic |

### Use Cases

1. **Categories** - Product/service categories
2. **Tags** - Content tags
3. **Status** - Status indicators
4. **Filters** - Active filters
5. **Skills** - Skill tags
6. **Roles** - User roles
7. **Priorities** - Priority levels
8. **Features** - Feature badges
9. **Badges** - Achievement badges
10. **Multi-select** - Selected items

## Future Enhancements

### Potential Additions

1. **Clickable** - Make entire chip clickable
2. **Link** - Convert to anchor tag
3. **Checkbox** - Built-in checkbox
4. **Dropdown** - Dropdown menu
5. **Tooltip** - Built-in tooltip
6. **Count** - Badge count
7. **Edit** - Edit mode
8. **Draggable** - Drag and drop
9. **Grouped** - Chip group with overflow
10. **Animation** - Add/remove animations

## Integration Opportunities

The Chip component can be integrated with:

1. **Data Tables** - Column filters
2. **Forms** - Multi-select inputs
3. **Cards** - Category tags
4. **Lists** - Status indicators
5. **Dashboards** - Metric labels
6. **Search Results** - Refinement tags
7. **User Profiles** - Role badges
8. **Task Management** - Priority labels

## CSS Architecture

### BEM Naming

- `.chip` - Block
- `.chip--size` - Modifier (e.g., `chip--sm`)
- `.chip--variant` - Modifier (e.g., `chip--solid`)
- `.chip--color` - Modifier (e.g., `chip--primary`)
- `.chip__icon` - Element
- `.chip__avatar` - Element
- `.chip__label` - Element
- `.chip__close` - Element
- `.chip__close-icon` - Element

### Color System

CSS classes for each combination:
- `.chip--solid.chip--primary` - Solid primary
- `.chip--outline.chip--success` - Outline success
- `.chip--soft.chip--warning` - Soft warning

### Spacing System

Gap-based spacing:
- Gap adjusts with size
- Consistent across elements
- Visual rhythm

## Comparison with Badge

| Feature | Badge | Chip |
|---------|-------|------|
| **Purpose** | Counts/Status | Labels/Tags |
| **Content** | Numbers/Icons | Text/Icons |
| **Size** | Very small | Small to large |
| **Interactive** | Usually not | Closable/clickable |
| **Use Case** | Notifications | Categories |

## Summary

Chip Component successfully provides:

✅ **Multiple Sizes** - 4 size presets (xs to lg)
✅ **Multiple Variants** - 4 styles (default, solid, outline, soft)
✅ **Color Schemes** - 6 color options (default + 5 colors)
✅ **Icon Support** - Left or right icon placement
✅ **Avatar Support** - Image or component avatar
✅ **Closable** - Removable with close button
✅ **Disabled State** - Non-interactive when disabled
✅ **Custom Slots** - Label slot for customization
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable chip/tag system for labels, categories, status indicators, and filter tags throughout the application.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# Badge Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Badges are essential UI elements for displaying labels, counts, and status indicators throughout an application. A reusable Badge component provides consistency and reduces boilerplate code.

The Badge component provides:
- Multiple variants (default, primary, success, warning, error, info, dot)
- Multiple sizes (xs, sm, md, lg)
- Custom color support
- Count badges with max display
- Pulse animation support
- Outlined variant
- Accessibility support

## Implementation

### Badge Component

**File**: `src/components/Badge.vue` (~220 lines)

#### Type Definitions

```typescript
export type BadgeVariant =
  | 'default'    // Gray badge
  | 'primary'    // Blue badge
  | 'success'    // Green badge
  | 'warning'    // Yellow badge
  | 'error'      // Red badge
  | 'info'       // Light blue badge
  | 'dot'        // Dot indicator

export type BadgeSize = 'xs' | 'sm' | 'md' | 'lg'

interface Props {
  text?: string
  variant?: BadgeVariant
  size?: BadgeSize
  rounded?: boolean
  outlined?: boolean
  pulse?: boolean
  color?: string // Custom color
  backgroundColor?: string // Custom background color
  max?: number // Maximum count to display (for count badges)
  count?: number // Count to display
  showZero?: boolean // Show zero count
}
```

## Feature Highlights

### Variants

| Variant | Use Case | Color |
|---------|----------|-------|
| **default** | General purpose | Gray |
| **primary** | Primary actions/info | Blue |
| **success** | Success states | Green |
| **warning** | Warning states | Yellow |
| **error** | Error states | Red |
| **info** | Informational | Light blue |
| **dot** | Status indicator | Colored dot |

### Sizes

| Size | Font Size | Height | Use Case |
|------|-----------|--------|----------|
| **xs** | 0.65rem | 1rem | Compact lists |
| **sm** | 0.75rem | 1.25rem | Default |
| **md** | 0.875rem | 1.5rem | Cards |
| **lg** | 1rem | 1.75rem | Headers |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `text` | `string` | - | Badge text content |
| `variant` | `BadgeVariant` | `'default'` | Badge color variant |
| `size` | `BadgeSize` | `'sm'` | Badge size |
| `rounded` | `boolean` | `false` | Fully rounded corners |
| `outlined` | `boolean` | `false` | Outlined style |
| `pulse` | `boolean` | `false` | Pulse animation |
| `color` | `string` | - | Custom text color |
| `backgroundColor` | `string` | - | Custom background color |
| `max` | `number` | - | Max count to display |
| `count` | `number` | - | Count number |
| `showZero` | `boolean` | `false` | Show zero count |

## Usage Examples

### Basic Badge

```vue
<template>
  <Badge text="New" variant="success" />
  <Badge text="Error" variant="error" />
  <Badge text="Warning" variant="warning" />
</template>
```

### Count Badge

```vue
<template>
  <Badge :count="5" variant="primary" />
  <Badge :count="100" :max="99" variant="error" />
  <Badge :count="0" variant="default" :show-zero="true" />
</template>
```

### Sizes

```vue
<template>
  <Badge text="XS" size="xs" />
  <Badge text="SM" size="sm" />
  <Badge text="MD" size="md" />
  <Badge text="LG" size="lg" />
</template>
```

### Rounded and Outlined

```vue
<template>
  <Badge text="Rounded" variant="primary" :rounded="true" />
  <Badge text="Outlined" variant="success" :outlined="true" />
</template>
```

### Pulse Animation

```vue
<template>
  <Badge text="Live" variant="error" :pulse="true" />
</template>
```

### Dot Indicator

```vue
<template>
  <Badge variant="dot" />
</template>
```

### Custom Colors

```vue
<template>
  <Badge
    text="Custom"
    color="#ffffff"
    background-color="#8b5cf6"
  />
</template>
```

### Slot Content

```vue
<template>
  <Badge variant="primary">
    <Icon name="star" />
    <span>Favorite</span>
  </Badge>
</template>
```

## Integration Examples

### Agent Status Badge

```vue
<template>
  <div class="agent-item">
    <span>{{ agent.name }}</span>
    <Badge
      :text="agent.status"
      :variant="getStatusVariant(agent.status)"
      size="xs"
    />
  </div>
</template>

<script setup lang="ts">
const getStatusVariant = (status: string) => {
  const variants = {
    running: 'success',
    paused: 'warning',
    error: 'error',
    idle: 'default'
  }
  return variants[status] || 'default'
}
</script>
```

### Notification Badge

```vue
<template>
  <button class="notification-button">
    <Icon name="bell" />
    <Badge
      v-if="unreadCount > 0"
      :count="unreadCount"
      variant="error"
      size="xs"
      :rounded="true"
    />
  </button>
</template>
```

### Status Dot Indicator

```vue
<template>
  <div class="connection-status">
    <span>Connected</span>
    <Badge variant="dot" class="status-dot" />
  </div>
</template>
```

### Count Display with Max

```vue
<template>
  <div class="agent-count">
    Agents:
    <Badge :count="totalAgents" :max="999" variant="primary" />
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
- [x] All sizes render correctly
- [x] Count badges display correctly
- [x] Max count truncation works
- [x] Zero count hides when showZero is false
- [x] Rounded corners apply correctly
- [x] Outlined style applies correctly
- [x] Pulse animation works
- [x] Custom colors apply
- [x] Slot content renders
- [x] Dark mode support

## Styling Features

### Default Border Radius

Small border radius (0.25rem) for standard badges.

### Rounded Variant

Fully rounded (9999px) for pill-shaped badges.

### Responsive Sizing

- **XS**: Extra small for compact lists
- **SM**: Small (default) for most use cases
- **MD**: Medium for cards and panels
- **LG**: Large for headers and hero sections

### Accessibility

- Aria labels for screen readers
- Semantic HTML structure
- Keyboard-accessible when used with buttons
- High contrast colors

### Dark Mode

Automatic color adaptation for dark theme:
- Lighter gray for default badges
- Adjusted border colors for outlined variant

## File Changes

### New Files

1. **src/components/Badge.vue** (~220 lines)
   - Badge component with all variants
   - Size variants
   - Count badge support
   - Custom color support
   - Pulse animation
   - Dark mode support

## Benefits

### Over Inline Styling

| Aspect | Inline | Badge Component |
|--------|--------|-----------------|
| **Consistency** | Manual | Automatic |
| **Styling** | Repeated | Centralized |
| **Variants** | Hard-coded | Pre-configured |
| **Sizing** | Manual | Standardized |
| **Accessibility** | Manual | Built-in |
| **Maintenance** | Difficult | Easy |

### Use Cases

1. **Status Indicators** - Show agent/connection status
2. **Count Badges** - Display unread/total counts
3. **Labels** - Tag items with categories
4. **Notifications** - Show notification counts
5. **Priority** - Indicate priority levels
6. **State** - Show component states
7. **Filters** - Display active filter counts
8. **Dot Indicators** - Compact status dots

## Future Enhancements

### Potential Additions

1. **Positioning** - Built-in positioning support (top-right, etc.)
2. **Removable** - Close button for removable badges
3. **Clickable** - Built-in click handling
4. **Icon Support** - Pre-defined icon badges
5. **Gradient** - Gradient background support
6. **Glow Effect** - Subtle glow for emphasis
7. **Animation Variants** - More animation options
8. **Tooltip Integration** - Built-in tooltip support

## Integration Opportunities

The Badge component can be integrated with:

1. **Agent List Items** - Status badges
2. **Notification Icons** - Unread count badges
3. **Filter Buttons** - Active filter count
4. **Tabs** - Active tab indicator
5. **Cards** - Status/label badges
6. **Tables** - Column/row indicators
7. **Menus** - Item counts
8. **Headers** - Section labels
9. **Progress Indicators** - Step counts
10. **Tags** - Category labels

## CSS Architecture

### BEM Naming

- `.badge` - Block
- `.badge--variant` - Modifier (e.g., `badge--primary`)
- `.badge__dot` - Element

### Custom Properties

Easy customization via props:
- `color` - Custom text color
- `backgroundColor` - Custom background color

### Responsive Design

Size variants provide flexibility for different screen sizes and contexts.

## Summary

Badge Component successfully provides:

✅ **Multiple Variants** - 7 color variants including dot indicator
✅ **Flexible Sizing** - 4 size options (xs, sm, md, lg)
✅ **Count Support** - Display counts with max truncation
✅ **Custom Colors** - Override default colors
✅ **Pulse Animation** - Animated attention-grabbing
✅ **Outlined Style** - Alternative visual style
✅ **Rounded Option** - Pill-shaped badges
✅ **Accessibility** - ARIA labels and semantic HTML
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable UI element for displaying labels, counts, and status indicators throughout the application, improving visual consistency and reducing code duplication.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# Card Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Cards are fundamental container components for organizing and displaying content in a structured way. A reusable Card component provides consistency and reduces boilerplate code for content layout.

The Card component provides:
- Header, body, and footer sections
- Multiple variants (default, primary, success, warning, error, info)
- Multiple sizes (sm, md, lg)
- Elevation levels (shadow depth)
- Hoverable and clickable states
- Customizable padding and styling
- Overlay support
- Accessibility support

## Implementation

### Card Component

**File**: `src/components/Card.vue` (~230 lines)

#### Type Definitions

```typescript
export type CardVariant = 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info'
export type CardSize = 'sm' | 'md' | 'lg'
export type CardElevation = 'none' | 'xs' | 'sm' | 'md' | 'lg' | 'xl'

interface Props {
  title?: string // Card title
  subtitle?: string // Card subtitle
  variant?: CardVariant
  size?: CardSize
  elevation?: CardElevation
  hoverable?: boolean
  clickable?: boolean
  bordered?: boolean
  padding?: string
  bodyPadding?: string
  noPadding?: boolean
  rounded?: boolean
  backgroundColor?: string
}
```

## Feature Highlights

### Variants

| Variant | Use Case | Visual |
|---------|----------|--------|
| **default** | General purpose | White background |
| **primary** | Primary content | Blue tint, blue left border |
| **success** | Success states | Green tint, green left border |
| **warning** | Warning states | Yellow tint, yellow left border |
| **error** | Error states | Red tint, red left border |
| **info** | Informational | Cyan tint, cyan left border |

### Sizes

| Size | Font Size | Use Case |
|------|-----------|----------|
| **sm** | 0.875rem | Compact cards |
| **md** | 1rem | Default |
| **lg** | 1.125rem | Large cards |

### Elevation Levels

| Level | Shadow | Use Case |
|-------|--------|----------|
| **none** | None | Flat design |
| **xs** | Subtle | Background cards |
| **sm** | Light | Default elevation |
| **md** | Medium | Emphasized cards |
| **lg** | High | Popups/modals |
| **xl** | Highest | Floating elements |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `title` | `string` | - | Card title |
| `subtitle` | `string` | - | Card subtitle |
| `variant` | `CardVariant` | `'default'` | Card color variant |
| `size` | `CardSize` | `'md'` | Card size |
| `elevation` | `CardElevation` | `'sm'` | Shadow depth |
| `hoverable` | `boolean` | `false` | Hover effect |
| `clickable` | `boolean` | `false` | Clickable with cursor |
| `bordered` | `boolean` | `false` | Add border |
| `padding` | `string` | - | Custom padding |
| `noPadding` | `boolean` | `false` | Remove default padding |
| `rounded` | `boolean` | `true` | Rounded corners |
| `backgroundColor` | `string` | - | Custom background color |

### Slots

| Slot | Description |
|------|-------------|
| `header` | Custom header content |
| `default` | Card body content |
| `footer` | Card footer content |
| `actions` | Header action buttons |
| `overlay` | Overlay content (loading, etc.) |

## Usage Examples

### Basic Card

```vue
<template>
  <Card title="Card Title" subtitle="Card subtitle">
    <p>Card content goes here.</p>
  </Card>
</template>
```

### With Actions

```vue
<template>
  <Card title="Settings">
    <template #actions>
      <button>Save</button>
      <button>Cancel</button>
    </template>
    <p>Settings content</p>
  </Card>
</template>
```

### Variant Cards

```vue
<template>
  <Card title="Success" variant="success">
    <p>Operation completed successfully!</p>
  </Card>

  <Card title="Warning" variant="warning">
    <p>Please review before proceeding.</p>
  </Card>

  <Card title="Error" variant="error">
    <p>An error occurred.</p>
  </Card>
</template>
```

### Clickable Card

```vue
<template>
  <Card
    title="Agent Details"
    :clickable="true"
    @click="viewAgent(agent)"
  >
    <p>Click to view details</p>
  </Card>
</template>
```

### With Footer

```vue
<template>
  <Card title="Statistics">
    <div class="stats">
      <div>Active: 10</div>
      <div>Inactive: 5</div>
    </div>
    <template #footer>
      <span>Last updated: 2 mins ago</span>
      <button>Refresh</button>
    </template>
  </Card>
</template>
```

### With Overlay

```vue
<template>
  <Card title="Loading Data">
    <p>Data content</p>
    <template #overlay>
      <div class="loading">Loading...</div>
    </template>
  </Card>
</template>
```

### Custom Padding

```vue
<template>
  <Card
    title="Compact Card"
    :no-padding="true"
  >
    <div style="padding: 0.5rem;">
      Custom padding content
    </div>
  </Card>
</template>
```

### Custom Background Color

```vue
<template>
  <Card
    title="Custom Color"
    background-color="#f3e8ff"
  >
    <p>Custom styled card</p>
  </Card>
</template>
```

### High Elevation

```vue
<template>
  <Card
    title="Featured"
    elevation="lg"
    :hoverable="true"
  >
    <p>Featured content</p>
  </Card>
</template>
```

## Integration Examples

### Info Card

```vue
<template>
  <Card variant="info" title="Information">
    <p>This is an informational message for the user.</p>
  </Card>
</template>
```

### Agent Status Card

```vue
<template>
  <Card
    :title="agent.name"
    :subtitle="agent.role"
    :variant="getStatusVariant(agent.status)"
  >
    <div class="agent-details">
      <div>Status: {{ agent.status }}</div>
      <div>Tasks: {{ agent.taskCount }}</div>
    </div>
    <template #actions>
      <button @click="editAgent(agent)">Edit</button>
    </template>
  </Card>
</template>
```

### Stat Card

```vue
<template>
  <Card
    :title="title"
    :variant="variant"
    :hoverable="true"
    class="stat-card"
  >
    <div class="stat-value">{{ value }}</div>
    <div class="stat-change">{{ change }}</div>
    <template #footer>
      <span>{{ period }}</span>
    </template>
  </Card>
</template>
```

### Form Card

```vue
<template>
  <Card title="Edit Agent" elevation="md">
    <form @submit.prevent="save">
      <input v-model="agent.name" placeholder="Name" />
      <input v-model="agent.role" placeholder="Role" />
      <template #footer>
        <button type="button">Cancel</button>
        <button type="submit">Save</button>
      </template>
    </form>
  </Card>
</template>
```

### Content Card with Header Slot

```vue
<template>
  <Card>
    <template #header>
      <div class="custom-header">
        <Icon name="chart" />
        <div>
          <div class="title">Analytics</div>
          <div class="subtitle">Performance metrics</div>
        </div>
      </div>
    </template>
    <div class="analytics-content">
      <!-- Charts, tables, etc. -->
    </div>
  </Card>
</template>
```

### Grid of Cards

```vue
<template>
  <div class="card-grid">
    <Card
      v-for="item in items"
      :key="item.id"
      :title="item.title"
      :clickable="true"
      @click="openItem(item)"
    >
      <p>{{ item.description }}</p>
    </Card>
  </div>
</template>

<style scoped>
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}
</style>
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
- [x] All sizes work correctly
- [x] All elevation levels apply
- [x] Hoverable state works
- [x] Clickable state works
- [x] Bordered style applies
- [x] Padding customization works
- [x] No padding removes default padding
- [x] Rounded corners apply
- [x] Header with actions renders
- [x] Footer renders correctly
- [x] Overlay positions correctly
- [x] Slots render correctly
- [x] Dark mode support

## Styling Features

### Flexible Layout

Flexbox-based layout for responsive behavior:
- Vertical flex direction
- Body takes available space
- Header and footer don't grow

### Visual Hierarchy

Elevation levels create depth:
- Subtle shadows for background
- Higher shadows for emphasis
- Hover effects for interaction

### Color Variants

Each variant has:
- Background color tint
- Left border accent
- Dark mode support

### Interactive States

Hoverable and clickable states provide visual feedback:
- Shadow increase
- Subtle lift effect
- Active state press

## File Changes

### New Files

1. **src/components/Card.vue** (~230 lines)
   - Card component with all variants
   - Size variants
   - Elevation levels
   - Header, body, footer, overlay slots
   - Actions slot
   - Interactive states
   - Dark mode support

## Benefits

### Over Manual Styling

| Aspect | Manual Styling | Card Component |
|--------|----------------|----------------|
| **Consistency** | Variable | Standardized |
| **Styling** | Repeated CSS | Props-based |
| **Layout** | Manual structure | Built-in sections |
| **Interactivity** | Custom CSS | Built-in states |
| **Maintenance** | Difficult | Easy |
| **Accessibility** | Manual | Semantic HTML |

### Use Cases

1. **Content Containers** - Organize related content
2. **Dashboard Widgets** - Display stats and metrics
3. **Info Panels** - Show messages and alerts
4. **Form Containers** - Wrap form content
5. **List Items** - Card-based list layout
6. **Grid Layouts** - Card grids for content
7. **Status Cards** - Show status information
8. **Settings Panels** - Group settings

## Future Enhancements

### Potential Additions

1. **Collapse/Expand** - Built-in collapse functionality
2. **Draggable** - Drag and drop support
3. **Resizable** - User-controlled sizing
4. **Maximize** - Full-screen mode
5. **Tabs** - Multiple tabbed content
6. **Progress Bar** - Embedded progress indicator
7. **Image Support** - Card header image
8. **Close Button** - Dismissible cards

## Integration Opportunities

The Card component can be integrated with:

1. **Agent Lists** - Card-based agent display
2. **Statistics Panels** - Stat cards
3. **Settings Pages** - Settings cards
4. **Dashboard** - Dashboard widgets
5. **Forms** - Form containers
6. **Notifications** - Notification cards
7. **Messages** - Message bubbles
8. **Media Galleries** - Media cards
9. **Task Lists** - Task cards
10. **Documentation** - Content cards

## CSS Architecture

### BEM Naming

- `.card` - Block
- `.card--variant` - Modifier (e.g., `card--primary`)
- `.card__header` - Element
- `.card__body` - Element
- `.card__footer` - Element
- `.card__overlay` - Element

### Slot-Based Structure

Slots provide flexible content injection:
- `header` - Custom header
- `default` - Body content
- `footer` - Footer content
- `actions` - Header actions
- `overlay` - Overlay layer

### Responsive Design

Flexbox-based responsive layout:
- Auto-sizing sections
- Flexible body growth
- Consistent spacing

## Summary

Card Component successfully provides:

✅ **Flexible Structure** - Header, body, footer sections
✅ **Variant System** - 6 color variants with meanings
✅ **Size Options** - 3 size presets
✅ **Elevation Levels** - 6 shadow depth levels
✅ **Interactive States** - Hoverable and clickable modes
✅ **Slot System** - Flexible content injection
✅ **Customizable** - Padding, colors, borders
✅ **Overlay Support** - Loading states, etc.
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable container for organizing content throughout the application, improving layout consistency and reducing boilerplate code.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

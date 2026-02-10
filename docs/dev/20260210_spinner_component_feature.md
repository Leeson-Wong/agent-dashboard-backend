# Spinner Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Spinners (loading indicators) are essential UI elements for indicating that content is being loaded or processed. A reusable Spinner component provides consistency and improves perceived performance throughout the application.

The Spinner component provides:
- Multiple variants (ring, dots, bars, pulse)
- Multiple sizes (xs, sm, md, lg, xl)
- Customizable colors
- Optional label text
- Center alignment option
- Accessibility support
- Reduced motion support

## Implementation

### Spinner Component

**File**: `src/components/Spinner.vue` (~170 lines)

#### Type Definitions

```typescript
export type SpinnerSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl'
export type SpinnerVariant = 'ring' | 'dots' | 'bars' | 'pulse'
```

## Feature Highlights

### Variants

| Variant | Description | Best For |
|---------|-------------|----------|
| **ring** | Circular rotating ring | General purpose |
| **dots** | Bouncing dots | Friendly loading |
| **bars** | Stretching bars | Compact spaces |
| **pulse** | Pulsing circles | Modern feel |

### Sizes

| Size | Ring Size | Use Case |
|------|-----------|----------|
| **xs** | 1rem | Inline with text |
| **sm** | 1.25rem | Small components |
| **md** | 1.5rem | Default |
| **lg** | 2rem | Large components |
| **xl** | 2.5rem | Hero sections |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `variant` | `SpinnerVariant` | `'ring'` | Spinner variant |
| `size` | `SpinnerSize` | `'md'` | Spinner size |
| `color` | `string` | `'currentColor'` | Spinner color |
| `label` | `string` | - | Loading text |
| `ariaLabel` | `string` | `'Loading...'` | ARIA label |
| `center` | `boolean` | `false` | Center align content |

## Usage Examples

### Basic Spinner

```vue
<template>
  <Spinner />
</template>
```

### All Variants

```vue
<template>
  <Spinner variant="ring" />
  <Spinner variant="dots" />
  <Spinner variant="bars" />
  <Spinner variant="pulse" />
</template>
```

### Sizes

```vue
<template>
  <Spinner size="xs" />
  <Spinner size="sm" />
  <Spinner size="md" />
  <Spinner size="lg" />
  <Spinner size="xl" />
</template>
```

### With Label

```vue
<template>
  <Spinner label="Loading..." />
</template>
```

### Custom Color

```vue
<template>
  <Spinner color="#3b82f6" />
</template>
```

### Centered

```vue
<template>
  <div class="loading-container">
    <Spinner center />
  </div>
</template>
```

### With Button Loading State

```vue
<template>
  <Button :loading="isLoading">
    <Spinner v-if="isLoading" size="sm" class="button-spinner" />
    <span>Submit</span>
  </Button>
</template>

<script setup lang="ts">
const isLoading = ref(false)
</script>
```

### Full Page Loading

```vue
<template>
  <div class="page-loading">
    <Spinner size="xl" label="Loading application..." />
  </div>
</template>

<style scoped>
.page-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style>
```

### Inline Loading

```vue
<template>
  <div>
    <Spinner size="xs" />
    <span>Loading data...</span>
  </div>
</template>
```

### Card Loading

```vue
<template>
  <Card>
    <Spinner center label="Loading content..." />
  </Card>
</template>
```

### Colored Spinners

```vue
<template>
  <Spinner color="#3b82f6" label="Primary action" />
  <Spinner color="#22c55e" label="Success" />
  <Spinner color="#f59e0b" label="Warning" />
  <Spinner color="#ef4444" label="Error" />
</template>
```

## Integration Examples

### Table Loading

```vue
<template>
  <table class="data-table">
    <tbody>
      <tr v-if="loading">
        <td :colspan="columns.length">
          <div class="table-loading">
            <Spinner center label="Loading data..." />
          </div>
        </td>
      </tr>
      <tr v-else v-for="row in data" :key="row.id">
        <!-- Table rows -->
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
const loading = ref(true)
const columns = ref([{ id: 'name' }, { id: 'email' }])
const data = ref([])
</script>
```

### Async Content Loading

```vue
<template>
  <div>
    <div v-if="isLoading" class="content-loading">
      <Spinner size="lg" center />
    </div>

    <div v-else>
      <div v-for="item in items" :key="item.id">
        {{ item.name }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts>
const isLoading = ref(true)
const items = ref([])

onMounted(async () => {
  items.value = await fetchItems()
  isLoading.value = false
})
</script>
```

### Form Submission

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <Input v-model="name" label="Name" />
    <Textarea v-model="description" label="Description" />

    <Button type="submit" :loading="isSubmitting">
      <template v-if="isSubmitting">
        <Spinner size="xs" />
      </template>
      <span>{{ isSubmitting ? 'Submitting...' : 'Submit' }}</span>
    </Button>
  </form>
</template>

<script setup lang="ts">
const isSubmitting = ref(false)
const name = ref('')
const description = ref('')

const handleSubmit = async () => {
  isSubmitting.value = true
  await submitForm({ name: name.value, description: description.value })
  isSubmitting.value = false
}
</script>
```

### Overlay Loading

```vue
<template>
  <div class="container">
    <div v-if="isLoading" class="loading-overlay">
      <Spinner size="lg" center label="Processing..." />
    </div>

    <div class="content">
      <!-- Your content here -->
    </div>
  </div>
</template>

<style scoped>
.loading-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.container {
  position: relative;
}
</style>
```

### Skeleton Screen with Spinner

```vue
<template>
  <div class="skeleton-screen">
    <div class="skeleton-header">
      <SkeletonAvatar class="skeleton-avatar" :size="lg" />
      <SkeletonText class="skeleton-title" />
    </div>

    <div class="skeleton-content">
      <Spinner v-if="loading" center />
      <div v-else>
        <!-- Actual content -->
      </div>
    </div>
  </div>
</template>
```

### Infinite Scroll Loading

```vue
<template>
  <div class="infinite-list" @scroll="handleScroll">
    <div v-for="item in items" :key="item.id">
      {{ item.name }}
    </div>

    <div v-if="loadingMore" class="load-more">
      <Spinner center label="Loading more..." />
    </div>
  </div>
</template>

<script setup lang="ts">
const items = ref([])
const loadingMore = ref(false)

const handleScroll = async (event: Event) => {
  const target = event.target as HTMLElement
  if (target.scrollTop + target.clientHeight >= target.scrollHeight - 100) {
    loadingMore.value = true
    const newItems = await fetchMoreItems()
    items.value.push(...newItems)
    loadingMore.value = false
  }
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

- [x] All variants animate correctly
- [x] All sizes render correctly
- [x] Ring spinner rotates smoothly
- [x] Dots bounce in sequence
- [x] Bars stretch in sequence
- [x] Pulse scales in sequence
- [x] Custom colors apply
- [x] Label displays correctly
- [x] Center alignment works
- [x] CurrentColor inherits parent color
- [x] Animations loop continuously
- [x] Reduced motion pauses animations
- [x] ARIA role and label present
- [x] Dark mode support

## Styling Features

### Smooth Animations

CSS keyframe animations:
- Ring: Rotate + dash offset
- Dots: Bounce with delays
- Bars: Stretch with delays
- Pulse: Scale with delays

### Animation Delays

Staggered animations for each element:
- Dots: 3 elements with 0.16s delay
- Bars: 4 elements with 0.1s delay
- Pulse: 3 elements with 0.16s delay

### Reduced Motion

Respects user preferences:
- Pauses all animations
- Maintains visibility
- Accessibility best practice

## File Changes

### New Files

1. **src/components/Spinner.vue** (~170 lines)
   - Spinner with all variants
   - Size options
   - Custom colors
   - Label support
   - Center alignment
   - Reduced motion support
   - Dark mode support

## Benefits

### Over Custom Solutions

| Aspect | Custom | Spinner Component |
|--------|--------|-------------------|
| **Consistency** | Variable | Standardized |
| **Animation** | Manual CSS | Built-in |
| **Accessibility** | Missing | ARIA support |
| **Variants** | Manual | Multiple options |
| **Sizing** | Manual CSS | Props-based |
| **Color** | Manual CSS | Configurable |
| **Responsive** | Manual | Automatic |

### Use Cases

1. **Data Loading** - Table/list loading
2. **Form Submission** - Submit processing
3. **Page Loading** - Initial page load
4. **Async Operations** - API calls
5. **Button States** - Button loading
6. **Overlay Loading** - Modal/page overlay
7. **Inline Loading** - Text with spinner
8. **Skeleton Screens** - With skeleton components

## Future Enhancements

### Potential Additions

1. **Progress** - Combined with progress bar
2. **Percentage** - Show loading percent
3. ** ETA** - Estimated time
4. **Steps** - Show current step
5. **Custom SVG** - Custom SVG slot
6. **Dual Ring** - Dual ring animation
7. **Eclipse** - Eclipse-style spinner
8. **Wave** - Wave animation

## Integration Opportunities

The Spinner component can be integrated with:

1. **Buttons** - Loading button state
2. **Tables** - Table loading state
3. **Cards** - Card loading state
4. **Lists** - List loading state
5. **Modals** - Modal loading state
6. **Pages** - Page loading state
7. **Forms** - Form submission
8. **Infinite Scroll** - Load more indicator

## CSS Architecture

### BEM Naming

- `.spinner` - Block
- `.spinner--variant` - Modifier (e.g., `spinner--ring`)
- `.spinner--size` - Modifier (e.g., `spinner--sm`)
- `.spinner__ring` - Element (ring SVG)
- `.spinner__ring-path` - Element (ring circle)
- `.spinner__dots` - Element (dots container)
- `.spinner__dot` - Element (individual dot)
- `.spinner__bars` - Element (bars container)
- `.spinner__bar` - Element (individual bar)
- `.spinner__pulse` - Element (pulse container)
- `.spinner__pulse-circle` - Element (pulse circle)
- `.spinner__label` - Element (text label)

### Animation Timing

Stagger delays for visual interest:
- Calculated for smooth flow
- Different per variant
- Optimized for visual perception

### Accessibility

- ARIA role="status"
- aria-label for screen readers
- Reduced motion support
- Semantic HTML structure
- Keyboard accessible (inherited)

## Performance Considerations

### CSS Animations

- Use `transform` and `opacity` (GPU accelerated)
- Avoid layout-triggering properties
- Minimal repaints
- Smooth 60fps animations

### Reduced Motion

Media query support:
- Disables all animations
- Maintains visibility
- Respects user preferences
- Accessibility requirement

## Comparison with Progress Bar

| Feature | Spinner | Progress Bar |
|---------|---------|--------------|
| **Duration** | Indeterminate | Known/determinate |
| **Precision** | Rough estimate | Exact percentage |
| **Context** | General loading | Specific progress |
| **Space** | Minimal | Can be large |
| **Use Case** | API calls, forms | File uploads, steps |

## Summary

Spinner Component successfully provides:

✅ **Multiple Variants** - 4 styles (ring, dots, bars, pulse)
✅ **Flexible Sizing** - 5 size presets (xs to xl)
✅ **Custom Colors** - Override default color
✅ **Label Support** - Optional loading text
✅ **Center Alignment** - Built-in centering
✅ **Smooth Animations** - 60fps CSS animations
✅ **Staggered Timing** - Sequential element animation
✅ **Reduced Motion** - Accessibility support
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable loading indicator system for all loading states throughout the application.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

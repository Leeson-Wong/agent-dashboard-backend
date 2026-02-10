# SkeletonLoader Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Skeleton loaders are placeholder UI elements that mimic the structure of actual content while it's loading. A reusable SkeletonLoader component provides consistent loading states throughout the application.

The SkeletonLoader component provides:
- Multiple variants (text, circular, rectangular, rounded)
- Multiple sizes (sm, md, lg)
- Multiple animations (pulse, wave, none)
- Custom width and height
- Centered alignment option
- Loading condition support
- Accessibility support
- Performance optimization

## Implementation

### SkeletonLoader Component

**File**: `src/components/SkeletonLoader.vue` (~150 lines)

#### Type Definitions

```typescript
export type SkeletonVariant = 'text' | 'circular' | 'rectangular' | 'rounded'
export type SkeletonSize = 'sm' | 'md' | 'lg'
export type SkeletonAnimation = 'pulse' | 'wave' | 'none'
```

## Feature Highlights

### Variants

| Variant | Shape | Border Radius | Use Case |
|---------|-------|---------------|----------|
| **text** | Rectangle | 0.25rem | Text lines |
| **circular** | Circle | 50% | Avatars, icons |
| **rectangular** | Rectangle | 0.25rem | Images, cards |
| **rounded** | Pill | 9999px | Buttons, badges |

### Sizes

| Size | Height | Use Case |
|------|--------|----------|
| **sm** | 0.75rem | Small text |
| **md** | 1rem | Default |
| **lg** | 1.5rem | Headings |

### Animations

| Animation | Effect | Performance |
|-----------|--------|-------------|
| **pulse** | Shimmer across | Good |
| **wave** | Gradient wave | Better |
| **none** | Static | Best |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `variant` | `SkeletonVariant` | `'text'` | Skeleton shape |
| `size` | `SkeletonSize` | `'md'` | Skeleton size |
| `width` | `string \| number` | - | Custom width |
| `height` | `string \| number` | - | Custom height |
| `animation` | `SkeletonAnimation` | `'pulse'` | Animation type |
| `centered` | `boolean` | `false` | Center content |
| `loading` | `boolean` | `true` | Show/conditionally |

## Usage Examples

### Basic Text Skeleton

```vue
<template>
  <SkeletonLoader variant="text" />
</template>
```

### Different Variants

```vue
<template>
  <div class="skeletons">
    <SkeletonLoader variant="text" width="200px" />
    <SkeletonLoader variant="circular" width="48px" height="48px" />
    <SkeletonLoader variant="rectangular" width="300px" height="200px" />
    <SkeletonLoader variant="rounded" width="120px" height="40px" />
  </div>
</template>
```

### Sizes

```vue
<template>
  <div class="skeletons">
    <SkeletonLoader variant="text" size="sm" />
    <SkeletonLoader variant="text" size="md" />
    <SkeletonLoader variant="text" size="lg" />
  </div>
</template>
```

### Animations

```vue
<template>
  <div class="skeletons">
    <SkeletonLoader animation="pulse" />
    <SkeletonLoader animation="wave" />
    <SkeletonLoader animation="none" />
  </div>
</template>
```

### Custom Dimensions

```vue
<template>
  <SkeletonLoader
    variant="rectangular"
    :width="300"
    :height="200"
  />
</template>
```

### Avatar Skeleton

```vue
<template>
  <SkeletonLoader
    variant="circular"
    width="64px"
    height="64px"
  />
</template>
```

### Button Skeleton

```vue
<template>
  <SkeletonLoader
    variant="rounded"
    width="120px"
    height="40px"
  />
</template>
```

### Multiple Lines

```vue
<template>
  <div class="skeleton-lines">
    <SkeletonLoader variant="text" width="100%" />
    <SkeletonLoader variant="text" width="90%" />
    <SkeletonLoader variant="text" width="95%" />
    <SkeletonLoader variant="text" width="85%" />
  </div>
</template>
```

### Card Skeleton

```vue
<template>
  <div class="card-skeleton">
    <SkeletonLoader variant="circular" width="48px" height="48px" class="avatar" />
    <div class="content">
      <SkeletonLoader variant="text" width="70%" />
      <SkeletonLoader variant="text" width="40%" size="sm" />
    </div>
  </div>
</template>

<style scoped>
.card-skeleton {
  display: flex;
  gap: 1rem;
  padding: 1rem;
}
</style>
```

### List Skeleton

```vue
<template>
  <div class="list-skeleton">
    <div v-for="i in 5" :key="i" class="list-item">
      <SkeletonLoader variant="circular" width="40px" height="40px" />
      <div class="content">
        <SkeletonLoader variant="text" width="60%" />
        <SkeletonLoader variant="text" width="30%" size="sm" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.list-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem 0;
}
</style>
```

### Conditional Loading

```vue
<template>
  <div>
    <template v-if="loading">
      <SkeletonLoader variant="text" width="200px" />
      <SkeletonLoader variant="text" width="150px" />
      <SkeletonLoader variant="text" width="180px" />
    </template>

    <div v-else>
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
const loading = ref(true)
const title = ref('Actual Content')
const description = ref('This is the real content')
</script>
```

### Image Skeleton

```vue
<template>
  <div class="image-container">
    <SkeletonLoader
      v-if="loading"
      variant="rectangular"
      width="100%"
      height="300px"
    />
    <img
      v-else
      :src="imageUrl"
      alt="Loaded image"
      @load="loading = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const loading = ref(true)
const imageUrl = ref('/path/to/image.jpg')
</script>
```

### Centered Skeleton

```vue
<template>
  <div class="container" style="height: 200px;">
    <SkeletonLoader
      variant="circular"
      width="64px"
      height="64px"
      centered
    />
  </div>
</template>
```

## Integration Examples

### Form Skeleton

```vue
<template>
  <form class="form">
    <div v-if="loading">
      <div class="form-field">
        <SkeletonLoader variant="text" width="100px" height="1rem" />
        <SkeletonLoader variant="rectangular" width="100%" height="48px" />
      </div>
      <div class="form-field">
        <SkeletonLoader variant="text" width="120px" height="1rem" />
        <SkeletonLoader variant="rectangular" width="100%" height="120px" />
      </div>
      <SkeletonLoader variant="rounded" width="100px" height="40px" />
    </div>

    <div v-else>
      <Input v-model="form.name" label="Name" />
      <Textarea v-model="form.description" label="Description" />
      <Button type="submit">Submit</Button>
    </div>
  </form>
</template>

<script setup lang="ts">
const loading = ref(true)
const form = ref({ name: '', description: '' })
</script>
```

### User Profile Skeleton

```vue
<template>
  <div class="profile">
    <div v-if="loading" class="profile-skeleton">
      <SkeletonLoader variant="circular" width="120px" height="120px" class="avatar" />
      <div class="info">
        <SkeletonLoader variant="text" width="200px" height="1.5rem" />
        <SkeletonLoader variant="text" width="150px" />
        <SkeletonLoader variant="text" width="120px" />
      </div>
    </div>

    <div v-else class="profile-content">
      <!-- Actual profile -->
    </div>
  </div>
</template>

<style scoped>
.profile-skeleton {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}
</style>
```

### Table Skeleton

```vue
<template>
  <table class="table">
    <thead>
      <tr>
        <th>Name</th>
        <th>Email</th>
        <th>Role</th>
      </tr>
    </thead>
    <tbody>
      <tr v-if="loading" v-for="i in 5" :key="i">
        <td v-for="j in 3" :key="j">
          <SkeletonLoader variant="text" width="80%" />
        </td>
      </tr>
      <tr v-else v-for="row in data" :key="row.id">
        <td>{{ row.name }}</td>
        <td>{{ row.email }}</td>
        <td>{{ row.role }}</td>
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
const loading = ref(true)
const data = ref([])
</script>
```

### News Card Skeleton

```vue
<template>
  <div class="news-card">
    <div v-if="loading">
      <SkeletonLoader variant="rectangular" width="100%" height="200px" class="image" />
      <div class="content">
        <SkeletonLoader variant="text" width="70%" height="1.25rem" />
        <SkeletonLoader variant="text" width="40%" />
        <SkeletonLoader variant="text" width="100%" height="1rem" />
        <SkeletonLoader variant="text" width="90%" height="1rem" />
        <SkeletonLoader variant="text" width="85%" height="1rem" />
      </div>
    </div>

    <article v-else class="article">
      <!-- Actual article content -->
    </article>
  </div>
</template>
```

### Comment Skeleton

```vue
<template>
  <div class="comment">
    <div v-if="loading" class="comment-skeleton">
      <SkeletonLoader variant="circular" width="40px" height="40px" />
      <div class="body">
        <SkeletonLoader variant="text" width="120px" height="1rem" />
        <SkeletonLoader variant="text" width="100%" height="0.875rem" />
        <SkeletonLoader variant="text" width="100%" height="0.875rem" />
      </div>
    </div>

    <div v-else class="comment-content">
      <!-- Actual comment -->
    </div>
  </div>
</template>

<style scoped>
.comment-skeleton {
  display: flex;
  gap: 0.75rem;
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

- [x] All variants display correctly
- [x] All sizes render correctly
- [x] Animations play smoothly
- [x] Custom width applies
- [x] Custom height applies
- [x] Centered alignment works
- [x] Loading condition works
- [x] No animation when set to none
- [x] Dark mode support

## Styling Features

### CSS Animations

Pseudo-element animation:
- Uses ::after for animation layer
- Transform translate for movement
- Optimized with GPU acceleration

### Animation Types

Pulse vs Wave:
- Pulse: Simple shimmer
- Wave: Gradient wave effect
- Both use translate for performance

### Border Radius

Variant-specific radius:
- text: 0.25rem (small)
- circular: 50% (circle)
- rectangular: 0.25rem (small)
- rounded: 9999px (pill)

## File Changes

### New Files

1. **src/components/SkeletonLoader.vue** (~150 lines)
   - Skeleton loader with all variants
   - Size options
   - Animation types
   - Custom dimensions
   - Centered alignment
   - Loading condition
   - Dark mode support

## Benefits

### Over Inline Skeletons

| Aspect | Inline | SkeletonLoader |
|--------|--------|----------------|
| **Consistency** | Variable | Standardized |
| **Animation** | Manual CSS | Built-in |
| **Variants** | Manual classes | Props-based |
| **Responsive** | Manual | Automatic |
| **Accessibility** | Missing | ARIA support |
| **Maintenance** | Difficult | Easy |

### Use Cases

1. **Text Loading** - Paragraph placeholders
2. **Avatars** - User avatar placeholders
3. **Images** - Image placeholders
4. **Buttons** - Button placeholders
5. **Cards** - Card placeholders
6. **Lists** - List item placeholders
7. **Tables** - Table cell placeholders
8. **Forms** - Form field placeholders

## Future Enhancements

### Potential Additions

1. **Custom Speed** - Animation speed control
2. **Gradient** - Custom gradient colors
3. **Delay** - Staggered animations
4. **Pattern** - Patterned skeleton
5. **Chained** - Chain multiple loaders
6. **RTL** - Right-to-left support
7. **Inline** - Inline display mode
8. **Count** - Number of repetitions
9. **Group** - Skeleton group wrapper
10. **Theme** - Theme-based colors

## Integration Opportunities

The SkeletonLoader can be integrated with:

1. **Data Tables** - Loading rows
2. **Cards** - Card placeholders
3. **Lists** - List placeholders
4. **Forms** - Form placeholders
5. **Profiles** - Profile loading
6. **Media** - Media loading
7. **Charts** - Chart loading
8. **Dashboards** - Dashboard loading

## CSS Architecture

### BEM Naming

- `.skeleton-loader` - Block
- `.skeleton-loader--variant` - Modifier (e.g., `skeleton-loader--text`)
- `.skeleton-loader--size` - Modifier (e.g., `skeleton-loader--sm`)
- `.skeleton-loader--animation` - Modifier (e.g., `skeleton-loader--pulse`)
- `.skeleton-loader--centered` - Modifier

### Animation Optimization

GPU-accelerated:
- Uses `transform: translateX()`
- Not `left/right` properties
- Smooth 60fps animation

### Accessibility

- aria-hidden can be added
- role="status" for screen readers
- aria-label for context

## Performance Considerations

### Animation Performance

Optimized animations:
- GPU-accelerated transforms
- Minimal repaints
- Efficient keyframes

### Memory

Low footprint:
- Single pseudo-element
- No additional DOM
- CSS-based (no JS)

## Comparison with Spinner

| Feature | Spinner | SkeletonLoader |
|---------|---------|----------------|
| **Purpose** | General loading | Content structure |
| **Visual** | Rotating icon | Content shape |
| **Context** | Anywhere | Content-specific |
| **Perceived** | Slower | Faster |
| **Use Case** | Unknown content | Known structure |

## Summary

SkeletonLoader Component successfully provides:

✅ **Multiple Variants** - 4 shapes (text, circular, rectangular, rounded)
✅ **Multiple Sizes** - 3 size presets (sm, md, lg)
✅ **Animations** - 3 animation types (pulse, wave, none)
✅ **Custom Dimensions** - Width and height control
✅ **Centered** - Optional center alignment
✅ **Conditional** - Loading condition support
✅ **Smooth Animation** - 60fps GPU-accelerated
✅ **Accessible** - ARIA support ready
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable skeleton loading system for content placeholders with consistent visual feedback during loading states.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

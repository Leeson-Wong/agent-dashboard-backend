# Image Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Image components provide enhanced image display with loading states, error handling, placeholder support, and various fitting options. They're essential for displaying images with consistent styling and behavior. A reusable Image component provides consistent image handling throughout the application.

The Image component provides:
- Loading state display
- Error handling with fallback
- Placeholder support
- Multiple fit modes (contain, cover, fill, none, scale-down)
- Multiple shapes (square, circle, rounded)
- Custom width/height
- Lazy loading support
- Fallback image support
- Overlay content slot
- Accessibility support
- Dark mode support

## Implementation

### Image Component

**File**: `src/components/Image.vue` (~200 lines)

#### Type Definitions

```typescript
export type ImageFit = 'contain' | 'cover' | 'fill' | 'none' | 'scale-down'
export type ImageShape = 'square' | 'circle' | 'rounded'

interface Props {
  src: string
  alt?: string
  width?: number | string
  height?: number | string
  fit?: ImageFit
  shape?: ImageShape
  loading?: 'lazy' | 'eager'
  placeholder?: boolean
  fallback?: string
  preview?: boolean
}
```

## Feature Highlights

### Fit Modes

| Fit Mode | Description | Use Case |
|----------|-------------|----------|
| **cover** | Covers container | Standard display |
| **contain** | Shows entire image | Full image visible |
| **fill** | Stretches to fill | Fill container |
| **none** | Natural size | Actual dimensions |
| **scale-down** | Shrink to fit | Scale down only |

### Shapes

| Shape | Border Radius | Use Case |
|-------|--------------|----------|
| **square** | None | Standard |
| **circle** | 50% | Avatars |
| **rounded** | 0.375rem | Cards |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `src` | `string` | **required** | Image source URL |
| `alt` | `string` | `''` | Alt text for accessibility |
| `width` | `number \| string` | `undefined` | Image width |
| `height` | `number \| string` | `undefined` | Image height |
| `fit` | `ImageFit` | `'cover'` | Object-fit behavior |
| `shape` | `ImageShape` | `'rounded'` | Image shape |
| `loading` | `'lazy' \| 'eager'` | `'lazy'` | Loading mode |
| `placeholder` | `boolean` | `false` | Show placeholder |
| `fallback` | `string` | `undefined` | Fallback image URL |
| `preview` | `boolean` | `false` | Preview mode |

### Slots

| Slot | Description |
|------|-------------|
| `loading` | Custom loading content |
| `error` | Custom error content |
| `placeholder` | Custom placeholder |
| `overlay` | Overlay content |

## Usage Examples

### Basic Image

```vue
<template>
  <Image
    src="/photo.jpg"
    alt="A beautiful sunset"
    :width="400"
    :height="300"
  />
</template>
```

### Circle Avatar

```vue
<template>
  <Image
    src="/avatar.jpg"
    alt="User avatar"
    :width="100"
    :height="100"
    shape="circle"
  />
</template>
```

### With Placeholder

```vue
<template>
  <Image
    src="/photo.jpg"
    alt="Photo"
    :width="400"
    :height="300"
    :placeholder="true"
  />
</template>
```

### With Fallback

```vue
<template>
  <Image
    src="/maybe-missing.jpg"
    alt="Photo"
    :width="400"
    :height="300"
    fallback="/fallback.jpg"
  />
</template>
```

### Lazy Loading

```vue
<template>
  <Image
    src="/large-photo.jpg"
    alt="Large photo"
    :width="800"
    :height="600"
    loading="lazy"
  />
</template>
```

### Object Fit Modes

```vue
<template>
  <div>
    <Image src="/photo.jpg" fit="cover" :width="200" :height="200" />
    <Image src="/photo.jpg" fit="contain" :width="200" :height="200" />
    <Image src="/photo.jpg" fit="fill" :width="200" :height="200" />
    <Image src="/photo.jpg" fit="none" :width="200" :height="200" />
    <Image src="/photo.jpg" fit="scale-down" :width="200" :height="200" />
  </div>
</template>
```

### With Overlay

```vue
<template>
  <Image src="/photo.jpg" :width="400" :height="300">
    <template #overlay>
      <div class="overlay-content">
        <h3>Photo Title</h3>
        <p>Click to view</p>
      </div>
    </template>
  </Image>
</template>

<style scoped>
.overlay-content {
  text-align: center;
  color: white;
}
</style>
```

### Custom Loading

```vue
<template>
  <Image src="/photo.jpg" :width="400" :height="300">
    <template #loading>
      <div class="custom-loading">
        <div class="spinner"></div>
        <p>Loading...</p>
      </div>
    </template>
  </Image>
</template>
```

### Custom Error

```vue
<template>
  <Image src="/missing.jpg" :width="400" :height="300">
    <template #error>
      <div class="custom-error">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="16" />
        </svg>
        <p>Image not available</p>
      </div>
    </template>
  </Image>
</template>
```

### Responsive Images

```vue
<template>
  <Image
    src="/photo.jpg"
    alt="Responsive photo"
    :width="width"
    :height="height"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const width = ref(400)
const height = ref(300)

const updateDimensions = () => {
  if (window.innerWidth < 600) {
    width.value = 300
    height.value = 225
  }
}

onMounted(() => {
  updateDimensions()
  window.addEventListener('resize', updateDimensions)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateDimensions)
})
</script>
```

## Integration Examples

### User Avatar

```vue
<template>
  <div class="user-avatar">
    <Image
      :src="user.avatar"
      :alt="user.name"
      :width="80"
      :height="80"
      shape="circle"
      :placeholder="true"
    />
    <p>{{ user.name }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const user = ref({
  name: 'John Doe',
  avatar: '/avatar.jpg'
})
</script>
```

### Gallery Image

```vue
<template>
  <div class="gallery-item">
    <Image
      :src="image.url"
      :alt="image.title"
      :width="300"
      :height="200"
      fit="cover"
      shape="rounded"
      :placeholder="true"
      :fallback="image.fallback"
      loading="lazy"
    >
      <template #overlay>
        <div class="gallery-overlay">
          <h3>{{ image.title }}</h3>
        </div>
      </template>
    </Image>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const image = ref({
  url: '/gallery/photo1.jpg',
  title: 'Sunset at the beach',
  fallback: '/gallery/placeholder.jpg'
})
</script>
```

### Product Image

```vue
<template>
  <div class="product-image">
    <Image
      :src="product.image"
      :alt="product.name"
      :width="400"
      :height="400"
      fit="contain"
      :fallback="product.placeholder"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const product = ref({
  name: 'Premium Headphones',
  image: '/products/headphones.jpg',
  placeholder: '/products/placeholder.jpg'
})
</script>
```

### Hero Background

```vue
<template>
  <div class="hero">
    <Image
      src="/hero-bg.jpg"
      alt="Hero background"
      :width="1920"
      :height="600"
      fit="cover"
      class="hero__bg"
    />
    <div class="hero__content">
      <h1>Welcome</h1>
      <p>Subtitle</p>
    </div>
  </div>
</template>

<style scoped>
.hero {
  position: relative;
  height: 600px;
}

.hero__bg {
  position: absolute;
  inset: 0;
}

.hero__content {
  position: relative;
  z-index: 1;
  padding: 2rem;
  color: white;
}
</style>
```

### Profile Cover

```vue
<template>
  <div class="profile-cover">
    <Image
      src="/cover-photo.jpg"
      alt="Profile cover"
      :width="1200"
      :height="300"
      fit="cover"
    />
    <div class="profile-info">
      <Image
        :src="avatar"
        :alt="name"
        :width="120"
        :height="120"
        shape="circle"
        class="avatar"
      />
      <h2>{{ name }}</h2>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const avatar = ref('/avatar.jpg')
const name = ref('John Doe')
</script>

<style scoped>
.profile-cover {
  position: relative;
}

.profile-info {
  display: flex;
  align-items: flex-end;
  padding: 1rem;
  margin-top: -60px;
  position: relative;
}

.avatar {
  border: 4px solid white;
}
</style>
```

### Card Image

```vue
<template>
  <Card>
    <Image
      :src="article.image"
      :alt="article.title"
      :width="400"
      :height="250"
      fit="cover"
      shape="rounded"
    />
    <div class="card-content">
      <h3>{{ article.title }}</h3>
      <p>{{ article.excerpt }}</p>
    </div>
  </Card>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const article = ref({
  title: 'Amazing Article',
  excerpt: 'This is a brief excerpt...',
  image: '/articles/article1.jpg'
})
</script>
```

### Thumbnail Grid

```vue
<template>
  <div class="thumbnail-grid">
    <Image
      v-for="img in images"
      :key="img.id"
      :src="img.url"
      :alt="img.title"
      :width="150"
      :height="150"
      fit="cover"
      shape="rounded"
      loading="lazy"
      @click="selectImage(img)"
      class="thumbnail"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const images = ref([
  { id: 1, url: '/thumb1.jpg', title: 'Thumbnail 1' },
  { id: 2, url: '/thumb2.jpg', title: 'Thumbnail 2' },
  { id: 3, url: '/thumb3.jpg', title: 'Thumbnail 3' },
  { id: 4, url: '/thumb4.jpg', title: 'Thumbnail 4' }
])

const selectImage = (image: any) => {
  console.log('Selected:', image.title)
}
</script>

<style scoped>
.thumbnail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
}

.thumbnail {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.thumbnail:hover {
  transform: scale(1.05);
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

- [x] Image displays correctly
- [x] Loading state shows
- [x] Error state shows
- [x] Placeholder displays
- [x] Fallback works
- [x] All fit modes work
- [x] All shapes work
- [x] Custom width/height works
- [x] Lazy loading works
- [x] Overlay content works
- [x] Custom slots work
- [x] Alt text applied
- [x] Dark mode support

## Styling Features

### Object Fit

Image positioning:
- cover - Covers area
- contain - Shows all
- fill - Stretches
- none - Natural
- scale-down - Shrinks

### Shapes

Border radius:
- square - No radius
- circle - 50% radius
- rounded - 0.375rem radius

### States

Visual feedback:
- Loading spinner
- Error icon
- Placeholder icon
- Overlay option

## File Changes

### New Files

1. **src/components/Image.vue** (~200 lines)
   - Loading state
   - Error handling
   - Fallback support
   - Placeholder
   - Fit modes
   - Shapes
   - Lazy loading
   - Overlay slot
   - Dark mode support

## Benefits

### Over HTML Image

| Aspect | HTML img | Image Component |
|--------|----------|---------------|
| **Loading** | Manual | Built-in state |
| **Error** | Broken | Fallback support |
| **Placeholder** | Manual | Built-in |
| **Styling** | Manual CSS | Consistent |
| **Fit** | Manual CSS | Prop-based |
| **Shapes** | Manual CSS | Built-in |
| **Accessibility** | Manual | Enhanced |

### Use Cases

1. **Avatars** - User profile images
2. **Galleries** - Photo collections
3. **Products** - Product images
4. **Heroes** - Background images
5. **Cards** - Card media
6. **Thumbnails** - Preview images
7. **Covers** - Profile covers
8. **Banners** - Ad banners

## Future Enhancements

### Potential Additions

1. **Zoom** - Image zoom on click
2. **Preview** - Lightbox preview
3. **Crop** - Image cropping
4. **Rotate** - Image rotation
5. **Filter** - Image filters
6. **Caption** - Image caption
6. **Gallery** - Built-in gallery
7. **Slider** - Image carousel
8. **Compare** - Before/after
9. **Upload** - File upload
10. **Edit** - Image editing

## Integration Opportunities

The Image component can be integrated with:

1. **Avatars** - User avatars
2. **Galleries** - Photo galleries
3. **Products** - Product images
4. **Blogs** - Article images
5. **Banners** - Ad banners
6. **Profiles** - Profile covers
7. **Cards** - Card media
8. **Thumbnails** - Previews

## CSS Architecture

### BEM Naming

- `.image` - Block
- `.image--shape` - Modifier (e.g., `image--circle`)
- `.image--fit` - Modifier (e.g., `image--cover`)
- `.image--loading` - Modifier
- `.image--error` - Modifier
- `.image--loaded` - Modifier
- `.image__img` - Element
- `.image__loading` - Element
- `.image__error` - Element
- `.image__placeholder` - Element
- `.image__overlay` - Element

## Accessibility

- Alt text support
- ARIA attributes
- Loading announcement
- Error announcement
- Keyboard accessible
- Screen reader support

## Performance Considerations

### Lazy Loading

Efficient loading:
- Native lazy loading
- Viewport detection
- Bandwidth saving

### Fallback

Error handling:
- Automatic fallback
- Prevents broken images
- User-friendly display

## Comparison with Img

| Feature | Image Component | HTML img |
|---------|----------------|----------|
| **Loading** | Built-in | Manual |
| **Error** | Fallback | Broken icon |
| **Placeholder** | Built-in | Manual |
| **Styling** | Props | CSS |
| **Fit** | Prop | CSS |
| **Shapes** | Prop | CSS |

## Summary

Image Component successfully provides:

✅ **Loading State** - Loading indicator
✅ **Error Handling** - Fallback image
✅ **Placeholder** - Default placeholder
✅ **Fit Modes** - 5 object-fit options
✅ **Shapes** - square, circle, rounded
✅ **Custom Size** - Width/height props
✅ **Lazy Loading** - Native lazy loading
✅ **Overlay Content** - Overlay slot
✅ **Custom Slots** - Loading/error/placeholder
✅ **Fallback Image** - Error fallback
✅ **Accessible** - Alt text support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable image system with enhanced loading states, error handling, and multiple display options.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

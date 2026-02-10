# BackTop Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

BackTop components provide a convenient way for users to return to the top of a page or scrollable container. They're especially useful for long pages with lots of content. A reusable BackTop component provides consistent back-to-top behavior throughout the application.

The BackTop component provides:
- Scroll visibility detection
- Smooth scroll animation
- Multiple position options (left/right)
- Multiple sizes (sm, md, lg)
- Multiple color variants
- Customizable visibility height
- Custom scroll container support
- Custom icon slot
- Multiple easing functions
- Accessibility support
- Dark mode support

## Implementation

### BackTop Component

**File**: `src/components/BackTop.vue` (~200 lines)

#### Type Definitions

```typescript
export type BackTopPosition = 'right' | 'left'
export type BackTopSize = 'sm' | 'md' | 'lg'
export type BackTopVariant = 'default' | 'primary' | 'success' | 'warning' | 'error'

interface Props {
  visibilityHeight?: number
  target?: string | (() => HTMLElement)
  duration?: number
  position?: BackTopPosition
  size?: BackTopSize
  variant?: BackTopVariant
  right?: number | string
  bottom?: number | string
  easing?: string
}
```

## Feature Highlights

### Positions

| Position | Location | Use Case |
|----------|----------|----------|
| **right** | Right side | Standard position |
| **left** | Left side | Alternative position |

### Sizes

| Size | Dimensions | Icon Size | Use Case |
|------|------------|-----------|----------|
| **sm** | 32×32px | 0.875rem | Compact button |
| **md** | 40×40px | 1rem | Standard size |
| **lg** | 48×48px | 1.25rem | Large button |

### Variants

| Variant | Background | Text | Hover | Use Case |
|---------|------------|------|-------|----------|
| **default** | White | Gray | Light gray | Standard |
| **primary** | Blue | White | Dark blue | Primary |
| **success** | Green | White | Dark green | Success |
| **warning** | Orange | White | Dark orange | Warning |
| **error** | Red | White | Dark red | Error |

### Easing Options

| Easing | Description |
|--------|-------------|
| **linear** | Constant speed |
| **ease-in** | Accelerating |
| **ease-out** | Decelerating |
| **ease-in-out** | Accelerate then decelerate |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `visibilityHeight` | `number` | `400` | Scroll height before showing (px) |
| `target` | `string \| function` | `undefined` | Scroll container selector or function |
| `duration` | `number` | `450` | Scroll animation duration (ms) |
| `position` | `BackTopPosition` | `'right'` | Button position |
| `size` | `BackTopSize` | `'md'` | Button size |
| `variant` | `BackTopVariant` | `'default'` | Color variant |
| `right` | `number \| string` | `40` | Distance from right/left (px) |
| `bottom` | `number \| string` | `40` | Distance from bottom (px) |
| `easing` | `string` | `'ease-in-out'` | Scroll easing function |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `click` | - | Button clicked |

### Slots

| Slot | Description |
|------|-------------|
| `default` | Custom icon/content |

### Exposed Methods

| Method | Description |
|--------|-------------|
| `scrollToTop()` | Programmatically scroll to top |

## Usage Examples

### Basic BackTop

```vue
<template>
  <div>
    <!-- Long content -->
    <BackTop />
  </div>
</template>
```

### Custom Visibility Height

```vue
<template>
  <BackTop
    :visibility-height="200"
  />
</template>
```

### Custom Position

```vue
<template>
  <BackTop
    position="left"
    :right="20"
    :bottom="100"
  />
</template>
```

### Color Variants

```vue
<template>
  <div>
    <BackTop variant="default" />
    <BackTop variant="primary" />
    <BackTop variant="success" />
    <BackTop variant="warning" />
    <BackTop variant="error" />
  </div>
</template>
```

### Size Variants

```vue
<template>
  <div>
    <BackTop size="sm" />
    <BackTop size="md" />
    <BackTop size="lg" />
  </div>
</template>
```

### Custom Scroll Container

```vue
<template>
  <div id="custom-container" class="scroll-container">
    <!-- Long content -->
  </div>
  <BackTop target="#custom-container" />
</template>

<style scoped>
.scroll-container {
  height: 400px;
  overflow-y: auto;
}
</style>
```

### Custom Duration and Easing

```vue
<template>
  <BackTop
    :duration="1000"
    easing="linear"
  />
</template>
```

### Custom Icon

```vue
<template>
  <BackTop>
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M12 19V5M5 12l7-7 7 7" />
    </svg>
  </BackTop>
</template>
```

### With Text

```vue
<template>
  <BackTop size="lg">
    <div class="back-top-content">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="18 15 12 9 6 15" />
      </svg>
      <span>Top</span>
    </div>
  </BackTop>
</template>

<style scoped>
.back-top-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
}
</style>
```

### With Click Handler

```vue
<template>
  <BackTop
    @click="handleClick"
  />
</template>

<script setup lang="ts">
const handleClick = () => {
  console.log('Scrolling to top')
}
</script>
```

### Programmatic Control

```vue
<template>
  <div>
    <Button @click="scrollToTop">Scroll to Top</Button>
    <BackTop ref="backTopRef" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const backTopRef = ref()

const scrollToTop = () => {
  backTopRef.value?.scrollToTop()
}
</script>
```

## Integration Examples

### Long Page with BackTop

```vue
<template>
  <div class="long-page">
    <h1>Long Page</h1>
    <div v-for="i in 100" :key="i" class="section">
      <h2>Section {{ i }}</h2>
      <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>
    </div>
    <BackTop />
  </div>
</template>
```

### Documentation Page

```vue
<template>
  <div class="documentation">
    <div class="sidebar">
      <!-- Table of contents -->
    </div>
    <div class="content">
      <h1>Documentation</h1>
      <!-- Long documentation content -->
    </div>
    <BackTop
      position="left"
      :right="20"
      variant="primary"
    />
  </div>
</template>
```

### Chat Application

```vue
<template>
  <div class="chat-app">
    <div id="messages-container" class="messages">
      <div v-for="msg in messages" :key="msg.id" class="message">
        {{ msg.text }}
      </div>
    </div>
    <BackTop
      target="#messages-container"
      :visibility-height="300"
      variant="primary"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const messages = ref([
  { id: 1, text: 'Hello' },
  { id: 2, text: 'How are you?' }
  // ... many messages
])
</script>

<style scoped>
.messages {
  height: 500px;
  overflow-y: auto;
}
</style>
```

### Feed Timeline

```vue
<template>
  <div class="feed">
    <h1>Activity Feed</h1>
    <div id="feed-container" class="feed-container">
      <article v-for="item in feedItems" :key="item.id" class="feed-item">
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
      </article>
    </div>
    <BackTop
      target="#feed-container"
      :visibility-height="500"
      variant="success"
      :duration="600"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const feedItems = ref([
  { id: 1, title: 'Item 1', content: 'Content 1' },
  { id: 2, title: 'Item 2', content: 'Content 2' }
  // ... many items
])
</script>
```

### Settings Panel

```vue
<template>
  <div class="settings-panel">
    <div id="settings-container" class="settings-content">
      <h2>Settings</h2>
      <section v-for="section in settings" :key="section.id">
        <h3>{{ section.title }}</h3>
        <!-- Settings options -->
      </section>
    </div>
    <BackTop
      target="#settings-container"
      size="sm"
      variant="warning"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const settings = ref([
  { id: 1, title: 'General' },
  { id: 2, title: 'Privacy' },
  { id: 3, title: 'Notifications' }
  // ... many settings sections
])
</script>
```

### Product Listings

```vue
<template>
  <div class="product-list">
    <h1>Products</h1>
    <div id="products-container" class="products">
      <div v-for="product in products" :key="product.id" class="product-card">
        <img :src="product.image" :alt="product.name" />
        <h3>{{ product.name }}</h3>
        <p>{{ product.price }}</p>
      </div>
    </div>
    <BackTop
      target="#products-container"
      :visibility-height="600"
      variant="primary"
      :duration="500"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const products = ref([
  { id: 1, name: 'Product 1', price: '$99', image: '/p1.jpg' },
  { id: 2, name: 'Product 2', price: '$149', image: '/p2.jpg' }
  // ... many products
])
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

- [x] BackTop displays correctly
- [x] Visibility on scroll works
- [x] Smooth scroll animation works
- [x] All positions work
- [x] All sizes render correctly
- [x] All variants display correctly
- [x] Custom target works
- [x] Custom visibility height works
- [x] Custom duration works
- [x] Custom easing works
- [x] Custom icon slot works
- [x] Click event emits
- [x] Exposed method works
- [x] Dark mode support

## Styling Features

### Fixed Positioning

Stays in place:
- Fixed position
- Customizable right/left
- Customizable bottom
- High z-index

### Smooth Animation

Animated transitions:
- Fade in/out
- Slide effect
- Smooth scroll

### Hover Effects

Interactive feedback:
- Shadow increase
- Scale effect
- Color change

## File Changes

### New Files

1. **src/components/BackTop.vue** (~200 lines)
   - Scroll visibility
   - Smooth scroll
   - Position options
   - Size variants
   - Color variants
   - Custom target
   - Easing functions
   - Custom icon
   - Dark mode support

## Benefits

### Over Manual Implementation

| Aspect | Manual | BackTop Component |
|--------|--------|-------------------|
| **Scroll Detection** | Manual | Automatic |
| **Animation** | Manual CSS | JS animation |
| **Positioning** | Manual | Configurable |
| **Container** | Window only | Custom target |
| **Easing** | Linear | Multiple options |
| **Accessibility** | Missing | Full ARIA |

### Use Cases

1. **Long Pages** - Scroll back to top
2. **Documentation** - Navigate long docs
3. **Chat Apps** - Scroll to latest
4. **Feeds** - Return to top
5. **Lists** - Navigate long lists
6. **Settings** - Scroll settings panel
7. **Products** - Navigate product list
8. **Comments** - Scroll comments section

## Future Enhancements

### Potential Additions

1. **Progress** - Scroll progress indicator
2. **Tooltip** - Hover tooltip
3. **Badge** - Notification badge
4. **Keyboard Shortcut** - Ctrl+Home support
5. **Auto Hide** - Auto-hide after timeout
6. **Multiple Targets** - Multiple containers
7. **Threshold** - Scroll threshold percentage
8. **Custom Shape** - Circle/square options
9. **Animation Delay** - Show/hide delay
10. **Scroll Progress** - Visual progress bar

## Integration Opportunities

The BackTop component can be integrated with:

1. **Long Pages** - Content pages
2. **Documentation** - Doc sites
3. **Blogs** - Article pages
4. **Feeds** - Social feeds
5. **Chats** - Message lists
6. **Lists** - Data tables
7. **Settings** - Settings panels
8. **Products** - E-commerce listings

## CSS Architecture

### BEM Naming

- `.back-top` - Block
- `.back-top--position` - Modifier (e.g., `back-top--right`)
- `.back-top--size` - Modifier (e.g., `back-top--sm`)
- `.back-top--variant` - Modifier (e.g., `back-top--primary`)

## Accessibility

- ARIA role="button"
- aria-label for screen readers
- Keyboard accessible
- Focus indicators
- Screen reader support

## Performance Considerations

### Scroll Handling

Efficient listening:
- Throttled scroll events
- requestAnimationFrame
- Minimal DOM reads

### Animation

Smooth scrolling:
- requestAnimationFrame
- Efficient easing
- Minimal reflows

## Comparison with Scroll

| Feature | BackTop | Scroll |
|---------|---------|--------|
| **Purpose** | Navigation | Behavior |
| **Visibility** | Conditional | Always |
| **Position** | Fixed | Inline |
| **Use Case** | Page top | Any position |

## Summary

BackTop Component successfully provides:

✅ **Scroll Visibility** - Shows after scroll threshold
✅ **Smooth Animation** - Animated scroll to top
✅ **Position Options** - Left/right positions
✅ **Size Variants** - 3 size presets (sm, md, lg)
✅ **Color Variants** - 5 color options
✅ **Custom Target** - Scroll container support
✅ **Custom Position** - Right/left/bottom options
✅ **Easing Functions** - Multiple easing options
✅ **Custom Duration** - Animation duration
✅ **Custom Icon** - Icon slot support
✅ **Events** - Click event
✅ **Exposed Methods** - scrollToTop method
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable back-to-top system with smooth scrolling and multiple customization options.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

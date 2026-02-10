# Popover Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Popover components are floating content containers that display information relative to a trigger element. They're commonly used for tooltips, contextual menus, and additional information display. A reusable Popover component provides consistent floating content behavior throughout the application.

The Popover component provides:
- 12 placement options (top, bottom, left, right, with start/end variants)
- 4 trigger modes (click, hover, focus, manual)
- Auto placement calculation
- Arrow indicator
- Multiple sizes (sm, md, lg)
- v-model support for controlled state
- Close on click outside
- Close on escape key
- Viewport boundary detection
- Accessibility support
- Dark mode support

## Implementation

### Popover Component

**File**: `src/components/Popover.vue` (~360 lines)

#### Type Definitions

```typescript
export type PopoverPlacement =
  | 'top'
  | 'top-start'
  | 'top-end'
  | 'bottom'
  | 'bottom-start'
  | 'bottom-end'
  | 'left'
  | 'left-start'
  | 'left-end'
  | 'right'
  | 'right-start'
  | 'right-end'
  | 'auto'

export type PopoverTrigger = 'click' | 'hover' | 'focus' | 'manual'
export type PopoverSize = 'sm' | 'md' | 'lg'
```

## Feature Highlights

### Placements

| Placement | Description |
|-----------|-------------|
| **top** | Above trigger, centered |
| **top-start** | Above trigger, left aligned |
| **top-end** | Above trigger, right aligned |
| **bottom** | Below trigger, centered |
| **bottom-start** | Below trigger, left aligned |
| **bottom-end** | Below trigger, right aligned |
| **left** | Left of trigger, centered |
| **left-start** | Left of trigger, top aligned |
| **left-end** | Left of trigger, bottom aligned |
| **right** | Right of trigger, centered |
| **right-start** | Right of trigger, top aligned |
| **right-end** | Right of trigger, bottom aligned |
| **auto** | Automatically choose best position |

### Triggers

| Trigger | Description |
|---------|-------------|
| **click** | Toggle on click |
| **hover** | Show on hover, hide on leave |
| **focus** | Show on focus, hide on blur |
| **manual** | Controlled via v-model or methods |

### Sizes

| Size | Padding | Title Size | Body Size |
|------|---------|------------|-----------|
| **sm** | 0.5rem | 0.875rem | 0.8125rem |
| **md** | 0.75rem | 0.9375rem | 0.875rem |
| **lg** | 1rem | 1rem | 0.9375rem |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `title` | `string` | `undefined` | Popover title |
| `content` | `string` | `undefined` | Popover content (when not using slot) |
| `placement` | `PopoverPlacement` | `'bottom'` | Position relative to trigger |
| `trigger` | `PopoverTrigger` | `'click'` | How to trigger popover |
| `size` | `PopoverSize` | `'md'` | Popover size |
| `disabled` | `boolean` | `false` | Disabled state |
| `offset` | `number` | `8` | Distance from trigger (px) |
| `showArrow` | `boolean` | `true` | Show arrow indicator |
| `width` | `number \| string` | `undefined` | Fixed width |
| `maxWidth` | `number \| string` | `undefined` | Maximum width |
| `delay` | `number` | `100` | Delay for hover trigger (ms) |
| `closeOnClickOutside` | `boolean` | `true` | Close when clicking outside |
| `closeOnEscape` | `boolean` | `true` | Close on Escape key |
| `persistent` | `boolean` | `false` | Don't close on hover leave |
| `triggerText` | `string` | `'Trigger'` | Default trigger button text |
| `modelValue` | `boolean` | `undefined` | Controlled open state (v-model) |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `boolean` | Open state changed (v-model) |
| `open` | - | Popover opened |
| `close` | - | Popover closed |
| `before-enter` | - | Before animation starts |
| `after-leave` | - | After animation ends |

### Slots

| Slot | Description |
|------|-------------|
| `trigger` | Custom trigger element |
| `default` | Popover content |

### Exposed Methods

| Method | Description |
|--------|-------------|
| `open()` | Programmatically open popover |
| `close()` | Programmatically close popover |
| `toggle()` | Toggle popover state |

## Usage Examples

### Basic Popover

```vue
<template>
  <Popover
    title="Popover Title"
    content="This is the popover content."
  />
</template>
```

### With Custom Trigger

```vue
<template>
  <Popover>
    <template #trigger>
      <Button>Hover Me</Button>
    </template>
    <div>This is custom content</div>
  </Popover>
</template>
```

### Different Placements

```vue
<template>
  <div>
    <Popover placement="top" title="Top">
      <template #trigger><Button>Top</Button></template>
      Content on top
    </Popover>
    <Popover placement="right" title="Right">
      <template #trigger><Button>Right</Button></template>
      Content on right
    </Popover>
    <Popover placement="bottom" title="Bottom">
      <template #trigger><Button>Bottom</Button></template>
      Content on bottom
    </Popover>
    <Popover placement="left" title="Left">
      <template #trigger><Button>Left</Button></template>
      Content on left
    </Popover>
  </div>
</template>
```

### Hover Trigger

```vue
<template>
  <Popover
    trigger="hover"
    title="Information"
    content="This appears on hover"
  >
    <template #trigger>
      <Button>Hover Me</Button>
    </template>
  </Popover>
</template>
```

### Focus Trigger

```vue
<template>
  <Popover
    trigger="focus"
    title="Help"
    content="This appears on focus"
  >
    <template #trigger>
      <Input placeholder="Focus me" />
    </template>
  </Popover>
</template>
```

### Controlled with v-model

```vue
<template>
  <div>
    <Button @click="isOpen = !isOpen">Toggle Popover</Button>
    <Popover
      v-model="isOpen"
      title="Controlled Popover"
      trigger="manual"
    >
      This popover is controlled by v-model
    </Popover>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const isOpen = ref(false)
</script>
```

### Custom Size

```vue
<template>
  <div>
    <Popover size="sm" title="Small">
      <template #trigger><Button>Small</Button></template>
      Small popover content
    </Popover>
    <Popover size="md" title="Medium">
      <template #trigger><Button>Medium</Button></template>
      Medium popover content
    </Popover>
    <Popover size="lg" title="Large">
      <template #trigger><Button>Large</Button></template>
      Large popover content with more content
    </Popover>
  </div>
</template>
```

### Without Arrow

```vue
<template>
  <Popover
    :show-arrow="false"
    title="No Arrow"
    content="This popover has no arrow indicator"
  >
    <template #trigger>
      <Button>Click Me</Button>
    </template>
  </Popover>
</template>
```

### Custom Width

```vue
<template>
  <Popover
    :width="300"
    title="Fixed Width"
    content="This popover has a fixed width of 300px"
  >
    <template #trigger>
      <Button>Click Me</Button>
    </template>
  </Popover>
</template>
```

### Auto Placement

```vue
<template>
  <Popover
    placement="auto"
    title="Auto Position"
    content="This popover automatically finds the best position"
  >
    <template #trigger>
      <Button>Click Me</Button>
    </template>
  </Popover>
</template>
```

### Persistent on Hover

```vue
<template>
  <Popover
    trigger="hover"
    :persistent="true"
    title="Persistent"
  >
    <template #trigger>
      <Button>Hover Me</Button>
    </template>
    <div>
      <p>This popover won't close when you hover over it</p>
      <Input placeholder="You can interact here" />
    </div>
  </Popover>
</template>
```

### With Action Buttons

```vue
<template>
  <Popover title="Confirm Action">
    <template #trigger>
      <Button variant="error">Delete</Button>
    </template>
    <p>Are you sure you want to delete this item?</p>
    <div class="actions">
      <Button size="sm" @click="handleConfirm">Confirm</Button>
      <Button size="sm" variant="outline" @click="handleCancel">Cancel</Button>
    </div>
  </Popover>
</template>

<script setup lang="ts">
const handleConfirm = () => {
  console.log('Confirmed')
}

const handleCancel = () => {
  console.log('Cancelled')
}
</script>
```

### Rich Content

```vue
<template>
  <Popover title="User Information">
    <template #trigger>
      <Avatar src="/user-avatar.jpg" />
    </template>
    <div class="user-info">
      <h4>John Doe</h4>
      <p>Software Engineer</p>
      <p>john@example.com</p>
      <div class="user-stats">
        <div>Projects: 12</div>
        <div>Tasks: 45</div>
      </div>
      <Button size="sm">View Profile</Button>
    </div>
  </Popover>
</template>
```

### Form in Popover

```vue
<template>
  <Popover title="Quick Add">
    <template #trigger>
      <Button variant="primary">Add Item</Button>
    </template>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>Name</label>
        <Input v-model="name" placeholder="Enter name" />
      </div>
      <div class="form-group">
        <label>Email</label>
        <Input v-model="email" type="email" placeholder="Enter email" />
      </div>
      <Button type="submit" size="sm" variant="primary">
        Add
      </Button>
    </form>
  </Popover>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const name = ref('')
const email = ref('')

const handleSubmit = () => {
  console.log('Add:', name.value, email.value)
}
</script>
```

### Custom Offset

```vue
<template>
  <Popover
    :offset="20"
    title="Custom Offset"
    content="This popover has 20px offset from trigger"
  >
    <template #trigger>
      <Button>Click Me</Button>
    </template>
  </Popover>
</template>
```

### Programmatic Control

```vue
<template>
  <div>
    <Button @click="openPopover">Open</Button>
    <Button @click="closePopover">Close</Button>
    <Button @click="togglePopover">Toggle</Button>

    <Popover
      ref="popoverRef"
      title="Programmatic"
      trigger="manual"
    >
      This popover is controlled programmatically
    </Popover>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const popoverRef = ref()

const openPopover = () => {
  popoverRef.value?.open()
}

const closePopover = () => {
  popoverRef.value?.close()
}

const togglePopover = () => {
  popoverRef.value?.toggle()
}
</script>
```

## Integration Examples

### Help Tooltip

```vue
<template>
  <div class="form-group">
    <label>
      Email Address
      <Popover
        trigger="hover"
        :delay="200"
        size="sm"
      >
        <template #trigger>
          <span class="help-icon">?</span>
        </template>
        Enter your email address. We'll send a confirmation link.
      </Popover>
    </label>
    <Input type="email" placeholder="you@example.com" />
  </div>
</template>

<style scoped>
.help-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1rem;
  height: 1rem;
  margin-left: 0.25rem;
  background-color: #3b82f6;
  color: white;
  border-radius: 50%;
  font-size: 0.75rem;
  cursor: help;
}
</style>
```

### Context Menu

```vue
<template>
  <div>
    <div
      class="file-item"
      @contextmenu.prevent="showContextMenu"
    >
      📄 Document.pdf
    </div>

    <Popover
      v-model="contextMenuVisible"
      trigger="manual"
      :show-arrow="false"
      :close-on-click-outside="true"
    >
      <div class="context-menu">
        <div @click="handleAction('open')">Open</div>
        <div @click="handleAction('download')">Download</div>
        <div @click="handleAction('share')">Share</div>
        <hr>
        <div @click="handleAction('delete')" class="danger">Delete</div>
      </div>
    </Popover>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const contextMenuVisible = ref(false)

const showContextMenu = () => {
  contextMenuVisible.value = true
}

const handleAction = (action: string) => {
  console.log('Action:', action)
  contextMenuVisible.value = false
}
</script>
```

### Notification Preview

```vue
<template>
  <Popover
    placement="bottom-end"
    :width="350"
    title="Notifications"
  >
    <template #trigger>
      <Button variant="ghost">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
      </Button>
    </template>
    <div class="notifications">
      <div v-for="notif in notifications" :key="notif.id" class="notif-item">
        <div class="notif-icon" :class="notif.type">
          {{ notif.icon }}
        </div>
        <div class="notif-content">
          <div class="notif-title">{{ notif.title }}</div>
          <div class="notif-text">{{ notif.text }}</div>
        </div>
      </div>
    </div>
  </Popover>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const notifications = ref([
  { id: 1, icon: '📧', title: 'New email', text: 'You have a new message', type: 'info' },
  { id: 2, icon: '✅', title: 'Success', text: 'Your task was completed', type: 'success' },
  { id: 3, icon: '⚠️', title: 'Warning', text: 'Storage almost full', type: 'warning' }
])
</script>
```

### User Menu

```vue
<template>
  <Popover
    placement="bottom-end"
    :show-arrow="true"
    :offset="8"
  >
    <template #trigger>
      <div class="user-avatar">
        <Avatar src="/user.jpg" />
      </div>
    </template>
    <div class="user-menu">
      <div class="user-header">
        <Avatar src="/user.jpg" size="md" />
        <div>
          <div class="user-name">John Doe</div>
          <div class="user-email">john@example.com</div>
        </div>
      </div>
      <hr>
      <div class="menu-item" @click="navigate('profile')">
        👤 Profile
      </div>
      <div class="menu-item" @click="navigate('settings')">
        ⚙️ Settings
      </div>
      <div class="menu-item" @click="navigate('billing')">
        💳 Billing
      </div>
      <hr>
      <div class="menu-item danger" @click="logout">
        🚪 Logout
      </div>
    </div>
  </Popover>
</template>

<script setup lang="ts">
const navigate = (page: string) => {
  console.log('Navigate to:', page)
}

const logout = () => {
  console.log('Logout')
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

- [x] Popover displays correctly
- [x] All placements work
- [x] Click trigger toggles
- [x] Hover trigger shows/hides
- [x] Focus trigger shows/hides
- [x] Manual trigger works with v-model
- [x] Arrow displays correctly
- [x] Arrow points correct direction
- [x] All sizes render correctly
- [x] Close on outside click works
- [x] Close on escape works
- [x] Custom width works
- [x] Auto placement works
- [x] Viewport boundary detection works
- [x] v-model two-way binding works
- [x] Events emit correctly
- [x] Exposed methods work
- [x] Dark mode support

## Styling Features

### Positioning

Smart positioning:
- Calculates position relative to trigger
- Adjusts for viewport boundaries
- Supports 12 placement options
- Auto placement for optimal position

### Arrow

Visual indicator:
- Points to trigger element
- Rotates based on placement
- Positioned correctly for each placement

### Animation

Smooth transitions:
- Fade in/out
- Scale effect
- Configurable duration

## File Changes

### New Files

1. **src/components/Popover.vue** (~360 lines)
   - 12 placement options
   - 4 trigger modes
   - Auto placement
   - Arrow indicator
   - Size variants
   - v-model support
   - Close behaviors
   - Boundary detection
   - Dark mode support

## Benefits

### Over Native Tooltips

| Aspect | Native Tooltip | Popover Component |
|--------|----------------|-------------------|
| **Styling** | Limited | Full control |
| **Content** | Text only | Rich HTML |
| **Placement** | Browser-dependent | Configurable |
| **Trigger** | Hover only | Multiple modes |
| **Interactivity** | No | Yes |
| **Accessibility** | Basic | Full ARIA |

### Use Cases

1. **Tooltips** - Helpful information
2. **Context Menus** - Right-click actions
3. **Help Content** - Explanatory text
4. **User Menus** - Account options
5. **Notifications** - Preview notifications
6. **Confirmations** - Action confirmations
7. **Forms** - Quick input forms
8. **Rich Previews** - Content previews

## Future Enhancements

### Potential Additions

1. **Virtual Scroll** - For long content
2. **Lazy Loading** - Load content on open
3. **Nested** - Nested popovers
4. **Follow Mouse** - Cursor following
5. **Grouping** - Grouped popovers
6. **Transitions** - Custom animations
7. **Z-Index** - Configurable z-index
8. **Container** - Custom container
9. **Boundary** - Boundary element
10. **Flip** - Auto flip on boundary

## Integration Opportunities

The Popover component can be integrated with:

1. **Tooltips** - Helpful hints
2. **Menus** - Context menus
3. **Notifications** - Notification previews
4. **User Profiles** - User information
5. **Actions** - Action confirmations
6. **Forms** - Quick inputs
7. **Filters** - Filter options
8. **Settings** - Quick settings

## CSS Architecture

### BEM Naming

- `.popover` - Block
- `.popover--size` - Modifier (e.g., `popover--sm`)
- `.popover--placement` - Modifier (e.g., `popover--top`)
- `.popover__arrow` - Element
- `.popover__content` - Element
- `.popover__title` - Element
- `.popover__body` - Element

## Accessibility

- ARIA role="tooltip"
- aria-describedby for content
- Keyboard navigation
- Focus management
- Screen reader support

## Performance Considerations

### Rendering Efficiency

Optimized positioning:
- Calculates position once
- Updates on scroll/resize
- Minimal DOM manipulation

### Event Handling

Efficient listeners:
- Document-level click outside
- Window-level scroll/resize
- Cleanup on unmount

## Comparison with Modal

| Feature | Popover | Modal |
|---------|---------|-------|
| **Purpose** | Contextual | Focused |
| **Position** | Relative to trigger | Centered |
| **Backdrop** | No | Yes |
| **Use Case** | Quick info | Focused tasks |

## Summary

Popover Component successfully provides:

✅ **12 Placements** - Top, bottom, left, right with start/end variants
✅ **4 Triggers** - Click, hover, focus, manual modes
✅ **Auto Placement** - Automatic position selection
✅ **Arrow Indicator** - Visual pointer to trigger
✅ **Size Variants** - 3 size presets (sm, md, lg)
✅ **Custom Width** - Fixed and max width options
✅ **v-model Support** - Controlled open state
✅ **Close Behaviors** - Click outside, escape key
✅ **Boundary Detection** - Viewport edge handling
✅ **Rich Content** - HTML content support
✅ **Exposed Methods** - Open, close, toggle
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable popover system for floating content display with smart positioning and multiple trigger options.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

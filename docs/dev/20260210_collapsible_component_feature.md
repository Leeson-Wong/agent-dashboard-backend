# Collapsible Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Collapsible components allow users to show and hide content panels, making them ideal for FAQs, settings panels, and content organization. A reusable Collapsible component provides consistent show/hide behavior throughout the application.

The Collapsible component provides:
- Click-to-toggle functionality
- Smooth height animation
- Multiple sizes (sm, md, lg)
- Multiple color variants
- Custom header slot
- Disabled state
- v-model support
- Accessibility support
- Dark mode support

## Implementation

### Collapsible Component

**File**: `src/components/Collapsible.vue` (~200 lines)

#### Type Definitions

```typescript
export type CollapsibleSize = 'sm' | 'md' | 'lg'
export type CollapsibleVariant = 'default' | 'primary' | 'success' | 'warning' | 'error'

interface Props {
  title?: string
  expanded?: boolean
  disabled?: boolean
  size?: CollapsibleSize
  variant?: CollapsibleVariant
}
```

## Feature Highlights

### Sizes

| Size | Header Padding | Content Font | Use Case |
|------|---------------|-------------|----------|
| **sm** | 0.5rem 0.75rem | 0.875rem | Compact panels |
| **md** | 0.75rem 1rem | Default | Standard usage |
| **lg** | 1rem 1.25rem | 0.9375rem | Large panels |

### Variants

| Variant | Border Color | Header Bg | Toggle Color | Use Case |
|---------|--------------|-----------|--------------|----------|
| **default** | Gray | White | Gray | Neutral |
| **primary** | Blue | Light blue | Blue | Primary |
| **success** | Green | Light green | Green | Success |
| **warning** | Orange | Light orange | Orange | Warnings |
| **error** | Red | Light red | Red | Errors |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `title` | `string` | `''` | Header title |
| `expanded` | `boolean` | `false` | Initial expanded state (v-model) |
| `disabled` | `boolean` | `false` | Disabled state |
| `size` | `CollapsibleSize` | `'md'` | Collapsible size |
| `variant` | `CollapsibleVariant` | `'default'` | Color variant |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:expanded` | `boolean` | Expanded state changed (v-model) |
| `toggle` | `boolean` | Toggled to new state |

### Slots

| Slot | Scope | Description |
|------|-------|-------------|
| `header` | `{ expanded }` | Custom header content |
| `default` | - | Collapsible content |

## Usage Examples

### Basic Collapsible

```vue
<template>
  <Collapsible title="Click to expand">
    <p>This is the collapsible content that can be shown or hidden.</p>
  </Collapsible>
</template>
```

### With v-model

```vue
<template>
  <div>
    <Button @click="isExpanded = !isExpanded">
      {{ isExpanded ? 'Collapse' : 'Expand' }}
    </Button>
    <Collapsible v-model:expanded="isExpanded" title="Controlled Collapsible">
      <p>This collapsible is controlled by v-model.</p>
    </Collapsible>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const isExpanded = ref(false)
</script>
```

### Size Variants

```vue
<template>
  <div>
    <Collapsible title="Small" size="sm">
      <p>Small collapsible content</p>
    </Collapsible>
    <Collapsible title="Medium" size="md">
      <p>Medium collapsible content</p>
    </Collapsible>
    <Collapsible title="Large" size="lg">
      <p>Large collapsible content</p>
    </Collapsible>
  </div>
</template>
```

### Color Variants

```vue
<template>
  <div>
    <Collapsible title="Default" variant="default">
      <p>Default variant content</p>
    </Collapsible>
    <Collapsible title="Primary" variant="primary">
      <p>Primary variant content</p>
    </Collapsible>
    <Collapsible title="Success" variant="success">
      <p>Success variant content</p>
    </Collapsible>
    <Collapsible title="Warning" variant="warning">
      <p>Warning variant content</p>
    </Collapsible>
    <Collapsible title="Error" variant="error">
      <p>Error variant content</p>
    </Collapsible>
  </div>
</template>
```

### Custom Header

```vue
<template>
  <Collapsible>
    <template #header="{ expanded }">
      <div class="custom-header">
        <Icon name="info" />
        <span>Custom Header ({{ expanded ? 'Expanded' : 'Collapsed' }})</span>
      </div>
    </template>
    <p>Content with custom header</p>
  </Collapsible>
</template>
```

### Initially Expanded

```vue
<template>
  <Collapsible title="Initially Expanded" :expanded="true">
    <p>This content is visible by default.</p>
  </Collapsible>
</template>
```

### Disabled State

```vue
<template>
  <Collapsible title="Disabled Collapsible" :disabled="true">
    <p>This cannot be toggled.</p>
  </Collapsible>
</template>
```

### With Toggle Handler

```vue
<template>
  <Collapsible
    title="With Event Handler"
    @toggle="handleToggle"
  >
    <p>This fires an event on toggle.</p>
  </Collapsible>
</template>

<script setup lang="ts">
const handleToggle = (expanded: boolean) => {
  console.log(`Collapsible is now ${expanded ? 'expanded' : 'collapsed'}`)
}
</script>
```

## Integration Examples

### FAQ Section

```vue
<template>
  <div class="faq">
    <h2>Frequently Asked Questions</h2>
    <Collapsible
      v-for="(faq, index) in faqs"
      :key="index"
      :title="faq.question"
    >
      <p>{{ faq.answer }}</p>
    </Collapsible>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const faqs = ref([
  {
    question: 'What is your return policy?',
    answer: 'We offer a 30-day return policy for all unused items.'
  },
  {
    question: 'How long does shipping take?',
    answer: 'Standard shipping takes 3-5 business days.'
  },
  {
    question: 'Do you offer international shipping?',
    answer: 'Yes, we ship to over 50 countries worldwide.'
  }
])
</script>
```

### Settings Panel

```vue
<template>
  <div class="settings">
    <Collapsible title="General Settings" variant="primary" :expanded="true">
      <div class="setting-item">
        <label>Language</label>
        <Select v-model="settings.language">
          <option value="en">English</option>
          <option value="es">Spanish</option>
          <option value="fr">French</option>
        </Select>
      </div>
      <div class="setting-item">
        <label>Timezone</label>
        <Select v-model="settings.timezone">
          <option value="utc">UTC</option>
          <option value="est">EST</option>
          <option value="pst">PST</option>
        </Select>
      </div>
    </Collapsible>

    <Collapsible title="Privacy Settings" variant="warning">
      <div class="setting-item">
        <Checkbox v-model="settings.profileVisible">
          Make profile visible to everyone
        </Checkbox>
      </div>
      <div class="setting-item">
        <Checkbox v-model="settings.showActivity">
          Show activity status
        </Checkbox>
      </div>
    </Collapsible>

    <Collapsible title="Notification Settings" variant="success">
      <div class="setting-item">
        <Checkbox v-model="settings.emailNotifications">
          Email notifications
        </Checkbox>
      </div>
      <div class="setting-item">
        <Checkbox v-model="settings.pushNotifications">
          Push notifications
        </Checkbox>
      </div>
    </Collapsible>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const settings = ref({
  language: 'en',
  timezone: 'utc',
  profileVisible: false,
  showActivity: true,
  emailNotifications: true,
  pushNotifications: false
})
</script>
```

### Documentation Sections

```vue
<template>
  <div class="documentation">
    <Collapsible
      v-for="(section, index) in docs"
      :key="index"
      :title="section.title"
      :variant="section.variant"
      size="lg"
    >
      <div v-html="section.content"></div>
    </Collapsible>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const docs = ref([
  {
    title: 'Getting Started',
    variant: 'primary',
    content: '<p>Welcome to our platform. Here\'s how to get started...</p>'
  },
  {
    title: 'API Reference',
    variant: 'success',
    content: '<p>Our API provides endpoints for...</p>'
  },
  {
    title: 'Troubleshooting',
    variant: 'warning',
    content: '<p>Common issues and solutions...</p>'
  }
])
</script>
```

### Nested Collapsibles

```vue
<template>
  <div class="nested">
    <Collapsible title="Parent Section" :expanded="true">
      <p>Parent content here.</p>

      <Collapsible title="Nested Section 1" size="sm" variant="primary">
        <p>Nested content 1</p>
      </Collapsible>

      <Collapsible title="Nested Section 2" size="sm" variant="success">
        <p>Nested content 2</p>
      </Collapsible>
    </Collapsible>
  </div>
</template>
```

### Product Specifications

```vue
<template>
  <div class="product-specs">
    <Collapsible
      v-for="(spec, index) in specifications"
      :key="index"
      :title="spec.category"
      :expanded="index === 0"
    >
      <ul>
        <li v-for="(item, i) in spec.items" :key="i">
          <strong>{{ item.label }}:</strong> {{ item.value }}
        </li>
      </ul>
    </Collapsible>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const specifications = ref([
  {
    category: 'Technical Specifications',
    items: [
      { label: 'Dimensions', value: '10 x 5 x 3 inches' },
      { label: 'Weight', value: '1.5 lbs' },
      { label: 'Material', value: 'Aluminum alloy' }
    ]
  },
  {
    category: 'Package Contents',
    items: [
      { label: 'Device', value: '1 unit' },
      { label: 'Cable', value: '6 feet USB-C' },
      { label: 'Manual', value: 'Quick start guide' }
    ]
  }
])
</script>
```

### accordion-Style (Single Open)

```vue
<template>
  <div class="accordion">
    <Collapsible
      v-for="(item, index) in items"
      :key="index"
      :title="item.title"
      :expanded="openIndex === index"
      @toggle="handleToggle(index)"
    >
      {{ item.content }}
    </Collapsible>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { title: 'Item 1', content: 'Content 1' },
  { title: 'Item 2', content: 'Content 2' },
  { title: 'Item 3', content: 'Content 3' }
])

const openIndex = ref(0)

const handleToggle = (index: number) => {
  if (openIndex.value === index) {
    openIndex.value = -1
  } else {
    openIndex.value = index
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

- [x] Collapsible displays correctly
- [x] Header clicking toggles
- [x] Smooth animation works
- [x] All sizes render correctly
- [x] All variants display correctly
- [x] Custom header slot works
- [x] Content slot works
- [x] Disabled state works
- [x] v-model two-way binding works
- [x] Events emit correctly
- [x] Toggle icon rotates
- [x] Dark mode support

## Styling Features

### Smooth Animation

Height transition:
- Auto height calculation
- Smooth 0.3s ease
- Overflow hidden during transition

### Toggle Icon

Visual indicator:
- Rotates 180° when expanded
- Color matches variant
- Smooth transition

### Variant Styling

Color coordination:
- Border color
- Header background
- Toggle icon color

## File Changes

### New Files

1. **src/components/Collapsible.vue** (~200 lines)
   - Click-to-toggle
   - Smooth height animation
   - Size variants
   - Color variants
   - Custom header slot
   - v-model support
   - Disabled state
   - Dark mode support

## Benefits

### Over Manual Implementation

| Aspect | Manual | Collapsible Component |
|--------|--------|----------------------|
| **Animation** | Manual CSS/JS | Built-in |
| **State** | Manual | v-model |
| **Styling** | Manual CSS | Variant-based |
| **Accessibility** | Missing | Full ARIA |
| **Icons** | Manual | Automatic |
| **Maintenance** | Difficult | Easy |

### Use Cases

1. **FAQs** - Question/answer panels
2. **Settings** - Settings sections
3. **Documentation** - Doc sections
4. **Product Info** - Specifications
5. **Nested Content** - Hierarchical data
6. **Filters** - Filter options
7. **Forms** - Form sections
8. **Menus** - Expandable menus

## Future Enhancements

### Potential Additions

1. **Multiple** - Allow multiple open
2. **Animation Speed** - Configurable duration
3. **Easing** - Custom easing functions
4. **Icon Slot** - Custom toggle icons
5. **Borderless** - Remove borders
6. **Shadow** - Add shadows
7. **Rounded** - Corner radius options
8. **Position** - Icon position options
9. **Gradient** - Gradient headers
10. **Nested** - Built-in accordion mode

## Integration Opportunities

The Collapsible component can be integrated with:

1. **FAQ Pages** - Question panels
2. **Settings** - Settings categories
3. **Documentation** - Doc sections
4. **E-commerce** - Product specs
5. **Dashboards** - Panel sections
6. **Forms** - Form sections
7. **Tables** - Row details
8. **Lists** - Nested lists

## CSS Architecture

### BEM Naming

- `.collapsible` - Block
- `.collapsible--size` - Modifier (e.g., `collapsible--sm`)
- `.collapsible--variant` - Modifier (e.g., `collapsible--primary`)
- `.collapsible--disabled` - Modifier
- `.collapsible--expanded` - Modifier
- `.collapsible__header` - Element
- `.collapsible__title` - Element
- `.collapsible__toggle` - Element
- `.collapsible__toggle--expanded` - Modifier
- `.collapsible__content-wrapper` - Element
- `.collapsible__content` - Element

## Accessibility

- ARIA expanded attribute
- ARIA controls
- Keyboard accessible
- Focus indicators
- Screen reader support

## Performance Considerations

### Animation

Smooth transitions:
- CSS transitions
- requestAnimationFrame
- Minimal reflows

### State Management

Efficient updates:
- Computed properties
- Watch for changes
- Minimal re-renders

## Comparison with Accordion

| Feature | Collapsible | Accordion |
|---------|-------------|-----------|
| **Independence** | Independent | Coordinated |
| **Multiple** | Yes | Usually single |
| **Use Case** | General | Organized |
| **Control** | Manual | Auto-close |

## Summary

Collapsible Component successfully provides:

✅ **Toggle Functionality** - Click to show/hide
✅ **Smooth Animation** - Height transition
✅ **Size Variants** - 3 size presets (sm, md, lg)
✅ **Color Variants** - 5 color options
✅ **Custom Header** - Header slot support
✅ **v-model Support** - Two-way binding
✅ **Disabled State** - Non-interactive mode
✅ **Toggle Icon** - Animated indicator
✅ **Events** - Toggle event emission
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable collapsible system for content organization with smooth animations and consistent styling.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

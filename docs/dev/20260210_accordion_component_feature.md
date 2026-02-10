# Accordion Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Accordions are collapsible content panels that help organize content into expandable sections, allowing users to show/hide information as needed. A reusable Accordion component provides consistency and improves content organization.

The Accordion component provides:
- Single and multiple expand modes
- Multiple visual variants (default, bordered, ghost)
- Multiple sizes (sm, md, lg)
- Icon position control (left/right)
- Disabled state for items
- v-model support for controlled state
- Smooth expand/collapse animations
- Accessibility support

## Implementation

### Accordion Component

**File**: `src/components/Accordion.vue` (~110 lines)
**File**: `src/components/AccordionItem.vue` (~200 lines)

#### Type Definitions

```typescript
export interface AccordionItemData {
  id: string
  title: string
  content?: string
  disabled?: boolean
  icon?: any
}

export type AccordionVariant = 'default' | 'bordered' | 'ghost'
export type AccordionSize = 'sm' | 'md' | 'lg'
export type IconPosition = 'left' | 'right'
```

## Feature Highlights

### Variants

| Variant | Visual | Use Case |
|---------|--------|----------|
| **default** | Card-like with shadow | General purpose |
| **bordered** | Bordered with no gaps | Continuous sections |
| **ghost** | Transparent, hover effect | Minimalist |

### Expand Modes

| Mode | Behavior | Use Case |
|------|----------|----------|
| **single** | Only one item open at a time | FAQ, settings |
| **multiple** | Multiple items can be open | Content browsing |

### Sizes

| Size | Header Padding | Font Size | Use Case |
|------|---------------|-----------|----------|
| **sm** | 0.75rem | 0.875rem | Compact |
| **md** | 1rem | 1rem | Default |
| **lg** | 1.25rem | 1.125rem | Emphasized |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `items` | `AccordionItemData[]` | **required** | Array of accordion items |
| `modelValue` | `string \| string[]` | - | v-model for open items |
| `multiple` | `boolean` | `false` | Allow multiple items open |
| `variant` | `AccordionVariant` | `'default'` | Visual variant |
| `size` | `AccordionSize` | `'md'` | Item size |
| `bordered` | `boolean` | `false` | Add borders |
| `iconPosition` | `IconPosition` | `'right'` | Icon position |
| `defaultOpen` | `string[]` | `[]` | IDs of items open by default |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `string \| string[]` | Emitted when items open/close |
| `toggle` | `[itemId, isOpen]` | Emitted when item is toggled |

### Slots

| Slot | Props | Description |
|------|-------|-------------|
| `icon` | `{ item, isOpen }` | Custom icon for toggle |
| `title` | `{ item }` | Custom title content |
| `content` | `{ item }` | Custom content panel |
| `actions` | `{ item }` | Additional actions (badge, etc.) |

## Usage Examples

### Basic Accordion

```vue
<template>
  <Accordion :items="items">
    <template #content="{ item }">
      <div v-html="item.content" />
    </template>
  </Accordion>
</template>

<script setup lang="ts">
const items = [
  {
    id: '1',
    title: 'What is an accordion?',
    content: '<p>An accordion is a vertically stacked list of items...</p>'
  },
  {
    id: '2',
    title: 'How do I use it?',
    content: '<p>Click on the header to expand or collapse...</p>'
  }
]
</script>
```

### Single Expand Mode (Default)

```vue
<template>
  <Accordion :items="items" />
</template>
```

### Multiple Expand Mode

```vue
<template>
  <Accordion :items="items" :multiple="true" />
</template>
```

### Controlled with v-model

```vue
<template>
  <Accordion v-model="openItems" :items="items" :multiple="true" />

  <div>Currently open: {{ openItems.join(', ') }}</div>
</template>

<script setup lang="ts">
const openItems = ref<string[]>(['item-1'])

const items = [
  { id: 'item-1', title: 'Section 1', content: 'Content 1' },
  { id: 'item-2', title: 'Section 2', content: 'Content 2' }
]
</script>
```

### Bordered Variant

```vue
<template>
  <Accordion :items="items" variant="bordered" />
</template>
```

### Ghost Variant

```vue
<template>
  <Accordion :items="items" variant="ghost" />
</template>
```

### Icon on Left

```vue
<template>
  <Accordion :items="items" icon-position="left" />
</template>
```

### Custom Icons

```vue
<template>
  <Accordion :items="items">
    <template #icon="{ isOpen }">
      <Icon :name="isOpen ? 'chevron-up' : 'chevron-down'" />
    </template>
    <template #content="{ item }">
      <div>{{ item.content }}</div>
    </template>
  </Accordion>
</template>
```

### With Badge Actions

```vue
<template>
  <Accordion :items="items">
    <template #actions="{ item }">
      <Badge :count="item.count" variant="primary" size="xs" />
    </template>
    <template #content="{ item }">
      <div>{{ item.content }}</div>
    </template>
  </Accordion>
</template>

<script setup lang="ts">
const items = [
  { id: '1', title: 'Notifications', content: '...', count: 5 },
  { id: '2', title: 'Messages', content: '...', count: 12 }
]
</script>
```

### Disabled Items

```vue
<template>
  <Accordion :items="items">
    <template #content="{ item }">
      <div>{{ item.content }}</div>
    </template>
  </Accordion>
</template>

<script setup lang="ts">
const items = [
  { id: '1', title: 'Available', content: '...' },
  { id: '2', title: 'Premium (Locked)', content: '...', disabled: true },
  { id: '3', title: 'Enterprise (Locked)', content: '...', disabled: true }
]
</script>
```

### Default Open Items

```vue
<template>
  <Accordion
    :items="items"
    :default-open="['faq-1', 'faq-3']"
  >
    <template #content="{ item }">
      <div>{{ item.content }}</div>
    </template>
  </Accordion>
</template>
```

### Custom Title Slot

```vue
<template>
  <Accordion :items="items">
    <template #title="{ item }">
      <div class="custom-title">
        <Icon :name="item.icon" />
        <span>{{ item.title }}</span>
        <Badge :variant="item.variant" size="xs">{{ item.tag }}</Badge>
      </div>
    </template>
    <template #content="{ item }">
      <div>{{ item.content }}</div>
    </template>
  </Accordion>
</template>
```

### Size Variants

```vue
<template>
  <Accordion :items="items" size="sm" />
  <Accordion :items="items" size="md" />
  <Accordion :items="items" size="lg" />
</template>
```

## Integration Examples

### FAQ Section

```vue
<template>
  <div class="faq-section">
    <h2>Frequently Asked Questions</h2>
    <Accordion :items="faqItems">
      <template #content="{ item }">
        <div v-html="item.answer" />
      </template>
    </Accordion>
  </div>
</template>

<script setup lang="ts">
const faqItems = [
  {
    id: 'q1',
    title: 'How do I create an agent?',
    answer: '<p>To create an agent, navigate to the Agents page...</p>'
  },
  {
    id: 'q2',
    title: 'What frameworks are supported?',
    answer: '<p>We currently support CrewAI, LangChain, and AutoGen...</p>'
  }
]
</script>
```

### Settings Panel

```vue
<template>
  <div class="settings-panel">
    <h2>Settings</h2>
    <Accordion
      v-model="openSections"
      :items="settingsItems"
      :multiple="true"
    >
      <template #content="{ item }">
        <component :is="item.component" />
      </template>
    </Accordion>
  </div>
</template>

<script setup lang="ts">
const openSections = ref(['general'])

const settingsItems = [
  { id: 'general', title: 'General Settings', component: GeneralSettings },
  { id: 'notifications', title: 'Notifications', component: NotificationSettings },
  { id: 'security', title: 'Security', component: SecuritySettings }
]
</script>
```

### Agent Details

```vue
<template>
  <Card>
    <Accordion :items="agentSections" :multiple="true">
      <template #title="{ item }">
        <div class="section-title">
          <Icon :name="item.icon" />
          <span>{{ item.title }}</span>
        </div>
      </template>
      <template #actions="{ item }">
        <Badge v-if="item.count" :count="item.count" size="xs" />
      </template>
      <template #content="{ item }">
        <component :is="item.component" :agent="agent" />
      </template>
    </Accordion>
  </Card>
</template>
```

### Documentation Navigation

```vue
<template>
  <div class="docs-nav">
    <Accordion :items="docsSections">
      <template #content="{ item }">
        <ul class="doc-links">
          <li v-for="link in item.links" :key="link.id">
            <a :href="link.href">{{ link.title }}</a>
          </li>
        </ul>
      </template>
    </Accordion>
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
- [x] All sizes apply correctly
- [x] Single expand mode works
- [x] Multiple expand mode works
- [x] v-model binding works
- [x] Default open items work
- [x] Disabled items don't toggle
- [x] Icon position changes correctly
- [x] Custom icons render
- [x] Custom titles render
- [x] Custom content renders
- [x] Action slots render
- [x] Smooth animations work
- [x] ARIA attributes are set correctly
- [x] Keyboard navigation works
- [x] Dark mode support

## Styling Features

### Smooth Animations

CSS transitions with Vue transition hooks:
- Height-based animation
- 0.3s ease timing
- Overflow hidden during transition

### Visual Hierarchy

Clear visual distinction for:
- Active/open items
- Disabled items
- Hover states
- Different variants

### Responsive Design

Flexbox-based responsive layout:
- Content-aware sizing
- Flexible icon positioning
- Mobile-friendly touch targets

## File Changes

### New Files

1. **src/components/Accordion.vue** (~110 lines)
   - Accordion container component
   - State management for open items
   - Single/multiple mode handling
   - v-model support

2. **src/components/AccordionItem.vue** (~200 lines)
   - Individual accordion item
   - Header trigger with icon
   - Collapsible content panel
   - Smooth expand/collapse animation
   - Accessibility attributes
   - Dark mode support

## Benefits

### Over Manual Implementation

| Aspect | Manual | Accordion Component |
|--------|--------|---------------------|
| **State Management** | Manual tracking | Built-in state |
| **Animation** | Custom CSS | Built-in transitions |
| **Accessibility** | Missing | Full ARIA support |
| **Variants** | Custom CSS | Props-based |
| **Consistency** | Variable | Standardized |
| **Maintenance** | Difficult | Easy |

### Use Cases

1. **FAQ Sections** - Frequently asked questions
2. **Settings Panels** - Organized settings groups
3. **Documentation** - Collapsible sections
4. **Content Navigation** - Table of contents
5. **Product Details** - Product specifications
6. **Forms** - Multi-step forms
7. **Dashboards** - Collapsible widgets
8. **Data Display** - Grouped data views

## Future Enhancements

### Potential Additions

1. **Nested Accordions** - Accordion within accordion
2. **Drag and Drop** - Reorder items
3. **Search/Filter** - Filter accordion items
4. **Lazy Loading** - Load content on expand
5. **Keyboard Shortcuts** - Shortcut to toggle all
6. **Persist State** - Remember open state
7. **Animation Variants** - Different animation styles
8. **Level Indicators** - Visual depth for nested items

## Integration Opportunities

The Accordion component can be integrated with:

1. **Agent Details** - Agent information sections
2. **Settings Pages** - Settings categories
3. **FAQ Pages** - Question/answer sections
4. **Documentation** - Doc navigation
5. **Help Centers** - Help topics
6. **Product Pages** - Product specifications
7. **Forms** - Form sections
8. **Dashboards** - Collapsible panels

## CSS Architecture

### BEM Naming

- `.accordion` - Block
- `.accordion--variant` - Modifier
- `.accordion-item` - Nested block
- `.accordion-item__header` - Element
- `.accordion-item__content` - Element

### Animation Approach

Uses Vue transition hooks with direct style manipulation:
```javascript
const beforeEnter = (el) => { el.style.height = '0' }
const enter = (el) => { el.style.height = el.scrollHeight + 'px' }
const leave = (el) => {
  el.style.height = el.scrollHeight + 'px'
  el.offsetHeight // Force reflow
  el.style.height = '0'
}
```

### Accessibility

Complete ARIA attribute support:
- `aria-expanded` on header buttons
- `aria-controls` links header to content
- `aria-labelledby` links content to header
- `role="region"` on content panels
- `disabled` attribute for disabled items

## Summary

Accordion Component successfully provides:

✅ **Expand Modes** - Single and multiple expand
✅ **Visual Variants** - 3 styles (default, bordered, ghost)
✅ **Flexible Sizing** - 3 size presets (sm, md, lg)
✅ **Icon Control** - Left/right icon positioning
✅ **State Management** - v-model and internal state
✅ **Slot System** - Flexible content customization
✅ **Disabled State** - Non-interactive items
✅ **Smooth Animation** - Height-based transitions
✅ **Accessible** - Full ARIA support
✅ **Keyboard Support** - Enter/Space to toggle
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable accordion system for organizing collapsible content throughout the application, improving content organization and user experience.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

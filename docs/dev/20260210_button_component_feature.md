# Button Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Buttons are fundamental UI elements for user actions and navigation. A reusable Button component provides consistency, reduces boilerplate code, and ensures proper accessibility throughout the application.

The Button component provides:
- Multiple variants (default, primary, success, warning, error, info, ghost)
- Multiple sizes (xs, sm, md, lg, xl)
- Icon support with position control
- Loading state with spinner
- Disabled state
- Outlined and text variants
- Block/full-width option
- Rounded pill option
- Link and router-link support
- Accessibility support

## Implementation

### Button Component

**File**: `src/components/Button.vue` (~300 lines)

#### Type Definitions

```typescript
export type ButtonVariant =
  | 'default'    // Gray button
  | 'primary'    // Blue button
  | 'success'    // Green button
  | 'warning'    // Yellow button
  | 'error'      // Red button
  | 'info'       // Cyan button
  | 'ghost'      // Transparent with hover

export type ButtonSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl'
export type IconPosition = 'left' | 'right'
```

## Feature Highlights

### Variants

| Variant | Use Case | Color |
|---------|----------|-------|
| **default** | General purpose | Gray |
| **primary** | Primary actions | Blue |
| **success** | Success actions | Green |
| **warning** | Warning actions | Yellow |
| **error** | Destructive actions | Red |
| **info** | Informational | Cyan |
| **ghost** | Secondary actions | Transparent |
| **outlined** | Outlined buttons | Border only |
| **text** | Text buttons | No background |

### Sizes

| Size | Padding | Font | Height | Use Case |
|------|---------|------|--------|----------|
| **xs** | 0.25rem 0.5rem | 0.75rem | 1.5rem | Compact |
| **sm** | 0.375rem 0.75rem | 0.875rem | 2rem | Small |
| **md** | 0.5rem 1rem | 0.875rem | 2.5rem | Default |
| **lg** | 0.75rem 1.5rem | 1rem | 3rem | Large |
| **xl** | 1rem 2rem | 1.125rem | 3.5rem | Extra large |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `variant` | `ButtonVariant` | `'default'` | Button variant |
| `size` | `ButtonSize` | `'md'` | Button size |
| `icon` | `Component` | - | Icon component |
| `iconPosition` | `IconPosition` | `'left'` | Icon position |
| `disabled` | `boolean` | `false` | Disabled state |
| `loading` | `boolean` | `false` | Loading state |
| `block` | `boolean` | `false` | Full width |
| `rounded` | `boolean` | `false` | Pill shape |
| `outlined` | `boolean` | `false` | Outlined style |
| `text` | `boolean` | `false` | Text style |
| `nativeType` | `'button' \| 'submit' \| 'reset'` | `'button'` | Button type |
| `href` | `string` | - | Link href |
| `to` | `string \| object` | - | Router link target |
| `target` | `string` | - | Link target |
| `tag` | `string` | `'button'` | Override HTML tag |

## Usage Examples

### Basic Button

```vue
<template>
  <Button>Click me</Button>
</template>
```

### Variants

```vue
<template>
  <Button variant="default">Default</Button>
  <Button variant="primary">Primary</Button>
  <Button variant="success">Success</Button>
  <Button variant="warning">Warning</Button>
  <Button variant="error">Error</Button>
  <Button variant="info">Info</Button>
</template>
```

### Sizes

```vue
<template>
  <Button size="xs">Extra Small</Button>
  <Button size="sm">Small</Button>
  <Button size="md">Medium</Button>
  <Button size="lg">Large</Button>
  <Button size="xl">Extra Large</Button>
</template>
```

### With Icon

```vue
<template>
  <Button :icon="SaveIcon">Save</Button>
  <Button :icon="DeleteIcon" variant="error">Delete</Button>
</template>

<script setup lang="ts">
import SaveIcon from './icons/SaveIcon.vue'
import DeleteIcon from './icons/DeleteIcon.vue'
</script>
```

### Icon Position

```vue
<template>
  <Button :icon="ArrowIcon" icon-position="left">Previous</Button>
  <Button :icon="ArrowIcon" icon-position="right">Next</Button>
</template>
```

### Icon Only

```vue
<template>
  <Button :icon="CloseIcon" />
  <Button :icon="SearchIcon" size="lg" />
</template>
```

### Loading State

```vue
<template>
  <Button :loading="isLoading" @click="handleSubmit">
    Submit
  </Button>
</template>

<script setup lang="ts">
const isLoading = ref(false)

const handleSubmit = async () => {
  isLoading.value = true
  await submitForm()
  isLoading.value = false
}
</script>
```

### Disabled State

```vue
<template>
  <Button disabled>Disabled</Button>
  <Button :disabled="!isValid">Submit</Button>
</template>
```

### Outlined Variant

```vue
<template>
  <Button variant="primary" outlined>Outlined Primary</Button>
  <Button variant="success" outlined>Outlined Success</Button>
</template>
```

### Text Variant

```vue
<template>
  <Button variant="primary" text>Text Button</Button>
  <Button variant="error" text>Delete</Button>
</template>
```

### Ghost Variant

```vue
<template>
  <Button variant="ghost">Ghost Button</Button>
</template>
```

### Rounded (Pill)

```vue
<template>
  <Button rounded>Pill Button</Button>
  <Button variant="primary" rounded>Primary Pill</Button>
</template>
```

### Block (Full Width)

```vue
<template>
  <Button block>Full Width Button</Button>
</template>
```

### Link Button

```vue
<template>
  <Button href="https://example.com" target="_blank">
    External Link
  </Button>

  <Button to="/dashboard">
    Go to Dashboard
  </Button>
</template>
```

### Submit Button

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <Button native-type="submit">Submit Form</Button>
  </form>
</template>
```

### Combined Options

```vue
<template>
  <Button
    variant="primary"
    size="lg"
    :icon="UploadIcon"
    :loading="isUploading"
    :disabled="!hasFile"
    @click="uploadFile"
  >
    Upload File
  </Button>
</template>

<script setup lang="ts">
const isUploading = ref(false)
const hasFile = ref(false)

const uploadFile = async () => {
  isUploading.value = true
  await doUpload()
  isUploading.value = false
}
</script>
```

## Integration Examples

### Form Actions

```vue
<template>
  <form @submit.prevent="submit">
    <!-- Form fields -->
    <div class="form-actions">
      <Button variant="default" type="button" @click="cancel">
        Cancel
      </Button>
      <Button variant="primary" type="submit" :loading="isSubmitting">
        Save Changes
      </Button>
    </div>
  </form>
</template>
```

### Action Toolbar

```vue
<template>
  <div class="toolbar">
    <Button :icon="RefreshIcon" @click="refresh">Refresh</Button>
    <Button :icon="FilterIcon" @click="filter">Filter</Button>
    <Button :icon="ExportIcon" @click="export">Export</Button>
    <Button variant="primary" :icon="PlusIcon" @click="add">
      Add New
    </Button>
  </div>
</template>
```

### Confirmation Dialog

```vue
<template>
  <div class="dialog">
    <p>Are you sure you want to delete this item?</p>
    <div class="dialog-actions">
      <Button variant="default" @click="cancel">Cancel</Button>
      <Button variant="error" @click="confirm">Delete</Button>
    </div>
  </div>
</template>
```

### Icon Button Group

```vue
<template>
  <div class="button-group">
    <Button :icon="EditIcon" size="sm" />
    <Button :icon="CopyIcon" size="sm" />
    <Button :icon="DeleteIcon" size="sm" variant="error" />
  </div>
</template>
```

### Pagination

```vue
<template>
  <div class="pagination">
    <Button :icon="ChevronLeftIcon" :disabled="page === 1" @click="prev" />
    <span>Page {{ page }} of {{ totalPages }}</span>
    <Button :icon="ChevronRightIcon" :disabled="page === totalPages" @click="next" />
  </div>
</template>
```

### Loading with Progress

```vue
<template>
  <Button
    variant="primary"
    :loading="isLoading"
    :disabled="isLoading"
    @click="process"
  >
    {{ isLoading ? `Processing (${progress}%)` : 'Start Process' }}
  </Button>
</template>

<script setup lang="ts">
const isLoading = ref(false)
const progress = ref(0)

const process = async () => {
  isLoading.value = true
  for (let i = 0; i <= 100; i += 10) {
    progress.value = i
    await delay(100)
  }
  isLoading.value = false
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

- [x] All variants render correctly
- [x] All sizes work correctly
- [x] Icons display correctly
- [x] Icon position changes correctly
- [x] Loading spinner works
- [x] Content hides during loading
- [x] Disabled state applies
- [x] Hover effects work
- [x] Outlined variant works
- [x] Text variant works
- [x] Ghost variant works
- [x] Rounded corners apply
- [x] Block width works
- [x] Icon-only buttons work
- [x] Link buttons work
- [x] Router-link buttons work
- [x] Form submit works
- [x] Dark mode support

## Styling Features

### Consistent Sizing

Height-based sizing for consistency:
- All sizes have consistent minimum heights
- Icon-only buttons maintain same heights
- Touch-friendly target sizes

### Loading Animation

SVG-based circular spinner:
- Smooth rotation animation
- Dashed stroke for animated effect
- Adapts color to variant

### Visual Hierarchy

Clear distinction for:
- Primary vs secondary actions
- Destructive actions (error/red)
- Disabled states
- Loading states

## File Changes

### New Files

1. **src/components/Button.vue** (~300 lines)
   - Button with all variants
   - Size options
   - Icon support
   - Loading state
   - Disabled state
   - Outlined/text/ghost variants
   - Block and rounded options
   - Link and router-link support
   - Dark mode support

## Benefits

### Over Native Buttons

| Aspect | Native | Button Component |
|--------|--------|------------------|
| **Consistency** | Variable | Standardized |
| **Styling** | Manual CSS | Props-based |
| **Icons** | Manual | Built-in |
| **Loading** | Manual | Built-in spinner |
| **Accessibility** | Partial | Full support |
| **Variants** | Manual | Pre-configured |
| **Links** | Different tag | Unified API |

### Use Cases

1. **Form Actions** - Submit, cancel, reset buttons
2. **Navigation** - Links and navigation buttons
3. **Actions** - CRUD operations
4. **Toolbars** - Action toolbars
5. **Dialogs** - Confirmation buttons
6. **Pagination** - Page navigation
7. **Filters** - Filter actions
8. **Downloads** - Download/export buttons

## Future Enhancements

### Potential Additions

1. **Button Groups** - Grouped button component
2. **Dropdown Toggle** - Button with dropdown menu
3. **Split Button** - Action + dropdown button
4. **Toggle Button** - On/off state
5. **Icon Button** - Dedicated icon button variant
6. **Fab** - Floating action button
7. **Loading Progress** - Progress bar in loading button
8. **Ripple Effect** - Material Design ripple

## Integration Opportunities

The Button component can be integrated with:

1. **Forms** - Submit/cancel buttons
2. **Toolbars** - Action buttons
3. **Cards** - Card actions
4. **Modals** - Modal actions
5. **Tables** - Row actions
6. **Lists** - Item actions
7. **Headers** - Header actions
8. **Footers** - Footer actions

## CSS Architecture

### BEM Naming

- `.button` - Block
- `.button--variant` - Modifier (e.g., `button--primary`)
- `.button--size` - Modifier (e.g., `button--lg`)
- `.button__icon` - Element
- `.button__content` - Element
- `.button__spinner` - Element

### Animation Approach

Pure CSS animations for spinner:
- `rotate` - 360-degree rotation
- `dash` - Stroke dash animation

### Accessibility

- Proper button/anchor elements
- `disabled` attribute for disabled state
- `type` attribute for forms
- Keyboard accessible
- ARIA attributes for loading state

## Summary

Button Component successfully provides:

✅ **Multiple Variants** - 7 color variants + outlined/text/ghost
✅ **Flexible Sizing** - 5 size options (xs, sm, md, lg, xl)
✅ **Icon Support** - Left/right positioning
✅ **Loading State** - Animated spinner
✅ **Disabled State** - Proper disabled handling
✅ **Style Options** - Outlined, text, rounded, block
✅ **Link Support** - Anchor and router-link
✅ **Form Support** - Submit/reset types
✅ **Icon Only** - Standalone icon buttons
✅ **Accessible** - Keyboard and screen reader support
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable button system for user actions throughout the application, improving consistency and reducing boilerplate code.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

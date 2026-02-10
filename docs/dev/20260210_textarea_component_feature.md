# Textarea Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Textareas are essential form elements for multi-line text input. A reusable Textarea component provides consistency, proper validation states, and improved accessibility throughout the application.

The Textarea component provides:
- Multiple visual variants (default, filled, outlined)
- Multiple sizes (sm, md, lg)
- Configurable resize behavior
- Character count display
- Label and helper text
- Error state styling
- Disabled and readonly states
- v-model support
- Accessibility support

## Implementation

### Textarea Component

**File**: `src/components/Textarea.vue` (~190 lines)

#### Type Definitions

```typescript
export type TextareaSize = 'sm' | 'md' | 'lg'
export type TextareaVariant = 'default' | 'filled' | 'outlined'
export type TextareaResize = 'none' | 'vertical' | 'horizontal' | 'both'
```

## Feature Highlights

### Variants

| Variant | Visual | Use Case |
|---------|--------|----------|
| **default** | Bordered with white background | General purpose |
| **filled** | Gray background, no border | Modern look |
| **outlined** | Border only, transparent background | Minimalist |

### Sizes

| Size | Padding | Font | Use Case |
|------|---------|------|----------|
| **sm** | 0.375rem 0.5rem | 0.875rem | Compact forms |
| **md** | 0.5rem 0.75rem | 0.875rem | Default |
| **lg** | 0.75rem 1rem | 1rem | Emphasized inputs |

### Resize Options

| Option | Description | Use Case |
|---------|-------------|----------|
| **none** | No resize | Fixed size |
| **vertical** | Vertical only (default) | Height adjustment |
| **horizontal** | Horizontal only | Width adjustment |
| **both** | Both directions | Full resize |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `modelValue` | `string` | - | v-model value |
| `label` | `string` | - | Field label |
| `placeholder` | `string` | - | Placeholder text |
| `helperText` | `string` | - | Helper/description text |
| `errorMessage` | `string` | - | Error message |
| `disabled` | `boolean` | `false` | Disabled state |
| `readonly` | `boolean` | `false` | Readonly state |
| `required` | `boolean` | `false` | Required field indicator |
| `showCount` | `boolean` | `false` | Show character count |
| `size` | `TextareaSize` | `'md'` | Textarea size |
| `variant` | `TextareaVariant` | `'default'` | Visual variant |
| `resize` | `TextareaResize` | `'vertical'` | Resize behavior |
| `rows` | `number` | `3` | Initial rows |
| `cols` | `number` | - | Columns width |
| `maxlength` | `number` | - | Max length |
| `minlength` | `number` | - | Min length |
| `autocomplete` | `string` | `'off'` | Autocomplete attribute |
| `name` | `string` | - | Input name |
| `id` | `string` | - | Custom textarea ID |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `string` | Emitted on input |
| `focus` | `FocusEvent` | Emitted on focus |
| `blur` | `FocusEvent` | Emitted on blur |

### Exposed Methods

| Method | Description |
|--------|-------------|
| `focus()` | Focus the textarea |
| `blur()` | Blur the textarea |
| `select()` | Select textarea text |

## Usage Examples

### Basic Textarea

```vue
<template>
  <Textarea
    v-model="message"
    label="Message"
    placeholder="Enter your message"
  />
</template>

<script setup lang="ts">
const message = ref('')
</script>
```

### With Character Count

```vue
<template>
  <Textarea
    v-model="bio"
    label="Bio"
    :show-count="true"
    :maxlength="500"
    placeholder="Tell us about yourself"
  />
</template>
```

### Sizes

```vue
<template>
  <Textarea size="sm" label="Small" placeholder="Small textarea" />
  <Textarea size="md" label="Medium" placeholder="Medium textarea" />
  <Textarea size="lg" label="Large" placeholder="Large textarea" />
</template>
```

### Variants

```vue
<template>
  <Textarea variant="default" label="Default" />
  <Textarea variant="filled" label="Filled" />
  <Textarea variant="outlined" label="Outlined" />
</template>
```

### Resize Options

```vue
<template>
  <Textarea resize="none" label="No Resize" />
  <Textarea resize="vertical" label="Vertical Resize" />
  <Textarea resize="horizontal" label="Horizontal Resize" />
  <Textarea resize="both" label="Full Resize" />
</template>
```

### With Helper Text

```vue
<template>
  <Textarea
    v-model="description"
    label="Description"
    helper-text="Provide a detailed description of the item"
    placeholder="Enter description..."
  />
</template>
```

### With Error Message

```vue
<template>
  <Textarea
    v-model="content"
    label="Content"
    :error-message="contentError"
    :required="true"
  />
</template>

<script setup lang="ts">
const content = ref('')
const contentError = computed(() => {
  if (!content.value) return 'Content is required'
  if (content.value.length < 10) return 'Content must be at least 10 characters'
  return ''
})
</script>
```

### Custom Rows

```vue
<template>
  <Textarea
    v-model="longText"
    label="Details"
    :rows="8"
    placeholder="Enter detailed information..."
  />
</template>
```

### Disabled and Readonly

```vue
<template>
  <Textarea
    v-model="readonlyValue"
    label="Readonly"
    :readonly="true"
  />

  <Textarea
    v-model="disabledValue"
    label="Disabled"
    :disabled="true"
  />
</template>
```

### With Min/Max Length

```vue
<template>
  <Textarea
    v-model="comment"
    label="Comment"
    :minlength="10"
    :maxlength="200"
    :show-count="true"
    placeholder="Enter your comment (10-200 characters)"
  />
</template>
```

### With Ref

```vue
<template>
  <Textarea ref="textareaRef" v-model="value" label="Focus Example" />
  <Button @click="focusTextarea">Focus Textarea</Button>
</template>

<script setup lang="ts">
const textareaRef = ref()
const value = ref('')

const focusTextarea = () => {
  textareaRef.value?.focus()
}
</script>
```

## Integration Examples

### Comment Form

```vue
<template>
  <Card title="Leave a Comment">
    <form @submit.prevent="handleSubmit">
      <Textarea
        v-model="comment"
        label="Your Comment"
        :required="true"
        :minlength="10"
        :maxlength="500"
        :show-count="true"
        placeholder="Share your thoughts..."
        :error-message="commentError"
      />

      <div class="form-actions">
        <Button type="submit" variant="primary" :loading="isSubmitting">
          Post Comment
        </Button>
      </div>
    </form>
  </Card>
</template>

<script setup lang="ts">
const comment = ref('')
const isSubmitting = ref(false)

const commentError = computed(() => {
  if (comment.value.length < 10) return 'Comment must be at least 10 characters'
  return ''
})

const handleSubmit = async () => {
  isSubmitting.value = true
  await postComment(comment.value)
  comment.value = ''
  isSubmitting.value = false
}
</script>
```

### Notes Editor

```vue
<template>
  <div class="notes-editor">
    <Textarea
      v-model="notes"
      label="Notes"
      :rows="12"
      placeholder="Take notes here..."
      helper-text="Notes are automatically saved"
    />
  </div>
</template>

<script setup lang="ts">
const notes = ref('')

// Auto-save on change
watch(notes, debounce(() => {
  saveNotes(notes.value)
}, 1000))
</script>
```

### Feedback Form

```vue
<template>
  <Card title="Send Feedback">
    <form @submit.prevent="submitFeedback">
      <Input
        v-model="feedback.email"
        type="email"
        label="Email"
        placeholder="your@email.com"
        :required="true"
      />

      <Textarea
        v-model="feedback.message"
        label="Your Feedback"
        :required="true"
        :minlength="20"
        :maxlength="1000"
        :show-count="true"
        placeholder="Tell us what you think..."
        rows="6"
      />

      <Button type="submit" variant="primary" block>
        Send Feedback
      </Button>
    </form>
  </Card>
</template>
```

### Description Field

```vue
<template>
  <Textarea
    v-model="description"
    label="Description"
    variant="filled"
    placeholder="Enter a detailed description"
    helper-text="Include relevant details and context"
    :rows="5"
  />
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
- [x] All sizes work correctly
- [x] v-model binding works
- [x] Label displays correctly
- [x] Helper text displays
- [x] Error message displays
- [x] Required indicator shows
- [x] Character count shows correctly
- [x] Placeholder displays
- [x] Disabled state works
- [x] Readonly state works
- [x] Focus state styling
- [x] Error state styling
- [x] Maxlength limits input
- [x] Minlength validation works
- [x] Resize behavior works correctly
- [x] Rows attribute works
- [x] Exposed methods work
- [x] Dark mode support

## Styling Features

### Focus States

Visual feedback on focus:
- Blue border color
- Subtle box-shadow
- Smooth transitions

### Error States

Clear error indication:
- Red border color
- Red error message
- Red shadow on focus

### Resize Control

CSS resize property:
- `none` - Fixed size
- `vertical` - Height only (default)
- `horizontal` - Width only
- `both` - Full resize

### Responsive Sizing

Consistent sizing with:
- Padding-based sizing
- Font-based scaling
- Touch-friendly dimensions

## File Changes

### New Files

1. **src/components/Textarea.vue** (~190 lines)
   - Textarea with all variants
   - Size options
   - Resize behavior control
   - Character count
   - Label and helper text
   - Error state
   - Disabled/readonly states
   - Exposed methods
   - Dark mode support

## Benefits

### Over Native Textarea

| Aspect | Native | Textarea Component |
|--------|--------|-------------------|
| **Styling** | Manual | Props-based |
| **Validation** | Manual | Built-in states |
| **Character Count** | Manual | Built-in |
| **Consistency** | Variable | Standardized |
| **Accessibility** | Basic | Enhanced |
| **Error States** | Manual | Built-in styling |
| **Resize Control** | CSS only | Configurable prop |

### Use Cases

1. **Forms** - Multi-line input
2. **Comments** - Comment sections
3. **Notes** - Note-taking
4. **Feedback** - Feedback forms
5. **Messages** - Message composition
6. **Descriptions** - Long descriptions
7. **Bios** - User bios
8. **Reviews** - Review text

## Future Enhancements

### Potential Additions

1. **Auto-resize** - Auto-grow height
2. **Markdown Preview** - Live preview
3. **Syntax Highlight** - Code highlighting
4. **Toolbar** - Formatting toolbar
5. **Character Limit Bar** - Visual limit indicator
6. **Paste Formatting** - Handle paste events
7. **Auto-save** - Built-in auto-save
8. **Mentions** - @mention support

## Integration Opportunities

The Textarea component can be integrated with:

1. **Forms** - Multi-line form fields
2. **Comment Systems** - Comment input
3. **Notes Apps** - Note editing
4. **Feedback Forms** - Feedback collection
5. **Messaging** - Message composition
6. **Reviews** - Review writing
7. **CMS** - Content editing
8. **Forums** - Forum posts

## CSS Architecture

### BEM Naming

- `.textarea` - Block (wrapper)
- `.textarea--size` - Modifier (e.g., `textarea--sm`)
- `.textarea--variant` - Modifier (e.g., `textarea--filled`)
- `.textarea__label` - Element
- `.textarea__container` - Element (textarea wrapper)
- `.textarea__field` - Element (actual textarea)
- `.textarea__footer` - Element (count display)
- `.textarea__helper` - Element
- `.textarea__error` - Element

### Resize Behavior

Uses CSS `resize` property:
- Controlled via prop
- Applied as class modifier
- Default is vertical

### Accessibility Features

- Associated label with `for` attribute
- Unique ID generation
- Required indicator
- Error message association
- Disabled/readonly states
- Keyboard accessible
- Proper semantic HTML

## Comparison with Input

| Feature | Input | Textarea |
|---------|-------|----------|
| **Lines** | Single | Multiple |
| **Control** | Text input type | Textarea element |
| **Resize** | None | Configurable |
| **Use Case** | Short text | Long content |
| **Scroll** | None | Scrollable |

## Summary

Textarea Component successfully provides:

✅ **Multiple Variants** - 3 styles (default, filled, outlined)
✅ **Flexible Sizing** - 3 size presets (sm, md, lg)
✅ **Resize Control** - 4 resize options (none, vertical, horizontal, both)
✅ **Character Count** - Optional count display
✅ **Label Support** - With required indicator
✅ **Validation States** - Error and helper text
✅ **Disabled/Readonly** - Proper state handling
✅ **Configurable Rows** - Set initial height
✅ **Length Constraints** - Min/max length
✅ **v-model Support** - Two-way binding
✅ **Exposed Methods** - Focus, blur, select
✅ **Accessible** - Full ARIA support
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable textarea system for multi-line text input throughout the application.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

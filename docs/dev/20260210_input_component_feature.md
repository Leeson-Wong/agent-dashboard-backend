# Input Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Input fields are fundamental form elements for user data entry. A reusable Input component provides consistency, proper validation states, and improved accessibility throughout the application.

The Input component provides:
- Multiple visual variants (default, filled, outlined)
- Multiple sizes (sm, md, lg)
- Prefix and suffix icon/slot support
- Clear button functionality
- Character count display
- Label and helper text
- Error state styling
- Disabled and readonly states
- v-model support
- Accessibility support

## Implementation

### Input Component

**File**: `src/components/Input.vue` (~280 lines)

#### Type Definitions

```typescript
export type InputSize = 'sm' | 'md' | 'lg'
export type InputVariant = 'default' | 'filled' | 'outlined'
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

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `modelValue` | `string \| number` | - | v-model value |
| `type` | `string` | `'text'` | Input type |
| `label` | `string` | - | Field label |
| `placeholder` | `string` | - | Placeholder text |
| `helperText` | `string` | - | Helper/description text |
| `errorMessage` | `string` | - | Error message |
| `disabled` | `boolean` | `false` | Disabled state |
| `readonly` | `boolean` | `false` | Readonly state |
| `required` | `boolean` | `false` | Required field indicator |
| `clearable` | `boolean` | `false` | Show clear button |
| `showCount` | `boolean` | `false` | Show character count |
| `size` | `InputSize` | `'md'` | Input size |
| `variant` | `InputVariant` | `'default'` | Visual variant |
| `maxlength` | `number` | - | Max length |
| `minlength` | `number` | - | Min length |
| `min` | `number` | - | Min value (number) |
| `max` | `number` | - | Max value (number) |
| `step` | `number` | - | Step value (number) |
| `autocomplete` | `string` | `'off'` | Autocomplete attribute |
| `name` | `string` | - | Input name |
| `prefixIcon` | `Component` | - | Prefix icon |
| `suffixIcon` | `Component` | - | Suffix icon |
| `id` | `string` | - | Custom input ID |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `string \| number` | Emitted on input |
| `focus` | `FocusEvent` | Emitted on focus |
| `blur` | `FocusEvent` | Emitted on blur |
| `clear` | - | Emitted when cleared |
| `change` | `string \| number` | Emitted on change |

### Slots

| Slot | Description |
|------|-------------|
| `prefix` | Prefix content (icon, text) |
| `suffix` | Suffix content (icon, text) |

### Exposed Methods

| Method | Description |
|--------|-------------|
| `focus()` | Focus the input |
| `blur()` | Blur the input |
| `select()` | Select input text |

## Usage Examples

### Basic Input

```vue
<template>
  <Input v-model="name" label="Name" placeholder="Enter your name" />
</template>

<script setup lang="ts">
const name = ref('')
</script>
```

### With Types

```vue
<template>
  <Input v-model="email" type="email" label="Email" />
  <Input v-model="password" type="password" label="Password" />
  <Input v-model="age" type="number" label="Age" :min="0" :max="120" />
</template>
```

### Sizes

```vue
<template>
  <Input size="sm" label="Small" placeholder="Small input" />
  <Input size="md" label="Medium" placeholder="Medium input" />
  <Input size="lg" label="Large" placeholder="Large input" />
</template>
```

### Variants

```vue
<template>
  <Input variant="default" label="Default" />
  <Input variant="filled" label="Filled" />
  <Input variant="outlined" label="Outlined" />
</template>
```

### With Helper Text

```vue
<template>
  <Input
    v-model="username"
    label="Username"
    helper-text="Choose a unique username"
  />
</template>
```

### With Error Message

```vue
<template>
  <Input
    v-model="email"
    label="Email"
    :error-message="emailError"
  />
</template>

<script setup lang="ts">
const email = ref('')
const emailError = computed(() => {
  if (!email.value) return ''
  if (!email.value.includes('@')) return 'Please enter a valid email'
  return ''
})
</script>
```

### Clearable

```vue
<template>
  <Input
    v-model="search"
    label="Search"
    placeholder="Search..."
    :clearable="true"
  />
</template>
```

### Character Count

```vue
<template>
  <Input
    v-model="bio"
    label="Bio"
    :show-count="true"
    :maxlength="200"
  />
</template>
```

### With Icons

```vue
<template>
  <Input
    v-model="search"
    label="Search"
    :prefix-icon="SearchIcon"
  />

  <Input
    v-model="website"
    label="Website"
    :suffix-icon="LinkIcon"
  />
</template>

<script setup lang="ts">
import SearchIcon from './icons/SearchIcon.vue'
import LinkIcon from './icons/LinkIcon.vue'
</script>
```

### With Custom Slots

```vue
<template>
  <Input v-model="price" label="Price">
    <template #prefix>
      <span>$</span>
    </template>
  </Input>

  <Input v-model="url" label="Website URL">
    <template #prefix>
      <span>https://</span>
    </template>
    <template #suffix>
      <Button size="xs" variant="primary">Go</Button>
    </template>
  </Input>
</template>
```

### Disabled and Readonly

```vue
<template>
  <Input
    v-model="readonlyValue"
    label="Readonly"
    :readonly="true"
  />

  <Input
    v-model="disabledValue"
    label="Disabled"
    :disabled="true"
  />
</template>
```

### Required Field

```vue
<template>
  <Input
    v-model="requiredField"
    label="Email Address"
    :required="true"
    placeholder="Required field"
  />
</template>
```

### Number Input

```vue
<template>
  <Input
    v-model="quantity"
    type="number"
    label="Quantity"
    :min="1"
    :max="100"
    :step="1"
  />
</template>
```

### With Ref

```vue
<template>
  <Input ref="inputRef" v-model="value" label="Focus Example" />
  <Button @click="focusInput">Focus Input</Button>
</template>

<script setup lang="ts">
const inputRef = ref()
const value = ref('')

const focusInput = () => {
  inputRef.value?.focus()
}
</script>
```

## Integration Examples

### Login Form

```vue
<template>
  <Card title="Login">
    <form @submit.prevent="handleLogin">
      <Input
        v-model="credentials.email"
        type="email"
        label="Email"
        placeholder="you@example.com"
        :required="true"
        :error-message="errors.email"
      />

      <Input
        v-model="credentials.password"
        type="password"
        label="Password"
        placeholder="••••••••"
        :required="true"
        :error-message="errors.password"
      />

      <Button type="submit" variant="primary" block :loading="isLoading">
        Login
      </Button>
    </form>
  </Card>
</template>
```

### Search Bar

```vue
<template>
  <div class="search-bar">
    <Input
      v-model="searchQuery"
      :prefix-icon="SearchIcon"
      placeholder="Search agents..."
      :clearable="true"
      @keyup.enter="handleSearch"
    />
    <Button :icon="SearchIcon" @click="handleSearch">Search</Button>
  </div>
</template>
```

### URL Input

```vue
<template>
  <Input v-model="url" label="Website URL">
    <template #prefix>
      <span class="text-gray-500">https://</span>
    </template>
  </Input>
</template>
```

### Validation Example

```vue
<template>
  <Input
    v-model="password"
    type="password"
    label="Password"
    :show-count="true"
    :maxlength="32"
    :minlength="8"
    :error-message="passwordError"
    helper-text="Must be at least 8 characters"
  />
</template>

<script setup lang="ts">
const password = ref('')
const passwordError = computed(() => {
  if (password.value.length < 8) {
    return 'Password must be at least 8 characters'
  }
  return ''
})
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
- [x] v-model binding works
- [x] Label displays correctly
- [x] Helper text displays
- [x] Error message displays
- [x] Required indicator shows
- [x] Clear button works
- [x] Character count shows
- [x] Prefix icon/slot displays
- [x] Suffix icon/slot displays
- [x] Disabled state works
- [x] Readonly state works
- [x] Focus state styling
- [x] Error state styling
- [x] Different input types work
- [x] Number constraints work
- [x] Maxlength limits input
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

### Disabled/Readonly

Distinct visual states:
- Gray background
- Reduced opacity
- Not-allowed cursor (disabled)

### Responsive Sizing

Consistent sizing with:
- Height-based approach
- Touch-friendly targets
- Consistent padding

## File Changes

### New Files

1. **src/components/Input.vue** (~280 lines)
   - Input with all variants
   - Size options
   - Prefix/suffix support
   - Clear button
   - Character count
   - Label and helper text
   - Error state
   - Disabled/readonly states
   - Exposed methods
   - Dark mode support

## Benefits

### Over Native Input

| Aspect | Native | Input Component |
|--------|--------|-----------------|
| **Styling** | Manual | Props-based |
| **Validation** | Manual | Built-in states |
| **Icons** | Manual | Built-in slots |
| **Clear Button** | Manual | Built-in |
| **Character Count** | Manual | Built-in |
| **Accessibility** | Partial | Full support |
| **Consistency** | Variable | Standardized |
| **Error States** | Manual | Built-in styling |

### Use Cases

1. **Forms** - All form inputs
2. **Search** - Search bars
3. **Filters** - Filter inputs
4. **Login** - Authentication forms
5. **Settings** - Configuration inputs
6. **Data Entry** - CRUD operations
7. **URLs** - URL/path inputs
8. **Numbers** - Numeric inputs

## Future Enhancements

### Potential Additions

1. **Textarea** - Multi-line input variant
2. **Select** - Dropdown select variant
3. **Password Toggle** - Show/hide password
4. **Autocomplete** - Suggestion dropdown
5. **Mask** - Input masking (phone, date)
6. **Debounce** - Debounced input
7. **Character Counter** - Visual character bar
8. **Copy Button** - Copy value button

## Integration Opportunities

The Input component can be integrated with:

1. **Forms** - All form fields
2. **Search Bars** - Search/filter inputs
3. **Settings Pages** - Configuration inputs
4. **Modals** - Modal form inputs
5. **Tables** - Filter inputs
6. **Toolbars** - Search/toolbar inputs
7. **Cards** - Card form inputs
8. **Wizards** - Step form inputs

## CSS Architecture

### BEM Naming

- `.input` - Block (wrapper)
- `.input--size` - Modifier (e.g., `input--sm`)
- `.input--variant` - Modifier (e.g., `input--filled`)
- `.input__label` - Element
- `.input__container` - Element (input wrapper)
- `.input__field` - Element (actual input)
- `.input__prefix` - Element
- `.input__suffix` - Element
- `.input__helper` - Element
- `.input__error` - Element

### Accessibility Features

- Associated label with `for` attribute
- Unique ID generation
- Required indicator
- Error message association
- Disabled/readonly states
- Keyboard accessible
- Proper semantic HTML

## Summary

Input Component successfully provides:

✅ **Multiple Variants** - 3 styles (default, filled, outlined)
✅ **Flexible Sizing** - 3 size presets (sm, md, lg)
✅ **Icon Support** - Prefix and suffix slots/icons
✅ **Clear Button** - Built-in clear functionality
✅ **Character Count** - Optional count display
✅ **Label Support** - With required indicator
✅ **Validation States** - Error and helper text
✅ **Disabled/Readonly** - Proper state handling
✅ **v-model Support** - Two-way binding
✅ **Exposed Methods** - Focus, blur, select
✅ **Accessible** - Full ARIA support
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable input system for form data entry throughout the application, improving consistency and user experience.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

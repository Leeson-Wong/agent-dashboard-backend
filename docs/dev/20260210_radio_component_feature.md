# Radio Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Radio buttons are essential form elements for single-choice selections from a set of options. A reusable Radio component provides consistency, proper accessibility, and improved styling throughout the application.

The Radio component provides:
- Multiple sizes (sm, md, lg)
- Multiple color variants (default, primary, success, warning, error)
- v-model support for group selections
- Label and description support
- Disabled state
- Accessibility support

## Implementation

### Radio Component

**File**: `src/components/Radio.vue` (~180 lines)

#### Type Definitions

```typescript
export type RadioSize = 'sm' | 'md' | 'lg'
export type RadioColor = 'default' | 'primary' | 'success' | 'warning' | 'error'
```

## Feature Highlights

### Sizes

| Size | Box Size | Dot Size | Use Case |
|------|----------|----------|----------|
| **sm** | 1rem | 0.5rem | Compact forms |
| **md** | 1.25rem | 0.625rem | Default |
| **lg** | 1.5rem | 0.75rem | Emphasized radios |

### Color Variants

| Variant | Use Case | Color |
|---------|----------|-------|
| **default** | General purpose | Blue |
| **primary** | Primary actions | Blue |
| **success** | Success states | Green |
| **warning** | Warning states | Yellow |
| **error** | Error states | Red |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `modelValue` | `string \| number \| boolean` | - | v-model value |
| `value` | `string \| number \| boolean` | **required** | Radio value |
| `label` | `string` | - | Radio label |
| `description` | `string` | - | Helper/description text |
| `disabled` | `boolean` | `false` | Disabled state |
| `size` | `RadioSize` | `'md'` | Radio size |
| `color` | `RadioColor` | `'primary'` | Color variant |
| `name` | `string` | - | Input name |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `string \| number \| boolean` | Emitted on selection |
| `focus` | `FocusEvent` | Emitted on focus |
| `blur` | `FocusEvent` | Emitted on blur |
| `change` | `string \| number \| boolean` | Emitted on change |

### Exposed Methods

| Method | Description |
|--------|-------------|
| `focus()` | Focus the radio |
| `blur()` | Blur the radio |

## Usage Examples

### Basic Radio Group

```vue
<template>
  <div>
    <Radio v-model="selected" value="option1" label="Option 1" />
    <Radio v-model="selected" value="option2" label="Option 2" />
    <Radio v-model="selected" value="option3" label="Option 3" />
  </div>

  <p>Selected: {{ selected }}</p>
</template>

<script setup lang="ts">
const selected = ref('option1')
</script>
```

### With Label Prop

```vue
<template>
  <Radio v-model="plan" value="free" label="Free Plan" />
  <Radio v-model="plan" value="pro" label="Pro Plan" />
  <Radio v-model="plan" value="enterprise" label="Enterprise Plan" />
</template>
```

### With Description

```vue
<template>
  <Radio
    v-model="shipping"
    value="standard"
    label="Standard Shipping"
    description="5-7 business days, free"
  />

  <Radio
    v-model="shipping"
    value="express"
    label="Express Shipping"
    description="2-3 business days, $9.99"
  />
</template>
```

### Sizes

```vue
<template>
  <Radio v-model="selected" value="a" label="Small" size="sm" />
  <Radio v-model="selected" value="b" label="Medium" size="md" />
  <Radio v-model="selected" value="c" label="Large" size="lg" />
</template>
```

### Color Variants

```vue
<template>
  <Radio v-model="priority" value="low" label="Low" color="primary" />
  <Radio v-model="priority" value="medium" label="Medium" color="warning" />
  <Radio v-model="priority" value="high" label="High" color="error" />
</template>
```

### Disabled State

```vue
<template>
  <Radio v-model="selected" value="1" label="Available" />
  <Radio v-model="selected" value="2" label="Sold Out" :disabled="true" />
  <Radio v-model="selected" value="3" label="Premium (Locked)" :disabled="true" />
</template>
```

### With Numbers

```vue
<template>
  <div>
    <Radio v-model="rating" :value="1" label="1 Star" />
    <Radio v-model="rating" :value="2" label="2 Stars" />
    <Radio v-model="rating" :value="3" label="3 Stars" />
    <Radio v-model="rating" :value="4" label="4 Stars" />
    <Radio v-model="rating" :value="5" label="5 Stars" />
  </div>
</template>

<script setup lang="ts">
const rating = ref(3)
</script>
```

### Controlled with Ref

```vue
<template>
  <Radio ref="radioRef" v-model="selected" value="option1" label="Focus Example" />
  <Button @click="focusRadio">Focus Radio</Button>
</template>

<script setup lang="ts">
const radioRef = ref()
const selected = ref('option1')

const focusRadio = () => {
  radioRef.value?.focus()
}
</script>
```

### Custom Label Slot

```vue
<template>
  <Radio v-model="selected" value="custom">
    <span style="color: blue; font-weight: bold;">Custom styled label</span>
  </Radio>
</template>
```

## Integration Examples

### Plan Selection

```vue
<template>
  <Card title="Choose Your Plan">
    <div class="plans">
      <Radio
        v-model="selectedPlan"
        value="free"
        label="Free"
        description="$0/month, basic features"
      />

      <Radio
        v-model="selectedPlan"
        value="pro"
        label="Pro"
        description="$29/month, advanced features"
        color="primary"
      />

      <Radio
        v-model="selectedPlan"
        value="enterprise"
        label="Enterprise"
        description="Custom pricing, full access"
        color="success"
      />
    </div>

    <Divider />

    <Button variant="primary" block>Continue with {{ selectedPlan }}</Button>
  </Card>
</template>

<script setup lang="ts">
const selectedPlan = ref('free')
</script>
```

### Language Selection

```vue
<template>
  <div class="language-selector">
    <h3>Select Language</h3>

    <Radio v-model="language" value="en" label="English" />
    <Radio v-model="language" value="es" label="Spanish" />
    <Radio v-model="language" value="fr" label="French" />
    <Radio v-model="language" value="de" label="German" />
    <Radio v-model="language" value="zh" label="Chinese" />
  </div>
</template>

<script setup lang="ts">
const language = ref('en')
</script>
```

### Priority Setting

```vue
<template>
  <div class="priority-setting">
    <label>Task Priority</label>

    <Radio
      v-model="priority"
      value="low"
      label="Low"
      description="No urgency, can be done anytime"
      color="primary"
    />

    <Radio
      v-model="priority"
      value="medium"
      label="Medium"
      description="Standard priority"
      color="warning"
    />

    <Radio
      v-model="priority"
      value="high"
      label="High"
      description="Urgent, requires immediate attention"
      color="error"
    />
  </div>
</template>

<script setup lang="ts">
const priority = ref('medium')
</script>
```

### Time Zone Selection

```vue
<template>
  <SelectGroup label="Time Zone">
    <Radio v-model="timezone" value="pst" label="Pacific Time (PST)" />
    <Radio v-model="timezone" value="mst" label="Mountain Time (MST)" />
    <Radio v-model="timezone" value="cst" label="Central Time (CST)" />
    <Radio v-model="timezone" value="est" label="Eastern Time (EST)" />
  </SelectGroup>
</template>

<script setup lang="ts">
const timezone = ref('pst')
</script>
```

### Payment Method

```vue
<template>
  <Card title="Payment Method">
    <div class="payment-methods">
      <Radio
        v-model="paymentMethod"
        value="card"
        label="Credit Card"
        description="Visa, Mastercard, Amex"
      />

      <Radio
        v-model="paymentMethod"
        value="paypal"
        label="PayPal"
        description="Fast and secure checkout"
      />

      <Radio
        v-model="paymentMethod"
        value="bank"
        label="Bank Transfer"
        description="Direct bank transfer"
      />
    </div>
  </Card>
</template>

<script setup lang="ts">
const paymentMethod = ref('card')
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

- [x] v-model binding works correctly
- [x] Only one radio can be selected in a group
- [x] All sizes render correctly
- [x] All color variants work
- [x] Checked state displays correctly
- [x] Unchecked state displays correctly
- [x] Disabled state works
- [x] Label displays correctly
- [x] Description displays correctly
- [x] Focus state styling
- [x] Hover effects work
- [x] Click selects radio
- [x] Keyboard navigation works (arrow keys)
- [x] Exposed methods work
- [x] Same name attribute works
- [x] Different values work correctly
- [x] Dark mode support

## Styling Features

### Custom Radio Design

- Hidden native radio
- Custom styled circular box
- Inner dot indicator
- Scale animation on selection

### Focus States

Visual feedback on focus:
- Blue box-shadow ring
- Smooth transitions

### Color Variants

Different colors for different contexts:
- Primary (default)
- Success (green)
- Warning (yellow)
- Error (red)

### Animation

Smooth dot appearance:
- Transform scale from 0 to 1
- 0.2s ease transition

## File Changes

### New Files

1. **src/components/Radio.vue** (~180 lines)
   - Radio with all sizes
   - Color variants
   - v-model support
   - Label and description
   - Disabled state
   - Exposed methods
   - Dark mode support

## Benefits

### Over Native Radio

| Aspect | Native | Radio Component |
|--------|--------|-----------------|
| **Styling** | Limited | Full custom style |
| **Size Control** | Browser-dependent | Consistent |
| **Accessibility** | Basic | Enhanced |
| **Consistency** | Variable | Standardized |
| **Colors** | Manual | Built-in variants |
| **Label Layout** | Manual | Flexible |
| **Description** | Manual | Built-in support |

### Use Cases

1. **Forms** - Single choice selections
2. **Plan Selection** - Subscription tiers
3. **Settings** - Configuration options
4. **Surveys** - Multiple choice questions
5. **Filters** - Single-select filters
6. **Preferences** - User preferences
7. **Quizzes** - Quiz answer selection
8. **Language** - Language selection

## Future Enhancements

### Potential Additions

1. **Ripple Effect** - Material Design ripple
2. **Button Mode** - Button-style radio
3. **Card Mode** - Card-based selection
4. **Icon Support** - Icon in label
5. **Group Component** - RadioGroup wrapper
6. **Validation** - Built-in validation
7. **Animation** - Custom selection animation

## Integration Opportunities

The Radio component can be integrated with:

1. **Forms** - Form radio groups
2. **Settings Pages** - Setting selections
3. **Filter Panels** - Single-select filters
4. **Surveys** - Survey questions
5. **Wizards** - Wizard step selections
6. **Modals** - Modal form radios
7. **Cards** - Card-based selection
8. **Quizzes** - Quiz answer options

## CSS Architecture

### BEM Naming

- `.radio` - Block (wrapper)
- `.radio--size` - Modifier (e.g., `radio--sm`)
- `.radio--color` - Modifier (e.g., `radio--success`)
- `.radio__input` - Element (hidden input)
- `.radio__box` - Element (custom circle)
- `.radio__dot` - Element (inner dot)
- `.radio__label` - Element
- `.radio__description` - Element

### Hidden Input Approach

Native radio hidden but functional:
- `position: absolute`
- `opacity: 0`
- Maintains keyboard accessibility
- Screen reader accessible
- Groups work via `name` attribute

### Accessibility Features

- Associated label wraps radio
- `for` attribute not needed (wrapping)
- Focus visible styles
- Keyboard accessible (Arrow keys, Space)
- Proper grouping via `name` attribute

## Comparison with Checkbox

| Feature | Radio | Checkbox |
|---------|-------|----------|
| **Selection** | Single choice | Multiple choices |
| **Group Behavior** | Mutually exclusive | Independent |
| **Same v-model** | Shared value | Individual or array |
| **Visual** | Circular | Square |
| **Uncheck** | Can't uncheck (must select another) | Can uncheck |

## Summary

Radio Component successfully provides:

✅ **Flexible Sizing** - 3 size presets (sm, md, lg)
✅ **Color Variants** - 5 color options
✅ **v-model Support** - Group selection
✅ **Mutually Exclusive** - Automatic single selection
✅ **Label Support** - With description
✅ **Disabled State** - Proper handling
✅ **Custom Design** - Fully styled
✅ **Accessible** - Full keyboard/ARIA support
✅ **Group Support** - Works with name attribute
✅ **Exposed Methods** - Focus/blur control
✅ **Smooth Animation** - Dot scale transition
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a versatile, reusable radio system for single-choice selections throughout the application.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# Switch Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Switches (toggle switches) are modern UI elements for binary on/off choices. A reusable Switch component provides consistency, smooth animations, and improved user experience for boolean settings.

The Switch component provides:
- Multiple sizes (sm, md, lg)
- Multiple color variants (default, primary, success, warning, error)
- Smooth toggle animation
- Optional icons (check/cross)
- Label and description support
- Disabled state
- Accessibility support

## Implementation

### Switch Component

**File**: `src/components/Switch.vue` (~180 lines)

#### Type Definitions

```typescript
export type SwitchSize = 'sm' | 'md' | 'lg'
export type SwitchColor = 'default' | 'primary' | 'success' | 'warning' | 'error'
```

## Feature Highlights

### Sizes

| Size | Track Size | Thumb Size | Use Case |
|------|------------|------------|----------|
| **sm** | 2.5rem x 1.25rem | 1rem | Compact forms |
| **md** | 3rem x 1.5rem | 1.25rem | Default |
| **lg** | 3.5rem x 1.75rem | 1.5rem | Emphasized switches |

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
| `modelValue` | `boolean` | - | v-model value |
| `label` | `string` | - | Switch label |
| `description` | `string` | - | Helper/description text |
| `disabled` | `boolean` | `false` | Disabled state |
| `size` | `SwitchSize` | `'md'` | Switch size |
| `color` | `SwitchColor` | `'primary'` | Color variant |
| `name` | `string` | - | Input name |
| `checkedIcon` | `boolean` | `false` | Show checkmark when on |
| `uncheckedIcon` | `boolean` | `false` | Show X when off |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `boolean` | Emitted on toggle |
| `focus` | `FocusEvent` | Emitted on focus |
| `blur` | `FocusEvent` | Emitted on blur |
| `change` | `boolean` | Emitted on change |

### Exposed Methods

| Method | Description |
|--------|-------------|
| `focus()` | Focus the switch |
| `blur()` | Blur the switch |

## Usage Examples

### Basic Switch

```vue
<template>
  <Switch v-model="enabled">
    Enable notifications
  </Switch>
</template>

<script setup lang="ts">
const enabled = ref(false)
</script>
```

### With Label Prop

```vue
<template>
  <Switch v-model="darkMode" label="Dark Mode" />
</template>
```

### With Description

```vue
<template>
  <Switch
    v-model="autoSave"
    label="Auto-save"
    description="Automatically save changes every 30 seconds"
  />
</template>
```

### Sizes

```vue
<template>
  <Switch v-model="checked1" size="sm" label="Small" />
  <Switch v-model="checked2" size="md" label="Medium" />
  <Switch v-model="checked3" size="lg" label="Large" />
</template>
```

### Color Variants

```vue
<template>
  <Switch v-model="feature1" color="primary" label="Feature 1" />
  <Switch v-model="feature2" color="success" label="Feature 2" />
  <Switch v-model="feature3" color="warning" label="Feature 3" />
  <Switch v-model="feature4" color="error" label="Feature 4" />
</template>
```

### With Icons

```vue
<template>
  <Switch
    v-model="notifications"
    label="Notifications"
    :checked-icon="true"
    :unchecked-icon="true"
  />
</template>
```

### Disabled State

```vue
<template>
  <Switch v-model="setting" label="Available" />
  <Switch v-model="locked" label="Locked Setting" :disabled="true" />
</template>
```

### Controlled with Ref

```vue
<template>
  <Switch ref="switchRef" v-model="enabled" label="Focus Example" />
  <Button @click="focusSwitch">Focus Switch</Button>
</template>

<script setup lang="ts">
const switchRef = ref()
const enabled = ref(false)

const focusSwitch = () => {
  switchRef.value?.focus()
}
</script>
```

## Integration Examples

### Settings Panel

```vue
<template>
  <Card title="Settings">
    <div class="settings-list">
      <Switch
        v-model="settings.notifications"
        label="Push Notifications"
        description="Receive push notifications for important updates"
      />

      <Switch
        v-model="settings.emailAlerts"
        label="Email Alerts"
        description="Get email summaries of your activity"
      />

      <Switch
        v-model="settings.darkMode"
        label="Dark Mode"
        description="Use dark theme across the application"
        color="primary"
      />
    </div>
  </Card>
</template>

<script setup lang="ts">
const settings = ref({
  notifications: true,
  emailAlerts: false,
  darkMode: false
})
</script>
```

### Feature Toggles

```vue
<template>
  <div class="feature-flags">
    <h3>Beta Features</h3>

    <Switch
      v-model="features.newDashboard"
      label="New Dashboard"
      description="Try our redesigned dashboard interface"
      color="success"
    />

    <Switch
      v-model="features.advancedSearch"
      label="Advanced Search"
      description="Enable powerful search filters"
    />

    <Switch
      v-model="features.apiAccess"
      label="API Access"
      description="Enable programmatic access"
      color="warning"
    />
  </div>
</template>

<script setup lang="ts">
const features = ref({
  newDashboard: false,
  advancedSearch: true,
  apiAccess: false
})
</script>
```

### Privacy Controls

```vue
<template>
  <div class="privacy-settings">
    <h3>Privacy</h3>

    <Switch
      v-model="privacy.profileVisible"
      label="Public Profile"
      description="Allow others to see your profile"
      color="primary"
    />

    <Switch
      v-model="privacy.showActivity"
      label="Activity Status"
      description="Show when you're online"
    />

    <Switch
      v-model="privacy.allowMessages"
      label="Allow Messages"
      description="Let anyone send you messages"
    />

    <Switch
      v-model="privacy.dataSharing"
      label="Data Sharing"
      description="Help improve our service"
      color="error"
    />
  </div>
</template>

<script setup lang="ts">
const privacy = ref({
  profileVisible: true,
  showActivity: true,
  allowMessages: false,
  dataSharing: false
})
</script>
```

### Action Confirmation

```vue
<template>
  <div class="danger-zone">
    <h3>Danger Zone</h3>

    <Switch
      v-model="confirmDelete"
      label="Enable Delete"
      description="Enable the delete button below"
      color="error"
      :checked-icon="true"
      :unchecked-icon="true"
    />

    <Button
      v-if="confirmDelete"
      variant="error"
      @click="deleteAccount"
    >
      Delete Account
    </Button>
  </div>
</template>

<script setup lang="ts">
const confirmDelete = ref(false)

const deleteAccount = () => {
  // Delete account logic
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

- [x] v-model binding works correctly
- [x] Toggles on click
- [x] All sizes render correctly
- [x] All color variants work
- [x] On state displays correctly
- [x] Off state displays correctly
- [x] Disabled state works
- [x] Label displays correctly
- [x] Description displays correctly
- [x] Focus state styling
- [x] Hover effects work
- [x] Smooth thumb animation
- [x] Icons display correctly
- [x] Keyboard navigation works (Space to toggle)
- [x] Exposed methods work
- [x] Dark mode support

## Styling Features

### Smooth Animation

CSS transitions for:
- Thumb slide transform
- Track background color
- Focus ring appearance

### Visual Design

- Rounded track (pill shape)
- Circular thumb
- Box-shadow on thumb
- Track padding for thumb movement

### Color Variants

Different colors for different contexts:
- Primary (default) - Blue
- Success - Green
- Warning - Yellow
- Error - Red

### Focus States

Visual feedback on focus:
- Colored box-shadow ring
- Smooth transition

## File Changes

### New Files

1. **src/components/Switch.vue** (~180 lines)
   - Switch with all sizes
   - Color variants
   - v-model support
   - Optional icons
   - Label and description
   - Disabled state
   - Exposed methods
   - Dark mode support

## Benefits

### Over Checkbox for Boolean

| Aspect | Checkbox | Switch |
|--------|----------|--------|
| **Visual** | Square with check | Slide toggle |
| **Intent** | Selection | On/off action |
| **Space** | Compact | Wider |
| **Mobile** | Small target | Touch-friendly |
| **Animation** | Minimal | Smooth slide |
| **Modern Feel** | Traditional | Contemporary |

### Use Cases

1. **Settings** - On/off settings
2. **Features** - Feature toggles
3. **Privacy** - Privacy controls
4. **Notifications** - Notification preferences
5. **Modes** - Mode switching (dark/light)
6. **Permissions** - Permission toggles
7. **Automation** - Auto-enable features
8. **Confirmations** - Confirmation toggles

## Future Enhancements

### Potential Additions

1. **Loading State** - Loading spinner in thumb
2. **Custom Labels** - On/off text labels
3. **Icons Slot** - Custom icon slots
4. **Validation** - Required state
5. **Sizes** - More size options
6. **Animation** - Bounce effect
7. **Group** - Switch group component
8. **RTL** - Right-to-left support

## Integration Opportunities

The Switch component can be integrated with:

1. **Settings Pages** - Settings toggles
2. **User Preferences** - User options
3. **Feature Flags** - Feature toggles
4. **Privacy Settings** - Privacy controls
5. **Notification Settings** - Notification preferences
6. **Dashboard** - Dashboard options
7. **Modals** - Modal settings
8. **Forms** - Form boolean fields

## CSS Architecture

### BEM Naming

- `.switch` - Block (wrapper)
- `.switch--size` - Modifier (e.g., `switch--sm`)
- `.switch--color` - Modifier (e.g., `switch--success`)
- `.switch__input` - Element (hidden input)
- `.switch__track` - Element (background pill)
- `.switch__thumb` - Element (sliding circle)
- `.switch__icon` - Element (check/cross icon)
- `.switch__label` - Element
- `.switch__description` - Element

### Hidden Input Approach

Native checkbox hidden but functional:
- `position: absolute`
- `opacity: 0`
- Maintains keyboard accessibility
- Screen reader accessible
- Toggle behavior inherited

### Animation Approach

CSS transform for thumb:
- `translateX(0)` when off
- `translateX(100%)` when on
- 0.2s ease transition

## Accessibility

- Associated label wraps switch
- `for` attribute not needed (wrapping)
- Focus visible styles
- Keyboard accessible (Space to toggle)
- ARIA attributes from native input
- Semantic checkbox input

## Comparison with Checkbox

| Feature | Checkbox | Switch |
|---------|----------|--------|
| **Metaphor** | Selection | On/off action |
| **Visual** | Checkmark | Slide |
| **Space** | Compact | Wider |
| **Animation** | Fade | Slide |
| **Best For** | Forms, lists | Settings, toggles |
| **Mobile** | OK target | Better target |

## Summary

Switch Component successfully provides:

✅ **Flexible Sizing** - 3 size presets (sm, md, lg)
✅ **Color Variants** - 5 color options
✅ **v-model Support** - Two-way binding
✅ **Smooth Animation** - Thumb slide transition
✅ **Optional Icons** - Check/cross icons
✅ **Label Support** - With description
✅ **Disabled State** - Proper handling
✅ **Custom Design** - Modern toggle switch
✅ **Accessible** - Full keyboard/ARIA support
✅ **Touch Friendly** - Large click target
✅ **Exposed Methods** - Focus/blur control
✅ **Dark Mode** - Automatic theme adaptation
✅ **Type Safety** - Full TypeScript support

The component provides a modern, reusable switch system for boolean toggles and on/off settings throughout the application.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

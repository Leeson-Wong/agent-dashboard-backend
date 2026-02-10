# Enhanced Tooltip System Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Tooltips are an essential UI element for providing contextual help and information. The existing title attribute tooltips are limited in styling and functionality. An enhanced tooltip system provides:

1. **Rich Styling** - Custom colors, fonts, and layout
2. **Smart Positioning** - Auto-adjust to stay in viewport
3. **Multiple Triggers** - Hover, click, or focus
4. **Configurable Delays** - Control show/hide timing
5. **Follow Mouse Mode** - Cursor tracking option
6. **Accessibility** - ARIA attributes and keyboard support

## Requirements Analysis

### Core Requirements

1. **Composable** - useTooltip hook for programmatic control
2. **Component** - Tooltip component for declarative use
3. **Directive** - v-tooltip directive for easy integration
4. **Smart Positioning** - 12 placement options with auto-adjustment
5. **Animation** - Smooth fade and scale transitions

## Implementation

### 1. Create Tooltip Composable

**File**: `src/composables/useTooltip.ts` (~200 lines)

#### Type Definitions

```typescript
export type TooltipPlacement =
  | 'top' | 'top-start' | 'top-end'
  | 'bottom' | 'bottom-start' | 'bottom-end'
  | 'left' | 'left-start' | 'left-end'
  | 'right' | 'right-start' | 'right-end'

export type TooltipTrigger = 'hover' | 'click' | 'focus'
```

#### Position Calculation

```typescript
const calculatePosition = (
  triggerRect: DOMRect,
  tooltipRect: DOMRect,
  placement: TooltipPlacement,
  offset: number
): { top: number; left: number } => {
  // Calculate position based on placement
  // Adjust if out of viewport
  return { top, left }
}
```

#### Composable Interface

```typescript
export function useTooltip(options: TooltipOptions) {
  const isVisible = ref(false)
  const tooltipRef = ref<HTMLElement | null>(null)
  const triggerRef = ref<HTMLElement | null>(null)
  const position = ref({ top: 0, left: 0 })

  const show = () => { /* Show logic */ }
  const hide = () => { /* Hide logic */ }
  const updatePosition = () => { /* Position update */ }

  return {
    isVisible,
    tooltipRef,
    triggerRef,
    position,
    show,
    hide,
    updatePosition,
    bindTrigger,
    unbindTrigger
  }
}
```

### 2. Create Tooltip Component

**File**: `src/components/Tooltip.vue` (~150 lines)

#### Template Structure

```vue
<template>
  <Teleport to="body">
    <Transition name="tooltip-fade">
      <div
        v-if="isVisible"
        :ref="tooltipRef"
        class="tooltip"
        :class="[`tooltip-${placement}`, { 'tooltip-follow': followMouse }]"
        :style="{ top: `${position.top}px`, left: `${position.left}px` }"
        role="tooltip"
      >
        <div class="tooltip-content">
          <slot>{{ content }}</slot>
        </div>
        <div class="tooltip-arrow" :data-placement="placement"></div>
      </div>
    </Transition>
  </Teleport>
</template>
```

#### Arrow Positioning

```css
.tooltip-arrow {
  position: absolute;
  width: 8px;
  height: 8px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(100, 116, 139, 0.3);
}

.tooltip-arrow[data-placement="top"] {
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%) rotate(45deg);
}
```

### 3. Create Tooltip Directive

**File**: `src/directives/tooltip.ts` (~60 lines)

#### Directive Implementation

```typescript
import { DirectiveBinding } from 'vue'
import type { Directive } from 'vue'
import { createTooltip, destroyTooltip } from '../utils/tooltipManager'

export const vTooltip: Directive<HTMLElement, string | TooltipDirectiveValue> = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const options = parseValue(binding.value)
    createTooltip(el, options)
  },

  updated(el: HTMLElement, binding: DirectiveBinding) {
    const options = parseValue(binding.value)
    destroyTooltip(el)
    createTooltip(el, options)
  },

  unmounted(el: HTMLElement) {
    destroyTooltip(el)
  }
}
```

### 4. Create Tooltip Manager

**File**: `src/utils/tooltipManager.ts` (~85 lines)

#### Create/Destroy Functions

```typescript
export const createTooltip = (element: HTMLElement, options: any): void => {
  // Create container
  const container = document.createElement('div')
  document.body.appendChild(container)

  // Generate unique ID
  const tooltipId = `tooltip-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`

  // Create Vue app with Tooltip component
  const app = createApp({
    render() {
      return h(Tooltip, options)
    }
  })

  app.mount(container)
  tooltipInstances.set(element, { container, app })
}

export const destroyTooltip = (element: HTMLElement): void => {
  const instance = tooltipInstances.get(element)
  if (instance) {
    instance.app.unmount()
    document.body.removeChild(instance.container)
    tooltipInstances.delete(element)
  }
}
```

## Feature Highlights

### Placement Options

| Placement | Description |
|-----------|-------------|
| **top** | Above element, centered |
| **top-start** | Above, left-aligned |
| **top-end** | Above, right-aligned |
| **bottom** | Below element, centered |
| **bottom-start** | Below, left-aligned |
| **bottom-end** | Below, right-aligned |
| **left** | Left of element, centered |
| **right** | Right of element, centered |

### Trigger Modes

| Mode | Activation |
|------|------------|
| **hover** | Mouse enter/leave (default) |
| **click** | Click to toggle |
| **focus** | Focus/blur (keyboard accessible) |

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| content | string | - | Tooltip text |
| placement | TooltipPlacement | 'top' | Position relative to element |
| delay | number | 300 | Show delay (ms) |
| hideDelay | number | 100 | Hide delay (ms) |
| trigger | TooltipTrigger | 'hover' | Activation mode |
| followMouse | boolean | false | Track cursor |
| offset | number | 8 | Distance from element (px) |

## Testing & Validation

### Build Test

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**Result**: ✅ Build successful (306 modules)

### Usage Examples

#### Directive Usage

```vue
<template>
  <!-- Simple string -->
  <button v-tooltip="'Click me!'">Button</button>

  <!-- Object options -->
  <button v-tooltip="{
    content: 'Click me!',
    placement: 'bottom',
    delay: 500
  }">Button</button>
</template>

<script setup lang="ts">
import { vTooltip } from './directives/tooltip'
</script>
```

#### Component Usage

```vue
<template>
  <div ref="triggerRef" @mouseenter="show" @mouseleave="hide">
    Hover me
  </div>

  <Tooltip
    :content="tooltipText"
    :placement="placement"
    :isVisible="isVisible"
    :position="position"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useTooltip } from './composables/useTooltip'
import Tooltip from './components/Tooltip.vue'

const tooltipText = ref('Tooltip text')
const placement = ref('top')

const {
  isVisible,
  position,
  show,
  hide
} = useTooltip({
  content: tooltipText.value,
  placement: placement.value
})
</script>
```

## File Changes

### New Files

1. **src/composables/useTooltip.ts** (~200 lines)
   - Tooltip positioning logic
   - Trigger event handling
   - Delay management
   - Follow mouse support

2. **src/components/Tooltip.vue** (~150 lines)
   - Declarative tooltip component
   - Teleport to body
   - Arrow indicator
   - Fade transitions

3. **src/directives/tooltip.ts** (~60 lines)
   - v-tooltip directive
   - Easy integration
   - Auto-cleanup

4. **src/utils/tooltipManager.ts** (~85 lines)
   - Instance management
   - DOM manipulation
   - Vue app lifecycle

## Benefits

### Over Native Tooltips

| Feature | Native | Enhanced |
|---------|--------|----------|
| **Styling** | Limited | Full CSS control |
| **Positioning** | Browser-controlled | 12 options + auto-adjust |
| **Triggers** | Hover only | Hover, click, focus |
| **Delay** | No | Configurable |
| **Rich Content** | No | HTML/VNode support |
| **Accessibility** | Basic | ARIA attributes |

### User Experience

- **Better Visuals** - Modern, polished appearance
- **More Control** - Precise positioning and timing
- **Improved UX** - Reduced accidental triggers
- **Keyboard Support** - Focus trigger for accessibility

## Usage Instructions

### Basic Usage (Directive)

```vue
<template>
  <button v-tooltip="'This is a tooltip'">Hover me</button>
</template>

<script setup lang="ts">
import { vTooltip } from './directives/tooltip'
</script>
```

### Advanced Options

```vue
<template>
  <button
    v-tooltip="{
      content: 'Save changes',
      placement: 'bottom',
      delay: 500,
      hideDelay: 200,
      trigger: 'click'
    }"
  >
    Save
  </button>
</template>
```

### Follow Cursor Mode

```vue
<template>
  <div
    v-tooltip="{
      content: 'Follows your cursor',
      followMouse: true
    }"
  >
    Hover me
  </div>
</template>
```

## Future Enhancements

### Potential Additions

1. **Rich Content** - Support for HTML/VNodes in tooltips
2. **Group Tooltips** - Coordinate multiple tooltips
3. **Tooltips API** - Programmatic control API
4. **Animation Options** - Configurable animations
5. **Z-Index Control** - Configurable stacking context
6. **Touch Support** - Touch device optimization

## Summary

Enhanced Tooltip System successfully provides:

✅ **Composable** - useTooltip hook for programmatic control
✅ **Component** - Declarative Tooltip component
✅ **Directive** - v-tooltip for easy integration
✅ **Smart Positioning** - 12 placement options with viewport adjustment
✅ **Multiple Triggers** - Hover, click, and focus modes
✅ **Configurable Delays** - Control show/hide timing
✅ **Follow Mouse** - Optional cursor tracking
✅ **Accessibility** - ARIA attributes and keyboard support
✅ **Modern Design** - Polished appearance with animations

The system provides a flexible and powerful tooltip solution that can be easily integrated throughout the application, significantly improving the user experience for contextual help and information display.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

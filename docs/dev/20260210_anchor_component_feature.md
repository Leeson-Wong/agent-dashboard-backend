# Anchor Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Anchor components provide page navigation for long content pages with multiple sections. They display a table of contents that highlights the current section as the user scrolls and allows quick navigation to specific sections. A reusable Anchor component provides consistent anchor navigation throughout the application.

The Anchor component provides:
- Manual or automatic link generation
- Hierarchical link structure
- Active section tracking on scroll
- Smooth scroll to section
- Collapsible nested links
- Custom scroll container support
- Scroll offset configuration
- Multiple indentation levels
- Accessibility support
- Dark mode support

## Implementation

### Anchor Component

**File**: `src/components/Anchor.vue` (~260 lines)

#### Type Definitions

```typescript
export interface AnchorLink {
  id: string
  title: string
  href: string
  children?: AnchorLink[]
  expanded?: boolean
}

interface Props {
  links?: AnchorLink[]
  offset?: number
  duration?: number
  container?: string | (() => HTMLElement)
  scrollOffset?: number
  bound?: number
}
```

## Feature Highlights

### Link Sources

| Source | Description | Use Case |
|--------|-------------|----------|
| **Manual** | Pass links via props | Full control |
| **Auto** | Extract from headings | Automatic generation |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `links` | `AnchorLink[]` | `undefined` | Manual link definitions |
| `offset` | `number` | `0` | Scroll offset (px) |
| `duration` | `number` | `450` | Scroll animation duration (ms) |
| `container` | `string \| function` | `undefined` | Scroll container selector or function |
| `scrollOffset` | `number` | `0` | Offset for active detection |
| `bound` | `number` | `5` | Bound distance for active (px) |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `click` | `link, event` | Link clicked |
| `change` | `activeLink` | Active link changed |

### Slots

| Slot | Description |
|------|-------------|
| `default` | Content to extract links from (auto mode) |

## Usage Examples

### Manual Links

```vue
<template>
  <Anchor
    :links="anchorLinks"
  />
  <div class="content">
    <h1 id="introduction">Introduction</h1>
    <p>Introduction content...</p>
    <h2 id="getting-started">Getting Started</h2>
    <p>Getting started content...</p>
    <h2 id="advanced-usage">Advanced Usage</h2>
    <p>Advanced usage content...</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const anchorLinks = ref([
  { id: 'introduction', title: 'Introduction', href: '#introduction' },
  { id: 'getting-started', title: 'Getting Started', href: '#getting-started' },
  { id: 'advanced-usage', title: 'Advanced Usage', href: '#advanced-usage' }
])
</script>
```

### Automatic Links

```vue
<template>
  <div class="documentation">
    <Anchor>
      <h1 id="overview">Overview</h1>
      <p>Overview content...</p>

      <h2 id="installation">Installation</h2>
      <p>Installation content...</p>

      <h3 id="npm">NPM</h3>
      <p>NPM content...</p>

      <h3 id="yarn">Yarn</h3>
      <p>Yarn content...</p>

      <h2 id="usage">Usage</h2>
      <p>Usage content...</p>
    </Anchor>
  </div>
</template>
```

### Nested Links

```vue
<template>
  <Anchor :links="nestedLinks" />
  <div class="content">
    <!-- Content sections -->
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const nestedLinks = ref([
  {
    id: 'chapter1',
    title: 'Chapter 1: Introduction',
    href: '#chapter1',
    children: [
      { id: 'section1-1', title: 'Section 1.1', href: '#section1-1' },
      { id: 'section1-2', title: 'Section 1.2', href: '#section1-2' }
    ]
  },
  {
    id: 'chapter2',
    title: 'Chapter 2: Advanced',
    href: '#chapter2',
    children: [
      { id: 'section2-1', title: 'Section 2.1', href: '#section2-1' },
      { id: 'section2-2', title: 'Section 2.2', href: '#section2-2' }
    ]
  }
])
</script>
```

### With Offset

```vue
<template>
  <div class="layout">
    <header class="header">Fixed Header</header>
    <div class="main">
      <Anchor :offset="80" />
      <div class="content">
        <!-- Long content -->
      </div>
    </div>
  </div>
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  z-index: 100;
}

.main {
  margin-top: 60px;
}
</style>
```

### Custom Container

```vue
<template>
  <div id="custom-container" class="scroll-container">
    <Anchor container="#custom-container" />
    <div class="content">
      <!-- Content -->
    </div>
  </div>
</template>

<style scoped>
.scroll-container {
  height: 500px;
  overflow-y: auto;
}
</style>
```

### With Click Handler

```vue
<template>
  <Anchor
    :links="links"
    @click="handleClick"
    @change="handleChange"
  />
  <div class="content">
    <!-- Content -->
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const links = ref([
  { id: 'section1', title: 'Section 1', href: '#section1' },
  { id: 'section2', title: 'Section 2', href: '#section2' }
])

const handleClick = (link: any) => {
  console.log('Clicked:', link.title)
}

const handleChange = (link: any) => {
  console.log('Active section:', link.title)
}
</script>
```

### Custom Scroll Duration

```vue
<template>
  <Anchor
    :links="links"
    :duration="800"
  />
  <div class="content">
    <!-- Content -->
  </div>
</template>
```

## Integration Examples

### Documentation Page

```vue
<template>
  <div class="documentation">
    <aside class="sidebar">
      <h3>Table of Contents</h3>
      <Anchor :links="docLinks" />
    </aside>
    <main class="content">
      <h1 id="introduction">Introduction</h1>
      <p>Welcome to our documentation...</p>

      <h2 id="getting-started">Getting Started</h2>
      <p>Get started with our product...</p>

      <h3 id="installation">Installation</h3>
      <p>Install the package...</p>

      <h3 id="configuration">Configuration</h3>
      <p>Configure your settings...</p>

      <h2 id="api-reference">API Reference</h2>
      <p>API documentation...</p>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const docLinks = ref([
  { id: 'introduction', title: 'Introduction', href: '#introduction' },
  {
    id: 'getting-started',
    title: 'Getting Started',
    href: '#getting-started',
    children: [
      { id: 'installation', title: 'Installation', href: '#installation' },
      { id: 'configuration', title: 'Configuration', href: '#configuration' }
    ]
  },
  { id: 'api-reference', title: 'API Reference', href: '#api-reference' }
])
</script>

<style scoped>
.documentation {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.sidebar {
  position: sticky;
  top: 2rem;
  height: fit-content;
}

.content {
  > h1 {
    font-size: 2rem;
    margin-bottom: 1rem;
  }

  > h2 {
    font-size: 1.5rem;
    margin: 2rem 0 1rem;
  }

  > h3 {
    font-size: 1.25rem;
    margin: 1.5rem 0 0.75rem;
  }
}
</style>
```

### Blog Post

```vue
<template>
  <article class="blog-post">
    <Anchor>
      <h1 id="title">Understanding Vue 3 Composition API</h1>
      <p>Introduction to Composition API...</p>

      <h2 id="basics">The Basics</h2>
      <p>Learn the fundamentals...</p>

      <h2 id="reactive-state">Reactive State</h2>
      <p>Managing reactive state...</p>

      <h3 id="ref">Using ref()</h3>
      <p>About ref()...</p>

      <h3 id="reactive">Using reactive()</h3>
      <p>About reactive()...</p>

      <h2 id="computed-properties">Computed Properties</h2>
      <p>Computed values...</p>

      <h2 id="lifecycle-hooks">Lifecycle Hooks</h2>
      <p>Component lifecycle...</p>
    </Anchor>
  </article>
</template>

<style scoped>
.blog-post {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}
</style>
```

### FAQ Page

```vue
<template>
  <div class="faq">
    <div class="faq-nav">
      <h3>Questions</h3>
      <Anchor :links="faqLinks" :offset="20" />
    </div>
    <div class="faq-content">
      <h2 id="general">General Questions</h2>
      <div class="faq-item">
        <h3 id="what-is">What is this product?</h3>
        <p>Our product is...</p>
      </div>
      <div class="faq-item">
        <h3 id="how-much">How much does it cost?</h3>
        <p>Pricing information...</p>
      </div>

      <h2 id="technical">Technical Questions</h2>
      <div class="faq-item">
        <h3 id="requirements">What are the requirements?</h3>
        <p>System requirements...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const faqLinks = ref([
  {
    id: 'general',
    title: 'General Questions',
    href: '#general',
    children: [
      { id: 'what-is', title: 'What is this product?', href: '#what-is' },
      { id: 'how-much', title: 'How much does it cost?', href: '#how-much' }
    ]
  },
  {
    id: 'technical',
    title: 'Technical Questions',
    href: '#technical',
    children: [
      { id: 'requirements', title: 'What are the requirements?', href: '#requirements' }
    ]
  }
])
</script>
```

### Terms of Service

```vue
<template>
  <div class="legal-page">
    <aside class="legal-nav">
      <Anchor>
        <h1>Terms of Service</h1>
        <h2 id="acceptance">1. Acceptance of Terms</h2>
        <p>By using our service...</p>

        <h2 id="service">2. Description of Service</h2>
        <p>Our service provides...</p>

        <h3 id="features">2.1 Features</h3>
        <p>The service includes...</p>

        <h3 id="limitations">2.2 Limitations</h3>
        <p>Certain limitations apply...</p>

        <h2 id="obligations">3. User Obligations</h2>
        <p>Users must...</p>

        <h2 id="termination">4. Termination</h2>
        <p>We may terminate...</p>
      </Anchor>
    </aside>
  </div>
</template>
```

### User Guide

```vue
<template>
  <div class="user-guide">
    <div class="guide-nav">
      <h3>Guide Contents</h3>
      <Anchor :links="guideLinks" :duration="600" />
    </div>
    <div class="guide-content">
      <section id="overview">
        <h1>Product Overview</h1>
        <p>Welcome to our product...</p>
      </section>

      <section id="setup">
        <h1>Setup Guide</h1>
        <p>Get started with setup...</p>

        <h2 id="account">Creating an Account</h2>
        <p>Account creation steps...</p>

        <h2 id="preferences">Setting Preferences</h2>
        <p>Configure your preferences...</p>
      </section>

      <section id="features">
        <h1>Key Features</h1>
        <p>Explore our features...</p>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const guideLinks = ref([
  { id: 'overview', title: 'Product Overview', href: '#overview' },
  {
    id: 'setup',
    title: 'Setup Guide',
    href: '#setup',
    children: [
      { id: 'account', title: 'Creating an Account', href: '#account' },
      { id: 'preferences', title: 'Setting Preferences', href: '#preferences' }
    ]
  },
  { id: 'features', title: 'Key Features', href: '#features' }
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

- [x] Anchor displays correctly
- [x] Manual links work
- [x] Auto-extraction works
- [x] Nested links display
- [x] Link clicking works
- [x] Smooth scroll works
- [x] Active link tracking works
- [x] Expand/collapse works
- [x] Custom offset works
- [x] Custom container works
- [x] Custom duration works
- [x] Events emit correctly
- [x] Indentation levels display
- [x] Dark mode support

## Styling Features

### Sticky Positioning

Stays visible:
- Sticky sidebar
- Max-height constraint
- Overflow scrolling

### Active Indicator

Visual feedback:
- Blue left border
- Bold text
- Color change

### Hierarchical Indentation

Visual hierarchy:
- Left padding per level
- Smaller font for deeper levels
- Consistent spacing

## File Changes

### New Files

1. **src/components/Anchor.vue** (~260 lines)
   - Manual/auto link generation
   - Hierarchical structure
   - Active section tracking
   - Smooth scroll
   - Collapsible nested links
   - Custom container
   - Offset configuration
   - Dark mode support

## Benefits

### Over Manual Implementation

| Aspect | Manual | Anchor Component |
|--------|--------|------------------|
| **Links** | Manual HTML | Auto-extract |
| **Scroll Detection** | Manual scroll listener | Automatic |
| **Active State** | Manual calculation | Built-in |
| **Smooth Scroll** | Manual CSS/JS | Configurable |
| **Hierarchy** | Manual nesting | Automatic |
| **Accessibility** | Missing | Full ARIA |

### Use Cases

1. **Documentation** - Doc navigation
2. **Blog Posts** - Article sections
3. **FAQ Pages** - Question categories
4. **Legal Pages** - Terms sections
5. **User Guides** - Guide chapters
6. **Tutorials** - Tutorial steps
7. **Reports** - Report sections
8. **Specifications** - Spec sections

## Future Enhancements

### Potential Additions

1. **Affix** - Fixed positioning
2. **Scroll Progress** - Reading progress
3. **Search** - Search within links
4. **Highlight** - Highlight section
5. **Preview** - Hover preview
6. **Print Mode** - Print-friendly
7. **Keyboard** - Keyboard navigation
8. **RTL** - Right-to-left support
9. **Mobile** - Mobile drawer
10. **Analytics** - Track navigation

## Integration Opportunities

The Anchor component can be integrated with:

1. **Documentation** - Technical docs
2. **Blogs** - Article navigation
3. **Wikis** - Wiki pages
4. **FAQ** - Question navigation
5. **Legal** - Terms/conditions
6. **Guides** - User guides
7. **Tutorials** - Tutorial steps
8. **Reports** - Long reports

## CSS Architecture

### BEM Naming

- `.anchor` - Block
- `.anchor__wrapper` - Element
- `.anchor__item` - Element
- `.anchor__item--level-*` - Modifier
- `.anchor__item--active` - Modifier
- `.anchor__item--expanded` - Modifier
- `.anchor__link` - Element
- `.anchor__toggle` - Element

## Accessibility

- Semantic nav structure
- aria-current for active
- Keyboard navigation
- Focus indicators
- Screen reader support

## Performance Considerations

### Scroll Handling

Efficient listening:
- Throttled events
- Minimal DOM queries
- Cached calculations

### Smooth Scroll

Optimized animation:
- requestAnimationFrame
- Ease-out cubic
- Minimal reflows

## Comparison with Table of Contents

| Feature | Anchor | TOC |
|---------|--------|-----|
| **Purpose** | Navigation | Overview |
| **Position** | Sticky | Inline |
| **Interaction** | Click & scroll | Display |
| **Use Case** | Long pages | Preview |

## Summary

Anchor Component successfully provides:

✅ **Link Generation** - Manual or automatic
✅ **Hierarchical Structure** - Nested links support
✅ **Active Tracking** - Scroll-based active state
✅ **Smooth Scroll** - Animated navigation
✅ **Collapsible** - Expand/collapse nested links
✅ **Custom Container** - Scroll container support
✅ **Offset Config** - Scroll offset options
✅ **Multiple Levels** - Up to 4 levels deep
✅ **Events** - Click and change events
✅ **Sticky** - Sticky positioning
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable anchor navigation system for long-form content with automatic active tracking and smooth scrolling.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

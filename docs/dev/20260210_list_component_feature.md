# List/ListItem Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Lists are fundamental UI components for displaying groups of related items. A reusable List/ListItem component system provides consistent presentation of collections, selections, and navigation throughout the application.

The List/ListItem components provide:
- Multiple sizes (sm, md, lg)
- Multiple variants (default, bordered, striped, hover)
- Alignment options (left, center, right)
- Optional numbering/inline modes
- Selectable items with checkboxes
- Active/disabled states
- Color variants
- Flexible content structure
- Accessibility support

## Implementation

### List Component

**File**: `src/components/List.vue` (~150 lines)

#### Type Definitions

```typescript
export type ListSize = 'sm' | 'md' | 'lg'
export type ListVariant = 'default' | 'bordered' | 'striped' | 'hover'
export type ListAlign = 'left' | 'center' | 'right'
```

### ListItem Component

**File**: `src/components/ListItem.vue` (~280 lines)

#### Type Definitions

```typescript
export type ListItemSize = 'sm' | 'md' | 'lg'
export type ListItemColor = 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info'
```

## Feature Highlights

### List Component

#### Sizes

| Size | Item Padding | Font Size | Use Case |
|------|--------------|-----------|----------|
| **sm** | 0.375rem 0.5rem | 0.8125rem | Compact lists |
| **md** | 0.5rem 0.75rem | 0.875rem | Default |
| **lg** | 0.75rem 1rem | 0.9375rem | Large items |

#### Variants

| Variant | Description | Best For |
|---------|-------------|----------|
| **default** | No special styling | General use |
| **bordered** | Border around list and between items | Distinct containers |
| **striped** | Alternating row colors | Data tables |
| **hover** | Hover highlight | Interactive lists |

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `size` | `ListSize` | `'md'` | List size |
| `variant` | `ListVariant` | `'default'` | List variant |
| `align` | `ListAlign` | `'left'` | Text alignment |
| `spaced` | `boolean` | `false` | Add spacing between items |
| `numbered` | `boolean` | `false` | Auto-number items |
| `inline` | `boolean` | `false` | Horizontal layout |
| `tag` | `string` | `'ul'` | HTML tag |

### ListItem Component

#### Sizes

| Size | Min Height | Padding | Use Case |
|------|------------|---------|----------|
| **sm** | 2rem | 0.375rem 0.5rem | Compact items |
| **md** | 2.75rem | 0.5rem 0.75rem | Default |
| **lg** | 3.5rem | 0.75rem 1rem | Large items |

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `tag` | `string` | `'li'` | HTML tag |
| `title` | `string` | - | Item title |
| `subtitle` | `string` | - | Item subtitle |
| `meta` | `string` | - | Meta information |
| `size` | `ListItemSize` | `'md'` | Item size |
| `color` | `ListItemColor` | `'default'` | Color variant |
| `disabled` | `boolean` | `false` | Disabled state |
| `active` | `boolean` | `false` | Active state |
| `selectable` | `boolean` | `false` | Show checkbox |
| `selected` | `boolean` | `false` | Selected state |
| `trailingIcon` | `Component` | - | Trailing icon |
| `clickable` | `boolean` | `false` | Clickable style |

#### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `click` | `Event` | Item clicked |
| `select` | `boolean` | Selection changed |

#### Slots

| Slot | Description |
|------|-------------|
| `default` | Item content |
| `leading` | Leading icon/avatar |
| `title` | Title content |
| `subtitle` | Subtitle content |
| `meta` | Meta information |
| `trailing` | Trailing content |

## Usage Examples

### Basic List

```vue
<template>
  <List>
    <ListItem>Item 1</ListItem>
    <ListItem>Item 2</ListItem>
    <ListItem>Item 3</ListItem>
  </List>
</template>
```

### Sizes

```vue
<template>
  <List size="sm">
    <ListItem>Small Item 1</ListItem>
    <ListItem>Small Item 2</ListItem>
  </List>

  <List size="md">
    <ListItem>Medium Item 1</ListItem>
    <ListItem>Medium Item 2</ListItem>
  </List>

  <List size="lg">
    <ListItem>Large Item 1</ListItem>
    <ListItem>Large Item 2</ListItem>
  </List>
</template>
```

### Variants

```vue
<template>
  <List variant="bordered">
    <ListItem>Bordered Item 1</ListItem>
    <ListItem>Bordered Item 2</ListItem>
  </List>

  <List variant="striped">
    <ListItem>Striped Item 1</ListItem>
    <ListItem>Striped Item 2</ListItem>
    <ListItem>Striped Item 3</ListItem>
  </List>

  <List variant="hover">
    <ListItem>Hoverable Item 1</ListItem>
    <ListItem>Hoverable Item 2</ListItem>
  </List>
</template>
```

### With Titles and Subtitles

```vue
<template>
  <List>
    <ListItem
      title="John Doe"
      subtitle="Software Engineer"
      meta "@johndoe"
    />
    <ListItem
      title="Jane Smith"
      subtitle="Product Designer"
      meta "@janesmith"
    />
    <ListItem
      title="Bob Johnson"
      subtitle="Project Manager"
      meta "@bobjohnson"
    />
  </List>
</template>
```

### With Avatars

```vue
<template>
  <List>
    <ListItem
      title="John Doe"
      subtitle="Software Engineer"
    >
      <template #leading>
        <Avatar src="https://i.pravatar.cc/150?img=1" />
      </template>
    </ListItem>
    <ListItem
      title="Jane Smith"
      subtitle="Product Designer"
    >
      <template #leading>
        <Avatar src="https://i.pravatar.cc/150?img=2" />
      </template>
    </ListItem>
  </List>
</template>

<script setup lang="ts">
import Avatar from './Avatar.vue'
</script>
```

### Selectable Items

```vue
<template>
  <List>
    <ListItem
      v-for="item in items"
      :key="item.id"
      :title="item.title"
      selectable
      :selected="selectedItems.includes(item.id)"
      @select="toggleSelect(item.id)"
    />
  </List>
</template>

<script setup lang="ts">
const items = ref([
  { id: 1, title: 'Item 1' },
  { id: 2, title: 'Item 2' },
  { id: 3, title: 'Item 3' }
])

const selectedItems = ref<number[]>([])

const toggleSelect = (id: number) => {
  const index = selectedItems.value.indexOf(id)
  if (index > -1) {
    selectedItems.value.splice(index, 1)
  } else {
    selectedItems.value.push(id)
  }
}
</script>
```

### Clickable Items

```vue
<template>
  <List>
    <ListItem
      v-for="page in pages"
      :key="page.id"
      :title="page.title"
      :subtitle="page.description"
      clickable
      @click="navigate(page.id)"
    />
  </List>
</template>

<script setup lang="ts">
const pages = ref([
  { id: 1, title: 'Dashboard', description: 'View your dashboard' },
  { id: 2, title: 'Settings', description: 'Manage settings' },
  { id: 3, title: 'Profile', description: 'Edit your profile' }
])

const navigate = (id: number) => {
  console.log('Navigate to page', id)
}
</script>
```

### Active Items

```vue
<template>
  <List>
    <ListItem
      v-for="item in items"
      :key="item.id"
      :title="item.title"
      :active="item.id === activeId"
      @click="activeId = item.id"
    />
  </List>
</template>

<script setup lang="ts">
const items = ref([
  { id: 1, title: 'Item 1' },
  { id: 2, title: 'Item 2' },
  { id: 3, title: 'Item 3' }
])

const activeId = ref(1)
</script>
```

### Numbered List

```vue
<template>
  <List numbered>
    <ListItem>First item</ListItem>
    <ListItem>Second item</ListItem>
    <ListItem>Third item</ListItem>
  </List>
</template>
```

### Inline List

```vue
<template>
  <List inline>
    <ListItem>React</ListItem>
    <ListItem>Vue</ListItem>
    <ListItem>Angular</ListItem>
    <ListItem>Svelte</ListItem>
  </List>
</template>
```

### Color Variants

```vue
<template>
  <List>
    <ListItem title="Primary" color="primary" />
    <ListItem title="Success" color="success" />
    <ListItem title="Warning" color="warning" />
    <ListItem title="Error" color="error" />
    <ListItem title="Info" color="info" />
  </List>
</template>
```

### With Trailing Icons

```vue
<template>
  <List>
    <ListItem
      title="Settings"
      :trailing-icon="ChevronRightIcon"
      clickable
    />
    <ListItem
      title="Notifications"
      :trailing-icon="ChevronRightIcon"
      clickable
    />
    <ListItem
      title="Logout"
      :trailing-icon="LogoutIcon"
      clickable
    />
  </List>
</template>

<script setup lang="ts">
import ChevronRightIcon from './icons/ChevronRightIcon.vue'
import LogoutIcon from './icons/LogoutIcon.vue'
</script>
```

## Integration Examples

### User List

```vue
<template>
  <List variant="bordered">
    <ListItem
      v-for="user in users"
      :key="user.id"
      :title="user.name"
      :subtitle="user.email"
      clickable
      @click="showUser(user)"
    >
      <template #leading>
        <Avatar :name="user.name" />
      </template>
      <template #trailing>
        <Chip :label="user.role" size="sm" color="info" variant="soft" />
      </template>
    </ListItem>
  </List>
</template>

<script setup lang="ts">
const users = ref([
  { id: 1, name: 'Alice', email: 'alice@example.com', role: 'Admin' },
  { id: 2, name: 'Bob', email: 'bob@example.com', role: 'User' },
  { id: 3, name: 'Charlie', email: 'charlie@example.com', role: 'User' }
])

const showUser = (user: any) => {
  console.log('Show user', user)
}
</script>
```

### Navigation Menu

```vue
<template>
  <nav>
    <List>
      <ListItem
        v-for="item in menuItems"
        :key="item.path"
        :title="item.title"
        clickable
        :active="currentPath === item.path"
        @click="navigate(item.path)"
      >
        <template #leading>
          <component :is="item.icon" class="menu-icon" />
        </template>
      </ListItem>
    </List>
  </nav>
</template>

<script setup lang="ts">
const menuItems = ref([
  { path: '/', title: 'Home', icon: HomeIcon },
  { path: '/products', title: 'Products', icon: ProductsIcon },
  { path: '/about', title: 'About', icon: AboutIcon }
])

const currentPath = ref('/')
const navigate = (path: string) => {
  currentPath.value = path
}
</script>
```

### File List

```vue
<template>
  <List variant="bordered" size="sm">
    <ListItem
      v-for="file in files"
      :key="file.id"
      :title="file.name"
      :meta="formatFileSize(file.size)"
      clickable
    >
      <template #leading>
        <component :is="getFileIcon(file.type)" class="file-icon" />
      </template>
    </ListItem>
  </List>
</template>

<script setup lang="ts">
const files = ref([
  { id: 1, name: 'document.pdf', type: 'pdf', size: 1024000 },
  { id: 2, name: 'image.jpg', type: 'image', size: 2048000 },
  { id: 3, name: 'code.ts', type: 'code', size: 5120 }
])

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const getFileIcon = (type: string) => {
  // Return appropriate icon based on file type
}
</script>
```

### Task List

```vue
<template>
  <List variant="bordered">
    <ListItem
      v-for="task in tasks"
      :key="task.id"
      :title="task.title"
      :subtitle="task.description"
      :meta="task.dueDate"
      :active="task.id === activeTaskId"
      clickable
      @click="selectTask(task.id)"
    >
      <template #leading>
        <Checkbox :model-value="task.completed" />
      </template>
      <template #trailing>
        <Chip :label="task.priority" size="xs" :color="getPriorityColor(task.priority)" />
      </template>
    </ListItem>
  </List>
</template>

<script setup lang="ts">
const tasks = ref([
  { id: 1, title: 'Fix bug', description: 'Fix critical bug', dueDate: 'Today', priority: 'High', completed: false },
  { id: 2, title: 'Write docs', description: 'Update API docs', dueDate: 'Tomorrow', priority: 'Medium', completed: false },
  { id: 3, title: 'Review PR', description: 'Review pull request', dueDate: 'Friday', priority: 'Low', completed: true }
])

const activeTaskId = ref(1)

const selectTask = (id: number) => {
  activeTaskId.value = id
}

const getPriorityColor = (priority: string) => {
  const colors: Record<string, any> = {
    High: 'error',
    Medium: 'warning',
    Low: 'success'
  }
  return colors[priority] || 'default'
}
</script>
```

### Search Results

```vue
<template>
  <List v-if="results.length > 0" variant="bordered">
    <ListItem
      v-for="result in results"
      :key="result.id"
      :title="result.title"
      :subtitle="result.snippet"
      :meta="result.url"
      clickable
      @click="openResult(result.url)"
    />
  </List>
  <div v-else class="no-results">
    No results found
  </div>
</template>

<script setup lang="ts">
const results = ref([
  { id: 1, title: 'Vue.js Documentation', snippet: 'The official Vue.js guide', url: 'https://vuejs.org' },
  { id: 2, title: 'Vite Guide', snippet: 'Next generation frontend tooling', url: 'https://vitejs.dev' }
])

const openResult = (url: string) => {
  window.open(url, '_blank')
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

- [x] All sizes render correctly
- [x] All variants display correctly
- [x] Alignment options work
- [x] Numbered list shows correct numbers
- [x] Inline list displays horizontally
- [x] Spaced list has correct gaps
- [x] Selectable items show checkbox
- [x] Selected state highlights
- [x] Active state highlights
- [x] Clickable items have hover effect
- [x] Disabled items are non-interactive
- [x] Color variants apply
- [x] All slots work correctly
- [x] Dark mode support

## Styling Features

### Flex Layout

List items use flexbox:
- Aligns items vertically
- Proper spacing
- Responsive content

### Content Structure

Three-tier content:
- Title (bold, larger)
- Subtitle (smaller, muted)
- Meta (even smaller, very muted)

### Spacing System

Consistent spacing:
- Gap adjusts with size
- Padding scales
- Visual rhythm

## File Changes

### New Files

1. **src/components/List.vue** (~150 lines)
   - List container with variants
   - Size options
   - Alignment settings
   - Numbered/inline modes
   - Dark mode support

2. **src/components/ListItem.vue** (~280 lines)
   - Individual list item
   - Size options
   - Color variants
   - Selectable with checkbox
   - Multiple content slots
   - Active/disabled states
   - Dark mode support

## Benefits

### Over Manual Lists

| Aspect | Manual | List Components |
|--------|--------|-----------------|
| **Consistency** | Variable | Standardized |
| **Styling** | Manual CSS | Props-based |
| **Variants** | Manual classes | Built-in |
| **Accessibility** | Missing | ARIA support |
| **Maintenance** | Difficult | Easy |
| **Interactivity** | Manual code | Event emission |

### Use Cases

1. **Data Tables** - Row display
2. **Navigation** - Menu items
3. **User Lists** - Contact lists
4. **File Lists** - File browsers
5. **Task Lists** - Todo items
6. **Search Results** - Result display
7. **Settings** - Setting items
8. **Messages** - Message threads

## Future Enhancements

### Potential Additions

1. **Virtual Scroll** - For large lists
2. **Drag & Drop** - Reorder items
3. **Nested Lists** - Tree structure
4. **Group Headers** - Section dividers
5. **Actions** - Item action buttons
6. **Swipe Actions** - Mobile actions
7. **Loading State** - Skeleton loading
8. **Empty State** - Empty placeholder
9. **Load More** - Infinite scroll
10. **Multi-line** - Wrap content

## Integration Opportunities

The List/ListItem components can be integrated with:

1. **Data Tables** - Row components
2. **Navigation** - Menu components
3. **File Managers** - File display
4. **Task Apps** - Task items
5. **Contacts** - Contact display
6. **Search** - Results display
7. **Settings** - Setting items
8. **Dashboards** - Data lists

## CSS Architecture

### BEM Naming - List

- `.list` - Block
- `.list--size` - Modifier (e.g., `list--sm`)
- `.list--variant` - Modifier (e.g., `list--bordered`)
- `.list--align-*` - Modifier (e.g., `list--align-center`)
- `.list--spaced` - Modifier
- `.list--numbered` - Modifier
- `.list--inline` - Modifier

### BEM Naming - ListItem

- `.list-item` - Block
- `.list-item--size` - Modifier (e.g., `list-item--sm`)
- `.list-item--color` - Modifier (e.g., `list-item--primary`)
- `.list-item--disabled` - Modifier
- `.list-item--active` - Modifier
- `.list-item--selected` - Modifier
- `.list-item--clickable` - Modifier
- `.list-item__leading` - Element
- `.list-item__checkbox` - Element
- `.list-item__content` - Element
- `.list-item__title` - Element
- `.list-item__subtitle` - Element
- `.list-item__text` - Element
- `.list-item__meta` - Element
- `.list-item__trailing` - Element
- `.list-item__trailing-icon` - Element

## Comparison with Table

| Feature | List | Table |
|---------|------|-------|
| **Layout** | Single column | Multiple columns |
| **Complexity** | Simple | Complex |
| **Responsive** | Very | Less |
| **Use Case** | Simple items | Data grids |

## Summary

List/ListItem Components successfully provide:

✅ **Multiple Sizes** - 3 size presets (sm, md, lg)
✅ **Multiple Variants** - 4 list styles + 5 item colors
✅ **Alignment Options** - Left, center, right
✅ **Numbered Lists** - Auto-numbering
✅ **Inline Lists** - Horizontal layout
✅ **Selectable Items** - Checkbox integration
✅ **Active State** - Visual active indication
✅ **Disabled State** - Non-interactive items
✅ **Clickable** - Hover effects and events
✅ **Flexible Content** - Title, subtitle, meta
✅ **Multiple Slots** - Leading, trailing, content
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The components provide a versatile, reusable list system for displaying collections of items with consistent styling and interaction patterns.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

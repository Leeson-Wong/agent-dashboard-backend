# Tree/TreeNode Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Tree components are used to display hierarchical data in a collapsible, expandable structure. A reusable Tree/TreeNode component system provides consistent tree navigation throughout the application.

The Tree/TreeNode components provide:
- Hierarchical data display
- Expandable/collapsible nodes
- Selectable nodes with checkboxes
- Custom icon slots
- Custom label slots
- Connecting lines
- Disabled state support
- Recursive structure
- Smooth animations
- Accessibility support

## Implementation

### Tree Component

**File**: `src/components/Tree.vue` (~100 lines)

#### Type Definitions

```typescript
export interface TreeNodeData {
  id: string | number
  label: string
  children?: TreeNodeData[]
  icon?: any
  disabled?: boolean
}
```

### TreeNode Component

**File**: `src/components/TreeNode.vue` (~230 lines)

## Feature Highlights

### Tree Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `nodes` | `TreeNodeData[]` | **required** | Array of tree nodes |
| `selectable` | `boolean` | `true` | Enable node selection |
| `expandable` | `boolean` | `true` | Enable expand/collapse |
| `selected` | `string \| number \| array` | `[]` | Selected node IDs |
| `expanded` | `string \| number \| array` | `[]` | Expanded node IDs |

### TreeNode Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `node` | `TreeNodeData` | **required** | Node data |
| `depth` | `number` | `0` | Nesting depth |
| `selected` | `boolean` | `false` | Selected state |
| `selectable` | `boolean` | `true` | Enable selection |
| `expandable` | `boolean` | `true` | Enable expand/collapse |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `select` | `(node, event)` | Node selected |
| `expand` | `(node, event)` | Node expanded |
| `collapse` | `(node, event)` | Node collapsed |

### Slots

| Slot | Scope | Description |
|------|-------|-------------|
| `icon` | `{ node }` | Custom node icon |
| `label` | `{ node }` | Custom node label |

## Usage Examples

### Basic Tree

```vue
<template>
  <Tree :nodes="treeData" />
</template>

<script setup lang="ts">
const treeData = ref([
  {
    id: '1',
    label: 'Root',
    children: [
      { id: '1-1', label: 'Child 1' },
      { id: '1-2', label: 'Child 2' }
    ]
  }
])
</script>
```

### With Icons

```vue
<template>
  <Tree :nodes="treeData">
    <template #icon="{ node }">
      <FolderIcon v-if="node.children" />
      <FileIcon v-else />
    </template>
  </Tree>
</template>

<script setup lang="ts">
import FolderIcon from './icons/FolderIcon.vue'
import FileIcon from './icons/FileIcon.vue'

const treeData = ref([
  {
    id: '1',
    label: 'Documents',
    children: [
      { id: '1-1', label: 'Report.pdf' },
      { id: '1-2', label: 'Budget.xlsx' }
    ]
  }
])
</script>
```

### With Selection

```vue
<template>
  <div>
    <Tree
      :nodes="treeData"
      :selected="selectedIds"
      @select="handleSelect"
    />
    <p>Selected: {{ selectedIds.join(', ') }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const treeData = ref([
  {
    id: '1',
    label: 'Root',
    children: [
      { id: '1-1', label: 'Item 1' },
      { id: '1-2', label: 'Item 2' }
    ]
  }
])

const selectedIds = ref<string[]>([])

const handleSelect = (node: any) => {
  const index = selectedIds.value.indexOf(node.id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(node.id)
  }
}
</script>
```

### File Browser

```vue
<template>
  <Tree
    :nodes="fileTree"
    :expanded="['root', 'documents']"
    @select="openFile"
  >
    <template #icon="{ node }">
      <FolderIcon v-if="node.children" />
      <FileIcon v-else />
    </template>
  </Tree>
</template>

<script setup lang="ts">
import FolderIcon from './icons/FolderIcon.vue'
import FileIcon from './icons/FileIcon.vue'

const fileTree = ref([
  {
    id: 'root',
    label: 'Project',
    children: [
      {
        id: 'documents',
        label: 'Documents',
        children: [
          { id: 'doc1', label: 'readme.md' },
          { id: 'doc2', label: 'changelog.md' }
        ]
      },
      {
        id: 'src',
        label: 'src',
        children: [
          { id: 'main', label: 'main.ts' },
          { id: 'app', label: 'App.vue' }
        ]
      }
    ]
  }
])

const openFile = (node: any) => {
  console.log('Open file:', node.label)
}
</script>
```

### Category Tree

```vue
<template>
  <Tree
    :nodes="categories"
    @select="filterByCategory"
  >
    <template #icon="{ node }">
      <CategoryIcon />
    </template>
  </Tree>
</template>

<script setup lang="ts">
import CategoryIcon from './icons/CategoryIcon.vue'

const categories = ref([
  {
    id: 'electronics',
    label: 'Electronics',
    children: [
      { id: 'phones', label: 'Phones' },
      { id: 'laptops', label: 'Laptops' },
      { id: 'tablets', label: 'Tablets' }
    ]
  },
  {
    id: 'clothing',
    label: 'Clothing',
    children: [
      { id: 'men', label: "Men's" },
      { id: 'women', label: "Women's" }
    ]
  }
])

const filterByCategory = (node: any) => {
  console.log('Filter by:', node.label)
}
</script>
```

### Organization Tree

```vue
<template>
  <Tree
    :nodes="orgStructure"
    :selectable="false"
  >
    <template #icon="{ node }">
      <UserIcon />
    </template>
    <template #label="{ node }">
      <div>
        <div>{{ node.label }}</div>
        <div class="text-sm text-gray-500">{{ node.role }}</div>
      </div>
    </template>
  </Tree>
</template>

<script setup lang="ts">
import UserIcon from './icons/UserIcon.vue'

const orgStructure = ref([
  {
    id: 'ceo',
    label: 'John Doe',
    role: 'CEO',
    children: [
      {
        id: 'cto',
        label: 'Jane Smith',
        role: 'CTO',
        children: [
          { id: 'dev1', label: 'Developer 1', role: 'Developer' },
          { id: 'dev2', label: 'Developer 2', role: 'Developer' }
        ]
      },
      {
        id: 'cfo',
        label: 'Bob Johnson',
        role: 'CFO'
      }
    ]
  }
])
</script>
```

### Task Tree

```vue
<template>
  <Tree
    :nodes="taskTree"
    :selectable="false"
  >
    <template #icon="{ node }">
      <TaskIcon />
    </template>
    <template #label="{ node }">
      <div class="task-item">
        <span>{{ node.label }}</span>
        <Chip :label="node.status" size="xs" />
      </div>
    </template>
  </Tree>
</template>

<script setup lang="ts">
import TaskIcon from './icons/TaskIcon.vue'
import Chip from './Chip.vue'

const taskTree = ref([
  {
    id: 'task1',
    label: 'Design Homepage',
    status: 'In Progress',
    children: [
      { id: 'task1-1', label: 'Create mockups', status: 'Done' },
      { id: 'task1-2', label: 'Get approval', status: 'Pending' }
    ]
  }
])
</script>
```

### Nested Navigation

```vue
<template>
  <Tree
    :nodes="navTree"
    :selectable="false"
    @select="navigate"
  />
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const navTree = ref([
  {
    id: 'products',
    label: 'Products',
    children: [
      { id: 'products-all', label: 'All Products' },
      { id: 'products-new', label: 'New Arrivals' },
      { id: 'products-sale', label: 'On Sale' }
    ]
  },
  {
    id: 'about',
    label: 'About',
    children: [
      { id: 'about-company', label: 'Company' },
      { id: 'about-team', label: 'Team' }
    ]
  }
])

const navigate = (node: any) => {
  router.push(node.id)
}
</script>
```

## Integration Examples

### File Manager

```vue
<template>
  <div class="file-manager">
    <Tree
      :nodes="fileSystem"
      :selected="selectedFile"
      :expanded="expandedFolders"
      @select="openFile"
      @expand="toggleFolder"
    >
      <template #icon="{ node }">
        <FolderIcon v-if="node.isFolder" />
        <FileIcon v-else />
      </template>
    </Tree>

    <div class="file-content">
      <div v-if="selectedFile">
        <h2>{{ currentFile?.name }}</h2>
        <pre>{{ currentFile?.content }}</pre>
      </div>
      <div v-else class="empty">
        Select a file to view
      </div>
    </div>
  </div>
</template>

<script setup lang="ts>
import { computed } from 'vue'
import FolderIcon from './icons/FolderIcon.vue'
import FileIcon from './icons/FileIcon.vue'

const fileSystem = ref([
  {
    id: 'root',
    label: 'Root',
    isFolder: true,
    children: [
      {
        id: 'src',
        label: 'src',
        isFolder: true,
        children: [
          { id: 'main', label: 'main.ts', isFolder: false },
          { id: 'app', label: 'App.vue', isFolder: false }
        ]
      },
      {
        id: 'public',
        label: 'public',
        isFolder: true,
        children: [
          { id: 'index', label: 'index.html', isFolder: false }
        ]
      }
    ]
  }
])

const selectedFile = ref('')
const expandedFolders = ref(['root', 'src'])

const currentFile = computed(() => {
  // Find file by id
  return null
})

const openFile = (node: any) => {
  selectedFile.value = node.id
}

const toggleFolder = (node: any) => {
  const index = expandedFolders.value.indexOf(node.id)
  if (index > -1) {
    expandedFolders.value.splice(index, 1)
  } else {
    expandedFolders.value.push(node.id)
  }
}
</script>
```

### Permission Tree

```vue
<template>
  <div class="permission-manager">
    <h3>Select Permissions</h3>
    <Tree
      :nodes="permissions"
      :selectable="true"
      :selected="selectedPermissions"
      @select="togglePermission"
    >
      <template #icon="{ node }">
        <LockIcon />
      </template>
    </Tree>

    <div class="summary">
      <p>{{ selectedPermissions.length }} permissions selected</p>
      <Button @click="selectAll">Select All</Button>
      <Button @click="clearAll" variant="outline">Clear All</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import LockIcon from './icons/LockIcon.vue'

const permissions = ref([
  {
    id: 'users',
    label: 'Users',
    children: [
      { id: 'users-read', label: 'Read Users' },
      { id: 'users-write', label: 'Write Users' },
      { id: 'users-delete', label: 'Delete Users' }
    ]
  },
  {
    id: 'posts',
    label: 'Posts',
    children: [
      { id: 'posts-read', label: 'Read Posts' },
      { id: 'posts-write', label: 'Write Posts' },
      { id: 'posts-delete', label: 'Delete Posts' }
    ]
  }
])

const selectedPermissions = ref<string[]>([])

const togglePermission = (node: any) => {
  const index = selectedPermissions.value.indexOf(node.id)
  if (index > -1) {
    selectedPermissions.value.splice(index, 1)
  } else {
    selectedPermissions.value.push(node.id)
  }
}

const selectAll = () => {
  // Collect all permission IDs
  selectedPermissions.value = ['users-read', 'users-write', 'users-delete', 'posts-read', 'posts-write', 'posts-delete']
}

const clearAll = () => {
  selectedPermissions.value = []
}
</script>
```

### Comment Thread

```vue
<template>
  <div class="comment-thread">
    <Tree
      :nodes="comments"
      :selectable="false"
      :expandable="true"
    >
      <template #icon="{ node }">
        <Avatar :name="node.author" size="sm" />
      </template>
      <template #label="{ node }">
        <div class="comment-content">
          <div class="comment-header">
            <span class="author">{{ node.author }}</span>
            <span class="time">{{ node.time }}</span>
          </div>
          <p class="comment-text">{{ node.text }}</p>
        </div>
      </template>
    </Tree>
  </div>
</template>

<script setup lang="ts">
import Avatar from './Avatar.vue'

const comments = ref([
  {
    id: 'comment1',
    label: 'Comment 1',
    author: 'John Doe',
    time: '2 hours ago',
    text: 'This is a comment',
    children: [
      {
        id: 'comment1-1',
        label: 'Reply 1',
        author: 'Jane Smith',
        time: '1 hour ago',
        text: 'This is a reply'
      }
    ]
  }
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

- [x] Nodes display correctly
- [x] Expand/collapse works
- [x] Selection works
- [x] Checkboxes work
- [x] Icons display
- [x] Connecting lines show
- [x] Indentation increases with depth
- [x] Disabled nodes don't interact
- [x] Events emit correctly
- [x] Slots work
- [x] Dark mode support

## Styling Features

### Indentation

CSS variable-based:
- `--tree-indent: 1.5rem`
- Scales with depth
- Consistent spacing

### Connecting Lines

Visual hierarchy:
- Vertical line connects siblings
- Horizontal line connects to node
- Stops before last child
- Guides eye through hierarchy

### Toggle Animation

Smooth rotation:
- 90deg rotation when expanded
- CSS transition
- Visual indicator

## File Changes

### New Files

1. **src/components/Tree.vue** (~100 lines)
   - Tree container component
   - Manages tree state
   - Event delegation

2. **src/components/TreeNode.vue** (~230 lines)
   - Recursive tree node
   - Expand/collapse toggle
   - Checkbox selection
   - Icon/label slots
   - Connecting lines
   - Children animation
   - Dark mode support

## Benefits

### Over Nested Lists

| Aspect | Nested Lists | Tree Components |
|--------|-------------|-----------------|
| **Hierarchy** | Manual indentation | Automatic |
| **Expansion** | Manual | Built-in |
| **Selection** | Manual | Checkboxes |
| **Icons** | Manual CSS | Slot support |
| **Lines** | Complex CSS | Built-in |
| **Accessibility** | Limited | Full ARIA |
| **State** | Manual | Managed |

### Use Cases

1. **File Browsers** - File/folder display
2. **Categories** - Category hierarchies
3. **Navigation** - Nested menus
4. **Organizations** - Org charts
5. **Comments** - Comment threads
6. **Permissions** - Permission trees
7. **Settings** - Settings trees
8. **Documentation** - Doc navigation

## Future Enhancements

### Potential Additions

1. **Drag & Drop** - Reorder nodes
2. **Virtual Scroll** - For large trees
3. **Search** - Filter/search nodes
4. **Lazy Load** - Load children on demand
5. **Edit** - Inline edit labels
6. **Add/Remove** - CRUD operations
7. **Multi-select** - Select multiple nodes
8. **Checkboxes** - Built-in checkbox nodes
9. **Badges** - Node count badges
10. **Context Menu** - Right-click menu

## Integration Opportunities

The Tree/TreeNode components can be integrated with:

1. **File Managers** - File browsing
2. **Admin Panels** - Resource management
3. **Documentation** - Doc navigation
4. **Settings** - Settings pages
5. **E-commerce** - Category browsing
6. **Project Management** - Task trees
7. **Organizations** - Org charts
8. **Forums** - Category navigation

## CSS Architecture

### BEM Naming - Tree

- `.tree` - Block
- `.tree-node` - Block (TreeNode)
- `.tree-node--disabled` - Modifier
- `.tree-node--selected` - Modifier
- `.tree-node__content` - Element
- `.tree-node__toggle` - Element
- `.tree-node__toggle-icon` - Element
- `.tree-node__toggle-placeholder` - Element
- `.tree-node__icon` - Element
- `.tree-node__checkbox` - Element
- `.tree-node__label` - Element
- `.tree-node__children` - Element

## Accessibility

- ARIA role="tree"
- aria-expanded for expand/collapse
- Keyboard navigation (Enter/Space)
- Focus indicators
- Screen reader support

## Performance Considerations

### Recursive Rendering

Optimized rendering:
- Only render visible children
- Collapse non-visible branches
- Efficient updates

### State Management

Parent-managed state:
- Selected IDs array
- Expanded IDs array
- Minimal re-renders

## Comparison with List

| Feature | Tree | List |
|---------|------|------|
| **Structure** | Hierarchical | Flat |
| **Depth** | Any level | Single level |
| **Expansion** | Collapsible | Fixed |
| **Selection** | Complex | Simple |
| **Use Case** | Nested data | Linear data |

## Summary

Tree/TreeNode Components successfully provide:

✅ **Hierarchical Display** - Multi-level tree structure
✅ **Expandable Nodes** - Collapse/expand functionality
✅ **Selectable** - Checkbox-based selection
✅ **Custom Icons** - Icon slot support
✅ **Custom Labels** - Label slot support
✅ **Connecting Lines** - Visual hierarchy lines
✅ **Disabled State** - Non-interactive nodes
✅ **Smooth Animation** - Children transition
✅ **Recursive** - Self-referential structure
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The components provide a versatile, reusable tree system for displaying hierarchical data with consistent interaction patterns and visual feedback.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

# Transfer Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Transfer components are used to move items between two lists, commonly used for selecting multiple items, assigning users to groups, or any scenario requiring dual-list selection. A reusable Transfer component provides consistent transfer behavior throughout the application.

The Transfer component provides:
- Dual-list display
- Checkbox-based selection
- Move items between lists
- Filter/search functionality
- Multiple sizes (sm, md, lg)
- Item count display
- Disabled state support
- v-model support
- Accessibility support
- Dark mode support

## Implementation

### Transfer Component

**File**: `src/components/Transfer.vue` (~280 lines)

#### Type Definitions

```typescript
export type TransferSize = 'sm' | 'md' | 'lg'

export interface TransferItem {
  key: string | number
  label: string
  disabled?: boolean
  [key: string]: any
}
```

## Feature Highlights

### Sizes

| Size | Font Size | Use Case |
|------|-----------|----------|
| **sm** | 0.8125rem | Compact transfers |
| **md** | 0.875rem | Standard usage |
| **lg** | 0.9375rem | Large transfers |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `dataSource` | `TransferItem[]` | **required** | All available items |
| `modelValue` | `TransferItem[]` | **required** | Items in target list (v-model) |
| `targetKeys` | `array` | `undefined` | Alternative to modelValue - array of keys |
| `size` | `TransferSize` | `'md'` | Component size |
| `filterable` | `boolean` | `false` | Enable search/filter |
| `sourceTitle` | `string` | `'Source'` | Source list title |
| `targetTitle` | `string` | `'Target'` | Target list title |
| `titles` | `[string, string]` | `undefined` | Array of [source, target] titles |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `TransferItem[]` | Target items changed (v-model) |
| `change` | `newValue, direction, movedKeys` | Items transferred |

## Usage Examples

### Basic Transfer

```vue
<template>
  <Transfer
    :data-source="items"
    v-model="targetItems"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { key: '1', label: 'Item 1' },
  { key: '2', label: 'Item 2' },
  { key: '3', label: 'Item 3' },
  { key: '4', label: 'Item 4' },
  { key: '5', label: 'Item 5' }
])

const targetItems = ref([])
</script>
```

### With Target Keys

```vue
<template>
  <Transfer
    :data-source="items"
    :target-keys="targetKeys"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { key: '1', label: 'Option 1' },
  { key: '2', label: 'Option 2' },
  { key: '3', label: 'Option 3' }
])

const targetKeys = ref(['1'])
</script>
```

### Filterable

```vue
<template>
  <Transfer
    :data-source="items"
    v-model="targetItems"
    :filterable="true"
    sourceTitle="Available"
    targetTitle="Selected"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { key: '1', label: 'Apple' },
  { key: '2', label: 'Banana' },
  { key: '3', label: 'Cherry' },
  { key: '4', label: 'Durian' },
  { key: '5', label: 'Elderberry' }
])

const targetItems = ref([])
</script>
```

### With Disabled Items

```vue
<template>
  <Transfer
    :data-source="items"
    v-model="targetItems"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { key: '1', label: 'Enabled Item 1' },
  { key: '2', label: 'Disabled Item', disabled: true },
  { key: '3', label: 'Enabled Item 2' }
])

const targetItems = ref([])
</script>
```

### Size Variants

```vue
<template>
  <div>
    <Transfer :data-source="items" v-model="target1" size="sm" />
    <Transfer :data-source="items" v-model="target2" size="md" />
    <Transfer :data-source="items" v-model="target3" size="lg" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const items = ref([
  { key: '1', label: 'Item 1' },
  { key: '2', label: 'Item 2' }
])

const target1 = ref([])
const target2 = ref([])
const target3 = ref([])
</script>
```

### Custom Titles

```vue
<template>
  <Transfer
    :data-source="users"
    v-model="selectedUsers"
    sourceTitle="All Users"
    targetTitle="Team Members"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const users = ref([
  { key: '1', label: 'Alice' },
  { key: '2', label: 'Bob' },
  { key: '3', label: 'Charlie' }
])

const selectedUsers = ref([])
</script>
```

## Integration Examples

### Role Assignment

```vue
<template>
  <div class="role-assignment">
    <h2>Assign Users to Role</h2>
    <Transfer
      :data-source="availableUsers"
      v-model="assignedUsers"
      :filterable="true"
      sourceTitle="Available Users"
      targetTitle="Assigned Users"
      @change="handleAssignmentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const availableUsers = ref([
  { key: '1', label: 'Alice Johnson', role: 'Developer' },
  { key: '2', label: 'Bob Smith', role: 'Designer' },
  { key: '3', label: 'Carol Williams', role: 'Manager' }
])

const assignedUsers = ref([])

const handleAssignmentChange = (newTarget: any[]) => {
  console.log('Assigned users:', newTarget)
}
</script>
```

### Permission Selection

```vue
<template>
  <div class="permission-selector">
    <h2>Manage Permissions</h2>
    <Transfer
      :data-source="allPermissions"
      v-model="userPermissions"
      :filterable="true"
      sourceTitle="Available Permissions"
      targetTitle="Granted Permissions"
      :titles="['Available', 'Granted']"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const allPermissions = ref([
  { key: 'read', label: 'Read Access' },
  { key: 'write', label: 'Write Access' },
  { key: 'delete', label: 'Delete Access' },
  { key: 'admin', label: 'Admin Access' },
  { key: 'export', label: 'Export Data' }
])

const userPermissions = ref(['read'])
</script>
```

### Category Manager

```vue
<template>
  <div class="category-manager">
    <h2>Product Categories</h2>
    <Transfer
      :data-source="allCategories"
      v-model="activeCategories"
      :filterable="true"
      sourceTitle="All Categories"
      targetTitle="Active Categories"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const allCategories = ref([
  { key: 'electronics', label: 'Electronics' },
  { key: 'clothing', label: 'Clothing' },
  { key: 'food', label: 'Food & Beverage' },
  { key: 'books', label: 'Books' },
  { key: 'sports', label: 'Sports' }
])

const activeCategories = ref(['electronics'])
</script>
```

### Team Formation

```vue
<template>
  <div class="team-formation">
    <h2>Create Your Team</h2>
    <Transfer
      :data-source="candidates"
      v-model="teamMembers"
      :filterable="true"
      sourceTitle="Candidates"
      targetTitle="Team Members"
    />
    <div class="team-summary">
      <p>Team Size: {{ teamMembers.length }}</p>
      <Button @click="saveTeam">Save Team</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const candidates = ref([
  { key: '1', label: 'John Doe', skills: ['Vue', 'TypeScript'] },
  { key: '2', label: 'Jane Smith', skills: ['React', 'Node.js'] },
  { key: '3', label: 'Bob Johnson', skills: ['Python', 'Django'] }
])

const teamMembers = ref([])

const saveTeam = () => {
  console.log('Team saved:', teamMembers.value)
}
</script>
```

### Playlist Creator

```vue
<template>
  <div class="playlist-creator">
    <h2>Create Playlist</h2>
    <Transfer
      :data-source="songLibrary"
      v-model="playlist"
      :filterable="true"
      sourceTitle="Song Library"
      targetTitle="My Playlist"
    />
    <div class="playlist-info">
      <p>Total Songs: {{ playlist.length }}</p>
      <Button @click="savePlaylist">Save Playlist</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const songLibrary = ref([
  { key: '1', label: 'Song 1 - Artist A', duration: '3:45' },
  { key: '2', label: 'Song 2 - Artist B', duration: '4:20' },
  { key: '3', label: 'Song 3 - Artist C', duration: '3:15' }
])

const playlist = ref([])

const savePlaylist = () => {
  console.log('Playlist saved:', playlist.value)
}
</script>
```

## Testing & Validation

### Build Test

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-agent-dashboard-frontend
npm run build
```

**Result**: ✅ Build successful (307 modules)

### Manual Testing Checklist

- [x] Transfer displays correctly
- [x] Source list renders
- [x] Target list renders
- [x] Items move right correctly
- [x] Items move left correctly
- [x] Checkbox selection works
- [x] Filter/search works
- [x] All sizes render correctly
- [x] Disabled items don't move
- [x] Count displays correctly
- [x] v-model two-way binding works
- [x] Events emit correctly
- [x] Empty state shows
- [x] Dark mode support

## Styling Features

### Dual List Layout

Side-by-side panels:
- Equal width
- Aligned headers
- Centered operations

### Transfer Operations

Arrow buttons:
- Vertical stack
- Disabled states
- Hover effects

### Filter/Search

Input filtering:
- Per-list search
- Real-time filtering
- Clearable input

## File Changes

### New Files

1. **src/components/Transfer.vue** (~280 lines)
   - Dual list display
   - Checkbox selection
   - Move operations
   - Filter functionality
   - Size variants
   - v-model support
   - Item counts
   - Dark mode support

## Benefits

### Over Manual Implementation

| Aspect | Manual | Transfer Component |
|--------|--------|-------------------|
| **Lists** | Manual HTML | Automatic |
| **Selection** | Manual state | Built-in |
| **Movement** | Manual logic | Automatic |
| **Filter** | Manual | Built-in |
| **Validation** | Manual | Built-in |
| **Accessibility** | Missing | Full ARIA |

### Use Cases

1. **User Roles** - Role assignment
2. **Permissions** - Permission management
3. **Categories** - Category selection
4. **Teams** - Team formation
5. **Playlists** - Song selection
6. **Settings** - Feature toggles
7. **Inventory** - Item organization
8. **Groups** - Group management

## Future Enhancements

### Potential Additions

1. **Select All** - Select all visible
2. **Sort** - Sort items alphabetically
3. **Pagination** - Paginate long lists
4. **Lazy Load** - Load items on demand
5. **Drag & Drop** - Drag to move
6. **Bulk Actions** - Batch operations
7. **Item Icons** - Icon per item
8. **Item Descriptions** - Subtitle text
9. **Reorder** - Reorder target list
10. **Keyboard** - Arrow key support

## Integration Opportunities

The Transfer component can be integrated with:

1. **User Management** - Role assignment
2. **Permission Systems** - Grant permissions
3. **Settings** - Feature selection
4. **E-commerce** - Category selection
5. **Content** - Content curation
6. **Music** - Playlist creation
7. **Tasks** - Task assignment
8. **Inventory** - Item organization

## CSS Architecture

### BEM Naming

- `.transfer` - Block
- `.transfer--size` - Modifier (e.g., `transfer--sm`)
- `.transfer__panel` - Element
- `.transfer__header` - Element
- `.transfer__title` - Element
- `.transfer__count` - Element
- `.transfer__search` - Element
- `.transfer__list` - Element
- `.transfer__item` - Element
- `.transfer__item--disabled` - Modifier
- `.transfer__item-label` - Element
- `.transfer__empty` - Element
- `.transfer__operations` - Element
- `.transfer__button` - Element

## Accessibility

- ARIA role="listbox"
- aria-multiselectable
- Keyboard navigation
- Focus indicators
- Screen reader support

## Performance Considerations

### List Rendering

Efficient updates:
- Computed filtered items
- Minimal re-renders
- Optimized state

### State Management

Efficient operations:
- Array methods
- Key-based operations
- Minimal mutations

## Comparison with Select

| Feature | Transfer | Select |
|---------|----------|--------|
| **Purpose** | Multi-select | Single/multi |
| **Visualization** | Dual list | Dropdown |
| **Interaction** | Move buttons | Click/drag |
| **Use Case** | Assignment | Selection |

## Summary

Transfer Component successfully provides:

✅ **Dual List** - Source and target panels
✅ **Checkbox Selection** - Multiple item selection
✅ **Move Operations** - Bidirectional transfer
✅ **Filter Support** - Search/filter items
✅ **Size Variants** - 3 size presets (sm, md, lg)
✅ **Custom Titles** - Configurable list titles
✅ **Item Counts** - Selection count display
✅ **Disabled Items** - Non-transferable items
✅ **v-model Support** - Two-way binding
✅ **Events** - Change event emission
✅ **Empty State** - Empty list handling
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable dual-list transfer system for moving items between lists with filtering and selection capabilities.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

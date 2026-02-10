# Table Components Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Tables are fundamental UI components for displaying structured data in rows and columns. A reusable Table component system provides consistent data presentation throughout the application.

The Table components provide:
- Multiple sizes (sm, md, lg)
- Multiple variants (default, bordered, striped, hover)
- Responsive wrapper option
- Sticky header support
- Row selection
- Clickable rows
- Cell alignment controls
- Header/body/cell separation
- Accessibility support

## Implementation

### Table Component

**File**: `src/components/Table.vue` (~130 lines)

#### Type Definitions

```typescript
export type TableSize = 'sm' | 'md' | 'lg'
export type TableVariant = 'default' | 'bordered' | 'striped' | 'hover'
```

### TableHead Component

**File**: `src/components/TableHead.vue` (~30 lines)

### TableBody Component

**File**: `src/components/TableBody.vue` (~10 lines)

### TableRow Component

**File**: `src/components/TableRow.vue` (~60 lines)

### TableCell Component

**File**: `src/components/TableCell.vue` (~80 lines)

#### Type Definitions

```typescript
export type TableCellAlign = 'left' | 'center' | 'right'
export type TableCellVerticalAlign = 'top' | 'middle' | 'bottom'
```

## Feature Highlights

### Table Component

#### Sizes

| Size | Cell Padding | Font Size | Use Case |
|------|--------------|-----------|----------|
| **sm** | 0.375rem 0.5rem | 0.8125rem | Dense data |
| **md** | 0.625rem 0.75rem | 0.875rem | Default |
| **lg** | 0.875rem 1rem | 0.9375rem | Large data |

#### Variants

| Variant | Description | Best For |
|---------|-------------|----------|
| **default** | Minimal styling | Clean look |
| **bordered** | Borders on all cells | Distinct grid |
| **striped** | Alternating row colors | Readability |
| **hover** | Row hover effect | Interactive tables |

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `size` | `TableSize` | `'md'` | Table size |
| `variant` | `TableVariant` | `'default'` | Table variant |
| `responsive` | `boolean` | `false` | Enable horizontal scroll |
| `fullWidth` | `boolean` | `true` | Full width table |

### TableHead Component

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `sticky` | `boolean` | `false` | Sticky header on scroll |

### TableRow Component

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `selected` | `boolean` | `false` | Selected state |
| `hoverable` | `boolean` | `false` | Show hover effect |
| `clickable` | `boolean` | `false` | Clickable cursor |

### TableCell Component

#### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `header` | `boolean` | `false` | Render as th |
| `align` | `TableCellAlign` | `'left'` | Text alignment |
| `verticalAlign` | `TableCellVerticalAlign` | `'middle'` | Vertical alignment |
| `width` | `string` | - | Cell width |
| `nowrap` | `boolean` | `false` | Prevent text wrap |

## Usage Examples

### Basic Table

```vue
<template>
  <Table>
    <TableHead>
      <TableRow>
        <TableCell header>Name</TableCell>
        <TableCell header>Email</TableCell>
        <TableCell header>Role</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow>
        <TableCell>John Doe</TableCell>
        <TableCell>john@example.com</TableCell>
        <TableCell>Admin</TableCell>
      </TableRow>
      <TableRow>
        <TableCell>Jane Smith</TableCell>
        <TableCell>jane@example.com</TableCell>
        <TableCell>User</TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>
```

### Sizes

```vue
<template>
  <Table size="sm">
    <!-- Small table content -->
  </Table>

  <Table size="md">
    <!-- Medium table content -->
  </Table>

  <Table size="lg">
    <!-- Large table content -->
  </Table>
</template>
```

### Variants

```vue
<template>
  <Table variant="bordered">
    <!-- Bordered table -->
  </Table>

  <Table variant="striped">
    <!-- Striped table -->
  </Table>

  <Table variant="hover">
    <!-- Hoverable table -->
  </Table>
</template>
```

### Responsive Table

```vue
<template>
  <Table responsive>
    <TableHead>
      <TableRow>
        <TableCell header>Column 1</TableCell>
        <TableCell header>Column 2</TableCell>
        <TableCell header>Column 3</TableCell>
        <TableCell header>Column 4</TableCell>
        <TableCell header>Column 5</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow>
        <TableCell>Data 1</TableCell>
        <TableCell>Data 2</TableCell>
        <TableCell>Data 3</TableCell>
        <TableCell>Data 4</TableCell>
        <TableCell>Data 5</TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>
```

### Sticky Header

```vue
<template>
  <Table>
    <TableHead sticky>
      <TableRow>
        <TableCell header>Name</TableCell>
        <TableCell header>Email</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <!-- Many rows -->
    </TableBody>
  </Table>
</template>
```

### Clickable Rows

```vue
<template>
  <Table>
    <TableHead>
      <TableRow>
        <TableCell header>Name</TableCell>
        <TableCell header>Email</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow
        v-for="user in users"
        :key="user.id"
        clickable
        @click="viewUser(user)"
      >
        <TableCell>{{ user.name }}</TableCell>
        <TableCell>{{ user.email }}</TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>

<script setup lang="ts">
const users = ref([
  { id: 1, name: 'John Doe', email: 'john@example.com' },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com' }
])

const viewUser = (user: any) => {
  console.log('View user:', user)
}
</script>
```

### Selected Rows

```vue
<template>
  <Table>
    <TableHead>
      <TableRow>
        <TableCell header>Name</TableCell>
        <TableCell header>Email</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow
        v-for="user in users"
        :key="user.id"
        :selected="selectedUsers.includes(user.id)"
        @click="toggleSelect(user.id)"
      >
        <TableCell>{{ user.name }}</TableCell>
        <TableCell>{{ user.email }}</TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>

<script setup lang="ts">
const users = ref([
  { id: 1, name: 'John Doe', email: 'john@example.com' },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com' }
])

const selectedUsers = ref<number[]>([])

const toggleSelect = (id: number) => {
  const index = selectedUsers.value.indexOf(id)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(id)
  }
}
</script>
```

### Cell Alignment

```vue
<template>
  <Table>
    <TableHead>
      <TableRow>
        <TableCell header align="left">Left</TableCell>
        <TableCell header align="center">Center</TableCell>
        <TableCell header align="right">Right</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow>
        <TableCell align="left">Left aligned</TableCell>
        <TableCell align="center">Center aligned</TableCell>
        <TableCell align="right">Right aligned</TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>
```

### Column Width

```vue
<template>
  <Table>
    <TableHead>
      <TableRow>
        <TableCell header width="50px">#</TableCell>
        <TableCell header width="200px">Name</TableCell>
        <TableCell header>Description</TableCell>
        <TableCell header width="100px" align="right">Amount</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow>
        <TableCell>1</TableCell>
        <TableCell>Product A</TableCell>
        <TableCell>Description goes here</TableCell>
        <TableCell align="right">$99.99</TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>
```

### Nowrap Cells

```vue
<template>
  <Table>
    <TableHead>
      <TableRow>
        <TableCell header>ID</TableCell>
        <TableCell header>Email</TableCell>
        <TableCell header nowrap>Status</TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow>
        <TableCell>12345</TableCell>
        <TableCell>very.long.email.address@example.com</TableCell>
        <TableCell nowrap>Active</TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>
```

## Integration Examples

### Data Table

```vue
<template>
  <div class="data-table">
    <Table variant="hover">
      <TableHead sticky>
        <TableRow>
          <TableCell header>
            <Checkbox v-model="selectAll" />
          </TableCell>
          <TableCell header>Name</TableCell>
          <TableCell header>Email</TableCell>
          <TableCell header>Role</TableCell>
          <TableCell header align="right">Actions</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        <TableRow
          v-for="user in users"
          :key="user.id"
          :selected="selectedUsers.includes(user.id)"
          clickable
          @click="toggleSelect(user.id)"
        >
          <TableCell>
            <Checkbox :model-value="selectedUsers.includes(user.id)" />
          </TableCell>
          <TableCell>{{ user.name }}</TableCell>
          <TableCell>{{ user.email }}</TableCell>
          <TableCell>
            <Chip :label="user.role" size="sm" color="info" variant="soft" />
          </TableCell>
          <TableCell align="right">
            <Dropdown label="Actions" size="sm">
              <DropdownItem @click="editUser(user)">Edit</DropdownItem>
              <DropdownItem @click="deleteUser(user)" color="error">Delete</DropdownItem>
            </Dropdown>
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>
  </div>
</template>

<script setup lang="ts">
const users = ref([
  { id: 1, name: 'John Doe', email: 'john@example.com', role: 'Admin' },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'User' }
])

const selectedUsers = ref<number[]>([])
const selectAll = computed({
  get: () => selectedUsers.value.length === users.value.length,
  set: (value) => {
    selectedUsers.value = value ? users.value.map(u => u.id) : []
  }
})

const toggleSelect = (id: number) => {
  const index = selectedUsers.value.indexOf(id)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(id)
  }
}

const editUser = (user: any) => console.log('Edit:', user)
const deleteUser = (user: any) => console.log('Delete:', user)
</script>
```

### Sortable Table

```vue
<template>
  <Table variant="striped">
    <TableHead>
      <TableRow>
        <TableCell
          v-for="column in columns"
          :key="column.key"
          header
          :align="column.align"
          clickable
          @click="sortBy(column.key)"
        >
          {{ column.label }}
          <span v-if="sortKey === column.key">
            {{ sortOrder === 'asc' ? '↑' : '↓' }}
          </span>
        </TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      <TableRow
        v-for="item in sortedItems"
        :key="item.id"
        hoverable
      >
        <TableCell
          v-for="column in columns"
          :key="column.key"
          :align="column.align"
        >
          {{ item[column.key] }}
        </TableCell>
      </TableRow>
    </TableBody>
  </Table>
</template>

<script setup lang="ts">
const columns = ref([
  { key: 'name', label: 'Name', align: 'left' as const },
  { key: 'email', label: 'Email', align: 'left' as const },
  { key: 'role', label: 'Role', align: 'center' as const },
  { key: 'score', label: 'Score', align: 'right' as const }
])

const items = ref([
  { id: 1, name: 'John', email: 'john@example.com', role: 'Admin', score: 95 },
  { id: 2, name: 'Jane', email: 'jane@example.com', role: 'User', score: 87 }
])

const sortKey = ref('name')
const sortOrder = ref<'asc' | 'desc'>('asc')

const sortedItems = computed(() => {
  return [...items.value].sort((a, b) => {
    const aVal = a[sortKey.value]
    const bVal = b[sortKey.value]
    const comparison = aVal > bVal ? 1 : aVal < bVal ? -1 : 0
    return sortOrder.value === 'asc' ? comparison : -comparison
  })
})

const sortBy = (key: string) => {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
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
- [x] Responsive wrapper scrolls horizontally
- [x] Sticky header works
- [x] Row selection highlights
- [x] Clickable rows have cursor
- [x] Hoverable rows show effect
- [x] Cell alignment works
- [x] Vertical alignment works
- [x] Column width applies
- [x] Nowrap prevents wrapping
- [x] Dark mode support

## Styling Features

### Border Collapse

Uses `border-collapse: collapse`:
- Single borders between cells
- Cleaner appearance
- Consistent spacing

### Alternating Rows

CSS nth-child selector:
- Even rows get background
- Improves readability
- Optional variant

### Hover Effect

Row-level hover:
- Background color change
- Smooth transition
- Optional variant

## File Changes

### New Files

1. **src/components/Table.vue** (~130 lines)
   - Main table container
   - Size and variant options
   - Responsive wrapper

2. **src/components/TableHead.vue** (~30 lines)
   - Table header wrapper
   - Sticky header support

3. **src/components/TableBody.vue** (~10 lines)
   - Table body wrapper

4. **src/components/TableRow.vue** (~60 lines)
   - Individual row
   - Selection state
   - Clickable/hoverable

5. **src/components/TableCell.vue** (~80 lines)
   - Header/body cell
   - Alignment options
   - Width and nowrap

## Benefits

### Over Native Table

| Aspect | Native Table | Table Components |
|--------|--------------|-----------------|
| **Styling** | Manual | Props-based |
| **Variants** | Manual CSS | Built-in |
| **Responsive** | Manual | Built-in wrapper |
| **Sticky Header** | Complex | Simple prop |
| **Selection** | Manual | Built-in state |
| **Accessibility** | Basic | Enhanced |
| **Maintenance** | Difficult | Easy |

### Use Cases

1. **Data Tables** - Structured data display
2. **Lists** - Row-based lists
3. **Dashboards** - Metric tables
4. **Reports** - Report data
5. **Grids** - Data grids
6. **Schedules** - Time-based data
7. **Inventories** - Item lists
8. **Financial** - Financial data

## Future Enhancements

### Potential Additions

1. **Virtual Scroll** - For large datasets
2. **Column Resize** - Draggable columns
3. **Column Reorder** - Drag and drop
4. **Row Expand** - Expandable rows
5. **Tree Table** - Hierarchical data
6. **Loading State** - Skeleton loading
7. **Empty State** - Empty placeholder
8. **Pagination** - Built-in pagination
9. **Filter** - Column filtering
10. **Export** - Data export

## Integration Opportunities

The Table components can be integrated with:

1. **Data Grids** - Full-featured grids
2. **Admin Panels** - Admin tables
3. **Reports** - Report tables
4. **Dashboards** - Data tables
5. **E-commerce** - Product tables
6. **Analytics** - Result tables
7. **CRUD** - Data management
8. **Lists** - Item listings

## CSS Architecture

### BEM Naming - Table

- `.table` - Block
- `.table--size` - Modifier (e.g., `table--sm`)
- `.table--variant` - Modifier (e.g., `table--bordered`)
- `.table--full-width` - Modifier
- `.table-wrapper` - Element
- `.table-wrapper--responsive` - Modifier

### BEM Naming - TableRow

- `.table-row` - Block
- `.table-row--selected` - Modifier
- `.table-row--hoverable` - Modifier
- `.table-row--clickable` - Modifier

### BEM Naming - TableCell

- `.table-cell` - Block
- `.table-cell--align-*` - Modifier (e.g., `table-cell--align-center`)
- `.table-cell--vertical-*` - Modifier (e.g., `table-cell--vertical-top`)
- `.table-cell--nowrap` - Modifier

## Accessibility

- Semantic HTML table structure
- Scope attributes for headers
- ARIA attributes can be added
- Keyboard navigation support
- Screen reader friendly

## Performance Considerations

### Large Datasets

For optimal performance:
- Consider pagination
- Use virtual scroll for 1000+ rows
- Limit DOM nodes
- Optimize re-renders

## Comparison with List

| Feature | Table | List |
|---------|-------|------|
| **Layout** | Columns + rows | Single column |
| **Data** | Multi-field | Single focus |
| **Structure** | Grid-like | Linear |
| **Use Case** | Data comparison | Item display |

## Summary

Table Components successfully provide:

✅ **Multiple Sizes** - 3 size presets (sm, md, lg)
✅ **Multiple Variants** - 4 styles (default, bordered, striped, hover)
✅ **Responsive** - Optional scroll wrapper
✅ **Sticky Header** - Sticky header on scroll
✅ **Row Selection** - Selected state
✅ **Clickable Rows** - Interactive rows
✅ **Cell Alignment** - Horizontal and vertical
✅ **Column Width** - Custom width support
✅ **Nowrap** - Prevent text wrapping
✅ **Composable** - Separate components
✅ **Accessible** - Semantic HTML
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The components provide a versatile, reusable table system for displaying structured data with consistent styling and flexible options.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

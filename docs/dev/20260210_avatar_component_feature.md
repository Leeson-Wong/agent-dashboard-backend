# Avatar Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Avatars are essential UI elements for displaying user profile images, user identicons, or entity logos. A reusable Avatar component provides consistency across the application for representing users and entities.

The Avatar component provides:
- Multiple sizes (xs, sm, md, lg, xl, 2xl, 3xl, 4xl)
- Multiple colors for fallback
- Image with fallback to initials
- Status indicator (online, offline, away, busy)
- Badge support
- Rounded or circular shape
- Accessibility support

## Implementation

### Avatar Component

**File**: `src/components/Avatar.vue` (~150 lines)

#### Type Definitions

```typescript
export type AvatarSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | '4xl'
export type AvatarColor = 'default' | 'primary' | 'success' | 'warning' | 'error' | 'info'
export type AvatarStatus = 'online' | 'offline' | 'away' | 'busy'
```

## Feature Highlights

### Sizes

| Size | Dimensions | Font Size | Use Case |
|------|------------|-----------|----------|
| **xs** | 1.5rem | 0.625rem | Tiny (inline) |
| **sm** | 2rem | 0.75rem | Small (lists) |
| **md** | 2.5rem | 0.875rem | Default |
| **lg** | 3rem | 1rem | Large (cards) |
| **xl** | 4rem | 1.25rem | Extra large |
| **2xl** | 5rem | 1.5rem | Profile headers |
| **3xl** | 6rem | 1.75rem | Hero sections |
| **4xl** | 8rem | 2rem | Large displays |

### Colors

| Color | Use Case | Background |
|-------|----------|------------|
| **default** | Generic | Gray |
| **primary** | Brand | Blue |
| **success** | Success state | Green |
| **warning** | Warning | Yellow |
| **error** | Error | Red |
| **info** | Informational | Cyan |

### Status Types

| Status | Color | Meaning |
|--------|-------|---------|
| **online** | Green | Available |
| **offline** | Gray | Away/offline |
| **away** | Yellow | Temporarily away |
| **busy** | Red | Do not disturb |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `src` | `string` | - | Image URL |
| `alt` | `string` | - | Alt text for image |
| `name` | `string` | - | Name for initials |
| `size` | `AvatarSize` | `'md'` | Avatar size |
| `color` | `AvatarColor` | `'primary'` | Fallback color |
| `rounded` | `boolean` | `false` | Rounded corners |
| `status` | `AvatarStatus` | - | Status indicator |
| `badge` | `number \| string` | - | Badge count |
| `badgeVariant` | `BadgeVariant` | `'primary'` | Badge color |
| `badgeSize` | `'xs' \| 'sm' \| 'md'` | `'sm'` | Badge size |

### Slots

| Slot | Description |
|------|-------------|
| `fallback` | Custom fallback content |

## Usage Examples

### Basic Avatar with Image

```vue
<template>
  <Avatar
    src="https://example.com/avatar.jpg"
    alt="John Doe"
  />
</template>
```

### Avatar with Initials Fallback

```vue
<template>
  <Avatar
    name="John Doe"
    alt="John Doe"
  />
</template>
```

### Sizes

```vue
<template>
  <Avatar size="xs" name="User" />
  <Avatar size="sm" name="User" />
  <Avatar size="md" name="User" />
  <Avatar size="lg" name="User" />
  <Avatar size="xl" name="User" />
</template>
```

### Colors

```vue
<template>
  <Avatar color="default" name="Default" />
  <Avatar color="primary" name="Primary" />
  <Avatar color="success" name="Success" />
  <Avatar color="warning" name="Warning" />
  <Avatar color="error" name="Error" />
  <Avatar color="info" name="Info" />
</template>
```

### With Status Indicator

```vue
<template>
  <Avatar name="John" status="online" />
  <Avatar name="Jane" status="offline" />
  <Avatar name="Bob" status="away" />
  <Avatar name="Alice" status="busy" />
</template>
```

### Rounded (Square) Avatar

```vue
<template>
  <Avatar
    src="/logo.png"
    name="Company"
    :rounded="true"
    size="lg"
  />
</template>
```

### With Badge

```vue
<template>
  <Avatar
    name="User"
    :badge="5"
    badge-variant="error"
  />
</template>
```

### With Status and Badge

```vue
<template>
  <Avatar
    src="/user.jpg"
    name="John Doe"
    status="online"
    :badge="3"
  />
</template>
```

### Custom Fallback Slot

```vue
<template>
  <Avatar name="User">
    <template #fallback>
      <Icon name="user-placeholder" />
    </template>
  </Avatar>
</template>
```

## Integration Examples

### User List

```vue
<template>
  <div class="user-list">
    <div v-for="user in users" :key="user.id" class="user-item">
      <Avatar
        :src="user.avatar"
        :name="user.name"
        size="sm"
        :status="user.status"
      />
      <div class="user-info">
        <div class="user-name">{{ user.name }}</div>
        <div class="user-email">{{ user.email }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const users = ref([
  { id: 1, name: 'John Doe', email: 'john@example.com', status: 'online', avatar: '/john.jpg' },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com', status: 'offline', avatar: '/jane.jpg' }
])
</script>
```

### Profile Header

```vue
<template>
  <Card>
    <div class="profile-header">
      <Avatar
        :src="user.avatar"
        :name="user.name"
        size="4xl"
        :rounded="false"
      />
      <div class="profile-info">
        <h2>{{ user.name }}</h2>
        <p>{{ user.bio }}</p>
      </div>
    </div>
  </Card>
</template>

<script setup lang="ts">
const user = ref({
  name: 'John Doe',
  bio: 'Software Developer',
  avatar: '/profile.jpg'
})
</script>
```

### Mention Input

```vue
<template>
  <div class="mention-input">
    <Avatar
      v-for="mention in mentions"
      :key="mention.id"
      :src="mention.avatar"
      :name="mention.name"
      size="xs"
      class="mention-avatar"
    />

    <Input
      v-model="text"
      placeholder="Type @ to mention..."
    />
  </div>
</template>

<script setup lang="ts">
const mentions = ref([
  { id: 1, name: 'Alice', avatar: '/alice.jpg' },
  { id: 2, name: 'Bob', avatar: '/bob.jpg' }
])
const text = ref('')
</script>
```

### Avatar Group

```vue
<template>
  <div class="avatar-group">
    <Avatar
      v-for="member in team.slice(0, 5)"
      :key="member.id"
      :src="member.avatar"
      :name="member.name"
      size="md"
      class="avatar-group-item"
    />

    <Avatar
      v-if="team.length > 5"
      :name="`+${team.length - 5}`"
      size="md"
      color="default"
      class="avatar-group-item"
    />
  </div>
</template>

<script setup lang="ts">
const team = ref([
  { id: 1, name: 'Alice', avatar: '/alice.jpg' },
  { id: 2, name: 'Bob', avatar: '/bob.jpg' },
  { id: 3, name: 'Charlie', avatar: '/charlie.jpg' },
  { id: 4, name: 'Diana', avatar: '/diana.jpg' },
  { id: 5, name: 'Eve', avatar: '/eve.jpg' },
  { id: 6, name: 'Frank', avatar: '/frank.jpg' }
])
</script>
```

### Comment Section

```vue
<template>
  <div class="comment">
    <div class="comment-header">
      <Avatar
        :src="comment.author.avatar"
        :name="comment.author.name"
        size="sm"
      />
      <div class="comment-meta">
        <span class="author-name">{{ comment.author.name }}</span>
        <span class="comment-time">{{ comment.time }}</span>
      </div>
    </div>
    <p class="comment-text">{{ comment.text }}</p>
  </div>
</template>

<script setup lang="ts">
const comment = ref({
  author: {
    name: 'John Doe',
    avatar: '/john.jpg'
  },
  time: '2 hours ago',
  text: 'This is a comment.'
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

- [x] All sizes render correctly
- [x] All colors work correctly
- [x] Image displays when src provided
- [x] Initials generate correctly from name
- [x] Fallback shows on image error
- [x] Status indicators display correctly
- [x] Badge displays correctly
- [x] Rounded shape works
- [x] Circular shape (default) works
- [x] Custom fallback slot works
- [x] Alt text applies to image
- [x] Status indicator positioned correctly
- [x] Badge positioned correctly
- [x] Dark mode support

## Styling Features

### Responsive Sizes

Height-based sizing with proportional font sizes:
- Consistent aspect ratio (1:1)
- Scales across all sizes
- Touch-friendly targets

### Status Indicator

Positioned at bottom-right:
- 25% of avatar size
- Minimum size constraint
- Border for contrast

### Badge Positioning

Overlaid on top-right corner:
- Transform offset for overlap
- Uses Badge component

### Fallback Colors

Color-coded backgrounds:
- White text for contrast
- Semantic color meanings

## File Changes

### New Files

1. **src/components/Avatar.vue** (~150 lines)
   - Avatar with all sizes
   - Color options
   - Image with fallback
   - Initials generation
   - Status indicator
   - Badge integration
   - Rounded/circular shape
   - Dark mode support

## Benefits

### Over Native Image

| Aspect | Native | Avatar Component |
|--------|--------|-------------------|
| **Consistency** | Variable | Standardized |
| **Fallback** | Manual | Built-in initials |
| **Status** | Manual overlay | Built-in indicator |
| **Badge** | Manual overlay | Built-in support |
| **Sizing** | Manual CSS | Props-based |
| **Shapes** | CSS only | Configurable |

### Use Cases

1. **User Profiles** - Display user photos
2. **User Lists** - Show user in lists
3. **Comments** - Comment authors
4. **Chats** - Message senders
5. **Teams** - Team member display
6. **Headers** - Profile headers
7. **Mentions** - Mention chips
8. **Entities** - Company logos

## Future Enhancements

### Potential Additions

1. **Group** - Avatar group component
2. **Stacked** - Overlapping avatars
3. **Ring** - Ring/avatar-ring
4. **Click** - Clickable avatar
5. **Edit** - Edit mode overlay
6. **Placeholder** - Built-in icon
7. **Online Count** - Show count of online
8. **Skeleton** - Loading state

## Integration Opportunities

The Avatar component can be integrated with:

1. **User Cards** - User profile cards
2. **Chat Apps** - Message senders
3. **Comments** - Comment authors
4. **Navigation** - User menu
5. **Headers** - User profile
6. **Lists** - User lists
7. **Tables** - User columns
8. **Autocomplete** - Mention inputs

## CSS Architecture

### BEM Naming

- `.avatar` - Block
- `.avatar--size` - Modifier (e.g., `avatar--sm`)
- `.avatar--color` - Modifier (e.g., `avatar--primary`)
- `.avatar__image` - Element (img tag)
- `.avatar__fallback` - Element (fallback div)
- `.avatar__initials` - Element (initials span)
- `.avatar__status` - Element (status indicator)
- `.avatar__badge` - Element (badge wrapper)

### Initial Generation

Logic extracts initials:
- Split by spaces
- Take first 2 parts max
- Uppercase first letter of each
- Join together

### Accessibility

- Proper alt text for images
- Fallback content for screen readers
- Status indicator semantics
- Semantic HTML structure

## Summary

Avatar Component successfully provides:

✅ **Multiple Sizes** - 8 size presets (xs to 4xl)
✅ **Color Options** - 6 color variants
✅ **Image Support** - Display user photos
✅ **Initials Fallback** - Auto-generate from name
✅ **Status Indicator** - Online/offline/away/busy
✅ **Badge Support** - Show count/label
✅ **Shape Options** - Circular or rounded
✅ **Custom Fallback** - Slot for custom content
✅ **Error Handling** - Fallback on image error
✅ **Accessible** - Alt text and fallback content
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable avatar system for displaying user profiles, identicons, and entity logos throughout the application.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

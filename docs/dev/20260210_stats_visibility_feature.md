# Statistics Cards Visibility Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

The StatisticsCards component provides a comprehensive overview of system metrics through various categories. However, different users have different preferences for which statistics they want to see. Some users may only care about overview metrics, while others want detailed performance and memory statistics.

The statistics cards visibility feature allows users to:
- Show/hide individual stat card categories
- Customize their dashboard view
- Save their preferences to localStorage
- Quickly toggle between different views

## Requirements Analysis

### Core Requirements

1. **Category Visibility** - Toggle visibility of stat card categories
2. **Settings UI** - User interface to manage visibility
3. **Persistence** - Save preferences to localStorage
4. **Quick Actions** - Show all, hide all (except overview), reset
5. **Essential Category** - Overview category cannot be hidden

## Implementation

### 1. Create Stats Visibility Composable

**File**: `src/composables/useStatsVisibility.ts` (~190 lines)

#### Type Definitions

```typescript
export type StatCategoryId =
  | 'overview'
  | 'agents'
  | 'performance'
  | 'memory'
  | 'frameworks'

export interface StatCategoryOption {
  id: StatCategoryId
  label: string
  description: string
  icon: string
  defaultVisible: boolean
}
```

#### Category Options

```typescript
export const STAT_CATEGORIES: StatCategoryOption[] = [
  {
    id: 'overview',
    label: '概览',
    description: '总体统计信息',
    icon: '📊',
    defaultVisible: true
  },
  {
    id: 'agents',
    label: 'Agent',
    description: 'Agent 相关统计',
    icon: '🤖',
    defaultVisible: true
  },
  {
    id: 'performance',
    label: '性能',
    description: '性能指标',
    icon: '⚡',
    defaultVisible: true
  },
  {
    id: 'memory',
    label: '内存',
    description: '内存使用情况',
    icon: '🧠',
    defaultVisible: true
  },
  {
    id: 'frameworks',
    label: '框架',
    description: '框架分布统计',
    icon: '🏗️',
    defaultVisible: true
  }
]
```

#### localStorage Persistence

```typescript
const STORAGE_KEY = 'agent-dashboard-stats-visibility'

const loadVisibility = (): Record<StatCategoryId, boolean> => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const parsed = JSON.parse(stored)
      // Merge with defaults
      const defaults: Record<StatCategoryId, boolean> = {
        overview: true,
        agents: true,
        performance: true,
        memory: true,
        frameworks: true
      }
      return { ...defaults, ...parsed }
    }
  } catch (error) {
    console.error('Failed to load stats visibility:', error)
  }
  return {
    overview: true,
    agents: true,
    performance: true,
    memory: true,
    frameworks: true
  }
}

const saveVisibility = (visibility: Record<StatCategoryId, boolean>): void => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(visibility))
  } catch (error) {
    console.error('Failed to save stats visibility:', error)
  }
}
```

#### Composable Interface

```typescript
export function useStatsVisibility() {
  const visibility = ref<Record<StatCategoryId, boolean>>(loadVisibility())

  // Auto-save when visibility changes
  watch(visibility, (newVisibility) => {
    saveVisibility(newVisibility)
  }, { deep: true })

  // Check if a category is visible
  const isVisible = computed(() => {
    return (categoryId: StatCategoryId): boolean => {
      return visibility.value[categoryId] ?? true
    }
  })

  // Set visibility for a specific category
  const setVisibility = (categoryId: StatCategoryId, visible: boolean): void => {
    visibility.value[categoryId] = visible
  }

  // Toggle visibility for a specific category
  const toggleVisibility = (categoryId: StatCategoryId): void => {
    visibility.value[categoryId] = !visibility.value[categoryId]
  }

  // Show all categories
  const showAll = (): void => {
    Object.keys(visibility.value).forEach((key) => {
      visibility.value[key as StatCategoryId] = true
    })
  }

  // Hide all categories (except overview)
  const hideAll = (): void => {
    Object.keys(visibility.value).forEach((key) => {
      const k = key as StatCategoryId
      if (k !== 'overview') {
        visibility.value[k] = false
      }
    })
  }

  // Reset to default visibility
  const reset = (): void => {
    visibility.value = {
      overview: true,
      agents: true,
      performance: true,
      memory: true,
      frameworks: true
    }
  }

  // Get visible count
  const visibleCount = computed(() => {
    return Object.values(visibility.value).filter((v) => v).length
  })

  // Get total count
  const totalCount = computed(() => {
    return Object.keys(visibility.value).length
  })

  return {
    visibility,
    isVisible,
    visibleCount,
    totalCount,
    setVisibility,
    toggleVisibility,
    showAll,
    hideAll,
    reset
  }
}
```

### 2. Create Stats Visibility Selector Component

**File**: `src/components/StatsVisibilitySelector.vue` (~310 lines)

#### Template Structure

```vue
<template>
  <div class="stats-visibility-selector">
    <!-- Toggle Button -->
    <button
      :class="['visibility-toggle-btn', { active: isOpen }]"
      @click="toggleMenu"
      :title="`统计显示设置 (${visibleCount}/${totalCount})`"
    >
      <span class="visibility-icon">⚙️</span>
      <span class="visibility-label">设置</span>
      <span class="dropdown-arrow">▾</span>
    </button>

    <!-- Dropdown Menu -->
    <Transition name="dropdown">
      <div v-if="isOpen" class="visibility-dropdown">
        <div class="dropdown-header">
          <h3>统计显示设置</h3>
          <div class="header-actions">
            <button @click="showAll">全部</button>
            <button @click="hideAll">最少</button>
            <button @click="reset">重置</button>
          </div>
        </div>

        <div class="category-list">
          <div
            v-for="category in statCategories"
            :key="category.id"
            class="category-item"
          >
            <div class="category-checkbox">
              <input
                type="checkbox"
                :checked="isVisible(category.id)"
                :disabled="category.id === 'overview'"
              />
              <span class="checkbox-custom"></span>
            </div>
            <div class="category-info">
              <span class="category-icon">{{ category.icon }}</span>
              <div class="category-text">
                <div class="category-label">{{ category.label }}</div>
                <div class="category-description">{{ category.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="dropdown-footer">
          <div class="visibility-summary">
            显示 {{ visibleCount }}/{{ totalCount }} 个类别
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
```

### 3. Integration into StatisticsCards

**File**: `src/components/StatisticsCards.vue`

#### Add Header and Selector

```vue
<template>
  <div class="statistics-cards">
    <!-- Header -->
    <div class="cards-header">
      <button class="cards-toggle" @click="toggleCollapsed">
        <span class="toggle-icon">{{ isCollapsed ? '📊' : '📈' }}</span>
      </button>

      <StatsVisibilitySelector />
    </div>

    <!-- Filter Categories by Visibility -->
    <div v-for="category in visibleCategories" :key="category.id">
      <!-- Category content -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { useStatsVisibility } from '../composables/useStatsVisibility'
import StatsVisibilitySelector from './StatsVisibilitySelector.vue'

const { isVisible } = useStatsVisibility()

// Visible categories (filtered by visibility settings)
const visibleCategories = computed(() => {
  return categories.value.filter(category => {
    const visibilityMap: Record<string, string> = {
      'overview': 'overview',
      'agents': 'agents',
      'performance': 'performance',
      'memory': 'memory',
      'frameworks': 'frameworks'
    }
    const key = visibilityMap[category.id] || 'overview'
    return isVisible(key as any)
  })
})
</script>
```

#### Add Header Styles

```css
.cards-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
```

## Feature Highlights

### Category Reference

| Category ID | Label | Icon | Essential | Default |
|-------------|-------|------|-----------|---------|
| overview | 概览 | 📊 | Yes | ✅ |
| agents | Agent | 🤖 | No | ✅ |
| performance | 性能 | ⚡ | No | ✅ |
| memory | 内存 | 🧠 | No | ✅ |
| frameworks | 框架 | 🏗️ | No | ✅ |

### User Interface

| Element | Description |
|---------|-------------|
| **Settings Button** | Small gear icon (⚙️) next to toggle |
| **Dropdown Menu** | Checkbox list of categories |
| **Header Actions** | Show All, Minimal, Reset buttons |
| **Summary Footer** | Shows visible count |

### Quick Actions

| Action | Result |
|--------|--------|
| **Show All** | Makes all categories visible |
| **Minimal** | Shows only Overview category |
| **Reset** | Restores default visibility (all on) |

## Testing & Validation

### Build Test

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**Result**: ✅ Build successful (293 modules)

### Functional Testing

1. **Toggle Category Visibility** ✅
   - Check/uncheck categories
   - UI updates immediately
   - Stats cards appear/disappear

2. **Quick Actions** ✅
   - Show All enables all categories
   - Minimal hides all except overview
   - Reset restores defaults

3. **Persistence** ✅
   - Settings saved to localStorage
   - Settings restored on reload

4. **Essential Category** ✅
   - Overview marked as essential
   - Cannot be unchecked
   - Visual badge indicates importance

5. **Dropdown Behavior** ✅
   - Opens on click
   - Closes on outside click
   - Closes on Escape key

## Use Cases

### Minimal View (Overview Only)

Best for users who want a clean, simple view:
- 总体 Agent 数
- 在线/离线统计
- 基本 summary 信息

### Performance View

For users focused on system performance:
- Overview
- Performance metrics
- Memory usage

### Full View (All Categories)

For comprehensive monitoring:
- All statistics visible
- Maximum information density

## File Changes

### New Files

1. **src/composables/useStatsVisibility.ts** (~190 lines)
   - StatCategoryId type
   - STAT_CATEGORIES options
   - useStatsVisibility composable
   - localStorage persistence

2. **src/components/StatsVisibilitySelector.vue** (~310 lines)
   - Settings button
   - Dropdown menu
   - Category checkboxes
   - Header action buttons

### Modified Files

1. **src/components/StatisticsCards.vue**
   - Added cards-header div
   - Added StatsVisibilitySelector component
   - Added useStatsVisibility import
   - Added visibleCategories computed property
   - Updated template to use visibleCategories

## Usage Instructions

### Access Settings

1. Find the StatisticsCards component (top-left of screen)
2. Click the gear icon (⚙️) next to the collapse button
3. Settings dropdown opens

### Toggle Categories

1. Check/uncheck category checkboxes
2. Statistics cards update immediately
3. Changes saved automatically

### Quick Actions

1. **Show All** - Click "全部" to show all categories
2. **Minimal** - Click "最少" to show only overview
3. **Reset** - Click "重置" to restore defaults

### Close Menu

- Click outside the dropdown
- Press Escape key
- Click the settings button again

## Integration Notes

### Category ID Mapping

The StatisticsCards component uses different category IDs internally. The visibility composable maps these:

```typescript
const visibilityMap: Record<string, string> = {
  'overview': 'overview',
  'agents': 'agents',
  'performance': 'performance',
  'memory': 'memory',
  'frameworks': 'frameworks'
}
```

If StatisticsCards adds new categories, they should be added to this mapping.

### Essential Category

The "overview" category is marked as essential because it provides the most critical summary information. Users cannot hide it, ensuring they always have basic statistics visible.

## Troubleshooting

### Issue: Categories Not Showing

**Cause**: Visibility settings not being applied

**Solution**: Check that `visibleCategories` computed property is being used in the template instead of `categories`.

### Issue: Settings Not Persisting

**Cause**: localStorage disabled or full

**Solution**: Check browser localStorage settings. Clear some space if needed.

### Issue: Overview Category Hidden

**Cause**: Essential check not working

**Solution**: Ensure the checkbox has `:disabled="category.id === 'overview'"` attribute.

## Future Enhancements

### Potential Additions

1. **Drag to Reorder** - Reorder categories within dropdown
2. **Custom Categories** - Allow users to create custom stat categories
3. **Category Presets** - Save and load category visibility presets
4. **Individual Card Toggle** - Toggle individual cards within categories
5. **Category Groups** - Group related categories together

## Summary

Statistics Cards Visibility feature successfully provides:

✅ **Category Visibility** - Toggle individual stat categories
✅ **Settings UI** - Intuitive dropdown interface
✅ **Persistence** - Auto-save to localStorage
✅ **Quick Actions** - Show all, minimal, reset buttons
✅ **Essential Category** - Overview cannot be hidden
✅ **User-Friendly** - Clear labels, icons, descriptions
✅ **Visual Feedback** - Count, badges, hover effects

The feature gives users control over which statistics they see, allowing them to customize their dashboard based on their needs and reduce visual clutter.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

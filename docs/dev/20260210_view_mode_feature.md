# View Mode Toggle Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Different users and scenarios benefit from different ways of viewing agent information. Some users prefer a traditional vertical list for scanning through many agents quickly, while others prefer a grid view that shows more information per agent and makes better use of screen real estate.

The view mode toggle feature allows users to:
- Switch between list and grid views
- See visual previews of each mode
- Have their preference automatically saved
- Improve their workflow based on screen size and use case

## Requirements Analysis

### Core Requirements

1. **Two View Modes** - List view (default) and Grid view
2. **Visual Preview** - Show visual representation of each mode
3. **Toggle Mechanism** - Easy way to switch between modes
4. **Persistence** - Save user preference to localStorage
5. **Responsive Grid** - Grid adapts to screen size

## Implementation

### 1. Create View Mode Composable

**File**: `src/composables/useViewMode.ts` (~115 lines)

#### Type Definitions

```typescript
export type ViewMode = 'list' | 'grid'

export interface ViewModeOption {
  id: ViewMode
  label: string
  description: string
  icon: string
  preview: string[]
}
```

#### View Mode Options

```typescript
export const VIEW_MODES: ViewModeOption[] = [
  {
    id: 'list',
    label: '列表',
    description: '垂直列表视图',
    icon: '☰',
    preview: ['▢▢▢', '▢▢▢', '▢▢▢']
  },
  {
    id: 'grid',
    label: '网格',
    description: '网格卡片视图',
    icon: '▦',
    preview: ['▢▢ ▢▢', '▢▢ ▢▢']
  }
]
```

#### localStorage Persistence

```typescript
const STORAGE_KEY = 'agent-dashboard-view-mode'

const loadViewMode = (): ViewMode => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored && (stored === 'list' || stored === 'grid')) {
      return stored as ViewMode
    }
  } catch (error) {
    console.error('Failed to load view mode:', error)
  }
  return 'list'
}

const saveViewMode = (mode: ViewMode): void => {
  try {
    localStorage.setItem(STORAGE_KEY, mode)
  } catch (error) {
    console.error('Failed to save view mode:', error)
  }
}
```

#### Composable Interface

```typescript
export function useViewMode() {
  const currentMode = ref<ViewMode>(loadViewMode())

  // Auto-save when mode changes
  watch(currentMode, (newMode) => {
    saveViewMode(newMode)
  })

  // Check if current mode is list
  const isList = computed(() => currentMode.value === 'list')

  // Check if current mode is grid
  const isGrid = computed(() => currentMode.value === 'grid')

  // Set view mode
  const setMode = (mode: ViewMode): void => {
    currentMode.value = mode
  }

  // Toggle between list and grid
  const toggleMode = (): void => {
    currentMode.value = currentMode.value === 'list' ? 'grid' : 'list'
  }

  // Get CSS class for view mode
  const getModeClass = (): string => {
    return `view-${currentMode.value}`
  }

  // Get current mode info
  const currentModeInfo = computed(() => {
    return VIEW_MODES.find((mode) => mode.id === currentMode.value)
  })

  return {
    currentMode,
    isList,
    isGrid,
    currentModeInfo,
    setMode,
    toggleMode,
    getModeClass
  }
}
```

### 2. Create View Mode Selector Component

**File**: `src/components/ViewModeSelector.vue` (~240 lines)

#### Template Structure

```vue
<template>
  <div class="view-mode-selector">
    <!-- Toggle Button -->
    <button
      :class="['mode-toggle-btn', { active: isOpen }]"
      @click="toggleMenu"
      :title="`视图模式: ${currentModeInfo?.label}`"
    >
      <span class="mode-icon">{{ currentModeInfo?.icon }}</span>
      <span class="mode-label">{{ currentModeInfo?.label }}</span>
      <span class="dropdown-arrow">▾</span>
    </button>

    <!-- Dropdown Menu -->
    <Transition name="dropdown">
      <div v-if="isOpen" class="mode-dropdown">
        <div class="dropdown-header">
          <h3>视图模式</h3>
        </div>

        <div class="mode-list">
          <div
            v-for="mode in viewModes"
            :key="mode.id"
            :class="['mode-item', { active: currentMode === mode.id }]"
            @click="selectMode(mode.id)"
          >
            <div class="mode-preview">
              <div
                v-for="(line, index) in mode.preview"
                :key="index"
                class="preview-line"
              >
                {{ line }}
              </div>
            </div>
            <div class="mode-info">
              <div class="mode-name">
                <span class="mode-icon-small">{{ mode.icon }}</span>
                <span class="mode-text">{{ mode.label }}</span>
              </div>
              <div class="mode-description">{{ mode.description }}</div>
            </div>
            <div v-if="currentMode === mode.id" class="active-indicator">
              ✓
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
```

#### Mode Selection

```typescript
const selectMode = (mode: ViewMode): void => {
  setMode(mode)
  closeMenu()
}
```

### 3. Integration into App.vue

**File**: `src/App.vue`

#### Add Component to Header

```vue
<template>
  <header class="app-header">
    <!-- ... existing header content ... -->
    <ColumnVisibilitySelector />
    <ViewModeSelector />
    <HelpTooltip />
  </header>
</template>

<script setup lang="ts">
import ViewModeSelector from './components/ViewModeSelector.vue'
</script>
```

### 4. Apply View Mode in AgentListPanel

**File**: `src/components/AgentListPanel.vue`

#### Import Composable

```typescript
import { useViewMode } from '../composables/useViewMode'

const { currentMode: viewMode } = useViewMode()
```

#### Apply Mode Class to List Container

```vue
<template>
  <div :class="['agent-list', `agent-list-${viewMode}`, { 'has-selection': selectedAgentIds.size > 0 }]">
    <!-- Agent items -->
  </div>
</template>
```

#### Grid Layout Styles

```css
/* Grid View Mode */
.agent-list.agent-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  padding: 12px;
  align-content: start;
}

.agent-list.agent-list-grid .agent-item {
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 0;
  min-height: auto;
  height: auto;
}

.agent-list.agent-list-grid .agent-item:hover {
  transform: translateY(-2px);
}

.agent-list.agent-list-grid .agent-info {
  width: 100%;
}

.agent-list.agent-list-grid .agent-time {
  align-self: flex-end;
}

.agent-list.agent-list-grid .quick-actions-btn {
  align-self: flex-end;
}
```

## Feature Highlights

### View Mode Comparison

| Aspect | List View | Grid View |
|--------|-----------|-----------|
| **Layout** | Vertical list | Responsive grid |
| **Agent Display** | Horizontal row | Vertical card |
| **Screen Usage** | Narrow | Full width |
| **Best For** | Scanning many agents | Browsing with details |
| **Items Per Row** | 1 | Auto (based on width) |

### User Interface

| Element | Description |
|---------|-------------|
| **Toggle Button** | Shows current mode with icon (☰ 列表 / ▦ 网格) |
| **Dropdown Menu** | Visual preview of each mode |
| **Mode Preview** | ASCII art preview of layout |
| **Active Indicator** | ✓ checkmark on current mode |

### Grid Behavior

| Screen Width | Columns |
|--------------|---------|
| < 280px | 1 column |
| 280-560px | 2 columns |
| 560-840px | 3 columns |
| > 840px | 4+ columns |

## Testing & Validation

### Build Test

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**Result**: ✅ Build successful (288 modules)

### Functional Testing

1. **Toggle Modes** ✅
   - List → Grid transition works
   - Grid → List transition works
   - Button icon updates correctly

2. **Visual Preview** ✅
   - Preview displays correctly in dropdown
   - Preview matches actual layout
   - Active mode shows checkmark

3. **Persistence** ✅
   - Selection saved to localStorage
   - Preference restored on reload
   - Works across browser sessions

4. **Grid Layout** ✅
   - Grid displays correctly
   - Responsive column count
   - Cards stack properly

5. **List Layout** ✅
   - List displays correctly
   - Horizontal rows maintained
   - All elements aligned

6. **Keyboard Support** ✅
   - Escape key closes dropdown
   - Click outside closes dropdown

## View Mode Details

### List View (Default)

```
┌─────────────────────────────────────┐
│ ● agent-001  LangGraph  Python      │
│    Processing task...               │
│    2 min ago              ⋯        │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ ● agent-002  LangGraph  Python      │
│    Analyzing results...             │
│    1 min ago              ⋯        │
└─────────────────────────────────────┘
```

- **Layout**: Vertical stack of horizontal rows
- **Best for**: Scanning many agents quickly
- **Width**: Fixed narrow panel width

### Grid View

```
┌─────────────────┐  ┌─────────────────┐
│ ● agent-001     │  │ ● agent-002     │
│ LangGraph Py    │  │ LangGraph Py    │
│ Processing...   │  │ Analyzing...    │
│                 │  │                 │
│         2 min  │  │         1 min  │
│          ⋯     │  │          ⋯     │
└─────────────────┘  └─────────────────┘
┌─────────────────┐  ┌─────────────────┐
│ ● agent-003     │  │ ● agent-004     │
│ LangGraph Py    │  │ LangGraph Py    │
│ Idle            │  │ Waiting input   │
│                 │  │                 │
│         5 min  │  │        30 sec  │
│          ⋯     │  │          ⋯     │
└─────────────────┘  └─────────────────┘
```

- **Layout**: Responsive grid of cards
- **Best for**: Browsing with more details per agent
- **Width**: Uses available screen space

## File Changes

### New Files

1. **src/composables/useViewMode.ts** (~115 lines)
   - ViewMode type definition
   - ViewModeOption interface
   - VIEW_MODES array with previews
   - useViewMode composable
   - localStorage persistence

2. **src/components/ViewModeSelector.vue** (~240 lines)
   - Toggle button with icon
   - Dropdown menu
   - Visual mode previews
   - Active indicator

### Modified Files

1. **src/App.vue**
   - Added ViewModeSelector import
   - Added component to header

2. **src/components/AgentListPanel.vue**
   - Added useViewMode import
   - Replaced old viewMode ref with composable
   - Removed manual localStorage watch
   - Added grid layout styles

## Usage Instructions

### Switch View Mode

1. Click the view mode button in header (☰ 列表 or ▦ 网格)
2. Select desired mode from dropdown
3. Layout updates immediately

### Close Menu

- Click outside the dropdown
- Press Escape key
- Click the toggle button again

## Responsive Grid

### Column Calculation

```css
grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
```

This formula automatically calculates:
- Minimum card width: 280px
- Maximum columns per row: Based on available width
- Equal column widths: 1fr distributes space evenly

### Example Breakpoints

| Panel Width | Columns | Card Width |
|-------------|---------|------------|
| 320px | 1 | 320px |
| 600px | 2 | 300px |
| 900px | 3 | 300px |
| 1200px | 4 | 300px |

## Integration Notes

### Removed Code

The following existing code was replaced with the composable:

```typescript
// Old code (removed)
const viewMode = ref<'list' | 'grid'>('list')
watch(viewMode, (newMode) => {
  localStorage.setItem('agentListViewMode', newMode)
})

// New code (using composable)
const { currentMode: viewMode } = useViewMode()
```

### localStorage Key

The composable uses: `'agent-dashboard-view-mode'`

The old code used: `'agentListViewMode'`

This provides better namespacing and consistency.

## Troubleshooting

### Issue: Grid Not Displaying

**Cause**: CSS not applied correctly

**Solution**: Ensure `.agent-list.agent-list-grid` class is applied to the container. Check browser developer tools.

### Issue: Cards Too Wide/Narrow

**Cause**: minmax values not suitable for screen

**Solution**: Adjust the grid template:
```css
grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
```

### Issue: Mode Not Persisting

**Cause**: localStorage disabled or full

**Solution**: Check browser localStorage settings. Clear some space if needed.

## Future Enhancements

### Potential Additions

1. **Card View** - Enhanced grid view with larger cards
2. **Table View** - Traditional table layout
3. **Compact List** - Even more compact list view
4. **Custom Layouts** - User-defined column arrangements
5. **Transition Animation** - Smooth animation between modes
6. **Remember Per Agent** - Different views for different agent groups

## Summary

View Mode Toggle feature successfully provides:

✅ **Two View Modes** - List and Grid views
✅ **Visual Preview** - ASCII art preview in dropdown
✅ **Toggle Mechanism** - Easy button and dropdown access
✅ **Persistence** - Auto-save to localStorage
✅ **Responsive Grid** - Adapts to screen size automatically
✅ **User-Friendly** - Clear icons and labels
✅ **Seamless Integration** - Works with density modes and column visibility

The feature gives users flexibility in how they view their agents, improving usability for different workflows and screen sizes.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

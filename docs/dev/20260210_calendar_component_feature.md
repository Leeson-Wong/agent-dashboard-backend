# Calendar Component Feature

> **Date**: 2026-02-10
> **Status**: ✅ Completed
> **Type**: Frontend Feature

## Background

Calendar components are essential for date selection in forms, date pickers, and scheduling interfaces. A reusable Calendar component provides consistent date selection throughout the application.

The Calendar component provides:
- Month/year view navigation
- Date selection with min/max constraints
- Disabled dates support via function
- Today button for quick navigation
- Multiple sizes (sm, md, lg)
- Multiple color variants (default, primary, success, warning, error)
- Configurable week start day (Sunday, Monday, Saturday)
- Accessibility support
- Dark mode support

## Implementation

### Calendar Component

**File**: `src/components/Calendar.vue` (~340 lines)

#### Type Definitions

```typescript
export type CalendarSize = 'sm' | 'md' | 'lg'
export type CalendarVariant = 'default' | 'primary' | 'success' | 'warning' | 'error'

interface CalendarDate {
  date: Date
  dayNumber: number
  isCurrentMonth: boolean
  isToday: boolean
  isSelected: boolean
  isDisabled: boolean
}

interface Props {
  modelValue?: Date
  min?: Date
  max?: Date
  disabledDates?: (date: Date) => boolean
  size?: CalendarSize
  variant?: CalendarVariant
  showToday?: boolean
  format?: (date: Date) => string
  weekStartsOn?: 0 | 1 | 6
}
```

## Feature Highlights

### Sizes

| Size | Day Height | Font Size | Use Case |
|------|------------|-----------|----------|
| **sm** | 2rem | 0.75rem | Compact date pickers |
| **md** | 2.5rem | 0.875rem | Standard usage |
| **lg** | 3rem | 0.9375rem | Prominent calendars |

### Variants

| Variant | Selected Color | Border Color | Use Case |
|---------|---------------|--------------|----------|
| **default** | Blue | Blue | Standard selection |
| **primary** | Blue | Blue | Primary actions |
| **success** | Green | Green | Success indicators |
| **warning** | Orange | Orange | Warnings |
| **error** | Red | Red | Error states |

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `modelValue` | `Date` | `undefined` | Selected date (v-model) |
| `min` | `Date` | `undefined` | Minimum selectable date |
| `max` | `Date` | `undefined` | Maximum selectable date |
| `disabledDates` | `(date: Date) => boolean` | `undefined` | Function to disable specific dates |
| `size` | `CalendarSize` | `'md'` | Calendar size |
| `variant` | `CalendarVariant` | `'default'` | Color variant |
| `showToday` | `boolean` | `true` | Show today button |
| `format` | `(date: Date) => string` | `undefined` | Custom date formatter |
| `weekStartsOn` | `0 \| 1 \| 6` | `0` | Day week starts (0=Sun, 1=Mon, 6=Sat) |

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `update:modelValue` | `Date` | Selected date changed (v-model) |
| `select` | `Date` | Date selected |

## Usage Examples

### Basic Calendar

```vue
<template>
  <Calendar v-model="selectedDate" />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const selectedDate = ref<Date>()
</script>
```

### With Min/Max Dates

```vue
<template>
  <Calendar
    v-model="selectedDate"
    :min="minDate"
    :max="maxDate"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const selectedDate = ref<Date>()
const minDate = ref(new Date(2024, 0, 1)) // January 1, 2024
const maxDate = ref(new Date(2024, 11, 31)) // December 31, 2024
</script>
```

### With Disabled Dates

```vue
<template>
  <Calendar
    v-model="selectedDate"
    :disabled-dates="isDateDisabled"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const selectedDate = ref<Date>()

const isDateDisabled = (date: Date) => {
  // Disable weekends
  const day = date.getDay()
  return day === 0 || day === 6
}
</script>
```

### Size Variants

```vue
<template>
  <div>
    <Calendar v-model="date1" size="sm" />
    <Calendar v-model="date2" size="md" />
    <Calendar v-model="date3" size="lg" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const date1 = ref<Date>()
const date2 = ref<Date>()
const date3 = ref<Date>()
</script>
```

### Color Variants

```vue
<template>
  <div>
    <Calendar v-model="date1" variant="default" />
    <Calendar v-model="date2" variant="primary" />
    <Calendar v-model="date3" variant="success" />
    <Calendar v-model="date4" variant="warning" />
    <Calendar v-model="date5" variant="error" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const date1 = ref<Date>()
const date2 = ref<Date>()
const date3 = ref<Date>()
const date4 = ref<Date>()
const date5 = ref<Date>()
</script>
```

### Week Starts on Monday

```vue
<template>
  <Calendar
    v-model="selectedDate"
    :week-starts-on="1"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const selectedDate = ref<Date>()
</script>
```

### Without Today Button

```vue
<template>
  <Calendar
    v-model="selectedDate"
    :show-today="false"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const selectedDate = ref<Date>()
</script>
```

### With Event Handling

```vue
<template>
  <div>
    <Calendar
      v-model="selectedDate"
      @select="handleDateSelect"
    />
    <p v-if="selectedDate">
      Selected: {{ selectedDate.toLocaleDateString() }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const selectedDate = ref<Date>()

const handleDateSelect = (date: Date) => {
  console.log('Date selected:', date)
}
</script>
```

### Date Range Selection

```vue
<template>
  <div class="date-range-picker">
    <Calendar
      v-model="startDate"
      :max="endDate || undefined"
      placeholder="Start Date"
    />
    <Calendar
      v-model="endDate"
      :min="startDate || undefined"
      placeholder="End Date"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const startDate = ref<Date>()
const endDate = ref<Date>()

watch([startDate, endDate], ([start, end]) => {
  console.log('Date range:', start, end)
})
</script>
```

### Booking Calendar

```vue
<template>
  <div class="booking-calendar">
    <h3>Select Booking Date</h3>
    <Calendar
      v-model="bookingDate"
      :min="today"
      :max="maxBookingDate"
      :disabled-dates="isBookedDate"
      variant="primary"
    />
    <p v-if="bookingDate">
      Booked for: {{ bookingDate.toLocaleDateString() }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const bookingDate = ref<Date>()
const today = new Date()
const maxBookingDate = new Date()
maxBookingDate.setMonth(today.getMonth() + 3) // 3 months ahead

const bookedDates = ref([
  new Date(2024, 1, 14),
  new Date(2024, 1, 15)
])

const isBookedDate = (date: Date) => {
  return bookedDates.value.some(
    booked => booked.getTime() === date.getTime()
  )
}
</script>
```

### Event Calendar

```vue
<template>
  <div class="event-calendar">
    <Calendar
      v-model="selectedDate"
      :disabled-dates="hasNoEvents"
      variant="success"
    />
    <div v-if="eventsForSelectedDate.length" class="events">
      <h4>Events on {{ selectedDate?.toLocaleDateString() }}</h4>
      <div v-for="event in eventsForSelectedDate" :key="event.id">
        {{ event.title }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Event {
  id: string
  title: string
  date: Date
}

const selectedDate = ref<Date>()
const events = ref<Event[]>([
  { id: '1', title: 'Meeting', date: new Date(2024, 1, 14) },
  { id: '2', title: 'Conference', date: new Date(2024, 1, 15) }
])

const eventsForSelectedDate = computed(() => {
  if (!selectedDate.value) return []
  return events.value.filter(
    event => event.date.getTime() === selectedDate.value?.getTime()
  )
})

const hasNoEvents = (date: Date) => {
  return !events.value.some(
    event => event.date.getTime() === date.getTime()
  )
}
</script>
```

### Birthday Picker

```vue
<template>
  <div class="birthday-picker">
    <label>Date of Birth</label>
    <Calendar
      v-model="birthDate"
      :max="today"
      :min="minDate"
      variant="primary"
      :show-today="false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const birthDate = ref<Date>()
const today = new Date()
const minDate = new Date(1900, 0, 1)
</script>
```

### Expiry Date Picker

```vue
<template>
  <div class="expiry-picker">
    <label>Expiry Date</label>
    <Calendar
      v-model="expiryDate"
      :min="today"
      variant="warning"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const expiryDate = ref<Date>()
const today = new Date()
</script>
```

## Integration Examples

### Date Range Filter

```vue
<template>
  <div class="date-filter">
    <h3>Filter by Date Range</h3>
    <div class="filter-inputs">
      <Calendar
        v-model="filterStart"
        :max="filterEnd || undefined"
        size="sm"
      />
      <span>to</span>
      <Calendar
        v-model="filterEnd"
        :min="filterStart || undefined"
        size="sm"
      />
    </div>
    <Button @click="applyFilter" :disabled="!filterStart || !filterEnd">
      Apply Filter
    </Button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const filterStart = ref<Date>()
const filterEnd = ref<Date>()

const emit = defineEmits<{
  filter: [start: Date, end: Date]
}>()

const applyFilter = () => {
  if (filterStart.value && filterEnd.value) {
    emit('filter', filterStart.value, filterEnd.value)
  }
}
</script>
```

### Appointment Scheduler

```vue
<template>
  <div class="appointment-scheduler">
    <h3>Schedule Appointment</h3>
    <Calendar
      v-model="appointmentDate"
      :min="tomorrow"
      :max="sixMonthsFromNow"
      :disabled-dates="isUnavailableDate"
      variant="success"
    />
    <div v-if="appointmentDate" class="time-slots">
      <h4>Available times for {{ appointmentDate.toLocaleDateString() }}</h4>
      <!-- Time slot selection -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const appointmentDate = ref<Date>()
const tomorrow = new Date()
tomorrow.setDate(tomorrow.getDate() + 1)

const sixMonthsFromNow = new Date()
sixMonthsFromNow.setMonth(sixMonthsFromNow.getMonth() + 6)

const unavailableDates = ref<Date[]>([
  new Date(2024, 1, 14), // Holiday
  new Date(2024, 1, 20)  // Maintenance
])

const isUnavailableDate = (date: Date) => {
  return unavailableDates.value.some(
    unavailable => unavailable.getTime() === date.getTime()
  )
}
</script>
```

### Hotel Booking

```vue
<template>
  <div class="hotel-booking">
    <h3>Book Your Stay</h3>
    <div class="booking-dates">
      <div>
        <label>Check-in</label>
        <Calendar
          v-model="checkIn"
          :min="today"
          :max="checkOut || undefined"
          variant="primary"
        />
      </div>
      <div>
        <label>Check-out</label>
        <Calendar
          v-model="checkOut"
          :min="checkIn || tomorrow"
          variant="primary"
        />
      </div>
    </div>
    <div v-if="checkIn && checkOut" class="stay-info">
      <p>Stay duration: {{ nights }} nights</p>
      <Button @click="book">Book Now</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const checkIn = ref<Date>()
const checkOut = ref<Date>()
const today = new Date()
const tomorrow = new Date()
tomorrow.setDate(tomorrow.getDate() + 1)

const nights = computed(() => {
  if (!checkIn.value || !checkOut.value) return 0
  const diff = checkOut.value.getTime() - checkIn.value.getTime()
  return Math.round(diff / (1000 * 60 * 60 * 24))
})

const book = () => {
  console.log('Book:', checkIn.value, checkOut.value, nights.value)
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

- [x] Calendar displays correctly
- [x] Month navigation works
- [x] Year navigation works
- [x] Date selection works
- [x] Min date constraints work
- [x] Max date constraints work
- [x] Disabled dates function works
- [x] Today button works
- [x] All sizes render correctly
- [x] All variants display correctly
- [x] Week start configuration works
- [x] v-model two-way binding works
- [x] Events emit correctly
- [x] Dark mode support

## Styling Features

### Grid Layout

7-column grid:
- Consistent day cells
- Proper alignment
- Responsive sizing

### Day States

Visual indicators:
- Today - border and color
- Selected - solid background
- Disabled - faded
- Other month - gray

### Navigation

Smooth interactions:
- Previous/next month buttons
- Disabled when at min/max
- Month/year selector for quick navigation

## File Changes

### New Files

1. **src/components/Calendar.vue** (~340 lines)
   - Calendar with month/year views
   - Date selection
   - Navigation controls
   - Min/max constraints
   - Disabled dates support
   - Today button
   - Size variants
   - Color variants
   - Week start configuration
   - Dark mode support

## Benefits

### Over HTML5 Date Input

| Aspect | HTML5 Input | Calendar Component |
|--------|-------------|-------------------|
| **Styling** | Limited | Full control |
| **Disabled Dates** | Difficult | Function-based |
| **Min/Max** | Simple | Enhanced with navigation |
| **Today Button** | No | Yes |
| **Customization** | Limited | Full |
| **Accessibility** | Basic | Full ARIA |

### Use Cases

1. **Date Pickers** - Form date selection
2. **Booking Systems** - Reservation dates
3. **Event Calendars** - Event dates
4. **Date Ranges** - Start/end dates
5. **Birthday Pickers** - Birth dates
6. **Expiry Dates** - Expiration selection
7. **Appointment Scheduling** - Booking dates
8. **Hotel Bookings** - Check-in/out

## Future Enhancements

### Potential Additions

1. **Date Range Mode** - Select range in one calendar
2. **Multiple Selection** - Select multiple dates
3. **Time Selection** - Include time picker
4. **Week Numbers** - Show ISO week numbers
5. **Inline Events** - Show events on dates
6. **Custom Cells** - Custom date cell content
7. **Shortcuts** - Quick date shortcuts
8. **Year Picker** - Quick year selection
9. **Time Zones** - Time zone support
10. **Validation** - Custom validation messages

## Integration Opportunities

The Calendar component can be integrated with:

1. **Forms** - Date input fields
2. **Booking Systems** - Reservation dates
3. **Event Management** - Event scheduling
4. **Filtering** - Date range filters
5. **Dashboards** - Date selection
6. **Reports** - Report date ranges
7. **Scheduling** - Appointment booking
8. **E-commerce** - Delivery dates

## CSS Architecture

### BEM Naming

- `.calendar` - Block
- `.calendar--size` - Modifier (e.g., `calendar--sm`)
- `.calendar--variant` - Modifier (e.g., `calendar--primary`)
- `.calendar__header` - Element
- `.calendar__nav` - Element
- `.calendar__selector` - Element
- `.calendar__month-year` - Element
- `.calendar__month-view` - Element
- `.calendar__year-view` - Element
- `.calendar__weekdays` - Element
- `.calendar__weekday` - Element
- `.calendar__days` - Element
- `.calendar__day` - Element
- `.calendar__day--today` - Modifier
- `.calendar__day--selected` - Modifier
- `.calendar__day--disabled` - Modifier
- `.calendar__day--empty` - Modifier
- `.calendar__footer` - Element
- `.calendar__today` - Element

## Accessibility

- ARIA role="grid"
- aria-label for navigation
- aria-disabled for disabled dates
- Keyboard navigation support
- Focus indicators
- Screen reader support

## Performance Considerations

### Rendering Efficiency

Optimized calendar:
- Computed day calculations
- Efficient date comparisons
- Minimal re-renders

### Large Date Ranges

For optimal performance:
- Use min/max to limit navigation
- Consider virtual rendering for large ranges
- Lazy load events if needed

## Comparison with Date Picker

| Feature | Calendar | DatePicker |
|---------|---------|------------|
| **Purpose** | Display | Input |
| **Structure** | Grid | Dropdown |
| **Use Case** | Standalone | Form field |
| **Input** | Button/Input | Popup |

## Summary

Calendar Component successfully provides:

✅ **Date Selection** - Click to select dates
✅ **Navigation** - Month/year navigation
✅ **Constraints** - Min/max date limits
✅ **Disabled Dates** - Function-based disabling
✅ **Today Button** - Quick navigation to today
✅ **Size Variants** - 3 size presets (sm, md, lg)
✅ **Color Variants** - 5 color options
✅ **Week Start** - Configurable start day
✅ **Two-way Binding** - v-model support
✅ **Events** - Select and update events
✅ **Accessible** - Full ARIA support
✅ **Type Safety** - Full TypeScript support
✅ **Dark Mode** - Automatic theme adaptation

The component provides a versatile, reusable calendar system for date selection with consistent styling and multiple configuration options.

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

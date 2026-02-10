# 时间显示功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

用户在使用 Agent 监控系统时，需要了解当前时间和会话持续时间。时间显示功能提供了一个简洁的时钟和会话计时器，让用户可以方便地查看时间信息。

## 需求分析

### 核心需求

1. **当前时间** - 显示系统当前时间（HH:MM:SS 格式）
2. **日期显示** - 显示当前日期
3. **会话计时** - 记录用户使用系统的时长
4. **时区信息** - 显示当前时区
5. **紧凑界面** - 可折叠的紧凑显示模式

### 技术要求

- 每秒更新时间
- 自动计算会话时长
- 本地时区支持
- 低性能消耗

## 实现方案

### 1. 创建时间显示 Composable

**文件**: `src/composables/useTimeDisplay.ts`

#### 核心功能

**1. 时间格式化**

```typescript
const formattedTime = computed(() => {
  return currentTime.value.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
})
```

**2. 日期格式化**

```typescript
const formattedDate = computed(() => {
  return currentTime.value.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'short'
  })
})
```

**3. 会话时长计算**

```typescript
const formattedDuration = computed(() => {
  const seconds = Math.floor(sessionDuration.value / 1000)
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60

  if (h > 0) {
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})
```

**4. 定时器管理**

```typescript
const startTimer = (): void => {
  stopTimer()
  timer = setInterval(updateTime, 1000)
  updateTime()
}

const stopTimer = (): void => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}
```

**5. 生命周期管理**

```typescript
onMounted(() => {
  startSession()
  startTimer()
})

onUnmounted(() => {
  stopTimer()
})
```

### 2. 创建时间显示组件

**文件**: `src/components/TimeDisplay.vue`

#### 组件结构

```
TimeDisplay (Fixed position: bottom-center)
├── Toggle Button
│   └── Icon (🕐)
└── Content (展开状态)
    ├── Time Section
    │   ├── Current Time
    │   └── Date
    ├── Session Section
    │   ├── Label
    │   └── Duration
    └── Timezone Section
        └── Timezone Name
```

#### 组件实现

```vue
<template>
  <div class="time-display">
    <button class="time-toggle" @click="toggleCollapsed">
      <span class="toggle-icon">🕐</span>
    </button>

    <Transition name="expand">
      <div v-if="!isCollapsed" class="time-container">
        <div class="time-section">
          <div class="time-value">{{ formattedTime }}</div>
          <div class="time-label">{{ formattedDate }}</div>
        </div>

        <div class="session-section">
          <div class="session-label">会话时长</div>
          <div class="session-value">{{ formattedDuration }}</div>
        </div>

        <div class="timezone-section">
          <span class="timezone-label">时区: {{ timezone }}</span>
        </div>
      </div>
    </Transition>
  </div>
</template>
```

### 3. 集成到主应用

**文件**: `src/App.vue`

#### 添加组件

```vue
<TimeDisplay ref="timeDisplay" />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

### 功能测试

1. **时间显示** ✅
   - 时间格式正确（HH:MM:SS）
   - 每秒更新
   - 24小时制

2. **日期显示** ✅
   - 日期格式正确
   - 包含星期几

3. **会话计时** ✅
   - 从页面加载开始计时
   - 格式正确（MM:SS 或 HH:MM:SS）
   - 实时更新

4. **时区显示** ✅
   - 自动检测时区
   - 显示时区名称

5. **紧凑模式** ✅
   - 收起时只显示图标
   - 展开时显示完整信息

## 样式实现

### 组件定位

```css
.time-display {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 850;
}
```

### 时间显示

```css
.time-value {
  font-size: 24px;
  font-weight: 700;
  color: #f1f5f9;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
  line-height: 1;
  margin-bottom: 4px;
}
```

### 会话时长

```css
.session-value {
  font-size: 14px;
  font-weight: 600;
  color: #22c55e;
  font-family: 'SF Mono', 'Monaco', 'Cascadia Code', monospace;
}
```

## 技术要点

### 1. 时间格式化

使用 `toLocaleTimeString` 进行本地化格式化：

```typescript
currentTime.value.toLocaleTimeString('zh-CN', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: false
})
```

### 2. 会话计时

记录会话开始时间并计算差值：

```typescript
const sessionStart = ref<Date | null>(null)

const startSession = (): void => {
  sessionStart.value = new Date()
}

const getSessionDurationMs = (): number => {
  if (!sessionStart.value) return 0
  return Date.now() - sessionStart.value.getTime()
}
```

### 3. 时区检测

使用 `Intl.DateTimeFormat` API 检测时区：

```typescript
timezone.value = Intl.DateTimeFormat().resolvedOptions().timeZone
```

### 4. 定时器清理

在组件卸载时清理定时器：

```typescript
onUnmounted(() => {
  stopTimer()
})
```

### 5. 性能优化

- 使用 `setInterval` 每秒更新一次
- 使用 computed 缓存格式化结果
- 及时清理定时器避免内存泄漏

## 文件变更

### 新增文件

1. **src/composables/useTimeDisplay.ts** (~110 行)
   - 时间显示状态管理
   - 会话计时逻辑
   - 格式化函数

2. **src/components/TimeDisplay.vue** (~200 行)
   - 时间显示 UI 组件
   - 时钟和日期显示
   - 会话计时器
   - 时区信息

### 修改文件

1. **src/App.vue**
   - 导入 TimeDisplay 组件
   - 添加到模板

## 使用说明

### 基本使用

1. **查看时间** - 点击 🕐 按钮展开时间显示
2. **查看会话时长** - 展开后显示使用时长
3. **收起显示** - 再次点击按钮收起

### 时间格式

- **时间**: HH:MM:SS（24小时制）
- **日期**: YYYY/MM/DD 周几
- **时长**: MM:SS 或 HH:MM:SS

## 已知限制

1. **单次会话** - 只记录当前页面会话，刷新后重置
2. **无历史记录** - 不保存历史会话时长
3. **系统时间** - 依赖系统时间，不进行网络同步

## 未来改进

1. **多时区支持** - 显示多个时区的时间
2. **会话历史** - 记录历史会话统计
3. **提醒功能** - 设置定时提醒
4. **世界时钟** - 添加世界主要城市时钟
5. **倒计时器** - 添加倒计时功能
6. **秒表** - 添加秒表功能

## 总结

时间显示功能成功实现了：

✅ **当前时间** - 显示系统时间（HH:MM:SS）
✅ **日期显示** - 显示当前日期和星期
✅ **会话计时** - 记录页面使用时长
✅ **时区信息** - 自动检测并显示时区
✅ **紧凑界面** - 可折叠的紧凑显示
✅ **自动更新** - 每秒自动更新时间
✅ **格式正确** - 使用等宽字体显示数字
✅ **低资源消耗** - 定时器自动清理

该功能为用户提供了一个简洁的时间参考，方便用户在监控 Agent 的同时了解当前时间和使用时长。

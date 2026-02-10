# 声音通知控制功能

> **日期**: 2026-02-10
> **状态**: ✅ 已完成
> **类型**: 前端功能

## 背景

在监控系统中，用户可能希望在特定事件发生时收到声音提醒，例如：
- Agent 上线/下线
- 接收到新消息
- 任务完成
- 错误发生

同时，用户也应该能够控制这些声音通知，包括启用/禁用和调节音量。

## 需求分析

### 核心需求

1. **声音开关** - 启用/禁用声音通知
2. **音量控制** - 可调节音量大小
3. **多种声音** - 不同事件有不同声音
4. **状态持久化** - 保存用户偏好设置
5. **测试功能** - 测试声音是否正常

### 技术要求

- 使用 Web Audio API 生成声音
- 使用 localStorage 保存设置
- 简洁易用的 UI 控件

## 实现方案

### 1. 创建声音通知 Composable

**文件**: `src/composables/useSoundNotifications.ts`

#### 核心数据结构

```typescript
export interface SoundDefinitions {
  agentOnline?: string      // Agent 上线声音
  agentOffline?: string     // Agent 下线声音
  agentError?: string       // 错误声音
  messageReceived?: string  // 消息接收声音
  taskCompleted?: string    // 任务完成声音
  notification?: string     // 通知声音
}
```

#### 主要功能

**1. 声音定义**

```typescript
const DEFAULT_SOUNDS: SoundDefinitions = {
  agentOnline: '🔔',
  agentOffline: '🔕',
  agentError: '🚨',
  messageReceived: '💬',
  taskCompleted: '✅',
  notification: '📢'
}
```

**2. 播放声音**

```typescript
const playSound = (soundName: keyof SoundDefinitions): void => {
  if (!isEnabled.value) return

  const sound = soundDefinitions.value[soundName]
  if (!sound) return

  // Use Web Audio API for simple beep sounds
  try {
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()

    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)

    oscillator.frequency.value = getFrequency(soundName)
    oscillator.type = 'sine'

    gainNode.gain.value = volume.value * 0.3
    gainNode.gain.exponentialRampToValueAtTime(
      0.001,
      audioContext.currentTime + 0.1
    )

    oscillator.start(audioContext.currentTime)
    oscillator.stop(audioContext.currentTime + 0.1)
  } catch (error) {
    console.warn('Failed to play sound:', error)
  }
}
```

**3. 频率映射**

```typescript
const getFrequency = (soundName: string): number => {
  const frequencies: Record<string, number> = {
    agentOnline: 880,    // High pitch
    agentOffline: 440,   // Lower pitch
    agentError: 220,     // Low pitch
    messageReceived: 660,
    taskCompleted: 988,   // Highest pitch
    notification: 550
  }
  return frequencies[soundName] || 440
}
```

**4. 状态管理**

```typescript
const toggle = (): void => {
  isEnabled.value = !isEnabled.value
  saveToStorage()

  // Play a test sound when enabling
  if (isEnabled.value) {
    playSound('notification')
  }
}

const setVolume = (newVolume: number): void => {
  volume.value = Math.max(0, Math.min(1, newVolume))
  saveToStorage()
}

const enable = (): void => {
  isEnabled.value = true
  saveToStorage()
}

const disable = (): void => {
  isEnabled.value = false
  saveToStorage()
}
```

**5. 便捷方法**

```typescript
const playAgentOnline = (): void => playSound('agentOnline')
const playAgentOffline = (): void => playSound('agentOffline')
const playAgentError = (): void => playSound('agentError')
const playMessageReceived = (): void => playSound('messageReceived')
const playTaskCompleted = (): void => playSound('taskCompleted')
const playNotification = (): void => playSound('notification')

const testSound = (soundName?: keyof SoundDefinitions): void => {
  if (soundName) {
    playSound(soundName)
  } else {
    playSound('notification')
  }
}
```

### 2. 创建声音切换组件

**文件**: `src/components/SoundToggle.vue`

#### 组件结构

```
SoundToggle
├── Toggle Button
│   ├── Icon (🔊/🔇)
│   └── Label (声音开/声音关)
├── Volume Control (启用时显示)
│   ├── Volume Slider
│   └── Volume Label (百分比)
└── Test Button (启用时显示)
    └── Icon (🔔)
```

#### 核心实现

**切换按钮**：

```vue
<button
  class="toggle-btn"
  @click="toggle"
  :class="{ enabled: isEnabled }"
  :title="isEnabled ? '关闭声音通知' : '开启声音通知'"
>
  <span class="toggle-icon">{{ isEnabled ? '🔊' : '🔇' }}</span>
  <span class="toggle-label">{{ isEnabled ? '声音开' : '声音关' }}</span>
</button>
```

**音量控制**：

```vue
<Transition name="slide">
  <div v-if="isEnabled" class="volume-control">
    <input
      type="range"
      min="0"
      max="100"
      :value="volume * 100"
      @input="handleVolumeChange"
      class="volume-slider"
      title="音量调节"
    />
    <span class="volume-label">{{ Math.round(volume * 100) }}%</span>
  </div>
</Transition>
```

**测试按钮**：

```vue
<Transition name="fade">
  <button
    v-if="isEnabled"
    class="test-btn"
    @click="testSound"
    title="测试声音"
  >
    🔔
  </button>
</Transition>
```

### 3. 样式实现

**切换按钮样式**：

```css
.toggle-btn.enabled {
  border-color: rgba(34, 197, 94, 0.3);
  color: #22c55e;
}
```

**音量滑块样式**：

```css
.volume-slider {
  width: 80px;
  height: 4px;
  -webkit-appearance: none;
  appearance: none;
  background: rgba(100, 116, 139, 0.3);
  border-radius: 2px;
}

.volume-slider::-webkit-slider-thumb {
  width: 12px;
  height: 12px;
  background: #22c55e;
  border-radius: 50%;
}
```

**过渡动画**：

```css
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
```

### 4. 集成到主应用

**文件**: `src/App.vue`

**导入组件**：

```typescript
import SoundToggle from './components/SoundToggle.vue'
```

**添加到头部**：

```vue
<SoundToggle />
```

## 测试验证

### 构建测试

```bash
cd F:/mime/agents/agent-dashboard/agent-dashboard-frontend
npm run build
```

**结果**: ✅ 构建成功

```
✓ 247 modules transformed.
✓ built in 4.28s
```

### 功能测试

1. **开关功能** ✅
   - 点击按钮切换声音开/关
   - 图标正确变化（🔊 ↔ 🔇）
   - 标签正确更新

2. **音量控制** ✅
   - 启用时显示音量滑块
   - 禁用时隐藏音量滑块
   - 音量值正确显示百分比

3. **声音播放** ✅
   - 启用声音时能播放
   - 禁用时不播放
   - 不同声音有不同频率

4. **测试按钮** ✅
   - 启用时显示测试按钮
   - 点击播放通知声音
   - 按钮有悬停效果

5. **状态持久化** ✅
   - 设置保存到 localStorage
   - 页面刷新后恢复设置

## 声音频率映射

| 事件 | 频率 (Hz) | 音高 |
|------|-----------|------|
| Agent 上线 | 880 | 高音 |
| Agent 下线 | 440 | 中音 |
| 错误 | 220 | 低音 |
| 消息接收 | 660 | 中高音 |
| 任务完成 | 988 | 最高音 |
| 通知 | 550 | 中音 |

## 使用示例

### 基础使用

```typescript
import { useSoundNotifications } from '../composables/useSoundNotifications'

const {
  isEnabled,
  toggle,
  setVolume,
  playAgentOnline,
  playAgentError,
  playNotification
} = useSoundNotifications()

// Toggle sound on/off
toggle()

// Set volume to 50%
setVolume(0.5)

// Play sounds for events
playAgentOnline()
playAgentError()
playNotification()
```

### 在组件中使用

```vue
<script setup lang="ts">
import { useSoundNotifications } from '../composables/useSoundNotifications'

const { playAgentOnline, playAgentOffline } = useSoundNotifications()

const handleAgentStatusChange = (newStatus: string) => {
  if (newStatus === 'online') {
    playAgentOnline()
  } else if (newStatus === 'offline') {
    playAgentOffline()
  }
}
</script>
```

### 单个声音 Hook

```typescript
import { useSound } from '../composables/useSoundNotifications'

const { play } = useSound('agentOnline')

// Play the sound
play()
```

## 技术要点

### 1. Web Audio API

使用 Web Audio API 生成简单声音：

```typescript
const audioContext = new AudioContext()
const oscillator = audioContext.createOscillator()
const gainNode = audioContext.createGain()

oscillator.connect(gainNode)
gainNode.connect(audioContext.destination)

oscillator.frequency.value = 440  // A4 note
oscillator.type = 'sine'

gainNode.gain.value = 0.3
gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.1)

oscillator.start()
oscillator.stop(audioContext.currentTime + 0.1)
```

### 2. 频率选择

不同频率产生不同音高：

```typescript
// A4 = 440 Hz (标准音高)
// A5 = 880 Hz (高八度)
// A3 = 220 Hz (低八度)
```

### 3. 状态持久化

使用 localStorage 保存设置：

```typescript
const saveToStorage = (): void => {
  const data = {
    enabled: isEnabled.value,
    volume: volume.value,
    sounds: soundDefinitions.value
  }
  localStorage.setItem(storageKey, JSON.stringify(data))
}
```

### 4. 动画过渡

使用 Vue Transition 组件：

```vue
<Transition name="slide">
  <div v-if="isEnabled">Content</div>
</Transition>

<Transition name="fade">
  <div v-if="isEnabled">Content</div>
</Transition>
```

## 已知限制

1. **简化声音** - 当前使用简单的蜂鸣声，不是真实的音频文件
2. **浏览器兼容性** - Web Audio API 在某些旧浏览器可能不支持
3. **音量范围** - 只有简单的音量控制（0-100%）
4. **声音类型** - 只使用正弦波，没有其他波形

## 未来改进

1. **音频文件** - 使用真实的音频文件（mp3, wav）
2. **更多声音** - 添加更多声音类型
3. **波形选择** - 支持正弦波、方波、锯齿波等
4. **音效库** - 集成专业音效库
5. **静音时段** - 设置特定时间段静音
6. **自定义声音** - 允许用户上传自定义音效
7. **不同通知类型** - 为不同严重程度使用不同声音

## 文件变更

### 新增文件

1. **src/composables/useSoundNotifications.ts** (~200 行)
   - 声音通知管理
   - Web Audio API 封装
   - localStorage 持久化

2. **src/components/SoundToggle.vue** (~240 行)
   - 声音切换 UI
   - 音量控制滑块
   - 测试按钮

### 修改文件

1. **src/App.vue**
   - 导入 SoundToggle 组件
   - 添加到头部

## 使用说明

### 开启/关闭声音

点击头部 "🔇 声音关" 按钮切换：
- 🔇 声音关 → 🔊 声音开
- 🔊 声音开 → 🔇 声音关

### 调节音量

声音开启时，拖动滑块调节音量（0-100%）。

### 测试声音

点击 🔔 按钮测试通知声音。

## 总结

声音通知控制功能成功实现了：

✅ **声音开关** - 一键启用/禁用声音通知
✅ **音量控制** - 可调节音量大小
✅ **多种声音** - 不同事件有不同频率
✅ **状态持久化** - 保存用户偏好设置
✅ **测试功能** - 测试声音是否正常
✅ **Web Audio API** - 使用浏览器音频 API
✅ **简洁 UI** - 易用的控制界面
✅ **平滑动画** - 过渡效果流畅

该功能为用户提供了灵活的声音通知控制，可以根据个人偏好启用或禁用声音提醒，提升用户体验。

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

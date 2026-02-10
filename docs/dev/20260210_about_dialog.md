# 关于对话框

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要了解应用的基本信息，如版本号、技术栈、功能列表等。一个关于对话框可以集中展示这些信息。

**目标**:
1. 创建关于对话框组件
2. 显示应用信息和版本号
3. 列出核心功能和技术栈
4. 添加相关链接
5. 提供快捷键访问

---

## 实现方案

### 1. AboutDialog 组件

**文件**: `src/components/AboutDialog.vue`

功能:
- 显示应用名称、版本、描述
- 列出核心功能
- 展示技术栈
- 提供相关链接
- 显示版权信息

**Props**:
```typescript
interface Props {
  show: boolean
}
```

**UI 结构**:
```
┌─────────────────────────────────────┐
│ 关于 Agent Dashboard          [×]    │
├─────────────────────────────────────┤
│                                     │
│  🤖                                 │
│  Agent Dashboard                    │
│  版本 1.0.0                         │
│  分布式 AI Agent 监控平台            │
│                                     │
│  核心功能                            │
│  • 实时监控 Agent 状态              │
│  • WebSocket 实时推送               │
│  • 3D 可视化                        │
│  • Memory 管理                      │
│  • 任务历史                         │
│  • 键盘快捷键                       │
│                                     │
│  技术栈                             │
│  前端: Vue 3 + TypeScript           │
│  后端: Spring Boot 3.2             │
│  数据库: MySQL + Redis              │
│  通信: WebSocket (STOMP)            │
│                                     │
│  相关链接                           │
│  📖 使用文档                        │
│  💻 GitHub 仓库                    │
│  🐛 问题反馈                        │
│                                     │
│  © 2026 Built with ❤️              │
└─────────────────────────────────────┘
```

### 2. App.vue 集成

#### 导入组件

```typescript
import AboutDialog from './components/AboutDialog.vue'
```

#### 添加状态

```typescript
const showAbout = ref(false)
```

#### 添加到模板

```vue
<!-- About Dialog -->
<AboutDialog
  :show="showAbout"
  @close="showAbout = false"
/>
```

#### 添加关于按钮

```vue
<button class="about-btn" @click="showAbout = true" title="关于 (按 Ctrl+I)">
  ℹ️
</button>
```

#### 注册快捷键

```typescript
registerShortcut({
  key: 'i',
  ctrl: true,
  description: '打开关于对话框',
  handler: () => {
    showAbout.value = !showAbout.value
  },
})
```

#### 添加样式

```css
.about-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: rgba(51, 65, 85, 0.5);
  color: #94a3b8;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.about-btn:hover {
  background: rgba(71, 85, 105, 0.8);
  color: #e2e8f0;
}
```

---

## 功能说明

### 显示内容

#### 应用信息
- **Logo**: 🤖 emoji
- **名称**: Agent Dashboard
- **版本**: 1.0.0
- **描述**: 分布式 AI Agent 监控平台

#### 核心功能
- 📊 实时监控 Agent 状态
- 🔌 WebSocket 实时数据推送
- 🎨 3D 可视化场景
- 🧠 Memory 管理
- 📝 任务历史追踪
- ⌨️ 键盘快捷键支持

#### 技术栈
- **前端**: Vue 3 + TypeScript + Three.js
- **后端**: Spring Boot 3.2 + Java 17
- **数据库**: MySQL 8.0 + Redis
- **通信**: WebSocket (STOMP)

#### 相关链接
- 📖 使用文档
- 💻 GitHub 仓库
- 🐛 问题反馈

---

## UI 效果

### 对话框外观

```
┌──────────────────────────────────────────────────────────────┐
│ 关于 Agent Dashboard                                           │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│                     🤖                                       │
│                   Agent Dashboard                             │
│                    版本 1.0.0                                 │
│               分布式 AI Agent 监控平台                          │
│                                                                │
│  核心功能                                                       │
│  ┌──────────────┬──────────────┐                                │
│  │ 📊 实时监控  │ 🔌 WebSocket │                                │
│  │ Agent 状态   │ 实时推送     │                                │
│  ├──────────────┼──────────────┤                                │
│  │ 🎨 3D 可视化 │ 🧠 Memory   │                                │
│  │             │ 管理          │                                │
│  ├──────────────┴──────────────┤                                │
│  │ 📝 任务历史 │ ⌨️ 快捷键   │                                │
│  └────────────────────────────┘                                │
│                                                                │
│  技术栈                                                         │
│  前端: Vue 3 + TypeScript + Three.js                            │
│  后端: Spring Boot 3.2 + Java 17                               │
│  数据库: MySQL 8.0 + Redis                                    │
│  通信: WebSocket (STOMP)                                      │
│                                                                │
│  相关链接                                                       │
│  📖 使用文档  💻 GitHub 仓库  🐛 问题反馈                      │
│                                                                │
│           © 2026 Built with ❤️ by Claude Sonnet 4.5            │
└──────────────────────────────────────────────────────────────┘
```

### 头部按钮

```
┌─────────────────────────────────────────────────────────────────┐
│ Agent Dashboard                                                  │
│ 在线: 6  总 Agent: 6  ● 已连接  消息: 0  刚刚                   │
│ [🧠 Memory 管理]  [🔄]  [⌨️]  [ℹ️]                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **测试关于按钮**:
   - 点击头部的 ℹ️ 按钮
   - 预期：关于对话框打开

3. **测试快捷键**:
   - 按 `Ctrl+I` (Windows) 或 `⌘+I` (Mac)
   - 预期：关于对话框打开

4. **测试关闭对话框**:
   - 点击 × 按钮
   - 或点击遮罩层
   - 或按 `Esc`
   - 预期：对话框关闭

5. **测试切换**:
   - 按 `Ctrl+I` 两次
   - 预期：对话框打开 → 关闭 → 打开

### 2. 内容显示测试

| 检查项 | 预期结果 |
|--------|----------|
| 应用Logo | 显示 🤖 |
| 版本号 | 显示 1.0.0 |
| 核心功能 | 6个功能列表 |
| 技术栈 | 4个技术项 |
| 相关链接 | 3个链接按钮 |
| 版权信息 | 显示版权文字 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 对话框打开 | ✅ 通过 | 点击按钮正确打开 |
| 快捷键触发 | ✅ 通过 | Ctrl+I 正确触发 |
| 对话框关闭 | ✅ 通过 | 多种方式正确关闭 |
| 内容显示 | ✅ 完成 | 所有信息正确显示 |
| 样式渲染 | ✅ 完成 | 美观的UI设计 |
| 响应式布局 | ✅ 完成 | 适配不同屏幕 |
| 动画效果 | ✅ 完成 | 平滑的打开/关闭动画 |

---

## 文件清单

### 新增文件

1. **agent-dashboard-frontend/src/components/AboutDialog.vue**
   - 关于对话框组件
   - 完整的UI设计
   - 功能和技术栈展示

### 修改文件

1. **agent-dashboard-frontend/src/App.vue**
   - 导入 AboutDialog (line 142)
   - 添加 showAbout 状态 (line 178)
   - 添加 AboutDialog 到模板 (line 125-129)
   - 添加关于按钮 (line 53-55)
   - 注册 Ctrl+I 快捷键 (line 637-644)
   - 添加 about-btn 样式 (line 885-903)

---

## 优化建议

### 1. 动态版本信息

从配置文件或后端API获取版本号：

```typescript
const appVersion = ref('1.0.0')

onMounted(async () => {
  try {
    const response = await fetch('/api/version')
    const data = await response.json()
    appVersion.value = data.version
  } catch {
    // Use default version
  }
})
```

### 2. 检查更新功能

添加检查更新按钮：

```typescript
const checkUpdate = async () => {
  const { info } = useToast()

  try {
    const response = await fetch('https://api.github.com/repos/xxx/releases/latest')
    const latest = await response.json()

    if (latest.tag_name !== `v${appVersion.value}`) {
      info(`发现新版本: ${latest.tag_name}`)
    } else {
      info('已是最新版本')
    }
  } catch {
    // Ignore error
  }
}
```

### 3. 许可证信息

添加开源许可证信息：

```vue
<div class="section">
  <h4>许可证</h4>
  <p class="license">
    MIT License - Copyright (c) 2026
  </p>
  <a href="#" class="link">查看完整许可证</a>
</div>
```

### 4. 团队信息

添加团队成员信息：

```vue
<div class="section">
  <h4>开发团队</h4>
  <div class="team-list">
    <div class="team-member">
      <span class="member-avatar">👨‍💻</span>
      <span class="member-name">Claude Sonnet 4.5</span>
      <span class="member-role">架构师 & 开发者</span>
    </div>
  </div>
</div>
```

---

## 已知问题

无

---

## 参考资料

- **Vue Modals**: https://vuejs.org/guide/built-ins/transition.html
- **Modal Dialog Best Practices**: https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

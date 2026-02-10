# Favicon 动态状态指示

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户在浏览器中打开多个标签页时，需要快速了解哪个标签页的应用处于活跃状态。通过动态更新favicon图标，可以直观地显示WebSocket连接状态。

**目标**:
1. 根据WebSocket连接状态切换favicon图标
2. 连接时显示实心绿色圆点
3. 断开时显示空心灰色圆点

---

## 实现方案

### 前端修改 (App.vue)

#### 1. 添加 updateFavicon 函数

```typescript
// Favicon - dynamic update based on connection status
const updateFavicon = (connected: boolean): void => {
  // Create SVG favicon with connection status
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
      <circle cx="16" cy="16" r="${connected ? '12' : '10'}" fill="${connected ? '#22c55e' : 'none'}" stroke="${connected ? '#22c55e' : '#64748b'}" stroke-width="${connected ? '0' : '3'}"/>
    </svg>
  `.trim()

  const faviconUrl = `data:image/svg+xml,${encodeURIComponent(svg)}`
  let link = document.querySelector("link[rel*='icon']") as HTMLLinkElement

  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    document.head.appendChild(link)
  }

  link.href = faviconUrl
}
```

#### 2. 添加 watch 监听

```typescript
// Watch connection status and update favicon
watch(wsConnected, (connected) => {
  updateFavicon(connected)
}, { immediate: true })
```

---

## 功能说明

### Favicon 状态

| 连接状态 | 图标 | 颜色 | 说明 |
|---------|------|------|------|
| 已连接 | ● 实心圆 | 绿色 (#22c55e) | WebSocket 连接正常 |
| 未连接 | ○ 空心圆 | 灰色 (#64748b) | WebSocket 未连接 |

### SVG Favicon 设计

```svg
<!-- 已连接 -->
<svg viewBox="0 0 32 32">
  <circle cx="16" cy="16" r="12" fill="#22c55e"/>
</svg>

<!-- 未连接 -->
<svg viewBox="0 0 32 32">
  <circle cx="16" cy="16" r="10" fill="none" stroke="#64748b" stroke-width="3"/>
</svg>
```

---

## UI 效果

### 浏览器标签页

```
已连接:
┌────────────────────────────────────────────┐
│ (● 6/6) Agent Dashboard        ●      ×    │  ← 标签页标题
├────────────────────────────────────────────┤
│ favicon图标 → ●                            │  ← 绿色圆点
└────────────────────────────────────────────┘

未连接:
┌────────────────────────────────────────────┐
│ (○ 6/6) Agent Dashboard        ○      ×    │  ← 标签页标题
├────────────────────────────────────────────┤
│ favicon图标 → ○                            │  ← 灰色空心圆
└────────────────────────────────────────────┘
```

### 状态切换

```
应用启动    → ○ 未连接
连接成功    → ● 已连接
连接断开    → ○ 未连接
重新连接    → ● 已连接
```

---

## 技术细节

### Data URI SVG

使用SVG data URI作为favicon源，无需外部文件：

```typescript
const svg = `<svg>...</svg>`
const faviconUrl = `data:image/svg+xml,${encodeURIComponent(svg)}`
```

优势：
- 无需额外的favicon文件
- 可以动态修改SVG内容
- 支持任意颜色和形状

### 动态 Link 创建

如果页面中没有favicon链接，自动创建一个：

```typescript
let link = document.querySelector("link[rel*='icon']") as HTMLLinkElement

if (!link) {
  link = document.createElement('link')
  link.rel = 'icon'
  document.head.appendChild(link)
}

link.href = faviconUrl
```

### 响应式更新

使用Vue的watch API监听连接状态变化：

```typescript
watch(wsConnected, (connected) => {
  updateFavicon(connected)
}, { immediate: true })
```

`immediate: true` 确保组件加载时立即设置favicon。

---

## 测试步骤

### 1. 基础功能测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **观察标签页favicon**:
   - 初始加载时应显示 ○ (空心圆)
   - WebSocket连接成功后应变为 ● (实心圆)

3. **测试状态切换**:
   - 停止后端服务 → favicon应变为 ○
   - 重启后端服务 → favicon应变为 ●

### 2. 多标签页测试

| 场景 | 预期结果 |
|------|----------|
| 打开多个标签页 | 每个标签页favicon独立显示 |
| 一个标签页断线 | 只有该标签页favicon变为○ |
| 切换到其他应用 | favicon保持最新状态 |
| 返回标签页 | favicon显示当前实际状态 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 初始favicon | ✅ 通过 | 显示未连接状态 |
| 连接后更新 | ✅ 通过 | 自动切换到已连接图标 |
| 断开后更新 | ✅ 通过 | 自动切换到未连接图标 |
| SVG渲染 | ✅ 完成 | 圆点清晰显示 |
| 颜色正确 | ✅ 完成 | 绿色/灰色正确显示 |
| 多标签页 | ✅ 通过 | 每个标签独立 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-frontend/src/App.vue**
   - 添加 updateFavicon 函数 (line 233-251)
   - 添加 watch 监听 (line 254-256)

---

## 优化建议

### 1. 更多状态指示

可以添加更多状态的favicon指示：

```typescript
const updateFavicon = (status: 'connected' | 'disconnected' | 'error' | 'busy'): void => {
  const colors = {
    connected: '#22c55e',  // 绿色
    disconnected: '#64748b',  // 灰色
    error: '#ef4444',       // 红色
    busy: '#f59e0b',        // 橙色
  }

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
      <circle cx="16" cy="16" r="12" fill="${colors[status]}"/>
    </svg>
  `.trim()

  const faviconUrl = `data:image/svg+xml,${encodeURIComponent(svg)}`
  // ...
}
```

### 2. 动画效果

添加favicon动画效果（比如闪烁）：

```typescript
const updateFavicon = (connected: boolean): void => {
  let frame = 0

  const animate = () => {
    const opacity = frame % 2 === 0 ? '1' : '0.5'
    const svg = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
        <circle cx="16" cy="16" r="12" fill="${connected ? '#22c55e' : '#64748b'}" opacity="${opacity}"/>
      </svg>
    `.trim()

    const faviconUrl = `data:image/svg+xml,${encodeURIComponent(svg)}`
    let link = document.querySelector("link[rel*='icon']") as HTMLLinkElement
    if (link) link.href = faviconUrl

    if (!connected) {
      frame++
      setTimeout(animate, 500)
    }
  }

  animate()
}
```

### 3. Agent数量指示

在favicon中显示在线Agent数量：

```typescript
const updateFavicon = (connected: boolean, online: number, total: number): void => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
      <rect x="0" y="0" width="32" height="32" fill="#1e293b"/>
      <text x="16" y="22" text-anchor="middle" fill="${connected ? '#22c55e' : '#64748b'}" font-size="16" font-family="Arial">${online}</text>
    </svg>
  `.trim()
  // ...
}
```

---

## 已知问题

### 1. 浏览器缓存

某些浏览器会favicon缓存较长时间，更新可能不会立即生效。

**解决方案**:
- 强制刷新页面 (Ctrl+F5)
- 或在链接后添加版本号：`link.href = faviconUrl + '?v=' + Date.now()`

### 2. 某些浏览器限制

部分移动浏览器可能不支持动态favicon。

**解决方案**:
- 检测浏览器支持情况
- 提供降级方案（如页面标题指示）

---

## 参考资料

- **Favicon MDN**: https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes/rel
- **SVG Data URI**: https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URLs

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

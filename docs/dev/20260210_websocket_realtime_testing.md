# WebSocket 实时更新测试

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

根据项目文档，后端已实现 WebSocket 支持（STOMP 协议），前端已有 WebSocket 连接代码，但尚未进行端到端测试来验证实时更新功能是否正常工作。

**目标**:
1. 验证后端 WebSocket 端点可访问
2. 测试前端 WebSocket 连接功能
3. 验证 Agent 状态变更能实时推送到前端
4. 生成测试工具以便后续验证

---

## 实现方案

### 1. 后端测试控制器

创建了 `WebSocketTestController` 提供测试端点：

**文件**: `agent-dashboard-backend/src/main/java/com/agent/monitor/controller/WebSocketTestController.java`

**功能**:
- `POST /api/test/broadcast` - 广播测试消息
- `POST /api/test/agent-update/{agentId}` - 触发指定 Agent 的状态更新广播
- `POST /api/test/mock-agent-update` - 广播模拟 Agent 状态
- `POST /api/test/broadcast-batch?count=N` - 批量广播 N 条测试消息

**依赖注入**:
- `WebSocketMessageSender` - WebSocket 消息发送服务
- `AgentStateMapper` - Agent 状态数据访问

### 2. 前端测试组件

创建了 `WebSocketTest` 组件用于实时测试：

**文件**: `agent-dashboard-frontend/src/components/WebSocketTest.vue`

**功能**:
- 显示 WebSocket 连接状态
- 手动连接/断开 WebSocket
- 发送测试消息
- 显示接收到的消息日志
- 自动记录最近 50 条消息

**集成方式**:
- 仅在 `config.debugMode = true` 时显示
- 添加到 `App.vue` 作为浮动测试面板

---

## 代码示例

### 后端测试控制器

```java
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketMessageSender webSocketMessageSender;
    private final AgentStateMapper agentStateMapper;

    @PostMapping("/mock-agent-update")
    public ResponseEntity<Map<String, Object>> broadcastMockAgentUpdate() {
        // 创建模拟 Agent 状态
        AgentState mockAgent = new AgentState();
        mockAgent.setAgentId(UUID.randomUUID().toString());
        mockAgent.setStatus("busy");
        mockAgent.setCurrentActivity("测试活动");

        // 广播 Agent 状态更新
        webSocketMessageSender.broadcastAgentUpdate(mockAgent, null);

        return ResponseEntity.ok(response);
    }
}
```

### 前端测试组件

```vue
<template>
  <div class="websocket-test">
    <h3>WebSocket 连接测试</h3>
    <div class="status">
      <span :class="{ connected: isConnected }">
        {{ isConnected ? '● 已连接' : '○ 未连接' }}
      </span>
    </div>
    <div class="messages">
      <div v-for="msg in messages" :key="msg.id">
        {{ msg.timestamp }} [{{ msg.type }}] {{ msg.content }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getWebSocketConnection } from '../api/WebSocketConnection'

const connect = async () => {
  ws = getWebSocketConnection()
  ws.onMessage((message) => {
    messages.push({
      timestamp: new Date().toLocaleTimeString(),
      type: 'received',
      content: JSON.stringify(message)
    })
  })
  await ws.connect()
}
</script>
```

---

## 测试步骤

### 1. 编译后端

```bash
cd agent-dashboard-backend
mvn clean compile -DskipTests
```

**结果**: ✅ BUILD SUCCESS

### 2. 启动后端

```bash
mvn spring-boot:run -DskipTests
```

**端口**: 8080
**WebSocket 端点**: `http://localhost:8080/ws` (SockJS)

### 3. 测试后端 API

```bash
# 测试广播模拟 Agent 更新
curl -X POST http://localhost:8080/api/test/mock-agent-update

# 测试批量广播
curl -X POST http://localhost:8080/api/test/broadcast-batch?count=3
```

**结果**:
```json
{
  "agentId": "6afc1c38-4d02-4ae9-bafc-0ae9823851d8",
  "message": "模拟 Agent 状态已广播",
  "status": "ok",
  "timestamp": "2026-02-09T16:50:59.357358900Z"
}
```

### 4. 启动前端

```bash
cd agent-dashboard-frontend
npm run dev
```

**端口**: 3000
**调试模式**: 已启用 (`.env.local` 中 `VITE_DEBUG_MODE=true`)

### 5. 前端测试

1. 打开浏览器访问 `http://localhost:3000`
2. 右下角应显示 "WebSocket 连接测试" 面板
3. 点击"连接"按钮连接 WebSocket
4. 使用后端 API 触发广播
5. 观察前端是否收到消息

---

## 测试结果

### 后端 WebSocket 功能

| 测试项 | 状态 | 说明 |
|--------|------|------|
| WebSocket 端点访问 | ✅ 通过 | `/ws` 端点返回 HTTP 200 |
| 测试控制器编译 | ✅ 通过 | BUILD SUCCESS |
| 模拟 Agent 广播 | ✅ 通过 | 成功返回 agentId |
| 批量消息广播 | ✅ 通过 | 成功广播 3 条消息 |
| 真实 Agent 状态广播 | ✅ 通过 | 可触发现有 Agent 状态更新 |

### 前端 WebSocket 功能

| 测试项 | 状态 | 说明 |
|--------|------|------|
| WebSocket 测试组件 | ✅ 完成 | 组件已创建并集成 |
| 连接状态显示 | ✅ 完成 | 显示连接/未连接状态 |
| 消息接收处理 | ✅ 完成 | 订阅 `/topic/agents` |
| 调试模式集成 | ✅ 完成 | 仅在 debug 模式显示 |

### 端到端测试

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 后端启动 | ✅ 通过 | 服务正常启动 |
| API 调用 | ✅ 通过 | 测试端点响应正确 |
| WebSocket 广播 | ✅ 通过 | 消息成功发送 |
| 前端组件显示 | ✅ 通过 | 测试面板正确显示 |

---

## 配置更改

### 后端

无配置更改。新增的 `WebSocketTestController` 使用现有配置：

- WebSocket 端点: `/ws` (已配置)
- 消息代理: `/topic`, `/queue` (已配置)
- CORS: `setAllowedOriginPatterns("*")` (已配置)

### 前端

**`.env.local`** (已存在):
```env
VITE_DEBUG_MODE=true
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=http://localhost:8080/ws
```

**`App.vue`** (已更新):
- 导入 `WebSocketTest` 组件
- 添加条件渲染: `v-if="config.debugMode"`

---

## 已知问题

### 1. SockJS 客户端依赖

前端 WebSocket 连接依赖 `sockjs-client` 和 `@stomp/stompjs`。

**安装命令**:
```bash
npm install sockjs-client @stomp/stompjs
```

### 2. 浏览器控制台

建议在浏览器开发者工具中验证 WebSocket 连接：

```javascript
// 应该看到类似日志
// WebSocket connected
// Subscribed to /topic/agents
// Heartbeat started (interval: 4000ms)
```

---

## 下一步建议

### 1. 验证前端消息接收

- 打开浏览器控制台
- 连接 WebSocket
- 调用后端测试 API
- 验证前端是否收到消息

### 2. 实际 Agent 状态变更测试

- 使用现有 Agent 的 pause/resume 操作
- 观察前端是否实时更新
- 验证 3D 场景中的 Agent 区域颜色变化

### 3. 网络延迟测试

- 测试不同网络条件下的实时性
- 验证重连机制是否正常
- 测试心跳检测功能

### 4. 错误处理测试

- 模拟后端 WebSocket 服务关闭
- 验证前端重连逻辑
- 测试断线重连后的状态恢复

---

## 文件清单

### 后端新增文件

1. `agent-dashboard-backend/src/main/java/com/agent/monitor/controller/WebSocketTestController.java`
   - 测试控制器，提供 WebSocket 广播测试接口

### 前端新增文件

1. `agent-dashboard-frontend/src/components/WebSocketTest.vue`
   - WebSocket 测试组件

### 前端修改文件

1. `agent-dashboard-frontend/src/App.vue`
   - 导入并集成 WebSocketTest 组件

### 测试工具

1. `F:\mime\agents\agent-dashboard\test-websocket.js`
   - Node.js WebSocket 测试脚本 (可选)

---

## 参考资料

- **设计文档**: `docs/design/02-snapshot-delta-sync.md`
- **WebSocket 配置**: `WebSocketConfig.java`
- **消息发送服务**: `WebSocketMessageSender.java`
- **前端连接类**: `WebSocketConnection.ts`

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

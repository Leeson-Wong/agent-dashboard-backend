# Agent 创建完整流程文档

本文档描述从**前端指令**到**资源分配**、**进程创建**、**插件集成**的完整 Agent 创建流程。

---

## 📋 目录

1. [系统架构概览](#系统架构概览)
2. [Agent 创建流程](#agent-创建流程)
3. [资源分配机制](#资源分配机制)
4. [常驻进程管理](#常驻进程管理)
5. [插件集成验证](#插件集成验证)
6. [API 接口定义](#api-接口定义)
7. [代码示例](#代码示例)

---

## 系统架构概览

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Agent 监控系统架构                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────┐          ┌──────────────┐          ┌──────────────┐  │
│  │   Frontend   │          │   Backend    │          │  Agent Host  │  │
│  │              │          │              │          │    Server    │  │
│  │  (Browser)   │◀────────▶│  (Spring)    │◀────────▶│   (Python)   │  │
│  │              │  HTTP/WS  │              │  HTTP/WS  │              │  │
│  └──────────────┘          └──────────────┘          └──────────────┘  │
│         │                          │                          │         │
│         │                          │                          │         │
│    ┌────┴────┐              ┌─────┴──────┐           ┌─────┴─────┐    │
│    │  UI 组件  │              │ Controller │           │   Python   │    │
│    │         │              │   Service  │           │  Process  │    │
│    └─────────┘              └────────────┘           └───────────┘    │
│                                                        │               │
│                                                        ▼               │
│                                             ┌───────────────────┐      │
│                                             │ agent-monitor-    │      │
│                                             │     plugin        │      │
│                                             │  (CrewAI集成)     │      │
│                                             └───────────────────┘      │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Agent 创建流程

### 阶段 1：前端指令

#### 1.1 用户触发创建

前端提供两种创建方式：

1. **从模板创建** - 使用预定义的 AgentTemplate
2. **自定义创建** - 用户手动配置所有参数

```typescript
// 前端创建 Agent 请求
interface CreateAgentRequest {
  // 方式1: 从模板创建
  templateId?: string

  // 方式2: 自定义配置
  name?: string
  role?: string
  goal?: string
  backstory?: string
  framework?: 'crewai' | 'langgraph' | 'autogen'
  model?: string
  temperature?: number

  // 部署配置
  targetServer?: string  // 目标服务器ID
  resources?: {
    cpu?: string
    memory?: string
    gpu?: boolean
  }
  environment?: Record<string, string>
}
```

#### 1.2 调用后端 API

```typescript
// POST /api/agents/create
async function createAgent(request: CreateAgentRequest) {
  const response = await fetch(`${API_BASE}/api/agents/create`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  return response.json()
}
```

---

### 阶段 2：后端处理

#### 2.1 创建 Agent Controller

**文件位置**: `agent-dashboard-backend/src/main/java/com/agent/monitor/controller/AgentController.java`

需要添加创建接口：

```java
/**
 * 创建新 Agent
 *
 * POST /api/agents/create
 */
@PostMapping("/create")
public ResponseEntity<ApiResponse<AgentCreateResponse>> createAgent(
        @RequestBody CreateAgentRequest request) {

    log.info("创建 Agent: framework={}, serverId={}",
             request.getFramework(), request.getTargetServer());

    // 1. 解析请求
    AgentTemplate template = null;
    if (request.getTemplateId() != null) {
        template = agentTemplateService.getTemplate(request.getTemplateId());
    }

    // 2. 生成唯一 Agent ID
    String agentId = generateAgentId(request, template);

    // 3. 资源分配
    ResourceAllocation allocation = resourceService.allocate(
        request.getTargetServer(),
        request.getResources()
    );

    if (!allocation.isSuccess()) {
        return ResponseEntity.status(503)
            .body(ApiResponse.error(503, "资源不足: " + allocation.getMessage()));
    }

    // 4. 部署 Agent 到目标服务器
    DeployResult deployResult = deployAgent(
        agentId,
        request,
        template,
        allocation
    );

    if (!deployResult.isSuccess()) {
        // 回滚资源分配
        resourceService.release(allocation.getAllocationId());
        return ResponseEntity.status(500)
            .body(ApiResponse.error(500, "部署失败: " + deployResult.getError()));
    }

    // 5. 创建本地状态记录
    AgentState agentState = new AgentState();
    agentState.setAgentId(agentId);
    agentState.setServerId(request.getTargetServer());
    agentState.setFramework(request.getFramework());
    agentState.setLanguage("python");  // 根据框架确定
    agentState.setStatus("initializing");
    agentState.setCurrentActivity("正在初始化...");
    agentState.setRole(template?.getRole() ?? request.getRole());
    agentState.setCreatedAt(Instant.now());
    agentState.setUpdatedAt(Instant.now());

    agentStateMapper.insert(agentState);

    // 6. 广播创建事件
    webSocketMessageSender.broadcastAgentCreated(agentState);

    AgentCreateResponse response = AgentCreateResponse.builder()
        .agentId(agentId)
        .status("initializing")
        .serverId(request.getTargetServer())
        .allocationId(allocation.getAllocationId())
        .monitorUrl(deployResult.getMonitorUrl())
        .build();

    return ResponseEntity.ok(ApiResponse.success(response));
}
```

#### 2.2 部署 Agent 到目标服务器

```java
/**
 * 部署 Agent 到目标服务器
 */
private DeployResult deployAgent(
    String agentId,
    CreateAgentRequest request,
    AgentTemplate template,
    ResourceAllocation allocation) {

    try {
        // 1. 生成 Agent 代码
        String agentCode = generateAgentCode(agentId, request, template);

        // 2. 打包运行环境
        DeploymentPackage pkg = new DeploymentPackage();
        pkg.setAgentId(agentId);
        pkg.setCode(agentCode);
        pkg.setRequirements(generateRequirements(request));
        pkg.setConfig(generateAgentConfig(request, template, allocation));
        pkg.setMonitorConfig(generateMonitorConfig(agentId));

        // 3. 发送到目标服务器
        RestTemplate restTemplate = new RestTemplate();
        String deployUrl = buildDeployUrl(request.getTargetServer());

        DeployResponse deployResponse = restTemplate.postForObject(
            deployUrl,
            pkg,
            DeployResponse.class
        );

        if (deployResponse.isSuccess()) {
            return DeployResult.builder()
                .success(true)
                .monitorUrl(deployResponse.getMonitorUrl())
                .processId(deployResponse.getProcessId())
                .build();
        } else {
            return DeployResult.builder()
                .success(false)
                .error(deployResponse.getError())
                .build();
        }

    } catch (Exception e) {
        log.error("部署 Agent 失败: {}", agentId, e);
        return DeployResult.builder()
            .success(false)
            .error(e.getMessage())
            .build();
    }
}
```

#### 2.3 生成监控配置

```java
/**
 * 生成监控插件配置
 */
private MonitorConfig generateMonitorConfig(String agentId) {
    MonitorConfig config = new MonitorConfig();
    config.setEnabled(true);
    config.setServerUrl(monitorServerUrl);  // 当前后端地址
    config.setAgentId(agentId);
    config.setServerId(getLocalServerId());
    config.setDebug(false);

    // 环境变量格式
    config.setEnvironment(Map.of(
        "AGENT_MONITOR_ENABLED", "true",
        "AGENT_MONITOR_URL", monitorServerUrl,
        "AGENT_SERVER_ID", getLocalServerId(),
        "AGENT_MONITOR_DEBUG", "false"
    ));

    return config;
}
```

---

### 阶段 3：目标服务器处理

#### 3.1 Agent Host Server 接收部署请求

目标服务器需要运行一个 **Agent Host Service**，接收部署请求：

```python
# agent_host_service.py
"""
Agent Host Service - 运行在目标服务器上
接收 Agent 部署请求并启动进程
"""

from flask import Flask, request, jsonify
import subprocess
import os
import uuid
import json

app = Flask(__name__)

class AgentProcessManager:
    """管理 Agent 进程"""

    def __init__(self):
        self.processes = {}  # agentId -> subprocess.Popen
        self.workspaces = {}  # agentId -> workspace_path

    def deploy_agent(self, deployment_package):
        """部署并启动 Agent"""
        agent_id = deployment_package['agent_id']

        # 1. 创建工作目录
        workspace = f"/tmp/agents/{agent_id}"
        os.makedirs(workspace, exist_ok=True)

        # 2. 写入代码
        with open(f"{workspace}/agent.py", "w") as f:
            f.write(deployment_package['code'])

        # 3. 写入配置
        with open(f"{workspace}/config.json", "w") as f:
            json.dump(deployment_package['config'], f)

        # 4. 设置监控环境变量
        env = os.environ.copy()
        monitor_config = deployment_package['monitor_config']
        for key, value in monitor_config['environment'].items():
            env[key] = value

        # 5. 启动进程
        process = subprocess.Popen(
            ['python', f"{workspace}/agent.py"],
            cwd=workspace,
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )

        self.processes[agent_id] = process
        self.workspaces[agent_id] = workspace

        return {
            'success': True,
            'process_id': process.pid,
            'monitor_url': monitor_config['server_url']
        }

    def stop_agent(self, agent_id):
        """停止 Agent"""
        if agent_id in self.processes:
            process = self.processes[agent_id]
            process.terminate()
            process.wait(timeout=5)
            del self.processes[agent_id]
            return {'success': True}
        return {'success': False, 'error': 'Agent not found'}

manager = AgentProcessManager()

@app.route('/deploy', methods=['POST'])
def deploy():
    """接收部署请求"""
    pkg = request.json
    result = manager.deploy_agent(pkg)
    return jsonify(result)

@app.route('/stop/<agent_id>', methods=['POST'])
def stop(agent_id):
    """停止 Agent"""
    result = manager.stop_agent(agent_id)
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9999)
```

#### 3.2 生成的 Agent 代码模板

```python
# 生成的 agent.py
"""
Auto-generated Agent
Agent ID: {agent_id}
Generated at: {timestamp}
"""

import os
import sys

# ==================== 监控插件集成 ====================
# 监控插件会在 AGENT_MONITOR_ENABLED=true 时自动初始化
# 并通过以下环境变量连接到监控服务器：
#   AGENT_MONITOR_URL: 监控服务器地址
#   AGENT_SERVER_ID: 服务器唯一标识
#   AGENT_MONITOR_DEBUG: 调试模式

try:
    from agent_monitor import CrewAIPlugin

    # 插件会在导入时自动检查环境变量并安装
    # 如果 AGENT_MONITOR_ENABLED=true，插件会自动拦截 CrewAI 事件
    MONITOR_AVAILABLE = True
except ImportError:
    MONITOR_AVAILABLE = False
    print("[WARN] agent-monitor-plugin 未安装", file=sys.stderr)
# =====================================================

from crewai import Agent, Task, Crew

# Agent 配置
AGENT_CONFIG = {config}

# 创建 CrewAI Agent
def create_agent():
    """根据配置创建 Agent"""
    return Agent(
        role=AGENT_CONFIG['role'],
        goal=AGENT_CONFIG['goal'],
        backstory=AGENT_CONFIG.get('backstory', ''),
        verbose=True,
        allow_delegation=True,
    )

def main():
    """Agent 主函数"""

    # 验证监控插件状态
    if MONITOR_AVAILABLE:
        print("[INFO] Agent 监控已启用")
        # 插件已经在模块导入时自动安装
    else:
        print("[WARN] Agent 监控未启用")

    # 创建 Agent
    agent = create_agent()

    # 创建任务
    task = Task(
        description=AGENT_CONFIG.get('initial_task', 'Ready to serve'),
        agent=agent,
    )

    # 创建 Crew
    crew = Crew(
        agents=[agent],
        tasks=[task],
        verbose=True
    )

    # 启动 - 这里会触发 agent_online 事件
    print(f"[INFO] Agent {AGENT_CONFIG['agent_id']} starting...")

    # 保持运行
    while True:
        try:
            # 等待任务输入
            # 这里可以根据实际需求实现任务队列或 API 接口
            import time
            time.sleep(1)
        except KeyboardInterrupt:
            print("[INFO] Agent shutting down...")
            break

if __name__ == "__main__":
    main()
```

---

### 阶段 4：资源分配

#### 4.1 资源分配服务

```java
@Service
public class ResourceService {

    private final Map<String, ServerResources> serverResources = new ConcurrentHashMap<>();

    /**
     * 分配资源
     */
    public ResourceAllocation allocate(String serverId, ResourceRequest request) {
        ServerResources server = serverResources.get(serverId);

        if (server == null) {
            return ResourceAllocation.builder()
                .success(false)
                .message("服务器不存在: " + serverId)
                .build();
        }

        // 检查资源可用性
        if (!server.canAllocate(request)) {
            return ResourceAllocation.builder()
                .success(false)
                .message("资源不足")
                .build();
        }

        // 分配资源
        String allocationId = UUID.randomUUID().toString();
        server.allocate(allocationId, request);

        return ResourceAllocation.builder()
            .success(true)
            .allocationId(allocationId)
            .serverId(serverId)
            .cpu(request.getCpu())
            .memory(request.getMemory())
            .gpu(request.isGpu())
            .build();
    }

    /**
     * 释放资源
     */
    public void release(String allocationId) {
        serverResources.values().forEach(server -> {
            server.release(allocationId);
        });
    }
}
```

---

## 常驻进程管理

### 进程保活机制

```python
# agent_supervisor.py
"""
Agent 进程监控器
确保 Agent 进程常驻运行，异常退出时自动重启
"""

import subprocess
import time
import signal
import os
from typing import Optional

class AgentSupervisor:
    """Agent 进程监控器"""

    def __init__(self, agent_id: str, workspace: str, env: dict):
        self.agent_id = agent_id
        self.workspace = workspace
        self.env = env
        self.process: Optional[subprocess.Popen] = None
        self.restart_count = 0
        self.max_restarts = 10
        self.running = True

    def start(self):
        """启动监控"""
        while self.running and self.restart_count < self.max_restarts:
            self._start_agent()
            exit_code = self._wait_for_exit()

            if exit_code == 0:
                print(f"[INFO] Agent {self.agent_id} 正常退出")
                break

            self.restart_count += 1
            wait_time = min(2 ** self.restart_count, 60)

            print(f"[WARN] Agent {self.agent_id} 异常退出，{wait_time}秒后重启...")
            time.sleep(wait_time)

    def _start_agent(self):
        """启动 Agent 进程"""
        self.process = subprocess.Popen(
            ['python', f"{self.workspace}/agent.py"],
            cwd=self.workspace,
            env=self.env,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        print(f"[INFO] Agent {self.agent_id} 进程已启动 (PID: {self.process.pid})")

    def _wait_for_exit(self) -> int:
        """等待进程退出"""
        try:
            return self.process.wait(timeout=300)  # 5分钟超时
        except subprocess.TimeoutExpired:
            # 超时，强制杀死
            self.process.kill()
            return -1

    def stop(self):
        """停止监控"""
        self.running = False
        if self.process:
            self.process.terminate()
            self.process.wait(timeout=5)
```

---

## 插件集成验证

### 验证流程

#### 1. 启动时验证

```python
# 在 Agent 启动时验证插件状态
def verify_monitor_plugin():
    """验证监控插件是否正确安装"""
    checks = {
        'module_available': False,
        'env_enabled': False,
        'server_url': None,
        'can_connect': False
    }

    # 检查模块
    try:
        import agent_monitor
        checks['module_available'] = True
    except ImportError:
        return checks

    # 检查环境变量
    checks['env_enabled'] = os.getenv('AGENT_MONITOR_ENABLED') == 'true'
    checks['server_url'] = os.getenv('AGENT_MONITOR_URL')

    # 检查连接
    if checks['server_url']:
        try:
            import requests
            response = requests.get(f"{checks['server_url']}/api/health", timeout=2)
            checks['can_connect'] = response.status_code == 200
        except:
            pass

    return checks

# 在 Agent 主函数中调用
def main():
    print("[INFO] 验证监控插件...")
    checks = verify_monitor_plugin()

    print(f"  模块可用: {checks['module_available']}")
    print(f"  已启用: {checks['env_enabled']}")
    print(f"  服务器: {checks['server_url']}")
    print(f"  可连接: {checks['can_connect']}")

    if all(checks.values()):
        print("[SUCCESS] 监控插件已正确集成")
    else:
        print("[WARN] 监控插件集成存在问题，部分功能可能不可用")
```

#### 2. 事件验证

Agent 启动后，插件会自动发送 `agent_online` 事件，后端可以通过以下方式验证：

```java
// EventService.java 中的处理
private void handleAgentOnline(MonitorEventDTO event) {
    String agentId = event.getSource().getAgentId();

    // 验证必需字段
    if (event.getSource().getServerId() == null) {
        log.warn("Agent online 事件缺少 serverId: {}", agentId);
        // 可以请求客户端补充信息或拒绝连接
    }

    // 验证插件版本
    String protocolVersion = event.getProtocol();
    if (!"agent-monitor".equals(event.getProtocol())) {
        log.warn("不支持的协议版本: {}, agent: {}", protocolVersion, agentId);
    }

    // 记录 Agent 上线
    // ... 后续处理逻辑
}
```

#### 3. 心跳验证

插件可以定期发送心跳事件验证连接：

```python
# agent_monitor/plugins/crewai_plugin.py
# 添加心跳功能
def _start_heartbeat(self):
    """启动心跳线程"""
    import threading

    def heartbeat_loop():
        while True:
            try:
                heartbeat_event = MonitorEvent(
                    source=EventSource(
                        server_id=self.server_id,
                        agent_id=self.agent_id,
                        framework="crewai",
                        language=Language.python,
                    ),
                    event={
                        "type": "heartbeat",
                        "data": {"timestamp": time.time()}
                    },
                    metadata=EventMetadata(hostname=socket.gethostname())
                )
                self.transport.send(heartbeat_event.to_dict())
                time.sleep(30)  # 每30秒一次心跳
            except Exception as e:
                logger.error(f"心跳发送失败: {e}")

    thread = threading.Thread(target=heartbeat_loop, daemon=True)
    thread.start()
```

后端处理心跳：

```java
// EventService.java
case "heartbeat":
    handleHeartbeat(event);
    break;

private void handleHeartbeat(MonitorEventDTO event) {
    String agentId = event.getSource().getAgentId();
    AgentState state = agentStateMapper.findByAgentId(agentId);

    if (state != null) {
        state.setLastActivity(Instant.now());
        agentStateMapper.update(state);
        log.debug("收到心跳: {}", agentId);
    }
}
```

---

## API 接口定义

### 创建 Agent

```
POST /api/agents/create

Request Body:
{
  "templateId": "customer-service-crew-v1",  // 可选，从模板创建
  "targetServer": "server-001",
  "resources": {
    "cpu": "2",
    "memory": "4Gi",
    "gpu": false
  }
}

Response (200 OK):
{
  "success": true,
  "data": {
    "agentId": "agent-cs-001-abc123",
    "status": "initializing",
    "serverId": "server-001",
    "allocationId": "alloc-xyz-789",
    "monitorUrl": "http://monitor-server:8080"
  }
}

Response (503 Service Unavailable):
{
  "success": false,
  "error": "资源不足: 目标服务器 CPU 已满载"
}
```

### 查询 Agent 状态

```
GET /api/agents/{agentId}

Response:
{
  "agentId": "agent-cs-001-abc123",
  "status": "online",
  "currentActivity": "处理客户咨询中",
  "framework": "crewai",
  "language": "python",
  "role": "Customer Service Representative",
  "lastActivity": "2025-02-09T10:30:00Z"
}
```

### 停止 Agent

```
POST /api/agents/{agentId}/stop

Response:
{
  "success": true,
  "data": {
    "agentId": "agent-cs-001-abc123",
    "operation": "stop",
    "status": "success",
    "message": "Agent 已停止",
    "currentAgentStatus": "stopped",
    "timestamp": 1707477000000
  }
}
```

---

## 完整流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Agent 创建完整流程                                    │
└─────────────────────────────────────────────────────────────────────────────┘

  用户                   前端                  后端                  Agent Host
   │                      │                     │                      │
   │  点击创建Agent       │                     │                      │
   ├─────────────────────▶│                     │                      │
   │                      │                     │                      │
   │                      │  POST /api/agents/create                     │
   │                      ├────────────────────▶│                      │
   │                      │                     │                      │
   │                      │                     │ 1. 生成AgentID       │
   │                      │                     │ 2. 分配资源          │
   │                      │                     │ 3. 生成代码          │
   │                      │                     │ 4. 生成监控配置      │
   │                      │                     │                      │
   │                      │                     │  POST /deploy        │
   │                      │                     ├─────────────────────▶│
   │                      │                     │                      │
   │                      │                     │                      │ 创建工作目录
   │                      │                     │                      │ 写入代码+配置
   │                      │                     │                      │ 设置环境变量
   │                      │                     │                      │
   │                      │                     │                      │ 启动Python进程
   │                      │                     │                      │   │
   │                      │                     │                      │   ▼
   │                      │                     │                      │ 导入agent_monitor
   │                      │                     │                      │ 插件自动安装
   │                      │                     │                      │   │
   │                      │                     │                      │   ▼
   │                      │                     │                      │ 创建CrewAI Agent
   │                      │                     │                      │   │
   │                      │                     │                      │   ▼
   │                      │                     │                      │ 发送agent_online事件
   │                      │                     │◀─────────────────────┤ HTTP POST /events
   │                      │                     │                      │
   │                      │                     │ 更新AgentState       │
   │                      │                     │ 广播WebSocket更新    │
   │                      │◀────────────────────┤                      │
   │                      │                     │                      │
   │  显示Agent已创建     │                     │                      │
   │◀─────────────────────┤                     │                      │
   │                      │                     │                      │
   │  WebSocket实时更新   │                     │                      │
   │◀────────────────═════┤ STOMP /topic/agents │                      │
   │                      │                     │                      │
   │                      │                     │                      │
   │  看到Agent状态=online│                     │                      │
   │                      │                     │                      │
```

---

## 数据流验证

### 插件事件流

```
Python Agent (with plugin)
        │
        │ 1. 导入时自动安装
        ▼
   CrewAIPlugin.install()
        │
        │ 2. 注册事件监听器
        ▼
   crewai_event_bus.on(AgentExecutionStartedEvent)
        │
        │ 3. Agent 运行时触发事件
        ▼
   on_agent_start()
        │
        │ 4. 构造 MonitorEvent
        ▼
   MonitorEvent(
       source=EventSource(
           server_id="server-001",
           agent_id="agent-123",
           framework="crewai",
           language="python"
       ),
       event={
           "type": "agent_online",
           "data": {"role": "Customer Service"}
       }
   )
        │
        │ 5. 发送到后端
        ▼
   DirectTransport.send()
        │
        │ HTTP POST http://monitor-server:8080/api/events
        ▼
   EventController.receiveEvent()
        │
        │ 6. 处理事件
        ▼
   EventService.processEvent()
        │
        │ 7. 更新状态
        ▼
   AgentState.status = "online"
   AgentStateMapper.insert()
        │
        │ 8. 广播更新
        ▼
   WebSocketMessageSender.broadcastAgentUpdate()
        │
        │ STOMP /topic/agents
        ▼
   Frontend EventStream
        │
        │ 9. 更新 UI
        ▼
   AgentStore.update()
   AgentScene.updateAgentStatus()
```

---

## 总结

### 关键点

1. **前端发起** - 用户通过 UI 选择模板或自定义配置
2. **后端协调** - 生成 ID、分配资源、生成代码、部署到目标服务器
3. **目标服务器** - 运行 Agent Host Service，接收部署请求，启动 Python 进程
4. **插件集成** - 通过环境变量配置，导入时自动安装和初始化
5. **事件驱动** - Agent 运行时自动发送事件到后端，实时更新状态
6. **验证机制** - 启动验证、心跳验证、事件验证确保插件正常工作

### 验证插件已集成

| 验证点 | 方法 |
|--------|------|
| 模块导入 | `import agent_monitor` 成功 |
| 环境变量 | `AGENT_MONITOR_ENABLED=true` |
| 服务器连接 | `GET /api/health` 返回 200 |
| 事件接收 | 后端收到 `agent_online` 事件 |
| 心跳正常 | 后端定期收到 `heartbeat` 事件 |
| WebSocket 更新 | 前端收到实时状态更新 |

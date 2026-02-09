# Agent 监控系统部署与数据流设计

> **版本**: v1.0
> **日期**: 2025-02-06
> **核心主题**: 插件部署方案与数据流转设计

---

## 一、核心问题

### 1.1 原始架构的问题

**用户质疑：**
> "在 Agent 的环境上要安装插件也要安装 Monitor Agent 是吗？"

**问题分析：**
```
Agent 服务器需要安装：
1. Agent 框架
2. 监控插件 (agent-monitor-plugin)
3. Monitor Agent (独立进程) ❌ 多了一个组件！

问题：
- 需要安装和运行两个东西
- Monitor Agent 需要单独维护
- 增加部署复杂度
```

---

## 二、三种部署模式

### 模式 1：插件直连（最简单）⭐⭐⭐

#### 架构图

```
Agent 服务器：
├── Agent 框架 (CrewAI/LangGraph)
└── 监控插件 ← 直接发送到远程监控服务
       │
       │ HTTP/2 POST
       ▼
监控服务器 (Java/Spring)
```

#### 实现方式

```python
# agent_monitor/plugin/simple.py
"""
简单插件 - 直接发送到远程
"""

import threading
import requests

class SimplePlugin:
    def __init__(self, monitor_url: str):
        self.monitor_url = monitor_url

    def send_event(self, event: dict):
        """直接发送（非阻塞）"""
        def send():
            try:
                requests.post(
                    f"{self.monitor_url}/api/events",
                    json=event,
                    timeout=1
                )
            except:
                pass  # 静默失败，不影响 Agent

        threading.Thread(target=send, daemon=True).start()
```

#### 用户操作步骤

```bash
# 1. 安装插件
pip install agent-monitor-plugin

# 2. 配置环境变量
export AGENT_MONITOR_URL=http://monitor.example.com

# 3. 运行 Agent
python my_agent.py

# 完成！
```

#### 优点
- ✅ 只需要安装一个包
- ✅ 无需额外进程
- ✅ 部署最简单
- ✅ 适合快速上手

#### 缺点
- ⚠️ 每个插件都独立建立连接
- ⚠️ 网络故障时可能丢事件
- ⚠️ 无法批量优化

#### 适用场景
- 开发/测试环境
- 小规模部署（< 20 个 Agent）
- 网络稳定的环境

---

### 模式 2：集成代理（推荐）⭐⭐⭐⭐⭐

#### 架构图

```
Agent 服务器：
├── Agent 框架
└── 监控插件 ← 通过内存通信
       │
       ▼
监控代理 (嵌入在插件进程中，单例模式)
       │
       │ HTTP/2 批量发送
       ▼
监控服务器
```

#### 核心改变

**监控代理不是独立进程，而是插件库的一部分！**

```python
# agent_monitor/python/proxy.py
"""
内置代理 - 不是独立进程，而是插件的一部分
"""

import threading
import time
import msgpack
from typing import List, Dict

class EmbeddedProxy:
    """嵌入在插件中的轻量级代理"""

    _instance = None
    _lock = threading.Lock()

    def __new__(cls, *args, **kwargs):
        """单例模式 - 整个进程只有一个代理实例"""
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super().__new__(cls)
                    cls._instance._initialized = False
        return cls._instance

    def __init__(self, remote_url: str):
        if self._initialized:
            return

        self.remote_url = remote_url
        self.event_buffer: List[Dict] = []
        self.buffer_size = 100
        self.flush_interval = 5
        self._initialized = True

        # 启动后台刷新线程
        self._start_flush_thread()

    def send(self, event: Dict):
        """发送事件（所有插件共用这个代理）"""
        with self._lock:
            self.event_buffer.append(event)

            if len(self.event_buffer) >= self.buffer_size:
                self._flush()

    def _start_flush_thread(self):
        """启动后台刷新线程"""
        def flush_worker():
            while True:
                time.sleep(self.flush_interval)
                self._flush()

        thread = threading.Thread(target=flush_worker, daemon=True)
        thread.start()

    def _flush(self):
        """批量发送到远程"""
        with self._lock:
            if not self.event_buffer:
                return

            events = self.event_buffer[:]
            self.event_buffer.clear()

        # 发送
        try:
            payload = msgpack.packb(events)
            requests.post(
                f"{self.remote_url}/api/events/batch",
                data=payload,
                timeout=5
            )
        except:
            # 失败时缓存到本地文件
            self._cache_to_disk(events)

    def _cache_to_disk(self, events: List[Dict]):
        """缓存到磁盘"""
        cache_file = "/tmp/agent_monitor_cache.msgpack"

        try:
            with open(cache_file, "ab") as f:
                f.write(msgpack.packb(events))
        except:
            pass


# 插件使用
class CrewAIPlugin:
    def __init__(self, monitor_url: str):
        # 获取或创建共享代理（单例）
        self.proxy = EmbeddedProxy(monitor_url)

    def on_agent_event(self, source, event):
        # 通过代理发送
        self.proxy.send({
            "protocol": "agent-monitor",
            "event": {"type": "agent_online"}
        })
```

#### 用户操作步骤

```bash
# 1. 安装插件（内置代理）
pip install agent-monitor-plugin

# 2. 启用（环境变量）
export AGENT_MONITOR_ENABLED=true
export AGENT_MONITOR_URL=http://monitor.example.com

# 3. 运行 Agent（自动监控）
python my_crewai_app.py
```

#### 关键点
- ✅ **只安装一个包** - `agent-monitor-plugin`
- ✅ **无需额外进程** - 代理在插件进程中运行
- ✅ **所有插件共享** - 单例模式，一个进程一个代理
- ✅ **自动批量发送** - 后台线程自动处理

---

### 模式 3：独立 Monitor Agent（可选，企业级）

#### 架构图

```
Agent 服务器：
├── Agent 框架
├── 监控插件
└── Monitor Agent (独立进程) ← 仅在需要时安装
       │
       │ UDP (本地)
       ▼
监控服务器
```

#### 安装步骤

```bash
# 可选：安装独立代理
pip install agent-monitor-agent

# 启动
agent-monitor-agent --start

# 插件自动检测并使用本地代理
```

#### 适用场景
- 大规模部署（100+ 个 Agent）
- 需要高可靠性
- 需要集中管理

---

## 三、三种模式对比

| 特性 | 模式 1 (直连) | 模式 2 (集成代理) | 模式 3 (独立代理) |
|------|-------------|-----------------|-----------------|
| **安装复杂度** | ⭐ 一个包 | ⭐ 一个包 | ⭐⭐ 两个包 |
| **运行复杂度** | ⭐ 无额外进程 | ⭐ 无额外进程 | ⭐⭐⭐ 额外进程 |
| **性能优化** | ⭐⭐ 各自发送 | ⭐⭐⭐⭐ 批量优化 | ⭐⭐⭐⭐⭐ 最佳优化 |
| **可靠性** | ⭐⭐ 一般 | ⭐⭐⭐ 好 | ⭐⭐⭐⭐⭐ 最佳 |
| **适用规模** | < 20 Agents | < 100 Agents | 100+ Agents |
| **推荐度** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

---

## 四、数据流转设计

### 4.1 直连模式数据流（MVP）

```
1. Python Agent 执行
   ↓
2. CrewAI 发射事件
   ↓
3. Python 插件捕获
   {
     "protocol": "agent-monitor",
     "event": {"type": "agent_online"}
   }
   ↓
4. HTTP POST 到监控服务器
   POST http://monitor.example.com/api/events
   Content-Type: application/json
   Body: {"event": {...}}
   ↓
5. 监控服务器处理
   - 验证 Token
   - 验证格式
   - 存储到数据库
   ↓
6. 返回 200 OK
```

### 4.2 集成代理模式数据流（生产）

```
1. Python Agent 执行
   ↓
2. CrewAI 发射事件
   ↓
3. Python 插件捕获
   ↓
4. 发送到内置代理（内存）
   - 加入缓冲区
   - 等待批量或定时
   ↓
5. 达到 100 条或 5 秒
   ↓
6. 批量压缩
   - MessagePack 序列化
   - 从 1MB → 700KB
   ↓
7. HTTP POST 到监控服务器
   POST http://monitor.example.com/api/events/batch
   Content-Type: application/msgpack
   Body: [100 events, 700KB]
   ↓
8. 监控服务器处理
   - 验证
   - 批量存储
   ↓
9. 返回 200 OK
```

---

## 五、技术栈确定

### 5.1 插件（Python）

**框架：**
- `agent-monitor-plugin` - Python 包

**依赖：**
- `requests` - HTTP 客户端
- `msgpack` - 序列化（可选，生产用）
- `pydantic` - 数据验证

**支持框架：**
- CrewAI（优先）
- LangGraph（后续）
- AutoGen（后续）

### 5.2 监控服务器（Java/Spring）

**技术栈：**
- Java 17+
- Maven 3.8+
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- PostgreSQL（数据库）
- WebSocket（实时推送）

**目录结构：**
```
agent-dashboard-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/agent/monitor/
│   │   │   ├── MonitorApplication.java
│   │   │   ├── controller/
│   │   │   │   └── EventController.java
│   │   │   ├── service/
│   │   │   │   ├── EventService.java
│   │   │   │   └── AgentStateService.java
│   │   │   ├── model/
│   │   │   │   ├── MonitorEvent.java
│   │   │   │   └── AgentState.java
│   │   │   └── repository/
│   │   │       └── AgentStateRepository.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/
└── README.md
```

---

## 六、API 设计

### 6.1 接收单个事件

```http
POST /api/events
Content-Type: application/json
Authorization: Bearer {token}

{
  "protocol": "agent-monitor",
  "version": "1.0",
  "timestamp": "2025-02-06T12:00:00Z",
  "source": {
    "server_id": "server-1",
    "agent_id": "agent-123",
    "framework": "crewai",
    "language": "python"
  },
  "event": {
    "type": "agent_online",
    "data": {
      "role": "研究员",
      "goal": "研究 AI"
    }
  },
  "metadata": {
    "hostname": "server-1.example.com"
  }
}
```

### 6.2 接收批量事件

```http
POST /api/events/batch
Content-Type: application/json
Authorization: Bearer {token}

[
  {...event1},
  {...event2},
  {...event3}
]
```

### 6.3 查询 Agent 状态

```http
GET /api/agents
Authorization: Bearer {token}

[
  {
    "agent_id": "agent-123",
    "server_id": "server-1",
    "framework": "crewai",
    "status": "online",
    "last_activity": "2025-02-06T12:05:00Z"
  }
]
```

---

## 七、实现路线图

### Phase 1: MVP 直连模式（2周）

**插件（Python）：**
- ✅ CrewAI 事件监听
- ✅ 直连发送到监控服务器
- ✅ 基础错误处理

**监控服务器（Java/Spring）：**
- ✅ Spring Boot 项目搭建
- ✅ 接收事件 API
- ✅ PostgreSQL 存储
- ✅ Agent 状态查询 API

**集成：**
- ✅ 端到端测试
- ✅ 文档编写

### Phase 2: 集成代理模式（2周）

- ✅ 内置代理实现
- ✅ 批量发送
- ✅ 本地缓存
- ✅ 性能优化

### Phase 3: 生产增强（后续）

- ⚠️ 认证授权
- ⚠️ WebSocket 实时推送
- ⚠️ 前端看板
- ⚠️ 关系图谱

---

## 八、项目结构

### 8.1 插件项目

```
agent-monitor-plugin/
├── setup.py
├── README.md
├── agent_monitor/
│   ├── __init__.py
│   ├── config/
│   │   ├── __init__.py
│   │   └── settings.py
│   ├── plugins/
│   │   ├── __init__.py
│   │   ├── base.py
│   │   └── crewai_plugin.py
│   ├── transports/
│   │   ├── __init__.py
│   │   ├── direct.py          # 直连（Phase 1）
│   │   └── embedded_proxy.py  # 集成代理（Phase 2）
│   ├── protocol/
│   │   ├── __init__.py
│   │   └── unified_event.py
│   └── utils/
│       ├── __init__.py
│       └── validation.py
├── tests/
│   ├── test_crewai_plugin.py
│   └── test_transport.py
└── requirements.txt
```

### 8.2 监控服务项目

```
agent-dashboard-backend/
├── pom.xml
├── README.md
└── src/main/java/com/agent/monitor/
    ├── MonitorApplication.java
    ├── config/
    │   └── SecurityConfig.java
    ├── controller/
    │   ├── EventController.java
    │   └── AgentController.java
    ├── dto/
    │   ├── MonitorEventDTO.java
    │   └── AgentStateDTO.java
    ├── service/
    │   ├── EventService.java
    │   └── AgentStateService.java
    ├── model/
    │   ├── MonitorEvent.java
    │   └── AgentState.java
    ├── repository/
    │   └── AgentStateRepository.java
    └── exception/
        └── GlobalExceptionHandler.java
```

---

## 九、快速开始

### 9.1 安装插件

```bash
# 从源码安装
cd agent-monitor-plugin
pip install -e .

# 或从 PyPI 安装（未来）
pip install agent-monitor-plugin
```

### 9.2 启动监控服务器

```bash
cd agent-dashboard-backend
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/agent-dashboard-backend-1.0.0.jar
```

### 9.3 配置 Agent

```bash
export AGENT_MONITOR_ENABLED=true
export AGENT_MONITOR_URL=http://localhost:8080

python my_crewai_app.py
```

---

## 十、关键决策

### 10.1 为什么选择直连模式作为 MVP？

1. **最简单** - 只需安装一个插件
2. **快速验证** - 验证监控思路是否可行
3. **易于调试** - 直接 HTTP 调用，问题易定位
4. **平滑升级** - 后期可无缝升级到集成代理

### 10.2 为什么选择 Java/Spring 作为后端？

1. **企业级** - 成熟、稳定、可扩展
2. **生态丰富** - 大量现成组件
3. **团队熟悉** - Java 开发者多
4. **性能好** - 适合处理大量事件

### 10.3 技术栈兼容性

| 组件 | 技术栈 | 原因 |
|------|--------|------|
| 插件 | Python | Agent 框架都是 Python |
| 监控服务器 | Java/Spring | 企业级、可扩展 |
| 数据库 | PostgreSQL | 成熟、支持 JSON |
| 前端 | 待定 | 可选 React/Vue |

---

## 十一、常见问题

### Q1: 直连模式会阻塞 Agent 吗？

**A:** 不会。插件使用独立线程发送，设置超时（1秒），失败不影响 Agent。

### Q2: 网络故障时事件会丢失吗？

**A:** MVP 阶段会丢失。Phase 2 会增加本地缓存和重试。

### Q3: 如何处理大量事件？

**A:** Phase 1 使用直连，Phase 2 使用批量发送，Phase 3 引入 Kafka。

### Q4: 可以监控非 Python Agent 吗？

**A:** 可以。未来会实现 TypeScript 插件，使用相同的 API。

---

## 十二、下一步行动

1. ✅ 创建项目结构
2. ✅ 实现 Python 插件（直连模式）
3. ✅ 实现 Java 监控服务器
4. ✅ 集成测试
5. ⚠️ 编写文档
6. ⚠️ 示例代码

---

**文档维护**: 随开发进展持续更新

**最后更新**: 2025-02-06

# Agent Dashboard Backend

Agent 监控服务器后端 - 基于 Spring Boot 3.x

## 技术栈

- Java 17
- Spring Boot 3.2.0
- MyBatis 3.x（持久层框架）
- MySQL 8.0+（数据库）
- Liquibase（数据库版本管理）
- Druid（阿里连接池）
- Maven

## 📚 文档

| 文档 | 说明 |
|------|------|
| **[系统架构总览](./docs/design/01-system-overview.md)** ⭐ | 项目定位、架构概览、技术栈（入口文档）|
| [WebSocket 实时同步设计](./docs/design/02-snapshot-delta-sync.md) | 快照+增量数据同步方案设计 |
| [Memory-First 架构设计](./docs/design/03-memory-first-architecture.md) | Memory 优先 + 思考-执行分离的核心架构 |
| [Memory 管理系统](./docs/design/04-memory-management.md) | Memory 作为 AI 本体的完整生命周期管理 |
| [Agent 行为定义](./docs/design/05-agent-behavior.md) | Agent 生命周期、执行能力、工具系统完整定义 |
| [CrewAI 事件映射](./docs/design/06-crewai-event-mapping.md) | CrewAI 事件到统一 Agent 行为模型的映射 |

## 快速开始

### 1. 安装 MySQL

```bash
# Windows: 下载并安装 MySQL 8.0+
# https://dev.mysql.com/downloads/mysql/

# Linux
sudo apt install mysql-server

# macOS
brew install mysql
```

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/db/init.sql
```

或直接执行：

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agent_monitor?...
    username: root  # 改成你的 MySQL 用户名
    password: root  # 改成你的 MySQL 密码
```

### 4. 编译运行

```bash
cd agent-dashboard-backend

# 编译
mvn clean package

# 运行
mvn spring-boot:run

# 或运行打包后的 jar
java -jar target/agent-dashboard-backend-1.0.0.jar
```

服务启动后访问：
- API: http://localhost:8080/api
- Druid 监控: http://localhost:8080/druid（用户名/密码: 查看 Druid 配置）

## API 接口

### 事件接收

#### 接收单个事件

```http
POST /api/events
Content-Type: application/json

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
    "hostname": "server-1"
  }
}
```

#### 接收批量事件

```http
POST /api/events/batch
Content-Type: application/json

[
  {...event1},
  {...event2}
]
```

### Agent 查询

#### 获取所有 Agent

```http
GET /api/agents
```

#### 获取在线 Agent

```http
GET /api/agents/online
```

#### 根据 ID 获取 Agent

```http
GET /api/agents/{agentId}
```

#### 根据服务器 ID 获取 Agent

```http
GET /api/agents/server/{serverId}
```

#### 获取统计信息

```http
GET /api/agents/stats
```

### Agent 操作

#### 暂停 Agent

```http
POST /api/agents/{agentId}/pause
```

#### 恢复 Agent

```http
POST /api/agents/{agentId}/resume
```

#### 停止 Agent

```http
POST /api/agents/{agentId}/stop
```

#### 重启 Agent

```http
POST /api/agents/{agentId}/restart
```

#### 删除 Agent

```http
DELETE /api/agents/{agentId}
```

#### 更新 Agent 配置

```http
PATCH /api/agents/{agentId}/config
Content-Type: application/json

{
  "role": "高级研究员",
  "temperature": 0.7
}
```

#### 批量操作 Agent

```http
POST /api/agents/batch
Content-Type: application/json

{
  "operation": "pause",
  "agentIds": ["agent-001", "agent-002"]
}
```

支持的操作: `pause`, `resume`, `stop`, `restart`, `delete`

### Memory 管理

#### 获取所有 Memory

```http
GET /api/memories
```

#### 创建 Memory

```http
POST /api/memories
Content-Type: application/json

{
  "name": "研究员 Memory",
  "role": "高级研究员",
  "persona": "专业、严谨",
  "goal": "深入研究特定领域"
}
```

#### 激活 Memory

```http
POST /api/memories/{memoryId}/activate
```

#### 停用 Memory

```http
POST /api/memories/{memoryId}/deactivate
```

### Task 管理

#### 获取所有任务

```http
GET /api/tasks
```

#### 获取待分配任务

```http
GET /api/tasks/pending
```

#### 创建任务

```http
POST /api/tasks
Content-Type: application/json

{
  "name": "数据分析",
  "description": "分析用户行为数据",
  "priority": 8
}
```

#### 分配任务

```http
POST /api/tasks/{taskId}/assign
Content-Type: application/json

{
  "agentId": "agent-001",
  "memoryId": "mem-001"
}
```

#### 开始任务

```http
POST /api/tasks/{taskId}/start
```

#### 更新任务进度

```http
PATCH /api/tasks/{taskId}/progress
Content-Type: application/json

{
  "progress": 50,
  "output": "已完成50%"
}
```

#### 完成任务

```http
POST /api/tasks/{taskId}/complete
Content-Type: application/json

{
  "output": "任务完成，生成了分析报告"
}
```

#### 任务失败

```http
POST /api/tasks/{taskId}/fail
Content-Type: application/json

{
  "error": "连接超时"
}
```

### Agent Template 管理

#### 获取所有模板

```http
GET /api/templates
```

#### 获取启用的模板

```http
GET /api/templates/enabled
```

#### 根据类型获取模板

```http
GET /api/templates/type/{type}
```

支持类型: `CrewAI`, `LangChain`, `AutoGen`

#### 创建模板

```http
POST /api/templates
Content-Type: application/json

{
  "name": "CrewAI 研究员",
  "type": "CrewAI",
  "category": "research",
  "role": "高级研究员",
  "goal": "深入研究特定领域",
  "backstory": "经验丰富的研究员",
  "persona": "专业、严谨",
  "skills": ["信息检索", "数据分析"],
  "tools": ["search_tool", "pdf_reader"],
  "enabled": true
}
```

### WebSocket 实时推送

连接 WebSocket 后可订阅以下频道:

- `/topic/agents` - Agent 状态更新推送
- `/topic/agents/{agentId}` - 特定 Agent 的更新
- `/topic/notifications` - 系统通知

```javascript
const ws = new SockJS('http://localhost:8080/ws')
const client = Stomp.over(ws)

client.connect({}, () => {
  // 订阅所有 Agent 更新
  client.subscribe('/topic/agents', (message) => {
    const data = JSON.parse(message.body)
    console.log('Agent updated:', data)
  })

  // 订阅系统通知
  client.subscribe('/topic/notifications', (message) => {
    const notification = JSON.parse(message.body)
    console.log('Notification:', notification)
  })
})
```

## 数据库管理

### Liquibase 自动建表

启动应用时，Liquibase 会自动执行以下操作：

1. 创建 `agent_states` 表
2. 创建索引
3. 记录执行日志

### 查看变更历史

Liquibase 会在数据库中创建两张表：

- `databasechangelog` - 变更日志
- `databasechangeloglock` - 变更锁

### 添加新的变更

在 `src/main/resources/db/changelog/` 目录下创建新的 changelog 文件：

```yaml
databaseChangeLog:
  - changeSet:
      id: add-new-column
      author: your-name
      changes:
        - addColumn:
            tableName: agent_states
            columns:
              - column:
                  name: new_field
                  type: VARCHAR(255)
```

## Druid 监控

访问 http://localhost:8080/druid 可以查看：

- SQL 监控
- 慢 SQL 分析
- 连接池状态
- URI 监控

默认配置下，Druid 统计功能已开启，慢 SQL 阈值设置为 1000ms。

## 项目结构

```
src/main/java/com/agent/monitor/
├── MonitorApplication.java       # 主应用类
├── controller/                    # 控制器层
│   ├── EventController.java      # 事件接收
│   ├── AgentController.java      # Agent 查询 + 操作
│   ├── MemoryController.java     # Memory 管理
│   ├── TaskController.java       # 任务管理
│   └── AgentTemplateController.java # 模板管理
├── service/                       # 服务层
│   ├── EventService.java        # 事件处理
│   ├── AgentOperationService.java # Agent 操作
│   ├── MemoryService.java       # Memory 业务逻辑
│   ├── TaskService.java         # 任务业务逻辑
│   └── AgentTemplateService.java # 模板业务逻辑
├── entity/                        # 实体类
│   ├── AgentState.java          # Agent 状态实体
│   ├── Memory.java              # Memory 实体
│   ├── Task.java                # 任务实体
│   └── AgentTemplate.java       # 模板实体
├── mapper/                        # MyBatis Mapper
│   ├── AgentStateMapper.java    # Agent 数据访问
│   ├── MemoryMapper.java        # Memory 数据访问
│   ├── TaskMapper.java          # 任务数据访问
│   └── AgentTemplateMapper.java # 模板数据访问
├── dto/                           # 数据传输对象
│   ├── MonitorEventDTO.java     # 事件 DTO
│   ├── ApiResponse.java         # 统一响应格式
│   └── AgentOperationResponse.java # 操作响应
└── websocket/                     # WebSocket
    ├── WebSocketConfig.java     # WebSocket 配置
    └── WebSocketMessageSender.java # 消息发送

src/main/resources/
├── application.yml             # 应用配置
├── db/
│   ├── changelog/              # Liquibase 变更日志
│   │   └── db.changelog-master.yaml
│   └── init.sql                # 初始化脚本
└── mapper/                     # MyBatis XML 映射
    └── AgentStateMapper.xml
```

## 配置说明

### 数据源配置

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/agent_monitor
    username: root
    password: root
```

### Druid 连接池配置

```yaml
spring:
  datasource:
    druid:
      initial-size: 5          # 初始连接数
      min-idle: 5              # 最小空闲连接
      max-active: 20           # 最大活动连接
      max-wait: 60000          # 最大等待时间（毫秒）
```

### MyBatis 配置

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.agent.monitor.entity
  configuration:
    map-underscore-to-camel-case: true  # 下划线转驼峰
    cache-enabled: true                 # 开启缓存
```

## 开发

```bash
# 运行测试
mvn test

# 编译
mvn clean compile

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

## 常见问题

### Q1: 启动报错 "Access denied for user"

**A:** 检查 `application.yml` 中的用户名和密码是否正确。

### Q2: 表已存在错误

**A:** Liquibase 会检测表是否存在，如果表已存在会跳过创建。如需重建，手动删除表后重启应用。

### Q3: 中文乱码

**A:** 确保 JDBC URL 包含 `characterEncoding=utf8`：

```
jdbc:mysql://localhost:3306/agent_monitor?useUnicode=true&characterEncoding=utf8
```

### Q4: 连接池耗尽

**A:** 调整 Druid 配置中的 `max-active` 参数。

---

**最后更新**: 2025-02-07
**版本**: 0.1.0

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
├── MonitorApplication.java    # 主应用类
├── controller/                 # 控制器层
│   ├── EventController.java   # 事件接收
│   └── AgentController.java   # Agent 查询
├── service/                    # 服务层
│   └── EventService.java      # 事件处理
├── entity/                     # 实体类
│   └── AgentState.java        # Agent 状态实体
├── mapper/                     # MyBatis Mapper
│   └── AgentStateMapper.java  # 数据访问接口
├── dto/                        # 数据传输对象
│   └── MonitorEventDTO.java   # 事件 DTO
└── exception/                  # 异常处理

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

**最后更新**: 2025-02-06
**版本**: 0.1.0

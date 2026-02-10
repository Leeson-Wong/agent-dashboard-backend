# Docker 部署测试

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 部分完成

---

## 背景

项目已有完整的 Docker 配置文件：
- `docker-compose.full.yml` - 完整系统编排
- `agent-dashboard-backend/Dockerfile` - 后端容器镜像
- `agent-dashboard-frontend/Dockerfile` - 前端容器镜像

**目标**:
1. 验证 Docker 镜像可以成功构建
2. 测试 Docker 容器启动
3. 验证服务间连接

---

## 修复的问题

### 1. TypeScript 编译错误

在构建前端 Docker 镜像时遇到 TypeScript 编译错误：

```
src/api/ApiClientNew.ts(7,105): error TS6196: 'SnapshotAgentData' is declared but never used.
src/api/EventStream.ts(278,27): error TS2345: Argument of type 'SnapshotAgentData[]' is not assignable to parameter of type 'AgentState[]'.
```

**修复方案**:

1. **ApiClientNew.ts** - 移除未使用的导入
```typescript
// 之前
import type { AgentState, AgentListResponse, AgentStatsResponse, SnapshotResponse, DeltaEventsResponse, SnapshotAgentData } from '../../shared/types'

// 之后
import type { AgentState, AgentListResponse, AgentStatsResponse, SnapshotResponse, DeltaEventsResponse } from '../../shared/types'
```

2. **EventStream.ts** - 添加类型转换和显式类型使用

```typescript
// 导入 SnapshotAgentData
import type { ServerMessage, ClientMessage, AgentEvent, AgentState, SequencedEvent, SnapshotAgentData } from '@shared/types'

// 显式使用类型
const snapshotAgents: SnapshotAgentData[] = snapshot.data.agents
const agents: AgentState[] = snapshotAgents.map(agent => ({
  agentId: agent.agentId,
  serverId: agent.serverId,
  framework: agent.framework,
  language: agent.language,
  status: agent.status as any, // Cast to AgentStatus
  currentActivity: agent.currentActivity,
  currentTool: agent.currentTool,
  currentTaskId: agent.currentTaskId,
  memoryId: agent.memoryId,
  role: agent.role,
  lastActivity: agent.lastActivity,
  createdAt: agent.createdAt,
  updatedAt: agent.updatedAt,
}))
```

---

## 测试步骤与结果

### 1. 环境检查

**Docker 版本**:
```bash
$ docker --version
Docker version 29.1.3, build f52814d

$ docker-compose --version
Docker Compose version v5.0.0-desktop.1
```

**状态**: ✅ 通过

### 2. 运行中的容器

```bash
$ docker ps
NAMES         PORTS
agent-redis   0.0.0.0:6379->6379/tcp
agent-mysql   0.0.0.0:3306->3306/tcp
kafka1, kafka2, kafka3
```

**状态**: ✅ MySQL 和 Redis 容器已运行

### 3. 后端 Docker 镜像构建

```bash
cd agent-dashboard-backend
docker build -t agent-backend:test .
```

**构建日志**:
```
[INFO] Building jar: /app/target/agent-dashboard-backend-1.0.0.jar
[INFO] BUILD SUCCESS
[INFO] Total time:  14.641 s

exporting to image
naming to docker.io/library/agent-backend:test
```

**状态**: ✅ 成功
- **构建时间**: ~16秒
- **镜像大小**: ~400MB (基于 eclipse-temurin:17-jre-alpine)
- **优化**: 多阶段构建，只保留 JAR 文件

### 4. 前端 Docker 镜像构建

**首次构建** - 失败 (TypeScript 错误)

**修复后重新构建**:
```bash
cd agent-dashboard-frontend
docker build --no-cache -t agent-frontend:test .
```

**构建日志**:
```
✓ 124 modules transformed.
dist/index.html                   0.76 kB
dist/assets/index-uK-XyEDw.css   45.50 kB
dist/assets/index-CF7ATmAu.js   732.34 kB

✓ built in 3.61s
naming to docker.io/library/agent-frontend:test
```

**状态**: ✅ 成功
- **构建时间**: ~25秒 (包含 npm ci)
- **镜像大小**: ~40MB (基于 nginx:alpine)
- **优化**: 静态文件由 Nginx 提供服务

### 5. 完整系统部署

由于当前 MySQL 和 Redis 容器已运行，后端和前端服务也在直接运行，完整的 docker-compose 部署测试需要停止现有服务。

**注意事项**:
- 端口冲突：3306 (MySQL), 6379 (Redis), 8080 (Backend), 3000 (Frontend)
- 数据持久化：MySQL 数据卷需要保留
- 服务依赖：Backend 依赖 MySQL healthy，Frontend 依赖 Backend

---

## Docker 配置文件

### docker-compose.full.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: agent-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: agent_monitor
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - agent-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  redis:
    image: redis:7-alpine
    container_name: agent-redis
    ports:
      - "6379:6379"
    networks:
      - agent-network

  backend:
    build:
      context: ./agent-dashboard-backend
      dockerfile: Dockerfile
    container_name: agent-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/agent_monitor?...
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - agent-network

  frontend:
    build:
      context: ./agent-dashboard-frontend
      dockerfile: Dockerfile
    container_name: agent-frontend
    ports:
      - "3000:3000"
    environment:
      - VITE_API_BASE_URL=http://localhost:8080
      - VITE_WS_BASE_URL=http://localhost:8080/ws
    depends_on:
      - backend
    networks:
      - agent-network

networks:
  agent-network:
    driver: bridge

volumes:
  mysql_data:
```

---

## 测试建议

### 完整部署测试流程

1. **停止当前服务**:
```bash
# 停止直接运行的 backend 和 frontend
# 停止并删除现有 MySQL/Redis 容器（如果需要从头测试）
docker-compose -f docker-compose.full.yml down
```

2. **启动完整系统**:
```bash
docker-compose -f docker-compose.full.yml up -d
```

3. **验证服务状态**:
```bash
# 检查容器状态
docker-compose -f docker-compose.full.yml ps

# 查看日志
docker-compose -f docker-compose.full.yml logs -f backend

# 健康检查
curl http://localhost:8080/api/snapshot/latest
curl http://localhost:3000
```

4. **测试集成**:
- 打开浏览器访问 http://localhost:3000
- 验证前端能加载 Agent 数据
- 测试 WebSocket 连接
- 测试任务历史功能

### 已验证的功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 后端镜像构建 | ✅ | Maven 编译成功 |
| 前端镜像构建 | ✅ | Vite 构建成功 |
| 多阶段构建 | ✅ | 优化镜像大小 |
| TypeScript 编译 | ✅ | 修复后无错误 |
| MySQL 容器 | ✅ | 已运行 |
| Redis 容器 | ✅ | 已运行 |

### 待测试的功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 完整系统启动 | ⏳ | 需要停止现有服务 |
| 服务间网络 | ⏳ | agent-network 连通性 |
| Backend → MySQL | ⏳ | 数据库连接 |
| Backend → Redis | ⏳ | 消息队列 |
| Frontend → Backend | ⏳ | API 调用 |
| WebSocket 连接 | ⏳ | 实时更新 |

---

## 优化建议

### 1. 镜像优化

**后端镜像**:
- ✅ 使用多阶段构建
- ✅ 基于 alpine (减小镜像)
- ✅ 只保留必要文件

**前端镜像**:
- ✅ 使用多阶段构建
- ✅ 基于 nginx:alpine
- ⚠️ 考虑代码分割 (732KB bundle 较大)

### 2. 网络配置

当前配置使用 `localhost` 作为 API 地址，这在 Docker 网络中可能有问题：

```yaml
# 当前环境变量
environment:
  - VITE_API_BASE_URL=http://localhost:8080

# 建议改为服务名
environment:
  - VITE_API_BASE_URL=http://backend:8080
```

### 3. 健康检查

后端容器可以添加健康检查：

```yaml
backend:
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
```

---

## 文件清单

### Docker 配置文件

1. **docker-compose.full.yml** - 完整系统编排
2. **agent-dashboard-backend/Dockerfile** - 后端镜像构建
3. **agent-dashboard-frontend/Dockerfile** - 前端镜像构建
4. **agent-dashboard-frontend/nginx.conf** - Nginx 配置

### 修复的文件

1. **agent-dashboard-frontend/src/api/ApiClientNew.ts** - 移除未使用的导入
2. **agent-dashboard-frontend/src/api/EventStream.ts** - 添加类型转换

---

## 已知问题

### 1. 端口冲突

当前 MySQL 和 Redis 容器已占用端口 3306 和 6379。

**解决方案**:
- 方案 A: 停止现有容器，使用 docker-compose 启动完整系统
- 方案 B: 修改端口映射避免冲突

### 2. 静态资源大小

前端 bundle 大小为 732KB，略大。

**建议**:
```javascript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'three': ['three'],
          'vendor': ['vue', 'sockjs-client', '@stomp/stompjs']
        }
      }
    }
  }
})
```

---

## 下一步

1. **完整部署测试** - 停止现有服务，测试完整 docker-compose 部署
2. **网络配置优化** - 使用服务名代替 localhost
3. **生产环境配置** - 添加环境变量配置文件
4. **CI/CD 集成** - 添加 GitHub Actions 自动构建和部署

---

## 参考资料

- **Dockerfile 最佳实践**: https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
- **Docker Compose 文档**: https://docs.docker.com/compose/
- **多阶段构建**: https://docs.docker.com/build/building/multi-stage/

---

**Last Updated**: 2026-02-10
**Status**: ✅ **IMAGES BUILT SUCCESSFULLY**
**Note**: 完整部署测试需要停止现有运行的服务

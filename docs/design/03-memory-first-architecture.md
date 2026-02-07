# Memory-First 架构设计

## 文档信息

| 项目 | 内容 |
|------|------|
| **设计目标** | 基于"Memory 优先"理念的 Agent 编排平台架构 |
| **创建时间** | 2025-02-07 |
| **状态** | 设计阶段 - 深度讨论中 |
| **优先级** | P0 - 核心架构 |
| **核心理念** | Agent 是朝生暮死的容器，Memory 才是本体 |

---

## 核心洞察

### 范式转变

```
传统范式: Agent 是主体，Memory 是附属
新范式:    Memory 是本体，Agent 是临时容器

核心理念: Agent = Memory (本体) + Model (运行时) + Skills (运行时)
```

### 关键原则

| 原则 | 说明 |
|------|------|
| **Memory 本体** | 身份、经验、知识都在 Memory，Agent 只负责执行 |
| **Agent 无状态** | Agent 不维护状态，所有状态持久化到 Memory Store |
| **朝生暮死** | Agent 按需创建，用完即销毁，资源高效利用 |
| **思考-执行分离** | 推理规划与实际执行分离到不同节点 |

---

## 1. 核心概念：Memory 作为本体

### 1.1 Memory 的组成

```
┌─────────────────────────────────────────────────────────────────┐
│                      Memory (数字灵魂)                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  身份信息 (我是谁)                                       │    │
│  │  - name: "客服小王"                                       │    │
│  │  - persona: {"role": "assistant", "tone": "friendly"}     │    │
│  │  - description: "专业的技术支持助手"                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  经验记忆 (我学到了什么)                                 │    │
│  │  - conversations: 对话历史                                │    │
│  │  - skills_progress: 技能熟练度                           │    │
│  │  - knowledge: 积累的知识库                                │    │
│  │  - patterns: 学习到的模式                                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  偏好设置 (我的风格)                                     │    │
│  │  - response_style: "简洁专业"                            │    │
│  │  - tool_preferences: ["python", "git"]                   │    │
│  │  - language: "中文"                                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  关系图谱 (我认识谁)                                     │    │
│  │  - users: 交互过的用户                                   │    │
│  │  - contexts: 不同场景的上下文                             │    │
│  │  - relationships: 关系网络                                │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Memory vs Agent

```
┌─────────────────────────────────────────────────────────────────┐
│                   Memory vs Agent 对比                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  维度              │ Memory (本体)      │ Agent (容器)          │
│  ──────────────────┼───────────────────┼─────────────────────  │
│  生命周期           │ 持久化 (月/年)     │ 临时 (秒/分钟)        │
│  包含内容           │ 身份+经验+记忆     │ 执行引擎+API调用      │
│  状态管理           │ 状态的源头         │ 无状态               │
│  可替换性           │ 不可替换           │ 随时替换             │
│  成本               │ 存储成本 (低)      │ 计算成本 (高)         │
│  并发               │ 可多 Agent 共享    │ 单次执行             │
│  版本管理           │ 支持版本历史       │ 不适用               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 思考-执行分离架构

### 2.1 核心理念

**实践洞察**: 思考（Reasoning）和执行（Execution）是两回事

- **思考节点**: 负责 LLM 推理、规划、决策，需要 GPU，轻量级
- **执行节点**: 负责代码执行、命令操作、文件访问，需要高权限，重量级
- **分离优势**: 安全隔离、成本优化、弹性伸缩

### 2.2 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│              思考-执行分离架构                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           思考节点 (Reasoning Node)                       │    │
│  │           ┌─────────────────────────────────┐            │    │
│  │           │  Memory Pool (共享)            │            │    │
│  │           │  - Memory 1: "客服小王"         │            │    │
│  │           │  - Memory 2: "代码助手"        │            │    │
│  │           │  - Memory 3: "数据分析师"      │            │    │
│  │           └─────────────────────────────────┘            │    │
│  │                          ↓                                │    │
│  │           ┌─────────────────────────────────┐            │    │
│  │           │  LLM 推理引擎                  │            │    │
│  │           │  - GPT-4 / Claude / Ollama     │            │    │
│  │           │  - 规划、决策、生成响应        │            │    │
│  │           └─────────────────────────────────┘            │    │
│  │                          ↓                                │    │
│  │           指令 (JSON-RPC / gRPC)                           │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           执行节点 (Execution Node)                       │    │
│  │           ┌─────────────────────────────────┐            │    │
│  │           │  OpenInterpreter / 执行环境     │            │    │
│  │           │  - 代码执行 (Python/JS)        │            │    │
│  │           │  - Shell 命令                   │            │    │
│  │           │  - 文件操作                     │            │    │
│  │           │  - Docker 管理                  │            │    │
│  │           │  - 高权限操作 (sudo)           │            │    │
│  │           └─────────────────────────────────┘            │    │
│  │                          ↓                                │    │
│  │           结果 / 日志                                         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↑                                       │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 节点类型

```typescript
enum NodeType {
  REASONING = 'reasoning',   // 思考节点 (轻量、GPU密集)
  EXECUTION = 'execution',   // 执行节点 (重量、IO密集、高权限)
  HYBRID = 'hybrid',         // 混合节点 (低延迟场景)
}

interface ReasoningNodeConfig {
  type: NodeType.REASONING
  gpu: boolean              // 需要 GPU (LLM 推理)
  memory: number            // 内存需求 (MB)
  llmProviders: string[]    // ['openai', 'anthropic', 'ollama']
  maxMemories: number       // 最大支持的 Memory 数
}

interface ExecutionNodeConfig {
  type: NodeType.EXECUTION
  cpu: number
  memory: number
  disk: number
  permissions: {
    sudo: boolean
    filesystem: boolean
    network: boolean
    docker: boolean
  }
  runtime: 'openinterpreter' | 'e2b' | 'docker'
}

interface HybridNodeConfig {
  type: NodeType.HYBRID
  reasoning: Partial<ReasoningNodeConfig>
  execution: Partial<ExecutionNodeConfig>
  useCase: 'low-latency' | 'offline' | 'edge-computing'
}
```

---

## 3. Memory 迁移机制

### 3.1 迁移场景

```
┌─────────────────────────────────────────────────────────────────┐
│                  Memory 迁移场景                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  场景 1: 远程部署后迁移                                          │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │   Server A (思考) ──→ Server B (执行)                     │    │
│  │                                                          │    │
│  │   1. A 控制 B 部署应用                                    │    │
│  │   2. 部署完成后，Memory 迁移到 B                          │    │
│  │   3. B 变成独立节点 (本地 Memory + 本地执行)             │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  场景 2: 节点负载均衡                                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │   节点 A (高负载) ──→ 节点 B (空闲)                       │    │
│   │                                                          │    │
│  │   1. A 负载过高，需要迁移部分 Memory                      │    │
│  │   2. 选择目标节点 B                                       │    │
│  │   3. Memory 迁移到 B，新请求由 B 处理                     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  场景 3: 网络优化                                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │   远程节点 ──→ 本地节点                                   │    │
│  │                                                          │    │
│  │   1. 用户在亚洲，Memory 在美国                            │    │
│  │   2. 延迟太高，迁移到亚洲节点                             │    │
│  │   3. 降低延迟，提升体验                                   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 迁移服务接口

```typescript
interface MemoryMigrationService {
  /**
   * 完整迁移 Memory 到目标节点
   */
  migrate(
    memoryId: string,
    fromNode: string,
    toNode: string,
    options: {
      keepCopy?: boolean    // 是否在原节点保留副本
      compress?: boolean    // 是否压缩传输
    }
  ): Promise<void>

  /**
   * 增量同步 (用于长时间运行的 Memory)
   */
  sync(
    memoryId: string,
    sourceNode: string,
    targetNode: string,
    since: Timestamp
  ): Promise<void>

  /**
   * Fork Memory (创建副本)
   */
  fork(
    memoryId: string,
    targetNode: string,
    newName: string
  ): Promise<string>

  /**
   * 回滚到历史版本
   */
  rollback(
    memoryId: string,
    version: number
  ): Promise<void>
}
```

---

## 4. 任务调度策略

### 4.1 决策流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    任务调度决策树                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  接收任务                                                        │
│     ↓                                                            │
│  分析任务特征                                                    │
│  ├─ 需要实时反馈? (代码执行、交互式调试)                          │
│  ├─ 执行时长? (秒级、分钟级、小时级)                             │
│  ├─ 安全级别? (隔离要求)                                         │
│  └─ 资源需求? (GPU、CPU、磁盘)                                   │
│     ↓                                                            │
│  测量网络延迟                                                    │
│  (思考节点 ↔ 执行节点)                                           │
│     ↓                                                            │
│  ┌──────────┴──────────┐                                        │
│  ↓                     ↓                                        │
│ 延迟 < 50ms           延迟 ≥ 50ms                               │
│  │                     │                                        │
│  │                     │                                        │
│  ↓                     ↓                                        │
│ 可以分离             考虑合并                                   │
│  │                     │                                        │
│  ├─→ 分离部署         ├─→ 合并部署 (Hybrid Node)                │
│  │   思考节点在云端    │   单节点同时具备                       │
│  │   执行节点靠近资源  │   思考和执行能力                       │
│  │                                                          │    │
│  ├─→ 高安全场景       └─→ 实时交互场景                         │
│  │   必须分离 (权限隔离)                                         │
│  │                                                          │    │
│  └─→ 批处理任务                                                │
│      无需实时反馈                                                │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 任务分类

| 任务类型 | 部署模式 | 原因 | 示例 |
|----------|----------|------|------|
| **交互式编程** | Hybrid | 需要频繁代码执行和实时反馈 | 结对编程、实时调试 |
| **批处理** | Separated | 提交任务后等待结果，无需实时反馈 | 数据分析、批量转换 |
| **系统部署** | Separated | 需要高权限执行，思考节点应该无权限 | 服务器部署、CI/CD |
| **研究任务** | Separated | 主要是信息收集和分析，少量执行 | 文献调研、报告生成 |

---

## 5. 数据模型设计

### 5.1 核心表结构

```sql
-- ============================================
-- Memory (核心本体)
-- ============================================
CREATE TABLE memories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) UNIQUE NOT NULL,
  org_id BIGINT NOT NULL,
  created_by BIGINT,

  -- 身份信息
  name VARCHAR(255) NOT NULL,
  description TEXT,
  persona JSON,

  -- 核心配置
  default_model VARCHAR(50) DEFAULT 'gpt-4',
  default_skills JSON,

  -- 状态
  is_active BOOLEAN DEFAULT TRUE,
  total_interactions INT DEFAULT 0,
  last_active_at TIMESTAMP,

  -- 版本
  version INT DEFAULT 1,
  parent_memory_id VARCHAR(36),

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_org (org_id),
  INDEX idx_active (is_active)
) COMMENT='Memory - Agent 的本体';


-- ============================================
-- Memory 内容存储
-- ============================================
CREATE TABLE memory_content (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,

  content_type ENUM(
    'conversation',    -- 对话历史
    'knowledge',       -- 知识条目
    'preference',      -- 偏好设置
    'relationship',    -- 关系信息
    'context',         -- 上下文
    'skill_progress'   -- 技能进度
  ) NOT NULL,

  key VARCHAR(255),
  value TEXT NOT NULL,
  metadata JSON,

  -- 向量 embedding (用于语义搜索)
  embedding JSON,

  -- TTL
  ttl_seconds INT,
  expires_at TIMESTAMP,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_memory (memory_id),
  INDEX idx_type (content_type),
  INDEX idx_expires (expires_at)
) COMMENT='Memory 存储';


-- ============================================
-- Agent 实例 (临时容器)
-- ============================================
CREATE TABLE agents (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  agent_id VARCHAR(36) UNIQUE NOT NULL,
  memory_id VARCHAR(36) NOT NULL,

  -- 运行时配置
  model VARCHAR(50),
  skills JSON,
  api_keys JSON,

  -- 节点分配
  reasoning_node_id VARCHAR(36),
  execution_node_id VARCHAR(36),

  -- 执行信息
  status ENUM('starting', 'ready', 'busy', 'stopping', 'stopped'),
  current_task_id VARCHAR(36),

  -- 资源
  allocated_memory_mb INT,

  -- 时间戳 (生命周期很短)
  started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  stopped_at TIMESTAMP,
  uptime_seconds INT,

  INDEX idx_memory (memory_id),
  INDEX idx_status (status),
  INDEX idx_reasoning_node (reasoning_node_id),
  INDEX idx_execution_node (execution_node_id)
) COMMENT='Agent 实例 - 临时的执行容器';


-- ============================================
-- 执行节点 (含思考和执行节点)
-- ============================================
CREATE TABLE execution_nodes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  node_id VARCHAR(36) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,

  -- 节点类型
  node_type ENUM('reasoning', 'execution', 'hybrid') NOT NULL,

  -- 能力配置
  capabilities JSON,

  -- 资源配置
  resources JSON,

  -- 网络位置
  region VARCHAR(50),
  zone VARCHAR(50),
  ip_address VARCHAR(50),

  -- 状态
  status ENUM('online', 'offline', 'busy', 'maintenance') DEFAULT 'offline',
  current_load INT DEFAULT 0,

  -- 连接信息
  api_endpoint VARCHAR(255),
  auth_token VARCHAR(255),

  -- 延迟信息
  latencies JSON,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_type (node_type),
  INDEX idx_status (status)
) COMMENT='执行节点 (含思考和执行节点)';


-- ============================================
-- Memory 部署关系
-- ============================================
CREATE TABLE memory_deployments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,
  node_id VARCHAR(36) NOT NULL,

  deployment_type ENUM('primary', 'replica', 'cache') DEFAULT 'primary',

  status ENUM('deploying', 'active', 'syncing', 'inactive') DEFAULT 'deploying',

  last_sync_at TIMESTAMP,
  sync_lag_ms INT,

  bytes_stored BIGINT,

  deployed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_memory_node (memory_id, node_id),
  INDEX idx_memory (memory_id),
  INDEX idx_node (node_id)
) COMMENT='Memory 部署关系';
```

---

## 6. 容器技术选型

### 6.1 技术对比

| 容器技术 | 启动时间 | 内存占用 | 隔离级别 | 单机最大数 | 适用场景 |
|----------|----------|----------|----------|------------|----------|
| **进程级** | <10ms | ~50MB | 低 | ~200 | MVP、快速验证 |
| **Docker** | 1-5s | ~100MB | 中 | ~100 | 生产环境、成熟生态 |
| **Firecracker** | ~100ms | ~30MB | 高 | ~500 | 高并发、高安全 |
| **WASM** | <10ms | ~2MB | 高 | >10000 | 边缘计算、超大规模 |

### 6.2 推荐路线

```
阶段 1: MVP (1-2 周)
├── 进程级隔离 (multiprocessing)
└── 最大并发: 10 个 Agent

阶段 2: Beta (1-2 月)
├── Docker 容器 + 池化 (最可靠的方案)
└── 最大并发: 100 个 Agent

阶段 3: Production (3-6 月)
├── 混合架构: Docker + E2B (企业级执行环境)
├── 抽象执行层接口 (支持多种 Runtime)
└── 最大并发: 1000 个 Agent

阶段 4: Scale (长期)
├── 引入 Firecracker/WASM
└── 无限水平扩展
```

### 6.3 执行技术选型（更新）

| 技术 | 维护状态 | 可靠性 | 适用场景 | 推荐度 |
|------|----------|--------|----------|--------|
| **Docker** | ✅ 活跃 | ⭐⭐⭐⭐⭐ | 通用执行 | ⭐⭐⭐⭐⭐ |
| **Process Pool** | 自维护 | ⭐⭐⭐⭐ | 简单任务 | ⭐⭐⭐⭐ |
| **E2B** | ✅ 活跃 | ⭐⭐⭐⭐ | AI 代码执行 | ⭐⭐⭐⭐ |
| **Firecracker** | ✅ 活跃 | ⭐⭐⭐⭐ | 高安全隔离 | ⭐⭐⭐ |
| **WASM** | ✅ 活跃 | ⭐⭐⭐ | 边缘计算 | ⭐⭐⭐ |

**注意**: 不推荐依赖维护状态不明确的开源项目（如 OpenInterpreter，截至 2026-01-15 已超过 1 年未更新）

---

## 7. 核心优势

| 优势 | 说明 |
|------|------|
| **成本优化** | 思考节点集中部署 GPU 集群，执行节点按需部署 |
| **安全隔离** | 思考节点无权限，执行节点有权限，即使被攻破损失可控 |
| **弹性伸缩** | 思考能力共享 (多 Memory 共用 LLM)，执行能力按需扩展 |
| **灵活迁移** | Memory 可在不同执行节点间迁移，支持"漫游"计算 |
| **专业化** | 思考节点专注推理 (GPU)，执行节点专注操作 (IO) |
| **容错能力** | 单个执行节点挂掉，Memory 完好，可迁移继续 |

---

## 8. 相关讨论记录

### 关键决策点

1. **Memory 优先理念** (2025-02-07)
   - Agent 是临时容器，Memory 才是本体
   - 状态外部化，支持版本管理和回滚

2. **思考-执行分离** (2025-02-07)
   - 基于 OpenInterpreter 实践经验
   - 网络延迟 < 50ms 时可以分离，否则考虑合并
   - 高安全场景必须分离

3. **容器技术选择** (2025-02-07)
   - MVP 用进程级隔离 (最快验证)
   - 生产用 Docker (成熟可靠)
   - 高并发考虑 Firecracker/WASM

### 待定事项

- [ ] Memory 具体存储方案 (Redis vs Database vs Vector DB)
- [ ] 节点间通信协议 (JSON-RPC vs gRPC vs WebSocket)
- [ ] Memory 同步策略 (实时 vs 定时 vs 增量)
- [ ] 容器池化管理机制
- [ ] 成本计量和计费策略

---

## 9. 参考架构

类似理念的架构：

- **Kubernetes**: Control Plane (控制) + Worker Nodes (执行)
- **AWS Lambda**: 函数无状态，状态在外部存储
- **Docker**: 容器临时，数据卷持久化
- **Actor Model**: Actor 是地址+行为，状态在消息中

---

**文档状态**: 🟢 设计阶段 - 深度讨论中
**下一步**: 确定核心数据模型 → 设计节点通信协议 → 原型验证

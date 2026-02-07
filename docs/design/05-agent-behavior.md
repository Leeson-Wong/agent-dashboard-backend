# Agent 行为定义

## 文档信息

| 项目 | 内容 |
|------|------|
| **设计目标** | 定义 Agent 的生命周期、执行能力、工具系统 |
| **创建时间** | 2025-02-07 |
| **状态** | 设计阶段 |
| **依赖** | 基于 [Memory-First 架构](./03-memory-first-architecture.md) |

---

## 1. Agent 生命周期状态

### 1.1 状态定义

```typescript
/**
 * Agent 生命周期状态
 *
 * 定义 Agent 从创建到销毁的完整生命周期
 */
enum AgentLifecycleState {
  // 初始阶段
  CREATING = 'creating',           // 正在创建中（分配资源、加载 Memory）
  INITIALIZING = 'initializing',   // 初始化中（加载配置、建立连接）
  READY = 'ready',                 // 就绪（可以接受任务）

  // 工作阶段
  BUSY = 'busy',                   // 执行任务中
  THINKING = 'thinking',           // 思考中（LLM 推理）
  WAITING = 'waiting',             // 等待中（等待资源或用户输入）

  // 暂停/停止
  PAUSED = 'paused',             // 已暂停（资源不足或用户要求）
  STOPPING = 'stopping',           // 正在停止中
  STOPPED = 'stopped',             // 已停止（资源释放）

  // 异常状态
  ERROR = 'error',                 // 错误状态
  HANGING = 'hanging',             // 卡住（超时检测）
  ORPHANED = 'orphaned',           // 孤儿（Memory 丢失）

  // 终态
  TERMINATED = 'terminated'        // 已终止（不可恢复）
}

/**
 * 状态转换规则
 */
const STATE_TRANSITIONS: Record<AgentLifecycleState, AgentLifecycleState[]> = {
  // 初始阶段
  [AgentLifecycleState.CREATING]: [
    AgentLifecycleState.INITIALIZING,
    AgentLifecycleState.ERROR,
  ],
  [AgentLifecycleState.INITIALIZING]: [
    AgentLifecycleState.READY,
    AgentLifecycleState.ERROR,
  ],

  // 工作阶段
  [AgentLifecycleState.READY]: [
    AgentLifecycleState.BUSY,
    AgentLifecycleState.THINKING,
    AgentLifecycleState.PAUSED,
    AgentLifecycleState.STOPPING,
  ],

  [AgentLifecycleState.BUSY]: [
    AgentLifecycleState.READY,           // 任务完成
    AgentLifecycleState.THINKING,       // 需要进一步思考
    AgentLifecycleState.ERROR,         // 执行失败
    AgentLifecycleState.HANGING,       // 卡住
  ],

  [AgentLifecycleState.THINKING]: [
    AgentLifecycleState.BUSY,           // 思考完成，开始执行
    AgentLifecycleState.ERROR,         // 推理失败
    AgentLifecycleState.HANGING,       // 卡住
  ],

  [AgentLifecycleState.WAITING]: [
    AgentLifecycleState.BUSY,           // 资源就绪
    AgentLifecycleState.PAUSED,         // 被暂停
  ],

  // 暂停/停止
  [AgentLifecycleState.PAUSED]: [
    AgentLifecycleState.READY,           // 恢复
    AgentLifecycleState.STOPPING,       // 停止
  ],

  [AgentLifecycleState.STOPPING]: [
    AgentLifecycleState.STOPPED,
  ],

  // 异常处理
  [AgentLifecycleState.ERROR]: [
    AgentLifecycleState.READY,           // 恢复
    AgentLifecycleState.STOPPING,       // 无法恢复，停止
    AgentLifecycleState.TERMINATED,    // 致命错误
  ],

  [AgentLifecycleState.HANGING]: [
    AgentLifecycleState.BUSY,           // 恢复（超时重试）
    AgentLifecycleState.ERROR,         // 超时无法恢复
    AgentLifecycleState.TERMINATED,
  ],

  [AgentLifecycleState.ORPHANED]: [
    AgentLifecycleState.TERMINATED,      // 无法恢复
  ],

  // 终态
  [AgentLifecycleState.STOPPED]: [
    AgentLifecycleState.CREATING,    // 可以重新激活
    AgentLifecycleState.TERMINATED,
  ],

  [AgentLifecycleState.TERMINATED]: [
    // 不可转换
  ],
}
```

---

## 2. Agent 执行能力

### 2.1 基础操作

```typescript
/**
 * Agent 基础执行能力
 */
interface AgentCapabilities {
  // ========================================
  // 信息获取
  // ========================================
  readonly information: {
    // 读取文件
    readFile: (path: string) => Promise<string | Buffer>

    // 列出目录
    listDirectory: (path: string) => Promise<string[]>

    // 获取文件信息
    getFileInfo: (path: string) => Promise<{ size: number; modified: Date; isDirectory: boolean }>

    // 搜索文件
    searchFiles: (pattern: string, path: string) => Promise<string[]>

    // 读取环境变量
    getEnv: () => Record<string, string>

    // 系统信息
    getSystemInfo: () => Promise<{
      os: string
      arch: string
      hostname: string
      cwd: string
      memory: NodeJS.MemoryUsage
    }>
  }

  // ========================================
  // 文件操作
  // ========================================
  readonly file: {
    // 写入文件
    writeFile: (path: string, content: string | Buffer) => Promise<void>

    // 创建目录
    createDirectory: (path: string) => Promise<void>

    // 删除文件/目录
    delete: (path: string) => Promise<void>

    // 移动/重命名
    move: (from: string, to: string) => Promise<void>

    // 复制
    copy: (from: string, to: string) => Promise<void>

    // 压缩/解压
    compress: (path: string) => Promise<void>
    extract: (archive: string, to: string) => Promise<void>

    // 监控文件变化
    watchFile: (path: string, callback: FileChangeCallback) => () => void
    watchDirectory: (path: string, callback: DirectoryChangeCallback) => () => void
  }

  // ========================================
  // 代码执行
  // ========================================
  readonly execution: {
    // 执行 Python 代码
    executePython: (code: string, options?: {
      cwd?: string
      timeout?: number
      input?: string
    }) => Promise<ExecutionResult>

    // 执行 JavaScript/TypeScript 代码
    executeJavaScript: (code: string, options?: {
      cwd?: string
      timeout?: number
      nodeVersion?: string
    }) => Promise<ExecutionResult>

    // 执行 Shell 命令
    executeShell: (command: string, options?: {
      cwd?: string
      env?: Record<string, string>
      timeout?: number
      background?: boolean
    }) => Promise<ShellResult>

    // 执行 SQL 查询
    executeSQL: (query: string, database: string) => Promise<QueryResult>
  }

  // ========================================
  // 网络操作
  // ========================================
  readonly network: {
    // HTTP 请求
    httpRequest: (url: string, options: {
      method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
      headers?: Record<string, string>
      body?: any
      timeout?: number
    }) => Promise<HttpResponse>

    // WebSocket 连接
    websocketConnect: (url: string) => WebSocket

    // 上传文件
    uploadFile: (file: string | Buffer, url: string) => Promise<string>
  }

  // ========================================
  // 开发工具
  // ========================================
  readonly development: {
    // 运行测试
    runTests: (path?: string) => Promise<TestResult>

    // 代码检查
    lint: (path: string, language: string) => Promise<LintResult>

    // 格式化代码
    format: (path: string, language: string) => Promise<void>

    // 生成代码
    generateCode: (prompt: string, language: string) => Promise<string>
  }
}
```

---

## 3. 工具系统

### 3.1 工具分类

```typescript
/**
 * 工具类别定义
 */
enum ToolCategory {
  // 文件操作
  FILE_READ = 'file_read',
  FILE_WRITE = 'file_write',

  // 代码执行
  CODE_PYTHON = 'code_python',
  CODE_JAVASCRIPT = 'code_javascript',
  CODE_SHELL = 'code_shell',
  CODE_SQL = 'code_sql',

  // 网络操作
  HTTP_REQUEST = 'http_request',
  WEBSOCKET = 'websocket',

  // LLM 操作
  LLM_INFERENCE = 'llm_inference',
  EMBEDDING = 'embedding',

  // 数据操作
  VECTOR_SEARCH = 'vector_search',
  DATABASE_QUERY = 'database_query',

  // 开发工具
  RUN_TESTS = 'run_tests',
  LINT = 'lint',
  FORMAT = 'format',
}

/**
 * 工具定义
 */
interface ToolDefinition {
  id: string                    // 工具唯一标识
  name: string                  // 工具名称
  category: ToolCategory        // 类别
  description: string           // 描述

  // 参数定义
  parameters: {
    name: string
    type: 'string' | 'number' | 'boolean' | 'object' | 'array'
    required: boolean
    description: string
    default?: any
  }[]

  // 返回值定义
  returns: {
    type: string
    description: string
  }

  // 执行配置
  config: {
    timeout?: number           // 超时时间（毫秒）
    dangerous?: boolean        // 是否危险操作
    requireConfirmation?: boolean  // 是否需要确认
    sandbox?: 'docker' | 'process' | 'none'  // 沙箱类型
  }

  // 权限要求
  permissions?: {
    allowPaths?: string[]      // 允许访问的路径
    denyPaths?: string[]       // 禁止访问的路径
    allowDomains?: string[]    // 允许访问的域名
    maxResources?: {           // 资源限制
      cpu?: number
      memory?: number
      disk?: number
    }
  }
}
```

### 3.2 内置工具清单

| 工具 ID | 名称 | 类别 | 危险等级 | 说明 |
|---------|------|------|----------|------|
| `file.read` | 读取文件 | FILE_READ | 安全 | 读取文件内容 |
| `file.write` | 写入文件 | FILE_WRITE | 中等 | 写入文件（覆盖） |
| `file.delete` | 删除文件 | FILE_WRITE | 危险 | 删除文件/目录 |
| `file.list` | 列出目录 | FILE_READ | 安全 | 列出目录内容 |
| `file.search` | 搜索文件 | FILE_READ | 安全 | 按模式搜索文件 |
| `code.python` | 执行 Python | CODE_PYTHON | 中等 | 在沙箱中执行 Python 代码 |
| `code.node` | 执行 JavaScript | CODE_JAVASCRIPT | 中等 | 在沙箱中执行 JS 代码 |
| `code.shell` | 执行 Shell | CODE_SHELL | 危险 | 执行 Shell 命令 |
| `code.sql` | 执行 SQL | CODE_SQL | 中等 | 执行数据库查询 |
| `net.http` | HTTP 请求 | HTTP_REQUEST | 安全 | 发起 HTTP 请求 |
| `llm.chat` | LLM 对话 | LLM_INFERENCE | 安全 | 调用 LLM 推理 |
| `vector.search` | 向量搜索 | VECTOR_SEARCH | 安全 | 语义搜索 |
| `dev.test` | 运行测试 | RUN_TESTS | 安全 | 运行项目测试 |
| `dev.lint` | 代码检查 | LINT | 安全 | 静态代码分析 |

---

## 4. 行为模式与自主级别

### 4.1 自主级别定义

```typescript
/**
 * Agent 自主级别
 */
enum AutonomyLevel {
  MANUAL = 'manual',                   // 手动模式：所有操作需要人工确认
  SEMI_AUTONOMOUS = 'semi_autonomous', // 半自动：只确认危险操作
  AUTONOMOUS = 'autonomous',           // 自动：在白名单内自主决策
  FULLY_AUTONOMOUS = 'fully_autonomous' // 完全自动：完全自主，仅汇报
}

/**
 * 行为配置
 */
interface BehaviorConfig {
  autonomyLevel: AutonomyLevel

  // 确认规则
  confirmRules: {
    dangerousOps: boolean        // 危险操作是否确认
    fileWrite: boolean           // 文件写入是否确认
    networkRequest: boolean      // 网络请求是否确认
    resourceUsage: boolean       // 资源使用是否确认
  }

  // 超时配置
  timeouts: {
    singleTool: number           // 单个工具超时（毫秒）
    totalTask: number            // 整个任务超时（毫秒）
    thinking: number             // 思考超时（毫秒）
  }

  // 重试策略
  retryPolicy: {
    maxAttempts: number
    backoffMs: number
    retryableErrors: string[]
  }

  // 资源限制
  resourceLimits: {
    maxTokensPerTask: number
    maxMemoryMb: number
    maxCpuPercent: number
    maxNetworkRequests: number
  }
}
```

### 4.2 各级别行为特征

| 自主级别 | 确认要求 | 适用场景 | 示例 |
|----------|----------|----------|------|
| **MANUAL** | 所有操作都需确认 | 学习 Agent、调试模式 | 演示、教学 |
| **SEMI_AUTONOMOUS** | 只确认危险操作 | 日常任务执行 | 代码重构、数据分析 |
| **AUTONOMOUS** | 白名单内自主，其他确认 | 受限环境自动化 | 自动化测试、定时任务 |
| **FULLY_AUTONOMOUS** | 完全自主，仅汇报结果 | 可信环境、内部工具 | 后台服务、数据处理 |

---

## 5. 工具使用流程

### 5.1 标准执行流程

```typescript
/**
 * 工具执行流程
 */
class ToolExecutor {
  /**
   * 执行工具的完整流程
   */
  async executeTool(
    toolId: string,
    parameters: Record<string, any>,
    context: ExecutionContext
  ): Promise<ToolResult> {
    // 1. 工具识别
    const tool = await this.identifyTool(toolId)

    // 2. 权限检查
    await this.checkPermission(tool, context)

    // 3. 参数校验
    const validatedParams = this.validateParameters(tool, parameters)

    // 4. 风险评估
    const risk = await this.assessRisk(tool, validatedParams, context)
    if (risk.level === 'high' && context.requireConfirmation) {
      const approved = await this.requestConfirmation(tool, risk)
      if (!approved) {
        return { status: 'cancelled', reason: 'User declined' }
      }
    }

    // 5. 资源检查
    await this.checkResources(context)

    // 6. 执行前钩子
    await this.beforeExecution(tool, validatedParams)

    // 7. 实际执行
    const result = await this.executeWithTimeout(
      tool,
      validatedParams,
      context.config.timeouts.singleTool
    )

    // 8. 执行后钩子
    await this.afterExecution(tool, result)

    // 9. 结果处理
    return this.processResult(result)
  }
}
```

### 5.2 流程图

```
┌─────────────┐
│ 1. 识别工具  │
└──────┬──────┘
       ▼
┌─────────────┐
│ 2. 权限检查  │ ──> 权限不足 ──> 拒绝执行
└──────┬──────┘
       ▼
┌─────────────┐
│ 3. 参数校验  │ ──> 参数错误 ──> 返回错误
└──────┬──────┘
       ▼
┌─────────────┐
│ 4. 风险评估  │ ──> 高风险且需确认 ──> 等待用户
└──────┬──────┘           │
       ▼                  ├──> 拒绝 ──> 取消执行
       │                  └──> 同意
┌─────────────┐
│ 5. 资源检查  │ ──> 资源不足 ──> 等待/拒绝
└──────┬──────┘
       ▼
┌─────────────┐
│ 6. 执行工具  │
└──────┬──────┘
       ▼
┌─────────────┐
│ 7. 记录结果  │
└──────┬──────┘
       ▼
┌─────────────┐
│ 8. 返回结果  │
└─────────────┘
```

---

## 6. 安全策略

### 6.1 沙箱隔离

```typescript
/**
 * 沙箱配置
 */
interface SandboxConfig {
  type: 'docker' | 'process' | 'firecracker' | 'e2b'

  // 资源限制
  limits: {
    cpu: number              // CPU 核心数（0.5 = 50%）
    memory: number           // 内存限制（MB）
    disk: number             // 磁盘限制（MB）
    network: boolean         // 是否允许网络
  }

  // 挂载配置
  mounts: {
    source: string           // 源路径
    target: string           // 目标路径（容器内）
    readOnly: boolean        // 是否只读
  }[]

  // 环境变量
  env: Record<string, string>
}
```

### 6.2 危险操作防护

| 操作类型 | 危险等级 | 防护措施 |
|----------|----------|----------|
| 删除文件/目录 | 🔴 高 | 必须确认，显示完整路径，支持二次确认 |
| 执行 Shell 命令 | 🔴 高 | 显示完整命令，禁用特定命令（rm -rf /） |
| 网络请求 | 🟡 中 | 显示 URL，限制可访问域名 |
| 文件写入 | 🟡 中 | 显示写入位置和大小，防止覆盖重要文件 |
| 代码执行 | 🟡 中 | 沙箱隔离，资源限制 |
| LLM 调用 | 🟢 低 | 仅记录使用情况 |

### 6.3 白名单/黑名单机制

```typescript
/**
 * 访问控制配置
 */
interface AccessControl {
  // 工具白名单/黑名单
  tools: {
    allow?: string[]          // 允许使用的工具 ID
    deny?: string[]           // 禁止使用的工具 ID
  }

  // 路径白名单/黑名单
  paths: {
    allow?: string[]          // 允许访问的路径（支持通配符）
    deny?: string[]           // 禁止访问的路径
    allowReadOnly?: string[]  // 只读访问的路径
  }

  // 域名白名单/黑名单
  domains: {
    allow?: string[]          // 允许访问的域名
    deny?: string[]           // 禁止访问的域名
  }

  // 验证函数
  checkAccess(toolId: string, target: string): boolean
}
```

---

## 7. 与 Memory 的集成

### 7.1 工具使用记录

每次工具使用都会记录到 Memory：

```typescript
/**
 * 工具使用记录
 */
interface ToolUsageRecord {
  toolId: string
  timestamp: Date
  parameters: Record<string, any>
  result: any
  success: boolean
  executionTime: number

  // 反馈
  feedback?: {
    rating: number            // 1-5 评分
    notes: string             // 备注
  }
}

// 存储到 Memory 的短期记忆
memory.shortTerm.workingMemory.toolUsages.push(record)
```

### 7.2 经验提取

从工具使用中提取经验：

```typescript
/**
 * 从工具使用中提取经验
 */
async function extractExperience(memory: Memory, usage: ToolUsageRecord): Promise<void> {
  if (!usage.success) {
    // 失败经验：记录错误模式
    memory.longTerm.experiences.push({
      type: 'tool_failure',
      toolId: usage.toolId,
      error: usage.result.error,
      lesson: `避免在 ${usage.parameters.context} 中使用 ${usage.toolId}`,
      timestamp: new Date()
    })
  } else {
    // 成功经验：记录使用模式
    memory.longTerm.experiences.push({
      type: 'tool_success',
      toolId: usage.toolId,
      context: usage.parameters,
      outcome: usage.result,
      timestamp: new Date()
    })

    // 更新技能熟练度
    const skill = memory.longTerm.skills.get(usage.toolId)
    if (skill) {
      skill.proficiency = Math.min(1.0, skill.proficiency + 0.01)
      skill.practiceCount++
      skill.lastUsedAt = new Date()
    }
  }
}
```

### 7.3 工具熟练度管理

```typescript
/**
 * 工具熟练度
 */
interface SkillProficiency {
  toolId: string
  proficiency: number        // 0.0 - 1.0
  practiceCount: number      // 使用次数
  successRate: number        // 成功率
  lastUsedAt: Date
  averageExecutionTime: number

  // 练习轨迹
  practiceHistory: {
    timestamp: Date
    success: boolean
    executionTime: number
  }[]
}

// 存储在 Memory 的长期记忆中
memory.longTerm.skills.set(toolId, skillProficiency)
```

---

## 8. 数据模型

### 8.1 Agent 执行记录表

```sql
-- ============================================
-- Agent 执行记录
-- ============================================
CREATE TABLE agent_executions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  execution_id VARCHAR(36) UNIQUE NOT NULL,
  agent_id VARCHAR(36) NOT NULL,
  memory_id VARCHAR(36) NOT NULL,

  -- 任务信息
  task_id VARCHAR(36),
  task_type VARCHAR(100),
  task_description TEXT,

  -- 执行信息
  tool_id VARCHAR(100),
  tool_name VARCHAR(255),
  tool_category VARCHAR(50),

  -- 输入
  input JSON,

  -- 输出
  output JSON,
  success BOOLEAN,
  exit_code INT,

  -- 资源使用
  execution_time_ms INT,
  memory_used_mb DECIMAL(10, 2),
  tokens_used INT,

  -- 状态
  status ENUM('pending', 'running', 'completed', 'failed', 'cancelled') DEFAULT 'pending',

  -- 时间戳
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  started_at TIMESTAMP,
  completed_at TIMESTAMP,

  INDEX idx_agent (agent_id),
  INDEX idx_memory (memory_id),
  INDEX idx_tool (tool_id),
  INDEX idx_status (status),
  INDEX idx_created (created_at)
) COMMENT='Agent 执行记录';


-- ============================================
-- 工具使用统计
-- ============================================
CREATE TABLE tool_usage_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tool_id VARCHAR(100) NOT NULL,
  memory_id VARCHAR(36) NOT NULL,

  -- 统计
  total_uses BIGINT DEFAULT 0,
  successful_uses BIGINT DEFAULT 0,
  failed_uses BIGINT DEFAULT 0,

  -- 熟练度
  proficiency_level DECIMAL(3,2) DEFAULT 0,
  total_practice_time BIGINT DEFAULT 0,

  -- 最近使用
  last_used_at TIMESTAMP,
  last_success_at TIMESTAMP,

  -- 时间戳
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_tool_memory (tool_id, memory_id),
  INDEX idx_memory (memory_id),
  INDEX idx_proficiency (proficiency_level)
) COMMENT='工具使用统计';


-- ============================================
-- 工具权限配置
-- ============================================
CREATE TABLE tool_permissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  agent_id VARCHAR(36) NOT NULL,

  -- 白名单
  allowed_tools JSON,
  allowed_paths JSON,
  allowed_domains JSON,

  -- 黑名单
  forbidden_tools JSON,
  forbidden_paths JSON,

  -- 权限级别
  permission_level ENUM('basic', 'standard', 'admin') DEFAULT 'basic',

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_agent (agent_id)
) COMMENT='Agent 工具权限配置';
```

---

**文档状态**: 🟢 设计阶段
**相关文档**:
- [Memory-First 架构设计](./03-memory-first-architecture.md)
- [Memory 管理系统](./04-memory-management.md)

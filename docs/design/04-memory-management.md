# Memory 管理系统设计

## 文档信息

| 项目 | 内容 |
|------|------|
| **设计目标** | Memory 作为 AI 本体的完整生命周期管理系统 |
| **创建时间** | 2025-02-07 |
| **状态** | 设计阶段 - 深度讨论中 |
| **优先级** | P0 - 核心功能 |
| **核心理念** | Memory 是真实的 AI，管理其过去、现在、未来的时间感知 |

---

## 核心概念

### Memory = 真实的 AI

```
┌─────────────────────────────────────────────────────────────────┐
│                    Memory 作为 AI 本体                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Memory 不是一个简单的数据存储，而是一个：                         │
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐    │
│   │  有记忆的实体                                              │    │
│   │  - 记得过去做过什么（经验）                               │    │
│   │  - 知道现在在做什么（短期记忆）                           │    │
│   │  - 理解世界是什么样（知识）                               │    │
│   │  - 知道自己是谁（身份）                                   │    │
│   │  - 感知时间流逝（时间感知）                               │    │
│   └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│   每次激活 Agent 时，实际上是在"唤醒"这个 AI 实体：                │
│   ┌─────────────────────────────────────────────────────────┐    │
│   │  激活流程:                                                │    │
│   │  1. 加载身份（我是谁）                                   │    │
│   │  2. 加载经验（我学到了什么）                             │    │
│   │  3. 加载知识（我知道什么）                               │    │
│   │  4. 加载上下文（我在做什么）                             │    │
│   │  5. 应用时间补丁（更新过时知识）                         │    │
│   │  6. 开始执行任务                                        │    │
│   └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1. Memory 分层架构

### 1.1 四层模型

```
┌─────────────────────────────────────────────────────────────────┐
│                    Memory 分层结构                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐    │
│   │  第一层：核心身份                     │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  • ID: 唯一标识                                        │    │    │
│   │  │  • 名字: "客服小王"                                   │    │    │
│   │  │  • 角色: 技术支持助手                                  │    │    │
│   │  │  • 人格: {语气: "友好", 风格: "简洁"}                   │    │    │
│   │  │  • 目标: 帮助用户解决技术问题                          │    │    │
│   │  │  • 价值观: {优先级: [安全, 效率, 质量]}               │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  特点: 很少变化，定义了 Memory 的本质                      │    │
│   └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│   ┌─────────────────────────────────────────────────────────┐    │
│   │  第二层：长期记忆                       │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  经验记忆 "我曾经做过什么"                           │    │    │
│   │  │  ┌─────────────────────────────────────────┐    │    │    │
│   │  │  │  • 任务: 实现 Web Scraper                │    │    │    │
│   │  │  │  • 结果: 成功                                │    │    │    │
│   │  │  │  • 学到: BeautifulSoup, requests        │    │    │    │
│   │  │  │  • 教训: 先检查 robots.txt              │    │    │    │
│   │  │  └─────────────────────────────────────────┘    │    │    │
│   │  │  ┌─────────────────────────────────────────┐    │    │    │
│   │  │  │  任务: 调试 API 问题                      │    │    │    │
│   │  │  │  • 结果: 失败（超时）                        │    │    │    │
│   │  │  │  • 学到: 添加重试逻辑                        │    │    │    │
│   │  │  │  • 教训: 检查网络连接                       │    │    │    │
│   │  │  └─────────────────────────────────────────┘    │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  知识记忆 "我知道什么"                              │    │    │
│   │  │  • 领域知识: Python, Docker, Git                  │    │    │
│   │  │  • 世界知识: 公司架构, 产品信息                    │    │    │
│   │  │  • 用户偏好: 张三喜欢详细解释, 李四喜欢简洁        │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  技能记忆 "我会什么"                                │    │    │
│   │  │  • Python 编程: 熟练度 8/10                       │    │    │
│   │  │  • Docker 部署: 熟练度 6/10                        │    │    │
│   │  │  • 前端开发: 熟练度 4/10                            │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  特点: 压缩存储，按需加载，可以积累                       │    │
│   └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│   ┌─────────────────────────────────────────────────────────┐    │
│   │  第三层：短期记忆                       │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  当前会话 "我现在在做什么"                           │    │    │
│   │  │  • 对话历史: [用户: "...", AI: "...", ...]          │    │    │
│   │  │  • 当前话题: "调试 API 问题"                        │    │    │
│   │  │  • 上下文: API endpoint, 错误信息等                   │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  工作记忆 "当前任务的临时状态"                       │    │    │
│   │  │  • 当前任务: 找到 API 500 的原因                    │    │    │
│   │  │  • 当前进度: 第 3 步 / 共 7 步                      │    │    │
│   │  │  • 临时变量: endpoint, last_error, 等等               │    │    │
│   │  │  • 临时文件: /tmp/api_test.py                         │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  特点: 快速访问，任务结束后清理或归档                       │    │
│   └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│   ┌─────────────────────────────────────────────────────────┐    │
│   │  第四层：时间补丁                    │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  模型训练时间: 2023-12 (知识截止日期)                 │    │    │
│   │  │  真实世界时间: 2026-02 (当前时间)                     │    │    │
│   │  │  → 时间差: 14 个月                                     │    │    │
│   │  │  → 需要补丁: 2024、2025 发生的技术、政策变化          │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  ┌─────────────────────────────────────────────────┐    │    │
│   │  │  补丁示例:                                            │    │
│   │  │  • 技术: Python 3.12/3.13 已发布                     │    │    │
│   │  │  • 政策: GDPR 新增条款                              │    │    │
│   │  │  • 事件: 公司收购、产品停服                          │    │    │
│   │  │  • 纠正: 用户告知的过时信息                           │    │    │
│   │  └─────────────────────────────────────────────────┘    │    │
│   │  特点: 动态更新，模型时间越早，补丁越多                     │    │
│   └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 记忆类型对比

| 记忆类型 | 存储时长 | 容量 | 访问频率 | 更新频率 | 持久化 |
|----------|----------|------|----------|----------|--------|
| **核心身份** | 永久 | 小 | 每次激活 | 极少 | 是 |
| **长期记忆** | 月/年 | 大 | 按需 | 任务完成时 | 是 |
| **短期记忆** | 分钟/小时 | 小 | 实时 | 持续 | 是 |
| **工作记忆** | 秒/分钟 | 极小 | 实时 | 极快 | 临时 |
| **时间补丁** | 动态 | 中 | 每次激活 | 定期 | 是 |

---

## 2. 经验提取与总结

### 2.1 经验记忆结构

```typescript
interface Experience {
  id: string
  memoryId: string

  // 什么任务
  task: {
    type: string          // 'coding', 'debugging', 'analysis', 'deployment'
    description: string
    complexity: number     // 1-10, 复杂度评分
    domain: string[]       // ['python', 'api', 'database']
  }

  // 怎么做的
  approach: {
    tools: string[]       // ['python', 'git', 'docker']
    methods: string[]     // ['trial_and_error', 'research', 'divide_conquer']
    timeSpent: number     // 分钟
    iterations: number    // 尝试次数
  }

  // 结果如何
  outcome: {
    success: boolean
    quality: number       // 1-10, 完成质量评分
    efficiency: number    // 1-10, 效率评分
    userSatisfaction?: number // 1-5, 用户满意度
  }

  // 学到了什么
  learning: {
    skillsAcquired: string[]     // ['beautifulsoup', 'requests']
    lessonsLearned: string[]      // ['先检查 robots.txt', '添加 User-Agent']
    mistakes: string[]            // ['忘记处理异常', '硬编码了路径']
    bestPractices: string[]       // ['使用 session 管理器']
  }

  // 关联信息
  associations: {
    relatedUsers: string[]   // 参与的用户
    relatedTasks: string[]  // 相似的任务 ID
    relatedContext: string[] // 相关的上下文标签
  }

  // 元数据
  timestamp: Date
  version: number         // Memory 的版本（可追溯）

  // 向量 embedding（用于语义搜索）
  embedding?: number[]
}
```

### 2.2 经验提取流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    经验提取流程                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  任务完成后                                                       │
│     ↓                                                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  1. 收集数据                                               │    │
│  │     • 任务元数据 (类型、描述、复杂度)                      │    │
│  │     • 执行过程 (使用的工具、方法、迭代次数)                 │    │
│  │     • 执行结果 (成功/失败、质量、耗时)                      │    │
│  │     • 用户反馈 (满意度、评价)                              │    │
│  └────────────────────┬────────────────────────────────────┘    │
│                        ↓                                        │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  2. 分析学习                                              │    │
│  │     • 新使用的技能?                                       │    │
│  │     • 遇到的问题和解决方法?                               │    │
│  │     • 可以改进的地方?                                     │    │
│  │     • 可以复用的模式?                                     │    │
│  └────────────────────┬────────────────────────────────────┘    │
│                        ↓                                        │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  3. 生成经验条目                                         │    │
│  │     {                                                   │    │
│  │       type: 'success',                                   │    │
│  │       task: { type: 'coding', description: '...' },     │    │
│  │       learning: {                                       │    │
│  │         skillsAcquired: ['bs4', 'requests'],           │    │
│  │         lessonsLearned: ['检查 robots.txt'],          │    │
│  │       }                                                  │    │
│  │     }                                                   │    │
│  └────────────────────┬────────────────────────────────────┘    │
│                        ↓                                        │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  4. 存储到长期记忆                                       │    │
│  │     • 保存到 memory_experiences 表                       │    │
│  │     • 生成向量 embedding                                 │    │
│  │     • 建立索引 (类型、领域、标签)                         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 经验检索与复用

```typescript
class ExperienceManager {
  /**
   * 检索相关经验
   */
  async retrieveRelevant(
    query: string,
    context: {
      taskType: string
      domain?: string[]
      complexity?: number
    }
  ): Promise<Experience[]> {
    // 1. 向量搜索（语义相似）
    const semanticResults = await this.vectorSearch(
      query,
      { topK: 10 }
    )

    // 2. 过滤（按类型、领域）
    const filtered = semanticResults.filter(exp => {
      if (exp.task.type === context.taskType) return true
      if (context.domain && exp.domain.some(d => context.domain.includes(d))) return true
      return false
    })

    // 3. 排序（相关性 + 成功率）
    const ranked = filtered.sort((a, b) => {
      const scoreA = this.calculateScore(a, context)
      const scoreB = this.calculateScore(b, context)
      return scoreB - scoreA
    })

    // 4. 返回前 N 条
    return ranked.slice(0, 5)
  }

  /**
   * 计算经验相关性得分
   */
  private calculateScore(exp: Experience, context: any): number {
    let score = 0

    // 任务类型匹配
    if (exp.task.type === context.taskType) score += 0.3

    // 领域匹配
    if (context.domain) {
      const overlap = exp.domain.filter(d => context.domain.includes(d)).length
      score += (overlap / exp.domain.length) * 0.3
    }

    // 复杂度匹配（偏好类似复杂度的经验）
    if (context.complexity) {
      const diff = Math.abs(exp.task.complexity - context.complexity)
      score += Math.max(0, 0.4 - diff * 0.1)
    }

    // 成功经验优先
    if (exp.outcome.success) score += 0.2

    return score
  }
}
```

---

## 3. 知识更新机制

### 3.1 知识来源

```
┌─────────────────────────────────────────────────────────────────┐
│                    知识更新来源                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  来源 1: 用户明确告知                                     │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  用户: "我们公司的 API 端点是 api.example.com"    │    │    │
│  │  │  ↓                                               │    │    │
│  │  │  记录: {                                         │    │    │
│  │  │    type: "fact",                                 │    │    │
│  │  │    content: "公司 API 端点是 api.example.com",   │    │    │
│  │  │    source: { type: "user", user: "张三" },        │    │    │
│  │  │    confidence: 0.9                               │    │    │
│  │  │  }                                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  来源 2: 从对话中推断                                     │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  对话片段:                                        │    │    │
│  │  │  用户: "为什么这个接口总是超时?"                   │    │    │
│  │  │  AI: "让我检查一下..."                          │    │    │
│  │  │  (AI 发现接口响应时间 > 30s)                      │    │    │
│  │  │  ↓                                               │    │    │
│  │  │  记录: {                                         │    │    │
│  │  │    type: "preference",                            │    │    │
│  │  │    content: "API 响应时间较慢，约 30s",           │    │    │
│  │  │    source: { type: "observation" },              │    │    │
│  │  │    confidence: 0.5                               │    │    │
│  │  │  }                                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  来源 3: 从执行中观察                                     │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  观察: 运行 npm install 时经常卡住在某个包         │    │    │
│  │  │  ↓                                               │    │    │
│  │  │  记录: {                                         │    │    │
│  │  │    type: "procedure",                              │    │    │
│  │  │    content: "安装 package-x 时跳过 postinstall    │    │    │
│  │  │             可以加快速度",                          │    │    │
│  │  │    source: { type: "observation" },              │    │    │
│  │  │    confidence: 0.7                               │    │    │
│  │  │  }                                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 知识更新策略

```typescript
class KnowledgeManager {
  /**
   * 处理新知识
   */
  async processUpdate(update: KnowledgeUpdate): Promise<void> {
    // 1. 检查是否与现有知识冲突
    const existing = await this.findExisting(update.content)

    if (existing) {
      // 2. 有冲突 → 解决冲突
      const resolved = await this.resolveConflict(existing, update)
      await this.save(resolved)
    } else {
      // 3. 新知识 → 直接保存
      await this.save(update)
    }

    // 4. 验证知识（如果可以）
    if (this.canVerify(update)) {
      const verified = await this.verify(update)
      if (!verified) {
        await this.markAsUnverified(update.id)
      }
    }
  }

  /**
   * 解决知识冲突
   */
  async resolveConflict(old: KnowledgeUpdate, new: KnowledgeUpdate): Promise<KnowledgeUpdate> {
    // 策略 1: 更新的信息优先
    if (new.timestamp > old.timestamp) {
      return { ...new, confidence: Math.min(old.confidence + 0.1, 1) }
    }

    // 策略 2: 用户明确告知的优先
    if (new.source.type === 'user' && old.source.type !== 'user') {
      return new
    }

    // 策略 3: 合并（融合两者）
    return {
      ...old,
      content: this.mergeContent(old.content, new.content),
      confidence: (old.confidence + new.confidence) / 2,
      source: {
        type: 'merged',
        sources: [old.source, new.source],
      }
    }
  }

  /**
   * 定期清理过期知识
   */
  async cleanup(): Promise<void> {
    const now = Date.now()

    // 删除过期的知识
    await this.db('memory_knowledge')
      .where('expires_at', '<', new Date(now))
      .delete()

    // 降低长期未验证的知识的置信度
    await this.db('memory_knowledge')
      .where('verified', false)
      .where('updated_at', '<', new Date(now - 30 * 24 * 60 * 60 * 1000))
      .update('confidence', db.raw('confidence * 0.9'))
  }
}
```

---

## 4. 工作记忆管理

### 4.1 工作记忆结构

```typescript
interface WorkingMemory {
  // 当前任务
  currentTask?: {
    id: string
    goal: string
    steps: Array<{
      description: string
      completed: boolean
      result?: any
    }>
    currentStep: number
  }

  // 上下文变量
  context: Map<string, {
    value: any
    expiresAt?: number
  }>

  // 临时文件
  temporaryFiles: Array<{
    path: string
    purpose: string
    created: number
  }>

  // 思考链（用于复杂推理）
  thoughtChain?: {
    steps: Array<{
      thought: string
      timestamp: number
    }>
    current: number
  }

  // 临时状态
  temporaryState: Record<string, any>
}
```

### 4.2 工作记忆生命周期

```
┌─────────────────────────────────────────────────────────────────┐
│                  工作记忆生命周期                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  任务开始                                                        │
│     ↓                                                             │
│  初始化工作记忆                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  workingMemory = {                                     │    │
│  │    currentTask: { id, goal, steps, currentStep: 0 },   │    │
│  │    context: new Map(),                                  │    │
│  │    temporaryFiles: [],                                │    │
│  │  }                                                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  执行任务中                                                       │
│  ├─ 添加上下文变量: workingMemory.context.set('apiKey', '...')│    │
│  ├─ 创建临时文件: workingMemory.temporaryFiles.push(...)    │    │
│  ├─ 更新任务进度: workingMemory.currentTask.currentStep++      │    │
│  └─ 记录思考: workingMemory.thoughtChain.steps.push(...)     │    │
│                          ↓                                       │
│  定期清理                                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  • 删除过期的上下文变量                                   │    │
│  │  • 清理不再需要的临时文件                                 │    │
│  │  • 压缩过长的思考链                                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                          ↓                                       │
│  任务完成                                                        │
│  ├─ 提取经验到长期记忆                                         │
│  ├─ 清理临时文件                                               │
│  └─ 重置工作记忆                                               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. 时间感知与补丁系统

### 5.1 时间差问题

```
┌─────────────────────────────────────────────────────────────────┐
│                模型时间 vs 真实世界时间                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  模型知识截止日期: 2023-12-01                                  │
│  真实世界当前日期: 2026-02-07                                  │
│  时间差: ~800 天                                                │
│                                                                  │
│  在这段时间里可能发生:                                          │
│  • 新技术发布 (Python 3.12, GPT-5, React 19)                  │
│  • 政策法规变化 (GDPR 修正, 数据出境新规)                   │
│  • 公司变动 (收购、倒闭、产品停服)                          │
│  • 世界事件 (奥运会、战争、疫情)                              │
│  • 用户变化 (换工作、晋升、离职)                             │
│                                                                  │
│  如果没有补丁，AI 可能会:                                      │
│  • 使用已废弃的 API                                           │
│  • 给出过时的建议                                             │
│  • 提及已停服的服务                                           │
│  • 不知道重要的新特性                                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 补丁类型

```typescript
interface WorldPatch {
  id: string
  memoryId: string

  // 补丁类型
  type: 'technology' | 'policy' | 'event' | 'correction'

  // 描述
  description: string

  // 发生时间
  eventDate: Date

  // 影响的知识领域
  affectedDomains: string[]  // ['python', 'api', 'security']

  // 补丁内容
  patch: {
    // 要删除的过时知识
    remove: string[]

    // 要添加的新知识
    add: string[]

    // 要更新的知识
    update: Array<{
      old: string
      new: string
    }>
  }

  // 置信度
  confidence: number

  // 来源
  source: {
    type: 'user' | 'system' | 'api'
    url?: string
    providedBy?: string
  }

  // 元数据
  createdAt: Date
  applied: boolean
}
```

### 5.3 补丁生成策略

```
┌─────────────────────────────────────────────────────────────────┐
│                   补丁生成策略                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  策略 1: 自动生成 (系统定期)                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  每月检查:                                               │    │
│  │  • 技术新闻 API (Hacker News, TechCrunch)                │    │
│  │  • Python/Node/JS 发布日志                                │    │
│  │  • CVE 安全数据库                                        │    │
│  │  • 生成候选补丁 → 用户确认                               │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  策略 2: 用户告知 (实时)                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  用户说: "Python 3.13 已经发布了"                      │    │
│  │  ↓                                                       │    │
│  │  系统检测: 模型不知道 Python 3.13                        │    │
│  │  ↓                                                       │    │
│  │  生成补丁:                                               │    │
│  │  {                                                      │    │
│  │    type: "technology",                                  │    │
│  │    description: "Python 3.13 发布",                     │    │
│  │    patch: {                                           │    │
│  │      add: ["Python 最新版本是 3.13"],                  │    │
│  │    }                                                   │    │
│  │  }                                                      │    │
│  │  ↓                                                       │    │
│  │  直接应用 (confidence = 1.0, 来源 = user)              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  策略 3: 从对话中推断                                           │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  AI: "你应该使用 TypeScript 5.0 的功能"                   │    │
│  │  User: "实际上 TypeScript 5.3 才是最新的"                │    │
│  │  ↓                                                       │    │
│  │  生成补丁:                                               │    │
│  │  {                                                      │    │
│  │    type: "correction",                                  │    │
│  │    patch: {                                           │    │
│  │      update: [                                        │    │
│  │        { old: "TypeScript 5.0", new: "TypeScript 5.3" },│    │
│  │      ]                                                 │    │
│  │    }                                                   │    │
│  │  }                                                      │    │
│  │  ↓                                                       │    │
│  │  保存到 temporalPatch (confidence = 0.8, 来源 = 用户纠正) │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.4 补丁应用时机

```typescript
class PatchManager {
  /**
   * 激活时应用补丁
   */
  async applyPatchesOnActivate(
    memory: Memory,
    context: {
      modelTrainingDate: Date
      modelCapabilities: string[]
    }
  ): Promise<Memory> {
    // 1. 获取所有补丁
    const patches = await this.db('memory_temporal_patches')
      .where('memory_id', memory.id)
      .where('event_date', '>', context.modelTrainingDate)
      .orderBy('event_date', 'desc')
      .select()

    // 2. 过滤适用的补丁
    const applicable = this.filterApplicable(patches, context)

    // 3. 按优先级排序
    const sorted = this.sortByPriority(applicable)

    // 4. 应用到系统提示词
    memory.systemPrompt = this.applyToPrompt(memory.systemPrompt, sorted)

    return memory
  }

  /**
   * 将补丁应用到系统提示词
   */
  private applyToPrompt(basePrompt: string, patches: WorldPatch[]): string {
    let patched = basePrompt
    const appliedPatches: WorldPatch[] = []

    for (const patch of patches) {
      // 移除过时知识
      for (const old of patch.patch.remove) {
        if (patched.includes(old)) {
          patched = patched.replace(old, '')
          appliedPatches.push(patch)
        }
      }

      // 添加新知识
      if (patch.patch.add.length > 0) {
        patched += '\n\n' + patch.patch.add.join('\n')
        appliedPatches.push(patch)
      }

      // 更新知识
      for (const update of patch.patch.update) {
        patched = patched.replace(update.old, update.new)
        appliedPatches.push(patch)
      }
    }

    // 添加补丁摘要
    if (appliedPatches.length > 0) {
      patched += '\n\n' + this.generatePatchSummary(appliedPatches)
    }

    return patched
  }
}
```

---

## 6. 激活时携带策略

### 6.1 携带决策树

```
┌─────────────────────────────────────────────────────────────────┐
│                  激活时携带什么记忆？                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  激活请求                                                        │
│     ↓                                                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  分析任务特征                                             │    │
│  │  • 任务类型: coding / conversation / analysis          │    │
│  │  • 复杂度: 简单 / 中等 / 复杂                            │    │
│  │  • 是否需要上下文: 是 / 否                                │    │
│  │  • Token 预算: 充足 / 有限                                │    │
│  └────────────────────┬────────────────────────────────────┘    │
│                        ↓                                        │
│         ┌───────────────┼───────────────┐                          │
│         ↓               ↓               ↓                          │
│   ┌─────────┐      ┌─────────┐      ┌─────────┐                       │
│   │ 简单任务 │      │ 复杂任务 │      │ 对话任务 │                       │
│   └────┬────┘      └────┬────┘      └────┬────┘                       │
│        │               │               │                          │
│        ↓               ↓               ↓                          │
│   ┌─────────────────────────────────────────────────┐    │
│   │  最小化携带                                              │    │
│   │  ✅ 核心身份                     │    │
│   │  ❌ 长期经验                                              │    │
│   │  ❌ 当前会话                                              │    │
│   │  ❌ 工作记忆                                              │    │
│   │  ✅ 时间补丁 (如果模型较旧)                              │    │
│   └─────────────────────────────────────────────────┘    │
│                          ↓                                       │
│   ┌─────────────────────────────────────────────────┐    │
│   │  智能携带                                                │    │
│   │  ✅ 核心身份                                              │    │
│   │  ✅ 相关经验 (语义搜索，top 5)                          │    │
│   │  ❌ 当前会话 (新对话)                                    │    │
│   │  ✅ 工作记忆 (如果需要上下文)                              │    │
│   │  ✅ 时间补丁                                              │    │
│   └─────────────────────────────────────────────────┘    │
│                          ↓                                       │
│   ┌─────────────────────────────────────────────────┐    │
│   │  完整携带 (Token 充足)                                   │    │
│   │  ✅ 核心身份                                              │    │
│   │  ✅ 长期经验 (top 10)                                     │    │
│   │  ✅ 当前会话 (最近 50 条消息)                             │    │
│   │  ✅ 工作记忆                                              │    │
│   │  ✅ 时间补丁                                              │    │
│   └─────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 携带策略配置

```typescript
interface MemoryLoadStrategy {
  // 必须携带
  required: {
    identity: boolean
    temporalPatches: boolean
  }

  // 条件携带
  conditional: {
    // 长期经验
    longTermExperiences: {
      enabled: boolean
      limit: number           // 最多带多少条
      relevanceThreshold: number  // 相关性阈值
      compress: boolean        // 是否压缩摘要
    }

    // 短期会话
    shortTermSession: {
      enabled: boolean
      maxMessages: number      // 最多带多少条消息
      includeContext: boolean  // 是否带上下文摘要
    }

    // 工作记忆
    workingMemory: {
      enabled: boolean
      contextOnly: boolean    // 只带上下文，不带任务状态
    }
  }
}

class MemoryLoader {
  /**
   * 计算最优携带策略
   */
  calculateStrategy(
    memory: Memory,
    task: Task,
    constraints: {
      maxTokens: number
      latency: 'realtime' | 'batch'
    }
  ): MemoryLoadStrategy {
    const strategy: MemoryLoadStrategy = {
      required: {
        identity: true,
        temporalPatches: this.isModelOutdated(memory),
      },
      conditional: {} as any,
    }

    // 根据任务复杂度决定是否携带经验
    if (task.complexity > 5) {
      strategy.conditional.longTermExperiences = {
        enabled: true,
        limit: 5,
        relevanceThreshold: 0.7,
        compress: true,
      }
    }

    // 对话任务必须带当前会话
    if (task.type === 'conversation') {
      strategy.conditional.shortTermSession = {
        enabled: true,
        maxMessages: this.calculateMessageLimit(constraints.maxTokens),
        includeContext: true,
      }
    }

    // 需要上下文的任务带工作记忆
    if (task.requiresContext) {
      strategy.conditional.workingMemory = {
        enabled: true,
        contextOnly: true,
      }
    }

    return strategy
  }
}
```

### 6.3 Token 预算管理

```typescript
class TokenBudgetManager {
  /**
   * 估算不同组件的 Token 消耗
   */
  estimate(strategy: MemoryLoadStrategy): TokenEstimate {
    return {
      identity: this.estimateIdentity(),          // ~100 tokens
      temporalPatches: this.estimatePatches(), // ~50-200 tokens
      longTermExperiences: strategy.conditional.longTermExperiences.limit * 200,
      shortTermSession: strategy.conditional.shortTermSession.maxMessages * 100,
      workingMemory: this.estimateWorkingMemory(), // ~50-100 tokens
    }
  }

  /**
   * 压缩策略（当 Token 预算不足时）
   */
  compress(
    memory: Memory,
    budget: number
  ): CompressedMemory {
    const estimate = this.estimate(this.defaultStrategy())

    // 如果超出预算，按优先级压缩
    if (estimate.total > budget) {
      return {
        ...memory,
        longTerm: {
          ...memory.longTerm,
          experiences: memory.longTerm.experiences
            .slice(0, this.compressExperiences(budget))
            .map(exp => this.compressExperience(exp))
        },
        shortTerm: {
          currentSession: {
            ...memory.shortTerm.currentSession,
            messages: memory.shortTerm.currentSession.messages
              .slice(-this.compressMessages(budget))
              .map(msg => this.compressMessage(msg))
          },
        },
      }
    }

    return this.defaultCompress(memory)
  }
}
```

---

## 7. 数据模型

### 7.1 核心表结构

```sql
-- ============================================
-- 核心身份
-- ============================================
CREATE TABLE memory_identity (
  memory_id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  role VARCHAR(100),
  persona JSON NOT NULL,
  goals JSON,
  values JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='Memory 核心身份';


-- ============================================
-- 长期记忆：经验
-- ============================================
CREATE TABLE memory_experiences (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,
  experience_id VARCHAR(36) UNIQUE NOT NULL,

  type ENUM('success', 'failure', 'learning') NOT NULL,
  task_type VARCHAR(100) NOT NULL,
  task_description TEXT,
  task_complexity INT DEFAULT 5,
  task_domains JSON,

  approach JSON,
  outcome JSON,
  learning JSON,

  associations JSON,

  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  -- 向量 embedding
  embedding JSON,

  INDEX idx_memory (memory_id),
  INDEX idx_task_type (task_type),
  INDEX idx_timestamp (timestamp),
  FULLTEXT INDEX idx_search (task_description)
) COMMENT='经验记忆';


-- ============================================
-- 长期记忆：知识
-- ============================================
CREATE TABLE memory_knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,

  type ENUM('fact', 'procedure', 'preference', 'correction') NOT NULL,
  content TEXT NOT NULL,

  confidence DECIMAL(3,2) DEFAULT 0.50,

  source_type ENUM('user', 'observation', 'inference') NOT NULL,
  source_details JSON,

  verified BOOLEAN DEFAULT FALSE,
  verified_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_memory (memory_id),
  INDEX idx_type (type),
  INDEX idx_confidence (confidence),
  INDEX idx_verified (verified, expires_at)
) COMMENT='知识库';


-- ============================================
-- 长期记忆：技能
-- ============================================
CREATE TABLE memory_skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,

  skill_name VARCHAR(100) NOT NULL,
  category VARCHAR(50),

  proficiency_level INT DEFAULT 0,
  total_practice_time INT DEFAULT 0,
  last_practiced_at TIMESTAMP,

  learning_history JSON,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_memory_skill (memory_id, skill_name),
  INDEX idx_category (category),
  INDEX idx_proficiency (proficiency_level)
) COMMENT='技能熟练度';


-- ============================================
-- 时间补丁
-- ============================================
CREATE TABLE memory_temporal_patches (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,

  patch_id VARCHAR(36) UNIQUE NOT NULL,
  type ENUM('technology', 'policy', 'event', 'correction') NOT NULL,

  description TEXT NOT NULL,
  event_date DATE NOT NULL,
  affected_domains JSON,

  patch JSON NOT NULL,
  confidence DECIMAL(3,2) DEFAULT 0.50,

  source_type ENUM('user', 'system', 'api') NOT NULL,
  source_url VARCHAR(500),
  provided_by VARCHAR(255),

  applied BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_memory (memory_id),
  INDEX idx_type (type),
  INDEX idx_event_date (event_date),
  INDEX idx_applied (applied)
) COMMENT='时间补丁';


-- ============================================
-- 短期记忆：会话
-- ============================================
CREATE TABLE memory_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,
  session_id VARCHAR(36) UNIQUE NOT NULL,

  messages JSON NOT NULL,
  summary TEXT,
  started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP NULL,

  message_count INT DEFAULT 0,
  total_tokens INT DEFAULT 0,

  INDEX idx_memory (memory_id),
  INDEX idx_started (started_at),
  INDEX idx_completed (completed_at)
) COMMENT='会话记录';


-- ============================================
-- 工作记忆
-- ============================================
CREATE TABLE memory_working (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  memory_id VARCHAR(36) NOT NULL,

  current_task JSON,
  context JSON,
  temporary_files JSON,
  temporary_state JSON,

  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_memory (memory_id),
  INDEX idx_updated (last_updated)
) COMMENT='工作记忆 (临时状态)';


-- ============================================
-- 补丁应用记录
-- ============================================
CREATE TABLE memory_patch_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patch_id VARCHAR(36) NOT NULL,
  memory_id VARCHAR(36) NOT NULL,

  applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  session_id VARCHAR(36),

  outcome ENUM('success', 'failed', 'partial') NOT NULL,
  feedback TEXT,

  INDEX idx_memory (memory_id),
  INDEX idx_patch (patch_id),
  INDEX idx_applied (applied_at)
) COMMENT='补丁应用历史';
```

---

## 8. 待讨论事项

- [ ] Memory 版本控制与回滚
- [ ] 经验检索的语义搜索策略
- [ ] 知识验证机制
- [ ] 跨会话的知识迁移
- [ ] Memory 合并与 Fork
- [ ] 隐私保护与数据删除
- [ ] 补丁的自动生成与人工审核
- [ ] Token 使用统计与优化

---

**文档状态**: 🟢 设计阶段 - 深度讨论中
**相关文档**:
- [Memory-First 架构设计](./03-memory-first-architecture.md)
- [WebSocket 实时同步设计](./02-snapshot-delta-sync.md)

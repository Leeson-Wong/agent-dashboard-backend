package com.agent.monitor.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Agent 执行记录实体
 * Design Document: 05-agent-behavior.md
 */
@Data
public class AgentExecution {

    private Long id;

    /**
     * 执行 ID
     */
    private String executionId;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务描述
     */
    private String taskDescription;

    /**
     * 工具 ID
     */
    private String toolId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具类别
     */
    private String toolCategory;

    /**
     * 输入参数 (JSON)
     */
    private String input;

    /**
     * 输出结果 (JSON)
     */
    private String output;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 退出码
     */
    private Integer exitCode;

    /**
     * 执行时长（毫秒）
     */
    private Integer executionTimeMs;

    /**
     * 内存使用 (MB)
     */
    private BigDecimal memoryUsedMb;

    /**
     * Token 使用量
     */
    private Integer tokensUsed;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 开始时间
     */
    private Instant startedAt;

    /**
     * 完成时间
     */
    private Instant completedAt;
}

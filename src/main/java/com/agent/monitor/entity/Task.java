package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * 任务实体
 * 分配给 Agent 执行的任务
 */
@Data
public class Task {

    private Long id;

    /**
     * 任务 ID（UUID）
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型
     */
    private String type;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 优先级 (1-10, 10 最高)
     */
    private Integer priority;

    /**
     * 分配的 Agent ID
     */
    private String agentId;

    /**
     * 分配的 Memory ID
     */
    private String memoryId;

    /**
     * 输入参数（JSON）
     */
    private String input;

    /**
     * 输出结果（JSON）
     */
    private String output;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 进度 (0-100)
     */
    private Integer progress;

    /**
     * 开始时间
     */
    private Instant startedAt;

    /**
     * 完成时间
     */
    private Instant completedAt;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 所属 Crew ID
     */
    private String crewId;
}

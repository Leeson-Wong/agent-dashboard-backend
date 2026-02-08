package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Memory 经验记忆实体
 *
 * 用于存储从任务执行中提取的经验
 * Design Document: 04-memory-management.md
 */
@Data
public class MemoryExperience {

    private Long id;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 经验唯一标识
     */
    private String experienceId;

    /**
     * 经验类型 (success, failure, learning)
     */
    private String type;

    /**
     * 任务类型 (coding, debugging, analysis, etc)
     */
    private String taskType;

    /**
     * 任务描述
     */
    private String taskDescription;

    /**
     * 任务复杂度 (1-10)
     */
    private Integer taskComplexity;

    /**
     * 任务领域 (JSON)
     */
    private String taskDomains;

    /**
     * 执行方法 (JSON)
     */
    private String approach;

    /**
     * 结果 (JSON)
     */
    private String outcome;

    /**
     * 学到的内容 (JSON)
     */
    private String learning;

    /**
     * 关联信息 (JSON)
     */
    private String associations;

    /**
     * 时间戳
     */
    private Instant timestamp;

    /**
     * 向量 embedding (JSON)
     */
    private String embedding;
}

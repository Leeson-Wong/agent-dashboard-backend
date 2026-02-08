package com.agent.monitor.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Memory 时间补丁实体
 *
 * 用于存储更新模型过时知识的补丁
 * Design Document: 04-memory-management.md
 */
@Data
public class MemoryTemporalPatch {

    private Long id;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 补丁唯一标识
     */
    private String patchId;

    /**
     * 补丁类型 (technology, policy, event, correction)
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * 事件发生日期
     */
    private Instant eventDate;

    /**
     * 影响的领域 (JSON)
     */
    private String affectedDomains;

    /**
     * 补丁内容 (JSON)
     */
    private String patch;

    /**
     * 置信度 (0.00 - 1.00)
     */
    private BigDecimal confidence;

    /**
     * 来源类型 (user, system, api)
     */
    private String sourceType;

    /**
     * 来源 URL
     */
    private String sourceUrl;

    /**
     * 提供者
     */
    private String providedBy;

    /**
     * 是否已应用
     */
    private Boolean applied;

    /**
     * 创建时间
     */
    private Instant createdAt;
}

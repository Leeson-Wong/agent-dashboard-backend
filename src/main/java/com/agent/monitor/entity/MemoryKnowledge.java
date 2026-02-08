package com.agent.monitor.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Memory 知识条目实体
 *
 * 用于存储从对话、观察中提取的知识
 * Design Document: 04-memory-management.md
 */
@Data
public class MemoryKnowledge {

    private Long id;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 知识类型 (fact, procedure, preference, correction)
     */
    private String type;

    /**
     * 知识内容
     */
    private String content;

    /**
     * 置信度 (0.00 - 1.00)
     */
    private BigDecimal confidence;

    /**
     * 来源类型 (user, observation, inference)
     */
    private String sourceType;

    /**
     * 来源详情 (JSON)
     */
    private String sourceDetails;

    /**
     * 是否已验证
     */
    private Boolean verified;

    /**
     * 验证时间
     */
    private Instant verifiedAt;

    /**
     * 过期时间
     */
    private Instant expiresAt;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}

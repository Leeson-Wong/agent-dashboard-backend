package com.agent.monitor.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 工具使用统计实体
 * Design Document: 05-agent-behavior.md
 */
@Data
public class ToolUsageStats {

    private Long id;

    /**
     * 工具 ID
     */
    private String toolId;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 总使用次数
     */
    private Long totalUses;

    /**
     * 成功使用次数
     */
    private Long successfulUses;

    /**
     * 失败使用次数
     */
    private Long failedUses;

    /**
     * 熟练度等级 (0.00-1.00)
     */
    private BigDecimal proficiencyLevel;

    /**
     * 总练习时间（秒）
     */
    private Long totalPracticeTime;

    /**
     * 最后使用时间
     */
    private Instant lastUsedAt;

    /**
     * 最后成功时间
     */
    private Instant lastSuccessAt;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}

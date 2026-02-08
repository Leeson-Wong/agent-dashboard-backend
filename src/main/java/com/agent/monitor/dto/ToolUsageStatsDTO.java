package com.agent.monitor.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Tool Usage Stats DTO
 */
@Data
public class ToolUsageStatsDTO {
    private Long id;
    private String toolId;
    private String memoryId;
    private Long totalUses;
    private Long successfulUses;
    private Long failedUses;
    private BigDecimal proficiencyLevel;
    private Long totalPracticeTime;
    private String lastUsedAt;
    private String lastSuccessAt;
    private String createdAt;
    private String updatedAt;
}

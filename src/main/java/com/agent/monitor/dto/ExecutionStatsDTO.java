package com.agent.monitor.dto;

import lombok.Data;

/**
 * Execution Stats DTO
 */
@Data
public class ExecutionStatsDTO {
    private Long totalExecutions;
    private Long completed;
    private Long failed;
    private Long running;
    private Long pending;
    private Double averageExecutionTimeMs;
    private Long totalTokensUsed;
}

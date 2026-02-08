package com.agent.monitor.dto;

import lombok.Data;

/**
 * Complete Execution Request DTO
 */
@Data
public class CompleteExecutionRequestDTO {
    private String output;
    private Boolean success;
    private Integer exitCode;
    private Integer executionTimeMs;
    private Double memoryUsedMb;
    private Integer tokensUsed;
}

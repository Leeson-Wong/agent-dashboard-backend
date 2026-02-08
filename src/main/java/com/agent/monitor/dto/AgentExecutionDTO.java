package com.agent.monitor.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Agent Execution DTO
 */
@Data
public class AgentExecutionDTO {
    private Long id;
    private String executionId;
    private String agentId;
    private String memoryId;
    private String taskId;
    private String taskType;
    private String taskDescription;
    private String toolId;
    private String toolName;
    private String toolCategory;
    private String input;
    private String output;
    private Boolean success;
    private Integer exitCode;
    private Integer executionTimeMs;
    private BigDecimal memoryUsedMb;
    private Integer tokensUsed;
    private String status;
    private String createdAt;
    private String startedAt;
    private String completedAt;
}

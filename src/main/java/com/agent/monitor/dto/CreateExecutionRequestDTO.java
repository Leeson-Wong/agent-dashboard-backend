package com.agent.monitor.dto;

import lombok.Data;

/**
 * Create Execution Request DTO
 */
@Data
public class CreateExecutionRequestDTO {
    private String memoryId;
    private String taskId;
    private String taskType;
    private String taskDescription;
    private String toolId;
    private String toolName;
    private String toolCategory;
    private String input;
}

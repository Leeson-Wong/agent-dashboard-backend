package com.agent.monitor.dto;

import lombok.Data;

/**
 * Extract Experience Request DTO
 */
@Data
public class ExperienceRequestDTO {
    private String taskType;
    private String taskDescription;
    private Integer complexity;
    private Boolean success;
    private String output;
    private String error;
}

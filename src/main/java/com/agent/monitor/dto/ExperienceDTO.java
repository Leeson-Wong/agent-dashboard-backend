package com.agent.monitor.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Memory Experience DTO
 */
@Data
public class ExperienceDTO {
    private String experienceId;
    private String memoryId;
    private String type; // success, failure
    private String taskType;
    private String taskDescription;
    private Integer taskComplexity;
    private String approach; // JSON string
    private String learning; // JSON string
    private String outcome; // JSON string
    private Instant createdAt;
}

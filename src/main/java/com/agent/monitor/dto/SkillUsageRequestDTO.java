package com.agent.monitor.dto;

import lombok.Data;

/**
 * Record Skill Usage Request DTO
 */
@Data
public class SkillUsageRequestDTO {
    private String skillName;
    private String category;
    private Boolean success;
    private Long durationSeconds;
}

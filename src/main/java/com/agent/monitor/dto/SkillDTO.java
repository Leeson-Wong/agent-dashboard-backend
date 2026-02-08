package com.agent.monitor.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Memory Skill DTO
 */
@Data
public class SkillDTO {
    private Long id;
    private String memoryId;
    private String skillName;
    private String category;
    private Integer proficiencyLevel; // 0-100
    private Long totalPracticeTime; // seconds
    private Instant lastPracticedAt;
    private Instant createdAt;
}

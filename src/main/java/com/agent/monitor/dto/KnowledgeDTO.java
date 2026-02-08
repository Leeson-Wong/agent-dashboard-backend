package com.agent.monitor.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Memory Knowledge DTO
 */
@Data
public class KnowledgeDTO {
    private Long id;
    private String memoryId;
    private String type; // fact, procedure, preference, correction
    private String content;
    private String sourceType; // user, observation, inference
    private String sourceDetails; // JSON string
    private BigDecimal confidence;
    private Boolean verified;
    private Instant createdAt;
    private Instant updatedAt;
}

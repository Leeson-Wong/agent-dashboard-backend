package com.agent.monitor.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Add Knowledge Request DTO
 */
@Data
public class KnowledgeRequestDTO {
    private String type; // fact, procedure, preference, correction
    private String content;
    private String sourceType; // user, observation, inference
    private String sourceDetails; // JSON string
    private BigDecimal confidence;
}

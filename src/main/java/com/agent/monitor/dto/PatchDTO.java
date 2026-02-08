package com.agent.monitor.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Memory Temporal Patch DTO
 */
@Data
public class PatchDTO {
    private String patchId;
    private String memoryId;
    private String type; // technology, policy, event, correction
    private String description;
    private Instant eventDate;
    private List<String> affectedDomains;
    private String patch; // JSON string
    private BigDecimal confidence;
    private String sourceType;
    private String providedBy;
    private Boolean applied;
    private Instant createdAt;
}

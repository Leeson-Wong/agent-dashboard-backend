package com.agent.monitor.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Create Patch Request DTO
 */
@Data
public class PatchRequestDTO {
    private String type; // technology, policy, event, correction
    private String description;
    private Instant eventDate;
    private List<String> affectedDomains;
    private Object patchData; // Will be serialized to JSON
    private Float confidence;
    private String sourceType;
    private String providedBy;
}

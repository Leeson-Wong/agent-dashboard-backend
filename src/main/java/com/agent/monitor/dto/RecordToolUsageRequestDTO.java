package com.agent.monitor.dto;

import lombok.Data;

/**
 * Record Tool Usage Request DTO
 */
@Data
public class RecordToolUsageRequestDTO {
    private String toolId;
    private Boolean success;
    private Long durationSeconds;
}

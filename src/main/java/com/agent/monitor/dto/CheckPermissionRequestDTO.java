package com.agent.monitor.dto;

import lombok.Data;

/**
 * Check Permission Request DTO
 */
@Data
public class CheckPermissionRequestDTO {
    private String toolId;
    private String target; // path or domain
}

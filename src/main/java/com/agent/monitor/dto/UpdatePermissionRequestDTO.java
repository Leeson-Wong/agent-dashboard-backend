package com.agent.monitor.dto;

import lombok.Data;

import java.util.List;

/**
 * Update Permission Request DTO
 */
@Data
public class UpdatePermissionRequestDTO {
    private String permissionLevel;
    private List<String> allowedTools;
    private List<String> allowedPaths;
    private List<String> allowedDomains;
    private List<String> forbiddenTools;
    private List<String> forbiddenPaths;
}

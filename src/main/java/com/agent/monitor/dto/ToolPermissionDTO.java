package com.agent.monitor.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Tool Permission DTO
 */
@Data
public class ToolPermissionDTO {
    private Long id;
    private String agentId;
    private List<String> allowedTools;
    private List<String> allowedPaths;
    private List<String> allowedDomains;
    private List<String> forbiddenTools;
    private List<String> forbiddenPaths;
    private String permissionLevel;
    private String createdAt;
    private String updatedAt;
}

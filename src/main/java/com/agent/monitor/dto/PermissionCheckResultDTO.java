package com.agent.monitor.dto;

import lombok.Data;

/**
 * Permission Check Result DTO
 */
@Data
public class PermissionCheckResultDTO {
    private Boolean allowed;
    private String reason;
    private String permissionLevel;

    public static PermissionCheckResultDTO allowed(String permissionLevel) {
        PermissionCheckResultDTO result = new PermissionCheckResultDTO();
        result.setAllowed(true);
        result.setReason("Access granted");
        result.setPermissionLevel(permissionLevel);
        return result;
    }

    public static PermissionCheckResultDTO denied(String reason, String permissionLevel) {
        PermissionCheckResultDTO result = new PermissionCheckResultDTO();
        result.setAllowed(false);
        result.setReason(reason);
        result.setPermissionLevel(permissionLevel);
        return result;
    }
}

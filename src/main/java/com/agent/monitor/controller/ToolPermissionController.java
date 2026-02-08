package com.agent.monitor.controller;

import com.agent.monitor.dto.*;
import com.agent.monitor.entity.ToolPermission;
import com.agent.monitor.service.ToolPermissionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool Permission Controller
 *
 * Provides tool access control API
 * Design Document: 05-agent-behavior.md
 */
@Slf4j
@RestController
@RequestMapping("/api/tool-permissions")
@RequiredArgsConstructor
public class ToolPermissionController {

    private final ToolPermissionService permissionService;
    private final ObjectMapper objectMapper;

    /**
     * 获取权限配置
     *
     * GET /api/tool-permissions/{agentId}
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<ApiResponse<ToolPermissionDTO>> getPermission(@PathVariable String agentId) {
        log.debug("获取权限配置: agentId={}", agentId);

        try {
            ToolPermission permission = permissionService.getPermission(agentId);
            return ResponseEntity.ok(ApiResponse.success(convertToDTO(permission)));

        } catch (Exception e) {
            log.error("获取权限配置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get permission: " + e.getMessage()));
        }
    }

    /**
     * 更新权限配置
     *
     * PUT /api/tool-permissions/{agentId}
     */
    @PutMapping("/{agentId}")
    public ResponseEntity<ApiResponse<Boolean>> updatePermission(
            @PathVariable String agentId,
            @RequestBody UpdatePermissionRequestDTO request) {
        log.info("更新权限配置: agentId={}, level={}", agentId, request.getPermissionLevel());

        try {
            String allowedToolsJson = toJsonString(request.getAllowedTools());
            String allowedPathsJson = toJsonString(request.getAllowedPaths());
            String allowedDomainsJson = toJsonString(request.getAllowedDomains());
            String forbiddenToolsJson = toJsonString(request.getForbiddenTools());
            String forbiddenPathsJson = toJsonString(request.getForbiddenPaths());

            boolean updated = permissionService.updatePermission(
                    agentId,
                    request.getPermissionLevel(),
                    allowedToolsJson,
                    allowedPathsJson,
                    allowedDomainsJson,
                    forbiddenToolsJson,
                    forbiddenPathsJson
            );

            return ResponseEntity.ok(ApiResponse.success(updated));

        } catch (Exception e) {
            log.error("更新权限配置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to update permission: " + e.getMessage()));
        }
    }

    /**
     * 检查工具权限
     *
     * POST /api/tool-permissions/{agentId}/check
     */
    @PostMapping("/{agentId}/check")
    public ResponseEntity<ApiResponse<PermissionCheckResultDTO>> checkPermission(
            @PathVariable String agentId,
            @RequestBody CheckPermissionRequestDTO request) {
        log.debug("检查权限: agentId={}, toolId={}, target={}",
                agentId, request.getToolId(), request.getTarget());

        try {
            boolean allowed = permissionService.checkPermission(
                    agentId,
                    request.getToolId(),
                    request.getTarget()
            );

            ToolPermission permission = permissionService.getPermission(agentId);

            if (allowed) {
                return ResponseEntity.ok(ApiResponse.success(
                        PermissionCheckResultDTO.allowed(permission.getPermissionLevel())
                ));
            } else {
                return ResponseEntity.ok(ApiResponse.success(
                        PermissionCheckResultDTO.denied("Access denied by permission policy", permission.getPermissionLevel())
                ));
            }

        } catch (Exception e) {
            log.error("检查权限失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to check permission: " + e.getMessage()));
        }
    }

    /**
     * 设置权限级别
     *
     * POST /api/tool-permissions/{agentId}/level
     */
    @PostMapping("/{agentId}/level")
    public ResponseEntity<ApiResponse<Boolean>> setPermissionLevel(
            @PathVariable String agentId,
            @RequestParam String level) {
        log.info("设置权限级别: agentId={}, level={}", agentId, level);

        try {
            boolean updated = permissionService.setPermissionLevel(agentId, level);
            return ResponseEntity.ok(ApiResponse.success(updated));

        } catch (Exception e) {
            log.error("设置权限级别失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to set permission level: " + e.getMessage()));
        }
    }

    /**
     * 添加允许的工具
     *
     * POST /api/tool-permissions/{agentId}/allowed-tools
     */
    @PostMapping("/{agentId}/allowed-tools")
    public ResponseEntity<ApiResponse<Boolean>> addAllowedTool(
            @PathVariable String agentId,
            @RequestParam String toolId) {
        log.info("添加允许工具: agentId={}, toolId={}", agentId, toolId);

        try {
            boolean added = permissionService.addAllowedTool(agentId, toolId);
            return ResponseEntity.ok(ApiResponse.success(added));

        } catch (Exception e) {
            log.error("添加允许工具失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to add allowed tool: " + e.getMessage()));
        }
    }

    /**
     * 移除允许的工具
     *
     * DELETE /api/tool-permissions/{agentId}/allowed-tools/{toolId}
     */
    @DeleteMapping("/{agentId}/allowed-tools/{toolId}")
    public ResponseEntity<ApiResponse<Boolean>> removeAllowedTool(
            @PathVariable String agentId,
            @PathVariable String toolId) {
        log.info("移除允许工具: agentId={}, toolId={}", agentId, toolId);

        try {
            boolean removed = permissionService.removeAllowedTool(agentId, toolId);
            return ResponseEntity.ok(ApiResponse.success(removed));

        } catch (Exception e) {
            log.error("移除允许工具失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to remove allowed tool: " + e.getMessage()));
        }
    }

    /**
     * 删除权限配置
     *
     * DELETE /api/tool-permissions/{agentId}
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<ApiResponse<Boolean>> deletePermission(@PathVariable String agentId) {
        log.info("删除权限配置: agentId={}", agentId);

        try {
            boolean deleted = permissionService.deletePermission(agentId);
            return ResponseEntity.ok(ApiResponse.success(deleted));

        } catch (Exception e) {
            log.error("删除权限配置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to delete permission: " + e.getMessage()));
        }
    }

    /**
     * Convert ToolPermission to DTO
     */
    private ToolPermissionDTO convertToDTO(ToolPermission entity) {
        if (entity == null) return null;

        ToolPermissionDTO dto = new ToolPermissionDTO();
        dto.setId(entity.getId());
        dto.setAgentId(entity.getAgentId());
        dto.setPermissionLevel(entity.getPermissionLevel());

        // Parse JSON lists
        try {
            if (entity.getAllowedTools() != null && !entity.getAllowedTools().isEmpty()) {
                dto.setAllowedTools(objectMapper.readValue(
                        entity.getAllowedTools(),
                        new TypeReference<List<String>>() {}
                ));
            }
            if (entity.getAllowedPaths() != null && !entity.getAllowedPaths().isEmpty()) {
                dto.setAllowedPaths(objectMapper.readValue(
                        entity.getAllowedPaths(),
                        new TypeReference<List<String>>() {}
                ));
            }
            if (entity.getAllowedDomains() != null && !entity.getAllowedDomains().isEmpty()) {
                dto.setAllowedDomains(objectMapper.readValue(
                        entity.getAllowedDomains(),
                        new TypeReference<List<String>>() {}
                ));
            }
            if (entity.getForbiddenTools() != null && !entity.getForbiddenTools().isEmpty()) {
                dto.setForbiddenTools(objectMapper.readValue(
                        entity.getForbiddenTools(),
                        new TypeReference<List<String>>() {}
                ));
            }
            if (entity.getForbiddenPaths() != null && !entity.getForbiddenPaths().isEmpty()) {
                dto.setForbiddenPaths(objectMapper.readValue(
                        entity.getForbiddenPaths(),
                        new TypeReference<List<String>>() {}
                ));
            }
        } catch (Exception e) {
            log.error("解析权限配置失败", e);
        }

        // Convert timestamps
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().toString());
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().toString());
        }

        return dto;
    }

    /**
     * Convert List to JSON string
     */
    private String toJsonString(List<String> list) {
        if (list == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("序列化列表失败", e);
            return "[]";
        }
    }
}

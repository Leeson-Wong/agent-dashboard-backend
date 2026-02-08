package com.agent.monitor.service;

import com.agent.monitor.entity.ToolPermission;
import com.agent.monitor.mapper.ToolPermissionMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 工具权限服务
 *
 * 负责工具访问控制管理
 * Design Document: 05-agent-behavior.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolPermissionService {

    private final ToolPermissionMapper permissionMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取或创建权限配置
     *
     * @param agentId Agent ID
     * @return Permission configuration
     */
    @Transactional
    public ToolPermission getOrCreatePermission(String agentId) {
        ToolPermission permission = permissionMapper.findByAgentId(agentId);

        if (permission == null) {
            // 创建默认权限配置
            permission = new ToolPermission();
            permission.setAgentId(agentId);
            permission.setPermissionLevel("basic");
            permission.setAllowedTools("[]");
            permission.setAllowedPaths("[]");
            permission.setAllowedDomains("[]");
            permission.setForbiddenTools("[]");
            permission.setForbiddenPaths("[]");
            permission.setCreatedAt(Instant.now());

            permissionMapper.insert(permission);
            log.info("默认权限配置已创建: agentId={}", agentId);
        }

        return permission;
    }

    /**
     * 更新权限配置
     *
     * @param agentId Agent ID
     * @param permissionLevel Permission level (basic, standard, admin)
     * @param allowedTools Allowed tools (JSON list)
     * @param allowedPaths Allowed paths (JSON list)
     * @param allowedDomains Allowed domains (JSON list)
     * @param forbiddenTools Forbidden tools (JSON list)
     * @param forbiddenPaths Forbidden paths (JSON list)
     * @return Whether successful
     */
    @Transactional
    public boolean updatePermission(String agentId, String permissionLevel,
                                   String allowedTools, String allowedPaths, String allowedDomains,
                                   String forbiddenTools, String forbiddenPaths) {
        log.info("更新权限配置: agentId={}, level={}", agentId, permissionLevel);

        ToolPermission permission = getOrCreatePermission(agentId);

        permission.setPermissionLevel(permissionLevel);
        permission.setAllowedTools(allowedTools);
        permission.setAllowedPaths(allowedPaths);
        permission.setAllowedDomains(allowedDomains);
        permission.setForbiddenTools(forbiddenTools);
        permission.setForbiddenPaths(forbiddenPaths);

        int updated = permissionMapper.update(permission);
        log.info("权限配置已更新: agentId={}", agentId);
        return updated > 0;
    }

    /**
     * 检查工具权限
     *
     * @param agentId Agent ID
     * @param toolId Tool ID
     * @return Whether allowed
     */
    public boolean checkToolPermission(String agentId, String toolId) {
        ToolPermission permission = getOrCreatePermission(agentId);

        // 检查黑名单
        if (isInList(permission.getForbiddenTools(), toolId)) {
            log.warn("工具在禁止列表中: agentId={}, toolId={}", agentId, toolId);
            return false;
        }

        // 如果有白名单，检查白名单
        String allowedTools = permission.getAllowedTools();
        if (allowedTools != null && !allowedTools.equals("[]") && !allowedTools.isEmpty()) {
            if (isInList(allowedTools, toolId)) {
                return true;
            }
            log.warn("工具不在允许列表中: agentId={}, toolId={}", agentId, toolId);
            return false;
        }

        // 根据权限级别检查
        return checkByPermissionLevel(permission.getPermissionLevel(), toolId);
    }

    /**
     * 检查路径权限
     *
     * @param agentId Agent ID
     * @param path Path
     * @return Whether allowed
     */
    public boolean checkPathPermission(String agentId, String path) {
        ToolPermission permission = getOrCreatePermission(agentId);

        // 检查黑名单
        if (matchesPattern(permission.getForbiddenPaths(), path)) {
            log.warn("路径在禁止列表中: agentId={}, path={}", agentId, path);
            return false;
        }

        // 如果有白名单，检查白名单
        String allowedPaths = permission.getAllowedPaths();
        if (allowedPaths != null && !allowedPaths.equals("[]") && !allowedPaths.isEmpty()) {
            if (matchesPattern(allowedPaths, path)) {
                return true;
            }
            log.warn("路径不在允许列表中: agentId={}, path={}", agentId, path);
            return false;
        }

        // 根据权限级别检查
        return checkByPermissionLevel(permission.getPermissionLevel(), path);
    }

    /**
     * 检查域名权限
     *
     * @param agentId Agent ID
     * @param domain Domain
     * @return Whether allowed
     */
    public boolean checkDomainPermission(String agentId, String domain) {
        ToolPermission permission = getOrCreatePermission(agentId);

        // 检查黑名单
        if (isInList(permission.getAllowedDomains(), domain)) {
            log.warn("域名在禁止列表中: agentId={}, domain={}", agentId, domain);
            return false;
        }

        // 如果有白名单，检查白名单
        String allowedDomains = permission.getAllowedDomains();
        if (allowedDomains != null && !allowedDomains.equals("[]") && !allowedDomains.isEmpty()) {
            if (isInList(allowedDomains, domain)) {
                return true;
            }
            log.warn("域名不在允许列表中: agentId={}, domain={}", agentId, domain);
            return false;
        }

        // 根据权限级别检查
        return checkByPermissionLevel(permission.getPermissionLevel(), domain);
    }

    /**
     * 综合权限检查
     *
     * @param agentId Agent ID
     * @param toolId Tool ID
     * @param target Target (path, domain, etc.)
     * @return Whether allowed
     */
    public boolean checkPermission(String agentId, String toolId, String target) {
        // 检查工具权限
        if (!checkToolPermission(agentId, toolId)) {
            return false;
        }

        // 根据目标类型检查
        if (target != null && !target.isEmpty()) {
            if (target.startsWith("/") || target.startsWith(".")) {
                // 路径
                return checkPathPermission(agentId, target);
            } else if (target.contains(".")) {
                // 域名
                return checkDomainPermission(agentId, target);
            }
        }

        return true;
    }

    /**
     * 获取权限配置
     *
     * @param agentId Agent ID
     * @return Permission configuration
     */
    public ToolPermission getPermission(String agentId) {
        return getOrCreatePermission(agentId);
    }

    /**
     * 删除权限配置
     *
     * @param agentId Agent ID
     * @return Whether successful
     */
    @Transactional
    public boolean deletePermission(String agentId) {
        log.info("删除权限配置: agentId={}", agentId);
        int deleted = permissionMapper.deleteByAgentId(agentId);
        return deleted > 0;
    }

    /**
     * 根据权限级别检查
     *
     * @param level Permission level
     * @param resource Resource
     * @return Whether allowed
     */
    private boolean checkByPermissionLevel(String level, String resource) {
        switch (level) {
            case "admin":
                // 管理员可以访问所有资源
                return true;
            case "standard":
                // 标准用户可以访问大部分资源，但不能执行危险操作
                return !isDangerousResource(resource);
            case "basic":
            default:
                // 基础用户只能访问安全资源
                return isSafeResource(resource);
        }
    }

    /**
     * 检查是否为危险资源
     *
     * @param resource Resource
     * @return Whether dangerous
     */
    private boolean isDangerousResource(String resource) {
        if (resource == null) {
            return false;
        }

        // 危险工具列表
        String[] dangerousTools = {
                "code.shell", "file.delete", "system.execute"
        };

        // 危险路径模式
        String[] dangerousPaths = {
                "/etc", "/sys", "/proc", "/boot", "/root"
        };

        // 检查工具
        for (String tool : dangerousTools) {
            if (resource.equals(tool) || resource.startsWith(tool)) {
                return true;
            }
        }

        // 检查路径
        for (String path : dangerousPaths) {
            if (resource.startsWith(path)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否为安全资源
     *
     * @param resource Resource
     * @return Whether safe
     */
    private boolean isSafeResource(String resource) {
        return !isDangerousResource(resource);
    }

    /**
     * 检查是否在列表中
     *
     * @param jsonList JSON list string
     * @param value Value to check
     * @return Whether in list
     */
    private boolean isInList(String jsonList, String value) {
        if (jsonList == null || jsonList.isEmpty() || jsonList.equals("[]")) {
            return false;
        }

        try {
            List<String> list = objectMapper.readValue(jsonList, new TypeReference<List<String>>() {});
            return list.contains(value);
        } catch (Exception e) {
            log.error("解析 JSON 列表失败: {}", jsonList, e);
            return false;
        }
    }

    /**
     * 检查是否匹配模式
     *
     * @param jsonList JSON list string (may contain wildcards)
     * @param value Value to check
     * @return Whether matches
     */
    private boolean matchesPattern(String jsonList, String value) {
        if (jsonList == null || jsonList.isEmpty() || jsonList.equals("[]")) {
            return false;
        }

        try {
            List<String> patterns = objectMapper.readValue(jsonList, new TypeReference<List<String>>() {});

            for (String pattern : patterns) {
                if (value.equals(pattern)) {
                    return true;
                }

                // 支持通配符
                if (pattern.contains("*")) {
                    String regex = pattern.replace("*", ".*");
                    if (value.matches(regex)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.error("解析 JSON 模式失败: {}", jsonList, e);
            return false;
        }
    }

    /**
     * 添加允许的工具
     *
     * @param agentId Agent ID
     * @param toolId Tool ID
     * @return Whether successful
     */
    @Transactional
    public boolean addAllowedTool(String agentId, String toolId) {
        ToolPermission permission = getOrCreatePermission(agentId);

        try {
            Set<String> tools = new HashSet<>(
                    objectMapper.readValue(permission.getAllowedTools(), new TypeReference<List<String>>() {})
            );

            if (tools.add(toolId)) {
                permission.setAllowedTools(objectMapper.writeValueAsString(tools));
                return permissionMapper.update(permission) > 0;
            }

            return true; // Already in list
        } catch (Exception e) {
            log.error("添加允许工具失败: agentId={}, toolId={}", agentId, toolId, e);
            return false;
        }
    }

    /**
     * 移除允许的工具
     *
     * @param agentId Agent ID
     * @param toolId Tool ID
     * @return Whether successful
     */
    @Transactional
    public boolean removeAllowedTool(String agentId, String toolId) {
        ToolPermission permission = getOrCreatePermission(agentId);

        try {
            Set<String> tools = new HashSet<>(
                    objectMapper.readValue(permission.getAllowedTools(), new TypeReference<List<String>>() {})
            );

            if (tools.remove(toolId)) {
                permission.setAllowedTools(objectMapper.writeValueAsString(tools));
                return permissionMapper.update(permission) > 0;
            }

            return true; // Not in list
        } catch (Exception e) {
            log.error("移除允许工具失败: agentId={}, toolId={}", agentId, toolId, e);
            return false;
        }
    }

    /**
     * 设置权限级别
     *
     * @param agentId Agent ID
     * @param level Permission level
     * @return Whether successful
     */
    @Transactional
    public boolean setPermissionLevel(String agentId, String level) {
        ToolPermission permission = getOrCreatePermission(agentId);
        permission.setPermissionLevel(level);
        return permissionMapper.update(permission) > 0;
    }
}

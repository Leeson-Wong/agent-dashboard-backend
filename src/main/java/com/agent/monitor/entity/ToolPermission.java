package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * 工具权限配置实体
 * Design Document: 05-agent-behavior.md
 */
@Data
public class ToolPermission {

    private Long id;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 允许的工具列表 (JSON)
     */
    private String allowedTools;

    /**
     * 允许的路径列表 (JSON)
     */
    private String allowedPaths;

    /**
     * 允许的域名列表 (JSON)
     */
    private String allowedDomains;

    /**
     * 禁止的工具列表 (JSON)
     */
    private String forbiddenTools;

    /**
     * 禁止的路径列表 (JSON)
     */
    private String forbiddenPaths;

    /**
     * 权限级别 (basic, standard, admin)
     */
    private String permissionLevel;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}

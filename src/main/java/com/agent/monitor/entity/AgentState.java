package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Agent 状态实体
 */
@Data
public class AgentState {

    private Long id;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 服务器 ID
     */
    private String serverId;

    /**
     * 框架名称 (crewai, langgraph, etc)
     */
    private String framework;

    /**
     * 编程语言 (python, typescript, etc)
     */
    private String language;

    /**
     * 状态 (online, offline, error)
     */
    private String status;

    /**
     * 当前活动描述
     */
    private String currentActivity;

    /**
     * 角色信息
     */
    private String role;

    /**
     * 最后活跃时间
     */
    private Instant lastActivity;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}

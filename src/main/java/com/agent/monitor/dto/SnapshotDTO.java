package com.agent.monitor.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * 快照数据传输对象
 *
 * Design Document: 02-snapshot-delta-sync.md
 */
@Data
public class SnapshotDTO {

    /**
     * 快照唯一标识 (UUID)
     */
    private String snapshotId;

    /**
     * 快照时的最大事件序列号
     */
    private Long seq;

    /**
     * 所有 Agent 状态数据
     */
    private AgentListData data;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 过期时间
     */
    private Instant expiresAt;

    /**
     * Agent 列表数据
     */
    @Data
    public static class AgentListData {
        /**
         * Agent 列表
         */
        private List<AgentData> agents;
    }

    /**
     * Agent 数据
     */
    @Data
    public static class AgentData {
        private String agentId;
        private String serverId;
        private String framework;
        private String language;
        private String status;
        private String currentActivity;
        private String currentTool;
        private String currentTaskId;
        private String memoryId;
        private String role;
        private Instant lastActivity;
        private Instant createdAt;
        private Instant updatedAt;
    }
}

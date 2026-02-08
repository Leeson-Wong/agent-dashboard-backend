package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Memory 补丁应用历史实体
 *
 * 用于记录补丁应用结果
 * Design Document: 04-memory-management.md
 */
@Data
public class MemoryPatchHistory {

    private Long id;

    /**
     * 补丁 ID
     */
    private String patchId;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 应用时间
     */
    private Instant appliedAt;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 应用结果 (success, failed, partial)
     */
    private String outcome;

    /**
     * 反馈
     */
    private String feedback;
}

package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Memory 会话记录实体
 *
 * 用于存储短期对话历史
 * Design Document: 04-memory-management.md
 */
@Data
public class MemorySession {

    private Long id;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 会话唯一标识
     */
    private String sessionId;

    /**
     * 消息列表 (JSON)
     */
    private String messages;

    /**
     * 会话摘要
     */
    private String summary;

    /**
     * 开始时间
     */
    private Instant startedAt;

    /**
     * 完成时间
     */
    private Instant completedAt;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 总 Token 数
     */
    private Long totalTokens;
}

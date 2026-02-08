package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Agent 状态快照实体
 *
 * 用于存储所有 Agent 的当前状态快照，支持增量数据同步
 * Design Document: 02-snapshot-delta-sync.md
 */
@Data
public class Snapshot {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 快照唯一标识 (UUID)
     */
    private String snapshotId;

    /**
     * 快照时的最大事件序列号
     */
    private Long seq;

    /**
     * 所有 Agent 状态的 JSON 数据
     * 格式: {"agents": [{agentId, serverId, framework, status, ...}]}
     */
    private String data;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 过期时间 (用于清理)
     * 快照保留时间: 24 小时
     */
    private Instant expiresAt;
}

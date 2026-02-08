package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Agent 事件流实体
 *
 * 用于存储带全局序列号的事件流，支持增量同步
 * Design Document: 02-snapshot-delta-sync.md
 */
@Data
public class AgentEvent {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 全局递增序列号 (唯一)
     */
    private Long seq;

    /**
     * 事件类型
     * 可选值: agent_status, agent_activity, agent_error, tool_usage, llm_call, etc.
     */
    private String eventType;

    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 事件数据 (JSON)
     * 包含事件的详细信息，不同类型的事件数据结构不同
     */
    private String data;

    /**
     * 创建时间
     */
    private Instant createdAt;
}

package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * 序列号生成器实体
 *
 * 用于生成全局递增的事件序列号
 * Design Document: 02-snapshot-delta-sync.md
 */
@Data
public class SequenceGenerator {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 序列名称 (唯一)
     * 例如: agent_events_seq
     */
    private String sequenceName;

    /**
     * 当前值
     */
    private Long currentValue;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}

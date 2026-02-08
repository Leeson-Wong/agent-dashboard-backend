package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Memory 工作记忆实体
 *
 * 用于存储临时状态和上下文
 * Design Document: 04-memory-management.md
 */
@Data
public class MemoryWorking {

    private Long id;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 当前任务 (JSON)
     */
    private String currentTask;

    /**
     * 上下文变量 (JSON)
     */
    private String context;

    /**
     * 临时文件列表 (JSON)
     */
    private String temporaryFiles;

    /**
     * 临时状态 (JSON)
     */
    private String temporaryState;

    /**
     * 最后更新时间
     */
    private Instant lastUpdated;
}

package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Memory 技能熟练度实体
 *
 * 用于存储和管理技能熟练度
 * Design Document: 04-memory-management.md
 */
@Data
public class MemorySkill {

    private Long id;

    /**
     * Memory ID
     */
    private String memoryId;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 技能类别
     */
    private String category;

    /**
     * 熟练度级别 (0-100)
     */
    private Integer proficiencyLevel;

    /**
     * 总练习时间 (秒)
     */
    private Long totalPracticeTime;

    /**
     * 最后练习时间
     */
    private Instant lastPracticedAt;

    /**
     * 学习历史 (JSON)
     */
    private String learningHistory;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}

package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Memory 实体 - AI 本体
 * Memory = Agent 的持久化身份和经验
 * Design Document: 04-memory-management.md
 */
@Data
public class Memory {

    private Long id;

    /**
     * Memory ID（UUID）
     */
    private String memoryId;

    /**
     * Memory 名称
     */
    private String name;

    /**
     * Memory 类型
     */
    private String type;

    /**
     * 状态
     */
    private String status;

    /**
     * 角色设定
     */
    private String role;

    /**
     * 人格/Persona (JSON 格式)
     */
    private String persona;

    /**
     * 人格/Persona (独立字段 - JSON 格式)
     */
    private String personaJson;

    /**
     * 目标
     */
    private String goal;

    /**
     * 目标列表 (JSON 格式)
     */
    private String goalsJson;

    /**
     * 背景故事
     */
    private String backstory;

    /**
     * 经验数据（JSON）
     */
    private String experiences;

    /**
     * 知识库数据（JSON）
     */
    private String knowledge;

    /**
     * 技能数据（JSON）
     */
    private String skills;

    /**
     * 关系图数据（JSON）
     */
    private String relationships;

    /**
     * 价值观 (JSON 格式)
     */
    private String valuesJson;

    /**
     * 模型训练日期
     */
    private Instant modelTrainingDate;

    /**
     * 知识截止日期
     */
    private Instant knowledgeCutoff;

    /**
     * 时间补丁数据（JSON）
     */
    private String temporalPatches;

    /**
     * 总 Token 数
     */
    private Long totalTokens;

    /**
     * 总交互次数
     */
    private Long totalInteractions;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 最后激活时间
     */
    private Instant lastActivatedAt;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 父 Memory ID (用于 fork 操作)
     */
    private String parentMemoryId;
}

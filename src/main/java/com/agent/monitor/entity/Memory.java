package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Memory 实体 - AI 本体
 * Memory = Agent 的持久化身份和经验
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
     * 人格/Persona
     */
    private String persona;

    /**
     * 目标
     */
    private String goal;

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
}

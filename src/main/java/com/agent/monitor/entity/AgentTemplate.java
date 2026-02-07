package com.agent.monitor.entity;

import lombok.Data;

import java.time.Instant;

/**
 * Agent 模板实体
 * 预定义的 Agent 配置模板
 */
@Data
public class AgentTemplate {

    private Long id;

    /**
     * 模板 ID
     */
    private String templateId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板类型
     */
    private String type;

    /**
     * 模板分类
     */
    private String category;

    /**
     * 角色设定
     */
    private String role;

    /**
     * 目标
     */
    private String goal;

    /**
     * 背景故事
     */
    private String backstory;

    /**
     * 人格/Persona
     */
    private String persona;

    /**
     * 技能列表（JSON）
     */
    private String skills;

    /**
     * 工具列表（JSON）
     */
    private String tools;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 默认温度参数
     */
    private Double defaultTemperature;

    /**
     * 默认最大 Token
     */
    private Integer defaultMaxTokens;

    /**
     * 系统提示词模板
     */
    private String systemPromptTemplate;

    /**
     * 示例任务（JSON）
     */
    private String exampleTasks;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 版本号
     */
    private String version;

    /**
     * 作者
     */
    private String author;

    /**
     * 标签（JSON）
     */
    private String tags;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}

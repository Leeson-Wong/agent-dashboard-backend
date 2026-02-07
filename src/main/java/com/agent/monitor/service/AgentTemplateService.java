package com.agent.monitor.service;

import com.agent.monitor.entity.AgentTemplate;
import com.agent.monitor.mapper.AgentTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Agent 模板管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentTemplateService {

    private final AgentTemplateMapper agentTemplateMapper;

    /**
     * 创建模板
     */
    @Transactional
    public AgentTemplate createTemplate(AgentTemplate template) {
        // 生成 UUID
        if (template.getTemplateId() == null || template.getTemplateId().isEmpty()) {
            template.setTemplateId(UUID.randomUUID().toString());
        }

        // 设置默认值
        if (template.getEnabled() == null) {
            template.setEnabled(true);
        }
        if (template.getType() == null) {
            template.setType("standard");
        }
        if (template.getCategory() == null) {
            template.setCategory("general");
        }
        if (template.getUsageCount() == null) {
            template.setUsageCount(0L);
        }
        if (template.getDefaultTemperature() == null) {
            template.setDefaultTemperature(0.7);
        }
        if (template.getDefaultMaxTokens() == null) {
            template.setDefaultMaxTokens(2000);
        }
        if (template.getVersion() == null) {
            template.setVersion("1.0.0");
        }

        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());

        agentTemplateMapper.insert(template);
        log.info("创建 Agent 模板: {} ({})", template.getName(), template.getTemplateId());
        return template;
    }

    /**
     * 更新模板
     */
    @Transactional
    public AgentTemplate updateTemplate(AgentTemplate template) {
        AgentTemplate existing = agentTemplateMapper.findByTemplateId(template.getTemplateId());
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在: " + template.getTemplateId());
        }

        template.setUpdatedAt(Instant.now());
        agentTemplateMapper.update(template);
        log.info("更新 Agent 模板: {}", template.getTemplateId());
        return template;
    }

    /**
     * 获取模板
     */
    public AgentTemplate getTemplate(String templateId) {
        return agentTemplateMapper.findByTemplateId(templateId);
    }

    /**
     * 获取所有模板
     */
    public List<AgentTemplate> getAllTemplates() {
        return agentTemplateMapper.findAll();
    }

    /**
     * 获取启用的模板
     */
    public List<AgentTemplate> getEnabledTemplates() {
        return agentTemplateMapper.findEnabled();
    }

    /**
     * 根据类型获取模板
     */
    public List<AgentTemplate> getTemplatesByType(String type) {
        return agentTemplateMapper.findByType(type);
    }

    /**
     * 根据分类获取模板
     */
    public List<AgentTemplate> getTemplatesByCategory(String category) {
        return agentTemplateMapper.findByCategory(category);
    }

    /**
     * 搜索模板
     */
    public List<AgentTemplate> searchTemplates(String keyword) {
        return agentTemplateMapper.searchByName(keyword);
    }

    /**
     * 删除模板
     */
    @Transactional
    public void deleteTemplate(String templateId) {
        int count = agentTemplateMapper.deleteByTemplateId(templateId);
        if (count > 0) {
            log.info("删除 Agent 模板: {}", templateId);
        }
    }

    /**
     * 使用模板（增加使用计数）
     */
    @Transactional
    public void useTemplate(String templateId) {
        agentTemplateMapper.incrementUsage(templateId, Instant.now());
    }

    /**
     * 启用/禁用模板
     */
    @Transactional
    public void setTemplateEnabled(String templateId, boolean enabled) {
        AgentTemplate template = agentTemplateMapper.findByTemplateId(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }

        template.setEnabled(enabled);
        template.setUpdatedAt(Instant.now());
        agentTemplateMapper.update(template);
        log.info("{} 模板: {}", enabled ? "启用" : "禁用", templateId);
    }

    /**
     * 克隆模板
     */
    @Transactional
    public AgentTemplate cloneTemplate(String templateId) {
        AgentTemplate original = agentTemplateMapper.findByTemplateId(templateId);
        if (original == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }

        // 创建副本
        AgentTemplate clone = new AgentTemplate();
        clone.setName(original.getName() + " (副本)");
        clone.setDescription(original.getDescription());
        clone.setType(original.getType());
        clone.setCategory(original.getCategory());
        clone.setRole(original.getRole());
        clone.setGoal(original.getGoal());
        clone.setBackstory(original.getBackstory());
        clone.setPersona(original.getPersona());
        clone.setSkills(original.getSkills());
        clone.setTools(original.getTools());
        clone.setDefaultModel(original.getDefaultModel());
        clone.setDefaultTemperature(original.getDefaultTemperature());
        clone.setDefaultMaxTokens(original.getDefaultMaxTokens());
        clone.setSystemPromptTemplate(original.getSystemPromptTemplate());
        clone.setEnabled(true);
        clone.setVersion(original.getVersion());
        clone.setAuthor(original.getAuthor());
        clone.setTags(original.getTags());

        return createTemplate(clone);
    }
}

package com.agent.monitor.service;

import com.agent.monitor.entity.MemoryKnowledge;
import com.agent.monitor.mapper.MemoryKnowledgeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Memory 知识管理服务
 *
 * 负责知识的存储、验证、冲突解决
 * Design Document: 04-memory-management.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryKnowledgeService {

    private final MemoryKnowledgeMapper knowledgeMapper;
    private final ObjectMapper objectMapper;

    /**
     * 添加知识
     *
     * @param memoryId Memory ID
     * @param type 知识类型 (fact, procedure, preference, correction)
     * @param content 知识内容
     * @param sourceType 来源类型 (user, observation, inference)
     * @param sourceDetails 来源详情 (JSON)
     * @param confidence 置信度 (0.00-1.00)
     * @return 知识ID
     */
    @Transactional
    public Long addKnowledge(String memoryId, String type, String content,
                            String sourceType, String sourceDetails, BigDecimal confidence) {
        log.debug("添加知识: memoryId={}, type={}, content={}", memoryId, type, content);

        // 检查是否与现有知识冲突
        List<MemoryKnowledge> existing = knowledgeMapper.findByMemoryId(memoryId);
        for (MemoryKnowledge know : existing) {
            if (isSimilar(know.getContent(), content)) {
                log.info("发现相似知识，执行合并");
                return resolveConflict(know, type, content, confidence);
            }
        }

        // 创建新知识
        MemoryKnowledge knowledge = new MemoryKnowledge();
        knowledge.setMemoryId(memoryId);
        knowledge.setType(type);
        knowledge.setContent(content);
        knowledge.setSourceType(sourceType);
        knowledge.setSourceDetails(sourceDetails);
        knowledge.setConfidence(confidence);
        knowledge.setVerified("user".equals(sourceType));
        knowledge.setCreatedAt(Instant.now());

        knowledgeMapper.insert(knowledge);

        log.info("知识已添加: id={}, type={}", knowledge.getId(), type);
        return knowledge.getId();
    }

    /**
     * 获取 Memory 的所有知识
     */
    public List<MemoryKnowledge> getKnowledge(String memoryId) {
        return knowledgeMapper.findByMemoryId(memoryId);
    }

    /**
     * 获取特定类型的知识
     */
    public List<MemoryKnowledge> getKnowledgeByType(String memoryId, String type) {
        return knowledgeMapper.findByType(memoryId, type);
    }

    /**
     * 验证知识
     */
    @Transactional
    public boolean verifyKnowledge(Long knowledgeId) {
        log.debug("验证知识: id={}", knowledgeId);
        return knowledgeMapper.markAsVerified(knowledgeId, Instant.now()) > 0;
    }

    /**
     * 更新知识
     */
    @Transactional
    public boolean updateKnowledge(Long knowledgeId, String content, BigDecimal confidence) {
        log.debug("更新知识: id={}", knowledgeId);

        MemoryKnowledge knowledge = knowledgeMapper.findById(knowledgeId);
        if (knowledge == null) {
            log.warn("知识不存在: id={}", knowledgeId);
            return false;
        }

        knowledge.setContent(content);
        knowledge.setConfidence(confidence);
        knowledge.setUpdatedAt(Instant.now());

        return knowledgeMapper.update(knowledge) > 0;
    }

    /**
     * 定时任务：清理过期知识
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 120000)  // 每小时运行
    @Transactional
    public void cleanupExpiredKnowledge() {
        log.debug("清理过期知识");

        // 删除超过30天未验证且置信度低于0.5的知识
        Instant threshold = Instant.now().minusSeconds(30 * 24 * 60 * 60);
        int deleted = knowledgeMapper.deleteExpired(threshold);
        log.info("清理过期知识: 删除 {} 条", deleted);
    }

    /**
     * 检查两个知识是否相似
     */
    private boolean isSimilar(String content1, String content2) {
        // 简单的相似度检查：如果内容完全相同或包含关系
        if (content1 == null || content2 == null) {
            return false;
        }

        if (content1.equals(content2)) {
            return true;
        }

        // 检查包含关系
        return content1.contains(content2) || content2.contains(content1);
    }

    /**
     * 解决知识冲突
     */
    private Long resolveConflict(MemoryKnowledge existing, String newType, String newContent, BigDecimal newConfidence) {
        log.info("解决知识冲突: existing={}, newType={}", existing.getId(), newType);

        // 策略1：如果用户明确告知，更新知识
        if ("user".equals(existing.getSourceType()) && !"correction".equals(newType)) {
            log.debug("用户提供的知识优先，不更新");
            return existing.getId();
        }

        // 策略2：如果新置信度更高，更新
        if (newConfidence.compareTo(existing.getConfidence()) > 0) {
            existing.setContent(newContent);
            existing.setType(newType);
            existing.setConfidence(newConfidence);
            existing.setUpdatedAt(Instant.now());

            knowledgeMapper.update(existing);
            log.info("知识已更新: id={}", existing.getId());
            return existing.getId();
        }

        // 策略3：合并知识
        String mergedContent = mergeContent(existing.getContent(), newContent);
        existing.setContent(mergedContent);
        existing.setConfidence(existing.getConfidence().add(newConfidence).divide(BigDecimal.valueOf(2)));

        knowledgeMapper.update(existing);
        log.info("知识已合并: id={}", existing.getId());
        return existing.getId();
    }

    /**
     * 合并知识内容
     */
    private String mergeContent(String content1, String content2) {
        // 简单的合并策略：使用分隔符组合
        return content1 + " | " + content2;
    }
}

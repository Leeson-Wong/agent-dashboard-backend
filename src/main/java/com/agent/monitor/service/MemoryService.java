package com.agent.monitor.service;

import com.agent.monitor.entity.Memory;
import com.agent.monitor.mapper.MemoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Memory 管理服务 - 统一的 Memory 管理入口
 *
 * Design Document: 04-memory-management.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryMapper memoryMapper;
    private final MemoryExperienceService experienceService;
    private final MemoryKnowledgeService knowledgeService;
    private final MemorySkillService skillService;
    private final MemoryPatchService patchService;

    /**
     * 创建 Memory
     */
    @Transactional
    public Memory createMemory(Memory memory) {
        // 生成 UUID
        if (memory.getMemoryId() == null || memory.getMemoryId().isEmpty()) {
            memory.setMemoryId(UUID.randomUUID().toString());
        }

        // 设置默认值
        if (memory.getStatus() == null) {
            memory.setStatus("inactive");
        }
        if (memory.getType() == null) {
            memory.setType("standard");
        }

        memory.setCreatedAt(Instant.now());
        memory.setUpdatedAt(Instant.now());
        memory.setLastActivatedAt(Instant.now());

        if (memory.getTotalTokens() == null) {
            memory.setTotalTokens(0L);
        }

        memoryMapper.insert(memory);
        log.info("创建 Memory: {} ({})", memory.getName(), memory.getMemoryId());
        return memory;
    }

    /**
     * 更新 Memory
     */
    @Transactional
    public Memory updateMemory(Memory memory) {
        Memory existing = memoryMapper.findByMemoryId(memory.getMemoryId());
        if (existing == null) {
            throw new IllegalArgumentException("Memory 不存在: " + memory.getMemoryId());
        }

        memory.setUpdatedAt(Instant.now());
        memoryMapper.update(memory);
        log.info("更新 Memory: {}", memory.getMemoryId());
        return memory;
    }

    /**
     * 获取 Memory
     */
    public Memory getMemory(String memoryId) {
        return memoryMapper.findByMemoryId(memoryId);
    }

    /**
     * 获取所有 Memory
     */
    public List<Memory> getAllMemories() {
        return memoryMapper.findAll();
    }

    /**
     * 根据状态获取 Memory
     */
    public List<Memory> getMemoriesByStatus(String status) {
        return memoryMapper.findByStatus(status);
    }

    /**
     * 删除 Memory
     */
    @Transactional
    public void deleteMemory(String memoryId) {
        int count = memoryMapper.deleteByMemoryId(memoryId);
        if (count > 0) {
            log.info("删除 Memory: {}", memoryId);
        } else {
            log.warn("Memory 不存在: {}", memoryId);
        }
    }

    /**
     * 激活 Memory（标记为已使用）
     */
    @Transactional
    public void activateMemory(String memoryId) {
        Memory memory = memoryMapper.findByMemoryId(memoryId);
        if (memory == null) {
            throw new IllegalArgumentException("Memory 不存在: " + memoryId);
        }

        memory.setStatus("active");
        memory.setLastActivatedAt(Instant.now());
        memory.setUpdatedAt(Instant.now());
        memoryMapper.update(memory);
        log.info("激活 Memory: {}", memoryId);
    }

    /**
     * 停用 Memory
     */
    @Transactional
    public void deactivateMemory(String memoryId) {
        Memory memory = memoryMapper.findByMemoryId(memoryId);
        if (memory == null) {
            throw new IllegalArgumentException("Memory 不存在: " + memoryId);
        }

        memory.setStatus("inactive");
        memory.setUpdatedAt(Instant.now());
        memoryMapper.update(memory);
        log.info("停用 Memory: {}", memoryId);
    }

    /**
     * 获取统计信息
     */
    public MemoryStats getStats() {
        MemoryStats stats = new MemoryStats();
        stats.setTotalMemories((long) memoryMapper.count());
        stats.setActiveMemories((long) memoryMapper.findByStatus("active").size());
        stats.setInactiveMemories((long) memoryMapper.findByStatus("inactive").size());
        return stats;
    }

    // ========================================================
    // 经验管理
    // ========================================================

    /**
     * 提取经验
     */
    public String extractExperience(String memoryId, String taskType, String taskDescription,
                                     Integer complexity, boolean success, String output, String error) {
        return experienceService.extractExperience(memoryId, taskType, taskDescription,
                complexity, success, output, error);
    }

    /**
     * 获取相关经验
     */
    public List<com.agent.monitor.entity.MemoryExperience> getRelevantExperiences(String memoryId, String taskType, Integer limit) {
        return experienceService.getRelevantExperiences(memoryId, taskType, limit);
    }

    // ========================================================
    // 知识管理
    // ========================================================

    /**
     * 添加知识
     */
    public Long addKnowledge(String memoryId, String type, String content,
                           String sourceType, String sourceDetails, java.math.BigDecimal confidence) {
        return knowledgeService.addKnowledge(memoryId, type, content, sourceType, sourceDetails, confidence);
    }

    /**
     * 获取知识
     */
    public List<com.agent.monitor.entity.MemoryKnowledge> getKnowledge(String memoryId) {
        return knowledgeService.getKnowledge(memoryId);
    }

    /**
     * 验证知识
     */
    public boolean verifyKnowledge(Long knowledgeId) {
        return knowledgeService.verifyKnowledge(knowledgeId);
    }

    // ========================================================
    // 技能管理
    // ========================================================

    /**
     * 记录技能使用
     */
    public Long recordSkillUsage(String memoryId, String skillName, String category,
                                boolean success, Long durationSeconds) {
        return skillService.recordSkillUsage(memoryId, skillName, category, success, durationSeconds);
    }

    /**
     * 获取技能
     */
    public List<com.agent.monitor.entity.MemorySkill> getSkills(String memoryId) {
        return skillService.getSkills(memoryId);
    }

    /**
     * 获取最熟练技能
     */
    public List<com.agent.monitor.entity.MemorySkill> getMostProficientSkills(String memoryId, Integer limit) {
        return skillService.getMostProficientSkills(memoryId, limit);
    }

    // ========================================================
    // 时间补丁管理
    // ========================================================

    /**
     * 创建时间补丁
     */
    public String createPatch(String memoryId, String type, String description,
                             Instant eventDate, List<String> affectedDomains,
                             Object patchData, float confidence, String sourceType, String providedBy) {
        return patchService.createPatch(memoryId, type, description, eventDate,
                affectedDomains, patchData, confidence, sourceType, providedBy);
    }

    /**
     * 应用时间补丁
     */
    public MemoryPatchService.PatchResult applyPatches(String memoryId, String sessionId) {
        return patchService.applyPatches(memoryId, sessionId);
    }

    /**
     * 获取补丁
     */
    public List<com.agent.monitor.entity.MemoryTemporalPatch> getPatches(String memoryId) {
        return patchService.getPatches(memoryId);
    }

    /**
     * Memory 统计信息
     */
    @lombok.Data
    public static class MemoryStats {
        private Long totalMemories;
        private Long activeMemories;
        private Long inactiveMemories;
    }
}

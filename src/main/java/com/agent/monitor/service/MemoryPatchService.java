package com.agent.monitor.service;

import com.agent.monitor.entity.Memory;
import com.agent.monitor.entity.MemoryPatchHistory;
import com.agent.monitor.entity.MemoryTemporalPatch;
import com.agent.monitor.mapper.MemoryMapper;
import com.agent.monitor.mapper.MemoryPatchHistoryMapper;
import com.agent.monitor.mapper.MemoryTemporalPatchMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Memory 时间补丁服务
 *
 * 负责应用时间补丁到 Memory，更新过时知识
 * Design Document: 04-memory-management.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryPatchService {

    private final MemoryMapper memoryMapper;
    private final MemoryTemporalPatchMapper patchMapper;
    private final MemoryPatchHistoryMapper historyMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建时间补丁
     *
     * @param memoryId Memory ID
     * @param type 补丁类型 (technology, policy, event, correction)
     * @param description 描述
     * @param eventDate 事件发生日期
     * @param affectedDomains 影响的领域
     * @param patchData 补丁内容
     * @param confidence 置信度
     * @param sourceType 来源类型
     * @param providedBy 提供者
     * @return 补丁ID
     */
    @Transactional
    public String createPatch(String memoryId, String type, String description,
                             Instant eventDate, List<String> affectedDomains,
                             Object patchData, float confidence, String sourceType, String providedBy) {
        log.info("创建时间补丁: memoryId={}, type={}, description={}", memoryId, type, description);

        try {
            MemoryTemporalPatch patch = new MemoryTemporalPatch();
            patch.setMemoryId(memoryId);
            patch.setPatchId(UUID.randomUUID().toString());
            patch.setType(type);
            patch.setDescription(description);
            patch.setEventDate(eventDate);
            patch.setAffectedDomains(objectMapper.writeValueAsString(affectedDomains));
            patch.setPatch(objectMapper.writeValueAsString(patchData));
            patch.setConfidence(java.math.BigDecimal.valueOf(confidence));
            patch.setSourceType(sourceType);
            patch.setProvidedBy(providedBy);
            patch.setApplied(false);
            patch.setCreatedAt(Instant.now());

            patchMapper.insert(patch);

            log.info("时间补丁已创建: patchId={}", patch.getPatchId());
            return patch.getPatchId();

        } catch (JsonProcessingException e) {
            log.error("补丁数据序列化失败", e);
            throw new RuntimeException("Failed to create patch", e);
        }
    }

    /**
     * 应用时间补丁到 Memory
     *
     * @param memoryId Memory ID
     * @param sessionId 会话 ID
     * @return 应用结果
     */
    @Transactional
    public PatchResult applyPatches(String memoryId, String sessionId) {
        log.info("应用时间补丁: memoryId={}", memoryId);

        // 获取 Memory
        Memory memory = memoryMapper.findByMemoryId(memoryId);
        if (memory == null) {
            log.warn("Memory 不存在: memoryId={}", memoryId);
            return new PatchResult(false, "Memory not found");
        }

        // 获取未应用的补丁
        List<MemoryTemporalPatch> patches = patchMapper.findUnapplied(memoryId);
        if (patches.isEmpty()) {
            log.info("没有需要应用的补丁");
            return new PatchResult(true, "No patches to apply");
        }

        int appliedCount = 0;
        int failedCount = 0;

        for (MemoryTemporalPatch patch : patches) {
            try {
                boolean success = applySinglePatch(memory, patch);

                // 记录历史
                MemoryPatchHistory history = new MemoryPatchHistory();
                history.setPatchId(patch.getPatchId());
                history.setMemoryId(memoryId);
                history.setSessionId(sessionId);
                history.setOutcome(success ? "success" : "failed");
                history.setAppliedAt(Instant.now());
                historyMapper.insert(history);

                // 标记补丁为已应用
                if (success) {
                    patchMapper.markAsApplied(patch.getPatchId());
                    appliedCount++;
                    log.info("补丁已应用: patchId={}", patch.getPatchId());
                } else {
                    failedCount++;
                }

            } catch (Exception e) {
                log.error("应用补丁失败: patchId={}", patch.getPatchId(), e);
                failedCount++;
            }
        }

        // 更新 Memory 的 temporal_patches 字段
        memory.setUpdatedAt(Instant.now());
        memoryMapper.update(memory);

        String message = String.format("Applied %d patches, %d failed", appliedCount, failedCount);
        return new PatchResult(failedCount == 0, message);
    }

    /**
     * 应用单个补丁
     */
    private boolean applySinglePatch(Memory memory, MemoryTemporalPatch patch) {
        try {
            // 简化实现：将补丁信息附加到 Memory 的 temporal_patches 字段
            String existingPatches = memory.getTemporalPatches() != null ? memory.getTemporalPatches() : "[]";

            PatchInfo patchInfo = new PatchInfo();
            patchInfo.setPatchId(patch.getPatchId());
            patchInfo.setType(patch.getType());
            patchInfo.setDescription(patch.getDescription());
            patchInfo.setAppliedAt(Instant.now());

            // 这里简化处理，实际应该解析 JSON 并追加
            String updatedPatches = existingPatches + "," + objectMapper.writeValueAsString(patchInfo);

            memory.setTemporalPatches(updatedPatches);
            return true;

        } catch (JsonProcessingException e) {
            log.error("补丁信息序列化失败", e);
            return false;
        }
    }

    /**
     * 获取所有补丁
     */
    public List<MemoryTemporalPatch> getPatches(String memoryId) {
        return patchMapper.findByMemoryId(memoryId);
    }

    /**
     * 获取特定类型的补丁
     */
    public List<MemoryTemporalPatch> getPatchesByType(String memoryId, String type) {
        return patchMapper.findByType(memoryId, type);
    }

    /**
     * 获取未应用的补丁
     */
    public List<MemoryTemporalPatch> getUnappliedPatches(String memoryId) {
        return patchMapper.findUnapplied(memoryId);
    }

    /**
     * 补丁应用结果
     */
    public static class PatchResult {
        private final boolean success;
        private final String message;

        public PatchResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    /**
     * 补丁信息
     */
    private static class PatchInfo {
        private String patchId;
        private String type;
        private String description;
        private Instant appliedAt;

        // getters and setters
        public String getPatchId() { return patchId; }
        public void setPatchId(String patchId) { this.patchId = patchId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Instant getAppliedAt() { return appliedAt; }
        public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }
    }
}

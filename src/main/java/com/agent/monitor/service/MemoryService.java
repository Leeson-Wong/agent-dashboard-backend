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
 * Memory 管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryMapper memoryMapper;

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

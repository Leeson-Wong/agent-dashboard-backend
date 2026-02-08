package com.agent.monitor.mapper;

import com.agent.monitor.entity.MemoryKnowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * Memory 知识 Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemoryKnowledgeMapper {

    /**
     * 插入知识
     */
    int insert(MemoryKnowledge knowledge);

    /**
     * 根据 ID 查找
     */
    MemoryKnowledge findById(@Param("id") Long id);

    /**
     * 根据 memory_id 查找所有知识
     */
    List<MemoryKnowledge> findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 根据类型查找知识
     */
    List<MemoryKnowledge> findByType(@Param("memoryId") String memoryId, @Param("type") String type);

    /**
     * 查找未验证的知识
     */
    List<MemoryKnowledge> findUnverified(@Param("memoryId") String memoryId);

    /**
     * 查找过期的知识
     */
    List<MemoryKnowledge> findExpired(@Param("before") Instant before);

    /**
     * 更新知识
     */
    int update(MemoryKnowledge knowledge);

    /**
     * 标记为已验证
     */
    int markAsVerified(@Param("id") Long id, @Param("verifiedAt") Instant verifiedAt);

    /**
     * 删除知识
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除过期知识
     */
    int deleteExpired(@Param("before") Instant before);
}

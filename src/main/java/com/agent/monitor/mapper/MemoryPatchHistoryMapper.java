package com.agent.monitor.mapper;

import com.agent.monitor.entity.MemoryPatchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Memory 补丁历史 Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemoryPatchHistoryMapper {

    /**
     * 插入历史记录
     */
    int insert(MemoryPatchHistory history);

    /**
     * 根据 ID 查找
     */
    MemoryPatchHistory findById(@Param("id") Long id);

    /**
     * 根据 memory_id 查找所有历史
     */
    List<MemoryPatchHistory> findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 根据 patch_id 查找历史
     */
    List<MemoryPatchHistory> findByPatchId(@Param("patchId") String patchId);

    /**
     * 根据会话查找
     */
    List<MemoryPatchHistory> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 获取最近的应用记录
     */
    List<MemoryPatchHistory> findRecent(@Param("limit") Integer limit);
}

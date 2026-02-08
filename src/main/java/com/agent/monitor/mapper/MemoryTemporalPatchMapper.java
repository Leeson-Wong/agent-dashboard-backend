package com.agent.monitor.mapper;

import com.agent.monitor.entity.MemoryTemporalPatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Memory 时间补丁 Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemoryTemporalPatchMapper {

    /**
     * 插入补丁
     */
    int insert(MemoryTemporalPatch patch);

    /**
     * 根据 ID 查找
     */
    MemoryTemporalPatch findById(@Param("id") Long id);

    /**
     * 根据 patch_id 查找
     */
    MemoryTemporalPatch findByPatchId(@Param("patchId") String patchId);

    /**
     * 根据 memory_id 查找所有补丁
     */
    List<MemoryTemporalPatch> findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 查找未应用的补丁
     */
    List<MemoryTemporalPatch> findUnapplied(@Param("memoryId") String memoryId);

    /**
     * 根据类型查找补丁
     */
    List<MemoryTemporalPatch> findByType(@Param("memoryId") String memoryId, @Param("type") String type);

    /**
     * 更新补丁
     */
    int update(MemoryTemporalPatch patch);

    /**
     * 标记为已应用
     */
    int markAsApplied(@Param("patchId") String patchId);

    /**
     * 删除补丁
     */
    int deleteByPatchId(@Param("patchId") String patchId);
}

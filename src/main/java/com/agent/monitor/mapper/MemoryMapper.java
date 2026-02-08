package com.agent.monitor.mapper;

import com.agent.monitor.entity.Memory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Memory Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemoryMapper {

    /**
     * 插入 Memory
     */
    int insert(Memory memory);

    /**
     * 更新 Memory
     */
    int update(Memory memory);

    /**
     * 根据 ID 查找 Memory
     */
    Memory findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 查找所有 Memory
     */
    List<Memory> findAll();

    /**
     * 根据状态查找 Memory
     */
    List<Memory> findByStatus(@Param("status") String status);

    /**
     * 根据类型查找 Memory
     */
    List<Memory> findByType(@Param("type") String type);

    /**
     * 根据 ID 删除 Memory
     */
    int deleteByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 统计 Memory 数量
     */
    int count();

    /**
     * 根据类型统计
     */
    int countByType(@Param("type") String type);
}

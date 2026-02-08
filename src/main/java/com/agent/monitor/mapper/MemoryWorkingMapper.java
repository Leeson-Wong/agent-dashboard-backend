package com.agent.monitor.mapper;

import com.agent.monitor.entity.MemoryWorking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Memory 工作记忆 Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemoryWorkingMapper {

    /**
     * 插入或更新工作记忆
     */
    int upsert(MemoryWorking working);

    /**
     * 根据 memory_id 查找
     */
    MemoryWorking findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 更新当前任务
     */
    int updateCurrentTask(@Param("memoryId") String memoryId, @Param("currentTask") String currentTask);

    /**
     * 更新上下文
     */
    int updateContext(@Param("memoryId") String memoryId, @Param("context") String context);

    /**
     * 更新临时状态
     */
    int updateTemporaryState(@Param("memoryId") String memoryId, @Param("temporaryState") String temporaryState);

    /**
     * 清空工作记忆
     */
    int clear(@Param("memoryId") String memoryId);

    /**
     * 删除工作记忆
     */
    int deleteByMemoryId(@Param("memoryId") String memoryId);
}

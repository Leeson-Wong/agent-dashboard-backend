package com.agent.monitor.mapper;

import com.agent.monitor.entity.MemoryExperience;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Memory 经验 Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemoryExperienceMapper {

    /**
     * 插入经验
     */
    int insert(MemoryExperience experience);

    /**
     * 根据 experience_id 查找
     */
    MemoryExperience findByExperienceId(@Param("experienceId") String experienceId);

    /**
     * 根据 memory_id 查找所有经验
     */
    List<MemoryExperience> findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 根据类型查找经验
     */
    List<MemoryExperience> findByType(@Param("memoryId") String memoryId, @Param("type") String type);

    /**
     * 根据任务类型查找经验
     */
    List<MemoryExperience> findByTaskType(@Param("memoryId") String memoryId, @Param("taskType") String taskType);

    /**
     * 获取最近的 N 条经验
     */
    List<MemoryExperience> findRecent(@Param("memoryId") String memoryId, @Param("limit") Integer limit);

    /**
     * 更新经验
     */
    int update(MemoryExperience experience);

    /**
     * 删除经验
     */
    int deleteByExperienceId(@Param("experienceId") String experienceId);
}

package com.agent.monitor.mapper;

import com.agent.monitor.entity.ToolUsageStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工具使用统计 Mapper
 * Design Document: 05-agent-behavior.md
 */
@Mapper
public interface ToolUsageStatsMapper {

    /**
     * 插入统计记录
     */
    int insert(ToolUsageStats stats);

    /**
     * 更新统计记录
     */
    int update(ToolUsageStats stats);

    /**
     * 根据 toolId 和 memoryId 查询
     */
    ToolUsageStats findByToolIdAndMemoryId(@Param("toolId") String toolId, @Param("memoryId") String memoryId);

    /**
     * 根据 memoryId 查询所有统计
     */
    List<ToolUsageStats> findByMemoryId(String memoryId);

    /**
     * 查询最熟练的工具
     */
    List<ToolUsageStats> findMostProficient(@Param("memoryId") String memoryId, @Param("limit") int limit);

    /**
     * 更新使用次数
     */
    int incrementUses(@Param("toolId") String toolId, @Param("memoryId") String memoryId, @Param("success") boolean success);

    /**
     * 更新熟练度
     */
    int updateProficiency(@Param("toolId") String toolId, @Param("memoryId") String memoryId, @Param("proficiency") java.math.BigDecimal proficiency);

    /**
     * 更新练习时间
     */
    int updatePracticeTime(@Param("toolId") String toolId, @Param("memoryId") String memoryId, @Param("additionalSeconds") long additionalSeconds);
}

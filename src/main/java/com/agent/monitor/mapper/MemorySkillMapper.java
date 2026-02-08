package com.agent.monitor.mapper;

import com.agent.monitor.entity.MemorySkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Memory 技能 Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemorySkillMapper {

    /**
     * 插入技能
     */
    int insert(MemorySkill skill);

    /**
     * 根据 ID 查找
     */
    MemorySkill findById(@Param("id") Long id);

    /**
     * 根据 memory_id 查找所有技能
     */
    List<MemorySkill> findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 根据 memory_id 和 skill_name 查找
     */
    MemorySkill findByMemoryIdAndSkillName(@Param("memoryId") String memoryId, @Param("skillName") String skillName);

    /**
     * 根据类别查找技能
     */
    List<MemorySkill> findByCategory(@Param("memoryId") String memoryId, @Param("category") String category);

    /**
     * 获取最熟练的技能
     */
    List<MemorySkill> findMostProficient(@Param("memoryId") String memoryId, @Param("limit") Integer limit);

    /**
     * 更新技能
     */
    int update(MemorySkill skill);

    /**
     * 增加熟练度
     */
    int incrementProficiency(@Param("id") Long id, @Param("increment") Integer increment);

    /**
     * 更新练习时间
     */
    int updatePracticeTime(@Param("id") Long id, @Param("seconds") Long seconds);

    /**
     * 删除技能
     */
    int deleteById(@Param("id") Long id);
}

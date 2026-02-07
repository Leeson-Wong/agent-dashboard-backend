package com.agent.monitor.mapper;

import com.agent.monitor.entity.Memory;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;

/**
 * Memory Mapper
 */
@Mapper
public interface MemoryMapper {

    /**
     * 插入 Memory
     */
    @Insert("INSERT INTO memories (memory_id, name, type, status, role, persona, goal, backstory, " +
            "model_training_date, knowledge_cutoff, total_tokens, created_at, updated_at) " +
            "VALUES (#{memoryId}, #{name}, #{type}, #{status}, #{role}, #{persona}, #{goal}, #{backstory}, " +
            "#{modelTrainingDate}, #{knowledgeCutoff}, #{totalTokens}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Memory memory);

    /**
     * 更新 Memory
     */
    @Update("UPDATE memories SET name=#{name}, type=#{type}, status=#{status}, " +
            "role=#{role}, persona=#{persona}, goal=#{goal}, backstory=#{backstory}, " +
            "experiences=#{experiences}, knowledge=#{knowledge}, skills=#{skills}, " +
            "relationships=#{relationships}, temporal_patches=#{temporalPatches}, " +
            "total_tokens=#{totalTokens}, updated_at=#{updatedAt}, last_activated_at=#{lastActivatedAt} " +
            "WHERE memory_id=#{memoryId}")
    int update(Memory memory);

    /**
     * 根据 ID 查找 Memory
     */
    @Select("SELECT * FROM memories WHERE memory_id = #{memoryId}")
    Memory findByMemoryId(String memoryId);

    /**
     * 查找所有 Memory
     */
    @Select("SELECT * FROM memories ORDER BY updated_at DESC")
    List<Memory> findAll();

    /**
     * 根据状态查找 Memory
     */
    @Select("SELECT * FROM memories WHERE status = #{status} ORDER BY updated_at DESC")
    List<Memory> findByStatus(String status);

    /**
     * 根据 ID 删除 Memory
     */
    @Delete("DELETE FROM memories WHERE memory_id = #{memoryId}")
    int deleteByMemoryId(String memoryId);

    /**
     * 统计 Memory 数量
     */
    @Select("SELECT COUNT(*) FROM memories")
    int count();

    /**
     * 根据类型统计
     */
    @Select("SELECT COUNT(*) FROM memories WHERE type = #{type}")
    int countByType(String type);
}

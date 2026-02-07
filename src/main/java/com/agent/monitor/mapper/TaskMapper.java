package com.agent.monitor.mapper;

import com.agent.monitor.entity.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Task Mapper
 */
@Mapper
public interface TaskMapper {

    /**
     * 插入任务
     */
    @Insert("INSERT INTO tasks (task_id, name, description, type, status, priority, " +
            "agent_id, memory_id, input, created_by, crew_id, created_at, updated_at) " +
            "VALUES (#{taskId}, #{name}, #{description}, #{type}, #{status}, #{priority}, " +
            "#{agentId}, #{memoryId}, #{input}, #{createdBy}, #{crewId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task task);

    /**
     * 更新任务
     */
    @Update("UPDATE tasks SET status=#{status}, progress=#{progress}, output=#{output}, " +
            "error=#{error}, started_at=#{startedAt}, completed_at=#{completedAt}, " +
            "updated_at=#{updatedAt} WHERE task_id=#{taskId}")
    int update(Task task);

    /**
     * 根据 ID 查找任务
     */
    @Select("SELECT * FROM tasks WHERE task_id = #{taskId}")
    Task findByTaskId(String taskId);

    /**
     * 查找所有任务
     */
    @Select("SELECT * FROM tasks ORDER BY created_at DESC")
    List<Task> findAll();

    /**
     * 根据状态查找任务
     */
    @Select("SELECT * FROM tasks WHERE status = #{status} ORDER BY priority DESC, created_at ASC")
    List<Task> findByStatus(String status);

    /**
     * 根据 Agent ID 查找任务
     */
    @Select("SELECT * FROM tasks WHERE agent_id = #{agentId} ORDER BY created_at DESC")
    List<Task> findByAgentId(String agentId);

    /**
     * 根据 Memory ID 查找任务
     */
    @Select("SELECT * FROM tasks WHERE memory_id = #{memoryId} ORDER BY created_at DESC")
    List<Task> findByMemoryId(String memoryId);

    /**
     * 根据 Crew ID 查找任务
     */
    @Select("SELECT * FROM tasks WHERE crew_id = #{crewId} ORDER BY priority DESC, created_at ASC")
    List<Task> findByCrewId(String crewId);

    /**
     * 删除任务
     */
    @Delete("DELETE FROM tasks WHERE task_id = #{taskId}")
    int deleteByTaskId(String taskId);

    /**
     * 统计任务数量
     */
    @Select("SELECT COUNT(*) FROM tasks")
    int count();

    /**
     * 根据状态统计
     */
    @Select("SELECT COUNT(*) FROM tasks WHERE status = #{status}")
    int countByStatus(String status);
}

package com.agent.monitor.mapper;

import com.agent.monitor.entity.AgentExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent 执行记录 Mapper
 * Design Document: 05-agent-behavior.md
 */
@Mapper
public interface AgentExecutionMapper {

    /**
     * 插入执行记录
     */
    int insert(AgentExecution execution);

    /**
     * 更新执行记录
     */
    int update(AgentExecution execution);

    /**
     * 根据 executionId 查询
     */
    AgentExecution findByExecutionId(String executionId);

    /**
     * 根据 agentId 查询执行记录
     */
    List<AgentExecution> findByAgentId(String agentId);

    /**
     * 根据 memoryId 查询执行记录
     */
    List<AgentExecution> findByMemoryId(String memoryId);

    /**
     * 根据 toolId 查询执行记录
     */
    List<AgentExecution> findByToolId(String toolId);

    /**
     * 根据 status 查询执行记录
     */
    List<AgentExecution> findByStatus(String status);

    /**
     * 查询最近的执行记录
     */
    List<AgentExecution> findRecent(@Param("limit") int limit);
}

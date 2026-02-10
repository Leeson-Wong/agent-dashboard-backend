package com.agent.monitor.mapper;

import com.agent.monitor.entity.AgentState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent 状态 Mapper
 */
@Mapper
public interface AgentStateMapper {

    /**
     * 插入 Agent 状态
     */
    int insert(AgentState agentState);

    /**
     * 更新 Agent 状态
     */
    int update(AgentState agentState);

    /**
     * 根据 Agent ID 查找
     */
    AgentState findByAgentId(@Param("agentId") String agentId);

    /**
     * 根据服务器 ID 查找所有 Agent
     */
    List<AgentState> findByServerId(@Param("serverId") String serverId);

    /**
     * 根据状态查找所有 Agent
     */
    List<AgentState> findByStatus(@Param("status") String status);

    /**
     * 查找所有 Agent
     */
    List<AgentState> findAll();

    /**
     * 统计在线 Agent 数量
     */
    int countByStatus(@Param("status") String status);

    /**
     * 统计总 Agent 数量
     */
    int countAll();

    /**
     * 根据 Agent ID 删除
     */
    int deleteByAgentId(@Param("agentId") String agentId);

    /**
     * 更新收藏状态
     */
    int updateFavoriteStatus(@Param("agentId") String agentId, @Param("isFavorite") Boolean isFavorite);

    /**
     * 查找所有收藏的 Agent
     */
    List<AgentState> findFavorites();

    /**
     * 更新备注
     */
    int updateNotes(@Param("agentId") String agentId, @Param("notes") String notes);

    /**
     * 更新标签
     */
    int updateTags(@Param("agentId") String agentId, @Param("tags") String tags);
}

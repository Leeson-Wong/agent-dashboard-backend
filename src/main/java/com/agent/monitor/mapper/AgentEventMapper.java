package com.agent.monitor.mapper;

import com.agent.monitor.entity.AgentEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent 事件流 Mapper
 * Design Document: 02-snapshot-delta-sync.md
 */
@Mapper
public interface AgentEventMapper {

    /**
     * 插入新事件
     */
    int insert(AgentEvent event);

    /**
     * 根据 ID 查找事件
     */
    AgentEvent findById(@Param("id") Long id);

    /**
     * 根据序列号查找事件
     */
    AgentEvent findBySeq(@Param("seq") Long seq);

    /**
     * 获取指定序列号之后的事件
     */
    List<AgentEvent> findAfterSeq(@Param("seq") Long seq, @Param("limit") Integer limit);

    /**
     * 根据事件类型查找事件
     */
    List<AgentEvent> findByEventType(@Param("eventType") String eventType, @Param("limit") Integer limit);

    /**
     * 根据 Agent ID 查找事件
     */
    List<AgentEvent> findByAgentId(@Param("agentId") String agentId, @Param("limit") Integer limit);

    /**
     * 删除过期事件 (超过指定时间)
     */
    int deleteExpired(@Param("hours") int hours);

    /**
     * 获取最大序列号
     */
    Long getMaxSeq();
}

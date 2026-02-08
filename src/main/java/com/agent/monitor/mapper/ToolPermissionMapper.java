package com.agent.monitor.mapper;

import com.agent.monitor.entity.ToolPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工具权限配置 Mapper
 * Design Document: 05-agent-behavior.md
 */
@Mapper
public interface ToolPermissionMapper {

    /**
     * 插入权限配置
     */
    int insert(ToolPermission permission);

    /**
     * 更新权限配置
     */
    int update(ToolPermission permission);

    /**
     * 根据 agentId 查询
     */
    ToolPermission findByAgentId(String agentId);

    /**
     * 删除权限配置
     */
    int deleteByAgentId(String agentId);
}

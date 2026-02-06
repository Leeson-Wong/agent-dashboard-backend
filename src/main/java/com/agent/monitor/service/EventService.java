package com.agent.monitor.service;

import com.agent.monitor.dto.MonitorEventDTO;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 事件处理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final AgentStateMapper agentStateMapper;

    /**
     * 处理单个事件
     */
    @Transactional
    public void processEvent(MonitorEventDTO event) {
        String eventType = event.getEvent().getType();

        switch (eventType) {
            case "agent_online":
                handleAgentOnline(event);
                break;
            case "agent_offline":
                handleAgentOffline(event);
                break;
            case "agent_working":
                handleAgentWorking(event);
                break;
            case "agent_error":
                handleAgentError(event);
                break;
            default:
                log.debug("未处理的事件类型: {}", eventType);
        }
    }

    /**
     * 批量处理事件
     */
    @Transactional
    public void processEvents(List<MonitorEventDTO> events) {
        log.info("批量处理 {} 个事件", events.size());

        for (MonitorEventDTO event : events) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("处理事件失败: {}", event, e);
            }
        }
    }

    /**
     * 处理 Agent 上线事件
     */
    private void handleAgentOnline(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();

        AgentState state = new AgentState();
        state.setAgentId(agentId);
        state.setServerId(event.getSource().getServerId());
        state.setFramework(event.getSource().getFramework());
        state.setLanguage(event.getSource().getLanguage());
        state.setStatus("online");
        state.setLastActivity(Instant.now());

        // 从事件数据中提取角色信息
        Object role = event.getEvent().getData().get("role");
        if (role != null) {
            state.setRole(role.toString());
        }

        agentStateMapper.insert(state);
        log.info("Agent 上线: {} ({})", agentId, state.getRole());
    }

    /**
     * 处理 Agent 离线事件
     */
    private void handleAgentOffline(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();

        AgentState state = agentStateMapper.findByAgentId(agentId);
        if (state == null) {
            log.warn("Agent 不存在: {}", agentId);
            return;
        }

        state.setStatus("offline");
        state.setCurrentActivity(null);
        state.setLastActivity(Instant.now());

        agentStateMapper.update(state);
        log.info("Agent 离线: {}", agentId);
    }

    /**
     * 处理 Agent 工作事件
     */
    private void handleAgentWorking(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();

        AgentState state = new AgentState();
        state.setAgentId(agentId);
        state.setServerId(event.getSource().getServerId());
        state.setFramework(event.getSource().getFramework());
        state.setStatus("online");
        state.setLastActivity(Instant.now());

        // 从事件数据中提取任务描述
        Object task = event.getEvent().getData().get("task");
        if (task != null) {
            state.setCurrentActivity(task.toString());
        }

        agentStateMapper.insert(state);
        log.debug("Agent 工作中: {} - {}", agentId, state.getCurrentActivity());
    }

    /**
     * 处理 Agent 错误事件
     */
    private void handleAgentError(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();

        AgentState state = new AgentState();
        state.setAgentId(agentId);
        state.setServerId(event.getSource().getServerId());
        state.setStatus("error");
        state.setLastActivity(Instant.now());

        Object error = event.getEvent().getData().get("error");
        if (error != null) {
            state.setCurrentActivity("Error: " + error);
        }

        agentStateMapper.insert(state);
        log.warn("Agent 错误: {} - {}", agentId, state.getCurrentActivity());
    }
}

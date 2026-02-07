package com.agent.monitor.service;

import com.agent.monitor.dto.MonitorEventDTO;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import com.agent.monitor.websocket.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 事件处理服务 - 支持 CrewAI 事件映射
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final AgentStateMapper agentStateMapper;
    private final WebSocketMessageSender webSocketMessageSender;

    /**
     * 处理单个事件
     */
    @Transactional
    public void processEvent(MonitorEventDTO event) {
        String eventType = event.getEvent().getType();

        // 原有事件类型
        switch (eventType) {
            case "agent_online":
                handleAgentOnline(event);
                return;
            case "agent_offline":
                handleAgentOffline(event);
                return;
            case "agent_working":
                handleAgentWorking(event);
                return;
            case "agent_error":
                handleAgentError(event);
                return;
        }

        // CrewAI 事件类型映射
        processCrewAIEvent(event);
    }

    /**
     * 处理 CrewAI 事件
     */
    private void processCrewAIEvent(MonitorEventDTO event) {
        String eventType = event.getEvent().getType();

        switch (eventType) {
            // Crew 级别事件
            case "crew_started":
                handleCrewStarted(event);
                break;
            case "crew_completed":
                handleCrewCompleted(event);
                break;
            case "crew_failed":
                handleCrewFailed(event);
                break;

            // Agent 执行事件
            case "agent_execution_started":
                handleAgentExecutionStarted(event);
                break;
            case "agent_execution_completed":
                handleAgentExecutionCompleted(event);
                break;

            // 思考状态事件
            case "agent_thinking":
                handleAgentThinking(event);
                break;

            // 工具使用事件
            case "tool_usage_started":
                handleToolUsageStarted(event);
                break;
            case "tool_usage_finished":
                handleToolUsageFinished(event);
                break;

            default:
                log.debug("未处理的 CrewAI 事件类型: {}", eventType);
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

    // ========================================================================
    // CrewAI 事件处理方法
    // ========================================================================

    /**
     * 处理 Crew 开始事件
     */
    private void handleCrewStarted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("initializing");
        state.setCurrentActivity("Crew 初始化中");
        state.setLastActivity(Instant.now());
        agentStateMapper.update(state);
        log.info("Crew 开始: {}", agentId);
    }

    /**
     * 处理 Crew 完成事件
     */
    private void handleCrewCompleted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("ready");
        state.setCurrentActivity("Crew 任务完成");
        state.setLastActivity(Instant.now());
        agentStateMapper.update(state);
        log.info("Crew 完成: {}", agentId);
    }

    /**
     * 处理 Crew 失败事件
     */
    private void handleCrewFailed(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("error");
        Object error = event.getEvent().getData().get("error");
        state.setCurrentActivity(error != null ? "Crew 失败: " + error : "Crew 失败");
        state.setLastActivity(Instant.now());
        agentStateMapper.update(state);
        log.warn("Crew 失败: {} - {}", agentId, state.getCurrentActivity());
    }

    /**
     * 处理 Agent 执行开始事件
     */
    private void handleAgentExecutionStarted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        Map<String, Object> data = event.getEvent().getData();

        AgentState state = getOrCreateAgentState(event);
        state.setStatus("busy");
        Object task = data.get("task");
        state.setCurrentActivity(task != null ? "执行任务: " + task : "执行任务中");
        state.setLastActivity(Instant.now());

        agentStateMapper.update(state);
        webSocketMessageSender.broadcastAgentUpdate(state);
        log.info("Agent 开始执行: {} - {}", agentId, state.getCurrentActivity());
    }

    /**
     * 处理 Agent 执行完成事件
     */
    private void handleAgentExecutionCompleted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("ready");
        state.setCurrentActivity("任务完成");
        state.setLastActivity(Instant.now());
        agentStateMapper.update(state);
        webSocketMessageSender.broadcastAgentUpdate(state);
        log.info("Agent 完成执行: {}", agentId);
    }

    /**
     * 处理 Agent 思考状态事件
     */
    private void handleAgentThinking(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        Map<String, Object> data = event.getEvent().getData();
        String action = (String) data.get("action");

        AgentState state = getOrCreateAgentState(event);

        if ("completed".equals(action)) {
            // 思考完成，恢复在线状态
            state.setStatus("online");
            state.setCurrentActivity("在线 - 就绪");
            Object tokensUsed = data.get("tokens_used");
            if (tokensUsed != null) {
                state.setCurrentActivity("在线 - 就绪 (tokens: " + tokensUsed + ")");
            }
            log.debug("Agent 思考完成: {}", agentId);
        } else {
            // 开始思考
            state.setStatus("thinking");
            Object model = data.get("model");
            state.setCurrentActivity("思考中" + (model != null ? " (模型: " + model + ")" : ""));
            log.debug("Agent 思考中: {}", agentId);
        }

        state.setLastActivity(Instant.now());
        agentStateMapper.update(state);
        webSocketMessageSender.broadcastAgentUpdate(state);
    }

    /**
     * 处理工具使用开始事件
     */
    private void handleToolUsageStarted(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        Map<String, Object> data = event.getEvent().getData();

        AgentState state = getOrCreateAgentState(event);
        state.setStatus("busy");
        Object toolName = data.get("tool_name");
        state.setCurrentActivity(toolName != null ? "使用工具: " + toolName : "使用工具中");

        // 保存当前工具信息
        if (toolName != null) {
            try {
                state.setCurrentTool(toolName.toString());
            } catch (Exception e) {
                log.debug("设置当前工具失败（字段可能不存在）: {}", e.getMessage());
            }
        }
        state.setLastActivity(Instant.now());

        agentStateMapper.update(state);
        webSocketMessageSender.broadcastAgentUpdate(state);
        log.info("Agent 使用工具: {} - {}", agentId, state.getCurrentActivity());
    }

    /**
     * 处理工具使用完成事件
     */
    private void handleToolUsageFinished(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);

        // 清除当前工具
        try {
            state.setCurrentTool(null);
        } catch (Exception e) {
            log.debug("清除当前工具失败（字段可能不存在）: {}", e.getMessage());
        }

        state.setCurrentActivity("工具执行完成");
        state.setLastActivity(Instant.now());
        agentStateMapper.update(state);
        log.info("Agent 工具使用完成: {}", agentId);
    }

    /**
     * 获取或创建 Agent 状态
     */
    private AgentState getOrCreateAgentState(MonitorEventDTO event) {
        String agentId = event.getSource().getAgentId();
        AgentState state = agentStateMapper.findByAgentId(agentId);
        if (state == null) {
            state = new AgentState();
            state.setAgentId(agentId);
            state.setServerId(event.getSource().getServerId());
            state.setFramework(event.getSource().getFramework());
            state.setLanguage(event.getSource().getLanguage());
            state.setCreatedAt(Instant.now());
            agentStateMapper.insert(state);
        }
        return state;
    }
}

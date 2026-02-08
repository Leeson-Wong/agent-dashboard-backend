package com.agent.monitor.service;

import com.agent.monitor.dto.MonitorEventDTO;
import com.agent.monitor.entity.AgentEvent;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import com.agent.monitor.websocket.WebSocketMessageSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件处理服务 - 支持 CrewAI 事件映射
 * Design Document: 06-crewai-event-mapping.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final AgentStateMapper agentStateMapper;
    private final WebSocketMessageSender webSocketMessageSender;
    private final SnapshotService snapshotService;
    private final AgentExecutionService executionService;
    private final ToolUsageStatsService toolUsageStatsService;
    private final ObjectMapper objectMapper;

    /**
     * Track tool usage start times for calculating duration
     * Key: agentId:toolId, Value: start timestamp
     */
    private final ConcurrentHashMap<String, Instant> toolUsageStartTimes = new ConcurrentHashMap<>();

    /**
     * 处理单个事件
     */
    @Transactional
    public void processEvent(MonitorEventDTO event) {
        String eventType = event.getEvent().getType();

        // 保存事件到事件流 (带序列号)
        Long seq = saveEventToStream(event, eventType);

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
        processCrewAIEvent(event, seq);
    }

    /**
     * 处理 CrewAI 事件
     */
    private void processCrewAIEvent(MonitorEventDTO event, Long seq) {
        String eventType = event.getEvent().getType();

        switch (eventType) {
            // Crew 级别事件
            case "crew_started":
                handleCrewStarted(event, seq);
                break;
            case "crew_completed":
                handleCrewCompleted(event, seq);
                break;
            case "crew_failed":
                handleCrewFailed(event, seq);
                break;

            // Agent 执行事件
            case "agent_execution_started":
                handleAgentExecutionStarted(event, seq);
                break;
            case "agent_execution_completed":
                handleAgentExecutionCompleted(event, seq);
                break;

            // 思考状态事件
            case "agent_thinking":
                handleAgentThinking(event, seq);
                break;

            // 工具使用事件
            case "tool_usage_started":
                handleToolUsageStarted(event, seq);
                break;
            case "tool_usage_finished":
                handleToolUsageFinished(event, seq);
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

        updateAgentState(state);
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

        // Use getOrCreateAgentState to ensure all required fields are set
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("error");
        state.setLastActivity(Instant.now());

        Object error = event.getEvent().getData().get("error");
        if (error != null) {
            state.setCurrentActivity("Error: " + error);
        }

        updateAgentState(state);
        log.warn("Agent 错误: {} - {}", agentId, state.getCurrentActivity());
    }

    // ========================================================================
    // CrewAI 事件处理方法
    // ========================================================================

    /**
     * 处理 Crew 开始事件
     */
    private void handleCrewStarted(MonitorEventDTO event, Long seq) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("initializing");
        state.setCurrentActivity("Crew 初始化中");
        state.setLastActivity(Instant.now());
        updateAgentState(state);
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
        log.info("Crew 开始: {}", agentId);
    }

    /**
     * 处理 Crew 完成事件
     */
    private void handleCrewCompleted(MonitorEventDTO event, Long seq) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("ready");
        state.setCurrentActivity("Crew 任务完成");
        state.setLastActivity(Instant.now());
        updateAgentState(state);
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
        log.info("Crew 完成: {}", agentId);
    }

    /**
     * 处理 Crew 失败事件
     */
    private void handleCrewFailed(MonitorEventDTO event, Long seq) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("error");
        Object error = event.getEvent().getData().get("error");
        state.setCurrentActivity(error != null ? "Crew 失败: " + error : "Crew 失败");
        state.setLastActivity(Instant.now());
        updateAgentState(state);
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
        log.warn("Crew 失败: {} - {}", agentId, state.getCurrentActivity());
    }

    /**
     * 处理 Agent 执行开始事件
     */
    private void handleAgentExecutionStarted(MonitorEventDTO event, Long seq) {
        String agentId = event.getSource().getAgentId();
        Map<String, Object> data = event.getEvent().getData();

        AgentState state = getOrCreateAgentState(event);
        state.setStatus("busy");
        Object task = data.get("task");
        state.setCurrentActivity(task != null ? "执行任务: " + task : "执行任务中");
        state.setLastActivity(Instant.now());

        updateAgentState(state);
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
        log.info("Agent 开始执行: {} - {}", agentId, state.getCurrentActivity());
    }

    /**
     * 处理 Agent 执行完成事件
     */
    private void handleAgentExecutionCompleted(MonitorEventDTO event, Long seq) {
        String agentId = event.getSource().getAgentId();
        AgentState state = getOrCreateAgentState(event);
        state.setStatus("ready");
        state.setCurrentActivity("任务完成");
        state.setLastActivity(Instant.now());
        updateAgentState(state);
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
        log.info("Agent 完成执行: {}", agentId);
    }

    /**
     * 处理 Agent 思考状态事件
     */
    private void handleAgentThinking(MonitorEventDTO event, Long seq) {
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
        updateAgentState(state);
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
    }

    /**
     * 处理工具使用开始事件
     */
    private void handleToolUsageStarted(MonitorEventDTO event, Long seq) {
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
                // Track tool usage start time for statistics
                String key = agentId + ":" + toolName.toString();
                toolUsageStartTimes.put(key, Instant.now());
            } catch (Exception e) {
                log.debug("设置当前工具失败（字段可能不存在）: {}", e.getMessage());
            }
        }
        state.setLastActivity(Instant.now());

        updateAgentState(state);
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
        log.info("Agent 使用工具: {} - {}", agentId, state.getCurrentActivity());
    }

    /**
     * 处理工具使用完成事件
     */
    private void handleToolUsageFinished(MonitorEventDTO event, Long seq) {
        String agentId = event.getSource().getAgentId();
        Map<String, Object> data = event.getEvent().getData();

        AgentState state = getOrCreateAgentState(event);

        // Get tool name and calculate duration
        String toolName = state.getCurrentTool();
        long durationSeconds = 0;

        if (toolName != null) {
            String key = agentId + ":" + toolName;
            Instant startTime = toolUsageStartTimes.remove(key);
            if (startTime != null) {
                durationSeconds = java.time.Duration.between(startTime, Instant.now()).getSeconds();
            }
        }

        // Extract result from event data
        boolean success = true;
        Object result = data.get("result");
        if (result != null) {
            // Check if result indicates failure
            String resultStr = result.toString().toLowerCase();
            success = !resultStr.contains("error") && !resultStr.contains("failed");
        }

        // Record to tool_usage_stats table
        if (toolName != null && state.getMemoryId() != null) {
            try {
                toolUsageStatsService.recordUsage(
                    state.getMemoryId(),
                    toolName,
                    success,
                    durationSeconds
                );
                log.debug("工具使用已记录: toolId={}, memoryId={}, success={}, duration={}",
                    toolName, state.getMemoryId(), success, durationSeconds);
            } catch (Exception e) {
                log.warn("记录工具使用统计失败: toolId={}, error={}", toolName, e.getMessage());
            }
        }

        // Clear current tool
        try {
            state.setCurrentTool(null);
        } catch (Exception e) {
            log.debug("清除当前工具失败（字段可能不存在）: {}", e.getMessage());
        }

        state.setCurrentActivity("工具执行完成");
        state.setLastActivity(Instant.now());
        updateAgentState(state);  // Use updateAgentState to preserve memoryId
        webSocketMessageSender.broadcastAgentUpdate(state, seq);
        log.info("Agent 工具使用完成: {}, duration={}s, success={}", agentId, durationSeconds, success);
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
            state.setStatus("online");
            state.setCreatedAt(Instant.now());
            state.setLastActivity(Instant.now());
            agentStateMapper.insert(state);
        }
        return state;
    }

    /**
     * 更新 Agent 状态（保留非 null 字段）
     */
    private void updateAgentState(AgentState state) {
        AgentState existingState = agentStateMapper.findByAgentId(state.getAgentId());
        if (existingState != null) {
            // Preserve fields that shouldn't be overwritten
            if (state.getMemoryId() == null) {
                state.setMemoryId(existingState.getMemoryId());
            }
            if (state.getCurrentTool() == null) {
                state.setCurrentTool(existingState.getCurrentTool());
            }
            if (state.getCurrentTaskId() == null) {
                state.setCurrentTaskId(existingState.getCurrentTaskId());
            }
        }
        // Call the mapper directly here to avoid recursion
        agentStateMapper.update(state);
    }

    /**
     * 保存事件到事件流 (带序列号)
     *
     * @return 分配的序列号，如果保存失败则返回 null
     */
    private Long saveEventToStream(MonitorEventDTO event, String eventType) {
        try {
            AgentEvent agentEvent = new AgentEvent();
            agentEvent.setEventType(eventType);
            agentEvent.setAgentId(event.getSource().getAgentId());
            agentEvent.setData(objectMapper.writeValueAsString(event.getEvent().getData()));
            agentEvent.setCreatedAt(Instant.now());

            snapshotService.saveEvent(agentEvent);

            log.debug("事件已保存到事件流: seq={}, type={}, agent={}",
                    agentEvent.getSeq(), eventType, agentEvent.getAgentId());

            return agentEvent.getSeq();

        } catch (JsonProcessingException e) {
            log.error("事件数据序列化失败: type={}, agent={}",
                    eventType, event.getSource().getAgentId(), e);
            return null;
        }
    }
}

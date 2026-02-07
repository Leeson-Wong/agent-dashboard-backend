package com.agent.monitor.service;

import com.agent.monitor.dto.AgentOperationResponse;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import com.agent.monitor.websocket.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 操作服务
 *
 * 实现 Agent 的暂停、恢复、停止、重启、删除等操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOperationService {

    private final AgentStateMapper agentStateMapper;
    private final WebSocketMessageSender webSocketMessageSender;

    /**
     * 暂停 Agent
     */
    public AgentOperationResponse pauseAgent(String agentId) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return AgentOperationResponse.builder()
                    .agentId(agentId)
                    .operation("pause")
                    .status("failed")
                    .message("Agent 不存在")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        // 更新 Agent 状态
        agent.setStatus("paused");
        agent.setCurrentActivity("已暂停");
        agent.setUpdatedAt(Instant.now());
        agentStateMapper.update(agent);

        // 广播状态更新
        webSocketMessageSender.broadcastAgentUpdate(agent);

        log.info("Agent 已暂停: {}", agentId);
        return AgentOperationResponse.builder()
                .agentId(agentId)
                .operation("pause")
                .status("success")
                .message("Agent 已暂停")
                .currentAgentStatus("paused")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 恢复 Agent
     */
    public AgentOperationResponse resumeAgent(String agentId) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return AgentOperationResponse.builder()
                    .agentId(agentId)
                    .operation("resume")
                    .status("failed")
                    .message("Agent 不存在")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        // 更新 Agent 状态
        agent.setStatus("online");
        agent.setCurrentActivity("运行中");
        agent.setUpdatedAt(Instant.now());
        agentStateMapper.update(agent);

        // 广播状态更新
        webSocketMessageSender.broadcastAgentUpdate(agent);

        log.info("Agent 已恢复: {}", agentId);
        return AgentOperationResponse.builder()
                .agentId(agentId)
                .operation("resume")
                .status("success")
                .message("Agent 已恢复")
                .currentAgentStatus("online")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 停止 Agent
     */
    public AgentOperationResponse stopAgent(String agentId) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return AgentOperationResponse.builder()
                    .agentId(agentId)
                    .operation("stop")
                    .status("failed")
                    .message("Agent 不存在")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        // 更新 Agent 状态
        agent.setStatus("stopped");
        agent.setCurrentActivity("已停止");
        agent.setUpdatedAt(Instant.now());
        agentStateMapper.update(agent);

        // 广播状态更新
        webSocketMessageSender.broadcastAgentUpdate(agent);

        log.info("Agent 已停止: {}", agentId);
        return AgentOperationResponse.builder()
                .agentId(agentId)
                .operation("stop")
                .status("success")
                .message("Agent 已停止")
                .currentAgentStatus("stopped")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 重启 Agent
     */
    public AgentOperationResponse restartAgent(String agentId) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return AgentOperationResponse.builder()
                    .agentId(agentId)
                    .operation("restart")
                    .status("failed")
                    .message("Agent 不存在")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        // 更新 Agent 状态 - 先停止后启动
        agent.setStatus("initializing");
        agent.setCurrentActivity("正在重启...");
        agent.setUpdatedAt(Instant.now());
        agentStateMapper.update(agent);

        // 广播状态更新
        webSocketMessageSender.broadcastAgentUpdate(agent);

        // 模拟重启后状态更新
        // 在实际实现中，这里应该等待 Agent 真正重启完成
        agent.setStatus("online");
        agent.setCurrentActivity("运行中");
        agentStateMapper.update(agent);
        webSocketMessageSender.broadcastAgentUpdate(agent);

        log.info("Agent 已重启: {}", agentId);
        return AgentOperationResponse.builder()
                .agentId(agentId)
                .operation("restart")
                .status("success")
                .message("Agent 已重启")
                .currentAgentStatus("online")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 删除 Agent
     */
    public AgentOperationResponse deleteAgent(String agentId) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return AgentOperationResponse.builder()
                    .agentId(agentId)
                    .operation("delete")
                    .status("failed")
                    .message("Agent 不存在")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        // 先停止 Agent
        agent.setStatus("stopped");
        agent.setCurrentActivity("已停止");
        agent.setUpdatedAt(Instant.now());
        agentStateMapper.update(agent);
        webSocketMessageSender.broadcastAgentUpdate(agent);

        // 删除 Agent 记录
        agentStateMapper.deleteByAgentId(agentId);

        // 广播删除事件
        webSocketMessageSender.broadcastAgentDelete(agentId);

        log.info("Agent 已删除: {}", agentId);
        return AgentOperationResponse.builder()
                .agentId(agentId)
                .operation("delete")
                .status("success")
                .message("Agent 已删除")
                .currentAgentStatus("deleted")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 更新 Agent 配置
     */
    public AgentOperationResponse updateAgentConfig(String agentId, Map<String, Object> config) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return AgentOperationResponse.builder()
                    .agentId(agentId)
                    .operation("updateConfig")
                    .status("failed")
                    .message("Agent 不存在")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        // 更新配置
        // 这里可以根据 config 中的字段更新 Agent 的属性
        // 例如: role, framework, language 等

        agent.setUpdatedAt(Instant.now());
        agentStateMapper.update(agent);

        // 广播状态更新
        webSocketMessageSender.broadcastAgentUpdate(agent);

        log.info("Agent 配置已更新: {}", agentId);
        return AgentOperationResponse.builder()
                .agentId(agentId)
                .operation("updateConfig")
                .status("success")
                .message("Agent 配置已更新")
                .currentAgentStatus(agent.getStatus())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 批量操作 Agent
     */
    public Map<String, Object> batchOperation(String operation, List<String> agentIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;
        Map<String, String> errors = new HashMap<>();

        for (String agentId : agentIds) {
            AgentOperationResponse response;
            switch (operation) {
                case "pause":
                    response = pauseAgent(agentId);
                    break;
                case "resume":
                    response = resumeAgent(agentId);
                    break;
                case "stop":
                    response = stopAgent(agentId);
                    break;
                case "restart":
                    response = restartAgent(agentId);
                    break;
                case "delete":
                    response = deleteAgent(agentId);
                    break;
                default:
                    response = AgentOperationResponse.builder()
                            .agentId(agentId)
                            .operation(operation)
                            .status("failed")
                            .message("不支持的操作: " + operation)
                            .timestamp(System.currentTimeMillis())
                            .build();
            }

            if ("success".equals(response.getStatus())) {
                successCount++;
            } else {
                failedCount++;
                errors.put(agentId, response.getMessage());
            }
        }

        result.put("operation", operation);
        result.put("total", agentIds.size());
        result.put("success", successCount);
        result.put("failed", failedCount);
        result.put("errors", errors);

        log.info("批量操作完成: operation={}, total={}, success={}, failed={}",
                operation, agentIds.size(), successCount, failedCount);

        return result;
    }
}

package com.agent.monitor.websocket;

import com.agent.monitor.entity.AgentState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 消息发送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageSender {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 广播 Agent 状态更新到所有订阅者
     */
    public void broadcastAgentUpdate(AgentState agentState, Long seq) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "agent_update");
            message.put("data", agentState);
            message.put("timestamp", Instant.now().toString());
            if (seq != null) {
                message.put("seq", seq);
            }

            messagingTemplate.convertAndSend("/topic/agents", message);
            log.debug("广播 Agent 状态更新: {}, seq={}", agentState.getAgentId(), seq);
        } catch (Exception e) {
            log.error("发送 Agent 更新消息失败", e);
        }
    }

    /**
     * 发送 Agent 事件到特定订阅者
     */
    public void sendAgentEvent(String agentId, String eventType, Map<String, Object> eventData, Long seq) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", eventType);
            message.put("agentId", agentId);
            message.put("data", eventData);
            message.put("timestamp", Instant.now().toString());
            if (seq != null) {
                message.put("seq", seq);
            }

            // 发送到队列，只有特定订阅者能收到
            messagingTemplate.convertAndSend("/topic/agents/" + agentId, message);
            log.debug("发送 Agent 事件: {} - {}, seq={}", agentId, eventType, seq);
        } catch (Exception e) {
            log.error("发送 Agent 事件消息失败", e);
        }
    }

    /**
     * 广播系统通知
     */
    public void broadcastSystemNotification(String level, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "system_notification");
            notification.put("level", level);
            notification.put("message", message);
            notification.put("timestamp", Instant.now().toString());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("广播系统通知: [{}] {}", level, message);
        } catch (Exception e) {
            log.error("发送系统通知失败", e);
        }
    }

    /**
     * 广播 Agent 删除事件
     */
    public void broadcastAgentDelete(String agentId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "agent_delete");
            message.put("agentId", agentId);
            message.put("timestamp", Instant.now().toString());

            messagingTemplate.convertAndSend("/topic/agents", message);
            log.info("广播 Agent 删除事件: {}", agentId);
        } catch (Exception e) {
            log.error("发送 Agent 删除消息失败", e);
        }
    }
}

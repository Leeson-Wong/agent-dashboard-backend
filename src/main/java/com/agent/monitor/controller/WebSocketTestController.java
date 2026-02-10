package com.agent.monitor.controller;

import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import com.agent.monitor.websocket.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * WebSocket 测试控制器
 *
 * 提供测试端点用于验证 WebSocket 功能
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketMessageSender webSocketMessageSender;
    private final AgentStateMapper agentStateMapper;

    /**
     * 广播测试消息
     *
     * POST /api/test/broadcast
     *
     * @param message 要广播的消息
     * @return 响应
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, String>> broadcastTestMessage(@RequestBody Map<String, String> message) {
        log.info("收到广播测试请求: {}", message);

        String text = message.getOrDefault("message", "测试消息");

        // 广播系统通知
        webSocketMessageSender.broadcastSystemNotification("info", text);

        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "测试消息已广播");
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * 触发 Agent 状态更新广播
     *
     * POST /api/test/agent-update/{agentId}
     *
     * @param agentId Agent ID
     * @return 响应
     */
    @PostMapping("/agent-update/{agentId}")
    public ResponseEntity<Map<String, Object>> triggerAgentUpdate(@PathVariable String agentId) {
        log.info("触发 Agent 状态更新广播: agentId={}", agentId);

        try {
            // 获取 Agent 状态
            AgentState agentState = agentStateMapper.findByAgentId(agentId);

            if (agentState == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Agent not found: " + agentId);
                return ResponseEntity.notFound().build();
            }

            // 广播 Agent 状态更新
            webSocketMessageSender.broadcastAgentUpdate(agentState, null);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("agentId", agentId);
            response.put("currentStatus", agentState.getStatus());
            response.put("timestamp", Instant.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("触发 Agent 更新失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 广播模拟 Agent 状态
     *
     * POST /api/test/mock-agent-update
     *
     * @return 响应
     */
    @PostMapping("/mock-agent-update")
    public ResponseEntity<Map<String, Object>> broadcastMockAgentUpdate() {
        log.info("广播模拟 Agent 状态更新");

        // 创建模拟 Agent 状态
        AgentState mockAgent = new AgentState();
        mockAgent.setAgentId(UUID.randomUUID().toString());
        mockAgent.setServerId("test-server");
        mockAgent.setFramework("test-framework");
        mockAgent.setLanguage("python");
        mockAgent.setStatus("busy");
        mockAgent.setCurrentActivity("测试活动 - " + Instant.now().toString());
        mockAgent.setCurrentTool("test-tool");
        mockAgent.setRole("测试 Agent");
        mockAgent.setLastActivity(Instant.now());
        mockAgent.setCreatedAt(Instant.now());
        mockAgent.setUpdatedAt(Instant.now());

        // 广播模拟 Agent 状态
        webSocketMessageSender.broadcastAgentUpdate(mockAgent, null);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("agentId", mockAgent.getAgentId());
        response.put("message", "模拟 Agent 状态已广播");
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * 广播多条测试消息
     *
     * POST /api/test/broadcast-batch
     *
     * @param count 消息数量
     * @return 响应
     */
    @PostMapping("/broadcast-batch")
    public ResponseEntity<Map<String, Object>> broadcastBatch(@RequestParam(defaultValue = "5") int count) {
        log.info("广播 {} 条测试消息", count);

        for (int i = 0; i < count; i++) {
            webSocketMessageSender.broadcastSystemNotification("info", "测试消息 #" + (i + 1));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("count", count);
        response.put("message", "已广播 " + count + " 条测试消息");
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }
}

package com.agent.monitor.controller;

import com.agent.monitor.dto.AgentOperationResponse;
import com.agent.monitor.dto.ApiResponse;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import com.agent.monitor.service.AgentOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent Controller
 *
 * 提供 Agent 查询和操作接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentStateMapper agentStateMapper;
    private final AgentOperationService agentOperationService;

    /**
     * 获取所有 Agent
     */
    @GetMapping
    public ResponseEntity<List<AgentState>> getAllAgents() {
        List<AgentState> agents = agentStateMapper.findAll();
        return ResponseEntity.ok(agents);
    }

    /**
     * 根据 ID 获取 Agent
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentState> getAgent(@PathVariable String agentId) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agent);
    }

    /**
     * 获取在线 Agent
     */
    @GetMapping("/online")
    public ResponseEntity<List<AgentState>> getOnlineAgents() {
        List<AgentState> agents = agentStateMapper.findByStatus("online");
        return ResponseEntity.ok(agents);
    }

    /**
     * 根据服务器 ID 获取 Agent
     */
    @GetMapping("/server/{serverId}")
    public ResponseEntity<List<AgentState>> getAgentsByServer(@PathVariable String serverId) {
        List<AgentState> agents = agentStateMapper.findByServerId(serverId);
        return ResponseEntity.ok(agents);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long total = agentStateMapper.countAll();
        long online = agentStateMapper.countByStatus("online");
        long offline = agentStateMapper.countByStatus("offline");
        long error = agentStateMapper.countByStatus("error");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("online", online);
        stats.put("offline", offline);
        stats.put("error", error);

        return ResponseEntity.ok(stats);
    }

    // ========================================================================
    // Agent Operations
    // ========================================================================

    /**
     * 暂停 Agent
     */
    @PostMapping("/{agentId}/pause")
    public ResponseEntity<ApiResponse<AgentOperationResponse>> pauseAgent(@PathVariable String agentId) {
        log.info("暂停 Agent: {}", agentId);
        AgentOperationResponse response = agentOperationService.pauseAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 恢复 Agent
     */
    @PostMapping("/{agentId}/resume")
    public ResponseEntity<ApiResponse<AgentOperationResponse>> resumeAgent(@PathVariable String agentId) {
        log.info("恢复 Agent: {}", agentId);
        AgentOperationResponse response = agentOperationService.resumeAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 停止 Agent
     */
    @PostMapping("/{agentId}/stop")
    public ResponseEntity<ApiResponse<AgentOperationResponse>> stopAgent(@PathVariable String agentId) {
        log.info("停止 Agent: {}", agentId);
        AgentOperationResponse response = agentOperationService.stopAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 重启 Agent
     */
    @PostMapping("/{agentId}/restart")
    public ResponseEntity<ApiResponse<AgentOperationResponse>> restartAgent(@PathVariable String agentId) {
        log.info("重启 Agent: {}", agentId);
        AgentOperationResponse response = agentOperationService.restartAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除 Agent
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<ApiResponse<AgentOperationResponse>> deleteAgent(@PathVariable String agentId) {
        log.info("删除 Agent: {}", agentId);
        AgentOperationResponse response = agentOperationService.deleteAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新 Agent 配置
     */
    @PatchMapping("/{agentId}/config")
    public ResponseEntity<ApiResponse<AgentOperationResponse>> updateAgentConfig(
            @PathVariable String agentId,
            @RequestBody Map<String, Object> config) {
        log.info("更新 Agent 配置: {}, config: {}", agentId, config);
        AgentOperationResponse response = agentOperationService.updateAgentConfig(agentId, config);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 批量操作 Agent
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchOperation(@RequestBody Map<String, Object> request) {
        String operation = (String) request.get("operation");
        @SuppressWarnings("unchecked")
        List<String> agentIds = (List<String>) request.get("agentIds");

        log.info("批量操作 Agent: operation={}, agentIds={}", operation, agentIds);
        Map<String, Object> response = agentOperationService.batchOperation(operation, agentIds);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

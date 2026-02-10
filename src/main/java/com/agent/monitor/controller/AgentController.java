package com.agent.monitor.controller;

import com.agent.monitor.dto.AgentOperationResponse;
import com.agent.monitor.dto.ApiResponse;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import com.agent.monitor.service.AgentOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
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

    /**
     * 导出 Agent 数据
     * @param format 导出格式 (json 或 csv)
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAgents(@RequestParam(defaultValue = "json") String format) {
        log.info("导出 Agent 数据, format={}", format);

        List<AgentState> agents = agentStateMapper.findAll();

        try {
            byte[] data;
            String filename;
            String contentType;

            if ("csv".equalsIgnoreCase(format)) {
                // CSV 格式
                data = generateCsv(agents);
                filename = "agents_" + System.currentTimeMillis() + ".csv";
                contentType = "text/csv; charset=UTF-8";
            } else {
                // JSON 格式 (默认)
                data = generateJson(agents);
                filename = "agents_" + System.currentTimeMillis() + ".json";
                contentType = "application/json; charset=UTF-8";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(data.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);

        } catch (IOException e) {
            log.error("导出失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成 JSON 格式数据
     */
    private byte[] generateJson(List<AgentState> agents) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        for (int i = 0; i < agents.size(); i++) {
            AgentState agent = agents.get(i);
            json.append("  {\n");
            json.append("    \"agentId\": \"").append(escapeJson(agent.getAgentId())).append("\",\n");
            json.append("    \"serverId\": \"").append(escapeJson(agent.getServerId())).append("\",\n");
            json.append("    \"framework\": \"").append(escapeJson(agent.getFramework())).append("\",\n");
            json.append("    \"language\": \"").append(escapeJson(agent.getLanguage())).append("\",\n");
            json.append("    \"status\": \"").append(escapeJson(agent.getStatus())).append("\",\n");
            json.append("    \"currentActivity\": \"").append(escapeJson(agent.getCurrentActivity())).append("\",\n");
            json.append("    \"currentTool\": \"").append(escapeJson(agent.getCurrentTool())).append("\",\n");
            json.append("    \"role\": \"").append(escapeJson(agent.getRole())).append("\",\n");
            json.append("    \"lastActivity\": \"").append(agent.getLastActivity() != null ? agent.getLastActivity().toString() : "").append("\",\n");
            json.append("    \"createdAt\": \"").append(agent.getCreatedAt() != null ? agent.getCreatedAt().toString() : "").append("\",\n");
            json.append("    \"updatedAt\": \"").append(agent.getUpdatedAt() != null ? agent.getUpdatedAt().toString() : "").append("\"\n");
            json.append("  }").append(i < agents.size() - 1 ? "," : "").append("\n");
        }

        json.append("]");
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 生成 CSV 格式数据
     */
    private byte[] generateCsv(List<AgentState> agents) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // CSV 头部 (添加 BOM 以支持 Excel 正确显示中文)
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        out.write(bom);

        // 写入 CSV 表头
        String header = "Agent ID,Server ID,Framework,Language,Status,Current Activity,Current Tool,Role,Last Activity,Created At,Updated At\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));

        // 写入数据行
        for (AgentState agent : agents) {
            StringBuilder row = new StringBuilder();
            row.append(escapeCsv(agent.getAgentId())).append(",");
            row.append(escapeCsv(agent.getServerId())).append(",");
            row.append(escapeCsv(agent.getFramework())).append(",");
            row.append(escapeCsv(agent.getLanguage())).append(",");
            row.append(escapeCsv(agent.getStatus())).append(",");
            row.append(escapeCsv(agent.getCurrentActivity())).append(",");
            row.append(escapeCsv(agent.getCurrentTool())).append(",");
            row.append(escapeCsv(agent.getRole())).append(",");
            row.append(escapeCsv(agent.getLastActivity() != null ? agent.getLastActivity().toString() : "")).append(",");
            row.append(escapeCsv(agent.getCreatedAt() != null ? agent.getCreatedAt().toString() : "")).append(",");
            row.append(escapeCsv(agent.getUpdatedAt() != null ? agent.getUpdatedAt().toString() : "")).append("\n");
            out.write(row.toString().getBytes(StandardCharsets.UTF_8));
        }

        return out.toByteArray();
    }

    /**
     * 转义 JSON 字符串
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 转义 CSV 字符串
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        // 如果包含逗号、引号或换行符，用引号包裹并转义引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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

    // ========================================================================
    // Agent Favorites
    // ========================================================================

    /**
     * 获取所有收藏的 Agent
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<AgentState>> getFavoriteAgents() {
        List<AgentState> agents = agentStateMapper.findFavorites();
        return ResponseEntity.ok(agents);
    }

    /**
     * 切换 Agent 收藏状态
     */
    @PutMapping("/{agentId}/favorite")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleFavorite(
            @PathVariable String agentId,
            @RequestBody Map<String, Boolean> request) {
        Boolean isFavorite = request.get("isFavorite");
        if (isFavorite == null) {
            isFavorite = true;
        }

        log.info("更新 Agent 收藏状态: agentId={}, isFavorite={}", agentId, isFavorite);

        // 检查 Agent 是否存在
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }

        // 更新收藏状态
        agentStateMapper.updateFavoriteStatus(agentId, isFavorite);

        Map<String, Object> response = new HashMap<>();
        response.put("agentId", agentId);
        response.put("isFavorite", isFavorite);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取 Agent 收藏状态
     */
    @GetMapping("/{agentId}/favorite")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFavoriteStatus(@PathVariable String agentId) {
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("agentId", agentId);
        response.put("isFavorite", agent.getIsFavorite() != null ? agent.getIsFavorite() : false);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================================================================
    // Agent Notes
    // ========================================================================

    /**
     * 更新 Agent 备注信息
     */
    @PutMapping("/{agentId}/notes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateNotes(
            @PathVariable String agentId,
            @RequestBody Map<String, String> request) {
        String notes = request.get("notes");

        log.info("更新 Agent 备注: agentId={}, notesLength={}", agentId, notes != null ? notes.length() : 0);

        // 检查 Agent 是否存在
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }

        // 更新备注
        agentStateMapper.updateNotes(agentId, notes);

        Map<String, Object> response = new HashMap<>();
        response.put("agentId", agentId);
        response.put("notes", notes);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================================================================
    // Agent Tags
    // ========================================================================

    /**
     * 更新 Agent 标签
     */
    @PutMapping("/{agentId}/tags")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTags(
            @PathVariable String agentId,
            @RequestBody Map<String, String> request) {
        String tags = request.get("tags");

        log.info("更新 Agent 标签: agentId={}, tags={}", agentId, tags);

        // 检查 Agent 是否存在
        AgentState agent = agentStateMapper.findByAgentId(agentId);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }

        // 更新标签
        agentStateMapper.updateTags(agentId, tags);

        Map<String, Object> response = new HashMap<>();
        response.put("agentId", agentId);
        response.put("tags", tags);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

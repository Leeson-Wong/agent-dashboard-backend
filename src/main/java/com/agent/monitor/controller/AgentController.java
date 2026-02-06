package com.agent.monitor.controller;

import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 查询 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentStateMapper agentStateMapper;

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
}

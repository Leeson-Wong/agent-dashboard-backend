package com.agent.monitor.controller;

import com.agent.monitor.dto.*;
import com.agent.monitor.entity.AgentExecution;
import com.agent.monitor.service.AgentExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent Execution Controller
 *
 * Provides execution tracking API
 * Design Document: 05-agent-behavior.md
 */
@Slf4j
@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class AgentExecutionController {

    private final AgentExecutionService executionService;

    /**
     * 创建执行记录
     *
     * POST /api/executions
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AgentExecutionDTO>> createExecution(
            @RequestParam String agentId,
            @RequestBody CreateExecutionRequestDTO request) {
        log.info("创建执行记录: agentId={}, toolId={}", agentId, request.getToolId());

        try {
            String executionId = executionService.createExecution(
                    agentId,
                    request.getMemoryId(),
                    request.getTaskId(),
                    request.getTaskType(),
                    request.getTaskDescription(),
                    request.getToolId(),
                    request.getToolName(),
                    request.getToolCategory(),
                    request.getInput()
            );

            AgentExecution execution = executionService.getExecution(executionId);
            return ResponseEntity.ok(ApiResponse.success(convertToDTO(execution)));

        } catch (Exception e) {
            log.error("创建执行记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to create execution: " + e.getMessage()));
        }
    }

    /**
     * 开始执行
     *
     * POST /api/executions/{executionId}/start
     */
    @PostMapping("/{executionId}/start")
    public ResponseEntity<ApiResponse<Boolean>> startExecution(@PathVariable String executionId) {
        log.info("开始执行: executionId={}", executionId);

        try {
            boolean started = executionService.startExecution(executionId);
            return ResponseEntity.ok(ApiResponse.success(started));

        } catch (Exception e) {
            log.error("开始执行失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to start execution: " + e.getMessage()));
        }
    }

    /**
     * 完成执行
     *
     * POST /api/executions/{executionId}/complete
     */
    @PostMapping("/{executionId}/complete")
    public ResponseEntity<ApiResponse<Boolean>> completeExecution(
            @PathVariable String executionId,
            @RequestBody CompleteExecutionRequestDTO request) {
        log.info("完成执行: executionId={}, success={}", executionId, request.getSuccess());

        try {
            boolean completed = executionService.completeExecution(
                    executionId,
                    request.getOutput(),
                    request.getSuccess(),
                    request.getExitCode() != null ? request.getExitCode() : 0,
                    request.getExecutionTimeMs() != null ? request.getExecutionTimeMs() : 0,
                    request.getMemoryUsedMb() != null ? request.getMemoryUsedMb() : 0.0,
                    request.getTokensUsed() != null ? request.getTokensUsed() : 0
            );

            return ResponseEntity.ok(ApiResponse.success(completed));

        } catch (Exception e) {
            log.error("完成执行失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to complete execution: " + e.getMessage()));
        }
    }

    /**
     * 标记执行失败
     *
     * POST /api/executions/{executionId}/fail
     */
    @PostMapping("/{executionId}/fail")
    public ResponseEntity<ApiResponse<Boolean>> failExecution(
            @PathVariable String executionId,
            @RequestBody String errorMessage) {
        log.info("执行失败: executionId={}", executionId);

        try {
            boolean failed = executionService.failExecution(executionId, errorMessage);
            return ResponseEntity.ok(ApiResponse.success(failed));

        } catch (Exception e) {
            log.error("标记执行失败失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to mark execution as failed: " + e.getMessage()));
        }
    }

    /**
     * 获取执行记录
     *
     * GET /api/executions/{executionId}
     */
    @GetMapping("/{executionId}")
    public ResponseEntity<ApiResponse<AgentExecutionDTO>> getExecution(@PathVariable String executionId) {
        log.debug("获取执行记录: executionId={}", executionId);

        try {
            AgentExecution execution = executionService.getExecution(executionId);
            if (execution == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Execution not found"));
            }

            return ResponseEntity.ok(ApiResponse.success(convertToDTO(execution)));

        } catch (Exception e) {
            log.error("获取执行记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get execution: " + e.getMessage()));
        }
    }

    /**
     * 获取 Agent 的执行记录
     *
     * GET /api/executions/agent/{agentId}
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<ApiResponse<List<AgentExecutionDTO>>> getExecutionsByAgent(
            @PathVariable String agentId) {
        log.debug("获取 Agent 执行记录: agentId={}", agentId);

        try {
            List<AgentExecution> executions = executionService.getExecutionsByAgent(agentId);
            List<AgentExecutionDTO> dtos = executions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取 Agent 执行记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get executions: " + e.getMessage()));
        }
    }

    /**
     * 获取 Memory 的执行记录
     *
     * GET /api/executions/memory/{memoryId}
     */
    @GetMapping("/memory/{memoryId}")
    public ResponseEntity<ApiResponse<List<AgentExecutionDTO>>> getExecutionsByMemory(
            @PathVariable String memoryId) {
        log.debug("获取 Memory 执行记录: memoryId={}", memoryId);

        try {
            List<AgentExecution> executions = executionService.getExecutionsByMemory(memoryId);
            List<AgentExecutionDTO> dtos = executions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取 Memory 执行记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get executions: " + e.getMessage()));
        }
    }

    /**
     * 获取工具的执行记录
     *
     * GET /api/executions/tool/{toolId}
     */
    @GetMapping("/tool/{toolId}")
    public ResponseEntity<ApiResponse<List<AgentExecutionDTO>>> getExecutionsByTool(
            @PathVariable String toolId) {
        log.debug("获取工具执行记录: toolId={}", toolId);

        try {
            List<AgentExecution> executions = executionService.getExecutionsByTool(toolId);
            List<AgentExecutionDTO> dtos = executions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取工具执行记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get executions: " + e.getMessage()));
        }
    }

    /**
     * 获取最近的执行记录
     *
     * GET /api/executions/recent?limit={}
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AgentExecutionDTO>>> getRecentExecutions(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("获取最近执行记录: limit={}", limit);

        try {
            List<AgentExecution> executions = executionService.getRecentExecutions(limit);
            List<AgentExecutionDTO> dtos = executions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取最近执行记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get recent executions: " + e.getMessage()));
        }
    }

    /**
     * 获取待处理的执行记录
     *
     * GET /api/executions/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<AgentExecutionDTO>>> getPendingExecutions() {
        log.debug("获取待处理执行记录");

        try {
            List<AgentExecution> executions = executionService.getPendingExecutions();
            List<AgentExecutionDTO> dtos = executions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取待处理执行记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get pending executions: " + e.getMessage()));
        }
    }

    /**
     * 获取执行统计
     *
     * GET /api/executions/stats?agentId={}
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ExecutionStatsDTO>> getStats(
            @RequestParam(required = false) String agentId) {
        log.debug("获取执行统计: agentId={}", agentId);

        try {
            AgentExecutionService.ExecutionStats stats = executionService.getStats(agentId);

            ExecutionStatsDTO dto = new ExecutionStatsDTO();
            dto.setTotalExecutions(stats.getTotalExecutions());
            dto.setCompleted(stats.getCompleted());
            dto.setFailed(stats.getFailed());
            dto.setRunning(stats.getRunning());
            dto.setPending(stats.getPending());
            dto.setAverageExecutionTimeMs(stats.getAverageExecutionTimeMs());
            dto.setTotalTokensUsed(stats.getTotalTokensUsed());

            return ResponseEntity.ok(ApiResponse.success(dto));

        } catch (Exception e) {
            log.error("获取执行统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get stats: " + e.getMessage()));
        }
    }

    /**
     * Convert AgentExecution to DTO
     */
    private AgentExecutionDTO convertToDTO(AgentExecution entity) {
        if (entity == null) return null;

        AgentExecutionDTO dto = new AgentExecutionDTO();
        dto.setId(entity.getId());
        dto.setExecutionId(entity.getExecutionId());
        dto.setAgentId(entity.getAgentId());
        dto.setMemoryId(entity.getMemoryId());
        dto.setTaskId(entity.getTaskId());
        dto.setTaskType(entity.getTaskType());
        dto.setTaskDescription(entity.getTaskDescription());
        dto.setToolId(entity.getToolId());
        dto.setToolName(entity.getToolName());
        dto.setToolCategory(entity.getToolCategory());
        dto.setInput(entity.getInput());
        dto.setOutput(entity.getOutput());
        dto.setSuccess(entity.getSuccess());
        dto.setExitCode(entity.getExitCode());
        dto.setExecutionTimeMs(entity.getExecutionTimeMs());
        dto.setMemoryUsedMb(entity.getMemoryUsedMb());
        dto.setTokensUsed(entity.getTokensUsed());
        dto.setStatus(entity.getStatus());

        // Convert timestamps to strings
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().toString());
        }
        if (entity.getStartedAt() != null) {
            dto.setStartedAt(entity.getStartedAt().toString());
        }
        if (entity.getCompletedAt() != null) {
            dto.setCompletedAt(entity.getCompletedAt().toString());
        }

        return dto;
    }
}

package com.agent.monitor.controller;

import com.agent.monitor.dto.*;
import com.agent.monitor.entity.ToolUsageStats;
import com.agent.monitor.service.ToolUsageStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool Usage Stats Controller
 *
 * Provides tool proficiency and usage statistics API
 * Design Document: 05-agent-behavior.md
 */
@Slf4j
@RestController
@RequestMapping("/api/tool-stats")
@RequiredArgsConstructor
public class ToolUsageStatsController {

    private final ToolUsageStatsService statsService;

    /**
     * 记录工具使用
     *
     * POST /api/tool-stats/{memoryId}/record
     */
    @PostMapping("/{memoryId}/record")
    public ResponseEntity<ApiResponse<Boolean>> recordUsage(
            @PathVariable String memoryId,
            @RequestBody RecordToolUsageRequestDTO request) {
        log.info("记录工具使用: memoryId={}, toolId={}, success={}",
                memoryId, request.getToolId(), request.getSuccess());

        try {
            long duration = request.getDurationSeconds() != null ? request.getDurationSeconds() : 0L;
            statsService.recordUsage(
                    memoryId,
                    request.getToolId(),
                    request.getSuccess(),
                    duration
            );

            return ResponseEntity.ok(ApiResponse.success(true));

        } catch (Exception e) {
            log.error("记录工具使用失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to record usage: " + e.getMessage()));
        }
    }

    /**
     * 获取工具统计
     *
     * GET /api/tool-stats/{memoryId}/tool/{toolId}
     */
    @GetMapping("/{memoryId}/tool/{toolId}")
    public ResponseEntity<ApiResponse<ToolUsageStatsDTO>> getStats(
            @PathVariable String memoryId,
            @PathVariable String toolId) {
        log.debug("获取工具统计: memoryId={}, toolId={}", memoryId, toolId);

        try {
            ToolUsageStats stats = statsService.getStats(memoryId, toolId);
            if (stats == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Tool stats not found"));
            }

            return ResponseEntity.ok(ApiResponse.success(convertToDTO(stats)));

        } catch (Exception e) {
            log.error("获取工具统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get stats: " + e.getMessage()));
        }
    }

    /**
     * 获取 Memory 的所有工具统计
     *
     * GET /api/tool-stats/{memoryId}
     */
    @GetMapping("/{memoryId}")
    public ResponseEntity<ApiResponse<List<ToolUsageStatsDTO>>> getAllStats(
            @PathVariable String memoryId) {
        log.debug("获取所有工具统计: memoryId={}", memoryId);

        try {
            List<ToolUsageStats> statsList = statsService.getAllStats(memoryId);
            List<ToolUsageStatsDTO> dtos = statsList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取所有工具统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get all stats: " + e.getMessage()));
        }
    }

    /**
     * 获取最熟练的工具
     *
     * GET /api/tool-stats/{memoryId}/proficient?limit={}
     */
    @GetMapping("/{memoryId}/proficient")
    public ResponseEntity<ApiResponse<List<ToolUsageStatsDTO>>> getMostProficientTools(
            @PathVariable String memoryId,
            @RequestParam(defaultValue = "5") Integer limit) {
        log.debug("获取最熟练工具: memoryId={}, limit={}", memoryId, limit);

        try {
            List<ToolUsageStats> statsList = statsService.getMostProficientTools(memoryId, limit);
            List<ToolUsageStatsDTO> dtos = statsList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取最熟练工具失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get proficient tools: " + e.getMessage()));
        }
    }

    /**
     * 获取工具熟练度
     *
     * GET /api/tool-stats/{memoryId}/proficiency/{toolId}
     */
    @GetMapping("/{memoryId}/proficiency/{toolId}")
    public ResponseEntity<ApiResponse<BigDecimal>> getProficiency(
            @PathVariable String memoryId,
            @PathVariable String toolId) {
        log.debug("获取工具熟练度: memoryId={}, toolId={}", memoryId, toolId);

        try {
            BigDecimal proficiency = statsService.getProficiency(memoryId, toolId);
            return ResponseEntity.ok(ApiResponse.success(proficiency));

        } catch (Exception e) {
            log.error("获取工具熟练度失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get proficiency: " + e.getMessage()));
        }
    }

    /**
     * 设置熟练度
     *
     * POST /api/tool-stats/{memoryId}/proficiency
     */
    @PostMapping("/{memoryId}/proficiency")
    public ResponseEntity<ApiResponse<Boolean>> setProficiency(
            @PathVariable String memoryId,
            @RequestBody SetProficiencyRequestDTO request) {
        log.info("设置熟练度: memoryId={}, toolId={}, proficiency={}",
                memoryId, request.getToolId(), request.getProficiency());

        try {
            boolean updated = statsService.setProficiency(
                    memoryId,
                    request.getToolId(),
                    request.getProficiency()
            );

            return ResponseEntity.ok(ApiResponse.success(updated));

        } catch (Exception e) {
            log.error("设置熟练度失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to set proficiency: " + e.getMessage()));
        }
    }

    /**
     * 增加熟练度
     *
     * POST /api/tool-stats/{memoryId}/proficiency/increment
     */
    @PostMapping("/{memoryId}/proficiency/increment")
    public ResponseEntity<ApiResponse<Boolean>> incrementProficiency(
            @PathVariable String memoryId,
            @RequestBody SetProficiencyRequestDTO request) {
        log.info("增加熟练度: memoryId={}, toolId={}, increment={}",
                memoryId, request.getToolId(), request.getProficiency());

        try {
            boolean updated = statsService.incrementProficiency(
                    memoryId,
                    request.getToolId(),
                    request.getProficiency()
            );

            return ResponseEntity.ok(ApiResponse.success(updated));

        } catch (Exception e) {
            log.error("增加熟练度失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to increment proficiency: " + e.getMessage()));
        }
    }

    /**
     * Convert ToolUsageStats to DTO
     */
    private ToolUsageStatsDTO convertToDTO(ToolUsageStats entity) {
        if (entity == null) return null;

        ToolUsageStatsDTO dto = new ToolUsageStatsDTO();
        dto.setId(entity.getId());
        dto.setToolId(entity.getToolId());
        dto.setMemoryId(entity.getMemoryId());
        dto.setTotalUses(entity.getTotalUses());
        dto.setSuccessfulUses(entity.getSuccessfulUses());
        dto.setFailedUses(entity.getFailedUses());
        dto.setProficiencyLevel(entity.getProficiencyLevel());
        dto.setTotalPracticeTime(entity.getTotalPracticeTime());

        // Convert timestamps to strings
        if (entity.getLastUsedAt() != null) {
            dto.setLastUsedAt(entity.getLastUsedAt().toString());
        }
        if (entity.getLastSuccessAt() != null) {
            dto.setLastSuccessAt(entity.getLastSuccessAt().toString());
        }
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().toString());
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().toString());
        }

        return dto;
    }
}

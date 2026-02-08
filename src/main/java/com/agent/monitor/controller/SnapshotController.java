package com.agent.monitor.controller;

import com.agent.monitor.dto.ApiResponse;
import com.agent.monitor.dto.SnapshotDTO;
import com.agent.monitor.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 快照控制器
 *
 * 提供快照查询 API
 * Design Document: 02-snapshot-delta-sync.md
 */
@Slf4j
@RestController
@RequestMapping("/api/snapshot")
@RequiredArgsConstructor
public class SnapshotController {

    private final SnapshotService snapshotService;

    /**
     * 获取最新快照
     *
     * GET /api/snapshot/latest
     *
     * @return 最新快照
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<SnapshotDTO>> getLatestSnapshot() {
        log.debug("获取最新快照");

        SnapshotDTO snapshot = snapshotService.getLatestSnapshot();

        if (snapshot == null) {
            log.warn("没有可用的快照");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "No snapshot available"));
        }

        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }

    /**
     * 根据 ID 获取快照
     *
     * GET /api/snapshot/{snapshotId}
     *
     * @param snapshotId 快照 ID
     * @return 快照数据
     */
    @GetMapping("/{snapshotId}")
    public ResponseEntity<ApiResponse<SnapshotDTO>> getSnapshotById(@PathVariable String snapshotId) {
        log.debug("获取快照: snapshotId={}", snapshotId);

        SnapshotDTO snapshot = snapshotService.getSnapshotById(snapshotId);

        if (snapshot == null) {
            log.warn("快照不存在: snapshotId={}", snapshotId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Snapshot not found: " + snapshotId));
        }

        return ResponseEntity.ok(ApiResponse.success(snapshot));
    }

    /**
     * 手动触发快照生成
     *
     * POST /api/snapshot/generate
     *
     * @return 生成的快照
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SnapshotDTO>> generateSnapshot() {
        log.info("手动触发快照生成");

        try {
            SnapshotDTO snapshot = snapshotService.generateSnapshot();
            return ResponseEntity.ok(ApiResponse.success(snapshot));
        } catch (Exception e) {
            log.error("快照生成失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to generate snapshot: " + e.getMessage()));
        }
    }
}

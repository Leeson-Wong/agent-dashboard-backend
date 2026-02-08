package com.agent.monitor.controller;

import com.agent.monitor.dto.ApiResponse;
import com.agent.monitor.dto.EventsResponseDTO;
import com.agent.monitor.entity.AgentEvent;
import com.agent.monitor.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事件控制器
 *
 * 提供增量事件查询 API
 * Design Document: 02-snapshot-delta-sync.md
 */
@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventsController {

    private final SnapshotService snapshotService;

    /**
     * 获取增量事件
     *
     * GET /api/events?since=<seq>&limit=<limit>
     *
     * @param since 起始序列号
     * @param limit 限制数量 (可选，默认 100)
     * @return 增量事件列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<EventsResponseDTO>> getEvents(
            @RequestParam(value = "since", required = false) Long since,
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit) {

        log.debug("获取增量事件: since={}, limit={}", since, limit);

        // 如果没有指定 since，使用 0
        Long startSeq = since != null ? since : 0L;

        List<AgentEvent> events;
        try {
            events = snapshotService.getEventsAfterSeq(startSeq, limit);
        } catch (Exception e) {
            log.error("获取事件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get events: " + e.getMessage()));
        }

        // 如果没有事件且 since > 0，可能 seq 已过期
        if (events.isEmpty() && startSeq > 0) {
            log.warn("没有找到从 seq={} 开始的事件，可能已过期", startSeq);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404,
                            "Events since seq " + startSeq + " not found (expired). " +
                                    "Please fetch latest snapshot."));
        }

        // 转换为 DTO
        EventsResponseDTO response = new EventsResponseDTO();
        response.setSince(startSeq);
        response.setEvents(events.stream()
                .map(this::convertToEventData)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取最大序列号
     *
     * GET /api/events/max-seq
     *
     * @return 当前最大序列号
     */
    @GetMapping("/max-seq")
    public ResponseEntity<ApiResponse<Long>> getMaxSeq() {
        log.debug("获取最大序列号");

        try {
            Long maxSeq = snapshotService.getEventsAfterSeq(0L, 1).isEmpty()
                    ? 0L
                    : snapshotService.getEventsAfterSeq(0L, 1).get(0).getSeq();

            return ResponseEntity.ok(ApiResponse.success(maxSeq));
        } catch (Exception e) {
            log.error("获取最大序列号失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get max seq: " + e.getMessage()));
        }
    }

    /**
     * 将 AgentEvent 转换为 EventData
     */
    private EventsResponseDTO.EventData convertToEventData(AgentEvent event) {
        EventsResponseDTO.EventData data = new EventsResponseDTO.EventData();
        data.setSeq(event.getSeq());
        data.setType(event.getEventType());
        data.setAgentId(event.getAgentId());
        data.setData(event.getData());
        data.setTimestamp(event.getCreatedAt().toString());
        return data;
    }
}

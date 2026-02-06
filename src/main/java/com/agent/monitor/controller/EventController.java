package com.agent.monitor.controller;

import com.agent.monitor.dto.MonitorEventDTO;
import com.agent.monitor.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 事件接收 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /**
     * 接收单个事件
     */
    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> receiveEvent(
            @Valid @RequestBody MonitorEventDTO event
    ) {
        log.debug("接收到事件: {}", event.getEvent().getType());

        eventService.processEvent(event);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Event received"
        ));
    }

    /**
     * 接收批量事件
     */
    @PostMapping("/events/batch")
    public ResponseEntity<Map<String, Object>> receiveBatchEvents(
            @Valid @RequestBody List<MonitorEventDTO> events
    ) {
        log.info("接收到 {} 个批量事件", events.size());

        eventService.processEvents(events);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "received", events.size(),
                "message", "Events processed"
        ));
    }
}

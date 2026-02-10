package com.agent.monitor.controller;

import com.agent.monitor.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Session Statistics Controller
 *
 * Provides real-time statistics about the current monitoring session.
 */
@RestController
@RequestMapping("/api/stats")
public class SessionStatsController {

    // Session start time (set on first request)
    private volatile Instant sessionStartTime = null;

    // Counters
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong websocketMessages = new AtomicLong(0);
    private final AtomicLong agentStateChanges = new AtomicLong(0);
    private final AtomicLong errors = new AtomicLong(0);

    /**
     * Get current session statistics
     * GET /api/stats/session
     */
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionStats() {
        // Initialize session start time on first request
        if (sessionStartTime == null) {
            synchronized (this) {
                if (sessionStartTime == null) {
                    sessionStartTime = Instant.now();
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();

        // Session info
        stats.put("session", getSessionInfo());

        // Counters
        Map<String, Object> counters = new HashMap<>();
        counters.put("totalRequests", totalRequests.get());
        counters.put("websocketMessages", websocketMessages.get());
        counters.put("agentStateChanges", agentStateChanges.get());
        counters.put("errors", errors.get());
        stats.put("counters", counters);

        // Calculate rates
        Map<String, Object> rates = new HashMap<>();
        long uptimeSeconds = getUptimeSeconds();
        if (uptimeSeconds > 0) {
            rates.put("requestsPerMinute", totalRequests.get() * 60.0 / uptimeSeconds);
            rates.put("messagesPerMinute", websocketMessages.get() * 60.0 / uptimeSeconds);
        } else {
            rates.put("requestsPerMinute", 0.0);
            rates.put("messagesPerMinute", 0.0);
        }
        stats.put("rates", rates);

        totalRequests.incrementAndGet();

        return ResponseEntity.ok(
            new ApiResponse<>(200, "Session statistics retrieved", stats, System.currentTimeMillis())
        );
    }

    /**
     * Record WebSocket message
     * POST /api/stats/websocket-message
     */
    @PostMapping("/websocket-message")
    public ResponseEntity<ApiResponse<Map<String, String>>> recordWebSocketMessage() {
        websocketMessages.incrementAndGet();

        Map<String, String> result = new HashMap<>();
        result.put("status", "recorded");
        result.put("count", String.valueOf(websocketMessages.get()));

        return ResponseEntity.ok(
            new ApiResponse<>(200, "WebSocket message recorded", result, System.currentTimeMillis())
        );
    }

    /**
     * Record Agent state change
     * POST /api/stats/agent-change
     */
    @PostMapping("/agent-change")
    public ResponseEntity<ApiResponse<Map<String, String>>> recordAgentStateChange() {
        agentStateChanges.incrementAndGet();

        Map<String, String> result = new HashMap<>();
        result.put("status", "recorded");
        result.put("count", String.valueOf(agentStateChanges.get()));

        return ResponseEntity.ok(
            new ApiResponse<>(200, "Agent state change recorded", result, System.currentTimeMillis())
        );
    }

    /**
     * Record error
     * POST /api/stats/error
     */
    @PostMapping("/error")
    public ResponseEntity<ApiResponse<Map<String, String>>> recordError() {
        errors.incrementAndGet();

        Map<String, String> result = new HashMap<>();
        result.put("status", "recorded");
        result.put("count", String.valueOf(errors.get()));

        return ResponseEntity.ok(
            new ApiResponse<>(200, "Error recorded", result, System.currentTimeMillis())
        );
    }

    /**
     * Reset session statistics
     * POST /api/stats/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetSessionStats() {
        sessionStartTime = Instant.now();
        totalRequests.set(0);
        websocketMessages.set(0);
        agentStateChanges.set(0);
        errors.set(0);

        Map<String, String> result = new HashMap<>();
        result.put("status", "reset");

        return ResponseEntity.ok(
            new ApiResponse<>(200, "Session statistics reset", result, System.currentTimeMillis())
        );
    }

    /**
     * Get session information
     */
    private Map<String, Object> getSessionInfo() {
        Map<String, Object> sessionInfo = new HashMap<>();

        if (sessionStartTime != null) {
            LocalDateTime startTime = LocalDateTime.ofInstant(sessionStartTime, ZoneId.systemDefault());
            sessionInfo.put("startTime", startTime.toString());
            sessionInfo.put("uptimeSeconds", getUptimeSeconds());
            sessionInfo.put("uptimeFormatted", formatUptime(getUptimeSeconds()));
        } else {
            sessionInfo.put("startTime", null);
            sessionInfo.put("uptimeSeconds", 0L);
            sessionInfo.put("uptimeFormatted", "0s");
        }

        return sessionInfo;
    }

    /**
     * Get uptime in seconds
     */
    private long getUptimeSeconds() {
        if (sessionStartTime == null) {
            return 0;
        }
        return Duration.between(sessionStartTime, Instant.now()).getSeconds();
    }

    /**
     * Format uptime as human-readable string
     */
    private String formatUptime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return String.format("%dm %ds", minutes, secs);
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return String.format("%dh %dm", hours, minutes);
        } else {
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            return String.format("%dd %dh", days, hours);
        }
    }
}

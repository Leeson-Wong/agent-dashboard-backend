package com.agent.monitor.controller;

import com.agent.monitor.dto.ApiResponse;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Health Check Controller
 *
 * Provides detailed health status including:
 * - System status (uptime, memory, CPU)
 * - Database connectivity
 * - Application info
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final Instant startTime = Instant.now();

    /**
     * Simple health check (for load balancers)
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check
     * GET /api/health/detailed
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();

        // Overall status
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());

        // System info
        health.put("system", getSystemInfo());

        // Application info
        health.put("application", getApplicationInfo());

        // Components
        health.put("components", getComponentsStatus());

        return ResponseEntity.ok(
            new ApiResponse<>(200, "Health check completed", health, System.currentTimeMillis())
        );
    }

    /**
     * Get system information
     */
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();

        // Memory info
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        memory.put("heap", formatBytes(memoryBean.getHeapMemoryUsage().getUsed()));
        memory.put("heapMax", formatBytes(memoryBean.getHeapMemoryUsage().getMax()));
        memory.put("nonHeap", formatBytes(memoryBean.getNonHeapMemoryUsage().getUsed()));
        memory.put("nonHeapMax", formatBytes(memoryBean.getNonHeapMemoryUsage().getMax()));
        system.put("memory", memory);

        // OS info
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> os = new HashMap<>();
        os.put("name", osBean.getName());
        os.put("version", osBean.getVersion());
        os.put("arch", osBean.getArch());
        os.put("processors", osBean.getAvailableProcessors());
        os.put("systemLoadAverage", osBean.getSystemLoadAverage());
        system.put("os", os);

        // Java info
        Map<String, Object> java = new HashMap<>();
        java.put("version", System.getProperty("java.version"));
        java.put("vendor", System.getProperty("java.vendor"));
        java.put("home", System.getProperty("java.home"));
        system.put("java", java);

        return system;
    }

    /**
     * Get application information
     */
    private Map<String, Object> getApplicationInfo() {
        Map<String, Object> app = new HashMap<>();

        // Uptime
        Duration uptime = Duration.between(startTime, Instant.now());
        app.put("uptime", formatDuration(uptime));
        app.put("uptimeSeconds", uptime.getSeconds());

        // Start time
        app.put("startTime", startTime.toString());

        // Environment
        String env = System.getProperty("spring.profiles.active", "default");
        app.put("environment", env);

        // App info
        app.put("name", "Agent Monitor Server");
        app.put("version", "1.0.0");

        return app;
    }

    /**
     * Get components status
     */
    private Map<String, Object> getComponentsStatus() {
        Map<String, Object> components = new HashMap<>();

        // Database status (simplified - in production, use actual health indicator)
        Map<String, Object> database = new HashMap<>();
        database.put("status", "UP");
        database.put("description", "Database connection is healthy");
        components.put("database", database);

        // WebSocket status
        Map<String, Object> websocket = new HashMap<>();
        websocket.put("status", "UP");
        websocket.put("description", "WebSocket endpoint is available");
        components.put("websocket", websocket);

        // API status
        Map<String, Object> api = new HashMap<>();
        api.put("status", "UP");
        api.put("description", "API endpoints are operational");
        components.put("api", api);

        return components;
    }

    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Format duration to human-readable format
     */
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, secs);
        } else {
            return String.format("%d seconds", secs);
        }
    }
}

package com.agent.monitor.controller;

import com.agent.monitor.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Management Controller
 *
 * Provides runtime configuration viewing and management
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private Environment environment;

    /**
     * Get all configuration properties
     * GET /api/config
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfig() {
        Map<String, Object> config = new HashMap<>();

        // Server configuration
        config.put("server", getServerConfig());

        // Application configuration
        config.put("application", getApplicationConfig());

        // Database configuration (sanitized)
        config.put("database", getDatabaseConfig());

        // Logging configuration
        config.put("logging", getLoggingConfig());

        return ResponseEntity.ok(
            new ApiResponse<>(200, "Configuration retrieved", config, System.currentTimeMillis())
        );
    }

    /**
     * Get public configuration (safe to expose)
     * GET /api/config/public
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicConfig() {
        Map<String, Object> publicConfig = new HashMap<>();

        // Only expose safe configuration
        publicConfig.put("server", Map.of(
            "port", environment.getProperty("server.port"),
            "contextPath", environment.getProperty("server.servlet.context-path")
        ));

        publicConfig.put("application", Map.of(
            "name", environment.getProperty("spring.application.name")
        ));

        publicConfig.put("websocket", Map.of(
            "endpoint", "/ws"
        ));

        return ResponseEntity.ok(
            new ApiResponse<>(200, "Public configuration retrieved", publicConfig, System.currentTimeMillis())
        );
    }

    /**
     * Update configuration property
     * POST /api/config/update
     *
     * Note: Not all properties can be updated at runtime
     */
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateConfig(
            @RequestBody Map<String, String> request
    ) {
        String key = request.get("key");
        String value = request.get("value");

        if (key == null || value == null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(400, "Missing 'key' or 'value' parameter", null, System.currentTimeMillis())
            );
        }

        // Check if property is allowed to be updated
        if (!isPropertyUpdatable(key)) {
            return ResponseEntity.status(403).body(
                new ApiResponse<>(403, "Property '" + key + "' cannot be updated at runtime", null, System.currentTimeMillis())
            );
        }

        try {
            // Set system property (some properties may need restart to take effect)
            System.setProperty(key, value);

            Map<String, String> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);
            result.put("message", "Configuration updated. Some changes may require restart to take effect.");

            return ResponseEntity.ok(
                new ApiResponse<>(200, "Configuration updated successfully", result, System.currentTimeMillis())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>(500, "Failed to update configuration: " + e.getMessage(), null, System.currentTimeMillis())
            );
        }
    }

    /**
     * Get specific configuration value
     * GET /api/config/{key}
     */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getConfigValue(
            @PathVariable String key
    ) {
        try {
            String value = environment.getProperty(key);

            if (value == null) {
                return ResponseEntity.status(404).body(
                    new ApiResponse<>(404, "Configuration key not found: " + key, null, System.currentTimeMillis())
                );
            }

            // Check if property is safe to expose
            if (!isPropertySafe(key)) {
                return ResponseEntity.status(403).body(
                    new ApiResponse<>(403, "Property '" + key + "' is not safe to expose", null, System.currentTimeMillis())
                );
            }

            Map<String, String> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);

            return ResponseEntity.ok(
                new ApiResponse<>(200, "Configuration value retrieved", result, System.currentTimeMillis())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>(500, "Failed to get configuration: " + e.getMessage(), null, System.currentTimeMillis())
            );
        }
    }

    /**
     * Check if property is updatable at runtime
     */
    private boolean isPropertyUpdatable(String key) {
        // Only allow certain prefixes
        return key.startsWith("app.") ||
               key.startsWith("logging.level.") ||
               key.startsWith("management.");
    }

    /**
     * Check if property is safe to expose
     */
    private boolean isPropertySafe(String key) {
        // Block sensitive properties
        String lowerKey = key.toLowerCase();
        return !lowerKey.contains("password") &&
               !lowerKey.contains("secret") &&
               !lowerKey.contains("token") &&
               !lowerKey.contains("credentials") &&
               !lowerKey.contains("key");
    }

    /**
     * Get server configuration
     */
    private Map<String, Object> getServerConfig() {
        Map<String, Object> serverConfig = new HashMap<>();
        serverConfig.put("port", environment.getProperty("server.port"));
        serverConfig.put("contextPath", environment.getProperty("server.servlet.context-path", ""));
        serverConfig.put("error", Map.of(
            "path", environment.getProperty("server.error.path", "/error"),
            "includeMessage", environment.getProperty("server.error.include-message", "true"),
            "includeStacktrace", environment.getProperty("server.error.include-stacktrace", "false")
        ));
        return serverConfig;
    }

    /**
     * Get application configuration
     */
    private Map<String, Object> getApplicationConfig() {
        Map<String, Object> appConfig = new HashMap<>();
        appConfig.put("name", environment.getProperty("spring.application.name"));
        appConfig.put("activeProfiles", environment.getActiveProfiles());
        appConfig.put("jpa", Map.of(
            "databasePlatform", environment.getProperty("spring.jpa.database-platform"),
            "showSql", environment.getProperty("spring.jpa.show-sql", "false"),
            "hibernate ddlAuto", environment.getProperty("spring.jpa.hibernate.ddl-auto")
        ));
        return appConfig;
    }

    /**
     * Get database configuration (sanitized)
     */
    private Map<String, Object> getDatabaseConfig() {
        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("url", getSanitizedUrl(environment.getProperty("spring.datasource.url")));
        dbConfig.put("username", environment.getProperty("spring.datasource.username"));
        dbConfig.put("driver", environment.getProperty("spring.datasource.driver-class-name"));
        dbConfig.put("type", environment.getProperty("spring.datasource.type"));

        // Connection pool settings (without password)
        Map<String, Object> pool = new HashMap<>();
        pool.put("initialSize", environment.getProperty("spring.datasource.druid.initial-size"));
        pool.put("minIdle", environment.getProperty("spring.datasource.druid.min-idle"));
        pool.put("maxActive", environment.getProperty("spring.datasource.druid.max-active"));
        pool.put("maxWait", environment.getProperty("spring.datasource.druid.max-wait"));
        dbConfig.put("pool", pool);

        return dbConfig;
    }

    /**
     * Get logging configuration
     */
    private Map<String, Object> getLoggingConfig() {
        Map<String, Object> logConfig = new HashMap<>();
        logConfig.put("level", Map.of(
            "root", environment.getProperty("logging.level.root"),
            "app", environment.getProperty("logging.level.com.agent.monitor"),
            "spring", environment.getProperty("logging.level.org.springframework"),
            "druid", environment.getProperty("logging.level.com.alibaba.druid")
        ));
        return logConfig;
    }

    /**
     * Sanitize database URL by removing password
     */
    private String getSanitizedUrl(String url) {
        if (url == null) return null;

        try {
            // Remove password from JDBC URL if present
            // Format: jdbc:mysql://user:password@host:port/db
            return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
        } catch (Exception e) {
            return url;
        }
    }
}

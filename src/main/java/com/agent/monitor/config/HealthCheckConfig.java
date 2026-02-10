package com.agent.monitor.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Health Indicator
 *
 * Checks database connectivity and connection pool status
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);

            if (isValid) {
                Map<String, Object> details = new HashMap<>();

                // Get database metadata
                details.put("database", connection.getMetaData().getDatabaseProductName());
                details.put("version", connection.getMetaData().getDatabaseProductVersion());
                details.put("url", connection.getMetaData().getURL());

                // Get connection info
                if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
                    var tomcatDataSource = (org.apache.tomcat.jdbc.pool.DataSource) dataSource;
                    details.put("active", tomcatDataSource.getNumActive());
                    details.put("idle", tomcatDataSource.getNumIdle());
                    details.put("maxActive", tomcatDataSource.getMaxActive());
                } else if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                    var hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                    details.put("active", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                    details.put("idle", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
                    details.put("maxActive", hikariDataSource.getMaximumPoolSize());
                    details.put("totalConnections", hikariDataSource.getHikariPoolMXBean().getTotalConnections());
                }

                return Health.up()
                        .withDetails(details)
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Database connection is not valid")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}

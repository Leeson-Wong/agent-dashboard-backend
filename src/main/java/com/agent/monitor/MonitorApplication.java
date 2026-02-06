package com.agent.monitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Agent Monitor Application
 *
 * Agent 监控服务器主应用
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.agent.monitor.mapper")
public class MonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
        System.out.println("""

            ======================================================
               🚀 Agent Monitor Server Started!
               📍 http://localhost:8080
               📊 API: http://localhost:8080/api
               💾 Database: MySQL + Liquibase + MyBatis
               🏊 Connection Pool: Druid
            ======================================================
            """);
    }
}

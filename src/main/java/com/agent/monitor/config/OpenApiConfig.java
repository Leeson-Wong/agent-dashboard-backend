package com.agent.monitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置
 *
 * API 文档自动生成
 * 访问地址: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agentMonitorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agent Monitor API")
                        .description("Agent 监控系统 REST API 文档\n\n" +
                                "## 核心功能\n" +
                                "- **Agent 状态监控**: 实时跟踪 Agent 状态\n" +
                                "- **事件流处理**: Snapshot + Delta Sync 架构\n" +
                                "- **Memory 管理**: 多层记忆系统（经验、知识、技能、时间补丁）\n" +
                                "- **执行追踪**: Agent 执行记录与性能分析\n" +
                                "- **工具权限**: 灵活的访问控制系统\n" +
                                "- **技能熟练度**: 自动计算与跟踪工具熟练度\n\n" +
                                "## 设计文档\n" +
                                "- 02-snapshot-delta-sync.md: 快照+增量同步\n" +
                                "- 04-memory-management.md: Memory 管理系统\n" +
                                "- 05-agent-behavior.md: Agent 行为定义\n" +
                                "- 06-crewai-event-mapping.md: CrewAI 事件映射")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Agent Monitor Team")
                                .email("support@agent-monitor.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.agent-monitor.com")
                                .description("生产环境")
                ));
    }
}

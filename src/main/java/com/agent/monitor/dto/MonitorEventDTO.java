package com.agent.monitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * 监控事件 DTO
 */
@Data
public class MonitorEventDTO {

    @NotBlank(message = "协议不能为空")
    @JsonProperty("protocol")
    private String protocol = "agent-monitor";

    @NotBlank(message = "版本不能为空")
    @JsonProperty("version")
    private String version = "1.0";

    @NotNull(message = "时间戳不能为空")
    @JsonProperty("timestamp")
    private Instant timestamp;

    @NotNull(message = "事件源不能为空")
    @JsonProperty("source")
    private EventSource source;

    @NotNull(message = "事件内容不能为空")
    @JsonProperty("event")
    private Event event;

    @JsonProperty("metadata")
    private EventMetadata metadata;

    /**
     * 事件源信息
     */
    @Data
    public static class EventSource {
        @NotBlank(message = "服务器 ID 不能为空")
        @JsonProperty("server_id")
        private String serverId;

        @NotBlank(message = "Agent ID 不能为空")
        @JsonProperty("agent_id")
        private String agentId;

        @NotBlank(message = "框架不能为空")
        @JsonProperty("framework")
        private String framework;

        @NotBlank(message = "语言不能为空")
        @JsonProperty("language")
        private String language;

        @JsonProperty("process_id")
        private Integer processId;
    }

    /**
     * 事件内容
     */
    @Data
    public static class Event {
        @NotBlank(message = "事件类型不能为空")
        @JsonProperty("type")
        private String type;

        @NotNull(message = "事件数据不能为空")
        @JsonProperty("data")
        private Map<String, Object> data;
    }

    /**
     * 元数据
     */
    @Data
    public static class EventMetadata {
        @NotBlank(message = "主机名不能为空")
        @JsonProperty("hostname")
        private String hostname;

        @JsonProperty("ip_address")
        private String ipAddress;

        @JsonProperty("tags")
        private Map<String, String> tags;
    }
}

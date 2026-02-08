package com.agent.monitor.dto;

import lombok.Data;

import java.util.List;

/**
 * 增量事件响应
 *
 * Design Document: 02-snapshot-delta-sync.md
 */
@Data
public class EventsResponseDTO {

    /**
     * 查询的起始序列号
     */
    private Long since;

    /**
     * 事件列表
     */
    private List<EventData> events;

    /**
     * 事件数据
     */
    @Data
    public static class EventData {
        /**
         * 序列号
         */
        private Long seq;

        /**
         * 事件类型
         */
        private String type;

        /**
         * Agent ID
         */
        private String agentId;

        /**
         * 事件数据 (JSON 字符串)
         */
        private String data;

        /**
         * 时间戳
         */
        private String timestamp;
    }
}

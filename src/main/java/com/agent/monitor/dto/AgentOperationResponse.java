package com.agent.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 操作响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentOperationResponse {
    /**
     * Agent ID
     */
    private String agentId;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作状态: success, failed, pending
     */
    private String status;

    /**
     * 操作消息
     */
    private String message;

    /**
     * 操作后的 Agent 状态
     */
    private String currentAgentStatus;

    /**
     * 操作时间戳
     */
    private long timestamp;
}

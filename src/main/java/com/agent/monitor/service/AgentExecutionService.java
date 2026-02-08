package com.agent.monitor.service;

import com.agent.monitor.entity.AgentExecution;
import com.agent.monitor.mapper.AgentExecutionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Agent 执行记录服务
 *
 * 负责跟踪和管理 Agent 的所有执行记录
 * Design Document: 05-agent-behavior.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentExecutionService {

    private final AgentExecutionMapper executionMapper;

    /**
     * 创建新的执行记录
     *
     * @param agentId Agent ID
     * @param memoryId Memory ID
     * @param taskId Task ID
     * @param taskType Task type
     * @param taskDescription Task description
     * @param toolId Tool ID
     * @param toolName Tool name
     * @param toolCategory Tool category
     * @param input Input parameters (JSON)
     * @return Execution ID
     */
    @Transactional
    public String createExecution(String agentId, String memoryId, String taskId,
                                 String taskType, String taskDescription,
                                 String toolId, String toolName, String toolCategory,
                                 String input) {
        log.debug("创建执行记录: agentId={}, toolId={}", agentId, toolId);

        AgentExecution execution = new AgentExecution();
        execution.setExecutionId(UUID.randomUUID().toString());
        execution.setAgentId(agentId);
        execution.setMemoryId(memoryId);
        execution.setTaskId(taskId);
        execution.setTaskType(taskType);
        execution.setTaskDescription(taskDescription);
        execution.setToolId(toolId);
        execution.setToolName(toolName);
        execution.setToolCategory(toolCategory);
        execution.setInput(input);
        execution.setStatus("pending");
        execution.setCreatedAt(Instant.now());

        executionMapper.insert(execution);
        log.info("执行记录已创建: executionId={}", execution.getExecutionId());
        return execution.getExecutionId();
    }

    /**
     * 开始执行
     *
     * @param executionId Execution ID
     * @return 是否成功
     */
    @Transactional
    public boolean startExecution(String executionId) {
        log.debug("开始执行: executionId={}", executionId);

        AgentExecution execution = executionMapper.findByExecutionId(executionId);
        if (execution == null) {
            log.warn("执行记录不存在: executionId={}", executionId);
            return false;
        }

        execution.setStatus("running");
        execution.setStartedAt(Instant.now());

        int updated = executionMapper.update(execution);
        log.info("执行已开始: executionId={}", executionId);
        return updated > 0;
    }

    /**
     * 完成执行
     *
     * @param executionId Execution ID
     * @param output Output result (JSON)
     * @param success Whether successful
     * @param exitCode Exit code
     * @param executionTimeMs Execution time in milliseconds
     * @param memoryUsedMb Memory used in MB
     * @param tokensUsed Tokens used
     * @return 是否成功
     */
    @Transactional
    public boolean completeExecution(String executionId, String output,
                                    boolean success, int exitCode,
                                    int executionTimeMs, double memoryUsedMb,
                                    int tokensUsed) {
        log.debug("完成执行: executionId={}, success={}", executionId, success);

        AgentExecution execution = executionMapper.findByExecutionId(executionId);
        if (execution == null) {
            log.warn("执行记录不存在: executionId={}", executionId);
            return false;
        }

        execution.setOutput(output);
        execution.setSuccess(success);
        execution.setExitCode(exitCode);
        execution.setExecutionTimeMs(executionTimeMs);
        execution.setMemoryUsedMb(java.math.BigDecimal.valueOf(memoryUsedMb));
        execution.setTokensUsed(tokensUsed);
        execution.setStatus(success ? "completed" : "failed");
        execution.setCompletedAt(Instant.now());

        int updated = executionMapper.update(execution);
        log.info("执行已完成: executionId={}, success={}", executionId, success);
        return updated > 0;
    }

    /**
     * 标记执行失败
     *
     * @param executionId Execution ID
     * @param errorMessage Error message
     * @return 是否成功
     */
    @Transactional
    public boolean failExecution(String executionId, String errorMessage) {
        log.debug("执行失败: executionId={}, error={}", executionId, errorMessage);

        AgentExecution execution = executionMapper.findByExecutionId(executionId);
        if (execution == null) {
            log.warn("执行记录不存在: executionId={}", executionId);
            return false;
        }

        execution.setOutput(errorMessage);
        execution.setSuccess(false);
        execution.setStatus("failed");
        execution.setCompletedAt(Instant.now());

        int updated = executionMapper.update(execution);
        log.warn("执行已标记为失败: executionId={}", executionId);
        return updated > 0;
    }

    /**
     * 获取执行记录
     *
     * @param executionId Execution ID
     * @return Execution record
     */
    public AgentExecution getExecution(String executionId) {
        return executionMapper.findByExecutionId(executionId);
    }

    /**
     * 获取 Agent 的所有执行记录
     *
     * @param agentId Agent ID
     * @return Execution records
     */
    public List<AgentExecution> getExecutionsByAgent(String agentId) {
        return executionMapper.findByAgentId(agentId);
    }

    /**
     * 获取 Memory 的所有执行记录
     *
     * @param memoryId Memory ID
     * @return Execution records
     */
    public List<AgentExecution> getExecutionsByMemory(String memoryId) {
        return executionMapper.findByMemoryId(memoryId);
    }

    /**
     * 获取工具的执行记录
     *
     * @param toolId Tool ID
     * @return Execution records
     */
    public List<AgentExecution> getExecutionsByTool(String toolId) {
        return executionMapper.findByToolId(toolId);
    }

    /**
     * 获取最近的执行记录
     *
     * @param limit Limit
     * @return Execution records
     */
    public List<AgentExecution> getRecentExecutions(int limit) {
        return executionMapper.findRecent(limit);
    }

    /**
     * 获取待处理的执行记录
     *
     * @return Pending executions
     */
    public List<AgentExecution> getPendingExecutions() {
        return executionMapper.findByStatus("pending");
    }

    /**
     * 获取执行统计
     *
     * @param agentId Agent ID (optional)
     * @return Statistics
     */
    public ExecutionStats getStats(String agentId) {
        List<AgentExecution> executions = agentId != null ?
                executionMapper.findByAgentId(agentId) :
                executionMapper.findRecent(10000); // Large limit to get all

        ExecutionStats stats = new ExecutionStats();
        stats.setTotalExecutions((long) executions.size());

        long completed = executions.stream().filter(e -> "completed".equals(e.getStatus())).count();
        long failed = executions.stream().filter(e -> "failed".equals(e.getStatus())).count();
        long running = executions.stream().filter(e -> "running".equals(e.getStatus())).count();
        long pending = executions.stream().filter(e -> "pending".equals(e.getStatus())).count();

        stats.setCompleted(completed);
        stats.setFailed(failed);
        stats.setRunning(running);
        stats.setPending(pending);

        if (completed > 0) {
            double avgTime = executions.stream()
                    .filter(e -> e.getExecutionTimeMs() != null)
                    .mapToInt(AgentExecution::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);
            stats.setAverageExecutionTimeMs(avgTime);
        }

        long totalTokens = executions.stream()
                .filter(e -> e.getTokensUsed() != null)
                .mapToLong(AgentExecution::getTokensUsed)
                .sum();
        stats.setTotalTokensUsed(totalTokens);

        return stats;
    }

    /**
     * 执行统计
     */
    @lombok.Data
    public static class ExecutionStats {
        private Long totalExecutions;
        private Long completed;
        private Long failed;
        private Long running;
        private Long pending;
        private Double averageExecutionTimeMs;
        private Long totalTokensUsed;
    }
}

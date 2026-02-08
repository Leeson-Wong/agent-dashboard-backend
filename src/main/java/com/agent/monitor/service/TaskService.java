package com.agent.monitor.service;

import com.agent.monitor.entity.Task;
import com.agent.monitor.mapper.TaskMapper;
import com.agent.monitor.websocket.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 任务调度服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final WebSocketMessageSender webSocketMessageSender;

    /**
     * 创建任务
     */
    @Transactional
    public Task createTask(Task task) {
        // 生成 UUID
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            task.setTaskId(UUID.randomUUID().toString());
        }

        // 设置默认值
        if (task.getStatus() == null) {
            task.setStatus("pending");
        }
        if (task.getType() == null) {
            task.setType("general");
        }
        if (task.getPriority() == null) {
            task.setPriority(5);
        }
        if (task.getProgress() == null) {
            task.setProgress(0);
        }

        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        taskMapper.insert(task);
        log.info("创建任务: {} ({})", task.getName(), task.getTaskId());

        // 通知前端
        broadcastTaskUpdate(task);

        return task;
    }

    /**
     * 分配任务给 Agent
     */
    @Transactional
    public Task assignTask(String taskId, String agentId, String memoryId) {
        Task task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setAgentId(agentId);
        task.setMemoryId(memoryId);
        task.setStatus("assigned");
        task.setUpdatedAt(Instant.now());

        taskMapper.update(task);
        log.info("分配任务 {} 给 Agent {}", taskId, agentId);

        broadcastTaskUpdate(task);
        return task;
    }

    /**
     * 开始执行任务
     */
    @Transactional
    public Task startTask(String taskId) {
        Task task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setStatus("running");
        task.setStartedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        taskMapper.update(task);
        log.info("开始执行任务: {}", taskId);

        broadcastTaskUpdate(task);
        return task;
    }

    /**
     * 更新任务进度
     */
    @Transactional
    public Task updateProgress(String taskId, Integer progress, String output) {
        Task task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setProgress(Math.min(100, Math.max(0, progress)));
        if (output != null) {
            task.setOutput(output);
        }
        task.setUpdatedAt(Instant.now());

        taskMapper.update(task);

        if (progress % 25 == 0) {  // 每25%通知一次
            broadcastTaskUpdate(task);
        }

        return task;
    }

    /**
     * 完成任务
     */
    @Transactional
    public Task completeTask(String taskId, String output) {
        Task task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setStatus("completed");
        task.setProgress(100);
        task.setOutput(output);
        task.setCompletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        taskMapper.update(task);
        log.info("任务完成: {}", taskId);

        broadcastTaskUpdate(task);
        return task;
    }

    /**
     * 任务失败
     */
    @Transactional
    public Task failTask(String taskId, String error) {
        Task task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setStatus("failed");
        task.setError(error);
        task.setCompletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        taskMapper.update(task);
        log.warn("任务失败: {} - {}", taskId, error);

        broadcastTaskUpdate(task);
        return task;
    }

    /**
     * 取消任务
     */
    @Transactional
    public Task cancelTask(String taskId) {
        Task task = taskMapper.findByTaskId(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setStatus("cancelled");
        task.setUpdatedAt(Instant.now());

        taskMapper.update(task);
        log.info("任务取消: {}", taskId);

        broadcastTaskUpdate(task);
        return task;
    }

    /**
     * 获取任务
     */
    public Task getTask(String taskId) {
        return taskMapper.findByTaskId(taskId);
    }

    /**
     * 获取所有任务
     */
    public List<Task> getAllTasks() {
        return taskMapper.findAll();
    }

    /**
     * 根据状态获取任务
     */
    public List<Task> getTasksByStatus(String status) {
        return taskMapper.findByStatus(status);
    }

    /**
     * 获取 Agent 的任务
     */
    public List<Task> getTasksByAgent(String agentId) {
        return taskMapper.findByAgentId(agentId);
    }

    /**
     * 获取待分配的任务
     */
    public List<Task> getPendingTasks() {
        return taskMapper.findByStatus("pending");
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(String taskId) {
        int count = taskMapper.deleteByTaskId(taskId);
        if (count > 0) {
            log.info("删除任务: {}", taskId);
        }
    }

    /**
     * 获取统计信息
     */
    public TaskStats getStats() {
        TaskStats stats = new TaskStats();
        stats.setTotalTasks((long) taskMapper.count());
        stats.setPendingTasks((long) taskMapper.countByStatus("pending"));
        stats.setRunningTasks((long) taskMapper.countByStatus("running"));
        stats.setCompletedTasks((long) taskMapper.countByStatus("completed"));
        stats.setFailedTasks((long) taskMapper.countByStatus("failed"));
        return stats;
    }

    /**
     * 广播任务更新
     */
    private void broadcastTaskUpdate(Task task) {
        try {
            webSocketMessageSender.sendAgentEvent(
                task.getAgentId() != null ? task.getAgentId() : "system",
                "task_update",
                Map.of(
                    "task_id", task.getTaskId(),
                    "name", task.getName(),
                    "status", task.getStatus(),
                    "progress", task.getProgress()
                ),
                null  // Task updates don't have event sequence numbers
            );
        } catch (Exception e) {
            log.error("广播任务更新失败", e);
        }
    }

    /**
     * 任务统计信息
     */
    @lombok.Data
    public static class TaskStats {
        private Long totalTasks;
        private Long pendingTasks;
        private Long runningTasks;
        private Long completedTasks;
        private Long failedTasks;
    }
}

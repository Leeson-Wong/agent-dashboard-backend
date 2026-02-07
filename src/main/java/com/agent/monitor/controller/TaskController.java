package com.agent.monitor.controller;

import com.agent.monitor.entity.Task;
import com.agent.monitor.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Task Controller
 * 任务调度 API
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = taskService.createTask(task);
        return ResponseEntity.ok(created);
    }

    /**
     * 分配任务
     */
    @PostMapping("/{taskId}/assign")
    public ResponseEntity<Task> assignTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        String agentId = request.get("agentId");
        String memoryId = request.get("memoryId");
        Task assigned = taskService.assignTask(taskId, agentId, memoryId);
        return ResponseEntity.ok(assigned);
    }

    /**
     * 开始任务
     */
    @PostMapping("/{taskId}/start")
    public ResponseEntity<Task> startTask(@PathVariable String taskId) {
        Task task = taskService.startTask(taskId);
        return ResponseEntity.ok(task);
    }

    /**
     * 更新任务进度
     */
    @PatchMapping("/{taskId}/progress")
    public ResponseEntity<Task> updateProgress(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> request) {
        Integer progress = ((Number) request.get("progress")).intValue();
        String output = (String) request.get("output");
        Task task = taskService.updateProgress(taskId, progress, output);
        return ResponseEntity.ok(task);
    }

    /**
     * 完成任务
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Task> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        String output = request.get("output");
        Task task = taskService.completeTask(taskId, output);
        return ResponseEntity.ok(task);
    }

    /**
     * 任务失败
     */
    @PostMapping("/{taskId}/fail")
    public ResponseEntity<Task> failTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        String error = request.get("error");
        Task task = taskService.failTask(taskId, error);
        return ResponseEntity.ok(task);
    }

    /**
     * 取消任务
     */
    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<Task> cancelTask(@PathVariable String taskId) {
        Task task = taskService.cancelTask(taskId);
        return ResponseEntity.ok(task);
    }

    /**
     * 获取任务
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable String taskId) {
        Task task = taskService.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    /**
     * 获取所有任务
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * 根据状态获取任务
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable String status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取 Agent 的任务
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Task>> getTasksByAgent(@PathVariable String agentId) {
        List<Task> tasks = taskService.getTasksByAgent(agentId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取待分配任务
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Task>> getPendingTasks() {
        List<Task> tasks = taskService.getPendingTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<TaskService.TaskStats> getStats() {
        TaskService.TaskStats stats = taskService.getStats();
        return ResponseEntity.ok(stats);
    }
}

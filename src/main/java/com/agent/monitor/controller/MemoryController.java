package com.agent.monitor.controller;

import com.agent.monitor.entity.Memory;
import com.agent.monitor.service.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Memory Controller
 * Memory CRUD API
 */
@Slf4j
@RestController
@RequestMapping("/api/memories")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;

    /**
     * 创建 Memory
     */
    @PostMapping
    public ResponseEntity<Memory> createMemory(@RequestBody Memory memory) {
        Memory created = memoryService.createMemory(memory);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新 Memory
     */
    @PutMapping("/{memoryId}")
    public ResponseEntity<Memory> updateMemory(
            @PathVariable String memoryId,
            @RequestBody Memory memory) {
        memory.setMemoryId(memoryId);
        Memory updated = memoryService.updateMemory(memory);
        return ResponseEntity.ok(updated);
    }

    /**
     * 获取 Memory
     */
    @GetMapping("/{memoryId}")
    public ResponseEntity<Memory> getMemory(@PathVariable String memoryId) {
        Memory memory = memoryService.getMemory(memoryId);
        if (memory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(memory);
    }

    /**
     * 获取所有 Memory
     */
    @GetMapping
    public ResponseEntity<List<Memory>> getAllMemories() {
        List<Memory> memories = memoryService.getAllMemories();
        return ResponseEntity.ok(memories);
    }

    /**
     * 根据状态获取 Memory
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Memory>> getMemoriesByStatus(@PathVariable String status) {
        List<Memory> memories = memoryService.getMemoriesByStatus(status);
        return ResponseEntity.ok(memories);
    }

    /**
     * 删除 Memory
     */
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> deleteMemory(@PathVariable String memoryId) {
        memoryService.deleteMemory(memoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 激活 Memory
     */
    @PostMapping("/{memoryId}/activate")
    public ResponseEntity<Void> activateMemory(@PathVariable String memoryId) {
        memoryService.activateMemory(memoryId);
        return ResponseEntity.ok().build();
    }

    /**
     * 停用 Memory
     */
    @PostMapping("/{memoryId}/deactivate")
    public ResponseEntity<Void> deactivateMemory(@PathVariable String memoryId) {
        memoryService.deactivateMemory(memoryId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<MemoryService.MemoryStats> getStats() {
        MemoryService.MemoryStats stats = memoryService.getStats();
        return ResponseEntity.ok(stats);
    }
}

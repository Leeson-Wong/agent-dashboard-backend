package com.agent.monitor.controller;

import com.agent.monitor.entity.AgentTemplate;
import com.agent.monitor.service.AgentTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AgentTemplate Controller
 * Agent 模板管理 API
 */
@Slf4j
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class AgentTemplateController {

    private final AgentTemplateService agentTemplateService;

    /**
     * 创建模板
     */
    @PostMapping
    public ResponseEntity<AgentTemplate> createTemplate(@RequestBody AgentTemplate template) {
        AgentTemplate created = agentTemplateService.createTemplate(template);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新模板
     */
    @PutMapping("/{templateId}")
    public ResponseEntity<AgentTemplate> updateTemplate(
            @PathVariable String templateId,
            @RequestBody AgentTemplate template) {
        template.setTemplateId(templateId);
        AgentTemplate updated = agentTemplateService.updateTemplate(template);
        return ResponseEntity.ok(updated);
    }

    /**
     * 获取模板
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<AgentTemplate> getTemplate(@PathVariable String templateId) {
        AgentTemplate template = agentTemplateService.getTemplate(templateId);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    /**
     * 获取所有模板
     */
    @GetMapping
    public ResponseEntity<List<AgentTemplate>> getAllTemplates() {
        List<AgentTemplate> templates = agentTemplateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * 获取启用的模板
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<AgentTemplate>> getEnabledTemplates() {
        List<AgentTemplate> templates = agentTemplateService.getEnabledTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * 根据类型获取模板
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AgentTemplate>> getTemplatesByType(@PathVariable String type) {
        List<AgentTemplate> templates = agentTemplateService.getTemplatesByType(type);
        return ResponseEntity.ok(templates);
    }

    /**
     * 根据分类获取模板
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<AgentTemplate>> getTemplatesByCategory(@PathVariable String category) {
        List<AgentTemplate> templates = agentTemplateService.getTemplatesByCategory(category);
        return ResponseEntity.ok(templates);
    }

    /**
     * 搜索模板
     */
    @GetMapping("/search")
    public ResponseEntity<List<AgentTemplate>> searchTemplates(@RequestParam String keyword) {
        List<AgentTemplate> templates = agentTemplateService.searchTemplates(keyword);
        return ResponseEntity.ok(templates);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateId) {
        agentTemplateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 启用/禁用模板
     */
    @PatchMapping("/{templateId}/enabled")
    public ResponseEntity<Void> setTemplateEnabled(
            @PathVariable String templateId,
            @RequestBody Boolean enabled) {
        agentTemplateService.setTemplateEnabled(templateId, enabled);
        return ResponseEntity.ok().build();
    }

    /**
     * 克隆模板
     */
    @PostMapping("/{templateId}/clone")
    public ResponseEntity<AgentTemplate> cloneTemplate(@PathVariable String templateId) {
        AgentTemplate cloned = agentTemplateService.cloneTemplate(templateId);
        return ResponseEntity.ok(cloned);
    }
}

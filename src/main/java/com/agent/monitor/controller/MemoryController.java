package com.agent.monitor.controller;

import com.agent.monitor.dto.*;
import com.agent.monitor.entity.Memory;
import com.agent.monitor.entity.MemoryExperience;
import com.agent.monitor.entity.MemoryKnowledge;
import com.agent.monitor.entity.MemorySkill;
import com.agent.monitor.entity.MemoryTemporalPatch;
import com.agent.monitor.service.MemoryPatchService;
import com.agent.monitor.service.MemoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Memory Controller
 * Memory CRUD API
 * Design Document: 04-memory-management.md
 */
@Slf4j
@RestController
@RequestMapping("/api/memories")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;
    private final ObjectMapper objectMapper;

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

    // ========================================================
    // 经验管理
    // ========================================================

    /**
     * 提取经验
     *
     * POST /api/memories/{memoryId}/experiences
     */
    @PostMapping("/{memoryId}/experiences")
    public ResponseEntity<ApiResponse<ExperienceDTO>> extractExperience(
            @PathVariable String memoryId,
            @RequestBody ExperienceRequestDTO request) {
        log.info("提取经验: memoryId={}, taskType={}, success={}",
                memoryId, request.getTaskType(), request.getSuccess());

        try {
            String experienceId = memoryService.extractExperience(
                    memoryId,
                    request.getTaskType(),
                    request.getTaskDescription(),
                    request.getComplexity(),
                    request.getSuccess(),
                    request.getOutput(),
                    request.getError()
            );

            // 返回提取的经验
            List<MemoryExperience> experiences = memoryService.getRelevantExperiences(
                    memoryId, request.getTaskType(), 1);

            if (experiences.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(null));
            }

            return ResponseEntity.ok(ApiResponse.success(convertToExperienceDTO(experiences.get(0))));

        } catch (Exception e) {
            log.error("提取经验失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to extract experience: " + e.getMessage()));
        }
    }

    /**
     * 获取相关经验
     *
     * GET /api/memories/{memoryId}/experiences?taskType={}&limit={}
     */
    @GetMapping("/{memoryId}/experiences")
    public ResponseEntity<ApiResponse<List<ExperienceDTO>>> getExperiences(
            @PathVariable String memoryId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.debug("获取经验: memoryId={}, taskType={}, limit={}", memoryId, taskType, limit);

        try {
            List<MemoryExperience> experiences = memoryService.getRelevantExperiences(
                    memoryId, taskType, limit);

            List<ExperienceDTO> dtos = experiences.stream()
                    .map(this::convertToExperienceDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取经验失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get experiences: " + e.getMessage()));
        }
    }

    // ========================================================
    // 知识管理
    // ========================================================

    /**
     * 添加知识
     *
     * POST /api/memories/{memoryId}/knowledge
     */
    @PostMapping("/{memoryId}/knowledge")
    public ResponseEntity<ApiResponse<KnowledgeDTO>> addKnowledge(
            @PathVariable String memoryId,
            @RequestBody KnowledgeRequestDTO request) {
        log.info("添加知识: memoryId={}, type={}, content={}",
                memoryId, request.getType(), request.getContent());

        try {
            Long knowledgeId = memoryService.addKnowledge(
                    memoryId,
                    request.getType(),
                    request.getContent(),
                    request.getSourceType(),
                    request.getSourceDetails(),
                    request.getConfidence()
            );

            // 返回添加的知识
            List<MemoryKnowledge> knowledge = memoryService.getKnowledge(memoryId);
            MemoryKnowledge created = knowledge.stream()
                    .filter(k -> k.getId().equals(knowledgeId))
                    .findFirst()
                    .orElse(null);

            return ResponseEntity.ok(ApiResponse.success(convertToKnowledgeDTO(created)));

        } catch (Exception e) {
            log.error("添加知识失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to add knowledge: " + e.getMessage()));
        }
    }

    /**
     * 获取知识
     *
     * GET /api/memories/{memoryId}/knowledge
     */
    @GetMapping("/{memoryId}/knowledge")
    public ResponseEntity<ApiResponse<List<KnowledgeDTO>>> getKnowledge(
            @PathVariable String memoryId) {
        log.debug("获取知识: memoryId={}", memoryId);

        try {
            List<MemoryKnowledge> knowledge = memoryService.getKnowledge(memoryId);

            List<KnowledgeDTO> dtos = knowledge.stream()
                    .map(this::convertToKnowledgeDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取知识失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get knowledge: " + e.getMessage()));
        }
    }

    /**
     * 验证知识
     *
     * PUT /api/memories/knowledge/{knowledgeId}/verify
     */
    @PutMapping("/knowledge/{knowledgeId}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyKnowledge(
            @PathVariable Long knowledgeId) {
        log.info("验证知识: knowledgeId={}", knowledgeId);

        try {
            boolean verified = memoryService.verifyKnowledge(knowledgeId);
            return ResponseEntity.ok(ApiResponse.success(verified));

        } catch (Exception e) {
            log.error("验证知识失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to verify knowledge: " + e.getMessage()));
        }
    }

    // ========================================================
    // 技能管理
    // ========================================================

    /**
     * 获取技能
     *
     * GET /api/memories/{memoryId}/skills
     */
    @GetMapping("/{memoryId}/skills")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> getSkills(
            @PathVariable String memoryId) {
        log.debug("获取技能: memoryId={}", memoryId);

        try {
            List<MemorySkill> skills = memoryService.getSkills(memoryId);

            List<SkillDTO> dtos = skills.stream()
                    .map(this::convertToSkillDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取技能失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get skills: " + e.getMessage()));
        }
    }

    /**
     * 获取最熟练技能
     *
     * GET /api/memories/{memoryId}/skills/top?limit={}
     */
    @GetMapping("/{memoryId}/skills/top")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> getTopSkills(
            @PathVariable String memoryId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        log.debug("获取最熟练技能: memoryId={}, limit={}", memoryId, limit);

        try {
            List<MemorySkill> skills = memoryService.getMostProficientSkills(memoryId, limit);

            List<SkillDTO> dtos = skills.stream()
                    .map(this::convertToSkillDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取最熟练技能失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get top skills: " + e.getMessage()));
        }
    }

    /**
     * 记录技能使用
     *
     * POST /api/memories/{memoryId}/skills/usage
     */
    @PostMapping("/{memoryId}/skills/usage")
    public ResponseEntity<ApiResponse<SkillDTO>> recordSkillUsage(
            @PathVariable String memoryId,
            @RequestBody SkillUsageRequestDTO request) {
        log.info("记录技能使用: memoryId={}, skill={}, success={}",
                memoryId, request.getSkillName(), request.getSuccess());

        try {
            Long skillId = memoryService.recordSkillUsage(
                    memoryId,
                    request.getSkillName(),
                    request.getCategory(),
                    request.getSuccess(),
                    request.getDurationSeconds()
            );

            // 返回更新后的技能
            List<MemorySkill> skills = memoryService.getSkills(memoryId);
            MemorySkill updatedSkill = skills.stream()
                    .filter(s -> s.getId().equals(skillId))
                    .findFirst()
                    .orElse(null);

            return ResponseEntity.ok(ApiResponse.success(convertToSkillDTO(updatedSkill)));

        } catch (Exception e) {
            log.error("记录技能使用失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to record skill usage: " + e.getMessage()));
        }
    }

    // ========================================================
    // 时间补丁管理
    // ========================================================

    /**
     * 创建时间补丁
     *
     * POST /api/memories/{memoryId}/patches
     */
    @PostMapping("/{memoryId}/patches")
    public ResponseEntity<ApiResponse<PatchDTO>> createPatch(
            @PathVariable String memoryId,
            @RequestBody PatchRequestDTO request) {
        log.info("创建时间补丁: memoryId={}, type={}, description={}",
                memoryId, request.getType(), request.getDescription());

        try {
            String patchId = memoryService.createPatch(
                    memoryId,
                    request.getType(),
                    request.getDescription(),
                    request.getEventDate(),
                    request.getAffectedDomains(),
                    request.getPatchData(),
                    request.getConfidence(),
                    request.getSourceType(),
                    request.getProvidedBy()
            );

            // 返回创建的补丁
            List<MemoryTemporalPatch> patches = memoryService.getPatches(memoryId);
            MemoryTemporalPatch createdPatch = patches.stream()
                    .filter(p -> p.getPatchId().equals(patchId))
                    .findFirst()
                    .orElse(null);

            return ResponseEntity.ok(ApiResponse.success(convertToPatchDTO(createdPatch)));

        } catch (Exception e) {
            log.error("创建时间补丁失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to create patch: " + e.getMessage()));
        }
    }

    /**
     * 获取补丁
     *
     * GET /api/memories/{memoryId}/patches
     */
    @GetMapping("/{memoryId}/patches")
    public ResponseEntity<ApiResponse<List<PatchDTO>>> getPatches(
            @PathVariable String memoryId) {
        log.debug("获取补丁: memoryId={}", memoryId);

        try {
            List<MemoryTemporalPatch> patches = memoryService.getPatches(memoryId);

            List<PatchDTO> dtos = patches.stream()
                    .map(this::convertToPatchDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(dtos));

        } catch (Exception e) {
            log.error("获取补丁失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get patches: " + e.getMessage()));
        }
    }

    /**
     * 应用时间补丁
     *
     * POST /api/memories/{memoryId}/patches/apply
     */
    @PostMapping("/{memoryId}/patches/apply")
    public ResponseEntity<ApiResponse<PatchApplyResultDTO>> applyPatches(
            @PathVariable String memoryId,
            @RequestParam(required = false) String sessionId) {
        log.info("应用时间补丁: memoryId={}, sessionId={}", memoryId, sessionId);

        try {
            // 如果没有提供 sessionId，生成一个
            String actualSessionId = sessionId != null ? sessionId :
                    "session_" + System.currentTimeMillis();

            MemoryPatchService.PatchResult result =
                    memoryService.applyPatches(memoryId, actualSessionId);

            PatchApplyResultDTO dto = PatchApplyResultDTO.success(
                    result.getMessage(),
                    0, // appliedCount not available in result
                    0  // failedCount not available in result
            );
            dto.setSuccess(result.isSuccess());

            return ResponseEntity.ok(ApiResponse.success(dto));

        } catch (Exception e) {
            log.error("应用时间补丁失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to apply patches: " + e.getMessage()));
        }
    }

    // ========================================================
    // DTO 转换方法
    // ========================================================

    private ExperienceDTO convertToExperienceDTO(MemoryExperience entity) {
        if (entity == null) return null;
        ExperienceDTO dto = new ExperienceDTO();
        dto.setExperienceId(entity.getExperienceId());
        dto.setMemoryId(entity.getMemoryId());
        dto.setType(entity.getType());
        dto.setTaskType(entity.getTaskType());
        dto.setTaskDescription(entity.getTaskDescription());
        dto.setTaskComplexity(entity.getTaskComplexity());
        dto.setApproach(entity.getApproach());
        dto.setLearning(entity.getLearning());
        dto.setOutcome(entity.getOutcome());
        dto.setCreatedAt(entity.getTimestamp());
        return dto;
    }

    private KnowledgeDTO convertToKnowledgeDTO(MemoryKnowledge entity) {
        if (entity == null) return null;
        KnowledgeDTO dto = new KnowledgeDTO();
        dto.setId(entity.getId());
        dto.setMemoryId(entity.getMemoryId());
        dto.setType(entity.getType());
        dto.setContent(entity.getContent());
        dto.setSourceType(entity.getSourceType());
        dto.setSourceDetails(entity.getSourceDetails());
        dto.setConfidence(entity.getConfidence());
        dto.setVerified(entity.getVerified());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private SkillDTO convertToSkillDTO(MemorySkill entity) {
        if (entity == null) return null;
        SkillDTO dto = new SkillDTO();
        dto.setId(entity.getId());
        dto.setMemoryId(entity.getMemoryId());
        dto.setSkillName(entity.getSkillName());
        dto.setCategory(entity.getCategory());
        dto.setProficiencyLevel(entity.getProficiencyLevel());
        dto.setTotalPracticeTime(entity.getTotalPracticeTime());
        dto.setLastPracticedAt(entity.getLastPracticedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private PatchDTO convertToPatchDTO(MemoryTemporalPatch entity) {
        if (entity == null) return null;
        PatchDTO dto = new PatchDTO();
        dto.setPatchId(entity.getPatchId());
        dto.setMemoryId(entity.getMemoryId());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setEventDate(entity.getEventDate());
        dto.setPatch(entity.getPatch());
        dto.setConfidence(entity.getConfidence());
        dto.setSourceType(entity.getSourceType());
        dto.setProvidedBy(entity.getProvidedBy());
        dto.setApplied(entity.getApplied());
        dto.setCreatedAt(entity.getCreatedAt());

        // 解析 affectedDomains
        try {
            if (entity.getAffectedDomains() != null) {
                List<String> domains = objectMapper.readValue(
                        entity.getAffectedDomains(),
                        new TypeReference<List<String>>() {}
                );
                dto.setAffectedDomains(domains);
            }
        } catch (Exception e) {
            log.warn("解析 affectedDomains 失败", e);
        }

        return dto;
    }
}

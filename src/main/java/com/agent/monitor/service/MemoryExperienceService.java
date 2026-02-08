package com.agent.monitor.service;

import com.agent.monitor.entity.MemoryExperience;
import com.agent.monitor.mapper.MemoryExperienceMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Memory 经验管理服务
 *
 * 负责从任务执行中提取、存储和检索经验
 * Design Document: 04-memory-management.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryExperienceService {

    private final MemoryExperienceMapper experienceMapper;
    private final ObjectMapper objectMapper;

    /**
     * 从任务执行中提取经验
     *
     * @param memoryId Memory ID
     * @param taskType 任务类型
     * @param taskDescription 任务描述
     * @param complexity 任务复杂度 (1-10)
     * @param success 是否成功
     * @param output 任务输出
     * @param error 错误信息（如果有）
     * @return 提取的经验ID
     */
    @Transactional
    public String extractExperience(String memoryId, String taskType, String taskDescription,
                                   Integer complexity, boolean success, String output, String error) {
        log.debug("提取经验: memoryId={}, task={}, success={}", memoryId, taskType, success);

        try {
            MemoryExperience experience = new MemoryExperience();
            experience.setMemoryId(memoryId);
            experience.setExperienceId(UUID.randomUUID().toString());

            // 设置类型
            experience.setType(success ? "success" : "failure");

            // 任务信息
            experience.setTaskType(taskType);
            experience.setTaskDescription(taskDescription);
            experience.setTaskComplexity(complexity != null ? complexity : 5);

            // 构建数据
            ExperienceData data = new ExperienceData();
            data.setSuccess(success);
            data.setOutput(output);
            data.setError(error);
            data.setTimestamp(System.currentTimeMillis());

            experience.setApproach(objectMapper.writeValueAsString(data));

            // 学习内容
            LearningData learning = new LearningData();
            if (success) {
                learning.setLessons("Task completed successfully");
            } else {
                learning.setLessons("Task failed: " + (error != null ? error : "Unknown error"));
            }
            experience.setLearning(objectMapper.writeValueAsString(learning));

            // 结果
            OutcomeData outcome = new OutcomeData();
            outcome.setSuccess(success);
            outcome.setQuality(success ? 8 : 3); // 简化的质量评分
            experience.setOutcome(objectMapper.writeValueAsString(outcome));

            experienceMapper.insert(experience);

            log.info("经验已保存: experienceId={}, type={}", experience.getExperienceId(), experience.getType());
            return experience.getExperienceId();

        } catch (JsonProcessingException e) {
            log.error("经验数据序列化失败", e);
            throw new RuntimeException("Failed to extract experience", e);
        }
    }

    /**
     * 获取相关经验
     *
     * @param memoryId Memory ID
     * @param taskType 任务类型（可选）
     * @param limit 限制数量
     * @return 相关经验列表
     */
    public List<MemoryExperience> getRelevantExperiences(String memoryId, String taskType, Integer limit) {
        log.debug("获取相关经验: memoryId={}, taskType={}", memoryId, taskType);

        if (taskType != null) {
            return experienceMapper.findByTaskType(memoryId, taskType);
        }

        Integer actualLimit = limit != null ? limit : 10;
        return experienceMapper.findRecent(memoryId, actualLimit);
    }

    /**
     * 获取所有成功经验
     */
    public List<MemoryExperience> getSuccessExperiences(String memoryId) {
        return experienceMapper.findByType(memoryId, "success");
    }

    /**
     * 获取所有失败经验
     */
    public List<MemoryExperience> getFailureExperiences(String memoryId) {
        return experienceMapper.findByType(memoryId, "failure");
    }

    /**
     * 内部数据类
     */
    private static class ExperienceData {
        private boolean success;
        private String output;
        private String error;
        private long timestamp;

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    private static class LearningData {
        private String lessons;

        public String getLessons() { return lessons; }
        public void setLessons(String lessons) { this.lessons = lessons; }
    }

    private static class OutcomeData {
        private boolean success;
        private int quality;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public int getQuality() { return quality; }
        public void setQuality(int quality) { this.quality = quality; }
    }
}

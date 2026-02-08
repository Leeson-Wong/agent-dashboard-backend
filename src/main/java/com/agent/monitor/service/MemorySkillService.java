package com.agent.monitor.service;

import com.agent.monitor.entity.MemorySkill;
import com.agent.monitor.mapper.MemorySkillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Memory 技能管理服务
 *
 * 负责技能熟练度的跟踪和更新
 * Design Document: 04-memory-management.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemorySkillService {

    private final MemorySkillMapper skillMapper;

    /**
     * 记录工具使用，更新技能熟练度
     *
     * @param memoryId Memory ID
     * @param skillName 技能名称
     * @param category 技能类别
     * @param success 是否成功
     * @param durationSeconds 执行时长（秒）
     * @return 技能ID
     */
    @Transactional
    public Long recordSkillUsage(String memoryId, String skillName, String category,
                                boolean success, Long durationSeconds) {
        log.debug("记录技能使用: memoryId={}, skill={}, success={}", memoryId, skillName, success);

        MemorySkill skill = skillMapper.findByMemoryIdAndSkillName(memoryId, skillName);

        if (skill == null) {
            // 创建新技能
            skill = new MemorySkill();
            skill.setMemoryId(memoryId);
            skill.setSkillName(skillName);
            skill.setCategory(category);
            skill.setProficiencyLevel(0);
            skill.setTotalPracticeTime(0L);
            skill.setCreatedAt(Instant.now());

            skillMapper.insert(skill);
            log.info("新技能已创建: id={}, name={}", skill.getId(), skillName);
        }

        // 更新熟练度
        if (success) {
            // 熟练度增加：基础 + 1，但不超过 100
            int increment = Math.max(1, (int) (durationSeconds / 60)); // 每分钟+1
            int newLevel = Math.min(100, skill.getProficiencyLevel() + increment);
            skill.setProficiencyLevel(newLevel);

            // 更新练习时间
            skill.setTotalPracticeTime(skill.getTotalPracticeTime() + (durationSeconds != null ? durationSeconds : 0));
            skill.setLastPracticedAt(Instant.now());

            skillMapper.update(skill);

            log.info("技能熟练度已更新: skill={}, level={}, time={}s",
                    skillName, newLevel, skill.getTotalPracticeTime());
        } else {
            log.debug("技能使用失败，不更新熟练度: skill={}", skillName);
        }

        return skill.getId();
    }

    /**
     * 获取 Memory 的所有技能
     */
    public List<MemorySkill> getSkills(String memoryId) {
        return skillMapper.findByMemoryId(memoryId);
    }

    /**
     * 获取最熟练的技能
     */
    public List<MemorySkill> getMostProficientSkills(String memoryId, Integer limit) {
        return skillMapper.findMostProficient(memoryId, limit != null ? limit : 5);
    }

    /**
     * 获取特定类别的技能
     */
    public List<MemorySkill> getSkillsByCategory(String memoryId, String category) {
        return skillMapper.findByCategory(memoryId, category);
    }

    /**
     * 增加熟练度
     */
    @Transactional
    public boolean incrementProficiency(Long skillId, Integer increment) {
        log.debug("增加熟练度: skillId={}, increment={}", skillId, increment);

        MemorySkill skill = skillMapper.findById(skillId);
        if (skill == null) {
            log.warn("技能不存在: id={}", skillId);
            return false;
        }

        int newLevel = Math.min(100, skill.getProficiencyLevel() + increment);
        skill.setProficiencyLevel(newLevel);
        skill.setUpdatedAt(Instant.now());

        return skillMapper.update(skill) > 0;
    }

    /**
     * 更新练习时间
     */
    @Transactional
    public boolean updatePracticeTime(Long skillId, Long additionalSeconds) {
        log.debug("更新练习时间: skillId={}, seconds={}", skillId, additionalSeconds);

        return skillMapper.updatePracticeTime(skillId, additionalSeconds) > 0;
    }

    /**
     * 获取技能熟练度
     */
    public Integer getProficiencyLevel(String memoryId, String skillName) {
        MemorySkill skill = skillMapper.findByMemoryIdAndSkillName(memoryId, skillName);
        return skill != null ? skill.getProficiencyLevel() : 0;
    }
}

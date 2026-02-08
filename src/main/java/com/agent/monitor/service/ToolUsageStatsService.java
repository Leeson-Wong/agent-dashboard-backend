package com.agent.monitor.service;

import com.agent.monitor.entity.ToolUsageStats;
import com.agent.monitor.mapper.ToolUsageStatsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 工具使用统计服务
 *
 * 负责工具熟练度跟踪和使用统计
 * Design Document: 05-agent-behavior.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolUsageStatsService {

    private final ToolUsageStatsMapper statsMapper;

    /**
     * 记录工具使用
     *
     * @param memoryId Memory ID
     * @param toolId Tool ID
     * @param success Whether successful
     * @param durationSeconds Duration in seconds
     */
    @Transactional
    public void recordUsage(String memoryId, String toolId, boolean success, long durationSeconds) {
        log.debug("记录工具使用: memoryId={}, toolId={}, success={}", memoryId, toolId, success);

        ToolUsageStats stats = statsMapper.findByToolIdAndMemoryId(toolId, memoryId);

        if (stats == null) {
            // 创建新的统计记录
            stats = new ToolUsageStats();
            stats.setToolId(toolId);
            stats.setMemoryId(memoryId);
            stats.setTotalUses(0L);
            stats.setSuccessfulUses(0L);
            stats.setFailedUses(0L);
            stats.setProficiencyLevel(BigDecimal.ZERO);
            stats.setTotalPracticeTime(0L);
            stats.setCreatedAt(Instant.now());

            statsMapper.insert(stats);
            log.info("工具统计记录已创建: toolId={}, memoryId={}", toolId, memoryId);
        }

        // 更新使用次数
        statsMapper.incrementUses(toolId, memoryId, success);

        // 更新练习时间 (无论成功失败都记录)
        statsMapper.updatePracticeTime(toolId, memoryId, durationSeconds);

        // 如果成功，更新熟练度
        if (success) {
            ToolUsageStats updated = statsMapper.findByToolIdAndMemoryId(toolId, memoryId);
            BigDecimal newProficiency = calculateProficiency(updated);
            statsMapper.updateProficiency(toolId, memoryId, newProficiency);

            log.info("工具熟练度已更新: toolId={}, level={}", toolId, newProficiency);
        }
    }

    /**
     * 获取工具统计
     *
     * @param memoryId Memory ID
     * @param toolId Tool ID
     * @return Tool usage stats
     */
    public ToolUsageStats getStats(String memoryId, String toolId) {
        return statsMapper.findByToolIdAndMemoryId(toolId, memoryId);
    }

    /**
     * 获取 Memory 的所有工具统计
     *
     * @param memoryId Memory ID
     * @return Tool usage stats
     */
    public List<ToolUsageStats> getAllStats(String memoryId) {
        return statsMapper.findByMemoryId(memoryId);
    }

    /**
     * 获取最熟练的工具
     *
     * @param memoryId Memory ID
     * @param limit Limit
     * @return Most proficient tools
     */
    public List<ToolUsageStats> getMostProficientTools(String memoryId, int limit) {
        return statsMapper.findMostProficient(memoryId, limit);
    }

    /**
     * 获取工具熟练度
     *
     * @param memoryId Memory ID
     * @param toolId Tool ID
     * @return Proficiency level (0.0-1.0)
     */
    public BigDecimal getProficiency(String memoryId, String toolId) {
        ToolUsageStats stats = statsMapper.findByToolIdAndMemoryId(toolId, memoryId);
        return stats != null ? stats.getProficiencyLevel() : BigDecimal.ZERO;
    }

    /**
     * 手动设置熟练度
     *
     * @param memoryId Memory ID
     * @param toolId Tool ID
     * @param proficiency Proficiency level (0.0-1.0)
     * @return Whether successful
     */
    @Transactional
    public boolean setProficiency(String memoryId, String toolId, BigDecimal proficiency) {
        log.debug("设置熟练度: memoryId={}, toolId={}, proficiency={}", memoryId, toolId, proficiency);

        // 确保熟练度在有效范围内
        if (proficiency.compareTo(BigDecimal.ZERO) < 0) {
            proficiency = BigDecimal.ZERO;
        } else if (proficiency.compareTo(BigDecimal.ONE) > 0) {
            proficiency = BigDecimal.ONE;
        }

        ToolUsageStats stats = statsMapper.findByToolIdAndMemoryId(toolId, memoryId);

        if (stats == null) {
            // 创建新的统计记录
            stats = new ToolUsageStats();
            stats.setToolId(toolId);
            stats.setMemoryId(memoryId);
            stats.setTotalUses(0L);
            stats.setSuccessfulUses(0L);
            stats.setFailedUses(0L);
            stats.setProficiencyLevel(proficiency);
            stats.setTotalPracticeTime(0L);
            stats.setCreatedAt(Instant.now());

            statsMapper.insert(stats);
            log.info("工具统计记录已创建并设置熟练度: toolId={}, proficiency={}", toolId, proficiency);
            return true;
        }

        // 更新熟练度
        int updated = statsMapper.updateProficiency(toolId, memoryId, proficiency);
        log.info("工具熟练度已更新: toolId={}, proficiency={}", toolId, proficiency);
        return updated > 0;
    }

    /**
     * 增加熟练度
     *
     * @param memoryId Memory ID
     * @param toolId Tool ID
     * @param increment Increment amount (0.0-1.0)
     * @return Whether successful
     */
    @Transactional
    public boolean incrementProficiency(String memoryId, String toolId, BigDecimal increment) {
        ToolUsageStats stats = statsMapper.findByToolIdAndMemoryId(toolId, memoryId);

        if (stats == null) {
            log.warn("工具统计记录不存在: toolId={}, memoryId={}", toolId, memoryId);
            return false;
        }

        BigDecimal newProficiency = stats.getProficiencyLevel().add(increment);
        return setProficiency(memoryId, toolId, newProficiency);
    }

    /**
     * 定时任务：清理长时间未使用的工具统计
     */
    @Scheduled(fixedRate = 86400000, initialDelay = 300000)  // 每天运行，初始延迟5分钟
    @Transactional
    public void cleanupUnusedStats() {
        log.info("清理长时间未使用的工具统计");

        // TODO: 实现清理逻辑（删除超过90天未使用的统计记录）
        // 需要在 mapper 中添加相应的方法

        log.info("工具统计清理完成");
    }

    /**
     * 计算熟练度
     *
     * @param stats Tool usage stats
     * @return Proficiency level (0.0-1.0)
     */
    private BigDecimal calculateProficiency(ToolUsageStats stats) {
        if (stats.getTotalUses() == 0) {
            return BigDecimal.ZERO;
        }

        // 基础熟练度：基于成功率
        double successRate = stats.getSuccessfulUses() / (double) stats.getTotalUses();

        // 时间因子：基于练习时间（每分钟增加 0.01）
        double timeFactor = stats.getTotalPracticeTime() / 60.0 * 0.01;

        // 综合熟练度
        double proficiency = successRate * 0.7 + timeFactor * 0.3;

        // 限制在 0.0-1.0 范围内
        proficiency = Math.max(0.0, Math.min(1.0, proficiency));

        return BigDecimal.valueOf(proficiency).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取工具使用建议
     *
     * @param memoryId Memory ID
     * @param taskId Task ID
     * @return Recommended tools
     */
    public List<ToolUsageStats> getRecommendedTools(String memoryId, String taskId) {
        // TODO: 实现基于任务类型和历史的推荐算法
        // 目前返回最熟练的工具
        return getMostProficientTools(memoryId, 5);
    }
}

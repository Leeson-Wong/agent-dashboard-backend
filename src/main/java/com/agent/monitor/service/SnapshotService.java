package com.agent.monitor.service;

import com.agent.monitor.dto.SnapshotDTO;
import com.agent.monitor.entity.AgentEvent;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.entity.Snapshot;
import com.agent.monitor.mapper.AgentEventMapper;
import com.agent.monitor.mapper.AgentStateMapper;
import com.agent.monitor.mapper.SnapshotMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 快照服务
 *
 * 负责生成、存储和清理 Agent 状态快照
 * Design Document: 02-snapshot-delta-sync.md
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final SnapshotMapper snapshotMapper;
    private final AgentStateMapper agentStateMapper;
    private final AgentEventMapper agentEventMapper;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final ObjectMapper objectMapper;

    /**
     * 快照保留时间 (24 小时)
     */
    private static final long SNAPSHOT_RETENTION_HOURS = 24;

    /**
     * 生成新快照
     *
     * @return 生成的快照
     */
    @Transactional
    public SnapshotDTO generateSnapshot() {
        log.debug("开始生成快照");

        try {
            // 1. 获取当前所有 Agent 状态
            List<AgentState> allAgents = agentStateMapper.findAll();
            log.debug("当前有 {} 个 Agent", allAgents.size());

            // 2. 获取当前最大序列号
            Long maxSeq = agentEventMapper.getMaxSeq();
            log.debug("当前最大序列号: {}", maxSeq);

            // 3. 构建 SnapshotDTO
            SnapshotDTO snapshotDTO = new SnapshotDTO();
            snapshotDTO.setSnapshotId(UUID.randomUUID().toString());
            snapshotDTO.setSeq(maxSeq != null ? maxSeq : 0L);
            snapshotDTO.setCreatedAt(Instant.now());

            // 设置过期时间
            Instant expiresAt = Instant.now().plusSeconds(SNAPSHOT_RETENTION_HOURS * 3600);
            snapshotDTO.setExpiresAt(expiresAt);

            // 4. 构建 Agent 数据列表
            SnapshotDTO.AgentListData agentListData = new SnapshotDTO.AgentListData();
            List<SnapshotDTO.AgentData> agents = allAgents.stream()
                    .map(this::convertToAgentData)
                    .collect(Collectors.toList());
            agentListData.setAgents(agents);
            snapshotDTO.setData(agentListData);

            // 5. 转换为 JSON 存储
            String jsonData = objectMapper.writeValueAsString(snapshotDTO);

            // 6. 保存到数据库
            Snapshot snapshot = new Snapshot();
            snapshot.setSnapshotId(snapshotDTO.getSnapshotId());
            snapshot.setSeq(snapshotDTO.getSeq());
            snapshot.setData(jsonData);
            snapshot.setCreatedAt(snapshotDTO.getCreatedAt());
            snapshot.setExpiresAt(expiresAt);

            snapshotMapper.insert(snapshot);

            log.info("快照生成成功: snapshotId={}, seq={}, agents={}",
                    snapshotDTO.getSnapshotId(), snapshotDTO.getSeq(), agents.size());

            return snapshotDTO;

        } catch (JsonProcessingException e) {
            log.error("快照 JSON 序列化失败", e);
            throw new RuntimeException("快照生成失败", e);
        }
    }

    /**
     * 获取最新快照
     *
     * @return 最新快照，如果没有则返回 null
     */
    public SnapshotDTO getLatestSnapshot() {
        log.debug("获取最新快照");

        Snapshot snapshot = snapshotMapper.findLatest();
        if (snapshot == null) {
            log.debug("没有可用的快照");
            return null;
        }

        try {
            return objectMapper.readValue(snapshot.getData(), SnapshotDTO.class);
        } catch (JsonProcessingException e) {
            log.error("快照 JSON 反序列化失败: snapshotId={}", snapshot.getSnapshotId(), e);
            return null;
        }
    }

    /**
     * 根据 snapshot_id 获取快照
     *
     * @param snapshotId 快照 ID
     * @return 快照数据，如果没有则返回 null
     */
    public SnapshotDTO getSnapshotById(String snapshotId) {
        log.debug("获取快照: snapshotId={}", snapshotId);

        Snapshot snapshot = snapshotMapper.findBySnapshotId(snapshotId);
        if (snapshot == null) {
            log.debug("快照不存在: snapshotId={}", snapshotId);
            return null;
        }

        try {
            return objectMapper.readValue(snapshot.getData(), SnapshotDTO.class);
        } catch (JsonProcessingException e) {
            log.error("快照 JSON 反序列化失败: snapshotId={}", snapshotId, e);
            return null;
        }
    }

    /**
     * 清理过期快照
     *
     * @return 删除的快照数量
     */
    @Transactional
    public int cleanupExpiredSnapshots() {
        log.debug("开始清理过期快照");

        int deleted = snapshotMapper.deleteExpired();
        log.info("清理过期快照: 删除 {} 个", deleted);

        return deleted;
    }

    /**
     * 获取下一个序列号
     *
     * @return 下一个序列号
     */
    public Long getNextSequence() {
        Long seq = sequenceGeneratorService.getNextValue("agent_events_seq");
        log.debug("获取序列号: {}", seq);
        return seq;
    }

    /**
     * 保存事件到事件流
     *
     * @param event 事件
     */
    @Transactional
    public void saveEvent(AgentEvent event) {
        if (event.getSeq() == null) {
            event.setSeq(getNextSequence());
        }
        agentEventMapper.insert(event);
        log.debug("事件已保存: seq={}, type={}, agent={}",
                event.getSeq(), event.getEventType(), event.getAgentId());
    }

    /**
     * 获取指定序列号之后的事件
     *
     * @param since 起始序列号
     * @param limit 限制数量
     * @return 事件列表
     */
    public List<AgentEvent> getEventsAfterSeq(Long since, Integer limit) {
        return agentEventMapper.findAfterSeq(since, limit);
    }

    /**
     * 定时任务：每 30 秒生成一次快照
     */
    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    @Transactional
    public void autoGenerateSnapshot() {
        log.debug("定时生成快照");
        try {
            SnapshotDTO snapshot = generateSnapshot();
            log.info("定时快照生成成功: snapshotId={}, agents={}",
                    snapshot.getSnapshotId(),
                    snapshot.getData().getAgents().size());
        } catch (Exception e) {
            log.error("定时快照生成失败", e);
        }
    }

    /**
     * 定时任务：每小时清理一次过期快照
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    @Transactional
    public void autoCleanupSnapshots() {
        log.debug("定时清理过期快照");
        try {
            cleanupExpiredSnapshots();
        } catch (Exception e) {
            log.error("定时清理快照失败", e);
        }
    }

    /**
     * 将 AgentState 转换为 AgentData
     */
    private SnapshotDTO.AgentData convertToAgentData(AgentState state) {
        SnapshotDTO.AgentData data = new SnapshotDTO.AgentData();
        data.setAgentId(state.getAgentId());
        data.setServerId(state.getServerId());
        data.setFramework(state.getFramework());
        data.setLanguage(state.getLanguage());
        data.setStatus(state.getStatus());
        data.setCurrentActivity(state.getCurrentActivity());
        data.setCurrentTool(state.getCurrentTool());
        data.setCurrentTaskId(state.getCurrentTaskId());
        data.setMemoryId(state.getMemoryId());
        data.setRole(state.getRole());
        data.setLastActivity(state.getLastActivity());
        data.setCreatedAt(state.getCreatedAt());
        data.setUpdatedAt(state.getUpdatedAt());
        return data;
    }
}

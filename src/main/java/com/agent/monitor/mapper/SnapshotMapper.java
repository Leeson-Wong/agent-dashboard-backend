package com.agent.monitor.mapper;

import com.agent.monitor.entity.Snapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent 状态快照 Mapper
 * Design Document: 02-snapshot-delta-sync.md
 */
@Mapper
public interface SnapshotMapper {

    /**
     * 插入新快照
     */
    int insert(Snapshot snapshot);

    /**
     * 根据 ID 查找快照
     */
    Snapshot findById(@Param("id") Long id);

    /**
     * 根据 snapshot_id 查找快照
     */
    Snapshot findBySnapshotId(@Param("snapshotId") String snapshotId);

    /**
     * 获取最新快照
     */
    Snapshot findLatest();

    /**
     * 获取指定序列号之后的快照
     */
    List<Snapshot> findAfterSeq(@Param("seq") Long seq);

    /**
     * 获取所有快照
     */
    List<Snapshot> findAll();

    /**
     * 删除过期快照
     */
    int deleteExpired();

    /**
     * 删除指定快照
     */
    int deleteById(@Param("id") Long id);
}

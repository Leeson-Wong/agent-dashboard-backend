package com.agent.monitor.mapper;

import com.agent.monitor.entity.MemorySession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * Memory 会话 Mapper
 * Design Document: 04-memory-management.md
 */
@Mapper
public interface MemorySessionMapper {

    /**
     * 插入会话
     */
    int insert(MemorySession session);

    /**
     * 根据 ID 查找
     */
    MemorySession findById(@Param("id") Long id);

    /**
     * 根据 session_id 查找
     */
    MemorySession findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据 memory_id 查找所有会话
     */
    List<MemorySession> findByMemoryId(@Param("memoryId") String memoryId);

    /**
     * 查找活跃会话（未完成的）
     */
    List<MemorySession> findActive(@Param("memoryId") String memoryId);

    /**
     * 查找指定时间段内的会话
     */
    List<MemorySession> findByTimeRange(@Param("memoryId") String memoryId,
                                        @Param("startTime") Instant startTime,
                                        @Param("endTime") Instant endTime);

    /**
     * 更新会话
     */
    int update(MemorySession session);

    /**
     * 完成会话
     */
    int completeSession(@Param("sessionId") String sessionId, @Param("completedAt") Instant completedAt);

    /**
     * 删除会话
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);
}

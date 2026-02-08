package com.agent.monitor.mapper;

import com.agent.monitor.entity.SequenceGenerator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 序列号生成器 Mapper
 * Design Document: 02-snapshot-delta-sync.md
 */
@Mapper
public interface SequenceGeneratorMapper {

    /**
     * 根据 sequence_name 查找
     */
    SequenceGenerator findBySequenceName(@Param("sequenceName") String sequenceName);

    /**
     * 获取并增加序列号 (原子操作)
     */
    Long getNextValue(@Param("sequenceName") String sequenceName);

    /**
     * 初始化序列
     */
    int insert(SequenceGenerator sequenceGenerator);

    /**
     * 重置序列值
     */
    int updateValue(@Param("sequenceName") String sequenceName, @Param("value") Long value);
}

package com.agent.monitor.mapper;

import com.agent.monitor.entity.AgentTemplate;
import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;

/**
 * AgentTemplate Mapper
 */
@Mapper
public interface AgentTemplateMapper {

    /**
     * 插入模板
     */
    @Insert("INSERT INTO agent_templates (template_id, name, description, type, category, " +
            "role, goal, backstory, persona, skills, tools, default_model, default_temperature, " +
            "default_max_tokens, system_prompt_template, enabled, version, author, tags, " +
            "usage_count, created_at, updated_at) " +
            "VALUES (#{templateId}, #{name}, #{description}, #{type}, #{category}, " +
            "#{role}, #{goal}, #{backstory}, #{persona}, #{skills}, #{tools}, #{defaultModel}, " +
            "#{defaultTemperature}, #{defaultMaxTokens}, #{systemPromptTemplate}, #{enabled}, " +
            "#{version}, #{author}, #{tags}, #{usageCount}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AgentTemplate template);

    /**
     * 更新模板
     */
    @Update("UPDATE agent_templates SET name=#{name}, description=#{description}, type=#{type}, " +
            "category=#{category}, role=#{role}, goal=#{goal}, backstory=#{backstory}, persona=#{persona}, " +
            "skills=#{skills}, tools=#{tools}, default_model=#{defaultModel}, default_temperature=#{defaultTemperature}, " +
            "default_max_tokens=#{defaultMaxTokens}, system_prompt_template=#{systemPromptTemplate}, " +
            "enabled=#{enabled}, version=#{version}, tags=#{tags}, updated_at=#{updatedAt} " +
            "WHERE template_id=#{templateId}")
    int update(AgentTemplate template);

    /**
     * 根据 ID 查找模板
     */
    @Select("SELECT * FROM agent_templates WHERE template_id = #{templateId}")
    AgentTemplate findByTemplateId(String templateId);

    /**
     * 查找所有模板
     */
    @Select("SELECT * FROM agent_templates ORDER BY updated_at DESC")
    List<AgentTemplate> findAll();

    /**
     * 查找启用的模板
     */
    @Select("SELECT * FROM agent_templates WHERE enabled = true ORDER BY usage_count DESC, updated_at DESC")
    List<AgentTemplate> findEnabled();

    /**
     * 根据类型查找模板
     */
    @Select("SELECT * FROM agent_templates WHERE type = #{type} AND enabled = true ORDER BY usage_count DESC")
    List<AgentTemplate> findByType(String type);

    /**
     * 根据分类查找模板
     */
    @Select("SELECT * FROM agent_templates WHERE category = #{category} AND enabled = true ORDER BY usage_count DESC")
    List<AgentTemplate> findByCategory(String category);

    /**
     * 根据名称搜索模板
     */
    @Select("SELECT * FROM agent_templates WHERE name LIKE CONCAT('%', #{keyword}, '%') AND enabled = true")
    List<AgentTemplate> searchByName(String keyword);

    /**
     * 删除模板
     */
    @Delete("DELETE FROM agent_templates WHERE template_id = #{templateId}")
    int deleteByTemplateId(String templateId);

    /**
     * 增加使用次数
     */
    @Update("UPDATE agent_templates SET usage_count = usage_count + 1, updated_at = #{updatedAt} WHERE template_id = #{templateId}")
    int incrementUsage(@Param("templateId") String templateId, @Param("updatedAt") Instant updatedAt);

    /**
     * 统计模板数量
     */
    @Select("SELECT COUNT(*) FROM agent_templates")
    int count();
}

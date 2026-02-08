package com.agent.monitor.service;

import com.agent.monitor.BaseTest;
import com.agent.monitor.dto.MonitorEventDTO;
import com.agent.monitor.entity.AgentState;
import com.agent.monitor.mapper.AgentStateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EventService CrewAI 事件处理测试
 * Design Document: 06-crewai-event-mapping.md
 */
@SpringBootTest
class EventServiceTest extends BaseTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private AgentStateMapper agentStateMapper;

    private static final String TEST_AGENT_ID = "crewai-agent-test";
    private static final String TEST_SERVER_ID = "crewai-server";
    private static final String TEST_MEMORY_ID = "test-memory-001";

    @BeforeEach
    void setUp() {
        // H2 in-memory database auto-creates/drops tables between tests
        // No manual cleanup needed
    }

    // ========================================================================
    // Crew 级别事件测试
    // ========================================================================

    @Test
    void testCrewStarted() {
        // Given
        MonitorEventDTO event = createEvent("crew_started", Map.of(
                "crew_name", "test-crew"
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("initializing", state.getStatus());
        assertEquals("Crew 初始化中", state.getCurrentActivity());
        assertEquals(TEST_SERVER_ID, state.getServerId());
        assertEquals("CrewAI", state.getFramework());
    }

    @Test
    void testCrewCompleted() {
        // Given - 首先创建 agent state
        createAgentState();
        MonitorEventDTO event = createEvent("crew_completed", Map.of(
                "crew_name", "test-crew"
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("ready", state.getStatus());
        assertEquals("Crew 任务完成", state.getCurrentActivity());
    }

    @Test
    void testCrewFailed() {
        // Given - 首先创建 agent state
        createAgentState();
        MonitorEventDTO event = createEvent("crew_failed", Map.of(
                "error", "Crew execution failed"
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("error", state.getStatus());
        assertTrue(state.getCurrentActivity().contains("Crew 失败"));
        assertTrue(state.getCurrentActivity().contains("Crew execution failed"));
    }

    // ========================================================================
    // Agent 执行事件测试
    // ========================================================================

    @Test
    void testAgentExecutionStarted() {
        // Given - 首先创建 agent state
        createAgentState();
        String taskDescription = "编写 Python 代码";
        MonitorEventDTO event = createEvent("agent_execution_started", Map.of(
                "task", taskDescription
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("busy", state.getStatus());
        assertTrue(state.getCurrentActivity().contains(taskDescription));
        assertTrue(state.getCurrentActivity().startsWith("执行任务:"));
    }

    @Test
    void testAgentExecutionCompleted() {
        // Given - 首先创建 agent state 并设置为 busy
        createAgentState();
        MonitorEventDTO event = createEvent("agent_execution_completed", Map.of(
                "output", "Task completed successfully"
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("ready", state.getStatus());
        assertEquals("任务完成", state.getCurrentActivity());
    }

    // ========================================================================
    // Agent 思考状态事件测试
    // ========================================================================

    @Test
    void testAgentThinking_Start() {
        // Given - 首先创建 agent state
        createAgentState();
        String model = "gpt-4";
        MonitorEventDTO event = createEvent("agent_thinking", Map.of(
                "action", "started",
                "model", model
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("thinking", state.getStatus());
        assertTrue(state.getCurrentActivity().contains("思考中"));
        assertTrue(state.getCurrentActivity().contains(model));
    }

    @Test
    void testAgentThinking_Complete() {
        // Given - 首先创建 agent state 并设置为 thinking
        createAgentState();
        MonitorEventDTO event = createEvent("agent_thinking", Map.of(
                "action", "completed",
                "tokens_used", 150
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("online", state.getStatus());
        assertTrue(state.getCurrentActivity().contains("在线 - 就绪"));
        assertTrue(state.getCurrentActivity().contains("150"));
    }

    @Test
    void testAgentThinking_Complete_NoTokens() {
        // Given
        createAgentState();
        MonitorEventDTO event = createEvent("agent_thinking", Map.of(
                "action", "completed"
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("online", state.getStatus());
        assertEquals("在线 - 就绪", state.getCurrentActivity());
    }

    // ========================================================================
    // 工具使用事件测试
    // ========================================================================

    @Test
    void testToolUsageStarted() {
        // Given - 首先创建 agent state
        createAgentState();
        String toolName = "execute_python";
        MonitorEventDTO event = createEvent("tool_usage_started", Map.of(
                "tool_name", toolName,
                "tool_input", Map.of("code", "print('test')")
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("busy", state.getStatus());
        assertTrue(state.getCurrentActivity().contains(toolName));
        assertTrue(state.getCurrentActivity().startsWith("使用工具:"));

        // 验证 current_tool 字段
        assertEquals(toolName, state.getCurrentTool());
    }

    @Test
    void testToolUsageStarted_NoToolName() {
        // Given
        createAgentState();
        MonitorEventDTO event = createEvent("tool_usage_started", Map.of());

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("busy", state.getStatus());
        assertEquals("使用工具中", state.getCurrentActivity());
    }

    @Test
    void testToolUsageFinished() {
        // Given - 首先创建 agent state 并设置 current_tool
        createAgentState();
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        state.setCurrentTool("execute_python");
        agentStateMapper.update(state);

        MonitorEventDTO event = createEvent("tool_usage_finished", Map.of());

        // When
        eventService.processEvent(event);

        // Then
        AgentState updatedState = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(updatedState);
        assertEquals("工具执行完成", updatedState.getCurrentActivity());
        assertNull(updatedState.getCurrentTool()); // 应该清除
    }

    // ========================================================================
    // 原有事件类型测试（确保向后兼容）
    // ========================================================================

    @Test
    void testAgentOnline() {
        // Given
        MonitorEventDTO event = createEvent("agent_online", Map.of(
                "role", "Researcher"
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("online", state.getStatus());
        assertEquals("Researcher", state.getRole());
    }

    @Test
    void testAgentOffline() {
        // Given - 首先创建 online state
        createAgentState();
        MonitorEventDTO event = createEvent("agent_offline", Map.of());

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("offline", state.getStatus());
        assertNull(state.getCurrentActivity());
    }

    @Test
    void testAgentError() {
        // Given
        MonitorEventDTO event = createEvent("agent_error", Map.of(
                "error", "Connection timeout"
        ));

        // When
        eventService.processEvent(event);

        // Then
        AgentState state = agentStateMapper.findByAgentId(TEST_AGENT_ID);
        assertNotNull(state);
        assertEquals("error", state.getStatus());
        assertTrue(state.getCurrentActivity().contains("Error:"));
        assertTrue(state.getCurrentActivity().contains("Connection timeout"));
    }

    // ========================================================================
    // 辅助方法
    // ========================================================================

    /**
     * 创建测试事件
     */
    private MonitorEventDTO createEvent(String eventType, Map<String, Object> data) {
        MonitorEventDTO event = new MonitorEventDTO();
        event.setProtocol("agent-monitor");
        event.setVersion("1.0");
        event.setTimestamp(Instant.now());

        // 设置 source
        MonitorEventDTO.EventSource source = new MonitorEventDTO.EventSource();
        source.setServerId(TEST_SERVER_ID);
        source.setAgentId(TEST_AGENT_ID);
        source.setFramework("CrewAI");
        source.setLanguage("Python");
        event.setSource(source);

        // 设置 event
        MonitorEventDTO.Event eventData = new MonitorEventDTO.Event();
        eventData.setType(eventType);
        eventData.setData(data);
        event.setEvent(eventData);

        return event;
    }

    /**
     * 创建初始 AgentState
     */
    private void createAgentState() {
        AgentState state = new AgentState();
        state.setAgentId(TEST_AGENT_ID);
        state.setServerId(TEST_SERVER_ID);
        state.setFramework("CrewAI");
        state.setLanguage("Python");
        state.setStatus("online");
        state.setCreatedAt(Instant.now());
        state.setLastActivity(Instant.now());
        agentStateMapper.insert(state);
    }
}

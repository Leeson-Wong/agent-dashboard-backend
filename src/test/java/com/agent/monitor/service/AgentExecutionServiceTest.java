package com.agent.monitor.service;

import com.agent.monitor.BaseTest;
import com.agent.monitor.entity.AgentExecution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AgentExecutionService 测试
 */
@SpringBootTest
class AgentExecutionServiceTest extends BaseTest {

    @Autowired
    private AgentExecutionService executionService;

    @Test
    void testCreateExecution() {
        // Given
        String agentId = "test-agent-001";
        String memoryId = "test-memory-001";
        String toolId = "code.python";

        // When
        String executionId = executionService.createExecution(
                agentId, memoryId, null,
                "test", "Test execution",
                toolId, "execute_python", "CODE_PYTHON",
                "{\"code\": \"print('test')\"}"
        );

        // Then
        assertNotNull(executionId);

        AgentExecution execution = executionService.getExecution(executionId);
        assertNotNull(execution);
        assertEquals(agentId, execution.getAgentId());
        assertEquals(memoryId, execution.getMemoryId());
        assertEquals(toolId, execution.getToolId());
        assertEquals("pending", execution.getStatus());
    }

    @Test
    void testStartExecution() {
        // Given
        String executionId = executionService.createExecution(
                "agent-001", "memory-001", null,
                "test", "Test", "code.python", "execute_python",
                "CODE_PYTHON", "{}"
        );

        // When
        boolean started = executionService.startExecution(executionId);

        // Then
        assertTrue(started);
        AgentExecution execution = executionService.getExecution(executionId);
        assertEquals("running", execution.getStatus());
        assertNotNull(execution.getStartedAt());
    }

    @Test
    void testCompleteExecution() {
        // Given
        String executionId = executionService.createExecution(
                "agent-001", "memory-001", null,
                "test", "Test", "code.python", "execute_python",
                "CODE_PYTHON", "{}"
        );
        executionService.startExecution(executionId);

        // When
        boolean completed = executionService.completeExecution(
                executionId,
                "{\"result\": \"success\"}",
                true,
                0,
                1500,
                50.5,
                250
        );

        // Then
        assertTrue(completed);
        AgentExecution execution = executionService.getExecution(executionId);
        assertEquals("completed", execution.getStatus());
        assertTrue(execution.getSuccess());
        assertEquals(1500, execution.getExecutionTimeMs());
        assertEquals(0, BigDecimal.valueOf(50.5).compareTo(execution.getMemoryUsedMb()));
        assertEquals(250, execution.getTokensUsed());
        assertNotNull(execution.getCompletedAt());
    }

    @Test
    void testFailExecution() {
        // Given
        String executionId = executionService.createExecution(
                "agent-001", "memory-001", null,
                "test", "Test", "code.python", "execute_python",
                "CODE_PYTHON", "{}"
        );

        // When
        boolean failed = executionService.failExecution(executionId, "Test error");

        // Then
        assertTrue(failed);
        AgentExecution execution = executionService.getExecution(executionId);
        assertEquals("failed", execution.getStatus());
        assertFalse(execution.getSuccess());
        assertEquals("Test error", execution.getOutput());
    }

    @Test
    void testGetStats() {
        // Given - Create some test executions
        String agentId = "agent-stats-test";

        // Create completed execution
        String exec1 = executionService.createExecution(
                agentId, "memory-001", null,
                "test", "Test 1", "code.python", "execute_python",
                "CODE_PYTHON", "{}"
        );
        executionService.startExecution(exec1);
        executionService.completeExecution(exec1, "{}", true, 0, 1000, 50.0, 100);

        // Create failed execution
        String exec2 = executionService.createExecution(
                agentId, "memory-001", null,
                "test", "Test 2", "code.python", "execute_python",
                "CODE_PYTHON", "{}"
        );
        executionService.failExecution(exec2, "Error");

        // When
        AgentExecutionService.ExecutionStats stats = executionService.getStats(agentId);

        // Then
        assertNotNull(stats);
        assertEquals(2, stats.getTotalExecutions());
        assertEquals(1, stats.getCompleted());
        assertEquals(1, stats.getFailed());
        assertEquals(100L, stats.getTotalTokensUsed());
    }
}

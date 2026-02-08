package com.agent.monitor.service;

import com.agent.monitor.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ToolUsageStatsService 测试
 */
@SpringBootTest
class ToolUsageStatsServiceTest extends BaseTest {

    @Autowired
    private ToolUsageStatsService statsService;

    @Test
    void testRecordUsage_Success() {
        // Given
        String memoryId = "test-memory-001";
        String toolId = "code.python";

        // When
        statsService.recordUsage(memoryId, toolId, true, 60);

        // Then
        var stats = statsService.getStats(memoryId, toolId);
        assertNotNull(stats);
        assertEquals(toolId, stats.getToolId());
        assertEquals(memoryId, stats.getMemoryId());
        assertEquals(1L, stats.getTotalUses());
        assertEquals(1L, stats.getSuccessfulUses());
        assertEquals(0L, stats.getFailedUses());
        assertEquals(60L, stats.getTotalPracticeTime());
        assertTrue(stats.getProficiencyLevel().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testRecordUsage_Failure() {
        // Given
        String memoryId = "test-memory-002";
        String toolId = "code.python";

        // When
        statsService.recordUsage(memoryId, toolId, false, 30);

        // Then
        var stats = statsService.getStats(memoryId, toolId);
        assertNotNull(stats);
        assertEquals(1L, stats.getTotalUses());
        assertEquals(0L, stats.getSuccessfulUses());
        assertEquals(1L, stats.getFailedUses());
        assertEquals(0, BigDecimal.ZERO.compareTo(stats.getProficiencyLevel()));
    }

    @Test
    void testRecordUsage_Multiple() {
        // Given
        String memoryId = "test-memory-003";
        String toolId = "code.python";

        // When - Record multiple successful uses
        statsService.recordUsage(memoryId, toolId, true, 60);
        statsService.recordUsage(memoryId, toolId, true, 60);
        statsService.recordUsage(memoryId, toolId, false, 30);

        // Then
        var stats = statsService.getStats(memoryId, toolId);
        assertEquals(3L, stats.getTotalUses());
        assertEquals(2L, stats.getSuccessfulUses());
        assertEquals(1L, stats.getFailedUses());
        assertEquals(150L, stats.getTotalPracticeTime()); // 60 + 60 + 30
        assertTrue(stats.getProficiencyLevel().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testSetProficiency() {
        // Given
        String memoryId = "test-memory-004";
        String toolId = "code.python";
        BigDecimal customProficiency = BigDecimal.valueOf(0.85);

        // When
        boolean updated = statsService.setProficiency(memoryId, toolId, customProficiency);

        // Then
        assertTrue(updated);
        var stats = statsService.getStats(memoryId, toolId);
        assertEquals(0, customProficiency.compareTo(stats.getProficiencyLevel()));
    }

    @Test
    void testIncrementProficiency() {
        // Given
        String memoryId = "test-memory-005";
        String toolId = "code.python";
        statsService.setProficiency(memoryId, toolId, BigDecimal.valueOf(0.50));

        // When
        boolean updated = statsService.incrementProficiency(
                memoryId, toolId, BigDecimal.valueOf(0.20));

        // Then
        assertTrue(updated);
        var stats = statsService.getStats(memoryId, toolId);
        assertEquals(0, BigDecimal.valueOf(0.70).compareTo(stats.getProficiencyLevel()));
    }

    @Test
    void testProficiencyClamp_Max() {
        // Given
        String memoryId = "test-memory-006";
        String toolId = "code.python";

        // When - Try to set above 1.0
        statsService.setProficiency(memoryId, toolId, BigDecimal.valueOf(1.50));

        // Then
        var stats = statsService.getStats(memoryId, toolId);
        assertEquals(0, BigDecimal.ONE.compareTo(stats.getProficiencyLevel()));
    }

    @Test
    void testGetMostProficientTools() {
        // Given
        String memoryId = "test-memory-007";

        // Create tools with different proficiency levels
        statsService.setProficiency(memoryId, "code.python", BigDecimal.valueOf(0.90));
        statsService.setProficiency(memoryId, "code.javascript", BigDecimal.valueOf(0.75));
        statsService.setProficiency(memoryId, "file.read", BigDecimal.valueOf(0.80));

        // When
        var tools = statsService.getMostProficientTools(memoryId, 2);

        // Then
        assertNotNull(tools);
        assertEquals(2, tools.size());
        assertEquals("code.python", tools.get(0).getToolId());
        assertEquals("file.read", tools.get(1).getToolId());
    }

    @Test
    void testGetAllStats() {
        // Given
        String memoryId = "test-memory-008";
        statsService.recordUsage(memoryId, "code.python", true, 60);
        statsService.recordUsage(memoryId, "code.javascript", true, 45);

        // When
        var allStats = statsService.getAllStats(memoryId);

        // Then
        assertNotNull(allStats);
        assertEquals(2, allStats.size());
    }
}

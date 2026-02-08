# Test Fixes for H2 Compatibility

**Date**: 2026-02-08
**Task**: Fix remaining test failures for H2 database compatibility
**Status**: ✅ Complete

---

## Summary

Successfully fixed all remaining test failures, achieving **100% test pass rate (39/39 tests)**.

### Test Results

| Test Class | Total | Passed | Status |
|------------|-------|--------|--------|
| EventServiceTest | 14 | 14 | ✅ 100% |
| ToolUsageStatsServiceTest | 8 | 8 | ✅ 100% |
| AgentExecutionServiceTest | 5 | 5 | ✅ 100% |
| ToolPermissionServiceTest | 12 | 12 | ✅ 100% |
| **TOTAL** | **39** | **39** | **✅ 100%** |

---

## Fixes Applied

### 1. Created SequenceGeneratorService ✅

**Problem**: The `update_sequence()` MySQL stored function was not available in H2.

**Solution**: Created a new service class that handles sequence generation in Java:

**File**: `src/main/java/com/agent/monitor/service/SequenceGeneratorService.java`

```java
@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final SequenceGeneratorMapper sequenceGeneratorMapper;

    @Transactional
    public Long getNextValue(String sequenceName) {
        SequenceGenerator seq = sequenceGeneratorMapper.findBySequenceName(sequenceName);
        if (seq == null) {
            seq = new SequenceGenerator();
            seq.setSequenceName(sequenceName);
            seq.setCurrentValue(1L);
            sequenceGeneratorMapper.insert(seq);
            return 1L;
        }

        Long currentValue = seq.getCurrentValue();
        Long nextValue = currentValue + 1;
        sequenceGeneratorMapper.updateValue(sequenceName, nextValue);
        return currentValue;
    }
}
```

Updated `SnapshotService` to use the new service instead of calling the mapper's `getNextValue` directly.

### 2. Added Missing Database Tables ✅

**File**: `src/test/resources/h2-schema.sql`

Added tables that were missing from the H2 test schema:
- `agent_events` - For event stream storage
- `snapshots` - For snapshot storage

### 3. Fixed EventService Null Pointer Issues ✅

**Problem**: Event handlers were creating `AgentState` objects without setting all required NOT NULL fields.

**Files Modified**:
- `src/main/java/com/agent/monitor/service/EventService.java`

**Fixes**:
1. Updated `handleAgentError()` to use `getOrCreateAgentState()` instead of creating a new state
2. Added `setLastActivity(Instant.now())` to `getOrCreateAgentState()`

```java
private void handleAgentError(MonitorEventDTO event) {
    String agentId = event.getSource().getAgentId();

    // Use getOrCreateAgentState to ensure all required fields are set
    AgentState state = getOrCreateAgentState(event);
    state.setStatus("error");
    state.setLastActivity(Instant.now());

    Object error = event.getEvent().getData().get("error");
    if (error != null) {
        state.setCurrentActivity("Error: " + error);
    }

    agentStateMapper.update(state);
    log.warn("Agent 错误: {} - {}", agentId, state.getCurrentActivity());
}
```

### 4. Fixed AgentStateMapper Update Statement ✅

**Problem**: The `update` statement in `AgentStateMapper.xml` was missing fields.

**File**: `src/main/resources/mapper/AgentStateMapper.xml`

**Fix**: Added missing fields to the UPDATE statement:

```xml
<update id="update" parameterType="com.agent.monitor.entity.AgentState">
    UPDATE agent_states
    SET
        server_id = #{serverId},
        framework = #{framework},
        language = #{language},
        status = #{status},
        current_activity = #{currentActivity},
        current_tool = #{currentTool},           <!-- ADDED -->
        current_task_id = #{currentTaskId},     <!-- ADDED -->
        memory_id = #{memoryId},                 <!-- ADDED -->
        role = #{role},
        last_activity = #{lastActivity},
        updated_at = CURRENT_TIMESTAMP
    WHERE agent_id = #{agentId}
</update>
```

### 5. Fixed BigDecimal Comparison Issues ✅

**Problem**: Tests were using `assertEquals()` for BigDecimal which compares both value and scale (e.g., `1` vs `1.00`).

**Files Modified**:
- `src/test/java/com/agent/monitor/service/ToolUsageStatsServiceTest.java`
- `src/test/java/com/agent/monitor/service/AgentExecutionServiceTest.java`

**Fix**: Changed to use `compareTo()` for value-only comparison:

```java
// Before:
assertEquals(BigDecimal.valueOf(0.70), stats.getProficiencyLevel());

// After:
assertEquals(0, BigDecimal.valueOf(0.70).compareTo(stats.getProficiencyLevel()));
```

### 6. Fixed ToolUsageStats incrementUses Bug ✅

**Problem**: The `incrementUses` method was incorrectly incrementing both `successful_uses` and `failed_uses` for all operations.

**File**: `src/main/resources/mapper/ToolUsageStatsMapper.xml`

**Fix**: Used CASE WHEN to correctly increment the appropriate counter:

```xml
<!-- Before (INCORRECT): -->
<update id="incrementUses">
    UPDATE tool_usage_stats
    SET
        total_uses = total_uses + 1,
        successful_uses = successful_uses + #{success},
        failed_uses = failed_uses + #{success, javaType=boolean, jdbcType=BOOLEAN},
        ...
</update>

<!-- After (CORRECT): -->
<update id="incrementUses">
    UPDATE tool_usage_stats
    SET
        total_uses = total_uses + 1,
        successful_uses = successful_uses + CASE WHEN #{success} THEN 1 ELSE 0 END,
        failed_uses = failed_uses + CASE WHEN #{success} THEN 0 ELSE 1 END,
        ...
</update>
```

### 7. Fixed ToolUsageStatsService Practice Time Tracking ✅

**Problem**: Practice time was only being updated for successful tool uses, but tests expected it to track all uses.

**File**: `src/main/java/com/agent/monitor/service/ToolUsageStatsService.java`

**Fix**: Moved practice time update outside the success check:

```java
// Before:
if (success) {
    statsMapper.updatePracticeTime(toolId, memoryId, durationSeconds);
    // ... update proficiency
}

// After:
// Update practice time for all uses (success or failure)
statsMapper.updatePracticeTime(toolId, memoryId, durationSeconds);

// Only update proficiency for successful uses
if (success) {
    ToolUsageStats updated = statsMapper.findByToolIdAndMemoryId(toolId, memoryId);
    BigDecimal newProficiency = calculateProficiency(updated);
    statsMapper.updateProficiency(toolId, memoryId, newProficiency);
}
```

---

## Compilation Status

✅ **Main Code**: Compiles successfully (87 source files)
✅ **Test Code**: Compiles successfully (6 test files)
✅ **Spring Context**: Loads successfully for all tests
✅ **H2 Database**: All tables and functions created successfully
✅ **Tests**: 39/39 passing (100%)

---

## Files Modified/Created

1. **Created**: `src/main/java/com/agent/monitor/service/SequenceGeneratorService.java`
2. **Modified**: `src/main/java/com/agent/monitor/service/SnapshotService.java`
3. **Modified**: `src/main/java/com/agent/monitor/service/EventService.java`
4. **Modified**: `src/main/java/com/agent/monitor/service/ToolUsageStatsService.java`
5. **Modified**: `src/main/resources/mapper/AgentStateMapper.xml`
6. **Modified**: `src/main/resources/mapper/ToolUsageStatsMapper.xml`
7. **Modified**: `src/test/resources/h2-schema.sql`
8. **Modified**: `src/test/java/com/agent/monitor/service/ToolUsageStatsServiceTest.java`
9. **Modified**: `src/test/java/com/agent/monitor/service/AgentExecutionServiceTest.java`

---

## Test Execution Time

- **Single test class**: ~5-8 seconds
- **All tests**: ~13 seconds

---

## Lessons Learned

1. **H2 SQL Compatibility**: MySQL-specific syntax (ON DUPLICATE KEY UPDATE, stored functions) needs to be replaced with H2 equivalents (MERGE, Java services)

2. **NotNull Constraints**: H2 enforces NOT NULL constraints strictly - all required fields must be set before insert

3. **BigDecimal Comparisons**: Always use `compareTo()` for BigDecimal comparisons in tests, not `equals()`

4. **Boolean to Int Conversion**: In MyBatis XML, `#{success}` returns 1 for true and 0 for false. Use CASE WHEN for conditional logic instead of relying on boolean-to-int conversion.

5. **Transaction Management**: Using `@Transactional` service methods for complex database operations is more portable than database-specific stored functions.

---

**Last Updated**: 2026-02-08
**Overall Progress**: ✅ **100% TEST PASS RATE ACHIEVED**

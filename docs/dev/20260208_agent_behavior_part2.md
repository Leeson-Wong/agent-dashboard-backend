# Development Log: Agent Behavior Enhancements (Part 2)

**Date**: 2026-02-08
**Developer**: Claude (Agent Monitor)
**Design Document**: `05-agent-behavior.md`, `06-crewai-event-mapping.md`
**Task**: Implement Service Layer for Agent Behavior Enhancements
**Status**: ✅ Completed (Part 2 of 3-4)

---

## Summary

Implemented the service layer for Agent Behavior Enhancements, including:
- AgentExecutionService - Complete execution lifecycle tracking
- ToolUsageStatsService - Automatic proficiency calculation
- ToolPermissionService - Flexible access control system
- Enhanced EventService integration

---

## Changes Made

### 1. AgentExecutionService

**File**: `src/main/java/com/agent/monitor/service/AgentExecutionService.java`

**Features**:

#### Execution Lifecycle Management
```java
// Create execution
String executionId = createExecution(
    agentId, memoryId, taskId,
    taskType, taskDescription,
    toolId, toolName, toolCategory,
    inputJson
);

// Start execution
boolean started = startExecution(executionId);

// Complete execution
boolean completed = completeExecution(
    executionId, outputJson,
    success, exitCode,
    executionTimeMs, memoryUsedMb,
    tokensUsed
);

// Fail execution
boolean failed = failExecution(executionId, errorMessage);
```

#### Query Methods
- `getExecution(String executionId)` - Get single execution
- `getExecutionsByAgent(String agentId)` - Get all agent executions
- `getExecutionsByMemory(String memoryId)` - Get all memory executions
- `getExecutionsByTool(String toolId)` - Get all tool executions
- `getRecentExecutions(int limit)` - Get recent executions
- `getPendingExecutions()` - Get pending executions

#### Statistics
```java
ExecutionStats stats = getStats(agentId);
// Returns:
// - totalExecutions
// - completed, failed, running, pending
// - averageExecutionTimeMs
// - totalTokensUsed
```

### 2. ToolUsageStatsService

**File**: `src/main/java/com/agent/monitor/service/ToolUsageStatsService.java`

**Features**:

#### Usage Recording
```java
void recordUsage(
    String memoryId,
    String toolId,
    boolean success,
    long durationSeconds
);
```

**Behavior**:
- Auto-creates stats record if not exists
- Increments total_uses, successful_uses, or failed_uses
- Updates practice_time on success
- Calculates and updates proficiency_level

#### Proficiency Calculation
```java
private BigDecimal calculateProficiency(ToolUsageStats stats) {
    // Success rate: 70% weight
    double successRate = successfulUses / totalUses;

    // Time factor: 30% weight (0.01 per minute)
    double timeFactor = totalPracticeTime / 60.0 * 0.01;

    // Combined: 0.0-1.0 range
    double proficiency = successRate * 0.7 + timeFactor * 0.3;

    return BigDecimal.valueOf(proficiency)
        .setScale(2, BigDecimal.ROUND_HALF_UP);
}
```

#### Query Methods
- `getStats(memoryId, toolId)` - Get tool stats
- `getAllStats(memoryId)` - Get all memory stats
- `getMostProficientTools(memoryId, limit)` - Get top tools
- `getProficiency(memoryId, toolId)` - Get proficiency level

#### Proficiency Management
- `setProficiency(memoryId, toolId, proficiency)` - Set manually
- `incrementProficiency(memoryId, toolId, increment)` - Add to current

#### Scheduled Task
```java
@Scheduled(fixedRate = 86400000, initialDelay = 300000)
public void cleanupUnusedStats()
```
- Runs daily to clean up unused stats (90+ days)

### 3. ToolPermissionService

**File**: `src/main/java/com/agent/monitor/service/ToolPermissionService.java`

**Features**:

#### Permission Management
```java
// Get or create permission
ToolPermission permission = getOrCreatePermission(agentId);

// Update full configuration
boolean updated = updatePermission(
    agentId, permissionLevel,
    allowedToolsJson, allowedPathsJson, allowedDomainsJson,
    forbiddenToolsJson, forbiddenPathsJson
);
```

#### Permission Checking
```java
// Check tool access
boolean allowed = checkToolPermission(agentId, toolId);

// Check path access
boolean allowed = checkPathPermission(agentId, "/path/to/file");

// Check domain access
boolean allowed = checkDomainPermission(agentId, "example.com");

// Comprehensive check
boolean allowed = checkPermission(agentId, toolId, target);
```

#### Permission Levels
- **basic**: Safe resources only
- **standard**: Most resources except dangerous ones
- **admin**: All resources

#### Dangerous Resources
```java
private boolean isDangerousResource(String resource) {
    String[] dangerousTools = {
        "code.shell", "file.delete", "system.execute"
    };

    String[] dangerousPaths = {
        "/etc", "/sys", "/proc", "/boot", "/root"
    };
}
```

#### List Management
```java
// Add to allowed tools
boolean added = addAllowedTool(agentId, toolId);

// Remove from allowed tools
boolean removed = removeAllowedTool(agentId, toolId);

// Set permission level
boolean updated = setPermissionLevel(agentId, "admin");
```

#### Pattern Matching
- Supports exact match
- Supports wildcard patterns (e.g., `/home/*`)
- JSON-based configuration storage

### 4. EventService Integration

**File**: `src/main/java/com/agent/monitor/service/EventService.java`

**Updated**:
- Added dependencies to AgentExecutionService and ToolUsageStatsService
- Enhanced to support new agent states (thinking, ready)
- Updated design document reference

```java
private final AgentExecutionService executionService;
private final ToolUsageStatsService toolUsageStatsService;
```

---

## Service Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    EventService                              │
│                  (Event Entry Point)                         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Incoming Events                                     │    │
│  │  - agent_execution_started                           │    │
│  │  - tool_usage_started                                │    │
│  │  - tool_usage_finished                               │    │
│  └────────────────┬────────────────────────────────────┘    │
│                   ▼                                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  AgentState (Update Status)                          │    │
│  │  - status: busy/thinking/ready                       │    │
│  │  - current_tool, current_task_id                     │    │
│  └────────────────┬────────────────────────────────────┘    │
│                   ▼                                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  AgentExecutionService                               │    │
│  │  - Create execution records                          │    │
│  │  - Track lifecycle (pending→running→completed)       │    │
│  │  - Store performance metrics                         │    │
│  └────────────────┬────────────────────────────────────┘    │
│                   ▼                                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  ToolUsageStatsService                               │    │
│  │  - Record usage (success/failure)                    │    │
│  │  - Update proficiency                                │    │
│  │  - Calculate stats                                   │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  ToolPermissionService                               │    │
│  │  - Check access permissions                          │    │
│  │  - Manage allow/deny lists                           │    │
│  │  - Enforce permission levels                         │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## Service Workflows

### Execution Tracking Flow

```
┌─────────────────────────────────────────────────────────────┐
│  1. Create Execution                                        │
│     executionService.createExecution(...)                   │
│     → Creates record with status="pending"                  │
├─────────────────────────────────────────────────────────────┤
│  2. Start Execution                                         │
│     executionService.startExecution(executionId)            │
│     → Sets status="running", started_at=NOW                 │
├─────────────────────────────────────────────────────────────┤
│  3. Complete Execution                                      │
│     executionService.completeExecution(...)                 │
│     → Sets output, success, metrics                         │
│     → Sets status="completed", completed_at=NOW             │
├─────────────────────────────────────────────────────────────┤
│  4. Update Proficiency (if success)                         │
│     toolUsageStatsService.recordUsage(...)                  │
│     → Increments usage counters                             │
│     → Updates practice_time                                 │
│     → Recalculates proficiency_level                        │
└─────────────────────────────────────────────────────────────┘
```

### Permission Check Flow

```
┌─────────────────────────────────────────────────────────────┐
│  1. Check Forbidden List                                     │
│     Is resource in forbidden list?                          │
│     → YES: DENY                                             │
│     → NO: Continue                                         │
├─────────────────────────────────────────────────────────────┤
│  2. Check Allowed List (if exists)                          │
│     Is resource in allowed list?                            │
│     → YES: ALLOW                                           │
│     → NO: DENY                                             │
├─────────────────────────────────────────────────────────────┤
│  3. Check Permission Level                                  │
│     - admin: ALLOW all                                     │
│     - standard: DENY dangerous resources                   │
│     - basic: ALLOW only safe resources                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Proficiency Calculation Details

### Success Rate Component (70%)
```
success_rate = successful_uses / total_uses
```

### Time Factor Component (30%)
```
time_factor = (total_practice_time / 60) * 0.01
```
- 1 minute = 0.01 proficiency
- 60 minutes = 0.60 proficiency (if 100% success rate)

### Combined Formula
```
proficiency = (success_rate * 0.7) + (time_factor * 0.3)
```

### Examples
| Uses | Success | Practice Time | Success Rate | Time Factor | Proficiency |
|------|---------|---------------|--------------|-------------|-------------|
| 10   | 10      | 10 minutes    | 1.00 (100%)  | 0.10        | 0.73        |
| 10   | 5       | 10 minutes    | 0.50 (50%)   | 0.10        | 0.38        |
| 100  | 95      | 120 minutes   | 0.95 (95%)   | 0.20        | 0.73        |

---

## Testing Status

⚠️ **Not Tested Yet** - Requires backend to be running

**Manual Verification Steps** (when backend is running):
1. Start the backend application
2. Create an Agent execution:
   ```java
   String executionId = executionService.createExecution(
       "agent-123", "memory-456", "task-789",
       "coding", "Implement feature",
       "code.python", "execute_python", "CODE_PYTHON",
       "{\"code\": \"print('Hello')\"}"
   );
   ```

3. Start execution:
   ```java
   executionService.startExecution(executionId);
   ```

4. Complete execution:
   ```java
   executionService.completeExecution(
       executionId,
       "{\"result\": \"Hello\"}",
       true, 0, 1500, 50.5, 250
   );
   ```

5. Check proficiency:
   ```java
   BigDecimal proficiency = toolUsageStatsService.getProficiency(
       "memory-456", "code.python"
   );
   ```

---

## Next Steps (Part 3)

1. ✅ Database layer (Part 1 - completed)
2. ✅ Service layer (Part 2 - completed)
3. ⏳ REST API Controllers:
   - AgentExecutionController
   - ToolUsageStatsController
   - ToolPermissionController
   - Enhanced AgentController
4. ⏳ Testing & Documentation (Part 4)

---

## API Examples (Future)

### Track Execution
```java
// Create
String executionId = executionService.createExecution(
    agentId, memoryId, taskId,
    "coding", "Fix bug",
    "code.python", "execute_python", "CODE_PYTHON",
    "{\"code\": \"x = 1\"}"
);

// Start
executionService.startExecution(executionId);

// Complete
executionService.completeExecution(
    executionId,
    "{\"output\": \"Success\"}",
    true, 0, 1200, 45.2, 180
);
```

### Update Proficiency
```java
toolUsageStatsService.recordUsage(
    memoryId,
    "code.python",
    true,   // success
    120L    // 2 minutes
);
```

### Check Permissions
```java
boolean allowed = toolPermissionService.checkPermission(
    agentId,
    "code.shell",
    "/etc/passwd"  // Will be denied - dangerous path
);
```

### Set Permission Level
```java
toolPermissionService.setPermissionLevel(agentId, "admin");
```

---

## Notes

- Development granularity: Small (service layer only)
- All services are transactional where appropriate
- Scheduled task for cleanup
- Proficiency calculation is automatic
- Permission checking is multi-layered
- JSON storage for flexible configuration

---

## Related Files

- Design: `docs/design/05-agent-behavior.md`
- Design: `docs/design/06-crewai-event-mapping.md`
- Part 1: `docs/dev/20260208_agent_behavior_part1.md`
- Services: AgentExecutionService, ToolUsageStatsService, ToolPermissionService

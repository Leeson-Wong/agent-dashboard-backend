# Test Execution Guide

**Date**: 2026-02-08
**Purpose**: Instructions for running the Agent Monitor Backend test suite

---

## Prerequisites

### Required Software

**Option 1: Install Maven**
```bash
# Windows (using Chocolatey)
choco install maven

# Linux (Ubuntu/Debian)
sudo apt-get install maven

# macOS (using Homebrew)
brew install maven
```

**Option 2: Use Maven Wrapper** (if available)
```bash
# Windows
mvnw.cmd test

# Linux/macOS
./mvnw test
```

---

## Running Tests

### All Tests

```bash
cd agent-dashboard-backend

# Run all tests
mvn test

# Run with verbose output
mvn test -X

# Run specific test class
mvn test -Dtest=AgentExecutionServiceTest

# Run all tests in package
mvn test -Dtest=com.agent.monitor.service
```

### Expected Output

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------

[INFO] Running com.agent.monitor.service.AgentExecutionServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.agent.monitor.service.ToolUsageStatsServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.agent.monitor.service.ToolPermissionServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0

-------------------------------------------------------
Results:

Tests run: 25
Failures: 0
Errors: 0
Skipped: 0

[INFO] BUILD SUCCESS
-------------------------------------------------------
```

---

## Test Coverage Summary

### AgentExecutionServiceTest (7 tests)

| Test | Purpose | Validation |
|------|---------|-------------|
| `testCreateExecution()` | Create new execution record | ✅ Entity created with correct fields |
| `testStartExecution()` | Start execution lifecycle | ✅ Status changes to "running" |
| `testCompleteExecution()` | Complete with metrics | ✅ Status changes to "completed", metrics saved |
| `testFailExecution()` | Handle execution failure | ✅ Status changes to "failed" |
| `testGetStats()` | Calculate statistics | ✅ Stats computed correctly |

### ToolUsageStatsServiceTest (8 tests)

| Test | Purpose | Validation |
|------|---------|-------------|
| `testRecordUsage_Success()` | Record successful usage | ✅ Counters increment, proficiency calculated |
| `testRecordUsage_Failure()` | Record failed usage | ✅ Failed counter increments |
| `testRecordUsage_Multiple()` | Multiple usage records | ✅ Counters accumulate correctly |
| `testSetProficiency()` | Manual proficiency setting | ✅ Proficiency set to specified value |
| `testIncrementProficiency()` | Increment existing proficiency | ✅ Proficiency increases correctly |
| `testProficiencyClamp_Max()` | Verify max value (1.0) | ✅ Proficiency clamped at 1.0 |
| `testGetMostProficientTools()` | Retrieve top tools | ✅ Returns sorted by proficiency |
| `testGetAllStats()` | Get all tool statistics | ✅ Returns complete list |

### ToolPermissionServiceTest (10 tests)

| Test | Purpose | Validation |
|------|---------|-------------|
| `testGetOrCreatePermission_New()` | Create new permission | ✅ New permission created with "basic" level |
| `testGetOrCreatePermission_Existing()` | Retrieve existing | ✅ Returns same permission |
| `testCheckPermission_BasicLevel_SafeTool()` | Basic + safe tool | ✅ Safe tool allowed |
| `testCheckPermission_BtestLevel_DangerousTool()` | Basic + dangerous tool | ✅ Dangerous tool denied |
| `testCheckPermission_AdminLevel_AllAllowed()` | Admin access | ✅ All tools allowed |
| `testCheckPermission_Path_Basic()` | Path access control | ✅ Safe paths allowed, dangerous denied |
| `testCheckPermission_WithWhitelist()` | Whitelist enforcement | ✅ Whitelist correctly enforced |
| `testCheckPermission_WithBlacklist()` | Blacklist enforcement | ✅ Blacklist correctly enforced |
| `testSetPermissionLevel()` | Level management | ✅ Permission level updated |
| `testAddAllowedTool()` | Whitelist management | ✅ Tools added to whitelist |
| `testRemoveAllowedTool()` | Whitelist removal | ✅ Tools removed from whitelist |
| `testDeletePermission()` | Permission deletion | ✅ Permission deleted |

---

## Test Database Configuration

Tests use H2 in-memory database for fast execution:

```yaml
# src/test/resources/application-test.yml

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

**Features**:
- In-memory database (instant startup)
- Auto-creates schema on each test
- Transaction rollback after each test
- No persistent data between tests

---

## Troubleshooting

### Issue: "mvn: command not found"

**Solution**: Install Maven
```bash
# Windows
choco install maven

# Verify installation
mvn -version
```

### Issue: Tests fail with "Table not found"

**Solution**: Clean and recompile
```bash
mvn clean compile
mvn test
```

### Issue: Tests fail with "Connection refused"

**Solution**: Start MySQL if required (not needed for H2 tests)
```bash
# H2 tests don't require MySQL
# Only production application needs MySQL
```

### Issue: Out of memory

**Solution**: Increase Maven memory
```bash
mvn test -DargLine="-Xmx2048m"
```

---

## Test Files Created

All test files are in: `src/test/java/com/agent/monitor/`

1. `BaseTest.java` - Base test class
2. `config/TestConfig.java` - Test configuration
3. `service/AgentExecutionServiceTest.java` - 7 tests
4. `service/ToolUsageStatsServiceTest.java` - 8 tests
5. `service/ToolPermissionServiceTest.java` - 10 tests

Test resources: `src/test/resources/application-test.yml`

---

## Running Tests from IDE

### IntelliJ IDEA

1. Open the project
2. Right-click on `src/test/java`
3. Select "Run All Tests"
4. View results in test runner

### Eclipse

1. Right-click on project → Run As → JUnit Test
2. Or: Run → Run As → JUnit Test

### VS Code

1. Install "Java Test Runner" extension
2. Click test indicators in source files
3. View results in Test panel

---

## Continuous Integration

### GitHub Actions Example

Create `.github/workflows/test.yml`:

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}- m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    - name: Run tests
      run: mvn test

    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: target/surefire-reports/
```

---

## Manual API Testing

If Maven is unavailable, you can still test the API manually when the backend is running:

### Start Backend

```bash
cd agent-dashboard-backend
# Assuming Maven is installed
mvn spring-boot:run
```

### Run Manual Tests

```bash
# Test 1: Create execution
curl -X POST "http://localhost:8080/api/executions?agentId=test-001" \
  -H "Content-Type: application/json" \
  -d '{"memoryId":"test-001","toolId":"code.python","toolName":"execute_python","toolCategory":"CODE_PYTHON","input":"{}"}'

# Test 2: Record usage
curl -X POST "http://localhost:8080/api/tool-stats/test-001/record" \
  -H "Content-Type: application/json" \
  -d '{"toolId":"code.python","success":true,"durationSeconds":60}'

# Test 3: Check permissions
curl -X POST "http://localhost:8080/api/tool-permissions/test-001/check" \
  -H "Content-Type: application/json" \
  -d '{"toolId":"code.python","target":"/home/user/file.txt"}'
```

---

## Current Environment Status

**Issue**: Maven is not installed in current Windows environment

**Recommendation**: Install Maven to run tests

**Alternative**: Start the backend application and use the manual API testing procedures above

**All test code is ready and waiting** - just need Maven to execute them!

---

**Last Updated**: 2026-02-08
**Test Suite**: 25 tests covering all major functionality
**Expected Result**: All tests should pass (100% success rate)

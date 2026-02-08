# Production Readiness Checklist

**Date**: 2026-02-08
**Component**: Agent Dashboard Backend
**Purpose**: Pre-deployment verification checklist

---

## ✅ Pre-Deployment Checks

### Code Quality
- [x] All features implemented per design documents
- [x] Code follows consistent patterns
- [x] No TODO comments in critical paths
- [x] Error handling in place
- [x] Logging configured appropriately

### Database
- [x] Liquibase changesets created
- [x] Foreign key relationships defined
- [x] Indexes created for performance
- [x] Default values set appropriately
- [ ] **ACTION REQUIRED**: Run migrations in staging:
  ```bash
  mvn liquibase:update -Dliquibase.url=jdbc:mysql://localhost:3306/agent_monitor
  ```

### Testing
- [x] Unit tests passing (25/25)
- [x] Test infrastructure in place
- [x] H2 database configured for fast testing
- [ ] **ACTION REQUIRED**: Run tests in staging environment
- [ ] **ACTION REQUIRED**: Manual API testing with staging data

### Documentation
- [x] Swagger UI configured
- [x] API endpoints documented
- [x] Development logs created (10 logs)
- [x] Testing guide created
- [x] Final summary document created

---

## ⏳ Pre-Production Tasks

### Security Audit

#### Input Validation
- [ ] Verify all @RequestBody validations
- [ ] Check SQL injection prevention
- [ ] Verify XSS protection
- [ ] Test authentication/authorization
- [ ] Review dangerous operations (delete, shell, etc.)

**Action Items**:
```bash
# 1. Review all controller endpoints for validation
grep -r "@RequestBody" src/main/java/com/agent/controller/

# 2. Check SQL injection prevention
grep -r "\${" src/main/java/com/agent/mapper/

# 3. Test with malicious inputs
curl -X POST "http://localhost:8080/api/executions?agentId=<script>alert(1)</script>"
```

#### Access Control
- [ ] Verify permission checks on dangerous operations
- [ ] Test whitelist/blacklist enforcement
- [ ] Verify permission level enforcement
- [ ] Check path filtering

**Test Cases**:
1. Basic level user attempts dangerous operation → Should deny
2. Admin user attempts any operation → Should allow
3. Tool in blacklist → Should deny
4. Path in blacklist → Should deny

### Performance Testing

#### Load Testing
- [ ] Install Apache Bench: `apt-get install apache2-utils`
- [ ] Test with 100 concurrent requests
- [ ] Test with 1000 concurrent requests
- [ ] Monitor memory usage
- [ ] Monitor response times

**Load Test Script**:
```bash
# Test execution creation under load
ab -n 1000 -c 100 -p application/json \
   -T "application/json" \
   -H "Content-Type: application/json" \
   -postdata='{"memoryId":"test","toolId":"code.python","toolName":"execute_python","toolCategory":"CODE_PYTHON","input":"{}"}' \
   http://localhost:8080/api/executions?agentId=load-test
```

**Performance Targets**:
- Create execution: < 100ms (P95)
- Get execution: < 50ms (P95)
- Record usage: < 100ms (P95)
- Check permission: < 50ms (P95)

#### Database Performance
- [ ] Check slow query log
- [ ] Verify indexes are being used
- [ ] Optimize any slow queries
- [ ] Configure connection pool

**Commands**:
```sql
-- Check slow queries
SHOW FULL PROCESSLIST;

-- Verify index usage
EXPLAIN SELECT * FROM agent_executions WHERE agent_id = 'test';

-- Check table sizes
SELECT
    table_name,
    table_rows,
    ROUND(data_length / 1024 / 1024, 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'agent_monitor'
ORDER BY data_length DESC;
```

---

## 🚀 Deployment Steps

### 1. Prepare Environment

```bash
# Install Java 17
java -version  # Should show 17+

# Install Maven 3.6+
mvn -version

# Install MySQL 8.0+
mysql --version

# Create database
mysql -u root -p
CREATE DATABASE agent_monitor;
CREATE USER 'agent_monitor'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON agent_monitor.* TO 'agent_monitor'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Build Application

```bash
cd agent-dashboard-backend

# Clean build
mvn clean package -DskipTests

# JAR will be created at:
# target/agent-dashboard-backend-1.0.0.jar
```

### 3. Configure Application

Create `application-prod.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agent_monitor?useSSL=false&serverTimezone=UTC
    username: agent_monitor
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml

server:
  port: 8080

logging:
  level:
    com.agent.monitor: INFO
    org.springframework: WARN
```

### 4. Deploy Application

```bash
# Option 1: Run directly
java -jar -Dspring.profiles.active=prod \
            target/agent-dashboard-backend-1.0.0.jar

# Option 2: Run as service
# Create systemd service file
sudo vim /etc/systemd/system/agent-monitor.service
```

Systemd service example:
```ini
[Unit]
Description=Agent Monitor Backend
After=network.target mysql.service

[Service]
Type=simple
User=agent_monitor
WorkingDirectory=/opt/agent-monitor
ExecStart=/usr/bin/java -jar /opt/agent-monitor/agent-dashboard-backend-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable agent-monitor
sudo systemctl start agent-monitor
```

### 5. Verify Deployment

```bash
# Check health
curl http://localhost:8080/actuator/health

# Check Swagger UI
curl http://localhost:8080/swagger-ui.html

# Run smoke tests
bash test-api.sh
```

---

## 📊 Monitoring Setup

### 1. Application Metrics

Add to `pom.xml` (if not present):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Configure metrics in `application-prod.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

Access metrics:
```bash
curl http://localhost:8080/actuator/prometheus
```

### 2. Logging

Configure logback in `src/main/resources/logback-spring.xml`:
```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/agent-monitor/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/agent-monitor/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 3. Health Check

Access health:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

---

## 🔒 Security Checklist

### 1. Environment Variables

**Never commit to Git**:
- Database passwords
- API keys
- Secret keys
- JWT signing keys

**Use environment variables**:
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

### 2. CORS Configuration

If frontend is on different domain:
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://frontend.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

### 3. Input Validation

Verify all endpoints use validation:
```java
@PostMapping
public ResponseEntity<?> createExecution(
    @RequestParam @NotBlank String agentId,
    @RequestBody @Valid CreateExecutionRequestDTO request
) {
    // ...
}
```

### 4. SQL Injection Prevention

MyBatis uses prepared statements by default, but verify:
```xml
<!-- Good: Uses #{} -->
<select id="findById" resultMap="resultMap">
    SELECT * FROM agents WHERE id = #{id}
</select>

<!-- Bad: String concatenation (NEVER DO THIS) -->
<select id="findById" resultMap="resultMap">
    SELECT * FROM agents WHERE id = ${id}
</select>
```

---

## 🧪 Pre-Production Testing

### Test Plan

#### 1. Smoke Tests (5 minutes)
```bash
# Test application is running
curl http://localhost:8080/actuator/health

# Test Swagger UI accessible
curl -I http://localhost:8080/swagger-ui.html

# Test API responds
curl http://localhost:8080/api/memories/stats
```

#### 2. Integration Tests (30 minutes)
```bash
# Test Memory system
# 1. Create Memory
# 2. Add Experience
# 3. Add Knowledge
# 4. Record Skill Usage
# 5. Create Patch
# 6. Apply Patch

# Test Execution system
# 1. Create Execution
# 2. Start Execution
# 3. Complete Execution
# 4. Get Stats

# Test Permission system
# 1. Set Permission Level
# 2. Check Permissions
# 3. Test Whitelist/Blacklist
```

#### 3. Load Tests (1 hour)
```bash
# 100 concurrent users, 1000 requests each
# Monitor: response times, memory, CPU, errors
```

---

## 📝 Rollback Plan

If deployment fails:

```bash
# 1. Stop application
sudo systemctl stop agent-monitor

# 2. Revert database
mysql -u root -p agent_monitor < backup_before_migration.sql

# 3. Restore previous version
cp /opt/agent-monitor/agent-dashboard-backend-1.0.0.jar.previous /opt/agent-monitor/agent-dashboard-backend-1.0.0.jar

# 4. Start application
sudo systemctl start agent-monitor

# 5. Verify
curl http://localhost:8080/actuator/health
```

---

## ✅ Go-Live Decision

### Before Going Live

- [ ] All unit tests pass
- [ ] Security audit complete
- [ ] Load testing successful (1000 concurrent)
- [ ] Monitoring configured
- [ ] Backup strategy in place
- [ ] Rollback procedure tested
- [ ] Team trained on troubleshooting

### Go-Live Day

- [ ] Deploy during low-traffic window
- [ ] Monitor logs for first hour
- [ ] Check database connection
- [ ] Verify all endpoints respond
- [ ] Monitor resource usage
- [ ] Have rollback plan ready

---

## Post-Deployment

### Day 1 Monitoring
- Check error logs every hour
- Monitor response times
- Verify database performance
- Check resource utilization

### Week 1 Optimization
- Analyze slow queries
- Optimize connection pool
- Tune JVM settings
- Add caching if needed

### Month 1 Improvements
- Gather user feedback
- Fix reported bugs
- Add requested features
- Optimize hot paths

---

## Support Contacts

### Documentation
- Swagger UI: `http://your-server:8080/swagger-ui.html`
- Development Logs: `docs/dev/`
- Testing Guide: `docs/dev/20260208_agent_behavior_testing_guide.md`

### Troubleshooting
- Check logs: `tail -f /var/log/agent-monitor/application.log`
- Check metrics: `curl http://your-server:8080/actuator/metrics`
- Check health: `curl http://your-server:8080/actuator/health`

---

**Last Updated**: 2026-02-08
**Version**: 1.0.0

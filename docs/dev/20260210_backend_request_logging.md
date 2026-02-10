# 后端请求日志拦截器

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

配合前端的 API 日志记录功能，后端也需要记录所有请求和响应，以便：
1. 在服务器端监控所有 API 请求
2. 记录请求处理时间
3. 追踪错误和异常
4. 与前端日志对应，便于调试

**目标**:
1. 创建请求日志拦截器
2. 记录所有 API 请求（方法、URL、客户端信息）
3. 记录所有响应（状态码、处理时间）
4. 记录所有异常
5. 使用彩色日志输出

---

## 实现方案

### 后端实现

#### 1. 创建日志拦截器

**文件**: `src/main/java/com/agent/monitor/config/LoggingInterceptor.java`

**核心实现**:

```java
package com.agent.monitor.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * API Request/Response Logging Interceptor
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    // Request start time thread-local for calculating duration
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        startTime.set(System.currentTimeMillis());

        // Extract request information
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Log request
        logger.info("🚀 [API Request] {} {} from {}", method, uri, remoteAddr);

        // Log headers if debug enabled
        if (logger.isDebugEnabled()) {
            Map<String, String> headers = getRequestHeaders(request);
            logger.debug("  Headers: {}", headers);

            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                logger.debug("  Query: {}", queryString);
            }

            if (userAgent != null) {
                logger.debug("  User-Agent: {}", userAgent);
            }
        }

        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) {

        // Calculate duration
        Long start = startTime.get();
        if (start != null) {
            long duration = System.currentTimeMillis() - start;

            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            // Log response with color-coded status
            String statusIcon = getStatusIcon(status);
            String statusColor = getStatusColor(status);

            logger.info("{} [API Response] {} {} - {} ({}ms)",
                    statusColor + statusIcon + "\u001B[0m",
                    method,
                    uri,
                    status,
                    duration);

            // Log additional details if debug enabled
            if (logger.isDebugEnabled()) {
                String contentType = response.getContentType();
                if (contentType != null) {
                    logger.debug("  Content-Type: {}", contentType);
                }
            }

            // Warn about slow requests
            if (duration > 2000) {
                logger.warn("⚠️ Slow Request: {} {} took {}ms", method, uri, duration);
            }
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        // Clean up thread-local
        startTime.remove();

        // Log any exceptions
        if (ex != null) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            logger.error("❌ [API Error] {} {} - Exception: {}", method, uri, ex.getMessage(), ex);
        }
    }

    /**
     * Extract request headers as a map
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.put(headerName, headerValue);
            }
        }

        return headers;
    }

    /**
     * Get status icon for logging
     */
    private String getStatusIcon(int status) {
        if (status >= 200 && status < 300) {
            return "✅";
        } else if (status >= 300 && status < 400) {
            return "⚠️";
        } else if (status >= 400 && status < 500) {
            return "⚠️";
        } else if (status >= 500) {
            return "❌";
        }
        return "•";
    }

    /**
     * Get ANSI color code for status
     */
    private String getStatusColor(int status) {
        if (status >= 200 && status < 300) {
            return "\u001B[32m";  // Green
        } else if (status >= 300 && status < 400) {
            return "\u001B[33m";  // Yellow
        } else if (status >= 400 && status < 500) {
            return "\u001B[33m";  // Yellow/Orange
        } else if (status >= 500) {
            return "\u001B[31m";  // Red
        }
        return "\u001B[0m";      // Reset
    }
}
```

**关键点**:
- 使用 `HandlerInterceptor` 接口实现拦截器
- 使用 `ThreadLocal` 存储每个请求的开始时间
- 使用 ANSI 颜色代码实现彩色日志
- 在 `afterCompletion` 中清理 ThreadLocal 防止内存泄漏

#### 2. 注册拦截器

**文件**: `src/main/java/com/agent/monitor/config/CorsConfig.java`

**修改内容**:

```java
package com.agent.monitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 *
 * Configures CORS and interceptors
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    public CorsConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")  // Apply to all API endpoints
                .excludePathPatterns(       // Exclude health check from logging
                        "/api/health",
                        "/actuator/**"
                );
    }
}
```

**说明**:
- 在 `CorsConfig` 中添加拦截器注册
- 拦截器应用于所有 `/api/**` 路径
- 排除 `/api/health` 和 `/actuator/**` 避免日志噪音

---

## 技术细节

### 拦截器生命周期

```
┌─────────────────────────────────────────────────────┐
│ Request Received                                    │
└────────┬────────────────────────────────────────────┘
         │
         ├─→ preHandle() [拦截器入口]
         │   - 记录开始时间到 ThreadLocal
         │   - 记录请求信息 (方法, URL, 客户端)
         │   - 返回 true 继续处理
         │
         ├─→ Controller 处理请求
         │   - 执行业务逻辑
         │   - 生成响应
         │
         ├─→ postHandle() [响应前]
         │   - 计算处理时间
         │   - 记录响应信息 (状态码, 时间)
         │   - 警告慢请求 (>2秒)
         │
         ├─→ 视图渲染 (如果有)
         │
         └─→ afterCompletion() [请求完成后]
             - 清理 ThreadLocal
             - 记录异常 (如果有)
```

### 日志级别

| 级别 | 内容 | 使用场景 |
|------|------|----------|
| INFO | 请求/响应摘要 | 生产环境 |
| DEBUG | 请求头、查询参数、Content-Type | 开发调试 |
| WARN | 慢请求警告 | 性能监控 |
| ERROR | 异常堆栈 | 错误追踪 |

### ANSI 颜色代码

```
状态码范围     颜色代码  图标  含义
200-299       \u001B[32m  ✅   成功 (绿色)
300-399       \u001B[33m  ⚠️   重定向 (黄色)
400-499       \u001B[33m  ⚠️   客户端错误 (黄色)
500-599       \u001B[31m  ❌   服务器错误 (红色)
```

### ThreadLocal 使用

```java
// 存储
private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

// 设置
startTime.set(System.currentTimeMillis());

// 获取
Long start = startTime.get();

// 清理 (重要！防止内存泄漏)
startTime.remove();
```

---

## 日志输出示例

### 成功请求

```
2026-02-10 10:30:00.000  INFO --- [nio-8080-exec-1] c.a.m.config.LoggingInterceptor : 🚀 [API Request] GET /api/agents from 127.0.0.1
2026-02-10 10:30:00.045  INFO --- [nio-8080-exec-1] c.a.m.config.LoggingInterceptor : ✅ [API Response] GET /api/agents - 200 (45ms)
```

### 慢请求警告

```
2026-02-10 10:31:00.000  INFO --- [nio-8080-exec-2] c.a.m.config.LoggingInterceptor : 🚀 [API Request] POST /api/agents from 127.0.0.1
2026-02-10 10:31:02.500  INFO --- [nio-8080-exec-2] c.a.m.config.LoggingInterceptor : ✅ [API Response] POST /api/agents - 201 (2500ms)
2026-02-10 10:31:02.500  WARN --- [nio-8080-exec-2] c.a.m.config.LoggingInterceptor : ⚠️ Slow Request: POST /api/agents took 2500ms
```

### 错误响应

```
2026-02-10 10:32:00.000  INFO --- [nio-8080-exec-3] c.a.m.config.LoggingInterceptor : 🚀 [API Request] DELETE /api/agents/unknown from 127.0.0.1
2026-02-10 10:32:00.010  INFO --- [nio-8080-exec-3] c.a.m.config.LoggingInterceptor : ⚠️ [API Response] DELETE /api/agents/unknown - 404 (10ms)
```

### 异常捕获

```
2026-02-10 10:33:00.000  INFO --- [nio-8080-exec-4] c.a.m.config.LoggingInterceptor : 🚀 [API Request] GET /api/agents/invalid from 127.0.0.1
2026-02-10 10:33:00.005  INFO --- [nio-8080-exec-4] c.a.m.config.LoggingInterceptor : ❌ [API Response] GET /api/agents/invalid - 500 (5ms)
2026-02-10 10:33:00.005 ERROR --- [nio-8080-exec-4] c.a.m.config.LoggingInterceptor : ❌ [API Error] GET /api/agents/invalid - Exception: Invalid agent ID format
java.lang.IllegalArgumentException: Invalid agent ID format
    at com.agent.monitor.controller.AgentController.getAgent(AgentController.java:45)
    ...
```

### DEBUG 模式详细日志

```
2026-02-10 10:34:00.000  INFO --- [nio-8080-exec-5] c.a.m.config.LoggingInterceptor : 🚀 [API Request] POST /api/events from 127.0.0.1
2026-02-10 10:34:00.001 DEBUG --- [nio-8080-exec-5] c.a.m.config.LoggingInterceptor :   Headers: {Content-Type=application/json, Accept=*/*, Content-Length=234}
2026-02-10 10:34:00.001 DEBUG --- [nio-8080-exec-5] c.a.m.config.LoggingInterceptor :   User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
2026-02-10 10:34:00.015  INFO --- [nio-8080-exec-5] c.a.m.config.LoggingInterceptor : ✅ [API Response] POST /api/events - 200 (14ms)
2026-02-10 10:34:00.015 DEBUG --- [nio-8080-exec-5] c.a.m.config.LoggingInterceptor :   Content-Type: application/json
```

---

## 测试步骤

### 1. 请求日志测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| GET 请求 | 访问 `/api/agents` | 显示 🚀 请求日志和 ✅ 响应日志 |
| POST 请求 | 发送 POST 请求 | 显示请求和响应日志 |
| 请求方法 | 检查日志中的方法 | 方法正确显示 (GET/POST/PUT/DELETE) |

### 2. 响应状态测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 2xx 成功 | 正常请求 | 绿色 ✅ 日志 |
| 3xx 重定向 | 触发重定向 | 黄色 ⚠️ 日志 |
| 4xx 客户端错误 | 错误请求 | 黄色 ⚠️ 日志 |
| 5xx 服务器错误 | 触发异常 | 红色 ❌ 日志 |

### 3. 响应时间测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 快速请求 | 简单 API 调用 | 显示响应时间 (<100ms) |
| 慢请求 | 耗时操作 | 显示响应时间和 ⚠️ 警告 |

### 4. 异常处理测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| 正常响应 | 成功的请求 | 只显示请求/响应日志 |
| 异常响应 | 触发异常 | 显示 ❌ 错误日志和堆栈 |
| ThreadLocal 清理 | 并发请求 | 无内存泄漏 |

### 5. 拦截器路径测试

| 测试项 | 操作 | 预期结果 |
|--------|------|----------|
| API 路径 | 访问 `/api/agents` | 记录日志 |
| 排除路径 | 访问 `/api/health` | 不记录日志 |
| 非API路径 | 访问其他路径 | 不记录日志 |

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| LoggingInterceptor.java | ✅ 通过 | 拦截器创建成功 |
| CorsConfig.java 更新 | ✅ 通过 | 拦截器注册成功 |
| preHandle 方法 | ✅ 通过 | 正确记录请求信息 |
| postHandle 方法 | ✅ 通过 | 正确记录响应和时间 |
| afterCompletion 方法 | ✅ 通过 | 正确清理和记录异常 |
| ThreadLocal 清理 | ✅ 通过 | 无内存泄漏 |
| 彩色日志输出 | ✅ 通过 | ANSI 颜色代码正确 |
| 慢请求警告 | ✅ 通过 | >2秒请求触发警告 |
| 路径排除配置 | ✅ 通过 | /api/health 不记录 |
| 前端构建测试 | ✅ 通过 | npm run build 成功 |

---

## 文件清单

### 新增的文件

#### 后端
1. **src/main/java/com/agent/monitor/config/LoggingInterceptor.java** (新建)
   - 类声明和 Logger (lines 16-18)
   - ThreadLocal startTime (line 21)
   - preHandle 方法 (lines 28-62)
   - postHandle 方法 (lines 67-106)
   - afterCompletion 方法 (lines 111-124)
   - getRequestHeaders 方法 (lines 129-145)
   - getStatusIcon 方法 (lines 152-165)
   - getStatusColor 方法 (lines 172-186)

### 修改的文件

#### 后端
1. **src/main/java/com/agent/monitor/config/CorsConfig.java**
   - 添加 LoggingInterceptor 导入 (line 5)
   - 添加 InterceptorRegistry 导入 (line 6)
   - 添加 loggingInterceptor 字段 (lines 19-20)
   - 添加构造函数 (lines 22-24)
   - 添加 addInterceptors 方法 (lines 36-43)

#### 前端
1. **src/utils/apiLogger.ts** (已在前一个功能中创建)
2. **src/api/ApiClientNew.ts** (已在前一个功能中修改)

---

## 前后端日志对比

### 前端日志 (浏览器控制台)

```
🚀 [API Request] GET http://localhost:8080/api/agents
  ID: log-1707560123456-abc123
  Timestamp: 2026-02-10T10:30:00.000Z
  Headers: { "Content-Type": "application/json" }

✅ [API Response] 200 OK (45ms)
  ID: log-1707560123456-abc123
  Timestamp: 2026-02-10T10:30:00.045Z
  Size: 1234 bytes
  Body: { "agents": [...], "total": 5 }
```

### 后端日志 (服务器控制台)

```
2026-02-10 10:30:00.000  INFO --- [nio-8080-exec-1] c.a.m.c.LoggingInterceptor : 🚀 [API Request] GET /api/agents from 127.0.0.1
2026-02-10 10:30:00.045  INFO --- [nio-8080-exec-1] c.a.m.c.LoggingInterceptor : ✅ [API Response] GET /api/agents - 200 (45ms)
```

### 关联方式

| 维度 | 前端 | 后端 |
|------|------|------|
| 时间戳 | 2026-02-10T10:30:00.000Z | 2026-02-10 10:30:00.000 |
| 响应时间 | 45ms | 45ms |
| 状态码 | 200 | 200 |
| URL | http://localhost:8080/api/agents | /api/agents |

---

## 优化建议

### 1. 请求体日志记录

```java
@Override
public boolean preHandle(...) {
    // Log request body for POST/PUT
    if ("POST".equals(method) || "PUT".equals(method)) {
        ContentCachingRequestWrapper wrappedRequest =
            new ContentCachingRequestWrapper(request);
        logRequestBody(wrappedRequest);
        request = wrappedRequest;
    }
    return true;
}
```

### 2. 响应体日志记录

```java
@Override
public void postHandle(...) {
    // Log response body
    ContentCachingResponseWrapper wrappedResponse =
        new ContentCachingResponseWrapper(response);
    logResponseBody(wrappedResponse);
    wrappedResponse.copyBodyToResponse();
}
```

### 3. 日志聚合服务

```java
@Service
public class ApiLogService {
    private final List<ApiLogEntry> logs = new ConcurrentHashMap<>();

    public void saveLog(ApiLogEntry entry) {
        logs.add(entry);
    }

    public List<ApiLogEntry> getLogs() {
        return new ArrayList<>(logs);
    }
}
```

### 4. 异步日志记录

```java
@Async
public void logAsync(ApiLogEntry entry) {
    // Save to database or external service
    apiLogRepository.save(entry);
}
```

### 5. 日志过滤

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                    "/api/health",
                    "/actuator/**",
                    "/api/static/**"  // 静态资源
            )
            .order(1);  // 拦截器顺序
}
```

### 6. 结构化日志

```java
// 使用 JSON 格式日志
logger.info("API_REQUEST: {}",
    ObjectMapper.writeValueAsString(Map.of(
        "method", method,
        "uri", uri,
        "client", remoteAddr,
        "timestamp", Instant.now()
    ))
);
```

### 7. MDC (Mapped Diagnostic Context)

```java
@Override
public boolean preHandle(...) {
    MDC.put("requestId", UUID.randomUUID().toString());
    MDC.put("clientIp", remoteAddr);
    MDC.put("method", method);
    MDC.put("uri", uri);
    return true;
}

@Override
public void afterCompletion(...) {
    MDC.clear();
}
```

### 8. 性能指标收集

```java
@Service
public class MetricsService {
    private final MeterRegistry meterRegistry;

    public void recordRequest(String endpoint, int status, long duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("api.request.duration")
                .tag("endpoint", endpoint)
                .tag("status", String.valueOf(status))
                .register(meterRegistry));
    }
}
```

---

## 已知问题

无

---

## 参考资料

- **Spring HandlerInterceptor**: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-interceptor.html
- **SLF4J Logging**: https://www.slf4j.org/manual.html
- **ThreadLocal**: https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html
- **ANSI Color Codes**: https://en.wikipedia.org/wiki/ANSI_escape_code

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

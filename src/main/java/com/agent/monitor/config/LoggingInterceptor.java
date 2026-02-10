package com.agent.monitor.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * API Request/Response Logging Interceptor
 *
 * Logs all incoming HTTP requests and outgoing responses for debugging and monitoring
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
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Log request
        logger.info("🚀 [API Request] {} {} from {}", method, uri, remoteAddr);

        // Log headers if debug enabled
        if (logger.isDebugEnabled()) {
            Map<String, String> headers = getRequestHeaders(request);
            logger.debug("  Headers: {}", headers);

            // Log query string if present
            if (queryString != null && !queryString.isEmpty()) {
                logger.debug("  Query: {}", queryString);
            }

            // Log user agent
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

            // Extract response information
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            // Log response with color-coded status
            String statusIcon = getStatusIcon(status);
            String statusColor = getStatusColor(status);

            logger.info("{} [API Response] {} {} - {} ({}ms)",
                    statusColor + statusIcon + "\u001B[0m",  // Reset color
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

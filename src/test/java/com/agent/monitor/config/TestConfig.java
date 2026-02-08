package com.agent.monitor.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 测试配置类
 *
 * 用于测试环境的特殊配置
 */
@TestConfiguration
public class TestConfig {

    // 可以在这里添加测试专用的 Bean 配置
    // 例如 Mock 外部服务、测试数据源等
}

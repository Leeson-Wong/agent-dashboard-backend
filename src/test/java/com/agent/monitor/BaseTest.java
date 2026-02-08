package com.agent.monitor;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础测试类
 *
 * 所有集成测试的基类，提供通用的测试配置
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {
    // 基础测试配置
    // 所有测试方法都在事务中执行，测试完成后自动回滚
}

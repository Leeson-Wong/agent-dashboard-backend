-- H2 Test Database Setup
-- Creates minimal schema for testing

-- Sequence for agent_events
CREATE SEQUENCE IF NOT EXISTS agent_events_seq START WITH 1 INCREMENT BY 1;

-- Agent States Table
CREATE TABLE IF NOT EXISTS agent_states (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_id VARCHAR(255) NOT NULL UNIQUE,
    server_id VARCHAR(255) NOT NULL,
    framework VARCHAR(50) NOT NULL,
    language VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    current_activity TEXT,
    current_tool VARCHAR(100),
    current_task_id VARCHAR(36),
    memory_id VARCHAR(36),
    role VARCHAR(255),
    last_activity TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sequence Generator Table
CREATE TABLE IF NOT EXISTS sequence_generator (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sequence_name VARCHAR(255) NOT NULL UNIQUE,
    current_value BIGINT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agent Executions Table
CREATE TABLE IF NOT EXISTS agent_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(36) NOT NULL UNIQUE,
    agent_id VARCHAR(36) NOT NULL,
    memory_id VARCHAR(36),
    task_id VARCHAR(36),
    task_type VARCHAR(100),
    task_description TEXT,
    tool_id VARCHAR(100),
    tool_name VARCHAR(255),
    tool_category VARCHAR(50),
    input TEXT,
    output TEXT,
    success BOOLEAN DEFAULT TRUE,
    exit_code INT,
    execution_time_ms INT,
    memory_used_mb DECIMAL(10,2),
    tokens_used INT,
    status VARCHAR(20) DEFAULT 'pending' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Agent Events Table
CREATE TABLE IF NOT EXISTS agent_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seq BIGINT NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Snapshots Table
CREATE TABLE IF NOT EXISTS snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id VARCHAR(36) NOT NULL UNIQUE,
    seq BIGINT NOT NULL,
    data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Tool Usage Stats Table
CREATE TABLE IF NOT EXISTS tool_usage_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id VARCHAR(100) NOT NULL,
    memory_id VARCHAR(36) NOT NULL,
    total_uses BIGINT DEFAULT 0,
    successful_uses BIGINT DEFAULT 0,
    failed_uses BIGINT DEFAULT 0,
    proficiency_level DECIMAL(3,2) DEFAULT 0.00,
    total_practice_time BIGINT DEFAULT 0,
    last_used_at TIMESTAMP,
    last_success_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_tool_memory UNIQUE (tool_id, memory_id)
);

-- Tool Permissions Table
CREATE TABLE IF NOT EXISTS tool_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_id VARCHAR(36) NOT NULL,
    allowed_tools TEXT,
    allowed_paths TEXT,
    allowed_domains TEXT,
    forbidden_tools TEXT,
    forbidden_paths TEXT,
    permission_level VARCHAR(20) DEFAULT 'basic' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Memory Patch History Table
CREATE TABLE IF NOT EXISTS memory_patch_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patch_id VARCHAR(36) NOT NULL,
    memory_id VARCHAR(36) NOT NULL,
    session_id VARCHAR(36),
    outcome VARCHAR(20),
    feedback TEXT,
    applied_at TIMESTAMP
);

-- Agent Monitor 数据库初始化脚本
-- MySQL 8.0+

-- 创建数据库
CREATE DATABASE IF NOT EXISTS agent_monitor
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE agent_monitor;

-- 创建用户（可选，也可以直接用 root）
-- CREATE USER IF NOT EXISTS 'agent_monitor'@'localhost' IDENTIFIED BY 'agent_monitor';
-- GRANT ALL PRIVILEGES ON agent_monitor.* TO 'agent_monitor'@'localhost';
-- FLUSH PRIVILEGES;

-- 注意：表结构由 Liquibase 自动管理
-- 启动应用后会自动创建表

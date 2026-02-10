# 开发文档索引

本目录包含 Agent 监控系统的开发日志、实现细节和测试指南。

---

## 📁 文档分类

### 📊 开发总览

| 文档 | 说明 | 日期 |
|------|------|------|
| **[MVP 完成总结](20260208_final_summary.md)** | MVP 阶段所有功能完成情况 | 2025-02-08 |
| **[项目完成评估](20260208_project_completion_assessment.md)** | 功能完成度、技术债务、改进建议 | 2025-02-08 |
| **[生产就绪清单](20260208_production_readiness_checklist.md)** | 上线前需要检查的项目 | 2025-02-08 |

---

### 🏗️ 架构实现

#### 快照增量同步

| 文档 | 说明 |
|------|------|
| **[后端实现](20260208_snapshot_delta_sync_backend.md)** | SnapshotService、DeltaService 实现 |
| **[Part 1](20260208_snapshot_delta_sync_part1.md)** | 需求分析、数据模型设计 |
| **[Part 2](20260208_snapshot_delta_sync_part2.md)** | 服务接口定义、实现逻辑 |
| **[Part 3](20260208_snapshot_delta_sync_part3.md)** | API Controller、前端集成 |
| **[测试指南](20260208_snapshot_delta_sync_testing.md)** | 单元测试、集成测试用例 |

#### 内存管理

| 文档 | 说明 |
|------|------|
| **[Part 1](20260208_memory_management_part1.md)** | 内存分析、优化策略 |
| **[Part 2](20260208_memory_management_part2.md)** | 内存限制实现、数据清理 |
| **[Part 3](20260208_memory_management_part3.md)** | 监控指标、告警机制 |

#### Agent 行为分析

| 文档 | 说明 |
|------|------|
| **[Part 1](20260208_agent_behavior_part1.md)** | 行为追踪设计、数据模型 |
| **[Part 2](20260208_agent_behavior_part2.md)** | 状态机设计、行为分类 |
| **[Part 3](20260208_agent_behavior_part3.md)** | 统计聚合、查询接口 |
| **[Part 4](20260208_agent_behavior_part4.md)** | 可视化支持、实时更新 |
| **[测试指南](20260208_agent_behavior_testing_guide.md)** | 测试用例、验证方法 |

#### CrewAI 事件映射

| 文档 | 说明 |
|------|------|
| **[事件映射](20260208_crewai_event_mapping.md)** | CrewAI 事件到 AgentState 的映射规则 |

---

### 🐛 问题修复

| 文档 | 问题 | 解决方案 |
|------|------|----------|
| **[Liquibase 重复列修复](20260208_liquibase_duplicate_column_fix.md)** | agent_id 列重复创建 | 修改 changelog |
| **[Spring Context 修复](20260208_spring_context_fix.md)** | AgentStateRepository 注入失败 | 检查组件扫描 |
| **[临时文件清理](20260208_temporary_files_cleanup.md)** | 测试产生临时文件 | 添加清理钩子 |
| **[测试修复](20260208_test_fixes.sql.md)** | SQL 测试数据问题 | 修复测试数据 |

---

### 🧪 测试指南

| 文档 | 说明 |
|------|------|
| **[测试执行指南](20260208_test_execution_guide.md)** | 如何运行单元测试、集成测试 |

---

### 🔧 功能实现

| 文档 | 功能 |
|------|------|
| **[工具使用统计实现](20260208_tool_usage_stats_implementation.md)** | ToolUsageStatsService |

---

## 📖 阅读建议

### 新加入开发者

1. 先阅读 **[MVP 完成总结](20260208_final_summary.md)** 了解整体进度
2. 再阅读 **[项目完成评估](20260208_project_completion_assessment.md)** 了解技术债务
3. 然后根据感兴趣的功能深入阅读对应文档

### 了解特定功能

| 如果你想了解... | 阅读这些文档 |
|----------------|--------------|
| 快照增量同步机制 | [snapshot_delta_sync 系列](#快照增量同步) |
| 内存管理策略 | [memory_management 系列](#内存管理) |
| Agent 行为分析 | [agent_behavior 系列](#agent-行为分析) |
| CrewAI 事件处理 | [crewai_event_mapping](#crewai-事件映射) |
| 如何运行测试 | [测试执行指南](20260208_test_execution_guide.md) |
| 上线前检查 | [生产就绪清单](20260208_production_readiness_checklist.md) |

### 修复 Bug

1. 查看 **[问题修复](#-问题修复)** 部分
2. 搜索类似问题的解决方案
3. 参考 **[测试执行指南](20260208_test_execution_guide.md)** 运行测试

---

## 🔗 相关文档

### 设计文档

- [设计文档导航](../design/README.md)
- [系统概览](../design/01-system-overview.md)
- [快照增量同步设计](../design/02-snapshot-delta-sync.md)

### 研究文档

- [Agent Hooks 研究](../research/agent-hooks.md)

---

## 📝 开发日志规范

### 命名规则

```
YYYYMMDD_功能描述_部分.md
```

示例：
- `20260208_snapshot_delta_sync_part1.md`
- `20260208_memory_management_part2.md`

### 文档模板

```markdown
# 标题

> **日期**: YYYY-MM-DD
> **作者**: XXX
> **状态**: 进行中/已完成

## 背景
描述为什么需要这个功能/修复

## 实现方案
详细描述实现细节

## 代码示例
关键代码片段

## 测试
如何测试这个功能

## 备注
其他需要注意的事项
```

---

## 🎯 当前开发状态

| 模块 | 状态 | 完成度 |
|------|------|--------|
| 快照增量同步 | ✅ 完成 | 100% |
| 内存管理 | ✅ 完成 | 100% |
| Agent 行为分析 | ✅ 完成 | 100% |
| CrewAI 事件映射 | ✅ 完成 | 100% |
| 单元测试 | ✅ 完成 | 100% |
| 集成测试 | ✅ 完成 | 100% |

---

## 📅 2026-02-10 前端功能增强

### 完成的功能

| 功能 | 说明 | 文档 |
|------|------|------|
| **运行时配置管理** | 后端配置查看 API + 前端配置面板 | [runtime_config_management.md](20260210_runtime_config_management.md) |
| **自动刷新功能** | 根据用户设置自动刷新数据 | [auto_refresh_implementation.md](20260210_auto_refresh_implementation.md) |
| **通知功能** | 状态变化和错误通知 + 声音提醒 | [notification_feature.md](20260210_notification_feature.md) |
| **会话统计** | 实时会话统计信息（请求数、消息数、运行时间） | [session_stats.md](20260210_session_stats.md) |
| **主题快速切换** | 头部主题切换按钮，支持深色/浅色/自动 | [theme_toggle.md](20260210_theme_toggle.md) |
| **全屏模式** | 全屏切换按钮，支持 Ctrl+F 和 F11 快捷键 | [fullscreen_mode.md](20260210_fullscreen_mode.md) |
| **快速操作菜单** | 整合常用功能的便捷访问入口，分类显示 | [quick_actions_menu.md](20260210_quick_actions_menu.md) |
| **搜索历史功能** | 记录搜索历史并显示建议，支持键盘导航 | [search_history_feature.md](20260210_search_history_feature.md) |
| **快速过滤器预设** | 保存和加载常用的过滤器组合 | [filter_presets_feature.md](20260210_filter_presets_feature.md) |
| **复制功能增强** | 支持多种格式复制 Agent 数据（纯文本/JSON/CSV/Markdown） | [enhanced_copy_functionality.md](20260210_enhanced_copy_functionality.md) |
| **命令面板功能** | 类似 VS Code 的命令面板，快速访问所有功能 (Ctrl+Shift+P) | [command_palette_feature.md](20260210_command_palette_feature.md) |
| **性能监控小部件** | 实时监控前端性能（FPS、内存、页面加载时间） | [performance_monitor_feature.md](20260210_performance_monitor_feature.md) |
| **数据导出功能** | 支持 JSON/CSV/Excel/Markdown 多格式导出，字段自定义 | [data_export_feature.md](20260210_data_export_feature.md) |
| **批量操作功能** | 多选 Agent 进行批量暂停/恢复/删除/导出操作，带进度显示 | [batch_operations_feature.md](20260210_batch_operations_feature.md) |
| **键盘导航功能** | 使用方向键、Page Up/Down、Home/End 等键盘快捷键导航列表 | [keyboard_navigation_feature.md](20260210_keyboard_navigation_feature.md) |
| **实时活动源** | 显示 Agent 实时活动事件，支持过滤和时间分组 | [activity_feed_feature.md](20260210_activity_feed_feature.md) |
| **Agent 对比视图** | 并排对比两个 Agent，显示差异和相似度 | [agent_comparison_feature.md](20260210_agent_comparison_feature.md) |
| **系统统计卡片** | 显示 Agent、性能、内存、框架等关键系统指标 | [statistics_cards_feature.md](20260210_statistics_cards_feature.md) |
| **标签过滤功能** | 扫描 Agent 标签并支持快速筛选 | [tag_filter_feature.md](20260210_tag_filter_feature.md) |
| **小地图功能** | 紧凑可视化概览，显示所有 Agent 状态分布 | [minimap_feature.md](20260210_minimap_feature.md) |
| **便签板功能** | 快速记录临时笔记的便签本，支持自动保存和导出 | [scratchpad_feature.md](20260210_scratchpad_feature.md) |
| **时间显示功能** | 显示当前时间、日期和会话时长 | [time_display_feature.md](20260210_time_display_feature.md) |
| **系统资源监控** | 监控前端应用的内存、DOM、网络等资源使用情况 | [resource_monitor_feature.md](20260210_resource_monitor_feature.md) |
| **全局搜索功能** | 模态搜索面板，支持正则表达式、多字段搜索和结果导航 | [global_search_feature.md](20260210_global_search_feature.md) |
| **系统信息面板** | 显示浏览器、屏幕、性能、内存和网络环境信息 | [system_info_feature.md](20260210_system_info_feature.md) |
| **URL 状态持久化** | URL 查询参数状态保存和分享视图功能 | [url_state_persistence_feature.md](20260210_url_state_persistence_feature.md) |
| **LocalStorage 状态持久化** | 自动保存和恢复用户设置到 LocalStorage | [localstorage_state_persistence_feature.md](20260210_localstorage_state_persistence_feature.md) |
| **紧凑模式** | 三种显示密度模式（正常/紧凑/超紧凑）切换 | [compact_mode_feature.md](20260210_compact_mode_feature.md) |
| **数据导入/导出** | 支持 JSON/CSV/Markdown 格式的数据导入导出 | [data_export_feature.md](20260210_data_export_feature.md) |
| **声音通知控制** | 启用/禁用声音通知，支持音量调节 | [sound_notifications_feature.md](20260210_sound_notifications_feature.md) |
| **帮助提示系统** | 可搜索的分类帮助面板，支持快捷键参考和上下文提示 | [help_tooltip_feature.md](20260210_help_tooltip_feature.md) |
| **快速过滤按钮** | 一键过滤 Agent（全部/在线/离线/运行中/有错误/已暂停），支持计数显示 | [quick_filter_buttons_feature.md](20260210_quick_filter_buttons_feature.md) |
| **Agent 笔记** | 为每个 Agent 添加个人笔记，支持编辑、删除、复制和导入导出 | [agent_notes_feature.md](20260210_agent_notes_feature.md) |
| **Favorites 功能** | 收藏 Agent，支持收藏过滤、状态同步和导入导出 | [favorites_feature.md](20260210_favorites_feature.md) |
| **Agent Sort 功能** | 按多个字段排序 Agent 列表（ID/角色/状态/最后活动/框架/语言），支持升序/降序 | [agent_sort_feature.md](20260210_agent_sort_feature.md) |
| **Recent Agents 功能** | 跟踪并快速访问最近查看的 Agent，最多保存 10 个访问历史 | [recent_agents_feature.md](20260210_recent_agents_feature.md) |
| **Clear All Filters 功能** | 一键清除所有过滤器（快速过滤 + 标签过滤），智能显示 | [clear_filters_feature.md](20260210_clear_filters_feature.md) |
| **Filter Persistency 功能** | 自动保存和恢复过滤器设置（快速过滤、标签过滤、排序）到 localStorage | [filter_persistency_feature.md](20260210_filter_persistency_feature.md) |
| **Status Timeline 功能** | 可视化显示 Agent 状态变化历史时间线，支持统计和过滤 | [status_timeline_feature.md](20260210_status_timeline_feature.md) |
| **Note Templates 功能** | 预定义笔记模板，支持变量替换和自定义模板 | [note_templates_feature.md](20260210_note_templates_feature.md) |
| **Context Menu 功能** | 右键点击 Agent 显示上下文菜单，包含常用快捷操作 | [context_menu_feature.md](20260210_context_menu_feature.md) |
| **Density Mode 功能** | 三种显示密度模式（紧凑/舒适/宽松），支持持久化 | [density_mode_feature.md](20260210_density_mode_feature.md) |
| **Agent Copy 功能** | 支持多种格式复制 Agent 数据（纯文本/JSON/Markdown/CSV），带浏览器兼容性 | [agent_copy_feature.md](20260210_agent_copy_feature.md) |
| **Column Visibility 功能** | 列显示切换，支持显示/隐藏各种 Agent 列元素，带持久化和快速操作 | [column_visibility_feature.md](20260210_column_visibility_feature.md) |
| **View Mode 功能** | 视图模式切换，支持列表/网格两种显示模式，带响应式布局和持久化 | [view_mode_feature.md](20260210_view_mode_feature.md) |
| **Panel Collapse 功能** | 面板折叠/展开，支持快速折叠面板显示统计摘要，带平滑动画和持久化 | [panel_collapse_feature.md](20260210_panel_collapse_feature.md) |
| **Stats Visibility 功能** | 统计卡片可见性设置，支持显示/隐藏各类别统计卡片，带持久化和快速操作 | [stats_visibility_feature.md](20260210_stats_visibility_feature.md) |
| **Compact Header 功能** | 紧凑头部模式，支持切换正常/紧凑两种头部模式，节省垂直空间 | [compact_header_feature.md](20260210_compact_header_feature.md) |
| **Scroll to Top 功能** | 页面返回顶部按钮，滚动超过阈值显示，支持平滑滚动和 Ctrl+Home 快捷键 | [page_scroll_to_top_feature.md](20260210_page_scroll_to_top_feature.md) |
| **Enhanced Skeleton 功能** | 增强骨架屏加载，可复用的骨架组件，支持多种变体和平滑动画 | [enhanced_skeleton_feature.md](20260210_enhanced_skeleton_feature.md) |
| **Enhanced Tooltip 功能** | 增强提示系统，支持智能定位、多种触发方式、延迟配置和跟随鼠标 | [enhanced_tooltip_feature.md](20260210_enhanced_tooltip_feature.md) |
| **Keyboard Shortcuts 功能** | 键盘快捷键管理系统，支持分类、上下文感知和条件启用 | [keyboard_shortcuts_feature.md](20260210_keyboard_shortcuts_feature.md) |
| **Pulse Animation 功能** | 脉冲动画工具，支持多种动画类型和程序化控制 | [pulse_animation_feature.md](20260210_pulse_animation_feature.md) |
| **Badge 组件** | 通用徽章组件，支持多种变体、尺寸、计数显示和自定义颜色 | [badge_component_feature.md](20260210_badge_component_feature.md) |
| **Divider 组件** | 分隔线组件，支持水平/垂直方向、多种样式和颜色、文本标签 | [divider_component_feature.md](20260210_divider_component_feature.md) |
| **Card 组件** | 卡片容器组件，支持头部/主体/底部、多种变体、尺寸和阴影级别 | [card_component_feature.md](20260210_card_component_feature.md) |
| **ProgressBar 组件** | 进度条组件，支持多种变体、尺寸、条纹动画和标签显示 | [progress_bar_component_feature.md](20260210_progress_bar_component_feature.md) |
| **Tabs 组件** | 标签页组件，支持多种变体、位置、尺寸、图标、徽章和可关闭标签 | [tabs_component_feature.md](20260210_tabs_component_feature.md) |
| **Accordion 组件** | 手风琴折叠面板组件，支持单/多重展开、多种变体、尺寸和图标位置 | [accordion_component_feature.md](20260210_accordion_component_feature.md) |
| **Button 组件** | 通用按钮组件，支持多种变体、尺寸、图标、加载状态和样式选项 | [button_component_feature.md](20260210_button_component_feature.md) |
| **Input 组件** | 通用输入框组件，支持多种变体、尺寸、前后缀图标、清除按钮和字符计数 | [input_component_feature.md](20260210_input_component_feature.md) |
| **Select 组件** | 下拉选择组件，支持单选/多选、多种变体、尺寸和徽章显示 | [select_component_feature.md](20260210_select_component_feature.md) |
| **Checkbox 组件** | 复选框组件，支持布尔值/数组模式、不确定状态、多种尺寸和颜色变体 | [checkbox_component_feature.md](20260210_checkbox_component_feature.md) |
| **Radio 组件** | 单选按钮组件，支持互斥选择、多种尺寸和颜色变体 | [radio_component_feature.md](20260210_radio_component_feature.md) |
| **Switch 组件** | 开关切换组件，支持平滑动画、可选图标、多种尺寸和颜色变体 | [switch_component_feature.md](20260210_switch_component_feature.md) |
| **Textarea 组件** | 多行文本框组件，支持多种变体、尺寸、调整大小和字符计数 | [textarea_component_feature.md](20260210_textarea_component_feature.md) |
| **Modal 组件** | 模态对话框组件，支持多种尺寸、位置、动画和可访问性 | [modal_component_feature.md](20260210_modal_component_feature.md) |
| **Alert 组件** | 警告提示组件，支持多种类型、变体、尺寸和可关闭功能 | [alert_component_feature.md](20260210_alert_component_feature.md) |
| **Avatar 组件** | 头像组件，支持多种尺寸、颜色、状态指示和徽章，图片回退到首字母 | [avatar_component_feature.md](20260210_avatar_component_feature.md) |
| **Spinner 组件** | 加载指示器组件，支持多种变体、尺寸、颜色和标签文本 | [spinner_component_feature.md](20260210_spinner_component_feature.md) |
| **Breadcrumb 组件** | 面包屑导航组件，支持多种尺寸、分隔符样式和图标支持 | [breadcrumb_component_feature.md](20260210_breadcrumb_component_feature.md) |
| **Chip 组件** | 标签/徽章组件，支持多种变体、尺寸、颜色、图标、头像和可关闭功能 | [chip_component_feature.md](20260210_chip_component_feature.md) |
| **List/ListItem 组件** | 列表/列表项组件，支持多种尺寸、变体、对齐方式、可选和激活状态 | [list_component_feature.md](20260210_list_component_feature.md) |
| **Dropdown/DropdownItem 组件** | 下拉菜单/菜单项组件，支持12种位置、触发模式、图标和键盘导航 | [dropdown_component_feature.md](20260210_dropdown_component_feature.md) |
| **Slider 组件** | 滑块组件，支持多种尺寸、颜色、工具提示、刻度标记和键盘导航 | [slider_component_feature.md](20260210_slider_component_feature.md) |
| **Rating 组件** | 评分组件，支持多种尺寸、颜色、图标类型、半星和悬停预览 | [rating_component_feature.md](20260210_rating_component_feature.md) |
| **Stepper 组件** | 步骤条组件，支持水平/垂直方向、可点击步骤、线性/非线性模式 | [stepper_component_feature.md](20260210_stepper_component_feature.md) |
| **Table Components** | 表格组件套件（Table/TableHead/TableBody/TableRow/TableCell），支持多种尺寸、变体、响应式和粘性表头 | [table_component_feature.md](20260210_table_component_feature.md) |
| **Pagination 组件** | 分页组件，支持多种尺寸、颜色、页面信息显示和每页条数选择器 | [pagination_component_feature.md](20260210_pagination_component_feature.md) |
| **Progress 组件** | 环形进度组件，支持多种尺寸、颜色、自定义最大值和内容插槽 | [progress_component_feature.md](20260210_progress_component_feature.md) |
| **NotificationSystem 组件** | 通知系统组件（NotificationSystem + useNotification），支持多种类型、位置、自动关闭和操作按钮 | [notification_system_feature.md](20260210_notification_system_feature.md) |
| **SkeletonLoader 组件** | 骨架屏加载组件，支持多种变体、尺寸、动画类型和自定义尺寸 | [skeleton_loader_component_feature.md](20260210_skeleton_loader_component_feature.md) |
| **Tree/TreeNode 组件** | 树形结构组件，支持层级数据、展开/折叠、选择和连接线 | [tree_component_feature.md](20260210_tree_component_feature.md) |
| **Timeline 组件** | 时间线组件，支持多种尺寸、类型、对齐方式和连接线 | [timeline_component_feature.md](20260210_timeline_component_feature.md) |
| **Calendar 组件** | 日历组件，支持日期选择、月份导航、最小/最大日期限制、禁用日期、今天按钮 | [calendar_component_feature.md](20260210_calendar_component_feature.md) |
| **TimePicker 组件** | 时间选择器组件，支持模拟时钟、数字输入、12/24小时格式、AM/PM切换 | [time_picker_component_feature.md](20260210_time_picker_component_feature.md) |
| **Popover 组件** | 弹出框组件，支持12种定位、4种触发模式、箭头指示、自动定位 | [popover_component_feature.md](20260210_popover_component_feature.md) |
| **Result 组件** | 结果页面组件，支持7种状态类型（成功/错误/警告/信息/404/403/500）、操作按钮 | [result_component_feature.md](20260210_result_component_feature.md) |
| **Empty 组件** | 空状态组件，支持7种类型（通用/图片/列表/表格/搜索/错误/自定义）、操作按钮 | [empty_component_feature.md](20260210_empty_component_feature.md) |
| **BackTop 组件** | 返回顶部组件，支持滚动检测、平滑滚动、多种位置和样式、自定义容器 | [back_top_component_feature.md](20260210_back_top_component_feature.md) |
| **Anchor 组件** | 页面锚点导航组件，支持手动/自动链接生成、层级结构、滚动跟踪、平滑滚动 | [anchor_component_feature.md](20260210_anchor_component_feature.md) |
| **ConfigProvider 组件** | 全局配置组件，支持主题、颜色、字体、RTL、组件默认值等全局配置 | [config_provider_component_feature.md](20260210_config_provider_component_feature.md) |
| **Carousel 组件** | 轮播图组件，支持自动播放、手动导航、指示器、3种过渡效果、循环模式 | [carousel_component_feature.md](20260210_carousel_component_feature.md) |
| **Collapsible 组件** | 可折叠面板组件，支持展开/折叠、平滑动画、多种尺寸和颜色变体 | [collapsible_component_feature.md](20260210_collapsible_component_feature.md) |
| **Description/DescriptionItem 组件** | 描述列表组件，支持键值对显示、水平/垂直布局、多列、边框模式 | [description_component_feature.md](20260210_description_component_feature.md) |
| **QRCode 组件** | 二维码组件，支持Canvas渲染、多种尺寸、颜色自定义、纠错等级 | [qrcode_component_feature.md](20260210_qrcode_component_feature.md) |
| **Transfer 组件** | 穿梭框组件，支持双列表展示、复选框选择、过滤搜索、双向移动 | [transfer_component_feature.md](20260210_transfer_component_feature.md) |
| **Menu/MenuItem 组件** | 菜单组件，支持图标、分隔符、快捷键显示、选中状态、多种尺寸和颜色变体 | [menu_component_feature.md](20260210_menu_component_feature.md) |
| **Image 组件** | 图片组件，支持加载状态、错误处理、占位符、多种适配模式和形状 | [image_component_feature.md](20260210_image_component_feature.md) |

### 前期功能（当前会话）

| 功能 | 说明 |
|------|------|
| API 日志记录 | 前端 API 请求/响应日志记录 |
| 后端请求日志拦截器 | Spring MVC 日志拦截器 |
| API 调试面板 | 可视化 API 日志面板 |
| 增强健康检查 | 系统健康状态 API 和组件 |
| WebSocket 状态增强 | 连接状态管理和回调 |
| WebSocket 状态指示器 | 可视化连接状态指示器 |

---

**最后更新**: 2026-02-10

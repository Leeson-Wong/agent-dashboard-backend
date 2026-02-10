# Agent 数据导出功能

> **日期**: 2026-02-10
> **作者**: Claude Sonnet 4.5
> **状态**: ✅ 已完成

---

## 背景

用户需要导出 Agent 数据用于备份、分析或报告。通过添加导出功能，用户可以将 Agent 列表导出为 JSON 或 CSV 格式，方便在 Excel 等工具中查看和处理数据。

**目标**:
1. 添加后端导出 API (JSON 和 CSV 格式)
2. 添加前端导出按钮
3. 支持文件下载
4. CSV 格式支持中文 (UTF-8 BOM)

---

## 实现方案

### 后端修改 (AgentController.java)

#### 1. 添加导入

```java
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
```

#### 2. 添加导出端点

```java
@GetMapping("/export")
public ResponseEntity<byte[]> exportAgents(@RequestParam(defaultValue = "json") String format) {
    log.info("导出 Agent 数据, format={}", format);

    List<AgentState> agents = agentStateMapper.findAll();

    try {
        byte[] data;
        String filename;
        String contentType;

        if ("csv".equalsIgnoreCase(format)) {
            data = generateCsv(agents);
            filename = "agents_" + System.currentTimeMillis() + ".csv";
            contentType = "text/csv; charset=UTF-8";
        } else {
            data = generateJson(agents);
            filename = "agents_" + System.currentTimeMillis() + ".json";
            contentType = "application/json; charset=UTF-8";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);

    } catch (IOException e) {
        log.error("导出失败", e);
        return ResponseEntity.internalServerError().build();
    }
}
```

#### 3. 生成 JSON 格式

```java
private byte[] generateJson(List<AgentState> agents) throws IOException {
    StringBuilder json = new StringBuilder();
    json.append("[\n");

    for (int i = 0; i < agents.size(); i++) {
        AgentState agent = agents.get(i);
        json.append("  {\n");
        json.append("    \"agentId\": \"").append(escapeJson(agent.getAgentId())).append("\",\n");
        json.append("    \"serverId\": \"").append(escapeJson(agent.getServerId())).append("\",\n");
        // ... 更多字段
        json.append("  }").append(i < agents.size() - 1 ? "," : "").append("\n");
    }

    json.append("]");
    return json.toString().getBytes(StandardCharsets.UTF_8);
}
```

#### 4. 生成 CSV 格式

```java
private byte[] generateCsv(List<AgentState> agents) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    // 添加 BOM 以支持 Excel 正确显示中文
    byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    out.write(bom);

    // 写入 CSV 表头
    String header = "Agent ID,Server ID,Framework,Language,Status,Current Activity,Current Tool,Role,Last Activity,Created At,Updated At\n";
    out.write(header.getBytes(StandardCharsets.UTF_8));

    // 写入数据行
    for (AgentState agent : agents) {
        StringBuilder row = new StringBuilder();
        row.append(escapeCsv(agent.getAgentId())).append(",");
        // ... 更多字段
        out.write(row.toString().getBytes(StandardCharsets.UTF_8));
    }

    return out.toByteArray();
}
```

#### 5. 字符串转义

```java
private String escapeJson(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
}

private String escapeCsv(String value) {
    if (value == null) return "";
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
}
```

### 前端修改 (AgentListPanel.vue)

#### 1. 添加导出按钮

```vue
<div class="export-buttons">
  <button
    class="export-btn"
    @click="exportAgents('json')"
    title="导出为 JSON"
  >
    JSON
  </button>
  <button
    class="export-btn"
    @click="exportAgents('csv')"
    title="导出为 CSV"
  >
    CSV
  </button>
</div>
```

#### 2. 添加导出函数

```typescript
const exportAgents = async (format: 'json' | 'csv'): Promise<void> => {
  try {
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const url = `${apiUrl}/api/agents/export?format=${format}`

    const response = await fetch(url)
    if (!response.ok) {
      throw new Error('导出失败')
    }

    // 获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    let filename = `agents_${format}_${Date.now()}.${format}`

    if (contentDisposition) {
      const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (match && match[1]) {
        filename = match[1].replace(/['"]/g, '')
      }
    }

    // 创建下载
    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)

    success(`已导出 ${format.toUpperCase()} 文件`)
  } catch (error) {
    console.error('Export failed:', error)
  }
}
```

#### 3. 添加导出按钮样式

```css
.export-buttons {
  display: flex;
  gap: 4px;
}

.export-btn {
  padding: 6px 10px;
  border: 1px solid rgba(100, 116, 139, 0.3);
  border-radius: 6px;
  background: rgba(51, 65, 85, 0.5);
  color: #94a3b8;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.export-btn:hover {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
  border-color: rgba(34, 197, 94, 0.4);
}
```

---

## 功能说明

### 导出格式

| 格式 | Content-Type | 文件扩展名 | 用途 |
|------|-------------|-----------|------|
| JSON | application/json | .json | 程序处理、API 数据 |
| CSV | text/csv | .csv | Excel、数据分析 |

### 导出字段

| 字段 | 说明 |
|------|------|
| agentId | Agent 唯一标识 |
| serverId | 服务器 ID |
| framework | 框架名称 (CrewAI, LangChain 等) |
| language | 编程语言 |
| status | 状态 (online, offline, error 等) |
| currentActivity | 当前活动描述 |
| currentTool | 当前使用的工具 |
| role | 角色信息 |
| lastActivity | 最后活跃时间 |
| createdAt | 创建时间 |
| updatedAt | 更新时间 |

### CSV 特殊处理

- **UTF-8 BOM**: 添加 BOM (0xEF 0xBB 0xBF) 使 Excel 正确识别中文
- **逗号处理**: 包含逗号的字段用引号包裹
- **引号转义**: 引号转义为两个引号 `"`

---

## API 接口

### GET /api/agents/export

**请求参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| format | string | json | 导出格式: json 或 csv |

**请求示例**:

```bash
# 导出 JSON
curl http://localhost:8080/api/agents/export?format=json -o agents.json

# 导出 CSV
curl http://localhost:8080/api/agents/export?format=csv -o agents.csv
```

**响应**:

```
HTTP/1.1 200
Content-Disposition: attachment; filename="agents_1234567890.json"
Content-Type: application/json;charset=UTF-8
Content-Length: 1234

[...agent data...]
```

---

## UI 效果

### 导出按钮位置

```
┌─────────────────────────────────────────────┐
│ Agent 状态 [6/6]                             │
│ 在线: 4  忙碌: 1  思考: 1  错误: 0           │
├─────────────────────────────────────────────┤
│ [按状态 ▼] [所有框架 ▼] [所有时间 ▼]       │
│ [按状态 ▼] [↑] [▦] [JSON] [CSV]            │  ← 导出按钮
└─────────────────────────────────────────────┘
```

### 导出按钮悬停

```
┌─────────────────────────────────────────────┐
│ [按状态 ▼] [所有框架 ▼] [所有时间 ▼]       │
│ [按状态 ▼] [↑] [▦] [🟢JSON] [🟢CSV]       │  ← 悬停时绿色
└─────────────────────────────────────────────┘
```

---

## 测试步骤

### 1. 后端 API 测试

1. **测试 JSON 导出**:
   ```bash
   curl http://localhost:8080/api/agents/export?format=json
   ```
   预期: 返回 JSON 格式的 Agent 数据

2. **测试 CSV 导出**:
   ```bash
   curl http://localhost:8080/api/agents/export?format=csv
   ```
   预期: 返回 CSV 格式的 Agent 数据

3. **检查响应头**:
   ```bash
   curl -I http://localhost:8080/api/agents/export?format=json
   ```
   预期: 包含 `Content-Disposition: attachment` 和正确的内容类型

### 2. 前端 UI 测试

1. **打开浏览器** 访问 `http://localhost:3000`

2. **检查导出按钮显示**:
   - 预期: 在排序控制区显示 JSON 和 CSV 按钮

3. **测试 JSON 导出**:
   - 点击 "JSON" 按钮
   - 预期: 浏览器下载 `agents_xxx.json` 文件
   - 显示 Toast "已导出 JSON 文件"

4. **测试 CSV 导出**:
   - 点击 "CSV" 按钮
   - 预期: 浏览器下载 `agents_xxx.csv` 文件
   - 显示 Toast "已导出 CSV 文件"

5. **测试 CSV 在 Excel 中打开**:
   - 用 Excel 打开下载的 CSV 文件
   - 预期: 中文正确显示

---

## 测试结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| JSON 导出 API | ✅ 通过 | 正确返回 JSON 格式 |
| CSV 导出 API | ✅ 通过 | 正确返回 CSV 格式 |
| UTF-8 BOM | ✅ 通过 | Excel 正确显示中文 |
| 响应头 | ✅ 通过 | Content-Disposition 正确 |
| JSON 按钮 | ✅ 通过 | 点击下载 JSON 文件 |
| CSV 按钮 | ✅ 通过 | 点击下载 CSV 文件 |
| Toast 通知 | ✅ 通过 | 显示导出成功消息 |
| 按钮样式 | ✅ 完成 | 绿色悬停效果 |

---

## 文件清单

### 修改的文件

1. **agent-dashboard-backend/src/main/java/com/agent/monitor/controller/AgentController.java**
   - 添加导入 (line 10-21)
   - 添加 exportAgents 端点 (line 95-135)
   - 添加 generateJson 方法 (line 137-165)
   - 添加 generateCsv 方法 (line 167-200)
   - 添加 escapeJson 方法 (line 202-212)
   - 添加 escapeCsv 方法 (line 214-224)

2. **agent-dashboard-frontend/src/components/AgentListPanel.vue**
   - 添加导出按钮 (line 82-97)
   - 添加 exportAgents 函数 (line 474-511)
   - 添加导出按钮样式 (line 939-962)

---

## 技术细节

### 文件下载处理

使用 Blob 和 ObjectURL API 实现浏览器文件下载:

```typescript
const blob = await response.blob()
const downloadUrl = window.URL.createObjectURL(blob)
const link = document.createElement('a')
link.href = downloadUrl
link.download = filename
document.body.appendChild(link)
link.click()
document.body.removeChild(link)
window.URL.revokeObjectURL(downloadUrl)
```

### Content-Disposition 解析

从响应头解析文件名:

```typescript
const contentDisposition = response.headers.get('Content-Disposition')
const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
if (match && match[1]) {
  filename = match[1].replace(/['"]/g, '')
}
```

### CSV BOM 处理

添加 UTF-8 BOM 使 Excel 正确识别编码:

```java
byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
out.write(bom);
```

### 字符串转义

#### JSON 转义

- `\\` → `\\\\`
- `"` → `\"`
- `\n` → `\\n`
- `\r` → `\\r`
- `\t` → `\\t`

#### CSV 转义

- 包含 `,`、`"` 或 `\n` 的字段用引号包裹
- 引号转义为两个引号 `"`

---

## 优化建议

### 1. 过滤导出

允许导出筛选后的数据:

```typescript
const exportAgents = async (format: 'json' | 'csv', filteredOnly = false): Promise<void> => {
  const url = filteredOnly
    ? `${apiUrl}/api/agents/export?format=${format}&filtered=true`
    : `${apiUrl}/api/agents/export?format=${format}`
  // ...
}
```

### 2. 字段选择

允许选择导出哪些字段:

```vue
<button @click="exportAgents('json', ['agentId', 'status', 'role'])">
  导出 (选中字段)
</button>
```

### 3. 分页导出

对于大量数据,支持分页导出:

```typescript
const exportAgentsPaginated = async (format: string) => {
  let page = 0
  const pageSize = 1000
  let allData = []

  while (true) {
    const response = await fetch(`${apiUrl}/api/agents?page=${page}&size=${pageSize}`)
    const data = await response.json()
    if (data.length === 0) break
    allData = allData.concat(data)
    page++
  }

  // 导出 allData
}
```

### 4. 后台导出

对于大量数据,使用后台任务导出:

```java
@PostMapping("/export/async")
public ResponseEntity<ApiResponse<String>> exportAsync(@RequestParam String format) {
    String taskId = UUID.randomUUID().toString();
    // 创建后台导出任务
    exportService.submitTask(taskId, format);
    return ResponseEntity.ok(ApiResponse.success(taskId));
}
```

### 5. 导出历史

记录导出历史:

```typescript
const exportHistory = ref<Array<{
  format: string
  filename: string
  timestamp: number
}>>([])
```

### 6. 快捷键支持

添加快捷键快速导出:

```typescript
registerShortcut({
  key: 'e',
  ctrl: true,
  description: '快速导出 JSON',
  handler: () => {
    exportAgents('json')
  },
})
```

---

## 已知问题

无

---

## 参考资料

- **Spring ResponseEntity**: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ResponseEntity.html
- **Blob API**: https://developer.mozilla.org/en-US/docs/Web/API/Blob
- **Object URL**: https://developer.mozilla.org/en-US/docs/Web/API/URL/createObjectURL
- **CSV BOM**: https://en.wikipedia.org/wiki/Byte_order_mark
- **Content-Disposition**: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition

---

**Last Updated**: 2026-02-10
**Status**: ✅ **COMPLETED**

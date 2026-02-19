# API 路径配置规则

## 问题背景

项目出现前端请求后端 API 时出现 404 错误，实际请求 URL 为：
```
http://localhost:8123/api/api/api/chat/history/app/{appId}
```

出现了三个 `/api` 重复，导致路径无法匹配。

## 根本原因

**Spring Boot 全局 `context-path` 与 Controller `@RequestMapping` 路径重复**

### 后端配置
- **application.yml** 中设置了全局 `context-path: /api`
- **ChatHistoryController** 的 `@RequestMapping` 包含了 `/api` 前缀

### 路径计算
```
实际路径 = context-path + @RequestMapping + 方法路径
        = /api + /api/chat/history + /app/{appId}
        = /api/api/chat/history/app/{appId}  ❌ 错误！重复了 /api
```

## 解决方案

### 规则

**当 Spring Boot 配置了 `context-path` 时，Controller 的 `@RequestMapping` 不应再包含该前缀。**

### 正确配置

**application.yml**:
```yaml
server:
  servlet:
    context-path: /api    # 全局路径前缀
```

**Controller (错误)**:
```java
@RequestMapping("/api/chat/history")  // ❌ 不要重复 /api
public class ChatHistoryController {
}
```

**Controller (正确)**:
```java
@RequestMapping("/chat/history")   // ✅ 只写相对路径
public class ChatHistoryController {
}
```

### 前端配置

**.env.development**:
```env
VITE_API_BASE_URL=/api
```

**vite.config.ts**:
```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8123',
      changeOrigin: true,
    },
  },
}
```

## 完整路径映射

| 后端组件 | 路径配置 |
|---------|----------|
| context-path | `/api` |
| @RequestMapping | `/chat/history` |
| @GetMapping | `/app/{appId}` |
| **后端完整路径** | `/api/chat/history/app/{appId}` |

| 前端组件 | 路径配置 |
|---------|----------|
| baseURL | `/api` |
| 请求路径 | `/chat/history/app/{appId}` |
| Vite 代理目标 | `http://localhost:8123` |
| **最终请求** | `http://localhost:8123/api/chat/history/app/{appId}` |

## 开发规范

1. **新增 Controller 时**：
   - 检查 `application.yml` 中的 `context-path`
   - `@RequestMapping` 不要包含 `context-path` 的值
   - 只写相对于 `context-path` 的路径

2. **路径设计原则**：
   ```
   context-path: /api           (全局，所有接口共用)
   @RequestMapping: /chat/history (模块/控制器级别)
   @GetMapping: /app/{appId}     (具体接口)

   完整路径: /api/chat/history/app/{appId}
   ```

3. **前端 API 生成**：
   - OpenAPI/Springdoc 会自动处理 `context-path`
   - 前端生成的路径不包含 `context-path`
   - 前端通过 `baseURL` 补充完整的路径前缀

## 相关文件

- 后端配置: `src/main/resources/application.yml`
- Controller 示例: `src/main/java/com/example/usercenterpractice/controller/ChatHistoryController.java`
- 前端配置: `EcomAIGen-fronted/.env.development`
- Vite 代理: `EcomAIGen-fronted/vite.config.ts`

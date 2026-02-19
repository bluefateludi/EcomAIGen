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

---

## 🔴 如何避免再犯（重要！）

### 1. 开发前必查清单

创建新 Controller 前，**必须**先检查：

```bash
# 1. 查看 context-path 配置
grep "context-path" src/main/resources/application.yml
```

**如果看到 `context-path: /api`，那么：**
- ❌ `@RequestMapping("/api/xxx")` → 错误！
- ✅ `@RequestMapping("/xxx")` → 正确！

---

### 2. 代码审查要点

**审查 Controller 时，重点检查：**

```java
// ❌ 错误示例
@RestController
@RequestMapping("/api/chat/history")  // 重复了 /api
public class ChatHistoryController {
    @GetMapping("/app/{appId}")
    public BaseResponse<?> getHistory(...) {
        // 实际路径变成: /api/api/chat/history/app/{appId}
    }
}

// ✅ 正确示例
@RestController
@RequestMapping("/chat/history")   // 只写相对路径
public class ChatHistoryController {
    @GetMapping("/app/{appId}")
    public BaseResponse<?> getHistory(...) {
        // 实际路径变成: /api/chat/history/app/{appId}
    }
}
```

---

### 3. 快速验证方法

**写完 Controller 后，立即验证路径是否正确：**

```bash
# 方法1：查看 Knife4j 文档
# 打开 http://localhost:8123/doc.html
# 检查接口路径是否只有一个 /api 前缀

# 方法2：直接访问测试
curl http://localhost:8123/api/你的控制器路径/方法路径
```

**判断标准：**
- 正确路径应该只有**一个** `/api` 前缀
- 如果看到 `/api/api/...`，说明配置错了！

---

### 4. 记忆口诀

```
全局前缀 context-path，
Controller 路径不要加！
前后端各一半，
拼接起来才对啦。
```

或者更简单：

> **"Context-path 已有 /api，@RequestMapping 别再加！"**

---

### 5. 常见错误模式

| 错误模式 | 说明 | 正确做法 |
|---------|------|----------|
| `@RequestMapping("/api/user")` | 与 context-path 重复 | `@RequestMapping("/user")` |
| `@RequestMapping("/api")` | 完全重复 context-path | `@RequestMapping("")` 或直接在类上省略 |
| 前端请求 `/api/api/xxx` | 前端多写了前缀 | 前端只写 `/api/xxx` |

---

### 6. 代码注释规范

**在 Controller 上添加清晰的注释，提醒其他开发者：**

```java
/**
 * 对话历史控制器
 *
 * 注意：由于 application.yml 配置了 context-path: /api
 * 此处 @RequestMapping 不要包含 /api 前缀
 *
 * 实际访问路径：/api/chat/history/xxx
 */
@RestController
@RequestMapping("/chat/history")  // ✅ 不带 /api 前缀
public class ChatHistoryController {
    // ...
}
```

---

### 7. 自动化检测（可选）

**可以在项目中添加单元测试，自动检测路径配置错误：**

```java
@Test
public void testNoDuplicateApiPrefix() {
    // 检查所有 Controller 的 @RequestMapping 不应包含 "/api"
    List<Class<?>> controllers = Arrays.stream(new Reflections("com.example.usercenterpractice")
            .getTypesAnnotatedWith(RestController.class))
            .toList();

    for (Class<?> controller : controllers) {
        RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
        if (mapping != null) {
            String[] paths = mapping.value();
            for (String path : paths) {
                assertFalse(path.startsWith("/api"),
                    "Controller " + controller.getSimpleName() +
                    " 的 @RequestMapping 不应包含 /api 前缀，context-path 已配置");
            }
        }
    }
}
```

---

## 开发规范

1. **新增 Controller 时**：
   - ✅ **必查** `application.yml` 中的 `context-path`
   - ✅ `@RequestMapping` 只写**相对路径**
   - ✅ 写完立即在 Knife4j 验证路径

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

## 快速参考卡

```
┌─────────────────────────────────────────────────────────┐
│  创建新 Controller 的步骤                                │
├─────────────────────────────────────────────────────────┤
│  1. grep "context-path" application.yml                 │
│     → 看到 context-path: /api                           │
│                                                          │
│  2. 写 @RequestMapping 时                                │
│     → ✅ @RequestMapping("/user")                        │
│     → ❌ @RequestMapping("/api/user")  ← 别这样！        │
│                                                          │
│  3. 写完后立即验证                                       │
│     → 打开 Knife4j (doc.html)                            │
│     → 检查路径只有一个 /api                               │
└─────────────────────────────────────────────────────────┘
```

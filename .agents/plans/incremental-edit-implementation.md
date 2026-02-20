# HTML/Multi-File 增量编辑功能实现计划

## Context

**问题：** 当前 HTML 和 Multi-File 类型的编辑模式会覆盖整个文件，而不是在原有内容上进行增量修改。用户通过可视化编辑器选中元素后，希望 AI 只修改被选中的部分，而不是重新生成整个文件。

**现状：**
- Vue 项目已有 FileModifyTool，支持增量修改
- HTML/Multi-File 类型每次都完整覆盖文件
- 前端已传递选中元素信息（selector, tagName, textContent），但后端未利用

**目标：** 让 HTML/Multi-File 类型也支持增量编辑，只修改用户选中的元素部分。

---

## Implementation Plan

### Step 1: 为 HTML/Multi-File 添加工具支持

**文件:** `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java`

**当前代码 (第 114-153 行):**
- `createAiCodeGeneratorService(long appId)` - 只有 HTML/Multi-File，没有工具
- `createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType)` - 只有 VUE_PROJECT 有工具

**修改内容:**
```java
private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
    // ... existing code ...

    // 为所有类型添加工具支持
    ToolSpecification[] toolSpecifications = ToolSpecifications.builder()
        .add(new FileReadTool(redisChatMemoryStore))
        .add(new FileWriteTool(redisChatMemoryStore))
        .add(new FileModifyTool(redisChatMemoryStore))
        .add(new FileDirReadTool(redisChatMemoryStore))
        .add(new FileDeleteTool(redisChatMemoryStore))
        .build();

    // ... rest of existing code with tools enabled ...
}
```

**关键点:** 确保 HTML/Multi-File 类型也能使用 FileModifyTool

---

### Step 2: 修改 HTML 和 Multi-File 系统提示词

**文件 1:** `src/main/resources/Prompt/codegen-html-system-prompt.txt`

**在文件末尾添加:**
```
## 编辑模式（使用工具）

当用户要求修改已生成的网页时：
1. 首先使用【目录读取工具】查看项目结构
2. 使用【文件读取工具】读取 index.html 文件内容
3. 使用【文件修改工具】精确修改用户要求的部分：
   - 找到要修改的元素或代码段
   - 只替换需要修改的部分
   - 保持其他内容不变

**重要**: 不要重新输出整个文件，只修改需要的部分。
```

**文件 2:** `src/main/resources/Prompt/codegen-multi-file-system-prompt.txt`

**在文件末尾添加:**
```
## 编辑模式（使用工具）

当用户要求修改已生成的网页时：
1. 首先使用【目录读取工具】查看项目结构
2. 使用【文件读取工具】读取需要修改的文件（index.html, style.css, script.js）
3. 使用【文件修改工具】精确修改用户要求的部分：
   - 找到要修改的元素或代码段
   - 只替换需要修改的部分
   - 保持其他内容不变

**重要**: 不要重新输出所有文件，只修改需要的部分。
```

---

### Step 3: 后端读取现有文件并注入到用户消息

**文件:** `src/main/java/com/example/usercenterpractice/service/impl/AppServiceImpl.java`

**修改 `wrapMessageWithIntent` 方法:**

```java
private String wrapMessageWithIntent(Long appId, String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
    // Vue 类型使用工具系统，不需要注入代码
    if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
        return userMessage;
    }

    // HTML 和 Multi-File 类型：判断是否有历史对话
    long aiResponseCount = chatHistoryService.count(
        new QueryWrapper<com.example.usercenterpractice.model.domain.ChatHistory>()
            .eq("appId", appId)
            .eq("messageType", ChatHistoryMessageTypeEnum.AI.getValue())
    );

    // 如果没有历史记录，这是首次生成 - 直接返回原始消息
    if (aiResponseCount == 0) {
        return userMessage;
    }

    // 有历史记录，判断是聊天还是编辑意图（使用优先级规则）
    if (isChatIntent(userMessage)) {
        // 聊天意图：添加"只回答不输出代码"的指令
        return "用户在问问题，请直接回答问题即可，不需要输出任何代码。\n\n用户问题：" + userMessage;
    } else {
        // 编辑意图：注入现有代码，让 AI 基于现有代码进行修改
        return injectExistingCodeForEdit(appId, userMessage, codeGenTypeEnum);
    }
}

/**
 * 简单的意图判断 - 判断用户消息是否属于聊天意图
 * 使用优先级规则：编辑关键词 > 聊天关键词
 * 避免将 "怎么改成红色" 误判为聊天意图
 *
 * @param message 用户消息
 * @return true 表示聊天意图，false 表示编辑意图
 */
private boolean isChatIntent(String message) {
    // 1. 优先检查明确的编辑意图关键词
    if (hasEditIntent(message)) {
        return false;  // 编辑意图
    }

    // 2. 再检查聊天意图关键词
    if (hasChatIntent(message)) {
        return true;   // 聊天意图
    }

    // 3. 默认为编辑意图（保守策略）
    return false;
}

/**
 * 检查是否包含编辑意图
 */
private boolean hasEditIntent(String message) {
    String[] editKeywords = {
        "改成", "修改为", "变成", "替换成", "设置成",
        "改为", "更新为", "调整成", "改成", "弄成"
    };
    String lowerMessage = message.toLowerCase();
    for (String keyword : editKeywords) {
        if (lowerMessage.contains(keyword)) {
            return true;
        }
    }
    return false;
}

/**
 * 检查是否包含聊天意图
 */
private boolean hasChatIntent(String message) {
    // 纯询问类关键词（不包含"怎么"这种可能有歧义的词）
    String[] chatKeywords = {
        "是什么", "做什么", "有什么功能", "如何使用",
        "解释一下", "介绍一下", "告诉我", "说明", "作用",
        "是什么意思", "为什么有", "好", "怎么样", "可以吗"
    };
    String lowerMessage = message.toLowerCase();
    for (String keyword : chatKeywords) {
        if (lowerMessage.contains(keyword)) {
            return true;
        }
    }
    return false;
}

/**
 * 注入现有代码到用户消息中（用于编辑模式）
 */
private String injectExistingCodeForEdit(Long appId, String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
    String codeGenType = codeGenTypeEnum.getValue();
    String sourceDirName = codeGenType + "_" + appId;
    String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

    StringBuilder injectedMessage = new StringBuilder();
    injectedMessage.append("【编辑模式】用户要求修改已生成的网页。\n\n");
    injectedMessage.append("用户要求：").append(userMessage).append("\n\n");

    try {
        if (codeGenTypeEnum == CodeGenTypeEnum.HTML) {
            // HTML 类型：读取 index.html
            File htmlFile = new File(sourceDirPath, "index.html");
            if (htmlFile.exists()) {
                String existingCode = Files.readString(htmlFile.toPath());
                injectedMessage.append("当前代码：\n```html\n").append(existingCode).append("\n```\n\n");
                injectedMessage.append("请使用【文件修改工具】修改文件，只修改用户要求的部分。");
            } else {
                injectedMessage.append("（文件不存在，请生成新文件）");
            }
        } else if (codeGenTypeEnum == CodeGenTypeEnum.MULTI_FILE) {
            // Multi-File 类型：读取三个文件
            File htmlFile = new File(sourceDirPath, "index.html");
            File cssFile = new File(sourceDirPath, "style.css");
            File jsFile = new File(sourceDirPath, "script.js");

            if (htmlFile.exists()) {
                injectedMessage.append("当前 index.html：\n```html\n").append(Files.readString(htmlFile.toPath())).append("\n```\n\n");
            }
            if (cssFile.exists()) {
                injectedMessage.append("当前 style.css：\n```css\n").append(Files.readString(cssFile.toPath())).append("\n```\n\n");
            }
            if (jsFile.exists()) {
                injectedMessage.append("当前 script.js：\n```javascript\n").append(Files.readString(jsFile.toPath())).append("\n```\n\n");
            }
            injectedMessage.append("请使用【文件修改工具】修改需要修改的文件，只修改用户要求的部分。");
        }
    } catch (IOException e) {
        log.error("读取现有代码失败: {}", e.getMessage());
        injectedMessage.append("（读取现有文件失败，请重新生成）");
    }

    return injectedMessage.toString();
}
```

---

### Step 4: 修改 AiCodeGeneratorFacade 以处理工具调用

**文件:** `src/main/java/com/example/usercenterpractice/ai/core/AiCodeGeneratorFacade.java`

**修改 `generateAndSaveCodeStream` 方法:**

```java
public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
    if (codeGenTypeEnum == null) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
    }

    // 根据 appId 动态获取 AI 服务实例
    AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

    return switch (codeGenTypeEnum) {
        case HTML -> {
            TokenStream tokenStream = aiCodeGeneratorService.generateHtmlCodeStreamWithTools(userMessage);
            yield processTokenStreamWithTools(tokenStream, appId, codeGenTypeEnum);
        }
        case MULTI_FILE -> {
            TokenStream tokenStream = aiCodeGeneratorService.generateMultiFileCodeStreamWithTools(userMessage);
            yield processTokenStreamWithTools(tokenStream, appId, codeGenTypeEnum);
        }
        case VUE_PROJECT -> {
            TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
            yield processTokenStream(tokenStream, appId);
        }
        default -> {
            String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
        }
    };
}

/**
 * 处理带工具调用的 TokenStream（用于 HTML/Multi-File 编辑模式）
 */
private Flux<String> processTokenStreamWithTools(TokenStream tokenStream, Long appId, CodeGenTypeEnum codeGenTypeEnum) {
    return Flux.create(sink -> {
        tokenStream.onPartialResponse((String partialResponse) -> {
            // 普通文本响应直接返回
            sink.next(partialResponse);
        })
        .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
            // 工具调用请求：返回工具信息
            ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
            sink.next(JSONUtil.toJsonStr(toolRequestMessage));
        })
        .onToolExecuted((ToolExecution toolExecution) -> {
            // 工具执行结果：返回工具结果
            ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
            sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
        })
        .onCompleteResponse((ChatResponse response) -> {
            // 完成后不需要额外操作（工具已经处理了文件保存）
            sink.complete();
        })
        .onError((Throwable error) -> {
            log.error("TokenStream 处理错误", error);
            sink.error(error);
        })
        .start();
    });
}
```

---

### Step 5: 更新 AiCodeGeneratorService 接口

**文件:** `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorService.java`

**添加新方法:**

```java
/**
 * 生成 HTML 代码（流式，支持工具）
 */
@SystemMessage(fromResource = "Prompt/codegen-html-system-prompt.txt")
TokenStream generateHtmlCodeStreamWithTools(@MemoryId long appId, @UserMessage String userMessage);

/**
 * 生成多文件代码（流式，支持工具）
 */
@SystemMessage(fromResource = "Prompt/codegen-multi-file-system-prompt.txt")
TokenStream generateMultiFileCodeStreamWithTools(@MemoryId long appId, @UserMessage String userMessage);
```

---

## Critical Files Reference

| 文件路径 | 作用 | 修改类型 |
|---------|------|---------|
| `ai/AiCodeGeneratorServiceFactory.java:114-153` | AI 服务工厂，需要添加工具支持 | 修改 |
| `ai/AiCodeGeneratorService.java` | AI 服务接口，添加新方法 | 修改 |
| `ai/core/AiCodeGeneratorFacade.java:73-105` | 生成门面，添加工具处理逻辑 | 修改 |
| `service/impl/AppServiceImpl.java:373-446` | 消息包装，添加代码注入逻辑 | 修改 |
| `resources/Prompt/codegen-html-system-prompt.txt` | HTML 系统提示词 | 添加编辑模式说明 |
| `resources/Prompt/codegen-multi-file-system-prompt.txt` | Multi-File 系统提示词 | 添加编辑模式说明 |
| `ai/tools/FileModifyTool.java` | 文件修改工具（已存在，复用） | 不修改 |
| `ai/handler/JsonMessageStreamHandler.java` | 工具流处理器（已存在，复用） | 不修改 |

---

## Verification

### 测试步骤

1. **创建 HTML 应用:**
   - 输入 "生成一个登录页面"
   - 验证：生成 index.html 文件

2. **编辑 HTML 应用:**
   - 选中标题元素（通过可视化编辑器）
   - 输入 "把标题改成红色"
   - 验证：
     - AI 使用 FileModifyTool 修改文件
     - 只有标题颜色被修改
     - 其他内容保持不变

3. **创建 Multi-File 应用:**
   - 重复上述测试步骤
   - 验证增量修改功能正常

4. **聊天模式测试:**
   - 输入 "这个按钮是什么意思？"
   - 验证：只返回文字，不使用工具

5. **混合意图测试（新增）:**
   - 输入 "把标题改成红色可以吗？" → 编辑模式，使用工具修改
   - 输入 "怎么改成红色" → 编辑模式，使用工具修改（不是聊天！）
   - 输入 "标题改成什么颜色好？" → 聊天模式，只返回建议

---

## Notes

### 前置条件

1. **Redis 缓存问题需要先修复** - `listGoodAppVOByPage` 方法的 `@Cacheable` 注解导致的 ClassCastException 问题

2. **依赖 LangChain4J 工具系统** - 确保 HTML/Multi-File 类型能正确使用工具

### 潜在风险

1. **AI 可能不正确使用工具** - 需要完善提示词
2. **FileModifyTool 的精确匹配问题** - 需要精确匹配旧内容
3. **并发文件操作** - 多次编辑可能产生冲突

### 意图识别优化（新增）

**问题：** 简单的关键词匹配可能误判用户意图

| 场景 | 用户输入 | 简单匹配结果 | 优化后结果 |
|------|---------|-------------|-------------|
| 编辑 | "把标题改成红色" | 聊天 ❌ | 编辑 ✅ |
| 编辑+咨询 | "改成红色可以吗？" | 聊天 ❌ | 编辑 ✅ |
| 编辑+咨询 | "怎么改成红色" | 聊天 ❌ | 编辑 ✅ |
| 纯咨询 | "改成什么颜色好？" | 聊天 ✅ | 聊天 ✅ |
| 纯咨询 | "这个按钮是什么？" | 聊天 ✅ | 聊天 ✅ |

**优化策略：优先级规则**
1. 先检查编辑关键词（改成、修改为、变成等）
2. 再检查聊天关键词（是什么、做什么、怎么用等）
3. 编辑关键词优先于聊天关键词

### 回滚方案

如果工具系统不稳定，可以禁用工具，回退到之前的"注入现有代码"方案：
- 修改 `injectExistingCodeForEdit` 方法
- 让 AI 生成完整代码，然后使用字符串替换实现增量修改

# 增量编辑功能问题分析与修复计划

## 问题概述

当前您的项目在执行增量编辑功能时出现问题。通过对比您的项目和教程源码（yu-ai-code-mother），发现了以下关键差异。

---

## 关键差异对比

### 1. AiCodeGeneratorServiceFactory.java - 工具配置差异 ⚠️ **关键问题**

#### 您的项目 (user-centerpractice)
**文件**: `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java`

**VUE_PROJECT 配置** (第 127-140 行):
```java
case VUE_PROJECT -> {
    StreamingChatModel streamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
    yield AiServices.builder(AiCodeGeneratorService.class)
            .streamingChatModel(streamingChatModel)
            .chatMemoryProvider(memoryId -> chatMemory)
            .tools(toolManager.getAllTools())
            .inputGuardrails(new PromptSafetyInputGuardrail())
            .hallucinatedToolNameStrategy(...)
            .build();
}
```

**HTML/MULTI_FILE 配置** (第 145-153 行):
```java
case HTML, MULTI_FILE -> {
    StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
    yield AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)
            .streamingChatModel(openAiStreamingChatModel)
            .chatMemory(chatMemory)
            .build();  // ❌ 没有工具，没有 maxSequentialToolsInvocations
}
```

#### 教程项目 (yu-ai-code-mother)
**文件**: `src/main/java/com/yupi/yuaicodemother/ai/AiCodeGeneratorServiceFactory.java`

**VUE_PROJECT 配置** (第 103-119 行):
```java
case VUE_PROJECT -> {
    StreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
    yield AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)  // ✅ 同时设置 chatModel
            .streamingChatModel(reasoningStreamingChatModel)
            .chatMemoryProvider(memoryId -> chatMemory)
            .tools(toolManager.getAllTools())
            .hallucinatedToolNameStrategy(...)
            .maxSequentialToolsInvocations(20)  // ✅ 限制最多连续调用 20 次工具
            .inputGuardrails(new PromptSafetyInputGuardrail())
            .build();
}
```

**HTML/MULTI_FILE 配置** (第 122-132 行):
```java
case HTML, MULTI_FILE -> {
    StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
    yield AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)  // ✅ 设置了 chatModel
            .streamingChatModel(openAiStreamingChatModel)
            .chatMemory(chatMemory)
            .inputGuardrails(new PromptSafetyInputGuardrail())  // ✅ 添加了输入护轨
            .build();
}
```

---

### 2. JsonMessageStreamHandler.java - AI 响应保存差异 ⚠️ **重要**

#### 您的项目 (user-centerpractice)
**文件**: `src/main/java/com/example/usercenterpractice/ai/handler/JsonMessageStreamHandler.java`

**第 59-66 行**:
```java
.doOnComplete(() -> {
   // 流式响应完成后，添加 AI 消息到对话历史
//                    String aiResponse = chatHistoryStringBuilder.toString();
//                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
    // ❌ AI 响应保存代码被注释掉了！
    // 异步构造 Vue 项目
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
    vueProjectBuilder.buildProjectAsync(projectPath);
})
```

#### 教程项目 (yu-ai-code-mother)
**文件**: `src/main/java/com/yupi/yuaicodemother/core/handler/JsonMessageStreamHandler.java`

**第 54-58 行**:
```java
.doOnComplete(() -> {
    // 流式响应完成后，添加 AI 消息到对话历史
    String aiResponse = chatHistoryStringBuilder.toString();
    // ✅ 正确保存 AI 响应到对话历史
    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
})
```

---

### 3. AppServiceImpl.java - 消息包装逻辑差异 ⚠️ **核心问题**

#### 您的项目 (user-centerpractice)
**文件**: `src/main/java/com/example/usercenterpractice/service/impl/AppServiceImpl.java`

**chatToGenCode 方法** (第 163-188 行):
```java
public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
    // ... 参数校验 ...
    // 5. 通过校验后，添加用户消息到对话历史
    chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
    // 6. 调用 AI 生成代码（流式）
    // ❌ 直接使用原始 message，没有消息包装逻辑
    Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    // ...
}
```

**缺失的方法**:
- ❌ 没有 `wrapMessageWithIntent` 方法
- ❌ 没有 `isChatIntent` 方法
- ❌ 没有 `hasEditIntent` 方法
- ❌ 没有 `injectExistingCodeForEdit` 方法

#### 教程项目 (yu-ai-code-mother)
**文件**: `src/main/java/com/yupi/yuaicodemother/service/impl/AppServiceImpl.java`

**chatToGenCode 方法** (第 77-100 行):
```java
public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
    // ... 参数校验 ...
    // 5. 在调用 AI 前，先保存用户消息到数据库中
    chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
    // 6. 调用 AI 生成代码（流式）
    // ✅ 注意：教程项目也没有在这里包装消息
    // 但是教程项目的工具系统配置更完善
    Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    // ...
}
```

---

### 4. 系统提示词差异

#### 您的项目
**文件**: `src/main/resources/Prompt/codegen-html-system-prompt.txt`

- ❌ **缺少编辑模式说明**
- 没有关于使用工具进行增量修改的指令

#### 需要添加的内容
根据 `incremental-edit-implementation.md` 计划，应该添加：

```txt
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

---

## 根本原因分析

### 增量编辑失败的主要原因

1. **HTML/MULTI_FILE 类型没有工具支持** 🔴 **最关键**
   - 您的项目中，HTML 和 MULTI_FILE 类型的 AI 服务没有配置工具
   - AI 无法调用 FileModifyTool 进行增量修改
   - 这导致 AI 只能重新生成整个文件

2. **缺少 `maxSequentialToolsInvocations` 配置**
   - 没有限制工具调用次数，可能导致 AI 无限循环调用工具
   - 教程项目设置为 20 次

3. **AI 响应未保存到对话历史**
   - `JsonMessageStreamHandler` 中保存 AI 响应的代码被注释
   - 影响对话上下文的连续性

4. **系统提示词缺少编辑模式说明**
   - AI 不知道在编辑模式应该如何使用工具

---

## 修复计划

### 修复优先级

| 优先级 | 修复项 | 文件 | 预计工作量 |
|-------|--------|------|-----------|
| **P0** | HTML/MULTI_FILE 添加工具支持 | `AiCodeGeneratorServiceFactory.java` | 10 分钟 |
| **P0** | 添加 maxSequentialToolsInvocations | `AiCodeGeneratorServiceFactory.java` | 5 分钟 |
| **P1** | 取消注释 AI 响应保存代码 | `JsonMessageStreamHandler.java` | 2 分钟 |
| **P1** | 更新系统提示词 | `codegen-html-system-prompt.txt` | 5 分钟 |
| **P2** | 添加消息包装逻辑 | `AppServiceImpl.java` | 30 分钟 |

### 修复步骤

#### Step 1: 修复 AiCodeGeneratorServiceFactory.java ⚠️ **必须**

**文件**: `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java`

**修改位置**: 第 145-153 行

**当前代码**:
```java
case HTML, MULTI_FILE -> {
    // 使用多例模式的 StreamingChatModel 解决并发问题
    StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
    yield AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)
            .streamingChatModel(openAiStreamingChatModel)
            .chatMemory(chatMemory)
            .build();
}
```

**修改为**:
```java
case HTML, MULTI_FILE -> {
    // 使用多例模式的 StreamingChatModel 解决并发问题
    StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
    yield AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)
            .streamingChatModel(openAiStreamingChatModel)
            .chatMemory(chatMemory)
            .tools(toolManager.getAllTools())  // ✅ 添加工具支持
            .maxSequentialToolsInvocations(20)  // ✅ 限制工具调用次数
            .inputGuardrails(new PromptSafetyInputGuardrail())  // ✅ 添加输入护轨
            .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                    toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
            ))  // ✅ 处理工具调用幻觉
            .build();
}
```

**同时修复 VUE_PROJECT 配置** (第 127-140 行):
```java
case VUE_PROJECT -> {
    // 使用多例模式的 StreamingChatModel 解决并发问题
    StreamingChatModel streamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
    yield AiServices.builder(AiCodeGeneratorService.class)
            .chatModel(chatModel)  // ✅ 添加 chatModel
            .streamingChatModel(streamingChatModel)
            .chatMemoryProvider(memoryId -> chatMemory)
            .tools(toolManager.getAllTools())
            .maxSequentialToolsInvocations(20)  // ✅ 添加此配置
            .inputGuardrails(new PromptSafetyInputGuardrail())
            .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                    toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
            ))
            .build();
}
```

---

#### Step 2: 修复 JsonMessageStreamHandler.java

**文件**: `src/main/java/com/example/usercenterpractice/ai/handler/JsonMessageStreamHandler.java`

**修改位置**: 第 59-66 行

**当前代码**:
```java
.doOnComplete(() -> {
   // 流式响应完成后，添加 AI 消息到对话历史
//                    String aiResponse = chatHistoryStringBuilder.toString();
//                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
    // 异步构造 Vue 项目
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
    vueProjectBuilder.buildProjectAsync(projectPath);
})
```

**修改为**:
```java
.doOnComplete(() -> {
    // 流式响应完成后，添加 AI 消息到对话历史
    String aiResponse = chatHistoryStringBuilder.toString();
    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
    // 异步构造 Vue 项目
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
    vueProjectBuilder.buildProjectAsync(projectPath);
})
```

---

#### Step 3: 更新系统提示词

**文件**: `src/main/resources/Prompt/codegen-html-system-prompt.txt`

**在文件末尾添加**:
```txt

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

**文件**: `src/main/resources/Prompt/codegen-multi-file-system-prompt.txt`

**在文件末尾添加**:
```txt

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

#### Step 4: （可选）添加消息包装逻辑

这是 `incremental-edit-implementation.md` 计划中提到的完整实现，但根据教程源码对比，**这可能不是必须的**。

如果完成上述 Step 1-3 后增量编辑仍有问题，可以考虑添加此逻辑。

**文件**: `src/main/java/com/example/usercenterpractice/service/impl/AppServiceImpl.java`

**添加方法**:
```java
/**
 * 包装用户消息，根据对话历史判断意图
 */
private String wrapMessageWithIntent(Long appId, String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
    // Vue 类型使用工具系统，不需要注入代码
    if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
        return userMessage;
    }

    // HTML 和 Multi-File 类型：判断是否有历史对话
    long aiResponseCount = chatHistoryService.count(
        new QueryWrapper<>()
            .eq("appId", appId)
            .eq("messageType", ChatHistoryMessageTypeEnum.AI.getValue())
    );

    // 如果没有历史记录，这是首次生成 - 直接返回原始消息
    if (aiResponseCount == 0) {
        return userMessage;
    }

    // 有历史记录，判断是聊天还是编辑意图
    if (isChatIntent(userMessage)) {
        // 聊天意图：添加"只回答不输出代码"的指令
        return "用户在问问题，请直接回答问题即可，不需要输出任何代码。\n\n用户问题：" + userMessage;
    } else {
        // 编辑意图：返回原始消息，让工具系统处理
        return userMessage;
    }
}

/**
 * 判断是否是聊天意图
 */
private boolean isChatIntent(String message) {
    // 优先检查编辑意图
    if (hasEditIntent(message)) {
        return false;
    }
    // 检查聊天意图
    return hasChatIntent(message);
}

private boolean hasEditIntent(String message) {
    String[] editKeywords = {"改成", "修改为", "变成", "替换成", "设置成"};
    String lowerMessage = message.toLowerCase();
    for (String keyword : editKeywords) {
        if (lowerMessage.contains(keyword)) {
            return true;
        }
    }
    return false;
}

private boolean hasChatIntent(String message) {
    String[] chatKeywords = {"是什么", "做什么", "有什么功能", "解释一下"};
    String lowerMessage = message.toLowerCase();
    for (String keyword : chatKeywords) {
        if (lowerMessage.contains(keyword)) {
            return true;
        }
    }
    return false;
}
```

**修改 chatToGenCode 方法**:
```java
// 6. 包装用户消息
String wrappedMessage = wrapMessageWithIntent(appId, message, codeGenTypeEnum);
// 7. 调用 AI 生成代码（流式）
Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(wrappedMessage, codeGenTypeEnum, appId);
```

---

## 验证测试步骤

### 测试 1: HTML 应用增量编辑

1. 创建一个新的 HTML 应用
   ```
   输入: "生成一个登录页面，包含用户名和密码输入框"
   ```

2. 等待首次生成完成

3. 进行增量编辑
   ```
   输入: "把登录按钮改成红色"
   ```

4. 验证结果：
   - ✅ AI 应该使用 FileModifyTool 修改按钮颜色
   - ✅ 其他内容保持不变
   - ✅ 不应该重新生成整个文件

### 测试 2: Multi-File 应用增量编辑

1. 创建一个新的 Multi-File 应用
2. 进行增量编辑
3. 验证工具调用正确

### 测试 3: 聊天模式测试

```
输入: "这个登录页面有什么功能？"
```

验证: AI 只返回文字说明，不使用工具

---

## 总结

### 必须修复的问题（P0）
1. ✅ 为 HTML/MULTI_FILE 添加 `.tools(toolManager.getAllTools())`
2. ✅ 添加 `.maxSequentialToolsInvocations(20)`
3. ✅ 添加 `.inputGuardrails(new PromptSafetyInputGuardrail())`
4. ✅ 添加 `.hallucinatedToolNameStrategy(...)`

### 建议修复的问题（P1）
5. ✅ 取消注释 JsonMessageStreamHandler 中的 AI 响应保存代码
6. ✅ 更新系统提示词，添加编辑模式说明

### 可选的增强（P2）
7. 添加消息包装和意图识别逻辑

---

## 相关文件清单

| 文件 | 修改类型 | 优先级 |
|------|---------|--------|
| `ai/AiCodeGeneratorServiceFactory.java` | 修改 | P0 |
| `ai/handler/JsonMessageStreamHandler.java` | 修改 | P1 |
| `resources/Prompt/codegen-html-system-prompt.txt` | 添加 | P1 |
| `resources/Prompt/codegen-multi-file-system-prompt.txt` | 添加 | P1 |
| `service/impl/AppServiceImpl.java` | 添加方法 | P2 |

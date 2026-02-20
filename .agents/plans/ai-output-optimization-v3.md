# AI 输出优化方案 - 针对性版本

## 问题分析

**核心问题：**
- HTML 和 Multi-File 类型：每次都输出完整代码，Token 浪费严重
- Vue 类型：已有工具系统，增量修改效率高，无需优化

**为什么 Vue 不需要优化：**
- Vue 提示词要求 AI 使用工具增量修改文件（第 142-147 行）
- 工具调用（FileModifyTool）本身就很高效
- 只修改受影响的文件，不重新输出整个项目

**真正需要优化的是 HTML 和 Multi-File：**
- 没有工具系统，每次对话都返回完整代码
- 聊天问题（如"这个按钮是什么？"）也返回完整代码 ❌

---

## 解决方案

### 核心思路

**只针对 HTML 和 Multi-File 类型优化：**
1. **聊天模式**：添加前缀指令，让 AI 只回答不输出代码
2. **编辑模式**：保持原有行为，输出完整代码（因为没有工具系统）
3. **Vue 类型**：完全不动，保持原有工具系统

---

## Implementation Steps

### Step 1: 添加意图判断和消息包装方法

**文件：** `src/main/java/com/example/usercenterpractice/service/impl/AppServiceImpl.java`

**位置：** 在 `AppServiceImpl` 类中添加两个私有方法（在 `copyDirectory` 方法后）

```java
/**
 * 构建带意图前缀的用户消息
 * 只对 HTML 和 Multi-File 类型进行优化，Vue 类型保持原样
 *
 * @param appId 应用ID
 * @param userMessage 用户原始消息
 * @param codeGenTypeEnum 代码生成类型
 * @return 包装后的消息
 */
private String wrapMessageWithIntent(Long appId, String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
    // Vue 类型使用工具系统，不需要优化
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

    // 有历史记录，判断是聊天还是编辑意图
    if (isChatIntent(userMessage)) {
        // 聊天意图：添加"只回答不输出代码"的指令
        return "用户在问问题，请直接回答问题即可，不需要输出任何代码。\n\n用户问题：" + userMessage;
    } else {
        // 编辑意图：直接返回原始消息，让 AI 按照提示词要求输出完整代码
        return userMessage;
    }
}

/**
 * 简单的意图判断 - 判断用户消息是否属于聊天意图
 *
 * @param message 用户消息
 * @return true 表示聊天意图，false 表示编辑意图
 */
private boolean isChatIntent(String message) {
    // 聊天类关键词
    String[] chatKeywords = {
        "什么", "怎么", "为什么", "如何", "解释",
        "说明", "介绍", "告诉我", "是什么意思", "作用", "功能",
        "是什么", "做什么", "为什么有", "怎么用"
    };

    String lowerMessage = message.toLowerCase();
    for (String keyword : chatKeywords) {
        if (lowerMessage.contains(keyword)) {
            return true;
        }
    }
    return false;
}
```

---

### Step 2: 修改 chatToGenCode 方法

**文件：** `src/main/java/com/example/usercenterpractice/service/impl/AppServiceImpl.java`

**位置：** `chatToGenCode` 方法中第 185-187 行

**修改前：**
```java
// 6. 调用 AI 生成代码（流式）
Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
```

**修改后：**
```java
// 6. 构建带意图前缀的用户消息（只对 HTML/Multi-File 优化）
String messageWithIntent = wrapMessageWithIntent(appId, message, codeGenTypeEnum);
// 7. 调用 AI 生成代码（流式）
Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(messageWithIntent, codeGenTypeEnum, appId);
```

---

### Step 3: 处理纯文本响应（可选，视情况而定）

**文件：** `src/main/java/com/example/usercenterpractice/ai/handler/JsonMessageStreamHandler.java`

**需要检查：** 当 AI 返回纯文本（无代码）时的处理逻辑

**问题：** 当前 `JsonMessageStreamHandler` 期望 JSON 格式的代码响应

**分析：** 需要确认当 AI 只返回文字时，系统是否正确处理

**可能需要的修改：**
- 在 `JsonMessageStreamHandler` 中检测纯文本响应
- 如果是纯文本（无代码块），直接保存到对话历史，不尝试解析代码

---

## Validation

### 测试场景

| 类型 | 场景 | 输入 | 预期输出 |
|------|------|------|----------|
| **HTML** | 首次生成 | "生成一个登录页面" | 完整 HTML 代码 |
| **HTML** | 纯聊天 | "这个按钮是什么意思？" | 文字解释，无代码 |
| **HTML** | 编辑模式 | "把标题改成红色" | 完整 HTML 代码（含修改） |
| **Multi-File** | 首次生成 | "生成一个登录页面" | 完整 3 文件代码 |
| **Multi-File** | 纯聊天 | "这个按钮是什么意思？" | 文字解释，无代码 |
| **Multi-File** | 编辑模式 | "把标题改成红色" | 完整 3 文件代码（含修改） |
| **Vue** | 任何场景 | 任何输入 | 使用工具系统（不变） |

### 手动测试步骤

1. **HTML 类型测试：**
   - 创建新应用（HTML 类型）
   - 输入"生成一个登录页面" → 验证返回完整代码
   - 输入"这个按钮是什么意思？" → 验证只返回文字
   - 输入"把标题改成红色" → 验证返回完整代码（标题已修改）

2. **Multi-File 类型测试：**
   - 创建新应用（Multi-File 类型）
   - 重复上述测试步骤

3. **Vue 类型测试：**
   - 创建新应用（Vue 类型）
   - 验证工具调用正常工作
   - 确认增量修改功能正常

---

## Notes

### 各类型优化策略对比

| 类型 | 是否优化 | 优化方式 | 编辑模式行为 |
|------|---------|---------|-------------|
| **HTML** | ✅ 是 | 消息前缀 | 输出完整代码（单文件，无法增量） |
| **Multi-File** | ✅ 是 | 消息前缀 | 输出完整 3 文件代码 |
| **Vue** | ❌ 否 | 不动 | 使用工具增量修改 |

### 潜在问题

1. **意图识别准确性**
   - "怎么改成红色"会被判为聊天（因为包含"怎么"）
   - 可以优化关键词列表，添加例外规则

2. **聊天模式下的代码保存**
   - 当 AI 只返回文字时，`CodeParserExecutor` 可能解析失败
   - 需要在流处理器中处理这种情况

3. **编辑模式仍输出完整代码**
   - 对于 HTML/Multi-File，这是设计如此（无工具系统）
   - 如果需要增量保存，需要引入工具系统（大改动）

### 关键依赖

- `MessageWindowChatMemory` - 保持现有配置不变
- `@SystemMessage` 注解 - 提示词路径不变
- `RedisChatMemoryStore` - 对话历史存储不变
- Vue 工具系统 - 完全不动

### 后续优化方向

1. **如果 HTML/Multi-File 也需要增量保存：**
   - 引入类似 Vue 的工具系统
   - 或者在后端做代码 diff 合并

2. **如果需要更精确的意图识别：**
   - 使用小模型进行意图分类
   - 在前端让用户选择模式（聊天/编辑）

# 增量编辑问题根因分析与简化修复方案

## 问题根因分析

### 当前系统架构

```
用户消息 → 前端 (editMode=true) → 后端 Controller
         → AppService.chatToGenCode()
         → AiCodeGeneratorFacade.generateAndSaveCodeStream()
         → AiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, type, editMode)
         → 创建 AI 服务实例
         → ChatHistoryService.loadChatHistoryToMemory(appId, memory, 20, skipLatest)
         → 返回响应 → JsonMessageStreamHandler / SimpleTextStreamHandler
         → 保存 AI 响应到历史记录
```

### 关键发现

#### 1. Vue 项目的增量编辑机制（理论上是完整的）

**工作流程**：
1. `editMode=true` 时，历史加载包含最新记录（`skipLatest=false`）
2. AI 可以看到之前生成的完整代码（通过历史记录）
3. 系统提示词第 119-128 行明确要求 AI 使用工具进行增量修改

**理论上的行为**：
```
用户: "把标题改成红色"
AI: 应该：
  1. 使用 FileReadTool 读取文件
  2. 使用 FileModifyTool 修改指定部分
  3. 不重新生成整个文件
```

#### 2. HTML/多文件类型的增量编辑机制（已实现）

**代码注入逻辑**（AiCodeGeneratorFacade.java:91-95）：
```java
if (editMode && (codeGenTypeEnum == CodeGenTypeEnum.HTML || codeGenTypeEnum == CodeGenTypeEnum.MULTI_FILE)) {
    enhancedMessage = codeContextInjector.injectExistingCode(appId, codeGenTypeEnum, userMessage, true);
    // 会将已生成的代码注入到用户消息中
}
```

**提示词支持**（codegen-html-system-prompt.txt:25-32）：
```
## 增量编辑模式

当用户请求中包含【当前已生成的代码】时，表示处于增量编辑模式：
1. 仔细理解用户的修改意图
2. 只修改用户要求的部分，不要改动其他内容
3. 保持原有的代码结构和风格
4. 输出完整的 HTML 代码（不是差异部分）
```

### 实际问题所在

经过深入分析，增量编辑功能**代码层面已经实现**，但可能存在以下问题导致体验不佳：

#### 问题 A：AI 没有理解"增量编辑"的指令

**现象**：
- 即使 `editMode=true` 且代码被注入，AI 仍然重新生成全部代码
- 提示词中的"只修改必要部分"指令不够强烈

**原因**：
- 提示词优先级问题：生成代码的指令可能覆盖了增量编辑指令
- 模型行为：某些模型倾向于重新生成而非修改

#### 问题 B：每次对话都触发代码生成（核心问题）

**现象**：
- 用户问"这个按钮是什么意思？"也会触发代码生成
- 用户只是想聊天，但系统仍然生成代码

**原因**：
- 系统设计上没有区分"聊天模式"和"编辑模式"
- `editMode` 参数只是控制历史加载方式，不控制是否生成代码

#### 问题 C：代码注入策略不够智能

**现象**：
- 每次都注入完整代码，导致 token 浪费
- AI 收到的上下文过长，处理时间增加

**原因**：
- `CodeContextInjector` 注入的是完整文件内容
- 没有分析用户意图来决定注入哪些代码片段

---

## 简化修复方案

### 方案概述

不进行大规模重构，通过**最小化修改**解决核心问题：

1. **添加意图识别**：区分聊天和编辑意图
2. **强化提示词**：让 AI 更好地理解增量编辑
3. **优化代码注入**：只注入相关代码片段

### Phase 1: 添加意图识别（优先级：高）

**目的**：让系统能够区分用户是想"聊天"还是"编辑代码"

#### 1.1 创建简单的意图识别服务

**新建文件**：`src/main/java/com/example/usercenterpractice/ai/intent/SimpleIntentRecognizer.java`

```java
package com.example.usercenterpractice.ai.intent;

/**
 * 简单意图识别器（基于规则，不使用 AI）
 */
public class SimpleIntentRecognizer {

    /**
     * 识别用户意图
     * @return true = 编辑意图，false = 聊天意图
     */
    public static boolean isEditIntent(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return false;
        }

        String lowerMessage = userMessage.toLowerCase();

        // 编辑关键词
        String[] editKeywords = {
            "改成", "修改为", "改为", "更改", "调整", "设置",
            "增加", "删除", "移除", "添加", "去掉",
            "把", "让", "使", "替换"
        };

        // 聊天关键词
        String[] chatKeywords = {
            "什么", "怎么", "为什么", "如何", "解释",
            "说明", "介绍", "告诉我", "是什么意思", "作用"
        };

        // 优先检查聊天关键词（聊天意图优先）
        for (String keyword : chatKeywords) {
            if (lowerMessage.contains(keyword)) {
                return false;
            }
        }

        // 检查编辑关键词
        for (String keyword : editKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }

        // 默认：如果用户选择了可视化编辑元素，认为是编辑意图
        // 这个判断在前端完成，后端通过参数传递
        return false;
    }
}
```

#### 1.2 修改 Controller 支持纯聊天模式

**修改文件**：`src/main/java/com/example/usercenterpractice/controller/AppController.java`

在 `chatToGenCode` 方法中添加意图判断：

```java
@GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> chatToGenCode(
    @RequestParam String appId,
    @RequestParam String message,
    @RequestParam(defaultValue = "false") boolean editMode,
    @RequestParam(defaultValue = "false") boolean hasSelectedElement,  // 新增
    HttpServletRequest request) {

    // ... 参数校验 ...

    User loginUser = userService.getLoginUser(request);
    App app = appService.getById(parsedAppId);

    // 意图识别：判断是聊天还是编辑
    boolean isEditIntent = SimpleIntentRecognizer.isEditIntent(message) || hasSelectedElement;

    if (!isEditIntent) {
        // 纯聊天模式：不修改代码，只返回文字
        log.info("用户 appId: {} 进入纯聊天模式", appId);
        return appService.chatOnly(parsedAppId, message, loginUser);
    }

    // 编辑模式：增量修改代码
    log.info("用户 appId: {} 进入编辑模式", appId);
    return appService.chatToGenCode(parsedAppId, message, true, loginUser);
}
```

### Phase 2: 强化提示词（优先级：高）

**目的**：让 AI 更好地理解增量编辑的要求

#### 2.1 修改 HTML 系统提示词

**修改文件**：`src/main/resources/Prompt/codegen-html-system-prompt.txt`

在原有提示词**开头**添加：

```text
## 重要：增量编辑优先原则

当【当前已生成的代码】存在时，你必须进行增量编辑：

1. 仔细阅读用户要求的修改点
2. **绝对禁止**重新生成整个页面
3. **只修改**用户明确要求的部分
4. 保持其他所有内容完全不变

示例：
- 用户说"把标题改成红色" → 只改标题颜色，其他不变
- 用户说"修改按钮颜色为蓝色" → 只改按钮颜色，其他不变

如果你重新生成了整个页面，即为失败！
```

#### 2.2 修改多文件系统提示词

**修改文件**：`src/main/resources/Prompt/codegen-multi-file-system-prompt.txt`

同样在开头添加上述增量编辑优先原则。

#### 2.3 修改 Vue 项目提示词

**修改文件**：`src/main/resources/Prompt/codegen-vue-system-prompt.txt`

在"特别注意"部分（第 119 行）强化说明：

```text
## 特别注意：增量编辑（最高优先级）

**当对话历史中包含已生成的代码时，你必须使用工具进行增量修改，而不是重新生成文件！**

正确流程：
1. 使用【目录读取工具】查看项目结构
2. 使用【文件读取工具】查看需要修改的文件
3. 使用【文件修改工具】精确修改用户要求的部分
4. 禁止使用【文件写入工具】重写整个文件（除非用户明确要求重新生成）

错误示例：
- ❌ 直接输出完整的新文件代码
- ❌ 使用 FileWriteTool 重写现有文件
- ❌ 给用户提供手动修改建议

正确示例：
- ✅ 使用 FileModifyTool 修改指定行
- ✅ 只修改用户明确要求改动的部分
- ✅ 修改后简要说明改动了什么
```

### Phase 3: 实现纯聊天模式（优先级：中）

**目的**：支持用户只聊天不修改代码的场景

#### 3.1 添加 chatOnly 接口

**修改文件**：`src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorService.java`

```java
/**
 * 纯聊天模式：只返回文字回答，不生成或修改代码
 */
String chatOnly(String userMessage);
```

#### 3.2 实现 chatOnly 方法

**修改文件**：`src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java`

创建一个不使用工具的 AI 服务实例：

```java
/**
 * 创建纯聊天模式的 AI 服务（不使用工具）
 */
private AiCodeGeneratorService createChatOnlyService(long appId) {
    MessageWindowChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .id(appId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(20)
            .build();

    // 加载历史对话
    chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20, false);

    StreamingChatModel streamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);

    return AiServices.builder(AiCodeGeneratorService.class)
            .streamingChatModel(streamingChatModel)
            .chatMemory(chatMemory)
            // 不添加工具
            .build();
}
```

### Phase 4: 优化代码注入策略（优先级：低）

**目的**：减少 token 消耗，提高响应速度

#### 4.1 智能代码注入

**修改文件**：`src/main/java/com/example/usercenterpractice/ai/context/CodeContextInjector.java`

添加关键词匹配逻辑：

```java
/**
 * 构建编辑模式的消息 - 智能注入版
 */
private String buildEditMessage(String userMessage, String existingCode,
                                CodeGenTypeEnum codeGenType) {
    // 尝试提取用户想要修改的目标
    String target = extractEditTarget(userMessage);

    if (target != null) {
        // 如果能定位到具体元素，只注入相关片段
        String relevantCode = extractRelevantCode(existingCode, target);
        return """
            【用户修改请求】%s

            【相关代码片段】
            %s

            请只修改上述代码中的相关部分。
            """.formatted(userMessage, relevantCode);
    }

    // 兜底：限制完整代码的长度
    int maxLength = 3000;  // 从 8000 减少到 3000
    if (existingCode.length() > maxLength) {
        existingCode = existingCode.substring(0, maxLength) + "\n\n...(代码已截断)";
    }

    return """
        【当前已生成的代码】
        %s

        【用户修改请求】
        %s

        请根据用户请求进行增量修改。
        """.formatted(existingCode, userMessage);
}

/**
 * 从用户消息中提取编辑目标
 * 例如："把标题改成红色" -> "标题"
 */
private String extractEditTarget(String userMessage) {
    // 简单规则：提取"把 X 改成"模式中的 X
    Pattern pattern = Pattern.compile("把(.{1,10})改成");
    Matcher matcher = pattern.matcher(userMessage);
    if (matcher.find()) {
        return matcher.group(1);
    }

    // 提取"修改 X"模式中的 X
    pattern = Pattern.compile("修改(.{1,10})");
    matcher = pattern.matcher(userMessage);
    if (matcher.find()) {
        return matcher.group(1);
    }

    return null;
}
```

---

## Files to Modify

### 新建文件
| 文件 | 用途 |
|------|------|
| `ai/intent/SimpleIntentRecognizer.java` | 基于规则的意图识别 |

### 修改文件
| 文件 | 修改内容 |
|------|---------|
| `controller/AppController.java` | 添加 `hasSelectedElement` 参数和意图判断 |
| `service/AppService.java` | 添加 `chatOnly` 接口 |
| `service/impl/AppServiceImpl.java` | 实现 `chatOnly` 方法 |
| `ai/AiCodeGeneratorService.java` | 添加 `chatOnly` 接口 |
| `ai/AiCodeGeneratorServiceFactory.java` | 创建聊天模式服务 |
| `ai/context/CodeContextInjector.java` | 优化代码注入策略 |
| `Prompt/codegen-html-system-prompt.txt` | 强化增量编辑指令 |
| `Prompt/codegen-multi-file-system-prompt.txt` | 强化增量编辑指令 |
| `Prompt/codegen-vue-system-prompt.txt` | 强化增量编辑指令 |

### 前端修改（可选）
| 文件 | 修改内容 |
|------|---------|
| `pages/app/AppChatPage.vue` | 传递 `hasSelectedElement` 参数 |

---

## 验证方案

### 测试场景 1：纯聊天模式
```
用户: "这个按钮是什么意思？"
期望: AI 返回文字解释，不修改代码，响应快速
```

### 测试场景 2：编辑模式
```
用户: "把标题改成红色"
期望: AI 只修改标题颜色，其他部分不变
```

### 测试场景 3：可视化编辑
```
操作: 用户选中某个元素 + 输入"改成绿色"
期望: AI 修改选中的元素
```

---

## 与 v2 方案的区别

| 维度 | v2 方案 | 简化方案 |
|------|--------|---------|
| 意图识别 | AI 服务（复杂） | 规则匹配（简单） |
| 聊天模式 | 需要新服务 | 复用现有服务 |
| 提示词修改 | 重写 | 在开头强化 |
| 代码注入 | 智能片段提取 | 简化 + 长度限制 |
| 实现复杂度 | 高 | 低 |
| 风险 | 较高 | 较低 |

---

## Estimated Effort

| 阶段 | 复杂度 | 时间 |
|------|--------|------|
| Phase 1: 意图识别 | 低 | 1-2 小时 |
| Phase 2: 提示词强化 | 低 | 0.5 小时 |
| Phase 3: 聊天模式 | 中 | 2-3 小时 |
| Phase 4: 代码注入优化 | 中 | 2-3 小时 |
| 测试验证 | 中 | 1-2 小时 |
| **总计** | | **6-10 小时** |

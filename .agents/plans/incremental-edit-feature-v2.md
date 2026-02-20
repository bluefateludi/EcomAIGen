# 增量编辑功能改进方案 v2

## 问题分析

### 当前问题
1. **每次对话都重新生成代码**：用户只是问问题（如"这个按钮是什么意思"），AI 也会重新生成整个代码
2. **代码重复发送**：每次都把完整代码注入上下文，导致等待时间长
3. **模式判断不明确**：前端有 `editMode` 参数，但后端没有真正的"仅聊天模式"

### 用户期望行为

| 用户意图 | 期望行为 | 代码变化 |
|---------|---------|---------|
| "这个按钮是什么意思？" | 仅文字回答，不修改代码 | ❌ 不变 |
| "帮我解释一下这段代码" | 仅文字回答，不修改代码 | ❌ 不变 |
| "把标题改成红色" | 修改代码并返回结果 | ✅ 修改指定部分 |
| "重新生成" | 全量重新生成代码 | ✅ 全部重写 |

---

## 改进方案

### 核心思路：添加意图识别层

在代码生成之前，先判断用户的**意图类型**：
1. **纯聊天模式**：用户只是提问、咨询、请求解释 → 不调用代码生成，直接返回文字
2. **编辑模式**：用户明确要求修改代码 → 增量修改代码
3. **重新生成模式**：用户要求重新生成 → 全量生成新代码

---

## Implementation Plan

### Phase 1: 创建意图识别服务

#### 1.1 新增意图枚举
**新建文件**: `src/main/java/com/example/usercenterpractice/ai/model/UserIntentEnum.java`

```java
package com.example.usercenterpractice.ai.model;

public enum UserIntentEnum {
    CHAT,           // 纯聊天：只回答问题，不修改代码
    EDIT,           // 编辑：修改现有代码
    REGENERATE      // 重新生成：全量生成新代码
}
```

#### 1.2 创建意图识别服务
**新建文件**: `src/main/java/com/example/usercenterpractice/ai/intent/UserIntentRecognizer.java`

```java
package com.example.usercenterpractice.ai.intent;

import com.example.usercenterpractice.ai.model.UserIntentEnum;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface UserIntentRecognizer {

    @SystemMessage("""
        你是一个用户意图识别器。根据用户输入判断其意图类型。

        意图类型：
        1. CHAT - 纯聊天模式。用户只是提问、咨询、请求解释，不涉及代码修改。
           触发词：什么、怎么、为什么、解释、说明、告诉我、介绍

        2. EDIT - 编辑模式。用户明确要求修改现有代码的某个部分。
           触发词：把...改成、修改...为、更改、调整、设置、增加、删除

        3. REGENERATE - 重新生成模式。用户要求重新生成整个页面或项目。
           触发词：重新生成、换个、重新做、不要这个了

        只返回意图类型名称（CHAT/EDIT/REGENERATE），不要其他内容。
        """)
    UserIntentEnum recognizeIntent(@UserMessage String userMessage);
}
```

---

### Phase 2: 修改路由提示词 - 关键改进

**修改文件**: `src/main/resources/Prompt/codegen-routing-system-prompt.txt`

**当前内容问题**：提示词过于简单，没有告诉 AI 如何区分聊天和编辑

**修改后内容**：

```text
你是一个专业的代码生成助手路由器。根据用户需求判断应该执行的操作类型。

操作类型：
1. CHAT - 用户只是提问、咨询、请求解释，不需要修改任何代码
   示例：
   - "这个按钮是什么意思？"
   - "帮我解释一下这段代码"
   - "这个页面有什么功能？"

2. EDIT - 用户要求修改现有代码的某个部分
   示例：
   - "把标题改成红色"
   - "修改按钮的颜色为蓝色"
   - "把导航栏移到顶部"

3. REGENERATE - 用户要求重新生成整个页面
   示例：
   - "重新生成一个登录页面"
   - "不要这个了，换个风格"

判断规则：
- 如果用户输入包含"什么"、"怎么"、"为什么"、"解释"、"说明"等疑问词 → CHAT
- 如果用户输入包含"改成"、"修改为"、"更改"、"调整"等修改词 → EDIT
- 如果用户输入包含"重新生成"、"换个"、"重新做" → REGENERATE
- 如果有已生成的代码且用户意图不明确，默认为 CHAT（不修改代码）

只返回操作类型名称（CHAT/EDIT/REGENERATE）。
```

---

### Phase 3: 添加纯聊天模式支持

#### 3.1 修改 AiCodeGeneratorFacade
**修改文件**: `src/main/java/com/example/usercenterpractice/ai/core/AiCodeGeneratorFacade.java`

添加新方法用于纯聊天：

```java
/**
 * 纯聊天模式：不生成或修改代码，只返回文字回答
 *
 * @param userMessage 用户消息
 * @param appId 应用 ID
 * @return 文字回答流
 */
public Flux<String> chatOnly(String userMessage, Long appId) {
    AiCodeGeneratorService aiService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);

    // 创建一个只返回文字的 Flux
    return Flux.create(sink -> {
        aiService.chatOnly(userMessage)
            .doOnNext(response -> {
                // 包装为 AI 响应消息
                AiResponseMessage message = new AiResponseMessage(response);
                sink.next(JSONUtil.toJsonStr(message));
            })
            .doOnComplete(() -> {
                // 保存聊天记录到历史，但不修改代码
                chatHistoryService.addChatMessage(appId, userMessage,
                    ChatHistoryMessageTypeEnum.USER.getValue(), userId);
                chatHistoryService.addChatMessage(appId, fullResponse,
                    ChatHistoryMessageTypeEnum.AI.getValue(), userId);
                sink.complete();
            })
            .doOnError(sink::error)
            .start();
    });
}
```

#### 3.2 修改 AiCodeGeneratorService
**修改文件**: `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorService.java`

添加纯聊天接口：

```java
/**
 * 纯聊天模式：只返回文字回答，不调用工具生成代码
 */
String chatOnly(String userMessage);
```

---

### Phase 4: 修改 AppController 集成意图识别

**修改文件**: `src/main/java/com/example/usercenterpractice/controller/AppController.java`

```java
@Resource
private UserIntentRecognizer userIntentRecognizer;

@GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> chatToGenCode(
    @RequestParam String appId,
    @RequestParam String message,
    @RequestParam(defaultValue = "false") boolean editMode,
    HttpServletRequest request) {

    // ... 参数校验 ...

    User loginUser = userService.getLoginUser(request);
    App app = appService.getById(parsedAppId);

    // 关键改进：先识别用户意图
    UserIntentEnum intent = userIntentRecognizer.recognizeIntent(message);

    return switch (intent) {
        case CHAT -> {
            // 纯聊天模式：不修改代码，只返回文字
            log.info("用户 appId: {} 进入纯聊天模式", appId);
            yield appService.chatOnly(parsedAppId, message, loginUser);
        }
        case EDIT -> {
            // 编辑模式：增量修改代码
            log.info("用户 appId: {} 进入编辑模式", appId);
            yield appService.chatToGenCode(parsedAppId, message, true, loginUser);
        }
        case REGENERATE -> {
            // 重新生成模式：全量生成
            log.info("用户 appId: {} 进入重新生成模式", appId);
            yield appService.chatToGenCode(parsedAppId, message, false, loginUser);
        }
    };
}
```

---

### Phase 5: 优化代码注入策略

#### 5.1 智能代码注入

**修改文件**: `src/main/java/com/example/usercenterpractice/ai/context/CodeContextInjector.java`

```java
/**
 * 构建编辑模式的消息 - 优化版
 * 只注入与用户修改相关的代码片段
 */
private String buildEditMessage(String userMessage, String existingCode,
                                CodeGenTypeEnum codeGenType) {
    // 分析用户的修改意图
    String targetElement = extractTargetElement(userMessage);

    // 如果能定位到具体元素，只注入相关代码片段
    if (targetElement != null) {
        String relevantCode = extractRelevantCode(existingCode, targetElement);
        return """
            【用户修改请求】%s

            【相关代码片段】
            %s

            请只修改上述代码中的相关部分，输出完整代码。
            """.formatted(userMessage, relevantCode);
    }

    // 兜底：注入完整代码（但限制长度）
    String codeToInclude = existingCode;
    int maxLength = 5000;  // 减小长度限制
    if (existingCode.length() > maxLength) {
        codeToInclude = existingCode.substring(0, maxLength) + "\n\n...(代码已截断)";
    }

    return """
        【当前已生成的代码】
        %s

        【用户修改请求】
        %s

        请根据用户请求进行增量修改。
        """.formatted(codeToInclude, userMessage);
}

/**
 * 从用户消息中提取要修改的目标元素
 * 例如："把标题改成红色" -> "标题"
 */
private String extractTargetElement(String userMessage) {
    // 简单实现：提取"把...改成"模式中的目标
    Pattern pattern = Pattern.compile("把(.*?)改成");
    Matcher matcher = pattern.matcher(userMessage);
    if (matcher.find()) {
        return matcher.group(1);
    }
    return null;
}
```

---

## Phase 6: 前端 UI 优化（可选）

**修改文件**: `EcomAIGen-fronted/src/pages/app/AppChatPage.vue`

添加模式切换按钮，让用户显式选择意图：

```vue
<template>
  <div class="chat-mode-selector">
    <a-radio-group v-model:value="chatMode" button-style="solid">
      <a-radio-button value="auto">🤖 智能判断</a-radio-button>
      <a-radio-button value="chat">💬 仅聊天</a-radio-button>
      <a-radio-button value="edit">✏️ 编辑代码</a-radio-button>
      <a-radio-button value="regenerate">🔄 重新生成</a-radio-button>
    </a-radio-group>
  </div>
</template>

<script setup>
const chatMode = ref('auto');

const generateCode = async () => {
  const params = new URLSearchParams({
    appId: appId.value,
    message: userMessage,
  });

  // 如果用户显式选择了模式，传递 mode 参数
  if (chatMode.value !== 'auto') {
    params.append('mode', chatMode.value);
  }

  // ...
};
</script>
```

---

## Files to Modify

### 新建文件
| 文件 | 用途 |
|------|------|
| `ai/model/UserIntentEnum.java` | 用户意图枚举 |
| `ai/intent/UserIntentRecognizer.java` | 意图识别服务 |

### 修改文件
| 文件 | 修改内容 |
|------|---------|
| `Prompt/codegen-routing-system-prompt.txt` | **关键**：添加意图判断规则 |
| `ai/AiCodeGeneratorService.java` | 添加 chatOnly 接口 |
| `ai/AiCodeGeneratorServiceFactory.java` | 支持创建聊天类型服务 |
| `ai/core/AiCodeGeneratorFacade.java` | 添加 chatOnly 方法 |
| `controller/AppController.java` | 集成意图识别 |
| `service/AppService.java` | 添加 chatOnly 接口 |
| `service/impl/AppServiceImpl.java` | 实现 chatOnly |
| `ai/context/CodeContextInjector.java` | 优化代码注入策略 |

---

## Verification

### 测试场景

| 场景 | 输入 | 期望行为 | 验证点 |
|------|------|---------|--------|
| 纯聊天 | "这个按钮是什么意思？" | 返回文字解释，代码不变 | 检查代码文件未修改 |
| 纯聊天 | "解释一下这段代码的功能" | 返回文字解释 | 响应时间 < 5秒 |
| 编辑模式 | "把标题改成红色" | 修改代码，返回结果 | 只有标题样式变化 |
| 编辑模式 | "修改按钮颜色为蓝色" | 修改按钮颜色 | 其他元素不变 |
| 重新生成 | "重新生成一个产品页" | 全量生成新代码 | 完全不同的页面 |

### 性能验证

```bash
# 测试纯聊天模式的响应时间
curl -N "http://localhost:8123/api/app/chat/gen/code?appId=xxx&message=这个按钮是什么"

# 验证代码未被修改
ls -l tmp/code_output/html_xxx/index.html
# 文件修改时间应该不变
```

---

## Key Improvements

### 与原方案的区别

| 维度 | 原方案 | 改进方案 |
|------|--------|---------|
| 模式判断 | 只有 editMode (true/false) | 三种模式：CHAT/EDIT/REGENERATE |
| 聊天支持 | ❌ 不支持，每次都生成代码 | ✅ 纯聊天模式，不修改代码 |
| 代码注入 | 每次注入完整代码 | 智能注入相关片段 |
| 响应速度 | 慢（每次重新生成） | 快（聊天模式直接返回） |
| 用户体验 | 每次都要等待代码生成 | 问问题时立即得到回答 |

### 核心优势

1. **真正的聊天模式**：用户问问题时不需要等待代码生成
2. **智能意图识别**：自动判断用户是想聊天还是想修改
3. **精确代码注入**：只注入与修改相关的代码片段
4. **可选的显式模式**：前端可以提供模式切换按钮

---

## Estimated Effort

| 阶段 | 复杂度 | 说明 |
|------|--------|------|
| Phase 1: 意图识别 | 低 | 新建枚举和服务接口 |
| Phase 2: 路由提示词 | 中 | **关键**，需要仔细设计规则 |
| Phase 3: 聊天模式 | 中 | 需要修改服务层 |
| Phase 4: 集成意图识别 | 低 | Controller 层修改 |
| Phase 5: 智能注入 | 高 | 需要代码解析逻辑 |
| Phase 6: 前端 UI | 低 | 可选功能 |
| 测试验证 | 中 | 需要多场景测试 |

---

## Notes

### 关键依赖
- LangChain4J 的流式响应支持
- 现有的 ChatHistory 保存机制

### 潜在风险
1. **意图识别准确度**：可能误判用户意图 → 缓解：添加前端显式模式选择
2. **代码注入不完整**：片段注入可能缺少上下文 → 缓解：保持完整注入作为兜底
3. **兼容性**：editMode 参数仍需保留 → 新旧参数共存，逐步迁移

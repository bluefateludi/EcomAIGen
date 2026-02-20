# 修复 AI 每次都输出完整代码的问题

## 问题描述

当前系统中，无论用户输入什么，AI 都会返回完整的代码：
- 用户问"这个按钮是什么意思？" → AI 返回完整 HTML 代码 ❌
- 用户说"把标题改成红色" → AI 返回完整 HTML 代码 ❌
- 用户首次生成"生成登录页" → AI 返回完整 HTML 代码 ✅

**期望行为**：
- **聊天模式**：只返回文字解释，不输出代码
- **编辑模式**：只返回简短的修改说明，不输出完整代码
- **首次生成**：返回完整代码

---

## 根本原因分析

### 1. 提示词问题

**HTML 提示词** (codegen-html-system-prompt.txt:31)：
```
4. 输出完整的 HTML 代码（不是差异部分）
```

**多文件提示词** (codegen-multi-file-system-prompt.txt:40)：
```
4. 输出完整的三个文件代码（不是差异部分）
```

这些提示词明确要求 AI 输出完整代码，导致编辑时也返回全部内容。

### 2. 没有意图识别

系统无法区分用户的三种意图：
- **聊天意图**："这是什么？"、"怎么用？"、"解释一下"
- **编辑意图**："把X改成Y"、"修改颜色"、"调整大小"
- **生成意图**："生成一个登录页"、"创建一个表单"

### 3. 没有模式参数传递

前端虽然传递了 `editMode` 参数，但这个参数只控制：
- 历史记录是否包含最新一条（skipLatest）
- 是否注入已生成的代码

**没有告诉 AI 当前是什么模式！**

---

## 解决方案

### 方案概述

1. **修改提示词**：区分三种模式，明确每种模式的输出要求
2. **添加模式标记**：在用户消息中添加模式标记，告诉 AI 当前是什么模式
3. **意图识别（可选）**：前端或后端自动判断用户意图

---

## Implementation Plan

### Phase 1: 修改提示词（必须）

#### 1.1 修改 HTML 提示词

**文件**：`src/main/resources/Prompt/codegen-html-system-prompt.txt`

**当前内容**（第 1-32 行）：
```
你是一位资深的 Web 前端开发专家...
...
## 增量编辑模式
当用户请求中包含【当前已生成的代码】时，表示处于增量编辑模式：
1. 仔细理解用户的修改意图
2. 只修改用户要求的部分，不要改动其他内容
3. 保持原有的代码结构和风格
4. 输出完整的 HTML 代码（不是差异部分）
```

**修改为**：
```
你是一位资深的 Web 前端开发专家。

## 重要：根据消息中的模式标记执行不同任务

你的回复必须根据用户消息开头的【模式标记】来决定：

### 【模式：首次生成】
- 输出完整的 HTML 代码
- 包含所有 CSS（在 `<style>` 中）和 JavaScript（在 `<script>` 中）
- 只输出 1 个 HTML 代码块

### 【模式：编辑】
- **只返回简短的修改说明**
- 例如："已将标题颜色改为红色"
- **不要输出任何代码**
- **不要输出完整 HTML 文件**

### 【模式：聊天】
- **只返回文字回答**
- **不要输出任何代码**
- **不要包含代码块**

---

## 首次生成时的约束

当【模式：首次生成】时：

1. 技术栈: 只能使用 HTML、CSS 和原生 JavaScript
2. 禁止外部依赖: 不允许使用任何外部 CSS 框架、JS 库
3. 独立文件: 所有 CSS 内联在 `<head>` 的 `<style>` 中，JS 放在 `</body>` 前的 `<script>` 中
4. 响应式设计: 优先使用 Flexbox 或 Grid
5. 只能输出 1 个 HTML 代码块

## 首次生成时输出格式

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        /* CSS 代码 */
    </style>
</head>
<body>
    <!-- HTML 内容 -->
    <script>
        // JavaScript 代码
    </script>
</body>
</html>
```

## 编辑模式说明

当【模式：编辑】时：
- 用户已经提供了要修改的元素信息
- 你只需要说明做了什么修改
- 不要输出代码
```

#### 1.2 修改多文件提示词

**文件**：`src/main/resources/Prompt/codegen-multi-file-system-prompt.txt`

**同样的修改方式**，添加三种模式的区分。

#### 1.3 修改 Vue 提示词

**文件**：`src/main/resources/Prompt/codegen-vue-system-prompt.txt`

Vue 项目使用工具系统，需要在提示词中说明：
- 编辑模式时，AI 应该使用工具修改文件
- 但返回给用户的应该是简短的说明，而不是完整代码

---

### Phase 2: 添加模式标记传递（必须）

#### 2.1 修改后端 - 在用户消息前添加模式标记

**文件**：`src/main/java/com/example/usercenterpractice/service/impl/AppServiceImpl.java`

在 `chatToGenCode` 方法中，发送消息给 AI 之前，添加模式标记：

```java
@Override
public Flux<String> chatToGenCode(Long appId, String message, boolean editMode, User loginUser) {
    // ... 现有代码 ...

    // 构建带模式标记的用户消息
    String messageWithMode = buildMessageWithMode(message, editMode);

    // 调用 AI 生成代码
    Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
        messageWithMode,  // 使用带模式标记的消息
        codeGenTypeEnum,
        appId,
        editMode
    );
    // ...
}

/**
 * 构建带模式标记的用户消息
 */
private String buildMessageWithMode(String userMessage, boolean editMode) {
    // 判断是否有历史对话记录
    long chatHistoryCount = chatHistoryService.count(
        new QueryWrapper<ChatHistory>()
            .eq("appId", appId)
            .eq("messageType", ChatHistoryMessageTypeEnum.AI.getValue())
    );

    if (chatHistoryCount == 0) {
        // 首次生成
        return "【模式：首次生成】\n" + userMessage;
    }

    // 有历史记录，判断是聊天还是编辑
    if (isChatIntent(userMessage)) {
        return "【模式：聊天】\n" + userMessage;
    } else {
        return "【模式：编辑】\n" + userMessage;
    }
}

/**
 * 简单的意图判断
 */
private boolean isChatIntent(String message) {
    String[] chatKeywords = {
        "什么", "怎么", "为什么", "如何", "解释",
        "说明", "介绍", "告诉我", "是什么意思", "作用", "功能"
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

### Phase 3: 前端配合（可选）

如果前端已经通过可视化编辑选择了元素，可以在消息中添加标记：

**文件**：`EcomAIGen-fronted/src/pages/app/AppChatPage.vue`

在 `sendMessage` 方法中：

```typescript
let message = userInput.value.trim()

// 如果有选中的元素，添加编辑标记
if (selectedElementInfo.value) {
  message = `【编辑选中元素】\n${message}`
  // ... 后续代码
}
```

---

## Files to Modify

### 必须修改
| 文件 | 修改内容 |
|------|---------|
| `Prompt/codegen-html-system-prompt.txt` | 添加三种模式说明 |
| `Prompt/codegen-multi-file-system-prompt.txt` | 添加三种模式说明 |
| `service/impl/AppServiceImpl.java` | 添加模式标记和意图判断 |

### 可选修改
| 文件 | 修改内容 |
|------|---------|
| `Prompt/codegen-vue-system-prompt.txt` | 添加模式说明 |
| `pages/app/AppChatPage.vue` | 配合传递编辑标记 |

---

## 验证方案

### 测试场景 1：首次生成
```
输入: "生成一个登录页面"
期望: AI 返回完整的 HTML 代码
```

### 测试场景 2：纯聊天
```
输入: "这个按钮是什么意思？"
期望: AI 只返回文字解释，不输出代码
```

### 测试场景 3：编辑模式
```
输入: "把标题改成红色"
期望: AI 返回 "已将标题颜色改为红色"，不输出完整代码
```

---

## 关键点

1. **模式标记必须在用户消息的开头**，这样 AI 会优先识别
2. **意图识别使用简单的关键词匹配**，避免过度复杂
3. **编辑模式不输出代码**，只输出修改说明
4. **保持向后兼容**，首次生成仍然返回完整代码

---

## Estimated Effort

| 阶段 | 时间 |
|------|------|
| Phase 1: 修改提示词 | 0.5 小时 |
| Phase 2: 添加模式标记 | 1-2 小时 |
| Phase 3: 前端配合（可选） | 0.5 小时 |
| 测试验证 | 1 小时 |
| **总计** | **3-4 小时** |

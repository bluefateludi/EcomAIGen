# 回退到教程源码设计方案

## 概述

将当前对增量编辑功能的修改全部回退，与教程源码 `yu-ai-code-mother` 保持一致。

教程的增量编辑设计思路：
- **VUE_PROJECT**：使用 `TokenStream` + 工具调用（FileModifyTool 等），AI 精确修改文件
- **HTML / MULTI_FILE**：**不用工具**，AI 直接重新输出完整代码（通过 prompt 约束只输出 1 个代码块）

---

## 当前状态

当前分支 `master`，HEAD 为 `43cd5cc`，有以下未提交的修改（均由 Claude 本次操作引入）：

| 文件 | 改动类型 |
|------|---------|
| `ai/AiCodeGeneratorService.java` | 新增了 `generateHtmlCodeStreamWithTools` / `generateMultiFileCodeStreamWithTools` 方法 |
| `ai/AiCodeGeneratorServiceFactory.java` | HTML/MULTI_FILE 添加了 tools、maxSequentialToolsInvocations 等 |
| `ai/core/AiCodeGeneratorFacade.java` | HTML/MULTI_FILE 改用 TokenStream，删除了 processCodeStream |
| `ai/handler/JsonMessageStreamHandler.java` | 取消注释了 AI 响应保存代码 |
| `ai/handler/StreamHandlerExecutor.java` | 所有类型统一走 jsonMessageStreamHandler |
| `resources/Prompt/codegen-html-system-prompt.txt` | 添加了工具编辑模式说明 |
| `resources/Prompt/codegen-multi-file-system-prompt.txt` | 添加了工具编辑模式说明 |

---

## 执行步骤

### Step 1：git restore 回退所有修改到 HEAD 状态
```bash
git restore src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorService.java
git restore src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java
git restore src/main/java/com/example/usercenterpractice/ai/core/AiCodeGeneratorFacade.java
git restore src/main/java/com/example/usercenterpractice/ai/handler/JsonMessageStreamHandler.java
git restore src/main/java/com/example/usercenterpractice/ai/handler/StreamHandlerExecutor.java
git restore src/main/resources/Prompt/codegen-html-system-prompt.txt
git restore src/main/resources/Prompt/codegen-multi-file-system-prompt.txt
```

> 执行后所有文件回到 43cd5cc 提交的状态，即干净基线。

### Step 2：修改 AiCodeGeneratorServiceFactory.java

对比教程源码，需要做以下调整（仅 VUE_PROJECT 分支）：

**教程有，您的项目缺少的：**
- `.chatModel(chatModel)` ← 需添加
- `.maxSequentialToolsInvocations(20)` ← 需添加

**HTML/MULTI_FILE 分支**（教程 vs 您的项目）：
- 教程：`.chatModel(chatModel)` + `.streamingChatModel(...)` + `.chatMemory(chatMemory)` + `.inputGuardrails(...)` ← 无工具
- 您的项目：`.chatModel(chatModel)` + `.streamingChatModel(...)` + `.chatMemory(chatMemory)` ← **缺少 inputGuardrails**

修改内容：
```java
// VUE_PROJECT 分支添加：
.chatModel(chatModel)
.maxSequentialToolsInvocations(20)

// HTML/MULTI_FILE 分支添加：
.inputGuardrails(new PromptSafetyInputGuardrail())
```

### Step 3：修改 JsonMessageStreamHandler.java

**教程版本：** `doOnComplete` 中 AI 响应保存代码是**启用的**（非注释）
**您的项目（HEAD）：** 该代码被注释掉了

需取消注释，使行为与教程一致：
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

### Step 4：更新系统提示词

**教程的 HTML prompt 末尾新增：**
```
特别注意：在生成代码后，用户可能会提出修改要求并给出要修改的元素信息。
1. 你必须严格按照要求修改，不要额外修改用户要求之外的元素和内容
2. 确保始终最多输出 1 个 HTML 代码块，里面包含了完整的页面代码（而不是要修改的部分代码）。
3. 一定不能输出超过 1 个代码块，否则会导致保存错误！
```

**教程的 Multi-File prompt 末尾新增：**
```
特别注意：在生成代码后，用户可能会提出修改要求并给出要修改的元素信息。
1. 你必须严格按照要求修改，不要额外修改用户要求之外的元素和内容
2. 确保始终最多输出 1 个 HTML 代码块 + 1 个 CSS 代码块 + 1 个 JavaScript 代码块，里面包含了完整的页面代码（而不是要修改的部分代码）。
3. 每种语言的代码块一定不能输出超过 1 个，否则会导致保存错误！
```

---

## 修改文件汇总

| 文件 | 步骤 | 操作 |
|------|------|------|
| 全部7个已修改文件 | Step 1 | git restore 回到 HEAD |
| `AiCodeGeneratorServiceFactory.java` | Step 2 | VUE_PROJECT 加 chatModel + maxSequentialToolsInvocations；HTML/MULTI_FILE 加 inputGuardrails |
| `JsonMessageStreamHandler.java` | Step 3 | 取消注释 AI 响应保存代码 |
| `codegen-html-system-prompt.txt` | Step 4 | 替换末尾为教程版本的特别注意说明 |
| `codegen-multi-file-system-prompt.txt` | Step 4 | 替换末尾为教程版本的特别注意说明 |

---

## 验证方法

1. **首次生成**：创建 HTML 应用，输入描述，验证代码正常生成并保存
2. **增量编辑**：输入"把标题改成客户专用"，验证 AI 重新输出完整 HTML（含修改）
3. **对话历史**：检查数据库中 AI 响应是否正确保存
4. **VUE_PROJECT**：验证工具调用流程不受影响

# 增量编辑功能实现计划

## Context（背景）

用户在使用 EcomAIGen 进行代码生成后，继续对话时 AI 会完全替换已生成的代码，而不是进行增量修改。例如，用户说「把标题改成红色」时，AI 会重新生成整个页面，而不是只修改标题样式。

**问题根源分析**：
1. **Vue 项目**：工具系统已存在，但 AI 响应未保存到历史记录（被注释掉），导致 AI 不记得之前生成的代码
2. **HTML/多文件类型**：没有增量编辑指令，每次都是全量生成
3. **历史加载逻辑**：`loadChatHistoryToMemory` 跳过最新一条记录，导致 AI 看不到自己最近的输出

**需求**：
- 支持所有代码类型（Vue/HTML/多文件）的增量编辑
- 前端显式传递 `editMode` 参数区分编辑模式和重新生成模式
- 编辑模式下 AI 只修改用户要求的部分

---

## Implementation Plan（实现方案）

### 方案选择：混合方案

| 类型 | 方案 | 说明 |
|------|------|------|
| Vue 项目 | 修复现有工具系统 | 取消注释 AI 响应保存，修复历史加载 |
| HTML/多文件 | 代码注入 + 增强提示词 | 将已生成代码注入 AI 上下文，指导增量修改 |

---

### Phase 1: 后端 API 支持 editMode 参数

#### 1.1 AppController.java
**文件**: `src/main/java/com/example/usercenterpractice/controller/AppController.java`

修改 `chatToGenCode` 方法：
```java
@GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> chatToGenCode(
    @RequestParam String appId,
    @RequestParam String message,
    @RequestParam(defaultValue = "false") boolean editMode,  // 新增
    HttpServletRequest request) {
    // ...
    Flux<String> contentFlux = appService.chatToGenCode(parsedAppId, message, editMode, loginUser);
    // ...
}
```

#### 1.2 AppService.java / AppServiceImpl.java
**文件**: `src/main/java/com/example/usercenterpractice/service/AppService.java`
**文件**: `src/main/java/com/example/usercenterpractice/service/impl/AppServiceImpl.java`

修改接口和实现，传递 `editMode` 参数到 `aiCodeGeneratorFacade`。

#### 1.3 AiCodeGeneratorFacade.java
**文件**: `src/main/java/com/example/usercenterpractice/ai/core/AiCodeGeneratorFacade.java`

修改 `generateAndSaveCodeStream` 方法：
```java
public Flux<String> generateAndSaveCodeStream(
    String userMessage,
    CodeGenTypeEnum codeGenTypeEnum,
    Long appId,
    boolean editMode) {  // 新增参数
    // ...
}
```

---

### Phase 2: 修复 Vue 项目的增量编辑

#### 2.1 JsonMessageStreamHandler.java - 取消注释 AI 响应保存
**文件**: `src/main/java/com/example/usercenterpractice/ai/handler/JsonMessageStreamHandler.java`

**修改**：取消注释第 61-62 行
```java
.doOnComplete(() -> {
    // 流式响应完成后，添加 AI 消息到对话历史
    String aiResponse = chatHistoryStringBuilder.toString();
    chatHistoryService.addChatMessage(appId, aiResponse,
        ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());  // 取消注释
    // 异步构造 Vue 项目
    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
    vueProjectBuilder.buildProjectAsync(projectPath);
})
```

#### 2.2 ChatHistoryServiceImpl.java - 修复历史加载
**文件**: `src/main/java/com/example/usercenterpractice/service/impl/ChatHistoryServiceImpl.java`

**问题**：第 144 行 `LIMIT 1, maxCount` 跳过了最新一条记录

**修改方案**：根据 `editMode` 决定是否跳过最新记录
```java
public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory,
                                   int maxCount, boolean skipLatest) {
    String limitSql = skipLatest
        ? "LIMIT 1, " + maxCount      // 跳过最新一条
        : "LIMIT " + maxCount;         // 包含最新一条
    // ...
}
```

**注意**：初次生成时需要跳过最新（避免看到未完成的响应），编辑模式时需要包含最新。

---

### Phase 3: HTML/多文件类型增量编辑支持

#### 3.1 创建代码注入服务
**新文件**: `src/main/java/com/example/usercenterpractice/ai/context/CodeContextInjector.java`

```java
@Service
public class CodeContextInjector {

    /**
     * 为编辑模式注入已生成的代码上下文
     */
    public String injectExistingCode(Long appId, CodeGenTypeEnum codeGenType,
                                      String userMessage) {
        if (!isEditMode) return userMessage;

        // 1. 读取已生成的代码文件
        String existingCode = readExistingCode(appId, codeGenType);

        // 2. 构建带上下文的用户消息
        return """
            【当前已生成的代码】
            %s

            【用户修改请求】
            %s

            请根据用户请求，只修改必要的部分，输出完整的代码。
            """.formatted(existingCode, userMessage);
    }
}
```

#### 3.2 更新 HTML/多文件 系统提示词
**文件**: `src/main/resources/Prompt/codegen-html-system-prompt.txt`
**文件**: `src/main/resources/Prompt/codegen-multi-file-system-prompt.txt`

在现有提示词基础上添加增量编辑指令（参考 Vue 提示词第 119-128 行）：

```text
## 增量编辑模式

当用户提出修改要求时（如「把标题改成红色」）：
1. 仔细理解用户的修改意图
2. 只修改用户要求的部分，不要改动其他内容
3. 保持原有的代码结构和风格
4. 输出完整的代码（不是差异部分）
```

---

### Phase 4: 前端支持

#### 4.1 AppChatPage.vue - 添加 editMode 参数
**文件**: `EcomAIGen-fronted/src/pages/app/AppChatPage.vue`

修改 `generateCode` 方法，添加 `editMode` 参数：

```typescript
// 构建URL参数
const params = new URLSearchParams({
  appId: appId.value || '',
  message: userMessage,
  editMode: hasExistingCode() ? 'true' : 'false',  // 新增
})

// 判断是否有已生成的代码
const hasExistingCode = () => {
  return messages.value.filter(m => m.type === 'ai' && m.content).length > 0
}
```

**UI 优化**（可选）：
- 添加「重新生成」按钮，显式设置 `editMode=false`
- 默认对话时自动设置 `editMode=true`（如果有已生成代码）

---

### Phase 5: 边界情况处理

| 场景 | 处理方式 |
|------|---------|
| 代码文件不存在 | `editMode=true` 时检测，自动切换为全量生成 |
| 代码文件损坏 | 返回错误提示，引导用户重新生成 |
| 首次生成 | 强制 `editMode=false` |
| 用户显式要求「重新生成」 | 前端设置 `editMode=false` |

---

## Files to Modify（需修改的文件清单）

### 后端
| 文件 | 修改内容 |
|------|---------|
| `controller/AppController.java` | 添加 `editMode` 参数 |
| `service/AppService.java` | 接口添加 `editMode` 参数 |
| `service/impl/AppServiceImpl.java` | 实现传递 `editMode` |
| `ai/core/AiCodeGeneratorFacade.java` | 处理 `editMode` 逻辑 |
| `ai/handler/JsonMessageStreamHandler.java` | **取消注释** AI 响应保存 |
| `service/impl/ChatHistoryServiceImpl.java` | 修复历史加载逻辑 |
| `ai/context/CodeContextInjector.java` | **新建** 代码注入服务 |
| `Prompt/codegen-html-system-prompt.txt` | 添加增量编辑指令 |
| `Prompt/codegen-multi-file-system-prompt.txt` | 添加增量编辑指令 |

### 前端
| 文件 | 修改内容 |
|------|---------|
| `pages/app/AppChatPage.vue` | 添加 `editMode` 参数传递 |

---

## Verification（验证步骤）

### 1. 单元测试
```bash
# 测试历史加载逻辑
mvn test -Dtest=ChatHistoryServiceImplTest
```

### 2. 手动测试场景

**场景 A：Vue 项目增量编辑**
1. 创建 Vue 项目，输入「生成一个电商首页」
2. 等待生成完成
3. 输入「把导航栏背景改成蓝色」
4. 验证：只修改导航栏样式，其他部分不变

**场景 B：HTML 单页增量编辑**
1. 创建 HTML 应用，输入「生成一个登录页面」
2. 等待生成完成
3. 输入「把按钮颜色改成绿色」
4. 验证：只修改按钮颜色，其他部分不变

**场景 C：全量重新生成**
1. 在已有代码的应用中，输入「重新生成一个完全不同的产品页」
2. 验证：生成全新的页面

### 3. 日志验证
```bash
# 查看 AI 响应是否保存到历史
grep "成功为 appId" logs/app.log
```

---

## Risks & Mitigations（风险与缓解）

| 风险 | 缓解措施 |
|------|---------|
| AI 仍然重写全部代码 | 优化提示词，强调「只修改必要部分」 |
| 大文件注入超 token 限制 | 限制注入的代码长度，或只注入关键部分 |
| 历史记录过大 | 保持 `maxCount=20` 限制 |
| 前端兼容性 | `editMode` 参数设置默认值 `false`，保持向后兼容 |

---

## Estimated Effort（工作量估计）

| 阶段 | 复杂度 |
|------|--------|
| Phase 1: API 参数 | 低 |
| Phase 2: Vue 修复 | 低 |
| Phase 3: HTML/多文件 | 中 |
| Phase 4: 前端 | 低 |
| Phase 5: 边界处理 | 中 |
| 测试验证 | 中 |

# Fix DeepSeek Reasoner reasoning_content Missing Field Error

## Overview

Fix the `Missing reasoning_content field` error when using DeepSeek's `deepseek-reasoner` model with tool calls during Vue project generation.

## Root Cause Analysis

### The Problem

When using DeepSeek's `deepseek-reasoner` model (thinking mode), the API requires:
1. Assistant messages with tool calls must include a `reasoning_content` field
2. The current LangChain4j OpenAI implementation doesn't handle DeepSeek's proprietary `reasoning_content` field
3. When the model returns a response with tool calls, the `reasoning_content` field is not captured and sent back to the API

### Error Details
```
Missing `reasoning_content` field in the assistant message at message index 2.
For more information, please refer to https://api-docs.deepseek.com/guides/thinking_mode#tool-calls
```

### DeepSeek API Behavior

According to [DeepSeek API documentation](https://api-docs.deepseek.com/guides/thinking_mode):
- In thinking mode, responses include `reasoning_content` (chain-of-thought) alongside `content` (final answer)
- When tool calls are used, the `reasoning_content` field MUST be included in subsequent requests
- This is a proprietary DeepSeek extension not part of the standard OpenAI API

## Implementation Steps

### Step 1: Identify Affected Files

1. **Custom OpenAiStreamingChatModel** - `src/main/java/dev/langchain4j/model/openai/OpenAiStreamingChatModel.java`
   - Line 176: Only reads `delta.content()`, missing `delta.reasoningContent()`

2. **Delta class** - Need to check if LangChain4j's `Delta` supports `reasoning_content`

### Step 2: Determine Solution Approach

**Option A: Switch to deepseek-chat (Recommended)**
- Change VUE_PROJECT generation to use `deepseek-chat` instead of `deepseek-reasoner`
- Pros: Simple, immediate fix, no code changes needed
- Cons: Loses reasoning capabilities

**Option B: Extend LangChain4j (Complex)**
- Fork/customize LangChain4j's `OpenAiStreamingChatModel` to handle `reasoning_content`
- Pros: Full reasoning capability
- Cons: High complexity, maintenance burden

**Option C: Use DeepSeek via non-thinking mode (Recommended)**
- Disable reasoning mode by not using `deepseek-reasoner` for tool calls
- Keep reasoning for non-tool scenarios only

### Step 3: Implement Quick Fix (Option A/C Hybrid)

#### Option 1: Update application-local.yml

**File**: `src/main/resources/application-local.yml`

Change from:
```yaml
reasoning-streaming-chat-model:
  model-name: deepseek-reasoner
```

To:
```yaml
reasoning-streaming-chat-model:
  model-name: deepseek-chat
```

#### Option 2: Create Non-Reasoning Config for Vue Projects

**CREATE**: `src/main/java/com/example/usercenterpractice/config/VueProjectStreamingChatModelConfig.java`
- Use `deepseek-chat` instead of `deepseek-reasoner` for Vue projects
- Reference: [ReasoningStreamingChatModelConfig.java](src/main/java/com/example/usercenterpractice/config/ReasoningStreamingChatModelConfig.java:1)

**UPDATE**: `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java`
- Line 129: Change from `reasoningStreamingChatModelPrototype` to new Vue-specific model
- Or use the same `streamingChatModelPrototype` used by HTML/MULTI_FILE

### Step 4: Long-term Solution (If Reasoning is Required)

If DeepSeek reasoning capability is essential for Vue projects:

1. **Fork LangChain4j** to add DeepSeek-specific support
2. **Submit PR** to LangChain4j for DeepSeek compatibility
3. **Use DeepSeek SDK directly** instead of LangChain4j

## Validation

### Test Commands

```bash
# Backend test
curl -X GET "http://localhost:8123/api/app/chat/gen/code?appId=2024455496353357826&message=create%20a%20simple%20vue%20app" \
  -H "Cookie: SESSION=..."

# Frontend test
# Use the Vue project generation feature and verify no error occurs
```

### Expected Results

- Vue project generation completes without `reasoning_content` error
- Generated code is properly saved to `tmp/code_output/vue_project_*`
- SSE stream returns valid responses

## Notes

### Potential Gotchas

1. **LangChain4j Version**: Current version 1.1.0 may not support DeepSeek reasoning mode
2. **API Breaking Change**: DeepSeek's reasoning_content is non-standard
3. **Message History**: Existing chat history may need migration if model changes

### Key Dependencies

- LangChain4J 1.1.0
- DeepSeek API (thinking mode)
- VUE_PROJECT code generation type

### Configuration Files

- `src/main/resources/application-local.yml` - Model configuration
- `src/main/java/com/example/usercenterpractice/config/ReasoningStreamingChatModelConfig.java` - Bean definition
- `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java` - Service factory

### Related Documentation

- [DeepSeek Thinking Mode API](https://api-docs.deepseek.com/guides/thinking_mode)
- [DeepSeek Tool Calls with Reasoning](https://api-docs.deepseek.com/guides/thinking_mode#tool-calls)

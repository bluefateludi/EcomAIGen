# Fix ChatModel Bean Conflicts - Implementation Plan

## Overview
Resolve Spring bean injection conflicts for ChatModel and StreamingChatModel by:
1. Creating explicit named beans for all AI models
2. Adding SpringContextUtil for dynamic bean retrieval
3. Updating all injection points to use specific bean names

**Complexity**: Medium
**Affected Modules**: AI config, Service factories, Utils

---

## Current Issues
- Multiple `ChatModel` beans defined without explicit names
- `AiCodeGenTypeRoutingServiceFactory` injects `ChatModel` without bean name (line 20)
- Missing `SpringContextUtil` class referenced in `AiCodeGeneratorServiceFactory` (line 127, 139)
- Risk of "No qualifying bean" errors when starting service

---

## Implementation Steps

### 1. CREATE SpringContextUtil utility class
**File**: `src/main/java/com/example/usercenterpractice/utils/SpringContextUtil.java`

```java
package com.example.usercenterpractice.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
```

**Reference pattern**: Standard Spring utility pattern
**Validation**: Class should compile and be scannable by `@ComponentScan`

---

### 2. UPDATE RoutingAiModelConfig - Add singleton bean
**File**: `src/main/java/com/example/usercenterpractice/config/RoutingAiModelConfig.java`

**Changes**:
```java
// Add new singleton bean after line 44:
@Bean
public ChatModel routingChatModel() {
    return OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .baseUrl(baseUrl)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .logRequests(logRequests)
            .logResponses(logResponses)
            .build();
}
```

**Reference**: Existing prototype bean pattern (lines 33-45)
**Validation**: Bean should be registered as "routingChatModel"

---

### 3. UPDATE StreamingChatModelConfig - Add singleton bean
**File**: `src/main/java/com/example/usercenterpractice/config/StreamingChatModelConfig.java`

**Changes**:
```java
// Add new singleton bean after line 41:
@Bean
public StreamingChatModel streamingChatModel() {
    return OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .logRequests(logRequests)
            .logResponses(logResponses)
            .build();
}
```

**Reference**: Existing prototype bean pattern (lines 30-42)
**Validation**: Bean should be registered as "streamingChatModel"

---

### 4. UPDATE ReasoningStreamingChatModelConfig - Add singleton bean
**File**: `src/main/java/com/example/usercenterpractice/config/ReasoningStreamingChatModelConfig.java`

**Changes**:
```java
// Add new singleton bean after line 41:
@Bean
public StreamingChatModel reasoningStreamingChatModel() {
    return OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .maxTokens(maxTokens)
            .temperature(temperature)
            .logRequests(logRequests)
            .logResponses(logResponses)
            .build();
}
```

**Reference**: Existing prototype bean pattern (lines 30-42)
**Validation**: Bean should be registered as "reasoningStreamingChatModel"

---

### 5. UPDATE AiCodeGenTypeRoutingServiceFactory - Specify bean name
**File**: `src/main/java/com/example/usercenterpractice/ai/AiCodeGenTypeRoutingServiceFactory.java`

**Change line 19-20**:
```java
// FROM:
@Resource
private ChatModel chatModel;

// TO:
@Resource(name = "routingChatModel")
private ChatModel chatModel;
```

**Reference pattern**: Already used in `AiCodeGeneratorServiceFactory` (line 29)
**Validation**: Injection should succeed without ambiguity

---

### 6. UPDATE AiCodeGeneratorServiceFactory - Fix StreamingChatModel injections
**File**: `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java`

**Change lines 33-37**:
```java
// FROM:
@Resource
private StreamingChatModel openAiStreamingChatModel;

@Resource
private StreamingChatModel reasoningStreamingChatModel;

// TO:
@Resource(name = "streamingChatModel")
private StreamingChatModel openAiStreamingChatModel;

@Resource(name = "reasoningStreamingChatModel")
private StreamingChatModel reasoningStreamingChatModel;
```

**Reference pattern**: ChatModel injection with name (line 29)
**Validation**: Both beans should inject correctly

---

### 7. UPDATE AiCodeGeneratorServiceFactory - Remove SpringContextUtil usage
**File**: `src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java`

**Remove lines 127, 139** - The SpringContextUtil calls are no longer needed since we inject the beans directly. The code should use the injected fields instead.

**Actually keep the existing pattern** - Upon review, the prototype beans are still needed for concurrent requests. The current implementation is correct, but we need to ensure the prototype beans have proper names.

---

## Validation Steps

### 1. Compile Check
```bash
mvn clean compile
```
Expected: No compilation errors

### 2. Bean Availability Check
```bash
mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=local'
```
Check logs for:
```
Registered bean: openAiChatModel
Registered bean: streamingChatModel
Registered bean: reasoningStreamingChatModel
Registered bean: routingChatModel
```

### 3. Startup Validation
Start application and verify no "No qualifying bean" exceptions

### 4. Functional Testing
- Create a new app (uses routing)
- Generate HTML code (uses streamingChatModel)
- Generate Vue project (uses reasoningStreamingChatModel)
- Verify concurrent requests don't interfere

---

## Configuration Requirements

Ensure `application-local.yml` has all required configs:
```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com
      api-key: sk-xxx
      model-name: deepseek-chat
    streaming-chat-model:
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
    reasoning-streaming-chat-model:
      base-url: https://api.deepseek.com
      model-name: deepseek-reasoner
    routing-chat-model:
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
```

---

## Notes

### Key Dependencies
- Spring Boot 3.5.6
- LangChain4j 1.1.0
- All configs must match existing property prefixes

### Potential Gotchas
1. **Bean Naming**: Default bean names are method names with lowercase first letter
   - ✅ `routingChatModel()` → bean name "routingChatModel"
   - ✅ `streamingChatModel()` → bean name "streamingChatModel"

2. **Prototype vs Singleton**:
   - Singleton beans for direct injection
   - Prototype beans (`*Prototype()`) for concurrent AI requests via SpringContextUtil

3. **Existing SpringContextUtil Usage**: Keep using it for prototype beans (lines 127, 139 in AiCodeGeneratorServiceFactory)

4. **Configuration Properties**: Must match exact prefixes in `@ConfigurationProperties`

### Testing Priority
1. High: Startup validation (prevent deployment failures)
2. High: Concurrent AI generation (verify prototype beans work)
3. Medium: Bean name conflicts (try injecting without name to ensure it fails as expected)

---

## File Summary

| File | Action | Lines |
|------|--------|-------|
| `utils/SpringContextUtil.java` | CREATE | ~30 lines |
| `config/RoutingAiModelConfig.java` | UPDATE | +7 lines |
| `config/StreamingChatModelConfig.java` | UPDATE | +7 lines |
| `config/ReasoningStreamingChatModelConfig.java` | UPDATE | +7 lines |
| `ai/AiCodeGenTypeRoutingServiceFactory.java` | UPDATE | 1 line |
| `ai/AiCodeGeneratorServiceFactory.java` | UPDATE | 2 lines |

**Total Changes**: 1 new file, 5 modified files, ~24 lines of code

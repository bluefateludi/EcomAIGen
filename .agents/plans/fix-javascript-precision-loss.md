# Fix JavaScript Precision Loss for Large IDs

## Overview
Fix critical bug where database IDs exceeding JavaScript's safe integer range (9007199254740991) lose precision during serialization, causing application lookup failures.

**Current Issue:**
- Backend creates app with ID: `2024415384286449666`
- Frontend receives and sends: `2024415384286449700`
- Lookup fails - user cannot enter chat page after creating app

## Root Cause
Jackson serializes `Long` IDs as JSON numbers. When JavaScript parses these numbers via `JSON.parse()`, values exceeding `Number.MAX_SAFE_INTEGER` (2^53-1) lose precision.

## Implementation Steps

### 1. CREATE Global Long Serializer (COMPLETED)
**File:** `src/main/java/com/example/usercenterpractice/config/LongToStringSerializerConfig.java`

Configure Jackson to serialize ALL `Long` values as strings globally:
```java
@Bean
public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return builder -> {
        builder.serializerByType(Long.class, new LongToStringSerializer());
        builder.serializerByType(Long.TYPE, new LongToStringSerializer());
    };
}
```

**Validation:** Compilation succeeds with `mvn compile`

### 2. VERIFY Request/Response DTOs
**Files to check:**
- `src/main/java/com/example/usercenterpractice/model/vo/AppVO.java`
- `src/main/java/com/example/usercenterpractice/model/vo/UserVO.java`
- `src/main/java/com/example/usercenterpractice/model/vo/LoginUserVO.java`
- `src/main/java/com/example/usercenterpractice/common/DeleteRequest.java`
- All Request DTOs

**Current state:** All use `Long id` - serializer will handle these automatically

### 3. UPDATE String ID Parameter Handling
**File:** `src/main/java/com/example/usercenterpractice/controller/AppController.java`

The `/app/get/vo` endpoint already accepts String ID (line 267):
```java
@GetMapping("/get/vo")
public BaseResponse<AppVO> getAppVOById(@RequestParam("id") String id) {
    Long appId = Long.parseLong(id);
    // ...
}
```

**This is correct** - keeps string handling on web layer, converts to Long for service layer.

### 4. VERIFY Frontend Types
**Files:**
- `EcomAIGen-fronted/src/typings.d.ts`
- `EcomAIGen-fronted/src/api/appController.ts`
- `EcomAIGen-fronted/src/pages/HomePage.vue`
- `EcomAIGen-fronted/src/pages/AppChatPage.vue`

**Ensure:** All ID fields are typed as `string` not `number`

### 5. TEST Complete Flow
**Test Steps:**
1. Start backend: `mvn spring-boot:run`
2. Create new app from frontend
3. Check backend logs for ID format
4. Verify redirect to chat page works
5. Check browser Network tab - verify IDs are strings in JSON

## Validation

### Backend Verification
```bash
# Check response format - IDs should be quoted strings
curl -s http://localhost:8123/api/app/get/vo?id=123 | jq .

# Expected: "id": "2024415384286449666" (string, not number)
```

### Frontend Verification
1. Open browser DevTools → Network tab
2. Create new app
3. Check response: `{ "code": 0, "data": "2024415384286449666" }` (quoted string)
4. Check next request: URL parameter should match exactly

### End-to-End Test
1. Create app → Success message
2. Auto-redirect to `/app/chat/{id}`
3. Chat page loads successfully
4. Can send messages

## Notes

### Impact Scope
- **Affects:** All API responses containing Long IDs (App, User, ChatHistory)
- **Breaking change:** No - frontend already handles string conversion
- **Performance:** Minimal - string vs number serialization difference is negligible

### Key Dependencies
- Spring Boot 3.5.6
- Jackson `Jackson2ObjectMapperBuilderCustomizer`
- Existing `@RequestParam String id` pattern in controllers

### Gotchas
1. **Request bodies:** `DeleteRequest` uses `Long id` - Jackson deserializes string to Long automatically
2. **Path variables:** Consider updating to `@PathVariable String id` pattern for consistency
3. **Database queries:** Service layer still uses `Long` - only serialization layer changes
4. **Testing:** Ensure any integration tests expecting JSON numbers are updated

### Future Considerations
- For new projects, use `String` IDs throughout (UUID or string-encoded longs)
- Consider `@JsonFormat(shape = JsonFormat.Shape.STRING)` on specific fields if global serializer causes issues
- Monitor performance - string serialization is slightly slower but safer

## Files Modified
- `src/main/java/com/example/usercenterpractice/config/LongToStringSerializerConfig.java` (NEW)
- All existing VO/DTO classes (no changes needed - handled by serializer)

# Fix Vue Generation and Build Issues

## Overview
Fix two critical issues:
1. Vue generation gets stuck at "选择工具 -> 写入文件" step
2. Repetitive file writing causing infinite loops

## Implementation Steps

### 1. [UPDATE] AiCodeGeneratorFacade.java - Align with tutorial project (SYNC BUILD)
- File: src/main/java/com/example/usercenterpractice/ai/core/AiCodeGeneratorFacade.java:157-159
- Change: buildProjectAsync() → buildProject()
- Reason: Tutorial project uses synchronous build to ensure project is ready
- Code: Same as tutorial project line 127-130

### 2. [UPDATE] AiCodeGeneratorServiceFactory.java - Add missing codeGenTypeEnum parameter
- File: src/main/java/com/example/usercenterpractice/ai/AiCodeGeneratorServiceFactory.java
- Change: getAiCodeGeneratorService(appId) → getAiCodeGeneratorService(appId, codeGenTypeEnum)
- Reason: Align with tutorial project's parameter pattern
- Reference: Tutorial project line 54 and 84

### 3. [UPDATE] AiCodeGeneratorFacade.java - Add codeGenTypeEnum to service calls
- File: src/main/java/com/example/usercenterpractice/ai/core/AiCodeGeneratorFacade.java:55 and 84
- Add codeGenTypeEnum parameter to getAiCodeGeneratorService calls
- Ensure consistent service instance creation

### 4. [CHECK] JsonMessageStreamHandler.java - Verify tool execution pattern
- File: src/main/java/com/example/usercenterpractice/ai/handler/JsonMessageStreamHandler.java
- Ensure no tool filtering logic prevents repeated tool calls
- Reference: Tutorial project tool execution flow

## Validation
- Test commands:
  1. Clean all vue_project_* directories
  2. Generate Vue project
  3. Monitor tool execution in browser console
  4. Verify single build execution
  5. Check for no duplicate files

- Manual testing steps:
  1. Open browser dev tools
  2. Start Vue project generation
  3. Watch for continuous tool execution
  4. Verify stream completes without hanging
  5. Check dist directory is created once

## Notes
- Critical fix: Switch from async to sync build (matches tutorial project exactly)
- Key difference: Tutorial project uses buildProject() not buildProjectAsync()
- Priority: Synchronous build is essential for proper completion
- Reference: Tutorial project AiCodeGeneratorFacade.java:127-130
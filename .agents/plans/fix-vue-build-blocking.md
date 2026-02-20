# Fix Vue Project Build Blocking Issue

## Overview
Vue project generation is stuck in loading state because the build process blocks the streaming response. Need to switch from synchronous to asynchronous build.

## Problem Analysis
- **Current Issue**: In `AiCodeGeneratorFacade.processTokenStream()`, the build call is synchronous, blocking the entire streaming response
- **Reference**: Tutorial project uses `buildProjectAsync()` in `JsonMessageStreamHandler`
- **Impact**: Users see "加载中" indefinitely while npm install/build completes

## Implementation Steps

1. [UPDATE] src/main/java/com/example/usercenterpractice/ai/core/AiCodeGeneratorFacade.java - line 118-129
   - Change `vueProjectBuilder.buildProject(projectPath);` to `vueProjectBuilder.buildProjectAsync(projectPath);`
   - Remove the synchronous call that blocks the stream

2. [ADD] src/main/java/com/example/usercenterpractice/ai/core/builder/VueProjectBuilder.java - ensure async method exists
   - Verify `buildProjectAsync()` method exists (should already be there)

## Validation
- Test commands:
  - Start the application
  - Create a Vue project via chat
  - Verify streaming completes and shows "生成完成"
  - Check if build continues asynchronously in logs
- Manual testing steps:
  - Monitor console for build logs after stream ends
  - Confirm dist directory is eventually created

## Notes
- The build will now happen in background without blocking user experience
- Need to ensure build errors are properly logged (current implementation handles exceptions)
- No database changes required
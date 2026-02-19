# Fix Preview URL Not Working

## Overview
Fix the preview functionality that stopped working after the deploy fix. The preview URL (`http://localhost:5173/api/static/html_xxx/`) returns 404 because the static resource controller now only reads from `code_deploy`, but preview needs to read from `code_output`.

## Root Cause
After fixing the deploy issue, we changed the static resource controller to read from `DEPLOY_ROOT_DIR` (code_deploy). However, the preview functionality still needs to read from `code_output` directory where generated files are stored initially.

## Solution
Create separate endpoints for deploy and preview:
- Deploy: `/api/static/{deployKey}/**` -> reads from `code_deploy`
- Preview: `/api/static/preview/{codeGenType}_{appId}/**` -> reads from `code_output`

## Implementation Steps

### 1. Update Static Resource Controller
**File**: `src/main/java/com/example/usercenterpractice/controller/AppController.java`

Add a new endpoint for preview functionality:
```java
@GetMapping("/preview/{path}/**")
public ResponseEntity<Resource> servePreviewResource(
        @PathVariable String path,
        HttpServletRequest request)
```
- Reads from `PREVIEW_ROOT_DIR` (code_output)
- Handles the same way as deploy (directory redirect, index.html default)
- Keep existing deploy endpoint unchanged

### 2. Update Frontend Preview URL Generation
**File**: `EcomAIGen-fronted/src/config/env.ts`

Update `getStaticPreviewUrl()` to use the new preview endpoint:
```typescript
export const getStaticPreviewUrl = (codeGenType: string, appId: string) => {
  const baseUrl = `${STATIC_BASE_URL}/preview/${codeGenType}_${appId}/`
  // Vue project handling remains the same
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return `${baseUrl}dist/index.html`
  }
  return baseUrl
}
```

## Validation
1. Restart backend
2. Generate code in an app
3. Verify preview iframe loads correctly
4. Verify deploy still works

## Notes
- The deploy endpoint remains `/api/static/{deployKey}/**`
- The preview endpoint becomes `/api/static/preview/{path}/**`
- No breaking changes to existing deployed applications

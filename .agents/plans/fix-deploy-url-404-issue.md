# Fix Deploy URL 404 Issue

## Overview
Fix the issue where deployed applications show "404 Not Found" when accessed via the returned URL (e.g., `http://localhost/DU34GK/`). The problem is that the deployment URL returned by the backend doesn't include the proper API path to access the static resource endpoint.

## Root Cause
- Backend returns: `http://localhost/DU34GK/`
- Static resource endpoint is: `http://localhost:8123/api/static/{deployKey}/**`
- The URL is missing the port (`8123`) and API path (`/api/static`)

## Implementation Steps

### 1. Update Backend Deployment Host Constant
**File**: `src/main/java/com/example/usercenterpractice/constant/AppConstant.java`

- Change `CODE_DEPLOY_HOST` from `"http://localhost"` to `"http://localhost:8123/api/static"`
- This ensures the deployment URL includes the correct port and API path

**Reference**: Line 28 in `AppConstant.java`

### 2. Update Frontend Environment Variable
**File**: `EcomAIGen-fronted/.env.development`

- Change `VITE_DEPLOY_DOMAIN` from `http://localhost` to `http://localhost:8123/api/static`
- This aligns frontend configuration with backend

### 3. Update Frontend env.ts Helper Function
**File**: `EcomAIGen-fronted/src/config/env.ts`

- Update `getDeployUrl()` function to use the new domain format
- Ensure consistency across the application

## Validation

### Test Commands
1. Build backend: `mvn clean package`
2. Start backend: `java -jar target/user-centerpractice-0.0.1-SNAPSHOT.jar`
3. Start frontend: `cd EcomAIGen-fronted && npm run dev`

### Manual Testing Steps
1. Login to the application
2. Create a new app or use existing app
3. Generate code via chat
4. Click "部署" (Deploy) button
5. Verify the returned URL format is `http://localhost:8123/api/static/{deployKey}/`
6. Click "访问网站" to open the deployed site
7. Verify the site loads correctly in new tab

## Notes
- The static resource controller at `AppController.java:374-431` already handles `/api/static/{deployKey}/**`
- Vite proxy configuration doesn't affect this since the URL opens in a new tab
- No changes needed to `AppServiceImpl.java` as it uses `AppConstant.CODE_DEPLOY_HOST`
- Vue projects are handled specially (build to dist directory) - this fix applies to all code gen types

## Key Dependencies
- `AppConstant.java` - Single source of truth for deployment URL
- `AppServiceImpl.java:243` - Uses `CODE_DEPLOY_HOST` constant
- `StaticResourceController` - Handles the actual static file serving
- Frontend `env.ts` - Must align with backend configuration

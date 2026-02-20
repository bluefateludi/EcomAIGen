# Fix Deploy and Preview System

## Overview
Implement complete deployment and preview functionality. Currently missing deployKey generation and proper URL routing for deployed applications.

## Problem Analysis
- **Missing Features**: No deployKey field, no deployment API, no proper preview URLs
- **Tutorial Reference**: yu-ai-code-mother has full deployment system with deployKey
- **Current State**: Deploy returns 404, preview URLs don't work

## Implementation Steps

1. [UPDATE] src/main/java/com/example/usercenterpractice/model/domain/App.java - Add deploy fields
   - Add `deployKey` field (varchar(64))
   - Add `deployedTime` field (datetime)
   - Add getter/setter methods

2. [UPDATE] src/main/java/com/example/usercenterpractice/mapper/AppMapper.java - Add column mappings
   - Add deployKey and deployedTime to mapper queries

3. [CREATE] src/main/java/com/example/usercenterpractice/model/dto/app/AppDeployRequest.java
   - Request DTO for deployment operation

4. [UPDATE] src/main/java/com/example/usercenterpractice/service/AppService.java - Add deployment logic
   - Add `deployApp()` method that generates unique deployKey
   - Store deployKey and deploy timestamp

5. [UPDATE] src/main/java/com/example/usercenterpractice/controller/AppController.java - Add deploy endpoint
   - Add `POST /app/deploy` endpoint
   - Return deployKey in response

6. [ADD] src/main/java/com/example/usercenterpractice/config/DeployConfig.java - Configuration
   - Add deployment domain configuration
   - Support custom domains

7. [UPDATE] src/main/java/com/example/usercenterpractice/model/vo/AppVO.java - Add deploy fields
   - Include deployKey in response VO

8. [UPDATE] Frontend deployment configuration
   - Update EcomAIGen-fronted/src/config/env.ts with DEPLOY_DOMAIN
   - Update AppChatPage.vue to show deploy URL when available

9. [ADD] Static file serving for deployed apps
   - Configure Spring Boot to serve static files from deploy directory

## Validation
- Test commands:
  - Create an app and deploy it
  - Verify deployKey is generated and returned
  - Access app via deploy URL
  - Test preview functionality
- Manual testing steps:
  - Create multiple apps and verify unique deployKeys
  - Test deployed apps are accessible
  - Confirm static files serve correctly

## Notes
- Use UUID for deployKey generation
- Need to configure static file serving for /{deployKey} route
- Tutorial project has complete reference implementation
- Consider adding deployment status tracking
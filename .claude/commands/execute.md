---
description: "Execute implementation plan"
argument-hint: [path-to-plan]
---

# Execute: Implement Plan

## Read Plan
Load plan from: `$ARGUMENTS`

## Execution Process

### 1. Read & Understand
- Read entire plan
- Note task dependencies
- Check validation commands

### 2. Execute Tasks Sequentially

For EACH task:
```
a. Read existing files (if modifying)
b. Implement as specified
c. Verify syntax/imports
```

### 3. Run Validation
```bash
# Execute all validation commands from plan
```

**⚠️ Manual Build & Test**
- DO NOT run Maven commands (compile/test/package)
- DO NOT run tests or start application
- User handles all build/test via IDEA manually
- Only write test files - DO NOT execute

If any code validation (non-build) fails:
- Fix the issue and re-check

### 4. Final Checks
- ✅ All tasks completed
- ✅ All code written (tests included, NOT executed)
- ✅ Code follows conventions
- ⏭️ Build/test deferred to user (manual IDEA execution)

## Report

### Completed
- Files created: [list]
- Files modified: [list]

### Tests
- Test files: [list]
- Results: [output]

### Validation
```bash
# Command outputs
```

### Status
Ready for `/commit`

## Notes
- **NEVER run Maven build/test commands** - User handles via IDEA
- Only write test code, don't execute
- Follow plan specifications exactly

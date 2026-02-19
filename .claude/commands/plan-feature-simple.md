---
description: "Plan feature with focused analysis"
---

# Quick Feature Planning

## Goal
Transform feature request into actionable implementation plan for one-pass success.

## Core Principles
- **Essential research only**: Find key files and patterns quickly
- **Action over perfection**: Focus on executable steps, not perfect docs
- **Simple clarity**: Express complex ideas simply

## Planning Process (3 steps)

### 1. Quick Understanding
- What does this feature do?
- Which modules are involved?
- Complexity level (Low/Medium/High)?

### 2. Code Analysis
Find key content:
- Related files (use Glob and Grep)
- Code patterns (review existing implementations)
- Testing approach (check test files)

### 3. Generate Plan

Output format:
```markdown
# Feature Name

## Overview
Brief description of purpose

## Implementation Steps
1. [CREATE/UPDATE/ADD] file_path - specific task
   - Reference pattern: file:line
   - Validation: command or method

2. Next step...

## Validation
- Test commands
- Manual testing steps

## Notes
- Potential gotchas
- Key dependencies
```

## Usage Example
```
User: Add user like feature

AI:
1. Understand: Need like table, API, frontend display
2. Analyze: Find similar user_level table structure
3. Plan:
   - Create Like entity and table
   - Implement LikeService
   - Add API endpoints
   - Validate: Test like functionality
```

## Output
Save to `.agents/plans/{feature-name}.md`

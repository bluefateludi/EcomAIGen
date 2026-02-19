---
description: Create concise Product Requirements Document
argument-hint: [output-filename]
---

# Create PRD - Concise Product Requirements Doc

## Overview
Generate **concise and practical** PRD from conversation history, focusing on essential info and actionability.

**IMPORTANT LANGUAGE RULE**: All generated PRD content MUST be in **中文 (Chinese)**, except for technical terms, code, API endpoints, and configuration values.

## Output File
Default: `PRD.md`, or specify: `$ARGUMENTS`

## PRD Structure (Concise)

### Required Sections

**1. Project Overview**
- 1-2 sentences describing what the project is
- Core value proposition (1 sentence)
- Current status (MVP/In Progress/Completed)

**2. Target Users**
- Main user types (2-3)
- Key pain points (2-3 per user type)
- Technical level required

**3. Feature Scope**
- ✅ **Implemented**: Core feature list
- 🚧 **In Progress**: Features being developed
- 📋 **Planned**: Next steps (by priority)
- ❌ **Out of Scope**: Explicitly not doing

**4. Tech Stack**
- Backend: framework + language + database
- Frontend: framework + main libraries
- AI/Tools: key dependencies
- Versions (key components only)

**5. Architecture Overview**
- System architecture (concise text description)
- Directory structure (2-3 levels max)
- Key design patterns (3-5)

**6. Core Features**
Each feature includes:
- Feature name: one-line description
- Key operations: 3-5 bullet points
- Technical notes: key tech or dependencies

**7. API Endpoints** (optional, if backend)
- List main endpoints (max 10)
- Format: `METHOD /path` - brief description

**8. Data Models** (optional, complex projects)
- Core tables/entities (3-5)
- Key fields and relationships

**9. Security & Config**
- Environment variables list
- Key config items
- Security measures (implemented + planned)

**10. Next Steps**
- Prioritized task list
- Each task: goal + acceptance criteria

## Generation Principles

### ✅ Do
- **Concise**: Keep only essential info in each section
- **Readable**: Use lists, tables, icons
- **Practical**: Focus on dev-needed info
- **Updated**: Clearly mark current status

### ❌ Don't
- No lengthy user stories (use feature lists instead)
- No exhaustive API docs (list main ones only)
- No repeated tech stack details (reference docs)
- No excessive risks/future planning

## Output Example

```markdown
# Project Name PRD

## Overview
One sentence about what this project is.
Core value: What problem it solves.

Current status: Phase 3 complete, Phase 4 in progress

## Tech Stack
- Backend: Spring Boot 3.5.5 + Java 21
- Frontend: Vue 3 + Element Plus
- Database: MySQL + Redis

## Core Features
1. User Management: Registration, login, salary tracking
2. AI Challenges: Generate questions, intelligent scoring
3. Leaderboard: Real-time ranking

## Next Steps
- [ ] High: Distributed locking
- [ ] Medium: Cache optimization

## Config
- Port: 8123
- API prefix: /api
```

## Quality Checks
- ✅ Total length under 200 lines
- ✅ Each section scannable at a glance
- ✅ Devs can quickly find needed info
- ✅ Next steps are clear and actionable

## Notes
- Generate from **current conversation**, don't over-assume
- Ask user if critical info is missing
- Recommend manual simplification to under 150 lines

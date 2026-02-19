# EcomAIGen 产品需求文档 (PRD)

## 项目概述

**EcomAIGen** 是一个基于 AI 的电商代码生成平台，允许用户通过自然语言提示词快速生成 Web 应用代码。

核心价值：降低开发门槛，让非技术人员也能快速创建电商类 Web 应用。

当前状态：**MVP 阶段**，核心功能已实现，正在进行性能优化和 bug 修复。

## 目标用户

| 用户类型 | 主要痛点 | 技术水平 |
|---------|---------|---------|
| **创业者/小商户** | 需要快速搭建电商展示页面，但无技术背景 | 无编程基础 |
| **开发者** | 需要快速生成项目脚手架，避免重复劳动 | 有一定编程经验 |
| **内容创作者** | 需要展示个人作品或产品目录 | 入门级 |

## 功能范围

### ✅ 已实现
- 用户注册/登录/权限管理
- AI 智能代码生成类型路由（HTML/Multi-File/Vue Project）
- 流式代码生成（SSE）
- 应用部署与预览
- 腾讯云 COS 存储集成
- 网页自动截图生成封面
- Redis 缓存与会话管理
- 限流保护（基于 Redisson）
- 聊天历史记录

### 🚧 进行中
- Long 类型序列化优化（解决 JS 精度丢失问题）
- 前端大数字精度处理

### 📋 规划中
- 多租户支持
- 更丰富的代码模板
- AI 对话上下文增强
- 应用市场/分享功能

### ❌ 超出范围
- 用户自定义域名
- 数据库自动迁移
- 多语言国际化

## 技术栈

### 后端
- **框架**: Spring Boot 3.5.6
- **语言**: Java 21
- **数据库**: MySQL 8.x + Redis
- **ORM**: MyBatis-Plus 3.5.7
- **AI**: LangChain4J 1.1.0
- **工具**: Hutool 5.8.25, Lombok, Knife4j 4.4.0

### 前端
- **框架**: Vue 3.5.17 + TypeScript 5.8
- **构建**: Vite 7.0.0
- **UI**: Ant Design Vue 4.2.6
- **状态**: Pinia 3.0.3
- **路由**: Vue Router 4.5.1

### 基础设施
- **对象存储**: 腾讯云 COS
- **缓存**: Redis + Redisson 3.50.0
- **截图**: Selenium 4.33.0

## 架构概览

### 系统架构
```
用户 → 前端(Vue3) → 后端API(Spring Boot) → AI服务(LangChain4J) → 代码生成
                                              ↓
                                         Redis缓存/会话
                                              ↓
                                         MySQL数据库
                                              ↓
                                         腾讯云COS存储
```

### 目录结构
```
src/main/java/com/example/usercenterpractice/
├── ai/                    # AI 代码生成核心
│   ├── core/             # 门面、解析器、保存器
│   ├── handler/          # SSE 流处理器
│   ├── parser/           # 代码解析策略
│   ├── saver/            # 文件保存模板
│   └── tools/            # AI 工具实现
├── controller/           # REST 接口
├── service/              # 业务逻辑
├── mapper/               # 数据访问
├── model/                # 实体/DTO/VO
├── config/               # 配置类
├── ratelimit/            # 限流组件
└── utils/                # 工具类
```

### 设计模式
- **工厂模式**: AI 服务工厂、类型路由服务工厂
- **策略模式**: 代码解析器、文件保存器
- **门面模式**: AiCodeGeneratorFacade
- **AOP**: 权限拦截、限流切面

## 核心功能

### 1. 用户管理
- 用户注册/登录
- 权限控制（普通用户/管理员）
- 会话管理（Redis Session）

### 2. AI 代码生成
- **智能路由**: 根据提示词自动选择生成类型
- **三种类型**: HTML 单页 / 多文件 / Vue 项目
- **流式返回**: SSE 实时推送生成进度
- **工具调用**: 支持文件读写、修改等操作

### 3. 应用管理
- 应用创建/编辑/删除
- 代码生成与保存
- 应用部署与预览
- 代码打包下载

### 4. 聊天历史
- 对话记录持久化
- 支持多轮对话上下文

## API 接口

### 用户相关
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/get/login` - 获取当前登录用户
- `POST /api/user/logout` - 用户登出

### 应用相关
- `POST /api/app/add` - 创建应用
- `POST /api/app/update` - 更新应用名称
- `POST /api/app/delete` - 删除应用
- `POST /api/app/my/list/page/vo` - 我的应用列表
- `POST /api/app/good/list/page/vo` - 精选应用列表
- `GET /api/app/get/vo?id={id}` - 获取应用详情
- `GET /api/app/chat/gen/code` - AI 聊天生成代码（SSE）
- `POST /api/app/deploy` - 部署应用
- `GET /api/app/download/{appId}` - 下载应用代码

### 管理员接口
- `POST /api/app/admin/delete` - 管理员删除应用
- `POST /api/app/admin/update` - 管理员更新应用
- `GET /api/app/admin/get/vo` - 管理员获取应用详情
- `POST /api/app/admin/list/page/vo` - 管理员应用列表

## 数据模型

### 核心实体

**User (用户)**
- id, userAccount, userPassword, userName, userAvatar
- userRole (user/admin), createTime, updateTime

**App (应用)**
- id, userId, appName, initPrompt, codeGenType
- deployKey, cover, priority, createTime, editTime, deployedTime

**ChatHistory (聊天历史)**
- id, appId, userId, content, messageType
- createTime

## 安全与配置

### 环境变量
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecomaigen
    username: root
    password: [your-password]
  data:
    redis:
      host: localhost
      port: 6379
      password: ""
```

### 关键配置
- 服务端口: 8123
- API 前缀: /api
- Session 过期: 30 天
- 限流: 5 次/分钟/用户

### 安全措施
- 密码加密存储
- AOP 权限拦截
- Redis 限流保护
- 输入安全校验
- CORS 跨域配置

## 下一步计划

### 高优先级
- [ ] **提交当前修改**: Long 序列化配置、公共接口优化
- [ ] **前端对接**: 处理大数字精度问题
- [ ] **测试验证**: 确保 ID 传输无误

### 中优先级
- [ ] **缓存优化**: 扩展缓存应用场景
- [ ] **性能优化**: 并发生成测试
- [ ] **错误处理**: 统一异常响应格式

### 低优先级
- [ ] **监控告警**: 接入监控系统
- [ ] **文档完善**: API 文档自动生成
- [ ] **单元测试**: 提升测试覆盖率

## 注意事项

1. **JavaScript 精度问题**: Long 类型 ID 需转为 String 传输
2. **AI 服务多例**: 每个 App 有独立的 AI 服务实例
3. **Vue 项目构建**: 部署前需执行 npm run build
4. **文件路径**: 代码输出目录为 `tmp/code_output/`
5. **Redis 依赖**: 缓存、会话、限流都依赖 Redis

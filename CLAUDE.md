# Claude 项目说明文档

## 项目关系说明

### 📁 项目结构

- **当前项目**: `D:\Program code\java\user-centerpractice`
  - 这是 **我的项目** - EcomAIGen 电商 AI 代码生成平台
  - 基于 Spring Boot 3 + Vue 3 的全栈应用
  - 已实现 MVP 版本核心功能

- **参考教程**: `D:\Program code\gitpro\yu-ai-code-mother`
  - 这是 **别人的教程项目** - yu-ai-code-mother
  - 提供 AI 代码生成架构的设计思路和最佳实践

### 🔄 项目关系

当前项目是在参考教程项目架构基础上进行实现和优化的独立项目，主要关系如下：

1. **参考关系**: 借鉴了教程项目的架构设计和实现思路
2. **实现独立**: 代码是独立编写和实现的，不是简单的复制
3. **功能扩展**: 在教程基础上增加了电商特定的功能模块
4. **技术优化**: 根据实际需求进行了技术选型和优化

---

## 开发思路（参考教程项目）

### 🏗️ 整体架构设计

#### 1. 前后端分离架构
```yaml
架构流程:
用户 → 前端(Vue3) → 后端API(Spring Boot) → AI服务 → 代码生成
                              ↓
                          Redis缓存/会话
                              ↓
                          MySQL数据库
                              ↓
                          腾讯云COS存储
```

#### 2. 核心设计模式

**工厂模式** - AI 服务管理
- 使用 Caffeine 缓存 AI 服务实例
- 基于 appId 的服务实例隔离
- 支持多种代码生成类型的路由

**策略模式** - 代码生成策略
- HTML 单页生成
- 多文件项目生成
- Vue 项目生成（带工具调用）

**门面模式** - 统一入口
- `AiCodeGeneratorFacade` 统一代码生成接口
- 简化调用方逻辑，隐藏内部复杂性

### 🤖 AI 代码生成设计

#### 1. 多模型策略
```java
// 根据任务类型选择不同的 AI 模型
switch (codeGenType) {
    case VUE_PROJECT ->
        // 使用推理模型 + 工具调用能力
        yield AiServices.builder(AiCodeGeneratorService.class)
            .streamingChatModel(reasoningStreamingChatModel)
            .tools(toolManager.getAllTools())
            .build();
    case HTML, MULTI_FILE ->
        // 使用标准流式模型
        yield AiServices.builder(AiCodeGeneratorService.class)
            .streamingChatModel(openAiStreamingChatModel)
            .build();
}
```

#### 2. 工具调用机制
- AI 可以执行文件读写、目录浏览等操作
- 基于工具名称和参数调用的机制
- 支持工具执行结果的反馈

#### 3. 对话记忆管理
- 基于 appId 的独立对话记忆
- Redis 存储历史对话
- 可配置的对话窗口大小

### 💻 前端设计思路

#### 1. 组件化架构
- 基于 Vue 3 Composition API
- Ant Design Vue 组件库
- Pinia 状态管理

#### 2. 实时交互
- Server-Sent Events (SSE) 流式响应
- 实时显示 AI 生成进度
- 支持工具调用事件的展示

### 🛠️ 技术选型

#### 后端技术栈
```yaml
Spring Boot: 3.5.6          # 主框架
Java: 21                    # 语言版本
MyBatis-Plus: 3.5.7        # ORM 框架
LangChain4j: 1.1.0         # AI 框架
Redis: 数据库 + 缓存        # 缓存和会话
MySQL: 主数据库             # 数据持久化
```

#### 前端技术栈
```yaml
Vue: 3.5.17                # 前端框架
TypeScript: 5.8            # 类型支持
Vite: 7.0.0                # 构建工具
Ant Design Vue: 4.2.6      # UI 组件库
Pinia: 3.0.3               # 状态管理
```

### 📊 关键设计决策

#### 1. 流式响应设计
- 使用 Reactor 的 Flux 处理流式响应
- 支持部分响应、工具调用、完成事件
- 异步处理，不阻塞主线程

#### 2. 缓存策略
- **Caffeine**: 本地缓存 AI 服务实例
- **Redis**: 会话管理和对话记忆
- **多级缓存**: 提升系统性能

#### 3. 并发处理
- 使用 prototype bean 解决并发问题
- 每个应用独立的 AI 服务实例
- 限流保护防止滥用

---

## 项目特色功能

### 🛒 电商特性增强

1. **电商模板支持**
   - 针对电商场景优化的提示词模板
   - 商品展示、购物车、订单管理等组件生成

2. **部署与预览**
   - 自动生成预览 URL
   - 网页截图生成应用封面
   - 一键部署功能

3. **应用管理**
   - 应用分类和标签
   - 精选应用展示
   - 代码打包下载

### 🔧 技术优化

1. **大数字处理**
   - Long 类型序列化为 String
   - 解决 JavaScript 精度丢失问题

2. **性能优化**
   - 多级缓存策略
   - 异步处理机制
   - 连接池优化

3. **安全增强**
   - 角色权限控制
   - API 限流保护
   - 输入参数校验

---

## 开发规范

### 📝 代码风格

1. **命名规范**
   - 使用有意义的英文变量名
   - 类名使用 PascalCase
   - 方法名使用 camelCase

2. **注释规范**
   - 关键业务逻辑添加注释
   - 复杂算法提供说明
   - API 接口添加文档

3. **异常处理**
   - 使用统一的异常处理机制
   - 提供友好的错误信息
   - 记录详细的错误日志

### 🔄 Git 工作流

1. **提交规范**
   ```
   feat: 新功能
   fix: Bug 修复
   docs: 文档更新
   style: 代码格式化
   refactor: 重构
   test: 测试相关
   chore: 构建或辅助工具变动
   ```

2. **分支策略**
   - master: 主分支，始终保持稳定
   - develop: 开发分支
   - feature/*: 功能分支

### 🧪 测试策略

1. **单元测试**
   - 核心业务逻辑必须写单元测试
   - 使用 JUnit 5 + Mockito
   - 保持测试覆盖率 > 80%

2. **集成测试**
   - API 接口测试
   - 数据库操作测试
   - AI 服务测试

---

## 注意事项

### ⚠️ 重要提醒

1. **JavaScript 精度问题**
   - 后端 Long 类型传给前端必须转为 String
   - 前端处理大数字时使用 BigInt

2. **AI 服务管理**
   - 每个 App 必须有独立的 AI 服务实例
   - 确保 Redis 会话管理正常工作

3. **Vue 项目构建**
   - Vue 项目生成后需要执行 npm run build
   - 构建是异步执行的，不阻塞流式响应

4. **文件路径**
   - 代码输出目录: `tmp/code_output/`
   - 确保目录有足够的权限

### 🚀 性能监控

1. **监控指标**
   - AI 响应时间
   - 并发请求数
   - 缓存命中率

2. **日志记录**
   - 关键操作记录日志
   - 性能数据统计
   - 错误追踪

---

## 项目状态

### 📈 当前进展

- ✅ **MVP 阶段完成**: 核心功能已实现
- 🚧 **进行中**: 性能优化和 Bug 修复
- 📋 **规划中**: 更多功能和优化

### 🎯 下一步计划

1. **高优先级**
   - 完善前端大数字处理
   - 优化 AI 服务性能
   - 增加单元测试

2. **中优先级**
   - 扩展缓存应用场景
   - 完善错误处理机制
   - 增加监控功能

3. **低优先级**
   - 完善文档
   - 增加用户反馈功能
   - 应用市场功能

---

## 联系方式

如有问题或建议，请通过以下方式联系：

- 项目负责人: [您的姓名]
- 邮箱: [您的邮箱]
- 项目地址: D:\Program code\java\user-centerpractice
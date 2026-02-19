 # 用户登录系统实现计划

## 📋 项目概述
基于现有用户表结构实现完整的用户登录系统，包括注册、登录、登出、权限管理等功能。

## 🗄️ 数据库表分析
现有 `user` 表包含以下字段：
- `id`: 主键
- `userAccount`: 账号（唯一索引）
- `userPassword`: 密码
- `userName`: 用户昵称
- `userAvatar`: 用户头像
- `userProfile`: 用户简介
- `userRole`: 用户角色（user/admin）
- `editTime`: 编辑时间
- `createTime`: 创建时间
- `updateTime`: 更新时间
- `isDelete`: 逻辑删除标记

## 📦 第一步：添加Maven依赖 ✅ 已完成

已在 `pom.xml` 中添加以下依赖：

1. ✅ **MyBatis-Plus** (3.5.7) - ORM框架
2. ✅ **Validation** - 参数校验
3. ✅ **Hutool** (5.8.25) - 工具类库
4. ✅ **Knife4j** (4.4.0) - API文档
5. ✅ **Lombok** - 简化代码
6. ✅ **MySQL Connector** - 数据库驱动

## 🏗️ 第二步：创建包结构

```
com.example.usercenterpractice
├── common/                    # 通用模块
│   ├── BaseResponse.java     # 统一响应封装
│   └── ErrorCode.java        # 错误码枚举
├── config/                   # 配置类
│   ├── MyBatisPlusConfig.java    # MyBatis-Plus配置
│   └── WebMvcConfig.java         # MVC配置
├── constant/                 # 常量定义
│   └── UserConstant.java        # 用户常量
├── controller/               # 控制层
│   └── UserController.java      # 用户接口
├── service/                  # 服务层
│   ├── UserService.java         # 用户服务接口
│   └── impl/
│       └── UserServiceImpl.java # 用户服务实现
├── mapper/                   # 数据访问层
│   └── UserMapper.java          # 用户Mapper
├── model/                    # 数据模型层
│   ├── domain/                # 数据库实体
│   │   └── User.java
│   ├── dto/                   # 数据传输对象
│   │   ├── UserLoginRequest.java
│   │   └── UserRegisterRequest.java
│   └── vo/                    # 视图对象
│       ├── UserVO.java
│       └── LoginUserVO.java
├── exception/                # 异常处理
│   ├── BusinessException.java   # 业务异常
│   └── GlobalExceptionHandler.java  # 全局异常处理
├── interceptor/              # 拦截器
│   ├── LoginInterceptor.java     # 登录验证拦截器
│   └── PermissionInterceptor.java # 权限验证拦截器
└── annotation/               # 自定义注解
    └── AuthCheck.java            # 权限校验注解
```

## 🔧 第三步：实现核心类

### 3.1 基础配置类
1. **ErrorCode.java** - 定义错误码枚举
2. **BaseResponse.java** - 统一响应封装
3. **MyBatisPlusConfig.java** - MyBatis-Plus配置（分页插件、自动填充）
4. **WebMvcConfig.java** - 拦截器配置

### 3.2 数据模型层
1. **User.java** - 用户实体类（映射user表）
2. **UserRegisterRequest.java** - 注册请求DTO
3. **UserLoginRequest.java** - 登录请求DTO
4. **UserVO.java** - 用户信息VO（脱敏）
5. **LoginUserVO.java** - 登录用户VO

### 3.3 数据访问层
1. **UserMapper.java** - 继承BaseMapper<User>

### 3.4 服务层（核心业务逻辑）
1. **UserService.java** - 定义接口
2. **UserServiceImpl.java** - 实现以下方法：
   - `userRegister()` - 用户注册
   - `userLogin()` - 用户登录
   - `userLogout()` - 用户登出
   - `getLoginUser()` - 获取当前登录用户
   - `isAdmin()` - 判断是否为管理员
   - `getUserById()` - 根据ID获取用户
   - `listUsers()` - 获取用户列表（仅管理员）
   - `deleteUser()` - 删除用户（仅管理员）
   - `updateUser()` - 更新用户（仅管理员）

### 3.5 控制层
1. **UserController.java** - 实现以下接口：
   - `POST /user/register` - 用户注册
   - `POST /user/login` - 用户登录
   - `POST /user/logout` - 用户登出
   - `GET /user/current` - 获取当前登录用户
   - `GET /user/{id}` - 根据ID获取用户
   - `GET /user/list` - 获取用户列表（仅管理员）
   - `DELETE /user/{id}` - 删除用户（仅管理员）
   - `PUT /user/update` - 更新用户（仅管理员）

### 3.6 拦截器层
1. **LoginInterceptor.java** - 登录验证拦截器
   - 拦截所有请求（除了登录、注册）
   - 从Session中获取用户信息
   - 未登录返回401错误

2. **PermissionInterceptor.java** - 权限验证拦截器
   - 检查@AuthCheck注解
   - 验证用户角色是否符合要求
   - 权限不足返回403错误

### 3.7 异常处理
1. **BusinessException.java** - 业务异常类
2. **GlobalExceptionHandler.java** - 全局异常处理器
   - 处理业务异常
   - 处理参数校验异常
   - 处理运行时异常

## 📝 第四步：配置文件

在 `application.properties` 中添加：
- 数据库连接配置
- MyBatis-Plus配置
- Session配置
- 日志配置

## 🔐 核心业务逻辑说明

### 用户注册流程
1. 接收注册请求参数
2. 校验两次密码是否一致
3. 检查账号是否已存在
4. 密码加密：`MD5(password + "brown")`
5. 保存用户到数据库（默认角色：user）
6. 返回用户ID

### 用户登录流程
1. 接收登录请求参数
2. 密码加密（使用相同盐值）
3. 根据账号和加密密码查询用户
4. 检查用户是否存在且未删除
5. 将用户信息存入Session
6. 返回脱敏后的用户信息（不包含密码）

### 权限验证流程
1. 请求到达 -> LoginInterceptor验证是否登录
2. 已登录 -> PermissionInterceptor检查@AuthCheck注解
3. 需要管理员权限 -> 验证userRole是否为"admin"
4. 验证通过 -> 放行到Controller
5. Service层再次校验权限（双重防护）

### 用户管理（仅管理员）
- **获取用户列表**：支持按userName模糊查询
- **删除用户**：逻辑删除（设置isDelete=1），不能删除自己
- **更新用户**：可修改用户信息、角色等

## 🎯 实现步骤顺序

### Phase 1: 基础设施 ✅ 大部分已完成
1. ✅ 修改pom.xml - 添加依赖
2. ✅ 创建common包 - BaseResponse, ResultUtils, PageRequest, DeleteRequest, GlobalExceptionHandler
3. ⏳ 创建constant包 - UserConstant（**待完成**）
4. ✅ 创建exception包 - BusinessException, ErrorCode, ThrowUtils
5. ⏳ 创建config包 - MyBatisPlusConfig（**待完成**）

### Phase 2: 数据模型
6. ✅ 创建model/domain/User.java - 实体类
7. ⏳ 创建model/dto/UserRegisterRequest.java（**待完成**）
8. ⏳ 创建model/dto/UserLoginRequest.java（**待完成**）
9. ⏳ 创建model/vo/UserVO.java（**待完成**）
10. ⏳ 创建model/vo/LoginUserVO.java（**待完成**）
11. ✅ 创建mapper/UserMapper.java

### Phase 3: 业务逻辑
12. ⏳ 完善service/UserService.java - 添加业务方法（**待完成**）
13. ⏳ 完善service/impl/UserServiceImpl.java - 实现核心逻辑（**待完成**）

### Phase 4: 控制层
14. ⏳ 创建controller/UserController.java（**待完成**）

### Phase 5: 拦截器和注解
15. ⏳ 创建annotation/AuthCheck.java（**待完成**）
16. ⏳ 创建interceptor/LoginInterceptor.java（**待完成**）
17. ⏳ 创建interceptor/PermissionInterceptor.java（**待完成**）
18. ⏳ 创建config/WebMvcConfig.java（**待完成**）

### Phase 6: 配置和测试
19. ⏳ 完善application.yml - 添加MyBatis-Plus配置（**待完成**）
20. ⏳ 初始化管理员账号数据（**待完成**）
21. ⏳ 测试所有功能（**待完成**）

## ✅ 验证测试步骤

### 1. 启动项目
```bash
mvn spring-boot:run
```

### 2. 测试用户注册
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "test001",
    "userPassword": "12345678",
    "checkPassword": "12345678",
    "userName": "测试用户"
  }'
```

### 3. 测试用户登录
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "test001",
    "userPassword": "12345678"
  }' \
  -c cookies.txt
```

### 4. 测试获取当前用户
```bash
curl -X GET http://localhost:8080/api/user/current \
  -b cookies.txt
```

### 5. 测试管理员功能
```bash
# 使用admin账号登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "admin",
    "userPassword": "admin123"
  }' \
  -c admin-cookies.txt

# 获取用户列表
curl -X GET http://localhost:8080/api/user/list \
  -b admin-cookies.txt
```

### 6. 测试权限控制
- 未登录访问受保护接口 -> 返回401
- 普通用户访问管理员接口 -> 返回403
- 管理员访问所有接口 -> 正常返回

## 📌 注意事项

1. **密码加密**：使用MD5(password + "brown")，存入数据库
2. **Session管理**：默认超时时间30分钟
3. **逻辑删除**：删除操作只设置isDelete=1，不物理删除
4. **权限验证**：三层防护（拦截器 -> 注解 -> Service层）
5. **参数校验**：使用@Valid注解自动校验请求参数
6. **异常处理**：全局异常处理器统一处理所有异常
7. **响应统一**：所有接口返回BaseResponse格式
8. **脱敏处理**：返回给前端的VO对象不包含密码字段

## 📂 关键文件清单

### 必须创建的文件（21个）：
- [ ] pom.xml（修改）
- [ ] ErrorCode.java
- [ ] BaseResponse.java
- [ ] UserConstant.java
- [ ] BusinessException.java
- [ ] GlobalExceptionHandler.java
- [ ] MyBatisPlusConfig.java
- [ ] User.java
- [ ] UserRegisterRequest.java
- [ ] UserLoginRequest.java
- [ ] UserVO.java
- [ ] LoginUserVO.java
- [ ] UserMapper.java
- [ ] UserService.java
- [ ] UserServiceImpl.java
- [ ] UserController.java
- [ ] AuthCheck.java
- [ ] LoginInterceptor.java
- [ ] PermissionInterceptor.java
- [ ] WebMvcConfig.java
- [ ] application.properties（修改）

## 🚀 实现后效果

1. ✅ 用户可以注册账号
2. ✅ 用户可以登录系统
3. ✅ 登录后可以查看自己的信息
4. ✅ 用户可以安全登出
5. ✅ 管理员可以查看所有用户
6. ✅ 管理员可以删除用户
7. ✅ 管理员可以修改用户信息
8. ✅ 权限控制完善，防止越权访问
9. ✅ 统一的错误处理和响应格式
10. ✅ 完整的参数校验机制

---

## 🎯 下一步行动

根据当前项目状态，建议按以下顺序完成：

### 1️⃣ 创建常量类（优先级：高）
```
src/main/java/com/example/usercenterpractice/constant/UserConstant.java
```
定义用户相关常量，如：
- 用户角色常量（USER_ROLE = "user", ADMIN_ROLE = "admin"）
- 默认头像URL
- Session key（USER_LOGIN_STATE）

### 2️⃣ 完善配置文件（优先级：高）
- 创建 `MyBatisPlusConfig.java`：配置分页插件、自动填充
- 完善 `application.yml`：添加MyBatis-Plus配置（日志、驼峰转下划线等）

### 3️⃣ 创建DTO和VO类（优先级：高）
- `UserRegisterRequest.java`：用户注册请求（userAccount, userPassword, checkPassword, userName）
- `UserLoginRequest.java`：用户登录请求（userAccount, userPassword）
- `UserVO.java`：用户信息视图对象（脱敏，不包含密码）
- `LoginUserVO.java`：登录用户视图对象

### 4️⃣ 完善业务层（优先级：核心）
- 在 `UserService.java` 中定义业务方法
- 在 `UserServiceImpl.java` 中实现：
  - `userRegister()` - 用户注册
  - `userLogin()` - 用户登录
  - `getLoginUser()` - 获取当前登录用户
  - `isAdmin()` - 判断是否为管理员
  - `getUserById()` - 根据ID获取用户
  - `listUsers()` - 获取用户列表（仅管理员）
  - `deleteUser()` - 删除用户（仅管理员）
  - `updateUser()` - 更新用户（仅管理员）

### 5️⃣ 创建拦截器和注解（优先级：中）
- `@AuthCheck` 注解：权限校验注解
- `LoginInterceptor`：登录验证拦截器
- `PermissionInterceptor`：权限验证拦截器
- `WebMvcConfig`：注册拦截器

### 6️⃣ 创建控制器（优先级：中）
- `UserController.java`：实现所有用户相关的HTTP接口

---

## 📌 可用工具函数提示

### 密码加密
```java
// Hutool 提供的加密工具
DigestUtil.md5Hex(salt + password)
```

### 对象转换
```java
// MyBatis-Plus 提供的属性复制工具
BeanUtil.copyProperties(source, target)
```

### Session操作
```java
// 在Controller中获取Session
request.getSession()

// 存入用户信息
session.setAttribute(USER_LOGIN_STATE, user)

// 获取用户信息
session.getAttribute(USER_LOGIN_STATE)
```

### 数据库查询
```java
// 条件构造器
QueryWrapper<User> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("userAccount", userAccount);

// 分页查询
Page<User> page = new Page<>(current, size);
userMapper.selectPage(page, queryWrapper);
```

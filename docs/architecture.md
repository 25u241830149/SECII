# CampusHub 校园互助服务平台架构设计文档

## 1. 项目概述

CampusHub 是一个面向校园场景的互助服务平台，支持用户发布任务、接单执行、订单流转、评价反馈与消息通知等核心能力。系统采用前后端分离架构，前端负责交互展示与状态管理，后端负责业务处理、数据持久化与接口服务。

---

## 2. 总体架构设计

### 2.1 架构风格

系统采用"前后端分离 + 模块化单体"的设计方式：

- 前端通过 Web 页面提供用户交互能力
- 后端通过 RESTful API 提供业务服务
- 后端内部按业务域拆分为多个模块，便于分工、维护与扩展
- 系统采用 JWT 实现无状态认证
- 通过 Redis 支撑缓存、会话控制与并发场景处理

### 2.2 技术选型

#### 前端
- Vue 3
- Vite
- Element Plus
- Pinia
- Axios

#### 后端
- Spring Boot 3
- MyBatis-Plus
- Redis
- PostgreSQL
- JWT
- Redisson

### 2.3 系统交互方式

- 前端通过 Axios 调用后端 REST API
- 后端统一返回标准 JSON 响应
- 登录后由后端签发 JWT，前端保存 Token 并在请求头中携带
- 对于实时通知类功能，可预留 WebSocket 支持

---

## 3. 前端架构设计

### 3.1 设计目标

前端架构需要满足以下要求：

- 页面响应式，适配常见桌面端浏览场景
- 组件化开发，便于复用与维护
- 状态集中管理，减少跨组件传值复杂度
- 请求层统一封装，便于鉴权和错误处理

### 3.2 技术架构

#### 视图层
采用 Vue 3 Composition API 构建页面，按业务场景划分页面模块，例如：

- 首页
- 登录/注册页
- 任务大厅
- 任务详情页
- 发布任务页
- 订单管理页
- 个人中心
- 消息中心

#### 组件层
使用 Element Plus 作为 UI 组件库，提供：

- 表单
- 表格
- 弹窗
- 分页
- 标签
- 消息提示

#### 状态管理层
使用 Pinia 管理全局状态，包括：

- 用户信息
- 登录状态
- Token
- 未读消息数
- 当前角色信息

#### 网络请求层
使用 Axios 封装请求模块，统一处理：

- BaseURL 配置
- Token 注入
- 响应拦截
- 401 处理
- 错误提示

### 3.3 前端目录建议

```text
src/
├── api/           # 接口请求封装
├── assets/        # 静态资源
├── components/    # 公共组件
├── layout/        # 页面布局
├── router/        # 路由配置
├── stores/        # Pinia 状态管理
├── views/         # 页面视图
├── utils/         # 工具方法
└── main.js        # 入口文件
```

### 3.4 请求与跨域设计

开发环境下，通过 Vite 代理解决跨域问题：

- 前端请求统一以 `/api` 开头
- Vite 将 `/api` 转发到后端服务
- 后端开启 CORS，允许前端域名访问

请求流程如下：

1. 用户发起操作
2. 前端 Axios 发送请求
3. 请求头携带 JWT
4. 后端校验 Token 并执行业务逻辑
5. 返回统一格式响应
6. 前端根据响应更新页面状态

---

## 4. 后端架构设计

### 4.1 设计目标

后端架构需要满足：

- 业务边界清晰
- 模块职责单一
- 支持高并发场景
- 易于扩展和测试
- 保持较低耦合度

### 4.2 架构模式

后端采用 Maven 多模块模块化单体架构，统一由父工程管理依赖和版本。

### 4.3 模块划分

```text
campushub-parent/
├── campushub-common/     # 公共模块
├── campushub-user/       # 用户模块
├── campushub-task/       # 任务模块
├── campushub-order/      # 订单模块
├── campushub-review/     # 评价模块
├── campushub-message/    # 消息模块
└── campushub-bootstrap/  # 启动模块
```

### 4.4 各模块职责

#### 4.4.1 common 模块
提供通用能力，包括：

- 全局异常处理
- 统一返回体
- JWT 工具类
- 公共常量
- CORS 配置
- 通用枚举与工具类

#### 4.4.2 user 模块
负责用户相关业务，包括：

- 用户注册与登录
- 用户资料维护
- 角色权限管理
- 信用分维护
- 个人中心信息查询

#### 4.4.3 task 模块
负责任务发布与浏览，包括：

- 任务发布
- 任务编辑
- 任务列表查询
- 分类筛选
- 地理位置标记
- 任务状态管理

#### 4.4.4 order 模块
负责接单与订单流转，包括：

- 抢单
- 接单确认
- 订单状态变更
- 订单完成确认
- 并发控制

#### 4.4.5 review 模块
负责评价与信誉管理，包括：

- 双向评价
- 评分记录
- 信誉分计算
- 评价展示

#### 4.4.6 message 模块
负责站内消息与通知，包括：

- 系统通知
- 订单状态通知
- 评价通知
- 未读消息管理
- 可扩展 WebSocket 推送

#### 4.4.7 bootstrap 模块
作为应用启动入口，负责：

- Spring Boot 启动类
- 配置加载
- 静态资源映射
- 模块装配

### 4.4.8 各模块详细架构设计

#### 4.4.8.1 common 模块架构

**模块职责**：提供全系统通用的基础设施和工具支持。

**分层设计**：
```
common/
├── config/              # 全局配置
│   ├── CorsConfig       # CORS 跨域配置
│   ├── JwtConfig        # JWT 配置
│   └── RedisConfig      # Redis 配置
├── exception/           # 异常处理
│   ├── GlobalExceptionHandler  # 全局异常拦截器
│   ├── BusinessException       # 业务异常
│   └── SystemException         # 系统异常
├── response/            # 统一响应
│   ├── ApiResponse      # 标准响应体
│   └── PageResponse     # 分页响应体
├── security/            # 安全相关
│   ├── JwtTokenProvider # JWT 生成与验证
│   ├── JwtAuthFilter    # JWT 认证过滤器
│   └── SecurityUtils    # 安全工具类
├── constant/            # 常量定义
│   ├── ErrorCode        # 错误码常量
│   ├── MessageType      # 消息类型常量
│   └── OrderStatus      # 订单状态常量
├── enums/               # 枚举定义
│   ├── UserRole         # 用户角色枚举
│   ├── TaskStatus       # 任务状态枚举
│   └── OrderStatus      # 订单状态枚举
└── utils/               # 工具类
    ├── DateUtils        # 日期工具
    ├── EncryptUtils     # 加密工具
    └── ValidateUtils    # 验证工具
```

**核心类职责**：
- `ApiResponse`: 统一返回体，包含 code、message、data 三个字段
- `JwtTokenProvider`: 负责 JWT 的生成、验证和刷新
- `GlobalExceptionHandler`: 捕获全局异常，统一返回错误响应
- `JwtAuthFilter`: 拦截请求，验证 Token 有效性

**对外接口**：
- 无 REST 接口，仅提供工具类和配置

---

#### 4.4.8.2 user 模块架构

**模块职责**：管理用户账户、认证、资料和信用体系。

**分层设计**：
```
user/
├── controller/          # 控制层
│   └── UserController   # 用户接口入口
├── service/             # 业务层
│   ├── UserService      # 用户业务逻辑
│   ├── AuthService      # 认证业务逻辑
│   └── CreditService    # 信用分业务逻辑
├── mapper/              # 数据访问层
│   └── UserMapper       # 用户数据操作
├── entity/              # 实体类
│   └── User             # 用户实体
├── dto/                 # 数据传输对象
│   ├── LoginRequest     # 登录请求
│   ├── RegisterRequest  # 注册请求
│   ├── UserInfoDTO      # 用户信息 DTO
│   └── UserProfileDTO   # 用户资料 DTO
└── event/               # 事件发布
    └── UserCreditEvent  # 信用分变更事件
```

**核心类职责**：
- `UserService`: 用户注册、登录、资料更新、信用分查询
- `AuthService`: Token 生成、验证、刷新
- `CreditService`: 信用分计算、更新、历史记录
- `UserController`: 暴露 REST 接口

**对外接口**：
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/profile` - 获取用户资料
- `PUT /api/user/profile` - 更新用户资料
- `GET /api/user/credit` - 获取信用分

**内部依赖**：
- 依赖 common 模块的 JWT、异常处理、常量

---

#### 4.4.8.3 task 模块架构

**模块职责**：管理任务的发布、编辑、查询和状态流转。

**分层设计**：
```
task/
├── controller/          # 控制层
│   └── TaskController   # 任务接口入口
├── service/             # 业务层
│   ├── TaskService      # 任务业务逻辑
│   ├── TaskQueryService # 任务查询逻辑
│   └── TaskStatusService# 任务状态管理
├── mapper/              # 数据访问层
│   └── TaskMapper       # 任务数据操作
├── entity/              # 实体类
│   └── Task             # 任务实体
├── dto/                 # 数据传输对象
│   ├── TaskCreateRequest  # 任务创建请求
│   ├── TaskUpdateRequest  # 任务更新请求
│   ├── TaskDetailDTO      # 任务详情 DTO
│   └── TaskListDTO        # 任务列表 DTO
└── event/               # 事件发布
    └── TaskPublishedEvent # 任务发布事件
```

**核心类职责**：
- `TaskService`: 任务创建、编辑、删除、状态变更
- `TaskQueryService`: 任务列表查询、分类筛选、地理位置查询
- `TaskStatusService`: 任务状态流转管理
- `TaskController`: 暴露 REST 接口

**对外接口**：
- `POST /api/task/publish` - 发布任务
- `PUT /api/task/{id}` - 编辑任务
- `GET /api/task/{id}` - 获取任务详情
- `GET /api/task/list` - 获取任务列表（支持分页、筛选）
- `DELETE /api/task/{id}` - 删除任务

**内部依赖**：
- 依赖 common 模块的异常处理、常量
- 发布事件供 order、message 模块监听

---

#### 4.4.8.4 order 模块架构

**模块职责**：管理订单的创建、流转、并发控制和完成确认。

**分层设计**：
```
order/
├── controller/          # 控制层
│   └── OrderController  # 订单接口入口
├── service/             # 业务层
│   ├── OrderService     # 订单业务逻辑
│   ├── GrabService      # 抢单业务逻辑（并发控制）
│   └── OrderStatusService # 订单状态管理
├── mapper/              # 数据访问层
│   └── OrderMapper      # 订单数据操作
├── entity/              # 实体类
│   └── Order            # 订单实体
├── dto/                 # 数据传输对象
│   ├── GrabOrderRequest # 抢单请求
│   ├── OrderDetailDTO   # 订单详情 DTO
│   └── OrderListDTO     # 订单列表 DTO
├── lock/                # 并发控制
│   └── RedissonLock     # 基于 Redisson 的分布式锁
└── event/               # 事件发布
    ├── OrderCreatedEvent    # 订单创建事件
    ├── OrderConfirmedEvent  # 订单确认事件
    └── OrderCompletedEvent  # 订单完成事件
```

**核心类职责**：
- `OrderService`: 订单创建、查询、状态查询
- `GrabService`: 抢单逻辑，使用 Redisson 分布式锁保证并发安全
- `OrderStatusService`: 订单状态流转管理
- `OrderController`: 暴露 REST 接口

**对外接口**：
- `POST /api/order/grab` - 抢单（高并发）
- `POST /api/order/{id}/confirm` - 确认接单
- `POST /api/order/{id}/complete` - 完成订单
- `GET /api/order/{id}` - 获取订单详情
- `GET /api/order/list` - 获取订单列表

**内部依赖**：
- 依赖 common 模块的异常处理、常量
- 依赖 task 模块的任务信息
- 依赖 user 模块的用户信息
- 发布事件供 review、message 模块监听

---

#### 4.4.8.5 review 模块架构

**模块职责**：管理订单完成后的双向评价和信誉分计算。

**分层设计**：
```
review/
├── controller/          # 控制层
│   └── ReviewController # 评价接口入口
├── service/             # 业务层
│   ├── ReviewService    # 评价业务逻辑
│   └── CreditCalculator # 信誉分计算器
├── mapper/              # 数据访问层
│   └── ReviewMapper     # 评价数据操作
├── entity/              # 实体类
│   └── Review           # 评价实体
├── dto/                 # 数据传输对象
│   ├── ReviewCreateRequest # 评价创建请求
│   ├── ReviewDetailDTO     # 评价详情 DTO
│   └── ReviewListDTO       # 评价列表 DTO
└── event/               # 事件监听
    └── OrderCompletedEventListener # 监听订单完成事件
```

**核心类职责**：
- `ReviewService`: 评价创建、查询、统计
- `CreditCalculator`: 根据评价计算用户信誉分
- `ReviewController`: 暴露 REST 接口

**对外接口**：
- `POST /api/review/create` - 创建评价
- `GET /api/review/{id}` - 获取评价详情
- `GET /api/review/user/{userId}` - 获取用户评价列表

**内部依赖**：
- 依赖 common 模块的异常处理、常量
- 依赖 order 模块的订单信息
- 依赖 user 模块的用户信息
- 监听 order 模块的订单完成事件

---

#### 4.4.8.6 message 模块架构

**模块职责**：管理系统消息、订单通知、评价通知和未读消息状态。

**分层设计**：
```
message/
├── controller/          # 控制层
│   └── MessageController # 消息接口入口
├── service/             # 业务层
│   ├── MessageService   # 消息业务逻辑
│   ├── NotificationService # 通知发送逻辑
│   └── MessageQueryService # 消息查询逻辑
├── mapper/              # 数据访问层
│   └── MessageMapper    # 消息数据操作
├── entity/              # 实体类
│   └── Message          # 消息实体
├── dto/                 # 数据传输对象
│   ├── MessageDetailDTO # 消息详情 DTO
│   └── MessageListDTO   # 消息列表 DTO
├── event/               # 事件监听
│   ├── OrderEventListener    # 监听订单事件
│   ├── ReviewEventListener   # 监听评价事件
│   └── TaskEventListener     # 监听任务事件
└── websocket/           # WebSocket 支持（可选）
    └── MessageWebSocketHandler # WebSocket 消息推送
```

**核心类职责**：
- `MessageService`: 消息创建、查询、标记已读
- `NotificationService`: 根据事件生成通知消息
- `MessageQueryService`: 消息列表查询、未读消息统计
- `MessageController`: 暴露 REST 接口

**对外接口**：
- `GET /api/message/list` - 获取消息列表
- `GET /api/message/unread-count` - 获取未读消息数
- `PUT /api/message/{id}/read` - 标记消息已读
- `DELETE /api/message/{id}` - 删除消息

**内部依赖**：
- 依赖 common 模块的异常处理、常量
- 监听 order、review、task 模块的事件

---

#### 4.4.8.7 bootstrap 模块架构

**模块职责**：应用启动入口，负责模块装配和配置加载。

**分层设计**：
```
bootstrap/
├── CampusHubApplication.java  # Spring Boot 启动类
├── application.yml            # 应用配置
├── application-dev.yml        # 开发环境配置
├���─ application-prod.yml       # 生产环境配置
└── resources/
    └── db/
        └── schema.sql         # 数据库初始化脚本
```

**核心职责**：
- 启动 Spring Boot 应用
- 加载所有子模块
- 初始化数据库连接
- 配置 Redis 连接

---

### 4.5 模块间通信方式

#### 同步调用
模块间通过内部服务接口进行调用，不直接跨模块访问 Mapper，避免业务层耦合过深。

#### 异步通信
通过事件机制实现模块解耦。例如：

- 订单完成��发布订单完成事件
- 用户模块监听事件并增加信用分
- 消息模块监听事件并发送通知

### 4.6 核心业务设计

#### 抢单并发控制
抢单属于高并发场景，需要采用分布式锁控制资源竞争：

- 以订单 ID 或任务 ID 作为锁粒度
- 获取锁后再进行状态校验与创建订单
- 业务完成后释放锁
- 配合乐观锁防止重复更新

#### 状态机控制
订单状态应严格按流程流转，例如：

- 待接单
- 已接单
- 进行中
- 已完成
- 已取消

状态变更应由服务层统一控制，避免前端直接传入任意状态。

---

## 5. 数据库设计

### 5.1 数据库选型

采用 PostgreSQL 作为主数据库，支持事务、并发控制与复杂查询场景。

### 5.2 设计原则

- 表结构按业务域划分
- 主键统一使用自增或雪花 ID
- 核心业务表保留创建时间、更新时间、逻辑删除字段
- 高频更新字段增加索引
- 订单等核心状态表保留版本号字段支持乐观锁

### 5.3 核心数据表

#### 5.3.1 用户表 `u_user`

用途：存储用户基础信息与账户状态。

主要字段：
- `id` - 主键
- `username` - 用户名
- `password` - 密码（加密存储）
- `nickname` - 昵称
- `phone` - 电话
- `avatar` - 头像 URL
- `role` - 角色
- `credit_score` - 信用分
- `status` - 账户状态
- `deleted` - 逻辑删除标识
- `created_at` - 创建时间
- `updated_at` - 更新时间

说明：
- `role` 用于区分普通用户、管理员等角色
- `credit_score` 用于记录用户信用分
- `deleted` 用于逻辑删除

#### 5.3.2 任务表 `t_task`

用途：存储用户发布的任务信息。

主要字段：
- `id` - 主键
- `publisher_id` - 发布者 ID
- `title` - 任务标题
- `description` - 任务描述
- `category` - 分类
- `reward` - 报酬
- `location` - 地理位置
- `status` - 任务状态
- `deadline` - 截止时间
- `created_at` - 创建时间
- `updated_at` - 更新时间
- `deleted` - 逻辑删除标识

说明：
- `publisher_id` 关联用户表
- `location` 预留用于地理位置扩展
- `status` 表示任务当前状态

#### 5.3.3 订单表 `o_order`

用途：存储任务接单后的订单流转信息。

主要字段：
- `id` - 主键
- `task_id` - 任务 ID
- `publisher_id` - 发布者 ID
- `receiver_id` - 接收者 ID
- `status` - 订单状态
- `version` - 版本号（乐观锁）
- `created_at` - 创建时间
- `updated_at` - 更新时间
- `deleted` - 逻辑删除标识

说明：
- `version` 用于乐观锁
- `status` 用于订单状态控制
- `task_id` 关联任务表

#### 5.3.4 评价表 `r_review`

用途：存储订单完成后的双向评价记录。

主要字段：
- `id` - 主键
- `order_id` - 订单 ID
- `reviewer_id` - 评价者 ID
- `reviewee_id` - 被评价者 ID
- `score` - 评分
- `content` - 评价内容
- `created_at` - 创建时间
- `deleted` - 逻辑删除标识

说明：
- 支持双方互评
- `score` 用于信誉计算

#### 5.3.5 消息表 `m_message`

用途：存储站内信和系统通知。

主要字段：
- `id` - 主键
- `receiver_id` - 接收者 ID
- `type` - 消息类型
- `title` - 消息标题
- `content` - 消息内容
- `read_status` - 已读状态
- `created_at` - 创建时间
- `deleted` - 逻辑删除标识

说明：
- `read_status` 表示是否已读
- `type` 区分系统消息、订单消息、评价消息等

### 5.4 关系设计

- `u_user` 与 `t_task` 为一对多
- `u_user` 与 `o_order` 为一对多
- `t_task` 与 `o_order` 为一对一或一对多，取决于业务是否允许多次抢单
- `o_order` 与 `r_review` 为一对多或一对二
- `u_user` 与 `m_message` 为一对多

### 5.5 索引建议

建议为以下字段建立索引：

- `u_user.username`
- `u_user.phone`
- `t_task.status`
- `t_task.category`
- `o_order.task_id`
- `o_order.receiver_id`
- `o_order.status`
- `m_message.receiver_id`
- `m_message.read_status`

---

## 6. 总结

CampusHub 采用前后端分离、模块化单体和统一数据设计的架构方案，能够较好支持校园互助场景下的任务发布、抢单、订单流转、评价反馈和消息通知等核心功能。该架构在保持开发效率的同时，也为后续扩展实时通知、地理位置服务和信用体系提供了基础。

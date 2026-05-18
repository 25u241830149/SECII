# CampusHub 项目完整实施计划

## 背景

**项目**：智协 CampusHub —— 校园互助服务平台（30 个功能需求，7 大模块 + 1 个举报子模块，13 张数据表）

**当前状态**：

- P0（项目启动）、P1（需求分析）、P2（架构设计）已完成
- 需求规格说明书、架构设计文档、API 规范文档已定稿
- `backend/` 和 `frontend/` 目录为空（仅有 .gitkeep）
- `sql/` 目录为空
- 需从零搭建前后端代码

**团队**：4 人（管泽昊、李宇翰、吴禹成、徐航宇，按模块分工，不区分前后端角色）

**技术栈**（已确定）：

- 前端：Vue 3 + Vite + TypeScript + Element Plus + Pinia + Axios
- 后端：Spring Boot 3 + MyBatis-Plus + Maven 多模块 + JWT + Redis + Redisson
- 数据库：PostgreSQL 15 + PostGIS

---

## 模块分工总表

团队 4 人按模块划分职责，每人同时承担所负责模块的**后端 + 前端**开发任务。管理后台接口分散在各模块中，不单独分配。

| 成员 | 负责模块 | 后端范围 | 前端范围 |
|------|----------|----------|----------|
| 管泽昊 | common + bootstrap（基础设施） | 父POM、8模块骨架、schema.sql、CORS/JWT/Redis配置、异常处理、统一响应、安全认证、枚举与工具类、敏感词过滤、启动类与配置文件、CI/CD、Docker部署 | 脚手架、环境配置、类型定义、request.ts、路由与守卫、Pinia stores、三套布局、404/403页面 |
| 李宇翰 | user + review + report（用户+评价+举报） | User/Verification实体与Mapper、AuthService/UserService/CreditService/VerificationService、Review实体与Mapper、ReviewService/CreditCalculator、Report实体与Mapper、ReportService、全部对应Controller（含后台管理接口）、事件监听 | 登录/注册页、资料编辑/注销页、信用展示、评价组件（StarRating/ReviewForm/ReviewList）、举报页面、AdminUserController对应后台页 |
| 吴禹成 | task + order（任务+订单） | Task实体与Mapper、TaskService/TaskQueryService/TaskFavoriteService/TaskStatusService、Order实体与Mapper、OrderService/GrabService/OrderStatusService、事件发布、并发测试 | 首页信息流、任务详情、发布任务、任务卡片/分类导航/订单状态徽章组件、订单列表/详情、feedStore、收藏页 |
| 徐航宇 | message + forum（消息+论坛） | Message/ChatMessage/Notice实体与Mapper、MessageService/ChatService/NoticeService/NotificationService、Post/Comment实体与Mapper、ForumService/PostInteractionService、事件监听、WebSocket（可选） | 消息中心/聊天面板/公告组件、论坛列表/详情/发帖/评论、PostCard组件、后台公告/帖子管理页 |

**管理后台接口归属**：
- `POST /api/admin/users/{userId}/ban`、`POST /api/admin/users/{userId}/verify` → 李宇翰（user模块）
- `DELETE /api/admin/tasks/{taskId}` → 吴禹成（task模块）
- `POST /api/admin/posts/{postId}/pin`、`POST /api/admin/posts/{postId}/recommend` → 徐航宇（forum模块）
- `GET /api/admin/notices`、`POST /api/admin/notices` → 徐航宇（message模块）
- `POST /api/admin/reports/{reportId}/handle` → 李宇翰（report模块）
- `GET /api/admin/stats/dashboard` → 管泽昊（bootstrap模块）

---

## 关键依赖链

```
common (无依赖)
  ├── user (依赖 common)
  ├── task (依赖 common)
  ├── forum (依赖 common + user)
  ├── order (依赖 common + task + user)      ← 最复杂，含 Redisson 分布锁
  ├── review (依赖 order + user)             ← 监听 OrderCompletedEvent
  ├── report (依赖 common + user)            ← 举报独立子模块
  ├── message (依赖 task/order/review 事件)  ← 跨模块事件监听
  └── bootstrap (聚合全部模块)
```

---

## 总览：四个 Sprint

| Sprint | 周期      | 目标                   | 后端                                    | 前端                               |
| ------ | --------- | ---------------------- | --------------------------------------- | ---------------------------------- |
| S1     | 第 1-2 周 | 基础设施 + 用户系统    | common 模块、bootstrap、user 模块、建表 | 脚手架、布局、路由、认证           |
| S2     | 第 3-4 周 | 任务 + 订单核心        | task 模块、order 模块（含分布锁抢单）   | 首页信息流、任务发布、订单流转     |
| S3     | 第 5-6 周 | 评价 + 消息 + 举报 + 管理后台 | review、message、report 模块、admin 统计 | 评价组件、消息中心、聊天、举报、后台页面 |
| S4     | 第 7-8 周 | 论坛 + 集成测试 + 部署 | forum 模块、全链路测试、部署配置        | 论坛页面、E2E 测试、UI 打磨        |

---

## Sprint 1：基础设施与用户系统（第 1-2 周）

**目标**：项目骨架、数据库就绪、注册登录跑通、前端基础布局

### 后端任务

| ID    | 任务                                                                                                        | 负责人 | 交付物                      |
| ----- | ----------------------------------------------------------------------------------------------------------- | ------ | --------------------------- |
| B1.1  | 创建 Maven 父 POM（Spring Boot 3, MyBatis-Plus, JWT, Redis, Redisson, PostgreSQL, Lombok）                  | 管泽昊 | campushub-parent/pom.xml    |
| B1.2  | 创建 8 个子模块目录及 POM（common, user, task, order, review, report, message, forum, bootstrap），配置模块间依赖 | 管泽昊 | 各模块 pom.xml              |
| B1.3  | 编写 schema.sql：13 张表 DDL + 全部索引 + PostGIS 扩展 + 管理员种子数据                                     | 管泽昊 | bootstrap/.../db/schema.sql |
| B1.4  | common 模块：CorsConfig, JwtConfig, RedisConfig, RedissonConfig                                             | 管泽昊 | config 类                   |
| B1.5  | common 模块：BusinessException, SystemException, GlobalExceptionHandler                                     | 管泽昊 | 异常处理                    |
| B1.6  | common 模块：ApiResponse, PageResponse, 全部枚举（UserRole, VerificationStatus, TaskCategory, TaskStatus, OrderStatus, SortType, PostSortType, PostCategory, MessageType） | 管泽昊 | 统一响应与枚举              |
| B1.7  | common 模块：JwtTokenProvider, JwtAuthFilter, SecurityUtils                                                 | 管泽昊 | 安全认证                    |
| B1.8  | common 模块：ErrorCode, MessageType 常量 + DateUtils, EncryptUtils, ValidateUtils                           | 管泽昊 | 工具类                      |
| B1.9  | common 模块：SensitiveWordFilter（敏感词过滤骨架）                                                          | 管泽昊 | 内容审核                    |
| B1.10 | bootstrap 模块：CampusHubApplication, application.yml, application-dev.yml                                  | 管泽昊 | 启动配置                    |
| B1.11 | user 模块：User 实体 + UserMapper + XML（t_user_verification 同理）                                         | 李宇翰 | 用户数据层                  |
| B1.12 | user 模块：AuthService（注册含学号/学生证上传、登录）、UserService（资料 CRUD）、VerificationService        | 李宇翰 | 认证服务                    |
| B1.13 | user 模块：CreditService（信用分查询骨架）                                                                  | 李宇翰 | 信用服务                    |
| B1.14 | user 模块：DTO（LoginRequest, RegisterRequest, LoginResponseDTO, UserInfoDTO, UserProfileDTO, UserPublicDTO, UserHomeDTO, VerificationSubmitRequest） | 李宇翰 | DTO                         |
| B1.15 | user 模块：AuthController（注册、登录）、UserController（资料、信用分、主页、公开信息、注销）、AdminUserController 骨架 | 李宇翰 | REST 接口                   |

**后端 Sprint 1 API 清单**（与 API 规范文档 Section 4 对齐）：

- `POST /api/user/register` — 注册（匿名）
- `POST /api/user/login` — 登录（匿名）
- `GET /api/user/profile?userId={id}` — 获取个人资料
- `PUT /api/user/profile?userId={id}` — 更新个人资料
- `GET /api/user/credit?userId={id}` — 获取信用分
- `GET /api/user/{userId}` — 获取公开用户信息（匿名）
- `GET /api/user/{userId}/home` — 获取个人主页
- `POST /api/user/verification/submit` — 提交实名认证
- `DELETE /api/user/account?userId={id}` — 注销账号

### 前端任务

| ID    | 任务                                                                                                  | 负责人 | 交付物                       |
| ----- | ----------------------------------------------------------------------------------------------------- | ------ | ---------------------------- |
| F1.1  | Vite + Vue 3 + TS 脚手架，安装 Element Plus, Pinia, Axios, Vue Router                                 | 管泽昊 | package.json, vite.config.ts |
| F1.2  | Vite 代理配置（/api → localhost:8080），.env.development / .env.production                           | 管泽昊 | 环境配置                     |
| F1.3  | TypeScript 类型定义：ApiResponse\<T\>, PageResponse\<T\>, 所有 DTO 与枚举接口                         | 管泽昊 | src/types/                   |
| F1.4  | request.ts：Axios 实例、Token 拦截器、401 响应拦截、统一错误处理                                      | 管泽昊 | src/api/request.ts           |
| F1.5  | 路由配置：所有路由定义、meta（title, auth）、路由守卫（guestOnly, requiresAuth, requiresAdmin）       | 管泽昊 | src/router/                  |
| F1.6  | Pinia stores：authStore（token, login/logout, restoreSession）、userStore（profile）、appStore        | 管泽昊 | src/stores/                  |
| F1.7  | PublicLayout.vue（登录/注册页布局）、MainLayout.vue（顶栏+侧栏+内容区）、AdminLayout.vue 骨架         | 管泽昊 | src/layouts/                 |
| F1.8  | API 模块 user.ts（注册、登录、资料、主页、注销接口）                                                  | 李宇翰 | src/api/user.ts              |
| F1.9  | 认证页面：Login.vue、Register.vue（含学号/学生证字段）                                                | 李宇翰 | src/views/auth/              |
| F1.10 | 资料页面：ProfileEdit.vue、AccountDeletion.vue                                                        | 李宇翰 | src/views/profile/           |

### Sprint 1 集成里程碑

- [ ] `mvn clean compile` 成功（所有 9 个模块：common + 7 业务 + bootstrap）
- [ ] 数据库 schema.sql 执行成功（13 张表验证）
- [ ] 注册 → 登录 → 获取 JWT → 访问受保护接口 端到端通过
- [ ] 前端 `npm run dev` 正常启动，可注册登录
- [ ] MainLayout 三栏布局可交互，路由守卫生效
- [ ] CI 流水线（.gitlab-ci.yml）编译通过

---

## Sprint 2：任务与订单核心（第 3-4 周）

**目标**：任务发布/浏览/收藏、订单抢单/流转、分布锁并发保护

### 后端任务

| ID    | 任务                                                                                                         | 负责人 | 交付物     |
| ----- | ------------------------------------------------------------------------------------------------------------ | ------ | ---------- |
| B2.1  | task 模块：Task 实体 + TaskMapper（含 PostGIS 位置支持）+ XML                                                | 吴禹成 | 任务数据层 |
| B2.2  | task 模块：TaskService（创建含敏感词过滤、编辑、删除）、TaskStatusService                                    | 吴禹成 | 任务服务   |
| B2.3  | task 模块：TaskQueryService（分类筛选、关键词搜索、时间/热度排序、分页）                                     | 吴禹成 | 查询服务   |
| B2.4  | task 模块：TaskFavoriteService（收藏/取消收藏、查收藏列表）                                                  | 吴禹成 | 收藏服务   |
| B2.5  | task 模块：DTO（TaskCreateRequest 多模板支持、TaskUpdateRequest、TaskDetailDTO、TaskListDTO）                | 吴禹成 | DTO        |
| B2.6  | task 模块：TaskController（发布、编辑、详情、列表、删除、收藏/取消收藏/收藏列表）、AdminTaskController      | 吴禹成 | REST 接口  |
| B2.7  | order 模块：Order 实体（含 version 乐观锁字段）+ OrderMapper + XML                                           | 吴禹成 | 订单数据层 |
| B2.8  | order 模块：OrderService（详情、按 role+status 列表）、OrderStatusService（确认、完成、取消、状态机校验）    | 吴禹成 | 订单服务   |
| B2.9  | order 模块：**GrabService**（Redisson 分布锁锁任务 ID → 状态校验 → 创建订单 → 乐观锁 version 更新） | 吴禹成 | 抢单服务   |
| B2.10 | order 模块：DTO（GrabOrderRequest, OrderDetailDTO, OrderListDTO）                                            | 吴禹成 | DTO        |
| B2.11 | order 模块：OrderController（抢单、确认、完成、取消、详情、列表）                                            | 吴禹成 | REST 接口  |
| B2.12 | order 模块：事件（OrderCreatedEvent, OrderConfirmedEvent, OrderCompletedEvent）                              | 吴禹成 | 事件类     |
| B2.13 | 编写 GrabService 并发测试（模拟 50 线程同时抢单，验证无重复）                                                | 吴禹成 | 单元测试   |

**后端 Sprint 2 API 清单**（与 API 规范文档 Section 5、6 对齐）：

- `POST /api/tasks` — 发布任务
- `GET /api/tasks?category=&keyword=&sort=&page=&size=` — 任务列表（匿名）
- `GET /api/tasks/{taskId}` — 任务详情（匿名）
- `PUT /api/tasks/{taskId}` — 编辑任务
- `DELETE /api/tasks/{taskId}` — 删除任务
- `POST /api/tasks/{taskId}/favorite` — 收藏任务
- `DELETE /api/tasks/{taskId}/favorite` — 取消收藏
- `GET /api/tasks/favorites?userId={id}&page=&size=&category=` — 收藏列表
- `DELETE /api/admin/tasks/{taskId}` — 后台下架任务
- `POST /api/orders/grab` — 抢单（高并发）
- `POST /api/orders/{orderId}/confirm` — 确认接单
- `POST /api/orders/{orderId}/complete` — 完成订单
- `POST /api/orders/{orderId}/cancel` — 取消订单
- `GET /api/orders?userId={id}&role=poster|helper&status=&page=&size=` — 订单列表
- `GET /api/orders/{orderId}` — 订单详情

### 前端任务

| ID   | 任务                                                                                                                | 负责人 | 交付物             |
| ---- | ------------------------------------------------------------------------------------------------------------------- | ------ | ------------------ |
| F2.1 | API 模块：task.ts、order.ts                                                                                         | 吴禹成 | src/api/           |
| F2.2 | feedStore：管理 category/keyword/sort 状态、任务列表缓存                                                            | 吴禹成 | src/stores/feed.ts |
| F2.3 | 通用组件：TaskCard.vue（头像+昵称+信用徽章+标题+描述+报酬）、CategoryNav.vue、OrderStatusBadge.vue                  | 吴禹成 | src/components/    |
| F2.4 | 首页 Home.vue：贴吧式信息流 + 分类筛选标签 + 搜索 + 排序切换 + 分页                                                 | 吴禹成 | src/views/home/    |
| F2.5 | 任务详情 TaskDetail.vue：完整任务信息 + 发布者信用 + 收藏按钮 + 抢单按钮 + 位置展示                                 | 吴禹成 | src/views/task/    |
| F2.6 | 发布任务 PublishTask.vue：多模板表单（快递代取/学习辅导/二手交易/组队匹配）+ 分类选择 + 报酬 + 截止时间             | 吴禹成 | src/views/publish/ |
| F2.7 | 订单页面 OrderList.vue（进行中/已完成/已取消标签 + 角色过滤）、OrderDetail.vue（订单信息 + 确认/完成/取消操作按钮） | 吴禹成 | src/views/order/   |
| F2.8 | LoadingBlock.vue、EmptyState.vue 通用组件                                                                           | 李宇翰 | src/components/    |
| F2.9 | 个人主页子页：MyTasks.vue、MyOrders.vue、MyFavorites.vue（任务收藏+论坛收藏聚合）                                  | 李宇翰 | src/views/profile/ |

**特别注意**：抢单并发控制是本阶段最核心的后端逻辑 —— Redisson 锁粒度 = 任务 ID，获取锁 → 校验任务是否已被抢 → 创建订单（带 version）→ 释放锁。

### Sprint 2 集成里程碑

- [ ] 任务 CRUD 完整可用（发布/编辑/删除/列表筛选搜索/详情）
- [ ] 任务收藏工作正常（收藏/取消/收藏列表）
- [ ] 抢单并发测试通过（50 线程无重复）
- [ ] 订单完整生命周期：抢单 → 确认 → 完成 / 取消
- [ ] 前端首页信息流、任务详情、发布、订单列表/详情全部可用

---

## Sprint 3：评价 + 消息 + 举报 + 管理后台（第 5-6 周）

**目标**：双向评价与信用体系、消息通知与订单聊天、举报系统、管理后台完整搭建

### 后端任务

| ID    | 任务                                                                                                                                                    | 负责人 | 交付物        |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ | ------------- |
| B3.1  | review 模块：Review 实体 + ReviewMapper + XML                                                                                                           | 李宇翰 | 评价数据层    |
| B3.2  | review 模块：ReviewService（创建双向评价、按订单/用户查询）、CreditCalculator（根据评分计算信用分、映射信用等级）                                       | 李宇翰 | 评价服务      |
| B3.3  | review 模块：ReviewController + OrderCompletedEventListener（订单完成时触发评价窗口）                                                                   | 李宇翰 | REST + 监听器 |
| B3.4  | report 模块：Report 实体 + ReportMapper + XML + ReportService（提交举报、管理员受理举报，支持 TASK/ORDER/USER/POST 四种目标类型）                       | 李宇翰 | 举报功能      |
| B3.5  | report 模块：ReportController（提交举报）+ AdminReportController（后台处理举报）                                                                        | 李宇翰 | REST 接口     |
| B3.6  | message 模块：Message/ChatMessage/Notice 实体 + Mapper + XML                                                                                            | 徐航宇 | 消息数据层    |
| B3.7  | message 模块：MessageService（列表、已读、删除）、NotificationService（根据事件生成通知）、ChatService（发送/查询订单聊天）、NoticeService（公告 CRUD） | 徐航宇 | 消息服务      |
| B3.8  | message 模块：事件监听器（OrderEventListener / ReviewEventListener / TaskEventListener）                                                                | 徐航宇 | 事件监听      |
| B3.9  | message 模块：MessageController（消息列表/未读数/已读/删除）、Chat 接口、Notice 接口（首页公告+后台公告管理）                                           | 徐航宇 | REST 接口     |
| B3.10 | 管理后台统计：AdminStatsController + StatsQueryService（跨模块聚合统计）                                                                                | 管泽昊 | 后台统计接口  |
| B3.11 | 信用分联动：订单取消扣分 + 评价后更新 + 信用等级展示                                                                                                    | 李宇翰 | 信用集成      |
| B3.12 | 全订单生命周期集成测试（发布→抢单→确认→完成→双向评价→信用更新→举报）                                                                                   | 全体   | 集成测试      |

**后端 Sprint 3 API 清单**（与 API 规范文档 Section 7、8、10、11 对齐）：

评价模块：
- `POST /api/reviews` — 提交评价
- `GET /api/reviews/{reviewId}` — 评价详情
- `GET /api/reviews/user/{userId}?page=&size=` — 用户评价列表（匿名）

消息模块：
- `GET /api/messages?userId={id}&page=&size=` — 消息列表
- `GET /api/messages/unread-count?userId={id}` — 未读消息数
- `PUT /api/messages/{messageId}/read` — 标记已读
- `DELETE /api/messages/{messageId}` — 删除消息
- `GET /api/orders/{orderId}/chat?page=&size=` — 订单聊天记录
- `POST /api/orders/{orderId}/chat` — 发送聊天消息
- `GET /api/notices/latest` — 首页公告（匿名）
- `GET /api/admin/notices?page=&size=` — 公告管理列表
- `POST /api/admin/notices` — 发布系统公告

举报模块：
- `POST /api/reports` — 提交举报
- `POST /api/admin/reports/{reportId}/handle` — 后台处理举报

后台统计：
- `GET /api/admin/stats/dashboard` — 统计面板
- `POST /api/admin/users/{userId}/ban` — 封禁用户（S1 骨架，本阶段完善）
- `POST /api/admin/users/{userId}/verify` — 审核认证（S1 骨架，本阶段完善）

### 前端任务

| ID    | 任务                                                                                                                    | 负责人 | 交付物                           |
| ----- | ----------------------------------------------------------------------------------------------------------------------- | ------ | -------------------------------- |
| F3.1  | API 模块：review.ts、message.ts、report.ts、admin.ts                                                                     | 各自   | src/api/                         |
| F3.2  | 评价组件：StarRating.vue、ReviewForm.vue、ReviewList.vue                                                                | 李宇翰 | src/components/                  |
| F3.3  | 消息中心：MessageList.vue、UnreadBadge.vue（顶栏红点）                                                                  | 徐航宇 | src/views/message/               |
| F3.4  | 聊天组件：ChatPanel.vue（嵌入订单详情页）                                                                               | 徐航宇 | src/components/                  |
| F3.5  | 公告组件：NoticeBar.vue（首页公告栏）                                                                                   | 徐航宇 | src/components/                  |
| F3.6  | AdminLayout.vue 完善（统计/用户/任务/论坛/公告/举报菜单）                                                                | 管泽昊 | src/layouts/                     |
| F3.7  | 后台页面：StatsDashboard.vue（管泽昊）、UserManagement.vue（封禁/审核，李宇翰）、TaskModeration.vue（下架，吴禹成）、ReportCenter.vue（举报处理，李宇翰） | 各自   | src/views/admin/                 |
| F3.8  | 信用展示：CreditBadge.vue（等级图标+分数）、CreditPage.vue                                                              | 李宇翰 | src/components/ + views/profile/ |
| F3.9  | 评分集成到订单详情：完成后显示评价入口                                                                                  | 李宇翰 | src/views/order/                 |
| F3.10 | 个人主页完善：足迹聚合、信用页、发单/接单分页                                                                           | 李宇翰 | src/views/profile/               |
| F3.11 | 公告管理后台页：NoticeManagement.vue                                                                                    | 徐航宇 | src/views/admin/                 |

### Sprint 3 集成里程碑

- [ ] 订单完成→双向评价→信用分更新 全链路通过
- [ ] 订单状态变更自动推送通知
- [ ] 订单内简易聊天可用
- [ ] 举报提交与后台处理流程完整可用
- [ ] 管理后台：统计面板、用户管理（封禁/审核）、任务审核（下架）、举报处理、公告管理均可操作
- [ ] 前端消息中心、聊天面板、评价组件、后台页面全部可用

---

## Sprint 4：论坛 + 集成 + 部署（第 7-8 周）

**目标**：论坛完整功能、全系统集成测试、CI/CD 部署、UI 打磨

### 后端任务

| ID   | 任务                                                                                           | 负责人 | 交付物            |
| ---- | ---------------------------------------------------------------------------------------------- | ------ | ----------------- |
| B4.1 | forum 模块：Post 实体 + PostMapper + Comment 实体 + CommentMapper + XML                        | 徐航宇 | 论坛数据层        |
| B4.2 | forum 模块：ForumService（发帖含敏感词过滤、24h 内可编辑、删除、详情、分类列表 + 排序）        | 徐航宇 | 论坛服务          |
| B4.3 | forum 模块：PostInteractionService（点赞/取消、收藏/取消、计数维护）                           | 徐航宇 | 互动服务          |
| B4.4 | forum 模块：ForumController（帖子 CRUD + 评论 + 点赞/收藏）、AdminForumController（置顶/推荐） | 徐航宇 | REST 接口         |
| B4.5 | WebSocket 实时通知（可选，可降级为轮询）                                                       | 徐航宇 | WebSocket Handler |
| B4.6 | 敏感词过滤器全面接入（任务、帖子、评论、评价）                                                 | 管泽昊 | 审核集成          |
| B4.7 | 全系统集成测试（覆盖全部 30 个 FR）                                                            | 全体   | 测试套件          |
| B4.8 | 性能测试：100 并发用户、抢单压测、页面加载时间                                                 | 管泽昊 | 性能报告          |

**后端 Sprint 4 API 清单**（与 API 规范文档 Section 9 对齐）：

- `POST /api/posts` — 发帖
- `GET /api/posts?category=&sort=latest|views|recommend&page=&size=` — 帖子列表（匿名）
- `GET /api/posts/{postId}` — 帖子详情（匿名）
- `PUT /api/posts/{postId}` — 编辑帖子（24h 内）
- `DELETE /api/posts/{postId}` — 删除帖子
- `POST /api/posts/{postId}/comments` — 发表评论
- `POST /api/posts/{postId}/like` — 点赞帖子
- `DELETE /api/posts/{postId}/like` — 取消点赞
- `POST /api/posts/{postId}/favorite` — 收藏帖子
- `DELETE /api/posts/{postId}/favorite` — 取消收藏
- `POST /api/admin/posts/{postId}/pin` — 置顶
- `POST /api/admin/posts/{postId}/recommend` — 推荐

### 前端任务

| ID   | 任务                                                                                                                    | 负责人 | 交付物             |
| ---- | ----------------------------------------------------------------------------------------------------------------------- | ------ | ------------------ |
| F4.1 | API 模块：forum.ts                                                                                                      | 徐航宇 | src/api/forum.ts   |
| F4.2 | 论坛页面：PostList.vue（分类标签+排序+置顶帖+分页）、PostDetail.vue（正文+评论+点赞收藏）、PublishPost.vue（发帖/编辑） | 徐航宇 | src/views/forum/   |
| F4.3 | 评论组件：CommentList.vue（嵌套回复）、CommentInput.vue                                                                 | 徐航宇 | src/components/    |
| F4.4 | PostCard.vue（论坛信息流卡片）                                                                                          | 徐航宇 | src/components/    |
| F4.5 | 后台论坛管理：PostManagement.vue（帖子列表+置顶/推荐操作）                                                              | 徐航宇 | src/views/admin/   |
| F4.6 | 404/403 错误页面                                                                                                        | 管泽昊 | src/views/system/  |
| F4.7 | 统一收藏页：MyFavorites.vue（任务标签+帖子标签）                                                                        | 李宇翰 | src/views/profile/ |
| F4.8 | UI 打磨：响应式适配、间距统一、所有异步加载状态                                                                         | 徐航宇 | 全局 CSS           |
| F4.9 | E2E 测试：关键用户旅程（注册→登录→发布任务→抢单→完成→评价→举报→发帖→评论→后台审核）                              | 全体   | E2E 测试           |

### 部署与交付

| ID   | 任务                                                                             | 负责人 | 交付物   |
| ---- | -------------------------------------------------------------------------------- | ------ | -------- |
| D4.1 | .gitlab-ci.yml 完善（backend build→test + frontend build→test + deploy stage） | 管泽昊 | CI/CD    |
| D4.2 | docker-compose.yml（PostgreSQL + Redis + 后端 JAR + 前端 Nginx）                 | 管泽昊 | 部署配置 |
| D4.3 | application-prod.yml（生产环境 DB/Redis 地址、CORS 白名单、JWT 过期时间）        | 管泽昊 | 生产配置 |
| D4.4 | API 文档（Swagger）、部署指南、用户手册                                          | 管泽昊 | 文档     |

### Sprint 4 集成里程碑

- [ ] 论坛完整可用：发帖编辑删除、评论回复、点赞收藏、分类排序、置顶/推荐
- [ ] 敏感词过滤覆盖所有内容创建入口
- [ ] 全系统集成测试通过（30 个 FR 全覆盖）
- [ ] E2E 测试通过（关键用户旅程）
- [ ] CI 完整流水线通过
- [ ] docker-compose up 一键部署可用

---

## 关键风险

| 风险                          | 可能性 | 影响           | 对策                                              |
| ----------------------------- | ------ | -------------- | ------------------------------------------------- |
| Redis 不可用 / 分布锁配置错误 | 中     | 高（抢单失败） | docker-compose 提供 Redis；GrabService 有降级路径 |
| PostGIS 空间查询复杂          | 中     | 中（位置功能） | 先用文本字段存储位置，空间查询延迟到 S3-S4        |
| 抢单并发有竞争条件            | 中     | 高             | S2 就写并发测试（50 线程），尽早发现              |
| 前后端接口对接延误            | 中     | 中             | API 规范文档已有完整接口契约；前端可 mock 先行开发 |
| 跨模块事件通信调试复杂        | 中     | 中             | 尽早搭建 message 模块的事件监听骨架，S3 联调预留缓冲 |

---

## 验证方式

### 每 Sprint 结束时

1. `mvn clean test` 全量通过
2. `npm run build` 无 TypeScript 错误
3. 对应 Sprint 的集成里程碑全部勾选
4. 手动走通该阶段关键用户流程

### 项目最终验证

1. 30 个 FR 逐一验证通过（按需求追踪矩阵）
2. E2E 测试通过（注册→任务→订单→评价→举报→论坛→后台）
3. 性能基准：100 并发用户在线、首页加载 < 2s、简单查询 < 500ms
4. docker-compose up 一键启动全栈可用
5. 所有 AI 协作标记符合《AI 协作契约》要求

---

## 参考文档

- 需求规格说明书：`docs/P1/需求规格说明书.md`
- 架构设计文档：`docs/P2/架构设计文档.md`
- API 规范文档：`docs/P3/API规范文档.md`（接口契约以本文档为准）
- 开发环境配置：`docs/help_document/setup.md`
- 参考作业要求：`refs/P0-项目启动与AI协作契约.md`、`refs/P1-需求分析.md`、`refs/P2-体系结构设计.md`

---

## 部分修改

- B1.2 原文写“创建 8 个子模块目录及 POM”，但实际模块清单包含 common、user、task、order、review、report、message、forum、bootstrap，共 9 个模块；已按实际清单创建 9 个 Maven 子模块。
- B1.3 原文写“13 张表 DDL”，但 P3 阶段 ER/建表 SQL 与后续 report 模块/API 均包含举报表 `t_report`；已在 `schema.sql` 中保留 `t_report`，因此当前数据库脚本共 14 张表。
- B1.6 已实现正式 `ApiResponse` / `PageResponse`，并将 B1.5 中 `GlobalExceptionHandler` 临时使用的内部 `ErrorBody` 替换为 `ApiResponse`，统一响应模型不再重复。
- B1.8 已将散落在异常处理与安全认证中的 HTTP/业务错误码改为统一引用 `ErrorCode` 常量。
- B1.10 已在 `application.yml` / `application-dev.yml` 中显式配置 JWT、Redis/Redisson、CORS、敏感词过滤、数据源、MyBatis-Plus 与 Springdoc 基础项。

## 注意事项

- 管理员种子数据是系统启动后的第一个后台账号。当前 `schema.sql` 中管理员密码字段保存的是 BCrypt 哈希，不是明文密码；B1.8 已实现 `EncryptUtils`，后续登录逻辑需要统一使用 `EncryptUtils.matchesPassword` 做 BCrypt 校验。当前管理员种子账号的明文初始密码需要团队确认，并建议上线前强制修改。
- 2026-05-15 已通过 OSGeo 官方 PostgreSQL 15 PostGIS bundle 安装 PostGIS 3.6.2，并在 `secii_db` 中成功执行 `schema.sql`。当前数据库包含 14 张业务表，另有 PostGIS 自动表 `spatial_ref_sys`；`postgis` 与 `pg_trgm` 扩展均已启用。（管泽昊电脑已配置好环境）
- B1.4 已引入 `campushub.cors`、`campushub.jwt`、`campushub.redisson` 三组配置前缀，B1.10 已在配置文件中覆盖开发环境默认值；生产环境仍必须通过环境变量设置强 JWT secret、数据库密码、Redis 密码与 CORS 白名单，禁止使用开发默认值。
- B1.6 的 Java 枚举按 API 规范使用字符串语义值；当前 `schema.sql` 中部分字段仍是 `SMALLINT` 编码。后续实现实体、Mapper 或 TypeHandler 时，需要统一枚举与数据库数字编码之间的映射，避免接口值与落库值混用。
- B1.7 的 JWT claim 约定为 `userId`、`username`、`role`，后续 `AuthService` 登录成功生成 token 时应统一使用 `JwtTokenProvider`。后续补 Spring Security 配置时，需要将 `JwtAuthFilter` 放入 Security 过滤器链，并显式放行注册、登录、公开用户信息、公开任务/帖子列表等匿名接口。
- B1.8 中 `com.campushub.common.constant.MessageType` 是字符串常量类，B1.6 中 `com.campushub.common.enums.MessageType` 是业务枚举；两者同名但包不同，后续代码 import 时要按用途明确选择，避免误用。
- B1.9 的 `SensitiveWordFilter` 是可配置骨架，当前采用大小写归一化后的精确包含匹配；后续 B2/B4 接入任务、帖子、评论、评价创建入口时，需要主动调用 `validate` 或 `filter`，若词库规模变大再替换为 Trie/AC 自动机或数据库词库加载。
- B1.10 的开发环境配置中 `spring.sql.init.mode` 为 `never`，避免启动时误重复执行建表脚本；新环境初始化数据库时仍需手动执行 `campushub-bootstrap/src/main/resources/db/schema.sql`，或临时调整 SQL 初始化策略。
- B1.6 当前枚举清单如下，后续若需求、API 规范或数据库编码调整，需要同步修改 Java 枚举、DTO、前端类型定义、数据库约束/映射逻辑：
  - `UserRole`：`USER`、`ADMIN`
  - `VerificationStatus`：`PENDING`、`APPROVED`、`REJECTED`
  - `TaskCategory`：`EXPRESS`、`STUDY`、`SECOND_HAND`、`TEAM_UP`、`OTHER`
  - `TaskStatus`：`OPEN`、`IN_PROGRESS`、`COMPLETED`、`CANCELLED`、`OFFLINE`
  - `OrderStatus`：`PENDING`、`CONFIRMED`、`COMPLETED`、`CANCELLED`
  - `SortType`：`time`、`hot`
  - `PostSortType`：`latest`、`views`、`recommend`
  - `PostCategory`：`HELP`、`STUDY`、`TRADE`、`LOST_FOUND`、`TEAM_UP`、`OTHER`
  - `MessageType`：`ORDER`、`CHAT`、`NOTICE`、`SYSTEM`

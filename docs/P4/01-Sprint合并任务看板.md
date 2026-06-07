# P4 Sprint 合并任务看板

更新时间：2026-06-07

## 1. 检查依据

本文档按 `docs/help_document/P4-编码开发.md` 的任务 1 和交付物 1 编写，并结合当前仓库源码、README 启动说明、CI 配置和本机验证结果修订。

- 当前分支：`main`
- 当前提交：`7913058`
- 远程仓库：`origin https://github.com/25u241830149/SECII.git`
- 本地可验证配置：`.gitlab-ci.yml`、`.gitlab/backend.yml`、`.gitlab/frontend.yml`
- 本次验证日期：2026-06-07，Windows PowerShell，Docker Desktop 可用

## 2. P4 交付物承接关系

| P4 序号 | 要求交付物 | 当前承接文档 | 齐全性 |
| --- | --- | --- | --- |
| 1 | Sprint 合并任务看板 | `01-Sprint合并任务看板.md` | 已具备 |
| 2 | 可运行的系统代码 | `02-可运行系统代码说明.md` + 源码 | 已具备 |
| 3 | 单元测试代码 | `03-单元测试代码与覆盖说明.md` + 测试代码 | 已具备 |
| 4 | 集成测试代码 | `04-集成测试代码与结果说明.md` + 集成测试代码 | 已具备 |
| 5 | CI/CD 配置与运行记录 | `05-CI-CD配置与运行记录.md` + `.gitlab/*.yml` | 已具备 |
| 6 | AI 代码信任度实验报告 | `06-AI代码信任度实验报告.md` | 已具备 |
| 7 | AI 调试对决实验报告 | `07-AI调试对决实验报告.md` | 已具备 |
| 8 | Bug 修复日志 | `08-Bug修复日志.md` | 已具备 |
| 9 | 演示说明 | `09-演示说明.md` | 已具备 |
| 10 | AI 协作反思日志 #4 | `10-AI协作反思日志#4.md` | 已具备 |

## 3. Sprint 目标

P4 阶段目标按“先保证 P0/P1 可运行，再保留 P2 作为扩展”的原则裁剪：

- P0：完成注册登录、任务发布/查询/详情、抢单、订单确认/完成/取消、基础鉴权和数据库初始化。
- P1：完成评价与信用、消息通知、举报处理和基础管理后台。
- 测试：后端单元/切片/集成测试与前端 Vitest 测试均可运行，覆盖率报告可生成。
- CI/CD：配置 GitLab 父子流水线，覆盖依赖安装、质量检查、测试、构建和产物归档。
- 演示：准备本地 Docker + 前后端启动方式、管理员种子账号和核心演示路径。

## 4. Sprint 合并任务看板

| 优先级 | 模块 | 任务 | 估计工时 | 负责人 | 状态 | 完成标准与证据 |
| --- | --- | --- | --- | --- | --- | --- |
| P0 | 用户管理 | 注册、登录、JWT 鉴权、个人资料、实名认证提交 | 4h | 李宇瀚、管泽昊 | 已完成 | `AuthController`、`UserController`、`SecurityConfig`、`LoginView.vue`、`RegisterView.vue`，后端用户测试通过 |
| P0 | 需求发布 | 发布任务、任务列表、详情、搜索筛选、收藏、评论 | 4h | 李宇瀚 | 已完成 | `TaskController`、`TaskService`、`TaskQueryService`、`Home.vue`、`PublishTask.vue`、`TaskDetail.vue` |
| P0 | 订单管理 | 抢单、确认、完成、取消、拒绝、放弃、订单列表/详情 | 4h | 李宇瀚、徐航宇 | 已完成 | `OrderController`、`GrabService`、`OrderStatusService`、`OrderList.vue`、`OrderDetail.vue`，并发抢单集成测试通过 |
| P0 | 基础设施 | Maven 多模块、统一响应、异常处理、CORS、数据库脚本、Redis/Redisson | 4h | 管泽昊 | 已完成 | `campushub-common`、`schema.sql`、`application*.yml`、`docker-compose.yml` |
| P1 | 评价与信用 | 完成后评价、重复评价拦截、信用分调整、信用记录展示 | 3h | 李宇瀚、徐航宇 | 已完成 | `ReviewController`、`ReviewService`、`CreditCalculator`、`ProfileCredit.vue`，评价信用集成测试通过 |
| P1 | 消息通知 | 站内消息、未读数、已读/删除、公告、订单聊天、事件通知 | 3h | 李宇瀚 | 已完成 | `MessageController`、`NoticeController`、`ChatController`、`NotificationService`、`MessageCenter.vue` |
| P1 | 举报与管理后台 | 举报提交/处理、用户封禁、实名审核、任务下架、统计看板、公告管理 | 4h | 吴禹成、管泽昊、李宇瀚 | 已完成 | `ReportController`、`AdminStatsController`、`AdminUserController`、`AdminTaskController` 与 `frontend/src/views/admin/*` |
| 测试 | 单元测试与覆盖率 | 后端服务/控制器/契约测试，前端组件/页面/API/store 测试，覆盖率报告 | 6h | 徐航宇 | 已完成 | 后端 285 个本机测试通过；前端 90 个测试通过；后端业务模块 JaCoCo 行覆盖率 75.67%，前端行覆盖率 81.67% |
| 测试 | 集成测试 | 注册登录、任务 HTTP 流、并发抢单、评价信用、举报、消息公告 | 4h | 徐航宇、管泽昊 | 已完成 | 6 个集成测试类、16 个用例通过，使用 Testcontainers 启动 PostGIS 和 Redis |
| DevOps | CI/CD | GitLab 父子流水线、前后端安装/质量检查/测试/构建 | 4h | 管泽昊、徐航宇 | 已完成 | `.gitlab-ci.yml`、`.gitlab/backend.yml`、`.gitlab/frontend.yml` |
| P2 | 论坛社区 | 论坛表结构与模块预留 | 2h | 可选 | 未纳入主演示 | 数据库含 `t_post`、`t_comment` 等表，`campushub-forum` 目前为模块骨架，不写作已完成功能 |

## 5. AI 辅助规划与人工确认

AI 主要用于拆解任务、对照 P4 验收条款、定位测试/文档不一致问题和汇总源码证据。人工确认结果如下：

- 必须进入本阶段：用户、任务、订单、评价信用、消息、举报、基础管理后台、测试、CI、演示说明。
- 可降级或延期：论坛社区、深度智能推荐、生产级部署、封禁时长持久化。
- 技术风险：数据库/Redis/Docker 环境依赖、后端覆盖率报告生成、前端大 chunk 构建警告。
- 集成风险：账号注销页面存在，但后端尚未实现“有未完成订单禁止注销”的校验，不能作为主演示功能宣传。

## 6. 当前风险与处理

| 风险 | 影响 | 当前处理 |
| --- | --- | --- |
| GitLab 页面运行记录不在仓库内 | 不能从本地直接证明 GitLab 最近一次页面状态 | 已保留 CI 配置和本地同等命令运行记录，提交后需在 GitLab Runner 上再跑一次并截图 |
| 后端 JaCoCo 未写入 POM 固定配置 | 本地可用命令生成报告，但 CI 默认不归档后端 coverage | 本次已生成 `target/site/jacoco` 报告；如老师要求 CI 归档，可把 JaCoCo 插件固化进 POM |
| 前端生产包超过 500 kB 警告 | 不影响构建通过，但影响后续性能优化 | P4 暂不阻塞，后续可拆分 Element Plus 或路由 chunk |
| 论坛模块未完成 | 如果写入主演示会与代码不一致 | 明确列为 P2 预留，不作为 P4 已实现功能 |
| 账号注销未校验未完成订单 | 与前端确认文案/公开协议存在差距 | 演示说明中不将账号注销列为核心闭环 |

## 7. 完成判定

截至 2026-06-07，P4 所需 10 项文档交付物已齐全；P0/P1 主流程已有源码、测试和构建证据支撑。

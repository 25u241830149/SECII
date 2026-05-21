# CampusHub E-R 图说明

本文档配合 `ER图.png` / `ER图.svg` 使用，说明核心实体、主键、外键和实体间关系。

## 1. 核心实体及属性

| 实体 | 表名 | 属性列 (含主外键) | 说明 |
|---|---|---|---|
| 用户 | `u_user` | `id`(主键), `student_id`(学号), `password`(哈希密码), `email`(邮箱), `phone`(手机号), `nickname`(昵称), `real_name`(真实姓名), `department`(所属院系), `avatar_url`(头像), `role`(角色), `credit_score`(信用分), `status`(账号状态), `create_time`, `update_time`, `is_deleted` | 存储用户基础信息、联系方式、角色、信用分与账号状态 |
| 用户认证 | `t_user_verification` | `id`(主键), `user_id`(外键), `reviewer_id`(外键), `real_name`(真实姓名), `student_card_image`(证件照片), `status`(审核状态), `reject_reason`(驳回原因), `create_time`, `update_time` | 存储学生证/统一身份认证材料与审核结果 |
| 互助任务 | `t_task` | `id`(主键), `publisher_id`(外键), `title`(标题), `description`(描述), `category`(分类), `location`(地点), `reward`(报酬), `status`(任务状态), `create_time`, `update_time`, `is_deleted` | 存储用户发布的快递代取、学习辅导、二手交易、组队等任务 |
| 任务收藏 | `t_task_favorite` | `id`(主键), `user_id`(外键), `task_id`(外键), `create_time` | 用户收藏互助任务的中间实体 |
| 订单 | `t_order` | `id`(主键), `task_id`(外键), `poster_id`(外键), `helper_id`(外键), `status`(订单状态: 进行中/已完成/纠纷等), `version`(乐观锁版本号), `create_time`, `update_time`, `is_deleted` | 存储任务被接单后的订单流转信息 |
| 评价 | `t_review` | `id`(主键), `order_id`(外键), `reviewer_id`(外键), `target_user_id`(外键), `rating`(星级1-5), `content`(文字评价), `create_time` | 存储订单完成后的双向评价和评分 |
| 站内消息 | `t_message` | `id`(主键), `receiver_id`(外键), `type`(消息类型), `title`(标题), `content`(内容), `is_read`(是否已读), `create_time` | 存储订单状态提醒、评价提醒等站内消息 |
| 订单聊天消息 | `t_chat_message` | `id`(主键), `order_id`(外键), `sender_id`(外键), `receiver_id`(外键), `content`(聊天正文), `is_read`(是否已读), `create_time` | 存储基于订单的一对一文字聊天记录 |
| 系统公告 | `t_notice` | `id`(主键), `publisher_id`(外键), `title`(标题), `content`(公告正文), `create_time`, `update_time` | 存储管理员发布的系统公告 |
| 论坛帖子 | `t_post` | `id`(主键), `author_id`(外键), `title`(标题), `content`(正文), `category`(分类), `view_count`(浏览量), `like_count`(点赞), `comment_count`(评论数), `is_top`(置顶标识), `is_recommended`(推荐标识), `create_time`, `update_time`, `is_deleted` | 存储校园论坛帖子正文、分类、互动统计和运营标记 |
| 论坛评论 | `t_comment` | `id`(主键), `post_id`(外键), `author_id`(外键), `reply_to_comment_id`(外键), `content`(评论内容), `create_time`, `is_deleted` | 存储帖子评论和评论回复关系 |
| 帖子点赞 | `t_post_like` | `id`(主键), `user_id`(外键), `post_id`(外键), `create_time` | 用户点赞论坛帖子的中间实体 |
| 帖子收藏 | `t_post_favorite` | `id`(主键), `user_id`(外键), `post_id`(外键), `create_time` | 用户收藏论坛帖子的中间实体 |
| 举报 | `t_report` | `id`(主键), `reporter_id`(外键), `target_user_id`(外键), `handler_id`(外键), `task_id`(外键), `order_id`(外键), `post_id`(外键), `comment_id`(外键), `target_type`(目标类型), `target_id`(目标ID), `reason`(举报原因), `status`(处理状态), `result`(处理结果), `create_time`, `update_time` | 存储用户对违规用户、任务、订单、帖子、评论的举报及处理结果 |

## 2. 主键与外键

| 表名 | 主键 | 外键 |
|---|---|---|
| `u_user` | `id` | 无 |
| `t_user_verification` | `id` | `user_id` -> `u_user.id`；`reviewer_id` -> `u_user.id` |
| `t_task` | `id` | `publisher_id` -> `u_user.id` |
| `t_task_favorite` | `id` | `user_id` -> `u_user.id`；`task_id` -> `t_task.id` |
| `t_order` | `id` | `task_id` -> `t_task.id`；`poster_id` -> `u_user.id`；`helper_id` -> `u_user.id` |
| `t_review` | `id` | `order_id` -> `t_order.id`；`reviewer_id` -> `u_user.id`；`target_user_id` -> `u_user.id` |
| `t_message` | `id` | `receiver_id` -> `u_user.id` |
| `t_chat_message` | `id` | `order_id` -> `t_order.id`；`sender_id` -> `u_user.id`；`receiver_id` -> `u_user.id` |
| `t_notice` | `id` | `publisher_id` -> `u_user.id` |
| `t_post` | `id` | `author_id` -> `u_user.id` |
| `t_comment` | `id` | `post_id` -> `t_post.id`；`author_id` -> `u_user.id`；`reply_to_comment_id` -> `t_comment.id` |
| `t_post_like` | `id` | `user_id` -> `u_user.id`；`post_id` -> `t_post.id` |
| `t_post_favorite` | `id` | `user_id` -> `u_user.id`；`post_id` -> `t_post.id` |
| `t_report` | `id` | `reporter_id` -> `u_user.id`；`target_user_id` -> `u_user.id`；`handler_id` -> `u_user.id`；`task_id` -> `t_task.id`；`order_id` -> `t_order.id`；`post_id` -> `t_post.id`；`comment_id` -> `t_comment.id` |

## 3. 实体关系

| 关系 | 关系名 | 基数 | 说明 |
|---|---|---|---|
| 用户 -> 互助任务 | 发布 | 1:N | 一个用户可以发布多个任务，一个任务只有一个发布者 |
| 用户 -> 用户认证 | 提交/审核 | 1:N | 一个用户可以提交多次认证记录；管理员也是用户，通过 `reviewer_id` 关联审核人 |
| 用户 -> 订单 | 参与 | 1:N | 用户可作为发布者参与多个订单，也可作为接单者参与多个订单 |
| 互助任务 -> 订单 | 生成 | 1:0..1 | 一个任务首版业务中最多生成一个有效订单；任务未被接单时可以没有订单 |
| 订单 -> 评价 | 产生 | 1:0..2 | 一个订单完成后最多产生双方两条评价 |
| 用户 -> 评价 | 评价/被评价 | 1:N | 用户既可以作为评价者，也可以作为被评价者 |
| 用户 -> 站内消息 | 接收 | 1:N | 一个用户可以收到多条站内消息 |
| 用户 -> 系统公告 | 发布 | 1:N | 管理员用户可以发布多条系统公告 |
| 订单 -> 订单聊天消息 | 产生 | 1:N | 一个订单下可以产生多条聊天消息 |
| 用户 -> 订单聊天消息 | 收发 | 1:N | 用户可以发送和接收多条订单聊天消息 |
| 用户 <-> 互助任务 | 收藏任务 | M:N | 通过 `t_task_favorite` 实现任务收藏关系 |
| 用户 -> 论坛帖子 | 发布帖子 | 1:N | 一个用户可以发布多个论坛帖子 |
| 论坛帖子 -> 论坛评论 | 包含 | 1:N | 一个帖子可以包含多条评论 |
| 用户 -> 论坛评论 | 发表 | 1:N | 一个用户可以发布多条评论 |
| 论坛评论 -> 论坛评论 | 回复 | 1:N | 评论可通过 `reply_to_comment_id` 回复其他评论 |
| 用户 <-> 论坛帖子 | 点赞帖子 | M:N | 通过 `t_post_like` 实现点赞关系 |
| 用户 <-> 论坛帖子 | 收藏帖子 | M:N | 通过 `t_post_favorite` 实现帖子收藏关系 |
| 用户 -> 举报 | 发起/处理 | 1:N | 一个用户可以发起多条举报，也可以作为被举报对象或处理管理员 |
| 任务/订单/帖子/评论 -> 举报 | 被举报目标 | 0..1:N | 举报目标可以是任务、订单、帖子或评论中的一种 |

## 4. 设计补充

- `t_task_favorite`、`t_post_like`、`t_post_favorite` 是多对多关系的中间实体，建议分别为 `(user_id, task_id)`、`(user_id, post_id)` 添加唯一约束。
- `t_order.version` 用于乐观锁，配合 Redisson 锁处理抢单并发。
- `t_report` 是根据需求规格说明书中的“举报系统”和“举报处理中心”补充的治理实体，最终实现时可根据项目范围选择是否落库。
- `u_user.credit_score` 存储当前信用分，信用等级可由信用分按规则映射生成，不需要单独存储。

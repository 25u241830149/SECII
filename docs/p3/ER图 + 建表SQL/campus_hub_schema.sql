-- 1. 用户表 (u_user)
CREATE TABLE `u_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` VARCHAR(32) NOT NULL COMMENT '学号',
  `password` VARCHAR(128) NOT NULL COMMENT '哈希密码',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `role` TINYINT NOT NULL COMMENT '角色',
  `credit_score` INT NOT NULL COMMENT '信用分',
  `status` TINYINT NOT NULL COMMENT '账号状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基本信息';

-- 2. 用户认证表 (t_user_verification)
CREATE TABLE `t_user_verification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联用户',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '外键（逻辑）：关联审核人(管理员)',
  `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
  `student_card_image` VARCHAR(255) NOT NULL COMMENT '证件照片',
  `status` TINYINT NOT NULL COMMENT '审核状态',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证';

-- 3. 互助任务表 (t_task)
CREATE TABLE `t_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `publisher_id` BIGINT NOT NULL COMMENT '外键（逻辑）：发布者ID',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `description` TEXT COMMENT '描述',
  `category` TINYINT NOT NULL COMMENT '分类',
  `location` VARCHAR(128) DEFAULT NULL COMMENT '地点',
  `reward` DECIMAL(10,2) DEFAULT '0.00' COMMENT '报酬',
  `status` TINYINT NOT NULL COMMENT '任务状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互助任务';

-- 4. 任务收藏表 (t_task_favorite)
CREATE TABLE `t_task_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联用户',
  `task_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联任务',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_task` (`user_id`, `task_id`) COMMENT '补充设计：添加联合唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务收藏';

-- 5. 订单表 (t_order)
CREATE TABLE `t_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联任务',
  `poster_id` BIGINT NOT NULL COMMENT '外键（逻辑）：发单人(任务发布者)',
  `helper_id` BIGINT NOT NULL COMMENT '外键（逻辑）：接单人',
  `status` TINYINT NOT NULL COMMENT '订单状态',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号(依据设计补充)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- 6. 评价表 (t_review)
CREATE TABLE `t_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联订单',
  `reviewer_id` BIGINT NOT NULL COMMENT '外键（逻辑）：评价人',
  `target_user_id` BIGINT NOT NULL COMMENT '外键（逻辑）：被评价人',
  `rating` TINYINT NOT NULL COMMENT '星级1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '文字评价',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价';

-- 7. 站内消息表 (t_message)
CREATE TABLE `t_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `receiver_id` BIGINT NOT NULL COMMENT '外键（逻辑）：接收人',
  `type` TINYINT NOT NULL COMMENT '消息类型',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- 8. 订单聊天消息表 (t_chat_message)
CREATE TABLE `t_chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联订单',
  `sender_id` BIGINT NOT NULL COMMENT '外键（逻辑）：发送人',
  `receiver_id` BIGINT NOT NULL COMMENT '外键（逻辑）：接收人',
  `content` VARCHAR(1000) NOT NULL COMMENT '聊天正文',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单聊天消息';

-- 9. 系统公告表 (t_notice)
CREATE TABLE `t_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `publisher_id` BIGINT NOT NULL COMMENT '外键（逻辑）：发布人(管理员)',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '公告正文',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告';

-- 10. 论坛帖子表 (t_post)
CREATE TABLE `t_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `author_id` BIGINT NOT NULL COMMENT '外键（逻辑）：作者',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '正文',
  `category` TINYINT NOT NULL COMMENT '分类',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '置顶标识',
  `is_recommended` TINYINT NOT NULL DEFAULT 0 COMMENT '推荐标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发帖时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子';

-- 11. 论坛评论表 (t_comment)
CREATE TABLE `t_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联帖子',
  `author_id` BIGINT NOT NULL COMMENT '外键（逻辑）：评论发表人',
  `reply_to_comment_id` BIGINT DEFAULT NULL COMMENT '外键（逻辑）：回复目标评论ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛评论';

-- 12. 帖子点赞表 (t_post_like)
CREATE TABLE `t_post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '外键（逻辑）：点赞人',
  `post_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联帖子',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`) COMMENT '补充设计：添加联合唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞';

-- 13. 帖子收藏表 (t_post_favorite)
CREATE TABLE `t_post_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '外键（逻辑）：收藏人',
  `post_id` BIGINT NOT NULL COMMENT '外键（逻辑）：关联帖子',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post_fav` (`user_id`, `post_id`) COMMENT '补充设计：添加联合唯一约束'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏';

-- 14. 举报表 (t_report)
CREATE TABLE `t_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reporter_id` BIGINT NOT NULL COMMENT '外键（逻辑）：举报人',
  `target_user_id` BIGINT NOT NULL COMMENT '外键（逻辑）：被举报人',
  `handler_id` BIGINT DEFAULT NULL COMMENT '外键（逻辑）：处理人(管理员)',
  `task_id` BIGINT DEFAULT NULL COMMENT '外键（逻辑）：关联任务(可选)',
  `order_id` BIGINT DEFAULT NULL COMMENT '外键（逻辑）：关联订单(可选)',
  `post_id` BIGINT DEFAULT NULL COMMENT '外键（逻辑）：关联帖子(可选)',
  `comment_id` BIGINT DEFAULT NULL COMMENT '外键（逻辑）：关联评论(可选)',
  `target_type` TINYINT NOT NULL COMMENT '目标类型',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `reason` VARCHAR(255) NOT NULL COMMENT '举报原因',
  `status` TINYINT NOT NULL COMMENT '处理状态',
  `result` VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报';

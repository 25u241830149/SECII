# CampusHub 校园互助服务平台 API 规范文档

## 1. 文档说明

本文档用于统一 CampusHub 平台在开发过程中的后端接口设计，作为前后端联调、测试用例编写、接口验收和后续维护的统一依据。

---

## 2. 统一设计约定

### 2.1 基础信息

- 服务基础前缀：/api
- 数据格式：application/json
- 字符编码：UTF-8
- 时间字段：ISO 8601 字符串，示例：2026-05-07T20:30:00+08:00

### 2.2 鉴权与身份约束

- 用户注册、用户登录允许匿名访问。
- 普通业务接口默认需要携带 JWT 登录态。
- 后台管理接口默认要求 ADMIN 角色。
- 请求是否需要显式传递 userId、publisherId、helperId、authorId、senderId、reviewerId，以具体接口定义为准。
- 资源定位统一通过路径参数或 query 参数传递目标资源 ID，例如 userId、taskId、orderId、postId。

请求头约定：

```http
Authorization: Bearer <token>
```

### 2.3 REST 风格约定

- 资源查询使用 GET。
- 资源创建使用 POST。
- 资源更新使用 PUT。
- 资源删除使用 DELETE。
- 列表筛选、搜索、分页统一使用 query 参数。
- 订单确认、完成、取消等强业务动作使用动作型子路径，以保证状态流转语义清晰：/confirm、/complete、/cancel。

### 2.4 统一响应格式

所有接口统一返回 ApiResponse<T>：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

字段说明：

- code：业务状态码
- message：响应说明
- data：实际返回数据；失败时固定为 null

错误响应示例：

```json
{
  "code": 422,
  "message": "score must be between 1 and 5",
  "data": null
}
```

### 2.5 分页响应格式

涉及列表分页的接口，data 字段统一使用 PageResponse<T>：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "page": 1,
    "size": 10
  }
}
```

分页约定：

- page 默认值：1
- size 默认值：10
- size 最大值：50

### 2.6 通用错误码定义

| 错误码 | 含义 | 典型场景 |
| --- | --- | --- |
| 200 | 成功 | 请求处理成功 |
| 400 | 请求格式错误 | 缺少必填字段、字段类型错误、分页参数非法 |
| 401 | 未认证或 Token 失效 | 未登录访问业务接口、Token 过期 |
| 403 | 无权限 | 普通用户访问后台接口、越权修改他人资源 |
| 404 | 资源不存在 | 用户、任务、订单或帖子不存在 |
| 409 | 资源冲突 | 用户名重复、重复接单、重复评价、状态不允许变更 |
| 422 | 业务校验失败 | 评分超范围、订单未完成不可评价、账号存在未完成订单不可注销 |
| 500 | 服务器内部错误 | 未预期异常 |

### 2.7 通用枚举定义

| 枚举 | 可选值 | 说明 |
| --- | --- | --- |
| Role | USER, ADMIN | 用户角色 |
| VerificationStatus | PENDING, APPROVED, REJECTED | 实名认证状态 |
| TaskCategory | EXPRESS, STUDY, SECOND_HAND, TEAM_UP, OTHER | 任务分类 |
| TaskStatus | OPEN, IN_PROGRESS, COMPLETED, CANCELLED, OFFLINE | 任务状态 |
| OrderStatus | PENDING, CONFIRMED, COMPLETED, CANCELLED | 订单状态 |
| SortType | time, hot | 任务列表排序方式 |
| PostSortType | latest, views, recommend | 论坛列表排序方式 |
| PostCategory | HELP, STUDY, TRADE, LOST_FOUND, TEAM_UP, OTHER | 帖子分类 |
| MessageType | ORDER, CHAT, NOTICE, SYSTEM | 消息类型 |

### 2.8 关键业务约束

- 任务被接单后，任务状态转为 IN_PROGRESS，并默认不再出现在开放任务列表中。
- 订单状态流转固定为 PENDING -> CONFIRMED -> COMPLETED，或在 PENDING/CONFIRMED 阶段进入 CANCELLED。
- 订单完成后，双方各自仅允许提交一条评价。
- 帖子编辑仅允许作者在发布后 24 小时内执行；删除可由作者本人或管理员执行。

---

## 3. 核心返回对象摘要

| 对象 | 字段说明 |
| --- | --- |
| LoginResponse | userId、username、role、token |
| UserProfileDTO | id、username、email、phone、nickname、realName、department、avatar、role、creditScore、verificationStatus |
| UserPublicDTO | userId、nickname、avatar、department、creditScore、creditLevel |
| UserHomeDTO | userId、nickname、avatar、creditScore、creditLevel、publishedTaskCount、completedOrderCount、reviewCount |
| TaskListDTO | taskId、title、category、reward、location、status、publishTime、publisher |
| TaskDetailDTO | 在 TaskListDTO 基础上补充 description、favoriteCount、imageUrls、publisherContactScope |
| OrderListDTO | orderId、taskId、taskTitle、status、roleView、counterpartUser、createdAt、updatedAt |
| OrderDetailDTO | orderId、task、publisher、helper、status、createdAt、confirmedAt、completedAt、cancelReason、chatEnabled |
| ReviewDTO | reviewId、orderId、reviewerId、targetUserId、score、comment、createdAt |
| MessageListDTO | messageId、type、title、contentPreview、isRead、createdAt |
| ChatMessageDTO | messageId、orderId、senderId、receiverId、content、createdAt |
| NoticeDTO | noticeId、title、content、isRead、publishTime |
| PostListDTO | postId、author、category、title、contentPreview、likeCount、favoriteCount、commentCount、createdAt |
| PostDetailDTO | 在 PostListDTO 基础上补充 content、mediaUrls、comments |- ReportDTO：reportId、reporterId、targetType、targetId、reason、description、status、createdAt、handledAt、handlerId、handleRemark| AdminStatsDTO | userCount、taskCount、orderCount、forumPostCount、pendingVerificationCount |

---

## 4. 用户与认证模块

### 4.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 用户注册 | POST | /api/user/register | 否 | body：username:string 必填，4-20 位；password:string 必填，8-20 位；email:string 必填；phone:string 必填；realName:string 必填 | null | 400, 409, 422 |
| 用户登录 | POST | /api/user/login | 否 | body：username:string 必填；password:string 必填 | LoginResponse：userId、username、role、token | 400, 401 |
| 获取个人资料 | GET | /api/user/profile | 是 | query：userId:number 必填 | UserProfileDTO | 401, 404 |
| 更新个人资料 | PUT | /api/user/profile | 是 | query：userId:number 必填；body：email?:string；phone?:string；nickname?:string；realName?:string；department?:string；avatar?:string | UserProfileDTO | 400, 401, 422 |
| 获取信用分 | GET | /api/user/credit | 是 | query：userId:number 必填 | { creditScore:number, creditLevel:string, completedRate:number, cancelledRate:number } | 401, 404 |
| 获取公开用户信息 | GET | /api/user/{userId} | 否 | path：userId:number 必填 | UserPublicDTO，不返回手机号、邮箱、学号 | 404 |
| 获取个人主页 | GET | /api/user/{userId}/home | 否 | path：userId:number 必填 | UserHomeDTO | 404 |
| 提交实名认证 | POST | /api/user/verification/submit | 是 | body：realName:string 必填；studentId:string 必填；college:string 必填；documentUrl:string 必填 | { verificationStatus:string } | 400, 401, 409, 422 |
| 注销账号 | DELETE | /api/user/account | 是 | query：userId:number 必填 | null | 401, 409, 422 |
| 后台封禁用户 | POST | /api/admin/users/{userId}/ban | 管理员 | path：userId:number 必填；body：reason:string 必填；days?:number | { userId:number, status:string, reason:string } | 401, 403, 404, 422 |
| 后台审核认证 | POST | /api/admin/users/{userId}/verify | 管理员 | path：userId:number 必填；body：approved:boolean 必填；remark?:string | { userId:number, verificationStatus:string, remark:string } | 401, 403, 404, 422 |

### 4.2 关键示例

#### 4.2.1 用户注册

请求示例：

```json
{
  "username": "alice",
  "password": "CampusHub123",
  "email": "alice@example.com",
  "phone": "13800000000",
  "realName": "张三"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 4.2.2 用户登录

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "alice",
    "role": "USER",
    "token": "jwt-token"
  }
}
```

---

## 5. 任务模块

### 5.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 发布任务 | POST | /api/tasks | 是 | body：title:string 必填；description:string 必填；category:TaskCategory 必填；reward:number 必填；location:string 必填；expectedCompleteTime?:string；imageUrls?:string[] | { taskId:number, status:string, createdAt:string } | 400, 401, 422 |
| 获取任务详情 | GET | /api/tasks/{taskId} | 否 | path：taskId:number 必填 | TaskDetailDTO | 404 |
| 更新任务 | PUT | /api/tasks/{taskId} | 是 | path：taskId:number 必填；body：title?:string；description?:string；category?:TaskCategory；reward?:number；location?:string；expectedCompleteTime?:string；imageUrls?:string[] | TaskDetailDTO | 400, 401, 403, 404, 409, 422 |
| 获取任务列表 | GET | /api/tasks | 否 | query：category?:TaskCategory；keyword?:string；sort?:SortType；page?:number；size?:number | PageResponse<TaskListDTO> | 400 |
| 删除任务 | DELETE | /api/tasks/{taskId} | 是 | path：taskId:number 必填 | null | 401, 403, 404, 409 |
| 收藏任务 | POST | /api/tasks/{taskId}/favorite | 是 | path：taskId:number 必填 | { taskId:number, favorited:boolean } | 401, 404, 409 |
| 取消收藏任务 | DELETE | /api/tasks/{taskId}/favorite | 是 | path：taskId:number 必填 | { taskId:number, favorited:boolean } | 401, 404 |
| 获取收藏列表 | GET | /api/tasks/favorites | 是 | query：userId:number 必填；page?:number；size?:number；category?:TaskCategory | PageResponse<TaskListDTO> | 400, 401 |
| 后台下架任务 | DELETE | /api/admin/tasks/{taskId} | 管理员 | path：taskId:number 必填；query：reason?:string | null | 401, 403, 404 |

### 5.2 关键示例

#### 5.2.1 发布任务

请求示例：

```json
{
  "title": "代取快递",
  "description": "今晚八点前帮我从驿站取一个快递",
  "category": "EXPRESS",
  "reward": 8,
  "location": "菜鸟驿站",
  "expectedCompleteTime": "2026-05-07T20:00:00+08:00"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1001,
    "status": "OPEN",
    "createdAt": "2026-05-07T16:10:00+08:00"
  }
}
```

#### 5.2.2 获取任务列表

请求路径示例：

```text
GET /api/tasks?category=EXPRESS&keyword=快递&sort=time&page=1&size=10
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "taskId": 1001,
        "title": "代取快递",
        "category": "EXPRESS",
        "reward": 8,
        "location": "菜鸟驿站",
        "status": "OPEN",
        "publishTime": "2026-05-07T16:10:00+08:00",
        "publisher": {
          "userId": 1,
          "nickname": "小智",
          "avatar": "https://example.com/avatar.png",
          "creditLevel": "诚信学生"
        }
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

---

## 6. 订单模块

### 6.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 抢单 | POST | /api/orders/grab | 是 | body：taskId:number 必填 | { orderId:number, taskId:number, status:string, helperId:number, createdAt:string } | 400, 401, 404, 409, 422 |
| 获取订单详情 | GET | /api/orders/{orderId} | 是 | path：orderId:number 必填 | OrderDetailDTO | 401, 403, 404 |
| 确认接单 | POST | /api/orders/{orderId}/confirm | 是 | path：orderId:number 必填；由发布者确认，不需要额外 body | { orderId:number, status:string, confirmedAt:string } | 401, 403, 404, 409 |
| 完成订单 | POST | /api/orders/{orderId}/complete | 是 | path：orderId:number 必填；由发布者确认完成；body：remark?:string | { orderId:number, status:string, completedAt:string, reviewEnabled:boolean } | 401, 403, 404, 409 |
| 取消订单 | POST | /api/orders/{orderId}/cancel | 是 | path：orderId:number 必填；body：reason:string 必填 | { orderId:number, status:string, cancelReason:string, creditAffected:boolean } | 400, 401, 403, 404, 409, 422 |
| 获取订单列表 | GET | /api/orders | 是 | query：userId:number 必填；role?:poster|helper；status?:OrderStatus；page?:number；size?:number | PageResponse<OrderListDTO> | 400, 401 |

### 6.2 关键示例

#### 6.2.1 抢单

请求示例：

```json
{
  "taskId": 1001
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 2001,
    "taskId": 1001,
    "status": "PENDING",
    "helperId": 2,
    "createdAt": "2026-05-07T16:30:00+08:00"
  }
}
```

#### 6.2.2 获取订单详情

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 2001,
    "status": "CONFIRMED",
    "createdAt": "2026-05-07T16:30:00+08:00",
    "confirmedAt": "2026-05-07T16:35:00+08:00",
    "task": {
      "taskId": 1001,
      "title": "代取快递",
      "reward": 8,
      "location": "菜鸟驿站"
    },
    "publisher": {
      "userId": 1,
      "nickname": "小智"
    },
    "helper": {
      "userId": 2,
      "nickname": "阿北"
    },
    "chatEnabled": true
  }
}
```

---

## 7. 评价模块

### 7.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 提交评价 | POST | /api/reviews | 是 | body：orderId:number 必填；targetUserId:number 必填；score:number 必填，1-5；comment:string 必填，最多 200 字 | { reviewId:number, createdAt:string } | 400, 401, 403, 404, 409, 422 |
| 获取评价详情 | GET | /api/reviews/{reviewId} | 是 | path：reviewId:number 必填 | ReviewDTO | 401, 403, 404 |
| 获取用户评价列表 | GET | /api/reviews/user/{userId} | 否 | path：userId:number 必填；query：page?:number；size?:number | PageResponse<ReviewDTO> | 400, 404 |

### 7.2 关键示例

#### 7.2.1 提交评价

请求示例：

```json
{
  "orderId": 2001,
  "targetUserId": 2,
  "score": 5,
  "comment": "完成很及时，沟通顺畅。"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reviewId": 3001,
    "createdAt": "2026-05-07T18:40:00+08:00"
  }
}
```

---

## 8. 消息模块

### 8.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 获取消息列表 | GET | /api/messages | 是 | query：userId:number 必填；page?:number；size?:number | PageResponse<MessageListDTO> | 400, 401 |
| 获取未读数 | GET | /api/messages/unread-count | 是 | query：userId:number 必填 | { unreadCount:number } | 401 |
| 标记消息已读 | PUT | /api/messages/{messageId}/read | 是 | path：messageId:number 必填 | { messageId:number, isRead:boolean } | 401, 403, 404 |
| 删除消息 | DELETE | /api/messages/{messageId} | 是 | path：messageId:number 必填 | null | 401, 403, 404 |
| 获取订单聊天记录 | GET | /api/orders/{orderId}/chat | 是 | path：orderId:number 必填；query：page?:number；size?:number | PageResponse<ChatMessageDTO> | 400, 401, 403, 404 |
| 发送聊天消息 | POST | /api/orders/{orderId}/chat | 是 | path：orderId:number 必填；body：senderId:number 必填；receiverId:number 必填；content:string 必填，最多 500 字 | ChatMessageDTO | 400, 401, 403, 404, 422 |
| 获取首页公告 | GET | /api/notices/latest | 否 | 无 | PageResponse<NoticeDTO> | 400 |
| 获取公告管理列表 | GET | /api/admin/notices | 管理员 | query：page?:number；size?:number | PageResponse<NoticeDTO> | 400, 401, 403 |
| 发布系统公告 | POST | /api/admin/notices | 管理员 | body：title:string 必填；content:string 必填；expiredAt?:string | { noticeId:number, createdAt:string } | 400, 401, 403, 422 |

---

## 9. 论坛模块

### 9.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 发布帖子 | POST | /api/posts | 是 | body：category:PostCategory 必填；title:string 必填；content:string 必填；mediaUrls?:string[] | { postId:number, createdAt:string } | 400, 401, 422 |
| 获取帖子详情 | GET | /api/posts/{postId} | 否 | path：postId:number 必填 | PostDetailDTO | 404 |
| 获取帖子列表 | GET | /api/posts | 否 | query：category?:PostCategory；sort?:PostSortType；page?:number；size?:number | PageResponse<PostListDTO> | 400 |
| 更新帖子 | PUT | /api/posts/{postId} | 是 | path：postId:number 必填；body：category?:PostCategory；title?:string；content?:string；mediaUrls?:string[] | PostDetailDTO | 400, 401, 403, 404, 409, 422 |
| 删除帖子 | DELETE | /api/posts/{postId} | 是 | path：postId:number 必填 | null | 401, 403, 404 |
| 发表评论 | POST | /api/posts/{postId}/comments | 是 | path：postId:number 必填；body：replyToCommentId?:number；content:string 必填 | { commentId:number, createdAt:string } | 400, 401, 404, 422 |
| 点赞帖子 | POST | /api/posts/{postId}/like | 是 | path：postId:number 必填 | { postId:number, liked:boolean, likeCount:number } | 401, 404, 409 |
| 取消点赞帖子 | DELETE | /api/posts/{postId}/like | 是 | path：postId:number 必填 | { postId:number, liked:boolean, likeCount:number } | 401, 404 |
| 收藏帖子 | POST | /api/posts/{postId}/favorite | 是 | path：postId:number 必填 | { postId:number, favorited:boolean, favoriteCount:number } | 401, 404, 409 |
| 取消收藏帖子 | DELETE | /api/posts/{postId}/favorite | 是 | path：postId:number 必填 | { postId:number, favorited:boolean, favoriteCount:number } | 401, 404 |
| 后台置顶帖子 | POST | /api/admin/posts/{postId}/pin | 管理员 | path：postId:number 必填 | { postId:number, pinned:boolean } | 401, 403, 404 |
| 后台推荐帖子 | POST | /api/admin/posts/{postId}/recommend | 管理员 | path：postId:number 必填 | { postId:number, recommended:boolean } | 401, 403, 404 |

---

## 10. 举报模块

### 10.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 提交举报 | POST | /api/reports | 是 | body：targetType:string 必填（TASK/ORDER/USER/POST）；targetId:number 必填；reason:string 必填；description?:string | { reportId:number, status:string, createdAt:string } | 400, 401, 404, 422 |
| 后台处理举报 | POST | /api/admin/reports/{reportId}/handle | 管理员 | path：reportId:number 必填；body：action:string 必填（RESOLVED/DISMISSED）；remark?:string | { reportId:number, status:string, handledAt:string } | 401, 403, 404, 409 |

### 10.2 返回对象

- ReportDTO：reportId、reporterId、targetType、targetId、reason、description、status、createdAt、handledAt、handlerId、handleRemark

### 10.3 关键示例

#### 10.3.1 提交举报

请求示例：

```json
{
  "targetType": "TASK",
  "targetId": 1001,
  "reason": "任务内容违规",
  "description": "该任务包含校外广告信息"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reportId": 4001,
    "status": "PENDING",
    "createdAt": "2026-05-07T19:00:00+08:00"
  }
}
```

#### 10.3.2 后台处理举报

请求示例：

```json
{
  "action": "RESOLVED",
  "remark": "已下架对应任务，并对发布者进行警告"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reportId": 4001,
    "status": "RESOLVED",
    "handledAt": "2026-05-07T20:00:00+08:00"
  }
}
```

---

## 11. 后台统计模块

### 11.1 接口总览

| 接口 | 方法 | 路径 | 鉴权 | 请求参数 | 成功响应 data | 错误码 |
| --- | --- | --- | --- | --- | --- | --- |
| 获取后台统计面板 | GET | /api/admin/stats/dashboard | 管理员 | 无 | AdminStatsDTO | 401, 403 |

成功响应 data 主要字段：

- userCount：用户总数
- taskCount：任务总数
- orderCount：订单总数
- forumPostCount：帖子总数
- pendingVerificationCount：待审核认证数量

---

## 12. 关键失败场景示例

### 12.1 未登录访问业务接口

```json
{
  "code": 401,
  "message": "unauthorized",
  "data": null
}
```

### 12.2 重复接单

```json
{
  "code": 409,
  "message": "task has already been grabbed",
  "data": null
}
```

### 12.3 未完成订单提交评价

```json
{
  "code": 422,
  "message": "order is not completed",
  "data": null
}
```

---

## 13. 文档维护要求

- 接口新增、删除或调整时，应同步更新本文档。

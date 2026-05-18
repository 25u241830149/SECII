# 智协 CampusHub - 校园互助服务平台

## 项目简介

“智协 CampusHub”是一个面向校园场景的互助服务平台，支持学生发布与承接快递代取、学习辅导、二手交易、活动组队等互助需求。平台计划包含任务发布、订单流转、双向评价、信用分、消息通知、举报处理、论坛社区与后台管理等能力。

当前代码进度：

- 后端已完成 `B1.1-B1.10`：Maven 多模块骨架、数据库脚本、common 基础配置、异常处理、统一响应、枚举、JWT、安全工具、通用工具类、敏感词过滤骨架、bootstrap 启动类与配置文件。
- 前端已完成 `F1.1`：Vue 3 + Vite + TypeScript 脚手架，已安装 Element Plus、Pinia、Axios、Vue Router。

## 团队成员

- 需求负责人/组长：吴禹成 241250025
- 架构负责人：管泽昊 241830149
- 开发负责人：李宇瀚 241250029
- 测试负责人：徐航宇 241250015

## 技术栈

### 前端

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Axios
- Vue Router

### 后端

- Java 17
- Spring Boot 3.2.5
- Maven 多模块
- MyBatis-Plus
- JWT
- Spring Security
- Redis
- Redisson
- Springdoc OpenAPI

### 数据库与中间件

- PostgreSQL 15
- PostGIS
- Redis

## 项目结构

```text
SECII/
├── backend/
│   └── campushub-parent/
│       ├── campushub-common/      # 公共配置、异常、响应、枚举、安全、工具、敏感词过滤
│       ├── campushub-user/        # 用户模块，后续 B1.11 起开发
│       ├── campushub-task/        # 任务模块
│       ├── campushub-order/       # 订单模块
│       ├── campushub-review/      # 评价模块
│       ├── campushub-report/      # 举报模块
│       ├── campushub-message/     # 消息模块
│       ├── campushub-forum/       # 论坛模块
│       └── campushub-bootstrap/   # Spring Boot 启动模块与配置文件
├── frontend/                      # Vue 3 + Vite + TypeScript 前端工程
├── docs/                          # 项目文档与 PLAN
├── refs/                          # 课程阶段参考材料
├── self/                          # 个人复习、面试问答、技术总结
├── sql/                           # 预留 SQL 目录
└── README.md
```

## 环境准备

### 1. 基础工具

建议版本：

| 工具 | 建议版本 | 说明 |
| --- | --- | --- |
| JDK | 17 | 后端使用 Java 17 |
| Maven | 3.8+ | 后端构建工具 |
| Node.js | 22+ | 当前本机验证版本为 Node 24.15.0 |
| npm | 随 Node 安装 | 前端依赖管理 |
| PostgreSQL | 15 | 主数据库 |
| PostGIS | 与 PostgreSQL 15 匹配 | 空间扩展 |
| Redis | 6+ / 7+ | 缓存与 Redisson 分布式锁 |

Windows PowerShell 如果直接运行 `npm` 报执行策略错误，可以使用 `npm.cmd`：

```powershell
npm.cmd --version
```

后续 README 中前端命令统一写 `npm.cmd`，普通 macOS/Linux 或 Git Bash 环境可改用 `npm`。

### 2. 数据库准备

默认开发配置在：

```text
backend/campushub-parent/campushub-bootstrap/src/main/resources/application-dev.yml
```

默认连接信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/secii_db
    username: postgres
    password: postgres
```

如果你的本地账号密码不同，推荐使用环境变量覆盖：

```powershell
$env:CAMPUSHUB_DATASOURCE_URL="jdbc:postgresql://localhost:5432/secii_db"
$env:CAMPUSHUB_DATASOURCE_USERNAME="postgres"
$env:CAMPUSHUB_DATASOURCE_PASSWORD="你的数据库密码"
```

创建数据库后，执行初始化脚本：

```powershell
psql -U postgres -d secii_db -f backend/campushub-parent/campushub-bootstrap/src/main/resources/db/schema.sql
```

说明：

- `schema.sql` 中包含 PostGIS / pg_trgm 扩展启用、业务表、索引和管理员种子数据。
- 当前开发环境配置了 `spring.sql.init.mode=never`，后端启动时不会自动执行建表脚本，避免重复初始化。
- 管泽昊本机已安装 PostgreSQL 15 + PostGIS 3.6.2，并已在 `secii_db` 中执行过 `schema.sql`。

### 3. Redis 准备

默认 Redis 配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

Redisson 默认地址：

```yaml
campushub:
  redisson:
    address: redis://localhost:6379
```

如果 Redis 有密码，可设置：

```powershell
$env:CAMPUSHUB_REDIS_PASSWORD="你的Redis密码"
```

如果地址不同，可设置：

```powershell
$env:CAMPUSHUB_REDIS_HOST="localhost"
$env:CAMPUSHUB_REDIS_PORT="6379"
$env:CAMPUSHUB_REDISSON_ADDRESS="redis://localhost:6379"
```

### 4. JWT 与 CORS 配置

开发环境已有默认 JWT secret，只能用于本地开发。生产环境必须设置强随机密钥：

```powershell
$env:CAMPUSHUB_JWT_SECRET="请换成足够长且随机的生产密钥"
```

前端开发地址已加入 CORS 白名单：

```text
http://localhost:5173
http://127.0.0.1:5173
```

## 后端启动说明

进入后端父工程：

```powershell
cd backend/campushub-parent
```

编译全部后端模块：

```powershell
mvn compile
```

只编译 common 和 bootstrap 及其依赖：

```powershell
mvn -pl campushub-common,campushub-bootstrap -am compile
```

启动后端应用：

```powershell
mvn -pl campushub-bootstrap -am spring-boot:run
```

默认后端地址：

```text
http://localhost:8080
```

Swagger UI 地址：

```text
http://localhost:8080/swagger-ui.html
```

注意：

- 启动前请确认 PostgreSQL 和 Redis 已启动。
- 当前业务 Controller 尚未完成，后端主要完成了基础设施和启动配置。
- Spring Security 后续还需要补正式 `SecurityFilterChain`，并把 `JwtAuthFilter` 接入过滤器链。

## 前端启动说明

进入前端目录：

```powershell
cd frontend
```

首次拉取或依赖变化后安装依赖：

```powershell
npm.cmd install
```

启动开发服务器：

```powershell
npm.cmd run dev -- --host 127.0.0.1
```

默认前端地址：

```text
http://127.0.0.1:5173/
```

生产构建检查：

```powershell
npm.cmd run build
```

预览构建产物：

```powershell
npm.cmd run preview
```

说明：

- F1.2 已配置 Vite 开发代理：前端请求 `/api/**` 会在开发环境转发到 `http://localhost:8080`。如需修改后端地址，请调整 `frontend/.env.development` 中的 `VITE_API_PROXY_TARGET`。
- 生产环境默认 API 前缀为 `/api`，需要由部署层 Nginx、网关或同源后端负责转发。
- F1.1 当前全量引入了 Element Plus，构建时 Vite 可能提示首包偏大。后续页面变多时，可以改成 Element Plus 按需导入来优化体积。
- 当前前端只有基础占位首页，后续 F1.3-F1.7 会继续补类型定义、request、路由守卫、Pinia stores 和布局。

## 常见问题

### 1. PowerShell 运行 `npm` 报 “无法加载 npm.ps1”

使用：

```powershell
npm.cmd install
npm.cmd run dev
```

### 2. 后端连接数据库失败

检查：

- PostgreSQL 是否启动
- 数据库 `secii_db` 是否存在
- `application-dev.yml` 或环境变量中的用户名密码是否正确
- 是否已经执行 `schema.sql`

### 3. 后端连接 Redis / Redisson 失败

检查：

- Redis 是否启动
- 端口是否为 `6379`
- 是否需要密码
- `CAMPUSHUB_REDIS_PASSWORD` 和 `CAMPUSHUB_REDISSON_ADDRESS` 是否一致

### 4. 前端请求后端跨域

确认前端地址是：

```text
http://localhost:5173
```

或：

```text
http://127.0.0.1:5173
```

这两个地址已在后端 CORS 配置中放行。

## 当前已完成任务

后端：

- `B1.1` Maven 父 POM
- `B1.2` 9 个 Maven 子模块与依赖关系
- `B1.3` `schema.sql`
- `B1.4` CORS / JWT / Redis / Redisson 配置
- `B1.5` 统一异常处理
- `B1.6` 统一响应、分页响应、枚举
- `B1.7` JWT 生成解析、认证过滤器、安全工具
- `B1.8` 错误码、消息类型常量、日期/加密/校验工具
- `B1.9` 敏感词过滤骨架
- `B1.10` Spring Boot 启动类与配置文件

前端：

- `F1.1` Vue 3 + Vite + TypeScript 脚手架，安装 Element Plus、Pinia、Axios、Vue Router

## 参考文档

- 开发计划：`docs/help_document/PLAN.md`
- 开发环境配置：`docs/help_document/setup.md`
- API 规范文档：`docs/P3/API规范文档.md`
- 架构设计文档：`docs/P2/架构设计文档.md`

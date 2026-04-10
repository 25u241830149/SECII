# 开发环境说明文档

本文档描述了“智协”项目在本地进行开发时的环境准备和启动步骤。由于项目采用了前后端分离的架构，后端使用 SpringBoot，前端使用 Vue + uni-app，请分别按照以下步骤进行配置。

## 1. 环境依赖 (Prerequisites)

在开始开发之前，请确保您的计算机上已安装以下软件：

**后端开发环境：**

- JDK: 17 或以上版本
- Maven: 3.6+
- IDE推荐: IntelliJ IDEA，vscode

**前端开发环境：**

- Node.js: v16 或以上版本
- IDE推荐: VS Code 或 HBuilderX (uni-app 官方推荐)

**数据库与中间件：**

- PostgreSQL: 15 或以上版本
- PostGIS: PostgreSQL 的空间扩展插件（地理位置与距离计算必须配置）
- Redis: 6.0+ (用于缓存、Session 或简单消息队列)

---

## 2. 数据库与中间件准备

### PostgreSQL 与 PostGIS

1. 安装 PostgreSQL 并启动服务。
2. 创建项目数据库（例如名为 `secii_db`）。
3. 在该数据库中开启 PostGIS 插件支持：
   ```sql
   CREATE EXTENSION postgis;
   ```
4. 执行 `sql/` 目录下的初始化脚本完成表结构的创建（若有）。

### Redis

启动本地的 Redis 服务，确认默认运行在 `localhost:6379`。

---

## 3. 后端启动步骤

1. 使用 IntelliJ IDEA 或其他编辑器打开 `backend/` 目录。
2. 刷新 Maven 依赖，下载所有项目所需的 jar 包。
3. 修改配置文件 `src/main/resources/application.yml`（如果有的话），将数据库和 Redis 的连接配置（账号、密码、端口）更改为您本地的配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/secii_db
       username: postgres
       password: 你的密码
     redis:
       host: localhost
       port: 6379
   ```
4. 运行主启动类 `Application.java` 启动后端服务。默认运行在 `localhost:8080`。

---

## 4. 前端启动步骤

前端基于 Vue 和 uni-app，您可以选择命令行启动或使用 HBuilderX。

**命令行模式 (针对 H5 网页开发)：**

1. 打开终端并进入前端目录：
   ```bash
   cd frontend
   ```
2. 安装项目依赖：
   ```bash
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev:h5
   ```
4. 启动完成后，根据终端提示访问本地链接 (通常是 `http://localhost:5173` 或类似地址)。

**HBuilderX 模式 (推荐多端开发)：**

1. 使用 HBuilderX 打开 `frontend/` 文件夹。
2. 在顶部菜单栏选择 **运行** -> **运行到浏览器** -> **Chrome**。
3. 如果需要调试微信小程序，选择 **运行** -> **运行到小程序模拟器** -> **微信开发者工具**（需要先在开发者工具中开启服务端口）。

---

## 5. 常见问题排查

- 如果后端启动报错 `Role postgres does not exist` 或相关数据库异常，请检查您的 PostgreSQL 账号密码配置是否正确。
- 如果前端报依赖错误或缺少相应的模块，尝试删除 `frontend/node_modules/` 文件夹后重新执行 `npm install`。
- 如果需要调用后端接口失败，请检查前端配置的后台 API 接口地址 (baseURL) 是否指向了本地环境 `http://localhost:8080`。

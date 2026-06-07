# Bug 修复日志

更新时间：2026-06-07

本文档对应 P4 交付物 8，记录本阶段编码、测试、CI 与交付文档复核中发现并处理的关键问题。只记录当前仓库能核验的问题，不把未复现的问题写成已修复。

## Bug 1：前端公开文档页测试断言过期

### 现象

执行：

```powershell
cd frontend
npm.cmd run test:coverage
```

`PublicDocumentView.test.ts` 中 2 个用例失败：

- 帮助中心目录断言期望 10 个 `.help-toc-link`，实际为 14 个。
- 注册页点击用户协议后，测试期望旧文案“平台使用规则与双方责任说明”，实际当前文档标题为“用户服务协议”。

### 根因分析

`frontend/src/views/system/publicDocuments.ts` 中帮助中心内容已经扩展为 14 个 section，服务条款文档标题也已调整为“用户服务协议”。测试仍停留在旧文档结构，属于测试断言滞后，不是业务组件错误。

### 修复方案

修改 `frontend/src/test/views/PublicDocumentView.test.ts`：

```diff
- expect(wrapper.findAll('.help-toc-link')).toHaveLength(10)
+ expect(wrapper.findAll('.help-toc-link')).toHaveLength(14)

- expect(wrapper.text()).toContain('平台使用规则与双方责任说明')
+ expect(wrapper.text()).toContain('用户服务协议')
```

### 验证结果

- `npm.cmd run test:coverage` 通过
- 27 个测试文件、90 个用例全部通过
- 前端行覆盖率 81.67%

## Bug 2：P4 文档中集成测试环境记录过期

### 现象

原 P4 文档写到：

- 本机无 Docker。
- 14 个集成测试被 Testcontainers 自动跳过。
- GitLab CI 通过 `docker:dind` 运行集成测试。

### 根因分析

这些描述来自旧环境记录，未重新对照当前仓库和当前机器。实际核验发现：

- `docker --version` 可用。
- `docker ps` 显示 PostGIS 和 Redis 容器 healthy。
- 本机已有 Testcontainers 所需镜像。
- `.gitlab/backend.yml` 使用 PostGIS/Redis service，并通过 `CAMPUSHUB_TESTCONTAINERS_ENABLED=false` 连接外部服务，不是 `docker:dind`。

### 修复方案

- 重新运行后端集成测试。
- 将 `04-集成测试代码与结果说明.md` 改为真实通过记录。
- 将 `05-CI-CD配置与运行记录.md` 改为 GitLab service 模式说明。
- 删除“本机无 Docker”“全部跳过”“docker:dind”等错误表述。

### 验证结果

```powershell
cd backend/campushub-parent
mvn -q "-Dtest=*IntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-DfailIfNoTests=false" test
```

结果：

- 6 个集成测试类通过
- 16 个用例通过
- 失败/错误/跳过均为 0

## Bug 3：README 项目进度说明与当前源码不一致

### 现象

README 前半部分仍写着：

- 后端只完成 `B1.1-B1.10` 基础设施。
- 前端只完成 `F1.1` 脚手架。
- 当前业务 Controller 尚未完成。
- `spring.sql.init.mode=never`。

### 根因分析

README 没有随 P4 代码推进同步更新。当前源码已经包含用户、任务、订单、评价、举报、消息、公告、管理后台等 Controller/Service/页面；`application-dev.yml` 实际配置为 `spring.sql.init.mode=always`。

### 修复方案

更新 README：

- 将当前进度改为 P4 可运行版本。
- 补充已实现后端接口和前端页面。
- 明确论坛模块仍为 P2 骨架。
- 修正 `schema.sql` 初始化说明。
- 删除“Controller 尚未完成”的过期说明。

### 验证结果

通过以下源码证据核验：

- 后端 Controller：`AuthController`、`TaskController`、`OrderController`、`ReviewController`、`ReportController`、`MessageController`、`NoticeController` 等。
- 前端路由和页面：`frontend/src/router/index.ts`、`frontend/src/views/**`。
- 配置文件：`application-dev.yml`、`docker-compose.yml`。

## Bug 4：覆盖率证据不足

### 现象

原 `03-单元测试代码与覆盖说明.md` 只写“测试代码存在且可执行”，同时说明后端没有 JaCoCo 覆盖率百分比。这不足以支撑 P4 “核心模块覆盖率 >=60%”的验收要求。

### 根因分析

仓库未在 POM 中固定 JaCoCo 插件，但本地可以通过 Maven 插件命令生成报告。原文档没有实际生成覆盖率，导致只能用测试数量间接说明。

### 修复方案

执行 JaCoCo 插件命令：

```powershell
cd backend/campushub-parent
mvn -q org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent "-Dtest=!*IntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-DfailIfNoTests=false" test org.jacoco:jacoco-maven-plugin:0.8.12:report
```

并同步运行前端 coverage：

```powershell
cd frontend
npm.cmd run test:coverage
```

### 验证结果

- 后端全部报告模块行覆盖率 66.55%。
- 后端业务模块行覆盖率 75.67%。
- 前端行覆盖率 81.67%。
- 文档已改为使用真实覆盖率数字。

## Bug 5：账号注销文案与后端边界规则不一致

### 现象

前端 `AccountDeletion.vue` 和公开文档提示“账号内无进行中的订单、任务或未完成交易”后才能注销。

### 根因分析

后端 `UserService.deleteAccount` 当前仍有 TODO：

```text
reject deletion when the user still has unfinished orders
```

也就是说，后端尚未真正实现“存在未完成订单时禁止注销”的校验。

### 修复方案

本次 P4 文档修订中不把账号注销写入核心已完成业务闭环，也不作为演示主路径。该问题属于后续代码修复项，不能通过文档包装成已完成。

### 验证结果

- `02-可运行系统代码说明.md` 和 `09-演示说明.md` 已将账号注销列为已知限制。
- 主演示路径仍覆盖注册、登录、发布、抢单、订单完成、评价、消息和信用，不依赖账号注销。

## Bug 6：登录学号前后空格导致误判为账号不存在

### 现象

在“AI 代码信任度实验”和“AI 调试对决”中复核 `AuthService.login` 时发现，AI 直出的登录校验代码直接使用原始学号查询：

```java
User user = userMapper.selectActiveByStudentId(request.studentId());
```

当用户输入 `" 20260001 "` 这类带前后空格的学号时，`validateLoginRequest` 不会判定为空，但数据库会按带空格的字符串精确匹配，导致真实存在的用户被误判为“学号或密码错误”。

### 根因分析

`validateLoginRequest` 只负责校验 `request`、`studentId`、`password` 是否为空或全空格，未对可接受输入做规范化。`UserMapper.xml` 中 `selectActiveByStudentId` 使用：

```sql
WHERE student_id = #{studentId}
  AND is_deleted = false
```

因此学号两端空格会参与精确匹配。该问题属于常见输入边界处理不足。

### 修复方案

修改 `backend/campushub-parent/campushub-user/src/main/java/com/campushub/user/service/AuthService.java`，在查询前规整学号：

```diff
 public LoginResponseDTO login(LoginRequest request) {
     validateLoginRequest(request);
-    User user = userMapper.selectActiveByStudentId(request.studentId());
+    String studentId = request.studentId().trim();
+    User user = userMapper.selectActiveByStudentId(studentId);
     if (user == null) {
         throw new BusinessException(ErrorCode.UNAUTHORIZED, "学号或密码错误");
     }
```

密码字段未做 `trim()`，避免改变用户真实密码语义。

### 验证结果

执行：

```powershell
cd backend/campushub-parent
mvn -pl campushub-user -am '-Dtest=AuthServiceTest,AuthControllerTest,AuthControllerWebMvcTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

结果：

- `AuthControllerTest`：2 个用例通过
- `AuthControllerWebMvcTest`：3 个用例通过
- `AuthServiceTest`：9 个用例通过
- 合计 14 个用例通过，0 failures，0 errors

## Bug 7：PowerShell/Maven 多模块测试命令失败

### 现象

运行登录相关现有测试时，第一次执行：

```powershell
cd backend/campushub-parent
mvn -pl campushub-user -am -Dtest=AuthServiceTest,AuthControllerTest,AuthControllerWebMvcTest -Dit.test=AuthFlowIntegrationTest test
```

PowerShell 直接报错：

```text
参数列表中缺少参量。
```

将 `-Dtest` 参数加引号后，Maven 继续失败：

```text
No tests matching pattern "AuthServiceTest, AuthControllerTest, AuthControllerWebMvcTest" were executed!
```

失败模块为 `campushub-common`。

### 根因分析

该问题包含两层原因：

1. PowerShell 会特殊解析未加引号的逗号分隔参数，导致 `-Dtest=AuthServiceTest,AuthControllerTest,...` 被错误拆分。
2. `-pl campushub-user -am` 会同时构建依赖模块 `campushub-common`，但指定的登录测试类只存在于 `campushub-user`。公共模块没有匹配测试时，Surefire 默认中断构建。

### 修复方案

将 `-Dtest` 整体加引号，并允许依赖模块没有匹配的指定测试：

```powershell
mvn -pl campushub-user -am '-Dtest=AuthServiceTest,AuthControllerTest,AuthControllerWebMvcTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

补充说明：`AuthFlowIntegrationTest` 依赖 Testcontainers/Docker，当前运行环境报 `Could not find a valid Docker environment`，因此该集成测试失败记录为环境限制，不作为本次代码修复对象。

### 验证结果

最终命令执行成功：

```text
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

该命令已同步写入 `07-AI调试对决实验报告.md`，作为 Bug 2 的最终验证命令。

## 演示前检查清单

- 使用 `npm.cmd`，避免 Windows PowerShell 执行策略拦截 `npm.ps1`。
- 后端测试复杂 `-Dtest` 参数建议加引号，避免 PowerShell 误解析。
- 不要在同一工作区并行运行多个 Maven 构建。
- 演示前确认 Docker Desktop、PostGIS、Redis、前端 5173 和后端 8080 均可用。
- GitLab 提交后补一次真实 Pipeline 页面截图或链接。

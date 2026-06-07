# CI/CD 配置与运行记录

更新时间：2026-06-07

## 1. P4 要求对照

P4 要求基础 CI/CD 至少包括：

- 自动安装依赖
- 自动运行静态检查或代码格式检查
- 自动运行单元测试
- 自动运行集成测试
- 自动构建项目，验证可以正常打包、部署
- 保留最近一次运行结果记录

当前仓库提供 GitLab CI 配置文件和本机等价命令运行记录。由于本地仓库不包含 GitLab Web 页面截图，提交后仍需在 GitLab Runner 上执行一次并补充页面截图或流水线链接。

## 2. 流水线文件清单

| 文件 | 作用 |
| --- | --- |
| `.gitlab-ci.yml` | 顶层父流水线，根据变更触发后端或前端子流水线 |
| `.gitlab/backend.yml` | 后端安装、质量检查、单元测试、集成测试、打包 |
| `.gitlab/frontend.yml` | 前端安装、类型检查、覆盖率测试、构建 |

## 3. 顶层流水线结构

`.gitlab-ci.yml` 只有 `trigger` 阶段：

- `backend`：当 `backend/**/*` 或 `.gitlab/backend.yml` 变化时触发。
- `frontend`：当 `frontend/**/*` 或 `.gitlab/frontend.yml` 变化时触发。

这符合 P4 “代码提交后自动检查”的要求，并避免前后端无关变更触发不必要任务。

## 4. 后端子流水线

`.gitlab/backend.yml` 包含 5 个阶段：

| 阶段 | Job | 命令/动作 |
| --- | --- | --- |
| `install` | `backend_install_dependencies` | `mvn ... dependency:go-offline` |
| `quality` | `backend_static_check` | `mvn ... -DskipTests compile` |
| `unit_test` | `backend_unit_test` | `mvn ... "-Dtest=!*IntegrationTest" ... test` |
| `integration_test` | `backend_integration_test` | 连接 PostGIS/Redis service，执行 `"-Dtest=*IntegrationTest"` |
| `build` | `backend_build` | `mvn ... -DskipTests package` |

后端集成测试不是使用 `docker:dind`，而是使用 GitLab service：

- `postgis/postgis:16-3.4`，alias 为 `postgres`
- `redis:7-alpine`，alias 为 `redis`
- `CAMPUSHUB_TESTCONTAINERS_ENABLED=false`
- 测试通过 `CAMPUSHUB_TEST_DATASOURCE_URL`、`CAMPUSHUB_TEST_REDIS_HOST` 等变量连接外部 service

后端测试报告会归档：

```text
backend/campushub-parent/**/target/surefire-reports/
```

## 5. 前端子流水线

`.gitlab/frontend.yml` 包含 3 个阶段和 4 个 job：

| 阶段 | Job | 命令/动作 |
| --- | --- | --- |
| `install` | `frontend_install_dependencies` | `npm ci --cache .npm --prefer-offline` |
| `quality` | `frontend_type_check` | `npm exec vue-tsc -- --noEmit` |
| `quality` | `frontend_test` | `npm run test:coverage` |
| `build` | `frontend_build` | `npm run build` |

前端 coverage 产物会归档：

```text
frontend/coverage/
```

前端构建产物会归档：

```text
frontend/dist/
```

## 6. 本机运行记录

以下命令均在 2026-06-07 当前工作区执行。

### 6.1 前端覆盖率测试

```powershell
cd frontend
npm.cmd run test:coverage
```

结果：

- 27 个测试文件通过
- 90 个测试用例通过
- Statements 79.06%
- Branches 68.53%
- Functions 72.50%
- Lines 81.67%
- 产物：`frontend/coverage/`

### 6.2 前端生产构建

```powershell
cd frontend
npm.cmd run build
```

结果：

- `vue-tsc -b` 通过
- Vite 构建成功
- 产物：`frontend/dist/`
- 构建提示存在大 chunk 警告，不影响 P4 构建通过

### 6.3 后端单元测试与覆盖率

```powershell
cd backend/campushub-parent
mvn -q org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent "-Dtest=!*IntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-DfailIfNoTests=false" test org.jacoco:jacoco-maven-plugin:0.8.12:report
```

结果：

- 285 个测试通过
- 失败/错误/跳过均为 0
- 后端全部报告模块行覆盖率 66.55%
- 后端业务模块行覆盖率 75.67%
- 产物：`backend/campushub-parent/*/target/site/jacoco/`

### 6.4 后端集成测试

```powershell
cd backend/campushub-parent
mvn -q "-Dtest=*IntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-DfailIfNoTests=false" test
```

结果：

- 6 个集成测试类通过
- 16 个集成测试用例通过
- 失败/错误/跳过均为 0
- Testcontainers 成功启动 PostGIS 和 Redis 临时容器

### 6.5 后端打包

```powershell
cd backend/campushub-parent
mvn -q -DskipTests package
```

结果：

- Maven package 成功
- 可执行 Jar：`backend/campushub-parent/campushub-bootstrap/target/campushub-bootstrap-0.0.1-SNAPSHOT.jar`

## 7. 与 P4 CI/CD 要求的映射

| P4 要求 | 仓库现状 | 结论 |
| --- | --- | --- |
| 自动安装依赖 | 后端 `dependency:go-offline`，前端 `npm ci` | 已满足 |
| 自动静态检查/质量检查 | 后端 `compile`，前端 `vue-tsc --noEmit` | 已满足 |
| 自动运行单元测试 | 后端 `backend_unit_test`，前端 `frontend_test` | 已满足 |
| 自动运行集成测试 | 后端 `backend_integration_test` | 已满足 |
| 自动构建 | 后端 `package`，前端 `build` | 已满足 |
| 运行结果记录 | 本文记录本机等价命令结果；GitLab 页面截图需提交后补 | 基本满足，外部截图待补 |

## 8. 当前限制与改进建议

- GitLab Pipeline 页面截图或 URL 不在本地仓库中，提交后需要人工补充最近一次 Runner 运行结果。
- 后端 CI 当前归档 surefire 报告，但未归档 JaCoCo coverage；如老师要求 CI 直接产出后端覆盖率，可把 JaCoCo 插件写入 POM，并在 `.gitlab/backend.yml` 增加 coverage artifact。
- 前端构建存在大 chunk 警告，后续可通过路由拆包或 Element Plus 按需导入优化。

## 9. 结论

CI/CD 配置已覆盖 P4 要求的依赖安装、质量检查、单元测试、集成测试和构建。当前本机运行记录证明同等命令可通过；最终提交前只需在 GitLab Runner 上跑一次并补充页面截图或流水线链接，不能在文档中虚构不存在的 GitLab Web 结果。

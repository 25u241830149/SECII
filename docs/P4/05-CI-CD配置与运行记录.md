# CI/CD 配置与运行记录

更新时间：2026-06-06

## 1. 流水线文件清单

仓库中的 P4 流水线配置文件如下：

- `.gitlab-ci.yml`
- `.gitlab/backend.yml`
- `.gitlab/frontend.yml`

## 2. 流水线结构

### 顶层流水线

`.gitlab-ci.yml` 采用父子流水线触发方式：

- `backend`：当 `backend/**/*` 或 `.gitlab/backend.yml` 变化时触发
- `frontend`：当 `frontend/**/*` 或 `.gitlab/frontend.yml` 变化时触发

### 后端子流水线

`.gitlab/backend.yml` 阶段如下：

1. `install`
2. `quality`
3. `unit_test`
4. `integration_test`
5. `build`

对应动作：

- 安装依赖：`dependency:go-offline`
- 静态检查：`mvn -DskipTests compile`
- 单元测试：`-Dtest=!*IntegrationTest`
- 集成测试：`-Dtest=*IntegrationTest`
- 打包：`mvn -DskipTests package`

### 前端子流水线

`.gitlab/frontend.yml` 阶段如下：

1. `install`
2. `quality`
3. `build`

对应动作：

- 依赖安装：`npm ci`
- 质量检查：`npm exec vue-tsc -- --noEmit`
- 构建：`npm run build`

## 3. 本地运行记录

以下记录均在 2026-06-06 本地执行。

### 前端

```powershell
cd frontend
npm.cmd ci
npm.cmd run build
```

结果：

- 依赖安装成功
- 生产构建成功
- 产物生成于 `frontend/dist/`

### 后端单元测试

```powershell
cd backend/campushub-parent
mvn --% -Dtest=!*IntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false test
```

结果：

- `BUILD SUCCESS`
- 283 个可执行测试全部通过

### 后端集成测试

```powershell
cd backend/campushub-parent
mvn --% -Dtest=*IntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false test
```

结果：

- `BUILD SUCCESS`
- 14 个集成测试被 `Testcontainers` 自动跳过
- 原因：本机无 Docker 环境

### 后端打包

```powershell
cd backend/campushub-parent
mvn -DskipTests package
```

结果：

- `BUILD SUCCESS`
- 产物：`campushub-bootstrap-0.0.1-SNAPSHOT.jar`

## 4. 与 GitLab CI 的对应关系

| 课程要求 | 仓库现状 | 说明 |
| --- | --- | --- |
| 自动安装依赖 | 已满足 | 前后端都已配置 |
| 自动静态检查/格式检查 | 基本满足 | 前端使用 `vue-tsc`，后端使用 `compile` 作为质量关口 |
| 自动单元测试 | 已满足 | 后端已配置 |
| 自动集成测试 | 已满足 | 后端已配置 Testcontainers + `docker:dind` |
| 自动构建 | 已满足 | 前端 `build` 与后端 `package` 均已配置 |

## 5. 当前限制与建议

### 当前限制

- 本地没有 GitLab Runner，因此没有“真实 GitLab 页面截图”
- 本地没有 Docker，因此无法复现集成测试在 Runner 中的真实执行

### 建议

- 你人工上传到 GitLab 后，至少跑一遍完整流水线
- 若页面上显示全部通过，可把截图或日志补充到本文件末尾
- 若老师要求“最近一次流水线运行结果”，建议补上传一张 GitLab Pipeline 页面截图

## 6. 可直接用于复核的结论

- 流水线文件齐全
- 阶段设计满足 P4 任务要求
- 本地已验证前端安装/构建、后端单元测试、后端打包
- 集成测试链路已接入，但真实运行依赖 GitLab Runner 或本地 Docker

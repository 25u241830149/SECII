# Bug 修复日志

更新时间：2026-06-06

本文档记录本次 P4 整理、验证与演示准备过程中遇到的关键问题。

## Bug 1：PowerShell 直接执行 `npm` 触发执行策略错误

### 现象

执行 `npm -v` 或 `npm run build` 时，PowerShell 提示 `npm.ps1 cannot be loaded`。

### 根因分析

当前 Windows PowerShell 启用了脚本签名限制，直接调用 `npm.ps1` 会被拦截。

### 修复方案

统一改用：

```powershell
npm.cmd ci
npm.cmd run build
```

### 验证结果

- `npm.cmd ci` 成功
- `npm.cmd run build` 成功

## Bug 2：Maven 非集成测试命令在 PowerShell 下参数被误解析

### 现象

执行非集成测试命令时报：

```text
Unknown lifecycle phase ".failIfNoSpecifiedTests=false"
```

### 根因分析

PowerShell 对 `-Dtest=!*IntegrationTest` 这类参数存在额外解释，导致后续参数被拆坏。

### 修复方案

改为：

```powershell
mvn --% -Dtest=!*IntegrationTest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false test
```

### 验证结果

- Maven 命令恢复正常
- 283 个非集成测试通过

## Bug 3：并行执行两个 Maven 进程导致假阳性编译错误

### 现象

同时执行后端打包和集成测试时，`campushub-order` 报出大量 `dto/service/class not found` 编译错误。

### 根因分析

两个 Maven 进程同时写入同一工作区的 `target/` 目录，产生中间状态冲突。

### 修复方案

- 不再并行执行同一工作区下的两个 Maven 构建
- 按顺序单独执行 `test` 和 `package`

### 验证结果

- 单独执行 `mvn -DskipTests package` 后，后端打包成功

## Bug 4：本机无 Docker，Testcontainers 集成测试无法真实启动

### 现象

执行集成测试时，日志提示：

```text
Could not find a valid Docker environment
```

### 根因分析

当前机器 `docker` 不在 PATH 中，而所有集成测试依赖 Testcontainers。

### 修复方案

- 本地暂不强行运行容器化集成测试
- 保留现有集成测试与 GitLab `docker:dind` 配置
- 在有 Docker 的环境中再跑一次真实集成测试

### 验证结果

- `mvn --% -Dtest=*IntegrationTest ... test` 返回 `BUILD SUCCESS`
- 14 个集成测试被自动标记为 `skipped`

## 演示前检查清单

- 使用 `npm.cmd`，不要直接用 `npm`
- PowerShell 下执行 Maven 复杂参数时优先用 `--%`
- 本地不要并行跑两个后端 Maven 任务
- 需要真实集成测试结果时，必须先确认 Docker 可用

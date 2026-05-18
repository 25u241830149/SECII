# CampusHub Frontend

Vue 3 + Vite + TypeScript 前端工程。

## 常用命令

```powershell
npm.cmd install
npm.cmd run dev -- --host 127.0.0.1
npm.cmd run build
npm.cmd run preview
```

## 环境变量

开发环境：

```text
.env.development
```

生产环境：

```text
.env.production
```

当前约定：

- `VITE_APP_TITLE`：应用标题
- `VITE_API_BASE_URL`：前端请求 API 前缀，默认 `/api`
- `VITE_API_PROXY_TARGET`：开发环境代理目标，默认 `http://localhost:8080`

开发环境中，Vite 会把 `/api/**` 请求代理到 `VITE_API_PROXY_TARGET`。

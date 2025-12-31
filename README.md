# 宠物管理系统（Pet Project）

一个前后端分离的宠物管理/领养平台示例项目：

- 前端：`pet-view-refactor/`（Vue 3 + Vite + TypeScript + Element Plus）
- 后端：`pet-project/`（Spring Boot + MyBatis-Plus + MySQL + Redis + JWT + WebSocket）
- 一键启动：根目录 `docker-compose.yml`

## 目录结构

- `pet-project/`：后端（Maven 多模块：`pet-common` / `pet-pojo` / `pet-service` / `pet-web`）
- `pet-view-refactor/`：前端
- `docker-compose.yml`：MySQL + Redis + Backend + Frontend（Nginx）
- `pet_project.sql`：MySQL 初始化脚本（表结构 + 示例数据）
- `.env`：docker-compose 环境变量（建议仅在本地使用，避免提交真实密钥）

## 快速开始（Docker Compose）

### 前置条件

- Docker Desktop / Docker Engine（支持 `docker compose`）

### 1) 配置环境变量

编辑根目录 `.env`，至少需要设置：

- `MYSQL_ROOT_PASSWORD`：MySQL root 密码
- `REDIS_PASSWORD`：Redis 密码
- `PET_JWT_USER_SECRET_KEY`：JWT Access Token 密钥
- `PET_JWT_REFRESH_SECRET_KEY`：JWT Refresh Token 密钥

可选配置：

- `FRONTEND_PORT` / `BACKEND_PORT`：对外端口（默认 80/8080）
- `MAVEN_MIRROR` / `NPM_REGISTRY`：构建加速镜像源
- `SPRING_MAIL_*`：邮件（用于验证码/找回密码相关能力）
- `PET_ALIOSS_*`：阿里云 OSS（用于对象存储，非必需）

### 2) 启动

```bash
docker compose up -d --build
```

说明：

- `pet_project.sql` 仅会在 MySQL 数据卷首次初始化时执行；如需重新导入，请先执行 `docker compose down -v` 清理数据卷后再启动。

### 3) 访问

- 前端：`http://localhost:${FRONTEND_PORT:-80}`
- 后端：`http://localhost:${BACKEND_PORT:-8080}`
- API 文档（Knife4j）：`http://localhost:${BACKEND_PORT:-8080}/doc.html`
- OpenAPI JSON：`http://localhost:${BACKEND_PORT:-8080}/v3/api-docs`
- 前端健康检查：`http://localhost:${FRONTEND_PORT:-80}/healthz`

### 4) 停止 / 清理

```bash
docker compose down

# 清理数据卷（会清空 MySQL/Redis 数据）
docker compose down -v
```

## 本地开发（不使用 Docker）

### 后端

前置：JDK 21、Maven（或使用 `mvnw`）、MySQL 8、Redis 7。

1) 创建数据库并导入初始化数据：

- 创建库：`pet_project`
- 导入：根目录 `pet_project.sql`

2) 配置连接信息：

- 开发环境默认读取 `pet-project/pet-web/src/main/resources/application-dev.yml`
- 也可通过环境变量覆盖 `pet.*` 配置（例如 `PET_DATASOURCE_URL` / `PET_REDIS_HOST` / `PET_JWT_USER_SECRET_KEY` 等）

3) 启动后端（从仓库根目录）：

```bash
cd pet-project
./mvnw -pl pet-web -am spring-boot:run
```

Windows（PowerShell）示例：

```powershell
cd pet-project
.\mvnw.cmd -pl pet-web -am spring-boot:run
```

后端默认端口：`http://localhost:8080`

### 前端

前置：Node.js 20+。

```bash
cd pet-view-refactor
npm ci
npm run dev
```

默认开发服务器：`http://localhost:5173`

已内置代理（见 `pet-view-refactor/vite.config.ts`）：

- `/api/**` -> `http://localhost:8080`（并去掉 `/api` 前缀）
- `/ws` -> `ws://localhost:8080`
- `/media/**`、`/uploads/**` -> `http://localhost:8080`

## 关键说明

### Nginx 反向代理（Docker）

`pet-view-refactor/nginx/default.conf.template` 中的规则：

- `/api/**` -> `backend`（转发到后端根路径 `/**`）
- `/ws/**` -> `backend`（WebSocket）
- `/uploads/**`、`/media/**` -> `backend`

### WebSocket 地址

后端 WebSocket 端点为：`/ws/{id}`；前端默认连接：

- `ws://{host}/ws/{userId}` 或 `wss://{host}/ws/{userId}`

可通过前端环境变量覆盖：

- `VITE_WS_URL_TEMPLATE` 或 `VITE_APP_WS_URL_TEMPLATE`

## 技术栈

- 后端：Spring Boot、Spring Security、MyBatis-Plus、MySQL、Redis、JWT、WebSocket、Knife4j/OpenAPI
- 前端：Vue 3、Vite、TypeScript、Pinia、Element Plus、Vue Router、Vue I18n
- 部署：Docker、Docker Compose、Nginx

## 镜像构建/推送（可选）

根目录提供了 `push-to-hub.sh` 作为示例脚本（需按自己的镜像仓库地址与 tag 调整后再使用）。

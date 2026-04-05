# MCLY

萌宠乐园管理系统项目骨架。

## 目录结构

- `backend`：Spring Boot 后端
- `frontend/admin-web`：管理后台
- `frontend/customer-mini`：消费者微信小程序
- `frontend/merchant-mobile`：商户移动端
- `docs`：项目文档

## 当前状态

当前已完成：
- PostgreSQL 初始化脚本
- Spring Boot 后端模块骨架与示例接口
- Web 管理后台占位页
- 消费者小程序页面骨架
- 商户移动端页面骨架

## 本地启动建议

### 启动 PostgreSQL

```bash
docker compose up -d postgres
```

### 启动后端

```bash
cd backend
gradle bootRun
```

### 启动管理后台

```bash
cd frontend/admin-web
npm install
npm run dev
```

## 说明

- 消费者小程序位于 `frontend/customer-mini`
- 商户移动端位于 `frontend/merchant-mobile`
- 数据库初始化脚本位于 `backend/src/main/resources/db`
- Flyway migration 位于 `backend/src/main/resources/db/migration`
- 当前仓库未生成 Gradle Wrapper，如需统一执行方式可后续补 `gradlew`
- 当前已经补了基础写接口：新增会员、预约、人工放行、物资领用
- GitHub Actions CI 位于 `.github/workflows/ci.yml`
- 本地 Git hooks 说明位于 `docs/git-hooks.md`

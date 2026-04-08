# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

萌宠乐园管理系统（MCLY），多端连锁宠物园区管理平台，一期为单体 Spring Boot 后端，三个前端端侧。

## 启动命令

```bash
# 启动 PostgreSQL（依赖 Docker）
docker compose up -d postgres

# 启动后端（Java 17，端口 8080）
cd backend && ./gradlew bootRun

# 启动管理后台（端口由 Vite 决定，通常 5173）
cd frontend/admin-web && npm install && npm run dev

# 运行后端所有测试
cd backend && ./gradlew test

# 运行单个测试类
cd backend && ./gradlew test --tests "com.mcly.customer.web.CustomerMiniControllerTests"
```

消费者小程序（`frontend/customer-mini`）用微信开发者工具打开；商户移动端（`frontend/merchant-mobile`）当前仍是骨架，尚未配置构建工具。

## 后端架构

### 技术栈

Spring Boot 3.3.2 · Java 17 · PostgreSQL 16 · Flyway · Spring Data JPA + JDBC · H2（测试）

### 包结构（`com.mcly`）

每个业务域下均为 `web / service / repository / api` 四层，Repository 层采用 Command/Query 分离：`*CommandRepository` 负责写操作（原生 JDBC），`*QueryRepository` 负责读操作，JPA Repository 用于简单 CRUD。

| 包 | 职责 |
|---|---|
| `common` | `ApiResponse<T>`、`GlobalExceptionHandler`、`QuerySupport`（JDBC 查询帮助类）、`GateProperties`（闸机 HMAC 配置） |
| `common.auth` | 小程序认证：`MiniAppAuthInterceptor`（拦截器）、`TokenStore`（令牌存储）、`AuthContext`（上下文） |
| `entrytoken` | 入园令牌管理：一次性动态二维码生成、HMAC 签名/验签、Token 原子消耗 |
| `member` | 会员档案、宠物档案 |
| `order` | 预约（`Reservation`）、卡种（`MembershipCard`） |
| `customer` | 消费者小程序聚合接口（`/api/c-app/`），含入园二维码生成、非本人操作举报 |
| `merchant` | 商户移动端接口（`/api/m-app/`），含人工放行、核销 |
| `device` | 闸机设备通信：核验（含二维码核验 `verify-qr`）、心跳、事件上报（`/api/device/gate/`） |
| `gate` | 闸机通行规则与事件查询 |
| `boarding` | 宠物寄养订单、每日记录 |
| `grooming` | 宠物美容订单 |
| `material` | 物资领用、报损、库存查询 |
| `pass` | 通行资格枚举（`PassType`）与资格写入 |
| `risk` | 风控事件记录与查询（含 `createWithContent`） |
| `dashboard` | 管理后台汇总看板 |

### API 前缀约定

- `/api/c-app/` — 消费者小程序（含 `GET /entry-token` 二维码生成、`POST /report-unauthorized-entry` 举报）
- `/api/m-app/` — 商户移动端
- `/api/device/gate/` — 闸机设备（含 `POST /verify-qr` 二维码核验、`POST /verify-pass` 传统核验）
- （管理后台接口尚未统一前缀）

### 数据库

- Flyway 管理迁移，文件位于 `backend/src/main/resources/db/migration/`（V1 ~ V10）
- `schema.sql` / `seed.sql` 供 docker-compose 初次初始化用，**与 Flyway migration 内容保持同步**
- 金额字段用 `numeric(12,2)`，扩展字段用 `jsonb`，业务主表标配 `created_at/by/updated_at/by`

### 测试

测试使用 H2（PostgreSQL 兼容模式），Flyway 关闭，由 `schema-test.sql` + `data-test.sql` 初始化（位于 `backend/src/test/resources/`）。新增业务表时需同步维护这两个文件。测试类命名用 `*Tests`，测试方法用 `should...` 风格。

## 前端架构

### 管理后台（`frontend/admin-web`）

Vue 3 + Vite，当前为占位页骨架。

### 消费者小程序（`frontend/customer-mini`）

原生微信小程序，Tab 导航：首页 / 预约 / 订单 / 我的。页面在 `pages/` 下按模块组织（index、tickets、cards、pets、orders、profile、pass-qr）。`pass-qr` 为入园凭证二维码页，通过订单页跳转访问。

### 商户移动端（`frontend/merchant-mobile`）

uni-app 方案，当前骨架，`pages.json` 配置页面路由。

## 代码风格

- Java / Gradle：4 空格缩进
- Vue / JavaScript / WXML / WXSS：2 空格缩进
- 类名 `PascalCase`，方法和字段 `camelCase`，DTO 后缀清晰（`*Request`、`*Response`）
- 提交信息用 Conventional Commits（`feat:`、`fix:`、`chore:` 等），标题祈使句

## 关键业务约束

- **通行资格（`pass_entitlement`）**：普通票首次入园成功后生成当日资格，次日失效；卡种生成跨天长期资格。
- **入园凭证防多人使用（`entry_token`）**：一次性 HMAC-SHA256 令牌，扫码即消耗（`SELECT FOR UPDATE` 原子消耗）。在园状态按 `entry_exit_record.id DESC` 判定（不可改用 `occurred_at`，H2 同秒记录顺序不确定）。核验链中 token 状态检查必须在在园检查之前，否则错误码不准确。架构决策详见 `docs/decisions/001-gate-anti-fraud.md`。
- **人工放行**：必须留审计记录，触发风控预警规则，不作为常规通行路径。
- **物资与零售库存严格分账**，`material_consumption_record` 按门店/宠物/服务单/员工记录消耗。
- 敏感操作（人工放行、报损、跨店调拨）必须校验 RBAC 权限并记录操作人、时间、门店、设备标识。

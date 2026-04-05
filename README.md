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
- 消费者侧门票预约、订单查询、有效入园凭证查询
- 商户侧现场核销队列、闸机核验、人工放行登记与风控留痕

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

## 新增接口约定

### 消费者小程序

- `GET /api/c-app/orders`：返回订单列表，当前字段包含 `orderNo`、`type`、`status`、`amount`、`storeName`、`reservationDate`、`timeSlot`
- `GET /api/c-app/passes`：返回当前有效通行资格，字段包含 `id`、`name`、`status`、`storeName`、`validFrom`、`validTo`、`reentryPolicy`
- `POST /api/c-app/reservations`：创建预约、订单，并同步生成当日有效通行资格

### 商户移动端

- `GET /api/m-app/checkin/orders?storeId=1`：返回当日核销队列，字段包含 `orderId`、`orderNo`、`memberId`、`memberName`、`petName`、`orderType`、`orderStatus`、`storeName`、`reservationDate`、`timeSlot`、`faceBound`、`activeEntitlement`、`entitlementName`、`entitlementValidTo`
- `POST /api/m-app/checkin/verify`：请求体包含 `orderId`、`deviceCode`、`direction`，返回 `allowed`、`reasonCode`、`riskFlagged`、`needManualReview` 等核验结果
- `POST /api/m-app/manual-releases`：登记人工放行，并同步创建风险事件记录

## 验证命令

```bash
cd backend && ./gradlew --no-daemon test --tests com.mcly.customer.web.CustomerMiniControllerTests
cd backend && ./gradlew --no-daemon test --tests com.mcly.merchant.web.MerchantMobileControllerTests
cd backend && ./gradlew --no-daemon test
```

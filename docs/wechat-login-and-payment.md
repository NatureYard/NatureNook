# 微信登录与微信支付集成方案

## 1. 概述

本文档描述消费者小程序（`frontend/customer-mini`）与后端（`backend`）之间的微信登录和微信支付集成方案。当前为开发阶段实现，微信 API 调用以存根（stub）形式提供，保持完整的前后端交互流程，正式上线前替换为真实调用即可。

## 2. 微信登录

### 2.1 流程

```
┌──────────────┐     wx.login()     ┌──────────────┐    code2Session    ┌──────────────┐
│  小程序前端   │ ───── code ──────> │   后端 API    │ ── openid ──────> │   微信服务器   │
│              │ <──── token ────── │              │ <─ openid+key ─── │              │
└──────────────┘                    └──────────────┘                    └──────────────┘
```

1. 小程序启动时 `app.js` 调用 `wx.login()` 获取临时 `code`
2. 前端将 `code` 发送到 `POST /api/c-app/login`
3. 后端调用微信 `code2Session` 接口，用 `code` 换取 `openid`
4. 后端根据 `openid` 查找已有会员，或绑定/创建新会员
5. 后端使用 `TokenStore` 签发 token，返回给前端
6. 前端将 token 存入本地存储，后续所有 `/api/c-app/**` 请求自动附加 `Authorization: Bearer <token>` header

### 2.2 关键文件

| 层 | 文件 | 职责 |
|---|------|------|
| 前端 | `utils/auth.js` | login / ensureLogin / getToken / clearAuth |
| 前端 | `app.js` | 启动时调用 `ensureLogin()`，提供 `waitForLogin()` |
| 前端 | `utils/request.js` | 请求自动附加 token；401 时清除本地 token |
| 后端 | `common/auth/AuthContext.java` | ThreadLocal 存储当前请求的会员 ID |
| 后端 | `common/auth/MiniAppAuthInterceptor.java` | 从 header 读取 token → 设置 AuthContext |
| 后端 | `common/auth/TokenStore.java` | 内存 token 存储（ConcurrentHashMap） |
| 后端 | `common/auth/AuthWebConfig.java` | 注册拦截器，仅拦截 `/api/c-app/**` |
| 后端 | `customer/service/WxAuthService.java` | code → openid → 查找会员 → 签发 token |
| 后端 | `customer/web/CustomerAuthController.java` | `POST /api/c-app/login` 端点 |
| DB | `V9__member_wx_openid.sql` | member 表增加 `wx_openid` 字段 |

### 2.3 开发兼容机制

- **前端**：开发环境（`develop`）下如果后端不可用，`ensureLogin()` 自动降级为 mock 登录，生成本地 mock token
- **后端**：`code2Session` 当前为存根，将 code 直接映射为模拟 openid（`dev_` + code）
- **后端**：拦截器未携带 token 时不返回 401，降级为 `currentCustomer()` 的 `LIMIT 1` 查询，确保现有测试和无 token 调试不受影响

### 2.4 会员绑定策略

当前开发阶段策略：

1. 优先按 `wx_openid` 查找已绑定会员
2. 未找到时，绑定第一个 `wx_openid` 为空的会员（开发便利）
3. 无可绑定会员时抛出异常

正式环境应改为：通过微信手机号授权获取手机号 → 匹配或新建会员 → 绑定 openid。

## 3. 微信支付

### 3.1 流程

```
┌──────────┐  createReservation  ┌──────────┐         ┌──────────┐
│  小程序   │ ── 预约信息 ──────> │   后端    │         │ 微信支付  │
│          │ <─ PENDING_PAY ─── │          │         │          │
│          │                    │          │         │          │
│          │  prepay(orderNo)   │          │ 下单API  │          │
│          │ ──────────────────>│          │ ──────> │          │
│          │ <── 支付参数 ────── │          │ <────── │          │
│          │                    │          │         │          │
│          │  wx.requestPayment │          │         │          │
│          │ ─────────────────────────────────────>  │          │
│          │ <──────── 支付结果 ─────────────────── │          │
│          │                    │          │         │          │
│          │  confirmPayment    │          │  回调    │          │
│          │ ──────────────────>│          │ <────── │          │
│          │ <── 成功 ────────  │ PAID +   │         │          │
│          │                    │ 通行资格  │         │          │
└──────────┘                    └──────────┘         └──────────┘
```

1. 用户提交预约 → `POST /api/c-app/reservations` → 后端创建订单（状态 `PENDING_PAY`）
2. 前端收到订单号后调用 `POST /api/c-app/prepay` → 后端生成预支付参数
3. 前端调用 `wx.requestPayment()` 拉起微信支付界面
4. 用户完成支付后，前端调用 `POST /api/c-app/payment/confirm` 确认支付
5. 后端将订单从 `PENDING_PAY` 更新为 `PAID`，并生成通行资格（`pass_entitlement`）

### 3.2 订单状态流转

```
PENDING_PAY ──支付成功──> PAID ──使用后──> USED
     │
     ├──超时/取消──> CANCELLED
     │
     └──退款──> REFUNDED
```

### 3.3 关键文件

| 层 | 文件 | 职责 |
|---|------|------|
| 前端 | `pages/tickets/index.js` | `submitReservation` → `prepay` → `wx.requestPayment` → `confirmPayment` |
| 前端 | `pages/orders/index.js` | 待支付订单的"立即支付"按钮，触发同样的支付流程 |
| 前端 | `utils/api.js` | `prepay()` 和 `confirmPayment()` 接口封装 |
| 前端 | `utils/mock.js` | `getMockPrepay()` 和 `confirmMockPayment()` 模拟支付 |
| 后端 | `customer/service/CustomerMiniService.java` | `prepay()` 生成支付参数；`confirmPayment()` 确认并生成通行资格 |
| 后端 | `customer/web/CustomerMiniController.java` | `POST /api/c-app/prepay` 和 `POST /api/c-app/payment/confirm` |
| 后端 | `customer/api/PrepayRequest.java` | 预支付请求 DTO |
| 后端 | `customer/api/PrepayResponse.java` | 预支付响应 DTO（timeStamp, nonceStr, package, signType, paySign） |
| DB | `V9__member_wx_openid.sql` | customer_order 表增加 `wx_prepay_id`、`paid_at` 字段 |

### 3.4 开发兼容机制

- **前端**：开发环境下 `callWxPayment()` 跳过 `wx.requestPayment()`（微信开发者工具不支持真实支付），直接模拟支付成功
- **后端**：`prepay()` 当前返回模拟支付参数（mock prepay_id 和 mock sign），不调用真实微信下单 API
- **后端**：`confirmPayment()` 供前端主动确认，正式环境应改为微信回调触发

### 3.5 取消支付处理

用户在微信支付界面取消时：
- 前端检测 `cancel` 错误，提示"已取消支付"
- 弹窗引导用户到订单页，可随时通过"立即支付"按钮重新发起支付
- 订单保持 `PENDING_PAY` 状态，不会丢失

## 4. 正式上线前 TODO

### 4.1 微信登录

- [ ] 在 `application.yml` 中配置 `wx.appId` 和 `wx.appSecret`
- [ ] 将 `WxAuthService.code2Session()` 替换为真实 HTTP 调用：`GET https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code`
- [ ] 实现手机号授权绑定流程（`wx.getPhoneNumber` → 后端解密 → 匹配会员）
- [ ] `MiniAppAuthInterceptor` 开启 401 拒绝（移除降级逻辑）
- [ ] `project.config.json` 填入正式 AppID

### 4.2 微信支付

- [ ] 在 `application.yml` 中配置商户号（mchId）、API v3 密钥、证书路径
- [ ] 引入微信支付 SDK 依赖（如 `wechatpay-java`）
- [ ] 将 `CustomerMiniService.prepay()` 替换为真实统一下单调用：`POST https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi`
- [ ] 新增 `POST /api/c-app/payment/notify` 端点接收微信异步回调，替代前端主动 confirm
- [ ] 实现支付回调签名验证
- [ ] 实现订单超时自动关闭（如 30 分钟未支付 → CANCELLED）

### 4.3 基础设施

- [ ] `TokenStore` 替换为 Redis 实现（支持多实例部署、token 过期淘汰）
- [ ] 配置 HTTPS 域名并在微信公众平台注册 request 合法域名
- [ ] 配置微信支付回调通知 URL（需公网可达的 HTTPS 地址）

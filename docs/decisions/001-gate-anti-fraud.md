# ADR-001: 入园凭证防多人使用方案

**日期**: 2026-04-08
**状态**: 已实施
**影响范围**: `entrytoken`、`device`、`customer`、`risk`

---

## 背景

原始闸机核验 (`GateDeviceService.verifyPass`) 直接接收 `memberId`，仅检查通行资格是否存在。存在三个漏洞：

1. 同一资格可被多人同时使用（无在园状态追踪）
2. `memberId` 明文传输，截图转发零成本（无动态凭证）
3. 持有者无法感知被盗刷（无通知机制）

---

## 决策

### 1. 一次性 HMAC 签名令牌，而非 JWT

**选择**: 服务端生成 UUID token + HMAC-SHA256 签名，存入 `entry_token` 表。

**放弃的方案**: JWT（无状态、无法一次性消耗）。

**原因**: 核心需求是"扫一次即失效"，JWT 的无状态特性反而是缺点。HMAC 签名提供防篡改，数据库行锁提供一次性消耗，两者配合比 JWT 更贴合业务。

### 2. 在园状态通过查询 `entry_exit_record` 判定，而非增加状态字段

**选择**: `isMemberInPark()` 查最新一条 `entry_exit_record`（按 `id DESC`），如果 `direction='ENTRY'` 且 `result='PASSED'` 则在园内。

**放弃的方案**: 在 `member` 表加 `in_park` 布尔字段。

**原因**: 布尔状态字段需要与通行记录严格同步，一旦不一致就无法自愈。查询方式始终与事实记录一致，且 `id DESC` 保证了同一事务内的时间顺序（`occurred_at` 在 H2 中可能相同）。

### 3. 核验链顺序：token 状态检查在在园检查之前

**选择**: `verifyQrPass` 中，先检查 token 是否已 `USED`，再检查在园状态。

**原因**: 顺序反了会导致错误码不准确。同一 token 第二次扫描时，如果先查在园状态会返回 `ALREADY_IN_PARK`（因为第一次扫描已经入园），而实际原因应该是 `TOKEN_ALREADY_USED`。先检查 token 状态能给出更精确的错误信息。

### 4. PostgreSQL 行锁而非 Redis

**选择**: `SELECT FOR UPDATE` + `UPDATE ... WHERE status = 'ACTIVE'`。

**放弃的方案**: Redis `SETNX`。

**原因**: 项目当前无 Redis 依赖。闸机核验 QPS 极低（单店每分钟最多几十次），PostgreSQL 行锁完全够用。引入 Redis 只为这一个场景增加了运维复杂度。

### 5. 配置驱动而非硬编码

**选择**: HMAC secret 和 TTL 通过 `mcly.gate.hmac-secret` / `mcly.gate.token-ttl-seconds` 配置。

**原因**: secret 需要按环境隔离（开发/测试/生产），TTL 可能需要根据现场体验调整。

---

## 不变量（破坏会导致故障）

| 不变量 | 破坏后果 | 保护机制 |
|--------|----------|----------|
| `entry_token.consume` 必须在事务内 `SELECT FOR UPDATE` | 并发扫描同一 token 可能都通过 | `@Transactional` + 行锁 |
| 核验链中 token 状态检查必须在在园检查之前 | 错误码不准确，给用户错误提示 | 代码顺序，无自动化保护 |
| `isMemberInPark` 必须按 `id DESC` 排序而非 `occurred_at DESC` | H2 测试中同一秒多条记录顺序不确定，导致测试不稳定 | 代码注释 |
| `entry_token` 无自动清理 | 表会持续增长 | 已知局限，见下方 |

---

## 已知局限

1. **无自动清理**: `entry_token` 中已消耗/过期的记录不会自动删除。当前数据量极小可忽略。当日活超过 10 万时需加定时任务清理。
2. **60 秒窗口内截图仍可用**: 在 token 过期前，截图理论上可被转发使用一次。但结合在园状态追踪，第二次使用时持有者本人会被拦，且会收到推送通知。
3. **无生物特征验证**: 当前无法区分持手机人是资格所有者还是截图转发者。人脸 1:1 比对是下一阶段方案。
4. **通知为开发存根**: `WxSubscribeMessageService` 仅 log，未接入微信订阅消息 API。
5. **单实例假设**: `SELECT FOR UPDATE` 行锁在单实例 PostgreSQL 下可靠。多实例需确认连接池配置一致。

---

## 演进方向

| 阶段 | 内容 | 触发条件 |
|------|------|----------|
| 2a | 微信订阅消息接入（入园推送） | 微信小程序审核通过后 |
| 2b | `entry_token` 定时清理任务 | 日活 > 10 万或表行数 > 100 万 |
| 3 | 人脸 1:1 比对 | 闸机硬件支持人脸采集 |
| 4 | Redis Token 存储 + 离线缓存 | 多门店并发量显著增长或需要离线容灾 |

---

## 相关文件

- `com.mcly.entrytoken.service.EntryTokenService` — 签名生成/验证
- `com.mcly.device.service.GateDeviceService.verifyQrPass()` — 完整核验链
- `com.mcly.entrytoken.repository.EntryTokenCommandRepository.consume()` — 原子消耗
- `V10__entry_token_and_in_park_tracking.sql` — 数据库迁移

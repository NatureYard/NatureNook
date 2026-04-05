# Repository Guidelines

## 项目结构与模块划分
`backend/` 是 Spring Boot 后端，代码位于 `backend/src/main/java/com/mcly/<domain>`，按 `api`、`web`、`service`、`repository`、`domain` 分层组织。数据库相关文件在 `backend/src/main/resources/db`，Flyway 迁移位于 `db/migration`。

`frontend/admin-web/` 是 Vue 3 + Vite 管理后台。`frontend/customer-mini/` 是微信小程序，页面位于 `pages/**`。`frontend/merchant-mobile/` 是 uni-app 风格的商户移动端骨架。需求说明和方案文档放在 `docs/`。

## 构建、测试与本地开发
- `docker compose up -d postgres`：启动本地 PostgreSQL，端口 `5432`。
- `cd backend && gradle bootRun`：启动后端服务，默认端口 `8080`。
- `cd backend && gradle test`：运行后端测试，使用 `test` profile 和 H2 内存库。
- `cd frontend/admin-web && npm install && npm run dev`：启动管理后台开发环境。
- `cd frontend/admin-web && npm run build`：构建管理后台。
- `cd frontend/admin-web && npm run preview`：预览构建产物。
- `cd frontend/merchant-mobile && npm run dev`：运行当前移动端占位脚手架。

## 代码风格与命名约定
Java 和 Gradle 使用 4 空格缩进；Vue、JavaScript、WXML、WXSS 使用 2 空格缩进。沿用现有 feature-first 目录方式，不要打散 `com.mcly` 下的领域模块。

类名使用 `PascalCase`，方法和字段使用 `camelCase`。接口和 DTO 保持后缀清晰，例如 `*Controller`、`*Service`、`*Repository`、`*Request`、`*Response`。前端当前以无分号风格为主，新增代码保持与所在文件一致。

## 测试要求
后端测试位于 `backend/src/test/java`，当前使用 JUnit 5、Spring Boot Test、MockMvc。测试类命名使用 `*Tests`，测试方法建议采用 `should...` 风格，例如 `shouldCreateReservationAndOrder`。新增接口、参数校验、查询逻辑时必须补测试。前端暂未接入自动化测试，PR 中需写明手工验证步骤。

## 提交与 Pull Request 规范
提交信息沿用现有 Conventional Commits 风格，如 `feat:`、`chore:`。标题用祈使句，聚焦单一改动，例如 `feat: add merchant task board endpoint`。

PR 需说明影响的模块、接口或数据库变更，关联需求或 issue。涉及页面改动时附截图或录屏；涉及初始化、样例数据、环境变量调整时在描述中明确写出。

## 配置与数据变更
不要提交真实账号、密码或密钥。开发环境默认配置在 `backend/src/main/resources/application.yml`，测试配置在 `backend/src/test/resources/application-test.yml`。涉及表结构或初始化数据调整时，优先新增 Flyway migration，不要直接改线上用的结构定义流程。

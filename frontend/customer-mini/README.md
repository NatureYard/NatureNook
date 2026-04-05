# 消费者小程序骨架

## 已初始化内容

- 基础小程序入口
- 首页占位
- 可扩展为购票、预约、卡种、宠物档案、人脸预录入等功能

## 建议下一步

- 接入微信登录
- 构建首页、订单页、会员中心页
- 接入后端订单与通行接口

## 小程序自动化测试

- 当前提供基于 `miniprogram-automator` 的 E2E 骨架，测试文件位于 `tests/e2e/`
- 首次使用前，需要本机已安装微信开发者工具，并设置环境变量 `WECHAT_DEVTOOLS_PATH`
- 安装依赖：`npm install`
- 运行 smoke test：`npm run test:e2e`
- 如需指定项目根目录，可额外设置 `MINIPROGRAM_PROJECT_PATH`

示例：

```bash
export WECHAT_DEVTOOLS_PATH="/path/to/wechatwebdevtools"
cd frontend/customer-mini
npm install
npm run test:e2e
```

未配置开发者工具路径时，测试会自动跳过，便于在 CI 中先做语法校验。


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
- 当前示例用例会在小程序运行态打开 `e2eMockMode`，避免依赖真实后端数据

示例：

```bash
export WECHAT_DEVTOOLS_PATH="/path/to/wechatwebdevtools"
cd frontend/customer-mini
npm install
npm run test:e2e
```

未配置开发者工具路径时，测试会自动跳过，便于在 CI 中先做语法校验。
当前 smoke test 包含三条链路：

1. 首页进入预约页
2. 预约页切换到寄养服务
3. mock 模式下提交预约并跳转订单页

### Windows 原生执行

当前仓库如果放在 WSL 路径下，微信开发者工具自动化容易卡在 WSL 与 Windows CLI 的边界。要跑真实小程序 GUI 自动化，优先使用 Windows 本地目录。

推荐流程：

1. 在 Windows 磁盘下准备一份项目副本，例如 `C:\dev\NatureNook`
2. 用微信开发者工具打开 `C:\dev\NatureNook\frontend\customer-mini`
3. 保持开发者工具可以正常打开该项目
4. 在 Windows `cmd` 或 PowerShell 中执行自动化测试

`cmd` 示例：

```bat
cd /d C:\dev\NatureNook\frontend\customer-mini
set WECHAT_DEVTOOLS_PATH=C:\Program Files (x86)\Tencent\微信web开发者工具\cli.bat
npm install
npm run test:e2e
```

PowerShell 示例：

```powershell
Set-Location C:\dev\NatureNook\frontend\customer-mini
$env:WECHAT_DEVTOOLS_PATH = 'C:\Program Files (x86)\Tencent\微信web开发者工具\cli.bat'
npm.cmd install
npm.cmd run test:e2e
```

仓库中已提供一个 Windows 辅助脚本：`run-e2e-windows.cmd`

```bat
cd /d C:\dev\NatureNook\frontend\customer-mini
run-e2e-windows.cmd
```

脚本约定：

- `WECHAT_DEVTOOLS_PATH` 未设置时，默认使用 `C:\Program Files (x86)\Tencent\微信web开发者工具\cli.bat`
- 该脚本应在 Windows 本地目录中执行，不建议从 WSL 路径直接调用






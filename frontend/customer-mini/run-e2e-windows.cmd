@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
if "%WECHAT_DEVTOOLS_PATH%"=="" set "WECHAT_DEVTOOLS_PATH=C:\Program Files (x86)\Tencent\微信web开发者工具\cli.bat"

cd /d "%SCRIPT_DIR%"

echo Using WECHAT_DEVTOOLS_PATH=%WECHAT_DEVTOOLS_PATH%

call npm.cmd run test:e2e
exit /b %ERRORLEVEL%
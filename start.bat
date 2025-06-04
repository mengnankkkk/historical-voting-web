@echo off
echo 正在启动历史投票系统...

:: 检查Node.js
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误：未找到Node.js，请先运行setup-env.bat
    pause
    exit /b 1
)

:: 检查Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误：未找到Java，请先运行setup-env.bat
    pause
    exit /b 1
)

:: 启动后端服务
cd backend
echo 正在启动后端服务...
start cmd /k "title 后端服务 && java -jar target/historical-voting-0.0.1-SNAPSHOT.jar"

:: 等待5秒让后端完全启动
echo 等待后端服务启动...
timeout /t 5 /nobreak >nul

:: 启动前端服务
cd ../web-ui
echo 正在启动前端服务...
if not exist "node_modules" (
    echo 正在安装前端依赖...
    call npm install
)
start cmd /k "title 前端服务 && npm run dev"

:: 等待3秒
timeout /t 3 /nobreak >nul

:: 打开默认浏览器访问前端页面
start http://localhost:3000

echo.
echo 系统启动完成！
echo -------------------
echo 前端地址：http://localhost:3000
echo 后端地址：http://localhost:8081
echo -------------------
echo.
echo 提示：
echo 1. 如果遇到问题，请先运行setup-env.bat设置环境变量
echo 2. 要停止服务，请关闭对应的命令行窗口
echo.
echo 按任意键退出此窗口...
pause > nul 
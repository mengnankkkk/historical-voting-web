@echo off
echo 正在设置环境变量...

:: 设置Node.js环境变量
set PATH=%PATH%;C:\Program Files\nodejs\
set NODE_PATH=%AppData%\npm\node_modules

:: 设置Java环境变量
set JAVA_HOME=%ProgramFiles%\Java\jdk1.8.0_202
set PATH=%PATH%;%JAVA_HOME%\bin

echo 环境变量设置完成！
echo.
echo 现在您可以运行start.bat来启动系统
pause 
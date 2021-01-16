@echo off
rem 切换到当前脚本所在目录
%~d0
cd "%~dp0"

echo ***********************************
echo *****欢迎使用JdbcLogsAnalyser*****
echo ***********************************
echo      正在设置JDK...
echo ***********************************

rem 指定JDK安装目录
set JDK_HOME=%~dp0JDK_1.7

echo      JDK设置完毕...
echo ***********************************

echo      正在设置指定路径...
echo ***********************************

rem 设置执行路径
set PATH=%JDK_HOME%\bin

echo      执行路径设置完毕...
echo ***********************************

echo      JdbcLogsAnalyser启动中...
echo ***********************************

java -jar JLA.jar
exit


rem 接收输入
rem --set destinationPath=:
rem --set /p destinationPath=请指定目标路径:

@echo off
rem �л�����ǰ�ű�����Ŀ¼
%~d0
cd "%~dp0"

echo ***********************************
echo *****��ӭʹ��JdbcLogsAnalyser*****
echo ***********************************
echo      ��������JDK...
echo ***********************************

rem ָ��JDK��װĿ¼
set JDK_HOME=%~dp0JDK_1.7

echo      JDK�������...
echo ***********************************

echo      ��������ָ��·��...
echo ***********************************

rem ����ִ��·��
set PATH=%JDK_HOME%\bin

echo      ִ��·���������...
echo ***********************************

echo      JdbcLogsAnalyser������...
echo ***********************************

java -jar JLA.jar
exit


rem ��������
rem --set destinationPath=:
rem --set /p destinationPath=��ָ��Ŀ��·��:

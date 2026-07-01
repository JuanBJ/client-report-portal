@echo off
setlocal

call "%~dp0mvnw.cmd" clean package -DskipTests
if errorlevel 1 exit /b %errorlevel%

java -jar "%~dp0target\client-report-portal-0.1.0.jar" %*

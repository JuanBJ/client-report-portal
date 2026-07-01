@echo off
setlocal

set "MAVEN_VERSION=3.9.9"
set "BASE_DIR=%~dp0"
set "MAVEN_HOME=%BASE_DIR%.mvn\apache-maven-%MAVEN_VERSION%"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
set "MAVEN_ZIP=%BASE_DIR%.mvn\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"

if exist "%MAVEN_CMD%" goto run_maven

echo Maven %MAVEN_VERSION% was not found locally. Downloading it now...
if not exist "%BASE_DIR%.mvn" mkdir "%BASE_DIR%.mvn"

powershell -NoProfile -ExecutionPolicy Bypass -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%' } catch { Write-Error $_; exit 1 }"
if errorlevel 1 exit /b %errorlevel%

powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%BASE_DIR%.mvn' -Force"
if errorlevel 1 exit /b %errorlevel%

del "%MAVEN_ZIP%" >nul 2>nul

:run_maven
call "%MAVEN_CMD%" %*
exit /b %errorlevel%

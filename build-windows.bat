@echo off
setlocal EnableExtensions
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo Java 17+ is required.
  echo Run run-windows.bat first so it can install Java automatically.
  pause
  exit /b 1
)

set "MAVEN_VERSION=3.9.16"
set "MAVEN_ROOT=%LOCALAPPDATA%\FortnitePE\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_ROOT%\bin\mvn.cmd"

where mvn >nul 2>nul
if not errorlevel 1 goto :maven_ready

if exist "%MAVEN_BIN%" (
  set "PATH=%MAVEN_ROOT%\bin;%PATH%"
  goto :maven_ready
)

echo.
echo Maven was not found. Downloading Apache Maven %MAVEN_VERSION%...
echo.

if not exist "%LOCALAPPDATA%\FortnitePE" mkdir "%LOCALAPPDATA%\FortnitePE"

set "MAVEN_ZIP=%LOCALAPPDATA%\FortnitePE\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://dlcdn.apache.org/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'"
if errorlevel 1 (
  echo.
  echo Failed to download Apache Maven.
  echo Check your internet connection and run this file again.
  pause
  exit /b 1
)

if exist "%MAVEN_ROOT%" rmdir /s /q "%MAVEN_ROOT%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%MAVEN_ZIP%' -DestinationPath '%LOCALAPPDATA%\FortnitePE' -Force"
if errorlevel 1 (
  echo.
  echo Failed to extract Apache Maven.
  pause
  exit /b 1
)

if not exist "%MAVEN_BIN%" (
  echo.
  echo Maven was downloaded but mvn.cmd could not be found.
  pause
  exit /b 1
)

set "PATH=%MAVEN_ROOT%\bin;%PATH%"

:maven_ready
echo.
echo Maven:
call "%MAVEN_BIN%" -version
if errorlevel 1 (
  echo.
  echo Maven could not start. Make sure Java 17+ is installed.
  pause
  exit /b 1
)

echo.
echo Building Fortnite PE...
call "%MAVEN_BIN%" -q clean package
if errorlevel 1 (
  echo.
  echo Build failed.
  pause
  exit /b 1
)

echo.
echo ========================================
echo Fortnite PE build complete!
echo ========================================
echo.
echo Runnable JAR:
echo target\fortnite-pe-1.0.0.jar
echo.
pause

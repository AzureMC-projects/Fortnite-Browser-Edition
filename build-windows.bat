@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo Java 17+ is required. Install a JDK and try again.
  pause
  exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
  if exist "%ProgramFiles%\Apache\Maven\bin\mvn.cmd" (
    set "PATH=%ProgramFiles%\Apache\Maven\bin;%PATH%"
  ) else (
    echo Maven is required to build Fortnite PE.
    echo Install Apache Maven, then run this file again.
    pause
    exit /b 1
  )
)

echo Building Fortnite PE...
call mvn -q clean package
if errorlevel 1 (
  echo Build failed.
  pause
  exit /b 1
)

echo.
echo Build complete.
echo Runnable JAR: target\fortnite-pe-1.0.0.jar
pause

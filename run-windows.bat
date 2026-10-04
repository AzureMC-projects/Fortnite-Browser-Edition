@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo Java 17+ is required.
  echo Install a JDK 17 or newer and try again.
  pause
  exit /b 1
)

if not exist "target\fortnite-pe-1.0.0.jar" (
  echo Fortnite PE has not been built yet.
  echo Running the Windows build first...
  call "%~dp0build-windows.bat"
  if errorlevel 1 exit /b 1
)

echo Starting Fortnite PE...
java -jar "target\fortnite-pe-1.0.0.jar"
if errorlevel 1 (
  echo.
  echo Fortnite PE closed with an error.
  pause
)

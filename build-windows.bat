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

where mvn >nul 2>nul
if errorlevel 1 (
  if exist "%ProgramFiles%\Apache\Maven\bin\mvn.cmd" (
    set "PATH=%ProgramFiles%\Apache\Maven\bin;%PATH%"
  ) else (
    echo.
    echo Maven was not found. Attempting automatic installation...
    echo.

    where winget >nul 2>nul
    if errorlevel 1 (
      echo Windows Package Manager ^(winget^) is not available.
      echo Please install Apache Maven manually, then run this file again.
      pause
      exit /b 1
    )

    winget install --id Apache.Maven --exact --accept-source-agreements --accept-package-agreements
    if errorlevel 1 (
      echo.
      echo Automatic Maven installation failed or was cancelled.
      pause
      exit /b 1
    )

    echo.
    echo Maven installation completed. Refreshing PATH...

    if exist "%ProgramFiles%\Apache\Maven\bin\mvn.cmd" (
      set "PATH=%ProgramFiles%\Apache\Maven\bin;%PATH%"
    ) else (
      where mvn >nul 2>nul
      if errorlevel 1 (
        echo Maven was installed, but Windows has not refreshed PATH yet.
        echo Please close this window, open a new Command Prompt, and run run-windows.bat again.
        pause
        exit /b 1
      )
    )
  )
)

echo.
echo Building Fortnite PE...
call mvn -q clean package
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

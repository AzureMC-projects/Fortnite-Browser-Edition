@echo off
setlocal EnableExtensions
cd /d "%~dp0"

set "JAVA_HOME=%JAVA_HOME%"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_found

for /d %%D in ("%ProgramFiles%\Java\jdk*" "%ProgramFiles%\Eclipse Adoptium\jdk*" "%ProgramFiles%\Microsoft\jdk*" "%LocalAppData%\Programs\Eclipse Adoptium\jdk*") do (
  if exist "%%~D\bin\java.exe" (
    set "JAVA_HOME=%%~D"
    goto :java_found
  )
)

where java >nul 2>nul
if not errorlevel 1 goto :java_found_path

echo.
echo Fortnite PE needs Java 17 or newer.
echo Java was not found on this PC.
echo.
echo Attempting automatic installation with Windows Package Manager...
echo.

where winget >nul 2>nul
if errorlevel 1 (
  echo Windows Package Manager ^(winget^) is not available.
  echo Please install a JDK 17+ manually, then run this launcher again.
  pause
  exit /b 1
)

winget install --id EclipseAdoptium.Temurin.17.JDK --exact --accept-source-agreements --accept-package-agreements
if errorlevel 1 (
  echo.
  echo Automatic Java installation failed or was cancelled.
  pause
  exit /b 1
)

echo.
echo Java installation completed. Refreshing Java detection...

for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk*" "%ProgramFiles%\Java\jdk*" "%ProgramFiles%\Microsoft\jdk*" "%LocalAppData%\Programs\Eclipse Adoptium\jdk*") do (
  if exist "%%~D\bin\java.exe" (
    set "JAVA_HOME=%%~D"
    goto :java_found
  )
)

where java >nul 2>nul
if errorlevel 1 (
  echo Java was installed, but Windows has not refreshed PATH yet.
  echo Please close this window, open a new Command Prompt, and run run-windows.bat again.
  pause
  exit /b 1
)

:java_found_path
set "JAVA_CMD=java"
goto :check_java

:java_found
set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"

:check_java
for /f "tokens=3" %%V in ('"%JAVA_CMD%" -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%~V"
echo Detected Java: %JAVA_VERSION%

if not exist "target\fortnite-pe-1.0.0.jar" (
  echo.
  echo Fortnite PE has not been built yet.
  echo Running the Windows build first...
  call "%~dp0build-windows.bat"
  if errorlevel 1 exit /b 1
)

echo.
echo Starting Fortnite PE...
"%JAVA_CMD%" -jar "target\fortnite-pe-1.0.0.jar"
if errorlevel 1 (
  echo.
  echo Fortnite PE closed with an error.
  pause
)

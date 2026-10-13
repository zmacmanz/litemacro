@echo off
setlocal

set GRADLE_VERSION=9.4.1
set TOOLS_DIR=%~dp0.gradle-tools
set GRADLE_DIR=%TOOLS_DIR%\gradle-%GRADLE_VERSION%
set GRADLE_BAT=%GRADLE_DIR%\bin\gradle.bat
set ZIP_PATH=%TOOLS_DIR%\gradle-%GRADLE_VERSION%-bin.zip

if not exist "%GRADLE_BAT%" (
    if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"
    if not exist "%ZIP_PATH%" (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP_PATH%'"
        if errorlevel 1 exit /b %errorlevel%
    )
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP_PATH%' -DestinationPath '%TOOLS_DIR%' -Force"
    if errorlevel 1 exit /b %errorlevel%
)

"%GRADLE_BAT%" %*
exit /b %errorlevel%

@echo off
echo Building WebcamManager with DEBUG MODE ENABLED...
echo.

call gradlew.bat clean build

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo DEBUG MODE IS ENABLED in this build!
    echo.
    echo To install:
    echo 1. Stop your server
    echo 2. Copy build\libs\WebcamManager-1.0.0.jar to plugins folder
    echo 3. Start server
    echo 4. Check logs for detailed packet analysis
    echo.
    echo The debug output will show:
    echo - Raw packet data in hex format
    echo - Attempt to read as integers
    echo - Exact byte structure
    echo.
) else (
    echo Build failed!
)

pause
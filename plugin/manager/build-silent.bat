@echo off
echo ========================================
echo Building SILENT WebcamManager Plugin
echo ========================================
echo.
echo This version:
echo - NO console spam
echo - NO error logging
echo - Silent operation
echo - Working webcam transmission
echo.

call gradlew.bat clean build -q

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo SILENT VERSION FEATURES:
    echo - All error logging removed
    echo - No console spam about dimensions
    echo - Silent error handling
    echo - Clean console output
    echo.
    echo Installation:
    echo 1. Stop your server
    echo 2. Copy build\libs\WebcamManager-1.0.0.jar to plugins folder
    echo 3. Start server
    echo 4. Enjoy spam-free webcam functionality!
    echo.
    echo Only shows:
    echo - Plugin enabled/disabled messages
    echo - Debug mode warnings (if enabled)
    echo.
) else (
    echo Build failed!
)

pause
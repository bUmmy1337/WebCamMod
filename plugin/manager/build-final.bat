@echo off
echo ========================================
echo Building WebcamManager Plugin (Final)
echo ========================================
echo.

call gradlew.bat clean build

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Plugin features:
    echo - Automatic config.yml creation
    echo - Debug mode (enabled by default)
    echo - Configurable settings
    echo - Admin commands
    echo.
    echo Installation:
    echo 1. Stop your server
    echo 2. Copy build\libs\WebcamManager-1.0.0.jar to plugins folder
    echo 3. Start server
    echo 4. Plugin will create plugins/WebcamManager/config.yml
    echo.
    echo Commands:
    echo /webcam status  - Show plugin status
    echo /webcam reload  - Reload configuration
    echo /webcam debug on/off - Toggle debug mode
    echo.
    echo Config file will be created at:
    echo plugins/WebcamManager/config.yml
    echo.
) else (
    echo Build failed!
)

pause
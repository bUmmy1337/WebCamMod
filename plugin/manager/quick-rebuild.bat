@echo off
echo Quick rebuild and deploy...

REM Build the plugin
call gradlew.bat clean build -q

if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build successful!
echo.
echo To deploy:
echo 1. Stop your server
echo 2. Copy build\libs\WebcamManager-1.0.0.jar to your server's plugins folder
echo 3. Start your server
echo.
echo To enable debug mode, add this to your server startup:
echo -Dwebcam.debug=true
echo.
echo Or set 'debug: true' in plugins/WebcamManager/config.yml

pause
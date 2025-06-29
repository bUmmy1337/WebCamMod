@echo off
echo Rebuilding WebcamManager Plugin...
echo.

REM Check if Gradle Wrapper JAR exists, download if not
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Gradle Wrapper JAR not found, downloading...
    call download-gradle-wrapper.bat
)

REM Clean and rebuild
call gradlew.bat clean build --info

if %errorlevel% equ 0 (
    echo.
    echo Rebuild successful!
    echo Plugin JAR is located in: build\libs\WebcamManager-1.0.0.jar
    echo.
    echo Copy this JAR to your server's plugins folder and restart the server.
) else (
    echo.
    echo Rebuild failed! Check the error messages above.
)

pause
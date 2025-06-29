@echo off
echo Building WebcamManager Plugin with Gradle...
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 21 or higher
    pause
    exit /b 1
)

REM Check if Gradle Wrapper JAR exists, download if not
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Gradle Wrapper JAR not found, downloading...
    call download-gradle-wrapper.bat
    if not exist "gradle\wrapper\gradle-wrapper.jar" (
        echo Failed to download Gradle Wrapper JAR
        pause
        exit /b 1
    )
)

REM Use Gradle Wrapper to build
echo Using Gradle Wrapper to build the plugin...
call gradlew.bat clean build

if %errorlevel% equ 0 (
    echo.
    echo Build successful!
    echo Plugin JAR is located in: build\libs\WebcamManager-1.0.0.jar
    echo.
    echo You can now copy this JAR to your server's plugins folder.
) else (
    echo.
    echo Build failed! Check the error messages above.
)

pause
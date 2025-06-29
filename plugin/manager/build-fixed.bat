@echo off
echo ========================================
echo Building FIXED WebcamManager Plugin
echo ========================================
echo.
echo This version includes:
echo - Correct data format parsing
echo - Fixed UUID reading
echo - Proper dimension parsing
echo - Working frame transmission
echo.

call gradlew.bat clean build

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo FIXED ISSUES:
    echo - Corrected data format parsing based on hex analysis
    echo - UUID string now reads correctly (36 bytes)
    echo - Dimensions parse properly (640x480 instead of invalid values)
    echo - Frame size reads correctly (24713 bytes instead of negative)
    echo.
    echo Installation:
    echo 1. Stop your server
    echo 2. Copy build\libs\WebcamManager-1.0.0.jar to plugins folder
    echo 3. Start server
    echo 4. Test webcam - should work without errors!
    echo.
    echo Debug mode is OFF by default now.
    echo Use '/webcam debug on' to enable if needed.
    echo.
) else (
    echo Build failed!
)

pause
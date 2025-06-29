@echo off
echo Compiling WebcamManager Plugin (without Maven)...
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 21 or higher
    pause
    exit /b 1
)

REM Create directories
if not exist "build\classes" mkdir "build\classes"
if not exist "build\jar" mkdir "build\jar"

REM Download Paper API if not exists
if not exist "lib\paper-api-1.21.4.jar" (
    echo Downloading Paper API...
    if not exist "lib" mkdir "lib"
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/1.21.4-R0.1-SNAPSHOT/paper-api-1.21.4-20241203.024919-14.jar' -OutFile 'lib\paper-api-1.21.4.jar'"
)

REM Compile Java files
echo Compiling Java sources...
javac -cp "lib\paper-api-1.21.4.jar" -d "build\classes" src\main\java\com\bummy\webcam\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Copy resources
echo Copying resources...
xcopy "src\main\resources\*" "build\classes\" /E /Y

REM Create JAR
echo Creating JAR file...
cd build\classes
jar cf "..\jar\WebcamManager-1.0.0.jar" *
cd ..\..

if exist "build\jar\WebcamManager-1.0.0.jar" (
    echo.
    echo Build successful!
    echo Plugin JAR is located in: build\jar\WebcamManager-1.0.0.jar
    echo.
    echo You can now copy this JAR to your server's plugins folder.
) else (
    echo JAR creation failed!
)

pause
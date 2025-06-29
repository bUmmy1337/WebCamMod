@echo off
echo Downloading Gradle Wrapper JAR...

if not exist "gradle\wrapper" mkdir "gradle\wrapper"

powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Gradle Wrapper JAR downloaded successfully!
    echo You can now run: gradlew.bat clean build
) else (
    echo Failed to download Gradle Wrapper JAR
    echo Trying alternative download...
    powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.5-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"
    
    if exist "gradle\wrapper\gradle-wrapper.jar" (
        echo Gradle Wrapper JAR downloaded successfully!
    ) else (
        echo Failed to download Gradle Wrapper JAR. Please check your internet connection.
    )
)

pause
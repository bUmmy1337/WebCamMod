# WebcamManager Plugin

Server-side plugin for the Webcam Mod that handles video frame broadcasting between players.

## Features

- Receives webcam video frames from players with the Webcam Mod
- Broadcasts frames to nearby players (within 100 blocks)
- Supports cross-world filtering
- Optimized for performance with distance-based filtering

## Requirements

- Minecraft Server 1.21.4+
- Paper/Spigot/Bukkit
- Players must have the Webcam Mod installed on client-side

## Installation

1. Download the latest WebcamManager.jar
2. Place it in your server's `plugins/` folder
3. Restart the server
4. The plugin will automatically handle webcam frame broadcasting

## Configuration

No configuration required - the plugin works out of the box.

## Technical Details

- **Channel**: `webcam:video_frame`
- **Max Distance**: 100 blocks
- **World Filtering**: Enabled (players only see webcams from the same world)

## Compatibility

- Minecraft 1.21.4
- Paper API
- Compatible with Fabric Webcam Mod

## Building

### Option 1: Using Gradle Wrapper (Recommended)
```cmd
gradlew.bat clean build
```
or simply run:
```cmd
build.bat
```

### Option 2: Using installed Gradle
```bash
gradle clean build
```

### Option 3: Using Maven Wrapper (Alternative)
```cmd
mvnw.cmd clean package
```

### Option 4: Manual Compilation (if build tools are not available)
```cmd
compile.bat
```

The compiled JAR will be in:
- Gradle build: `build/libs/WebcamManager-1.0.0.jar`
- Maven build: `target/WebcamManager-1.0.0.jar`
- Manual build: `build/jar/WebcamManager-1.0.0.jar`

## Requirements for Building

- Java 21 or higher
- Internet connection (for downloading dependencies)
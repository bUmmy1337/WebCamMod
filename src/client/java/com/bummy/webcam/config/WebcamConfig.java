package com.bummy.webcam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebcamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("webcam-mod.json");
    
    private static WebcamConfig instance;
    
    // Position settings
    public float offsetX = 0.0f;
    public float offsetY = 1.5f; // Above head
    public float offsetZ = 0.0f;
    
    // Size settings
    public float scale = 0.8f; // Larger than before
    
    // Shape settings
    public boolean circular = true;
    public int circleSegments = 32; // For smooth circle rendering
    
    // Visibility settings
    public float opacity = 1.0f;

    // Zoom and Stretch settings
    public float zoom = 1.0f;
    public float stretch = 1.0f;
    public float panX = 0.0f;
    public float panY = 0.0f;

    public boolean rotateWithPlayer = true;
    
    // First run flag
    public boolean isFirstRun = true;

    public static WebcamConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }
    
    public static WebcamConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, WebcamConfig.class);
            }
        } catch (IOException e) {
            System.err.println("Failed to load webcam config: " + e.getMessage());
        }
        
        // Return default config if loading fails
        WebcamConfig config = new WebcamConfig();
        config.save();
        return config;
    }
    
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Failed to save webcam config: " + e.getMessage());
        }
    }
    
    // Getters and setters for easy access
    public float getOffsetX() { return offsetX; }
    public void setOffsetX(float offsetX) { this.offsetX = offsetX; save(); }
    
    public float getOffsetY() { return offsetY; }
    public void setOffsetY(float offsetY) { this.offsetY = offsetY; save(); }
    
    public float getOffsetZ() { return offsetZ; }
    public void setOffsetZ(float offsetZ) { this.offsetZ = offsetZ; save(); }
    
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; save(); }
    
    public boolean isCircular() { return circular; }
    public void setCircular(boolean circular) { this.circular = circular; save(); }
    
    public int getCircleSegments() { return circleSegments; }
    public void setCircleSegments(int segments) { this.circleSegments = segments; save(); }
    
    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = opacity; save(); }

    // Getters and setters for zoom and stretch
    public float getZoom() { return zoom; }
    public void setZoom(float zoom) { this.zoom = zoom; save(); }

    public float getStretch() { return stretch; }
    public void setStretch(float stretch) { this.stretch = stretch; save(); }

    public float getPanX() { return panX; }
    public void setPanX(float panX) { this.panX = panX; save(); }

    public float getPanY() { return panY; }
    public void setPanY(float panY) { this.panY = panY; save(); }

    public boolean shouldRotateWithPlayer() { return rotateWithPlayer; }
    public void setRotateWithPlayer(boolean rotate) { this.rotateWithPlayer = rotate; save(); }
    
    public boolean isFirstRun() { return isFirstRun; }
    public void setFirstRun(boolean firstRun) { this.isFirstRun = firstRun; save(); }
}
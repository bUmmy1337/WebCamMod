package com.bummy.webcam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PreviewConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("webcam-mod-preview.json");

    private static PreviewConfig instance;

    private boolean enabled = true;
    private float x = 0.8f;
    private float y = 0.05f;
    private float size = 0.15f;

    public static PreviewConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static PreviewConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, PreviewConfig.class);
            }
        } catch (IOException e) {
            System.err.println("Failed to load webcam preview config: " + e.getMessage());
        }

        PreviewConfig config = new PreviewConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Failed to save webcam preview config: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        save();
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        save();
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
        save();
    }
}

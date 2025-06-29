package com.bummy.webcam;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class WebcamManager extends JavaPlugin {

    private static WebcamManager instance;

    public static final String CHANNEL = "webcam:video_frame";
    private final DebugWebcamListener debugListener = new DebugWebcamListener();
    private final CorrectWebcamListener correctListener = new CorrectWebcamListener();
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        instance = this;

        // Create plugin folder and config
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Load config and check debug mode
        boolean configDebug = getConfig().getBoolean("debug", false);
        debugMode = configDebug || Boolean.getBoolean("webcam.debug");

        getLogger().info("WebcamManager enabled!");
        
        if (debugMode) {
            getLogger().warning("DEBUG MODE ENABLED");
            Bukkit.getMessenger().registerIncomingPluginChannel(instance, CHANNEL, debugListener);
        } else {
            Bukkit.getMessenger().registerIncomingPluginChannel(instance, CHANNEL, correctListener);
        }
        
        Bukkit.getMessenger().registerOutgoingPluginChannel(instance, CHANNEL);
        
        // Register command
        WebcamCommand commandExecutor = new WebcamCommand();
        getCommand("webcam").setExecutor(commandExecutor);
        getCommand("webcam").setTabCompleter(commandExecutor);
    }

    @Override
    public void onDisable() {
        getLogger().info("WebcamManager disabled!");
        
        if (debugMode) {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(instance, CHANNEL, debugListener);
        } else {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(instance, CHANNEL, correctListener);
        }
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(instance, CHANNEL);
    }

    public static WebcamManager getInstance() {
        return instance;
    }
    
    // Getter methods for config values
    public int getMaxDistance() {
        return getConfig().getInt("max-distance", 100);
    }
    
    public int getMaxFrameSize() {
        return getConfig().getInt("max-frame-size", 10000000);
    }
    
    public int getLogInterval() {
        return getConfig().getInt("log-interval", 5000);
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
}
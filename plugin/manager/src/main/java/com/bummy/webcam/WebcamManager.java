package com.bummy.webcam;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class WebcamManager extends JavaPlugin {

    private static WebcamManager instance;

    public static final String CHANNEL = "webcam:video_frame";
    private final WebcamListener webcamListener = new WebcamListener();

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("WebcamManager plugin enabled!");
        getLogger().info("Channel: " + CHANNEL);

        Bukkit.getMessenger().registerIncomingPluginChannel(instance, CHANNEL, webcamListener);
        Bukkit.getMessenger().registerOutgoingPluginChannel(instance, CHANNEL);
    }

    @Override
    public void onDisable() {
        getLogger().info("WebcamManager plugin disabled!");
        
        Bukkit.getMessenger().unregisterIncomingPluginChannel(instance, CHANNEL, webcamListener);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(instance, CHANNEL);
    }

    public static WebcamManager getInstance() {
        return instance;
    }
}
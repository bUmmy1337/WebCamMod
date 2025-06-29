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

        Bukkit.getMessenger().registerIncomingPluginChannel(instance, webcamListener.getChannel(), webcamListener);
        Bukkit.getMessenger().registerOutgoingPluginChannel(instance, webcamListener.getChannel());
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(instance, CHANNEL, webcamListener);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(instance, CHANNEL);
    }

    public static WebcamManager getInstance() {
        return instance;
    }

}
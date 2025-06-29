package com.bummy.webcam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.UUID;

public class WebcamListener implements PluginMessageListener {

    private final String CHANNEL = "webcam:video_frame";
    private long lastLogTime = 0;
    private int messageCount = 0;

    @Override
    public void onPluginMessageReceived(String channel, Player sender, byte[] message) {
        if (!channel.equals(CHANNEL)) {
            WebcamManager.getInstance().getLogger().warning(
                String.format("Received message on wrong channel: %s (expected: %s)", channel, CHANNEL)
            );
            return;
        }

        messageCount++;
        long currentTime = System.currentTimeMillis();
        
        // Log only every X seconds to reduce spam (configurable)
        if (currentTime - lastLogTime > WebcamManager.getInstance().getLogInterval()) {
            WebcamManager.getInstance().getLogger().info(
                String.format("Processing webcam messages from %s (received %d messages in last %ds, current size: %d bytes)", 
                    sender.getName(), messageCount, WebcamManager.getInstance().getLogInterval() / 1000, message.length)
            );
            lastLogTime = currentTime;
            messageCount = 0;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            // Read string size with safety check
            int stringSize = in.readInt();
            
            if (stringSize < 0 || stringSize > 1000) {
                WebcamManager.getInstance().getLogger().warning(
                    String.format("Invalid string size from %s: %d (message size: %d)", 
                        sender.getName(), stringSize, message.length)
                );
                return;
            }
            
            // Read UUID string
            byte[] uuidBytes = new byte[stringSize];
            in.readFully(uuidBytes);
            String playerUuidString = new String(uuidBytes);
            
            // Read dimensions
            int width = in.readInt();
            int height = in.readInt();
            
            if (width < 0 || height < 0 || width > 10000 || height > 10000) {
                WebcamManager.getInstance().getLogger().warning(
                    String.format("Invalid dimensions from %s: %dx%d", sender.getName(), width, height)
                );
                return;
            }
            
            // Read frame data
            int frameLength = in.readInt();
            
            if (frameLength < 0 || frameLength > WebcamManager.getInstance().getMaxFrameSize()) {
                WebcamManager.getInstance().getLogger().warning(
                    String.format("Invalid frame length from %s: %d (max: %d)", 
                        sender.getName(), frameLength, WebcamManager.getInstance().getMaxFrameSize())
                );
                return;
            }
            
            byte[] frame = new byte[frameLength];
            in.readFully(frame);

            // Broadcast to nearby players
            int playersReached = 0;
            int maxDistance = WebcamManager.getInstance().getMaxDistance();
            
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(sender)) continue;
                if (!target.getWorld().equals(sender.getWorld())) continue;
                
                // Check distance (configurable)
                double distance = target.getLocation().distance(sender.getLocation());
                if (distance > maxDistance) continue;

                try {
                    // Send frame to target player using the same format
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(byteOut);

                    out.writeInt(playerUuidString.length());
                    out.writeBytes(playerUuidString);
                    out.writeInt(width);
                    out.writeInt(height);
                    out.writeInt(frame.length);
                    out.write(frame);

                    target.sendPluginMessage(WebcamManager.getInstance(), CHANNEL, byteOut.toByteArray());
                    playersReached++;
                } catch (IOException e) {
                    WebcamManager.getInstance().getLogger().warning(
                        String.format("Failed to send webcam data to %s: %s", target.getName(), e.getMessage())
                    );
                }
            }

        } catch (IOException e) {
            WebcamManager.getInstance().getLogger().warning(
                String.format("Failed to parse webcam payload from %s (size: %d): %s", 
                    sender.getName(), message.length, e.getMessage())
            );
            
            // Print detailed error info only occasionally
            if (System.currentTimeMillis() % 10000 < 100) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            WebcamManager.getInstance().getLogger().severe(
                String.format("Unexpected error processing webcam data from %s: %s", 
                    sender.getName(), e.getMessage())
            );
            e.printStackTrace();
        }
    }

    public String getChannel() {
        return CHANNEL;
    }
}
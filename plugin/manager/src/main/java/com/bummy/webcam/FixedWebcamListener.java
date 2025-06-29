package com.bummy.webcam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class FixedWebcamListener implements PluginMessageListener {

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
            // Based on debug output, the format is:
            // 1. int stringLength (36)
            // 2. String UUID (but with writeString format)
            // 3. int width
            // 4. int height  
            // 5. int frameLength
            // 6. byte[] frame
            
            // Read string length
            int stringLength = in.readInt();
            WebcamManager.getInstance().getLogger().info("String length: " + stringLength);
            
            if (stringLength < 0 || stringLength > 1000) {
                WebcamManager.getInstance().getLogger().warning(
                    String.format("Invalid string length from %s: %d", sender.getName(), stringLength)
                );
                return;
            }
            
            // Read UUID string using readUTF() format
            String playerUuidString = in.readUTF();
            WebcamManager.getInstance().getLogger().info("UUID: " + playerUuidString);
            
            // Read dimensions
            int width = in.readInt();
            int height = in.readInt();
            WebcamManager.getInstance().getLogger().info("Dimensions: " + width + "x" + height);
            
            if (width < 0 || height < 0 || width > 10000 || height > 10000) {
                WebcamManager.getInstance().getLogger().warning(
                    String.format("Invalid dimensions from %s: %dx%d", sender.getName(), width, height)
                );
                return;
            }
            
            // Read frame data
            int frameLength = in.readInt();
            WebcamManager.getInstance().getLogger().info("Frame length: " + frameLength);
            
            if (frameLength < 0 || frameLength > WebcamManager.getInstance().getMaxFrameSize()) {
                WebcamManager.getInstance().getLogger().warning(
                    String.format("Invalid frame length from %s: %d (max: %d)", 
                        sender.getName(), frameLength, WebcamManager.getInstance().getMaxFrameSize())
                );
                return;
            }
            
            byte[] frame = new byte[frameLength];
            in.readFully(frame);
            
            WebcamManager.getInstance().getLogger().info(
                String.format("Successfully parsed frame from %s: %dx%d, %d bytes", 
                    sender.getName(), width, height, frameLength)
            );

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
                    out.writeUTF(playerUuidString);
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
            
            if (playersReached > 0) {
                WebcamManager.getInstance().getLogger().info(
                    String.format("Broadcasted frame to %d players", playersReached)
                );
            }

        } catch (IOException e) {
            WebcamManager.getInstance().getLogger().warning(
                String.format("Failed to parse webcam payload from %s (size: %d): %s", 
                    sender.getName(), message.length, e.getMessage())
            );
            e.printStackTrace();
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
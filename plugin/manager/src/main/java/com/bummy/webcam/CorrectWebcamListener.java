package com.bummy.webcam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class CorrectWebcamListener implements PluginMessageListener {

    private final String CHANNEL = "webcam:video_frame";

    @Override
    public void onPluginMessageReceived(String channel, Player sender, byte[] message) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            // Read string length
            int stringLength = in.readInt();
            
            if (stringLength < 0 || stringLength > 1000) {
                return; // Silently ignore invalid data
            }
            
            // Read UUID string as raw bytes
            byte[] uuidBytes = new byte[stringLength];
            in.readFully(uuidBytes);
            String playerUuidString = new String(uuidBytes);
            
            // Read dimensions
            int width = in.readInt();
            int height = in.readInt();
            
            if (width < 0 || height < 0 || width > 10000 || height > 10000) {
                return; // Silently ignore invalid dimensions
            }
            
            // Read frame data
            int frameLength = in.readInt();
            
            if (frameLength < 0 || frameLength > WebcamManager.getInstance().getMaxFrameSize()) {
                return; // Silently ignore invalid frame size
            }
            
            byte[] frame = new byte[frameLength];
            in.readFully(frame);

            // Broadcast to nearby players
            int maxDistance = WebcamManager.getInstance().getMaxDistance();
            
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(sender)) continue;
                if (!target.getWorld().equals(sender.getWorld())) continue;
                
                // Check distance
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
                } catch (IOException e) {
                    // Silently ignore send errors
                }
            }

        } catch (IOException e) {
            // Silently ignore parsing errors
        } catch (Exception e) {
            // Silently ignore all other errors
        }
    }

    public String getChannel() {
        return CHANNEL;
    }
}
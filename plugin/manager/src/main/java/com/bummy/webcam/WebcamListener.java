package com.bummy.webcam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.UUID;

public class WebcamListener implements PluginMessageListener {

    private final String CHANNEL = "webcam:video_frame";

    @Override
    public void onPluginMessageReceived(String channel, Player sender, byte[] message) {
        if (!channel.equals(CHANNEL)) return;

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            // Read video frame data
            UUID playerUuid = new UUID(in.readLong(), in.readLong());
            int width = in.readInt();
            int height = in.readInt();
            int frameLength = in.readInt();
            byte[] frame = new byte[frameLength];
            in.readFully(frame);
            boolean isVisible = in.readBoolean();

            WebcamManager.getInstance().getLogger().fine(
                String.format("Received webcam frame from %s: %dx%d, %d bytes, visible: %s", 
                    sender.getName(), width, height, frameLength, isVisible)
            );

            // Broadcast to nearby players
            int playersReached = 0;
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(sender)) continue;
                if (!target.getWorld().equals(sender.getWorld())) continue;
                
                // Check distance (100 blocks max)
                double distance = target.getLocation().distance(sender.getLocation());
                if (distance > 100) continue;

                // Send frame to target player
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(byteOut);

                out.writeLong(playerUuid.getMostSignificantBits());
                out.writeLong(playerUuid.getLeastSignificantBits());
                out.writeInt(width);
                out.writeInt(height);
                out.writeInt(frame.length);
                out.write(frame);
                out.writeBoolean(isVisible);

                target.sendPluginMessage(WebcamManager.getInstance(), CHANNEL, byteOut.toByteArray());
                playersReached++;
            }

            WebcamManager.getInstance().getLogger().fine(
                String.format("Broadcasted webcam frame from %s to %d players", 
                    sender.getName(), playersReached)
            );

        } catch (IOException e) {
            WebcamManager.getInstance().getLogger().warning(
                "Failed to parse incoming webcam payload from " + sender.getName() + ": " + e.getMessage()
            );
        }
    }

    public String getChannel() {
        return CHANNEL;
    }
}
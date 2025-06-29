package com.bummy.webcam;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class DebugWebcamListener implements PluginMessageListener {

    private final String CHANNEL = "webcam:video_frame";

    @Override
    public void onPluginMessageReceived(String channel, Player sender, byte[] message) {
        if (!channel.equals(CHANNEL)) return;

        WebcamManager.getInstance().getLogger().info(
            String.format("=== DEBUG: Message from %s, size: %d bytes ===", sender.getName(), message.length)
        );

        // Print first 100 bytes as hex for debugging
        StringBuilder hex = new StringBuilder();
        StringBuilder ascii = new StringBuilder();
        
        int limit = Math.min(100, message.length);
        for (int i = 0; i < limit; i++) {
            byte b = message[i];
            hex.append(String.format("%02X ", b));
            ascii.append((b >= 32 && b <= 126) ? (char) b : '.');
            
            if ((i + 1) % 16 == 0) {
                WebcamManager.getInstance().getLogger().info(
                    String.format("Offset %04X: %s | %s", i - 15, hex.toString(), ascii.toString())
                );
                hex.setLength(0);
                ascii.setLength(0);
            }
        }
        
        if (hex.length() > 0) {
            WebcamManager.getInstance().getLogger().info(
                String.format("Offset %04X: %-48s | %s", limit - (limit % 16), hex.toString(), ascii.toString())
            );
        }

        // Try to read as integers
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            WebcamManager.getInstance().getLogger().info("=== READING AS INTEGERS ===");
            for (int i = 0; i < Math.min(15, message.length / 4); i++) {
                if (in.available() >= 4) {
                    int value = in.readInt();
                    WebcamManager.getInstance().getLogger().info(String.format("Int[%d]: %d (0x%08X)", i, value, value));
                }
            }
        } catch (IOException e) {
            WebcamManager.getInstance().getLogger().info("Failed to read as integers: " + e.getMessage());
        }

        // Try to analyze the expected format from client mod
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            WebcamManager.getInstance().getLogger().info("=== ANALYZING CLIENT MOD FORMAT ===");
            
            // Try reading string size first
            if (in.available() >= 4) {
                int stringSize = in.readInt();
                WebcamManager.getInstance().getLogger().info("Potential string size: " + stringSize + " (0x" + Integer.toHexString(stringSize) + ")");
                
                if (stringSize > 0 && stringSize < 1000 && in.available() >= stringSize) {
                    byte[] stringBytes = new byte[stringSize];
                    in.readFully(stringBytes);
                    String potentialUUID = new String(stringBytes);
                    WebcamManager.getInstance().getLogger().info("Potential UUID string: '" + potentialUUID + "'");
                    
                    // Try reading dimensions
                    if (in.available() >= 8) {
                        int width = in.readInt();
                        int height = in.readInt();
                        WebcamManager.getInstance().getLogger().info("Potential dimensions: " + width + "x" + height);
                        
                        if (in.available() >= 4) {
                            int frameSize = in.readInt();
                            WebcamManager.getInstance().getLogger().info("Potential frame size: " + frameSize + " bytes");
                            WebcamManager.getInstance().getLogger().info("Remaining bytes: " + in.available());
                            
                            if (frameSize > 0 && frameSize <= in.available()) {
                                WebcamManager.getInstance().getLogger().info("Frame size looks valid!");
                            } else {
                                WebcamManager.getInstance().getLogger().warning("Frame size looks invalid!");
                            }
                        }
                    }
                } else {
                    WebcamManager.getInstance().getLogger().warning("String size looks invalid or not enough data");
                }
            }
        } catch (IOException e) {
            WebcamManager.getInstance().getLogger().info("Analysis failed: " + e.getMessage());
        }

        // Try alternative format - maybe it's using writeString() instead of writeBytes()
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            WebcamManager.getInstance().getLogger().info("=== TRYING ALTERNATIVE FORMAT (writeString) ===");
            
            if (in.available() >= 2) {
                // Java's writeString writes length as short (2 bytes) then UTF-8 bytes
                short stringLength = in.readShort();
                WebcamManager.getInstance().getLogger().info("String length (as short): " + stringLength);
                
                if (stringLength > 0 && stringLength < 1000 && in.available() >= stringLength) {
                    byte[] stringBytes = new byte[stringLength];
                    in.readFully(stringBytes);
                    String uuid = new String(stringBytes, "UTF-8");
                    WebcamManager.getInstance().getLogger().info("UUID (UTF-8): '" + uuid + "'");
                    
                    if (in.available() >= 12) {
                        int width = in.readInt();
                        int height = in.readInt();
                        int frameSize = in.readInt();
                        WebcamManager.getInstance().getLogger().info("Alt format - Dimensions: " + width + "x" + height + ", frame: " + frameSize);
                    }
                }
            }
        } catch (IOException e) {
            WebcamManager.getInstance().getLogger().info("Alternative format analysis failed: " + e.getMessage());
        }

        WebcamManager.getInstance().getLogger().info("=== END DEBUG ===");
    }

    public String getChannel() {
        return CHANNEL;
    }
}
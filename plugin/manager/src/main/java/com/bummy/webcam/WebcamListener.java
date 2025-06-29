package com.bummy.webcam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class WebcamListener implements PluginMessageListener {

    private final String CHANNEL = "webcam:video_frame";
    private static final int MAX_PACKET_SIZE = 32000; // 32KB absolute maximum

    @Override
    public void onPluginMessageReceived(String channel, Player sender, byte[] message) {
        if (!channel.equals(CHANNEL)) return;

        // Check packet size before processing
        if (message.length > MAX_PACKET_SIZE) {
            return; // Drop oversized packets
        }

        try {
            MinecraftPacketReader reader = new MinecraftPacketReader(message);
            
            int uuidLengthPrefix = reader.readInt();
            String playerUUID = reader.readString();
            int width = reader.readInt();
            int height = reader.readInt();
            int frameLength = reader.readInt();
            byte[] frame = reader.readBytes(frameLength);

            // Validate frame size
            if (frameLength > 30000) { // 30KB frame limit
                return;
            }

            // Create response packet
            MinecraftPacketWriter writer = new MinecraftPacketWriter();
            writer.writeInt(uuidLengthPrefix);
            writer.writeString(playerUUID);
            writer.writeInt(width);
            writer.writeInt(height);
            writer.writeInt(frameLength);
            writer.writeBytes(frame);
            
            byte[] responseData = writer.toByteArray();

            // Final size check
            if (responseData.length > MAX_PACKET_SIZE) {
                return;
            }

            // Send to nearby players
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(sender)) continue;
                if (!target.getWorld().equals(sender.getWorld())) continue;
                if (target.getLocation().distance(sender.getLocation()) > 100) continue;

                target.sendPluginMessage(WebcamManager.getInstance(), CHANNEL, responseData);
            }

        } catch (Exception e) {
            // Silently ignore malformed packets
        }
    }

    public String getChannel() {
        return CHANNEL;
    }

    // Custom packet reader that matches Minecraft's PacketByteBuf behavior
    private static class MinecraftPacketReader {
        private final DataInputStream stream;

        public MinecraftPacketReader(byte[] data) {
            this.stream = new DataInputStream(new ByteArrayInputStream(data));
        }

        public int readInt() throws IOException {
            return stream.readInt();
        }

        public String readString() throws IOException {
            int length = readVarInt();
            if (length > 32767) throw new IOException("String too long");
            byte[] bytes = new byte[length];
            stream.readFully(bytes);
            return new String(bytes, "UTF-8");
        }

        public byte[] readBytes(int length) throws IOException {
            byte[] bytes = new byte[length];
            stream.readFully(bytes);
            return bytes;
        }

        private int readVarInt() throws IOException {
            int value = 0;
            int position = 0;
            byte currentByte;

            while (true) {
                currentByte = stream.readByte();
                value |= (currentByte & 0x7F) << position;

                if ((currentByte & 0x80) == 0) break;

                position += 7;
                if (position >= 32) throw new RuntimeException("VarInt too big");
            }

            return value;
        }
    }

    // Custom packet writer that matches Minecraft's PacketByteBuf behavior
    private static class MinecraftPacketWriter {
        private final ByteArrayOutputStream byteStream;
        private final DataOutputStream stream;

        public MinecraftPacketWriter() {
            this.byteStream = new ByteArrayOutputStream();
            this.stream = new DataOutputStream(byteStream);
        }

        public void writeInt(int value) throws IOException {
            stream.writeInt(value);
        }

        public void writeString(String value) throws IOException {
            byte[] bytes = value.getBytes("UTF-8");
            writeVarInt(bytes.length);
            stream.write(bytes);
        }

        public void writeBytes(byte[] bytes) throws IOException {
            stream.write(bytes);
        }

        public byte[] toByteArray() {
            return byteStream.toByteArray();
        }

        private void writeVarInt(int value) throws IOException {
            while (true) {
                if ((value & 0xFFFFFF80) == 0) {
                    stream.writeByte(value);
                    return;
                }

                stream.writeByte(value & 0x7F | 0x80);
                value >>>= 7;
            }
        }
    }
}
package com.bummy.webcam.Video;

import com.bummy.webcam.WebcamMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class PlayerVideoPacketCodec implements PacketCodec<PacketByteBuf, PlayerVideo> {
    public static final PlayerVideoPacketCodec PACKET_CODEC;

    @Override
    public PlayerVideo decode(PacketByteBuf buf) {
        try {
            int stringSize = buf.readInt();
            String playerUUID = buf.readString(stringSize);
            int width = buf.readInt();
            int height = buf.readInt();
            int frameBytes = buf.readInt();
            byte[] frame = new byte[frameBytes];
            for (int i = 0; i < frameBytes; i++) {
                frame[i] = buf.readByte();
            }

            PlayerVideo playerVideo = new PlayerVideo(width, height, playerUUID);
            playerVideo.setFrame(frame);
            return playerVideo;
        } catch (Exception e) {
            WebcamMod.LOGGER.error("ERROR DECODING", e);
            throw e;
        }
    }

    @Override
    public void encode(PacketByteBuf buf, PlayerVideo value) {
        buf.writeInt(value.playerUUID.length());
        buf.writeString(value.playerUUID);

        buf.writeInt(value.width);
        buf.writeInt(value.height);
        buf.writeInt(value.frame.length);
        buf.writeBytes(value.frame);
    }

    static {
        PACKET_CODEC = new PlayerVideoPacketCodec();
    }

}

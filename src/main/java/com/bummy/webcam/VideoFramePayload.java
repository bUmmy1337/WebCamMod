package com.bummy.webcam;

import com.bummy.webcam.Video.PlayerVideo;
import com.bummy.webcam.Video.PlayerVideoPacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static com.bummy.webcam.WebcamMod.MOD_ID;

public record VideoFramePayload(PlayerVideo video) implements CustomPayload {
    public static final Identifier VIDEO_FRAME_PAYLOAD_ID = Identifier.of(MOD_ID, "video_frame");
    public static final CustomPayload.Id<VideoFramePayload> ID = new CustomPayload.Id<>(VIDEO_FRAME_PAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, VideoFramePayload> CODEC = PacketCodec.tuple(PlayerVideoPacketCodec.PACKET_CODEC, VideoFramePayload::video, VideoFramePayload::new);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

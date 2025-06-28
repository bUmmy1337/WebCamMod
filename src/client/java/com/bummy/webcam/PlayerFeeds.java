package com.bummy.webcam;

import com.bummy.webcam.Video.PlayerVideo;
import com.bummy.webcam.render.image.RenderableImage;

import java.util.HashMap;

public class PlayerFeeds {
    public static HashMap<String, PlayerVideo> videos = new HashMap<>();
    public static HashMap<String, RenderableImage> images = new HashMap<>();

    public static RenderableImage get(String uuid) {
        return images.get(uuid);
    }

    public static PlayerVideo getPlayerVideo(String uuid) {
        return videos.get(uuid);
    }

    public static void update(PlayerVideo video) {
        videos.put(video.playerUUID, video);
        RenderableImage image = images.getOrDefault(video.playerUUID, new RenderableImage());
        image.fill(video);
        images.put(video.playerUUID, image);
    }
}

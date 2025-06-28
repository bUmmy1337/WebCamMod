package com.bummy.webcam.screen;

import com.bummy.webcam.PlayerFeeds;
import com.bummy.webcam.config.PreviewConfig;
import com.bummy.webcam.render.image.RenderableImage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class PreviewOverlay {
    public void render(DrawContext context) {
        // Preview functionality temporarily disabled due to shader compatibility issues
        // The main webcam functionality (above player heads) still works
        // TODO: Implement preview rendering with correct shader API for this Minecraft version
    }
}
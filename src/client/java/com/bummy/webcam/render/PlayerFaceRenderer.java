package com.bummy.webcam.render;

import com.bummy.webcam.PlayerFeeds;
import com.bummy.webcam.config.WebcamConfig;
import com.bummy.webcam.render.image.RenderableImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static org.lwjgl.opengl.GL33.*;

public class PlayerFaceRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel>  {

    public PlayerFaceRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (clientPlayNetworkHandler == null) {
            return;
        }
        PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(state.name);
        if (playerListEntry == null) {
            return;
        }

        if (MinecraftClient.getInstance().world != null) {
            PlayerEntity player = MinecraftClient.getInstance().world.getPlayerByUuid(playerListEntry.getProfile().getId());
            if (player != null && player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                return;
            }
        }

        String playerUUID = playerListEntry.getProfile().getId().toString();
        // Get the renderable image that represents the current video frame
        // if it is null, then we haven't received any video from them so we don't attempt to render
        RenderableImage image = PlayerFeeds.get(playerUUID);
        if (image == null) {
            return;
        }

        WebcamConfig config = WebcamConfig.getInstance();
        
        matrices.push();

        // Position above the head instead of in front of face
        matrices.translate(config.getOffsetX(), config.getOffsetY(), config.getOffsetZ());

        if (config.shouldRotateWithPlayer()) {
            // Traditional mode - webcam follows player's head rotation
            // No additional rotation needed, webcam will rotate with player model
        } else {
            // Hologram mode - webcam always faces the camera/viewer
            Quaternionf cameraRotation = new Quaternionf(MinecraftClient.getInstance().gameRenderer.getCamera().getRotation());
            matrices.multiply(cameraRotation.conjugate());
        }

        matrices.scale(config.getScale(), config.getScale(), 1f);

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f position = new Matrix4f(entry.getPositionMatrix());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);

        if (config.isCircular()) {
            // Render circular webcam
            renderCircularWebcam(buffer, position, config.getCircleSegments());
        } else {
            // Render rectangular webcam (original behavior)
            renderRectangularWebcam(buffer, position);
        }

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderColor(1, 1, 1, config.getOpacity());

        image.init();
        RenderSystem.setShaderTexture(0, image.id);
        // Set defaults because minecraft might change this during rendering
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4); // Default is 4
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, 0);
        glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0);

        // Upload new image to texture from buffer
        glBindTexture(GL_TEXTURE_2D, image.id);
        image.buffer.bind();
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image.width, image.height, GL_RGB, GL_UNSIGNED_BYTE, 0);
        image.buffer.unbind();

        image.buffer.writeAndSwap(image.data().duplicate());
        glBindTexture(GL_TEXTURE_2D, image.id);

        glDisable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        glUseProgram(0);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glBindTexture(GL_TEXTURE_2D, 0);

        matrices.pop();
    }

    private void renderCircularWebcam(BufferBuilder buffer, Matrix4f position, int segments) {
        float radius = 1.0f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            float x1 = (float) Math.cos(angle1) * radius;
            float y1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float y2 = (float) Math.sin(angle2) * radius;

            // Map circle coordinates to texture coordinates (0-1 range)
            // Since image is now forced to be square, we can use simple mapping
            float u1 = (x1 + 1) / 2;
            float v1 = (y1 + 1) / 2;
            float u2 = (x2 + 1) / 2;
            float v2 = (y2 + 1) / 2;
            float uc = 0.5f; // Center of texture
            float vc = 0.5f; // Center of texture

            buffer.vertex(position, 0, 0, 0).texture(uc, vc);
            buffer.vertex(position, x1, y1, 0).texture(u1, v1);
            buffer.vertex(position, x2, y2, 0).texture(u2, v2);
        }
    }

    private void renderRectangularWebcam(BufferBuilder buffer, Matrix4f position) {
        // Simple texture mapping for rectangular webcam
        // Since image is now forced to be square, we use full texture coordinates
        float u1 = 0.0f;
        float v1 = 0.0f;
        float u2 = 1.0f;
        float v2 = 1.0f;

        buffer.vertex(position, 1, -1, 0).texture(u2, v1);
        buffer.vertex(position, 1, 1, 0).texture(u2, v2);
        buffer.vertex(position, -1, 1, 0).texture(u1, v2);

        buffer.vertex(position, -1, 1, 0).texture(u1, v2);
        buffer.vertex(position, 1, -1, 0).texture(u2, v1);
        buffer.vertex(position, -1, -1, 0).texture(u1, v1);
    }
}

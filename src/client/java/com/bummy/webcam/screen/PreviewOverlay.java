package com.bummy.webcam.screen;

import com.bummy.webcam.PlayerFeeds;
import com.bummy.webcam.config.PreviewConfig;
import com.bummy.webcam.config.WebcamConfig;
import com.bummy.webcam.render.image.RenderableImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL33.*;

public class PreviewOverlay {
    private static final Identifier PREVIEW_TEXTURE = Identifier.of("webcam-mod", "preview");
    
    public void render(DrawContext context) {
        PreviewConfig config = PreviewConfig.getInstance();
        
        if (!config.isEnabled()) {
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        
        // Скрываем превью в режиме третьего лица - игрок и так видит себя
        if (!client.options.getPerspective().isFirstPerson()) {
            return;
        }
        
        // Получаем собственное видео игрока для превью
        String playerUUID = client.player.getUuidAsString();
        RenderableImage image = PlayerFeeds.get(playerUUID);
        
        if (image == null) {
            return;
        }
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // Вычисляем размеры и позицию превью
        int previewSize = (int) (Math.min(screenWidth, screenHeight) * config.getSize());
        int x = (int) (screenWidth * config.getX() - previewSize / 2f);
        int y = (int) (screenHeight * config.getY());
        
        // Убеждаемся что превью не выходит за границы экрана
        x = Math.max(0, Math.min(x, screenWidth - previewSize));
        y = Math.max(0, Math.min(y, screenHeight - previewSize));
        
        renderWebcamPreview(context, image, x, y, previewSize);
    }
    
    private void renderWebcamPreview(DrawContext context, RenderableImage image, int x, int y, int size) {
        // Проверяем что у нас есть данные для отображения
        if (image.data() == null || image.width <= 0 || image.height <= 0) {
            return;
        }
        
        // Инициализируем текстуру если нужно
        image.init();
        
        // Проверяем что буфер инициализирован
        if (image.buffer == null) {
            return;
        }
        
        // Убеждаемся что мы в правильном потоке рендеринга
        if (!RenderSystem.isOnRenderThread()) {
            return;
        }
        
        try {
            // Настраиваем состояние для рендеринга превью
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            
            // Обновляем текстуру безопасно
            updateTextureSafely(image);
            
            // Привязываем текстуру для рендеринга
            RenderSystem.setShaderTexture(0, image.id);
            
            // Используем прос��ой квад для надежности
            renderSimplePreview(context, x, y, size);
            
        } catch (Exception e) {
            // Если что-то пошло не так, просто пропускаем кадр
        } finally {
            // Всегда восстанавливаем состояние
            cleanupRenderState();
        }
    }
    
    private void renderSimplePreview(DrawContext context, int x, int y, int size) {
        // Простой и надежный квад - работает в любом контексте
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        
        // Квад с полным покрытием текстуры
        buffer.vertex(matrix, x, y + size, 0).texture(0.0f, 1.0f);
        buffer.vertex(matrix, x + size, y + size, 0).texture(1.0f, 1.0f);
        buffer.vertex(matrix, x + size, y, 0).texture(1.0f, 0.0f);
        buffer.vertex(matrix, x, y, 0).texture(0.0f, 0.0f);
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    
    private void updateTextureSafely(RenderableImage image) {
        try {
            // Настраиваем параметры распаковки пикселей
            glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
            
            // Обновляем данные текстуры
            glBindTexture(GL_TEXTURE_2D, image.id);
            image.buffer.bind();
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image.width, image.height, GL_RGB, GL_UNSIGNED_BYTE, 0);
            image.buffer.unbind();
            
            image.buffer.writeAndSwap(image.data().duplicate());
        } catch (Exception e) {
            // Если обновление текстуры не удалось, просто продолжаем с предыдущими данными
        }
    }
    
    private void cleanupRenderState() {
        // Сбрасываем шейдерную программу
        glUseProgram(0);
        
        // Отвязываем текстуру
        glBindTexture(GL_TEXTURE_2D, 0);
        
        // Сбрасываем цвет шейдера
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Восстанавливаем блендинг
        RenderSystem.disableBlend();
    }
}
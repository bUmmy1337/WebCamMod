package com.bummy.webcam.screen;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.bummy.webcam.WebcamMod;
import com.bummy.webcam.config.WebcamConfig;
import com.bummy.webcam.video.VideoCamara;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

public class SettingsScreen extends Screen {
    public static final int ELEMENT_HEIGHT = 20;
    public static final int ELEMENT_SPACING = 10;
    public float zoom = 1;
    private WebcamEntryList webcamEntryList;

    public SettingsScreen() {
        super(Text.translatable("gui.webcam.settings.title"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawEntity(context, this.width/4, 0, this.width/4*3, this.height, 100 * zoom, 0.0625F, mouseX, mouseY, this.client.player);
        if (!this.webcamEntryList.canSwitch) {
            context.drawTooltip(this.textRenderer, Text.translatable("gui.webcam.settings.opening_webcam"), this.width/4*2, 50);
        }
        
        // Draw configuration section title
        int rightPanelX = this.width / 4 * 3 + 10;
        context.drawTextWithShadow(this.textRenderer, Text.translatable("gui.webcam.settings.webcam_configuration"), rightPanelX, 10, 0xFFFFFF);
    }

    @Override
    public void init() {
        initCloseButton();
        initWebcamList();
        initConfigurationControls();
    }

    private void initCloseButton() {
        Text closeButtonTitle = Text.translatable("gui.webcam.settings.close");
        int closeButtonWidth = 100;
        int closeButtonX = width - closeButtonWidth - ELEMENT_SPACING;
        int closeButtonY = height - ELEMENT_HEIGHT - ELEMENT_SPACING;
        ButtonWidget closeButton = ButtonWidget.builder(
                closeButtonTitle,
                button -> client.setScreen(null)
        ).dimensions(closeButtonX, closeButtonY, closeButtonWidth, ELEMENT_HEIGHT).build();

        addDrawableChild(closeButton);
    }

    private void initWebcamList() {
        List<String> webcams = VideoCamara.getWebcamList();

        int listWidth = this.width/4;
        int closeButtonY = this.height - ELEMENT_SPACING;
        WebcamEntryList listWidget = new WebcamEntryList(this.client, listWidth, closeButtonY, ELEMENT_SPACING, 18, 18);
        String currentWebcam = VideoCamara.getCurrentWebcam();
        for (String webcamName : webcams) {
            int index = listWidget.addEntry(webcamName);
            if (currentWebcam != null && currentWebcam.equals(webcamName)) {
                listWidget.setSelected(index);
            }
        }

        listWidget.onSelected((String name) -> {
            try {
                VideoCamara.setWebcamByName(name);
                WebcamMod.LOGGER.info("Successfully selected webcam: {}", name);
            } catch (WebcamException exception) {
                WebcamMod.LOGGER.error("Failed to select webcam: {}", name, exception);
                // Show error message to user
                if (client != null && client.player != null) {
                    client.player.sendMessage(Text.translatable("gui.webcam.settings.webcam_error", name, exception.getMessage()), false);
                }
            }
            return null;
        });

        this.webcamEntryList = listWidget;
        addDrawableChild(listWidget);
    }

    private void initConfigurationControls() {
        WebcamConfig config = WebcamConfig.getInstance();
        int rightPanelX = this.width / 4 * 3 + 10;
        int controlWidth = 150;
        int y = 30;
        int spacing = 25;

        // Position controls
        addDrawableChild(new SliderWidget(rightPanelX, y, controlWidth, 20, 
            Text.translatable("gui.webcam.settings.x_offset", String.format("%.2f", config.getOffsetX())),
            (config.getOffsetX() + 2.0f) / 4.0f) {
            @Override
            protected void updateMessage() {
                float value = (float) this.value * 4.0f - 2.0f;
                this.setMessage(Text.translatable("gui.webcam.settings.x_offset", String.format("%.2f", value)));
            }
            @Override
            protected void applyValue() {
                float value = (float) this.value * 4.0f - 2.0f;
                config.setOffsetX(value);
            }
        });
        y += spacing;

        addDrawableChild(new SliderWidget(rightPanelX, y, controlWidth, 20, 
            Text.translatable("gui.webcam.settings.y_offset", String.format("%.2f", config.getOffsetY())),
            (config.getOffsetY() + 1.0f) / 4.0f) {
            @Override
            protected void updateMessage() {
                float value = (float) this.value * 4.0f - 1.0f;
                this.setMessage(Text.translatable("gui.webcam.settings.y_offset", String.format("%.2f", value)));
            }
            @Override
            protected void applyValue() {
                float value = (float) this.value * 4.0f - 1.0f;
                config.setOffsetY(value);
            }
        });
        y += spacing;

        addDrawableChild(new SliderWidget(rightPanelX, y, controlWidth, 20, 
            Text.translatable("gui.webcam.settings.z_offset", String.format("%.2f", config.getOffsetZ())),
            (config.getOffsetZ() + 2.0f) / 4.0f) {
            @Override
            protected void updateMessage() {
                float value = (float) this.value * 4.0f - 2.0f;
                this.setMessage(Text.translatable("gui.webcam.settings.z_offset", String.format("%.2f", value)));
            }
            @Override
            protected void applyValue() {
                float value = (float) this.value * 4.0f - 2.0f;
                config.setOffsetZ(value);
            }
        });
        y += spacing;

        // Scale control
        addDrawableChild(new SliderWidget(rightPanelX, y, controlWidth, 20, 
            Text.translatable("gui.webcam.settings.scale", String.format("%.2f", config.getScale())),
            config.getScale() / 2.0f) {
            @Override
            protected void updateMessage() {
                float value = (float) this.value * 2.0f;
                this.setMessage(Text.translatable("gui.webcam.settings.scale", String.format("%.2f", value)));
            }
            @Override
            protected void applyValue() {
                float value = (float) this.value * 2.0f;
                config.setScale(value);
            }
        });
        y += spacing;

        // Opacity control
        addDrawableChild(new SliderWidget(rightPanelX, y, controlWidth, 20, 
            Text.translatable("gui.webcam.settings.opacity", String.format("%.2f", config.getOpacity())),
            config.getOpacity()) {
            @Override
            protected void updateMessage() {
                float value = (float) this.value;
                this.setMessage(Text.translatable("gui.webcam.settings.opacity", String.format("%.2f", value)));
            }
            @Override
            protected void applyValue() {
                float value = (float) this.value;
                config.setOpacity(value);
            }
        });
        y += spacing;

        // Shape toggle button
        Text shapeText = config.isCircular() ? Text.translatable("gui.webcam.settings.shape_circle") : Text.translatable("gui.webcam.settings.shape_rectangle");
        addDrawableChild(ButtonWidget.builder(shapeText, button -> {
            config.setCircular(!config.isCircular());
            Text newText = config.isCircular() ? Text.translatable("gui.webcam.settings.shape_circle") : Text.translatable("gui.webcam.settings.shape_rectangle");
            button.setMessage(newText);
        }).dimensions(rightPanelX, y, controlWidth, 20).build());
        y += spacing;

        // Rotate with player toggle button
        Text rotateText = Text.translatable("gui.webcam.settings.rotate_with_player").append(config.shouldRotateWithPlayer() ? ": On" : ": Off");
        addDrawableChild(ButtonWidget.builder(rotateText, button -> {
            config.setRotateWithPlayer(!config.shouldRotateWithPlayer());
            button.setMessage(Text.translatable("gui.webcam.settings.rotate_with_player").append(config.shouldRotateWithPlayer() ? ": On" : ": Off"));
        }).dimensions(rightPanelX, y, controlWidth, 20).build());
        y += spacing;

        // Circle segments control (only visible when circular)
        if (config.isCircular()) {
            addDrawableChild(new SliderWidget(rightPanelX, y, controlWidth, 20, 
                Text.translatable("gui.webcam.settings.circle_quality", config.getCircleSegments()),
                (config.getCircleSegments() - 8) / 56.0f) {
                @Override
                protected void updateMessage() {
                    int value = (int) (this.value * 56) + 8;
                    this.setMessage(Text.translatable("gui.webcam.settings.circle_quality", value));
                }
                @Override
                protected void applyValue() {
                    int value = (int) (this.value * 56) + 8;
                    config.setCircleSegments(value);
                }
            });
            y += spacing;
        }

        
        // Reset button
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.webcam.settings.reset_to_defaults"), button -> {
            config.offsetX = 0.0f;
            config.offsetY = 1.5f;
            config.offsetZ = 0.0f;
            config.scale = 0.8f;
            config.circular = true;
            config.circleSegments = 32;
            config.opacity = 1.0f;
            config.rotateWithPlayer = true;
            config.save();
            // Reinitialize the screen to update all controls
            this.init();
        }).dimensions(rightPanelX, y, controlWidth, 20).build());
        y += spacing;

        // Preview settings button
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.webcam.settings.preview_settings"), button -> {
            client.setScreen(new PreviewSettingsScreen(this));
        }).dimensions(rightPanelX, y, controlWidth, 20).build());
    }

    public static void drawEntity(DrawContext context, int x1, int y1, int x2, int y2, float size, float f, float mouseX, float mouseY, LivingEntity entity) {
        float g = (float)(x1 + x2) / 2.0F;
        float h = (float)(y1 + y2) / 2.0F;
        context.enableScissor(x1, y1, x2, y2);
        float i = (float)Math.atan((double)((g - mouseX) / 40.0F));
        float j = (float)Math.atan((double)((h - mouseY) / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(j * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf2);
        float k = entity.bodyYaw;
        float l = entity.getYaw();
        float m = entity.getPitch();
        float n = entity.prevHeadYaw;
        float o = entity.headYaw;
        entity.bodyYaw = 180.0F + i * 20.0F;
        entity.setYaw(180.0F + i * 40.0F);
        entity.setPitch(-j * 20.0F);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        float p = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, entity.getHeight() - 0.6f + f * p, 0.0F);
        float q = (float)size / p;
        drawEntity(context, g, h, q, vector3f, quaternionf, quaternionf2, entity);
        entity.bodyYaw = k;
        entity.setYaw(l);
        entity.setPitch(m);
        entity.prevHeadYaw = n;
        entity.headYaw = o;
        context.disableScissor();
    }

    public static void drawEntity(DrawContext context, float x, float y, float size, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity entity) {
        context.getMatrices().push();
        context.getMatrices().translate((double)x, (double)y, (double)50.0F);
        context.getMatrices().scale(size, size, -size);
        context.getMatrices().translate(vector3f.x, vector3f.y, vector3f.z - 5);
        context.getMatrices().multiply(quaternionf);
        context.draw();
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            entityRenderDispatcher.setRotation(quaternionf2.conjugate(new Quaternionf()).rotateY((float)Math.PI));
        }

        entityRenderDispatcher.setRenderShadows(false);
        context.draw((vertexConsumers) -> entityRenderDispatcher.render(entity, (double)0.0F, (double)0.0F, (double)0.0F, 1.0F, context.getMatrices(), vertexConsumers, 15728880));
        context.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX > this.width/4) {
            zoom += verticalAmount/10;
            if (zoom < 0.1) {
                zoom = 0.1f;
            } else if (zoom > 15) {
                zoom = 15;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Environment(EnvType.CLIENT)
    public class WebcamEntryList extends EntryListWidget<WebcamEntryList.WebcamEntry> {
        public boolean canSwitch = true;
        private Function<String,?> selectedCallback;

        public WebcamEntryList(MinecraftClient client, int width, int height, int y, int itemHeight, int headerHeight) {
            super(client, width, height, y, itemHeight, headerHeight);
        }

        public void onSelected(Function<String, ?> selectedCallback) {
            this.selectedCallback = selectedCallback;
        }

        @Override
        protected void renderHeader(DrawContext context, int x, int y) {
            context.drawCenteredTextWithShadow(SettingsScreen.this.textRenderer, Text.translatable("gui.webcam.settings.select_webcam"), this.width/2, y, 0xFFFF00);
            context.draw();
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            WebcamEntry entry = getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                context.drawTooltip(SettingsScreen.this.textRenderer, Text.of(entry.text), mouseX, mouseY);
            }
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }

        public int addEntry(String text) {
            WebcamEntry webcamEntry = new WebcamEntry();
            webcamEntry.text = text;
            return super.addEntry(webcamEntry);
        }

        @Environment(EnvType.CLIENT)
        public class WebcamEntry extends EntryListWidget.Entry<WebcamEntry> {
            public String text;
            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int centerX = WebcamEntryList.this.width/2;
                int textY = y + entryHeight / 2;
                String textToDraw = this.text;
                if (this.text.length() > 15) {
                   textToDraw = this.text.substring(0, Math.min(this.text.length(), 15)) + "...";
                }

                Text text = Text.of(textToDraw);
                int color = -1;
                if (!WebcamEntryList.this.canSwitch)  {
                    color = 0x5B5B54;
                }
                context.drawCenteredTextWithShadow(SettingsScreen.this.textRenderer, text, centerX, textY - 9 / 2, color);
            }


            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (WebcamEntryList.this.canSwitch && this != WebcamEntryList.this.getSelectedOrNull()) {
                    WebcamEntryList.this.canSwitch = false;
                    WebcamEntryList.this.setSelected(this);
                    new Thread(() -> {
                        WebcamEntryList.this.selectedCallback.apply(this.text);
                        WebcamEntryList.this.canSwitch = true;
                    }).start();
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }

    }
}

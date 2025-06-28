package com.bummy.webcam.screen;

import com.bummy.webcam.config.PreviewConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class PreviewSettingsScreen extends Screen {
    private final Screen parent;

    public PreviewSettingsScreen(Screen parent) {
        super(Text.of("Preview Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        PreviewConfig config = PreviewConfig.getInstance();
        int controlWidth = 150;
        int y = 30;
        int spacing = 25;

        addDrawableChild(new SliderWidget(width / 2 - controlWidth / 2, y, controlWidth, 20,
                Text.of("X Position: " + String.format("%.2f", config.getX())),
                config.getX()) {
            @Override
            protected void updateMessage() {
                setMessage(Text.of("X Position: " + String.format("%.2f", this.value)));
            }

            @Override
            protected void applyValue() {
                config.setX((float) this.value);
            }
        });
        y += spacing;

        addDrawableChild(new SliderWidget(width / 2 - controlWidth / 2, y, controlWidth, 20,
                Text.of("Y Position: " + String.format("%.2f", config.getY())),
                config.getY()) {
            @Override
            protected void updateMessage() {
                setMessage(Text.of("Y Position: " + String.format("%.2f", this.value)));
            }

            @Override
            protected void applyValue() {
                config.setY((float) this.value);
            }
        });
        y += spacing;

        addDrawableChild(new SliderWidget(width / 2 - controlWidth / 2, y, controlWidth, 20,
                Text.of("Size: " + String.format("%.2f", config.getSize())),
                config.getSize()) {
            @Override
            protected void updateMessage() {
                setMessage(Text.of("Size: " + String.format("%.2f", this.value)));
            }

            @Override
            protected void applyValue() {
                config.setSize((float) this.value);
            }
        });
        y += spacing;

        addDrawableChild(ButtonWidget.builder(Text.of("Enabled: " + config.isEnabled()), button -> {
            config.setEnabled(!config.isEnabled());
            button.setMessage(Text.of("Enabled: " + config.isEnabled()));
        }).dimensions(width / 2 - controlWidth / 2, y, controlWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            client.setScreen(parent);
        }).dimensions(width / 2 - controlWidth / 2, height - 30, controlWidth, 20).build());
    }
}

package com.bummy.webcam.screen;

import com.bummy.webcam.config.WebcamConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WelcomeScreen extends Screen {
    
    public WelcomeScreen() {
        super(Text.translatable("gui.webcam.welcome.title"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Кнопка "Настроить мод"
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.webcam.welcome.configure"),
            button -> {
                WebcamConfig.getInstance().setFirstRun(false);
                this.client.setScreen(new SettingsScreen());
            }
        ).dimensions(this.width / 2 - 100, this.height / 2 + 80, 200, 20).build());
        
        // Кнопка "Пропустить"
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.webcam.welcome.skip"),
            button -> {
                WebcamConfig.getInstance().setFirstRun(false);
                this.close();
            }
        ).dimensions(this.width / 2 - 100, this.height / 2 + 110, 200, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Заголовок
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.translatable("gui.webcam.welcome.header").formatted(Formatting.GOLD, Formatting.BOLD),
            this.width / 2,
            this.height / 2 - 60,
            0xFFFFFF
        );
        
        // Описание
        String[] lineKeys = {
            "gui.webcam.welcome.description.line1",
            "gui.webcam.welcome.description.line2",
            "gui.webcam.welcome.description.line3",
            "gui.webcam.welcome.description.line4",
            "gui.webcam.welcome.description.line5",
            "gui.webcam.welcome.description.line6",
            "gui.webcam.welcome.description.line7",
            "gui.webcam.welcome.description.line8",
            "gui.webcam.welcome.description.line9"
        };
        
        int startY = this.height / 2 - 30;
        for (int i = 0; i < lineKeys.length; i++) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable(lineKeys[i]),
                this.width / 2,
                startY + i * 10,
                0xCCCCCC
            );
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        WebcamConfig.getInstance().setFirstRun(false);
        return true;
    }
}
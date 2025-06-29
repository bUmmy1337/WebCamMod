package com.bummy.webcam.screen;

import com.bummy.webcam.config.WebcamConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WelcomeScreen extends Screen {
    
    public WelcomeScreen() {
        super(Text.literal("Webcam Mod - Добро пожаловать!"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Кнопка "Настроить мод"
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Настроить мод"),
            button -> {
                WebcamConfig.getInstance().setFirstRun(false);
                this.client.setScreen(new SettingsScreen());
            }
        ).dimensions(this.width / 2 - 100, this.height / 2 + 80, 200, 20).build());
        
        // Кнопка "Пропустить"
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Пропустить"),
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
            Text.literal("Добро пожаловать в Webcam Bubbles!").formatted(Formatting.GOLD, Formatting.BOLD),
            this.width / 2,
            this.height / 2 - 60,
            0xFFFFFF
        );
        
        // Описание
        String[] lines = {
            "Этот мод позволяет отображать веб-камеру над головой игрока.",
            "",
            "Для корректной работы мода рекомендуется настроить:",
            "• Позицию и размер веб-камеры",
            "• Режим отображения (голограмма или следование за игроком)",
            "• Прозрачность и другие параметры",
            "",
            "Вы можете настроить мод сейчас или сделать это позже",
            "через команду /webcam-settings или горячую клавишу."
        };
        
        int startY = this.height / 2 - 30;
        for (int i = 0; i < lines.length; i++) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(lines[i]),
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
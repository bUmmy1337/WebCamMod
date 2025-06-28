package com.bummy.webcam.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyBinding settingsKeyBinding;

    public static void register() {
        settingsKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webcam.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.webcam.main"
        ));
    }

    public static KeyBinding getSettingsKeyBinding() {
        return settingsKeyBinding;
    }
}

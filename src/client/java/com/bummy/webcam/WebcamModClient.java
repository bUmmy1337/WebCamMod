package com.bummy.webcam;

import com.bummy.webcam.render.PlayerFaceRenderer;

import com.bummy.webcam.screen.SettingsScreen;
import com.bummy.webcam.video.VideoManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.bummy.webcam.screen.PreviewOverlay;
import com.bummy.webcam.config.KeyBindings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;


public class WebcamModClient implements ClientModInitializer {
	private final PreviewOverlay previewOverlay = new PreviewOverlay();

	@Override
	public void onInitializeClient() {
		KeyBindings.register();
		registerSettingsCommand();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (KeyBindings.getSettingsKeyBinding().wasPressed()) {
				client.setScreen(new SettingsScreen());
			}
		});

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			previewOverlay.render(drawContext);
		});

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerEntityRenderer) {
				registrationHelper.register(new PlayerFaceRenderer((PlayerEntityRenderer) entityRenderer));
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			VideoManager.startCameraLoop();
		});

		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
			VideoManager.stopThread();
		}));

		ClientPlayNetworking.registerGlobalReceiver(VideoFramePayload.ID, ((payload, context) -> {
			PlayerFeeds.update(payload.video());
		}));
	}


	private void registerSettingsCommand() {
		ClientCommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess) -> {
			commandDispatcher.register(ClientCommandManager.literal("webcam-settings").executes(context ->  {
				MinecraftClient client = context.getSource().getClient();

				client.send(() -> client.setScreen(new SettingsScreen()));
				return 1;
			}));
		}));
	}
}
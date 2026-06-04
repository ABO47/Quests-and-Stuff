package com.abo47.questsandstuff.fabric;

import com.abo47.questsandstuff.client.hud.QuestHudOverlayRenderer;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerPickOverlays;
import com.abo47.questsandstuff.client.tablet.screen.TabletClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

public final class QuestsAndStuffFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricQuestNetworkClient.register();
        TabletClientHooks.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> TabletClientHooks.onClientLogin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TabletClientHooks.onClientLogout());
        ClientTickEvents.END_CLIENT_TICK.register(client -> TabletClientHooks.onClientTick());
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> QuestHudOverlayRenderer.render(graphics));
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenEvents.afterRender(screen).register((currentScreen, graphics, mouseX, mouseY, tickDelta) ->
                    RecipeViewerPickOverlays.drawForScreen(currentScreen, graphics, mouseX, mouseY));
            ScreenMouseEvents.allowMouseClick(screen).register((currentScreen, mouseX, mouseY, button) ->
                    !RecipeViewerPickOverlays.pickFromScreen(currentScreen, mouseX, mouseY, button));
        });
    }
}

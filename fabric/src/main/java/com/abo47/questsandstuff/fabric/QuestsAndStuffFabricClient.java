package com.abo47.questsandstuff.fabric;

import com.abo47.questsandstuff.client.hud.QuestCompletionNotificationOverlay;
import com.abo47.questsandstuff.client.tablet.screen.TabletClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class QuestsAndStuffFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricQuestNetworkClient.register();
        TabletClientHooks.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> TabletClientHooks.onClientLogin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TabletClientHooks.onClientLogout());
        ClientTickEvents.END_CLIENT_TICK.register(client -> TabletClientHooks.onClientTick());
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> QuestCompletionNotificationOverlay.render(graphics));
    }
}
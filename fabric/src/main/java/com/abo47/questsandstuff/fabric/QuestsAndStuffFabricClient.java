package com.abo47.questsandstuff.fabric;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.compat.recipeviewer.ItemLockViewerSync;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerPickOverlays;
import com.abo47.questsandstuff.client.quest.hud.QuestHudOverlayRenderer;
import com.abo47.questsandstuff.client.quest.lock.LockedItemTooltips;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletKeybindings;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.quest.runtime.lock.ItemLockMenuGating;

public final class QuestsAndStuffFabricClient implements ClientModInitializer {
    private static final ResourceLocation EARLY_HUD_PHASE = new ResourceLocation(QuestsAndStuffMod.MODID, "early_hud");

    @Override
    public void onInitializeClient() {
        TabletLifecycle.prewarmClientAtGameLaunch();
        FabricModNetworkClient.register();
        ItemLockViewerSync.ensureSubscribed();
        TabletKeybindings.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> TabletLifecycle.onClientLogin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TabletLifecycle.onClientLogout());
        ClientTickEvents.END_CLIENT_TICK.register(client -> TabletLifecycle.onClientTick());
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, lines) -> LockedItemTooltips.append(lines, stack));
        HudRenderCallback.EVENT.addPhaseOrdering(EARLY_HUD_PHASE, net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE);
        HudRenderCallback.EVENT.register(EARLY_HUD_PHASE, (graphics, tickDelta) -> QuestHudOverlayRenderer.render(graphics));
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen && client.player != null) {
                ItemLockMenuGating.gateCraftingMenu(client.player, containerScreen.getMenu(), true);
            }
            ScreenEvents.afterRender(screen).register((currentScreen, graphics, mouseX, mouseY, tickDelta) ->
                    RecipeViewerPickOverlays.drawForScreen(currentScreen, graphics, mouseX, mouseY));
            ScreenMouseEvents.allowMouseClick(screen).register((currentScreen, mouseX, mouseY, button) ->
                    !RecipeViewerPickOverlays.pickFromScreen(currentScreen, mouseX, mouseY, button));
        });
    }
}

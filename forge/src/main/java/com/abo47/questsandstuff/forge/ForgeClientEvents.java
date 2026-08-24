package com.abo47.questsandstuff.forge;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.compat.recipeviewer.ItemLockViewerSync;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerPickOverlays;
import com.abo47.questsandstuff.client.quest.hud.QuestHudOverlayRenderer;
import com.abo47.questsandstuff.client.quest.lock.LockClientRefresh;
import com.abo47.questsandstuff.client.quest.lock.LockedItemTooltips;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletKeybindings;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ForgeClientEvents {
    private ForgeClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = QuestsAndStuffMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            TabletKeybindings.registerKeyMappings(event::register);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                TabletLifecycle.prewarmClientAtGameLaunch();
                ItemLockViewerSync.ensureSubscribed();
            });
        }
    }

    @Mod.EventBusSubscriber(modid = QuestsAndStuffMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBusEvents {
        private ForgeBusEvents() {
        }

        @SubscribeEvent
        public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            TabletLifecycle.onClientLogin();
        }

        @SubscribeEvent
        public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            TabletLifecycle.onClientLogout();
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                TabletLifecycle.onClientTick();
            }
        }

        @SubscribeEvent
        public static void onRecipesUpdated(RecipesUpdatedEvent event) {
            var minecraft = Minecraft.getInstance();
            LockClientRefresh.onRecipesUpdated(minecraft.level.getRecipeManager(), minecraft.level.registryAccess());
        }

        @SubscribeEvent
        public static void onItemTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
            LockedItemTooltips.append(event.getToolTip(), event.getItemStack());
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
            if (VanillaGuiOverlay.CHAT_PANEL.id().equals(event.getOverlay().id())) {
                QuestHudOverlayRenderer.render(event.getGuiGraphics());
            }
        }

        @SubscribeEvent
        public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
            RecipeViewerPickOverlays.drawForScreen(event.getScreen(), event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
        }

        @SubscribeEvent
        public static void onScreenMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
            if (RecipeViewerPickOverlays.pickFromScreen(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.setCanceled(true);
            }
        }
    }
}

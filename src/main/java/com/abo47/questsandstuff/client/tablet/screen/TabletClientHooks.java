package com.abo47.questsandstuff.client.tablet.screen;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = QuestsAndStuffMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TabletClientHooks {
    private static final String CATEGORY = "key.categories.questsandstuff";
    private static boolean uiPrewarmed;
    private static final KeyMapping OPEN_UI = new KeyMapping(
            "key.questsandstuff.open_ui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    private static final KeyMapping QUICK_CONNECT = new KeyMapping(
            "key.questsandstuff.quick_connect",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    private static final KeyMapping RENAME_SELECTED = new KeyMapping(
            "key.questsandstuff.rename_selected",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F3,
            CATEGORY
    );
    private static boolean suppressNextOpenClick;
    private static boolean rememberedQuestDetailsOpen;
    private static String rememberedQuestDetailsQuestId = "";

    private TabletClientHooks() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_UI);
        event.register(QUICK_CONNECT);
        event.register(RENAME_SELECTED);
    }

    public static boolean quickConnectDown() {
        return QUICK_CONNECT.isDown();
    }

    public static boolean quickConnectMatches(int keyCode, int scanCode) {
        return QUICK_CONNECT.matches(keyCode, scanCode);
    }

    public static boolean renameSelectedMatches(int keyCode, int scanCode) {
        return RENAME_SELECTED.matches(keyCode, scanCode);
    }

    public static boolean openUiMatches(int keyCode, int scanCode) {
        return OPEN_UI.matches(keyCode, scanCode);
    }

    public static void rememberActiveWindow(TabletUiState state) {
        rememberedQuestDetailsOpen = state != null && state.questDetailsOpen && state.questDetailsQuestId != null && !state.questDetailsQuestId.isBlank();
        rememberedQuestDetailsQuestId = rememberedQuestDetailsOpen ? state.questDetailsQuestId.trim() : "";
    }

    public static void rememberMainWindow() {
        rememberedQuestDetailsOpen = false;
        rememberedQuestDetailsQuestId = "";
    }

    public static void restoreRememberedWindow(TabletUiState state) {
        if (state == null || !rememberedQuestDetailsOpen || rememberedQuestDetailsQuestId.isBlank()) {
            return;
        }
        if (ClientQuestCache.quests().containsKey(rememberedQuestDetailsQuestId)) {
            QuestDetailsWindow.open(state, rememberedQuestDetailsQuestId);
        } else {
            rememberedQuestDetailsOpen = false;
            rememberedQuestDetailsQuestId = "";
        }
    }

    public static void suppressNextOpenClick() {
        suppressNextOpenClick = true;
    }

    @Mod.EventBusSubscriber(modid = QuestsAndStuffMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {
    private ForgeEvents() {
        }

        @SubscribeEvent
        public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            resetSessionLocalState();
        }

        @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            resetSessionLocalState();
        }

    private static void resetSessionLocalState() {
            ClientQuestCache.resetStateForTests();
            rememberedQuestDetailsOpen = false;
            rememberedQuestDetailsQuestId = "";
            suppressNextOpenClick = false;
            uiPrewarmed = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] reset client quest cache/window state for world session");
        }

        @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                uiPrewarmed = false;
                return;
            }
            if (!uiPrewarmed) {
                TabletUiFactory.prewarmClientUiAssets();
                uiPrewarmed = true;
                QuestsAndStuffMod.debugLog("[QnS:UI] prewarmed tablet ui caches");
            }
            if (minecraft.gameMode == null || minecraft.screen != null) {
                return;
            }
            if (suppressNextOpenClick && !OPEN_UI.isDown()) {
                suppressNextOpenClick = false;
            }
            while (OPEN_UI.consumeClick()) {
                if (suppressNextOpenClick) {
                    continue;
                }
                openQuestTabletUi(minecraft, minecraft.player);
                break;
            }
        }

    public static void openQuestTabletUiFromItem(Player player) {
            if (player instanceof LocalPlayer localPlayer) {
                openQuestTabletUi(Minecraft.getInstance(), localPlayer);
            }
        }

    private static void openQuestTabletUi(Minecraft minecraft, LocalPlayer player) {
            if (player == null) {
                return;
            }
            ModularUI uiTemplate = new ModularUI(TabletUiFactory.create(player), IUIHolder.EMPTY, player);
            uiTemplate.initWidgets();
            ModularUIGuiContainer modularUiGui = new ModularUIGuiContainer(uiTemplate, player.containerMenu.containerId);
            minecraft.setScreen(modularUiGui);
            player.containerMenu = modularUiGui.getMenu();
            QuestsAndStuffMod.debugLog("[QnS:UI] keybind open ui direct");
        }
    }
}

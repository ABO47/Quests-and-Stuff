package com.abo47.questsandstuff.client.tablet.screen;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.hud.QuestHudLayoutEditScreen;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public final class TabletClientHooks {
    private static final String CATEGORY = "key.categories.questsandstuff";
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
    private static final KeyMapping EDIT_HUD = new KeyMapping(
            "key.questsandstuff.edit_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping GIZMO_MOVE = new KeyMapping(
            "key.questsandstuff.gizmo_move",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_W,
            CATEGORY
    );
    private static final KeyMapping GIZMO_RESIZE = new KeyMapping(
            "key.questsandstuff.gizmo_resize",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY
    );
    private static final KeyMapping GIZMO_ROTATE = new KeyMapping(
            "key.questsandstuff.gizmo_rotate",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    private static boolean uiPrewarmed;
    private static boolean suppressNextOpenClick;
    private static boolean rememberedQuestDetailsOpen;
    private static String rememberedQuestDetailsQuestId = "";

    private TabletClientHooks() {
    }

    public static void registerKeyMappings(Consumer<KeyMapping> registrar) {
        registrar.accept(OPEN_UI);
        registrar.accept(QUICK_CONNECT);
        registrar.accept(RENAME_SELECTED);
        registrar.accept(EDIT_HUD);
        registrar.accept(GIZMO_MOVE);
        registrar.accept(GIZMO_RESIZE);
        registrar.accept(GIZMO_ROTATE);
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

    public static boolean gizmoMoveMatches(int keyCode, int scanCode) {
        return GIZMO_MOVE.matches(keyCode, scanCode);
    }

    public static boolean gizmoResizeMatches(int keyCode, int scanCode) {
        return GIZMO_RESIZE.matches(keyCode, scanCode);
    }

    public static boolean gizmoRotateMatches(int keyCode, int scanCode) {
        return GIZMO_ROTATE.matches(keyCode, scanCode);
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

    public static void closeQuestTabletUi(TabletUiState state, boolean suppressOpenClick, String reason) {
        rememberActiveWindow(state);
        if (suppressOpenClick) {
            suppressNextOpenClick();
        }
        Minecraft.getInstance().setScreen(null);
        QuestsAndStuffMod.debugLog("[QnS:UI] close ui reason={}", reason == null || reason.isBlank() ? "unknown" : reason);
    }

    public static void onClientLogin() {
        resetSessionLocalState();
    }

    public static void onClientLogout() {
        resetSessionLocalState();
    }

    public static void onClientTick() {
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
        while (EDIT_HUD.consumeClick()) {
            minecraft.setScreen(new QuestHudLayoutEditScreen());
            QuestsAndStuffMod.debugLog("[QnS:UI] hud layout editor opened from keybind");
            break;
        }
    }

    public static void openQuestTabletUiFromItem(Player player) {
        if (player instanceof LocalPlayer localPlayer) {
            openQuestTabletUi(Minecraft.getInstance(), localPlayer);
        }
    }

    public static void applyQuestTabletLayoutMode(TabletUiState state) {
        if (state == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean fullScreen = QuestsAndStuffConfig.fullScreenModeEnabled();
        int rootW = targetRootWidth(minecraft, fullScreen);
        int rootH = targetRootHeight(minecraft, fullScreen);
        TabletUiFactory.applyRootSize(state, rootW, rootH, fullScreen);
        if (minecraft.screen instanceof ModularUIGuiContainer container) {
            container.modularUI.setSize(rootW, rootH);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] tablet layout mode fullscreen={} width={} height={}", fullScreen, rootW, rootH);
    }

    private static void resetSessionLocalState() {
        ClientQuestCache.resetStateForTests();
        rememberedQuestDetailsOpen = false;
        rememberedQuestDetailsQuestId = "";
        suppressNextOpenClick = false;
        uiPrewarmed = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] reset client quest cache/window state for world session");
    }

    private static void openQuestTabletUi(Minecraft minecraft, LocalPlayer player) {
        if (player == null) {
            return;
        }
        boolean fullScreen = QuestsAndStuffConfig.fullScreenModeEnabled();
        int rootW = targetRootWidth(minecraft, fullScreen);
        int rootH = targetRootHeight(minecraft, fullScreen);
        ModularUI uiTemplate = new ModularUI(TabletUiFactory.create(player, rootW, rootH, fullScreen), IUIHolder.EMPTY, player);
        uiTemplate.initWidgets();
        ModularUIGuiContainer modularUiGui = new ModularUIGuiContainer(uiTemplate, player.containerMenu.containerId);
        minecraft.setScreen(modularUiGui);
        player.containerMenu = modularUiGui.getMenu();
        QuestsAndStuffMod.debugLog("[QnS:UI] keybind open ui direct");
    }

    private static int targetRootWidth(Minecraft minecraft, boolean fullScreen) {
        if (!fullScreen || minecraft == null) {
            return TabletUiFactory.ROOT_W;
        }
        return Math.max(1, minecraft.getWindow().getGuiScaledWidth());
    }

    private static int targetRootHeight(Minecraft minecraft, boolean fullScreen) {
        if (!fullScreen || minecraft == null) {
            return TabletUiFactory.ROOT_H;
        }
        return Math.max(1, minecraft.getWindow().getGuiScaledHeight());
    }
}

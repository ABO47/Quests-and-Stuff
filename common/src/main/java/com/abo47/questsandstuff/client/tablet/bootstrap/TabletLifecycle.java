package com.abo47.questsandstuff.client.tablet.bootstrap;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayoutManagerEditScreen;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.app.AppDescriptor;
import com.abo47.questsandstuff.client.tablet.app.TabletAppRegistry;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.modal.TabletBlockPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletSoundPickerModal;
import com.abo47.questsandstuff.client.tablet.modal.TabletStatPickerModal;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.ui.TabletGuiContainer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiStatePersistence;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class TabletLifecycle {
    private static boolean uiPrewarmed;
    private static boolean suppressNextOpenClick;
    private static boolean rememberedQuestDetailsOpen;
    private static String rememberedQuestDetailsQuestId = "";

    private TabletLifecycle() {
    }

    public static void prewarmClientAtGameLaunch() {
        DisplayIconProvider.prewarm();
        TabletStatPickerModal.prewarm();
        TabletSoundPickerModal.prewarm();
        TabletBlockPickerModal.prewarm();
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
        if (suppressNextOpenClick && !TabletKeybindings.OPEN_UI.isDown() && !TabletKeybindings.OPEN_QUESTS_UI.isDown()) {
            suppressNextOpenClick = false;
        }
        while (TabletKeybindings.OPEN_UI.consumeClick()) {
            if (suppressNextOpenClick) {
                continue;
            }
            openTabletUi(minecraft, minecraft.player);
            break;
        }
        while (TabletKeybindings.OPEN_QUESTS_UI.consumeClick()) {
            if (suppressNextOpenClick) {
                continue;
            }
            openApp("QUESTS");
            break;
        }
        while (TabletKeybindings.EDIT_HUD.consumeClick()) {
            minecraft.setScreen(new QuestHudLayoutManagerEditScreen());
            QuestsAndStuffMod.debugLog("[QnS:UI] hud layout editor opened from keybind");
            break;
        }
    }

    public static void openTabletUiFromItem(Player player) {
        if (player instanceof LocalPlayer localPlayer) {
            openTabletUi(Minecraft.getInstance(), localPlayer);
        }
    }

    public static void openTabletUiHome(Player player) {
        if (player instanceof LocalPlayer) {
            openApp("home");
        }
    }

    public static void openApp(String appId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.player instanceof LocalPlayer player)) {
            return;
        }
        TabletBootstrap.ensureAppsRegistered();
        AppDescriptor app = TabletAppRegistry.get(appId);
        if (app == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] unknown app id={}", appId);
            return;
        }
        saveLastApp("home".equals(appId) ? "" : appId);
        boolean isHome = "home".equals(appId);
        boolean fullScreen = !isHome && QuestsAndStuffConfig.fullScreenModeEnabled();
        int rootW = isHome ? TabletUiFactory.ROOT_W : TabletScreenManager.targetRootWidth(minecraft, fullScreen);
        int rootH = isHome ? TabletUiFactory.ROOT_H : TabletScreenManager.targetRootHeight(minecraft, fullScreen);
        WidgetGroup content = app.composer().create(player, rootW, rootH, fullScreen);
        ModularUI uiTemplate = new ModularUI(content, IUIHolder.EMPTY, player);
        uiTemplate.initWidgets();
        TabletGuiContainer modularUiGui = new TabletGuiContainer(uiTemplate, player.containerMenu.containerId);
        minecraft.setScreen(modularUiGui);
        player.containerMenu = modularUiGui.getMenu();
        QuestsAndStuffMod.debugLog("[QnS:UI] open app={}", appId);
    }

    public static void rememberActiveWindow(TabletUiState state) {
        rememberedQuestDetailsOpen = state != null && state.questDetails.questDetailsOpen && state.questDetails.questDetailsQuestId != null && !state.questDetails.questDetailsQuestId.isBlank();
        rememberedQuestDetailsQuestId = rememberedQuestDetailsOpen ? state.questDetails.questDetailsQuestId.trim() : "";
    }

    public static void rememberMainWindow() {
        rememberedQuestDetailsOpen = false;
        rememberedQuestDetailsQuestId = "";
    }

    public static void restoreRememberedWindow(TabletUiState state) {
        if (state == null || !rememberedQuestDetailsOpen || rememberedQuestDetailsQuestId.isBlank()) {
            return;
        }
        if (ClientQuestCache.containsQuest(rememberedQuestDetailsQuestId)) {
            if (state.questDetails.questDetailsOpen) {
                QuestDetailsWindow.swapQuest(state, rememberedQuestDetailsQuestId);
            } else {
                QuestDetailsWindow.open(state, rememberedQuestDetailsQuestId);
            }
        } else {
            rememberedQuestDetailsOpen = false;
            rememberedQuestDetailsQuestId = "";
        }
    }

    public static void suppressNextOpenClick() {
        suppressNextOpenClick = true;
    }

    public static void closeTabletUi(TabletUiState state, boolean suppressOpenClick, String reason) {
        rememberActiveWindow(state);
        if (state != null) {
            String app = state.root.currentApp;
            if ("quest".equals(app)) {
                saveLastApp("QUESTS");
            } else if ("teams".equals(app)) {
                saveLastApp("TEAMS");
            } else if ("home".equals(app)) {
                saveLastApp("");
            }
            if (state.root.skinEditMode) {
                state.root.skinEditMode = false;
                state.root.skinEditSelectedTarget = "";
                TabletUiFactory.persistSkinState(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] skin edit mode cleared on ui close");
            }
        }
        if (suppressOpenClick) {
            suppressNextOpenClick();
        }
        Minecraft.getInstance().setScreen(null);
        QuestsAndStuffMod.debugLog("[QnS:UI] close ui reason={}", reason == null || reason.isBlank() ? "unknown" : reason);
    }

    private static void resetSessionLocalState() {
        ClientQuestCache.resetStateForTests();
        rememberedQuestDetailsOpen = false;
        rememberedQuestDetailsQuestId = "";
        suppressNextOpenClick = false;
        uiPrewarmed = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] reset client quest cache/window state for world session");
    }

    private static void openTabletUi(Minecraft minecraft, LocalPlayer player) {
        if (player == null) {
            return;
        }
        String lastApp = readLastApp();
        if ("TEAMS".equals(lastApp)) {
            openApp("TEAMS");
        } else if ("QUESTS".equals(lastApp)) {
            openApp("QUESTS");
        } else {
            openApp("home");
        }
    }

    private static String readLastApp() {
        TabletUiState temp = new TabletUiState();
        TabletUiStatePersistence.read(temp);
        return temp.root.lastApp;
    }

    private static void saveLastApp(String appId) {
        TabletUiState temp = new TabletUiState();
        TabletUiStatePersistence.read(temp);
        temp.root.lastApp = appId;
        TabletUiStatePersistence.write(temp);
    }
}

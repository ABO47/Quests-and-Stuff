package com.abo47.questsandstuff.client.quest.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class QuestHudAssetLibraryBridge {
    private QuestHudAssetLibraryBridge() {
    }

    static boolean open(QuestHudLayoutEditScreen editScreen, QuestHudLayoutManager.Element element) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || element == null) {
            return false;
        }
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        TabletUiState state = new TabletUiState();
        state.root.tabletRootWidth = screenW;
        state.root.tabletRootHeight = screenH;
        ModalOpenActions.openHudBackgroundPicker(state, targetName(element), QuestHudLayoutManager.background(element), QuestHudLayoutManager.opacityPercent(element));

        Runnable[] refresh = new Runnable[1];
        WidgetGroup root = new WidgetGroup(0, 0, screenW, screenH);

        ModalDismissGuard modalLayer = new ModalDismissGuard(0, 0, screenW, screenH, state, () -> {
            if (refresh[0] != null) {
                refresh[0].run();
            }
        });
        refresh[0] = () -> {
            ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
            if (!ModalStateQueries.anyOpen(state)) {
                Minecraft.getInstance().setScreen(editScreen);
            }
        };
        root.addWidget(modalLayer);
        refresh[0].run();

        ModularUI uiTemplate = new ModularUI(root, IUIHolder.EMPTY, player);
        uiTemplate.initWidgets();
        HudBackgroundPickerScreen screen = new HudBackgroundPickerScreen(uiTemplate, player.containerMenu.containerId, editScreen);
        minecraft.setScreen(screen);
        player.containerMenu = screen.getMenu();
        return true;
    }

    private static String targetName(QuestHudLayoutManager.Element element) {
        return element == QuestHudLayoutManager.Element.COMPLETION ? "completion" : "pinned";
    }

    private static final class HudBackgroundPickerScreen extends ModularUIGuiContainer {
        private final QuestHudLayoutEditScreen parentScreen;
        private boolean returned;

        HudBackgroundPickerScreen(ModularUI modularUI, int windowId, QuestHudLayoutEditScreen parentScreen) {
            super(modularUI, windowId);
            this.parentScreen = parentScreen;
        }

        @Override
        public void onClose() {
            super.onClose();
            returnToParent();
        }

        @Override
        public void removed() {
            super.removed();
            returnToParent();
        }

        private void returnToParent() {
            if (returned) {
                return;
            }
            returned = true;
            parentScreen.returnFromChild();
            Minecraft.getInstance().setScreen(parentScreen);
        }
    }
}

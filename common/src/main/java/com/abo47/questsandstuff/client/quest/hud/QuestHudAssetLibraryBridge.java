package com.abo47.questsandstuff.client.quest.hud;

import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.ui.TabletGuiContainer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

final class QuestHudAssetLibraryBridge {
    private QuestHudAssetLibraryBridge() {
    }

    static boolean open(QuestHudLayoutManagerEditScreen editScreen, QuestHudLayoutManager.Element element) {
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

        boolean[] returning = new boolean[]{false};
        Runnable[] refresh = new Runnable[1];
        WidgetGroup root = new WidgetGroup(0, 0, screenW, screenH) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                if (!returning[0] && !ModalStateQueries.anyOpen(state)) {
                    returnToParent(editScreen, returning);
                }
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    ModalCloseActions.closeAll(state);
                    if (refresh[0] != null) {
                        refresh[0].run();
                    }
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };

        ModalDismissGuard modalLayer = new ModalDismissGuard(0, 0, screenW, screenH, state, () -> {
            if (refresh[0] != null) {
                refresh[0].run();
            }
        });
        refresh[0] = () -> ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
        root.addWidget(modalLayer);
        refresh[0].run();

        ModularUI uiTemplate = new ModularUI(root, IUIHolder.EMPTY, player);
        uiTemplate.initWidgets();
        TabletGuiContainer screen = new TabletGuiContainer(uiTemplate, player.containerMenu.containerId);
        minecraft.setScreen(screen);
        player.containerMenu = screen.getMenu();
        return true;
    }

    private static void returnToParent(QuestHudLayoutManagerEditScreen parent, boolean[] returning) {
        if (returning[0]) {
            return;
        }
        returning[0] = true;
        parent.returnFromChild();
        Minecraft.getInstance().setScreen(parent);
    }

    private static String targetName(QuestHudLayoutManager.Element element) {
        return element == QuestHudLayoutManager.Element.COMPLETION ? "completion" : "pinned";
    }
}

package com.abo47.questsandstuff.client.hud;

import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalLayerWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.screen.QuestTabletGuiContainer;
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

    static boolean open(QuestHudLayoutEditScreen editScreen, QuestHudLayout.Element element) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || element == null) {
            return false;
        }
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        TabletUiState state = new TabletUiState();
        state.tabletRootWidth = screenW;
        state.tabletRootHeight = screenH;
        ModalOpenActions.openHudBackgroundPicker(state, targetName(element), QuestHudLayout.background(element), QuestHudLayout.opacityPercent(element));

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

        ModalLayerWidget modalLayer = new ModalLayerWidget(0, 0, screenW, screenH, state, () -> {
            if (refresh[0] != null) {
                refresh[0].run();
            }
        });
        refresh[0] = () -> ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
        root.addWidget(modalLayer);
        refresh[0].run();

        ModularUI uiTemplate = new ModularUI(root, IUIHolder.EMPTY, player);
        uiTemplate.initWidgets();
        QuestTabletGuiContainer screen = new QuestTabletGuiContainer(uiTemplate, player.containerMenu.containerId);
        minecraft.setScreen(screen);
        player.containerMenu = screen.getMenu();
        return true;
    }

    private static void returnToParent(QuestHudLayoutEditScreen parent, boolean[] returning) {
        if (returning[0]) {
            return;
        }
        returning[0] = true;
        parent.returnFromChild();
        Minecraft.getInstance().setScreen(parent);
    }

    private static String targetName(QuestHudLayout.Element element) {
        return element == QuestHudLayout.Element.COMPLETION ? "completion" : "pinned";
    }
}

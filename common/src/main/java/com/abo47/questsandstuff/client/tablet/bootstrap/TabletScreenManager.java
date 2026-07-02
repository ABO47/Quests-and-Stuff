package com.abo47.questsandstuff.client.tablet.bootstrap;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletGuiContainer;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import net.minecraft.client.Minecraft;

public final class TabletScreenManager {
    private static final int FULLSCREEN_GRID_CELL_SIZE = 16;
    private static final int FULLSCREEN_ROOT_WIDTH_REMAINDER = Math.floorMod(TabletUiFactory.ROOT_W, FULLSCREEN_GRID_CELL_SIZE);
    private static final int FULLSCREEN_ROOT_HEIGHT_REMAINDER = Math.floorMod(TabletUiFactory.ROOT_H, FULLSCREEN_GRID_CELL_SIZE);

    private TabletScreenManager() {
    }

    public static void applyTabletLayoutMode(TabletUiState state) {
        if (state == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean fullScreen = QuestsAndStuffConfig.fullScreenModeEnabled();
        int rootW = targetRootWidth(minecraft, fullScreen);
        int rootH = targetRootHeight(minecraft, fullScreen);
        TabletUiFactory.applyRootSize(state, rootW, rootH, fullScreen);
        if (minecraft.screen instanceof TabletGuiContainer container) {
            container.modularUI.setSize(rootW, rootH);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] tablet layout mode fullscreen={} width={} height={}", fullScreen, rootW, rootH);
    }

    public static int targetRootWidth(Minecraft minecraft, boolean fullScreen) {
        if (!fullScreen || minecraft == null) {
            return TabletUiFactory.ROOT_W;
        }
        return quantizeFullscreenRootSize(minecraft.getWindow().getGuiScaledWidth(), FULLSCREEN_ROOT_WIDTH_REMAINDER);
    }

    public static int targetRootHeight(Minecraft minecraft, boolean fullScreen) {
        if (!fullScreen || minecraft == null) {
            return TabletUiFactory.ROOT_H;
        }
        return quantizeFullscreenRootSize(minecraft.getWindow().getGuiScaledHeight(), FULLSCREEN_ROOT_HEIGHT_REMAINDER);
    }

    private static int quantizeFullscreenRootSize(int screenSize, int remainder) {
        int safeSize = Math.max(1, screenSize);
        int delta = Math.floorMod(safeSize - remainder, FULLSCREEN_GRID_CELL_SIZE);
        int quantizedSize = safeSize - delta;
        return quantizedSize > 0 ? quantizedSize : safeSize;
    }
}

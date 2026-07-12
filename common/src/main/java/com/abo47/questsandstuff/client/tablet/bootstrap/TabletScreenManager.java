package com.abo47.questsandstuff.client.tablet.bootstrap;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletGuiContainer;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import net.minecraft.client.Minecraft;

public final class TabletScreenManager {
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
        return minecraft.getWindow().getGuiScaledWidth();
    }

    public static int targetRootHeight(Minecraft minecraft, boolean fullScreen) {
        if (!fullScreen || minecraft == null) {
            return TabletUiFactory.ROOT_H;
        }
        return minecraft.getWindow().getGuiScaledHeight();
    }
}

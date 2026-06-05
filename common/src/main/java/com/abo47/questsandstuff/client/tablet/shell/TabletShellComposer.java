package com.abo47.questsandstuff.client.tablet.shell;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_W;

public final class TabletShellComposer {
    private TabletShellComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletAppRouter.create(TabletAppId.QUESTS, player, rootWidth, rootHeight, fullScreenMode);
    }
}

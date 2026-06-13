package com.abo47.questsandstuff.client.tablet.shell;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class TabletShellComposer {
    private TabletShellComposer() {
    }

    public static WidgetGroup create(Player player) {
        return TabletHomeComposer.create(player);
    }

    public static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletHomeComposer.create(player, rootWidth, rootHeight, fullScreenMode);
    }

    public static WidgetGroup createQuests(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletAppRouter.create(TabletAppId.QUESTS, player, rootWidth, rootHeight, fullScreenMode);
    }

    public static WidgetGroup createTeams(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletAppRouter.create(TabletAppId.TEAMS, player, rootWidth, rootHeight, fullScreenMode);
    }
}

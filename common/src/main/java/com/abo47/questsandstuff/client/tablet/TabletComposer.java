package com.abo47.questsandstuff.client.tablet;

import com.abo47.questsandstuff.client.tablet.app.TabletAppRouter;
import com.abo47.questsandstuff.client.tablet.home.TabletHomeComposer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class TabletComposer {
    private TabletComposer() {
    }

    public static WidgetGroup create(Player player) {
        return TabletHomeComposer.create(player);
    }

    public static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletHomeComposer.create(player, rootWidth, rootHeight, fullScreenMode);
    }

    public static WidgetGroup createQuests(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletAppRouter.create("QUESTS", player, rootWidth, rootHeight, fullScreenMode);
    }

    public static WidgetGroup createTeams(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return TabletAppRouter.create("TEAMS", player, rootWidth, rootHeight, fullScreenMode);
    }
}

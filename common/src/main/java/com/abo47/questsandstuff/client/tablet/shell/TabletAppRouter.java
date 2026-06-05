package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.client.tablet.quest.QuestAppComposer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

final class TabletAppRouter {
    private TabletAppRouter() {
    }

    static WidgetGroup create(TabletAppId appId, Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        return switch (appId) {
            case QUESTS -> QuestAppComposer.create(player, rootWidth, rootHeight, fullScreenMode);
        };
    }
}

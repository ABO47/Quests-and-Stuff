package com.abo47.questsandstuff.client.tablet.quest.group;

import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class GroupPanel {
    private GroupPanel() {
    }

    public static void rebuildChapterList(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh) {
        GroupListRenderer.rebuild(chapterList, state, player, refresh);
    }

    public static void rebuildChapterMenu(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        GroupContextMenu.rebuild(overlay, state, player, refresh);
        EntityMotionEditor.renderChapterPanel(overlay, state, player, refresh);
        GroupTextStyleMenu.render(overlay, state, player, refresh);
    }

    public static int chapterMenuWidth(TabletUiState state, int maxAvailableWidth) {
        return GroupContextMenu.width(state, maxAvailableWidth);
    }

    public static boolean clickChapterMenu(TabletUiState state, Player player, Runnable refresh, int x, int y) {
        return GroupContextMenu.click(state, player, refresh, x, y);
    }
}

package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class ChapterPanel {
    private ChapterPanel() {
    }

    public static void rebuildChapterList(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh) {
        ChapterListRenderer.rebuild(chapterList, state, player, refresh);
    }

    public static void rebuildChapterMenu(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        ChapterContextMenu.rebuild(overlay, state, player, refresh);
        ChapterTextStyleMenu.renderFontSizePopover(overlay, state, player, refresh);
        EntityMotionEditor.renderChapterPanel(overlay, state, player, refresh);
    }

    public static int chapterMenuWidth(TabletUiState state, int maxAvailableWidth) {
        return ChapterContextMenu.width(state, maxAvailableWidth);
    }

    public static boolean clickChapterMenu(TabletUiState state, Player player, Runnable refresh, int x, int y) {
        return ChapterContextMenu.click(state, player, refresh, x, y);
    }
}

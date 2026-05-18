package com.abo47.questsandstuff.client.chapter;

import com.abo47.questsandstuff.client.chapter.menu.ChapterContextMenuLayout;
import com.abo47.questsandstuff.client.chapter.menu.ChapterContextMenuRows;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class ChapterContextMenu {
    private ChapterContextMenu() {
    }

    public static void rebuild(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!state.chapterMenuOpen) {
            return;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, overlay.getSize().width, overlay.getSize().height);
        state.chapterMenuX = layout.menuX();
        state.chapterMenuY = layout.menuY();
        WidgetGroup menu = TabletUiFactory.panel(layout.menuX(), layout.menuY(), layout.menuW(), layout.menuH(), TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 246), ModColors.BORDER_BASE);
        ChapterContextMenuRows.addRows(menu, layout, state, player, refresh);
        overlay.addWidget(menu);
    }

    public static int width(TabletUiState state, int maxAvailableWidth) {
        return ChapterContextMenuLayout.width(state, maxAvailableWidth);
    }

    public static boolean click(TabletUiState state, Player player, Runnable refresh, int x, int y) {
        if (!state.chapterMenuOpen) {
            return false;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, TabletUiFactory.ROOT_W, TabletUiFactory.ROOT_H);
        return ChapterContextMenuRows.click(layout, state, player, refresh, x, y);
    }
}

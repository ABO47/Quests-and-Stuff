package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.quest.chapter.menu.ChapterContextMenuLayout;
import com.abo47.questsandstuff.client.tablet.quest.chapter.menu.ChapterContextMenuRows;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class ChapterContextMenu {
    private ChapterContextMenu() {
    }

    public static void rebuild(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!state.chapterPanel.chapterMenuOpen) {
            return;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, overlay.getSize().width, overlay.getSize().height);
        state.chapterPanel.chapterMenuX = layout.menuX();
        state.chapterPanel.chapterMenuY = layout.menuY();
        List<ContextAction> actions = ChapterContextMenuRows.actions(layout, state, player, refresh);
        overlay.addWidget(ContextMenuPanel.build(
                layout.menuX(),
                layout.menuY(),
                layout.menuW(),
                actions,
                0,
                ContextMenuPanel.rowActionCount(actions),
                TabletColors.BORDER_BASE,
                state,
                null,
                ContextMenuAnimationBridge.CHAPTER_KEY
        ));
    }

    public static int width(TabletUiState state, int maxAvailableWidth) {
        return ChapterContextMenuLayout.width(state, maxAvailableWidth);
    }

    public static boolean click(TabletUiState state, Player player, Runnable refresh, int x, int y) {
        if (!state.chapterPanel.chapterMenuOpen) {
            return false;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, TabletStateQueries.rootWidth(state), TabletStateQueries.rootHeight(state));
        return ChapterContextMenuRows.click(layout, state, player, refresh, x, y);
    }
}

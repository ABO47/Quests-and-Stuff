package com.abo47.questsandstuff.client.chapter;

import com.abo47.questsandstuff.client.chapter.menu.ChapterContextMenuLayout;
import com.abo47.questsandstuff.client.chapter.menu.ChapterContextMenuRows;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

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
        List<ContextAction> actions = ChapterContextMenuRows.actions(layout, state, player, refresh);
        overlay.addWidget(ContextMenuPanel.build(
                layout.menuX(),
                layout.menuY(),
                layout.menuW(),
                actions,
                0,
                ContextMenuPanel.rowActionCount(actions),
                ModColors.BORDER_BASE,
                state,
                null,
                ContextMenuAnimation.CHAPTER_KEY
        ));
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

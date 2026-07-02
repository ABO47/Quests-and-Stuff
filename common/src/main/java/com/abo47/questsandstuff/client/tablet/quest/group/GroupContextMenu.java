package com.abo47.questsandstuff.client.tablet.quest.group;

import com.abo47.questsandstuff.client.tablet.quest.group.menu.GroupContextMenuLayout;
import com.abo47.questsandstuff.client.tablet.quest.group.menu.GroupContextMenuRows;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class GroupContextMenu {
    private GroupContextMenu() {
    }

    public static void rebuild(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!state.chapterPanel.chapterMenuOpen) {
            return;
        }
        GroupContextMenuLayout layout = GroupContextMenuLayout.resolve(state, overlay.getSize().width, overlay.getSize().height);
        state.chapterPanel.chapterMenuX = layout.menuX();
        state.chapterPanel.chapterMenuY = layout.menuY();
        List<ContextAction> actions = GroupContextMenuRows.actions(layout, state, player, refresh);
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
        return GroupContextMenuLayout.width(state, maxAvailableWidth);
    }

    public static boolean click(TabletUiState state, Player player, Runnable refresh, int x, int y) {
        if (!state.chapterPanel.chapterMenuOpen) {
            return false;
        }
        GroupContextMenuLayout layout = GroupContextMenuLayout.resolve(state, TabletStateQueries.rootWidth(state), TabletStateQueries.rootHeight(state));
        return GroupContextMenuRows.click(layout, state, player, refresh, x, y);
    }
}

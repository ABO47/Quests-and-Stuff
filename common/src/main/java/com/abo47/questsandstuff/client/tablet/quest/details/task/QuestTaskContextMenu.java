package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.List;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

final class QuestTaskContextMenu {
    private QuestTaskContextMenu() {
    }

    static void render(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId) {
        if (!state.questDetails.questDetailsContextOpen || !QuestDetailsEditController.canEdit(state)) {
            return;
        }
        ContextMenuSections sections = new ContextMenuSections();
        String kind = state.questDetails.questDetailsContextKind == null ? "" : state.questDetails.questDetailsContextKind;
        QuestTaskCreateMenuActions.addSections(sections, state, kind);
        if ("task".equals(kind) && !state.questDetails.questDetailsContextId.isBlank()) {
            QuestTaskMenuActions.addSections(sections, state, player, questId, state.questDetails.questDetailsContextId);
        }
        if ("reward".equals(kind) && !state.questDetails.questDetailsContextId.isBlank()) {
            QuestTaskRewardMenuActions.addSections(sections, state, player, questId, state.questDetails.questDetailsContextId);
        }
        List<ContextAction> actions = sections.build();
        if (actions.isEmpty()) {
            return;
        }
        int menuW = ContextMenuRenderer.CONTEXT_MENU_WIDTH;
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int maxMenuH = Math.max(ContextMenuPanel.heightForRows(1), state.questDetails.questDetailsH - 8);
        while (visibleRows > 1 && ContextMenuPanel.heightFor(actions, visibleRows) > maxMenuH) {
            visibleRows--;
        }
        state.questDetails.questDetailsContextScrollMax = Math.max(0, rowCount - visibleRows);
        state.questDetails.questDetailsContextScroll = ScrollMath.clamp(state.questDetails.questDetailsContextScroll, state.questDetails.questDetailsContextScrollMax);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int x = Math.max(4, Math.min(state.questDetails.questDetailsContextX, state.questDetails.questDetailsW - menuW - 4));
        int y = Math.max(4, Math.min(state.questDetails.questDetailsContextY, state.questDetails.questDetailsH - menuH - 4));
        state.questDetails.questDetailsContextX = x;
        state.questDetails.questDetailsContextY = y;
        state.questDetails.questDetailsContextW = menuW;
        state.questDetails.questDetailsContextH = menuH;
        WidgetGroup menu = ContextMenuPanel.build(x, y, menuW, actions, state.questDetails.questDetailsContextScroll, visibleRows, TabletColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                QuestDetailsTransientManager.closeContext(state);
            }
            refresh.run();
        }, state.questDetails.questDetailsW, state.questDetails.questDetailsH, ScrollState.bind(
                () -> state.questDetails.questDetailsContextScroll,
                value -> state.questDetails.questDetailsContextScroll = ScrollMath.clamp(value, state.questDetails.questDetailsContextScrollMax),
                () -> state.contextMenu.contextMenuScrollDragging,
                dragging -> state.contextMenu.contextMenuScrollDragging = dragging
        ), refresh);
        modal.addWidget(menu);
    }
}

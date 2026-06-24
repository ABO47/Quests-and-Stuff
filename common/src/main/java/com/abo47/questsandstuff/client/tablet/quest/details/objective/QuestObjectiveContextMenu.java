package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuSystem;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

final class QuestObjectiveContextMenu {
    private QuestObjectiveContextMenu() {
    }

    static void render(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId) {
        if (!state.questDetails.questDetailsContextOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        String kind = state.questDetails.questDetailsContextKind == null ? "" : state.questDetails.questDetailsContextKind;
        actions.addAll(QuestObjectiveCreateMenuActions.actions(state, kind));
        if ("requirement".equals(kind) && !state.questDetails.questDetailsContextId.isBlank()) {
            actions.addAll(QuestObjectiveRequirementMenuActions.actions(state, player, questId, state.questDetails.questDetailsContextId));
        }
        if ("reward".equals(kind) && !state.questDetails.questDetailsContextId.isBlank()) {
            actions.addAll(QuestObjectiveRewardMenuActions.actions(state, player, questId, state.questDetails.questDetailsContextId));
        }
        if (actions.isEmpty()) {
            return;
        }
        int menuW = ContextMenuSystem.CONTEXT_MENU_WIDTH;
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int maxMenuH = Math.max(ContextMenuPanel.heightForRows(1), state.questDetails.questDetailsH - 8);
        while (visibleRows > 1 && ContextMenuPanel.heightFor(actions, visibleRows) > maxMenuH) {
            visibleRows--;
        }
        state.questDetails.questDetailsContextScrollMax = Math.max(0, rowCount - visibleRows);
        state.questDetails.questDetailsContextScroll = ScrollController.clamp(state.questDetails.questDetailsContextScroll, state.questDetails.questDetailsContextScrollMax);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int x = Math.max(4, Math.min(state.questDetails.questDetailsContextX, state.questDetails.questDetailsW - menuW - 4));
        int y = Math.max(4, Math.min(state.questDetails.questDetailsContextY, state.questDetails.questDetailsH - menuH - 4));
        state.questDetails.questDetailsContextX = x;
        state.questDetails.questDetailsContextY = y;
        state.questDetails.questDetailsContextW = menuW;
        state.questDetails.questDetailsContextH = menuH;
        WidgetGroup menu = ContextMenuPanel.build(x, y, menuW, actions, state.questDetails.questDetailsContextScroll, visibleRows, ModColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                QuestDetailsTransientState.closeContext(state);
            }
            refresh.run();
        }, state.questDetails.questDetailsW, state.questDetails.questDetailsH, ScrollState.bind(
                () -> state.questDetails.questDetailsContextScroll,
                value -> state.questDetails.questDetailsContextScroll = ScrollController.clamp(value, state.questDetails.questDetailsContextScrollMax),
                () -> state.contextMenu.contextMenuScrollDragging,
                dragging -> state.contextMenu.contextMenuScrollDragging = dragging
        ), refresh);
        modal.addWidget(menu);
    }
}

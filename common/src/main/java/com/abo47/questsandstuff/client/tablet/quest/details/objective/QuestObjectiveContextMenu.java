package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
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
        if (!state.questDetailsContextOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        String kind = state.questDetailsContextKind == null ? "" : state.questDetailsContextKind;
        actions.addAll(QuestObjectiveCreateMenuActions.actions(state, kind));
        if ("requirement".equals(kind) && !state.questDetailsContextId.isBlank()) {
            actions.addAll(QuestObjectiveRequirementMenuActions.actions(state, player, questId, state.questDetailsContextId));
        }
        if ("reward".equals(kind) && !state.questDetailsContextId.isBlank()) {
            actions.addAll(QuestObjectiveRewardMenuActions.actions(state, player, questId, state.questDetailsContextId));
        }
        if (actions.isEmpty()) {
            return;
        }
        int menuW = 140;
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int maxMenuH = Math.max(ContextMenuPanel.heightForRows(1), state.questDetailsH - 8);
        while (visibleRows > 1 && ContextMenuPanel.heightFor(actions, visibleRows) > maxMenuH) {
            visibleRows--;
        }
        state.questDetailsContextScrollMax = Math.max(0, rowCount - visibleRows);
        state.questDetailsContextScroll = ScrollController.clamp(state.questDetailsContextScroll, state.questDetailsContextScrollMax);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int x = Math.max(4, Math.min(state.questDetailsContextX, state.questDetailsW - menuW - 4));
        int y = Math.max(4, Math.min(state.questDetailsContextY, state.questDetailsH - menuH - 4));
        state.questDetailsContextX = x;
        state.questDetailsContextY = y;
        state.questDetailsContextW = menuW;
        state.questDetailsContextH = menuH;
        WidgetGroup menu = ContextMenuPanel.build(x, y, menuW, actions, state.questDetailsContextScroll, visibleRows, ModColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                QuestDetailsTransientState.closeContext(state);
            }
            refresh.run();
        }, state.questDetailsW, state.questDetailsH, ScrollState.bind(
                () -> state.questDetailsContextScroll,
                value -> state.questDetailsContextScroll = ScrollController.clamp(value, state.questDetailsContextScrollMax),
                () -> state.contextMenuScrollDragging,
                dragging -> state.contextMenuScrollDragging = dragging
        ), refresh);
        modal.addWidget(menu);
    }
}

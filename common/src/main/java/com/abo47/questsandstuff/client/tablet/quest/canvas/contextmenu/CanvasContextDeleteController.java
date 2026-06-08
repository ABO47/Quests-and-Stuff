package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;


import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runPrerequisiteAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runRemoveQuestAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;

public final class CanvasContextDeleteController {
    private CanvasContextDeleteController() {
    }

    public static boolean canDeleteContext(TabletUiState state) {
        if (state.contextMenuTarget == ContextMenuTarget.EDGE) {
            return !state.contextEdgeSource.isBlank() && !state.contextEdgeTarget.isBlank();
        }
        if (state.contextMenuTarget == ContextMenuTarget.QUEST) {
            return !state.contextQuestId.isBlank();
        }
        if (state.contextMenuTarget == ContextMenuTarget.SELECTION) {
            return CanvasRenderer.totalCanvasSelectionCount(state) > 0;
        }
        if (state.contextMenuTarget == ContextMenuTarget.IMAGE) {
            return !state.contextCanvasImageId.isBlank();
        }
        if (state.contextMenuTarget == ContextMenuTarget.TEXT) {
            return !state.contextCanvasTextId.isBlank();
        }
        return false;
    }

    public static String deleteConfirmKey(TabletUiState state) {
        return state.contextMenuTarget.name()
                + "|" + state.contextQuestId
                + "|" + state.contextEdgeSource
                + "|" + state.contextEdgeTarget
                + "|" + state.contextCanvasImageId
                + "|" + state.contextCanvasTextId
                + "|" + String.join(",", state.selectedQuestIds)
                + "|" + String.join(",", CanvasRenderer.selectedCanvasImageIds(state))
                + "|" + String.join(",", CanvasRenderer.selectedCanvasTextIds(state));
    }

    public static void runDeleteAction(Player player, TabletUiState state) {
        String group = selectedGroupName(state);
        if (state.contextMenuTarget == ContextMenuTarget.SELECTION) {
            for (String imageId : CanvasRenderer.selectedCanvasImageIds(state)) {
                boolean removed = CanvasRenderer.removeCanvasImage(state, group, imageId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas image delete group={} id={} removed={}", group, imageId, removed);
            }
            for (String textId : CanvasRenderer.selectedCanvasTextIds(state)) {
                boolean removed = CanvasRenderer.removeCanvasText(state, group, textId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas text delete group={} id={} removed={}", group, textId, removed);
            }
        }

        if (state.contextMenuTarget == ContextMenuTarget.IMAGE && !state.contextCanvasImageId.isBlank()) {
            boolean removed = CanvasRenderer.removeCanvasImage(state, group, state.contextCanvasImageId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas image delete group={} id={} removed={}", group, state.contextCanvasImageId, removed);
            state.contextCanvasImageId = "";
            return;
        }
        if (state.contextMenuTarget == ContextMenuTarget.TEXT && !state.contextCanvasTextId.isBlank()) {
            boolean removed = CanvasRenderer.removeCanvasText(state, group, state.contextCanvasTextId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas text delete group={} id={} removed={}", group, state.contextCanvasTextId, removed);
            state.contextCanvasTextId = "";
            return;
        }

        if (state.contextMenuTarget == ContextMenuTarget.EDGE
                && !state.contextEdgeSource.isBlank()
                && !state.contextEdgeTarget.isBlank()) {
            runPrerequisiteAction(player, state.contextEdgeTarget, state.contextEdgeSource, false);
            return;
        }

        Set<String> questIds = new LinkedHashSet<>();
        if (state.contextMenuTarget == ContextMenuTarget.QUEST && !state.contextQuestId.isBlank()) {
            questIds.add(state.contextQuestId);
        } else {
            questIds.addAll(state.selectedQuestIds);
        }
        if (questIds.isEmpty()) {
            return;
        }
        for (String questId : questIds) {
            runRemoveQuestAction(player, questId);
        }
        state.selectedQuestIds.removeAll(questIds);
        if (!state.connectSourceQuestId.isBlank() && questIds.contains(state.connectSourceQuestId)) {
            state.connectSourceQuestId = "";
        }
        state.connectSourceQuestIds.removeAll(questIds);
        if (!state.lastJumpQuest.isBlank() && questIds.contains(state.lastJumpQuest)) {
            state.lastJumpQuest = "";
        }
    }
}

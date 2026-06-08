package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;



import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
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
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.EDGE) {
            return !state.contextMenu.contextEdgeSource.isBlank() && !state.contextMenu.contextEdgeTarget.isBlank();
        }
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.QUEST) {
            return !state.contextMenu.contextQuestId.isBlank();
        }
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.SELECTION) {
            return CanvasSelectionActions.totalCanvasSelectionCount(state) > 0;
        }
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.IMAGE) {
            return !state.contextMenu.contextCanvasImageId.isBlank();
        }
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.TEXT) {
            return !state.contextMenu.contextCanvasTextId.isBlank();
        }
        return false;
    }

    public static String deleteConfirmKey(TabletUiState state) {
        return state.contextMenu.contextMenuTarget.name()
                + "|" + state.contextMenu.contextQuestId
                + "|" + state.contextMenu.contextEdgeSource
                + "|" + state.contextMenu.contextEdgeTarget
                + "|" + state.contextMenu.contextCanvasImageId
                + "|" + state.contextMenu.contextCanvasTextId
                + "|" + String.join(",", state.canvas.canvasSelection.questIds())
                + "|" + String.join(",", CanvasSelectionActions.selectedImageIds(state))
                + "|" + String.join(",", CanvasSelectionActions.selectedTextIds(state));
    }

    public static void runDeleteAction(Player player, TabletUiState state) {
        String group = selectedGroupName(state);
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.SELECTION) {
            for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
                boolean removed = CanvasLayerMutations.removeCanvasImage(state, group, imageId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas image delete group={} id={} removed={}", group, imageId, removed);
            }
            for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
                boolean removed = CanvasLayerMutations.removeCanvasText(state, group, textId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas text delete group={} id={} removed={}", group, textId, removed);
            }
        }

        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.IMAGE && !state.contextMenu.contextCanvasImageId.isBlank()) {
            boolean removed = CanvasLayerMutations.removeCanvasImage(state, group, state.contextMenu.contextCanvasImageId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas image delete group={} id={} removed={}", group, state.contextMenu.contextCanvasImageId, removed);
            ContextMenuState.clearTarget(state);
            return;
        }
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.TEXT && !state.contextMenu.contextCanvasTextId.isBlank()) {
            boolean removed = CanvasLayerMutations.removeCanvasText(state, group, state.contextMenu.contextCanvasTextId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas text delete group={} id={} removed={}", group, state.contextMenu.contextCanvasTextId, removed);
            ContextMenuState.clearTarget(state);
            return;
        }

        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.EDGE
                && !state.contextMenu.contextEdgeSource.isBlank()
                && !state.contextMenu.contextEdgeTarget.isBlank()) {
            runPrerequisiteAction(player, state.contextMenu.contextEdgeTarget, state.contextMenu.contextEdgeSource, false);
            return;
        }

        Set<String> questIds = new LinkedHashSet<>();
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.QUEST && !state.contextMenu.contextQuestId.isBlank()) {
            questIds.add(state.contextMenu.contextQuestId);
        } else {
            questIds.addAll(state.canvas.canvasSelection.questIds());
        }
        if (questIds.isEmpty()) {
            return;
        }
        for (String questId : questIds) {
            runRemoveQuestAction(player, questId);
        }
        state.canvas.canvasSelection.questIds().removeAll(questIds);
        if (!state.canvas.connectSourceQuestId.isBlank() && questIds.contains(state.canvas.connectSourceQuestId)) {
            state.canvas.connectSourceQuestId = "";
        }
        state.canvas.connectSourceQuestIds.removeAll(questIds);
        if (!state.chapterPanel.lastJumpQuest.isBlank() && questIds.contains(state.chapterPanel.lastJumpQuest)) {
            state.chapterPanel.lastJumpQuest = "";
        }
    }
}

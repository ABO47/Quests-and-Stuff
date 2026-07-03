package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.runPrerequisiteAction;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.runRemoveQuestAction;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

public final class CanvasContextDeleteController {
    private CanvasContextDeleteController() {
    }

    public static boolean canDeleteContext(TabletUiState state) {
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.CONNECTION) {
            return !state.contextMenu.contextConnectionSource.isBlank() && !state.contextMenu.contextConnectionTarget.isBlank();
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
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.EXCLUSIVE_CHOICE) {
            return !state.contextMenu.contextCanvasExclusiveChoiceId.isBlank();
        }
        return false;
    }

    public static String deleteConfirmKey(TabletUiState state) {
        return state.contextMenu.contextMenuTarget.name()
                + "|" + state.contextMenu.contextQuestId
                + "|" + state.contextMenu.contextConnectionSource
                + "|" + state.contextMenu.contextConnectionTarget
                + "|" + state.contextMenu.contextCanvasImageId
                + "|" + state.contextMenu.contextCanvasTextId
                + "|" + state.contextMenu.contextCanvasExclusiveChoiceId
                + "|" + String.join(",", state.canvas.canvasSelection.questIds())
                + "|" + String.join(",", CanvasSelectionActions.selectedImageIds(state))
                + "|" + String.join(",", CanvasSelectionActions.selectedTextIds(state))
                + "|" + String.join(",", CanvasSelectionActions.selectedEcIds(state));
    }

    public static void runDeleteAction(Player player, TabletUiState state) {
        String chapter = selectedChapterName(state);
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.SELECTION) {
            for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
                boolean removed = CanvasLayerMutations.removeCanvasImage(state, chapter, imageId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas image delete chapter={} id={} removed={}", chapter, imageId, removed);
            }
            for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
                boolean removed = CanvasLayerMutations.removeCanvasText(state, chapter, textId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas text delete chapter={} id={} removed={}", chapter, textId, removed);
            }
            for (String ecId : CanvasSelectionActions.selectedEcIds(state)) {
                boolean removed = CanvasLayerMutations.removeCanvasExclusiveChoice(state, chapter, ecId);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas exclusive choice delete chapter={} id={} removed={}", chapter, ecId, removed);
            }
        }

        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.IMAGE && !state.contextMenu.contextCanvasImageId.isBlank()) {
            boolean removed = CanvasLayerMutations.removeCanvasImage(state, chapter, state.contextMenu.contextCanvasImageId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas image delete chapter={} id={} removed={}", chapter, state.contextMenu.contextCanvasImageId, removed);
            ContextMenuController.clearTarget(state);
            return;
        }
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.TEXT && !state.contextMenu.contextCanvasTextId.isBlank()) {
            boolean removed = CanvasLayerMutations.removeCanvasText(state, chapter, state.contextMenu.contextCanvasTextId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas text delete chapter={} id={} removed={}", chapter, state.contextMenu.contextCanvasTextId, removed);
            ContextMenuController.clearTarget(state);
            return;
        }
        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.EXCLUSIVE_CHOICE && !state.contextMenu.contextCanvasExclusiveChoiceId.isBlank()) {
            boolean removed = CanvasLayerMutations.removeCanvasExclusiveChoice(state, chapter, state.contextMenu.contextCanvasExclusiveChoiceId);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas exclusive choice delete chapter={} id={} removed={}", chapter, state.contextMenu.contextCanvasExclusiveChoiceId, removed);
            ContextMenuController.clearTarget(state);
            return;
        }

        if (state.contextMenu.contextMenuTarget == ContextMenuTarget.CONNECTION
                && !state.contextMenu.contextConnectionSource.isBlank()
                && !state.contextMenu.contextConnectionTarget.isBlank()) {
            String source = state.contextMenu.contextConnectionSource;
            String target = state.contextMenu.contextConnectionTarget;
            CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, source);
            if (ec != null) {
                CanvasExclusiveChoice updated = ec.removeAllEdgeState(target);
                CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, updated);
                CanvasLayerMutations.persistCanvasExclusiveChoice(state, chapter, updated.id());
                ConnectionRenderer.removeConnectionTransientState(state, chapter, source, target);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec connection delete ec={} quest={}", source, target);
            } else {
                CanvasExclusiveChoice ecTarget = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, target);
                if (ecTarget != null) {
                    CanvasExclusiveChoice updated = ecTarget.removeAllEdgeState(source);
                    CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, updated);
                    CanvasLayerMutations.persistCanvasExclusiveChoice(state, chapter, updated.id());
                    ConnectionRenderer.removeConnectionTransientState(state, chapter, source, target);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec prerequisite delete quest={} ec={}", source, target);
                } else {
                    runPrerequisiteAction(player, target, source, false);
                    ConnectionRenderer.removeConnectionTransientState(state, chapter, source, target);
                }
            }
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

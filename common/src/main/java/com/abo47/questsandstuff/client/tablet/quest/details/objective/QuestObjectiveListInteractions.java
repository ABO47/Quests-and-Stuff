package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.CardReorderController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class QuestObjectiveListInteractions {
    private QuestObjectiveListInteractions() {
    }

    static void select(TabletUiState state, String kind, String id) {
        state.questDetails.questDetailsSelectedObjectiveKind = "requirements".equals(kind) ? "requirement" : kind;
        state.questDetails.questDetailsSelectedObjectiveKind = "rewards".equals(kind) ? "reward" : state.questDetails.questDetailsSelectedObjectiveKind;
        state.questDetails.questDetailsSelectedObjectiveId = id == null ? "" : id;
    }

    static boolean clearSelection(TabletUiState state, String reason) {
        if (state == null) {
            return false;
        }
        boolean hadSelection = !state.questDetails.questDetailsSelectedObjectiveKind.isBlank()
                || !state.questDetails.questDetailsSelectedObjectiveId.isBlank();
        boolean hadDrag = state.questDetails.questDetailsObjectiveDragPending
                || state.questDetails.questDetailsObjectiveDragActive
                || !state.questDetails.questDetailsObjectiveDragKind.isBlank()
                || !state.questDetails.questDetailsObjectiveDragId.isBlank();
        boolean hadRename = state.questDetails.questDetailsObjectiveRenameOpen;
        if (!hadSelection && !hadDrag && !hadRename) {
            return false;
        }
        state.questDetails.questDetailsSelectedObjectiveKind = "";
        state.questDetails.questDetailsSelectedObjectiveId = "";
        ContextMenuState.clearDeleteConfirm(state);
        clearDrag(state);
        QuestDetailsTransientState.closeObjectiveRename(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] objective selection cleared reason={}", reason == null ? "" : reason);
        return true;
    }

    static void selectAndBeginDrag(TabletUiState state, String kind, String id, double mouseX, double mouseY) {
        select(state, kind, id);
        state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId("");
        state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId("");
        state.questDetails.questDetailsDescriptionSelection.textIds().clear();
        state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
        state.questDetails.questDetailsObjectiveDragPending = true;
        state.questDetails.questDetailsObjectiveDragActive = false;
        state.questDetails.questDetailsObjectiveDragKind = ("requirement".equals(kind) || "requirements".equals(kind)) ? "requirements" : "rewards";
        state.questDetails.questDetailsObjectiveDragId = id;
        state.questDetails.questDetailsObjectiveDragStartX = (int) Math.round(mouseX);
        state.questDetails.questDetailsObjectiveDragStartY = (int) Math.round(mouseY);
        state.questDetails.questDetailsObjectiveDragTargetIndex = -1;
    }

    static boolean handleDrag(Player player, TabletUiState state, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind, int listY, int listBottom, int localY, double mouseX, double mouseY, int button) {
        if (!QuestDetailsEditState.canEdit(state)) {
            clearDrag(state);
            return false;
        }
        if (!state.questDetails.questDetailsObjectiveDragKind.equals(kind)) {
            return false;
        }
        if (state.questDetails.questDetailsObjectiveDragActive) {
            int next = objectiveInsertIndexAtY(state, entries, kind, listY, listBottom, localY);
            if (next != state.questDetails.questDetailsObjectiveDragTargetIndex) {
                state.questDetails.questDetailsObjectiveDragTargetIndex = next;
                refresh.run();
            }
            return true;
        }
        if (state.questDetails.questDetailsObjectiveDragPending && button == 0) {
            if (!CardReorderController.pastDragThreshold(mouseX, mouseY, state.questDetails.questDetailsObjectiveDragStartX, state.questDetails.questDetailsObjectiveDragStartY)) {
                return true;
            }
            state.questDetails.questDetailsObjectiveDragPending = false;
            state.questDetails.questDetailsObjectiveDragActive = true;
            state.questDetails.questDetailsObjectiveDragTargetIndex = objectiveInsertIndexAtY(state, entries, kind, listY, listBottom, localY);
            refresh.run();
            return true;
        }
        return false;
    }

    static boolean handleRelease(Player player, TabletUiState state, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind) {
        if (!QuestDetailsEditState.canEdit(state)) {
            clearDrag(state);
            return false;
        }
        if (state.questDetails.questDetailsObjectiveDragActive && state.questDetails.questDetailsObjectiveDragKind.equals(kind)) {
            finishDrag(player, state, questId, entries);
            refresh.run();
            return true;
        }
        if (state.questDetails.questDetailsObjectiveDragPending && state.questDetails.questDetailsObjectiveDragKind.equals(kind)) {
            clearDrag(state);
            refresh.run();
            return true;
        }
        return false;
    }

    static void openRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        if (!QuestDetailsEditState.canEdit(state)) {
            return;
        }
        QuestObjectiveEditActions.openObjectiveRenameEditor(state, questId, id, task);
    }

    static boolean deleteSelected(Player player, TabletUiState state, String questId) {
        if (!QuestDetailsEditState.canEdit(state) || state.questDetails.questDetailsSelectedObjectiveId.isBlank()) {
            return false;
        }
        boolean task = "requirement".equals(state.questDetails.questDetailsSelectedObjectiveKind);
        if (task) {
            EditorQuestCommandClient.removeQuestTask(player, questId, state.questDetails.questDetailsSelectedObjectiveId);
        } else if ("reward".equals(state.questDetails.questDetailsSelectedObjectiveKind)) {
            EditorQuestCommandClient.removeQuestReward(player, questId, state.questDetails.questDetailsSelectedObjectiveId);
        } else {
            return false;
        }
        state.questDetails.questDetailsSelectedObjectiveKind = "";
        state.questDetails.questDetailsSelectedObjectiveId = "";
        clearDrag(state);
        return true;
    }

    static boolean moveSelected(Player player, TabletUiState state, String questId, int offset) {
        if (!QuestDetailsEditState.canEdit(state) || state.questDetails.questDetailsSelectedObjectiveId.isBlank() || offset == 0) {
            return false;
        }
        if ("requirement".equals(state.questDetails.questDetailsSelectedObjectiveKind)) {
            EditorQuestCommandClient.moveQuestTask(player, questId, state.questDetails.questDetailsSelectedObjectiveId, offset);
            return true;
        }
        if ("reward".equals(state.questDetails.questDetailsSelectedObjectiveKind)) {
            EditorQuestCommandClient.moveQuestReward(player, questId, state.questDetails.questDetailsSelectedObjectiveId, offset);
            return true;
        }
        return false;
    }

    static void clearDrag(TabletUiState state) {
        state.questDetails.questDetailsObjectiveDragPending = false;
        state.questDetails.questDetailsObjectiveDragActive = false;
        state.questDetails.questDetailsObjectiveDragKind = "";
        state.questDetails.questDetailsObjectiveDragId = "";
        state.questDetails.questDetailsObjectiveDragTargetIndex = -1;
    }

    private static int objectiveInsertIndexAtY(TabletUiState state, List<QuestDetailsObjectiveEntry> entries, String kind, int listY, int listBottom, int localY) {
        int scroll = "requirements".equals(kind) ? state.questDetails.questDetailsReqScroll : state.questDetails.questDetailsRewardScroll;
        return CardReorderController.insertIndexAtY(localY, listY, listBottom, scroll, QuestDetailsObjectivesPanel.LIST_PAD, QuestDetailsObjectivesPanel.CARD_H, QuestDetailsObjectivesPanel.CARD_GAP, entries.size());
    }

    private static void finishDrag(Player player, TabletUiState state, String questId, List<QuestDetailsObjectiveEntry> entries) {
        String moving = state.questDetails.questDetailsObjectiveDragId;
        int target = Math.max(0, state.questDetails.questDetailsObjectiveDragTargetIndex);
        boolean requirement = "requirements".equals(state.questDetails.questDetailsObjectiveDragKind);
        clearDrag(state);
        if (moving.isBlank()) {
            return;
        }
        int offset = CardReorderController.offsetForDrop(moving, entries, QuestDetailsObjectiveEntry::id, target);
        if (offset == 0) {
            return;
        }
        if (requirement) {
            EditorQuestCommandClient.moveQuestTask(player, questId, moving, offset);
        } else {
            EditorQuestCommandClient.moveQuestReward(player, questId, moving, offset);
        }
    }
}

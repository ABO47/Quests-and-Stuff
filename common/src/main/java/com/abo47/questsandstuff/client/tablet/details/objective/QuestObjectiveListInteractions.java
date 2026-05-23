package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.CardReorderController;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class QuestObjectiveListInteractions {
    private QuestObjectiveListInteractions() {
    }

    static void select(TabletUiState state, String kind, String id) {
        state.questDetailsSelectedObjectiveKind = "requirements".equals(kind) ? "requirement" : kind;
        state.questDetailsSelectedObjectiveKind = "rewards".equals(kind) ? "reward" : state.questDetailsSelectedObjectiveKind;
        state.questDetailsSelectedObjectiveId = id == null ? "" : id;
    }

    static boolean clearSelection(TabletUiState state, String reason) {
        if (state == null) {
            return false;
        }
        boolean hadSelection = !state.questDetailsSelectedObjectiveKind.isBlank()
                || !state.questDetailsSelectedObjectiveId.isBlank();
        boolean hadDrag = state.questDetailsObjectiveDragPending
                || state.questDetailsObjectiveDragActive
                || !state.questDetailsObjectiveDragKind.isBlank()
                || !state.questDetailsObjectiveDragId.isBlank();
        boolean hadRename = state.questDetailsObjectiveRenameOpen;
        if (!hadSelection && !hadDrag && !hadRename) {
            return false;
        }
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        state.contextDeleteConfirmKey = "";
        clearDrag(state);
        QuestDetailsTransientState.closeObjectiveRename(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] objective selection cleared reason={}", reason == null ? "" : reason);
        return true;
    }

    static void selectAndBeginDrag(TabletUiState state, String kind, String id, double mouseX, double mouseY) {
        select(state, kind, id);
        state.questDetailsSelectedTextId = "";
        state.questDetailsSelectedImageId = "";
        state.questDetailsSelectedTextIds.clear();
        state.questDetailsSelectedImageIds.clear();
        state.questDetailsObjectiveDragPending = true;
        state.questDetailsObjectiveDragActive = false;
        state.questDetailsObjectiveDragKind = ("requirement".equals(kind) || "requirements".equals(kind)) ? "requirements" : "rewards";
        state.questDetailsObjectiveDragId = id;
        state.questDetailsObjectiveDragStartX = (int) Math.round(mouseX);
        state.questDetailsObjectiveDragStartY = (int) Math.round(mouseY);
        state.questDetailsObjectiveDragTargetIndex = -1;
    }

    static boolean handleDrag(Player player, TabletUiState state, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind, int listY, int listBottom, int localY, double mouseX, double mouseY, int button) {
        if (!QuestDetailsEditState.canEdit(state)) {
            clearDrag(state);
            return false;
        }
        if (!state.questDetailsObjectiveDragKind.equals(kind)) {
            return false;
        }
        if (state.questDetailsObjectiveDragActive) {
            int next = objectiveInsertIndexAtY(state, entries, kind, listY, listBottom, localY);
            if (next != state.questDetailsObjectiveDragTargetIndex) {
                state.questDetailsObjectiveDragTargetIndex = next;
                refresh.run();
            }
            return true;
        }
        if (state.questDetailsObjectiveDragPending && button == 0) {
            if (!CardReorderController.pastDragThreshold(mouseX, mouseY, state.questDetailsObjectiveDragStartX, state.questDetailsObjectiveDragStartY)) {
                return true;
            }
            state.questDetailsObjectiveDragPending = false;
            state.questDetailsObjectiveDragActive = true;
            state.questDetailsObjectiveDragTargetIndex = objectiveInsertIndexAtY(state, entries, kind, listY, listBottom, localY);
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
        if (state.questDetailsObjectiveDragActive && state.questDetailsObjectiveDragKind.equals(kind)) {
            finishDrag(player, state, questId, entries);
            refresh.run();
            return true;
        }
        if (state.questDetailsObjectiveDragPending && state.questDetailsObjectiveDragKind.equals(kind)) {
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
        if (!QuestDetailsEditState.canEdit(state) || state.questDetailsSelectedObjectiveId.isBlank()) {
            return false;
        }
        boolean task = "requirement".equals(state.questDetailsSelectedObjectiveKind);
        if (task) {
            EditorCommandClient.removeQuestTask(player, questId, state.questDetailsSelectedObjectiveId);
        } else if ("reward".equals(state.questDetailsSelectedObjectiveKind)) {
            EditorCommandClient.removeQuestReward(player, questId, state.questDetailsSelectedObjectiveId);
        } else {
            return false;
        }
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        clearDrag(state);
        return true;
    }

    static boolean moveSelected(Player player, TabletUiState state, String questId, int offset) {
        if (!QuestDetailsEditState.canEdit(state) || state.questDetailsSelectedObjectiveId.isBlank() || offset == 0) {
            return false;
        }
        if ("requirement".equals(state.questDetailsSelectedObjectiveKind)) {
            EditorCommandClient.moveQuestTask(player, questId, state.questDetailsSelectedObjectiveId, offset);
            return true;
        }
        if ("reward".equals(state.questDetailsSelectedObjectiveKind)) {
            EditorCommandClient.moveQuestReward(player, questId, state.questDetailsSelectedObjectiveId, offset);
            return true;
        }
        return false;
    }

    static void clearDrag(TabletUiState state) {
        state.questDetailsObjectiveDragPending = false;
        state.questDetailsObjectiveDragActive = false;
        state.questDetailsObjectiveDragKind = "";
        state.questDetailsObjectiveDragId = "";
        state.questDetailsObjectiveDragTargetIndex = -1;
    }

    private static int objectiveInsertIndexAtY(TabletUiState state, List<QuestDetailsObjectiveEntry> entries, String kind, int listY, int listBottom, int localY) {
        int scroll = "requirements".equals(kind) ? state.questDetailsReqScroll : state.questDetailsRewardScroll;
        return CardReorderController.insertIndexAtY(localY, listY, listBottom, scroll, QuestDetailsObjectivesPanel.LIST_PAD, QuestDetailsObjectivesPanel.CARD_H, QuestDetailsObjectivesPanel.CARD_GAP, entries.size());
    }

    private static void finishDrag(Player player, TabletUiState state, String questId, List<QuestDetailsObjectiveEntry> entries) {
        String moving = state.questDetailsObjectiveDragId;
        int target = Math.max(0, state.questDetailsObjectiveDragTargetIndex);
        boolean requirement = "requirements".equals(state.questDetailsObjectiveDragKind);
        clearDrag(state);
        if (moving.isBlank()) {
            return;
        }
        int offset = CardReorderController.offsetForDrop(moving, entries, QuestDetailsObjectiveEntry::id, target);
        if (offset == 0) {
            return;
        }
        if (requirement) {
            EditorCommandClient.moveQuestTask(player, questId, moving, offset);
        } else {
            EditorCommandClient.moveQuestReward(player, questId, moving, offset);
        }
    }
}

package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.List;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.CardDragSortUtil;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class QuestTaskListInteractions {
    private QuestTaskListInteractions() {
    }

    static void select(TabletUiState state, String kind, String id) {
        state.questDetails.questDetailsSelectedTaskKind = "tasks".equals(kind) ? "task" : kind;
        state.questDetails.questDetailsSelectedTaskKind = "rewards".equals(kind) ? "reward" : state.questDetails.questDetailsSelectedTaskKind;
        state.questDetails.questDetailsSelectedTaskId = id == null ? "" : id;
    }

    static boolean clearSelection(TabletUiState state, String reason) {
        if (state == null) {
            return false;
        }
        boolean hadSelection = !state.questDetails.questDetailsSelectedTaskKind.isBlank()
                || !state.questDetails.questDetailsSelectedTaskId.isBlank();
        boolean hadDrag = state.questDetails.questDetailsTaskDragPending
                || state.questDetails.questDetailsTaskDragActive
                || !state.questDetails.questDetailsTaskDragKind.isBlank()
                || !state.questDetails.questDetailsTaskDragId.isBlank();
        boolean hadRename = state.questDetails.questDetailsTaskRenameOpen;
        if (!hadSelection && !hadDrag && !hadRename) {
            return false;
        }
        state.questDetails.questDetailsSelectedTaskKind = "";
        state.questDetails.questDetailsSelectedTaskId = "";
        ContextMenuController.clearDeleteConfirm(state);
        clearDrag(state);
        QuestDetailsTransientManager.closeTaskRename(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] task selection cleared reason={}", reason == null ? "" : reason);
        return true;
    }

    static void selectAndBeginDrag(TabletUiState state, String kind, String id, double mouseX, double mouseY) {
        select(state, kind, id);
        state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId("");
        state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId("");
        state.questDetails.questDetailsDescriptionSelection.textIds().clear();
        state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
        state.questDetails.questDetailsTaskDragPending = true;
        state.questDetails.questDetailsTaskDragActive = false;
        state.questDetails.questDetailsTaskDragKind = ("task".equals(kind) || "tasks".equals(kind)) ? "tasks" : "rewards";
        state.questDetails.questDetailsTaskDragId = id;
        state.questDetails.questDetailsTaskDragStartX = (int) Math.round(mouseX);
        state.questDetails.questDetailsTaskDragStartY = (int) Math.round(mouseY);
        state.questDetails.questDetailsTaskDragTargetIndex = -1;
    }

    static boolean handleDrag(Player player, TabletUiState state, Runnable refresh, String questId, List<QuestDetailsTaskEntry> entries, String kind, int listY, int listBottom, int localY, double mouseX, double mouseY, int button) {
        if (!QuestDetailsEditController.canEdit(state)) {
            clearDrag(state);
            return false;
        }
        if (!state.questDetails.questDetailsTaskDragKind.equals(kind)) {
            return false;
        }
        if (state.questDetails.questDetailsTaskDragActive) {
            int next = taskInsertIndexAtY(state, entries, kind, listY, listBottom, localY);
            if (next != state.questDetails.questDetailsTaskDragTargetIndex) {
                state.questDetails.questDetailsTaskDragTargetIndex = next;
                refresh.run();
            }
            return true;
        }
        if (state.questDetails.questDetailsTaskDragPending && button == 0) {
            if (!CardDragSortUtil.pastDragThreshold(mouseX, mouseY, state.questDetails.questDetailsTaskDragStartX, state.questDetails.questDetailsTaskDragStartY)) {
                return true;
            }
            state.questDetails.questDetailsTaskDragPending = false;
            state.questDetails.questDetailsTaskDragActive = true;
            state.questDetails.questDetailsTaskDragTargetIndex = taskInsertIndexAtY(state, entries, kind, listY, listBottom, localY);
            refresh.run();
            return true;
        }
        return false;
    }

    static boolean handleRelease(Player player, TabletUiState state, Runnable refresh, String questId, List<QuestDetailsTaskEntry> entries, String kind) {
        if (!QuestDetailsEditController.canEdit(state)) {
            clearDrag(state);
            return false;
        }
        if (state.questDetails.questDetailsTaskDragActive && state.questDetails.questDetailsTaskDragKind.equals(kind)) {
            finishDrag(player, state, questId, entries);
            refresh.run();
            return true;
        }
        if (state.questDetails.questDetailsTaskDragPending && state.questDetails.questDetailsTaskDragKind.equals(kind)) {
            clearDrag(state);
            refresh.run();
            return true;
        }
        return false;
    }

    static void openRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        if (!QuestDetailsEditController.canEdit(state)) {
            return;
        }
        QuestTaskEditActions.openTaskRenameEditor(state, questId, id, task);
    }

    static boolean deleteSelected(Player player, TabletUiState state, String questId) {
        if (!QuestDetailsEditController.canEdit(state) || state.questDetails.questDetailsSelectedTaskId.isBlank()) {
            return false;
        }
        boolean task = "task".equals(state.questDetails.questDetailsSelectedTaskKind);
        if (task) {
            EditorQuestCommandClient.removeQuestTask(player, questId, state.questDetails.questDetailsSelectedTaskId);
        } else if ("reward".equals(state.questDetails.questDetailsSelectedTaskKind)) {
            EditorQuestCommandClient.removeQuestReward(player, questId, state.questDetails.questDetailsSelectedTaskId);
        } else {
            return false;
        }
        state.questDetails.questDetailsSelectedTaskKind = "";
        state.questDetails.questDetailsSelectedTaskId = "";
        clearDrag(state);
        return true;
    }

    static boolean moveSelected(Player player, TabletUiState state, String questId, int offset) {
        if (!QuestDetailsEditController.canEdit(state) || state.questDetails.questDetailsSelectedTaskId.isBlank() || offset == 0) {
            return false;
        }
        if ("task".equals(state.questDetails.questDetailsSelectedTaskKind)) {
            EditorQuestCommandClient.moveQuestTask(player, questId, state.questDetails.questDetailsSelectedTaskId, offset);
            return true;
        }
        if ("reward".equals(state.questDetails.questDetailsSelectedTaskKind)) {
            EditorQuestCommandClient.moveQuestReward(player, questId, state.questDetails.questDetailsSelectedTaskId, offset);
            return true;
        }
        return false;
    }

    static void clearDrag(TabletUiState state) {
        state.questDetails.questDetailsTaskDragPending = false;
        state.questDetails.questDetailsTaskDragActive = false;
        state.questDetails.questDetailsTaskDragKind = "";
        state.questDetails.questDetailsTaskDragId = "";
        state.questDetails.questDetailsTaskDragTargetIndex = -1;
    }

    private static int taskInsertIndexAtY(TabletUiState state, List<QuestDetailsTaskEntry> entries, String kind, int listY, int listBottom, int localY) {
        int scroll = "tasks".equals(kind) ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll;
        return CardDragSortUtil.insertIndexAtY(localY, listY, listBottom, scroll, QuestDetailsTasksPanel.LIST_PAD, QuestDetailsTasksPanel.CARD_H, QuestDetailsTasksPanel.CARD_GAP, entries.size());
    }

    private static void finishDrag(Player player, TabletUiState state, String questId, List<QuestDetailsTaskEntry> entries) {
        String moving = state.questDetails.questDetailsTaskDragId;
        int target = Math.max(0, state.questDetails.questDetailsTaskDragTargetIndex);
        boolean task = "tasks".equals(state.questDetails.questDetailsTaskDragKind);
        clearDrag(state);
        if (moving.isBlank()) {
            return;
        }
        int offset = CardDragSortUtil.offsetForDrop(moving, entries, QuestDetailsTaskEntry::id, target);
        if (offset == 0) {
            return;
        }
        if (task) {
            EditorQuestCommandClient.moveQuestTask(player, questId, moving, offset);
        } else {
            EditorQuestCommandClient.moveQuestReward(player, questId, moving, offset);
        }
    }
}

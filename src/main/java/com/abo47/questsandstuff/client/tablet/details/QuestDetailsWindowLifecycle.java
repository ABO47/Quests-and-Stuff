package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

final class QuestDetailsWindowLifecycle {
    private QuestDetailsWindowLifecycle() {
    }

    static void open(TabletUiState state, String questId) {
        if (state == null || questId == null || questId.isBlank()) {
            return;
        }
        state.questDetailsOpen = true;
        state.questDetailsQuestId = questId.trim();
        state.questDetailsEditMode = state.canEdit;
        resetOpenTransientState(state);
        EntityMotionEditor.close(state);
        QuestDetailsDescriptionModel.applyToolsToState(state, QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(state.questDetailsQuestId)));
        CompoundTag quest = ClientQuestCache.quest(state.questDetailsQuestId);
        state.pendingQuestRenameId = "";
        state.questTitleDraft = quest == null ? "" : quest.getString("title");
        state.questDetailsTitleFocused = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details open quest={}", state.questDetailsQuestId);
    }

    static void close(TabletUiState state) {
        String closingQuestId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId;
        state.questDetailsOpen = false;
        state.questDetailsQuestId = "";
        state.questDetailsScreenX = state.questDetailsX;
        state.questDetailsScreenY = state.questDetailsY;
        QuestDetailsTransientState.closeFloatingPopups(state);
        state.questDetailsDraggingSplitter = false;
        state.questDetailsPickTarget = "";
        state.questDetailsAssetPickTarget = "";
        clearSelectionState(state);
        state.boxSelecting = false;
        state.questDetailsBoxSelecting = false;
        state.questDetailsTextEditTarget = "";
        state.questDetailsTextStyleOpen = false;
        state.questDetailsTextStyleMenuX = 0;
        state.questDetailsTextStyleMenuY = 0;
        state.questDetailsTextStyleMenuW = 0;
        state.questDetailsTextStyleMenuH = 0;
        state.questDetailsTextLastClickId = "";
        state.questDetailsTextLastClickAtMs = 0L;
        state.questDetailsTextFontSizeSliderTarget = "";
        state.questDetailsTextFontSizeSliderDragging = false;
        state.questDetailsTextFontSizeSliderDragTarget = "";
        state.questDetailsTextColorQuestId = "";
        state.questDetailsTextColorTextId = "";
        EntityMotionEditor.close(state);
        state.questDetailsTitleFocused = false;
        if (closingQuestId.equals(state.pendingQuestRenameId)) {
            state.pendingQuestRenameId = "";
            state.questTitleDraft = "";
        }
    }

    static void openAdjacentQuest(TabletUiState state, String questId, int direction) {
        List<String> ids = new ArrayList<>(ClientQuestCache.quests().keySet());
        if (ids.isEmpty()) {
            return;
        }
        ids.sort(String::compareToIgnoreCase);
        int current = ids.indexOf(questId);
        if (current < 0) {
            current = 0;
        }
        int next = current + direction;
        if (next < 0 || next >= ids.size()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details navigate blocked from={} direction={}", questId, direction);
            return;
        }
        open(state, ids.get(next));
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details navigate from={} to={} direction={}", questId, ids.get(next), direction);
    }

    private static void resetOpenTransientState(TabletUiState state) {
        QuestDetailsTransientState.closeFloatingPopups(state);
        state.questDetailsTextStyleOpen = false;
        state.questDetailsTextLastClickId = "";
        state.questDetailsTextLastClickAtMs = 0L;
        state.questDetailsDraggingSplitter = false;
        clearSelectionState(state);
        state.boxSelecting = false;
        state.draggingCanvas = false;
        state.draggingSelection = false;
        state.resizingSelection = false;
        state.rotatingSelection = false;
        state.contextMenuOpen = false;
        state.canvasTextMenuOpen = false;
        state.canvasTextMenuTarget = "";
        state.canvasTextFontSizeSliderTarget = "";
        state.canvasTextFontSizeSliderDragging = false;
        state.canvasTextFontSizeSliderDragTarget = "";
        state.selectionBoundsVisible = false;
        state.questDetailsBoxSelecting = false;
        state.questDetailsTextFontSizeSliderTarget = "";
        state.questDetailsTextFontSizeSliderDragging = false;
        state.questDetailsTextFontSizeSliderDragTarget = "";
        state.questDetailsTextColorQuestId = "";
        state.questDetailsTextColorTextId = "";
        state.questDetailsPickTarget = "";
        state.questDetailsAssetPickTarget = "";
    }

    private static void clearSelectionState(TabletUiState state) {
        state.questDetailsSelectedTextId = "";
        state.questDetailsSelectedImageId = "";
        state.questDetailsSelectedTextIds.clear();
        state.questDetailsSelectedImageIds.clear();
        state.selectedCanvasTextId = "";
        state.selectedCanvasImageId = "";
        state.selectedCanvasTextIds.clear();
        state.selectedCanvasImageIds.clear();
    }
}

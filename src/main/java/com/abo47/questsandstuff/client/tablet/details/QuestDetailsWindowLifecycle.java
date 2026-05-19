package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
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
        open(state, questId, false, 0, 0, 0, 0);
    }

    static void openAtSource(TabletUiState state, String questId, int sourceX, int sourceY, int sourceW, int sourceH) {
        open(state, questId, true, sourceX, sourceY, sourceW, sourceH);
    }

    private static void open(TabletUiState state, String questId, boolean hasSource, int sourceX, int sourceY, int sourceW, int sourceH) {
        if (state == null || questId == null || questId.isBlank()) {
            return;
        }
        state.questDetailsClosing = false;
        state.questDetailsOpen = true;
        state.questDetailsQuestId = questId.trim();
        state.questDetailsEditMode = state.canEdit;
        resetOpenTransientState(state);
        startOpenAnimation(state, hasSource, sourceX, sourceY, sourceW, sourceH);
        EntityMotionEditor.close(state);
        QuestDetailsDescriptionModel.applyToolsToState(state, QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(state.questDetailsQuestId)));
        CompoundTag quest = ClientQuestCache.quest(state.questDetailsQuestId);
        state.pendingQuestRenameId = "";
        state.questTitleDraft = quest == null ? "" : quest.getString("title");
        state.questDetailsTitleFocused = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details open quest={} source={} x={} y={} w={} h={}",
                state.questDetailsQuestId,
                state.questDetailsAnimationHasSource,
                state.questDetailsAnimationSourceX,
                state.questDetailsAnimationSourceY,
                state.questDetailsAnimationSourceW,
                state.questDetailsAnimationSourceH);
    }

    static void close(TabletUiState state) {
        if (state == null || state.questDetailsClosing || !state.questDetailsOpen) {
            return;
        }
        String closingQuestId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId;
        applyCloseTransientState(state, closingQuestId);
        if (!QuestsAndStuffConfig.questWindowAnimationsEnabled()) {
            finishClose(state);
            return;
        }
        state.questDetailsOpen = false;
        state.questDetailsClosing = true;
        state.questDetailsAnimationStartMs = System.currentTimeMillis();
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details close start quest={} source={} x={} y={} w={} h={}",
                closingQuestId,
                state.questDetailsAnimationHasSource,
                state.questDetailsAnimationSourceX,
                state.questDetailsAnimationSourceY,
                state.questDetailsAnimationSourceW,
                state.questDetailsAnimationSourceH);
    }

    static boolean finishCloseIfDone(TabletUiState state) {
        if (state == null || !state.questDetailsClosing) {
            return false;
        }
        if (QuestsAndStuffConfig.questWindowAnimationsEnabled()
                && SourceOriginRevealWidget.windowRunning(state.questDetailsAnimationStartMs)) {
            return false;
        }
        String closingQuestId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId;
        finishClose(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details close finish quest={}", closingQuestId);
        return true;
    }

    static void finishClose(TabletUiState state) {
        if (state == null) {
            return;
        }
        String closingQuestId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId;
        state.questDetailsOpen = false;
        state.questDetailsClosing = false;
        state.questDetailsQuestId = "";
        applyCloseTransientState(state, closingQuestId);
        clearOpenAnimation(state);
    }

    private static void applyCloseTransientState(TabletUiState state, String closingQuestId) {
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

    private static void startOpenAnimation(TabletUiState state, boolean hasSource, int sourceX, int sourceY, int sourceW, int sourceH) {
        boolean validSource = hasSource && sourceW > 0 && sourceH > 0;
        state.questDetailsAnimationStartMs = System.currentTimeMillis();
        state.questDetailsAnimationHasSource = validSource;
        state.questDetailsAnimationSourceX = validSource ? sourceX : 0;
        state.questDetailsAnimationSourceY = validSource ? sourceY : 0;
        state.questDetailsAnimationSourceW = validSource ? sourceW : 0;
        state.questDetailsAnimationSourceH = validSource ? sourceH : 0;
    }

    private static void clearOpenAnimation(TabletUiState state) {
        state.questDetailsAnimationStartMs = 0L;
        state.questDetailsAnimationHasSource = false;
        state.questDetailsAnimationSourceX = 0;
        state.questDetailsAnimationSourceY = 0;
        state.questDetailsAnimationSourceW = 0;
        state.questDetailsAnimationSourceH = 0;
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

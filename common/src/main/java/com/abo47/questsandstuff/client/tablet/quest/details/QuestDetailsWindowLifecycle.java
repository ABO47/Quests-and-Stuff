package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.ProgressAnimations;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
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
        String trimmedQuestId = questId.trim();
        if (!canOpenQuestDetails(state, trimmedQuestId)) {
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details open blocked preview_hidden quest={}", trimmedQuestId);
            return;
        }
        state.questDetails.questDetailsClosing = false;
        state.questDetails.questDetailsOpen = true;
        state.questDetails.questDetailsQuestId = trimmedQuestId;
        ProgressAnimations.reset(ProgressAnimations.key("details", trimmedQuestId));
        resetOpenTransientState(state);
        startOpenAnimation(state, hasSource, sourceX, sourceY, sourceW, sourceH);
        EntityMotionEditor.close(state);
        CompoundTag quest = ClientQuestCache.quest(state.questDetails.questDetailsQuestId);
        state.questDetails.pendingQuestTitleChangeId = "";
        state.questDetails.questTitleDraft = quest == null ? "" : quest.getString("title");
        state.questDetails.questDetailsTitleFocused = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details open quest={} source={} x={} y={} w={} h={}",
                state.questDetails.questDetailsQuestId,
                state.questDetails.questDetailsAnimationHasSource,
                state.questDetails.questDetailsAnimationSourceX,
                state.questDetails.questDetailsAnimationSourceY,
                state.questDetails.questDetailsAnimationSourceW,
                state.questDetails.questDetailsAnimationSourceH);
    }

    static void swapQuest(TabletUiState state, String questId) {
        if (state == null || questId == null || questId.isBlank()) {
            return;
        }
        String trimmedQuestId = questId.trim();
        if (!canOpenQuestDetails(state, trimmedQuestId)) {
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details swap blocked preview_hidden quest={}", trimmedQuestId);
            return;
        }
        state.questDetails.questDetailsQuestId = trimmedQuestId;
        ProgressAnimations.reset(ProgressAnimations.key("details", trimmedQuestId));
        resetOpenTransientState(state);
        EntityMotionEditor.close(state);
        CompoundTag quest = ClientQuestCache.quest(state.questDetails.questDetailsQuestId);
        state.questDetails.pendingQuestTitleChangeId = "";
        state.questDetails.questTitleDraft = quest == null ? "" : quest.getString("title");
        state.questDetails.questDetailsTitleFocused = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details swap quest={}", state.questDetails.questDetailsQuestId);
    }

    static void close(TabletUiState state) {
        if (state == null || state.questDetails.questDetailsClosing || !state.questDetails.questDetailsOpen) {
            return;
        }
        String closingQuestId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId;
        applyCloseTransientState(state, closingQuestId);
        if (!QuestsAndStuffConfig.questWindowAnimationsEnabled()) {
            finishClose(state);
            return;
        }
        state.questDetails.questDetailsOpen = false;
        state.questDetails.questDetailsClosing = true;
        state.questDetails.questDetailsAnimationStartMs = System.currentTimeMillis();
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details close start quest={} source={} x={} y={} w={} h={}",
                closingQuestId,
                state.questDetails.questDetailsAnimationHasSource,
                state.questDetails.questDetailsAnimationSourceX,
                state.questDetails.questDetailsAnimationSourceY,
                state.questDetails.questDetailsAnimationSourceW,
                state.questDetails.questDetailsAnimationSourceH);
    }

    static boolean finishCloseIfDone(TabletUiState state) {
        if (state == null || !state.questDetails.questDetailsClosing) {
            return false;
        }
        if (QuestsAndStuffConfig.questWindowAnimationsEnabled()
                && SourceOriginRevealWidget.windowRunning(state.questDetails.questDetailsAnimationStartMs)) {
            return false;
        }
        String closingQuestId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId;
        finishClose(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details close finish quest={}", closingQuestId);
        return true;
    }

    static void finishClose(TabletUiState state) {
        if (state == null) {
            return;
        }
        String closingQuestId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId;
        state.questDetails.questDetailsOpen = false;
        state.questDetails.questDetailsClosing = false;
        state.questDetails.questDetailsQuestId = "";
        applyCloseTransientState(state, closingQuestId);
        clearOpenAnimation(state);
    }

    private static void applyCloseTransientState(TabletUiState state, String closingQuestId) {
        state.questDetails.questDetailsScreenX = state.questDetails.questDetailsX;
        state.questDetails.questDetailsScreenY = state.questDetails.questDetailsY;
        QuestDetailsTransientManager.closeFloatingPopups(state);
        ToolMenuAnimation.finishQuestDetails(state);
        state.questDetails.questDetailsDraggingSplitter = false;
        state.questDetails.questDetailsDescScrollDragging = false;
        state.questDetails.questDetailsPanning = false;
        state.questDetails.questDetailsPickTarget = "";
        state.questDetails.questDetailsAssetPickTarget = "";
        clearSelectionState(state);
        state.canvas.boxSelecting = false;
        state.questDetails.questDetailsBoxSelecting = false;
        TextEditSession.closeAny(state, true);
        TextStyleSession.closeQuestDetails(state);
        state.questDetails.questDetailsTextLastClickId = "";
        state.questDetails.questDetailsTextLastClickAtMs = 0L;
        state.questDetails.questDetailsTextColorQuestId = "";
        state.questDetails.questDetailsTextColorTextId = "";
        CanvasTransformSessions.clearQuestDetailsSession(state);
        EntityMotionEditor.close(state);
        state.questDetails.questDetailsTitleFocused = false;
        if (closingQuestId.equals(state.questDetails.pendingQuestTitleChangeId)) {
            state.questDetails.pendingQuestTitleChangeId = "";
            state.questDetails.questTitleDraft = "";
        }
    }

    static void openAdjacentQuest(TabletUiState state, String questId, int direction) {
        List<String> ids = new ArrayList<>(ClientQuestCache.questIds());
        if (ids.isEmpty()) {
            return;
        }
        ids.sort(String::compareToIgnoreCase);
        int current = ids.indexOf(questId);
        if (current < 0) {
            current = 0;
        }
        int next = nextOpenableQuestIndex(state, ids, current, direction);
        if (next < 0) {
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details navigate blocked from={} direction={}", questId, direction);
            return;
        }
        swapQuest(state, ids.get(next));
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details navigate from={} to={} direction={}", questId, ids.get(next), direction);
    }

    private static void resetOpenTransientState(TabletUiState state) {
        QuestDetailsTransientManager.closeFloatingPopups(state);
        ToolMenuAnimation.finishQuestDetails(state);
        TextStyleSession.closeQuestDetails(state);
        state.questDetails.questDetailsTextLastClickId = "";
        state.questDetails.questDetailsTextLastClickAtMs = 0L;
        state.questDetails.questDetailsDraggingSplitter = false;
        state.questDetails.questDetailsDescScrollDragging = false;
        state.questDetails.questDetailsPanning = false;
        clearSelectionState(state);
        state.canvas.boxSelecting = false;
        state.canvas.draggingCanvas = false;
        state.canvas.draggingSelection = false;
        state.canvas.resizingSelection = false;
        state.canvas.rotatingSelection = false;
        ContextMenuState.close(state);
        TextStyleSession.closeMainCanvas(state);
        state.canvas.selectionBoundsVisible = false;
        state.questDetails.questDetailsBoxSelecting = false;
        state.questDetails.questDetailsTextColorQuestId = "";
        state.questDetails.questDetailsTextColorTextId = "";
        state.questDetails.questDetailsPickTarget = "";
        state.questDetails.questDetailsAssetPickTarget = "";
        state.questDetails.questDetailsClaimedOverrideQuestId = "";
        CanvasTransformSessions.clearQuestDetailsSession(state);
    }

    private static void startOpenAnimation(TabletUiState state, boolean hasSource, int sourceX, int sourceY, int sourceW, int sourceH) {
        boolean validSource = hasSource && sourceW > 0 && sourceH > 0;
        state.questDetails.questDetailsAnimationStartMs = System.currentTimeMillis();
        state.questDetails.questDetailsAnimationHasSource = validSource;
        state.questDetails.questDetailsAnimationSourceX = validSource ? sourceX : 0;
        state.questDetails.questDetailsAnimationSourceY = validSource ? sourceY : 0;
        state.questDetails.questDetailsAnimationSourceW = validSource ? sourceW : 0;
        state.questDetails.questDetailsAnimationSourceH = validSource ? sourceH : 0;
    }

    private static void clearOpenAnimation(TabletUiState state) {
        state.questDetails.questDetailsAnimationStartMs = 0L;
        state.questDetails.questDetailsAnimationHasSource = false;
        state.questDetails.questDetailsAnimationSourceX = 0;
        state.questDetails.questDetailsAnimationSourceY = 0;
        state.questDetails.questDetailsAnimationSourceW = 0;
        state.questDetails.questDetailsAnimationSourceH = 0;
    }

    private static void clearSelectionState(TabletUiState state) {
        state.questDetails.questDetailsSelectedObjectiveKind = "";
        state.questDetails.questDetailsSelectedObjectiveId = "";
        state.questDetails.questDetailsSelectableRewardChoices.clear();
        state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId("");
        state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId("");
        state.questDetails.questDetailsDescriptionSelection.textIds().clear();
        state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.imageIds().clear();
    }

    private static int nextOpenableQuestIndex(TabletUiState state, List<String> ids, int current, int direction) {
        if (direction == 0) {
            return -1;
        }
        for (int i = current + direction; i >= 0 && i < ids.size(); i += direction) {
            if (canOpenQuestDetails(state, ids.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean canOpenQuestDetails(TabletUiState state, String questId) {
        if (state == null || questId == null || questId.isBlank()) {
            return false;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        return state.root.canEdit || (!ClientQuestCache.questLockedPreview(quest) && !ClientQuestCache.questHiddenPreview(quest));
    }
}

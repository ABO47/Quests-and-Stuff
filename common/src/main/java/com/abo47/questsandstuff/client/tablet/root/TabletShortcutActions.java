package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TabletShortcutActions {
    private TabletShortcutActions() {
    }

    static boolean handleGlobal(Player player, TabletUiState state, CanvasViewport canvasViewport, int keyCode, int scanCode, boolean ctrl, boolean shift) {
        if (state == null || !activeEditMode(state) || TabletRootWindowController.isTextInputActive(state, null)) {
            return false;
        }
        if (TabletClientHooks.renameSelectedMatches(keyCode, scanCode)) {
            return beginRename(state);
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            return deleteSelection(player, state, canvasViewport);
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_A) {
            return selectAll(state, canvasViewport);
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_D) {
            return duplicateSelection(player, state, canvasViewport);
        }
        if (isArrow(keyCode)) {
            return nudgeSelection(player, state, canvasViewport, keyCode, shift);
        }
        return false;
    }

    static boolean cancelTransient(TabletUiState state) {
        if (state == null) {
            return false;
        }
        boolean changed = false;
        if (state.boxSelecting || state.draggingSelection || state.resizingSelection || state.rotatingSelection
                || state.draggingCanvas || state.draggingCanvasImage || state.resizingCanvasImage || state.rotatingCanvasImage
                || state.draggingCanvasText || state.resizingCanvasText || state.rotatingCanvasText) {
            state.boxSelecting = false;
            state.draggingCanvas = false;
            CanvasTransformSessions.clearMainCanvasSession(state);
            changed = true;
        }
        if (state.questDetailsBoxSelecting || state.questDetailsPanning || state.questDetailsDescScrollDragging || !state.questDetailsTransformKind.isBlank()) {
            state.questDetailsBoxSelecting = false;
            state.questDetailsPanning = false;
            state.questDetailsDescScrollDragging = false;
            CanvasTransformSessions.clearQuestDetailsSession(state);
            changed = true;
        }
        if (state.chapterDragPending || state.chapterDragActive || state.questDetailsObjectiveDragPending || state.questDetailsObjectiveDragActive) {
            state.chapterDragPending = false;
            state.chapterDragActive = false;
            state.chapterDragName = "";
            state.chapterDragTargetIndex = -1;
            state.questDetailsObjectiveDragPending = false;
            state.questDetailsObjectiveDragActive = false;
            state.questDetailsObjectiveDragKind = "";
            state.questDetailsObjectiveDragId = "";
            state.questDetailsObjectiveDragTargetIndex = -1;
            changed = true;
        }
        return changed;
    }

    private static boolean beginRename(TabletUiState state) {
        if (state.questDetailsOpen) {
            return QuestDetailsWindow.beginSelectedRename(state);
        }
        String selectedQuestId = TabletStateQueries.singleSelectedQuestId(state);
        if (!selectedQuestId.isBlank()) {
            EditorCommandClient.beginQuestTitleChange(state, selectedQuestId);
            return true;
        }
        if (!TabletStateQueries.hasSelectedQuests(state) && CanvasSelectionActions.selectedImageIds(state).isEmpty()
                && CanvasSelectionActions.selectedTextIds(state).isEmpty() && state.selectedGroup != null && !state.selectedGroup.isBlank()) {
            state.pendingChapterRename = state.selectedGroup;
            state.chapterDraftName = state.selectedGroup;
            return true;
        }
        return false;
    }

    private static boolean deleteSelection(Player player, TabletUiState state, CanvasViewport canvasViewport) {
        if (state.questDetailsOpen) {
            return QuestDetailsWindow.deleteSelected(player, state);
        }
        String group = TabletStateQueries.selectedGroupName(state);
        boolean changed = false;
        for (String questId : TabletStateQueries.selectedQuestIdSnapshot(state)) {
            EditorCommandClient.runRemoveQuestAction(player, questId);
            changed = true;
        }
        for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
            changed |= CanvasLayerMutations.removeCanvasImage(state, group, imageId);
        }
        for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
            changed |= CanvasLayerMutations.removeCanvasText(state, group, textId);
        }
        if (changed) {
            CanvasSelectionActions.clearCanvasSelection(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] shortcut delete canvas selection group={}", group);
        }
        return changed;
    }

    private static boolean selectAll(TabletUiState state, CanvasViewport canvasViewport) {
        if (state.questDetailsOpen) {
            return QuestDetailsWindow.selectAllDescription(state);
        }
        if (canvasViewport == null) {
            return false;
        }
        String group = TabletStateQueries.selectedGroupName(state);
        state.canvasSelection.questIds().clear();
        state.canvasSelection.questIds().addAll(canvasViewport.cardLookup().keySet());
        state.canvasSelection.imageIds().clear();
        state.canvasSelection.textIds().clear();
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            state.canvasSelection.imageIds().add(image.id());
            state.canvasSelection.setPrimaryImageId(image.id());
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            state.canvasSelection.textIds().add(text.id());
            state.canvasSelection.setPrimaryTextId(text.id());
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] shortcut select all canvas group={} quests={} images={} texts={}",
                group, state.canvasSelection.questIds().size(), state.canvasSelection.imageIds().size(), state.canvasSelection.textIds().size());
        return true;
    }

    private static boolean duplicateSelection(Player player, TabletUiState state, CanvasViewport canvasViewport) {
        if (state.questDetailsOpen) {
            return QuestDetailsWindow.duplicateSelected(player, state);
        }
        if (canvasViewport == null) {
            return false;
        }
        boolean copied = CanvasClipboardController.copySelectionToClipboard(canvasViewport, state);
        return copied && CanvasClipboardController.pasteNearSelectionOrViewportCenter(player, state, canvasViewport);
    }

    private static boolean nudgeSelection(Player player, TabletUiState state, CanvasViewport canvasViewport, int keyCode, boolean shift) {
        if (state.questDetailsOpen) {
            return QuestDetailsWindow.nudgeSelected(player, state, nudgeDx(keyCode, shift, CanvasGeometry.gridSize(state)), nudgeDy(keyCode, shift, CanvasGeometry.gridSize(state)));
        }
        if (canvasViewport == null) {
            return false;
        }
        int step = shift ? CanvasGeometry.gridSize(state) : 1;
        int dx = nudgeDx(keyCode, false, step);
        int dy = nudgeDy(keyCode, false, step);
        String group = TabletStateQueries.selectedGroupName(state);
        boolean changed = false;
        Map<String, CanvasPoint> questMoves = new LinkedHashMap<>();
        for (String questId : state.canvasSelection.questIds()) {
            QuestCardLayout card = canvasViewport.cardLookup().get(questId);
            if (card != null) {
                questMoves.put(questId, new CanvasPoint(Math.max(0, card.logicalX() + dx), Math.max(0, card.logicalY() + dy)));
            }
        }
        if (!questMoves.isEmpty()) {
            EditorCommandClient.runCanvasMoveAction(player, state, questMoves);
            changed = true;
        }
        for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
            CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, group, imageId);
            if (image != null) {
                CanvasPoint point = CanvasGeometry.clampRotatedAnchorToCanvas(state, image.x() + dx, image.y() + dy, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
                CanvasLayerMutations.putCanvasImage(state, group, image.moveTo(point.x, point.y));
                changed = true;
            }
        }
        for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
            CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, textId);
            if (text != null) {
                CanvasPoint point = CanvasGeometry.clampRotatedAnchorToCanvas(state, text.x() + dx, text.y() + dy, text.w(), text.h(), text.w() / 2, text.h() / 2, text.rotation());
                CanvasLayerMutations.putCanvasText(state, group, text.moveTo(point.x, point.y));
                changed = true;
            }
        }
        return changed;
    }

    private static boolean isArrow(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT
                || keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN;
    }

    private static boolean activeEditMode(TabletUiState state) {
        return state.questDetailsOpen ? QuestDetailsEditState.canEdit(state) : state.canEdit;
    }

    private static int nudgeDx(int keyCode, boolean shift, int step) {
        int amount = shift ? Math.max(1, step) : step;
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            return -amount;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            return amount;
        }
        return 0;
    }

    private static int nudgeDy(int keyCode, boolean shift, int step) {
        int amount = shift ? Math.max(1, step) : step;
        if (keyCode == GLFW.GLFW_KEY_UP) {
            return -amount;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            return amount;
        }
        return 0;
    }
}

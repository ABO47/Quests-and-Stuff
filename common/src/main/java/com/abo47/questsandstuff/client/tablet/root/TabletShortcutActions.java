package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.screen.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
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
            state.draggingSelection = false;
            state.resizingSelection = false;
            state.rotatingSelection = false;
            state.draggingCanvas = false;
            state.draggingCanvasImage = false;
            state.resizingCanvasImage = false;
            state.rotatingCanvasImage = false;
            state.canvasImageTransformAxis = "";
            state.draggingCanvasText = false;
            state.resizingCanvasText = false;
            state.rotatingCanvasText = false;
            state.transientQuestPositions.clear();
            state.transientQuestScales.clear();
            state.snapGuideXVisible = false;
            state.snapGuideYVisible = false;
            changed = true;
        }
        if (state.questDetailsBoxSelecting || state.questDetailsPanning || !state.questDetailsTransformKind.isBlank()) {
            state.questDetailsBoxSelecting = false;
            state.questDetailsPanning = false;
            state.questDetailsTransformKind = "";
            state.questDetailsTransformId = "";
            state.questDetailsTransformMode = "";
            state.questDetailsTransformAxis = "";
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
        if (state.selectedQuestIds.size() == 1) {
            EditorCommandClient.beginQuestTitleChange(state, state.selectedQuestIds.iterator().next());
            return true;
        }
        if (state.selectedQuestIds.isEmpty() && CanvasRenderer.selectedCanvasImageIds(state).isEmpty()
                && CanvasRenderer.selectedCanvasTextIds(state).isEmpty() && state.selectedGroup != null && !state.selectedGroup.isBlank()) {
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
        String group = TabletUiFactory.selectedGroupName(state);
        boolean changed = false;
        for (String questId : List.copyOf(state.selectedQuestIds)) {
            EditorCommandClient.runRemoveQuestAction(player, questId);
            changed = true;
        }
        for (String imageId : CanvasRenderer.selectedCanvasImageIds(state)) {
            changed |= CanvasRenderer.removeCanvasImage(state, group, imageId);
        }
        for (String textId : CanvasRenderer.selectedCanvasTextIds(state)) {
            changed |= CanvasRenderer.removeCanvasText(state, group, textId);
        }
        if (changed) {
            CanvasRenderer.clearCanvasSelection(state);
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
        String group = TabletUiFactory.selectedGroupName(state);
        state.selectedQuestIds.clear();
        state.selectedQuestIds.addAll(canvasViewport.cardLookup().keySet());
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasTextIds.clear();
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            state.selectedCanvasImageIds.add(image.id());
            state.selectedCanvasImageId = image.id();
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            state.selectedCanvasTextIds.add(text.id());
            state.selectedCanvasTextId = text.id();
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] shortcut select all canvas group={} quests={} images={} texts={}",
                group, state.selectedQuestIds.size(), state.selectedCanvasImageIds.size(), state.selectedCanvasTextIds.size());
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
        String group = TabletUiFactory.selectedGroupName(state);
        boolean changed = false;
        Map<String, CanvasPoint> questMoves = new LinkedHashMap<>();
        for (String questId : state.selectedQuestIds) {
            QuestCardLayout card = canvasViewport.cardLookup().get(questId);
            if (card != null) {
                questMoves.put(questId, new CanvasPoint(Math.max(0, card.logicalX() + dx), Math.max(0, card.logicalY() + dy)));
            }
        }
        if (!questMoves.isEmpty()) {
            EditorCommandClient.runCanvasMoveAction(player, state, questMoves);
            changed = true;
        }
        for (String imageId : CanvasRenderer.selectedCanvasImageIds(state)) {
            CanvasImageLayer image = CanvasRenderer.findCanvasImage(state, group, imageId);
            if (image != null) {
                CanvasPoint point = CanvasGeometry.clampAnchorToCanvas(state, image.x() + dx, image.y() + dy, image.w(), image.h());
                CanvasRenderer.putCanvasImage(state, group, image.moveTo(point.x, point.y));
                changed = true;
            }
        }
        for (String textId : CanvasRenderer.selectedCanvasTextIds(state)) {
            CanvasTextLayer text = CanvasRenderer.findCanvasText(state, group, textId);
            if (text != null) {
                CanvasPoint point = CanvasGeometry.clampAnchorToCanvas(state, text.x() + dx, text.y() + dy, text.w(), text.h());
                CanvasRenderer.putCanvasText(state, group, text.moveTo(point.x, point.y));
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

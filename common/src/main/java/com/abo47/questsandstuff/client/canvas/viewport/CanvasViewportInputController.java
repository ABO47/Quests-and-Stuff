package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.canvas.contextmenu.CanvasContextMenuController;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.selection.CanvasBoxSelectionController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasViewportZoom;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.TabletWidgetCoordinates;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

final class CanvasViewportInputController {
    private CanvasViewportInputController() {
    }

    static boolean mouseDragged(
            CanvasViewport viewport,
            TabletUiState state,
            Runnable refresher,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (!state.pendingQuestTitleChangeId.isBlank()) {
            return viewport.callSuperMouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int localX = TabletWidgetCoordinates.localX(viewport, state.canvasPanelX + state.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(viewport, state.canvasPanelY + state.canvasViewportY, mouseY);

        if (CanvasMinimapController.handleDrag(state, localX, localY)) {
            viewport.refreshCanvas();
            return true;
        }

        if (EntityMotionEditor.isMainCanvasOpen(state) && (EntityMotionEditor.isDragging(state) || EntityMotionEditor.isMainCanvasHit(state, localX, localY))) {
            if (viewport.callSuperMouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                refresher.run();
            }
            return true;
        }

        if (state.canvasTextMenuOpen && viewport.callSuperMouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (textEditor.dragSelectionTo(localX, localY)) {
            return true;
        }

        if (state.draggingCanvas) {
            int dx = localX - state.dragCurrentX;
            int dy = localY - state.dragCurrentY;
            state.canvasOffsetX += dx;
            state.canvasOffsetY += dy;
            state.dragCurrentX = localX;
            state.dragCurrentY = localY;
            viewport.refreshCanvas();
            return true;
        }

        if (state.draggingCanvasImage || state.resizingCanvasImage || state.rotatingCanvasImage) {
            elementTransforms.updateImageTransform(localX, localY, cards);
            viewport.refreshCanvas();
            return true;
        }
        if (state.draggingCanvasText || state.resizingCanvasText || state.rotatingCanvasText) {
            elementTransforms.updateTextTransform(localX, localY, cards);
            viewport.refreshCanvas();
            return true;
        }

        if (state.draggingSelection) {
            selectionTransforms.updateDrag(localX, localY, cards, byQuestId);
            viewport.refreshCanvas();
            return true;
        }

        if (state.resizingSelection) {
            selectionTransforms.updateResize(localX, localY);
            viewport.refreshCanvas();
            return true;
        }

        if (state.rotatingSelection) {
            selectionTransforms.updateRotate(localX, localY, byQuestId);
            viewport.refreshCanvas();
            return true;
        }

        if (state.boxSelecting) {
            state.boxCurrentX = localX;
            state.boxCurrentY = localY;
            viewport.refreshCanvas();
            return true;
        }

        return viewport.callSuperMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    static boolean mouseReleased(
            CanvasViewport viewport,
            TabletUiState state,
            Player player,
            Runnable refresher,
            List<QuestCardLayout> cards,
            CanvasInlineTextEditor textEditor,
            CanvasSelectionTransformController selectionTransforms,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!state.pendingQuestTitleChangeId.isBlank()) {
            return viewport.callSuperMouseReleased(mouseX, mouseY, button);
        }
        int localX = TabletWidgetCoordinates.localX(viewport, state.canvasPanelX + state.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(viewport, state.canvasPanelY + state.canvasViewportY, mouseY);
        if (CanvasMinimapController.finishDrag(state)) {
            viewport.refreshCanvas();
            return true;
        }
        if (EntityMotionEditor.isMainCanvasOpen(state) && (EntityMotionEditor.isDragging(state) || EntityMotionEditor.isMainCanvasHit(state, localX, localY))) {
            if (viewport.callSuperMouseReleased(mouseX, mouseY, button)) {
                refresher.run();
            }
            return true;
        }
        if (state.draggingCanvas) {
            state.draggingCanvas = false;
            viewport.refreshCanvas();
            return true;
        }

        if (textEditor.finishSelectionDrag()) {
            return true;
        }

        if (state.canvasTextMenuOpen && viewport.callSuperMouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        if (state.draggingCanvasImage || state.resizingCanvasImage || state.rotatingCanvasImage) {
            CanvasRenderer.persistCanvasImage(state, TabletUiFactory.selectedGroupName(state), state.selectedCanvasImageId);
            state.draggingCanvasImage = false;
            state.resizingCanvasImage = false;
            state.rotatingCanvasImage = false;
            state.snapGuideXVisible = false;
            state.snapGuideYVisible = false;
            refresher.run();
            return true;
        }
        if (state.draggingCanvasText || state.resizingCanvasText || state.rotatingCanvasText) {
            CanvasRenderer.persistCanvasText(state, TabletUiFactory.selectedGroupName(state), state.selectedCanvasTextId);
            state.draggingCanvasText = false;
            state.resizingCanvasText = false;
            state.rotatingCanvasText = false;
            state.snapGuideXVisible = false;
            state.snapGuideYVisible = false;
            refresher.run();
            return true;
        }

        if (state.draggingSelection) {
            state.draggingSelection = false;
            if (!state.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.transientQuestPositions);
            }
            String group = TabletUiFactory.selectedGroupName(state);
            for (String imageId : CanvasRenderer.selectedCanvasImageIds(state)) {
                CanvasRenderer.persistCanvasImage(state, group, imageId);
            }
            for (String textId : CanvasRenderer.selectedCanvasTextIds(state)) {
                CanvasRenderer.persistCanvasText(state, group, textId);
            }
            selectionTransforms.clear();
            refresher.run();
            return true;
        }

        if (state.resizingSelection) {
            state.resizingSelection = false;
            if (!state.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.transientQuestPositions);
            }
            if (!state.transientQuestScales.isEmpty()) {
                EditorCommandClient.runCanvasScaleAction(player, state, state.transientQuestScales);
            }
            selectionTransforms.clear();
            refresher.run();
            return true;
        }

        if (state.rotatingSelection) {
            state.rotatingSelection = false;
            if (!state.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.transientQuestPositions);
            }
            selectionTransforms.clear();
            refresher.run();
            return true;
        }

        if (state.boxSelecting) {
            state.boxSelecting = false;
            CanvasBoxSelectionController.finishBoxSelection(state, cards);
            refresher.run();
            return true;
        }

        return viewport.callSuperMouseReleased(mouseX, mouseY, button);
    }

    static boolean mouseWheelMove(
            CanvasViewport viewport,
            TabletUiState state,
            Runnable refresher,
            CanvasInlineTextEditor textEditor,
            double mouseX,
            double mouseY,
            double wheelDelta
    ) {
        if (!viewport.isMouseOverElement(mouseX, mouseY)) {
            return viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        int localX = TabletWidgetCoordinates.localX(viewport, state.canvasPanelX + state.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(viewport, state.canvasPanelY + state.canvasViewportY, mouseY);
        if (EntityMotionEditor.isMainCanvasOpen(state) && EntityMotionEditor.isMainCanvasHit(state, localX, localY)) {
            viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta);
            refresher.run();
            return true;
        }
        if (state.contextMenuOpen && TabletUiFactory.isContextMenuHit(state, localX, localY)) {
            CanvasContextMenuController.scrollContextMenu(state, wheelDelta);
            refresher.run();
            return true;
        }
        if (CanvasMinimapController.isPanelHit(state, localX, localY)) {
            return true;
        }
        if (state.canvasTextMenuOpen && textEditor.isMenuHit(localX, localY)) {
            viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta);
            return true;
        }
        CanvasViewportZoom.zoomAt(state, viewport::refreshCanvas, localX, localY, wheelDelta);
        return true;
    }
}

package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu.CanvasContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasBoxSelectionController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportZoom;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
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

        if (state.contextMenuOpen && state.contextMenuScrollDragging) {
            viewport.callSuperMouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }

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
            state.dragCurrentX = localX;
            state.dragCurrentY = localY;
            if (viewport.previewCanvasPan(dx, dy)) {
                if (viewport.panPreviewNeedsRefresh()) {
                    viewport.commitCanvasPan();
                    viewport.refreshCanvas();
                }
            } else {
                CanvasCameraController.panByScreen(state, dx, dy, true);
                viewport.refreshCanvas();
            }
            return true;
        }

        if (state.draggingCanvasImage || state.resizingCanvasImage || state.rotatingCanvasImage) {
            elementTransforms.updateImageTransform(localX, localY, cards);
            return true;
        }
        if (state.draggingCanvasText || state.resizingCanvasText || state.rotatingCanvasText) {
            elementTransforms.updateTextTransform(localX, localY, cards);
            if (state.canvasTextMenuOpen) {
                viewport.refreshCanvas();
            }
            return true;
        }

        if (state.draggingSelection) {
            boolean liveQuestPreview = viewport.selectionDragPreviewSupported();
            selectionTransforms.updateDrag(localX, localY, cards, byQuestId, liveQuestPreview);
            if (liveQuestPreview && viewport.previewSelectionDrag()) {
                return true;
            }
            if (liveQuestPreview) {
                selectionTransforms.populateDragPositions();
            }
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
            CanvasBoxSelectionController.updateBoxSelection(state, cards);
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
        if (state.contextMenuOpen && state.contextMenuScrollDragging) {
            viewport.callSuperMouseReleased(mouseX, mouseY, button);
            if (state.contextMenuScrollDragging) {
                state.contextMenuScrollDragging = false;
            }
            refresher.run();
            return true;
        }
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
            viewport.commitCanvasPan();
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
            String group = TabletStateQueries.selectedGroupName(state);
            CanvasRenderer.commitTransientCanvasImage(state, group, state.selectedCanvasImageId);
            CanvasRenderer.persistCanvasImage(state, group, state.selectedCanvasImageId);
            CanvasTransformSessions.clearMainCanvasSession(state);
            refresher.run();
            return true;
        }
        if (state.draggingCanvasText || state.resizingCanvasText || state.rotatingCanvasText) {
            String group = TabletStateQueries.selectedGroupName(state);
            CanvasRenderer.commitTransientCanvasText(state, group, state.selectedCanvasTextId);
            CanvasRenderer.persistCanvasText(state, group, state.selectedCanvasTextId);
            CanvasTransformSessions.clearMainCanvasSession(state);
            refresher.run();
            return true;
        }

        if (state.draggingSelection) {
            state.draggingSelection = false;
            viewport.endSelectionDragPreview();
            int movedImages = state.transientCanvasImages.size();
            int movedTexts = state.transientCanvasTexts.size();
            if (state.transientQuestPositions.isEmpty() && (state.dragSelectionDeltaX != 0 || state.dragSelectionDeltaY != 0)) {
                selectionTransforms.populateDragPositions();
            }
            if (!state.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.transientQuestPositions);
            }
            String group = TabletStateQueries.selectedGroupName(state);
            CanvasRenderer.commitSelectedTransientCanvasLayers(state, group);
            for (String imageId : CanvasRenderer.selectedCanvasImageIds(state)) {
                CanvasRenderer.persistCanvasImage(state, group, imageId);
            }
            for (String textId : CanvasRenderer.selectedCanvasTextIds(state)) {
                CanvasRenderer.persistCanvasText(state, group, textId);
            }
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] canvas selection drag commit quests={} images={} texts={} delta={},{}",
                    state.transientQuestPositions.size(),
                    movedImages,
                    movedTexts,
                    state.dragSelectionDeltaX,
                    state.dragSelectionDeltaY
            );
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
            CanvasRenderer.commitSelectedTransientCanvasLayers(state, TabletStateQueries.selectedGroupName(state));
            persistSelectedCanvasLayers(state);
            selectionTransforms.clear();
            refresher.run();
            return true;
        }

        if (state.rotatingSelection) {
            state.rotatingSelection = false;
            if (!state.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.transientQuestPositions);
            }
            CanvasRenderer.commitSelectedTransientCanvasLayers(state, TabletStateQueries.selectedGroupName(state));
            persistSelectedCanvasLayers(state);
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

    private static void persistSelectedCanvasLayers(TabletUiState state) {
        String group = TabletStateQueries.selectedGroupName(state);
        for (String imageId : CanvasRenderer.selectedCanvasImageIds(state)) {
            CanvasRenderer.persistCanvasImage(state, group, imageId);
        }
        for (String textId : CanvasRenderer.selectedCanvasTextIds(state)) {
            CanvasRenderer.persistCanvasText(state, group, textId);
        }
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
        if (state.contextMenuOpen) {
            int previous = state.contextMenuScroll;
            CanvasContextMenuController.scrollContextMenu(state, wheelDelta);
            if (state.contextMenuScroll != previous) {
                refresher.run();
            } else if (viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta)) {
                refresher.run();
            }
            return true;
        }
        if (CanvasMinimapController.isPanelHit(state, localX, localY)) {
            return true;
        }
        if (state.canvasTextMenuOpen && textEditor.isMenuHit(localX, localY)) {
            viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta);
            return true;
        }
        viewport.commitCanvasPan();
        CanvasViewportZoom.zoomAt(state, viewport::queueCanvasRefresh, localX, localY, wheelDelta);
        return true;
    }
}

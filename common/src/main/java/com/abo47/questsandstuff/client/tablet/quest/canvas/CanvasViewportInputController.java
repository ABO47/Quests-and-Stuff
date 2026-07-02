package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu.CanvasContextMenuController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasBoxSelectionController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportZoom;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
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
        if (!state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            return viewport.callSuperMouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int localX = TabletWidgetCoordinates.localX(viewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(viewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY, mouseY);

        if (state.contextMenu.contextMenuOpen && state.contextMenu.contextMenuScrollDragging) {
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

        if (state.canvas.canvasTextMenuOpen && viewport.callSuperMouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (textEditor.dragSelectionTo(localX, localY)) {
            return true;
        }

        if (state.canvas.draggingCanvas) {
            int dx = localX - state.canvas.dragCurrentX;
            int dy = localY - state.canvas.dragCurrentY;
            state.canvas.dragCurrentX = localX;
            state.canvas.dragCurrentY = localY;
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

        if (state.canvas.draggingCanvasImage || state.canvas.resizingCanvasImage || state.canvas.rotatingCanvasImage) {
            elementTransforms.updateImageTransform(localX, localY, cards);
            return true;
        }
        if (state.canvas.draggingCanvasText || state.canvas.resizingCanvasText || state.canvas.rotatingCanvasText) {
            elementTransforms.updateTextTransform(localX, localY, cards);
            if (state.canvas.canvasTextMenuOpen) {
                viewport.refreshCanvas();
            }
            return true;
        }

        if (state.canvas.draggingCanvasExclusiveChoice || state.canvas.resizingCanvasExclusiveChoice || state.canvas.rotatingCanvasExclusiveChoice) {
            elementTransforms.updateExclusiveChoiceTransform(localX, localY, cards);
            viewport.refreshCanvas();
            return true;
        }

        if (state.canvas.draggingSelection) {
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

        if (state.canvas.resizingSelection) {
            selectionTransforms.updateResize(localX, localY);
            viewport.refreshCanvas();
            return true;
        }

        if (state.canvas.rotatingSelection) {
            selectionTransforms.updateRotate(localX, localY, byQuestId);
            viewport.refreshCanvas();
            return true;
        }

        if (state.canvas.boxSelecting) {
            state.canvas.boxCurrentX = localX;
            state.canvas.boxCurrentY = localY;
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
        if (!state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            return viewport.callSuperMouseReleased(mouseX, mouseY, button);
        }
        int localX = TabletWidgetCoordinates.localX(viewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(viewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY, mouseY);
        if (state.contextMenu.contextMenuOpen && state.contextMenu.contextMenuScrollDragging) {
            viewport.callSuperMouseReleased(mouseX, mouseY, button);
            if (state.contextMenu.contextMenuScrollDragging) {
                ContextMenuController.setScrollDragging(state, false);
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
        if (state.canvas.draggingCanvas) {
            state.canvas.draggingCanvas = false;
            viewport.commitCanvasPan();
            viewport.refreshCanvas();
            return true;
        }

        if (textEditor.finishSelectionDrag()) {
            return true;
        }

        if (state.canvas.canvasTextMenuOpen && viewport.callSuperMouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        if (state.canvas.draggingCanvasImage || state.canvas.resizingCanvasImage || state.canvas.rotatingCanvasImage) {
            String group = TabletStateQueries.selectedChapterName(state);
            CanvasLayerMutations.commitTransientCanvasImage(state, group, state.canvas.canvasSelection.primaryImageId());
            CanvasLayerMutations.persistCanvasImage(state, group, state.canvas.canvasSelection.primaryImageId());
            CanvasTransformSessions.clearMainCanvasSession(state);
            refresher.run();
            return true;
        }
        if (state.canvas.draggingCanvasText || state.canvas.resizingCanvasText || state.canvas.rotatingCanvasText) {
            String group = TabletStateQueries.selectedChapterName(state);
            CanvasLayerMutations.commitTransientCanvasText(state, group, state.canvas.canvasSelection.primaryTextId());
            CanvasLayerMutations.persistCanvasText(state, group, state.canvas.canvasSelection.primaryTextId());
            CanvasTransformSessions.clearMainCanvasSession(state);
            refresher.run();
            return true;
        }

        if (state.canvas.draggingCanvasExclusiveChoice || state.canvas.resizingCanvasExclusiveChoice || state.canvas.rotatingCanvasExclusiveChoice) {
            String group = TabletStateQueries.selectedChapterName(state);
            CanvasLayerMutations.commitTransientCanvasExclusiveChoice(state, group, state.canvas.canvasSelection.primaryEcId());
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, group, state.canvas.canvasSelection.primaryEcId());
            CanvasTransformSessions.clearMainCanvasSession(state);
            refresher.run();
            return true;
        }

        if (state.canvas.draggingSelection) {
            state.canvas.draggingSelection = false;
            viewport.endSelectionDragPreview();
            int movedImages = state.canvas.transientCanvasImages.size();
            int movedTexts = state.canvas.transientCanvasTexts.size();
            if (state.canvas.transientQuestPositions.isEmpty() && (state.canvas.dragSelectionDeltaX != 0 || state.canvas.dragSelectionDeltaY != 0)) {
                selectionTransforms.populateDragPositions();
            }
            if (!state.canvas.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.canvas.transientQuestPositions);
            }
            String group = TabletStateQueries.selectedChapterName(state);
            CanvasLayerMutations.commitSelectedTransientCanvasLayers(state, group);
            for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
                CanvasLayerMutations.persistCanvasImage(state, group, imageId);
            }
            for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
                CanvasLayerMutations.persistCanvasText(state, group, textId);
            }
            int movedEcs = state.canvas.transientCanvasExclusiveChoices.size();
            for (String ecId : CanvasSelectionActions.selectedEcIds(state)) {
                CanvasLayerMutations.persistCanvasExclusiveChoice(state, group, ecId);
            }
            QuestsAndStuffMod.debugLog(
                    "[QnS:UI] canvas selection drag commit quests={} images={} texts={} ecs={} delta={},{}",
                    state.canvas.transientQuestPositions.size(),
                    movedImages,
                    movedTexts,
                    movedEcs,
                    state.canvas.dragSelectionDeltaX,
                    state.canvas.dragSelectionDeltaY
            );
            selectionTransforms.clear();
            refresher.run();
            return true;
        }

        if (state.canvas.resizingSelection) {
            state.canvas.resizingSelection = false;
            if (!state.canvas.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.canvas.transientQuestPositions);
            }
            if (!state.canvas.transientQuestScales.isEmpty()) {
                EditorCanvasCommandClient.runCanvasScaleAction(player, state, state.canvas.transientQuestScales);
            }
            CanvasLayerMutations.commitSelectedTransientCanvasLayers(state, TabletStateQueries.selectedChapterName(state));
            persistSelectedCanvasLayers(state);
            selectionTransforms.clear();
            refresher.run();
            return true;
        }

        if (state.canvas.rotatingSelection) {
            state.canvas.rotatingSelection = false;
            if (!state.canvas.transientQuestPositions.isEmpty()) {
                TabletUiFactory.runCanvasMoveAction(player, state, state.canvas.transientQuestPositions);
            }
            CanvasLayerMutations.commitSelectedTransientCanvasLayers(state, TabletStateQueries.selectedChapterName(state));
            persistSelectedCanvasLayers(state);
            selectionTransforms.clear();
            refresher.run();
            return true;
        }

        if (state.canvas.boxSelecting) {
            state.canvas.boxSelecting = false;
            CanvasBoxSelectionController.finishBoxSelection(state, cards);
            refresher.run();
            return true;
        }

        return viewport.callSuperMouseReleased(mouseX, mouseY, button);
    }

    private static void persistSelectedCanvasLayers(TabletUiState state) {
        String group = TabletStateQueries.selectedChapterName(state);
        for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
            CanvasLayerMutations.persistCanvasImage(state, group, imageId);
        }
        for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
            CanvasLayerMutations.persistCanvasText(state, group, textId);
        }
        for (String ecId : CanvasSelectionActions.selectedEcIds(state)) {
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, group, ecId);
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
        int localX = TabletWidgetCoordinates.localX(viewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(viewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY, mouseY);
        if (EntityMotionEditor.isMainCanvasOpen(state) && EntityMotionEditor.isMainCanvasHit(state, localX, localY)) {
            viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta);
            refresher.run();
            return true;
        }
        if (state.contextMenu.contextMenuOpen) {
            int previous = state.contextMenu.contextMenuScroll;
            CanvasContextMenuController.scrollContextMenu(state, wheelDelta);
            if (state.contextMenu.contextMenuScroll != previous) {
                refresher.run();
            } else if (viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta)) {
                refresher.run();
            }
            return true;
        }
        if (CanvasMinimapController.isPanelHit(state, localX, localY)) {
            return true;
        }
        if (state.canvas.canvasTextMenuOpen && textEditor.isMenuHit(localX, localY)) {
            viewport.callSuperMouseWheelMove(mouseX, mouseY, wheelDelta);
            return true;
        }
        viewport.commitCanvasPan();
        CanvasViewportZoom.zoomAt(state, viewport::queueCanvasRefresh, localX, localY, wheelDelta);
        return true;
    }
}

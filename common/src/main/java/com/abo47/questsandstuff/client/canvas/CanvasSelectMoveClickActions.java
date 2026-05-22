package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.selection.CanvasBoxSelectionController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.Map;

final class CanvasSelectMoveClickActions {
    private CanvasSelectMoveClickActions() {
    }

    static void handleSelectMove(
            CanvasViewport canvasViewport,
            TabletUiState state,
            Runnable refresher,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            int localX,
            int localY,
            int button,
            QuestCardLayout hit,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit
    ) {
        if (button == 0 && state.canEdit) {
            if (handleElementTransformStart(canvasViewport, state, refresher, byQuestId, textEditor, elementTransforms, selectionTransforms, localX, localY, imageHit, textHit)) {
                return;
            }
        }
        if (hit != null) {
            long now = System.currentTimeMillis();
            boolean doubleClick = hit.questId().equals(state.canvasLastClickedQuestId) && now - state.canvasLastQuestClickAtMs <= 350L;
            state.canvasLastClickedQuestId = hit.questId();
            state.canvasLastQuestClickAtMs = now;
            if (doubleClick && button == 0) {
                QuestDetailsWindow.openAtSource(
                        state,
                        hit.questId(),
                        canvasViewport.getPositionX() + hit.x(),
                        canvasViewport.getPositionY() + hit.y(),
                        hit.width(),
                        hit.height()
                );
                refresher.run();
                return;
            }
            if (canvasViewport.shiftDown()) {
                if (!state.selectedQuestIds.add(hit.questId())) {
                    state.selectedQuestIds.remove(hit.questId());
                }
            } else if (!state.selectedQuestIds.contains(hit.questId())) {
                CanvasRenderer.clearCanvasSelection(state);
                state.selectedQuestIds.add(hit.questId());
            }
            selectionTransforms.beginDrag(localX, localY, byQuestId);
        } else {
            state.draggingSelection = false;
            state.resizingSelection = false;
            state.rotatingSelection = false;
            state.transientQuestPositions.clear();
            state.transientQuestScales.clear();
            state.boxSelecting = true;
            state.boxAdditive = canvasViewport.shiftDown();
            state.boxStartX = localX;
            state.boxStartY = localY;
            state.boxCurrentX = localX;
            state.boxCurrentY = localY;
            if (!state.boxAdditive) {
                CanvasRenderer.clearCanvasSelection(state);
            }
        }
        refresher.run();
    }

    private static boolean handleElementTransformStart(
            CanvasViewport canvasViewport,
            TabletUiState state,
            Runnable refresher,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            int localX,
            int localY,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit
    ) {
        boolean textTransformHandleHit = textHit != null
                && (CanvasRenderer.isCanvasTextResizeHandleHit(state, textHit, localX, localY)
                || CanvasRenderer.isCanvasTextRotateHandleHit(state, textHit, localX, localY));
        boolean imageTransformHandleHit = imageHit != null
                && (CanvasRenderer.isCanvasImageResizeHandleHit(state, imageHit, localX, localY)
                || CanvasRenderer.isCanvasImageRotateHandleHit(state, imageHit, localX, localY));
        if (textTransformHandleHit) {
            state.canvasTextLastClickId = "";
            elementTransforms.beginTextTransform(textHit, localX, localY);
            refresher.run();
            return true;
        }
        if (imageTransformHandleHit) {
            elementTransforms.beginImageTransform(imageHit, localX, localY);
            refresher.run();
            return true;
        }
        boolean questResizeTransform = !state.selectedQuestIds.isEmpty();
        boolean questRotateTransform = state.selectedQuestIds.size() > 1;
        if (questRotateTransform && CanvasRenderer.isSelectionRotateHandleHit(state, localX, localY)) {
            selectionTransforms.beginRotate(localX, localY, byQuestId);
            refresher.run();
            return true;
        }
        if (questResizeTransform && CanvasRenderer.isSelectionResizeHandleHit(state, localX, localY)) {
            selectionTransforms.beginResize(localX, localY, byQuestId);
            refresher.run();
            return true;
        }
        if (CanvasRenderer.totalCanvasSelectionCount(state) > 1 && CanvasRenderer.isSelectionBoundsHit(state, localX, localY)) {
            selectionTransforms.beginDrag(localX, localY, byQuestId);
            refresher.run();
            return true;
        }
        if (textHit != null) {
            if (canvasViewport.shiftDown()) {
                CanvasBoxSelectionController.toggleCanvasTextSelection(state, textHit.id());
                refresher.run();
                return true;
            }
            long now = System.currentTimeMillis();
            boolean doubleClick = textHit.id().equals(state.canvasTextLastClickId) && now - state.canvasTextLastClickAtMs <= 350L;
            state.canvasTextLastClickId = textHit.id();
            state.canvasTextLastClickAtMs = now;
            if (doubleClick) {
                textEditor.begin(textHit);
                refresher.run();
                return true;
            }
            elementTransforms.beginTextTransform(textHit, localX, localY);
            refresher.run();
            return true;
        }
        if (imageHit != null) {
            if (canvasViewport.shiftDown()) {
                CanvasBoxSelectionController.toggleCanvasImageSelection(state, imageHit.id());
                refresher.run();
                return true;
            }
            elementTransforms.beginImageTransform(imageHit, localX, localY);
            refresher.run();
            return true;
        }
        return false;
    }
}

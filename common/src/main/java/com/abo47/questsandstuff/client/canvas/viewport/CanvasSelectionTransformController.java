package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.List;
import java.util.Map;

public final class CanvasSelectionTransformController {
    private final TabletUiState state;
    private final CanvasSelectionDragController dragController;
    private final CanvasSelectionResizeController resizeController;
    private final CanvasSelectionRotateController rotateController;

    public CanvasSelectionTransformController(TabletUiState state, CanvasElementTransformController elementTransforms) {
        this.state = state;
        this.dragController = new CanvasSelectionDragController(state, elementTransforms);
        this.resizeController = new CanvasSelectionResizeController(state);
        this.rotateController = new CanvasSelectionRotateController(state);
    }

    public void beginDrag(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        dragController.beginDrag(localX, localY, byQuestId);
    }

    public void updateDrag(int localX, int localY, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId) {
        updateDrag(localX, localY, cards, byQuestId, false);
    }

    public void updateDrag(int localX, int localY, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, boolean deferQuestPositions) {
        dragController.updateDrag(localX, localY, cards, deferQuestPositions);
    }

    public void populateDragPositions() {
        dragController.populateTransientQuestPositions();
    }

    public void beginResize(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        resizeController.beginResize(localX, localY, byQuestId);
    }

    public void updateResize(int localX, int localY) {
        resizeController.updateResize(localX, localY);
    }

    public void beginRotate(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        rotateController.beginRotate(localX, localY, byQuestId);
    }

    public void updateRotate(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        rotateController.updateRotate(localX, localY, byQuestId);
    }

    public void clear() {
        state.dragStartPositions.clear();
        state.dragStartImagePositions.clear();
        state.dragStartTextPositions.clear();
        state.resizeStartScales.clear();
        state.resizeStartPositions.clear();
        state.resizeStartImageLayers.clear();
        state.resizeStartTextLayers.clear();
        state.rotateStartPositions.clear();
        state.rotateStartCenters.clear();
        state.rotateStartImageLayers.clear();
        state.rotateStartTextLayers.clear();
        state.rotateStartBoundsLeft = 0;
        state.rotateStartBoundsTop = 0;
        state.rotateStartBoundsRight = 0;
        state.rotateStartBoundsBottom = 0;
        state.rotatePreviewAngle = 0.0;
        state.dragSelectionDeltaX = 0;
        state.dragSelectionDeltaY = 0;
        state.dragStartBoundsLeft = 0;
        state.dragStartBoundsTop = 0;
        state.dragStartBoundsRight = 0;
        state.dragStartBoundsBottom = 0;
        state.dragStartSelectionLeft = 0;
        state.dragStartSelectionTop = 0;
        state.dragStartSelectionRight = 0;
        state.dragStartSelectionBottom = 0;
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
        state.transientQuestPositions.clear();
        state.transientQuestScales.clear();
    }

}

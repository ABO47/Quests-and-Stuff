package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
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
        CanvasTransformSessions.clearMainCanvasSession(state);
    }

}

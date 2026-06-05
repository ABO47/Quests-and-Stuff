package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasSelectionRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

final class CanvasSelectionDragController {
    private final TabletUiState state;
    private final CanvasElementTransformController elementTransforms;

    CanvasSelectionDragController(TabletUiState state, CanvasElementTransformController elementTransforms) {
        this.state = state;
        this.elementTransforms = elementTransforms;
    }

    void beginDrag(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        state.draggingSelection = true;
        state.resizingSelection = false;
        state.rotatingSelection = false;
        state.dragStartX = localX;
        state.dragStartY = localY;
        state.dragCurrentX = localX;
        state.dragCurrentY = localY;
        state.dragSelectionDeltaX = 0;
        state.dragSelectionDeltaY = 0;
        state.transientQuestPositions.clear();
        state.transientQuestScales.clear();
        state.dragStartPositions.clear();
        state.dragStartImagePositions.clear();
        state.dragStartTextPositions.clear();
        CanvasRenderer.clearTransientCanvasTransforms(state);
        for (String questId : state.selectedQuestIds) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card != null) {
                state.dragStartPositions.put(questId, new CanvasPoint(card.logicalX(), card.logicalY()));
            }
        }
        String group = TabletUiFactory.selectedGroupName(state);
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (CanvasRenderer.isImageSelected(state, image.id())) {
                state.dragStartImagePositions.put(image.id(), new CanvasPoint(image.x(), image.y()));
            }
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (CanvasRenderer.isTextSelected(state, text.id())) {
                state.dragStartTextPositions.put(text.id(), new CanvasPoint(text.x(), text.y()));
            }
        }
        CanvasSelectionRenderer.updateSelectionBounds(state, List.copyOf(byQuestId.values()));
        CanvasSmartSnapper.Bounds bounds = CanvasSelectionBounds.currentSelectionBounds(state, elementTransforms, byQuestId, group);
        state.dragStartBoundsLeft = bounds.left();
        state.dragStartBoundsTop = bounds.top();
        state.dragStartBoundsRight = bounds.right();
        state.dragStartBoundsBottom = bounds.bottom();
        state.dragStartSelectionLeft = state.selectionBoundsLeft;
        state.dragStartSelectionTop = state.selectionBoundsTop;
        state.dragStartSelectionRight = state.selectionBoundsRight;
        state.dragStartSelectionBottom = state.selectionBoundsBottom;
        QuestsAndStuffMod.debugLog(
                "[QnS:UI] canvas selection drag start quests={} images={} texts={} bounds={}x{}",
                state.dragStartPositions.size(),
                state.dragStartImagePositions.size(),
                state.dragStartTextPositions.size(),
                Math.max(0, state.dragStartBoundsRight - state.dragStartBoundsLeft),
                Math.max(0, state.dragStartBoundsBottom - state.dragStartBoundsTop)
        );
    }

    void updateDrag(int localX, int localY, List<QuestCardLayout> cards, boolean deferQuestPositions) {
        int dx = (int) Math.round(CanvasGeometry.screenToLogicalX(state, localX) - CanvasGeometry.screenToLogicalX(state, state.dragStartX));
        int dy = (int) Math.round(CanvasGeometry.screenToLogicalY(state, localY) - CanvasGeometry.screenToLogicalY(state, state.dragStartY));
        state.dragCurrentX = localX;
        state.dragCurrentY = localY;
        CanvasPoint delta = snappedSelectionDelta(dx, dy);
        CanvasSmartSnapper.SnapResult snap = smartSnapSelectionDelta(delta.x, delta.y, cards);
        if (snap.hasOffset()) {
            int requestedDx = delta.x + snap.offsetX();
            int requestedDy = delta.y + snap.offsetY();
            CanvasPoint clamped = CanvasSelectionBounds.clampSelectionDelta(state, requestedDx, requestedDy);
            if (clamped.x != requestedDx || clamped.y != requestedDy) {
                clearSnapGuides();
            }
            delta = clamped;
        }
        applySelectionDragDelta(delta.x, delta.y, deferQuestPositions);
    }

    void populateTransientQuestPositions() {
        populateTransientQuestPositions(state.dragSelectionDeltaX, state.dragSelectionDeltaY);
    }

    private void applySelectionDragDelta(int dx, int dy, boolean deferQuestPositions) {
        state.dragSelectionDeltaX = dx;
        state.dragSelectionDeltaY = dy;
        if (deferQuestPositions) {
            state.transientQuestPositions.clear();
        } else {
            populateTransientQuestPositions(dx, dy);
        }
        state.transientQuestScales.clear();
        String group = TabletUiFactory.selectedGroupName(state);
        for (Map.Entry<String, CanvasPoint> entry : state.dragStartImagePositions.entrySet()) {
            CanvasImageLayer image = elementTransforms.findImage(group, entry.getKey());
            if (image != null) {
                CanvasRenderer.putTransientCanvasImage(state, image.moveTo(entry.getValue().x + dx, entry.getValue().y + dy));
            }
        }
        for (Map.Entry<String, CanvasPoint> entry : state.dragStartTextPositions.entrySet()) {
            CanvasTextLayer text = CanvasRenderer.findCanvasText(state, group, entry.getKey());
            if (text != null) {
                CanvasRenderer.putTransientCanvasText(state, text.moveTo(entry.getValue().x + dx, entry.getValue().y + dy));
            }
        }
    }

    private void populateTransientQuestPositions(int dx, int dy) {
        state.transientQuestPositions.clear();
        for (Map.Entry<String, CanvasPoint> entry : state.dragStartPositions.entrySet()) {
            state.transientQuestPositions.put(entry.getKey(), new CanvasPoint(entry.getValue().x + dx, entry.getValue().y + dy));
        }
    }

    private CanvasPoint snappedSelectionDelta(int dx, int dy) {
        int nextDx = dx;
        int nextDy = dy;
        if (state.gridSnapLocked) {
            nextDx = snapDelta(dx);
            nextDy = snapDelta(dy);
        }
        return CanvasSelectionBounds.clampSelectionDelta(state, nextDx, nextDy);
    }

    private int snapDelta(int delta) {
        int grid = CanvasGeometry.gridSize(state);
        return Math.round((float) delta / (float) grid) * grid;
    }

    private CanvasSmartSnapper.SnapResult smartSnapSelectionDelta(int dx, int dy, List<QuestCardLayout> cards) {
        String group = TabletUiFactory.selectedGroupName(state);
        return CanvasSmartSnapper.snap(
                state,
                CanvasSelectionBounds.translatedDragStartBounds(state, dx, dy),
                cards,
                group,
                state.selectedQuestIds,
                state.dragStartImagePositions.keySet(),
                state.dragStartTextPositions.keySet()
        );
    }

    private void clearSnapGuides() {
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
    }
}

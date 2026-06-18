package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasSelectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.quest.canvas.transform.LayerTransformEngine;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
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
        CanvasTransformSessions.clearMainCanvasSession(state);
        state.canvas.draggingSelection = true;
        state.canvas.resizingSelection = false;
        state.canvas.rotatingSelection = false;
        state.canvas.dragStartX = localX;
        state.canvas.dragStartY = localY;
        state.canvas.dragCurrentX = localX;
        state.canvas.dragCurrentY = localY;
        state.canvas.dragSelectionDeltaX = 0;
        state.canvas.dragSelectionDeltaY = 0;
        for (String questId : state.canvas.canvasSelection.questIds()) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card != null) {
                state.canvas.dragStartPositions.put(questId, new CanvasPoint(card.logicalX(), card.logicalY()));
            }
        }
        String group = TabletStateQueries.selectedGroupName(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (CanvasSelectionActions.isImageSelected(state, image.id())) {
                state.canvas.dragStartImagePositions.put(image.id(), new CanvasPoint(image.x(), image.y()));
            }
        }
        for (CanvasTextLayer text : state.canvas.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (CanvasSelectionActions.isTextSelected(state, text.id())) {
                state.canvas.dragStartTextPositions.put(text.id(), new CanvasPoint(text.x(), text.y()));
            }
        }
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            if (CanvasSelectionActions.isExclusiveChoiceSelected(state, ec.id())) {
                state.canvas.dragStartEcLayers.put(ec.id(), ec);
            }
        }
        CanvasSelectionRenderer.updateSelectionBounds(state, List.copyOf(byQuestId.values()));
        CanvasSnapEngine.Bounds bounds = CanvasSelectionBounds.currentSelectionBounds(state, elementTransforms, byQuestId, group);
        state.canvas.dragStartBoundsLeft = bounds.left();
        state.canvas.dragStartBoundsTop = bounds.top();
        state.canvas.dragStartBoundsRight = bounds.right();
        state.canvas.dragStartBoundsBottom = bounds.bottom();
        state.canvas.dragStartSelectionLeft = state.canvas.selectionBoundsLeft;
        state.canvas.dragStartSelectionTop = state.canvas.selectionBoundsTop;
        state.canvas.dragStartSelectionRight = state.canvas.selectionBoundsRight;
        state.canvas.dragStartSelectionBottom = state.canvas.selectionBoundsBottom;
        QuestsAndStuffMod.debugLog(
                "[QnS:UI] canvas selection drag start quests={} images={} texts={} ecs={} bounds={}x{}",
                state.canvas.dragStartPositions.size(),
                state.canvas.dragStartImagePositions.size(),
                state.canvas.dragStartTextPositions.size(),
                state.canvas.dragStartEcLayers.size(),
                Math.max(0, state.canvas.dragStartBoundsRight - state.canvas.dragStartBoundsLeft),
                Math.max(0, state.canvas.dragStartBoundsBottom - state.canvas.dragStartBoundsTop)
        );
    }

    void updateDrag(int localX, int localY, List<QuestCardLayout> cards, boolean deferQuestPositions) {
        int dx = (int) Math.round(CanvasGeometry.screenToLogicalX(state, localX) - CanvasGeometry.screenToLogicalX(state, state.canvas.dragStartX));
        int dy = (int) Math.round(CanvasGeometry.screenToLogicalY(state, localY) - CanvasGeometry.screenToLogicalY(state, state.canvas.dragStartY));
        state.canvas.dragCurrentX = localX;
        state.canvas.dragCurrentY = localY;
        CanvasPoint delta = snappedSelectionDelta(dx, dy);
        CanvasSnapEngine.SnapResult snap = smartSnapSelectionDelta(delta.x, delta.y, cards);
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
        populateTransientQuestPositions(state.canvas.dragSelectionDeltaX, state.canvas.dragSelectionDeltaY);
    }

    private void applySelectionDragDelta(int dx, int dy, boolean deferQuestPositions) {
        state.canvas.dragSelectionDeltaX = dx;
        state.canvas.dragSelectionDeltaY = dy;
        if (deferQuestPositions) {
            state.canvas.transientQuestPositions.clear();
        } else {
            populateTransientQuestPositions(dx, dy);
        }
        state.canvas.transientQuestScales.clear();
        String group = TabletStateQueries.selectedGroupName(state);
        for (Map.Entry<String, CanvasPoint> entry : state.canvas.dragStartImagePositions.entrySet()) {
            CanvasImageLayer image = elementTransforms.findImage(group, entry.getKey());
            if (image != null) {
                CanvasLayerMutations.putTransientCanvasImage(state, image.moveTo(entry.getValue().x + dx, entry.getValue().y + dy));
            }
        }
        for (Map.Entry<String, CanvasPoint> entry : state.canvas.dragStartTextPositions.entrySet()) {
            CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, entry.getKey());
            if (text != null) {
                CanvasLayerMutations.putTransientCanvasText(state, text.moveTo(entry.getValue().x + dx, entry.getValue().y + dy));
            }
        }
        for (Map.Entry<String, CanvasExclusiveChoice> entry : state.canvas.dragStartEcLayers.entrySet()) {
            CanvasExclusiveChoice ec = entry.getValue();
            CanvasLayerMutations.putTransientCanvasExclusiveChoice(state, ec.moveTo(ec.x() + dx, ec.y() + dy));
        }
    }

    private void populateTransientQuestPositions(int dx, int dy) {
        state.canvas.transientQuestPositions.clear();
        for (Map.Entry<String, CanvasPoint> entry : state.canvas.dragStartPositions.entrySet()) {
            state.canvas.transientQuestPositions.put(entry.getKey(), new CanvasPoint(entry.getValue().x + dx, entry.getValue().y + dy));
        }
    }

    private CanvasPoint snappedSelectionDelta(int dx, int dy) {
        CanvasPoint delta = LayerTransformEngine.freeDelta(
                dx,
                dy,
                new LayerTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), state.canvas.gridSnapLocked, false)
        );
        return CanvasSelectionBounds.clampSelectionDelta(state, delta.x, delta.y);
    }

    private CanvasSnapEngine.SnapResult smartSnapSelectionDelta(int dx, int dy, List<QuestCardLayout> cards) {
        String group = TabletStateQueries.selectedGroupName(state);
        return CanvasSmartSnapper.snap(
                state,
                CanvasSelectionBounds.translatedDragStartBounds(state, dx, dy),
                cards,
                group,
                state.canvas.canvasSelection.questIds(),
                state.canvas.dragStartImagePositions.keySet(),
                state.canvas.dragStartTextPositions.keySet()
        );
    }

    private void clearSnapGuides() {
        state.canvas.snapGuideXVisible = false;
        state.canvas.snapGuideYVisible = false;
    }
}

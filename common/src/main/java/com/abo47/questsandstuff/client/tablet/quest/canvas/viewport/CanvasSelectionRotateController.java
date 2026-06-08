package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerGroupTransform;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.Map;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

final class CanvasSelectionRotateController {
    private final TabletUiState state;

    CanvasSelectionRotateController(TabletUiState state) {
        this.state = state;
    }

    void beginRotate(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        state.canvas.draggingSelection = false;
        state.canvas.resizingSelection = false;
        state.canvas.rotatingSelection = true;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (String questId : state.canvas.canvasSelection.questIds()) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            state.canvas.rotateStartPositions.put(questId, new CanvasPoint(card.logicalX(), card.logicalY()));
            state.canvas.rotateStartCenters.put(questId, new CanvasDoublePoint(card.logicalCenterX(), card.logicalCenterY()));
            minX = Math.min(minX, card.visualLogicalX());
            minY = Math.min(minY, card.visualLogicalY());
            maxX = Math.max(maxX, card.logicalRight());
            maxY = Math.max(maxY, card.logicalBottom());
        }
        CanvasSelectionSnapshot snapshot = CanvasSelectionSnapshot.capture(state, TabletStateQueries.selectedGroupName(state), byQuestId);
        state.canvas.rotateStartImageLayers.putAll(snapshot.images());
        state.canvas.rotateStartTextLayers.putAll(snapshot.texts());
        if (snapshot.hasBounds()) {
            minX = Math.min(minX, snapshot.left());
            minY = Math.min(minY, snapshot.top());
            maxX = Math.max(maxX, snapshot.right());
            maxY = Math.max(maxY, snapshot.bottom());
        }
        if (minX == Integer.MAX_VALUE) {
            state.canvas.rotatePivotX = 0.0;
            state.canvas.rotatePivotY = 0.0;
            state.canvas.rotateStartAngle = 0.0;
            state.canvas.rotateStartBoundsLeft = 0;
            state.canvas.rotateStartBoundsTop = 0;
            state.canvas.rotateStartBoundsRight = TabletUiFactory.CARD_W;
            state.canvas.rotateStartBoundsBottom = TabletUiFactory.CARD_H;
            return;
        }
        state.canvas.rotateStartBoundsLeft = minX;
        state.canvas.rotateStartBoundsTop = minY;
        state.canvas.rotateStartBoundsRight = maxX;
        state.canvas.rotateStartBoundsBottom = maxY;
        state.canvas.rotatePivotX = (minX + maxX) / 2.0;
        state.canvas.rotatePivotY = (minY + maxY) / 2.0;
        double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
        double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
        state.canvas.rotateStartAngle = Math.atan2(logicalMouseY - state.canvas.rotatePivotY, logicalMouseX - state.canvas.rotatePivotX);
        state.canvas.rotatePreviewAngle = 0.0;
    }

    void updateRotate(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
        double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
        double currentAngle = Math.atan2(logicalMouseY - state.canvas.rotatePivotY, logicalMouseX - state.canvas.rotatePivotX);
        double delta = currentAngle - state.canvas.rotateStartAngle;
        if (isShiftDown()) {
            double snap = Math.PI / 12.0;
            delta = Math.round(delta / snap) * snap;
        }
        state.canvas.rotatePreviewAngle = delta;
        double cos = Math.cos(delta);
        double sin = Math.sin(delta);

        state.canvas.transientQuestPositions.clear();
        for (Map.Entry<String, CanvasDoublePoint> entry : state.canvas.rotateStartCenters.entrySet()) {
            String questId = entry.getKey();
            CanvasDoublePoint center = entry.getValue();
            QuestCardLayout card = byQuestId.get(questId);
            if (center == null || card == null) {
                continue;
            }
            double relX = center.x() - state.canvas.rotatePivotX;
            double relY = center.y() - state.canvas.rotatePivotY;
            double rotatedCenterX = state.canvas.rotatePivotX + relX * cos - relY * sin;
            double rotatedCenterY = state.canvas.rotatePivotY + relX * sin + relY * cos;
            float scale = CanvasSelectionBounds.scaleForQuest(questId, byQuestId);
            CanvasPoint anchor = CanvasGeometry.anchorForVisualCenter(state, rotatedCenterX, rotatedCenterY, scale);
            int nx = TabletUiFactory.snapToGrid(state, anchor.x);
            int ny = TabletUiFactory.snapToGrid(state, anchor.y);
            CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                    state,
                    nx,
                    ny,
                    CanvasGeometry.slotLogicalWidth(state, scale),
                    CanvasGeometry.slotLogicalHeight(state, scale)
            );
            state.canvas.transientQuestPositions.put(questId, new CanvasPoint(clamped.x, clamped.y));
        }
        CanvasLayerSelectionSnapshot layerSnapshot = new CanvasLayerSelectionSnapshot(
                state.canvas.rotateStartBoundsLeft,
                state.canvas.rotateStartBoundsTop,
                state.canvas.rotateStartBoundsRight,
                state.canvas.rotateStartBoundsBottom,
                state.canvas.rotateStartImageLayers,
                state.canvas.rotateStartTextLayers
        );
        CanvasLayerGroupTransform.Result result = CanvasLayerGroupTransform.rotate(
                layerSnapshot,
                state.canvas.rotatePivotX,
                state.canvas.rotatePivotY,
                delta,
                (x, y, width, height, pivotX, pivotY, rotation) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, width, height, pivotX, pivotY, rotation)
        );
        for (CanvasImageLayer image : result.images().values()) {
            CanvasLayerMutations.putTransientCanvasImage(state, clampRotationPreviewImage(image));
        }
        for (CanvasTextLayer text : result.texts().values()) {
            CanvasLayerMutations.putTransientCanvasText(state, clampRotationPreviewText(text));
        }
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedImage(state, image) : image;
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }

    private CanvasImageLayer clampRotationPreviewImage(CanvasImageLayer image) {
        CanvasImageLayer preview = shouldFitRotatedPreview(image.rotation())
                ? fittedImageIfGridLocked(image)
                : image;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), preview.pivotX(), preview.pivotY(), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    private CanvasTextLayer clampRotationPreviewText(CanvasTextLayer text) {
        CanvasTextLayer preview = shouldFitRotatedPreview(text.rotation())
                ? fittedTextIfGridLocked(text)
                : text;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), preview.w() / 2, preview.h() / 2, preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    private boolean shouldFitRotatedPreview(int rotation) {
        return state.canvas.gridSnapLocked && isShiftDown() && CanvasGeometry.isCardinalTurn(rotation);
    }
}

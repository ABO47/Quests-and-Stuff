package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerGroupTransform;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
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
        state.draggingSelection = false;
        state.resizingSelection = false;
        state.rotatingSelection = true;
        state.rotateStartPositions.clear();
        state.rotateStartCenters.clear();
        state.rotateStartImageLayers.clear();
        state.rotateStartTextLayers.clear();
        CanvasRenderer.clearTransientCanvasTransforms(state);
        state.transientQuestPositions.clear();
        state.transientQuestScales.clear();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (String questId : state.selectedQuestIds) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            state.rotateStartPositions.put(questId, new CanvasPoint(card.logicalX(), card.logicalY()));
            state.rotateStartCenters.put(questId, new CanvasDoublePoint(card.logicalCenterX(), card.logicalCenterY()));
            minX = Math.min(minX, card.visualLogicalX());
            minY = Math.min(minY, card.visualLogicalY());
            maxX = Math.max(maxX, card.logicalRight());
            maxY = Math.max(maxY, card.logicalBottom());
        }
        CanvasSelectionSnapshot snapshot = CanvasSelectionSnapshot.capture(state, TabletUiFactory.selectedGroupName(state), byQuestId);
        state.rotateStartImageLayers.putAll(snapshot.images());
        state.rotateStartTextLayers.putAll(snapshot.texts());
        if (snapshot.hasBounds()) {
            minX = Math.min(minX, snapshot.left());
            minY = Math.min(minY, snapshot.top());
            maxX = Math.max(maxX, snapshot.right());
            maxY = Math.max(maxY, snapshot.bottom());
        }
        if (minX == Integer.MAX_VALUE) {
            state.rotatePivotX = 0.0;
            state.rotatePivotY = 0.0;
            state.rotateStartAngle = 0.0;
            state.rotateStartBoundsLeft = 0;
            state.rotateStartBoundsTop = 0;
            state.rotateStartBoundsRight = TabletUiFactory.CARD_W;
            state.rotateStartBoundsBottom = TabletUiFactory.CARD_H;
            return;
        }
        state.rotateStartBoundsLeft = minX;
        state.rotateStartBoundsTop = minY;
        state.rotateStartBoundsRight = maxX;
        state.rotateStartBoundsBottom = maxY;
        state.rotatePivotX = (minX + maxX) / 2.0;
        state.rotatePivotY = (minY + maxY) / 2.0;
        double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
        double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
        state.rotateStartAngle = Math.atan2(logicalMouseY - state.rotatePivotY, logicalMouseX - state.rotatePivotX);
        state.rotatePreviewAngle = 0.0;
    }

    void updateRotate(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
        double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
        double currentAngle = Math.atan2(logicalMouseY - state.rotatePivotY, logicalMouseX - state.rotatePivotX);
        double delta = currentAngle - state.rotateStartAngle;
        if (isShiftDown()) {
            double snap = Math.PI / 12.0;
            delta = Math.round(delta / snap) * snap;
        }
        state.rotatePreviewAngle = delta;
        double cos = Math.cos(delta);
        double sin = Math.sin(delta);

        state.transientQuestPositions.clear();
        for (Map.Entry<String, CanvasDoublePoint> entry : state.rotateStartCenters.entrySet()) {
            String questId = entry.getKey();
            CanvasDoublePoint center = entry.getValue();
            QuestCardLayout card = byQuestId.get(questId);
            if (center == null || card == null) {
                continue;
            }
            double relX = center.x() - state.rotatePivotX;
            double relY = center.y() - state.rotatePivotY;
            double rotatedCenterX = state.rotatePivotX + relX * cos - relY * sin;
            double rotatedCenterY = state.rotatePivotY + relX * sin + relY * cos;
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
            state.transientQuestPositions.put(questId, new CanvasPoint(clamped.x, clamped.y));
        }
        CanvasLayerSelectionSnapshot layerSnapshot = new CanvasLayerSelectionSnapshot(
                state.rotateStartBoundsLeft,
                state.rotateStartBoundsTop,
                state.rotateStartBoundsRight,
                state.rotateStartBoundsBottom,
                state.rotateStartImageLayers,
                state.rotateStartTextLayers
        );
        CanvasLayerGroupTransform.Result result = CanvasLayerGroupTransform.rotate(
                layerSnapshot,
                state.rotatePivotX,
                state.rotatePivotY,
                delta,
                (x, y, width, height, pivotX, pivotY, rotation) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, width, height, pivotX, pivotY, rotation)
        );
        for (CanvasImageLayer image : result.images().values()) {
            CanvasRenderer.putTransientCanvasImage(state, clampRotationPreviewImage(image));
        }
        for (CanvasTextLayer text : result.texts().values()) {
            CanvasRenderer.putTransientCanvasText(state, clampRotationPreviewText(text));
        }
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.gridSnapLocked ? CanvasGridFitController.fittedImage(state, image) : image;
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
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
        return state.gridSnapLocked && isShiftDown() && CanvasGeometry.isCardinalTurn(rotation);
    }
}

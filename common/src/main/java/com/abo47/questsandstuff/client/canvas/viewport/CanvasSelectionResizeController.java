package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.selection.CanvasGroupResizeTransform;
import com.abo47.questsandstuff.client.canvas.selection.CanvasLayerSelectionSnapshot;
import com.abo47.questsandstuff.client.canvas.selection.CanvasSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.Map;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

final class CanvasSelectionResizeController {
    private final TabletUiState state;

    CanvasSelectionResizeController(TabletUiState state) {
        this.state = state;
    }

    void beginResize(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        state.draggingSelection = false;
        state.resizingSelection = true;
        state.rotatingSelection = false;
        state.resizeStartMouseX = CanvasSelectionBounds.toLogicalX(state, localX);
        state.resizeStartMouseY = CanvasSelectionBounds.toLogicalY(state, localY);
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        state.resizeStartScales.clear();
        state.resizeStartPositions.clear();
        state.resizeStartImageLayers.clear();
        state.resizeStartTextLayers.clear();
        state.transientQuestPositions.clear();
        state.transientQuestScales.clear();
        for (String questId : state.selectedQuestIds) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.visualLogicalX());
            minY = Math.min(minY, card.visualLogicalY());
            maxX = Math.max(maxX, card.logicalRight());
            maxY = Math.max(maxY, card.logicalBottom());
            state.resizeStartPositions.put(questId, new CanvasPoint(card.logicalX(), card.logicalY()));
            state.resizeStartScales.put(questId, CanvasSelectionBounds.scaleForQuest(questId, byQuestId));
        }
        CanvasSelectionSnapshot snapshot = CanvasSelectionSnapshot.capture(state, TabletUiFactory.selectedGroupName(state), byQuestId);
        state.resizeStartImageLayers.putAll(snapshot.images());
        state.resizeStartTextLayers.putAll(snapshot.texts());
        if (snapshot.hasBounds()) {
            minX = Math.min(minX, snapshot.left());
            minY = Math.min(minY, snapshot.top());
            maxX = Math.max(maxX, snapshot.right());
            maxY = Math.max(maxY, snapshot.bottom());
        }
        if (minX == Integer.MAX_VALUE) {
            state.resizeStartLeft = 0;
            state.resizeStartTop = 0;
            state.resizeStartRight = TabletUiFactory.CARD_W;
            state.resizeStartBottom = TabletUiFactory.CARD_H;
            return;
        }
        state.resizeStartLeft = minX;
        state.resizeStartTop = minY;
        state.resizeStartRight = maxX;
        state.resizeStartBottom = maxY;
    }

    void updateResize(int localX, int localY) {
        int logicalMouseX = CanvasSelectionBounds.toLogicalX(state, localX);
        int logicalMouseY = CanvasSelectionBounds.toLogicalY(state, localY);
        CanvasLayerSelectionSnapshot layerSnapshot = new CanvasLayerSelectionSnapshot(
                state.resizeStartLeft,
                state.resizeStartTop,
                state.resizeStartRight,
                state.resizeStartBottom,
                state.resizeStartImageLayers,
                state.resizeStartTextLayers
        );
        CanvasGroupResizeTransform.Result resize = CanvasGroupResizeTransform.resizeBottomRight(
                layerSnapshot,
                logicalMouseX,
                logicalMouseY,
                resizeConstraints()
        );

        state.transientQuestPositions.clear();
        state.transientQuestScales.clear();
        for (Map.Entry<String, Float> entry : state.resizeStartScales.entrySet()) {
            String questId = entry.getKey();
            Float baseScale = entry.getValue();
            CanvasPoint basePos = state.resizeStartPositions.get(questId);
            if (baseScale == null || basePos == null) {
                continue;
            }
            float targetScale = Math.max(0.5f, (float) (baseScale * resize.uniformScale()));
            int baseVisualX = basePos.x + CanvasGeometry.visualInsetX(state, baseScale);
            int baseVisualY = basePos.y + CanvasGeometry.visualInsetY(state, baseScale);
            double baseCenterX = baseVisualX + CanvasGeometry.visualLogicalWidth(baseScale) / 2.0;
            double baseCenterY = baseVisualY + CanvasGeometry.visualLogicalHeight(baseScale) / 2.0;
            double targetCenterX = resize.bounds().left() + (baseCenterX - state.resizeStartLeft) * resize.scaleX();
            double targetCenterY = resize.bounds().top() + (baseCenterY - state.resizeStartTop) * resize.scaleY();
            CanvasPoint anchor = CanvasGeometry.anchorForVisualCenter(state, targetCenterX, targetCenterY, targetScale);
            state.transientQuestPositions.put(questId, new CanvasPoint(anchor.x, anchor.y));
            state.transientQuestScales.put(questId, targetScale);
        }
        String group = TabletUiFactory.selectedGroupName(state);
        for (CanvasImageLayer image : resize.images().values()) {
            CanvasRenderer.putCanvasImage(state, group, image, false);
        }
        for (CanvasTextLayer text : resize.texts().values()) {
            CanvasRenderer.putCanvasText(state, group, text, false);
        }
    }

    private CanvasGroupResizeTransform.Constraints resizeConstraints() {
        int minimum = Math.max(4, CanvasGeometry.gridSize(state) / 2);
        int minLeft = state.gridCanvasLocked ? 0 : CanvasGroupResizeTransform.UNBOUNDED;
        int minTop = state.gridCanvasLocked ? 0 : CanvasGroupResizeTransform.UNBOUNDED;
        int maxRight = state.gridCanvasLocked ? state.canvasContentW : CanvasGroupResizeTransform.UNBOUNDED;
        int maxBottom = state.gridCanvasLocked ? state.canvasContentH : CanvasGroupResizeTransform.UNBOUNDED;
        return new CanvasGroupResizeTransform.Constraints(
                minimum,
                minimum,
                CanvasGeometry.gridSize(state),
                state.gridSnapLocked || isShiftDown(),
                isShiftDown() || isSingleQuestOnlyResize(),
                minLeft,
                minTop,
                maxRight,
                maxBottom
        );
    }

    private boolean isSingleQuestOnlyResize() {
        return state.resizeStartScales.size() == 1
                && state.resizeStartImageLayers.isEmpty()
                && state.resizeStartTextLayers.isEmpty();
    }
}

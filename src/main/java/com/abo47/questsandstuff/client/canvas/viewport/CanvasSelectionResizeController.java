package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

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
        int baseW = Math.max(1, state.resizeStartRight - state.resizeStartLeft);
        int baseH = Math.max(1, state.resizeStartBottom - state.resizeStartTop);
        int targetW = Math.max(4, logicalMouseX - state.resizeStartLeft);
        int targetH = Math.max(4, logicalMouseY - state.resizeStartTop);

        if (state.gridSnapLocked) {
            int step = CanvasGeometry.gridSize(state);
            targetW = Math.max(step, Math.round((float) targetW / (float) step) * step);
            targetH = Math.max(step, Math.round((float) targetH / (float) step) * step);
        }

        float factorX = (float) targetW / (float) baseW;
        float factorY = (float) targetH / (float) baseH;
        float factor;
        if (state.gridSnapLocked || isShiftDown()) {
            factor = Math.max(factorX, factorY);
        } else {
            int deltaW = Math.abs(targetW - baseW);
            int deltaH = Math.abs(targetH - baseH);
            factor = deltaW >= deltaH ? factorX : factorY;
        }
        factor = Math.max(0.5f, factor);

        state.transientQuestPositions.clear();
        state.transientQuestScales.clear();
        for (Map.Entry<String, Float> entry : state.resizeStartScales.entrySet()) {
            String questId = entry.getKey();
            Float baseScale = entry.getValue();
            CanvasPoint basePos = state.resizeStartPositions.get(questId);
            if (baseScale == null || basePos == null) {
                continue;
            }
            float targetScale = Math.max(0.5f, baseScale * factor);
            if (state.gridSnapLocked) {
                targetScale = CanvasGeometry.snapScaleToGrid(state, targetScale);
            }
            int baseVisualX = basePos.x + CanvasGeometry.visualInsetX(state, baseScale);
            int baseVisualY = basePos.y + CanvasGeometry.visualInsetY(state, baseScale);
            int targetVisualX = state.resizeStartLeft + Math.round((baseVisualX - state.resizeStartLeft) * factor);
            int targetVisualY = state.resizeStartTop + Math.round((baseVisualY - state.resizeStartTop) * factor);
            int nx = targetVisualX - CanvasGeometry.visualInsetX(state, targetScale);
            int ny = targetVisualY - CanvasGeometry.visualInsetY(state, targetScale);
            nx = TabletUiFactory.snapToGrid(state, nx);
            ny = TabletUiFactory.snapToGrid(state, ny);
            CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                    state,
                    nx,
                    ny,
                    CanvasGeometry.slotLogicalWidth(state, targetScale),
                    CanvasGeometry.slotLogicalHeight(state, targetScale)
            );
            state.transientQuestPositions.put(questId, new CanvasPoint(clamped.x, clamped.y));
            state.transientQuestScales.put(questId, targetScale);
        }
    }
}

package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.Map;

final class CanvasSelectionBounds {
    private CanvasSelectionBounds() {
    }

    static CanvasSmartSnapper.Bounds currentSelectionBounds(
            TabletUiState state,
            CanvasElementTransformController elementTransforms,
            Map<String, QuestCardLayout> byQuestId,
            String group
    ) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (String questId : state.dragStartPositions.keySet()) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.logicalX());
            minY = Math.min(minY, card.logicalY());
            maxX = Math.max(maxX, card.logicalX() + card.slotLogicalWidth());
            maxY = Math.max(maxY, card.logicalY() + card.slotLogicalHeight());
        }
        for (String imageId : state.dragStartImagePositions.keySet()) {
            CanvasImageLayer image = elementTransforms.findImage(group, imageId);
            if (image == null) {
                continue;
            }
            CanvasSmartSnapper.Bounds bounds = CanvasSmartSnapper.boundsForImage(state, image);
            minX = Math.min(minX, bounds.left());
            minY = Math.min(minY, bounds.top());
            maxX = Math.max(maxX, bounds.right());
            maxY = Math.max(maxY, bounds.bottom());
        }
        for (String textId : state.dragStartTextPositions.keySet()) {
            CanvasTextLayer text = CanvasRenderer.findCanvasText(state, group, textId);
            if (text == null) {
                continue;
            }
            CanvasSmartSnapper.Bounds bounds = CanvasSmartSnapper.boundsForText(state, text);
            minX = Math.min(minX, bounds.left());
            minY = Math.min(minY, bounds.top());
            maxX = Math.max(maxX, bounds.right());
            maxY = Math.max(maxY, bounds.bottom());
        }
        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || maxY == Integer.MIN_VALUE) {
            return new CanvasSmartSnapper.Bounds(0, 0, 0, 0);
        }
        return new CanvasSmartSnapper.Bounds(minX, minY, maxX, maxY);
    }

    static CanvasPoint clampSelectionDelta(TabletUiState state, int dx, int dy) {
        if (!state.gridCanvasLocked || !hasDragStartBounds(state)) {
            return new CanvasPoint(dx, dy);
        }
        int left = state.dragStartBoundsLeft + dx;
        int top = state.dragStartBoundsTop + dy;
        int right = state.dragStartBoundsRight + dx;
        int bottom = state.dragStartBoundsBottom + dy;
        if (left < 0) {
            dx -= left;
        }
        if (top < 0) {
            dy -= top;
        }
        if (right > state.canvasContentW) {
            dx -= right - state.canvasContentW;
        }
        if (bottom > state.canvasContentH) {
            dy -= bottom - state.canvasContentH;
        }
        return new CanvasPoint(dx, dy);
    }

    static CanvasSmartSnapper.Bounds translatedDragStartBounds(TabletUiState state, int dx, int dy) {
        if (!hasDragStartBounds(state)) {
            return new CanvasSmartSnapper.Bounds(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        return new CanvasSmartSnapper.Bounds(
                state.dragStartBoundsLeft + dx,
                state.dragStartBoundsTop + dy,
                state.dragStartBoundsRight + dx,
                state.dragStartBoundsBottom + dy
        );
    }

    static boolean hasDragStartBounds(TabletUiState state) {
        return state.dragStartBoundsRight > state.dragStartBoundsLeft && state.dragStartBoundsBottom > state.dragStartBoundsTop;
    }

    static int toLogicalX(TabletUiState state, int localX) {
        return CanvasGeometry.screenToNearestLogicalX(state, localX);
    }

    static int toLogicalY(TabletUiState state, int localY) {
        return CanvasGeometry.screenToNearestLogicalY(state, localY);
    }

    static float scaleForQuest(String questId, Map<String, QuestCardLayout> byQuestId) {
        QuestCardLayout card = byQuestId.get(questId);
        if (card == null || TabletUiFactory.CARD_W <= 0) {
            return 1.0f;
        }
        return Math.max(0.5f, card.scale());
    }
}

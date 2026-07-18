package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import java.util.Map;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

final class CanvasSelectionBounds {
    private CanvasSelectionBounds() {
    }

    static CanvasSnapEngine.Bounds currentSelectionBounds(
            TabletUiState state,
            CanvasElementTransformController elementTransforms,
            Map<String, QuestCardLayout> byQuestId,
            String chapter
    ) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (String questId : state.canvas.dragStartPositions.keySet()) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.logicalX());
            minY = Math.min(minY, card.logicalY());
            maxX = Math.max(maxX, card.logicalX() + card.slotLogicalWidth());
            maxY = Math.max(maxY, card.logicalY() + card.slotLogicalHeight());
        }
        for (String imageId : state.canvas.dragStartImagePositions.keySet()) {
            CanvasImageLayer image = elementTransforms.findImage(chapter, imageId);
            if (image == null) {
                continue;
            }
            CanvasSnapEngine.Bounds bounds = CanvasSmartSnapper.boundsForImage(state, image);
            minX = Math.min(minX, bounds.left());
            minY = Math.min(minY, bounds.top());
            maxX = Math.max(maxX, bounds.right());
            maxY = Math.max(maxY, bounds.bottom());
        }
        for (String textId : state.canvas.dragStartTextPositions.keySet()) {
            CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, chapter, textId);
            if (text == null) {
                continue;
            }
            CanvasSnapEngine.Bounds bounds = CanvasSmartSnapper.boundsForText(state, text);
            minX = Math.min(minX, bounds.left());
            minY = Math.min(minY, bounds.top());
            maxX = Math.max(maxX, bounds.right());
            maxY = Math.max(maxY, bounds.bottom());
        }
        for (CanvasExclusiveChoice ec : state.canvas.dragStartEcLayers.values()) {
            CanvasSnapEngine.Bounds ecBounds = CanvasSmartSnapper.boundsForExclusiveChoice(state, ec);
            minX = Math.min(minX, ecBounds.left());
            minY = Math.min(minY, ecBounds.top());
            maxX = Math.max(maxX, ecBounds.right());
            maxY = Math.max(maxY, ecBounds.bottom());
        }
        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || maxY == Integer.MIN_VALUE) {
            return new CanvasSnapEngine.Bounds(0, 0, 0, 0);
        }
        return new CanvasSnapEngine.Bounds(minX, minY, maxX, maxY);
    }

    static CanvasPoint clampSelectionDelta(TabletUiState state, int dx, int dy) {
        if (!hasDragStartBounds(state)) {
            return new CanvasPoint(dx, dy);
        }
        float zoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        int viewportLeft = (int) Math.floor((-state.canvas.canvasContentX - state.canvas.canvasOffsetX) / zoom);
        int viewportTop = (int) Math.floor((-state.canvas.canvasContentY - state.canvas.canvasOffsetY) / zoom);
        int viewportRight = (int) Math.ceil((state.canvas.canvasViewportW - state.canvas.canvasContentX - state.canvas.canvasOffsetX) / zoom);
        int viewportBottom = (int) Math.ceil((state.canvas.canvasViewportH - state.canvas.canvasContentY - state.canvas.canvasOffsetY) / zoom);
        int left = state.canvas.dragStartBoundsLeft + dx;
        int top = state.canvas.dragStartBoundsTop + dy;
        int right = state.canvas.dragStartBoundsRight + dx;
        int bottom = state.canvas.dragStartBoundsBottom + dy;
        if (state.canvas.gridCanvasLocked) {
            if (left < viewportLeft) {
                dx -= left - viewportLeft;
            }
            if (top < viewportTop) {
                dy -= top - viewportTop;
            }
            if (right > viewportRight) {
                dx -= right - viewportRight;
            }
            if (bottom > viewportBottom) {
                dy -= bottom - viewportBottom;
            }
        } else {
            int selMidX = (state.canvas.dragStartBoundsLeft + state.canvas.dragStartBoundsRight) / 2;
            int selMidY = (state.canvas.dragStartBoundsTop + state.canvas.dragStartBoundsBottom) / 2;
            int centerX = selMidX + dx;
            int centerY = selMidY + dy;
            if (centerX < viewportLeft) {
                dx += viewportLeft - centerX;
            }
            if (centerY < viewportTop) {
                dy += viewportTop - centerY;
            }
            if (centerX > viewportRight) {
                dx -= centerX - viewportRight;
            }
            if (centerY > viewportBottom) {
                dy -= centerY - viewportBottom;
            }
        }
        return new CanvasPoint(dx, dy);
    }

    static CanvasSnapEngine.Bounds translatedDragStartBounds(TabletUiState state, int dx, int dy) {
        if (!hasDragStartBounds(state)) {
            return CanvasSnapEngine.Bounds.invalid();
        }
        return new CanvasSnapEngine.Bounds(
                state.canvas.dragStartBoundsLeft + dx,
                state.canvas.dragStartBoundsTop + dy,
                state.canvas.dragStartBoundsRight + dx,
                state.canvas.dragStartBoundsBottom + dy
        );
    }

    static boolean hasDragStartBounds(TabletUiState state) {
        return state.canvas.dragStartBoundsRight > state.canvas.dragStartBoundsLeft && state.canvas.dragStartBoundsBottom > state.canvas.dragStartBoundsTop;
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

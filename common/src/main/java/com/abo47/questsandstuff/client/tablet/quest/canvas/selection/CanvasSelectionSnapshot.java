package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

public record CanvasSelectionSnapshot(
        CanvasSelectionSet selection,
        int left,
        int top,
        int right,
        int bottom,
        Map<String, CanvasImageLayer> images,
        Map<String, CanvasTextLayer> texts
) {
    public boolean hasBounds() {
        return right > left && bottom > top;
    }

    public static CanvasSelectionSnapshot capture(
            TabletUiState state,
            String group,
            Map<String, QuestCardLayout> byQuestId
    ) {
        CanvasSelectionSet selection = CanvasSelectionSet.current(state);
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (String questId : selection.questIds()) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.visualLogicalX());
            minY = Math.min(minY, card.visualLogicalY());
            maxX = Math.max(maxX, card.logicalRight());
            maxY = Math.max(maxY, card.logicalBottom());
        }

        CanvasLayerSelectionSnapshot layers = CanvasLayerSelectionSnapshot.capture(
                selection.imageIds(),
                selection.textIds(),
                state.canvas.canvasImagesByChapter.getOrDefault(group, List.of()),
                state.canvas.canvasTextsByChapter.getOrDefault(group, List.of())
        );
        if (layers.hasBounds()) {
            minX = Math.min(minX, layers.left());
            minY = Math.min(minY, layers.top());
            maxX = Math.max(maxX, layers.right());
            maxY = Math.max(maxY, layers.bottom());
        }

        if (minX == Integer.MAX_VALUE) {
            return new CanvasSelectionSnapshot(selection, 0, 0, 0, 0, layers.images(), layers.texts());
        }
        return new CanvasSelectionSnapshot(selection, minX, minY, maxX, maxY, layers.images(), layers.texts());
    }
}

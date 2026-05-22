package com.abo47.questsandstuff.client.canvas.selection;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;

public final class CanvasBoxSelectionController {
    private CanvasBoxSelectionController() {
    }

    public static void toggleCanvasImageSelection(TabletUiState state, String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return;
        }
        if (!state.selectedCanvasImageIds.add(imageId)) {
            state.selectedCanvasImageIds.remove(imageId);
            if (imageId.equals(state.selectedCanvasImageId)) {
                state.selectedCanvasImageId = state.selectedCanvasImageIds.stream().findFirst().orElse("");
            }
            return;
        }
        state.selectedCanvasImageId = imageId;
    }

    public static void toggleCanvasTextSelection(TabletUiState state, String textId) {
        if (textId == null || textId.isBlank()) {
            return;
        }
        if (!state.selectedCanvasTextIds.add(textId)) {
            state.selectedCanvasTextIds.remove(textId);
            if (textId.equals(state.selectedCanvasTextId)) {
                state.selectedCanvasTextId = state.selectedCanvasTextIds.stream().findFirst().orElse("");
            }
            return;
        }
        state.selectedCanvasTextId = textId;
    }

    public static void finishBoxSelection(TabletUiState state, List<QuestCardLayout> cards) {
        int minX = Math.min(state.boxStartX, state.boxCurrentX);
        int minY = Math.min(state.boxStartY, state.boxCurrentY);
        int maxX = Math.max(state.boxStartX, state.boxCurrentX);
        int maxY = Math.max(state.boxStartY, state.boxCurrentY);
        for (QuestCardLayout card : cards) {
            boolean intersects = card.x() < maxX && card.x() + card.width() > minX
                    && card.y() < maxY && card.y() + card.height() > minY;
            if (intersects) {
                state.selectedQuestIds.add(card.questId());
            }
        }
        String group = selectedGroupName(state);
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, image.x(), image.y(), image.w(), image.h(), image.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.selectedCanvasImageIds.add(image.id());
                state.selectedCanvasImageId = image.id();
            }
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.selectedCanvasTextIds.add(text.id());
                state.selectedCanvasTextId = text.id();
            }
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas mixed box selection total={}", CanvasRenderer.totalCanvasSelectionCount(state));
    }

    private static boolean intersects(int left, int top, int right, int bottom, int minX, int minY, int maxX, int maxY) {
        return left < maxX && right > minX && top < maxY && bottom > minY;
    }
}

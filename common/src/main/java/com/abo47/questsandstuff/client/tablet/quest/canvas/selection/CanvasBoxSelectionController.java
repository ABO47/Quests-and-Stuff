package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;

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

    public static void beginBoxSelection(TabletUiState state, boolean additive, int localX, int localY) {
        state.boxSelecting = true;
        state.boxAdditive = additive;
        state.boxStartX = localX;
        state.boxStartY = localY;
        state.boxCurrentX = localX;
        state.boxCurrentY = localY;
        captureBoxSelectionBase(state, additive);
        if (!additive) {
            CanvasSelectionActions.clearCanvasSelection(state);
        }
    }

    public static void updateBoxSelection(TabletUiState state, List<QuestCardLayout> cards) {
        int minX = Math.min(state.boxStartX, state.boxCurrentX);
        int minY = Math.min(state.boxStartY, state.boxCurrentY);
        int maxX = Math.max(state.boxStartX, state.boxCurrentX);
        int maxY = Math.max(state.boxStartY, state.boxCurrentY);
        state.selectedQuestIds.clear();
        state.selectedQuestIds.addAll(state.boxSelectionBaseQuestIds);
        for (QuestCardLayout card : cards) {
            boolean intersects = card.x() < maxX && card.x() + card.width() > minX
                    && card.y() < maxY && card.y() + card.height() > minY;
            if (intersects) {
                state.selectedQuestIds.add(card.questId());
            }
        }
        String group = selectedGroupName(state);
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasImageIds.addAll(state.boxSelectionBaseCanvasImageIds);
        String lastImageId = "";
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.selectedCanvasImageIds.add(image.id());
                lastImageId = image.id();
            }
        }
        state.selectedCanvasImageId = primarySelection(lastImageId, state.boxSelectionBaseCanvasImageId, state.selectedCanvasImageIds);
        state.selectedCanvasTextIds.clear();
        state.selectedCanvasTextIds.addAll(state.boxSelectionBaseCanvasTextIds);
        String lastTextId = "";
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.selectedCanvasTextIds.add(text.id());
                lastTextId = text.id();
            }
        }
        state.selectedCanvasTextId = primarySelection(lastTextId, state.boxSelectionBaseCanvasTextId, state.selectedCanvasTextIds);
    }

    public static void finishBoxSelection(TabletUiState state, List<QuestCardLayout> cards) {
        updateBoxSelection(state, cards);
        clearBoxSelectionBase(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas mixed box selection total={}", CanvasSelectionActions.totalCanvasSelectionCount(state));
    }

    private static void captureBoxSelectionBase(TabletUiState state, boolean additive) {
        clearBoxSelectionBase(state);
        if (!additive) {
            return;
        }
        state.boxSelectionBaseQuestIds.addAll(state.selectedQuestIds);
        state.boxSelectionBaseCanvasImageIds.addAll(CanvasSelectionActions.selectedCanvasImageIds(state));
        state.boxSelectionBaseCanvasTextIds.addAll(CanvasSelectionActions.selectedCanvasTextIds(state));
        state.boxSelectionBaseCanvasImageId = state.selectedCanvasImageId;
        state.boxSelectionBaseCanvasTextId = state.selectedCanvasTextId;
    }

    private static void clearBoxSelectionBase(TabletUiState state) {
        state.boxSelectionBaseQuestIds.clear();
        state.boxSelectionBaseCanvasImageIds.clear();
        state.boxSelectionBaseCanvasTextIds.clear();
        state.boxSelectionBaseCanvasImageId = "";
        state.boxSelectionBaseCanvasTextId = "";
    }

    private static String primarySelection(String boxId, String baseId, Set<String> allIds) {
        if (boxId != null && !boxId.isBlank()) {
            return boxId;
        }
        if (baseId != null && allIds.contains(baseId)) {
            return baseId;
        }
        return allIds.stream().findFirst().orElse("");
    }

    private static boolean intersects(int left, int top, int right, int bottom, int minX, int minY, int maxX, int maxY) {
        return left < maxX && right > minX && top < maxY && bottom > minY;
    }
}

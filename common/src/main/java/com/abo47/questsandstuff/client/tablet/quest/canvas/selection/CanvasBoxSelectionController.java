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
        if (!state.canvasSelection.imageIds().add(imageId)) {
            state.canvasSelection.imageIds().remove(imageId);
            if (imageId.equals(state.canvasSelection.primaryImageId())) {
                state.canvasSelection.setPrimaryImageId(state.canvasSelection.imageIds().stream().findFirst().orElse(""));
            }
            return;
        }
        state.canvasSelection.setPrimaryImageId(imageId);
    }

    public static void toggleCanvasTextSelection(TabletUiState state, String textId) {
        if (textId == null || textId.isBlank()) {
            return;
        }
        if (!state.canvasSelection.textIds().add(textId)) {
            state.canvasSelection.textIds().remove(textId);
            if (textId.equals(state.canvasSelection.primaryTextId())) {
                state.canvasSelection.setPrimaryTextId(state.canvasSelection.textIds().stream().findFirst().orElse(""));
            }
            return;
        }
        state.canvasSelection.setPrimaryTextId(textId);
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
        state.canvasSelection.questIds().clear();
        state.canvasSelection.questIds().addAll(state.boxSelectionBaseQuestIds);
        for (QuestCardLayout card : cards) {
            boolean intersects = card.x() < maxX && card.x() + card.width() > minX
                    && card.y() < maxY && card.y() + card.height() > minY;
            if (intersects) {
                state.canvasSelection.questIds().add(card.questId());
            }
        }
        String group = selectedGroupName(state);
        state.canvasSelection.imageIds().clear();
        state.canvasSelection.imageIds().addAll(state.boxSelectionBaseCanvasImageIds);
        String lastImageId = "";
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.canvasSelection.imageIds().add(image.id());
                lastImageId = image.id();
            }
        }
        state.canvasSelection.setPrimaryImageId(primarySelection(lastImageId, state.boxSelectionBaseCanvasImageId, state.canvasSelection.imageIds()));
        state.canvasSelection.textIds().clear();
        state.canvasSelection.textIds().addAll(state.boxSelectionBaseCanvasTextIds);
        String lastTextId = "";
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.canvasSelection.textIds().add(text.id());
                lastTextId = text.id();
            }
        }
        state.canvasSelection.setPrimaryTextId(primarySelection(lastTextId, state.boxSelectionBaseCanvasTextId, state.canvasSelection.textIds()));
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
        state.boxSelectionBaseQuestIds.addAll(state.canvasSelection.questIds());
        state.boxSelectionBaseCanvasImageIds.addAll(CanvasSelectionActions.selectedImageIds(state));
        state.boxSelectionBaseCanvasTextIds.addAll(CanvasSelectionActions.selectedTextIds(state));
        state.boxSelectionBaseCanvasImageId = state.canvasSelection.primaryImageId();
        state.boxSelectionBaseCanvasTextId = state.canvasSelection.primaryTextId();
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

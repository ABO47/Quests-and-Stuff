package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

public final class CanvasBoxSelectionController {
    private CanvasBoxSelectionController() {
    }

    public static void toggleCanvasImageSelection(TabletUiState state, String imageId) {
        if (imageId == null || imageId.isBlank()) {
            return;
        }
        if (!state.canvas.canvasSelection.imageIds().add(imageId)) {
            state.canvas.canvasSelection.imageIds().remove(imageId);
            if (imageId.equals(state.canvas.canvasSelection.primaryImageId())) {
                state.canvas.canvasSelection.setPrimaryImageId(state.canvas.canvasSelection.imageIds().stream().findFirst().orElse(""));
            }
            return;
        }
        state.canvas.canvasSelection.setPrimaryImageId(imageId);
    }

    public static void toggleCanvasTextSelection(TabletUiState state, String textId) {
        if (textId == null || textId.isBlank()) {
            return;
        }
        if (!state.canvas.canvasSelection.textIds().add(textId)) {
            state.canvas.canvasSelection.textIds().remove(textId);
            if (textId.equals(state.canvas.canvasSelection.primaryTextId())) {
                state.canvas.canvasSelection.setPrimaryTextId(state.canvas.canvasSelection.textIds().stream().findFirst().orElse(""));
            }
            return;
        }
        state.canvas.canvasSelection.setPrimaryTextId(textId);
    }

    public static void toggleCanvasExclusiveChoiceSelection(TabletUiState state, String ecId) {
        if (ecId == null || ecId.isBlank()) {
            return;
        }
        if (!state.canvas.canvasSelection.ecIds().add(ecId)) {
            state.canvas.canvasSelection.ecIds().remove(ecId);
            if (ecId.equals(state.canvas.canvasSelection.primaryEcId())) {
                state.canvas.canvasSelection.setPrimaryEcId(state.canvas.canvasSelection.ecIds().stream().findFirst().orElse(""));
            }
            return;
        }
        state.canvas.canvasSelection.setPrimaryEcId(ecId);
    }

    public static void beginBoxSelection(TabletUiState state, boolean additive, int localX, int localY) {
        state.canvas.boxSelecting = true;
        state.canvas.boxAdditive = additive;
        state.canvas.boxStartX = localX;
        state.canvas.boxStartY = localY;
        state.canvas.boxCurrentX = localX;
        state.canvas.boxCurrentY = localY;
        captureBoxSelectionBase(state, additive);
        if (!additive) {
            CanvasSelectionActions.clearCanvasSelection(state);
        }
    }

    public static void updateBoxSelection(TabletUiState state, List<QuestCardLayout> cards) {
        int minX = Math.min(state.canvas.boxStartX, state.canvas.boxCurrentX);
        int minY = Math.min(state.canvas.boxStartY, state.canvas.boxCurrentY);
        int maxX = Math.max(state.canvas.boxStartX, state.canvas.boxCurrentX);
        int maxY = Math.max(state.canvas.boxStartY, state.canvas.boxCurrentY);
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.questIds().addAll(state.canvas.boxSelectionBaseQuestIds);
        for (QuestCardLayout card : cards) {
            boolean intersects = card.x() < maxX && card.x() + card.width() > minX
                    && card.y() < maxY && card.y() + card.height() > minY;
            if (intersects) {
                state.canvas.canvasSelection.questIds().add(card.questId());
            }
        }
        String group = selectedChapterName(state);
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.imageIds().addAll(state.canvas.boxSelectionBaseCanvasImageIds);
        String lastImageId = "";
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.canvas.canvasSelection.imageIds().add(image.id());
                lastImageId = image.id();
            }
        }
        state.canvas.canvasSelection.setPrimaryImageId(primarySelection(lastImageId, state.canvas.boxSelectionBaseCanvasImageId, state.canvas.canvasSelection.imageIds()));
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.textIds().addAll(state.canvas.boxSelectionBaseCanvasTextIds);
        String lastTextId = "";
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.canvas.canvasSelection.textIds().add(text.id());
                lastTextId = text.id();
            }
        }
        state.canvas.canvasSelection.setPrimaryTextId(primarySelection(lastTextId, state.canvas.boxSelectionBaseCanvasTextId, state.canvas.canvasSelection.textIds()));
        state.canvas.canvasSelection.ecIds().clear();
        state.canvas.canvasSelection.ecIds().addAll(state.canvas.boxSelectionBaseCanvasExclusiveChoiceIds);
        String lastEcId = "";
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(group, List.of())) {
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation());
            boolean intersects = intersects(bounds[0], bounds[1], bounds[2], bounds[3], minX, minY, maxX, maxY);
            if (intersects) {
                state.canvas.canvasSelection.ecIds().add(ec.id());
                lastEcId = ec.id();
            }
        }
        state.canvas.canvasSelection.setPrimaryEcId(primarySelection(lastEcId, state.canvas.boxSelectionBaseCanvasExclusiveChoiceId, state.canvas.canvasSelection.ecIds()));
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
        state.canvas.boxSelectionBaseQuestIds.addAll(state.canvas.canvasSelection.questIds());
        state.canvas.boxSelectionBaseCanvasImageIds.addAll(CanvasSelectionActions.selectedImageIds(state));
        state.canvas.boxSelectionBaseCanvasTextIds.addAll(CanvasSelectionActions.selectedTextIds(state));
        state.canvas.boxSelectionBaseCanvasExclusiveChoiceIds.addAll(CanvasSelectionActions.selectedEcIds(state));
        state.canvas.boxSelectionBaseCanvasImageId = state.canvas.canvasSelection.primaryImageId();
        state.canvas.boxSelectionBaseCanvasTextId = state.canvas.canvasSelection.primaryTextId();
        state.canvas.boxSelectionBaseCanvasExclusiveChoiceId = state.canvas.canvasSelection.primaryEcId();
    }

    private static void clearBoxSelectionBase(TabletUiState state) {
        state.canvas.boxSelectionBaseQuestIds.clear();
        state.canvas.boxSelectionBaseCanvasImageIds.clear();
        state.canvas.boxSelectionBaseCanvasTextIds.clear();
        state.canvas.boxSelectionBaseCanvasExclusiveChoiceIds.clear();
        state.canvas.boxSelectionBaseCanvasImageId = "";
        state.canvas.boxSelectionBaseCanvasTextId = "";
        state.canvas.boxSelectionBaseCanvasExclusiveChoiceId = "";
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

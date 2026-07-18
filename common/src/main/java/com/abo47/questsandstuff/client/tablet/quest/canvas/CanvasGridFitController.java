package com.abo47.questsandstuff.client.tablet.quest.canvas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasGridFitController {
    private CanvasGridFitController() {
    }

    public static boolean canFitImageToGrid(TabletUiState state, String chapter, String imageId) {
        CanvasImageLayer image = findImage(state, chapter, imageId);
        return image != null && !image.equals(fittedImage(state, image));
    }

    public static boolean fitImageToGrid(TabletUiState state, String chapter, String imageId) {
        CanvasImageLayer image = findImage(state, chapter, imageId);
        if (image == null) {
            return false;
        }
        CanvasImageLayer fitted = fittedImage(state, image);
        if (image.equals(fitted)) {
            return false;
        }
        CanvasLayerMutations.putCanvasImage(state, chapter, fitted);
        state.canvas.canvasSelection.setPrimaryImageId(imageId);
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.imageIds().add(imageId);
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.questIds().clear();
        return true;
    }

    public static boolean canFitTextToGrid(TabletUiState state, String chapter, String textId) {
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, chapter, textId);
        return text != null && !text.equals(fittedText(state, text));
    }

    public static boolean fitTextToGrid(TabletUiState state, String chapter, String textId) {
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, chapter, textId);
        if (text == null) {
            return false;
        }
        CanvasTextLayer fitted = fittedText(state, text);
        if (text.equals(fitted)) {
            return false;
        }
        CanvasLayerMutations.putCanvasText(state, chapter, fitted);
        state.canvas.canvasSelection.setPrimaryTextId(textId);
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.textIds().add(textId);
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.questIds().clear();
        return true;
    }

    public static boolean canFitQuestToGrid(TabletUiState state, QuestCardLayout card) {
        return card != null && !fittedQuest(state, card).equals(new FittedQuest(card.logicalX(), card.logicalY(), card.scale()));
    }

    public static boolean fitQuestToGrid(Player player, TabletUiState state, QuestCardLayout card) {
        if (card == null || card.questId() == null || card.questId().isBlank()) {
            return false;
        }
        FittedQuest fitted = fittedQuest(state, card);
        boolean moved = fitted.x() != card.logicalX() || fitted.y() != card.logicalY();
        boolean scaled = Float.compare(fitted.scale(), card.scale()) != 0;
        if (!moved && !scaled) {
            return false;
        }
        if (moved) {
            Map<String, CanvasPoint> positions = new HashMap<>();
            positions.put(card.questId(), new CanvasPoint(fitted.x(), fitted.y()));
            EditorCanvasCommandClient.runCanvasMoveAction(player, state, positions);
        }
        if (scaled) {
            Map<String, Float> scales = new HashMap<>();
            scales.put(card.questId(), fitted.scale());
            EditorCanvasCommandClient.runCanvasScaleAction(player, state, scales);
        }
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.questIds().add(card.questId());
        return true;
    }

    public static boolean canFitSelectionToGrid(TabletUiState state, String chapter, Map<String, QuestCardLayout> byQuestId) {
        if (state == null || chapter == null || chapter.isBlank()) {
            return false;
        }
        for (String questId : state.canvas.canvasSelection.questIds()) {
            QuestCardLayout card = byQuestId == null ? null : byQuestId.get(questId);
            if (canFitQuestToGrid(state, card)) {
                return true;
            }
        }
        for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
            if (canFitImageToGrid(state, chapter, imageId)) {
                return true;
            }
        }
        for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
            if (canFitTextToGrid(state, chapter, textId)) {
                return true;
            }
        }
        for (String ecId : CanvasSelectionActions.selectedEcIds(state)) {
            if (canFitExclusiveChoiceToGrid(state, chapter, ecId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean fitSelectionToGrid(Player player, TabletUiState state, String chapter, Map<String, QuestCardLayout> byQuestId) {
        if (state == null || chapter == null || chapter.isBlank()) {
            return false;
        }
        boolean changed = false;
        Map<String, CanvasPoint> questPositions = new HashMap<>();
        Map<String, Float> questScales = new HashMap<>();
        for (String questId : state.canvas.canvasSelection.questIds()) {
            QuestCardLayout card = byQuestId == null ? null : byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            FittedQuest fitted = fittedQuest(state, card);
            if (fitted.x() != card.logicalX() || fitted.y() != card.logicalY()) {
                questPositions.put(questId, new CanvasPoint(fitted.x(), fitted.y()));
                changed = true;
            }
            if (Float.compare(fitted.scale(), card.scale()) != 0) {
                questScales.put(questId, fitted.scale());
                changed = true;
            }
        }

        Set<String> imageIds = CanvasSelectionActions.selectedImageIds(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            if (!imageIds.contains(image.id())) {
                continue;
            }
            CanvasImageLayer fitted = fittedImage(state, image);
            if (!image.equals(fitted)) {
                CanvasLayerMutations.putCanvasImage(state, chapter, fitted);
                changed = true;
            }
        }

        Set<String> textIds = CanvasSelectionActions.selectedTextIds(state);
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            if (!textIds.contains(text.id())) {
                continue;
            }
            CanvasTextLayer fitted = fittedText(state, text);
            if (!text.equals(fitted)) {
                CanvasLayerMutations.putCanvasText(state, chapter, fitted);
                changed = true;
            }
        }

        Set<String> ecIds = CanvasSelectionActions.selectedEcIds(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            if (!ecIds.contains(ec.id())) {
                continue;
            }
            CanvasExclusiveChoice fitted = fittedExclusiveChoice(state, ec);
            if (!ec.equals(fitted)) {
                CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, fitted);
                changed = true;
            }
        }

        if (!questPositions.isEmpty()) {
            EditorCanvasCommandClient.runCanvasMoveAction(player, state, questPositions);
        }
        if (!questScales.isEmpty()) {
            EditorCanvasCommandClient.runCanvasScaleAction(player, state, questScales);
        }
        return changed;
    }

    public static boolean canFitExclusiveChoiceToGrid(TabletUiState state, String chapter, String ecId) {
        CanvasExclusiveChoice ec = findExclusiveChoice(state, chapter, ecId);
        return ec != null && !ec.equals(fittedExclusiveChoice(state, ec));
    }

    public static boolean fitExclusiveChoiceToGrid(TabletUiState state, String chapter, String ecId) {
        CanvasExclusiveChoice ec = findExclusiveChoice(state, chapter, ecId);
        if (ec == null) {
            return false;
        }
        CanvasExclusiveChoice fitted = fittedExclusiveChoice(state, ec);
        if (ec.equals(fitted)) {
            return false;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, fitted);
        state.canvas.canvasSelection.setPrimaryEcId(ecId);
        state.canvas.canvasSelection.ecIds().clear();
        state.canvas.canvasSelection.ecIds().add(ecId);
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.questIds().clear();
        return true;
    }

    public static CanvasImageLayer fittedImage(TabletUiState state, CanvasImageLayer image) {
        return CanvasElementGridFit.fittedImage(image, CanvasGeometry.gridSize(state), (x, y, width, height, pivotX, pivotY, rotation) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, width, height, pivotX, pivotY, rotation));
    }

    public static CanvasTextLayer fittedText(TabletUiState state, CanvasTextLayer text) {
        return CanvasElementGridFit.fittedText(text, CanvasGeometry.gridSize(state), (x, y, width, height, pivotX, pivotY, rotation) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, width, height, pivotX, pivotY, rotation));
    }

    public static CanvasExclusiveChoice fittedExclusiveChoice(TabletUiState state, CanvasExclusiveChoice ec) {
        return CanvasElementGridFit.fittedExclusiveChoice(ec, CanvasGeometry.gridSize(state), (x, y, width, height, pivotX, pivotY, rotation) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, width, height, pivotX, pivotY, rotation));
    }

    private static FittedQuest fittedQuest(TabletUiState state, QuestCardLayout card) {
        int grid = CanvasGeometry.gridSize(state);
        boolean oldSnap = state.canvas.gridSnapLocked;
        state.canvas.gridSnapLocked = true;
        float scale;
        try {
            scale = CanvasGeometry.snapScaleToGrid(state, card.scale());
        } finally {
            state.canvas.gridSnapLocked = oldSnap;
        }
        int x = snapToGrid(card.logicalX(), grid);
        int y = snapToGrid(card.logicalY(), grid);
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                state,
                x,
                y,
                CanvasGeometry.slotLogicalWidth(state, scale),
                CanvasGeometry.slotLogicalHeight(state, scale)
        );
        return new FittedQuest(clamped.x, clamped.y, scale);
    }

    private static CanvasExclusiveChoice findExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        if (chapter == null || chapter.isBlank() || ecId == null || ecId.isBlank()) {
            return null;
        }
        return state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of()).stream()
                .filter(ec -> ec.id().equals(ecId))
                .findFirst()
                .orElse(null);
    }

    private static CanvasImageLayer findImage(TabletUiState state, String chapter, String imageId) {
        if (chapter == null || chapter.isBlank() || imageId == null || imageId.isBlank()) {
            return null;
        }
        return state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of()).stream()
                .filter(image -> image.id().equals(imageId))
                .findFirst()
                .orElse(null);
    }

    private static int snapToGrid(int value, int grid) {
        int safeGrid = Math.max(1, grid);
        return Math.round((float) value / (float) safeGrid) * safeGrid;
    }

    private record FittedQuest(int x, int y, float scale) {
    }
}

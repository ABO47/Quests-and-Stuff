package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CanvasGridFitController {
    private CanvasGridFitController() {
    }

    public static boolean canFitImageToGrid(TabletUiState state, String group, String imageId) {
        CanvasImageLayer image = findImage(state, group, imageId);
        return image != null && !image.equals(fittedImage(state, image));
    }

    public static boolean fitImageToGrid(TabletUiState state, String group, String imageId) {
        CanvasImageLayer image = findImage(state, group, imageId);
        if (image == null) {
            return false;
        }
        CanvasImageLayer fitted = fittedImage(state, image);
        if (image.equals(fitted)) {
            return false;
        }
        CanvasLayerMutations.putCanvasImage(state, group, fitted);
        state.canvasSelection.setPrimaryImageId(imageId);
        state.canvasSelection.imageIds().clear();
        state.canvasSelection.imageIds().add(imageId);
        state.canvasSelection.setPrimaryTextId("");
        state.canvasSelection.textIds().clear();
        state.canvasSelection.questIds().clear();
        return true;
    }

    public static boolean canFitTextToGrid(TabletUiState state, String group, String textId) {
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, textId);
        return text != null && !text.equals(fittedText(state, text));
    }

    public static boolean fitTextToGrid(TabletUiState state, String group, String textId) {
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, textId);
        if (text == null) {
            return false;
        }
        CanvasTextLayer fitted = fittedText(state, text);
        if (text.equals(fitted)) {
            return false;
        }
        CanvasLayerMutations.putCanvasText(state, group, fitted);
        state.canvasSelection.setPrimaryTextId(textId);
        state.canvasSelection.textIds().clear();
        state.canvasSelection.textIds().add(textId);
        state.canvasSelection.setPrimaryImageId("");
        state.canvasSelection.imageIds().clear();
        state.canvasSelection.questIds().clear();
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
            EditorCommandClient.runCanvasMoveAction(player, state, positions);
        }
        if (scaled) {
            Map<String, Float> scales = new HashMap<>();
            scales.put(card.questId(), fitted.scale());
            EditorCommandClient.runCanvasScaleAction(player, state, scales);
        }
        state.canvasSelection.questIds().clear();
        state.canvasSelection.questIds().add(card.questId());
        return true;
    }

    public static boolean canFitSelectionToGrid(TabletUiState state, String group, Map<String, QuestCardLayout> byQuestId) {
        if (state == null || group == null || group.isBlank()) {
            return false;
        }
        for (String questId : state.canvasSelection.questIds()) {
            QuestCardLayout card = byQuestId == null ? null : byQuestId.get(questId);
            if (canFitQuestToGrid(state, card)) {
                return true;
            }
        }
        for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
            if (canFitImageToGrid(state, group, imageId)) {
                return true;
            }
        }
        for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
            if (canFitTextToGrid(state, group, textId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean fitSelectionToGrid(Player player, TabletUiState state, String group, Map<String, QuestCardLayout> byQuestId) {
        if (state == null || group == null || group.isBlank()) {
            return false;
        }
        boolean changed = false;
        Map<String, CanvasPoint> questPositions = new HashMap<>();
        Map<String, Float> questScales = new HashMap<>();
        for (String questId : state.canvasSelection.questIds()) {
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
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (!imageIds.contains(image.id())) {
                continue;
            }
            CanvasImageLayer fitted = fittedImage(state, image);
            if (!image.equals(fitted)) {
                CanvasLayerMutations.putCanvasImage(state, group, fitted);
                changed = true;
            }
        }

        Set<String> textIds = CanvasSelectionActions.selectedTextIds(state);
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (!textIds.contains(text.id())) {
                continue;
            }
            CanvasTextLayer fitted = fittedText(state, text);
            if (!text.equals(fitted)) {
                CanvasLayerMutations.putCanvasText(state, group, fitted);
                changed = true;
            }
        }

        if (!questPositions.isEmpty()) {
            EditorCommandClient.runCanvasMoveAction(player, state, questPositions);
        }
        if (!questScales.isEmpty()) {
            EditorCommandClient.runCanvasScaleAction(player, state, questScales);
        }
        return changed;
    }

    public static CanvasImageLayer fittedImage(TabletUiState state, CanvasImageLayer image) {
        return CanvasElementGridFit.fittedImage(image, CanvasGeometry.gridSize(state), (x, y, width, height, pivotX, pivotY, rotation) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, width, height, pivotX, pivotY, rotation));
    }

    public static CanvasTextLayer fittedText(TabletUiState state, CanvasTextLayer text) {
        return CanvasElementGridFit.fittedText(text, CanvasGeometry.gridSize(state), (x, y, width, height, pivotX, pivotY, rotation) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, width, height, pivotX, pivotY, rotation));
    }

    private static FittedQuest fittedQuest(TabletUiState state, QuestCardLayout card) {
        int grid = CanvasGeometry.gridSize(state);
        boolean oldSnap = state.gridSnapLocked;
        state.gridSnapLocked = true;
        float scale;
        try {
            scale = CanvasGeometry.snapScaleToGrid(state, card.scale());
        } finally {
            state.gridSnapLocked = oldSnap;
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

    private static CanvasImageLayer findImage(TabletUiState state, String group, String imageId) {
        if (group == null || group.isBlank() || imageId == null || imageId.isBlank()) {
            return null;
        }
        return state.canvasImagesByGroup.getOrDefault(group, List.of()).stream()
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

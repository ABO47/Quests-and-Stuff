package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
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
        CanvasRenderer.putCanvasImage(state, group, fitted);
        state.selectedCanvasImageId = imageId;
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasImageIds.add(imageId);
        state.selectedCanvasTextId = "";
        state.selectedCanvasTextIds.clear();
        state.selectedQuestIds.clear();
        return true;
    }

    public static boolean canFitTextToGrid(TabletUiState state, String group, String textId) {
        CanvasTextLayer text = CanvasRenderer.findCanvasText(state, group, textId);
        return text != null && !text.equals(fittedText(state, text));
    }

    public static boolean fitTextToGrid(TabletUiState state, String group, String textId) {
        CanvasTextLayer text = CanvasRenderer.findCanvasText(state, group, textId);
        if (text == null) {
            return false;
        }
        CanvasTextLayer fitted = fittedText(state, text);
        if (text.equals(fitted)) {
            return false;
        }
        CanvasRenderer.putCanvasText(state, group, fitted);
        state.selectedCanvasTextId = textId;
        state.selectedCanvasTextIds.clear();
        state.selectedCanvasTextIds.add(textId);
        state.selectedCanvasImageId = "";
        state.selectedCanvasImageIds.clear();
        state.selectedQuestIds.clear();
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
        state.selectedQuestIds.clear();
        state.selectedQuestIds.add(card.questId());
        return true;
    }

    public static boolean canFitSelectionToGrid(TabletUiState state, String group, Map<String, QuestCardLayout> byQuestId) {
        if (state == null || group == null || group.isBlank()) {
            return false;
        }
        for (String questId : state.selectedQuestIds) {
            QuestCardLayout card = byQuestId == null ? null : byQuestId.get(questId);
            if (canFitQuestToGrid(state, card)) {
                return true;
            }
        }
        for (String imageId : CanvasRenderer.selectedCanvasImageIds(state)) {
            if (canFitImageToGrid(state, group, imageId)) {
                return true;
            }
        }
        for (String textId : CanvasRenderer.selectedCanvasTextIds(state)) {
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
        for (String questId : state.selectedQuestIds) {
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

        Set<String> imageIds = CanvasRenderer.selectedCanvasImageIds(state);
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (!imageIds.contains(image.id())) {
                continue;
            }
            CanvasImageLayer fitted = fittedImage(state, image);
            if (!image.equals(fitted)) {
                CanvasRenderer.putCanvasImage(state, group, fitted);
                changed = true;
            }
        }

        Set<String> textIds = CanvasRenderer.selectedCanvasTextIds(state);
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (!textIds.contains(text.id())) {
                continue;
            }
            CanvasTextLayer fitted = fittedText(state, text);
            if (!text.equals(fitted)) {
                CanvasRenderer.putCanvasText(state, group, fitted);
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

    private static CanvasImageLayer fittedImage(TabletUiState state, CanvasImageLayer image) {
        int grid = CanvasGeometry.gridSize(state);
        int nextW = snapSpanToGrid(image.w(), grid, 8);
        int nextH = snapSpanToGrid(image.h(), grid, 8);
        CanvasPoint anchor = CanvasGeometry.fitRotatedAnchorToGrid(image.x(), image.y(), image.w(), image.h(), nextW, nextH, image.rotation(), grid);
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                state,
                anchor.x,
                anchor.y,
                nextW,
                nextH
        );
        return image.withBounds(clamped.x, clamped.y, nextW, nextH);
    }

    private static CanvasTextLayer fittedText(TabletUiState state, CanvasTextLayer text) {
        int grid = CanvasGeometry.gridSize(state);
        int nextW = snapSpanToGrid(text.w(), grid, 24);
        int nextH = snapSpanToGrid(text.h(), grid, 14);
        CanvasPoint anchor = CanvasGeometry.fitRotatedAnchorToGrid(text.x(), text.y(), text.w(), text.h(), nextW, nextH, text.rotation(), grid);
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                state,
                anchor.x,
                anchor.y,
                nextW,
                nextH
        );
        return new CanvasTextLayer(text.id(), text.text(), clamped.x, clamped.y, nextW, nextH, text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
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

    private static int snapSpanToGrid(int value, int grid, int min) {
        int safeGrid = Math.max(1, grid);
        int snapped = Math.max(safeGrid, Math.round((float) Math.max(1, value) / (float) safeGrid) * safeGrid);
        while (snapped < min) {
            snapped += safeGrid;
        }
        return snapped;
    }

    private static int snapToGrid(int value, int grid) {
        int safeGrid = Math.max(1, grid);
        return Math.round((float) value / (float) safeGrid) * safeGrid;
    }

    private record FittedQuest(int x, int y, float scale) {
    }
}

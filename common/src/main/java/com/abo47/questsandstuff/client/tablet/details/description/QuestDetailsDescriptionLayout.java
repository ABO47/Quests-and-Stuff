package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.assetDimensions;

final class QuestDetailsDescriptionLayout {
    private QuestDetailsDescriptionLayout() {
    }

    static int opacityAlpha(int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        return Math.max(0, Math.min(255, 255 * clamped / 100));
    }

    static int[] gridFit(TabletUiState state, int w, int h) {
        int cell = Math.max(1, CanvasGeometry.gridSize(state));
        int fitW = Math.max(cell + 1, ((Math.max(cell, w - 1)) / cell) * cell + 1);
        int fitH = Math.max(cell + 1, ((Math.max(cell, h - 1)) / cell) * cell + 1);
        return new int[]{Math.min(w, fitW), Math.min(h, fitH)};
    }

    static int snap(TabletUiState state, int value) {
        if (!state.questDetailsGridSnapLocked) {
            return value;
        }
        int step = Math.max(1, CanvasGeometry.gridSize(state));
        return Math.round((float) value / (float) step) * step;
    }

    static CanvasTextLayer fittedText(TabletUiState state, CanvasTextLayer text) {
        int grid = Math.max(1, CanvasGeometry.gridSize(state));
        int width = snapSpan(text.w(), grid, 24);
        int height = snapSpan(text.h(), grid, 14);
        var anchor = CanvasGeometry.fitRotatedAnchorToGrid(text.x(), text.y(), text.w(), text.h(), width, height, text.rotation(), grid);
        return new CanvasTextLayer(text.id(), text.text(), Math.max(0, anchor.x), Math.max(0, anchor.y), width, height, text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
    }

    static CanvasImageLayer fittedImage(TabletUiState state, CanvasImageLayer image) {
        int grid = Math.max(1, CanvasGeometry.gridSize(state));
        int width = snapSpan(image.w(), grid, 8);
        int height = snapSpan(image.h(), grid, 8);
        var anchor = CanvasGeometry.fitRotatedAnchorToGrid(image.x(), image.y(), image.w(), image.h(), width, height, image.rotation(), grid);
        return image.withBounds(Math.max(0, anchor.x), Math.max(0, anchor.y), width, height);
    }

    static int[] imageSpawnSize(String asset) {
        var dimensions = assetDimensions(asset);
        if (dimensions == null || dimensions.width() <= 0 || dimensions.height() <= 0) {
            return new int[]{80, 56};
        }
        double scale = Math.min(1.0, 96.0 / Math.max(dimensions.width(), dimensions.height()));
        return new int[]{Math.max(12, (int) Math.round(dimensions.width() * scale)), Math.max(12, (int) Math.round(dimensions.height() * scale))};
    }

    private static int snapSpan(int value, int grid, int min) {
        int snapped = Math.max(grid, Math.round((float) Math.max(1, value) / (float) grid) * grid);
        while (snapped < min) {
            snapped += grid;
        }
        return snapped;
    }
}

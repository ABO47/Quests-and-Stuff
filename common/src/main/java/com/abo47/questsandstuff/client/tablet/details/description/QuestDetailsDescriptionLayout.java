package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.assetDimensions;

final class QuestDetailsDescriptionLayout {
    private static final int READ_ONLY_CONTENT_PAD = 16;

    private QuestDetailsDescriptionLayout() {
    }

    static int clampReadOnlyScroll(TabletUiState state, QuestDetailsDescriptionModel model, int viewportH, int scroll) {
        int current = Math.max(0, scroll);
        if (QuestDetailsEditState.canEdit(state)) {
            return current;
        }
        int[] bounds = elementVerticalBounds(model);
        if (bounds == null) {
            return 0;
        }
        int safeViewportH = Math.max(1, viewportH);
        int minScroll = Math.max(0, bounds[0] - READ_ONLY_CONTENT_PAD);
        int maxScroll = Math.max(0, bounds[1] + READ_ONLY_CONTENT_PAD - safeViewportH);
        if (maxScroll < minScroll) {
            int centered = (bounds[0] + bounds[1] - safeViewportH) / 2;
            return Math.max(0, centered);
        }
        return Math.max(minScroll, Math.min(maxScroll, current));
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
        int rotation = CanvasGeometry.normalizeDegrees(text.rotation());
        if (rotation == 0) {
            CanvasGeometry.GridVisualBox box = CanvasGeometry.fitVisualBoxToGridSlot(text.x(), text.y(), text.w(), text.h(), grid, 24, 14);
            return new CanvasTextLayer(text.id(), text.text(), Math.max(0, box.x()), Math.max(0, box.y()), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
        }
        CanvasGeometry.GridFittedBox fit = CanvasGeometry.fitRotatedElementToGridSlotAtPivot(text.x(), text.y(), text.w(), text.h(), text.w() / 2, text.h() / 2, rotation, grid, 24, 14);
        return new CanvasTextLayer(text.id(), text.text(), Math.max(0, fit.x()), Math.max(0, fit.y()), fit.width(), fit.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
    }

    static CanvasImageLayer fittedImage(TabletUiState state, CanvasImageLayer image) {
        int grid = Math.max(1, CanvasGeometry.gridSize(state));
        int rotation = CanvasGeometry.normalizeDegrees(image.rotation());
        if (rotation != 0) {
            CanvasGeometry.GridFittedBox fit = CanvasGeometry.fitRotatedElementToGridSlotAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), rotation, grid, 8, 8);
            CanvasImageLayer resized = image.withBounds(image.x(), image.y(), fit.width(), fit.height());
            return resized.moveTo(Math.max(0, fit.x()), Math.max(0, fit.y()));
        }
        CanvasGeometry.GridVisualBox box = CanvasGeometry.fitVisualBoxToGridSlot(image.x(), image.y(), image.w(), image.h(), grid, 8, 8);
        return image.withBounds(Math.max(0, box.x()), Math.max(0, box.y()), box.width(), box.height());
    }

    static int[] imageSpawnSize(String asset) {
        var dimensions = assetDimensions(asset);
        if (dimensions == null || dimensions.width() <= 0 || dimensions.height() <= 0) {
            return new int[]{80, 56};
        }
        double scale = Math.min(1.0, 96.0 / Math.max(dimensions.width(), dimensions.height()));
        return new int[]{Math.max(12, (int) Math.round(dimensions.width() * scale)), Math.max(12, (int) Math.round(dimensions.height() * scale))};
    }

    private static int[] elementVerticalBounds(QuestDetailsDescriptionModel model) {
        if (model == null || (model.texts.isEmpty() && model.images.isEmpty())) {
            return null;
        }
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CanvasTextLayer text : model.texts.values()) {
            int[] bounds = CanvasElementGeometry.logicalBounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
            minY = Math.min(minY, bounds[1]);
            maxY = Math.max(maxY, bounds[3]);
        }
        for (CanvasImageLayer image : model.images.values()) {
            int[] bounds = CanvasElementGeometry.logicalBoundsAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            minY = Math.min(minY, bounds[1]);
            maxY = Math.max(maxY, bounds[3]);
        }
        if (minY == Integer.MAX_VALUE) {
            return null;
        }
        return new int[]{minY, maxY};
    }

}

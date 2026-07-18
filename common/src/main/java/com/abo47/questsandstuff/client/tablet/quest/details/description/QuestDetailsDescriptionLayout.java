package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasElementGridFit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.assetDimensions;

final class QuestDetailsDescriptionLayout {
    private static final int READ_ONLY_CONTENT_PAD = 16;
    private static final double CANVAS_BLOCK_FILL = 0.94D;
    private static final int LOCKED_EDGE_INSET = 1;
    private static final int UNBOUNDED_EDGE = Integer.MIN_VALUE;

    private QuestDetailsDescriptionLayout() {
    }

    static int clampDescriptionScroll(TabletUiState state, QuestDetailsDescriptionModel model, int viewportH, int scroll) {
        int maxScroll = descriptionScrollMax(model, viewportH);
        int current = Math.max(0, Math.min(maxScroll, scroll));
        int[] bounds = elementVerticalBounds(model);
        if (bounds == null) {
            return 0;
        }
        if (QuestDetailsEditController.canEdit(state)) {
            return current;
        }
        int safeViewportH = Math.max(1, viewportH);
        int minScroll = Math.max(0, bounds[0] - READ_ONLY_CONTENT_PAD);
        if (maxScroll < minScroll) {
            int centered = (bounds[0] + bounds[1] - safeViewportH) / 2;
            return Math.max(0, Math.min(maxScroll, centered));
        }
        return Math.max(minScroll, Math.min(maxScroll, current));
    }

    static int descriptionScrollMax(QuestDetailsDescriptionModel model, int viewportH) {
        int[] bounds = elementVerticalBounds(model);
        if (bounds == null) {
            return 0;
        }
        int safeViewportH = Math.max(1, viewportH);
        if (bounds[1] <= safeViewportH) {
            return 0;
        }
        int bottomMax = Math.max(0, bounds[1] + READ_ONLY_CONTENT_PAD - safeViewportH);
        int centered = Math.max(0, (bounds[0] + bounds[1] - safeViewportH) / 2);
        return Math.max(bottomMax, centered);
    }

    static int descriptionScrollKnobHeight(int viewportH, int scrollMax) {
        int safeViewportH = Math.max(1, viewportH);
        int safeScrollMax = Math.max(0, scrollMax);
        return Math.max(12, Math.min(safeViewportH, Math.round((float) safeViewportH * ((float) safeViewportH / (float) (safeViewportH + safeScrollMax)))));
    }

    static int[] gridFit(TabletUiState state, int w, int h) {
        int cell = Math.max(1, CanvasGeometry.gridSize(state));
        int fitW = Math.max(cell + 1, ((Math.max(cell, w - 1)) / cell) * cell + 1);
        int fitH = Math.max(cell + 1, ((Math.max(cell, h - 1)) / cell) * cell + 1);
        return new int[]{Math.min(w, fitW), Math.min(h, fitH)};
    }

    static int snap(TabletUiState state, int value) {
        if (!state.questDetails.questDetailsGridSnapLocked) {
            return value;
        }
        int step = Math.max(1, CanvasGeometry.gridSize(state));
        return Math.round((float) value / (float) step) * step;
    }

    static CanvasTextLayer fittedText(TabletUiState state, CanvasTextLayer text) {
        return CanvasElementGridFit.fittedText(text, CanvasGeometry.gridSize(state), CanvasElementGridFit::nonNegativeClamp);
    }

    static CanvasImageLayer fittedImage(TabletUiState state, CanvasImageLayer image) {
        return CanvasElementGridFit.fittedImage(image, CanvasGeometry.gridSize(state), CanvasElementGridFit::nonNegativeClamp);
    }

    static CanvasTextLayer fitAndClampText(TabletUiState state, CanvasTextLayer text, int contentW) {
        CanvasTextLayer fitted = state.questDetails.questDetailsGridSnapLocked ? fittedText(state, text) : text;
        return clampTextToColumn(state, fitted, contentW);
    }

    static CanvasImageLayer fitAndClampImage(TabletUiState state, CanvasImageLayer image, int contentW) {
        CanvasImageLayer fitted = state.questDetails.questDetailsGridSnapLocked ? fittedImage(state, image) : image;
        return clampImageToColumn(state, fitted, contentW);
    }

    static CanvasTextLayer clampTextToColumn(TabletUiState state, CanvasTextLayer text, int contentW) {
        CanvasPoint clamped = clampAnchorToColumn(
                state,
                text.x(),
                text.y(),
                text.w(),
                text.h(),
                CanvasElementGeometry.defaultPivot(text.w()),
                CanvasElementGeometry.defaultPivot(text.h()),
                text.rotation(),
                contentW
        );
        return text.moveTo(clamped.x, clamped.y);
    }

    static CanvasImageLayer clampImageToColumn(TabletUiState state, CanvasImageLayer image, int contentW) {
        CanvasPoint clamped = clampImageAnchorToColumn(state, image, contentW);
        return image.moveTo(clamped.x, clamped.y);
    }

    static CanvasPoint clampImageAnchorToColumn(TabletUiState state, CanvasImageLayer image, int contentW) {
        int[] bounds = imageBoundsForColumnClamp(image);
        return clampAnchorToColumnBounds(state, image.x(), image.y(), bounds, contentW);
    }

    static CanvasPoint clampAnchorToColumn(
            TabletUiState state,
            int x,
            int y,
            int width,
            int height,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int contentW
    ) {
        int[] bounds = CanvasElementGeometry.logicalBoundsAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees);
        return clampAnchorToColumnBounds(state, x, y, bounds, contentW);
    }

    static int[] imageBoundsForColumnClamp(CanvasImageLayer image) {
        if (ModelAssetPreviewRenderer.isBlockModelAsset(image.asset())) {
            return blockModelVisualBounds(image);
        }
        return CanvasElementGeometry.logicalBoundsAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
    }

    private static CanvasPoint clampAnchorToColumnBounds(TabletUiState state, int x, int y, int[] bounds, int contentW) {
        int targetLeft = clampBoundsStart(bounds[0], bounds[2], visibleLeftEdge(), Math.max(1, contentW));
        int targetTop = clampBoundsStart(bounds[1], bounds[3], visibleTopEdge(state), UNBOUNDED_EDGE);
        return new CanvasPoint(x + targetLeft - bounds[0], y + targetTop - bounds[1]);
    }

    static int visibleLeftEdge() {
        return LOCKED_EDGE_INSET;
    }

    static int visibleTopEdge(TabletUiState state) {
        int scrollTop = state == null ? 0 : Math.max(0, state.questDetails.questDetailsDescScroll);
        return scrollTop + LOCKED_EDGE_INSET;
    }

    private static int clampBoundsStart(int start, int end, int minStart, int maxEnd) {
        if (maxEnd == UNBOUNDED_EDGE) {
            return Math.max(minStart, start);
        }
        int span = Math.max(1, end - start);
        int safeMaxEnd = Math.max(minStart + 1, maxEnd);
        int maxStart = Math.max(minStart, safeMaxEnd - span);
        return Math.max(minStart, Math.min(maxStart, start));
    }

    private static int[] blockModelVisualBounds(CanvasImageLayer image) {
        double[] local = blockModelLocalBounds(image.w(), image.h(), image.entityYaw(), image.modelPitch());
        double pivotX = image.x() + effectivePivot(image.pivotX(), image.w());
        double pivotY = image.y() + effectivePivot(image.pivotY(), image.h());
        double radians = Math.toRadians(CanvasImageLayer.normalizeDegrees(image.rotation()));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {local[0], local[1]},
                {local[2], local[1]},
                {local[2], local[3]},
                {local[0], local[3]}
        };
        for (double[] corner : corners) {
            double x = pivotX + corner[0] * cos - corner[1] * sin;
            double y = pivotY + corner[0] * sin + corner[1] * cos;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new int[]{floorClean(minX), floorClean(minY), ceilClean(maxX), ceilClean(maxY)};
    }

    private static double[] blockModelLocalBounds(int width, int height, int yawDegrees, int pitchDegrees) {
        int size = Math.max(1, Math.min(width, height));
        double scale = size * CANVAS_BLOCK_FILL;
        double yaw = Math.toRadians(CanvasImageLayer.normalizeDegrees(yawDegrees));
        double pitch = Math.toRadians(CanvasImageLayer.normalizeDegrees(pitchDegrees));
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (double x : new double[]{-0.5D, 0.5D}) {
            for (double y : new double[]{-0.5D, 0.5D}) {
                for (double z : new double[]{-0.5D, 0.5D}) {
                    double rotatedY = y * cosPitch - z * sinPitch;
                    double rotatedZ = y * sinPitch + z * cosPitch;
                    double projectedX = x * cosYaw + rotatedZ * sinYaw;
                    double projectedY = -rotatedY;
                    double sx = projectedX * scale;
                    double sy = projectedY * scale;
                    minX = Math.min(minX, sx);
                    minY = Math.min(minY, sy);
                    maxX = Math.max(maxX, sx);
                    maxY = Math.max(maxY, sy);
                }
            }
        }
        return new double[]{minX, minY, maxX, maxY};
    }

    private static double effectivePivot(int pivot, int span) {
        int safeSpan = Math.max(1, span);
        int safePivot = Math.max(0, Math.min(safeSpan, pivot));
        return safePivot == safeSpan / 2 ? safeSpan / 2.0D : safePivot;
    }

    private static double clean(double value) {
        double nearest = Math.rint(value);
        return Math.abs(value - nearest) < 1.0E-7D ? nearest : value;
    }

    private static int floorClean(double value) {
        return (int) Math.floor(clean(value));
    }

    private static int ceilClean(double value) {
        return (int) Math.ceil(clean(value));
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
            int[] bounds = imageBoundsForColumnClamp(image);
            minY = Math.min(minY, bounds[1]);
            maxY = Math.max(maxY, bounds[3]);
        }
        if (minY == Integer.MAX_VALUE) {
            return null;
        }
        return new int[]{minY, maxY};
    }

}

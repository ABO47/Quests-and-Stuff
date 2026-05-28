package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;

public final class CanvasGeometry {
    private static final int QUEST_CELL_MARGIN = 1;
    private static final int QUEST_SLOT_SIZE = 16;

    private CanvasGeometry() {
    }

    public static QuestCardLayout layoutQuest(String questId, CompoundTag questTag, TabletUiState state, String selectedGroup) {
        CanvasPoint override = state.transientQuestPositions.get(questId);
        CompoundTag groupsTag = questTag.getCompound("groups");
        String resolvedGroup = selectedGroup == null ? "" : selectedGroup.trim();
        if (resolvedGroup.isBlank() || !groupsTag.contains(resolvedGroup)) {
            resolvedGroup = groupsTag.getAllKeys().stream().sorted().findFirst().orElse("");
        }

        CompoundTag groupTag = resolvedGroup.isBlank() ? new CompoundTag() : groupsTag.getCompound(resolvedGroup);
        int logicalX = groupTag.getInt("x");
        int logicalY = groupTag.getInt("y");
        if (override != null) {
            logicalX = override.x;
            logicalY = override.y;
        }

        float scale = scaleFromGroup(state, questId, groupTag);
        int visualLogicalW = visualLogicalWidth(scale);
        int visualLogicalH = visualLogicalHeight(scale);
        int slotLogicalW = slotLogicalWidth(state, scale);
        int slotLogicalH = slotLogicalHeight(state, scale);
        int visualLogicalX = logicalX + visualInset(slotLogicalW, visualLogicalW);
        int visualLogicalY = logicalY + visualInset(slotLogicalH, visualLogicalH);

        int slotScreenX = screenX(state, logicalX);
        int slotScreenY = screenY(state, logicalY);
        int slotScreenW = Math.max(1, screenX(state, logicalX + slotLogicalW) - slotScreenX);
        int slotScreenH = Math.max(1, screenY(state, logicalY + slotLogicalH) - slotScreenY);
        int screenW = visualScreenSize(state, visualLogicalW, slotLogicalW, slotScreenW);
        int screenH = visualScreenSize(state, visualLogicalH, slotLogicalH, slotScreenH);
        int screenX = slotScreenX + visualScreenInset(slotScreenW, screenW);
        int screenY = slotScreenY + visualScreenInset(slotScreenH, screenH);

        return new QuestCardLayout(
                questId,
                questTag,
                logicalX,
                logicalY,
                visualLogicalW,
                visualLogicalH,
                slotLogicalW,
                slotLogicalH,
                visualLogicalX,
                visualLogicalY,
                scale,
                screenX,
                screenY,
                screenW,
                screenH
        );
    }

    public static int screenX(TabletUiState state, double logicalX) {
        return state.canvasContentX + state.canvasOffsetX + Math.round((float) (logicalX * zoom(state)));
    }

    public static int screenY(TabletUiState state, double logicalY) {
        return state.canvasContentY + state.canvasOffsetY + Math.round((float) (logicalY * zoom(state)));
    }

    public static int screenWidth(TabletUiState state, int logicalLeft, int logicalRight) {
        return Math.max(1, screenX(state, logicalRight) - screenX(state, logicalLeft));
    }

    public static int screenHeight(TabletUiState state, int logicalTop, int logicalBottom) {
        return Math.max(1, screenY(state, logicalBottom) - screenY(state, logicalTop));
    }

    public static int screenSpan(TabletUiState state, int logicalSize) {
        return Math.max(1, Math.round(Math.max(1, logicalSize) * zoom(state)));
    }

    public static double screenToLogicalX(TabletUiState state, int screenX) {
        return (screenX - state.canvasContentX - state.canvasOffsetX) / zoom(state);
    }

    public static double screenToLogicalY(TabletUiState state, int screenY) {
        return (screenY - state.canvasContentY - state.canvasOffsetY) / zoom(state);
    }

    public static int screenToNearestLogicalX(TabletUiState state, int screenX) {
        return (int) Math.round(screenToLogicalX(state, screenX));
    }

    public static int screenToNearestLogicalY(TabletUiState state, int screenY) {
        return (int) Math.round(screenToLogicalY(state, screenY));
    }

    public static CanvasPoint anchorForScreenVisualCenter(TabletUiState state, int screenX, int screenY, float scale) {
        return anchorForVisualCenter(state, screenToLogicalX(state, screenX), screenToLogicalY(state, screenY), scale);
    }

    public static CanvasPoint anchorForVisualCenter(TabletUiState state, double logicalCenterX, double logicalCenterY, float scale) {
        int visualW = visualLogicalWidth(scale);
        int visualH = visualLogicalHeight(scale);
        int anchorX = (int) Math.round(logicalCenterX - visualInset(slotLogicalWidth(state, scale), visualW) - visualW / 2.0);
        int anchorY = (int) Math.round(logicalCenterY - visualInset(slotLogicalHeight(state, scale), visualH) - visualH / 2.0);
        return new CanvasPoint(anchorX, anchorY);
    }

    public static int snapToGrid(TabletUiState state, int value) {
        if (!state.gridSnapLocked) {
            return value;
        }
        int step = gridSize(state);
        return Math.round((float) value / (float) step) * step;
    }

    public static int snapValueToGrid(int value, int grid) {
        int safeGrid = Math.max(1, grid);
        return Math.round((float) value / (float) safeGrid) * safeGrid;
    }

    public static int slotSpanForVisualSize(int visualSize) {
        return slotLogicalSpan(Math.max(1, visualSize));
    }

    public static int visualInsetForSlot(int slotLogicalSize, int visualLogicalSize) {
        if (slotLogicalSize <= visualLogicalSize) {
            return 0;
        }
        int centered = (slotLogicalSize - visualLogicalSize) / 2;
        return Math.min(slotLogicalSize - visualLogicalSize, Math.max(QUEST_CELL_MARGIN, centered));
    }

    public static int[] rotatedBounds(int x, int y, int width, int height, int rotationDegrees) {
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double radians = Math.toRadians(normalizeDegrees(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double halfW = width / 2.0;
        double halfH = height / 2.0;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {-halfW, -halfH},
                {halfW, -halfH},
                {halfW, halfH},
                {-halfW, halfH}
        };
        for (double[] corner : corners) {
            double sx = centerX + corner[0] * cos - corner[1] * sin;
            double sy = centerY + corner[0] * sin + corner[1] * cos;
            minX = Math.min(minX, sx);
            minY = Math.min(minY, sy);
            maxX = Math.max(maxX, sx);
            maxY = Math.max(maxY, sy);
        }
        return new int[]{
                (int) Math.floor(minX),
                (int) Math.floor(minY),
                (int) Math.ceil(maxX),
                (int) Math.ceil(maxY)
        };
    }

    public static CanvasPoint fitRotatedAnchorToGrid(int x, int y, int oldWidth, int oldHeight, int newWidth, int newHeight, int rotationDegrees, int grid) {
        int safeGrid = Math.max(1, grid);
        int[] oldBounds = rotatedBounds(x, y, oldWidth, oldHeight, rotationDegrees);
        int targetLeft = snapValueToGrid(oldBounds[0], safeGrid);
        int targetTop = snapValueToGrid(oldBounds[1], safeGrid);
        int[] nextBounds = rotatedBounds(x, y, newWidth, newHeight, rotationDegrees);
        return new CanvasPoint(x + targetLeft - nextBounds[0], y + targetTop - nextBounds[1]);
    }

    public static ResizedBox resizeRotatedFromBottomRight(
            double mouseX,
            double mouseY,
            int startX,
            int startY,
            int startWidth,
            int startHeight,
            int rotationDegrees,
            int minWidth,
            int minHeight,
            int grid,
            boolean snapSizeToGrid,
            boolean preserveAspect
    ) {
        return resizeRotatedFromCorner(
                mouseX,
                mouseY,
                startX,
                startY,
                startWidth,
                startHeight,
                rotationDegrees,
                minWidth,
                minHeight,
                grid,
                snapSizeToGrid,
                preserveAspect,
                1,
                1
        );
    }

    public static ResizedBox resizeRotatedFromCorner(
            double mouseX,
            double mouseY,
            int startX,
            int startY,
            int startWidth,
            int startHeight,
            int rotationDegrees,
            int minWidth,
            int minHeight,
            int grid,
            boolean snapSizeToGrid,
            boolean preserveAspect,
            int cornerX,
            int cornerY
    ) {
        int safeStartWidth = Math.max(1, startWidth);
        int safeStartHeight = Math.max(1, startHeight);
        int safeMinWidth = Math.max(1, minWidth);
        int safeMinHeight = Math.max(1, minHeight);
        int sx = cornerX < 0 ? -1 : 1;
        int sy = cornerY < 0 ? -1 : 1;
        double radians = Math.toRadians(normalizeDegrees(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double centerX = startX + safeStartWidth / 2.0;
        double centerY = startY + safeStartHeight / 2.0;
        double fixedX = centerX - sx * safeStartWidth * cos / 2.0 + sy * safeStartHeight * sin / 2.0;
        double fixedY = centerY - sx * safeStartWidth * sin / 2.0 - sy * safeStartHeight * cos / 2.0;
        double vectorX = mouseX - fixedX;
        double vectorY = mouseY - fixedY;
        double nextWidth = sx * (vectorX * cos + vectorY * sin);
        double nextHeight = sy * (-vectorX * sin + vectorY * cos);
        nextWidth = Math.max(safeMinWidth, nextWidth);
        nextHeight = Math.max(safeMinHeight, nextHeight);
        if (preserveAspect) {
            double scale = Math.max(nextWidth / safeStartWidth, nextHeight / safeStartHeight);
            nextWidth = safeStartWidth * scale;
            nextHeight = safeStartHeight * scale;
        }
        int roundedWidth = Math.max(safeMinWidth, (int) Math.round(nextWidth));
        int roundedHeight = Math.max(safeMinHeight, (int) Math.round(nextHeight));
        if (snapSizeToGrid) {
            roundedWidth = snapSpanToGrid(roundedWidth, grid, safeMinWidth);
            roundedHeight = snapSpanToGrid(roundedHeight, grid, safeMinHeight);
        }
        double nextCenterX = fixedX + sx * roundedWidth * cos / 2.0 - sy * roundedHeight * sin / 2.0;
        double nextCenterY = fixedY + sx * roundedWidth * sin / 2.0 + sy * roundedHeight * cos / 2.0;
        int nextX = (int) Math.round(nextCenterX - roundedWidth / 2.0);
        int nextY = (int) Math.round(nextCenterY - roundedHeight / 2.0);
        return new ResizedBox(nextX, nextY, roundedWidth, roundedHeight);
    }

    public static CanvasPoint clampAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int slotLogicalW, int slotLogicalH) {
        if (!state.gridCanvasLocked) {
            return new CanvasPoint(logicalX, logicalY);
        }
        int maxX = Math.max(0, state.canvasContentW - Math.max(1, slotLogicalW));
        int maxY = Math.max(0, state.canvasContentH - Math.max(1, slotLogicalH));
        return new CanvasPoint(
                Math.max(0, Math.min(maxX, logicalX)),
                Math.max(0, Math.min(maxY, logicalY))
        );
    }

    public static float snapScaleToGrid(TabletUiState state, float scale) {
        float normalized = Math.max(0.5f, scale);
        if (!state.gridSnapLocked) {
            return normalized;
        }
        int grid = gridSize(state);
        float targetVisible = Math.max(1.0f, TabletUiFactory.CARD_W * normalized);
        int cells = Math.max(1, Math.round((targetVisible + QUEST_CELL_MARGIN) / Math.max(1.0f, grid)));
        float snapped = ((cells * grid) - QUEST_CELL_MARGIN) / (float) TabletUiFactory.CARD_W;
        return Math.max(0.5f, snapped);
    }

    public static int visualInsetX(TabletUiState state, float scale) {
        return visualInset(slotLogicalWidth(state, scale), visualLogicalWidth(scale));
    }

    public static int visualInsetY(TabletUiState state, float scale) {
        return visualInset(slotLogicalHeight(state, scale), visualLogicalHeight(scale));
    }

    public static int visualLogicalWidth(float scale) {
        return Math.max(1, Math.round(TabletUiFactory.CARD_W * Math.max(0.5f, scale)));
    }

    public static int visualLogicalHeight(float scale) {
        return Math.max(1, Math.round(TabletUiFactory.CARD_H * Math.max(0.5f, scale)));
    }

    public static int slotLogicalWidth(TabletUiState state, float scale) {
        return slotSpanForVisualSize(visualLogicalWidth(scale));
    }

    public static int slotLogicalHeight(TabletUiState state, float scale) {
        return slotSpanForVisualSize(visualLogicalHeight(scale));
    }

    public static int gridSize(TabletUiState state) {
        int index = Math.max(0, Math.min(TabletUiFactory.GRID_SIZES.length - 1, state.gridSizeIndex));
        return Math.max(1, TabletUiFactory.GRID_SIZES[index]);
    }

    private static int slotLogicalSpan(int visualLogicalSize) {
        int needed = Math.max(1, visualLogicalSize + QUEST_CELL_MARGIN);
        int cells = Math.max(1, (needed + QUEST_SLOT_SIZE - 1) / QUEST_SLOT_SIZE);
        return Math.max(QUEST_SLOT_SIZE, cells * QUEST_SLOT_SIZE);
    }

    private static int snapSpanToGrid(int value, int grid, int min) {
        int safeGrid = Math.max(1, grid);
        int snapped = Math.max(safeGrid, Math.round((float) Math.max(1, value) / (float) safeGrid) * safeGrid);
        while (snapped < min) {
            snapped += safeGrid;
        }
        return snapped;
    }

    private static int normalizeDegrees(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    private static int visualInset(int slotLogicalSize, int visualLogicalSize) {
        return visualInsetForSlot(slotLogicalSize, visualLogicalSize);
    }

    private static int visualScreenSize(TabletUiState state, int visualLogicalSize, int slotLogicalSize, int slotScreenSize) {
        int preferred = Math.max(1, Math.round(visualLogicalSize * zoom(state)));
        int insideSlot = Math.max(1, slotScreenSize - QUEST_CELL_MARGIN);
        if (visualLogicalSize + QUEST_CELL_MARGIN >= slotLogicalSize) {
            return insideSlot;
        }
        return Math.max(1, Math.min(preferred, insideSlot));
    }

    private static int visualScreenInset(int slotScreenSize, int visualScreenSize) {
        if (slotScreenSize <= visualScreenSize) {
            return 0;
        }
        int centered = (slotScreenSize - visualScreenSize) / 2;
        return Math.min(slotScreenSize - visualScreenSize, Math.max(1, centered));
    }

    private static float scaleFromGroup(TabletUiState state, String questId, CompoundTag groupTag) {
        float scale = groupTag.contains("scale") ? groupTag.getFloat("scale") : 1.0f;
        Float transientScale = state.transientQuestScales.get(questId);
        if (transientScale != null && !Float.isNaN(transientScale) && !Float.isInfinite(transientScale)) {
            scale = transientScale;
        }
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            return 1.0f;
        }
        return Math.max(0.5f, scale);
    }

    private static float zoom(TabletUiState state) {
        return CanvasRenderer.clampZoom(state.canvasZoom);
    }

    public record ResizedBox(int x, int y, int width, int height) {
    }
}

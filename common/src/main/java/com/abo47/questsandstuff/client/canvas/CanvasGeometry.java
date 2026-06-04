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

    public static int snapVisualSpanToGridSlot(int value, int grid, int min) {
        int safeGrid = Math.max(1, grid);
        int safeMin = Math.max(1, min);
        int slot = Math.max(safeGrid, Math.round((float) (Math.max(1, value) + QUEST_CELL_MARGIN) / (float) safeGrid) * safeGrid);
        int visual = Math.max(1, slot - QUEST_CELL_MARGIN);
        while (visual < safeMin) {
            slot += safeGrid;
            visual = Math.max(1, slot - QUEST_CELL_MARGIN);
        }
        return visual;
    }

    public static GridVisualBox fitVisualBoxToGridSlot(int x, int y, int width, int height, int grid, int minWidth, int minHeight) {
        int safeGrid = Math.max(1, grid);
        int visualW = snapVisualSpanToGridSlot(width, safeGrid, minWidth);
        int visualH = snapVisualSpanToGridSlot(height, safeGrid, minHeight);
        int slotW = slotSpanForVisualSize(visualW);
        int slotH = slotSpanForVisualSize(visualH);
        int insetX = visualInsetForSlot(slotW, visualW);
        int insetY = visualInsetForSlot(slotH, visualH);
        int slotX = snapValueToGrid(x - insetX, safeGrid);
        int slotY = snapValueToGrid(y - insetY, safeGrid);
        return new GridVisualBox(slotX + insetX, slotY + insetY, visualW, visualH, slotX, slotY, slotW, slotH);
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
        return rotatedBoundsAtPivot(x, y, width, height, Math.max(1, width) / 2, Math.max(1, height) / 2, rotationDegrees);
    }

    public static int[] rotatedBoundsAtPivot(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safePivotX = Math.max(0, Math.min(safeWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeHeight, pivotY));
        double effectivePivotX = effectivePivot(safePivotX, safeWidth);
        double effectivePivotY = effectivePivot(safePivotY, safeHeight);
        int rotation = normalizeDegrees(rotationDegrees);
        if (isCardinalTurn(rotation)) {
            return cardinalRotatedBoundsAtPivot(x, y, safeWidth, safeHeight, effectivePivotX, effectivePivotY, rotation);
        }
        double centerX = x + effectivePivotX;
        double centerY = y + effectivePivotY;
        double radians = Math.toRadians(rotation);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {-effectivePivotX, -effectivePivotY},
                {safeWidth - effectivePivotX, -effectivePivotY},
                {safeWidth - effectivePivotX, safeHeight - effectivePivotY},
                {-effectivePivotX, safeHeight - effectivePivotY}
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
                floorClean(minX),
                floorClean(minY),
                ceilClean(maxX),
                ceilClean(maxY)
        };
    }

    public static CanvasPoint fitRotatedAnchorToGrid(int x, int y, int oldWidth, int oldHeight, int newWidth, int newHeight, int rotationDegrees, int grid) {
        return fitRotatedAnchorToGridAtPivot(
                x,
                y,
                oldWidth,
                oldHeight,
                newWidth,
                newHeight,
                Math.max(1, oldWidth) / 2,
                Math.max(1, oldHeight) / 2,
                Math.max(1, newWidth) / 2,
                Math.max(1, newHeight) / 2,
                rotationDegrees,
                grid
        );
    }

    public static CanvasPoint fitRotatedAnchorToGridAtPivot(
            int x,
            int y,
            int oldWidth,
            int oldHeight,
            int newWidth,
            int newHeight,
            int oldPivotX,
            int oldPivotY,
            int newPivotX,
            int newPivotY,
            int rotationDegrees,
            int grid
    ) {
        int safeGrid = Math.max(1, grid);
        int[] oldBounds = rotatedBoundsAtPivot(x, y, oldWidth, oldHeight, oldPivotX, oldPivotY, rotationDegrees);
        int targetLeft = snapValueToGrid(oldBounds[0], safeGrid);
        int targetTop = snapValueToGrid(oldBounds[1], safeGrid);
        int[] nextBounds = rotatedBoundsAtPivot(x, y, newWidth, newHeight, newPivotX, newPivotY, rotationDegrees);
        return new CanvasPoint(x + targetLeft - nextBounds[0], y + targetTop - nextBounds[1]);
    }

    public static CanvasPoint fitRotatedBoundsToGridSlotAtPivot(
            int x,
            int y,
            int width,
            int height,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int grid,
            int minBoundsWidth,
            int minBoundsHeight
    ) {
        int[] bounds = rotatedBoundsAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees);
        GridVisualBox box = fitVisualBoxToGridSlot(
                bounds[0],
                bounds[1],
                Math.max(1, bounds[2] - bounds[0]),
                Math.max(1, bounds[3] - bounds[1]),
                grid,
                minBoundsWidth,
                minBoundsHeight
        );
        return new CanvasPoint(x + box.x() - bounds[0], y + box.y() - bounds[1]);
    }

    public static GridFittedBox fitRotatedElementToGridSlotAtPivot(
            int x,
            int y,
            int width,
            int height,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int grid,
            int minWidth,
            int minHeight
    ) {
        int safeRotation = normalizeDegrees(rotationDegrees);
        int nextW = snapVisualSpanToGridSlot(width, grid, minWidth);
        int nextH = snapVisualSpanToGridSlot(height, grid, minHeight);
        if (isQuarterTurn(safeRotation)) {
            int[] bounds = rotatedBoundsAtPivot(x, y, width, height, pivotX, pivotY, safeRotation);
            int boundsW = Math.max(1, bounds[2] - bounds[0]);
            int boundsH = Math.max(1, bounds[3] - bounds[1]);
            nextW = snapVisualSpanToGridSlot(boundsH, grid, minWidth);
            nextH = snapVisualSpanToGridSlot(boundsW, grid, minHeight);
        }
        int nextPivotX = scaledPivot(pivotX, width, nextW);
        int nextPivotY = scaledPivot(pivotY, height, nextH);
        CanvasPoint anchor = fitRotatedBoundsToGridSlotAtPivot(
                x,
                y,
                nextW,
                nextH,
                nextPivotX,
                nextPivotY,
                safeRotation,
                grid,
                isQuarterTurn(safeRotation) ? minHeight : minWidth,
                isQuarterTurn(safeRotation) ? minWidth : minHeight
        );
        return new GridFittedBox(anchor.x, anchor.y, nextW, nextH);
    }

    public static ResizedBox fitRotatedElementToVisualBoundsAtPivot(
            int targetLeft,
            int targetTop,
            int targetWidth,
            int targetHeight,
            int oldWidth,
            int oldHeight,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int minWidth,
            int minHeight
    ) {
        int rotation = normalizeDegrees(rotationDegrees);
        int safeTargetWidth = Math.max(1, targetWidth);
        int safeTargetHeight = Math.max(1, targetHeight);
        int safeOldWidth = Math.max(1, oldWidth);
        int safeOldHeight = Math.max(1, oldHeight);
        int safeMinWidth = Math.max(1, minWidth);
        int safeMinHeight = Math.max(1, minHeight);
        int safePivotX = Math.max(0, Math.min(safeOldWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeOldHeight, pivotY));
        int nextWidth = isQuarterTurn(rotation) ? safeTargetHeight : safeTargetWidth;
        int nextHeight = isQuarterTurn(rotation) ? safeTargetWidth : safeTargetHeight;
        nextWidth = Math.max(safeMinWidth, nextWidth);
        nextHeight = Math.max(safeMinHeight, nextHeight);
        int nextPivotX = scaledPivot(safePivotX, safeOldWidth, nextWidth);
        int nextPivotY = scaledPivot(safePivotY, safeOldHeight, nextHeight);
        int[] relativeBounds = rotatedBoundsAtPivot(0, 0, nextWidth, nextHeight, nextPivotX, nextPivotY, rotation);
        return new ResizedBox(targetLeft - relativeBounds[0], targetTop - relativeBounds[1], nextWidth, nextHeight);
    }

    public static boolean isQuarterTurn(int rotationDegrees) {
        int normalized = normalizeDegrees(rotationDegrees);
        return normalized == 90 || normalized == 270;
    }

    public static boolean isCardinalTurn(int rotationDegrees) {
        int normalized = normalizeDegrees(rotationDegrees);
        return normalized == 0 || normalized == 90 || normalized == 180 || normalized == 270;
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
        return resizeRotatedFromCornerAtPivot(
                mouseX,
                mouseY,
                startX,
                startY,
                startWidth,
                startHeight,
                safeStartWidth / 2,
                safeStartHeight / 2,
                rotationDegrees,
                minWidth,
                minHeight,
                grid,
                snapSizeToGrid,
                preserveAspect,
                cornerX,
                cornerY
        );
    }

    public static ResizedBox resizeRotatedFromCornerAtPivot(
            double mouseX,
            double mouseY,
            int startX,
            int startY,
            int startWidth,
            int startHeight,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int minWidth,
            int minHeight,
            int grid,
            boolean snapSizeToGrid,
            boolean preserveAspect,
            int cornerX,
            int cornerY
    ) {
        boolean snapCardinalGrid = snapSizeToGrid && isCardinalTurn(rotationDegrees);
        if (snapCardinalGrid) {
            return resizeCardinalVisualBoundsFromCornerAtPivot(
                    mouseX,
                    mouseY,
                    startX,
                    startY,
                    startWidth,
                    startHeight,
                    pivotX,
                    pivotY,
                    rotationDegrees,
                    minWidth,
                    minHeight,
                    grid,
                    preserveAspect,
                    cornerX,
                    cornerY
            );
        }
        int safeStartWidth = Math.max(1, startWidth);
        int safeStartHeight = Math.max(1, startHeight);
        int safePivotX = Math.max(0, Math.min(safeStartWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeStartHeight, pivotY));
        double effectivePivotX = effectivePivot(safePivotX, safeStartWidth);
        double effectivePivotY = effectivePivot(safePivotY, safeStartHeight);
        int safeMinWidth = Math.max(1, minWidth);
        int safeMinHeight = Math.max(1, minHeight);
        int sx = cornerX < 0 ? -1 : 1;
        int sy = cornerY < 0 ? -1 : 1;
        double radians = Math.toRadians(normalizeDegrees(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double pivotWorldX = startX + effectivePivotX;
        double pivotWorldY = startY + effectivePivotY;
        double fixedLocalX = sx >= 0 ? -effectivePivotX : safeStartWidth - effectivePivotX;
        double fixedLocalY = sy >= 0 ? -effectivePivotY : safeStartHeight - effectivePivotY;
        double fixedX = pivotWorldX + fixedLocalX * cos - fixedLocalY * sin;
        double fixedY = pivotWorldY + fixedLocalX * sin + fixedLocalY * cos;
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
        int nextPivotX = scaledPivot(safePivotX, safeStartWidth, roundedWidth);
        int nextPivotY = scaledPivot(safePivotY, safeStartHeight, roundedHeight);
        double effectiveNextPivotX = effectivePivot(nextPivotX, roundedWidth);
        double effectiveNextPivotY = effectivePivot(nextPivotY, roundedHeight);
        double nextFixedLocalX = sx >= 0 ? -effectiveNextPivotX : roundedWidth - effectiveNextPivotX;
        double nextFixedLocalY = sy >= 0 ? -effectiveNextPivotY : roundedHeight - effectiveNextPivotY;
        double nextPivotWorldX = fixedX - nextFixedLocalX * cos + nextFixedLocalY * sin;
        double nextPivotWorldY = fixedY - nextFixedLocalX * sin - nextFixedLocalY * cos;
        int nextX = (int) Math.round(nextPivotWorldX - effectiveNextPivotX);
        int nextY = (int) Math.round(nextPivotWorldY - effectiveNextPivotY);
        return new ResizedBox(nextX, nextY, roundedWidth, roundedHeight);
    }

    private static ResizedBox resizeCardinalVisualBoundsFromCornerAtPivot(
            double mouseX,
            double mouseY,
            int startX,
            int startY,
            int startWidth,
            int startHeight,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int minWidth,
            int minHeight,
            int grid,
            boolean preserveAspect,
            int cornerX,
            int cornerY
    ) {
        int rotation = normalizeDegrees(rotationDegrees);
        int safeStartWidth = Math.max(1, startWidth);
        int safeStartHeight = Math.max(1, startHeight);
        int safePivotX = Math.max(0, Math.min(safeStartWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeStartHeight, pivotY));
        int[] startBounds = rotatedBoundsAtPivot(startX, startY, safeStartWidth, safeStartHeight, safePivotX, safePivotY, rotation);
        double[] draggedCorner = rotatedCorner(startX, startY, safeStartWidth, safeStartHeight, safePivotX, safePivotY, rotation, cornerX, cornerY);
        double centerX = (startBounds[0] + startBounds[2]) / 2.0D;
        double centerY = (startBounds[1] + startBounds[3]) / 2.0D;
        int dragSignX = draggedCorner[0] >= centerX ? 1 : -1;
        int dragSignY = draggedCorner[1] >= centerY ? 1 : -1;
        double fixedX = dragSignX >= 0 ? startBounds[0] : startBounds[2];
        double fixedY = dragSignY >= 0 ? startBounds[1] : startBounds[3];
        int minVisualWidth = isQuarterTurn(rotation) ? Math.max(1, minHeight) : Math.max(1, minWidth);
        int minVisualHeight = isQuarterTurn(rotation) ? Math.max(1, minWidth) : Math.max(1, minHeight);
        double targetVisualWidth = Math.max(minVisualWidth, dragSignX >= 0 ? mouseX - fixedX : fixedX - mouseX);
        double targetVisualHeight = Math.max(minVisualHeight, dragSignY >= 0 ? mouseY - fixedY : fixedY - mouseY);
        if (preserveAspect) {
            int startVisualWidth = Math.max(1, startBounds[2] - startBounds[0]);
            int startVisualHeight = Math.max(1, startBounds[3] - startBounds[1]);
            double scale = Math.max(targetVisualWidth / startVisualWidth, targetVisualHeight / startVisualHeight);
            targetVisualWidth = Math.max(minVisualWidth, startVisualWidth * scale);
            targetVisualHeight = Math.max(minVisualHeight, startVisualHeight * scale);
        }
        int visualWidth = snapVisualSpanToGridSlot((int) Math.round(targetVisualWidth), grid, minVisualWidth);
        int visualHeight = snapVisualSpanToGridSlot((int) Math.round(targetVisualHeight), grid, minVisualHeight);
        double targetLeft = dragSignX >= 0 ? fixedX : fixedX - visualWidth;
        double targetTop = dragSignY >= 0 ? fixedY : fixedY - visualHeight;
        return fitRotatedElementToVisualBoundsAtPivot(
                (int) Math.round(targetLeft),
                (int) Math.round(targetTop),
                visualWidth,
                visualHeight,
                safeStartWidth,
                safeStartHeight,
                safePivotX,
                safePivotY,
                rotation,
                minWidth,
                minHeight
        );
    }

    private static double[] rotatedCorner(
            int x,
            int y,
            int width,
            int height,
            int pivotX,
            int pivotY,
            int rotationDegrees,
            int cornerX,
            int cornerY
    ) {
        int sx = cornerX < 0 ? -1 : 1;
        int sy = cornerY < 0 ? -1 : 1;
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safePivotX = Math.max(0, Math.min(safeWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeHeight, pivotY));
        double effectivePivotX = effectivePivot(safePivotX, safeWidth);
        double effectivePivotY = effectivePivot(safePivotY, safeHeight);
        double localX = sx >= 0 ? safeWidth - effectivePivotX : -effectivePivotX;
        double localY = sy >= 0 ? safeHeight - effectivePivotY : -effectivePivotY;
        int rotation = normalizeDegrees(rotationDegrees);
        if (isCardinalTurn(rotation)) {
            double[] rotated = rotateCardinalLocal(localX, localY, rotation);
            return new double[]{
                    x + effectivePivotX + rotated[0],
                    y + effectivePivotY + rotated[1]
            };
        }
        double radians = Math.toRadians(rotation);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double pivotWorldX = x + effectivePivotX;
        double pivotWorldY = y + effectivePivotY;
        return new double[]{
                pivotWorldX + localX * cos - localY * sin,
                pivotWorldY + localX * sin + localY * cos
        };
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

    public static CanvasPoint clampRotatedAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        if (!state.gridCanvasLocked) {
            return new CanvasPoint(logicalX, logicalY);
        }
        int[] bounds = rotatedBoundsAtPivot(logicalX, logicalY, width, height, pivotX, pivotY, rotationDegrees);
        int boundsWidth = Math.max(1, bounds[2] - bounds[0]);
        int boundsHeight = Math.max(1, bounds[3] - bounds[1]);
        int targetLeft = boundsWidth > state.canvasContentW
                ? 0
                : Math.max(0, Math.min(Math.max(0, state.canvasContentW - boundsWidth), bounds[0]));
        int targetTop = boundsHeight > state.canvasContentH
                ? 0
                : Math.max(0, Math.min(Math.max(0, state.canvasContentH - boundsHeight), bounds[1]));
        return new CanvasPoint(logicalX + targetLeft - bounds[0], logicalY + targetTop - bounds[1]);
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

    private static int scaledPivot(int pivot, int oldSpan, int newSpan) {
        int safeOld = Math.max(1, oldSpan);
        int safeNew = Math.max(1, newSpan);
        int safePivot = Math.max(0, Math.min(safeOld, pivot));
        if (safePivot == safeOld / 2) {
            return safeNew / 2;
        }
        return Math.max(0, Math.min(safeNew, Math.round(safePivot * (float) safeNew / (float) safeOld)));
    }

    private static double effectivePivot(int pivot, int span) {
        int safeSpan = Math.max(1, span);
        int safePivot = Math.max(0, Math.min(safeSpan, pivot));
        return safePivot == safeSpan / 2 ? safeSpan / 2.0D : safePivot;
    }

    private static int[] cardinalRotatedBoundsAtPivot(
            int x,
            int y,
            int width,
            int height,
            double effectivePivotX,
            double effectivePivotY,
            int rotation
    ) {
        double centerX = x + effectivePivotX;
        double centerY = y + effectivePivotY;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {-effectivePivotX, -effectivePivotY},
                {width - effectivePivotX, -effectivePivotY},
                {width - effectivePivotX, height - effectivePivotY},
                {-effectivePivotX, height - effectivePivotY}
        };
        for (double[] corner : corners) {
            double[] rotated = rotateCardinalLocal(corner[0], corner[1], rotation);
            double sx = centerX + rotated[0];
            double sy = centerY + rotated[1];
            minX = Math.min(minX, sx);
            minY = Math.min(minY, sy);
            maxX = Math.max(maxX, sx);
            maxY = Math.max(maxY, sy);
        }
        return new int[]{
                floorClean(minX),
                floorClean(minY),
                ceilClean(maxX),
                ceilClean(maxY)
        };
    }

    private static double[] rotateCardinalLocal(double x, double y, int rotation) {
        return switch (rotation) {
            case 90 -> new double[]{-y, x};
            case 180 -> new double[]{-x, -y};
            case 270 -> new double[]{y, -x};
            default -> new double[]{x, y};
        };
    }

    private static int floorClean(double value) {
        double nearest = Math.rint(value);
        if (Math.abs(value - nearest) < 1.0E-7D) {
            return (int) nearest;
        }
        return (int) Math.floor(value);
    }

    private static int ceilClean(double value) {
        double nearest = Math.rint(value);
        if (Math.abs(value - nearest) < 1.0E-7D) {
            return (int) nearest;
        }
        return (int) Math.ceil(value);
    }

    public static int normalizeDegrees(int degrees) {
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

    public record GridVisualBox(int x, int y, int width, int height, int slotX, int slotY, int slotWidth, int slotHeight) {
    }

    public record GridFittedBox(int x, int y, int width, int height) {
    }
}

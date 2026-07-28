package com.abo47.questsandstuff.client.tablet.quest.canvas;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasGeometry {
    private CanvasGeometry() {
    }

    public static QuestCardLayout layoutQuest(String questId, CompoundTag questTag, TabletUiState state, String selectedChapter) {
        return CanvasQuestCardGeometry.layoutQuest(questId, questTag, state, selectedChapter);
    }

    public static int screenX(TabletUiState state, double logicalX) {
        return CanvasCoordinateMapper.screenX(state, logicalX);
    }

    public static int screenY(TabletUiState state, double logicalY) {
        return CanvasCoordinateMapper.screenY(state, logicalY);
    }

    public static int screenWidth(TabletUiState state, int logicalLeft, int logicalRight) {
        return CanvasCoordinateMapper.screenWidth(state, logicalLeft, logicalRight);
    }

    public static int screenHeight(TabletUiState state, int logicalTop, int logicalBottom) {
        return CanvasCoordinateMapper.screenHeight(state, logicalTop, logicalBottom);
    }

    public static int screenSpan(TabletUiState state, int logicalSize) {
        return CanvasCoordinateMapper.screenSpan(state, logicalSize);
    }

    public static double screenToLogicalX(TabletUiState state, int screenX) {
        return CanvasCoordinateMapper.screenToLogicalX(state, screenX);
    }

    public static double screenToLogicalY(TabletUiState state, int screenY) {
        return CanvasCoordinateMapper.screenToLogicalY(state, screenY);
    }

    public static int screenToNearestLogicalX(TabletUiState state, int screenX) {
        return CanvasCoordinateMapper.screenToNearestLogicalX(state, screenX);
    }

    public static int screenToNearestLogicalY(TabletUiState state, int screenY) {
        return CanvasCoordinateMapper.screenToNearestLogicalY(state, screenY);
    }

    public static CanvasPoint anchorForScreenVisualCenter(TabletUiState state, int screenX, int screenY, float scale) {
        return CanvasQuestCardGeometry.anchorForScreenVisualCenter(state, screenX, screenY, scale);
    }

    public static CanvasPoint anchorForVisualCenter(TabletUiState state, double logicalCenterX, double logicalCenterY, float scale) {
        return CanvasQuestCardGeometry.anchorForVisualCenter(state, logicalCenterX, logicalCenterY, scale);
    }

    public static int snapToGrid(TabletUiState state, int value) {
        return CanvasGridMath.snapToGrid(state, value);
    }

    public static int snapValueToGrid(int value, int grid) {
        return CanvasGridMath.snapValueToGrid(value, grid);
    }

    public static int snapVisualSpanToGridSlot(int value, int grid, int min) {
        return CanvasGridMath.snapVisualSpanToGridSlot(value, grid, min);
    }

    public static GridVisualBox fitVisualBoxToGridSlot(int x, int y, int width, int height, int grid, int minWidth, int minHeight) {
        return CanvasGridMath.fitVisualBoxToGridSlot(x, y, width, height, grid, minWidth, minHeight);
    }

    public static int slotSpanForVisualSize(int visualSize) {
        return CanvasGridMath.slotSpanForVisualSize(visualSize);
    }

    public static int visualInsetForSlot(int slotLogicalSize, int visualLogicalSize) {
        return CanvasGridMath.visualInsetForSlot(slotLogicalSize, visualLogicalSize);
    }

    public static int[] rotatedBounds(int x, int y, int width, int height, int rotationDegrees) {
        return CanvasRotationMath.rotatedBounds(x, y, width, height, rotationDegrees);
    }

    public static int[] rotatedBoundsAtPivot(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return CanvasRotationMath.rotatedBoundsAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees);
    }

    public static CanvasPoint fitRotatedAnchorToGrid(int x, int y, int oldWidth, int oldHeight, int newWidth, int newHeight, int rotationDegrees, int grid) {
        return CanvasResizeMath.fitRotatedAnchorToGrid(x, y, oldWidth, oldHeight, newWidth, newHeight, rotationDegrees, grid);
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
        return CanvasResizeMath.fitRotatedAnchorToGridAtPivot(
                x,
                y,
                oldWidth,
                oldHeight,
                newWidth,
                newHeight,
                oldPivotX,
                oldPivotY,
                newPivotX,
                newPivotY,
                rotationDegrees,
                grid
        );
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
        return CanvasResizeMath.fitRotatedBoundsToGridSlotAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees, grid, minBoundsWidth, minBoundsHeight);
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
        return CanvasResizeMath.fitRotatedElementToGridSlotAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees, grid, minWidth, minHeight);
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
        return CanvasResizeMath.fitRotatedElementToVisualBoundsAtPivot(
                targetLeft,
                targetTop,
                targetWidth,
                targetHeight,
                oldWidth,
                oldHeight,
                pivotX,
                pivotY,
                rotationDegrees,
                minWidth,
                minHeight
        );
    }

    public static boolean isQuarterTurn(int rotationDegrees) {
        return CanvasRotationMath.isQuarterTurn(rotationDegrees);
    }

    public static boolean isCardinalTurn(int rotationDegrees) {
        return CanvasRotationMath.isCardinalTurn(rotationDegrees);
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
        return CanvasResizeMath.resizeRotatedFromBottomRight(
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
                preserveAspect
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
        return CanvasResizeMath.resizeRotatedFromCorner(
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
        return CanvasResizeMath.resizeRotatedFromCornerAtPivot(
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
                snapSizeToGrid,
                preserveAspect,
                cornerX,
                cornerY
        );
    }

    public static CanvasPoint clampAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int slotLogicalW, int slotLogicalH) {
        return CanvasClamp.clampAnchorToCanvas(state, logicalX, logicalY, slotLogicalW, slotLogicalH);
    }

    public static CanvasPoint clampRotatedAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return CanvasClamp.clampRotatedAnchorToCanvas(state, logicalX, logicalY, width, height, pivotX, pivotY, rotationDegrees);
    }

    public static float snapScaleToGrid(TabletUiState state, float scale) {
        return CanvasGridMath.snapScaleToGrid(state, scale);
    }

    public static int visualInsetX(TabletUiState state, float scale) {
        return CanvasGridMath.visualInsetX(state, scale);
    }

    public static int visualInsetY(TabletUiState state, float scale) {
        return CanvasGridMath.visualInsetY(state, scale);
    }

    public static int visualLogicalWidth(float scale) {
        return CanvasGridMath.visualLogicalWidth(scale);
    }

    public static int visualLogicalHeight(float scale) {
        return CanvasGridMath.visualLogicalHeight(scale);
    }

    public static int slotLogicalWidth(TabletUiState state, float scale) {
        return CanvasGridMath.slotLogicalWidth(state, scale);
    }

    public static int slotLogicalHeight(TabletUiState state, float scale) {
        return CanvasGridMath.slotLogicalHeight(state, scale);
    }

    public static int gridSize(TabletUiState state) {
        return CanvasGridMath.gridSize(state);
    }

    public static int normalizeDegrees(int degrees) {
        return CanvasRotationMath.normalizeDegrees(degrees);
    }

    public static int dragDelta(TabletUiState state, double logicalStart, double logicalDelta) {
        return CanvasCoordinateMapper.screenX(state, logicalStart + logicalDelta) - CanvasCoordinateMapper.screenX(state, logicalStart);
    }

    public record ResizedBox(int x, int y, int width, int height) {
    }

    public record GridVisualBox(int x, int y, int width, int height, int slotX, int slotY, int slotWidth, int slotHeight) {
    }

    public record GridFittedBox(int x, int y, int width, int height) {
    }
}

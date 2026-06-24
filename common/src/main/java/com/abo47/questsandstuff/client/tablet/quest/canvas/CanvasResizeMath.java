package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;

final class CanvasResizeMath {
    private CanvasResizeMath() {
    }

    static CanvasPoint fitRotatedAnchorToGrid(int x, int y, int oldWidth, int oldHeight, int newWidth, int newHeight, int rotationDegrees, int grid) {
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

    static CanvasPoint fitRotatedAnchorToGridAtPivot(
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
        int[] oldBounds = CanvasRotationMath.rotatedBoundsAtPivot(x, y, oldWidth, oldHeight, oldPivotX, oldPivotY, rotationDegrees);
        int targetLeft = CanvasGridMath.snapValueToGrid(oldBounds[0], safeGrid);
        int targetTop = CanvasGridMath.snapValueToGrid(oldBounds[1], safeGrid);
        int[] nextBounds = CanvasRotationMath.rotatedBoundsAtPivot(x, y, newWidth, newHeight, newPivotX, newPivotY, rotationDegrees);
        return new CanvasPoint(x + targetLeft - nextBounds[0], y + targetTop - nextBounds[1]);
    }

    static CanvasPoint fitRotatedBoundsToGridSlotAtPivot(
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
        int[] bounds = CanvasRotationMath.rotatedBoundsAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees);
        CanvasGeometry.GridVisualBox box = CanvasGridMath.fitVisualBoxToGridSlot(
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

    static CanvasGeometry.GridFittedBox fitRotatedElementToGridSlotAtPivot(
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
        int safeRotation = CanvasRotationMath.normalizeDegrees(rotationDegrees);
        int nextW = CanvasGridMath.snapVisualSpanToGridSlot(width, grid, minWidth);
        int nextH = CanvasGridMath.snapVisualSpanToGridSlot(height, grid, minHeight);
        if (CanvasRotationMath.isQuarterTurn(safeRotation)) {
            int[] bounds = CanvasRotationMath.rotatedBoundsAtPivot(x, y, width, height, pivotX, pivotY, safeRotation);
            int boundsW = Math.max(1, bounds[2] - bounds[0]);
            int boundsH = Math.max(1, bounds[3] - bounds[1]);
            nextW = CanvasGridMath.snapVisualSpanToGridSlot(boundsH, grid, minWidth);
            nextH = CanvasGridMath.snapVisualSpanToGridSlot(boundsW, grid, minHeight);
        }
        int nextPivotX = CanvasRotationMath.scaledPivot(pivotX, width, nextW);
        int nextPivotY = CanvasRotationMath.scaledPivot(pivotY, height, nextH);
        CanvasPoint anchor = fitRotatedBoundsToGridSlotAtPivot(
                x,
                y,
                nextW,
                nextH,
                nextPivotX,
                nextPivotY,
                safeRotation,
                grid,
                CanvasRotationMath.isQuarterTurn(safeRotation) ? minHeight : minWidth,
                CanvasRotationMath.isQuarterTurn(safeRotation) ? minWidth : minHeight
        );
        return new CanvasGeometry.GridFittedBox(anchor.x, anchor.y, nextW, nextH);
    }

    static CanvasGeometry.ResizedBox fitRotatedElementToVisualBoundsAtPivot(
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
        int rotation = CanvasRotationMath.normalizeDegrees(rotationDegrees);
        int safeTargetWidth = Math.max(1, targetWidth);
        int safeTargetHeight = Math.max(1, targetHeight);
        int safeOldWidth = Math.max(1, oldWidth);
        int safeOldHeight = Math.max(1, oldHeight);
        int safeMinWidth = Math.max(1, minWidth);
        int safeMinHeight = Math.max(1, minHeight);
        int safePivotX = Math.max(0, Math.min(safeOldWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeOldHeight, pivotY));
        int nextWidth = CanvasRotationMath.isQuarterTurn(rotation) ? safeTargetHeight : safeTargetWidth;
        int nextHeight = CanvasRotationMath.isQuarterTurn(rotation) ? safeTargetWidth : safeTargetHeight;
        nextWidth = Math.max(safeMinWidth, nextWidth);
        nextHeight = Math.max(safeMinHeight, nextHeight);
        int nextPivotX = CanvasRotationMath.scaledPivot(safePivotX, safeOldWidth, nextWidth);
        int nextPivotY = CanvasRotationMath.scaledPivot(safePivotY, safeOldHeight, nextHeight);
        int[] relativeBounds = CanvasRotationMath.rotatedBoundsAtPivot(0, 0, nextWidth, nextHeight, nextPivotX, nextPivotY, rotation);
        return new CanvasGeometry.ResizedBox(targetLeft - relativeBounds[0], targetTop - relativeBounds[1], nextWidth, nextHeight);
    }

    static CanvasGeometry.ResizedBox resizeRotatedFromBottomRight(
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

    static CanvasGeometry.ResizedBox resizeRotatedFromCorner(
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

    static CanvasGeometry.ResizedBox resizeRotatedFromCornerAtPivot(
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
        boolean snapCardinalGrid = snapSizeToGrid && CanvasRotationMath.isCardinalTurn(rotationDegrees);
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
        double effectivePivotX = CanvasRotationMath.effectivePivot(safePivotX, safeStartWidth);
        double effectivePivotY = CanvasRotationMath.effectivePivot(safePivotY, safeStartHeight);
        int safeMinWidth = Math.max(1, minWidth);
        int safeMinHeight = Math.max(1, minHeight);
        int sx = cornerX < 0 ? -1 : 1;
        int sy = cornerY < 0 ? -1 : 1;
        double radians = Math.toRadians(CanvasRotationMath.normalizeDegrees(rotationDegrees));
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
        int nextPivotX = CanvasRotationMath.scaledPivot(safePivotX, safeStartWidth, roundedWidth);
        int nextPivotY = CanvasRotationMath.scaledPivot(safePivotY, safeStartHeight, roundedHeight);
        double effectiveNextPivotX = CanvasRotationMath.effectivePivot(nextPivotX, roundedWidth);
        double effectiveNextPivotY = CanvasRotationMath.effectivePivot(nextPivotY, roundedHeight);
        double nextFixedLocalX = sx >= 0 ? -effectiveNextPivotX : roundedWidth - effectiveNextPivotX;
        double nextFixedLocalY = sy >= 0 ? -effectiveNextPivotY : roundedHeight - effectiveNextPivotY;
        double nextPivotWorldX = fixedX - nextFixedLocalX * cos + nextFixedLocalY * sin;
        double nextPivotWorldY = fixedY - nextFixedLocalX * sin - nextFixedLocalY * cos;
        int nextX = (int) Math.round(nextPivotWorldX - effectiveNextPivotX);
        int nextY = (int) Math.round(nextPivotWorldY - effectiveNextPivotY);
        return new CanvasGeometry.ResizedBox(nextX, nextY, roundedWidth, roundedHeight);
    }

    private static CanvasGeometry.ResizedBox resizeCardinalVisualBoundsFromCornerAtPivot(
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
        int rotation = CanvasRotationMath.normalizeDegrees(rotationDegrees);
        int safeStartWidth = Math.max(1, startWidth);
        int safeStartHeight = Math.max(1, startHeight);
        int safePivotX = Math.max(0, Math.min(safeStartWidth, pivotX));
        int safePivotY = Math.max(0, Math.min(safeStartHeight, pivotY));
        int[] startBounds = CanvasRotationMath.rotatedBoundsAtPivot(startX, startY, safeStartWidth, safeStartHeight, safePivotX, safePivotY, rotation);
        double[] draggedCorner = CanvasRotationMath.rotatedCorner(startX, startY, safeStartWidth, safeStartHeight, safePivotX, safePivotY, rotation, cornerX, cornerY);
        double centerX = (startBounds[0] + startBounds[2]) / 2.0D;
        double centerY = (startBounds[1] + startBounds[3]) / 2.0D;
        int dragSignX = draggedCorner[0] >= centerX ? 1 : -1;
        int dragSignY = draggedCorner[1] >= centerY ? 1 : -1;
        double fixedX = dragSignX >= 0 ? startBounds[0] : startBounds[2];
        double fixedY = dragSignY >= 0 ? startBounds[1] : startBounds[3];
        int minVisualWidth = CanvasRotationMath.isQuarterTurn(rotation) ? Math.max(1, minHeight) : Math.max(1, minWidth);
        int minVisualHeight = CanvasRotationMath.isQuarterTurn(rotation) ? Math.max(1, minWidth) : Math.max(1, minHeight);
        double targetVisualWidth = Math.max(minVisualWidth, dragSignX >= 0 ? mouseX - fixedX : fixedX - mouseX);
        double targetVisualHeight = Math.max(minVisualHeight, dragSignY >= 0 ? mouseY - fixedY : fixedY - mouseY);
        if (preserveAspect) {
            int startVisualWidth = Math.max(1, startBounds[2] - startBounds[0]);
            int startVisualHeight = Math.max(1, startBounds[3] - startBounds[1]);
            double scale = Math.max(targetVisualWidth / startVisualWidth, targetVisualHeight / startVisualHeight);
            targetVisualWidth = Math.max(minVisualWidth, startVisualWidth * scale);
            targetVisualHeight = Math.max(minVisualHeight, startVisualHeight * scale);
        }
        int visualWidth = CanvasGridMath.snapVisualSpanToGridSlot((int) Math.round(targetVisualWidth), grid, minVisualWidth);
        int visualHeight = CanvasGridMath.snapVisualSpanToGridSlot((int) Math.round(targetVisualHeight), grid, minVisualHeight);
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
}

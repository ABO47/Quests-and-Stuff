package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class CanvasClamp {
    private CanvasClamp() {
    }

    static CanvasPoint clampAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int slotLogicalW, int slotLogicalH) {
        float zoom = CanvasCoordinateMapper.zoom(state);
        int viewportLeft = (int) Math.floor((-state.canvas.canvasContentX - state.canvas.canvasOffsetX) / zoom);
        int viewportTop = (int) Math.floor((-state.canvas.canvasContentY - state.canvas.canvasOffsetY) / zoom);
        int viewportRight = (int) Math.ceil((state.canvas.canvasViewportW - state.canvas.canvasContentX - state.canvas.canvasOffsetX) / zoom);
        int viewportBottom = (int) Math.ceil((state.canvas.canvasViewportH - state.canvas.canvasContentY - state.canvas.canvasOffsetY) / zoom);
        if (state.canvas.gridCanvasLocked) {
            int maxX = Math.max(viewportLeft, viewportRight - Math.max(1, slotLogicalW));
            int maxY = Math.max(viewportTop, viewportBottom - Math.max(1, slotLogicalH));
            return new CanvasPoint(
                    Math.max(viewportLeft, Math.min(maxX, logicalX)),
                    Math.max(viewportTop, Math.min(maxY, logicalY))
            );
        }
        int halfW = Math.max(1, slotLogicalW) / 2;
        int halfH = Math.max(1, slotLogicalH) / 2;
        int centerX = Math.max(viewportLeft, Math.min(viewportRight, logicalX + halfW));
        int centerY = Math.max(viewportTop, Math.min(viewportBottom, logicalY + halfH));
        return new CanvasPoint(centerX - halfW, centerY - halfH);
    }

    static CanvasPoint clampRotatedAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        float zoom = CanvasCoordinateMapper.zoom(state);
        int viewportLeft = (int) Math.floor((-state.canvas.canvasContentX - state.canvas.canvasOffsetX) / zoom);
        int viewportTop = (int) Math.floor((-state.canvas.canvasContentY - state.canvas.canvasOffsetY) / zoom);
        int viewportRight = (int) Math.ceil((state.canvas.canvasViewportW - state.canvas.canvasContentX - state.canvas.canvasOffsetX) / zoom);
        int viewportBottom = (int) Math.ceil((state.canvas.canvasViewportH - state.canvas.canvasContentY - state.canvas.canvasOffsetY) / zoom);
        if (state.canvas.gridCanvasLocked) {
            int[] bounds = CanvasRotationMath.rotatedBoundsAtPivot(logicalX, logicalY, width, height, pivotX, pivotY, rotationDegrees);
            int boundsWidth = Math.max(1, bounds[2] - bounds[0]);
            int boundsHeight = Math.max(1, bounds[3] - bounds[1]);
            int vpW = Math.max(1, viewportRight - viewportLeft);
            int vpH = Math.max(1, viewportBottom - viewportTop);
            int targetLeft = boundsWidth > vpW
                    ? viewportLeft
                    : Math.max(viewportLeft, Math.min(Math.max(viewportLeft, viewportRight - boundsWidth), bounds[0]));
            int targetTop = boundsHeight > vpH
                    ? viewportTop
                    : Math.max(viewportTop, Math.min(Math.max(viewportTop, viewportBottom - boundsHeight), bounds[1]));
            return new CanvasPoint(logicalX + targetLeft - bounds[0], logicalY + targetTop - bounds[1]);
        }
        int pivotWorldX = Math.max(viewportLeft, Math.min(viewportRight, logicalX + pivotX));
        int pivotWorldY = Math.max(viewportTop, Math.min(viewportBottom, logicalY + pivotY));
        return new CanvasPoint(pivotWorldX - pivotX, pivotWorldY - pivotY);
    }
}

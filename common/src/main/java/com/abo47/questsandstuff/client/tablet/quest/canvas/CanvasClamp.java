package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class CanvasClamp {
    private CanvasClamp() {
    }

    static CanvasPoint clampAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int slotLogicalW, int slotLogicalH) {
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

    static CanvasPoint clampRotatedAnchorToCanvas(TabletUiState state, int logicalX, int logicalY, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        if (!state.gridCanvasLocked) {
            return new CanvasPoint(logicalX, logicalY);
        }
        int[] bounds = CanvasRotationMath.rotatedBoundsAtPivot(logicalX, logicalY, width, height, pivotX, pivotY, rotationDegrees);
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
}

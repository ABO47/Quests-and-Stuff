package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class CanvasCoordinateMapper {
    private CanvasCoordinateMapper() {
    }

    static int screenX(TabletUiState state, double logicalX) {
        return state.canvasContentX + state.canvasOffsetX + Math.round((float) (logicalX * zoom(state)));
    }

    static int screenY(TabletUiState state, double logicalY) {
        return state.canvasContentY + state.canvasOffsetY + Math.round((float) (logicalY * zoom(state)));
    }

    static int screenWidth(TabletUiState state, int logicalLeft, int logicalRight) {
        return Math.max(1, screenX(state, logicalRight) - screenX(state, logicalLeft));
    }

    static int screenHeight(TabletUiState state, int logicalTop, int logicalBottom) {
        return Math.max(1, screenY(state, logicalBottom) - screenY(state, logicalTop));
    }

    static int screenSpan(TabletUiState state, int logicalSize) {
        return Math.max(1, Math.round(Math.max(1, logicalSize) * zoom(state)));
    }

    static double screenToLogicalX(TabletUiState state, int screenX) {
        return (screenX - state.canvasContentX - state.canvasOffsetX) / zoom(state);
    }

    static double screenToLogicalY(TabletUiState state, int screenY) {
        return (screenY - state.canvasContentY - state.canvasOffsetY) / zoom(state);
    }

    static int screenToNearestLogicalX(TabletUiState state, int screenX) {
        return (int) Math.round(screenToLogicalX(state, screenX));
    }

    static int screenToNearestLogicalY(TabletUiState state, int screenY) {
        return (int) Math.round(screenToLogicalY(state, screenY));
    }

    static float zoom(TabletUiState state) {
        return CanvasRenderer.clampZoom(state.canvasZoom);
    }
}

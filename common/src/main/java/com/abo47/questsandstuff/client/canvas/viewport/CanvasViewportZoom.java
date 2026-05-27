package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasViewportZoom {
    private CanvasViewportZoom() {
    }

    public static void zoomAt(TabletUiState state, Runnable refresh, int localX, int localY, double wheelDelta) {
        CanvasCameraController.zoomAt(state, refresh, localX, localY, wheelDelta);
    }
}

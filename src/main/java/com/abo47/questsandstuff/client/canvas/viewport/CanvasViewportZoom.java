package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasViewportZoom {
    private CanvasViewportZoom() {
    }

    public static void zoomAt(TabletUiState state, Runnable refresh, int localX, int localY, double wheelDelta) {
        float oldZoom = CanvasRenderer.clampZoom(state.canvasZoom);
        double focusX = CanvasGeometry.screenToLogicalX(state, localX);
        double focusY = CanvasGeometry.screenToLogicalY(state, localY);
        float multiplier = wheelDelta > 0 ? 1.125f : 1.0f / 1.125f;
        float nextZoom = CanvasRenderer.clampZoom(oldZoom * multiplier);
        if (nextZoom == oldZoom) {
            return;
        }
        state.canvasZoom = nextZoom;
        state.canvasOffsetX = localX - state.canvasContentX - Math.round((float) (focusX * nextZoom));
        state.canvasOffsetY = localY - state.canvasContentY - Math.round((float) (focusY * nextZoom));
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas zoom value={} focus={},{}", nextZoom, focusX, focusY);
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }
}

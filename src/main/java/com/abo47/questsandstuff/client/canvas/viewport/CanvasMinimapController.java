package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasMinimapController {
    private CanvasMinimapController() {
    }

    public static boolean handleClick(TabletUiState state, int localX, int localY) {
        if (state.minimapW <= 0
                || localX < state.minimapX || localX > state.minimapX + state.minimapW
                || localY < state.minimapY || localY > state.minimapY + state.minimapH) {
            return false;
        }
        float xNorm = (float) (localX - state.minimapX) / (float) Math.max(1, state.minimapW);
        float yNorm = (float) (localY - state.minimapY) / (float) Math.max(1, state.minimapH);
        xNorm = Math.max(0.0f, Math.min(1.0f, xNorm));
        yNorm = Math.max(0.0f, Math.min(1.0f, yNorm));
        int worldX = state.minimapWorldMinX + Math.round(xNorm * state.minimapWorldWidth);
        int worldY = state.minimapWorldMinY + Math.round(yNorm * state.minimapWorldHeight);
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        state.canvasOffsetX = (state.canvasContentW / 2) - Math.round(worldX * zoom);
        state.canvasOffsetY = (state.canvasContentH / 2) - Math.round(worldY * zoom);
        return true;
    }
}

package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;

public final class CanvasMinimapController {
    private CanvasMinimapController() {
    }

    public static boolean handleClick(TabletUiState state, int localX, int localY) {
        if (isToggleHit(state, localX, localY) || (state.minimapCollapsed && isPanelHit(state, localX, localY))) {
            boolean wasCollapsed = state.minimapCollapsed;
            state.minimapCollapsed = !state.minimapCollapsed;
            state.draggingMinimap = false;
            state.minimapAnimationStartMs = System.currentTimeMillis();
            state.minimapAnimationFromCollapsed = wasCollapsed;
            persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] minimap {}", state.minimapCollapsed ? "collapsed" : "expanded");
            return true;
        }
        if (!isPanelHit(state, localX, localY)) {
            return false;
        }
        if (isMapHit(state, localX, localY)) {
            state.draggingMinimap = true;
            centerCanvasOnMinimapPoint(state, localX, localY);
        }
        return true;
    }

    public static boolean handleDrag(TabletUiState state, int localX, int localY) {
        if (!state.draggingMinimap) {
            return false;
        }
        centerCanvasOnMinimapPoint(state, localX, localY);
        return true;
    }

    public static boolean finishDrag(TabletUiState state) {
        if (!state.draggingMinimap) {
            return false;
        }
        state.draggingMinimap = false;
        return true;
    }

    public static boolean isPanelHit(TabletUiState state, int localX, int localY) {
        return CanvasMinimapGeometry.hit(localX, localY, state.minimapPanelX, state.minimapPanelY, state.minimapPanelW, state.minimapPanelH);
    }

    private static boolean isToggleHit(TabletUiState state, int localX, int localY) {
        return CanvasMinimapGeometry.hit(localX, localY, state.minimapToggleX, state.minimapToggleY, state.minimapToggleW, state.minimapToggleH);
    }

    private static boolean isMapHit(TabletUiState state, int localX, int localY) {
        return !state.minimapCollapsed && CanvasMinimapGeometry.hit(localX, localY, state.minimapX, state.minimapY, state.minimapW, state.minimapH);
    }

    private static void centerCanvasOnMinimapPoint(TabletUiState state, int localX, int localY) {
        int worldX = CanvasMinimapGeometry.mapWorldX(state, localX);
        int worldY = CanvasMinimapGeometry.mapWorldY(state, localY);
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        state.canvasOffsetX = (state.canvasContentW / 2) - Math.round(worldX * zoom);
        state.canvasOffsetY = (state.canvasContentH / 2) - Math.round(worldY * zoom);
    }
}

package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;

public final class CanvasMinimapController {
    public static final long ANIMATION_MS = 180L;

    private CanvasMinimapController() {
    }

    public static boolean handleClick(TabletUiState state, int localX, int localY) {
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            state.draggingMinimap = false;
            return false;
        }
        if (isToggleHit(state, localX, localY) || (state.minimapCollapsed && isPanelHit(state, localX, localY))) {
            boolean wasCollapsed = state.minimapCollapsed;
            state.minimapCollapsed = !state.minimapCollapsed;
            state.draggingMinimap = false;
            startAnimation(state, wasCollapsed);
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
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            state.draggingMinimap = false;
            return false;
        }
        if (!state.draggingMinimap) {
            return false;
        }
        centerCanvasOnMinimapPoint(state, localX, localY);
        return true;
    }

    public static boolean finishDrag(TabletUiState state) {
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            state.draggingMinimap = false;
            return false;
        }
        if (!state.draggingMinimap) {
            return false;
        }
        state.draggingMinimap = false;
        return true;
    }

    public static boolean isClosingAnimationRunning(TabletUiState state) {
        return state != null
                && QuestsAndStuffConfig.minimapEnabled()
                && QuestsAndStuffConfig.minimapAnimationsEnabled()
                && UiAnimationProgress.running(state.minimapAnimationStartMs, ANIMATION_MS)
                && state.minimapCollapsed
                && !state.minimapAnimationFromCollapsed;
    }

    public static boolean finishAnimationIfDone(TabletUiState state) {
        if (state == null || state.minimapAnimationStartMs <= 0L) {
            return false;
        }
        if (QuestsAndStuffConfig.minimapAnimationsEnabled()
                && UiAnimationProgress.running(state.minimapAnimationStartMs, ANIMATION_MS)) {
            return false;
        }
        state.minimapAnimationStartMs = 0L;
        return true;
    }

    public static boolean isPanelHit(TabletUiState state, int localX, int localY) {
        return QuestsAndStuffConfig.minimapEnabled()
                && CanvasMinimapGeometry.hit(localX, localY, state.minimapPanelX, state.minimapPanelY, state.minimapPanelW, state.minimapPanelH);
    }

    private static boolean isToggleHit(TabletUiState state, int localX, int localY) {
        return CanvasMinimapGeometry.hit(localX, localY, state.minimapToggleX, state.minimapToggleY, state.minimapToggleW, state.minimapToggleH);
    }

    private static boolean isMapHit(TabletUiState state, int localX, int localY) {
        return !state.minimapCollapsed && CanvasMinimapGeometry.hit(localX, localY, state.minimapX, state.minimapY, state.minimapW, state.minimapH);
    }

    private static void startAnimation(TabletUiState state, boolean wasCollapsed) {
        if (!QuestsAndStuffConfig.minimapAnimationsEnabled()) {
            state.minimapAnimationStartMs = 0L;
            state.minimapAnimationFromCollapsed = state.minimapCollapsed;
            return;
        }
        state.minimapAnimationStartMs = System.currentTimeMillis();
        state.minimapAnimationFromCollapsed = wasCollapsed;
    }

    private static void centerCanvasOnMinimapPoint(TabletUiState state, int localX, int localY) {
        int worldX = CanvasMinimapGeometry.mapWorldX(state, localX);
        int worldY = CanvasMinimapGeometry.mapWorldY(state, localY);
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        state.canvasOffsetX = (state.canvasContentW / 2) - Math.round(worldX * zoom);
        state.canvasOffsetY = (state.canvasContentH / 2) - Math.round(worldY * zoom);
    }
}

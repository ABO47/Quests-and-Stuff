package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;

public final class CanvasMinimapController {
    public static final long ANIMATION_MS = 180L;

    private CanvasMinimapController() {
    }

    public static boolean handleClick(TabletUiState state, int localX, int localY) {
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            state.canvas.draggingMinimap = false;
            return false;
        }
        if (isToggleHit(state, localX, localY) || (state.canvas.minimapCollapsed && isPanelHit(state, localX, localY))) {
            boolean wasCollapsed = state.canvas.minimapCollapsed;
            state.canvas.minimapCollapsed = !state.canvas.minimapCollapsed;
            state.canvas.draggingMinimap = false;
            startAnimation(state, wasCollapsed);
            persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] minimap {}", state.canvas.minimapCollapsed ? "collapsed" : "expanded");
            return true;
        }
        if (!isPanelHit(state, localX, localY)) {
            return false;
        }
        if (isMapHit(state, localX, localY)) {
            state.canvas.draggingMinimap = true;
            centerCanvasOnMinimapPoint(state, localX, localY);
        }
        return true;
    }

    public static boolean handleDrag(TabletUiState state, int localX, int localY) {
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            state.canvas.draggingMinimap = false;
            return false;
        }
        if (!state.canvas.draggingMinimap) {
            return false;
        }
        centerCanvasOnMinimapPoint(state, localX, localY);
        return true;
    }

    public static boolean finishDrag(TabletUiState state) {
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            state.canvas.draggingMinimap = false;
            return false;
        }
        if (!state.canvas.draggingMinimap) {
            return false;
        }
        state.canvas.draggingMinimap = false;
        CanvasCameraController.rememberCurrentGroup(state);
        persistUiState(state);
        return true;
    }

    public static boolean isClosingAnimationRunning(TabletUiState state) {
        return state != null
                && QuestsAndStuffConfig.minimapEnabled()
                && QuestsAndStuffConfig.minimapAnimationsEnabled()
                && UiAnimationProgress.running(state.canvas.minimapAnimationStartMs, ANIMATION_MS)
                && state.canvas.minimapCollapsed
                && !state.canvas.minimapAnimationFromCollapsed;
    }

    public static boolean finishAnimationIfDone(TabletUiState state) {
        if (state == null || state.canvas.minimapAnimationStartMs <= 0L) {
            return false;
        }
        if (QuestsAndStuffConfig.minimapAnimationsEnabled()
                && UiAnimationProgress.running(state.canvas.minimapAnimationStartMs, ANIMATION_MS)) {
            return false;
        }
        state.canvas.minimapAnimationStartMs = 0L;
        return true;
    }

    public static boolean isPanelHit(TabletUiState state, int localX, int localY) {
        return QuestsAndStuffConfig.minimapEnabled()
                && CanvasMinimapGeometry.hit(localX, localY, state.canvas.minimapPanelX, state.canvas.minimapPanelY, state.canvas.minimapPanelW, state.canvas.minimapPanelH);
    }

    private static boolean isToggleHit(TabletUiState state, int localX, int localY) {
        return CanvasMinimapGeometry.hit(localX, localY, state.canvas.minimapToggleX, state.canvas.minimapToggleY, state.canvas.minimapToggleW, state.canvas.minimapToggleH);
    }

    private static boolean isMapHit(TabletUiState state, int localX, int localY) {
        return !state.canvas.minimapCollapsed && CanvasMinimapGeometry.hit(localX, localY, state.canvas.minimapX, state.canvas.minimapY, state.canvas.minimapW, state.canvas.minimapH);
    }

    private static void startAnimation(TabletUiState state, boolean wasCollapsed) {
        if (!QuestsAndStuffConfig.minimapAnimationsEnabled()) {
            state.canvas.minimapAnimationStartMs = 0L;
            state.canvas.minimapAnimationFromCollapsed = state.canvas.minimapCollapsed;
            return;
        }
        state.canvas.minimapAnimationStartMs = System.currentTimeMillis();
        state.canvas.minimapAnimationFromCollapsed = wasCollapsed;
    }

    private static void centerCanvasOnMinimapPoint(TabletUiState state, int localX, int localY) {
        int worldX = CanvasMinimapGeometry.mapWorldX(state, localX);
        int worldY = CanvasMinimapGeometry.mapWorldY(state, localY);
        CanvasCameraController.centerOn(state, worldX, worldY, false);
    }
}

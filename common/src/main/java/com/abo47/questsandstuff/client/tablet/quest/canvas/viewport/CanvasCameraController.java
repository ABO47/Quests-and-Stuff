package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

import java.util.List;

public final class CanvasCameraController {
    private static final float[] ZOOM_STOPS = {0.5f, 0.67f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f};

    private CanvasCameraController() {
    }

    public static void beforeCanvasRebuild(TabletUiState state) {
        rememberCurrentGroup(state);
    }

    public static void afterCanvasLayout(TabletUiState state, String group) {
        String normalizedGroup = normalizeChapter(group);
        state.canvas.canvasZoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        state.canvas.canvasLivePanX = 0;
        state.canvas.canvasLivePanY = 0;
        boolean groupChanged = !normalizedGroup.equals(state.canvas.canvasCameraGroup);
        state.canvas.canvasCameraGroup = normalizedGroup;
        CanvasDoublePoint center = state.canvas.canvasCameraCentersByGroup.get(normalizedGroup);
        Float zoom = state.canvas.canvasCameraZoomsByGroup.get(normalizedGroup);
        if (center != null) {
            if (zoom != null) {
                state.canvas.canvasZoom = CanvasRenderer.clampZoom(zoom);
            }
            centerOnInternal(state, center.x(), center.y());
            return;
        }
        clampCameraOffset(state);
        rememberCurrentGroup(state);
        if (groupChanged) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas camera group={} new_view", normalizedGroup);
        }
    }

    public static void rememberCurrentGroup(TabletUiState state) {
        if (state == null || state.canvas.canvasContentW <= 0 || state.canvas.canvasContentH <= 0) {
            return;
        }
        String group = normalizeChapter(state.canvas.canvasCameraGroup);
        if (group.isBlank()) {
            return;
        }
        state.canvas.canvasCameraCentersByGroup.put(group, currentCenter(state));
        state.canvas.canvasCameraZoomsByGroup.put(group, CanvasRenderer.clampZoom(state.canvas.canvasZoom));
    }

    public static CanvasPoint previewPanDelta(TabletUiState state, int requestedLivePanX, int requestedLivePanY) {
        CanvasPoint clamped = clampedOffset(state, state.canvas.canvasOffsetX + requestedLivePanX, state.canvas.canvasOffsetY + requestedLivePanY);
        return new CanvasPoint(clamped.x - state.canvas.canvasOffsetX, clamped.y - state.canvas.canvasOffsetY);
    }

    public static void panByScreen(TabletUiState state, int dx, int dy, boolean persist) {
        setOffset(state, state.canvas.canvasOffsetX + dx, state.canvas.canvasOffsetY + dy, persist);
    }

    public static void setOffset(TabletUiState state, int offsetX, int offsetY, boolean persist) {
        CanvasPoint clamped = clampedOffset(state, offsetX, offsetY);
        state.canvas.canvasOffsetX = clamped.x;
        state.canvas.canvasOffsetY = clamped.y;
        finishCameraChange(state, persist);
    }

    public static CanvasPoint clampedOffset(TabletUiState state, int offsetX, int offsetY) {
        if (state == null) {
            return new CanvasPoint(offsetX, offsetY);
        }
        if (!state.root.canEdit && QuestsAndStuffConfig.readOnlyCanvasFocusEnabled()) {
            int minLogicalX = state.canvas.canvasNavigationMinX;
            int minLogicalY = state.canvas.canvasNavigationMinY;
            int maxLogicalX = minLogicalX + Math.max(1, state.canvas.canvasNavigationWidth);
            int maxLogicalY = minLogicalY + Math.max(1, state.canvas.canvasNavigationHeight);
            return clampedOffsetToWorldBounds(state, offsetX, offsetY, minLogicalX, minLogicalY, maxLogicalX, maxLogicalY);
        }
        if (!state.canvas.gridCanvasLocked) {
            return new CanvasPoint(offsetX, offsetY);
        }
        float zoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        int contentW = Math.max(1, state.canvas.canvasContentW);
        int contentH = Math.max(1, state.canvas.canvasContentH);
        int scaledW = Math.max(contentW, Math.round(contentW * zoom));
        int scaledH = Math.max(contentH, Math.round(contentH * zoom));
        int minX = Math.min(0, contentW - scaledW);
        int minY = Math.min(0, contentH - scaledH);
        return new CanvasPoint(
                Math.max(minX, Math.min(0, offsetX)),
                Math.max(minY, Math.min(0, offsetY))
        );
    }

    public static CanvasPoint clampedOffsetToWorldBounds(
            TabletUiState state,
            int offsetX,
            int offsetY,
            int minLogicalX,
            int minLogicalY,
            int maxLogicalX,
            int maxLogicalY
    ) {
        if (state == null) {
            return new CanvasPoint(offsetX, offsetY);
        }
        float zoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        int pad = 24;
        int contentW = Math.max(1, state.canvas.canvasContentW);
        int contentH = Math.max(1, state.canvas.canvasContentH);
        int boundsW = Math.max(1, maxLogicalX - minLogicalX);
        int boundsH = Math.max(1, maxLogicalY - minLogicalY);
        int scaledBoundsW = Math.round(boundsW * zoom);
        int scaledBoundsH = Math.round(boundsH * zoom);

        int clampedX;
        if (scaledBoundsW + pad * 2 <= contentW) {
            clampedX = Math.round((contentW - scaledBoundsW) / 2.0f - minLogicalX * zoom);
        } else {
            int minOffset = Math.round(contentW - pad - maxLogicalX * zoom);
            int maxOffset = Math.round(pad - minLogicalX * zoom);
            clampedX = Math.max(minOffset, Math.min(maxOffset, offsetX));
        }

        int clampedY;
        if (scaledBoundsH + pad * 2 <= contentH) {
            clampedY = Math.round((contentH - scaledBoundsH) / 2.0f - minLogicalY * zoom);
        } else {
            int minOffset = Math.round(contentH - pad - maxLogicalY * zoom);
            int maxOffset = Math.round(pad - minLogicalY * zoom);
            clampedY = Math.max(minOffset, Math.min(maxOffset, offsetY));
        }

        return new CanvasPoint(clampedX, clampedY);
    }

    public static void clampCameraOffset(TabletUiState state) {
        CanvasPoint clamped = clampedOffset(state, state.canvas.canvasOffsetX, state.canvas.canvasOffsetY);
        state.canvas.canvasOffsetX = clamped.x;
        state.canvas.canvasOffsetY = clamped.y;
    }

    public static void zoomAt(TabletUiState state, Runnable refresh, int localX, int localY, double wheelDelta) {
        if (state == null || wheelDelta == 0.0D) {
            return;
        }
        float oldZoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        double focusX = screenToLogicalX(state, localX, false);
        double focusY = screenToLogicalY(state, localY, false);
        float nextZoom = nextZoomStop(oldZoom, wheelDelta > 0.0D);
        if (Math.abs(nextZoom - oldZoom) < 0.0001f) {
            return;
        }
        state.canvas.canvasZoom = nextZoom;
        state.canvas.canvasOffsetX = localX - state.canvas.canvasContentX - Math.round((float) (focusX * nextZoom));
        state.canvas.canvasOffsetY = localY - state.canvas.canvasContentY - Math.round((float) (focusY * nextZoom));
        clampCameraOffset(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas camera zoom value={} focus={},{}", nextZoom, focusX, focusY);
        finishCameraChange(state, true);
        if (refresh != null) {
            refresh.run();
        }
    }

    public static void resetZoom(TabletUiState state, boolean persist) {
        CanvasDoublePoint center = currentCenter(state);
        state.canvas.canvasZoom = 1.0f;
        centerOnInternal(state, center.x(), center.y());
        finishCameraChange(state, persist);
    }

    public static boolean consumePendingQuestFocus(TabletUiState state, List<QuestCardLayout> cards, String group) {
        String questId = QuestIdentity.questId(state.canvas.pendingCameraQuestId);
        String pendingGroup = normalizeChapter(state.canvas.pendingCameraGroup);
        String selectedChapter = normalizeChapter(group);
        if (questId.isBlank() || (!pendingGroup.isBlank() && !pendingGroup.equals(selectedChapter))) {
            return false;
        }
        for (QuestCardLayout card : cards) {
            if (!questId.equals(card.questId())) {
                continue;
            }
            centerOn(state, card.logicalCenterX(), card.logicalCenterY(), true);
            state.canvas.pendingCameraQuestId = "";
            state.canvas.pendingCameraGroup = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas camera center quest={} group={}", questId, selectedChapter);
            return true;
        }
        return false;
    }

    public static void centerOn(TabletUiState state, double logicalX, double logicalY, boolean persist) {
        centerOnInternal(state, logicalX, logicalY);
        finishCameraChange(state, persist);
    }

    public static double screenToLogicalX(TabletUiState state, int screenX, boolean includeLivePan) {
        int livePan = includeLivePan ? state.canvas.canvasLivePanX : 0;
        return (screenX - state.canvas.canvasContentX - state.canvas.canvasOffsetX - livePan) / CanvasRenderer.clampZoom(state.canvas.canvasZoom);
    }

    public static double screenToLogicalY(TabletUiState state, int screenY, boolean includeLivePan) {
        int livePan = includeLivePan ? state.canvas.canvasLivePanY : 0;
        return (screenY - state.canvas.canvasContentY - state.canvas.canvasOffsetY - livePan) / CanvasRenderer.clampZoom(state.canvas.canvasZoom);
    }

    private static void centerOnInternal(TabletUiState state, double logicalX, double logicalY) {
        float zoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        state.canvas.canvasOffsetX = (state.canvas.canvasContentW / 2) - Math.round((float) (logicalX * zoom));
        state.canvas.canvasOffsetY = (state.canvas.canvasContentH / 2) - Math.round((float) (logicalY * zoom));
        clampCameraOffset(state);
    }

    private static CanvasDoublePoint currentCenter(TabletUiState state) {
        int screenX = state.canvas.canvasContentX + state.canvas.canvasContentW / 2;
        int screenY = state.canvas.canvasContentY + state.canvas.canvasContentH / 2;
        return new CanvasDoublePoint(screenToLogicalX(state, screenX, false), screenToLogicalY(state, screenY, false));
    }

    private static void finishCameraChange(TabletUiState state, boolean persist) {
        rememberCurrentGroup(state);
        if (persist) {
            TabletUiFactory.persistUiState(state);
        }
    }

    private static float nextZoomStop(float zoom, boolean zoomIn) {
        if (zoomIn) {
            for (float stop : ZOOM_STOPS) {
                if (stop > zoom + 0.001f) {
                    return stop;
                }
            }
            return ZOOM_STOPS[ZOOM_STOPS.length - 1];
        }
        for (int i = ZOOM_STOPS.length - 1; i >= 0; i--) {
            float stop = ZOOM_STOPS[i];
            if (stop < zoom - 0.001f) {
                return stop;
            }
        }
        return ZOOM_STOPS[0];
    }

    private static String normalizeChapter(String group) {
        return QuestIdentity.chapterName(group);
    }
}

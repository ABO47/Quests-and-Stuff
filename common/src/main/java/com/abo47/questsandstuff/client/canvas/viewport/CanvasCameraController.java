package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasDoublePoint;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Set;

public final class CanvasCameraController {
    private static final float[] ZOOM_STOPS = {0.5f, 0.67f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f};
    private static final int FIT_PADDING = 32;

    private CanvasCameraController() {
    }

    public static void beforeCanvasRebuild(TabletUiState state) {
        rememberCurrentGroup(state);
    }

    public static void afterCanvasLayout(TabletUiState state, String group) {
        String normalizedGroup = normalizeGroup(group);
        state.canvasZoom = CanvasRenderer.clampZoom(state.canvasZoom);
        state.canvasLivePanX = 0;
        state.canvasLivePanY = 0;
        boolean groupChanged = !normalizedGroup.equals(state.canvasCameraGroup);
        state.canvasCameraGroup = normalizedGroup;
        CanvasDoublePoint center = state.canvasCameraCentersByGroup.get(normalizedGroup);
        Float zoom = state.canvasCameraZoomsByGroup.get(normalizedGroup);
        if (center != null) {
            if (zoom != null) {
                state.canvasZoom = CanvasRenderer.clampZoom(zoom);
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
        if (state == null || state.canvasContentW <= 0 || state.canvasContentH <= 0) {
            return;
        }
        String group = normalizeGroup(state.canvasCameraGroup);
        if (group.isBlank()) {
            return;
        }
        state.canvasCameraCentersByGroup.put(group, currentCenter(state));
        state.canvasCameraZoomsByGroup.put(group, CanvasRenderer.clampZoom(state.canvasZoom));
    }

    public static CanvasPoint previewPanDelta(TabletUiState state, int requestedLivePanX, int requestedLivePanY) {
        CanvasPoint clamped = clampedOffset(state, state.canvasOffsetX + requestedLivePanX, state.canvasOffsetY + requestedLivePanY);
        return new CanvasPoint(clamped.x - state.canvasOffsetX, clamped.y - state.canvasOffsetY);
    }

    public static void panByScreen(TabletUiState state, int dx, int dy, boolean persist) {
        setOffset(state, state.canvasOffsetX + dx, state.canvasOffsetY + dy, persist);
    }

    public static void setOffset(TabletUiState state, int offsetX, int offsetY, boolean persist) {
        CanvasPoint clamped = clampedOffset(state, offsetX, offsetY);
        state.canvasOffsetX = clamped.x;
        state.canvasOffsetY = clamped.y;
        finishCameraChange(state, persist);
    }

    public static CanvasPoint clampedOffset(TabletUiState state, int offsetX, int offsetY) {
        if (state == null) {
            return new CanvasPoint(offsetX, offsetY);
        }
        if (!state.canEdit && QuestsAndStuffConfig.readOnlyCanvasFocusEnabled()) {
            int minLogicalX = state.canvasNavigationMinX;
            int minLogicalY = state.canvasNavigationMinY;
            int maxLogicalX = minLogicalX + Math.max(1, state.canvasNavigationWidth);
            int maxLogicalY = minLogicalY + Math.max(1, state.canvasNavigationHeight);
            return clampedOffsetToWorldBounds(state, offsetX, offsetY, minLogicalX, minLogicalY, maxLogicalX, maxLogicalY);
        }
        if (!state.gridCanvasLocked) {
            return new CanvasPoint(offsetX, offsetY);
        }
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        int contentW = Math.max(1, state.canvasContentW);
        int contentH = Math.max(1, state.canvasContentH);
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
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        int pad = 24;
        int contentW = Math.max(1, state.canvasContentW);
        int contentH = Math.max(1, state.canvasContentH);
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
        CanvasPoint clamped = clampedOffset(state, state.canvasOffsetX, state.canvasOffsetY);
        state.canvasOffsetX = clamped.x;
        state.canvasOffsetY = clamped.y;
    }

    public static void zoomAt(TabletUiState state, Runnable refresh, int localX, int localY, double wheelDelta) {
        if (state == null || wheelDelta == 0.0D) {
            return;
        }
        float oldZoom = CanvasRenderer.clampZoom(state.canvasZoom);
        double focusX = screenToLogicalX(state, localX, false);
        double focusY = screenToLogicalY(state, localY, false);
        float nextZoom = nextZoomStop(oldZoom, wheelDelta > 0.0D);
        if (Math.abs(nextZoom - oldZoom) < 0.0001f) {
            return;
        }
        state.canvasZoom = nextZoom;
        state.canvasOffsetX = localX - state.canvasContentX - Math.round((float) (focusX * nextZoom));
        state.canvasOffsetY = localY - state.canvasContentY - Math.round((float) (focusY * nextZoom));
        clampCameraOffset(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas camera zoom value={} focus={},{}", nextZoom, focusX, focusY);
        finishCameraChange(state, true);
        if (refresh != null) {
            refresh.run();
        }
    }

    public static void resetZoom(TabletUiState state, boolean persist) {
        CanvasDoublePoint center = currentCenter(state);
        state.canvasZoom = 1.0f;
        centerOnInternal(state, center.x(), center.y());
        finishCameraChange(state, persist);
    }

    public static boolean fitAll(TabletUiState state, List<QuestCardLayout> cards, boolean persist) {
        String group = TabletUiFactory.selectedGroupName(state);
        LogicalBounds bounds = new LogicalBounds();
        addCards(bounds, cards);
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            addImage(bounds, image);
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            addText(bounds, text);
        }
        return fitBounds(state, bounds, persist, "fit_all");
    }

    public static boolean fitSelection(TabletUiState state, List<QuestCardLayout> cards, boolean persist) {
        String group = TabletUiFactory.selectedGroupName(state);
        LogicalBounds bounds = new LogicalBounds();
        for (QuestCardLayout card : cards) {
            if (state.selectedQuestIds.contains(card.questId())) {
                addCard(bounds, card);
            }
        }
        Set<String> imageIds = CanvasRenderer.selectedCanvasImageIds(state);
        Set<String> textIds = CanvasRenderer.selectedCanvasTextIds(state);
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (imageIds.contains(image.id())) {
                addImage(bounds, image);
            }
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (textIds.contains(text.id())) {
                addText(bounds, text);
            }
        }
        return fitBounds(state, bounds, persist, "fit_selection");
    }

    public static boolean consumePendingQuestFocus(TabletUiState state, List<QuestCardLayout> cards, String group) {
        String questId = state.pendingCameraQuestId == null ? "" : state.pendingCameraQuestId.trim();
        String pendingGroup = normalizeGroup(state.pendingCameraGroup);
        String selectedGroup = normalizeGroup(group);
        if (questId.isBlank() || (!pendingGroup.isBlank() && !pendingGroup.equals(selectedGroup))) {
            return false;
        }
        for (QuestCardLayout card : cards) {
            if (!questId.equals(card.questId())) {
                continue;
            }
            centerOn(state, card.logicalCenterX(), card.logicalCenterY(), true);
            state.pendingCameraQuestId = "";
            state.pendingCameraGroup = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas camera center quest={} group={}", questId, selectedGroup);
            return true;
        }
        return false;
    }

    public static void centerOn(TabletUiState state, double logicalX, double logicalY, boolean persist) {
        centerOnInternal(state, logicalX, logicalY);
        finishCameraChange(state, persist);
    }

    public static double screenToLogicalX(TabletUiState state, int screenX, boolean includeLivePan) {
        int livePan = includeLivePan ? state.canvasLivePanX : 0;
        return (screenX - state.canvasContentX - state.canvasOffsetX - livePan) / CanvasRenderer.clampZoom(state.canvasZoom);
    }

    public static double screenToLogicalY(TabletUiState state, int screenY, boolean includeLivePan) {
        int livePan = includeLivePan ? state.canvasLivePanY : 0;
        return (screenY - state.canvasContentY - state.canvasOffsetY - livePan) / CanvasRenderer.clampZoom(state.canvasZoom);
    }

    private static void centerOnInternal(TabletUiState state, double logicalX, double logicalY) {
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        state.canvasOffsetX = (state.canvasContentW / 2) - Math.round((float) (logicalX * zoom));
        state.canvasOffsetY = (state.canvasContentH / 2) - Math.round((float) (logicalY * zoom));
        clampCameraOffset(state);
    }

    private static CanvasDoublePoint currentCenter(TabletUiState state) {
        int screenX = state.canvasContentX + state.canvasContentW / 2;
        int screenY = state.canvasContentY + state.canvasContentH / 2;
        return new CanvasDoublePoint(screenToLogicalX(state, screenX, false), screenToLogicalY(state, screenY, false));
    }

    private static void finishCameraChange(TabletUiState state, boolean persist) {
        rememberCurrentGroup(state);
        if (persist) {
            TabletUiFactory.persistUiState(state);
        }
    }

    private static boolean fitBounds(TabletUiState state, LogicalBounds bounds, boolean persist, String source) {
        if (bounds.empty() || state.canvasContentW <= 0 || state.canvasContentH <= 0) {
            return false;
        }
        double width = Math.max(1.0D, bounds.right - bounds.left);
        double height = Math.max(1.0D, bounds.bottom - bounds.top);
        int pad = Math.max(8, Math.min(FIT_PADDING, Math.min(state.canvasContentW, state.canvasContentH) / 6));
        float zoomX = (float) ((Math.max(1, state.canvasContentW - pad * 2)) / width);
        float zoomY = (float) ((Math.max(1, state.canvasContentH - pad * 2)) / height);
        state.canvasZoom = CanvasRenderer.clampZoom(Math.min(zoomX, zoomY));
        centerOnInternal(state, bounds.centerX(), bounds.centerY());
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas camera {} zoom={} bounds={}x{}", source, state.canvasZoom, width, height);
        finishCameraChange(state, persist);
        return true;
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

    private static void addCards(LogicalBounds bounds, List<QuestCardLayout> cards) {
        if (cards == null) {
            return;
        }
        for (QuestCardLayout card : cards) {
            addCard(bounds, card);
        }
    }

    private static void addCard(LogicalBounds bounds, QuestCardLayout card) {
        if (card == null) {
            return;
        }
        bounds.add(card.visualLogicalX(), card.visualLogicalY(), card.logicalRight(), card.logicalBottom());
    }

    private static void addImage(LogicalBounds bounds, CanvasImageLayer image) {
        if (image == null) {
            return;
        }
        int[] box = CanvasGeometry.rotatedBounds(image.x(), image.y(), image.w(), image.h(), image.rotation());
        bounds.add(box[0], box[1], box[2], box[3]);
    }

    private static void addText(LogicalBounds bounds, CanvasTextLayer text) {
        if (text == null) {
            return;
        }
        int[] box = CanvasGeometry.rotatedBounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
        bounds.add(box[0], box[1], box[2], box[3]);
    }

    private static String normalizeGroup(String group) {
        return group == null ? "" : group.trim();
    }

    private static final class LogicalBounds {
        private double left = Double.MAX_VALUE;
        private double top = Double.MAX_VALUE;
        private double right = -Double.MAX_VALUE;
        private double bottom = -Double.MAX_VALUE;

        private void add(double nextLeft, double nextTop, double nextRight, double nextBottom) {
            left = Math.min(left, Math.min(nextLeft, nextRight));
            top = Math.min(top, Math.min(nextTop, nextBottom));
            right = Math.max(right, Math.max(nextLeft, nextRight));
            bottom = Math.max(bottom, Math.max(nextTop, nextBottom));
        }

        private boolean empty() {
            return left == Double.MAX_VALUE || top == Double.MAX_VALUE || right == -Double.MAX_VALUE || bottom == -Double.MAX_VALUE;
        }

        private double centerX() {
            return (left + right) / 2.0D;
        }

        private double centerY() {
            return (top + bottom) / 2.0D;
        }
    }
}

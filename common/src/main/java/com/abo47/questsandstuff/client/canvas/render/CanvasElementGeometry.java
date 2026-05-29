package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasElementGeometry {
    private CanvasElementGeometry() {
    }

    public static Box screenBox(TabletUiState state, int x, int y, int width, int height) {
        return screenBoxAtPivot(state, x, y, width, height, width / 2, height / 2);
    }

    public static Box screenBox(TabletUiState state, int x, int y, int width, int height, int rotationDegrees) {
        return screenBoxAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees);
    }

    public static Box screenBoxAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        ScreenAxis xAxis = screenAxis(state, x, safeWidth, pivotX, true);
        ScreenAxis yAxis = screenAxis(state, y, safeHeight, pivotY, false);
        return new Box(
                xAxis.start() + xAxis.pivot(),
                yAxis.start() + yAxis.pivot(),
                xAxis.size(),
                yAxis.size(),
                -xAxis.pivot(),
                -yAxis.pivot(),
                xAxis.size() - xAxis.pivot(),
                yAxis.size() - yAxis.pivot()
        );
    }

    public static Box screenBoxAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        int rotation = normalize(rotationDegrees);
        if (rotation == 0 || (rotation != 90 && rotation != 180 && rotation != 270)) {
            return screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY);
        }
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safePivotX = clamp(pivotX, 0, safeWidth);
        int safePivotY = clamp(pivotY, 0, safeHeight);
        int[] bounds = logicalBoundsAtPivot(x, y, safeWidth, safeHeight, safePivotX, safePivotY, rotation);
        ScreenAxis boundsX = screenAxis(state, bounds[0], Math.max(1, bounds[2] - bounds[0]), 0, true);
        ScreenAxis boundsY = screenAxis(state, bounds[1], Math.max(1, bounds[3] - bounds[1]), 0, false);
        int screenW = rotation == 180 ? boundsX.size() : boundsY.size();
        int screenH = rotation == 180 ? boundsY.size() : boundsX.size();
        int screenPivotX = pivotForScreenSize(screenW, safePivotX, safeWidth);
        int screenPivotY = pivotForScreenSize(screenH, safePivotY, safeHeight);
        int startX;
        int startY;
        if (rotation == 180) {
            startX = boundsX.start() + screenW - 2 * screenPivotX;
            startY = boundsY.start() + screenH - 2 * screenPivotY;
        } else if (rotation == 90) {
            startX = boundsX.start() + screenH - screenPivotY - screenPivotX;
            startY = boundsY.start() + screenPivotX - screenPivotY;
        } else {
            startX = boundsX.start() + screenPivotY - screenPivotX;
            startY = boundsY.start() + screenW - screenPivotX - screenPivotY;
        }
        return new Box(
                startX + screenPivotX,
                startY + screenPivotY,
                screenW,
                screenH,
                -screenPivotX,
                -screenPivotY,
                screenW - screenPivotX,
                screenH - screenPivotY
        );
    }

    public static int[] screenBounds(TabletUiState state, int x, int y, int width, int height, int rotationDegrees) {
        return rotatedBounds(screenBox(state, x, y, width, height, rotationDegrees), rotationDegrees);
    }

    public static int[] screenBoundsAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return rotatedBounds(screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees), rotationDegrees);
    }

    public static int[] logicalBoundsAtPivot(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return CanvasGeometry.rotatedBoundsAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees);
    }

    public static int[] logicalBounds(int x, int y, int width, int height, int rotationDegrees) {
        return logicalBoundsAtPivot(x, y, width, height, defaultPivot(width), defaultPivot(height), rotationDegrees);
    }

    public static int defaultPivot(int size) {
        return Math.max(1, size) / 2;
    }

    public static double logicalPivot(int start, int size, int pivot) {
        return start + clamp(pivot, 0, Math.max(1, size));
    }

    public static double logicalPivotX(int x, int width, int pivotX) {
        return logicalPivot(x, width, pivotX);
    }

    public static double logicalPivotY(int y, int height, int pivotY) {
        return logicalPivot(y, height, pivotY);
    }

    public static LocalPoint toLocalPoint(Box box, int rotationDegrees, int hitX, int hitY) {
        double dx = hitX - box.centerX();
        double dy = hitY - box.centerY();
        double radians = Math.toRadians(-normalize(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new LocalPoint(dx * cos - dy * sin, dx * sin + dy * cos);
    }

    private static int[] rotatedBounds(Box box, int rotationDegrees) {
        return rotatedBounds(box.centerX(), box.centerY(), box.left(), box.top(), box.right(), box.bottom(), rotationDegrees);
    }

    private static int[] rotatedBounds(double centerX, double centerY, double left, double top, double right, double bottom, int rotationDegrees) {
        double radians = Math.toRadians(normalize(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {left, top},
                {right, top},
                {right, bottom},
                {left, bottom}
        };
        for (double[] corner : corners) {
            double sx = centerX + corner[0] * cos - corner[1] * sin;
            double sy = centerY + corner[0] * sin + corner[1] * cos;
            minX = Math.min(minX, sx);
            minY = Math.min(minY, sy);
            maxX = Math.max(maxX, sx);
            maxY = Math.max(maxY, sy);
        }
        return new int[]{(int) Math.floor(minX), (int) Math.floor(minY), (int) Math.ceil(maxX), (int) Math.ceil(maxY)};
    }

    private static int normalize(int rotationDegrees) {
        return ((rotationDegrees % 360) + 360) % 360;
    }

    private static ScreenAxis screenAxis(TabletUiState state, int start, int size, int pivot, boolean horizontal) {
        int safeSize = Math.max(1, size);
        int safePivot = clamp(pivot, 0, safeSize);
        int rawStart = screenCoordinate(state, start, horizontal);
        int rawSize = Math.max(1, screenCoordinate(state, start + safeSize, horizontal) - rawStart);
        int grid = CanvasGeometry.gridSize(state);
        int slotSize = CanvasGeometry.slotSpanForVisualSize(safeSize);
        int inset = CanvasGeometry.visualInsetForSlot(slotSize, safeSize);
        boolean gridSized = safeSize == CanvasGeometry.snapVisualSpanToGridSlot(safeSize, grid, 1);
        if (!gridSized) {
            int rawPivot = Math.max(0, Math.min(rawSize, screenCoordinate(state, start + safePivot, horizontal) - rawStart));
            return new ScreenAxis(rawStart, rawSize, rawPivot);
        }

        boolean gridAnchored = Math.floorMod(start - inset, grid) == 0;
        int slotScreenSize;
        int visualStart;
        if (gridAnchored) {
            int slotStart = start - inset;
            int slotScreenStart = screenCoordinate(state, slotStart, horizontal);
            slotScreenSize = Math.max(1, screenCoordinate(state, slotStart + slotSize, horizontal) - slotScreenStart);
            int visualScreenSize = visualScreenSize(state, safeSize, slotSize, slotScreenSize);
            visualStart = slotScreenStart + visualScreenInset(slotScreenSize, visualScreenSize);
            int visualPivot = Math.max(0, Math.min(visualScreenSize, (int) Math.round(visualScreenSize * (safePivot / (double) safeSize))));
            return new ScreenAxis(visualStart, visualScreenSize, visualPivot);
        }
        slotScreenSize = CanvasGeometry.screenSpan(state, slotSize);
        int visualScreenSize = visualScreenSize(state, safeSize, slotSize, slotScreenSize);
        visualStart = rawStart;
        int visualPivot = Math.max(0, Math.min(visualScreenSize, (int) Math.round(visualScreenSize * (safePivot / (double) safeSize))));
        return new ScreenAxis(visualStart, visualScreenSize, visualPivot);
    }

    private static int screenCoordinate(TabletUiState state, int value, boolean horizontal) {
        return horizontal ? CanvasGeometry.screenX(state, value) : CanvasGeometry.screenY(state, value);
    }

    private static int visualScreenSize(TabletUiState state, int visualLogicalSize, int slotLogicalSize, int slotScreenSize) {
        int preferred = Math.max(1, Math.round(visualLogicalSize * CanvasRenderer.clampZoom(state.canvasZoom)));
        int insideSlot = Math.max(1, slotScreenSize - 1);
        if (visualLogicalSize + 1 >= slotLogicalSize) {
            return insideSlot;
        }
        return Math.max(1, Math.min(preferred, insideSlot));
    }

    private static int visualScreenInset(int slotScreenSize, int visualScreenSize) {
        if (slotScreenSize <= visualScreenSize) {
            return 0;
        }
        int centered = (slotScreenSize - visualScreenSize) / 2;
        return Math.min(slotScreenSize - visualScreenSize, Math.max(1, centered));
    }

    private static int pivotForScreenSize(int screenSize, int pivot, int logicalSize) {
        int safeLogicalSize = Math.max(1, logicalSize);
        int safePivot = clamp(pivot, 0, safeLogicalSize);
        return Math.max(0, Math.min(screenSize, (int) Math.round(screenSize * (safePivot / (double) safeLogicalSize))));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Box(double centerX, double centerY, int width, int height, int left, int top, int right, int bottom) {
        public Box moved(double dx, double dy) {
            return new Box(centerX + dx, centerY + dy, width, height, left, top, right, bottom);
        }
    }

    public record LocalPoint(double x, double y) {
    }

    private record ScreenAxis(int start, int size, int pivot) {
    }
}

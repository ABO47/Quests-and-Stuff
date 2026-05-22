package com.abo47.questsandstuff.client.canvas.render;


import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class CanvasElementSelectionSlot {
    private static final int HANDLE_SIZE = 6;

    private CanvasElementSelectionSlot() {
    }

    public static int[] logicalBounds(TabletUiState state, int x, int y, int width, int height, int rotationDegrees) {
        if (!state.gridSnapLocked) {
            return new int[]{x, y, x + width, y + height};
        }
        int grid = CanvasGeometry.gridSize(state);
        int spanW = Math.max(grid, ceilToGrid(width, grid));
        int spanH = Math.max(grid, ceilToGrid(height, grid));
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        int left = (int) Math.round(centerX - spanW / 2.0);
        int top = (int) Math.round(centerY - spanH / 2.0);
        return new int[]{
                left,
                top,
                left + spanW,
                top + spanH
        };
    }

    public static int[] screenBounds(TabletUiState state, int x, int y, int width, int height, int rotationDegrees) {
        ScreenBox box = screenBox(state, x, y, width, height, rotationDegrees);
        LocalRect rect = localRect(box.width(), box.height());
        double radians = Math.toRadians(normalize(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double[][] corners = {
                {rect.left(), rect.top()},
                {rect.right(), rect.top()},
                {rect.right(), rect.bottom()},
                {rect.left(), rect.bottom()}
        };
        for (double[] corner : corners) {
            double sx = box.centerX() + corner[0] * cos - corner[1] * sin;
            double sy = box.centerY() + corner[0] * sin + corner[1] * cos;
            minX = Math.min(minX, sx);
            minY = Math.min(minY, sy);
            maxX = Math.max(maxX, sx);
            maxY = Math.max(maxY, sy);
        }
        return new int[]{(int) Math.floor(minX), (int) Math.floor(minY), (int) Math.ceil(maxX), (int) Math.ceil(maxY)};
    }

    public static void draw(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int rotationDegrees) {
        ScreenBox box = screenBox(state, x, y, width, height, rotationDegrees);
        drawBox(graphics, originX, originY, box, rotationDegrees);
    }

    public static void drawDragging(
            GuiGraphics graphics,
            TabletUiState state,
            int originX,
            int originY,
            int currentX,
            int currentY,
            int startX,
            int startY,
            int startWidth,
            int startHeight,
            int rotationDegrees
    ) {
        ScreenBox startBox = screenBox(state, startX, startY, startWidth, startHeight, rotationDegrees);
        double dx = CanvasGeometry.screenX(state, currentX) - CanvasGeometry.screenX(state, startX);
        double dy = CanvasGeometry.screenY(state, currentY) - CanvasGeometry.screenY(state, startY);
        drawBox(graphics, originX, originY, new ScreenBox(startBox.centerX() + dx, startBox.centerY() + dy, startBox.width(), startBox.height()), rotationDegrees);
    }

    private static void drawBox(GuiGraphics graphics, int originX, int originY, ScreenBox box, int rotationDegrees) {
        LocalRect rect = localRect(box.width(), box.height());
        if (rect.right() > rect.left() && rect.bottom() > rect.top()) {
            graphics.pose().pushPose();
            graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(normalize(rotationDegrees))));
            graphics.fill(rect.left(), rect.top(), rect.right(), rect.bottom(), withAlpha(ModColors.INTERACTIVE, 18));
            graphics.renderOutline(rect.left(), rect.top(), Math.max(1, rect.right() - rect.left()), Math.max(1, rect.bottom() - rect.top()), withAlpha(ModColors.SUCCESS, 185));
            drawHandles(graphics, rect);
            graphics.pose().popPose();
        }
    }

    public static boolean resizeHandleHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        ScreenBox box = screenBox(state, x, y, width, height, rotationDegrees);
        LocalRect rect = localRect(box.width(), box.height());
        LocalPoint point = toLocalPoint(box, rotationDegrees, hitX, hitY);
        return point.x() >= rect.right() - HANDLE_SIZE - 1 && point.x() <= rect.right() + 1
                && point.y() >= rect.bottom() - HANDLE_SIZE - 1 && point.y() <= rect.bottom() + 1;
    }

    public static boolean rotateHandleHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        ScreenBox box = screenBox(state, x, y, width, height, rotationDegrees);
        LocalRect rect = localRect(box.width(), box.height());
        LocalPoint point = toLocalPoint(box, rotationDegrees, hitX, hitY);
        return point.x() >= rect.right() - HANDLE_SIZE - 1 && point.x() <= rect.right() + 1
                && point.y() >= rect.top() - 1 && point.y() <= rect.top() + HANDLE_SIZE + 1;
    }

    private static void drawHandles(GuiGraphics graphics, LocalRect rect) {
        graphics.fill(rect.right() - HANDLE_SIZE, rect.bottom() - HANDLE_SIZE, rect.right(), rect.bottom(), withAlpha(ModColors.SURFACE_BASE, 220));
        graphics.renderOutline(rect.right() - HANDLE_SIZE, rect.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE, ModColors.SUCCESS);
        graphics.fill(rect.right() - HANDLE_SIZE, rect.top(), rect.right(), rect.top() + HANDLE_SIZE, withAlpha(ModColors.WARNING, 220));
        graphics.renderOutline(rect.right() - HANDLE_SIZE, rect.top(), HANDLE_SIZE, HANDLE_SIZE, ModColors.WARNING);
    }

    private static ScreenBox screenBox(TabletUiState state, int x, int y, int width, int height, int rotationDegrees) {
        int[] bounds = logicalBounds(state, x, y, width, height, rotationDegrees);
        int left = CanvasGeometry.screenX(state, bounds[0]);
        int top = CanvasGeometry.screenY(state, bounds[1]);
        int screenWidth = CanvasGeometry.screenSpan(state, bounds[2] - bounds[0]);
        int screenHeight = CanvasGeometry.screenSpan(state, bounds[3] - bounds[1]);
        ScreenOffset offset = phaseOffset(rotationDegrees);
        return new ScreenBox(left + screenWidth / 2.0 + offset.x(), top + screenHeight / 2.0 + offset.y(), screenWidth, screenHeight);
    }

    private static LocalRect localRect(int width, int height) {
        int halfW = width / 2;
        int halfH = height / 2;
        return new LocalRect(-halfW + 1, -halfH + 1, width - halfW, height - halfH);
    }

    private static LocalPoint toLocalPoint(ScreenBox box, int rotationDegrees, int hitX, int hitY) {
        double dx = hitX - box.centerX();
        double dy = hitY - box.centerY();
        double radians = Math.toRadians(-normalize(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new LocalPoint(dx * cos - dy * sin, dx * sin + dy * cos);
    }

    private static int normalize(int rotationDegrees) {
        return ((rotationDegrees % 360) + 360) % 360;
    }

    private static ScreenOffset phaseOffset(int rotationDegrees) {
        int quadrant = ((normalize(rotationDegrees) + 45) / 90) % 4;
        return switch (quadrant) {
            case 1 -> new ScreenOffset(1, 0);
            case 2 -> new ScreenOffset(1, 1);
            case 3 -> new ScreenOffset(0, 1);
            default -> new ScreenOffset(0, 0);
        };
    }

    private static int ceilToGrid(double value, int grid) {
        return (int) Math.ceil(value / Math.max(1, grid)) * Math.max(1, grid);
    }

    private record ScreenBox(double centerX, double centerY, int width, int height) {
    }

    private record ScreenOffset(int x, int y) {
    }

    private record LocalRect(int left, int top, int right, int bottom) {
    }

    private record LocalPoint(double x, double y) {
    }
}

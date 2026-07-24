package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import org.joml.Quaternionf;

import net.minecraft.client.gui.GuiGraphics;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public final class CanvasElementSelectionSlot {
    private static final int HANDLE_SIZE = GRID_6;

    private CanvasElementSelectionSlot() {
    }

    public static int[] screenBounds(TabletUiState state, int x, int y, int width, int height, int rotationDegrees) {
        return CanvasElementGeometry.screenBounds(state, x, y, width, height, rotationDegrees);
    }

    public static int[] screenBoundsAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return CanvasElementGeometry.screenBoundsAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
    }

    public static void draw(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int rotationDegrees) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, x, y, width, height, rotationDegrees);
        drawBox(graphics, originX, originY, box, rotationDegrees);
    }

    public static void drawAtPivot(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        drawBox(graphics, originX, originY, box, rotationDegrees);
    }

    public static void drawResizeOnlyAtPivot(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        if (box.right() > box.left() && box.bottom() > box.top()) {
            graphics.pose().pushPose();
            graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(normalize(rotationDegrees))));
            SurfaceFactory.fill(withAlpha(TabletColors.SELECTION, 18)).draw(graphics, 0, 0, box.left(), box.top(), box.right() - box.left(), box.bottom() - box.top());
            drawRectOutline(graphics, box.left(), box.top(), Math.max(1, box.right() - box.left()), Math.max(1, box.bottom() - box.top()), withAlpha(TabletColors.SELECTION, 185));
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
            drawRectOutline(graphics, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE, TabletColors.SELECTION);
            graphics.pose().popPose();
        }
    }

    private static void drawBox(GuiGraphics graphics, int originX, int originY, CanvasElementGeometry.Box box, int rotationDegrees) {
        if (box.right() > box.left() && box.bottom() > box.top()) {
            graphics.pose().pushPose();
            graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(normalize(rotationDegrees))));
            drawBoxFillAndOutline(graphics, box);
            drawHandles(graphics, box);
            graphics.pose().popPose();
        }
    }

    private static int normalize(int rotationDegrees) {
        return ((rotationDegrees % 360) + 360) % 360;
    }

    public static void drawScreenRectResizeOnly(GuiGraphics graphics, int originX, int originY, int left, int top, int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        CanvasElementGeometry.Box box = screenRectBox(left, top, w, h);
        if (box.right() > box.left() && box.bottom() > box.top()) {
            graphics.pose().pushPose();
            graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
            drawBoxFillAndOutline(graphics, box);
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
            drawRectOutline(graphics, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE, TabletColors.SELECTION);
            graphics.pose().popPose();
        }
    }

    public static boolean resizeHandleHitAtScreenRect(int left, int top, int width, int height, int hitX, int hitY) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        CanvasElementGeometry.Box box = screenRectBox(left, top, w, h);
        CanvasElementGeometry.LocalPoint point = CanvasElementGeometry.toLocalPoint(box, 0, hitX, hitY);
        return point.x() >= box.right() - HANDLE_SIZE - GRID_1 && point.x() <= box.right() + GRID_1
                && point.y() >= box.bottom() - HANDLE_SIZE - GRID_1 && point.y() <= box.bottom() + GRID_1;
    }

    private static CanvasElementGeometry.Box screenRectBox(int left, int top, int width, int height) {
        return new CanvasElementGeometry.Box(left, top, width, height, 0, 0, width, height);
    }

    private static void drawBoxFillAndOutline(GuiGraphics graphics, CanvasElementGeometry.Box box) {
        int w = Math.max(1, box.right() - box.left());
        int h = Math.max(1, box.bottom() - box.top());
        SurfaceFactory.fill(withAlpha(TabletColors.SELECTION, 18)).draw(graphics, 0, 0, box.left(), box.top(), w, h);
        drawRectOutline(graphics, box.left(), box.top(), w, h, withAlpha(TabletColors.SELECTION, 185));
    }

    public static void drawFillAndOutline(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int rotationDegrees) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, x, y, width, height, rotationDegrees);
        drawFillAndOutlineAt(graphics, originX, originY, box, rotationDegrees);
    }

    public static void drawFillAndOutlineAtPivot(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        drawFillAndOutlineAt(graphics, originX, originY, box, rotationDegrees);
    }

    private static void drawFillAndOutlineAt(GuiGraphics graphics, int originX, int originY, CanvasElementGeometry.Box box, int rotationDegrees) {
        if (box.right() > box.left() && box.bottom() > box.top()) {
            graphics.pose().pushPose();
            graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(normalize(rotationDegrees))));
            drawBoxFillAndOutline(graphics, box);
            graphics.pose().popPose();
        }
    }

    public static boolean resizeHandleHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return resizeHandleHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, hitX, hitY);
    }

    public static boolean resizeHandleHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int hitX, int hitY) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        CanvasElementGeometry.LocalPoint point = CanvasElementGeometry.toLocalPoint(box, rotationDegrees, hitX, hitY);
        return point.x() >= box.right() - HANDLE_SIZE - GRID_1 && point.x() <= box.right() + GRID_1
                && point.y() >= box.bottom() - HANDLE_SIZE - GRID_1 && point.y() <= box.bottom() + GRID_1;
    }

    public static boolean rotateHandleHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return rotateHandleHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, hitX, hitY);
    }

    public static boolean rotateHandleHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int hitX, int hitY) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        CanvasElementGeometry.LocalPoint point = CanvasElementGeometry.toLocalPoint(box, rotationDegrees, hitX, hitY);
        return point.x() >= box.right() - HANDLE_SIZE - GRID_1 && point.x() <= box.right() + GRID_1
                && point.y() >= box.top() - GRID_1 && point.y() <= box.top() + HANDLE_SIZE + GRID_1;
    }

    private static void drawHandles(GuiGraphics graphics, CanvasElementGeometry.Box box) {
        SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
        drawRectOutline(graphics, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE, TabletColors.SELECTION);
        SurfaceFactory.fill(withAlpha(TabletColors.WARNING, 220)).draw(graphics, 0, 0, box.right() - HANDLE_SIZE, box.top(), HANDLE_SIZE, HANDLE_SIZE);
        drawRectOutline(graphics, box.right() - HANDLE_SIZE, box.top(), HANDLE_SIZE, HANDLE_SIZE, TabletColors.WARNING);
    }

    public static void drawCombinedBounds(
            GuiGraphics graphics,
            int originX,
            int originY,
            int maxW,
            int maxH,
            int left,
            int top,
            int right,
            int bottom,
            boolean showRotate
    ) {
        int width = Math.max(1, right - left);
        int height = Math.max(1, bottom - top);
        drawClippedFill(graphics, originX, originY, maxW, maxH, left, top, width, height, withAlpha(TabletColors.SELECTION, 26));
        drawClippedOutline(graphics, originX, originY, maxW, maxH, left, top, width, height, withAlpha(TabletColors.SELECTION, 214));
        int resizeX = right - HANDLE_SIZE;
        int resizeY = bottom - HANDLE_SIZE;
        drawClippedFill(graphics, originX, originY, maxW, maxH, resizeX, resizeY, HANDLE_SIZE, HANDLE_SIZE, withAlpha(TabletColors.SURFACE_BASE, 230));
        drawClippedOutline(graphics, originX, originY, maxW, maxH, resizeX, resizeY, HANDLE_SIZE, HANDLE_SIZE, TabletColors.SELECTION);
        if (showRotate) {
            int rotateX = right - HANDLE_SIZE;
            int rotateY = top;
            drawClippedFill(graphics, originX, originY, maxW, maxH, rotateX, rotateY, HANDLE_SIZE, HANDLE_SIZE, withAlpha(TabletColors.WARNING, 210));
            drawClippedOutline(graphics, originX, originY, maxW, maxH, rotateX, rotateY, HANDLE_SIZE, HANDLE_SIZE, TabletColors.WARNING);
        }
    }

    public static void drawFillAndOutlineScreenRect(GuiGraphics graphics, int originX, int originY, int left, int top, int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        CanvasElementGeometry.Box box = screenRectBox(left, top, w, h);
        if (box.right() > box.left() && box.bottom() > box.top()) {
            graphics.pose().pushPose();
            graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
            drawBoxFillAndOutline(graphics, box);
            graphics.pose().popPose();
        }
    }

    public static void drawBoxSelection(
            GuiGraphics graphics,
            int originX,
            int originY,
            int maxW,
            int maxH,
            int startX,
            int startY,
            int currentX,
            int currentY
    ) {
        int minX = Math.min(startX, currentX);
        int minY = Math.min(startY, currentY);
        int boxW = Math.max(1, Math.abs(currentX - startX));
        int boxH = Math.max(1, Math.abs(currentY - startY));
        drawClippedFill(graphics, originX, originY, maxW, maxH, minX, minY, boxW, boxH, withAlpha(TabletColors.SELECTION, 48));
        drawClippedOutline(graphics, originX, originY, maxW, maxH, minX, minY, boxW, boxH, TabletColors.SELECTION);
    }

    private static void drawClippedFill(GuiGraphics graphics, int originX, int originY, int maxW, int maxH, int x, int y, int width, int height, int color) {
        if ((color >>> 24) == 0) {
            return;
        }
        int left = Math.max(0, x);
        int top = Math.max(0, y);
        int right = Math.min(maxW, x + Math.max(1, width));
        int bottom = Math.min(maxH, y + Math.max(1, height));
        if (right <= left || bottom <= top) {
            return;
        }
        SurfaceFactory.fill(color).draw(graphics, 0, 0, originX + left, originY + top, right - left, bottom - top);
    }

    private static void drawClippedOutline(GuiGraphics graphics, int originX, int originY, int maxW, int maxH, int x, int y, int width, int height, int color) {
        drawClippedLine(graphics, originX, originY, maxW, maxH, x, y, x + width, y + 1, color);
        drawClippedLine(graphics, originX, originY, maxW, maxH, x, y + height - 1, x + width, y + height, color);
        drawClippedLine(graphics, originX, originY, maxW, maxH, x, y, x + 1, y + height, color);
        drawClippedLine(graphics, originX, originY, maxW, maxH, x + width - 1, y, x + width, y + height, color);
    }

    private static void drawClippedLine(GuiGraphics graphics, int originX, int originY, int maxW, int maxH, int left, int top, int right, int bottom, int color) {
        int clippedLeft = Math.max(0, left);
        int clippedTop = Math.max(0, top);
        int clippedRight = Math.min(maxW, right);
        int clippedBottom = Math.min(maxH, bottom);
        if (clippedRight <= clippedLeft || clippedBottom <= clippedTop) {
            return;
        }
        SurfaceFactory.fill(color).draw(graphics, 0, 0, originX + clippedLeft, originY + clippedTop, clippedRight - clippedLeft, clippedBottom - clippedTop);
    }

}

package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class CanvasElementSelectionSlot {
    private static final int HANDLE_SIZE = 6;

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
            SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 18)).draw(graphics, 0, 0, box.left(), box.top(), box.right() - box.left(), box.bottom() - box.top());
            drawRectOutline(graphics, box.left(), box.top(), Math.max(1, box.right() - box.left()), Math.max(1, box.bottom() - box.top()), withAlpha(TabletColors.SUCCESS, 185));
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
            drawRectOutline(graphics, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE, TabletColors.SUCCESS);
            graphics.pose().popPose();
        }
    }

    private static void drawBox(GuiGraphics graphics, int originX, int originY, CanvasElementGeometry.Box box, int rotationDegrees) {
        if (box.right() > box.left() && box.bottom() > box.top()) {
            graphics.pose().pushPose();
            graphics.pose().translate(originX + box.centerX(), originY + box.centerY(), 0.0f);
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(normalize(rotationDegrees))));
            SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 18)).draw(graphics, 0, 0, box.left(), box.top(), box.right() - box.left(), box.bottom() - box.top());
            drawRectOutline(graphics, box.left(), box.top(), Math.max(1, box.right() - box.left()), Math.max(1, box.bottom() - box.top()), withAlpha(TabletColors.SUCCESS, 185));
            drawHandles(graphics, box);
            graphics.pose().popPose();
        }
    }

    public static boolean resizeHandleHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return resizeHandleHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, hitX, hitY);
    }

    public static boolean resizeHandleHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int hitX, int hitY) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        CanvasElementGeometry.LocalPoint point = CanvasElementGeometry.toLocalPoint(box, rotationDegrees, hitX, hitY);
        return point.x() >= box.right() - HANDLE_SIZE - 1 && point.x() <= box.right() + 1
                && point.y() >= box.bottom() - HANDLE_SIZE - 1 && point.y() <= box.bottom() + 1;
    }

    public static boolean rotateHandleHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return rotateHandleHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, hitX, hitY);
    }

    public static boolean rotateHandleHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int hitX, int hitY) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees);
        CanvasElementGeometry.LocalPoint point = CanvasElementGeometry.toLocalPoint(box, rotationDegrees, hitX, hitY);
        return point.x() >= box.right() - HANDLE_SIZE - 1 && point.x() <= box.right() + 1
                && point.y() >= box.top() - 1 && point.y() <= box.top() + HANDLE_SIZE + 1;
    }

    private static void drawHandles(GuiGraphics graphics, CanvasElementGeometry.Box box) {
        SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
        drawRectOutline(graphics, box.right() - HANDLE_SIZE, box.bottom() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE, TabletColors.SUCCESS);
        SurfaceFactory.fill(withAlpha(TabletColors.WARNING, 220)).draw(graphics, 0, 0, box.right() - HANDLE_SIZE, box.top(), HANDLE_SIZE, HANDLE_SIZE);
        drawRectOutline(graphics, box.right() - HANDLE_SIZE, box.top(), HANDLE_SIZE, HANDLE_SIZE, TabletColors.WARNING);
    }

    private static int normalize(int rotationDegrees) {
        return ((rotationDegrees % 360) + 360) % 360;
    }

}

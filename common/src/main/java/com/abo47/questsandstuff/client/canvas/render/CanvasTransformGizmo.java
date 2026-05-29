package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.model.CanvasModelPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.Rect;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class CanvasTransformGizmo {
    private static final int HANDLE = 7;
    private static final int HIT_PAD = 7;
    private static final int MOVE_HANDLE = 7;
    private static final int RING_STEP = 7;
    private static final int RING_THICKNESS = 3;
    private static final int AXIS_X_COLOR = ModColors.ERROR;
    private static final int AXIS_Y_COLOR = ModColors.SUCCESS;
    private static final int AXIS_Z_COLOR = ModColors.INTERACTIVE;
    public static final String AXIS_YAW = "yaw";
    public static final String AXIS_PITCH = "pitch";
    public static final String AXIS_ROLL = "roll";
    public static final String AXIS_MOVE_X = "move_x";
    public static final String AXIS_MOVE_Y = "move_y";
    public static final String AXIS_MOVE_FREE = "move_free";
    public static final String AXIS_RESIZE_NW = "resize_nw";
    public static final String AXIS_RESIZE_NE = "resize_ne";
    public static final String AXIS_RESIZE_SW = "resize_sw";
    public static final String AXIS_RESIZE_SE = "resize_se";

    private CanvasTransformGizmo() {
    }

    public static boolean supports(String asset) {
        return EntityPreviewRenderer.isEntityAsset(asset) || CanvasModelPreviewRenderer.isBlockModelAsset(asset);
    }

    public static CanvasTransformMode activeMode(TabletUiState state) {
        return CanvasTransformMode.fromId(state == null ? "" : state.transformGizmoMode);
    }

    public static void setMode(TabletUiState state, CanvasTransformMode mode) {
        if (state != null && mode != null) {
            state.transformGizmoMode = mode.id;
        }
    }

    public static boolean controlHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return modeAt(state, x, y, width, height, rotationDegrees, 0, 0, hitX, hitY) != null;
    }

    public static boolean controlHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return controlHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
    }

    public static boolean controlHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return modeAtPivot(state, x, y, width, height, pivotX, pivotY, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY) != null;
    }

    public static CanvasTransformMode modeAt(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return modeAt(state, x, y, width, height, rotationDegrees, 0, 0, hitX, hitY);
    }

    public static CanvasTransformMode modeAt(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return modeAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
    }

    public static CanvasTransformMode modeAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        Hit hit = hitAt(state, x, y, width, height, pivotX, pivotY, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
        return hit == null ? null : hit.mode();
    }

    public static String axisAt(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        return axisAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
    }

    public static String axisAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        Hit hit = hitAt(state, x, y, width, height, pivotX, pivotY, rotationDegrees, yawDegrees, pitchDegrees, hitX, hitY);
        return hit == null ? "" : hit.axis();
    }

    private static Hit hitAt(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees, int hitX, int hitY) {
        Geometry geometry = geometry(state, x, y, width, height, pivotX, pivotY);
        if (geometry.width() <= 0 || geometry.height() <= 0) {
            return null;
        }
        LocalPoint point = toLocalPoint(geometry, rotationDegrees, hitX, hitY);
        LocalPoint screenPoint = toLocalPoint(geometry, 0, hitX, hitY);
        int radius = rotationRadius(geometry.width(), geometry.height());
        CanvasTransformMode active = activeMode(state);
        if (active == CanvasTransformMode.ROTATE) {
            RotateAxes axes = rotateAxes(radius, rotationDegrees, yawDegrees, pitchDegrees);
            String handleAxis = rotateHandleAxis(screenPoint, axes);
            if (!handleAxis.isBlank()) {
                return new Hit(CanvasTransformMode.ROTATE, handleAxis);
            }
            String ringAxis = rotateRingAxis(screenPoint, axes);
            if (!ringAxis.isBlank()) {
                return new Hit(CanvasTransformMode.ROTATE, ringAxis);
            }
        }
        if (active == CanvasTransformMode.RESIZE) {
            String resizeAxis = resizeAxisAt(point, geometry.left(), geometry.top(), geometry.right(), geometry.bottom());
            if (!resizeAxis.isBlank()) {
                return new Hit(CanvasTransformMode.RESIZE, resizeAxis);
            }
        }
        int axis = moveAxis(geometry.width(), geometry.height());
        if (active == CanvasTransformMode.MOVE) {
            String moveAxis = moveAxisAt(point, axis);
            if (!moveAxis.isBlank()) {
                return new Hit(CanvasTransformMode.MOVE, moveAxis);
            }
        }
        return null;
    }

    public static boolean boundsHit(TabletUiState state, int x, int y, int width, int height, int rotationDegrees, int hitX, int hitY) {
        return boundsHitAtPivot(state, x, y, width, height, width / 2, height / 2, rotationDegrees, hitX, hitY);
    }

    public static boolean boundsHitAtPivot(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int hitX, int hitY) {
        Geometry geometry = geometry(state, x, y, width, height, pivotX, pivotY);
        LocalPoint point = toLocalPoint(geometry, rotationDegrees, hitX, hitY);
        return point.x() >= geometry.left() - HIT_PAD
                && point.x() <= geometry.right() + HIT_PAD
                && point.y() >= geometry.top() - HIT_PAD
                && point.y() <= geometry.bottom() + HIT_PAD;
    }

    public static void draw(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int rotationDegrees) {
        draw(graphics, state, originX, originY, x, y, width, height, rotationDegrees, 0, 0);
    }

    public static void draw(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        drawAtPivot(graphics, state, originX, originY, x, y, width, height, width / 2, height / 2, rotationDegrees, yawDegrees, pitchDegrees);
    }

    public static void drawAtPivot(GuiGraphics graphics, TabletUiState state, int originX, int originY, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        Geometry geometry = geometry(state, x, y, width, height, pivotX, pivotY);
        if (geometry.width() <= 0 || geometry.height() <= 0) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        CanvasTransformMode mode = activeMode(state);
        RotateAxes rotateAxes = null;
        int moveAxis = moveAxis(geometry.width(), geometry.height());
        graphics.pose().pushPose();
        graphics.pose().translate(originX + geometry.centerX(), originY + geometry.centerY(), 0.0F);
        if (mode == CanvasTransformMode.ROTATE) {
            rotateAxes = rotateAxes(rotationRadius(geometry.width(), geometry.height()), rotationDegrees, yawDegrees, pitchDegrees);
            drawRotateGizmo(graphics, rotateAxes);
        } else {
            graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0F, 0.0F, (float) Math.toRadians(normalize(rotationDegrees))));
            if (mode == CanvasTransformMode.RESIZE) {
                drawResizeGizmo(graphics, geometry.left(), geometry.top(), geometry.right(), geometry.bottom());
            } else {
                drawMoveGizmo(graphics, moveAxis);
            }
        }
        graphics.pose().popPose();
        if (mode == CanvasTransformMode.ROTATE && rotateAxes != null) {
            drawActiveRotateLabel(graphics, state, originX, originY, geometry, rotateAxes, rotationDegrees, yawDegrees, pitchDegrees);
        } else if (mode == CanvasTransformMode.RESIZE) {
            drawActiveResizeLabel(graphics, state, originX, originY, geometry, rotationDegrees, width, height);
        } else if (mode == CanvasTransformMode.MOVE) {
            drawActiveMoveLabel(graphics, state, originX, originY, geometry, rotationDegrees, moveAxis, x, y);
        }
        RenderSystem.depthMask(true);
    }

    private static void drawMoveGizmo(GuiGraphics graphics, int axis) {
        int xColor = AXIS_X_COLOR;
        int yColor = AXIS_Y_COLOR;
        line(graphics, -axis + MOVE_HANDLE / 2, 0, axis - MOVE_HANDLE / 2, 0, xColor, 220);
        line(graphics, 0, -axis + MOVE_HANDLE / 2, 0, axis - MOVE_HANDLE / 2, yColor, 220);
        drawBoxHandle(graphics, axis, 0, xColor);
        drawBoxHandle(graphics, -axis, 0, xColor);
        drawBoxHandle(graphics, 0, -axis, yColor);
        drawBoxHandle(graphics, 0, axis, yColor);
        drawTransparentBoxHandle(graphics, 0, 0, AXIS_Z_COLOR);
    }

    private static void drawResizeGizmo(GuiGraphics graphics, int boxLeft, int boxTop, int boxRight, int boxBottom) {
        int left = boxLeft;
        int top = boxTop;
        int right = boxRight;
        int bottom = boxBottom;
        graphics.fill(left, top, right, bottom, withAlpha(ModColors.INTERACTIVE, 18));
        graphics.renderOutline(left, top, Math.max(1, right - left), Math.max(1, bottom - top), withAlpha(ModColors.SUCCESS, 185));
        drawInsideHandle(graphics, left, top, ModColors.SUCCESS);
        drawInsideHandle(graphics, right - HANDLE, top, ModColors.SUCCESS);
        drawInsideHandle(graphics, left, bottom - HANDLE, ModColors.SUCCESS);
        drawInsideHandle(graphics, right - HANDLE, bottom - HANDLE, ModColors.SUCCESS);
    }

    private static void drawRotateGizmo(GuiGraphics graphics, RotateAxes axes) {
        drawSmoothCircleRing(graphics, axes.pitchRadius(), AXIS_X_COLOR, 175);
        drawSmoothCircleRing(graphics, axes.yawRadius(), AXIS_Y_COLOR, 175);
        drawSmoothCircleRing(graphics, axes.rollRadius(), AXIS_Z_COLOR, 185);
        drawBoxHandle(graphics, axes.pitchHandleX(), axes.pitchHandleY(), AXIS_X_COLOR);
        drawBoxHandle(graphics, axes.yawHandleX(), axes.yawHandleY(), AXIS_Y_COLOR);
        drawBoxHandle(graphics, axes.rollHandleX(), axes.rollHandleY(), AXIS_Z_COLOR);
        drawTransparentBoxHandle(graphics, 0, 0, AXIS_Z_COLOR);
    }

    private static void line(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color, int alpha) {
        int c = withAlpha(color, alpha);
        if (y1 == y2) {
            graphics.fill(Math.min(x1, x2), y1 - 1, Math.max(x1, x2) + 1, y1 + 2, c);
        } else if (x1 == x2) {
            graphics.fill(x1 - 1, Math.min(y1, y2), x1 + 2, Math.max(y1, y2) + 1, c);
        }
    }

    private static void drawSmoothCircleRing(GuiGraphics graphics, int radius, int color, int alpha) {
        int diameter = Math.max(1, radius * 2);
        float outer = Math.max(1.0F, radius);
        float inner = Math.max(0.0F, outer - RING_THICKNESS);
        Vector4f outerRadius = new Vector4f(outer, outer, outer, outer);
        Vector4f innerRadius = new Vector4f(inner, inner, inner, inner);
        DrawerHelper.drawFrameRoundBox(graphics, Rect.ofRelative(-radius, diameter, -radius, diameter), RING_THICKNESS, innerRadius, outerRadius, withAlpha(color, alpha));
    }

    private static void drawActiveRotateLabel(GuiGraphics graphics, TabletUiState state, int originX, int originY, Geometry geometry, RotateAxes axes, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        if (state == null || !isRotating(state)) {
            return;
        }
        String axis = activeAxis(state);
        if (axis.isBlank()) {
            return;
        }
        int x;
        int y;
        int color;
        int value;
        if (AXIS_PITCH.equals(axis)) {
            x = axes.pitchHandleX();
            y = axes.pitchHandleY();
            color = AXIS_X_COLOR;
            value = pitchDegrees;
        } else if (AXIS_YAW.equals(axis)) {
            x = axes.yawHandleX();
            y = axes.yawHandleY();
            color = AXIS_Y_COLOR;
            value = yawDegrees;
        } else {
            x = axes.rollHandleX();
            y = axes.rollHandleY();
            color = AXIS_Z_COLOR;
            value = rotationDegrees;
        }
        ScreenPoint point = screenPoint(originX, originY, geometry, 0, x, y);
        drawValueLabel(graphics, normalize(value) + "\u00B0", point.x() + HANDLE, point.y() - HANDLE, color);
    }

    private static void drawActiveMoveLabel(GuiGraphics graphics, TabletUiState state, int originX, int originY, Geometry geometry, int rotationDegrees, int axisLength, int x, int y) {
        if (state == null || !isMoving(state)) {
            return;
        }
        String axis = activeMoveAxis(state);
        int labelX = 0;
        int labelY = 0;
        int color = AXIS_Z_COLOR;
        String text = x + ", " + y;
        if (AXIS_MOVE_X.equals(axis)) {
            labelX = axisLength;
            color = AXIS_X_COLOR;
            text = String.valueOf(x);
        } else if (AXIS_MOVE_Y.equals(axis)) {
            labelY = -axisLength;
            color = AXIS_Y_COLOR;
            text = String.valueOf(y);
        }
        ScreenPoint point = screenPoint(originX, originY, geometry, rotationDegrees, labelX, labelY);
        drawValueLabel(graphics, text, point.x() + HANDLE, point.y() - HANDLE, color);
    }

    private static void drawActiveResizeLabel(GuiGraphics graphics, TabletUiState state, int originX, int originY, Geometry geometry, int rotationDegrees, int width, int height) {
        if (state == null || !isResizing(state)) {
            return;
        }
        LocalPoint point = resizeHandlePoint(activeResizeAxis(state), geometry.left(), geometry.top(), geometry.right(), geometry.bottom());
        ScreenPoint screenPoint = screenPoint(originX, originY, geometry, rotationDegrees, point.x(), point.y());
        drawValueLabel(graphics, width + " x " + height, screenPoint.x() + HANDLE, screenPoint.y() - HANDLE, AXIS_Y_COLOR);
    }

    private static void drawValueLabel(GuiGraphics graphics, String text, int x, int y, int color) {
        var font = Minecraft.getInstance().font;
        int width = font.width(text) + 6;
        int height = font.lineHeight + 4;
        graphics.fill(x - 3, y - 2, x - 3 + width, y - 2 + height, withAlpha(ModColors.SURFACE_BASE, 205));
        graphics.renderOutline(x - 3, y - 2, width, height, withAlpha(color, 210));
        graphics.drawString(font, text, x, y, ModColors.TEXT_PRIMARY, false);
    }

    private static boolean isRotating(TabletUiState state) {
        return state.rotatingCanvasImage || "rotate".equals(state.questDetailsTransformMode);
    }

    private static boolean isMoving(TabletUiState state) {
        return state.draggingCanvasImage || "move".equals(state.questDetailsTransformMode);
    }

    private static boolean isResizing(TabletUiState state) {
        return state.resizingCanvasImage || "resize".equals(state.questDetailsTransformMode);
    }

    private static String activeAxis(TabletUiState state) {
        if (state.rotatingCanvasImage && state.canvasImageTransformAxis != null && !state.canvasImageTransformAxis.isBlank()) {
            return state.canvasImageTransformAxis;
        }
        if ("rotate".equals(state.questDetailsTransformMode) && state.questDetailsTransformAxis != null && !state.questDetailsTransformAxis.isBlank()) {
            return state.questDetailsTransformAxis;
        }
        if (state.canvasImageTransformAxis != null && !state.canvasImageTransformAxis.isBlank()) {
            return state.canvasImageTransformAxis;
        }
        return state.questDetailsTransformAxis == null ? "" : state.questDetailsTransformAxis;
    }

    private static String activeMoveAxis(TabletUiState state) {
        if (state.draggingCanvasImage) {
            return state.canvasImageTransformAxis == null || state.canvasImageTransformAxis.isBlank() ? AXIS_MOVE_FREE : state.canvasImageTransformAxis;
        }
        if ("move".equals(state.questDetailsTransformMode)) {
            return state.questDetailsTransformAxis == null || state.questDetailsTransformAxis.isBlank() ? AXIS_MOVE_FREE : state.questDetailsTransformAxis;
        }
        return AXIS_MOVE_FREE;
    }

    private static String activeResizeAxis(TabletUiState state) {
        if (state.resizingCanvasImage && state.canvasImageTransformAxis != null && !state.canvasImageTransformAxis.isBlank()) {
            return state.canvasImageTransformAxis;
        }
        if ("resize".equals(state.questDetailsTransformMode) && state.questDetailsTransformAxis != null && !state.questDetailsTransformAxis.isBlank()) {
            return state.questDetailsTransformAxis;
        }
        return AXIS_RESIZE_SE;
    }

    private static void drawTransparentBoxHandle(GuiGraphics graphics, int centerX, int centerY, int color) {
        int half = HANDLE / 2;
        int left = centerX - half;
        int top = centerY - half;
        graphics.fill(left, top, left + HANDLE, top + HANDLE, withAlpha(ModColors.SURFACE_BASE, 72));
        graphics.renderOutline(left, top, HANDLE, HANDLE, color);
    }

    private static void drawInsideHandle(GuiGraphics graphics, int left, int top, int color) {
        graphics.fill(left, top, left + HANDLE, top + HANDLE, withAlpha(ModColors.SURFACE_BASE, 220));
        graphics.renderOutline(left, top, HANDLE, HANDLE, color);
    }

    private static void drawBoxHandle(GuiGraphics graphics, int centerX, int centerY, int color) {
        int half = HANDLE / 2;
        int left = centerX - half;
        int top = centerY - half;
        graphics.fill(left, top, left + HANDLE, top + HANDLE, withAlpha(ModColors.SURFACE_BASE, 220));
        graphics.renderOutline(left, top, HANDLE, HANDLE, color);
    }

    private static boolean near(LocalPoint point, int x, int y) {
        return Math.abs(point.x() - x) <= HIT_PAD && Math.abs(point.y() - y) <= HIT_PAD;
    }

    private static double circleDistance(LocalPoint point, int radius) {
        if (radius <= 0) {
            return Double.MAX_VALUE;
        }
        double distance = Math.sqrt(point.x() * point.x() + point.y() * point.y());
        return Math.abs(distance - radius);
    }

    private static String moveAxisAt(LocalPoint point, int axis) {
        if (near(point, 0, 0)) {
            return AXIS_MOVE_FREE;
        }
        if (near(point, axis, 0) || near(point, -axis, 0)) {
            return AXIS_MOVE_X;
        }
        if (near(point, 0, -axis) || near(point, 0, axis)) {
            return AXIS_MOVE_Y;
        }
        return "";
    }

    private static String resizeAxisAt(LocalPoint point, int boxLeft, int boxTop, int boxRight, int boxBottom) {
        int left = boxLeft;
        int top = boxTop;
        int right = boxRight;
        int bottom = boxBottom;
        int inset = HANDLE / 2;
        if (near(point, left + inset, top + inset)) {
            return AXIS_RESIZE_NW;
        }
        if (near(point, right - HANDLE + inset, top + inset)) {
            return AXIS_RESIZE_NE;
        }
        if (near(point, left + inset, bottom - HANDLE + inset)) {
            return AXIS_RESIZE_SW;
        }
        if (near(point, right - HANDLE + inset, bottom - HANDLE + inset)) {
            return AXIS_RESIZE_SE;
        }
        return "";
    }

    private static LocalPoint resizeHandlePoint(String axis, int boxLeft, int boxTop, int boxRight, int boxBottom) {
        int left = boxLeft;
        int top = boxTop;
        int right = boxRight;
        int bottom = boxBottom;
        int inset = HANDLE / 2;
        return switch (axis) {
            case AXIS_RESIZE_NW -> new LocalPoint(left + inset, top + inset);
            case AXIS_RESIZE_NE -> new LocalPoint(right - HANDLE + inset, top + inset);
            case AXIS_RESIZE_SW -> new LocalPoint(left + inset, bottom - HANDLE + inset);
            default -> new LocalPoint(right - HANDLE + inset, bottom - HANDLE + inset);
        };
    }

    public static int resizeCornerX(String axis) {
        return AXIS_RESIZE_NW.equals(axis) || AXIS_RESIZE_SW.equals(axis) ? -1 : 1;
    }

    public static int resizeCornerY(String axis) {
        return AXIS_RESIZE_NW.equals(axis) || AXIS_RESIZE_NE.equals(axis) ? -1 : 1;
    }

    private static String rotateHandleAxis(LocalPoint point, RotateAxes axes) {
        if (near(point, axes.yawHandleX(), axes.yawHandleY())) {
            return AXIS_YAW;
        }
        if (near(point, axes.pitchHandleX(), axes.pitchHandleY())) {
            return AXIS_PITCH;
        }
        if (near(point, axes.rollHandleX(), axes.rollHandleY())) {
            return AXIS_ROLL;
        }
        return "";
    }

    private static String rotateRingAxis(LocalPoint point, RotateAxes axes) {
        double yaw = circleDistance(point, axes.yawRadius());
        double pitch = circleDistance(point, axes.pitchRadius());
        double roll = circleDistance(point, axes.rollRadius());
        double best = Math.min(yaw, Math.min(pitch, roll));
        if (best > HIT_PAD + 2.0D) {
            return "";
        }
        if (best == yaw) {
            return AXIS_YAW;
        }
        if (best == pitch) {
            return AXIS_PITCH;
        }
        return AXIS_ROLL;
    }

    private static RotateAxes rotateAxes(int radius, int rotationDegrees, int yawDegrees, int pitchDegrees) {
        int pitchRadius = Math.max(12, radius - RING_STEP * 2);
        int yawRadius = Math.max(pitchRadius + RING_STEP, radius - RING_STEP);
        int rollRadius = Math.max(yawRadius + RING_STEP, radius);
        LocalPoint pitchHandle = pointOnCircle(pitchRadius, pitchDegrees - 90);
        LocalPoint yawHandle = pointOnCircle(yawRadius, yawDegrees - 90);
        LocalPoint rollHandle = pointOnCircle(rollRadius, rotationDegrees - 90);
        return new RotateAxes(
                yawRadius,
                (int) Math.round(yawHandle.x()),
                (int) Math.round(yawHandle.y()),
                pitchRadius,
                (int) Math.round(pitchHandle.x()),
                (int) Math.round(pitchHandle.y()),
                rollRadius,
                (int) Math.round(rollHandle.x()),
                (int) Math.round(rollHandle.y())
        );
    }

    private static LocalPoint pointOnCircle(int radius, int degrees) {
        double radians = Math.toRadians(normalize(degrees));
        return new LocalPoint(Math.cos(radians) * radius, Math.sin(radians) * radius);
    }

    private static int moveAxis(int width, int height) {
        return Math.max(18, Math.min(46, Math.min(width, height) / 2 + 10));
    }

    private static int rotationRadius(int width, int height) {
        return Math.max(24, Math.min(70, Math.max(width, height) / 2 + 12));
    }

    private static Geometry geometry(TabletUiState state, int x, int y, int width, int height, int pivotX, int pivotY) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, width, height, pivotX, pivotY);
        return new Geometry(box.centerX(), box.centerY(), box.width(), box.height(), box.left(), box.top(), box.right(), box.bottom());
    }

    private static ScreenPoint screenPoint(int originX, int originY, Geometry geometry, int rotationDegrees, double localX, double localY) {
        double radians = Math.toRadians(normalize(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int x = (int) Math.round(originX + geometry.centerX() + localX * cos - localY * sin);
        int y = (int) Math.round(originY + geometry.centerY() + localX * sin + localY * cos);
        return new ScreenPoint(x, y);
    }

    private static LocalPoint toLocalPoint(Geometry geometry, int rotationDegrees, int hitX, int hitY) {
        double dx = hitX - geometry.centerX();
        double dy = hitY - geometry.centerY();
        double radians = Math.toRadians(-normalize(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new LocalPoint(dx * cos - dy * sin, dx * sin + dy * cos);
    }

    private static int normalize(int rotationDegrees) {
        return ((rotationDegrees % 360) + 360) % 360;
    }

    private record Geometry(double centerX, double centerY, int width, int height, int left, int top, int right, int bottom) {
    }

    private record LocalPoint(double x, double y) {
    }

    private record ScreenPoint(int x, int y) {
    }

    private record Hit(CanvasTransformMode mode, String axis) {
    }

    private record RotateAxes(
            int yawRadius,
            int yawHandleX,
            int yawHandleY,
            int pitchRadius,
            int pitchHandleX,
            int pitchHandleY,
            int rollRadius,
            int rollHandleX,
            int rollHandleY
    ) {
    }
}

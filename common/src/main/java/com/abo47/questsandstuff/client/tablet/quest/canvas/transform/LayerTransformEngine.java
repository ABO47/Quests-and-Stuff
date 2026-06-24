package com.abo47.questsandstuff.client.tablet.quest.canvas.transform;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformAxisDelta;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;

public final class LayerTransformEngine {
    public static final int ANGLE_SNAP_DEGREES = 15;

    private LayerTransformEngine() {
    }

    public static CanvasPoint moveAnchor(MoveRequest request, AnchorClamp clamp) {
        CanvasPoint delta = dragDelta(request.deltaX(), request.deltaY(), request.snap());
        return clamp.clamp(request.rect().x() + delta.x, request.rect().y() + delta.y, request.rect());
    }

    public static CanvasPoint axisMoveAnchor(AxisMoveRequest request, AnchorClamp clamp) {
        CanvasPoint delta = axisDelta(request.deltaX(), request.deltaY(), request.rect().rotation(), request.axis(), request.snap());
        return clamp.clamp(request.rect().x() + delta.x, request.rect().y() + delta.y, request.rect());
    }

    public static CanvasPoint freeDelta(int dx, int dy, SnapSettings snap) {
        if (!snap.gridSnapLocked()) {
            return new CanvasPoint(dx, dy);
        }
        return new CanvasPoint(snapToGrid(dx, snap.gridSize()), snapToGrid(dy, snap.gridSize()));
    }

    public static CanvasPoint dragDelta(int dx, int dy, SnapSettings snap) {
        if (!snap.gridSnapLocked() && !snap.shiftDown()) {
            return new CanvasPoint(dx, dy);
        }
        return new CanvasPoint(snapToGrid(dx, snap.gridSize()), snapToGrid(dy, snap.gridSize()));
    }

    public static CanvasPoint axisDelta(int dx, int dy, int rotation, String axis, SnapSettings snap) {
        return CanvasTransformAxisDelta.project(dx, dy, rotation, axis, snap.gridSnapLocked(), snap.gridSize());
    }

    public static CanvasPoint modelMoveDelta(ModelMoveRequest request) {
        if (request.freeAxis() || request.snap().shiftDown()) {
            return freeDelta(request.deltaX(), request.deltaY(), request.snap());
        }
        return axisDelta(request.deltaX(), request.deltaY(), request.rotation(), request.axis(), request.snap());
    }

    public static int snapDelta(int delta, SnapSettings snap) {
        if (!snap.gridSnapLocked() && !snap.shiftDown()) {
            return delta;
        }
        return snapToGrid(delta, snap.gridSize());
    }

    public static int snapToGrid(int value, int gridSize) {
        int step = Math.max(1, gridSize);
        return Math.round((float) value / (float) step) * step;
    }

    public static int layerRotation(RotationRequest request) {
        int angle = request.startRotation() + rotationDelta(request.pointerX(), request.pointerY(), request.pivotX(), request.pivotY(), request.startAngle());
        return request.snap() ? snapAngle(angle) : angle;
    }

    public static int rotationDelta(double pointerX, double pointerY, double pivotX, double pivotY, double startAngle) {
        double currentAngle = Math.atan2(pointerY - pivotY, pointerX - pivotX);
        double deltaDegrees = Math.toDegrees(currentAngle - startAngle);
        while (deltaDegrees > 180.0D) {
            deltaDegrees -= 360.0D;
        }
        while (deltaDegrees < -180.0D) {
            deltaDegrees += 360.0D;
        }
        return (int) Math.round(deltaDegrees);
    }

    public static int snapAngle(int angle) {
        return Math.round(angle / (float) ANGLE_SNAP_DEGREES) * ANGLE_SNAP_DEGREES;
    }

    public static ModelRotation modelRotation(ModelRotationRequest request) {
        int delta = rotationDelta(request.pointerX(), request.pointerY(), request.pivotX(), request.pivotY(), request.startAngle());
        int yaw = request.start().yaw();
        int pitch = request.start().pitch();
        if (request.pitchAxis()) {
            pitch += delta;
        } else {
            yaw += delta;
        }
        return request.snap()
                ? new ModelRotation(snapAngle(yaw), snapAngle(pitch))
                : new ModelRotation(yaw, pitch);
    }

    public static CanvasGeometry.ResizedBox resizeFromCorner(ResizeRequest request) {
        return CanvasGeometry.resizeRotatedFromCornerAtPivot(
                request.pointerX(),
                request.pointerY(),
                request.rect().x(),
                request.rect().y(),
                request.rect().width(),
                request.rect().height(),
                request.rect().pivotX(),
                request.rect().pivotY(),
                request.rect().rotation(),
                request.minWidth(),
                request.minHeight(),
                request.snap().gridSize(),
                request.snap().gridSnapLocked() || request.snap().shiftDown(),
                request.preserveAspect(),
                request.cornerX(),
                request.cornerY()
        );
    }

    @FunctionalInterface
    public interface AnchorClamp {
        CanvasPoint clamp(int x, int y, LayerRect rect);
    }

    public record LayerRect(int x, int y, int width, int height, int pivotX, int pivotY, int rotation) {
    }

    public record SnapSettings(int gridSize, boolean gridSnapLocked, boolean shiftDown) {
    }

    public record MoveRequest(LayerRect rect, int deltaX, int deltaY, SnapSettings snap) {
    }

    public record AxisMoveRequest(LayerRect rect, int deltaX, int deltaY, String axis, SnapSettings snap) {
    }

    public record ModelMoveRequest(int deltaX, int deltaY, int rotation, String axis, boolean freeAxis, SnapSettings snap) {
    }

    public record RotationRequest(int startRotation, double pivotX, double pivotY, double startAngle, double pointerX, double pointerY, boolean snap) {
    }

    public record ModelRotation(int yaw, int pitch) {
    }

    public record ModelRotationRequest(ModelRotation start, boolean pitchAxis, double pivotX, double pivotY, double startAngle, double pointerX, double pointerY, boolean snap) {
    }

    public record ResizeRequest(LayerRect rect, double pointerX, double pointerY, int minWidth, int minHeight, SnapSettings snap, boolean preserveAspect, int cornerX, int cornerY) {
    }
}

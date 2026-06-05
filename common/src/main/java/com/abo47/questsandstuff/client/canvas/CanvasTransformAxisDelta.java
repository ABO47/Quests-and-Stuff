package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmo;

public final class CanvasTransformAxisDelta {
    private CanvasTransformAxisDelta() {
    }

    public static CanvasPoint project(int dx, int dy, int rotationDegrees, String axis, boolean snap, int gridSize) {
        String moveAxis = CanvasTransformGizmo.moveAxisOrFree(axis);
        if (CanvasTransformGizmo.AXIS_MOVE_FREE.equals(moveAxis)) {
            return new CanvasPoint(dx, dy);
        }
        double radians = Math.toRadians(normalize(rotationDegrees));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double axisX = CanvasTransformGizmo.AXIS_MOVE_X.equals(moveAxis) ? cos : -sin;
        double axisY = CanvasTransformGizmo.AXIS_MOVE_X.equals(moveAxis) ? sin : cos;
        double distance = dx * axisX + dy * axisY;
        if (snap) {
            int grid = Math.max(1, gridSize);
            distance = Math.round(distance / grid) * grid;
        }
        return new CanvasPoint((int) Math.round(distance * axisX), (int) Math.round(distance * axisY));
    }

    private static int normalize(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }
}

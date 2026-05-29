package com.abo47.questsandstuff.client.canvas.selection;

import com.abo47.questsandstuff.client.canvas.model.CanvasDoublePoint;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CanvasLayerGroupTransform {
    private CanvasLayerGroupTransform() {
    }

    public static Result rotate(
            CanvasLayerSelectionSnapshot snapshot,
            double pivotX,
            double pivotY,
            double radians,
            boolean snapToGrid,
            int grid,
            BoundsClamp clamp
    ) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int deltaDegrees = (int) Math.round(Math.toDegrees(radians));
        Map<String, CanvasImageLayer> images = new LinkedHashMap<>();
        Map<String, CanvasTextLayer> texts = new LinkedHashMap<>();
        for (CanvasImageLayer image : snapshot.images().values()) {
            CanvasDoublePoint rotatedPivot = rotatePoint(
                    CanvasElementGeometry.logicalPivotX(image.x(), image.w(), image.pivotX()),
                    CanvasElementGeometry.logicalPivotY(image.y(), image.h(), image.pivotY()),
                    pivotX,
                    pivotY,
                    cos,
                    sin
            );
            int targetX = (int) Math.round(rotatedPivot.x() - image.pivotX());
            int targetY = (int) Math.round(rotatedPivot.y() - image.pivotY());
            if (snapToGrid) {
                targetX = snapValue(targetX, grid);
                targetY = snapValue(targetY, grid);
            }
            CanvasPoint clamped = clamp.clamp(targetX, targetY, image.w(), image.h());
            images.put(image.id(), image.moveTo(clamped.x, clamped.y).rotateTo(image.rotation() + deltaDegrees));
        }
        for (CanvasTextLayer text : snapshot.texts().values()) {
            int textPivotX = CanvasElementGeometry.defaultPivot(text.w());
            int textPivotY = CanvasElementGeometry.defaultPivot(text.h());
            CanvasDoublePoint rotatedCenter = rotatePoint(
                    CanvasElementGeometry.logicalPivotX(text.x(), text.w(), textPivotX),
                    CanvasElementGeometry.logicalPivotY(text.y(), text.h(), textPivotY),
                    pivotX,
                    pivotY,
                    cos,
                    sin
            );
            int targetX = (int) Math.round(rotatedCenter.x() - textPivotX);
            int targetY = (int) Math.round(rotatedCenter.y() - textPivotY);
            if (snapToGrid) {
                targetX = snapValue(targetX, grid);
                targetY = snapValue(targetY, grid);
            }
            CanvasPoint clamped = clamp.clamp(targetX, targetY, text.w(), text.h());
            texts.put(text.id(), text.moveTo(clamped.x, clamped.y).rotateTo(text.rotation() + deltaDegrees));
        }
        return new Result(images, texts);
    }

    private static CanvasDoublePoint rotatePoint(double x, double y, double pivotX, double pivotY, double cos, double sin) {
        double relX = x - pivotX;
        double relY = y - pivotY;
        return new CanvasDoublePoint(
                pivotX + relX * cos - relY * sin,
                pivotY + relX * sin + relY * cos
        );
    }

    private static int snapValue(int value, int grid) {
        int safeGrid = Math.max(1, grid);
        return Math.round((float) value / (float) safeGrid) * safeGrid;
    }

    public interface BoundsClamp {
        CanvasPoint clamp(int x, int y, int width, int height);
    }

    public record Result(Map<String, CanvasImageLayer> images, Map<String, CanvasTextLayer> texts) {
    }
}

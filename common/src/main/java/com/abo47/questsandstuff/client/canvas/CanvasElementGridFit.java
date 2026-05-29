package com.abo47.questsandstuff.client.canvas;

import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasElementGridFit {
    private CanvasElementGridFit() {
    }

    public static CanvasImageLayer fittedImage(CanvasImageLayer image, int grid, BoundsClamp clamp) {
        int safeGrid = Math.max(1, grid);
        int rotation = CanvasGeometry.normalizeDegrees(image.rotation());
        if (rotation != 0) {
            CanvasGeometry.GridFittedBox fit = CanvasGeometry.fitRotatedElementToGridSlotAtPivot(
                    image.x(),
                    image.y(),
                    image.w(),
                    image.h(),
                    image.pivotX(),
                    image.pivotY(),
                    rotation,
                    safeGrid,
                    8,
                    8
            );
            CanvasImageLayer resized = image.withBounds(image.x(), image.y(), fit.width(), fit.height());
            CanvasPoint clamped = clamp(clamp, fit.x(), fit.y(), resized.w(), resized.h(), resized.pivotX(), resized.pivotY(), rotation);
            return resized.moveTo(clamped.x, clamped.y);
        }
        CanvasGeometry.GridVisualBox box = CanvasGeometry.fitVisualBoxToGridSlot(image.x(), image.y(), image.w(), image.h(), safeGrid, 8, 8);
        CanvasPoint clamped = clamp(clamp, box.x(), box.y(), box.width(), box.height(), image.pivotX(), image.pivotY(), rotation);
        return image.withBounds(clamped.x, clamped.y, box.width(), box.height());
    }

    public static CanvasTextLayer fittedText(CanvasTextLayer text, int grid, BoundsClamp clamp) {
        int safeGrid = Math.max(1, grid);
        int rotation = CanvasGeometry.normalizeDegrees(text.rotation());
        if (rotation == 0) {
            CanvasGeometry.GridVisualBox box = CanvasGeometry.fitVisualBoxToGridSlot(text.x(), text.y(), text.w(), text.h(), safeGrid, 24, 14);
            CanvasPoint clamped = clamp(clamp, box.x(), box.y(), box.width(), box.height(), box.width() / 2, box.height() / 2, rotation);
            return new CanvasTextLayer(text.id(), text.text(), clamped.x, clamped.y, box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
        }
        CanvasGeometry.GridFittedBox fit = CanvasGeometry.fitRotatedElementToGridSlotAtPivot(text.x(), text.y(), text.w(), text.h(), text.w() / 2, text.h() / 2, rotation, safeGrid, 24, 14);
        CanvasPoint clamped = clamp(clamp, fit.x(), fit.y(), fit.width(), fit.height(), fit.width() / 2, fit.height() / 2, rotation);
        return new CanvasTextLayer(text.id(), text.text(), clamped.x, clamped.y, fit.width(), fit.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
    }

    public static CanvasPoint nonNegativeClamp(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        int[] bounds = CanvasGeometry.rotatedBoundsAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees);
        return new CanvasPoint(x + Math.max(0, -bounds[0]), y + Math.max(0, -bounds[1]));
    }

    private static CanvasPoint clamp(BoundsClamp clamp, int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return clamp == null ? nonNegativeClamp(x, y, width, height, pivotX, pivotY, rotationDegrees) : clamp.clamp(x, y, width, height, pivotX, pivotY, rotationDegrees);
    }

    public interface BoundsClamp {
        CanvasPoint clamp(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees);
    }
}

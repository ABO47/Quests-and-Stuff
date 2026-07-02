package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CanvasSelectionResize {
    public static final int UNBOUNDED = Integer.MIN_VALUE;

    private CanvasSelectionResize() {
    }

    public static Result resizeBottomRight(CanvasLayerSelectionSnapshot snapshot, int pointerX, int pointerY, Constraints constraints) {
        return resizeFromCorner(snapshot, pointerX, pointerY, 1, 1, constraints);
    }

    public static Result resizeFromCorner(
            CanvasLayerSelectionSnapshot snapshot,
            int pointerX,
            int pointerY,
            int cornerX,
            int cornerY,
            Constraints constraints
    ) {
        Bounds start = Bounds.fromSnapshot(snapshot);
        if (!start.valid()) {
            return new Result(start, 1.0D, 1.0D, 1.0D, Map.of(), Map.of());
        }
        Bounds resized = resizeBounds(start, pointerX, pointerY, cornerX, cornerY, constraints);
        double scaleX = resized.width() / (double) Math.max(1, start.width());
        double scaleY = resized.height() / (double) Math.max(1, start.height());
        double uniformScale = Math.max(scaleX, scaleY);
        Map<String, CanvasImageLayer> images = resizeImages(snapshot.images(), start, resized, scaleX, scaleY);
        Map<String, CanvasTextLayer> texts = resizeTexts(snapshot.texts(), start, resized, scaleX, scaleY);
        return new Result(resized, scaleX, scaleY, uniformScale, immutableMap(images), immutableMap(texts));
    }

    private static Bounds resizeBounds(Bounds start, int pointerX, int pointerY, int cornerX, int cornerY, Constraints constraints) {
        int targetPointerX = constraints.snapToGrid() ? snapValue(pointerX, constraints.grid()) : pointerX;
        int targetPointerY = constraints.snapToGrid() ? snapValue(pointerY, constraints.grid()) : pointerY;
        int fixedX = cornerX >= 0 ? start.left() : start.right();
        int fixedY = cornerY >= 0 ? start.top() : start.bottom();
        double targetWidth = Math.max(constraints.minWidth(), cornerX >= 0 ? targetPointerX - fixedX : fixedX - targetPointerX);
        double targetHeight = Math.max(constraints.minHeight(), cornerY >= 0 ? targetPointerY - fixedY : fixedY - targetPointerY);
        double maxWidth = availableWidth(start, cornerX, constraints);
        double maxHeight = availableHeight(start, cornerY, constraints);

        if (constraints.preserveAspect()) {
            double minScale = Math.max(
                    constraints.minWidth() / (double) Math.max(1, start.width()),
                    constraints.minHeight() / (double) Math.max(1, start.height())
            );
            double maxScale = Math.min(
                    maxWidth / (double) Math.max(1, start.width()),
                    maxHeight / (double) Math.max(1, start.height())
            );
            if (maxScale < minScale) {
                minScale = maxScale;
            }
            double scale = Math.max(targetWidth / Math.max(1.0D, start.width()), targetHeight / Math.max(1.0D, start.height()));
            scale = Math.max(minScale, Math.min(maxScale, scale));
            targetWidth = Math.round(start.width() * scale);
            targetHeight = Math.round(start.height() * scale);
        } else {
            targetWidth = Math.min(maxWidth, Math.max(1.0D, targetWidth));
            targetHeight = Math.min(maxHeight, Math.max(1.0D, targetHeight));
        }

        int width = Math.max(1, (int) Math.round(targetWidth));
        int height = Math.max(1, (int) Math.round(targetHeight));
        int left = cornerX >= 0 ? fixedX : fixedX - width;
        int right = cornerX >= 0 ? fixedX + width : fixedX;
        int top = cornerY >= 0 ? fixedY : fixedY - height;
        int bottom = cornerY >= 0 ? fixedY + height : fixedY;
        return new Bounds(left, top, right, bottom);
    }

    private static double availableWidth(Bounds start, int cornerX, Constraints constraints) {
        if (cornerX >= 0) {
            return constraints.hasMaxRight() ? Math.max(1, constraints.maxRight() - start.left()) : Integer.MAX_VALUE / 4.0D;
        }
        return constraints.hasMinLeft() ? Math.max(1, start.right() - constraints.minLeft()) : Integer.MAX_VALUE / 4.0D;
    }

    private static double availableHeight(Bounds start, int cornerY, Constraints constraints) {
        if (cornerY >= 0) {
            return constraints.hasMaxBottom() ? Math.max(1, constraints.maxBottom() - start.top()) : Integer.MAX_VALUE / 4.0D;
        }
        return constraints.hasMinTop() ? Math.max(1, start.bottom() - constraints.minTop()) : Integer.MAX_VALUE / 4.0D;
    }

    private static Map<String, CanvasImageLayer> resizeImages(
            Map<String, CanvasImageLayer> source,
            Bounds start,
            Bounds target,
            double scaleX,
            double scaleY
    ) {
        Map<String, CanvasImageLayer> resized = new LinkedHashMap<>();
        for (CanvasImageLayer image : source.values()) {
            int[] bounds = CanvasElementGeometry.logicalBoundsAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            int targetLeft = (int) Math.round(target.left() + (bounds[0] - start.left()) * scaleX);
            int targetTop = (int) Math.round(target.top() + (bounds[1] - start.top()) * scaleY);
            int targetRight = (int) Math.round(target.left() + (bounds[2] - start.left()) * scaleX);
            int targetBottom = (int) Math.round(target.top() + (bounds[3] - start.top()) * scaleY);
            if (CanvasGeometry.isCardinalTurn(image.rotation())) {
                CanvasGeometry.ResizedBox box = CanvasGeometry.fitRotatedElementToVisualBoundsAtPivot(
                        targetLeft,
                        targetTop,
                        Math.max(1, targetRight - targetLeft),
                        Math.max(1, targetBottom - targetTop),
                        image.w(),
                        image.h(),
                        image.pivotX(),
                        image.pivotY(),
                        image.rotation(),
                        8,
                        8
                );
                resized.put(image.id(), image.withBounds(box.x(), box.y(), box.width(), box.height()));
                continue;
            }
            int targetW = Math.max(8, (int) Math.round(image.w() * scaleX));
            int targetH = Math.max(8, (int) Math.round(image.h() * scaleY));
            double centerX = target.left() + (image.x() + image.w() / 2.0D - start.left()) * scaleX;
            double centerY = target.top() + (image.y() + image.h() / 2.0D - start.top()) * scaleY;
            int targetX = (int) Math.round(centerX - targetW / 2.0D);
            int targetY = (int) Math.round(centerY - targetH / 2.0D);
            resized.put(image.id(), image.withBounds(targetX, targetY, targetW, targetH));
        }
        return resized;
    }

    private static Map<String, CanvasTextLayer> resizeTexts(
            Map<String, CanvasTextLayer> source,
            Bounds start,
            Bounds target,
            double scaleX,
            double scaleY
    ) {
        Map<String, CanvasTextLayer> resized = new LinkedHashMap<>();
        for (CanvasTextLayer text : source.values()) {
            int textPivotX = CanvasElementGeometry.defaultPivot(text.w());
            int textPivotY = CanvasElementGeometry.defaultPivot(text.h());
            int[] bounds = CanvasElementGeometry.logicalBoundsAtPivot(text.x(), text.y(), text.w(), text.h(), textPivotX, textPivotY, text.rotation());
            int targetLeft = (int) Math.round(target.left() + (bounds[0] - start.left()) * scaleX);
            int targetTop = (int) Math.round(target.top() + (bounds[1] - start.top()) * scaleY);
            int targetRight = (int) Math.round(target.left() + (bounds[2] - start.left()) * scaleX);
            int targetBottom = (int) Math.round(target.top() + (bounds[3] - start.top()) * scaleY);
            if (CanvasGeometry.isCardinalTurn(text.rotation())) {
                CanvasGeometry.ResizedBox box = CanvasGeometry.fitRotatedElementToVisualBoundsAtPivot(
                        targetLeft,
                        targetTop,
                        Math.max(1, targetRight - targetLeft),
                        Math.max(1, targetBottom - targetTop),
                        text.w(),
                        text.h(),
                        textPivotX,
                        textPivotY,
                        text.rotation(),
                        24,
                        14
                );
                resized.put(text.id(), text.moveTo(box.x(), box.y()).resizeTo(box.width(), box.height()));
                continue;
            }
            int targetW = Math.max(24, (int) Math.round(text.w() * scaleX));
            int targetH = Math.max(14, (int) Math.round(text.h() * scaleY));
            double centerX = target.left() + (text.x() + text.w() / 2.0D - start.left()) * scaleX;
            double centerY = target.top() + (text.y() + text.h() / 2.0D - start.top()) * scaleY;
            int targetX = (int) Math.round(centerX - targetW / 2.0D);
            int targetY = (int) Math.round(centerY - targetH / 2.0D);
            resized.put(text.id(), text.moveTo(targetX, targetY).resizeTo(targetW, targetH));
        }
        return resized;
    }

    private static int snapValue(int value, int grid) {
        int safeGrid = Math.max(1, grid);
        return Math.round((float) value / (float) safeGrid) * safeGrid;
    }

    private static <T> Map<String, T> immutableMap(Map<String, T> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    public record Constraints(
            int minWidth,
            int minHeight,
            int grid,
            boolean snapToGrid,
            boolean preserveAspect,
            int minLeft,
            int minTop,
            int maxRight,
            int maxBottom
    ) {
        public Constraints {
            minWidth = Math.max(1, minWidth);
            minHeight = Math.max(1, minHeight);
            grid = Math.max(1, grid);
        }

        public boolean hasMinLeft() {
            return minLeft != UNBOUNDED;
        }

        public boolean hasMinTop() {
            return minTop != UNBOUNDED;
        }

        public boolean hasMaxRight() {
            return maxRight != UNBOUNDED;
        }

        public boolean hasMaxBottom() {
            return maxBottom != UNBOUNDED;
        }
    }

    public record Bounds(int left, int top, int right, int bottom) {
        static Bounds fromSnapshot(CanvasLayerSelectionSnapshot snapshot) {
            return new Bounds(snapshot.left(), snapshot.top(), snapshot.right(), snapshot.bottom());
        }

        public boolean valid() {
            return right > left && bottom > top;
        }

        public int width() {
            return Math.max(0, right - left);
        }

        public int height() {
            return Math.max(0, bottom - top);
        }
    }

    public record Result(
            Bounds bounds,
            double scaleX,
            double scaleY,
            double uniformScale,
            Map<String, CanvasImageLayer> images,
            Map<String, CanvasTextLayer> texts
    ) {
    }
}

package com.abo47.questsandstuff.client.canvas.selection;

import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record CanvasLayerSelectionSnapshot(
        int left,
        int top,
        int right,
        int bottom,
        Map<String, CanvasImageLayer> images,
        Map<String, CanvasTextLayer> texts
) {
    public boolean hasBounds() {
        return right > left && bottom > top;
    }

    public static CanvasLayerSelectionSnapshot capture(
            Set<String> imageIds,
            Set<String> textIds,
            Iterable<CanvasImageLayer> images,
            Iterable<CanvasTextLayer> texts
    ) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        Map<String, CanvasImageLayer> selectedImages = new LinkedHashMap<>();
        if (imageIds != null && images != null) {
            for (CanvasImageLayer image : images) {
                if (image == null || !imageIds.contains(image.id())) {
                    continue;
                }
                selectedImages.put(image.id(), image);
                int[] bounds = CanvasElementGeometry.logicalBoundsAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
                minX = Math.min(minX, bounds[0]);
                minY = Math.min(minY, bounds[1]);
                maxX = Math.max(maxX, bounds[2]);
                maxY = Math.max(maxY, bounds[3]);
            }
        }

        Map<String, CanvasTextLayer> selectedTexts = new LinkedHashMap<>();
        if (textIds != null && texts != null) {
            for (CanvasTextLayer text : texts) {
                if (text == null || !textIds.contains(text.id())) {
                    continue;
                }
                selectedTexts.put(text.id(), text);
                int[] bounds = CanvasElementGeometry.logicalBounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
                minX = Math.min(minX, bounds[0]);
                minY = Math.min(minY, bounds[1]);
                maxX = Math.max(maxX, bounds[2]);
                maxY = Math.max(maxY, bounds[3]);
            }
        }

        if (minX == Integer.MAX_VALUE) {
            return new CanvasLayerSelectionSnapshot(0, 0, 0, 0, immutableMap(selectedImages), immutableMap(selectedTexts));
        }
        return new CanvasLayerSelectionSnapshot(minX, minY, maxX, maxY, immutableMap(selectedImages), immutableMap(selectedTexts));
    }

    private static <T> Map<String, T> immutableMap(Map<String, T> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}

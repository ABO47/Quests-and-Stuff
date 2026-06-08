package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasGroupResizeTransformTest {
    @Test
    void bottomRightResizeKeepsOppositeCornerAndUsesIndependentScale() {
        CanvasImageLayer image = new CanvasImageLayer("image", "item:minecraft:stick", 10, 10, 20, 10, 0);
        CanvasTextLayer text = new CanvasTextLayer("text", "Label", 50, 30, 30, 20, 0, "left", "normal", 0xFFFFFF);
        CanvasLayerSelectionSnapshot snapshot = new CanvasLayerSelectionSnapshot(
                10,
                10,
                80,
                50,
                Map.of(image.id(), image),
                Map.of(text.id(), text)
        );

        CanvasGroupResizeTransform.Result result = CanvasGroupResizeTransform.resizeBottomRight(
                snapshot,
                150,
                70,
                constraints(false, false, CanvasGroupResizeTransform.UNBOUNDED, CanvasGroupResizeTransform.UNBOUNDED)
        );

        assertEquals(new CanvasGroupResizeTransform.Bounds(10, 10, 150, 70), result.bounds());
        assertEquals(2.0D, result.scaleX(), 0.0001D);
        assertEquals(1.5D, result.scaleY(), 0.0001D);
        assertEquals(new CanvasImageLayer("image", "item:minecraft:stick", 10, 10, 40, 15, 0, 0, 60, 0, 20, 7), result.images().get("image"));
        assertEquals(new CanvasTextLayer("text", "Label", 90, 40, 60, 30, 0, "left", "normal", 0xFFFFFF), result.texts().get("text"));
    }

    @Test
    void shiftAspectResizeUsesOneScaleForTheWholeGroup() {
        CanvasLayerSelectionSnapshot snapshot = new CanvasLayerSelectionSnapshot(0, 0, 100, 50, Map.of(), Map.of());

        CanvasGroupResizeTransform.Result result = CanvasGroupResizeTransform.resizeBottomRight(
                snapshot,
                200,
                60,
                constraints(false, true, CanvasGroupResizeTransform.UNBOUNDED, CanvasGroupResizeTransform.UNBOUNDED)
        );

        assertEquals(new CanvasGroupResizeTransform.Bounds(0, 0, 200, 100), result.bounds());
        assertEquals(2.0D, result.scaleX(), 0.0001D);
        assertEquals(2.0D, result.scaleY(), 0.0001D);
    }

    @Test
    void clampsTheGroupBoxBeforeTransformingChildren() {
        CanvasLayerSelectionSnapshot snapshot = new CanvasLayerSelectionSnapshot(0, 0, 100, 50, Map.of(), Map.of());

        CanvasGroupResizeTransform.Result result = CanvasGroupResizeTransform.resizeBottomRight(
                snapshot,
                300,
                300,
                constraints(false, false, 150, 80)
        );

        assertEquals(new CanvasGroupResizeTransform.Bounds(0, 0, 150, 80), result.bounds());
        assertEquals(1.5D, result.scaleX(), 0.0001D);
        assertEquals(1.6D, result.scaleY(), 0.0001D);
    }

    @Test
    void snapsTheDraggedGroupHandleInsteadOfEachChild() {
        CanvasLayerSelectionSnapshot snapshot = new CanvasLayerSelectionSnapshot(3, 5, 103, 55, Map.of(), Map.of());

        CanvasGroupResizeTransform.Result result = CanvasGroupResizeTransform.resizeBottomRight(
                snapshot,
                157,
                91,
                constraints(true, false, CanvasGroupResizeTransform.UNBOUNDED, CanvasGroupResizeTransform.UNBOUNDED)
        );

        assertEquals(new CanvasGroupResizeTransform.Bounds(3, 5, 160, 96), result.bounds());
    }

    @Test
    void descriptionLikeImageAndTextResizeThroughSharedSelectionSnapshot() {
        CanvasImageLayer modelImage = new CanvasImageLayer("model", "block:minecraft:oak_planks", 20, 30, 40, 40, 0, 45, 0, 30);
        CanvasTextLayer text = new CanvasTextLayer("text", "Description", 70, 50, 60, 30, 0, "left", "normal", 0xFFFFFF);
        CanvasLayerSelectionSnapshot snapshot = new CanvasLayerSelectionSnapshot(
                20,
                30,
                130,
                80,
                Map.of(modelImage.id(), modelImage),
                Map.of(text.id(), text)
        );

        CanvasGroupResizeTransform.Result result = CanvasGroupResizeTransform.resizeBottomRight(
                snapshot,
                185,
                105,
                constraints(false, false, CanvasGroupResizeTransform.UNBOUNDED, CanvasGroupResizeTransform.UNBOUNDED)
        );

        assertEquals(new CanvasGroupResizeTransform.Bounds(20, 30, 185, 105), result.bounds());
        assertEquals(1.5D, result.scaleX(), 0.0001D);
        assertEquals(1.5D, result.scaleY(), 0.0001D);
        assertEquals(new CanvasImageLayer("model", "block:minecraft:oak_planks", 20, 30, 60, 60, 0, 45, 0, 30, 30, 30), result.images().get("model"));
        assertEquals(new CanvasTextLayer("text", "Description", 95, 60, 90, 45, 0, "left", "normal", 0xFFFFFF), result.texts().get("text"));
    }

    private static CanvasGroupResizeTransform.Constraints constraints(boolean snap, boolean preserveAspect, int maxRight, int maxBottom) {
        return new CanvasGroupResizeTransform.Constraints(
                4,
                4,
                16,
                snap,
                preserveAspect,
                0,
                0,
                maxRight,
                maxBottom
        );
    }
}

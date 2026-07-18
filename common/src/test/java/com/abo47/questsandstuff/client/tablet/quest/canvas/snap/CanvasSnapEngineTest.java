package com.abo47.questsandstuff.client.tablet.quest.canvas.snap;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasSnapEngineTest {
    @Test
    void objectSnapWinsBeforeCenterFallbackPerAxis() {
        CanvasSnapEngine.SnapResult result = CanvasSnapEngine.snap(new CanvasSnapEngine.SnapContext(
                new CanvasSnapEngine.Bounds(45, 45, 65, 65),
                List.of(new CanvasSnapEngine.Bounds(70, 80, 90, 100)),
                new CanvasSnapEngine.SnapSettings(true, true, true, 50, 50, 10)
        ));

        assertEquals(5, result.offsetX());
        assertEquals(5, result.offsetY());
        assertTrue(result.guideXVisible());
        assertTrue(result.guideYVisible());
        assertEquals(70.0D, result.guideX(), 0.0001D);
        assertEquals(50.0D, result.guideY(), 0.0001D);
    }

    @Test
    void sameSnapContextProducesSameGuidesForBothCanvasSurfaceFactory() {
        CanvasSnapEngine.SnapContext mainCanvas = context();
        CanvasSnapEngine.SnapContext descriptionCanvas = context();

        assertEquals(CanvasSnapEngine.snap(mainCanvas), CanvasSnapEngine.snap(descriptionCanvas));
    }

    @Test
    void disabledContextReturnsNoGuidesOrOffsets() {
        CanvasSnapEngine.SnapResult result = CanvasSnapEngine.snap(new CanvasSnapEngine.SnapContext(
                new CanvasSnapEngine.Bounds(45, 45, 65, 65),
                List.of(new CanvasSnapEngine.Bounds(70, 80, 90, 100)),
                new CanvasSnapEngine.SnapSettings(false, false, false, 50, 50, 10)
        ));

        assertFalse(result.hasOffset());
        assertFalse(result.guideXVisible());
        assertFalse(result.guideYVisible());
    }

    private static CanvasSnapEngine.SnapContext context() {
        return new CanvasSnapEngine.SnapContext(
                new CanvasSnapEngine.Bounds(45, 45, 65, 65),
                List.of(new CanvasSnapEngine.Bounds(69, 45, 89, 65)),
                new CanvasSnapEngine.SnapSettings(true, true, true, 100, 100, 5)
        );
    }
}

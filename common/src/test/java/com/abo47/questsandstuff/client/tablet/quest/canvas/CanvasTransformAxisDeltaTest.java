package com.abo47.questsandstuff.client.tablet.quest.canvas;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasTransformAxisDeltaTest {
    @Test
    void moveXUsesHorizontalDeltaWhenUnrotated() {
        CanvasPoint delta = CanvasTransformAxisDelta.project(24, 16, 0, CanvasTransformGizmo.AXIS_MOVE_X, false, 16);

        assertPoint(24, 0, delta);
    }

    @Test
    void moveXUsesVerticalDeltaAfterQuarterTurn() {
        CanvasPoint delta = CanvasTransformAxisDelta.project(0, 24, 90, CanvasTransformGizmo.AXIS_MOVE_X, false, 16);

        assertPoint(0, 24, delta);
    }

    @Test
    void moveYUsesHorizontalDeltaAfterQuarterTurn() {
        CanvasPoint delta = CanvasTransformAxisDelta.project(24, 0, 90, CanvasTransformGizmo.AXIS_MOVE_Y, false, 16);

        assertPoint(24, 0, delta);
    }

    @Test
    void rotatedAxisDistanceSnapsBeforeProjection() {
        CanvasPoint delta = CanvasTransformAxisDelta.project(0, 22, 90, CanvasTransformGizmo.AXIS_MOVE_X, true, 16);

        assertPoint(0, 16, delta);
    }

    private static void assertPoint(int x, int y, CanvasPoint point) {
        assertAll(
                () -> assertEquals(x, point.x),
                () -> assertEquals(y, point.y)
        );
    }
}

package com.abo47.questsandstuff.client.tablet.quest.canvas.transform;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LayerTransformEngineTest {
    @Test
    void normalDragSnapsOnShiftButModelFreeDragDoesNot() {
        LayerTransformEngine.SnapSettings shiftOnly = new LayerTransformEngine.SnapSettings(16, false, true);
        LayerTransformEngine.SnapSettings lockedGrid = new LayerTransformEngine.SnapSettings(16, true, false);

        assertPoint(16, 16, LayerTransformEngine.dragDelta(10, 22, shiftOnly));
        assertPoint(10, 22, LayerTransformEngine.freeDelta(10, 22, shiftOnly));
        assertPoint(16, 16, LayerTransformEngine.freeDelta(10, 22, lockedGrid));
        assertPoint(10, 22, LayerTransformEngine.modelMoveDelta(new LayerTransformEngine.ModelMoveRequest(
                10,
                22,
                0,
                CanvasTransformGizmo.AXIS_MOVE_X,
                false,
                shiftOnly
        )));
    }

    @Test
    void layerRotationWrapsAndSnapsToSharedAngleStep() {
        assertEquals(90, LayerTransformEngine.rotationDelta(0.0D, 1.0D, 0.0D, 0.0D, 0.0D));
        assertEquals(20, LayerTransformEngine.rotationDelta(
                Math.cos(Math.toRadians(-170.0D)),
                Math.sin(Math.toRadians(-170.0D)),
                0.0D,
                0.0D,
                Math.toRadians(170.0D)
        ));
        assertEquals(15, LayerTransformEngine.snapAngle(22));
        assertEquals(30, LayerTransformEngine.snapAngle(23));
        assertEquals(105, LayerTransformEngine.layerRotation(new LayerTransformEngine.RotationRequest(
                10,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                true
        )));
    }

    @Test
    void modelRotationSharesYawPitchAndSnapRules() {
        LayerTransformEngine.ModelRotation yawRotation = LayerTransformEngine.modelRotation(new LayerTransformEngine.ModelRotationRequest(
                new LayerTransformEngine.ModelRotation(12, 7),
                false,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                true
        ));
        LayerTransformEngine.ModelRotation pitchRotation = LayerTransformEngine.modelRotation(new LayerTransformEngine.ModelRotationRequest(
                new LayerTransformEngine.ModelRotation(12, 7),
                true,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                1.0D,
                false
        ));

        assertAll(
                () -> assertEquals(new LayerTransformEngine.ModelRotation(105, 0), yawRotation),
                () -> assertEquals(new LayerTransformEngine.ModelRotation(12, 97), pitchRotation)
        );
    }

    @Test
    void resizeFromCornerUsesLayerRectAndSnapSettings() {
        CanvasGeometry.ResizedBox resized = LayerTransformEngine.resizeFromCorner(new LayerTransformEngine.ResizeRequest(
                new LayerTransformEngine.LayerRect(10, 20, 40, 20, 20, 10, 0),
                58.0D,
                48.0D,
                8,
                8,
                new LayerTransformEngine.SnapSettings(16, false, false),
                false,
                1,
                1
        ));

        assertEquals(new CanvasGeometry.ResizedBox(10, 20, 48, 28), resized);
    }

    @Test
    void moveAnchorAppliesClampAfterSharedDragMath() {
        CanvasPoint moved = LayerTransformEngine.moveAnchor(
                new LayerTransformEngine.MoveRequest(
                        new LayerTransformEngine.LayerRect(10, 20, 40, 20, 20, 10, 0),
                        11,
                        22,
                        new LayerTransformEngine.SnapSettings(16, false, true)
                ),
                (x, y, rect) -> new CanvasPoint(Math.min(24, x), Math.min(32, y))
        );

        assertPoint(24, 32, moved);
    }

    private static void assertPoint(int x, int y, CanvasPoint point) {
        assertAll(
                () -> assertEquals(x, point.x),
                () -> assertEquals(y, point.y)
        );
    }
}

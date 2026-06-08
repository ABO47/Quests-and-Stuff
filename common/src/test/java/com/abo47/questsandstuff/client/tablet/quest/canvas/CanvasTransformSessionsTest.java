package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasDoublePoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasTransformSessionsTest {
    @Test
    void mainCanvasSessionClearsFlagsPreviewsAndStartSnapshotsWithoutClearingSelection() {
        TabletUiState state = new TabletUiState();
        seedMainCanvasTransform(state);
        state.selectedCanvasImageId = "image/a";
        state.selectedCanvasImageIds.add("image/a");

        CanvasTransformSessions.clearMainCanvasSession(state);

        assertFalse(state.draggingSelection);
        assertFalse(state.resizingSelection);
        assertFalse(state.rotatingSelection);
        assertFalse(state.draggingCanvasImage);
        assertFalse(state.resizingCanvasImage);
        assertFalse(state.rotatingCanvasImage);
        assertFalse(state.draggingCanvasText);
        assertFalse(state.resizingCanvasText);
        assertFalse(state.rotatingCanvasText);
        assertEquals("", state.canvasImageTransformAxis);
        assertTrue(state.transientQuestPositions.isEmpty());
        assertTrue(state.transientQuestScales.isEmpty());
        assertTrue(state.transientCanvasImages.isEmpty());
        assertTrue(state.transientCanvasTexts.isEmpty());
        assertSharedSnapshotsCleared(state);
        assertGuidesCleared(state);
        assertEquals("image/a", state.selectedCanvasImageId);
        assertTrue(state.selectedCanvasImageIds.contains("image/a"));
    }

    @Test
    void questDetailsSessionClearsDetailsFieldsAndKeepsMainPreviewsScoped() {
        TabletUiState state = new TabletUiState();
        seedMainCanvasTransform(state);
        seedQuestDetailsTransform(state);

        CanvasTransformSessions.clearQuestDetailsSession(state);

        assertEquals("image:a", state.transientCanvasImages.keySet().iterator().next());
        assertEquals("text:a", state.transientCanvasTexts.keySet().iterator().next());
        assertEquals("", state.questDetailsTransformKind);
        assertEquals("", state.questDetailsTransformId);
        assertEquals("", state.questDetailsTransformMode);
        assertEquals("", state.questDetailsTransformAxis);
        assertTrue(state.questDetailsTransientImages.isEmpty());
        assertTrue(state.questDetailsTransientTexts.isEmpty());
        assertSharedSnapshotsCleared(state);
        assertGuidesCleared(state);
    }

    @Test
    void previewClearsAreSurfaceScoped() {
        TabletUiState state = new TabletUiState();
        state.transientCanvasImages.put("image:a", image("image:a"));
        state.transientCanvasTexts.put("text:a", text("text:a"));
        state.questDetailsTransientImages.put("image:b", image("image:b"));
        state.questDetailsTransientTexts.put("text:b", text("text:b"));

        CanvasTransformSessions.clearMainCanvasPreviews(state);

        assertTrue(state.transientCanvasImages.isEmpty());
        assertTrue(state.transientCanvasTexts.isEmpty());
        assertFalse(state.questDetailsTransientImages.isEmpty());
        assertFalse(state.questDetailsTransientTexts.isEmpty());

        CanvasTransformSessions.clearQuestDetailsPreviews(state);

        assertTrue(state.questDetailsTransientImages.isEmpty());
        assertTrue(state.questDetailsTransientTexts.isEmpty());
    }

    @Test
    void elementBeginAndUpdateClearStaleSnapshotsBeforeCreatingPreview() {
        TabletUiState state = new TabletUiState();
        state.selectedGroup = "main";
        state.canvasZoom = 1.0f;
        state.canvasContentW = 512;
        state.canvasContentH = 512;
        state.canvasImagesByGroup.put("main", List.of(image("image:a")));
        seedMainCanvasTransform(state);

        CanvasTransformSessions.clearMainCanvasSession(state);
        CanvasElementTransformController transforms = new CanvasElementTransformController(state);
        transforms.beginImageTransform(image("image:a"), 20, 20);
        CanvasLayerMutations.putTransientCanvasImage(state, image("image:a").moveTo(36, 36));

        assertTrue(state.dragStartImagePositions.isEmpty());
        assertTrue(state.resizeStartImageLayers.isEmpty());
        assertTrue(state.rotateStartImageLayers.isEmpty());
        assertFalse(state.transientCanvasImages.isEmpty());
        assertEquals("image:a", state.selectedCanvasImageId);

        CanvasTransformSessions.clearMainCanvasSession(state);

        assertTrue(state.transientCanvasImages.isEmpty());
    }

    @Test
    void selectionTransformCommitClearUsesMainCanvasSessionCleanup() {
        TabletUiState state = new TabletUiState();
        seedMainCanvasTransform(state);

        new CanvasSelectionTransformController(state, new CanvasElementTransformController(state)).clear();

        assertFalse(state.draggingSelection);
        assertFalse(state.resizingSelection);
        assertFalse(state.rotatingSelection);
        assertTrue(state.transientQuestPositions.isEmpty());
        assertTrue(state.transientCanvasImages.isEmpty());
        assertSharedSnapshotsCleared(state);
        assertGuidesCleared(state);
    }

    private static void seedMainCanvasTransform(TabletUiState state) {
        state.draggingSelection = true;
        state.resizingSelection = true;
        state.rotatingSelection = true;
        state.draggingCanvasImage = true;
        state.resizingCanvasImage = true;
        state.rotatingCanvasImage = true;
        state.canvasImageTransformAxis = CanvasTransformGizmo.AXIS_MOVE_X;
        state.draggingCanvasText = true;
        state.resizingCanvasText = true;
        state.rotatingCanvasText = true;
        state.transientQuestPositions.put("quest/a", new CanvasPoint(1, 2));
        state.transientQuestScales.put("quest/a", 1.5f);
        state.transientCanvasImages.put("image:a", image("image:a"));
        state.transientCanvasTexts.put("text:a", text("text:a"));
        seedSharedSnapshots(state);
    }

    private static void seedQuestDetailsTransform(TabletUiState state) {
        state.questDetailsTransformKind = "desc_image";
        state.questDetailsTransformId = "image:b";
        state.questDetailsTransformMode = "move";
        state.questDetailsTransformAxis = CanvasTransformGizmo.AXIS_MOVE_Y;
        state.questDetailsTransformStartMouseX = 11;
        state.questDetailsTransformStartMouseY = 12;
        state.questDetailsTransformStartX = 13;
        state.questDetailsTransformStartY = 14;
        state.questDetailsTransformStartW = 15;
        state.questDetailsTransformStartH = 16;
        state.questDetailsTransformStartPivotX = 17;
        state.questDetailsTransformStartPivotY = 18;
        state.questDetailsTransformStartRotation = 19;
        state.questDetailsTransformStartYaw = 20;
        state.questDetailsTransformStartPitch = 21;
        state.questDetailsTransformPivotX = 22.0;
        state.questDetailsTransformPivotY = 23.0;
        state.questDetailsTransformStartAngle = 24.0;
        state.questDetailsTransientImages.put("image:b", image("image:b"));
        state.questDetailsTransientTexts.put("text:b", text("text:b"));
        seedSharedSnapshots(state);
    }

    private static void seedSharedSnapshots(TabletUiState state) {
        state.dragStartPositions.put("quest/a", new CanvasPoint(1, 2));
        state.dragStartImagePositions.put("image:a", new CanvasPoint(3, 4));
        state.dragStartTextPositions.put("text:a", new CanvasPoint(5, 6));
        state.resizeStartScales.put("quest/a", 1.5f);
        state.resizeStartPositions.put("quest/a", new CanvasPoint(7, 8));
        state.resizeStartImageLayers.put("image:a", image("image:a"));
        state.resizeStartTextLayers.put("text:a", text("text:a"));
        state.rotateStartPositions.put("quest/a", new CanvasPoint(9, 10));
        state.rotateStartCenters.put("quest/a", new CanvasDoublePoint(11.0, 12.0));
        state.rotateStartImageLayers.put("image:a", image("image:a"));
        state.rotateStartTextLayers.put("text:a", text("text:a"));
        state.rotatePreviewAngle = 1.25;
        state.dragSelectionDeltaX = 3;
        state.dragSelectionDeltaY = 4;
        state.snapGuideXVisible = true;
        state.snapGuideYVisible = true;
        state.snapGuideX = 100;
        state.snapGuideY = 120;
    }

    private static void assertSharedSnapshotsCleared(TabletUiState state) {
        assertTrue(state.dragStartPositions.isEmpty());
        assertTrue(state.dragStartImagePositions.isEmpty());
        assertTrue(state.dragStartTextPositions.isEmpty());
        assertTrue(state.resizeStartScales.isEmpty());
        assertTrue(state.resizeStartPositions.isEmpty());
        assertTrue(state.resizeStartImageLayers.isEmpty());
        assertTrue(state.resizeStartTextLayers.isEmpty());
        assertTrue(state.rotateStartPositions.isEmpty());
        assertTrue(state.rotateStartCenters.isEmpty());
        assertTrue(state.rotateStartImageLayers.isEmpty());
        assertTrue(state.rotateStartTextLayers.isEmpty());
        assertEquals(0.0, state.rotatePreviewAngle, 0.0001);
        assertEquals(0, state.dragSelectionDeltaX);
        assertEquals(0, state.dragSelectionDeltaY);
    }

    private static void assertGuidesCleared(TabletUiState state) {
        assertFalse(state.snapGuideXVisible);
        assertFalse(state.snapGuideYVisible);
        assertEquals(0, state.snapGuideX);
        assertEquals(0, state.snapGuideY);
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }

    private static CanvasTextLayer text(String id) {
        return new CanvasTextLayer(id, "Label", 60, 70, 80, 30, 0, "left", "normal", 0xFFFFFFFF);
    }

}

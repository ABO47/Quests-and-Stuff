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
        state.canvas.canvasSelection.setPrimaryImageId("image/a");
        state.canvas.canvasSelection.imageIds().add("image/a");

        CanvasTransformSessions.clearMainCanvasSession(state);

        assertFalse(state.canvas.draggingSelection);
        assertFalse(state.canvas.resizingSelection);
        assertFalse(state.canvas.rotatingSelection);
        assertFalse(state.canvas.draggingCanvasImage);
        assertFalse(state.canvas.resizingCanvasImage);
        assertFalse(state.canvas.rotatingCanvasImage);
        assertFalse(state.canvas.draggingCanvasText);
        assertFalse(state.canvas.resizingCanvasText);
        assertFalse(state.canvas.rotatingCanvasText);
        assertEquals("", state.canvas.canvasImageTransformAxis);
        assertTrue(state.canvas.transientQuestPositions.isEmpty());
        assertTrue(state.canvas.transientQuestScales.isEmpty());
        assertTrue(state.canvas.transientCanvasImages.isEmpty());
        assertTrue(state.canvas.transientCanvasTexts.isEmpty());
        assertSharedSnapshotsCleared(state);
        assertGuidesCleared(state);
        assertEquals("image/a", state.canvas.canvasSelection.primaryImageId());
        assertTrue(state.canvas.canvasSelection.imageIds().contains("image/a"));
    }

    @Test
    void questDetailsSessionClearsDetailsFieldsAndKeepsMainPreviewsScoped() {
        TabletUiState state = new TabletUiState();
        seedMainCanvasTransform(state);
        seedQuestDetailsTransform(state);

        CanvasTransformSessions.clearQuestDetailsSession(state);

        assertEquals("image:a", state.canvas.transientCanvasImages.keySet().iterator().next());
        assertEquals("text:a", state.canvas.transientCanvasTexts.keySet().iterator().next());
        assertEquals("", state.questDetails.questDetailsTransformKind);
        assertEquals("", state.questDetails.questDetailsTransformId);
        assertEquals("", state.questDetails.questDetailsTransformMode);
        assertEquals("", state.questDetails.questDetailsTransformAxis);
        assertTrue(state.questDetails.questDetailsTransientImages.isEmpty());
        assertTrue(state.questDetails.questDetailsTransientTexts.isEmpty());
        assertSharedSnapshotsCleared(state);
        assertGuidesCleared(state);
    }

    @Test
    void previewClearsAreSurfaceScoped() {
        TabletUiState state = new TabletUiState();
        state.canvas.transientCanvasImages.put("image:a", image("image:a"));
        state.canvas.transientCanvasTexts.put("text:a", text("text:a"));
        state.questDetails.questDetailsTransientImages.put("image:b", image("image:b"));
        state.questDetails.questDetailsTransientTexts.put("text:b", text("text:b"));

        CanvasTransformSessions.clearMainCanvasPreviews(state);

        assertTrue(state.canvas.transientCanvasImages.isEmpty());
        assertTrue(state.canvas.transientCanvasTexts.isEmpty());
        assertFalse(state.questDetails.questDetailsTransientImages.isEmpty());
        assertFalse(state.questDetails.questDetailsTransientTexts.isEmpty());

        CanvasTransformSessions.clearQuestDetailsPreviews(state);

        assertTrue(state.questDetails.questDetailsTransientImages.isEmpty());
        assertTrue(state.questDetails.questDetailsTransientTexts.isEmpty());
    }

    @Test
    void elementBeginAndUpdateClearStaleSnapshotsBeforeCreatingPreview() {
        TabletUiState state = new TabletUiState();
        state.root.selectedGroup = "main";
        state.canvas.canvasZoom = 1.0f;
        state.canvas.canvasContentW = 512;
        state.canvas.canvasContentH = 512;
        state.canvas.canvasImagesByGroup.put("main", List.of(image("image:a")));
        seedMainCanvasTransform(state);

        CanvasTransformSessions.clearMainCanvasSession(state);
        CanvasElementTransformController transforms = new CanvasElementTransformController(state);
        transforms.beginImageTransform(image("image:a"), 20, 20);
        CanvasLayerMutations.putTransientCanvasImage(state, image("image:a").moveTo(36, 36));

        assertTrue(state.canvas.dragStartImagePositions.isEmpty());
        assertTrue(state.canvas.resizeStartImageLayers.isEmpty());
        assertTrue(state.canvas.rotateStartImageLayers.isEmpty());
        assertFalse(state.canvas.transientCanvasImages.isEmpty());
        assertEquals("image:a", state.canvas.canvasSelection.primaryImageId());

        CanvasTransformSessions.clearMainCanvasSession(state);

        assertTrue(state.canvas.transientCanvasImages.isEmpty());
    }

    @Test
    void selectionTransformCommitClearUsesMainCanvasSessionCleanup() {
        TabletUiState state = new TabletUiState();
        seedMainCanvasTransform(state);

        new CanvasSelectionTransformController(state, new CanvasElementTransformController(state)).clear();

        assertFalse(state.canvas.draggingSelection);
        assertFalse(state.canvas.resizingSelection);
        assertFalse(state.canvas.rotatingSelection);
        assertTrue(state.canvas.transientQuestPositions.isEmpty());
        assertTrue(state.canvas.transientCanvasImages.isEmpty());
        assertSharedSnapshotsCleared(state);
        assertGuidesCleared(state);
    }

    private static void seedMainCanvasTransform(TabletUiState state) {
        state.canvas.draggingSelection = true;
        state.canvas.resizingSelection = true;
        state.canvas.rotatingSelection = true;
        state.canvas.draggingCanvasImage = true;
        state.canvas.resizingCanvasImage = true;
        state.canvas.rotatingCanvasImage = true;
        state.canvas.canvasImageTransformAxis = CanvasTransformGizmo.AXIS_MOVE_X;
        state.canvas.draggingCanvasText = true;
        state.canvas.resizingCanvasText = true;
        state.canvas.rotatingCanvasText = true;
        state.canvas.transientQuestPositions.put("quest/a", new CanvasPoint(1, 2));
        state.canvas.transientQuestScales.put("quest/a", 1.5f);
        state.canvas.transientCanvasImages.put("image:a", image("image:a"));
        state.canvas.transientCanvasTexts.put("text:a", text("text:a"));
        seedSharedSnapshots(state);
    }

    private static void seedQuestDetailsTransform(TabletUiState state) {
        state.questDetails.questDetailsTransformKind = "desc_image";
        state.questDetails.questDetailsTransformId = "image:b";
        state.questDetails.questDetailsTransformMode = "move";
        state.questDetails.questDetailsTransformAxis = CanvasTransformGizmo.AXIS_MOVE_Y;
        state.questDetails.questDetailsTransformStartMouseX = 11;
        state.questDetails.questDetailsTransformStartMouseY = 12;
        state.questDetails.questDetailsTransformStartX = 13;
        state.questDetails.questDetailsTransformStartY = 14;
        state.questDetails.questDetailsTransformStartW = 15;
        state.questDetails.questDetailsTransformStartH = 16;
        state.questDetails.questDetailsTransformStartPivotX = 17;
        state.questDetails.questDetailsTransformStartPivotY = 18;
        state.questDetails.questDetailsTransformStartRotation = 19;
        state.questDetails.questDetailsTransformStartYaw = 20;
        state.questDetails.questDetailsTransformStartPitch = 21;
        state.questDetails.questDetailsTransformPivotX = 22.0;
        state.questDetails.questDetailsTransformPivotY = 23.0;
        state.questDetails.questDetailsTransformStartAngle = 24.0;
        state.questDetails.questDetailsTransientImages.put("image:b", image("image:b"));
        state.questDetails.questDetailsTransientTexts.put("text:b", text("text:b"));
        seedSharedSnapshots(state);
    }

    private static void seedSharedSnapshots(TabletUiState state) {
        state.canvas.dragStartPositions.put("quest/a", new CanvasPoint(1, 2));
        state.canvas.dragStartImagePositions.put("image:a", new CanvasPoint(3, 4));
        state.canvas.dragStartTextPositions.put("text:a", new CanvasPoint(5, 6));
        state.canvas.resizeStartScales.put("quest/a", 1.5f);
        state.canvas.resizeStartPositions.put("quest/a", new CanvasPoint(7, 8));
        state.canvas.resizeStartImageLayers.put("image:a", image("image:a"));
        state.canvas.resizeStartTextLayers.put("text:a", text("text:a"));
        state.canvas.rotateStartPositions.put("quest/a", new CanvasPoint(9, 10));
        state.canvas.rotateStartCenters.put("quest/a", new CanvasDoublePoint(11.0, 12.0));
        state.canvas.rotateStartImageLayers.put("image:a", image("image:a"));
        state.canvas.rotateStartTextLayers.put("text:a", text("text:a"));
        state.canvas.rotatePreviewAngle = 1.25;
        state.canvas.dragSelectionDeltaX = 3;
        state.canvas.dragSelectionDeltaY = 4;
        state.canvas.snapGuideXVisible = true;
        state.canvas.snapGuideYVisible = true;
        state.canvas.snapGuideX = 100;
        state.canvas.snapGuideY = 120;
    }

    private static void assertSharedSnapshotsCleared(TabletUiState state) {
        assertTrue(state.canvas.dragStartPositions.isEmpty());
        assertTrue(state.canvas.dragStartImagePositions.isEmpty());
        assertTrue(state.canvas.dragStartTextPositions.isEmpty());
        assertTrue(state.canvas.resizeStartScales.isEmpty());
        assertTrue(state.canvas.resizeStartPositions.isEmpty());
        assertTrue(state.canvas.resizeStartImageLayers.isEmpty());
        assertTrue(state.canvas.resizeStartTextLayers.isEmpty());
        assertTrue(state.canvas.rotateStartPositions.isEmpty());
        assertTrue(state.canvas.rotateStartCenters.isEmpty());
        assertTrue(state.canvas.rotateStartImageLayers.isEmpty());
        assertTrue(state.canvas.rotateStartTextLayers.isEmpty());
        assertEquals(0.0, state.canvas.rotatePreviewAngle, 0.0001);
        assertEquals(0, state.canvas.dragSelectionDeltaX);
        assertEquals(0, state.canvas.dragSelectionDeltaY);
    }

    private static void assertGuidesCleared(TabletUiState state) {
        assertFalse(state.canvas.snapGuideXVisible);
        assertFalse(state.canvas.snapGuideYVisible);
        assertEquals(0, state.canvas.snapGuideX);
        assertEquals(0, state.canvas.snapGuideY);
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }

    private static CanvasTextLayer text(String id) {
        return new CanvasTextLayer(id, "Label", 60, 70, 80, 30, 0, "left", "normal", 0xFFFFFFFF);
    }

}

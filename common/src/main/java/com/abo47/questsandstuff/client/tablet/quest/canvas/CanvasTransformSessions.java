package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasTransformSessions {
    private CanvasTransformSessions() {
    }

    public static void clearMainCanvasSession(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.draggingSelection = false;
        state.resizingSelection = false;
        state.rotatingSelection = false;
        state.draggingCanvasImage = false;
        state.resizingCanvasImage = false;
        state.rotatingCanvasImage = false;
        state.canvasImageTransformAxis = "";
        state.draggingCanvasText = false;
        state.resizingCanvasText = false;
        state.rotatingCanvasText = false;

        state.transientQuestPositions.clear();
        state.transientQuestScales.clear();
        state.transientCanvasImages.clear();
        state.transientCanvasTexts.clear();
        clearSharedLayerTransformSnapshots(state);

        state.canvasImageDragStartX = 0;
        state.canvasImageDragStartY = 0;
        state.canvasImageStartX = 0;
        state.canvasImageStartY = 0;
        state.canvasImageStartW = 0;
        state.canvasImageStartH = 0;
        state.canvasImageStartPivotX = 0;
        state.canvasImageStartPivotY = 0;
        state.canvasImageStartRotation = 0;
        state.canvasImageStartYaw = 0;
        state.canvasImageStartPitch = 0;
        state.canvasImageRotatePivotX = 0.0;
        state.canvasImageRotatePivotY = 0.0;
        state.canvasImageRotateStartAngle = 0.0;

        state.canvasTextDragStartX = 0;
        state.canvasTextDragStartY = 0;
        state.canvasTextStartX = 0;
        state.canvasTextStartY = 0;
        state.canvasTextStartW = 0;
        state.canvasTextStartH = 0;
        state.canvasTextStartRotation = 0;
        state.canvasTextRotatePivotX = 0.0;
        state.canvasTextRotatePivotY = 0.0;
        state.canvasTextRotateStartAngle = 0.0;
        clearSnapGuides(state);
    }

    public static void clearQuestDetailsSession(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetailsTransformKind = "";
        state.questDetailsTransformId = "";
        state.questDetailsTransformMode = "";
        state.questDetailsTransformAxis = "";
        state.questDetailsTransformStartMouseX = 0;
        state.questDetailsTransformStartMouseY = 0;
        state.questDetailsTransformStartX = 0;
        state.questDetailsTransformStartY = 0;
        state.questDetailsTransformStartW = 0;
        state.questDetailsTransformStartH = 0;
        state.questDetailsTransformStartPivotX = 0;
        state.questDetailsTransformStartPivotY = 0;
        state.questDetailsTransformStartRotation = 0;
        state.questDetailsTransformStartYaw = 0;
        state.questDetailsTransformStartPitch = 0;
        state.questDetailsTransformPivotX = 0.0;
        state.questDetailsTransformPivotY = 0.0;
        state.questDetailsTransformStartAngle = 0.0;
        state.questDetailsTransientImages.clear();
        state.questDetailsTransientTexts.clear();
        clearSharedLayerTransformSnapshots(state);
        clearSnapGuides(state);
    }

    public static void clearMainCanvasPreviews(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.transientCanvasImages.clear();
        state.transientCanvasTexts.clear();
    }

    public static void clearQuestDetailsPreviews(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetailsTransientImages.clear();
        state.questDetailsTransientTexts.clear();
    }

    public static void clearSnapGuides(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
        state.snapGuideX = 0;
        state.snapGuideY = 0;
    }

    private static void clearSharedLayerTransformSnapshots(TabletUiState state) {
        state.dragStartPositions.clear();
        state.dragStartImagePositions.clear();
        state.dragStartTextPositions.clear();
        state.resizeStartScales.clear();
        state.resizeStartPositions.clear();
        state.resizeStartImageLayers.clear();
        state.resizeStartTextLayers.clear();
        state.rotateStartPositions.clear();
        state.rotateStartCenters.clear();
        state.rotateStartImageLayers.clear();
        state.rotateStartTextLayers.clear();
        state.resizeStartLeft = 0;
        state.resizeStartTop = 0;
        state.resizeStartRight = 0;
        state.resizeStartBottom = 0;
        state.resizeStartMouseX = 0;
        state.resizeStartMouseY = 0;
        state.rotatePivotX = 0.0;
        state.rotatePivotY = 0.0;
        state.rotateStartAngle = 0.0;
        state.rotatePreviewAngle = 0.0;
        state.rotateStartBoundsLeft = 0;
        state.rotateStartBoundsTop = 0;
        state.rotateStartBoundsRight = 0;
        state.rotateStartBoundsBottom = 0;
        state.dragSelectionDeltaX = 0;
        state.dragSelectionDeltaY = 0;
        state.dragStartBoundsLeft = 0;
        state.dragStartBoundsTop = 0;
        state.dragStartBoundsRight = 0;
        state.dragStartBoundsBottom = 0;
        state.dragStartSelectionLeft = 0;
        state.dragStartSelectionTop = 0;
        state.dragStartSelectionRight = 0;
        state.dragStartSelectionBottom = 0;
    }
}

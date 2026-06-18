package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class CanvasTransformSessions {
    private CanvasTransformSessions() {
    }

    public static void clearMainCanvasSession(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.canvas.draggingSelection = false;
        state.canvas.resizingSelection = false;
        state.canvas.rotatingSelection = false;
        state.canvas.draggingCanvasImage = false;
        state.canvas.resizingCanvasImage = false;
        state.canvas.rotatingCanvasImage = false;
        state.canvas.canvasImageTransformAxis = "";
        state.canvas.draggingCanvasText = false;
        state.canvas.resizingCanvasText = false;
        state.canvas.rotatingCanvasText = false;
        state.canvas.draggingCanvasExclusiveChoice = false;
        state.canvas.resizingCanvasExclusiveChoice = false;
        state.canvas.rotatingCanvasExclusiveChoice = false;
        state.canvas.connectEcId = "";
        state.canvas.quickConnectEcId = "";

        state.canvas.transientQuestPositions.clear();
        state.canvas.transientQuestScales.clear();
        state.canvas.transientCanvasImages.clear();
        state.canvas.transientCanvasTexts.clear();
        state.canvas.transientCanvasExclusiveChoices.clear();
        clearSharedLayerTransformSnapshots(state);

        state.canvas.canvasImageDragStartX = 0;
        state.canvas.canvasImageDragStartY = 0;
        state.canvas.canvasImageStartX = 0;
        state.canvas.canvasImageStartY = 0;
        state.canvas.canvasImageStartW = 0;
        state.canvas.canvasImageStartH = 0;
        state.canvas.canvasImageStartPivotX = 0;
        state.canvas.canvasImageStartPivotY = 0;
        state.canvas.canvasImageStartRotation = 0;
        state.canvas.canvasImageStartYaw = 0;
        state.canvas.canvasImageStartPitch = 0;
        state.canvas.canvasImageRotatePivotX = 0.0;
        state.canvas.canvasImageRotatePivotY = 0.0;
        state.canvas.canvasImageRotateStartAngle = 0.0;

        state.canvas.canvasTextDragStartX = 0;
        state.canvas.canvasTextDragStartY = 0;
        state.canvas.canvasTextStartX = 0;
        state.canvas.canvasTextStartY = 0;
        state.canvas.canvasTextStartW = 0;
        state.canvas.canvasTextStartH = 0;
        state.canvas.canvasTextStartRotation = 0;
        state.canvas.canvasTextRotatePivotX = 0.0;
        state.canvas.canvasTextRotatePivotY = 0.0;
        state.canvas.canvasTextRotateStartAngle = 0.0;

        state.canvas.canvasEcDragStartX = 0;
        state.canvas.canvasEcDragStartY = 0;
        state.canvas.canvasEcStartX = 0;
        state.canvas.canvasEcStartY = 0;
        state.canvas.canvasEcStartW = 0;
        state.canvas.canvasEcStartH = 0;
        state.canvas.canvasEcStartRotation = 0;
        state.canvas.canvasEcRotatePivotX = 0.0;
        state.canvas.canvasEcRotatePivotY = 0.0;
        state.canvas.canvasEcRotateStartAngle = 0.0;
        clearSnapGuides(state);
    }

    public static void clearQuestDetailsSession(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetails.questDetailsTransformKind = "";
        state.questDetails.questDetailsTransformId = "";
        state.questDetails.questDetailsTransformMode = "";
        state.questDetails.questDetailsTransformAxis = "";
        state.questDetails.questDetailsTransformStartMouseX = 0;
        state.questDetails.questDetailsTransformStartMouseY = 0;
        state.questDetails.questDetailsTransformStartX = 0;
        state.questDetails.questDetailsTransformStartY = 0;
        state.questDetails.questDetailsTransformStartW = 0;
        state.questDetails.questDetailsTransformStartH = 0;
        state.questDetails.questDetailsTransformStartPivotX = 0;
        state.questDetails.questDetailsTransformStartPivotY = 0;
        state.questDetails.questDetailsTransformStartRotation = 0;
        state.questDetails.questDetailsTransformStartYaw = 0;
        state.questDetails.questDetailsTransformStartPitch = 0;
        state.questDetails.questDetailsTransformPivotX = 0.0;
        state.questDetails.questDetailsTransformPivotY = 0.0;
        state.questDetails.questDetailsTransformStartAngle = 0.0;
        state.questDetails.questDetailsTransientImages.clear();
        state.questDetails.questDetailsTransientTexts.clear();
        clearSharedLayerTransformSnapshots(state);
        clearSnapGuides(state);
    }

    public static void clearMainCanvasPreviews(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.canvas.transientCanvasImages.clear();
        state.canvas.transientCanvasTexts.clear();
        state.canvas.transientCanvasExclusiveChoices.clear();
    }

    public static void clearQuestDetailsPreviews(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.questDetails.questDetailsTransientImages.clear();
        state.questDetails.questDetailsTransientTexts.clear();
    }

    public static void clearSnapGuides(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.canvas.snapGuideXVisible = false;
        state.canvas.snapGuideYVisible = false;
        state.canvas.snapGuideX = 0;
        state.canvas.snapGuideY = 0;
    }

    private static void clearSharedLayerTransformSnapshots(TabletUiState state) {
        state.canvas.dragStartPositions.clear();
        state.canvas.dragStartImagePositions.clear();
        state.canvas.dragStartTextPositions.clear();
        state.canvas.resizeStartScales.clear();
        state.canvas.resizeStartPositions.clear();
        state.canvas.resizeStartImageLayers.clear();
        state.canvas.resizeStartTextLayers.clear();
        state.canvas.rotateStartPositions.clear();
        state.canvas.rotateStartCenters.clear();
        state.canvas.rotateStartImageLayers.clear();
        state.canvas.rotateStartTextLayers.clear();
        state.canvas.dragStartEcLayers.clear();
        state.canvas.resizeStartEcLayers.clear();
        state.canvas.rotateStartEcLayers.clear();
        state.canvas.resizeStartLeft = 0;
        state.canvas.resizeStartTop = 0;
        state.canvas.resizeStartRight = 0;
        state.canvas.resizeStartBottom = 0;
        state.canvas.resizeStartMouseX = 0;
        state.canvas.resizeStartMouseY = 0;
        state.canvas.rotatePivotX = 0.0;
        state.canvas.rotatePivotY = 0.0;
        state.canvas.rotateStartAngle = 0.0;
        state.canvas.rotatePreviewAngle = 0.0;
        state.canvas.rotateStartBoundsLeft = 0;
        state.canvas.rotateStartBoundsTop = 0;
        state.canvas.rotateStartBoundsRight = 0;
        state.canvas.rotateStartBoundsBottom = 0;
        state.canvas.dragSelectionDeltaX = 0;
        state.canvas.dragSelectionDeltaY = 0;
        state.canvas.dragStartBoundsLeft = 0;
        state.canvas.dragStartBoundsTop = 0;
        state.canvas.dragStartBoundsRight = 0;
        state.canvas.dragStartBoundsBottom = 0;
        state.canvas.dragStartSelectionLeft = 0;
        state.canvas.dragStartSelectionTop = 0;
        state.canvas.dragStartSelectionRight = 0;
        state.canvas.dragStartSelectionBottom = 0;
    }
}

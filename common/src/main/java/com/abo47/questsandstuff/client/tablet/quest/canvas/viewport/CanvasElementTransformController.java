package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.ui.TabletModifierKeys;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.quest.canvas.transform.LayerTransformEngine;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;

import java.util.List;
import java.util.Set;

public final class CanvasElementTransformController {
    private final TabletUiState state;

    public CanvasElementTransformController(TabletUiState state) {
        this.state = state;
    }

    public void beginImageTransform(CanvasImageLayer image, int localX, int localY) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        boolean gizmoSupported = CanvasTransformGizmo.supports(image.asset());
        boolean selectedBeforeClick = image.id().equals(state.canvas.canvasSelection.primaryImageId()) || state.canvas.canvasSelection.imageIds().contains(image.id());
        if (gizmoSupported && !selectedBeforeClick) {
            CanvasTransformGizmo.setMode(state, CanvasTransformMode.MOVE);
        }
        CanvasTransformMode gizmoMode = imageTransformMode(image, localX, localY);
        state.canvas.canvasSelection.setPrimaryImageId(image.id());
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.imageIds().add(image.id());
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasImageDragStartX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        state.canvas.canvasImageDragStartY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        state.canvas.canvasImageStartX = image.x();
        state.canvas.canvasImageStartY = image.y();
        state.canvas.canvasImageStartW = image.w();
        state.canvas.canvasImageStartH = image.h();
        state.canvas.canvasImageStartPivotX = image.pivotX();
        state.canvas.canvasImageStartPivotY = image.pivotY();
        state.canvas.canvasImageStartRotation = image.rotation();
        state.canvas.canvasImageStartYaw = image.entityYaw();
        state.canvas.canvasImageStartPitch = image.modelPitch();
        state.canvas.canvasImageTransformAxis = gizmoSupported
                ? CanvasTransformGizmo.axisAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), localX, localY)
                : "";
        if (gizmoMode == CanvasTransformMode.MOVE) {
            state.canvas.canvasImageTransformAxis = CanvasTransformGizmo.moveAxisOrFree(state.canvas.canvasImageTransformAxis);
        }
        state.canvas.resizingCanvasImage = gizmoMode == CanvasTransformMode.RESIZE
                || (!gizmoSupported && gizmoMode == null && CanvasRenderer.isCanvasImageResizeHandleHit(state, image, localX, localY));
        state.canvas.rotatingCanvasImage = gizmoMode == CanvasTransformMode.ROTATE
                || (!gizmoSupported && gizmoMode == null && CanvasRenderer.isCanvasImageRotateHandleHit(state, image, localX, localY));
        if (state.canvas.rotatingCanvasImage) {
            state.canvas.canvasImageRotatePivotX = CanvasElementGeometry.logicalPivotX(image.x(), image.w(), image.pivotX());
            state.canvas.canvasImageRotatePivotY = CanvasElementGeometry.logicalPivotY(image.y(), image.h(), image.pivotY());
            double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
            double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
            state.canvas.canvasImageRotateStartAngle = Math.atan2(logicalMouseY - state.canvas.canvasImageRotatePivotY, logicalMouseX - state.canvas.canvasImageRotatePivotX);
        }
        state.canvas.draggingCanvasImage = !(gizmoSupported && gizmoMode == null) && !state.canvas.resizingCanvasImage && !state.canvas.rotatingCanvasImage;
        state.canvas.canvasSelection.questIds().clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas image transform start id={} drag={} resize={} rotate={}", image.id(), state.canvas.draggingCanvasImage, state.canvas.resizingCanvasImage, state.canvas.rotatingCanvasImage);
    }

    private CanvasTransformMode imageTransformMode(CanvasImageLayer image, int localX, int localY) {
        if (!CanvasTransformGizmo.supports(image.asset())) {
            return null;
        }
        if (CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.RESIZE
                && TabletModifierKeys.shiftDown()
                && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY)) {
            return CanvasTransformMode.MOVE;
        }
        CanvasTransformMode hitMode = CanvasTransformGizmo.modeAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), localX, localY);
        if (hitMode != null) {
            return hitMode;
        }
        if (CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.MOVE
                && TabletModifierKeys.shiftDown()
                && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY)) {
            return CanvasTransformMode.MOVE;
        }
        return null;
    }

    public void updateImageTransform(int localX, int localY, List<QuestCardLayout> cards) {
        String chapter = TabletStateQueries.selectedChapterName(state);
        CanvasImageLayer image = findImage(chapter, state.canvas.canvasSelection.primaryImageId());
        if (image == null) {
            return;
        }
        int logicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int logicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        int dx = logicalX - state.canvas.canvasImageDragStartX;
        int dy = logicalY - state.canvas.canvasImageDragStartY;
        CanvasImageLayer next = image;
        if (state.canvas.draggingCanvasImage) {
            CanvasPoint anchor = CanvasTransformGizmo.supports(image.asset())
                    ? modelDragAnchor(state.canvas.canvasImageStartX, state.canvas.canvasImageStartY, state.canvas.canvasImageStartW, state.canvas.canvasImageStartH, dx, dy)
                    : dragAnchor(state.canvas.canvasImageStartX, state.canvas.canvasImageStartY, state.canvas.canvasImageStartW, state.canvas.canvasImageStartH, state.canvas.canvasImageStartPivotX, state.canvas.canvasImageStartPivotY, state.canvas.canvasImageStartRotation, dx, dy);
            next = new CanvasImageLayer(image.id(), image.asset(), anchor.x, anchor.y, state.canvas.canvasImageStartW, state.canvas.canvasImageStartH, state.canvas.canvasImageStartRotation, image.entityYaw(), image.entitySpinSpeed(), image.modelPitch(), image.pivotX(), image.pivotY());
            next = applySmartSnapToImage(next, cards, chapter);
        } else if (state.canvas.resizingCanvasImage) {
            clearSnapGuides();
            next = resizeImageFromHandle(image, localX, localY);
        } else if (state.canvas.rotatingCanvasImage) {
            clearSnapGuides();
            boolean modelAxisRotation = CanvasTransformGizmo.supports(image.asset()) && !CanvasTransformGizmo.AXIS_ROLL.equals(state.canvas.canvasImageTransformAxis);
            next = modelAxisRotation
                    ? rotateModelFromDrag(image, logicalX, logicalY)
                    : clampRotationPreviewImage(image.rotateTo(layerRotation(logicalX, logicalY)));
        }
        CanvasLayerMutations.putTransientCanvasImage(state, next);
    }

    private CanvasImageLayer rotateModelFromDrag(CanvasImageLayer image, int logicalX, int logicalY) {
        LayerTransformEngine.ModelRotation rotation = LayerTransformEngine.modelRotation(new LayerTransformEngine.ModelRotationRequest(
                new LayerTransformEngine.ModelRotation(state.canvas.canvasImageStartYaw, state.canvas.canvasImageStartPitch),
                CanvasTransformGizmo.AXIS_PITCH.equals(state.canvas.canvasImageTransformAxis),
                state.canvas.canvasImageRotatePivotX,
                state.canvas.canvasImageRotatePivotY,
                state.canvas.canvasImageRotateStartAngle,
                logicalX,
                logicalY,
                TabletModifierKeys.shiftDown()
        ));
        return image.withModelRotation(rotation.yaw(), rotation.pitch());
    }

    private int layerRotation(int logicalX, int logicalY) {
        return LayerTransformEngine.layerRotation(new LayerTransformEngine.RotationRequest(
                state.canvas.canvasImageStartRotation,
                state.canvas.canvasImageRotatePivotX,
                state.canvas.canvasImageRotatePivotY,
                state.canvas.canvasImageRotateStartAngle,
                logicalX,
                logicalY,
                TabletModifierKeys.shiftDown()
        ));
    }

    public CanvasImageLayer findImage(String chapter, String imageId) {
        if (chapter == null || chapter.isBlank() || imageId == null || imageId.isBlank()) {
            return null;
        }
        return state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of()).stream()
                .filter(entry -> entry.id().equals(imageId))
                .findFirst()
                .orElse(null);
    }

    public void beginTextTransform(CanvasTextLayer text, int localX, int localY) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        state.canvas.canvasSelection.setPrimaryTextId(text.id());
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.textIds().add(text.id());
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasTextMenuTarget = text.id();
        state.canvas.canvasTextDragStartX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        state.canvas.canvasTextDragStartY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        state.canvas.canvasTextStartX = text.x();
        state.canvas.canvasTextStartY = text.y();
        state.canvas.canvasTextStartW = text.w();
        state.canvas.canvasTextStartH = text.h();
        state.canvas.canvasTextStartRotation = text.rotation();
        state.canvas.resizingCanvasText = CanvasRenderer.isCanvasTextResizeHandleHit(state, text, localX, localY);
        state.canvas.rotatingCanvasText = CanvasRenderer.isCanvasTextRotateHandleHit(state, text, localX, localY);
        if (state.canvas.rotatingCanvasText) {
            state.canvas.canvasTextRotatePivotX = CanvasElementGeometry.logicalPivotX(text.x(), text.w(), CanvasElementGeometry.defaultPivot(text.w()));
            state.canvas.canvasTextRotatePivotY = CanvasElementGeometry.logicalPivotY(text.y(), text.h(), CanvasElementGeometry.defaultPivot(text.h()));
            double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
            double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
            state.canvas.canvasTextRotateStartAngle = Math.atan2(logicalMouseY - state.canvas.canvasTextRotatePivotY, logicalMouseX - state.canvas.canvasTextRotatePivotX);
        }
        state.canvas.draggingCanvasText = !state.canvas.resizingCanvasText && !state.canvas.rotatingCanvasText;
        state.canvas.canvasSelection.questIds().clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text transform start id={} drag={} resize={} rotate={}", text.id(), state.canvas.draggingCanvasText, state.canvas.resizingCanvasText, state.canvas.rotatingCanvasText);
    }

    public void beginExclusiveChoiceTransform(CanvasExclusiveChoice ec, int localX, int localY) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        state.canvas.canvasSelection.setPrimaryEcId(ec.id());
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.ecIds().clear();
        state.canvas.canvasSelection.ecIds().add(ec.id());
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasEcDragStartX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        state.canvas.canvasEcDragStartY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        CanvasExclusiveChoice effective = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
        state.canvas.canvasEcStartX = effective.x();
        state.canvas.canvasEcStartY = effective.y();
        state.canvas.canvasEcStartW = effective.w();
        state.canvas.canvasEcStartH = effective.h();
        state.canvas.canvasEcStartRotation = effective.rotation();
        state.canvas.resizingCanvasExclusiveChoice = CanvasRenderer.isCanvasExclusiveChoiceResizeHandleHit(state, ec, localX, localY);
        state.canvas.rotatingCanvasExclusiveChoice = CanvasRenderer.isCanvasExclusiveChoiceRotateHandleHit(state, ec, localX, localY);
        if (state.canvas.rotatingCanvasExclusiveChoice) {
            state.canvas.canvasEcRotatePivotX = CanvasElementGeometry.logicalPivotX(ec.x(), ec.w(), ec.pivotX());
            state.canvas.canvasEcRotatePivotY = CanvasElementGeometry.logicalPivotY(ec.y(), ec.h(), ec.pivotY());
            double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
            double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
            state.canvas.canvasEcRotateStartAngle = Math.atan2(logicalMouseY - state.canvas.canvasEcRotatePivotY, logicalMouseX - state.canvas.canvasEcRotatePivotX);
        }
        state.canvas.draggingCanvasExclusiveChoice = !state.canvas.resizingCanvasExclusiveChoice && !state.canvas.rotatingCanvasExclusiveChoice;
        state.canvas.canvasSelection.questIds().clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas exclusive choice transform start id={} drag={} resize={} rotate={}", ec.id(), state.canvas.draggingCanvasExclusiveChoice, state.canvas.resizingCanvasExclusiveChoice, state.canvas.rotatingCanvasExclusiveChoice);
    }

    public void updateExclusiveChoiceTransform(int localX, int localY, List<QuestCardLayout> cards) {
        String chapter = TabletStateQueries.selectedChapterName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, state.canvas.canvasSelection.primaryEcId());
        if (ec == null) {
            return;
        }
        int logicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int logicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        int dx = logicalX - state.canvas.canvasEcDragStartX;
        int dy = logicalY - state.canvas.canvasEcDragStartY;
        CanvasExclusiveChoice next = ec;
        if (state.canvas.draggingCanvasExclusiveChoice) {
            CanvasPoint anchor = dragAnchor(state.canvas.canvasEcStartX, state.canvas.canvasEcStartY, state.canvas.canvasEcStartW, state.canvas.canvasEcStartH, CanvasElementGeometry.defaultPivot(state.canvas.canvasEcStartW), CanvasElementGeometry.defaultPivot(state.canvas.canvasEcStartH), state.canvas.canvasEcStartRotation, dx, dy);
            next = new CanvasExclusiveChoice(ec.id(), anchor.x, anchor.y, state.canvas.canvasEcStartW, state.canvas.canvasEcStartH, state.canvas.canvasEcStartRotation, ec.connectionQuestIds(), ec.prerequisiteQuestIds(), ec.background(), ec.connectionColors(), ec.connectionModes(), ec.connectionTextures(), ec.connectionTextureSpacings(), ec.hiddenConnections());
            next = fittedExclusiveChoiceIfGridLocked(next);
            next = applySmartSnapToExclusiveChoice(next, cards, chapter);
        } else if (state.canvas.resizingCanvasExclusiveChoice) {
            clearSnapGuides();
            next = resizeExclusiveChoiceFromHandle(ec, localX, localY);
        } else if (state.canvas.rotatingCanvasExclusiveChoice) {
            clearSnapGuides();
            int angle = LayerTransformEngine.layerRotation(new LayerTransformEngine.RotationRequest(
                    state.canvas.canvasEcStartRotation,
                    state.canvas.canvasEcRotatePivotX,
                    state.canvas.canvasEcRotatePivotY,
                    state.canvas.canvasEcRotateStartAngle,
                    logicalX,
                    logicalY,
                    TabletModifierKeys.shiftDown()
            ));
            next = clampRotationPreviewExclusiveChoice(ec.rotateTo(angle));
        }
        CanvasLayerMutations.putTransientCanvasExclusiveChoice(state, next);
    }

    public void updateTextTransform(int localX, int localY, List<QuestCardLayout> cards) {
        String chapter = TabletStateQueries.selectedChapterName(state);
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, chapter, state.canvas.canvasSelection.primaryTextId());
        if (text == null) {
            return;
        }
        int logicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int logicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        int dx = logicalX - state.canvas.canvasTextDragStartX;
        int dy = logicalY - state.canvas.canvasTextDragStartY;
        CanvasTextLayer next = text;
        if (state.canvas.draggingCanvasText) {
            CanvasPoint anchor = dragAnchor(state.canvas.canvasTextStartX, state.canvas.canvasTextStartY, state.canvas.canvasTextStartW, state.canvas.canvasTextStartH, CanvasElementGeometry.defaultPivot(state.canvas.canvasTextStartW), CanvasElementGeometry.defaultPivot(state.canvas.canvasTextStartH), state.canvas.canvasTextStartRotation, dx, dy);
            next = new CanvasTextLayer(text.id(), text.text(), anchor.x, anchor.y, state.canvas.canvasTextStartW, state.canvas.canvasTextStartH, state.canvas.canvasTextStartRotation, text.align(), text.style(), text.color(), text.fontSize(), text.spans());
            next = applySmartSnapToText(next, cards, chapter);
        } else if (state.canvas.resizingCanvasText) {
            clearSnapGuides();
            next = resizeTextFromHandle(text, localX, localY);
        } else if (state.canvas.rotatingCanvasText) {
            clearSnapGuides();
            int angle = LayerTransformEngine.layerRotation(new LayerTransformEngine.RotationRequest(
                    state.canvas.canvasTextStartRotation,
                    state.canvas.canvasTextRotatePivotX,
                    state.canvas.canvasTextRotatePivotY,
                    state.canvas.canvasTextRotateStartAngle,
                    logicalX,
                    logicalY,
                    TabletModifierKeys.shiftDown()
            ));
            next = clampRotationPreviewText(text.rotateTo(angle));
        }
        CanvasLayerMutations.putTransientCanvasText(state, next);
    }

    private CanvasImageLayer applySmartSnapToImage(CanvasImageLayer image, List<QuestCardLayout> cards, String chapter) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForImage(state, image),
                cards,
                chapter,
                Set.of(),
                Set.of(image.id()),
                Set.of()
        );
        if (!snap.hasOffset()) {
            return image;
        }
        CanvasPoint offset = constrainedSnapOffset(snap.offsetX(), snap.offsetY(), image.rotation());
        int offsetX = offset.x;
        int offsetY = offset.y;
        if (!CanvasTransformGizmo.AXIS_MOVE_FREE.equals(CanvasTransformGizmo.moveAxisOrFree(state.canvas.canvasImageTransformAxis))) {
            state.canvas.snapGuideXVisible = state.canvas.snapGuideXVisible && offsetX != 0;
            state.canvas.snapGuideYVisible = state.canvas.snapGuideYVisible && offsetY != 0;
        }
        int requestedX = image.x() + offsetX;
        int requestedY = image.y() + offsetY;
        var clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, requestedX, requestedY, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides();
        }
        return image.moveTo(clamped.x, clamped.y);
    }

    private CanvasExclusiveChoice applySmartSnapToExclusiveChoice(CanvasExclusiveChoice ec, List<QuestCardLayout> cards, String chapter) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForExclusiveChoice(state, ec),
                cards,
                chapter,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(ec.id())
        );
        if (!snap.hasOffset()) {
            return ec;
        }
        int requestedX = ec.x() + snap.offsetX();
        int requestedY = ec.y() + snap.offsetY();
        var clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, requestedX, requestedY, ec.w(), ec.h(), 0, 0, ec.rotation());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides();
        }
        return ec.moveTo(clamped.x, clamped.y);
    }

    private CanvasTextLayer applySmartSnapToText(CanvasTextLayer text, List<QuestCardLayout> cards, String chapter) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForText(state, text),
                cards,
                chapter,
                Set.of(),
                Set.of(),
                Set.of(text.id())
        );
        if (!snap.hasOffset()) {
            return text;
        }
        int requestedX = text.x() + snap.offsetX();
        int requestedY = text.y() + snap.offsetY();
        var clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, requestedX, requestedY, text.w(), text.h(), CanvasElementGeometry.defaultPivot(text.w()), CanvasElementGeometry.defaultPivot(text.h()), text.rotation());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides();
        }
        return text.moveTo(clamped.x, clamped.y);
    }

    private CanvasPoint dragAnchor(int startX, int startY, int width, int height, int pivotX, int pivotY, int rotation, int dx, int dy) {
        return LayerTransformEngine.moveAnchor(
                new LayerTransformEngine.MoveRequest(layerRect(startX, startY, width, height, pivotX, pivotY, rotation), dx, dy, snapSettings()),
                (x, y, rect) -> CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, rect.width(), rect.height(), rect.pivotX(), rect.pivotY(), rect.rotation())
        );
    }

    private CanvasPoint modelDragAnchor(int startX, int startY, int width, int height, int dx, int dy) {
        String axis = CanvasTransformGizmo.moveAxisOrFree(state.canvas.canvasImageTransformAxis);
        CanvasPoint delta = LayerTransformEngine.modelMoveDelta(new LayerTransformEngine.ModelMoveRequest(
                dx,
                dy,
                state.canvas.canvasImageStartRotation,
                axis,
                CanvasTransformGizmo.AXIS_MOVE_FREE.equals(axis),
                snapSettings()
        ));
        return CanvasGeometry.clampRotatedAnchorToCanvas(state, startX + delta.x, startY + delta.y, width, height, state.canvas.canvasImageStartPivotX, state.canvas.canvasImageStartPivotY, state.canvas.canvasImageStartRotation);
    }

    private CanvasPoint constrainedSnapOffset(int offsetX, int offsetY, int rotation) {
        String axis = CanvasTransformGizmo.moveAxisOrFree(state.canvas.canvasImageTransformAxis);
        if (CanvasTransformGizmo.AXIS_MOVE_FREE.equals(axis)) {
            return new CanvasPoint(offsetX, offsetY);
        }
        return LayerTransformEngine.axisDelta(offsetX, offsetY, rotation, axis, new LayerTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), false, false));
    }

    private void clearSnapGuides() {
        state.canvas.snapGuideXVisible = false;
        state.canvas.snapGuideYVisible = false;
    }

    private CanvasImageLayer resizeImageFromHandle(CanvasImageLayer image, int localX, int localY) {
        ResizedBox box = resizeFromSelectionBox(
                localX,
                localY,
                state.canvas.canvasImageStartX,
                state.canvas.canvasImageStartY,
                state.canvas.canvasImageStartW,
                state.canvas.canvasImageStartH,
                state.canvas.canvasImageStartRotation,
                8,
                8,
                state.canvas.canvasImageStartPivotX,
                state.canvas.canvasImageStartPivotY,
                CanvasTransformGizmo.resizeCornerX(state.canvas.canvasImageTransformAxis),
                CanvasTransformGizmo.resizeCornerY(state.canvas.canvasImageTransformAxis),
                CanvasTransformGizmo.supports(image.asset()) || TabletModifierKeys.shiftDown()
        );
        return fitAndClampImage(image.withBounds(box.x(), box.y(), box.width(), box.height()));
    }

    private CanvasTextLayer resizeTextFromHandle(CanvasTextLayer text, int localX, int localY) {
        ResizedBox box = resizeFromSelectionBox(
                localX,
                localY,
                state.canvas.canvasTextStartX,
                state.canvas.canvasTextStartY,
                state.canvas.canvasTextStartW,
                state.canvas.canvasTextStartH,
                state.canvas.canvasTextStartRotation,
                24,
                14,
                CanvasElementGeometry.defaultPivot(state.canvas.canvasTextStartW),
                CanvasElementGeometry.defaultPivot(state.canvas.canvasTextStartH),
                1,
                1,
                TabletModifierKeys.shiftDown()
        );
        return fitAndClampText(new CanvasTextLayer(text.id(), text.text(), box.x(), box.y(), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans()));
    }

    private CanvasExclusiveChoice resizeExclusiveChoiceFromHandle(CanvasExclusiveChoice ec, int localX, int localY) {
        ResizedBox box = resizeFromSelectionBox(
                localX,
                localY,
                state.canvas.canvasEcStartX,
                state.canvas.canvasEcStartY,
                state.canvas.canvasEcStartW,
                state.canvas.canvasEcStartH,
                state.canvas.canvasEcStartRotation,
                8,
                8,
                CanvasElementGeometry.defaultPivot(state.canvas.canvasEcStartW),
                CanvasElementGeometry.defaultPivot(state.canvas.canvasEcStartH),
                1,
                1,
                TabletModifierKeys.shiftDown()
        );
        return fitAndClampExclusiveChoice(new CanvasExclusiveChoice(ec.id(), box.x(), box.y(), box.width(), box.height(), ec.rotation(), ec.connectionQuestIds(), ec.prerequisiteQuestIds(), ec.background(), ec.connectionColors(), ec.connectionModes(), ec.connectionTextures(), ec.connectionTextureSpacings(), ec.hiddenConnections()));
    }

    private ResizedBox resizeFromSelectionBox(int localX, int localY, int startX, int startY, int startW, int startH, int rotation, int minW, int minH, int pivotX, int pivotY, int cornerX, int cornerY, boolean preserveAspect) {
        CanvasGeometry.ResizedBox resized = LayerTransformEngine.resizeFromCorner(new LayerTransformEngine.ResizeRequest(
                layerRect(startX, startY, startW, startH, pivotX, pivotY, rotation),
                CanvasGeometry.screenToLogicalX(state, localX),
                CanvasGeometry.screenToLogicalY(state, localY),
                minW,
                minH,
                snapSettings(),
                preserveAspect,
                cornerX,
                cornerY
        ));
        return new ResizedBox(resized.x(), resized.y(), resized.width(), resized.height());
    }

    private LayerTransformEngine.LayerRect layerRect(int x, int y, int width, int height, int pivotX, int pivotY, int rotation) {
        return new LayerTransformEngine.LayerRect(x, y, width, height, pivotX, pivotY, rotation);
    }

    private LayerTransformEngine.SnapSettings snapSettings() {
        return new LayerTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), state.canvas.gridSnapLocked, TabletModifierKeys.shiftDown());
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedImage(state, image) : image;
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }

    private CanvasExclusiveChoice fittedExclusiveChoiceIfGridLocked(CanvasExclusiveChoice ec) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedExclusiveChoice(state, ec) : ec;
    }

    private CanvasImageLayer fitAndClampImage(CanvasImageLayer image) {
        CanvasImageLayer fitted = fittedImageIfGridLocked(image);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), fitted.pivotX(), fitted.pivotY(), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    private CanvasTextLayer fitAndClampText(CanvasTextLayer text) {
        CanvasTextLayer fitted = fittedTextIfGridLocked(text);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), CanvasElementGeometry.defaultPivot(fitted.w()), CanvasElementGeometry.defaultPivot(fitted.h()), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    private CanvasExclusiveChoice fitAndClampExclusiveChoice(CanvasExclusiveChoice ec) {
        CanvasExclusiveChoice fitted = fittedExclusiveChoiceIfGridLocked(ec);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), CanvasElementGeometry.defaultPivot(fitted.w()), CanvasElementGeometry.defaultPivot(fitted.h()), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    private CanvasImageLayer clampRotationPreviewImage(CanvasImageLayer image) {
        CanvasImageLayer preview = shouldFitRotatedPreview(image.rotation())
                ? fittedImageIfGridLocked(image)
                : image;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), preview.pivotX(), preview.pivotY(), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    private CanvasTextLayer clampRotationPreviewText(CanvasTextLayer text) {
        CanvasTextLayer preview = shouldFitRotatedPreview(text.rotation())
                ? fittedTextIfGridLocked(text)
                : text;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), CanvasElementGeometry.defaultPivot(preview.w()), CanvasElementGeometry.defaultPivot(preview.h()), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    private CanvasExclusiveChoice clampRotationPreviewExclusiveChoice(CanvasExclusiveChoice ec) {
        CanvasExclusiveChoice preview = shouldFitRotatedPreview(ec.rotation())
                ? fittedExclusiveChoiceIfGridLocked(ec)
                : ec;
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, preview.x(), preview.y(), preview.w(), preview.h(), CanvasElementGeometry.defaultPivot(preview.w()), CanvasElementGeometry.defaultPivot(preview.h()), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    private boolean shouldFitRotatedPreview(int rotation) {
        return state.canvas.gridSnapLocked && TabletModifierKeys.shiftDown() && CanvasGeometry.isCardinalTurn(rotation);
    }

    private record ResizedBox(int x, int y, int width, int height) {
    }
}

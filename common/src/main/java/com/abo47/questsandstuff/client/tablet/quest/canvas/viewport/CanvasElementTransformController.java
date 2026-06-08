package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.input.TabletModifierKeys;
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
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;

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
        boolean selectedBeforeClick = image.id().equals(state.canvasSelection.primaryImageId()) || state.canvasSelection.imageIds().contains(image.id());
        if (gizmoSupported && !selectedBeforeClick) {
            CanvasTransformGizmo.setMode(state, CanvasTransformMode.MOVE);
        }
        CanvasTransformMode gizmoMode = imageTransformMode(image, localX, localY);
        state.canvasSelection.setPrimaryImageId(image.id());
        state.canvasSelection.setPrimaryTextId("");
        state.canvasSelection.imageIds().clear();
        state.canvasSelection.imageIds().add(image.id());
        state.canvasSelection.textIds().clear();
        state.canvasImageDragStartX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        state.canvasImageDragStartY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        state.canvasImageStartX = image.x();
        state.canvasImageStartY = image.y();
        state.canvasImageStartW = image.w();
        state.canvasImageStartH = image.h();
        state.canvasImageStartPivotX = image.pivotX();
        state.canvasImageStartPivotY = image.pivotY();
        state.canvasImageStartRotation = image.rotation();
        state.canvasImageStartYaw = image.entityYaw();
        state.canvasImageStartPitch = image.modelPitch();
        state.canvasImageTransformAxis = gizmoSupported
                ? CanvasTransformGizmo.axisAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), localX, localY)
                : "";
        if (gizmoMode == CanvasTransformMode.MOVE) {
            state.canvasImageTransformAxis = CanvasTransformGizmo.moveAxisOrFree(state.canvasImageTransformAxis);
        }
        state.resizingCanvasImage = gizmoMode == CanvasTransformMode.RESIZE
                || (!gizmoSupported && gizmoMode == null && CanvasRenderer.isCanvasImageResizeHandleHit(state, image, localX, localY));
        state.rotatingCanvasImage = gizmoMode == CanvasTransformMode.ROTATE
                || (!gizmoSupported && gizmoMode == null && CanvasRenderer.isCanvasImageRotateHandleHit(state, image, localX, localY));
        if (state.rotatingCanvasImage) {
            state.canvasImageRotatePivotX = CanvasElementGeometry.logicalPivotX(image.x(), image.w(), image.pivotX());
            state.canvasImageRotatePivotY = CanvasElementGeometry.logicalPivotY(image.y(), image.h(), image.pivotY());
            double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
            double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
            state.canvasImageRotateStartAngle = Math.atan2(logicalMouseY - state.canvasImageRotatePivotY, logicalMouseX - state.canvasImageRotatePivotX);
        }
        state.draggingCanvasImage = !(gizmoSupported && gizmoMode == null) && !state.resizingCanvasImage && !state.rotatingCanvasImage;
        state.canvasSelection.questIds().clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas image transform start id={} drag={} resize={} rotate={}", image.id(), state.draggingCanvasImage, state.resizingCanvasImage, state.rotatingCanvasImage);
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
        String group = TabletStateQueries.selectedGroupName(state);
        CanvasImageLayer image = findImage(group, state.canvasSelection.primaryImageId());
        if (image == null) {
            return;
        }
        int logicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int logicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        int dx = logicalX - state.canvasImageDragStartX;
        int dy = logicalY - state.canvasImageDragStartY;
        CanvasImageLayer next = image;
        if (state.draggingCanvasImage) {
            CanvasPoint anchor = CanvasTransformGizmo.supports(image.asset())
                    ? modelDragAnchor(state.canvasImageStartX, state.canvasImageStartY, state.canvasImageStartW, state.canvasImageStartH, dx, dy)
                    : dragAnchor(state.canvasImageStartX, state.canvasImageStartY, state.canvasImageStartW, state.canvasImageStartH, state.canvasImageStartPivotX, state.canvasImageStartPivotY, state.canvasImageStartRotation, dx, dy);
            next = new CanvasImageLayer(image.id(), image.asset(), anchor.x, anchor.y, state.canvasImageStartW, state.canvasImageStartH, state.canvasImageStartRotation, image.entityYaw(), image.entitySpinSpeed(), image.modelPitch(), image.pivotX(), image.pivotY());
            next = applySmartSnapToImage(next, cards, group);
        } else if (state.resizingCanvasImage) {
            clearSnapGuides();
            next = resizeImageFromHandle(image, localX, localY);
        } else if (state.rotatingCanvasImage) {
            clearSnapGuides();
            boolean modelAxisRotation = CanvasTransformGizmo.supports(image.asset()) && !CanvasTransformGizmo.AXIS_ROLL.equals(state.canvasImageTransformAxis);
            next = modelAxisRotation
                    ? rotateModelFromDrag(image, logicalX, logicalY)
                    : clampRotationPreviewImage(image.rotateTo(layerRotation(logicalX, logicalY)));
        }
        CanvasLayerMutations.putTransientCanvasImage(state, next);
    }

    private CanvasImageLayer rotateModelFromDrag(CanvasImageLayer image, int logicalX, int logicalY) {
        LayerTransformEngine.ModelRotation rotation = LayerTransformEngine.modelRotation(new LayerTransformEngine.ModelRotationRequest(
                new LayerTransformEngine.ModelRotation(state.canvasImageStartYaw, state.canvasImageStartPitch),
                CanvasTransformGizmo.AXIS_PITCH.equals(state.canvasImageTransformAxis),
                state.canvasImageRotatePivotX,
                state.canvasImageRotatePivotY,
                state.canvasImageRotateStartAngle,
                logicalX,
                logicalY,
                TabletModifierKeys.shiftDown()
        ));
        return image.withModelRotation(rotation.yaw(), rotation.pitch());
    }

    private int layerRotation(int logicalX, int logicalY) {
        return LayerTransformEngine.layerRotation(new LayerTransformEngine.RotationRequest(
                state.canvasImageStartRotation,
                state.canvasImageRotatePivotX,
                state.canvasImageRotatePivotY,
                state.canvasImageRotateStartAngle,
                logicalX,
                logicalY,
                TabletModifierKeys.shiftDown()
        ));
    }

    public CanvasImageLayer findImage(String group, String imageId) {
        if (group == null || group.isBlank() || imageId == null || imageId.isBlank()) {
            return null;
        }
        return state.canvasImagesByGroup.getOrDefault(group, List.of()).stream()
                .filter(entry -> entry.id().equals(imageId))
                .findFirst()
                .orElse(null);
    }

    public void beginTextTransform(CanvasTextLayer text, int localX, int localY) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        state.canvasSelection.setPrimaryTextId(text.id());
        state.canvasSelection.setPrimaryImageId("");
        state.canvasSelection.textIds().clear();
        state.canvasSelection.textIds().add(text.id());
        state.canvasSelection.imageIds().clear();
        state.canvasTextMenuTarget = text.id();
        state.canvasTextDragStartX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        state.canvasTextDragStartY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        state.canvasTextStartX = text.x();
        state.canvasTextStartY = text.y();
        state.canvasTextStartW = text.w();
        state.canvasTextStartH = text.h();
        state.canvasTextStartRotation = text.rotation();
        state.resizingCanvasText = CanvasRenderer.isCanvasTextResizeHandleHit(state, text, localX, localY);
        state.rotatingCanvasText = CanvasRenderer.isCanvasTextRotateHandleHit(state, text, localX, localY);
        if (state.rotatingCanvasText) {
            state.canvasTextRotatePivotX = CanvasElementGeometry.logicalPivotX(text.x(), text.w(), CanvasElementGeometry.defaultPivot(text.w()));
            state.canvasTextRotatePivotY = CanvasElementGeometry.logicalPivotY(text.y(), text.h(), CanvasElementGeometry.defaultPivot(text.h()));
            double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
            double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
            state.canvasTextRotateStartAngle = Math.atan2(logicalMouseY - state.canvasTextRotatePivotY, logicalMouseX - state.canvasTextRotatePivotX);
        }
        state.draggingCanvasText = !state.resizingCanvasText && !state.rotatingCanvasText;
        state.canvasSelection.questIds().clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text transform start id={} drag={} resize={} rotate={}", text.id(), state.draggingCanvasText, state.resizingCanvasText, state.rotatingCanvasText);
    }

    public void updateTextTransform(int localX, int localY, List<QuestCardLayout> cards) {
        String group = TabletStateQueries.selectedGroupName(state);
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, state.canvasSelection.primaryTextId());
        if (text == null) {
            return;
        }
        int logicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int logicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        int dx = logicalX - state.canvasTextDragStartX;
        int dy = logicalY - state.canvasTextDragStartY;
        CanvasTextLayer next = text;
        if (state.draggingCanvasText) {
            CanvasPoint anchor = dragAnchor(state.canvasTextStartX, state.canvasTextStartY, state.canvasTextStartW, state.canvasTextStartH, CanvasElementGeometry.defaultPivot(state.canvasTextStartW), CanvasElementGeometry.defaultPivot(state.canvasTextStartH), state.canvasTextStartRotation, dx, dy);
            next = new CanvasTextLayer(text.id(), text.text(), anchor.x, anchor.y, state.canvasTextStartW, state.canvasTextStartH, state.canvasTextStartRotation, text.align(), text.style(), text.color(), text.fontSize(), text.spans());
            next = applySmartSnapToText(next, cards, group);
        } else if (state.resizingCanvasText) {
            clearSnapGuides();
            next = resizeTextFromHandle(text, localX, localY);
        } else if (state.rotatingCanvasText) {
            clearSnapGuides();
            int angle = LayerTransformEngine.layerRotation(new LayerTransformEngine.RotationRequest(
                    state.canvasTextStartRotation,
                    state.canvasTextRotatePivotX,
                    state.canvasTextRotatePivotY,
                    state.canvasTextRotateStartAngle,
                    logicalX,
                    logicalY,
                    TabletModifierKeys.shiftDown()
            ));
            next = clampRotationPreviewText(text.rotateTo(angle));
        }
        CanvasLayerMutations.putTransientCanvasText(state, next);
    }

    private CanvasImageLayer applySmartSnapToImage(CanvasImageLayer image, List<QuestCardLayout> cards, String group) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForImage(state, image),
                cards,
                group,
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
        if (!CanvasTransformGizmo.AXIS_MOVE_FREE.equals(CanvasTransformGizmo.moveAxisOrFree(state.canvasImageTransformAxis))) {
            state.snapGuideXVisible = state.snapGuideXVisible && offsetX != 0;
            state.snapGuideYVisible = state.snapGuideYVisible && offsetY != 0;
        }
        int requestedX = image.x() + offsetX;
        int requestedY = image.y() + offsetY;
        var clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, requestedX, requestedY, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides();
        }
        return image.moveTo(clamped.x, clamped.y);
    }

    private CanvasTextLayer applySmartSnapToText(CanvasTextLayer text, List<QuestCardLayout> cards, String group) {
        CanvasSnapEngine.SnapResult snap = CanvasSmartSnapper.snap(
                state,
                CanvasSmartSnapper.boundsForText(state, text),
                cards,
                group,
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
        String axis = CanvasTransformGizmo.moveAxisOrFree(state.canvasImageTransformAxis);
        CanvasPoint delta = LayerTransformEngine.modelMoveDelta(new LayerTransformEngine.ModelMoveRequest(
                dx,
                dy,
                state.canvasImageStartRotation,
                axis,
                CanvasTransformGizmo.AXIS_MOVE_FREE.equals(axis),
                snapSettings()
        ));
        return CanvasGeometry.clampRotatedAnchorToCanvas(state, startX + delta.x, startY + delta.y, width, height, state.canvasImageStartPivotX, state.canvasImageStartPivotY, state.canvasImageStartRotation);
    }

    private CanvasPoint constrainedSnapOffset(int offsetX, int offsetY, int rotation) {
        String axis = CanvasTransformGizmo.moveAxisOrFree(state.canvasImageTransformAxis);
        if (CanvasTransformGizmo.AXIS_MOVE_FREE.equals(axis)) {
            return new CanvasPoint(offsetX, offsetY);
        }
        return LayerTransformEngine.axisDelta(offsetX, offsetY, rotation, axis, new LayerTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), false, false));
    }

    private void clearSnapGuides() {
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
    }

    private CanvasImageLayer resizeImageFromHandle(CanvasImageLayer image, int localX, int localY) {
        ResizedBox box = resizeFromSelectionBox(
                localX,
                localY,
                state.canvasImageStartX,
                state.canvasImageStartY,
                state.canvasImageStartW,
                state.canvasImageStartH,
                state.canvasImageStartRotation,
                8,
                8,
                state.canvasImageStartPivotX,
                state.canvasImageStartPivotY,
                CanvasTransformGizmo.resizeCornerX(state.canvasImageTransformAxis),
                CanvasTransformGizmo.resizeCornerY(state.canvasImageTransformAxis),
                CanvasTransformGizmo.supports(image.asset()) || TabletModifierKeys.shiftDown()
        );
        return fitAndClampImage(image.withBounds(box.x(), box.y(), box.width(), box.height()));
    }

    private CanvasTextLayer resizeTextFromHandle(CanvasTextLayer text, int localX, int localY) {
        ResizedBox box = resizeFromSelectionBox(
                localX,
                localY,
                state.canvasTextStartX,
                state.canvasTextStartY,
                state.canvasTextStartW,
                state.canvasTextStartH,
                state.canvasTextStartRotation,
                24,
                14,
                CanvasElementGeometry.defaultPivot(state.canvasTextStartW),
                CanvasElementGeometry.defaultPivot(state.canvasTextStartH),
                1,
                1,
                TabletModifierKeys.shiftDown()
        );
        return fitAndClampText(new CanvasTextLayer(text.id(), text.text(), box.x(), box.y(), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans()));
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
        return new LayerTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), state.gridSnapLocked, TabletModifierKeys.shiftDown());
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.gridSnapLocked ? CanvasGridFitController.fittedImage(state, image) : image;
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
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

    private boolean shouldFitRotatedPreview(int rotation) {
        return state.gridSnapLocked && TabletModifierKeys.shiftDown() && CanvasGeometry.isCardinalTurn(rotation);
    }

    private record ResizedBox(int x, int y, int width, int height) {
    }
}

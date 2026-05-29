package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformMode;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.List;
import java.util.Set;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

public final class CanvasElementTransformController {
    private final TabletUiState state;

    public CanvasElementTransformController(TabletUiState state) {
        this.state = state;
    }

    public void beginImageTransform(CanvasImageLayer image, int localX, int localY) {
        CanvasRenderer.clearTransientCanvasTransforms(state);
        boolean gizmoSupported = CanvasTransformGizmo.supports(image.asset());
        boolean selectedBeforeClick = image.id().equals(state.selectedCanvasImageId) || state.selectedCanvasImageIds.contains(image.id());
        if (gizmoSupported && !selectedBeforeClick) {
            CanvasTransformGizmo.setMode(state, CanvasTransformMode.MOVE);
        }
        CanvasTransformMode gizmoMode = imageTransformMode(image, localX, localY);
        state.selectedCanvasImageId = image.id();
        state.selectedCanvasTextId = "";
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasImageIds.add(image.id());
        state.selectedCanvasTextIds.clear();
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
        state.resizingCanvasImage = gizmoMode == CanvasTransformMode.RESIZE
                || (!gizmoSupported && gizmoMode == null && CanvasRenderer.isCanvasImageResizeHandleHit(state, image, localX, localY));
        state.rotatingCanvasImage = gizmoMode == CanvasTransformMode.ROTATE
                || (!gizmoSupported && gizmoMode == null && CanvasRenderer.isCanvasImageRotateHandleHit(state, image, localX, localY));
        if (state.rotatingCanvasImage) {
            state.canvasImageRotatePivotX = image.x() + image.pivotX();
            state.canvasImageRotatePivotY = image.y() + image.pivotY();
            double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
            double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
            state.canvasImageRotateStartAngle = Math.atan2(logicalMouseY - state.canvasImageRotatePivotY, logicalMouseX - state.canvasImageRotatePivotX);
        }
        state.draggingCanvasImage = !(gizmoSupported && gizmoMode == null) && !state.resizingCanvasImage && !state.rotatingCanvasImage;
        state.selectedQuestIds.clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas image transform start id={} drag={} resize={} rotate={}", image.id(), state.draggingCanvasImage, state.resizingCanvasImage, state.rotatingCanvasImage);
    }

    private CanvasTransformMode imageTransformMode(CanvasImageLayer image, int localX, int localY) {
        if (!CanvasTransformGizmo.supports(image.asset())) {
            return null;
        }
        if (CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.RESIZE
                && isShiftDown()
                && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY)) {
            return CanvasTransformMode.MOVE;
        }
        CanvasTransformMode hitMode = CanvasTransformGizmo.modeAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), localX, localY);
        if (hitMode != null) {
            return hitMode;
        }
        if (CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.MOVE
                && isShiftDown()
                && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY)) {
            return CanvasTransformMode.MOVE;
        }
        return null;
    }

    public void updateImageTransform(int localX, int localY, List<QuestCardLayout> cards) {
        String group = TabletUiFactory.selectedGroupName(state);
        CanvasImageLayer image = findImage(group, state.selectedCanvasImageId);
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
                    : dragAnchor(state.canvasImageStartX, state.canvasImageStartY, state.canvasImageStartW, state.canvasImageStartH, dx, dy);
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
                    : fittedImageIfGridLocked(image.rotateTo(layerRotation(logicalX, logicalY)));
        }
        CanvasRenderer.putTransientCanvasImage(state, next);
    }

    private CanvasImageLayer rotateModelFromDrag(CanvasImageLayer image, int logicalX, int logicalY) {
        int delta = rotationDelta(logicalX, logicalY);
        int yaw = state.canvasImageStartYaw;
        int pitch = state.canvasImageStartPitch;
        if (CanvasTransformGizmo.AXIS_PITCH.equals(state.canvasImageTransformAxis)) {
            pitch = state.canvasImageStartPitch + delta;
        } else {
            yaw = state.canvasImageStartYaw + delta;
        }
        if (isShiftDown()) {
            yaw = snapAngle(yaw);
            pitch = snapAngle(pitch);
        }
        return image.withModelRotation(yaw, pitch);
    }

    private int layerRotation(int logicalX, int logicalY) {
        int angle = state.canvasImageStartRotation + rotationDelta(logicalX, logicalY);
        return isShiftDown() ? snapAngle(angle) : angle;
    }

    private int rotationDelta(int logicalX, int logicalY) {
        double currentAngle = Math.atan2(logicalY - state.canvasImageRotatePivotY, logicalX - state.canvasImageRotatePivotX);
        double deltaDegrees = Math.toDegrees(currentAngle - state.canvasImageRotateStartAngle);
        while (deltaDegrees > 180.0D) {
            deltaDegrees -= 360.0D;
        }
        while (deltaDegrees < -180.0D) {
            deltaDegrees += 360.0D;
        }
        return (int) Math.round(deltaDegrees);
    }

    private int snapAngle(int angle) {
        return Math.round(angle / 15.0f) * 15;
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
        CanvasRenderer.clearTransientCanvasTransforms(state);
        state.selectedCanvasTextId = text.id();
        state.selectedCanvasImageId = "";
        state.selectedCanvasTextIds.clear();
        state.selectedCanvasTextIds.add(text.id());
        state.selectedCanvasImageIds.clear();
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
        state.selectedQuestIds.clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text transform start id={} drag={} resize={} rotate={}", text.id(), state.draggingCanvasText, state.resizingCanvasText, state.rotatingCanvasText);
    }

    public void updateTextTransform(int localX, int localY, List<QuestCardLayout> cards) {
        String group = TabletUiFactory.selectedGroupName(state);
        CanvasTextLayer text = CanvasRenderer.findCanvasText(state, group, state.selectedCanvasTextId);
        if (text == null) {
            return;
        }
        int logicalX = CanvasGeometry.screenToNearestLogicalX(state, localX);
        int logicalY = CanvasGeometry.screenToNearestLogicalY(state, localY);
        int dx = logicalX - state.canvasTextDragStartX;
        int dy = logicalY - state.canvasTextDragStartY;
        CanvasTextLayer next = text;
        if (state.draggingCanvasText) {
            CanvasPoint anchor = dragAnchor(state.canvasTextStartX, state.canvasTextStartY, state.canvasTextStartW, state.canvasTextStartH, dx, dy);
            next = new CanvasTextLayer(text.id(), text.text(), anchor.x, anchor.y, state.canvasTextStartW, state.canvasTextStartH, state.canvasTextStartRotation, text.align(), text.style(), text.color(), text.fontSize(), text.spans());
            next = applySmartSnapToText(next, cards, group);
        } else if (state.resizingCanvasText) {
            clearSnapGuides();
            next = resizeTextFromHandle(text, localX, localY);
        } else if (state.rotatingCanvasText) {
            clearSnapGuides();
            double currentAngle = Math.atan2(logicalY - state.canvasTextRotatePivotY, logicalX - state.canvasTextRotatePivotX);
            double deltaDegrees = Math.toDegrees(currentAngle - state.canvasTextRotateStartAngle);
            int angle = state.canvasTextStartRotation + (int) Math.round(deltaDegrees);
            if (isShiftDown()) {
                angle = Math.round(angle / 15.0f) * 15;
            }
            next = fittedTextIfGridLocked(text.rotateTo(angle));
        }
        CanvasRenderer.putTransientCanvasText(state, next);
    }

    private CanvasImageLayer applySmartSnapToImage(CanvasImageLayer image, List<QuestCardLayout> cards, String group) {
        CanvasSmartSnapper.SnapResult snap = CanvasSmartSnapper.snap(
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
        int offsetX = snap.offsetX();
        int offsetY = snap.offsetY();
        if (CanvasTransformGizmo.AXIS_MOVE_X.equals(state.canvasImageTransformAxis)) {
            offsetY = 0;
            state.snapGuideYVisible = false;
        } else if (CanvasTransformGizmo.AXIS_MOVE_Y.equals(state.canvasImageTransformAxis)) {
            offsetX = 0;
            state.snapGuideXVisible = false;
        }
        int requestedX = image.x() + offsetX;
        int requestedY = image.y() + offsetY;
        var clamped = CanvasGeometry.clampAnchorToCanvas(state, requestedX, requestedY, image.w(), image.h());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides();
        }
        return image.moveTo(clamped.x, clamped.y);
    }

    private CanvasTextLayer applySmartSnapToText(CanvasTextLayer text, List<QuestCardLayout> cards, String group) {
        CanvasSmartSnapper.SnapResult snap = CanvasSmartSnapper.snap(
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
        var clamped = CanvasGeometry.clampAnchorToCanvas(state, requestedX, requestedY, text.w(), text.h());
        if (clamped.x != requestedX || clamped.y != requestedY) {
            clearSnapGuides();
        }
        return text.moveTo(clamped.x, clamped.y);
    }

    private CanvasPoint dragAnchor(int startX, int startY, int width, int height, int dx, int dy) {
        CanvasPoint delta = dragDelta(dx, dy);
        return CanvasGeometry.clampAnchorToCanvas(state, startX + delta.x, startY + delta.y, width, height);
    }

    private CanvasPoint modelDragAnchor(int startX, int startY, int width, int height, int dx, int dy) {
        CanvasPoint delta = modelDragDelta(dx, dy);
        return CanvasGeometry.clampAnchorToCanvas(state, startX + delta.x, startY + delta.y, width, height);
    }

    private CanvasPoint modelDragDelta(int dx, int dy) {
        if (isShiftDown() || CanvasTransformGizmo.AXIS_MOVE_FREE.equals(state.canvasImageTransformAxis)) {
            return freeDragDelta(dx, dy);
        }
        if (CanvasTransformGizmo.AXIS_MOVE_X.equals(state.canvasImageTransformAxis)) {
            return dragDelta(dx, 0);
        }
        if (CanvasTransformGizmo.AXIS_MOVE_Y.equals(state.canvasImageTransformAxis)) {
            return dragDelta(0, dy);
        }
        return dragDelta(dx, dy);
    }

    private CanvasPoint freeDragDelta(int dx, int dy) {
        if (!state.gridSnapLocked) {
            return new CanvasPoint(dx, dy);
        }
        return new CanvasPoint(snapDelta(dx), snapDelta(dy));
    }

    private CanvasPoint dragDelta(int dx, int dy) {
        if (!state.gridSnapLocked && !isShiftDown()) {
            return new CanvasPoint(dx, dy);
        }
        return new CanvasPoint(snapDelta(dx), snapDelta(dy));
    }

    private int snapDelta(int delta) {
        int grid = CanvasGeometry.gridSize(state);
        return Math.round((float) delta / (float) grid) * grid;
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
                CanvasTransformGizmo.supports(image.asset()) || isShiftDown()
        );
        return fittedImageIfGridLocked(image.withBounds(box.x(), box.y(), box.width(), box.height()));
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
                isShiftDown()
        );
        return fittedTextIfGridLocked(new CanvasTextLayer(text.id(), text.text(), box.x(), box.y(), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans()));
    }

    private ResizedBox resizeFromSelectionBox(int localX, int localY, int startX, int startY, int startW, int startH, int rotation, int minW, int minH, int pivotX, int pivotY, int cornerX, int cornerY, boolean preserveAspect) {
        CanvasGeometry.ResizedBox resized = CanvasGeometry.resizeRotatedFromCornerAtPivot(
                CanvasGeometry.screenToLogicalX(state, localX),
                CanvasGeometry.screenToLogicalY(state, localY),
                startX,
                startY,
                startW,
                startH,
                pivotX,
                pivotY,
                rotation,
                minW,
                minH,
                CanvasGeometry.gridSize(state),
                (state.gridSnapLocked || isShiftDown()) && CanvasGeometry.isCardinalTurn(rotation),
                preserveAspect,
                cornerX,
                cornerY
        );
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, resized.x(), resized.y(), resized.width(), resized.height());
        return new ResizedBox(clamped.x, clamped.y, resized.width(), resized.height());
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.gridSnapLocked && CanvasGeometry.isCardinalTurn(image.rotation()) ? CanvasGridFitController.fittedImage(state, image) : image;
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.gridSnapLocked && CanvasGeometry.isCardinalTurn(text.rotation()) ? CanvasGridFitController.fittedText(state, text) : text;
    }

    private record ResizedBox(int x, int y, int width, int height) {
    }
}

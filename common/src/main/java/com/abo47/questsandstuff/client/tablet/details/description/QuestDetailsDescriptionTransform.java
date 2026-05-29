package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.canvas.selection.CanvasGroupResizeTransform;
import com.abo47.questsandstuff.client.canvas.selection.CanvasLayerGroupTransform;
import com.abo47.questsandstuff.client.canvas.selection.CanvasLayerSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.Map;
import java.util.function.IntSupplier;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

public final class QuestDetailsDescriptionTransform {
    private final TabletUiState state;
    private final IntSupplier contentX;
    private final IntSupplier contentY;
    private final IntSupplier contentW;
    private final IntSupplier contentH;

    QuestDetailsDescriptionTransform(TabletUiState state, IntSupplier contentX, IntSupplier contentY, IntSupplier contentW, IntSupplier contentH) {
        this.state = state;
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentW = contentW;
        this.contentH = contentH;
    }

    void beginTransform(QuestDetailsDescriptionModel model, String kind, String id, ElementRect rect, boolean selectionMove, boolean resizeHit, boolean rotateHit, int lx, int visibleY, int ly) {
        if (selectionMove) {
            beginSelectionTransform(model, lx, visibleY, "move");
            return;
        }
        CanvasRenderer.clearTransientQuestDetailsTransforms(state);
        state.questDetailsTransformKind = kind;
        state.questDetailsTransformId = id;
        state.questDetailsTransformStartMouseX = contentX.getAsInt() + lx;
        state.questDetailsTransformStartMouseY = contentY.getAsInt() + ly - state.questDetailsDescScroll;
        state.questDetailsTransformStartX = rect.x();
        state.questDetailsTransformStartY = rect.y();
        state.questDetailsTransformStartW = rect.w();
        state.questDetailsTransformStartH = rect.h();
        state.questDetailsTransformStartRotation = rect.rotation();
        CanvasImageLayer image = "desc_image".equals(kind) ? model.image(id) : null;
        int pivotX = image == null ? CanvasElementGeometry.defaultPivot(rect.w()) : image.pivotX();
        int pivotY = image == null ? CanvasElementGeometry.defaultPivot(rect.h()) : image.pivotY();
        state.questDetailsTransformStartPivotX = pivotX;
        state.questDetailsTransformStartPivotY = pivotY;
        state.questDetailsTransformStartYaw = image == null ? CanvasImageLayer.DEFAULT_ENTITY_YAW : image.entityYaw();
        state.questDetailsTransformStartPitch = image == null ? CanvasImageLayer.DEFAULT_MODEL_PITCH : image.modelPitch();
        state.questDetailsTransformPivotX = contentX.getAsInt() + CanvasElementGeometry.logicalPivotX(rect.x(), rect.w(), pivotX);
        state.questDetailsTransformPivotY = contentY.getAsInt() - state.questDetailsDescScroll + CanvasElementGeometry.logicalPivotY(rect.y(), rect.h(), pivotY);
        if (resizeHit) {
            state.questDetailsTransformMode = "resize";
        } else if (rotateHit) {
            state.questDetailsTransformMode = "rotate";
            state.questDetailsTransformStartAngle = Math.atan2(state.questDetailsTransformStartMouseY - state.questDetailsTransformPivotY, state.questDetailsTransformStartMouseX - state.questDetailsTransformPivotX);
        } else {
            state.questDetailsTransformMode = "move";
        }
    }

    void beginSelectionTransform(QuestDetailsDescriptionModel model, int lx, int visibleY) {
        beginSelectionTransform(model, lx, visibleY, "move");
    }

    void beginSelectionTransform(QuestDetailsDescriptionModel model, int lx, int visibleY, String mode) {
        CanvasRenderer.clearTransientQuestDetailsTransforms(state);
        state.questDetailsTransformKind = "selection";
        state.questDetailsTransformId = "selection";
        state.questDetailsTransformMode = mode == null || mode.isBlank() ? "move" : mode;
        state.questDetailsTransformStartMouseX = contentX.getAsInt() + lx;
        state.questDetailsTransformStartMouseY = contentY.getAsInt() + visibleY;
        state.dragStartTextPositions.clear();
        state.dragStartImagePositions.clear();
        state.resizeStartImageLayers.clear();
        state.resizeStartTextLayers.clear();
        state.rotateStartImageLayers.clear();
        state.rotateStartTextLayers.clear();
        for (CanvasTextLayer text : model.texts.values()) {
            if (isSelectedText(text.id())) {
                state.dragStartTextPositions.put(text.id(), new CanvasPoint(text.x(), text.y()));
            }
        }
        for (CanvasImageLayer image : model.images.values()) {
            if (isSelectedImage(image.id())) {
                state.dragStartImagePositions.put(image.id(), new CanvasPoint(image.x(), image.y()));
            }
        }
        CanvasLayerSelectionSnapshot snapshot = selectedLayerSnapshot(model);
        state.resizeStartLeft = snapshot.left();
        state.resizeStartTop = snapshot.top();
        state.resizeStartRight = snapshot.right();
        state.resizeStartBottom = snapshot.bottom();
        state.resizeStartImageLayers.putAll(snapshot.images());
        state.resizeStartTextLayers.putAll(snapshot.texts());
        state.rotateStartBoundsLeft = snapshot.left();
        state.rotateStartBoundsTop = snapshot.top();
        state.rotateStartBoundsRight = snapshot.right();
        state.rotateStartBoundsBottom = snapshot.bottom();
        state.rotateStartImageLayers.putAll(snapshot.images());
        state.rotateStartTextLayers.putAll(snapshot.texts());
        state.rotatePivotX = (snapshot.left() + snapshot.right()) / 2.0;
        state.rotatePivotY = (snapshot.top() + snapshot.bottom()) / 2.0;
        state.rotateStartAngle = Math.atan2(visibleY + state.questDetailsDescScroll - state.rotatePivotY, lx - state.rotatePivotX);
        state.rotatePreviewAngle = 0.0;
    }

    void applyTransform(QuestDetailsDescriptionModel model, int mouseX, int mouseY) {
        String id = state.questDetailsTransformId;
        if (id.isBlank()) {
            return;
        }
        int dx = mouseX - state.questDetailsTransformStartMouseX;
        int dy = mouseY - state.questDetailsTransformStartMouseY;
        if ("desc_text".equals(state.questDetailsTransformKind)) {
            CanvasTextLayer text = model.text(id);
            if (text != null) {
                model.putText(transformText(model, text, dx, dy, mouseX, mouseY));
            }
        } else if ("desc_image".equals(state.questDetailsTransformKind)) {
            CanvasImageLayer image = model.image(id);
            if (image != null) {
                model.putImage(transformImage(model, image, dx, dy, mouseX, mouseY));
            }
        } else if ("selection".equals(state.questDetailsTransformKind)) {
            applySelectionTransform(model, dx, dy, mouseX, mouseY);
        }
    }

    private void applySelectionTransform(QuestDetailsDescriptionModel model, int dx, int dy, int mouseX, int mouseY) {
        if ("resize".equals(state.questDetailsTransformMode)) {
            applySelectionResize(model, mouseX, mouseY);
            return;
        }
        if ("rotate".equals(state.questDetailsTransformMode)) {
            applySelectionRotate(model, mouseX, mouseY);
            return;
        }
        int snappedDx = snapDelta(dx);
        int snappedDy = snapDelta(dy);
        for (Map.Entry<String, CanvasPoint> entry : state.dragStartTextPositions.entrySet()) {
            CanvasTextLayer text = model.text(entry.getKey());
            if (text != null) {
                model.putText(text.moveTo(clampX(entry.getValue().x + snappedDx, text.w()), clampY(entry.getValue().y + snappedDy, text.h())));
            }
        }
        for (Map.Entry<String, CanvasPoint> entry : state.dragStartImagePositions.entrySet()) {
            CanvasImageLayer image = model.image(entry.getKey());
            if (image != null) {
                model.putImage(image.moveTo(clampX(entry.getValue().x + snappedDx, image.w()), clampY(entry.getValue().y + snappedDy, image.h())));
            }
        }
    }

    private void applySelectionResize(QuestDetailsDescriptionModel model, int mouseX, int mouseY) {
        int logicalMouseX = mouseX - contentX.getAsInt();
        int logicalMouseY = mouseY - contentY.getAsInt() + state.questDetailsDescScroll;
        CanvasGroupResizeTransform.Result result = CanvasGroupResizeTransform.resizeBottomRight(
                activeLayerSnapshot(
                        state.resizeStartLeft,
                        state.resizeStartTop,
                        state.resizeStartRight,
                        state.resizeStartBottom,
                        state.resizeStartImageLayers,
                        state.resizeStartTextLayers
                ),
                logicalMouseX,
                logicalMouseY,
                selectionResizeConstraints()
        );
        applyLayerTransformResult(model, result);
    }

    private void applySelectionRotate(QuestDetailsDescriptionModel model, int mouseX, int mouseY) {
        double logicalMouseX = mouseX - contentX.getAsInt();
        double logicalMouseY = mouseY - contentY.getAsInt() + state.questDetailsDescScroll;
        double currentAngle = Math.atan2(logicalMouseY - state.rotatePivotY, logicalMouseX - state.rotatePivotX);
        double delta = currentAngle - state.rotateStartAngle;
        if (isShiftDown()) {
            double snap = Math.PI / 12.0;
            delta = Math.round(delta / snap) * snap;
        }
        state.rotatePreviewAngle = delta;
        CanvasLayerGroupTransform.Result result = CanvasLayerGroupTransform.rotate(
                activeLayerSnapshot(
                        state.rotateStartBoundsLeft,
                        state.rotateStartBoundsTop,
                        state.rotateStartBoundsRight,
                        state.rotateStartBoundsBottom,
                        state.rotateStartImageLayers,
                        state.rotateStartTextLayers
                ),
                state.rotatePivotX,
                state.rotatePivotY,
                delta,
                state.questDetailsGridSnapLocked || isShiftDown(),
                CanvasGeometry.gridSize(state),
                this::clampLayer
        );
        applyLayerTransformResult(model, result);
    }

    private CanvasLayerSelectionSnapshot activeLayerSnapshot(int left, int top, int right, int bottom, Map<String, CanvasImageLayer> images, Map<String, CanvasTextLayer> texts) {
        return new CanvasLayerSelectionSnapshot(
                left,
                top,
                right,
                bottom,
                images,
                texts
        );
    }

    private CanvasPoint clampLayer(int x, int y, int width, int height) {
        return new CanvasPoint(clampX(x, width), clampY(y, height));
    }

    private CanvasGroupResizeTransform.Constraints selectionResizeConstraints() {
        return new CanvasGroupResizeTransform.Constraints(
                4,
                4,
                CanvasGeometry.gridSize(state),
                state.questDetailsGridSnapLocked || isShiftDown(),
                isShiftDown(),
                0,
                0,
                state.questDetailsCanvasLocked ? contentW.getAsInt() : CanvasGroupResizeTransform.UNBOUNDED,
                state.questDetailsCanvasLocked ? state.questDetailsDescScroll + contentH.getAsInt() : CanvasGroupResizeTransform.UNBOUNDED
        );
    }

    private static void applyLayerTransformResult(QuestDetailsDescriptionModel model, CanvasGroupResizeTransform.Result result) {
        for (CanvasImageLayer image : result.images().values()) {
            model.putImage(image);
        }
        for (CanvasTextLayer text : result.texts().values()) {
            model.putText(text);
        }
    }

    private static void applyLayerTransformResult(QuestDetailsDescriptionModel model, CanvasLayerGroupTransform.Result result) {
        for (CanvasImageLayer image : result.images().values()) {
            model.putImage(image);
        }
        for (CanvasTextLayer text : result.texts().values()) {
            model.putText(text);
        }
    }

    private CanvasLayerSelectionSnapshot selectedLayerSnapshot(QuestDetailsDescriptionModel model) {
        return CanvasLayerSelectionSnapshot.capture(
                QuestDetailsDescriptionSelectionState.selectedImageIds(state),
                QuestDetailsDescriptionSelectionState.selectedTextIds(state),
                model.images.values(),
                model.texts.values()
        );
    }

    private CanvasTextLayer transformText(QuestDetailsDescriptionModel model, CanvasTextLayer text, int dx, int dy, int mouseX, int mouseY) {
        return switch (state.questDetailsTransformMode) {
            case "resize" -> resizedText(model, text, mouseX, mouseY, 1, 1);
            case "rotate" -> fittedTextIfGridLocked(text.rotateTo(rotation(mouseX, mouseY)));
            default -> {
                int x = snap(state.questDetailsTransformStartX + dx);
                int y = snap(state.questDetailsTransformStartY + dy);
                SnapMove snapped = snapMove(model, text.id(), x, y, text.w(), text.h(), CanvasElementGeometry.defaultPivot(text.w()), CanvasElementGeometry.defaultPivot(text.h()), text.rotation());
                yield text.moveTo(clampX(snapped.x(), text.w()), clampY(snapped.y(), text.h()));
            }
        };
    }

    private CanvasImageLayer transformImage(QuestDetailsDescriptionModel model, CanvasImageLayer image, int dx, int dy, int mouseX, int mouseY) {
        return switch (state.questDetailsTransformMode) {
            case "resize" -> resizedImage(model, image, mouseX, mouseY);
            case "rotate" -> CanvasTransformGizmo.supports(image.asset()) && !CanvasTransformGizmo.AXIS_ROLL.equals(state.questDetailsTransformAxis) ? rotateModelFromDrag(image, mouseX, mouseY) : fittedImageIfGridLocked(image.rotateTo(rotation(mouseX, mouseY)));
            default -> {
                CanvasPoint delta = CanvasTransformGizmo.supports(image.asset()) ? modelDragDelta(dx, dy) : new CanvasPoint(snapDelta(dx), snapDelta(dy));
                int x = state.questDetailsTransformStartX + delta.x;
                int y = state.questDetailsTransformStartY + delta.y;
                SnapMove snapped = snapMove(model, image.id(), x, y, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
                if (CanvasTransformGizmo.AXIS_MOVE_X.equals(state.questDetailsTransformAxis)) {
                    snapped = new SnapMove(snapped.x(), y);
                    state.snapGuideYVisible = false;
                } else if (CanvasTransformGizmo.AXIS_MOVE_Y.equals(state.questDetailsTransformAxis)) {
                    snapped = new SnapMove(x, snapped.y());
                    state.snapGuideXVisible = false;
                }
                yield image.moveTo(clampX(snapped.x(), image.w()), clampY(snapped.y(), image.h()));
            }
        };
    }

    private CanvasPoint modelDragDelta(int dx, int dy) {
        if (isShiftDown() || CanvasTransformGizmo.AXIS_MOVE_FREE.equals(state.questDetailsTransformAxis)) {
            return new CanvasPoint(gridDelta(dx), gridDelta(dy));
        }
        if (CanvasTransformGizmo.AXIS_MOVE_X.equals(state.questDetailsTransformAxis)) {
            return new CanvasPoint(snapDelta(dx), 0);
        }
        if (CanvasTransformGizmo.AXIS_MOVE_Y.equals(state.questDetailsTransformAxis)) {
            return new CanvasPoint(0, snapDelta(dy));
        }
        return new CanvasPoint(snapDelta(dx), snapDelta(dy));
    }

    private int gridDelta(int delta) {
        if (!state.questDetailsGridSnapLocked) {
            return delta;
        }
        int step = Math.max(1, CanvasGeometry.gridSize(state));
        return Math.round((float) delta / (float) step) * step;
    }

    private CanvasImageLayer rotateModelFromDrag(CanvasImageLayer image, int mouseX, int mouseY) {
        int delta = rotationDelta(mouseX, mouseY);
        int yaw = state.questDetailsTransformStartYaw;
        int pitch = state.questDetailsTransformStartPitch;
        if (CanvasTransformGizmo.AXIS_PITCH.equals(state.questDetailsTransformAxis)) {
            pitch = state.questDetailsTransformStartPitch + delta;
        } else {
            yaw = state.questDetailsTransformStartYaw + delta;
        }
        if (isShiftDown()) {
            yaw = snapAngle(yaw);
            pitch = snapAngle(pitch);
        }
        return image.withModelRotation(yaw, pitch);
    }

    private CanvasTextLayer resizedText(QuestDetailsDescriptionModel model, CanvasTextLayer text, int mouseX, int mouseY, int cornerX, int cornerY) {
        CanvasGeometry.ResizedBox box = resizedBox(model, mouseX, mouseY, 24, 14, state.questDetailsTransformStartPivotX, state.questDetailsTransformStartPivotY, cornerX, cornerY, isShiftDown());
        return fittedTextIfGridLocked(new CanvasTextLayer(text.id(), text.text(), clampX(box.x(), box.width()), clampY(box.y(), box.height()), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans()));
    }

    private CanvasImageLayer resizedImage(QuestDetailsDescriptionModel model, CanvasImageLayer image, int mouseX, int mouseY) {
        boolean gizmoSupported = CanvasTransformGizmo.supports(image.asset());
        int cornerX = gizmoSupported ? CanvasTransformGizmo.resizeCornerX(state.questDetailsTransformAxis) : 1;
        int cornerY = gizmoSupported ? CanvasTransformGizmo.resizeCornerY(state.questDetailsTransformAxis) : 1;
        CanvasGeometry.ResizedBox box = resizedBox(model, mouseX, mouseY, 8, 8, state.questDetailsTransformStartPivotX, state.questDetailsTransformStartPivotY, cornerX, cornerY, gizmoSupported || isShiftDown());
        return fittedImageIfGridLocked(image.withBounds(clampX(box.x(), box.width()), clampY(box.y(), box.height()), box.width(), box.height()));
    }

    private CanvasGeometry.ResizedBox resizedBox(QuestDetailsDescriptionModel model, int mouseX, int mouseY, int minW, int minH, int pivotX, int pivotY, int cornerX, int cornerY, boolean preserveAspect) {
        return CanvasGeometry.resizeRotatedFromCornerAtPivot(
                mouseX - contentX.getAsInt(),
                mouseY - contentY.getAsInt() + state.questDetailsDescScroll,
                state.questDetailsTransformStartX,
                state.questDetailsTransformStartY,
                state.questDetailsTransformStartW,
                state.questDetailsTransformStartH,
                pivotX,
                pivotY,
                state.questDetailsTransformStartRotation,
                minW,
                minH,
                CanvasGeometry.gridSize(state),
                (state.questDetailsGridSnapLocked || isShiftDown()) && CanvasGeometry.isCardinalTurn(state.questDetailsTransformStartRotation),
                preserveAspect,
                cornerX,
                cornerY
        );
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.questDetailsGridSnapLocked && CanvasGeometry.isCardinalTurn(text.rotation()) ? QuestDetailsDescriptionLayout.fittedText(state, text) : text;
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.questDetailsGridSnapLocked && CanvasGeometry.isCardinalTurn(image.rotation()) ? QuestDetailsDescriptionLayout.fittedImage(state, image) : image;
    }

    private int rotation(int mouseX, int mouseY) {
        int rotation = state.questDetailsTransformStartRotation + rotationDelta(mouseX, mouseY);
        return isShiftDown() ? snapAngle(rotation) : rotation;
    }

    private int rotationDelta(int mouseX, int mouseY) {
        double angle = Math.atan2(mouseY - state.questDetailsTransformPivotY, mouseX - state.questDetailsTransformPivotX);
        double deltaDegrees = Math.toDegrees(angle - state.questDetailsTransformStartAngle);
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

    private int snap(int value) {
        if (!state.questDetailsGridSnapLocked && !isShiftDown()) {
            return value;
        }
        int step = Math.max(1, CanvasGeometry.gridSize(state));
        return Math.round((float) value / (float) step) * step;
    }

    private int snapDelta(int delta) {
        if (!state.questDetailsGridSnapLocked && !isShiftDown()) {
            return delta;
        }
        int step = Math.max(1, CanvasGeometry.gridSize(state));
        return Math.round((float) delta / (float) step) * step;
    }

    private int clampX(int x, int w) {
        if (!state.questDetailsCanvasLocked) {
            return Math.max(0, x);
        }
        return Math.max(0, Math.min(Math.max(0, contentW.getAsInt() - Math.max(1, w)), x));
    }

    private int clampY(int y, int h) {
        if (!state.questDetailsCanvasLocked) {
            return Math.max(0, y);
        }
        int maxY = Math.max(0, state.questDetailsDescScroll + contentH.getAsInt() - Math.max(1, h));
        return Math.max(0, Math.min(maxY, y));
    }

    private SnapMove snapMove(QuestDetailsDescriptionModel model, String movingId, int x, int y, int w, int h, int pivotX, int pivotY, int rotation) {
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
        if (!state.questDetailsCenterSnapXEnabled && !state.questDetailsCenterSnapYEnabled && !state.questDetailsObjectSnapEnabled) {
            return new SnapMove(x, y);
        }
        int threshold = Math.max(5, (CanvasGeometry.gridSize(state) + 1) / 2);
        SnapChoice xChoice = SnapChoice.empty(threshold);
        SnapChoice yChoice = SnapChoice.empty(threshold);
        Bounds moving = bounds(x, y, w, h, pivotX, pivotY, rotation);
        if (state.questDetailsObjectSnapEnabled) {
            for (CanvasTextLayer text : model.texts.values()) {
                if (!text.id().equals(movingId)) {
                    Bounds target = bounds(text.x(), text.y(), text.w(), text.h(), CanvasElementGeometry.defaultPivot(text.w()), CanvasElementGeometry.defaultPivot(text.h()), text.rotation());
                    xChoice = bestX(xChoice, moving, target);
                    yChoice = bestY(yChoice, moving, target);
                }
            }
            for (CanvasImageLayer image : model.images.values()) {
                if (!image.id().equals(movingId)) {
                    Bounds target = bounds(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
                    xChoice = bestX(xChoice, moving, target);
                    yChoice = bestY(yChoice, moving, target);
                }
            }
        }
        if (state.questDetailsCenterSnapXEnabled) {
            int centerX = contentW.getAsInt() / 2;
            xChoice = bestX(xChoice, moving, new Bounds(centerX, state.questDetailsDescScroll, centerX, state.questDetailsDescScroll + contentH.getAsInt()));
        }
        if (state.questDetailsCenterSnapYEnabled) {
            int centerY = state.questDetailsDescScroll + contentH.getAsInt() / 2;
            yChoice = bestY(yChoice, moving, new Bounds(0, centerY, contentW.getAsInt(), centerY));
        }
        if (xChoice.valid()) {
            state.snapGuideXVisible = true;
            state.snapGuideX = xChoice.target();
        }
        if (yChoice.valid()) {
            state.snapGuideYVisible = true;
            state.snapGuideY = yChoice.target() - state.questDetailsDescScroll;
        }
        return new SnapMove(x + (xChoice.valid() ? xChoice.offset() : 0), y + (yChoice.valid() ? yChoice.offset() : 0));
    }

    private static Bounds bounds(int x, int y, int w, int h, int pivotX, int pivotY, int rotation) {
        int[] box = CanvasElementGeometry.logicalBoundsAtPivot(x, y, w, h, pivotX, pivotY, rotation);
        return new Bounds(box[0], box[1], box[2], box[3]);
    }

    private boolean isSelectedText(String id) {
        return id.equals(state.questDetailsSelectedTextId) || state.questDetailsSelectedTextIds.contains(id);
    }

    private boolean isSelectedImage(String id) {
        return id.equals(state.questDetailsSelectedImageId) || state.questDetailsSelectedImageIds.contains(id);
    }

    private static SnapChoice bestX(SnapChoice current, Bounds moving, Bounds target) {
        SnapChoice best = current;
        best = bestOffset(best, moving.left(), target.left());
        best = bestOffset(best, moving.left(), target.centerX());
        best = bestOffset(best, moving.left(), target.right());
        best = bestOffset(best, moving.centerX(), target.left());
        best = bestOffset(best, moving.centerX(), target.centerX());
        best = bestOffset(best, moving.centerX(), target.right());
        best = bestOffset(best, moving.right(), target.left());
        best = bestOffset(best, moving.right(), target.centerX());
        return bestOffset(best, moving.right(), target.right());
    }

    private static SnapChoice bestY(SnapChoice current, Bounds moving, Bounds target) {
        SnapChoice best = current;
        best = bestOffset(best, moving.top(), target.top());
        best = bestOffset(best, moving.top(), target.centerY());
        best = bestOffset(best, moving.top(), target.bottom());
        best = bestOffset(best, moving.centerY(), target.top());
        best = bestOffset(best, moving.centerY(), target.centerY());
        best = bestOffset(best, moving.centerY(), target.bottom());
        best = bestOffset(best, moving.bottom(), target.top());
        best = bestOffset(best, moving.bottom(), target.centerY());
        return bestOffset(best, moving.bottom(), target.bottom());
    }

    private static SnapChoice bestOffset(SnapChoice current, int moving, int target) {
        int offset = target - moving;
        int distance = Math.abs(offset);
        if (distance > current.threshold() || distance >= current.distance()) {
            return current;
        }
        return new SnapChoice(offset, distance, target, current.threshold());
    }

    record ElementRect(int x, int y, int w, int h, int rotation) {
    }

    private record Bounds(int left, int top, int right, int bottom) {
        int centerX() {
            return (left + right) / 2;
        }

        int centerY() {
            return (top + bottom) / 2;
        }
    }

    private record SnapChoice(int offset, int distance, int target, int threshold) {
        static SnapChoice empty(int threshold) {
            return new SnapChoice(0, threshold + 1, 0, threshold);
        }

        boolean valid() {
            return distance <= threshold;
        }
    }

    private record SnapMove(int x, int y) {
    }
}

package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.input.TabletModifierKeys;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasGroupResizeTransform;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerGroupTransform;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapBounds;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.quest.canvas.transform.LayerTransformEngine;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsMouse;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

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
        CanvasTransformSessions.clearQuestDetailsSession(state);
        state.questDetails.questDetailsTransformKind = kind;
        state.questDetails.questDetailsTransformId = id;
        int screenContentX = screenContentX();
        int screenContentY = screenContentY();
        state.questDetails.questDetailsTransformStartMouseX = screenContentX + lx;
        state.questDetails.questDetailsTransformStartMouseY = screenContentY + ly - state.questDetails.questDetailsDescScroll;
        state.questDetails.questDetailsTransformStartX = rect.x();
        state.questDetails.questDetailsTransformStartY = rect.y();
        state.questDetails.questDetailsTransformStartW = rect.w();
        state.questDetails.questDetailsTransformStartH = rect.h();
        state.questDetails.questDetailsTransformStartRotation = rect.rotation();
        CanvasImageLayer image = "desc_image".equals(kind) ? model.image(id) : null;
        int pivotX = image == null ? CanvasElementGeometry.defaultPivot(rect.w()) : image.pivotX();
        int pivotY = image == null ? CanvasElementGeometry.defaultPivot(rect.h()) : image.pivotY();
        state.questDetails.questDetailsTransformStartPivotX = pivotX;
        state.questDetails.questDetailsTransformStartPivotY = pivotY;
        state.questDetails.questDetailsTransformStartYaw = image == null ? CanvasImageLayer.DEFAULT_ENTITY_YAW : image.entityYaw();
        state.questDetails.questDetailsTransformStartPitch = image == null ? CanvasImageLayer.DEFAULT_MODEL_PITCH : image.modelPitch();
        state.questDetails.questDetailsTransformPivotX = screenContentX + CanvasElementGeometry.logicalPivotX(rect.x(), rect.w(), pivotX);
        state.questDetails.questDetailsTransformPivotY = screenContentY - state.questDetails.questDetailsDescScroll + CanvasElementGeometry.logicalPivotY(rect.y(), rect.h(), pivotY);
        if (resizeHit) {
            state.questDetails.questDetailsTransformMode = "resize";
        } else if (rotateHit) {
            state.questDetails.questDetailsTransformMode = "rotate";
            state.questDetails.questDetailsTransformStartAngle = Math.atan2(state.questDetails.questDetailsTransformStartMouseY - state.questDetails.questDetailsTransformPivotY, state.questDetails.questDetailsTransformStartMouseX - state.questDetails.questDetailsTransformPivotX);
        } else {
            state.questDetails.questDetailsTransformMode = "move";
        }
    }

    void beginSelectionTransform(QuestDetailsDescriptionModel model, int lx, int visibleY) {
        beginSelectionTransform(model, lx, visibleY, "move");
    }

    void beginSelectionTransform(QuestDetailsDescriptionModel model, int lx, int visibleY, String mode) {
        CanvasTransformSessions.clearQuestDetailsSession(state);
        state.questDetails.questDetailsTransformKind = "selection";
        state.questDetails.questDetailsTransformId = "selection";
        state.questDetails.questDetailsTransformMode = mode == null || mode.isBlank() ? "move" : mode;
        state.questDetails.questDetailsTransformStartMouseX = screenContentX() + lx;
        state.questDetails.questDetailsTransformStartMouseY = screenContentY() + visibleY;
        for (CanvasTextLayer text : model.texts.values()) {
            if (isSelectedText(text.id())) {
                state.canvas.dragStartTextPositions.put(text.id(), new CanvasPoint(text.x(), text.y()));
            }
        }
        for (CanvasImageLayer image : model.images.values()) {
            if (isSelectedImage(image.id())) {
                state.canvas.dragStartImagePositions.put(image.id(), new CanvasPoint(image.x(), image.y()));
            }
        }
        CanvasLayerSelectionSnapshot snapshot = selectedLayerSnapshot(model);
        state.canvas.resizeStartLeft = snapshot.left();
        state.canvas.resizeStartTop = snapshot.top();
        state.canvas.resizeStartRight = snapshot.right();
        state.canvas.resizeStartBottom = snapshot.bottom();
        state.canvas.resizeStartImageLayers.putAll(snapshot.images());
        state.canvas.resizeStartTextLayers.putAll(snapshot.texts());
        state.canvas.rotateStartBoundsLeft = snapshot.left();
        state.canvas.rotateStartBoundsTop = snapshot.top();
        state.canvas.rotateStartBoundsRight = snapshot.right();
        state.canvas.rotateStartBoundsBottom = snapshot.bottom();
        state.canvas.rotateStartImageLayers.putAll(snapshot.images());
        state.canvas.rotateStartTextLayers.putAll(snapshot.texts());
        state.canvas.rotatePivotX = (snapshot.left() + snapshot.right()) / 2.0;
        state.canvas.rotatePivotY = (snapshot.top() + snapshot.bottom()) / 2.0;
        state.canvas.rotateStartAngle = Math.atan2(visibleY + state.questDetails.questDetailsDescScroll - state.canvas.rotatePivotY, lx - state.canvas.rotatePivotX);
        state.canvas.rotatePreviewAngle = 0.0;
    }

    void applyTransform(QuestDetailsDescriptionModel model, int mouseX, int mouseY) {
        String id = state.questDetails.questDetailsTransformId;
        if (id.isBlank()) {
            return;
        }
        int dx = mouseX - state.questDetails.questDetailsTransformStartMouseX;
        int dy = mouseY - state.questDetails.questDetailsTransformStartMouseY;
        if ("desc_text".equals(state.questDetails.questDetailsTransformKind)) {
            CanvasTextLayer text = model.text(id);
            if (text != null) {
                model.putText(transformText(model, text, dx, dy, mouseX, mouseY));
            }
        } else if ("desc_image".equals(state.questDetails.questDetailsTransformKind)) {
            CanvasImageLayer image = model.image(id);
            if (image != null) {
                model.putImage(transformImage(model, image, dx, dy, mouseX, mouseY));
            }
        } else if ("selection".equals(state.questDetails.questDetailsTransformKind)) {
            applySelectionTransform(model, dx, dy, mouseX, mouseY);
        }
    }

    private void applySelectionTransform(QuestDetailsDescriptionModel model, int dx, int dy, int mouseX, int mouseY) {
        if ("resize".equals(state.questDetails.questDetailsTransformMode)) {
            applySelectionResize(model, mouseX, mouseY);
            return;
        }
        if ("rotate".equals(state.questDetails.questDetailsTransformMode)) {
            applySelectionRotate(model, mouseX, mouseY);
            return;
        }
        CanvasPoint delta = LayerTransformEngine.dragDelta(dx, dy, snapSettings());
        for (Map.Entry<String, CanvasPoint> entry : state.canvas.dragStartTextPositions.entrySet()) {
            CanvasTextLayer text = model.text(entry.getKey());
            if (text != null) {
                CanvasPoint clamped = clampTextAnchor(entry.getValue().x + delta.x, entry.getValue().y + delta.y, text.w(), text.h(), text.rotation());
                model.putText(text.moveTo(clamped.x, clamped.y));
            }
        }
        for (Map.Entry<String, CanvasPoint> entry : state.canvas.dragStartImagePositions.entrySet()) {
            CanvasImageLayer image = model.image(entry.getKey());
            if (image != null) {
                CanvasPoint clamped = clampImageAnchor(image.moveTo(entry.getValue().x + delta.x, entry.getValue().y + delta.y));
                model.putImage(image.moveTo(clamped.x, clamped.y));
            }
        }
    }

    private void applySelectionResize(QuestDetailsDescriptionModel model, int mouseX, int mouseY) {
        int logicalMouseX = mouseX - screenContentX();
        int logicalMouseY = mouseY - screenContentY() + state.questDetails.questDetailsDescScroll;
        CanvasGroupResizeTransform.Result result = CanvasGroupResizeTransform.resizeBottomRight(
                activeLayerSnapshot(
                        state.canvas.resizeStartLeft,
                        state.canvas.resizeStartTop,
                        state.canvas.resizeStartRight,
                        state.canvas.resizeStartBottom,
                        state.canvas.resizeStartImageLayers,
                        state.canvas.resizeStartTextLayers
                ),
                logicalMouseX,
                logicalMouseY,
                selectionResizeConstraints()
        );
        applyResizeTransformResult(model, result);
    }

    private void applySelectionRotate(QuestDetailsDescriptionModel model, int mouseX, int mouseY) {
        double logicalMouseX = mouseX - screenContentX();
        double logicalMouseY = mouseY - screenContentY() + state.questDetails.questDetailsDescScroll;
        double currentAngle = Math.atan2(logicalMouseY - state.canvas.rotatePivotY, logicalMouseX - state.canvas.rotatePivotX);
        double delta = currentAngle - state.canvas.rotateStartAngle;
        if (TabletModifierKeys.shiftDown()) {
            double snap = Math.PI / 12.0;
            delta = Math.round(delta / snap) * snap;
        }
        state.canvas.rotatePreviewAngle = delta;
        CanvasLayerGroupTransform.Result result = CanvasLayerGroupTransform.rotate(
                activeLayerSnapshot(
                        state.canvas.rotateStartBoundsLeft,
                        state.canvas.rotateStartBoundsTop,
                        state.canvas.rotateStartBoundsRight,
                        state.canvas.rotateStartBoundsBottom,
                        state.canvas.rotateStartImageLayers,
                        state.canvas.rotateStartTextLayers
                ),
                state.canvas.rotatePivotX,
                state.canvas.rotatePivotY,
                delta,
                this::clampLayer
        );
        applyRotationTransformResult(model, result);
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

    private CanvasPoint clampLayer(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return clampRotated(x, y, width, height, pivotX, pivotY, rotationDegrees);
    }

    private CanvasGroupResizeTransform.Constraints selectionResizeConstraints() {
        return new CanvasGroupResizeTransform.Constraints(
                4,
                4,
                CanvasGeometry.gridSize(state),
                state.questDetails.questDetailsGridSnapLocked || TabletModifierKeys.shiftDown(),
                TabletModifierKeys.shiftDown(),
                QuestDetailsDescriptionLayout.visibleLeftEdge(),
                QuestDetailsDescriptionLayout.visibleTopEdge(state),
                contentW.getAsInt(),
                CanvasGroupResizeTransform.UNBOUNDED
        );
    }

    private void applyResizeTransformResult(QuestDetailsDescriptionModel model, CanvasGroupResizeTransform.Result result) {
        for (CanvasImageLayer image : result.images().values()) {
            model.putImage(fitAndClampImage(image));
        }
        for (CanvasTextLayer text : result.texts().values()) {
            model.putText(fitAndClampText(text));
        }
    }

    private void applyRotationTransformResult(QuestDetailsDescriptionModel model, CanvasLayerGroupTransform.Result result) {
        for (CanvasImageLayer image : result.images().values()) {
            model.putImage(clampRotationPreviewImage(image));
        }
        for (CanvasTextLayer text : result.texts().values()) {
            model.putText(clampRotationPreviewText(text));
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
        return switch (state.questDetails.questDetailsTransformMode) {
            case "resize" -> resizedText(text, mouseX, mouseY, 1, 1);
            case "rotate" -> clampRotationPreviewText(text.rotateTo(rotation(mouseX, mouseY)));
            default -> {
                CanvasPoint delta = LayerTransformEngine.dragDelta(dx, dy, snapSettings());
                int x = state.questDetails.questDetailsTransformStartX + delta.x;
                int y = state.questDetails.questDetailsTransformStartY + delta.y;
                CanvasPoint snapped = snapMove(model, text.id(), x, y, text.w(), text.h(), CanvasElementGeometry.defaultPivot(text.w()), CanvasElementGeometry.defaultPivot(text.h()), text.rotation());
                CanvasPoint clamped = clampTextAnchor(snapped.x, snapped.y, text.w(), text.h(), text.rotation());
                yield text.moveTo(clamped.x, clamped.y);
            }
        };
    }

    private CanvasImageLayer transformImage(QuestDetailsDescriptionModel model, CanvasImageLayer image, int dx, int dy, int mouseX, int mouseY) {
        return switch (state.questDetails.questDetailsTransformMode) {
            case "resize" -> resizedImage(image, mouseX, mouseY);
            case "rotate" -> CanvasTransformGizmo.supports(image.asset()) && !CanvasTransformGizmo.AXIS_ROLL.equals(state.questDetails.questDetailsTransformAxis) ? rotateModelFromDrag(image, mouseX, mouseY) : clampRotationPreviewImage(image.rotateTo(rotation(mouseX, mouseY)));
            default -> {
                CanvasPoint delta = CanvasTransformGizmo.supports(image.asset())
                        ? modelMoveDelta(dx, dy)
                        : LayerTransformEngine.dragDelta(dx, dy, snapSettings());
                int x = state.questDetails.questDetailsTransformStartX + delta.x;
                int y = state.questDetails.questDetailsTransformStartY + delta.y;
                CanvasPoint snapped = snapMove(model, image.id(), x, y, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
                String axis = CanvasTransformGizmo.moveAxisOrFree(state.questDetails.questDetailsTransformAxis);
                if (!CanvasTransformGizmo.AXIS_MOVE_FREE.equals(axis)) {
                    CanvasPoint offset = LayerTransformEngine.axisDelta(snapped.x - x, snapped.y - y, state.questDetails.questDetailsTransformStartRotation, axis, new LayerTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), false, false));
                    snapped = new CanvasPoint(x + offset.x, y + offset.y);
                    state.canvas.snapGuideXVisible = state.canvas.snapGuideXVisible && offset.x != 0;
                    state.canvas.snapGuideYVisible = state.canvas.snapGuideYVisible && offset.y != 0;
                }
                CanvasPoint clamped = clampImageAnchor(image.moveTo(snapped.x, snapped.y));
                yield image.moveTo(clamped.x, clamped.y);
            }
        };
    }

    private CanvasPoint modelMoveDelta(int dx, int dy) {
        String axis = CanvasTransformGizmo.moveAxisOrFree(state.questDetails.questDetailsTransformAxis);
        return LayerTransformEngine.modelMoveDelta(new LayerTransformEngine.ModelMoveRequest(
                dx,
                dy,
                state.questDetails.questDetailsTransformStartRotation,
                axis,
                CanvasTransformGizmo.AXIS_MOVE_FREE.equals(axis),
                snapSettings()
        ));
    }

    private CanvasImageLayer rotateModelFromDrag(CanvasImageLayer image, int mouseX, int mouseY) {
        LayerTransformEngine.ModelRotation rotation = LayerTransformEngine.modelRotation(new LayerTransformEngine.ModelRotationRequest(
                new LayerTransformEngine.ModelRotation(state.questDetails.questDetailsTransformStartYaw, state.questDetails.questDetailsTransformStartPitch),
                CanvasTransformGizmo.AXIS_PITCH.equals(state.questDetails.questDetailsTransformAxis),
                state.questDetails.questDetailsTransformPivotX,
                state.questDetails.questDetailsTransformPivotY,
                state.questDetails.questDetailsTransformStartAngle,
                mouseX,
                mouseY,
                TabletModifierKeys.shiftDown()
        ));
        return image.withModelRotation(rotation.yaw(), rotation.pitch());
    }

    private CanvasTextLayer resizedText(CanvasTextLayer text, int mouseX, int mouseY, int cornerX, int cornerY) {
        CanvasGeometry.ResizedBox box = resizedBox(mouseX, mouseY, 24, 14, state.questDetails.questDetailsTransformStartPivotX, state.questDetails.questDetailsTransformStartPivotY, cornerX, cornerY, TabletModifierKeys.shiftDown());
        return fitAndClampText(new CanvasTextLayer(text.id(), text.text(), box.x(), box.y(), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans()));
    }

    private CanvasImageLayer resizedImage(CanvasImageLayer image, int mouseX, int mouseY) {
        boolean gizmoSupported = CanvasTransformGizmo.supports(image.asset());
        int cornerX = gizmoSupported ? CanvasTransformGizmo.resizeCornerX(state.questDetails.questDetailsTransformAxis) : 1;
        int cornerY = gizmoSupported ? CanvasTransformGizmo.resizeCornerY(state.questDetails.questDetailsTransformAxis) : 1;
        CanvasGeometry.ResizedBox box = resizedBox(mouseX, mouseY, 8, 8, state.questDetails.questDetailsTransformStartPivotX, state.questDetails.questDetailsTransformStartPivotY, cornerX, cornerY, gizmoSupported || TabletModifierKeys.shiftDown());
        return fitAndClampImage(image.withBounds(box.x(), box.y(), box.width(), box.height()));
    }

    private CanvasGeometry.ResizedBox resizedBox(int mouseX, int mouseY, int minW, int minH, int pivotX, int pivotY, int cornerX, int cornerY, boolean preserveAspect) {
        return LayerTransformEngine.resizeFromCorner(new LayerTransformEngine.ResizeRequest(
                layerRect(
                        state.questDetails.questDetailsTransformStartX,
                        state.questDetails.questDetailsTransformStartY,
                        state.questDetails.questDetailsTransformStartW,
                        state.questDetails.questDetailsTransformStartH,
                        pivotX,
                        pivotY,
                        state.questDetails.questDetailsTransformStartRotation
                ),
                mouseX - screenContentX(),
                mouseY - screenContentY() + state.questDetails.questDetailsDescScroll,
                minW,
                minH,
                snapSettings(),
                preserveAspect,
                cornerX,
                cornerY
        ));
    }

    private int screenContentX() {
        return QuestDetailsMouse.screenX(state, contentX.getAsInt());
    }

    private int screenContentY() {
        return QuestDetailsMouse.screenY(state, contentY.getAsInt());
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.questDetails.questDetailsGridSnapLocked ? QuestDetailsDescriptionLayout.fittedText(state, text) : text;
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.questDetails.questDetailsGridSnapLocked ? QuestDetailsDescriptionLayout.fittedImage(state, image) : image;
    }

    private CanvasTextLayer fitAndClampText(CanvasTextLayer text) {
        return QuestDetailsDescriptionLayout.fitAndClampText(state, text, contentW.getAsInt());
    }

    private CanvasImageLayer fitAndClampImage(CanvasImageLayer image) {
        return QuestDetailsDescriptionLayout.fitAndClampImage(state, image, contentW.getAsInt());
    }

    private CanvasTextLayer clampRotationPreviewText(CanvasTextLayer text) {
        CanvasTextLayer preview = shouldFitRotatedPreview(text.rotation())
                ? fittedTextIfGridLocked(text)
                : text;
        CanvasPoint clamped = clampTextAnchor(preview.x(), preview.y(), preview.w(), preview.h(), preview.rotation());
        return preview.moveTo(clamped.x, clamped.y);
    }

    private CanvasImageLayer clampRotationPreviewImage(CanvasImageLayer image) {
        CanvasImageLayer preview = shouldFitRotatedPreview(image.rotation())
                ? fittedImageIfGridLocked(image)
                : image;
        CanvasPoint clamped = clampImageAnchor(preview);
        return preview.moveTo(clamped.x, clamped.y);
    }

    private boolean shouldFitRotatedPreview(int rotation) {
        return state.questDetails.questDetailsGridSnapLocked && TabletModifierKeys.shiftDown() && CanvasGeometry.isCardinalTurn(rotation);
    }

    private int rotation(int mouseX, int mouseY) {
        return LayerTransformEngine.layerRotation(new LayerTransformEngine.RotationRequest(
                state.questDetails.questDetailsTransformStartRotation,
                state.questDetails.questDetailsTransformPivotX,
                state.questDetails.questDetailsTransformPivotY,
                state.questDetails.questDetailsTransformStartAngle,
                mouseX,
                mouseY,
                TabletModifierKeys.shiftDown()
        ));
    }

    private CanvasPoint clampTextAnchor(int x, int y, int width, int height, int rotationDegrees) {
        return clampRotated(x, y, width, height, CanvasElementGeometry.defaultPivot(width), CanvasElementGeometry.defaultPivot(height), rotationDegrees);
    }

    private CanvasPoint clampImageAnchor(CanvasImageLayer image) {
        return QuestDetailsDescriptionLayout.clampImageAnchorToColumn(state, image, contentW.getAsInt());
    }

    private CanvasPoint clampRotated(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        return QuestDetailsDescriptionLayout.clampAnchorToColumn(state, x, y, width, height, pivotX, pivotY, rotationDegrees, contentW.getAsInt());
    }

    private CanvasPoint snapMove(QuestDetailsDescriptionModel model, String movingId, int x, int y, int w, int h, int pivotX, int pivotY, int rotation) {
        state.canvas.snapGuideXVisible = false;
        state.canvas.snapGuideYVisible = false;
        CanvasSnapEngine.SnapResult result = CanvasSnapEngine.snap(new CanvasSnapEngine.SnapContext(
                CanvasSnapBounds.atPivot(x, y, w, h, pivotX, pivotY, rotation),
                snapTargets(model, movingId),
                new CanvasSnapEngine.SnapSettings(
                        state.questDetails.questDetailsCenterSnapXEnabled,
                        state.questDetails.questDetailsCenterSnapYEnabled,
                        state.questDetails.questDetailsObjectSnapEnabled,
                        contentW.getAsInt() / 2.0D,
                        state.questDetails.questDetailsDescScroll + contentH.getAsInt() / 2.0D,
                        snapThresholdLogical()
                )
        ));
        if (result.guideXVisible()) {
            state.canvas.snapGuideXVisible = true;
            state.canvas.snapGuideX = (int) Math.round(result.guideX());
        }
        if (result.guideYVisible()) {
            state.canvas.snapGuideYVisible = true;
            state.canvas.snapGuideY = (int) Math.round(result.guideY()) - state.questDetails.questDetailsDescScroll;
        }
        return new CanvasPoint(x + result.offsetX(), y + result.offsetY());
    }

    private List<CanvasSnapEngine.Bounds> snapTargets(QuestDetailsDescriptionModel model, String movingId) {
        if (!state.questDetails.questDetailsObjectSnapEnabled) {
            return List.of();
        }
        List<CanvasSnapEngine.Bounds> targets = new ArrayList<>();
        for (CanvasTextLayer text : model.texts.values()) {
            if (!text.id().equals(movingId)) {
                targets.add(CanvasSnapBounds.forText(text));
            }
        }
        for (CanvasImageLayer image : model.images.values()) {
            if (!image.id().equals(movingId)) {
                targets.add(CanvasSnapBounds.forImage(image));
            }
        }
        return targets;
    }

    private int snapThresholdLogical() {
        int screenThreshold = 5;
        if (!state.questDetails.questDetailsGridSnapLocked) {
            return screenThreshold;
        }
        int gridReach = Math.max(1, (CanvasGeometry.gridSize(state) + 1) / 2);
        return Math.max(screenThreshold, gridReach);
    }

    private boolean isSelectedText(String id) {
        return id.equals(state.questDetails.questDetailsDescriptionSelection.primaryTextId()) || state.questDetails.questDetailsDescriptionSelection.textIds().contains(id);
    }

    private boolean isSelectedImage(String id) {
        return id.equals(state.questDetails.questDetailsDescriptionSelection.primaryImageId()) || state.questDetails.questDetailsDescriptionSelection.imageIds().contains(id);
    }

    private LayerTransformEngine.LayerRect layerRect(int x, int y, int width, int height, int pivotX, int pivotY, int rotation) {
        return new LayerTransformEngine.LayerRect(x, y, width, height, pivotX, pivotY, rotation);
    }

    private LayerTransformEngine.SnapSettings snapSettings() {
        return new LayerTransformEngine.SnapSettings(CanvasGeometry.gridSize(state), state.questDetails.questDetailsGridSnapLocked, TabletModifierKeys.shiftDown());
    }

    record ElementRect(int x, int y, int w, int h, int rotation) {
    }
}

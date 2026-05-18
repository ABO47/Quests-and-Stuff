package com.abo47.questsandstuff.client.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
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
        state.canvasImageStartRotation = image.rotation();
        state.resizingCanvasImage = CanvasRenderer.isCanvasImageResizeHandleHit(state, image, localX, localY);
        state.rotatingCanvasImage = CanvasRenderer.isCanvasImageRotateHandleHit(state, image, localX, localY);
        if (state.rotatingCanvasImage) {
            state.canvasImageRotatePivotX = image.x() + image.w() / 2.0;
            state.canvasImageRotatePivotY = image.y() + image.h() / 2.0;
            double logicalMouseX = CanvasGeometry.screenToLogicalX(state, localX);
            double logicalMouseY = CanvasGeometry.screenToLogicalY(state, localY);
            state.canvasImageRotateStartAngle = Math.atan2(logicalMouseY - state.canvasImageRotatePivotY, logicalMouseX - state.canvasImageRotatePivotX);
        }
        state.draggingCanvasImage = !state.resizingCanvasImage && !state.rotatingCanvasImage;
        state.selectedQuestIds.clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas image transform start id={} drag={} resize={} rotate={}", image.id(), state.draggingCanvasImage, state.resizingCanvasImage, state.rotatingCanvasImage);
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
            CanvasPoint anchor = dragAnchor(state.canvasImageStartX, state.canvasImageStartY, state.canvasImageStartW, state.canvasImageStartH, dx, dy);
            next = new CanvasImageLayer(image.id(), image.asset(), anchor.x, anchor.y, state.canvasImageStartW, state.canvasImageStartH, state.canvasImageStartRotation, image.entityYaw(), image.entitySpinSpeed());
            next = applySmartSnapToImage(next, cards, group);
        } else if (state.resizingCanvasImage) {
            clearSnapGuides();
            next = resizeImageFromHandle(image, localX, localY);
        } else if (state.rotatingCanvasImage) {
            clearSnapGuides();
            double currentAngle = Math.atan2(logicalY - state.canvasImageRotatePivotY, logicalX - state.canvasImageRotatePivotX);
            double deltaDegrees = Math.toDegrees(currentAngle - state.canvasImageRotateStartAngle);
            int angle = state.canvasImageStartRotation + (int) Math.round(deltaDegrees);
            if (isShiftDown()) {
                angle = Math.round(angle / 15.0f) * 15;
            }
            next = image.rotateTo(angle);
        }
        CanvasRenderer.putCanvasImage(state, group, next, false);
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
            state.canvasTextRotatePivotX = text.x() + text.w() / 2.0;
            state.canvasTextRotatePivotY = text.y() + text.h() / 2.0;
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
            next = text.rotateTo(angle);
        }
        CanvasRenderer.putCanvasText(state, group, next, false);
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
        int requestedX = image.x() + snap.offsetX();
        int requestedY = image.y() + snap.offsetY();
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

    private CanvasPoint dragDelta(int dx, int dy) {
        if (!state.gridSnapLocked) {
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
                8
        );
        return new CanvasImageLayer(image.id(), image.asset(), box.x(), box.y(), box.width(), box.height(), image.rotation(), image.entityYaw(), image.entitySpinSpeed());
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
                14
        );
        return new CanvasTextLayer(text.id(), text.text(), box.x(), box.y(), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
    }

    private ResizedBox resizeFromSelectionBox(int localX, int localY, int startX, int startY, int startW, int startH, int rotation, int minW, int minH) {
        CanvasGeometry.ResizedBox resized = CanvasGeometry.resizeRotatedFromBottomRight(
                CanvasGeometry.screenToLogicalX(state, localX),
                CanvasGeometry.screenToLogicalY(state, localY),
                startX,
                startY,
                startW,
                startH,
                rotation,
                minW,
                minH,
                CanvasGeometry.gridSize(state),
                state.gridSnapLocked,
                isShiftDown()
        );
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, resized.x(), resized.y(), resized.width(), resized.height());
        return new ResizedBox(clamped.x, clamped.y, resized.width(), resized.height());
    }

    private record ResizedBox(int x, int y, int width, int height) {
    }
}

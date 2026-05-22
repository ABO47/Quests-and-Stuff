package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
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
            beginSelectionTransform(model, lx, visibleY);
            return;
        }
        state.questDetailsTransformKind = kind;
        state.questDetailsTransformId = id;
        state.questDetailsTransformStartMouseX = contentX.getAsInt() + lx;
        state.questDetailsTransformStartMouseY = contentY.getAsInt() + ly - state.questDetailsDescScroll;
        state.questDetailsTransformStartX = rect.x();
        state.questDetailsTransformStartY = rect.y();
        state.questDetailsTransformStartW = rect.w();
        state.questDetailsTransformStartH = rect.h();
        state.questDetailsTransformStartRotation = rect.rotation();
        state.questDetailsTransformPivotX = contentX.getAsInt() + rect.x() + rect.w() / 2.0;
        state.questDetailsTransformPivotY = contentY.getAsInt() + rect.y() - state.questDetailsDescScroll + rect.h() / 2.0;
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
        state.questDetailsTransformKind = "selection";
        state.questDetailsTransformId = "selection";
        state.questDetailsTransformMode = "move";
        state.questDetailsTransformStartMouseX = contentX.getAsInt() + lx;
        state.questDetailsTransformStartMouseY = contentY.getAsInt() + visibleY;
        state.dragStartTextPositions.clear();
        state.dragStartImagePositions.clear();
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
            applySelectionTransform(model, dx, dy);
        }
    }

    private void applySelectionTransform(QuestDetailsDescriptionModel model, int dx, int dy) {
        int snappedDx = snapDelta(model, dx);
        int snappedDy = snapDelta(model, dy);
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

    private CanvasTextLayer transformText(QuestDetailsDescriptionModel model, CanvasTextLayer text, int dx, int dy, int mouseX, int mouseY) {
        return switch (state.questDetailsTransformMode) {
            case "resize" -> resizedText(model, text, mouseX, mouseY);
            case "rotate" -> text.rotateTo(rotation(mouseX, mouseY));
            default -> {
                int x = snap(model, state.questDetailsTransformStartX + dx);
                int y = snap(model, state.questDetailsTransformStartY + dy);
                SnapMove snapped = snapMove(model, text.id(), x, y, text.w(), text.h(), text.rotation());
                yield text.moveTo(clampX(snapped.x(), text.w()), clampY(snapped.y(), text.h()));
            }
        };
    }

    private CanvasImageLayer transformImage(QuestDetailsDescriptionModel model, CanvasImageLayer image, int dx, int dy, int mouseX, int mouseY) {
        return switch (state.questDetailsTransformMode) {
            case "resize" -> resizedImage(model, image, mouseX, mouseY);
            case "rotate" -> image.rotateTo(rotation(mouseX, mouseY));
            default -> {
                int x = snap(model, state.questDetailsTransformStartX + dx);
                int y = snap(model, state.questDetailsTransformStartY + dy);
                SnapMove snapped = snapMove(model, image.id(), x, y, image.w(), image.h(), image.rotation());
                yield image.moveTo(clampX(snapped.x(), image.w()), clampY(snapped.y(), image.h()));
            }
        };
    }

    private CanvasTextLayer resizedText(QuestDetailsDescriptionModel model, CanvasTextLayer text, int mouseX, int mouseY) {
        CanvasGeometry.ResizedBox box = resizedBox(model, mouseX, mouseY, 24, 14);
        return new CanvasTextLayer(text.id(), text.text(), clampX(box.x(), box.width()), clampY(box.y(), box.height()), box.width(), box.height(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
    }

    private CanvasImageLayer resizedImage(QuestDetailsDescriptionModel model, CanvasImageLayer image, int mouseX, int mouseY) {
        CanvasGeometry.ResizedBox box = resizedBox(model, mouseX, mouseY, 8, 8);
        return new CanvasImageLayer(image.id(), image.asset(), clampX(box.x(), box.width()), clampY(box.y(), box.height()), box.width(), box.height(), image.rotation(), image.entityYaw(), image.entitySpinSpeed());
    }

    private CanvasGeometry.ResizedBox resizedBox(QuestDetailsDescriptionModel model, int mouseX, int mouseY, int minW, int minH) {
        return CanvasGeometry.resizeRotatedFromBottomRight(
                mouseX - contentX.getAsInt(),
                mouseY - contentY.getAsInt() + state.questDetailsDescScroll,
                state.questDetailsTransformStartX,
                state.questDetailsTransformStartY,
                state.questDetailsTransformStartW,
                state.questDetailsTransformStartH,
                state.questDetailsTransformStartRotation,
                minW,
                minH,
                CanvasGeometry.gridSize(state),
                model.gridSnapLocked,
                isShiftDown()
        );
    }

    private int rotation(int mouseX, int mouseY) {
        double angle = Math.atan2(mouseY - state.questDetailsTransformPivotY, mouseX - state.questDetailsTransformPivotX);
        int rotation = (int) Math.round(state.questDetailsTransformStartRotation + Math.toDegrees(angle - state.questDetailsTransformStartAngle));
        if (isShiftDown()) {
            return Math.round(rotation / 15.0f) * 15;
        }
        return rotation;
    }

    private int snap(QuestDetailsDescriptionModel model, int value) {
        if (!model.gridSnapLocked) {
            return value;
        }
        int step = Math.max(1, CanvasGeometry.gridSize(state));
        return Math.round((float) value / (float) step) * step;
    }

    private int snapDelta(QuestDetailsDescriptionModel model, int delta) {
        if (!model.gridSnapLocked) {
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

    private SnapMove snapMove(QuestDetailsDescriptionModel model, String movingId, int x, int y, int w, int h, int rotation) {
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
        if (!state.questDetailsCenterSnapXEnabled && !state.questDetailsCenterSnapYEnabled && !state.questDetailsObjectSnapEnabled) {
            return new SnapMove(x, y);
        }
        int threshold = Math.max(5, (CanvasGeometry.gridSize(state) + 1) / 2);
        SnapChoice xChoice = SnapChoice.empty(threshold);
        SnapChoice yChoice = SnapChoice.empty(threshold);
        Bounds moving = bounds(x, y, w, h, rotation);
        if (state.questDetailsObjectSnapEnabled) {
            for (CanvasTextLayer text : model.texts.values()) {
                if (!text.id().equals(movingId)) {
                    Bounds target = bounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
                    xChoice = bestX(xChoice, moving, target);
                    yChoice = bestY(yChoice, moving, target);
                }
            }
            for (CanvasImageLayer image : model.images.values()) {
                if (!image.id().equals(movingId)) {
                    Bounds target = bounds(image.x(), image.y(), image.w(), image.h(), image.rotation());
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

    private static Bounds bounds(int x, int y, int w, int h, int rotation) {
        int[] box = CanvasGeometry.rotatedBounds(x, y, w, h, rotation);
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

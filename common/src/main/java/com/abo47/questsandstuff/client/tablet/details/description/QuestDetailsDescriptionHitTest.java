package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.function.IntSupplier;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

final class QuestDetailsDescriptionHitTest {
    private final TabletUiState state;
    private final QuestDetailsDescriptionSelection selection;
    private final IntSupplier contentW;
    private final IntSupplier contentH;

    QuestDetailsDescriptionHitTest(TabletUiState state, QuestDetailsDescriptionSelection selection, IntSupplier contentW, IntSupplier contentH) {
        this.state = state;
        this.selection = selection;
        this.contentW = contentW;
        this.contentH = contentH;
    }

    Hit hit(QuestDetailsDescriptionModel model, int lx, int visibleY, int ly) {
        for (int i = model.order.size() - 1; i >= 0; i--) {
            String key = model.order.get(i);
            if (key.startsWith(QuestDetailsDescriptionModel.ORDER_TEXT)) {
                CanvasTextLayer text = model.text(key.substring(QuestDetailsDescriptionModel.ORDER_TEXT.length()));
                if (text != null && hitsText(text, lx, visibleY, ly)) {
                    return new Hit("desc_text", text.id());
                }
            } else if (key.startsWith(QuestDetailsDescriptionModel.ORDER_IMAGE)) {
                CanvasImageLayer image = model.image(key.substring(QuestDetailsDescriptionModel.ORDER_IMAGE.length()));
                if (image != null && hitsImage(image, lx, visibleY, ly)) {
                    return new Hit("desc_image", image.id());
                }
            }
        }
        return null;
    }

    Rect rect(QuestDetailsDescriptionModel model, Hit hit) {
        if ("desc_text".equals(hit.kind())) {
            CanvasTextLayer text = model.text(hit.id());
            return new Rect(text.x(), text.y(), text.w(), text.h(), text.rotation());
        }
        CanvasImageLayer image = model.image(hit.id());
        return new Rect(image.x(), image.y(), image.w(), image.h(), image.rotation());
    }

    boolean inResizeHandle(Rect rect, int px, int visibleY) {
        return selectionResizeHit(rect.x(), rect.y(), rect.w(), rect.h(), rect.rotation(), px, visibleY);
    }

    boolean inRotateHandle(Rect rect, int px, int visibleY) {
        return selectionRotateHit(rect.x(), rect.y(), rect.w(), rect.h(), rect.rotation(), px, visibleY);
    }

    CanvasTransformMode imageGizmoMode(QuestDetailsDescriptionModel model, Hit hit, int px, int visibleY) {
        if (hit == null || !"desc_image".equals(hit.kind())) {
            return null;
        }
        CanvasImageLayer image = model.image(hit.id());
        if (image == null || !CanvasTransformGizmo.supports(image.asset())) {
            return null;
        }
        CanvasTransformMode hitMode = imageGizmoMode(image, px, visibleY);
        return hitMode;
    }

    String imageGizmoAxis(QuestDetailsDescriptionModel model, Hit hit, int px, int visibleY) {
        if (hit == null || !"desc_image".equals(hit.kind())) {
            return "";
        }
        CanvasImageLayer image = model.image(hit.id());
        if (image == null || !CanvasTransformGizmo.supports(image.asset())) {
            return "";
        }
        final String[] axis = new String[1];
        withSelectionGeometry(() -> axis[0] = CanvasTransformGizmo.axisAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), px, visibleY));
        return axis[0] == null ? "" : axis[0];
    }

    boolean isHitSelected(Hit hit) {
        return "desc_text".equals(hit.kind()) ? selection.isSelectedText(hit.id()) : selection.isSelectedImage(hit.id());
    }

    private boolean hitsText(CanvasTextLayer text, int lx, int visibleY, int ly) {
        return selectedControlHit(text.x(), text.y(), text.w(), text.h(), text.rotation(), lx, visibleY, selection.isSelectedText(text.id()))
                || elementBoundsHit(text.x(), text.y(), text.w(), text.h(), text.rotation(), lx, visibleY)
                || contains(text.x(), text.y(), text.w(), text.h(), lx, ly);
    }

    private boolean hitsImage(CanvasImageLayer image, int lx, int visibleY, int ly) {
        return selectedImageControlHit(image, lx, visibleY)
                || elementBoundsHit(image.x(), image.y(), image.w(), image.h(), image.rotation(), lx, visibleY)
                || contains(image.x(), image.y(), image.w(), image.h(), lx, ly);
    }

    private boolean selectedImageControlHit(CanvasImageLayer image, int px, int visibleY) {
        if (!selection.isSelectedImage(image.id())) {
            return false;
        }
        if (CanvasTransformGizmo.supports(image.asset())) {
            return imageGizmoMode(image, px, visibleY) != null;
        }
        return selectedControlHit(image.x(), image.y(), image.w(), image.h(), image.rotation(), px, visibleY, true);
    }

    private boolean selectedControlHit(int x, int y, int w, int h, int rotation, int px, int visibleY, boolean selected) {
        return selected && (selectionResizeHit(x, y, w, h, rotation, px, visibleY)
                || selectionRotateHit(x, y, w, h, rotation, px, visibleY));
    }

    private boolean elementBoundsHit(int x, int y, int w, int h, int rotation, int px, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> {
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, x, y, w, h, rotation);
            hit[0] = px >= bounds[0] && px <= bounds[2] && visibleY >= bounds[1] && visibleY <= bounds[3];
        });
        return hit[0];
    }

    private boolean selectionResizeHit(int x, int y, int w, int h, int rotation, int px, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> hit[0] = CanvasElementSelectionSlot.resizeHandleHit(state, x, y, w, h, rotation, px, visibleY));
        return hit[0];
    }

    private boolean selectionRotateHit(int x, int y, int w, int h, int rotation, int px, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> hit[0] = CanvasElementSelectionSlot.rotateHandleHit(state, x, y, w, h, rotation, px, visibleY));
        return hit[0];
    }

    private CanvasTransformMode imageGizmoMode(CanvasImageLayer image, int px, int visibleY) {
        final CanvasTransformMode[] mode = new CanvasTransformMode[1];
        withSelectionGeometry(() -> {
            if (isShiftDown()
                    && CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.RESIZE
                    && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), px, visibleY)) {
                mode[0] = CanvasTransformMode.MOVE;
                return;
            }
            mode[0] = CanvasTransformGizmo.modeAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), px, visibleY);
            if (mode[0] == null
                    && isShiftDown()
                    && CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.MOVE
                    && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), px, visibleY)) {
                mode[0] = CanvasTransformMode.MOVE;
            }
        });
        return mode[0];
    }

    private static boolean contains(int x, int y, int w, int h, int px, int py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    private void withSelectionGeometry(Runnable draw) {
        int oldContentX = state.canvasContentX;
        int oldContentY = state.canvasContentY;
        int oldContentW = state.canvasContentW;
        int oldContentH = state.canvasContentH;
        int oldOffsetX = state.canvasOffsetX;
        int oldOffsetY = state.canvasOffsetY;
        float oldZoom = state.canvasZoom;
        boolean oldGridSnap = state.gridSnapLocked;
        state.canvasContentX = 0;
        state.canvasContentY = -state.questDetailsDescScroll;
        state.canvasContentW = contentW.getAsInt();
        state.canvasContentH = contentH.getAsInt();
        state.canvasOffsetX = 0;
        state.canvasOffsetY = 0;
        state.canvasZoom = 1.0f;
        state.gridSnapLocked = state.questDetailsGridSnapLocked;
        try {
            draw.run();
        } finally {
            state.canvasContentX = oldContentX;
            state.canvasContentY = oldContentY;
            state.canvasContentW = oldContentW;
            state.canvasContentH = oldContentH;
            state.canvasOffsetX = oldOffsetX;
            state.canvasOffsetY = oldOffsetY;
            state.canvasZoom = oldZoom;
            state.gridSnapLocked = oldGridSnap;
        }
    }

    record Hit(String kind, String id) {
    }

    record Rect(int x, int y, int w, int h, int rotation) {
    }
}

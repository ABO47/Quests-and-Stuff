package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
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

    Hit hit(QuestDetailsDescriptionModel model, int lx, int visibleY) {
        for (int i = model.order.size() - 1; i >= 0; i--) {
            String key = model.order.get(i);
            if (key.startsWith(QuestDetailsDescriptionModel.ORDER_TEXT)) {
                CanvasTextLayer text = model.text(key.substring(QuestDetailsDescriptionModel.ORDER_TEXT.length()));
                if (text != null && hitsText(text, lx, visibleY)) {
                    return new Hit("desc_text", text.id());
                }
            } else if (key.startsWith(QuestDetailsDescriptionModel.ORDER_IMAGE)) {
                CanvasImageLayer image = model.image(key.substring(QuestDetailsDescriptionModel.ORDER_IMAGE.length()));
                if (image != null && hitsImage(image, lx, visibleY)) {
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
        return new Rect(image.x(), image.y(), image.w(), image.h(), image.rotation(), image.pivotX(), image.pivotY());
    }

    boolean inResizeHandle(Rect rect, int px, int visibleY) {
        return selectionResizeHit(rect.x(), rect.y(), rect.w(), rect.h(), rect.pivotX(), rect.pivotY(), rect.rotation(), px, visibleY);
    }

    boolean inRotateHandle(Rect rect, int px, int visibleY) {
        return selectionRotateHit(rect.x(), rect.y(), rect.w(), rect.h(), rect.pivotX(), rect.pivotY(), rect.rotation(), px, visibleY);
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

    private boolean hitsText(CanvasTextLayer text, int lx, int visibleY) {
        return selectedControlHit(text.x(), text.y(), text.w(), text.h(), text.rotation(), lx, visibleY, selection.isSelectedText(text.id()))
                || elementBoundsHit(text.x(), text.y(), text.w(), text.h(), text.rotation(), lx, visibleY)
                || elementLocalHit(text.x(), text.y(), text.w(), text.h(), text.rotation(), lx, visibleY);
    }

    private boolean hitsImage(CanvasImageLayer image, int lx, int visibleY) {
        return selectedImageControlHit(image, lx, visibleY)
                || elementBoundsHit(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), lx, visibleY)
                || elementLocalHit(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), lx, visibleY);
    }

    private boolean selectedImageControlHit(CanvasImageLayer image, int px, int visibleY) {
        if (!selection.isSelectedImage(image.id())) {
            return false;
        }
        if (CanvasTransformGizmo.supports(image.asset())) {
            return imageGizmoMode(image, px, visibleY) != null;
        }
        return selectedControlHit(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), px, visibleY, true);
    }

    private boolean selectedControlHit(int x, int y, int w, int h, int rotation, int px, int visibleY, boolean selected) {
        return selectedControlHit(x, y, w, h, w / 2, h / 2, rotation, px, visibleY, selected);
    }

    private boolean selectedControlHit(int x, int y, int w, int h, int pivotX, int pivotY, int rotation, int px, int visibleY, boolean selected) {
        return selected && (selectionResizeHit(x, y, w, h, pivotX, pivotY, rotation, px, visibleY)
                || selectionRotateHit(x, y, w, h, pivotX, pivotY, rotation, px, visibleY));
    }

    private boolean elementBoundsHit(int x, int y, int w, int h, int rotation, int px, int visibleY) {
        return elementBoundsHit(x, y, w, h, w / 2, h / 2, rotation, px, visibleY);
    }

    private boolean elementBoundsHit(int x, int y, int w, int h, int pivotX, int pivotY, int rotation, int px, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> {
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, x, y, w, h, pivotX, pivotY, rotation);
            hit[0] = px >= bounds[0] && px <= bounds[2] && visibleY >= bounds[1] && visibleY <= bounds[3];
        });
        return hit[0];
    }

    private boolean elementLocalHit(int x, int y, int w, int h, int rotation, int px, int visibleY) {
        return elementLocalHit(x, y, w, h, w / 2, h / 2, rotation, px, visibleY);
    }

    private boolean elementLocalHit(int x, int y, int w, int h, int pivotX, int pivotY, int rotation, int px, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> {
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, x, y, w, h, pivotX, pivotY, rotation);
            CanvasElementGeometry.LocalPoint point = CanvasElementGeometry.toLocalPoint(box, rotation, px, visibleY);
            hit[0] = point.x() >= box.left() && point.x() <= box.right()
                    && point.y() >= box.top() && point.y() <= box.bottom();
        });
        return hit[0];
    }

    private boolean selectionResizeHit(int x, int y, int w, int h, int pivotX, int pivotY, int rotation, int px, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> hit[0] = CanvasElementSelectionSlot.resizeHandleHitAtPivot(state, x, y, w, h, pivotX, pivotY, rotation, px, visibleY));
        return hit[0];
    }

    private boolean selectionRotateHit(int x, int y, int w, int h, int pivotX, int pivotY, int rotation, int px, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> hit[0] = CanvasElementSelectionSlot.rotateHandleHitAtPivot(state, x, y, w, h, pivotX, pivotY, rotation, px, visibleY));
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

    record Rect(int x, int y, int w, int h, int rotation, int pivotX, int pivotY) {
        Rect(int x, int y, int w, int h, int rotation) {
            this(x, y, w, h, rotation, w / 2, h / 2);
        }
    }
}

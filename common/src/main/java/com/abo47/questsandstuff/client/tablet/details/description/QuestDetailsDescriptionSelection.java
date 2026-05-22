package com.abo47.questsandstuff.client.tablet.details.description;


import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.IntSupplier;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class QuestDetailsDescriptionSelection {
    private final TabletUiState state;
    private final IntSupplier contentX;
    private final IntSupplier contentY;
    private final IntSupplier contentW;
    private final IntSupplier contentH;

    QuestDetailsDescriptionSelection(TabletUiState state, IntSupplier contentX, IntSupplier contentY, IntSupplier contentW, IntSupplier contentH) {
        this.state = state;
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentW = contentW;
        this.contentH = contentH;
    }

    void clear() {
        state.questDetailsSelectedTextId = "";
        state.questDetailsSelectedImageId = "";
        state.questDetailsSelectedTextIds.clear();
        state.questDetailsSelectedImageIds.clear();
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        state.selectionBoundsVisible = false;
    }

    int count() {
        int count = state.questDetailsSelectedTextIds.size() + state.questDetailsSelectedImageIds.size();
        if (!state.questDetailsSelectedTextId.isBlank() && !state.questDetailsSelectedTextIds.contains(state.questDetailsSelectedTextId)) {
            count++;
        }
        if (!state.questDetailsSelectedImageId.isBlank() && !state.questDetailsSelectedImageIds.contains(state.questDetailsSelectedImageId)) {
            count++;
        }
        return count;
    }

    boolean isSelectedText(String id) {
        return id.equals(state.questDetailsSelectedTextId) || state.questDetailsSelectedTextIds.contains(id);
    }

    boolean isSelectedImage(String id) {
        return id.equals(state.questDetailsSelectedImageId) || state.questDetailsSelectedImageIds.contains(id);
    }

    void finishBoxSelection(QuestDetailsDescriptionModel model) {
        int minX = Math.min(state.questDetailsBoxStartX, state.questDetailsBoxCurrentX);
        int minY = Math.min(state.questDetailsBoxStartY, state.questDetailsBoxCurrentY) + state.questDetailsDescScroll;
        int maxX = Math.max(state.questDetailsBoxStartX, state.questDetailsBoxCurrentX);
        int maxY = Math.max(state.questDetailsBoxStartY, state.questDetailsBoxCurrentY) + state.questDetailsDescScroll;
        for (CanvasTextLayer text : model.texts.values()) {
            if (intersects(bounds(text.x(), text.y(), text.w(), text.h(), text.rotation()), minX, minY, maxX, maxY)) {
                state.questDetailsSelectedTextIds.add(text.id());
                state.questDetailsSelectedTextId = text.id();
            }
        }
        for (CanvasImageLayer image : model.images.values()) {
            if (intersects(bounds(image.x(), image.y(), image.w(), image.h(), image.rotation()), minX, minY, maxX, maxY)) {
                state.questDetailsSelectedImageIds.add(image.id());
                state.questDetailsSelectedImageId = image.id();
            }
        }
    }

    void drawBoxSelection(GuiGraphics graphics) {
        if (!state.questDetailsBoxSelecting) {
            return;
        }
        int left = Math.max(0, Math.min(state.questDetailsBoxStartX, state.questDetailsBoxCurrentX));
        int top = Math.max(0, Math.min(state.questDetailsBoxStartY, state.questDetailsBoxCurrentY));
        int right = Math.min(contentW.getAsInt(), Math.max(state.questDetailsBoxStartX, state.questDetailsBoxCurrentX));
        int bottom = Math.min(contentH.getAsInt(), Math.max(state.questDetailsBoxStartY, state.questDetailsBoxCurrentY));
        if (right <= left || bottom <= top) {
            return;
        }
        drawRect(graphics, left, top, right - left, bottom - top, withAlpha(ModColors.INTERACTIVE, 28), withAlpha(ModColors.INTERACTIVE, 210));
    }

    void drawMultiSelectionBounds(GuiGraphics graphics, QuestDetailsDescriptionModel model) {
        SelectionRect bounds = selectionBounds(model);
        if (!bounds.valid()) {
            return;
        }
        drawRect(graphics, bounds.left() - 4, bounds.top() - 4, bounds.width() + 8, bounds.height() + 8, withAlpha(ModColors.INTERACTIVE, 24), withAlpha(ModColors.INTERACTIVE, 214));
    }

    boolean selectionBoundsHit(QuestDetailsDescriptionModel model, int lx, int visibleY) {
        SelectionRect bounds = selectionBounds(model);
        return bounds.valid()
                && lx >= bounds.left() - 4
                && lx <= bounds.right() + 4
                && visibleY >= bounds.top() - 4
                && visibleY <= bounds.bottom() + 4;
    }

    private SelectionRect selectionBounds(QuestDetailsDescriptionModel model) {
        if (!state.canEdit || !state.questDetailsEditMode || count() <= 1) {
            return SelectionRect.empty();
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CanvasTextLayer text : model.texts.values()) {
            if (isSelectedText(text.id())) {
                int[] box = bounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
                minX = Math.min(minX, box[0]);
                minY = Math.min(minY, box[1] - state.questDetailsDescScroll);
                maxX = Math.max(maxX, box[2]);
                maxY = Math.max(maxY, box[3] - state.questDetailsDescScroll);
            }
        }
        for (CanvasImageLayer image : model.images.values()) {
            if (isSelectedImage(image.id())) {
                int[] box = bounds(image.x(), image.y(), image.w(), image.h(), image.rotation());
                minX = Math.min(minX, box[0]);
                minY = Math.min(minY, box[1] - state.questDetailsDescScroll);
                maxX = Math.max(maxX, box[2]);
                maxY = Math.max(maxY, box[3] - state.questDetailsDescScroll);
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return SelectionRect.empty();
        }
        return new SelectionRect(minX, minY, maxX, maxY);
    }

    private void drawRect(GuiGraphics graphics, int x, int y, int w, int h, int fill, int border) {
        int left = Math.max(0, x);
        int top = Math.max(0, y);
        int right = Math.min(contentW.getAsInt(), x + Math.max(1, w));
        int bottom = Math.min(contentH.getAsInt(), y + Math.max(1, h));
        if (right <= left || bottom <= top) {
            return;
        }
        if ((fill >>> 24) != 0) {
            graphics.fill(contentX.getAsInt() + left, contentY.getAsInt() + top, contentX.getAsInt() + right, contentY.getAsInt() + bottom, fill);
        }
        graphics.fill(contentX.getAsInt() + left, contentY.getAsInt() + top, contentX.getAsInt() + right, contentY.getAsInt() + top + 1, border);
        graphics.fill(contentX.getAsInt() + left, contentY.getAsInt() + bottom - 1, contentX.getAsInt() + right, contentY.getAsInt() + bottom, border);
        graphics.fill(contentX.getAsInt() + left, contentY.getAsInt() + top, contentX.getAsInt() + left + 1, contentY.getAsInt() + bottom, border);
        graphics.fill(contentX.getAsInt() + right - 1, contentY.getAsInt() + top, contentX.getAsInt() + right, contentY.getAsInt() + bottom, border);
    }

    private static int[] bounds(int x, int y, int w, int h, int rotation) {
        return CanvasGeometry.rotatedBounds(x, y, w, h, rotation);
    }

    private static boolean intersects(int[] bounds, int minX, int minY, int maxX, int maxY) {
        return bounds[0] < maxX && bounds[2] > minX && bounds[1] < maxY && bounds[3] > minY;
    }

    private record SelectionRect(int left, int top, int right, int bottom) {
        static SelectionRect empty() {
            return new SelectionRect(0, 0, -1, -1);
        }

        boolean valid() {
            return right >= left && bottom >= top;
        }

        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }
}

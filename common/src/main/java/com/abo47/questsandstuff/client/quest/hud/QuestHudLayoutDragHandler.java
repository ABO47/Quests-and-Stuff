package com.abo47.questsandstuff.client.quest.hud;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class QuestHudLayoutDragHandler {
    private static final int GRID_STEP = 16;
    private static final int HANDLE_SIZE = 6;
    private static final int SELECTION_PAD = 1;

    private final QuestHudLayoutManager.Snapshot original;

    private QuestHudLayoutManager.Element selected;
    private QuestHudLayoutManager.Element dragging;
    private int pinnedBaseHeightSnapshot;
    private DragMode dragMode = DragMode.NONE;
    private int dragOffsetX;
    private int dragOffsetY;
    private int resizeStartMouseX;
    private int resizeStartMouseY;
    private int resizeStartX;
    private int resizeStartY;
    private int resizeStartSlotX;
    private int resizeStartSlotY;
    private int resizeStartWidth;
    private int resizeStartHeight;
    private boolean closed;
    private boolean openingChild;
    private Button snapButton;

    public QuestHudLayoutDragHandler() {
        this.original = QuestHudLayoutManager.snapshot();
        this.pinnedBaseHeightSnapshot = PinnedQuestHudOverlay.currentStackHeight();
    }

    public QuestHudLayoutManager.Element selected() {
        return selected;
    }

    public void setSelected(QuestHudLayoutManager.Element element) {
        this.selected = element;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isOpeningChild() {
        return openingChild;
    }

    public void setOpeningChild(boolean opening) {
        this.openingChild = opening;
    }

    public QuestHudLayoutManager.Snapshot original() {
        return original;
    }

    public QuestHudLayoutManager.Element dragging() {
        return dragging;
    }

    public DragMode dragMode() {
        return dragMode;
    }

    public void setSnapButton(Button button) {
        this.snapButton = button;
    }

    public void snapAllElementsToGrid(int screenWidth, int screenHeight) {
        snapElementToGrid(QuestHudLayoutManager.Element.COMPLETION, screenWidth, screenHeight);
        snapElementToGrid(QuestHudLayoutManager.Element.PINNED, screenWidth, screenHeight);
    }

    private void snapElementToGrid(QuestHudLayoutManager.Element element, int screenWidth, int screenHeight) {
        QuestHudLayoutManager.HudBox raw = rawBoxFor(element, screenWidth, screenHeight);
        int snappedW = CanvasGeometry.snapVisualSpanToGridSlot(raw.width(), GRID_STEP, baseWidth(element));
        int snappedH = CanvasGeometry.snapVisualSpanToGridSlot(raw.height(), GRID_STEP, baseHeight(element));
        applySizeFromVisual(element, snappedW, snappedH);
        QuestHudLayoutManager.HudBox current = rawBoxFor(element, screenWidth, screenHeight);
        int slotW = CanvasGeometry.slotSpanForVisualSize(current.width());
        int slotH = CanvasGeometry.slotSpanForVisualSize(current.height());
        int insetX = CanvasGeometry.visualInsetForSlot(slotW, current.width());
        int insetY = CanvasGeometry.visualInsetForSlot(slotH, current.height());
        int slotX = Math.max(0, Math.min(snapSlot(raw.x() - insetX), snapFloor(Math.max(0, screenWidth - slotW))));
        int slotY = Math.max(0, Math.min(snapSlot(raw.y() - insetY), snapFloor(Math.max(0, screenHeight - slotH))));
        QuestHudLayoutManager.setPosition(element, slotX + insetX, slotY + insetY, screenWidth, screenHeight, current.width(), current.height());
    }

    public void handleMouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        QuestHudLayoutManager.Element target = elementAt(mouseX, mouseY, screenWidth, screenHeight);
        selected = target;
        if (target == null) {
            return;
        }
        QuestHudLayoutManager.HudBox box = slotBoxFor(target, screenWidth, screenHeight);
        dragging = target;
        if (resizeHandle(selectionBox(box)).contains(mouseX, mouseY)) {
            dragMode = DragMode.RESIZE;
            QuestHudLayoutManager.HudBox raw = rawBoxFor(target, screenWidth, screenHeight);
            resizeStartMouseX = (int) Math.round(mouseX);
            resizeStartMouseY = (int) Math.round(mouseY);
            resizeStartX = raw.x();
            resizeStartY = raw.y();
            resizeStartSlotX = box.x();
            resizeStartSlotY = box.y();
            resizeStartWidth = raw.width();
            resizeStartHeight = raw.height();
        } else {
            dragMode = DragMode.MOVE;
            dragOffsetX = (int) Math.round(mouseX) - box.x();
            dragOffsetY = (int) Math.round(mouseY) - box.y();
        }
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (dragging == null || button != 0) {
            return false;
        }
        if (dragMode == DragMode.RESIZE) {
            int baseWidth = baseWidth(dragging);
            int baseHeight = baseHeight(dragging);
            int targetWidth = Math.max(1, resizeStartWidth + (int) Math.round(mouseX) - resizeStartMouseX);
            int targetHeight = Math.max(1, resizeStartHeight + (int) Math.round(mouseY) - resizeStartMouseY);
            if (QuestHudLayoutManager.snapToGrid()) {
                if (Screen.hasShiftDown()) {
                    float scale = Math.max(targetWidth / (float) Math.max(1, baseWidth), targetHeight / (float) Math.max(1, baseHeight));
                    targetWidth = Math.max(1, Math.round(baseWidth * scale));
                    targetHeight = Math.max(1, Math.round(baseHeight * scale));
                }
                int snappedW = CanvasGeometry.snapVisualSpanToGridSlot(targetWidth, GRID_STEP, baseWidth);
                int snappedH = CanvasGeometry.snapVisualSpanToGridSlot(targetHeight, GRID_STEP, baseHeight);
                applySizeFromVisual(dragging, snappedW, snappedH);
                QuestHudLayoutManager.HudBox resized = rawBoxFor(dragging, screenWidth, screenHeight);
                int slotW = CanvasGeometry.slotSpanForVisualSize(resized.width());
                int slotH = CanvasGeometry.slotSpanForVisualSize(resized.height());
                int insetX = CanvasGeometry.visualInsetForSlot(slotW, resized.width());
                int insetY = CanvasGeometry.visualInsetForSlot(slotH, resized.height());
                int slotX = Math.max(0, Math.min(resizeStartSlotX, snapFloor(Math.max(0, screenWidth - slotW))));
                int slotY = Math.max(0, Math.min(resizeStartSlotY, snapFloor(Math.max(0, screenHeight - slotH))));
                QuestHudLayoutManager.setPosition(dragging, slotX + insetX, slotY + insetY, screenWidth, screenHeight, resized.width(), resized.height());
                return true;
            }
            if (Screen.hasShiftDown()) {
                float scale = Math.max(targetWidth / (float) Math.max(1, baseWidth), targetHeight / (float) Math.max(1, baseHeight));
                int percent = Math.round(scale * 100.0f);
                QuestHudLayoutManager.setScalePercent(dragging, percent);
            } else {
                int widthPercent = Math.round(targetWidth * 100.0f / Math.max(1, baseWidth));
                int heightPercent = Math.round(targetHeight * 100.0f / Math.max(1, baseHeight));
                QuestHudLayoutManager.setSizePercent(dragging, widthPercent, heightPercent);
            }
            QuestHudLayoutManager.HudBox resized = rawBoxFor(dragging, screenWidth, screenHeight);
            QuestHudLayoutManager.HudBox nextVisual = new QuestHudLayoutManager.HudBox(resizeStartX, resizeStartY, resized.width(), resized.height());
            QuestHudLayoutManager.setPosition(dragging, nextVisual.x(), nextVisual.y(), screenWidth, screenHeight, nextVisual.width(), nextVisual.height());
            return true;
        }
        int nextX = (int) Math.round(mouseX) - dragOffsetX;
        int nextY = (int) Math.round(mouseY) - dragOffsetY;
        if (QuestHudLayoutManager.snapToGrid()) {
            nextX = snapSlot(nextX);
            nextY = snapSlot(nextY);
        }
        QuestHudLayoutManager.HudBox raw = rawBoxFor(dragging, screenWidth, screenHeight);
        QuestHudLayoutManager.HudBox nextSlot = QuestHudLayoutManager.snapToGrid()
                ? clampedSlot(nextX, nextY, raw.width(), raw.height(), screenWidth, screenHeight)
                : new QuestHudLayoutManager.HudBox(nextX, nextY, raw.width(), raw.height());
        QuestHudLayoutManager.HudBox nextVisual = QuestHudLayoutManager.snapToGrid() ? visualBoxInSlot(nextSlot, dragging) : nextSlot;
        QuestHudLayoutManager.setPosition(dragging, nextVisual.x(), nextVisual.y(), screenWidth, screenHeight, nextVisual.width(), nextVisual.height());
        return true;
    }

    public void handleMouseReleased() {
        dragging = null;
        dragMode = DragMode.NONE;
    }

    public void saveAndClose() {
        QuestHudLayoutManager.save();
        closed = true;
        Minecraft.getInstance().setScreen(null);
    }

    public void cancelAndClose() {
        QuestHudLayoutManager.restore(original);
        closed = true;
        Minecraft.getInstance().setScreen(null);
    }

    public QuestHudLayoutManager.Element elementAt(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        if (slotBoxFor(QuestHudLayoutManager.Element.PINNED, screenWidth, screenHeight).contains(mouseX, mouseY)) {
            return QuestHudLayoutManager.Element.PINNED;
        }
        if (slotBoxFor(QuestHudLayoutManager.Element.COMPLETION, screenWidth, screenHeight).contains(mouseX, mouseY)) {
            return QuestHudLayoutManager.Element.COMPLETION;
        }
        return null;
    }

    public QuestHudLayoutManager.HudBox visualBoxFor(QuestHudLayoutManager.Element element, int screenWidth, int screenHeight) {
        QuestHudLayoutManager.HudBox raw = rawBoxFor(element, screenWidth, screenHeight);
        if (!QuestHudLayoutManager.snapToGrid()) {
            return raw;
        }
        QuestHudLayoutManager.HudBox slot = slotBox(raw, screenWidth, screenHeight);
        return visualBoxInSlot(slot, element);
    }

    public QuestHudLayoutManager.HudBox slotBoxFor(QuestHudLayoutManager.Element element, int screenWidth, int screenHeight) {
        QuestHudLayoutManager.HudBox raw = rawBoxFor(element, screenWidth, screenHeight);
        return QuestHudLayoutManager.snapToGrid() ? slotBox(raw, screenWidth, screenHeight) : raw;
    }

    public QuestHudLayoutManager.HudBox rawBoxFor(QuestHudLayoutManager.Element element, int screenWidth, int screenHeight) {
        return element == QuestHudLayoutManager.Element.COMPLETION
                ? completionBox(screenWidth, screenHeight)
                : pinnedBox(screenWidth, screenHeight);
    }

    public QuestHudLayoutManager.HudBox visualBoxInSlot(QuestHudLayoutManager.HudBox slot, QuestHudLayoutManager.Element element) {
        int visualW = baseWidth(element) == 0 ? 0 : QuestHudLayoutManager.scaledSize(element, baseWidth(element));
        int visualH = baseHeight(element) == 0 ? 0 : QuestHudLayoutManager.scaledHeight(element, baseHeight(element));
        int insetX = CanvasGeometry.visualInsetForSlot(slot.width(), visualW);
        int insetY = CanvasGeometry.visualInsetForSlot(slot.height(), visualH);
        return new QuestHudLayoutManager.HudBox(
                slot.x() + insetX,
                slot.y() + insetY,
                visualW,
                visualH
        );
    }

    public static QuestHudLayoutManager.HudBox selectionBox(QuestHudLayoutManager.HudBox box) {
        return new QuestHudLayoutManager.HudBox(
                box.x() - SELECTION_PAD,
                box.y() - SELECTION_PAD,
                box.width() + SELECTION_PAD * 2,
                box.height() + SELECTION_PAD * 2
        );
    }

    public QuestHudLayoutManager.HudBox resizeHandle(QuestHudLayoutManager.HudBox box) {
        return new QuestHudLayoutManager.HudBox(box.x() + box.width() - HANDLE_SIZE, box.y() + box.height() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
    }

    public Component snapLabel() {
        return Component.translatable(QuestHudLayoutManager.snapToGrid()
                ? "ui.questsandstuff.hud.layout.snap_on"
                : "ui.questsandstuff.hud.layout.snap_off");
    }

    public void updateSnapButton() {
        if (snapButton != null) {
            snapButton.setMessage(snapLabel());
        }
    }

    private QuestHudLayoutManager.HudBox completionBox(int screenWidth, int screenHeight) {
        return QuestHudLayoutManager.completionBox(
                screenWidth,
                screenHeight,
                QuestHudLayoutManager.scaledSize(QuestHudLayoutManager.Element.COMPLETION, QuestCompletionNotificationOverlay.width()),
                QuestHudLayoutManager.scaledHeight(QuestHudLayoutManager.Element.COMPLETION, QuestCompletionNotificationOverlay.height())
        );
    }

    private QuestHudLayoutManager.HudBox pinnedBox(int screenWidth, int screenHeight) {
        return QuestHudLayoutManager.pinnedBox(
                screenWidth,
                screenHeight,
                QuestHudLayoutManager.scaledSize(QuestHudLayoutManager.Element.PINNED, PinnedQuestHudOverlay.width()),
                QuestHudLayoutManager.scaledHeight(QuestHudLayoutManager.Element.PINNED, pinnedBaseHeightSnapshot)
        );
    }

    private int baseWidth(QuestHudLayoutManager.Element element) {
        return element == QuestHudLayoutManager.Element.COMPLETION ? QuestCompletionNotificationOverlay.width() : PinnedQuestHudOverlay.width();
    }

    private int baseHeight(QuestHudLayoutManager.Element element) {
        return element == QuestHudLayoutManager.Element.COMPLETION ? QuestCompletionNotificationOverlay.height() : pinnedBaseHeightSnapshot;
    }

    private QuestHudLayoutManager.HudBox slotBox(QuestHudLayoutManager.HudBox visual, int screenWidth, int screenHeight) {
        int slotW = CanvasGeometry.slotSpanForVisualSize(visual.width());
        int slotH = CanvasGeometry.slotSpanForVisualSize(visual.height());
        int insetX = CanvasGeometry.visualInsetForSlot(slotW, visual.width());
        int insetY = CanvasGeometry.visualInsetForSlot(slotH, visual.height());
        return clampedSlot(
                snapSlot(visual.x() - insetX),
                snapSlot(visual.y() - insetY),
                visual.width(),
                visual.height(),
                screenWidth,
                screenHeight
        );
    }

    private QuestHudLayoutManager.HudBox clampedSlot(int slotX, int slotY, int visualWidth, int visualHeight, int screenWidth, int screenHeight) {
        int slotW = CanvasGeometry.slotSpanForVisualSize(visualWidth);
        int slotH = CanvasGeometry.slotSpanForVisualSize(visualHeight);
        int x = Math.max(0, Math.min(snapSlot(slotX), snapFloor(Math.max(0, screenWidth - slotW))));
        int y = Math.max(0, Math.min(snapSlot(slotY), snapFloor(Math.max(0, screenHeight - slotH))));
        return new QuestHudLayoutManager.HudBox(x, y, slotW, slotH);
    }

    private static int snapSlot(int value) {
        return CanvasGeometry.snapValueToGrid(value, GRID_STEP);
    }

    private static int snapFloor(int value) {
        return Math.max(0, (value / GRID_STEP) * GRID_STEP);
    }

    private void applySizeFromVisual(QuestHudLayoutManager.Element element, int visualWidth, int visualHeight) {
        int baseW = baseWidth(element);
        int baseH = baseHeight(element);
        int widthPercent = Math.round(visualWidth * 100.0f / Math.max(1, baseW));
        int heightPercent = Math.round(visualHeight * 100.0f / Math.max(1, baseH));
        QuestHudLayoutManager.setSizePercent(element, widthPercent, heightPercent);
    }

    enum DragMode {
        NONE,
        MOVE,
        RESIZE
    }
}

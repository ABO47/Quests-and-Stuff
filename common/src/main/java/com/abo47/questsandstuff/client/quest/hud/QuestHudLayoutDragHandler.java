package com.abo47.questsandstuff.client.quest.hud;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class QuestHudLayoutDragHandler {
    private static final int GRID_STEP = 16;
    private static final int GRID_VISUAL_MARGIN = 1;
    private static final int HANDLE_SIZE = 6;

    private final QuestHudLayout.Snapshot original;

    private QuestHudLayout.Element selected;
    private QuestHudLayout.Element dragging;
    private QuestHudLayout.Element contextElement;
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
    private int contextX;
    private int contextY;
    private boolean closed;
    private boolean openingChild;
    private Button snapButton;

    public QuestHudLayoutDragHandler() {
        this.original = QuestHudLayout.snapshot();
    }

    public QuestHudLayout.Element selected() {
        return selected;
    }

    public void setSelected(QuestHudLayout.Element element) {
        this.selected = element;
    }

    public QuestHudLayout.Element contextElement() {
        return contextElement;
    }

    public void setContextElement(QuestHudLayout.Element element) {
        this.contextElement = element;
    }

    public int contextX() {
        return contextX;
    }

    public int contextY() {
        return contextY;
    }

    public void setContextPosition(int x, int y) {
        this.contextX = x;
        this.contextY = y;
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

    public QuestHudLayout.Snapshot original() {
        return original;
    }

    public QuestHudLayout.Element dragging() {
        return dragging;
    }

    public DragMode dragMode() {
        return dragMode;
    }

    public void setSnapButton(Button button) {
        this.snapButton = button;
    }

    public void snapAllElementsToGrid(int screenWidth, int screenHeight) {
        snapElementToGrid(QuestHudLayout.Element.COMPLETION, screenWidth, screenHeight);
        snapElementToGrid(QuestHudLayout.Element.PINNED, screenWidth, screenHeight);
    }

    private void snapElementToGrid(QuestHudLayout.Element element, int screenWidth, int screenHeight) {
        QuestHudLayout.HudBox raw = rawBoxFor(element, screenWidth, screenHeight);
        QuestHudLayout.HudBox slot = slotBox(raw);
        QuestHudLayout.HudBox visual = visualBoxInSlot(slot);
        applySizeFromVisual(element, visual.width(), visual.height());
        QuestHudLayout.setPosition(element, visual.x(), visual.y(), screenWidth, screenHeight, visual.width(), visual.height());
    }

    public void handleMouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (contextElement != null && !insideContext(mouseX, mouseY, screenWidth)) {
            contextElement = null;
        }
        QuestHudLayout.Element target = elementAt(mouseX, mouseY, screenWidth, screenHeight);
        selected = target;
        if (target == null) {
            return;
        }
        QuestHudLayout.HudBox box = slotBoxFor(target, screenWidth, screenHeight);
        dragging = target;
        if (resizeHandle(selectionBox(box)).contains(mouseX, mouseY)) {
            dragMode = DragMode.RESIZE;
            QuestHudLayout.HudBox raw = rawBoxFor(target, screenWidth, screenHeight);
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
            if (QuestHudLayout.snapToGrid()) {
                if (Screen.hasShiftDown()) {
                    float scale = Math.max(targetWidth / (float) Math.max(1, baseWidth), targetHeight / (float) Math.max(1, baseHeight));
                    targetWidth = Math.max(1, Math.round(baseWidth * scale));
                    targetHeight = Math.max(1, Math.round(baseHeight * scale));
                }
                QuestHudLayout.HudBox nextSlot = clampedSlot(resizeStartSlotX, resizeStartSlotY, targetWidth, targetHeight, screenWidth, screenHeight);
                QuestHudLayout.HudBox nextVisual = visualBoxInSlot(nextSlot);
                applySizeFromVisual(dragging, nextVisual.width(), nextVisual.height());
                QuestHudLayout.HudBox resized = rawBoxFor(dragging, screenWidth, screenHeight);
                nextSlot = clampedSlot(resizeStartSlotX, resizeStartSlotY, resized.width(), resized.height(), screenWidth, screenHeight);
                nextVisual = visualBoxInSlot(nextSlot);
                applySizeFromVisual(dragging, nextVisual.width(), nextVisual.height());
                QuestHudLayout.setPosition(dragging, nextVisual.x(), nextVisual.y(), screenWidth, screenHeight, nextVisual.width(), nextVisual.height());
                return true;
            }
            if (Screen.hasShiftDown()) {
                float scale = Math.max(targetWidth / (float) Math.max(1, baseWidth), targetHeight / (float) Math.max(1, baseHeight));
                int percent = Math.round(scale * 100.0f);
                QuestHudLayout.setScalePercent(dragging, percent);
            } else {
                int widthPercent = Math.round(targetWidth * 100.0f / Math.max(1, baseWidth));
                int heightPercent = Math.round(targetHeight * 100.0f / Math.max(1, baseHeight));
                QuestHudLayout.setSizePercent(dragging, widthPercent, heightPercent);
            }
            QuestHudLayout.HudBox resized = rawBoxFor(dragging, screenWidth, screenHeight);
            QuestHudLayout.HudBox nextVisual = new QuestHudLayout.HudBox(resizeStartX, resizeStartY, resized.width(), resized.height());
            QuestHudLayout.setPosition(dragging, nextVisual.x(), nextVisual.y(), screenWidth, screenHeight, nextVisual.width(), nextVisual.height());
            return true;
        }
        int nextX = (int) Math.round(mouseX) - dragOffsetX;
        int nextY = (int) Math.round(mouseY) - dragOffsetY;
        if (QuestHudLayout.snapToGrid()) {
            nextX = snapSlot(nextX);
            nextY = snapSlot(nextY);
        }
        QuestHudLayout.HudBox raw = rawBoxFor(dragging, screenWidth, screenHeight);
        QuestHudLayout.HudBox nextSlot = QuestHudLayout.snapToGrid()
                ? clampedSlot(nextX, nextY, raw.width(), raw.height(), screenWidth, screenHeight)
                : new QuestHudLayout.HudBox(nextX, nextY, raw.width(), raw.height());
        QuestHudLayout.HudBox nextVisual = QuestHudLayout.snapToGrid() ? visualBoxInSlot(nextSlot) : nextSlot;
        QuestHudLayout.setPosition(dragging, nextVisual.x(), nextVisual.y(), screenWidth, screenHeight, nextVisual.width(), nextVisual.height());
        return true;
    }

    public void handleMouseReleased() {
        dragging = null;
        dragMode = DragMode.NONE;
    }

    public void saveAndClose() {
        QuestHudLayout.save();
        closed = true;
        Minecraft.getInstance().setScreen(null);
    }

    public void cancelAndClose() {
        QuestHudLayout.restore(original);
        closed = true;
        Minecraft.getInstance().setScreen(null);
    }

    public QuestHudLayout.Element elementAt(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        if (slotBoxFor(QuestHudLayout.Element.PINNED, screenWidth, screenHeight).contains(mouseX, mouseY)) {
            return QuestHudLayout.Element.PINNED;
        }
        if (slotBoxFor(QuestHudLayout.Element.COMPLETION, screenWidth, screenHeight).contains(mouseX, mouseY)) {
            return QuestHudLayout.Element.COMPLETION;
        }
        return null;
    }

    public QuestHudLayout.HudBox visualBoxFor(QuestHudLayout.Element element, int screenWidth, int screenHeight) {
        QuestHudLayout.HudBox raw = rawBoxFor(element, screenWidth, screenHeight);
        if (!QuestHudLayout.snapToGrid()) {
            return raw;
        }
        QuestHudLayout.HudBox slot = slotBox(raw);
        return visualBoxInSlot(slot);
    }

    public QuestHudLayout.HudBox slotBoxFor(QuestHudLayout.Element element, int screenWidth, int screenHeight) {
        QuestHudLayout.HudBox raw = rawBoxFor(element, screenWidth, screenHeight);
        return QuestHudLayout.snapToGrid() ? slotBox(raw) : raw;
    }

    public QuestHudLayout.HudBox rawBoxFor(QuestHudLayout.Element element, int screenWidth, int screenHeight) {
        return element == QuestHudLayout.Element.COMPLETION
                ? completionBox(screenWidth, screenHeight)
                : pinnedBox(screenWidth, screenHeight);
    }

    public QuestHudLayout.HudBox visualBoxInSlot(QuestHudLayout.HudBox slot) {
        return QuestHudLayout.visualBoxInSlot(slot, GRID_VISUAL_MARGIN);
    }

    public static QuestHudLayout.HudBox selectionBox(QuestHudLayout.HudBox box) {
        return QuestHudLayout.snapToGrid() ? QuestHudLayout.visualBoxInSlot(box) : box;
    }

    public QuestHudLayout.HudBox resizeHandle(QuestHudLayout.HudBox box) {
        return new QuestHudLayout.HudBox(box.x() + box.width() - HANDLE_SIZE, box.y() + box.height() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
    }

    public Component snapLabel() {
        return Component.translatable(QuestHudLayout.snapToGrid()
                ? "ui.questsandstuff.hud.layout.snap_on"
                : "ui.questsandstuff.hud.layout.snap_off");
    }

    public void updateSnapButton() {
        if (snapButton != null) {
            snapButton.setMessage(snapLabel());
        }
    }

    private QuestHudLayout.HudBox completionBox(int screenWidth, int screenHeight) {
        return QuestHudLayout.completionBox(
                screenWidth,
                screenHeight,
                QuestHudLayout.scaledSize(QuestHudLayout.Element.COMPLETION, QuestCompletionNotificationOverlay.width()),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.COMPLETION, QuestCompletionNotificationOverlay.height())
        );
    }

    private QuestHudLayout.HudBox pinnedBox(int screenWidth, int screenHeight) {
        return QuestHudLayout.pinnedBox(
                screenWidth,
                screenHeight,
                QuestHudLayout.scaledSize(QuestHudLayout.Element.PINNED, PinnedQuestHudOverlay.width()),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.PINNED, PinnedQuestHudOverlay.currentStackHeight())
        );
    }

    private int baseWidth(QuestHudLayout.Element element) {
        return element == QuestHudLayout.Element.COMPLETION ? QuestCompletionNotificationOverlay.width() : PinnedQuestHudOverlay.width();
    }

    private int baseHeight(QuestHudLayout.Element element) {
        return element == QuestHudLayout.Element.COMPLETION ? QuestCompletionNotificationOverlay.height() : PinnedQuestHudOverlay.currentStackHeight();
    }

    private QuestHudLayout.HudBox slotBox(QuestHudLayout.HudBox visual) {
        return clampedSlot(
                snapSlot(visual.x() - GRID_VISUAL_MARGIN),
                snapSlot(visual.y() - GRID_VISUAL_MARGIN),
                visual.width(),
                visual.height(),
                0,
                0
        );
    }

    private QuestHudLayout.HudBox clampedSlot(int slotX, int slotY, int visualWidth, int visualHeight, int screenWidth, int screenHeight) {
        int slotW = CanvasGeometry.slotSpanForVisualSize(visualWidth);
        int slotH = CanvasGeometry.slotSpanForVisualSize(visualHeight);
        int x = Math.max(0, Math.min(snapSlot(slotX), snapFloor(Math.max(0, screenWidth - slotW))));
        int y = Math.max(0, Math.min(snapSlot(slotY), snapFloor(Math.max(0, screenHeight - slotH))));
        return new QuestHudLayout.HudBox(x, y, slotW, slotH);
    }

    private static int snapSlot(int value) {
        return CanvasGeometry.snapValueToGrid(value, GRID_STEP);
    }

    private static int snapFloor(int value) {
        return Math.max(0, (value / GRID_STEP) * GRID_STEP);
    }

    private void applySizeFromVisual(QuestHudLayout.Element element, int visualWidth, int visualHeight) {
        int baseW = baseWidth(element);
        int baseH = baseHeight(element);
        int widthPercent = Math.round(visualWidth * 100.0f / Math.max(1, baseW));
        int heightPercent = Math.round(visualHeight * 100.0f / Math.max(1, baseH));
        QuestHudLayout.setSizePercent(element, widthPercent, heightPercent);
    }

    private boolean insideContext(double mouseX, double mouseY, int screenWidth) {
        int menuW = contextMenuW();
        return QuestHudLayoutEditScreen.inside(mouseX, mouseY, contextMenuX(screenWidth), contextMenuY(screenWidth), menuW, QuestHudLayoutEditScreen.contextMenuH());
    }

    private int contextMenuX(int screenWidth) {
        int menuW = contextMenuW();
        return Math.max(4, Math.min(contextX, screenWidth - menuW - 4));
    }

    private int contextMenuY(int screenHeight) {
        int menuH = QuestHudLayoutEditScreen.contextMenuH();
        return Math.max(4, Math.min(contextY, screenHeight - menuH - 4));
    }

    private static int contextMenuW() {
        return 160;
    }

    enum DragMode {
        NONE,
        MOVE,
        RESIZE
    }
}

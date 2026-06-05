package com.abo47.questsandstuff.client.hud;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuSystem;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public final class QuestHudLayoutEditScreen extends Screen {
    private static final int GRID_STEP = 16;
    private static final int GRID_VISUAL_MARGIN = 1;
    private static final int BUTTON_W = 64;
    private static final int BUTTON_H = 20;
    private static final int BUTTON_GAP = 8;
    private static final int HANDLE_SIZE = 6;

    private final QuestHudLayout.Snapshot original;
    private QuestHudLayout.Element selected = QuestHudLayout.Element.PINNED;
    private QuestHudLayout.Element dragging;
    private QuestHudLayout.Element contextElement;
    private DragMode dragMode = DragMode.NONE;
    private Button snapButton;
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

    public QuestHudLayoutEditScreen() {
        super(Component.translatable("ui.questsandstuff.hud.layout.title"));
        this.original = QuestHudLayout.snapshot();
    }

    void returnFromChild() {
        openingChild = false;
    }

    @Override
    protected void init() {
        if (QuestHudLayout.snapToGrid()) {
            snapAllElementsToGrid();
        }
        int totalW = BUTTON_W * 4 + BUTTON_GAP * 3;
        int startX = width / 2 - totalW / 2;
        int y = Math.max(8, height - 44);
        addRenderableWidget(Button.builder(Component.translatable("ui.questsandstuff.common.save"), button -> saveAndClose())
                .bounds(startX, y, BUTTON_W, BUTTON_H)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("ui.questsandstuff.hud.layout.reset"), button -> {
                    QuestHudLayout.resetToDefaults();
                    snapAllElementsToGrid();
                    selected = null;
                    contextElement = null;
                    updateSnapButton();
                })
                .bounds(startX + BUTTON_W + BUTTON_GAP, y, BUTTON_W, BUTTON_H)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("ui.questsandstuff.common.cancel"), button -> cancelAndClose())
                .bounds(startX + (BUTTON_W + BUTTON_GAP) * 2, y, BUTTON_W, BUTTON_H)
                .build());
        snapButton = addRenderableWidget(Button.builder(snapLabel(), button -> {
                    boolean enabled = !QuestHudLayout.snapToGrid();
                    QuestHudLayout.setSnapToGrid(enabled);
                    if (enabled) {
                        snapAllElementsToGrid();
                    }
                    updateSnapButton();
                })
                .bounds(startX + (BUTTON_W + BUTTON_GAP) * 3, y, BUTTON_W, BUTTON_H)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        QuestHudOverlayRenderer.resetGuiState(graphics);
        renderEditSurface(graphics);
        renderHudPreviews(graphics, mouseX, mouseY);
        QuestHudOverlayRenderer.resetGuiState(graphics);
        if (contextElement != null) {
            renderContextMenu(graphics, mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && contextElement != null && handleContextClick(mouseX, mouseY)) {
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1) {
            QuestHudLayout.Element target = elementAt(mouseX, mouseY);
            if (target != null) {
                selected = target;
                contextElement = target;
                contextX = (int) Math.round(mouseX);
                contextY = (int) Math.round(mouseY);
                return true;
            }
            contextElement = null;
            return false;
        }
        if (button != 0) {
            return false;
        }
        if (contextElement != null && !insideContext(mouseX, mouseY)) {
            contextElement = null;
        }
        QuestHudLayout.Element target = elementAt(mouseX, mouseY);
        selected = target;
        if (target == null) {
            return false;
        }
        QuestHudLayout.HudBox box = slotBoxFor(target);
        dragging = target;
        if (resizeHandle(selectionBox(box)).contains(mouseX, mouseY)) {
            dragMode = DragMode.RESIZE;
            QuestHudLayout.HudBox raw = rawBoxFor(target);
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
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging == null || button != 0) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
                QuestHudLayout.HudBox nextSlot = clampedSlot(resizeStartSlotX, resizeStartSlotY, targetWidth, targetHeight);
                QuestHudLayout.HudBox nextVisual = visualBoxInSlot(nextSlot);
                applySizeFromVisual(dragging, nextVisual.width(), nextVisual.height());
                QuestHudLayout.HudBox resized = rawBoxFor(dragging);
                nextSlot = clampedSlot(resizeStartSlotX, resizeStartSlotY, resized.width(), resized.height());
                nextVisual = visualBoxInSlot(nextSlot);
                applySizeFromVisual(dragging, nextVisual.width(), nextVisual.height());
                QuestHudLayout.setPosition(dragging, nextVisual.x(), nextVisual.y(), width, height, nextVisual.width(), nextVisual.height());
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
            QuestHudLayout.HudBox resized = rawBoxFor(dragging);
            QuestHudLayout.HudBox nextVisual = new QuestHudLayout.HudBox(resizeStartX, resizeStartY, resized.width(), resized.height());
            QuestHudLayout.setPosition(dragging, nextVisual.x(), nextVisual.y(), width, height, nextVisual.width(), nextVisual.height());
            return true;
        }
        int nextX = (int) Math.round(mouseX) - dragOffsetX;
        int nextY = (int) Math.round(mouseY) - dragOffsetY;
        if (QuestHudLayout.snapToGrid()) {
            nextX = snapSlot(nextX);
            nextY = snapSlot(nextY);
        }
        QuestHudLayout.HudBox raw = rawBoxFor(dragging);
        QuestHudLayout.HudBox nextSlot = QuestHudLayout.snapToGrid()
                ? clampedSlot(nextX, nextY, raw.width(), raw.height())
                : new QuestHudLayout.HudBox(nextX, nextY, raw.width(), raw.height());
        QuestHudLayout.HudBox nextVisual = QuestHudLayout.snapToGrid() ? visualBoxInSlot(nextSlot) : nextSlot;
        QuestHudLayout.setPosition(dragging, nextVisual.x(), nextVisual.y(), width, height, nextVisual.width(), nextVisual.height());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        dragMode = DragMode.NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        cancelAndClose();
    }

    @Override
    public void removed() {
        if (!closed && !openingChild) {
            QuestHudLayout.restore(original);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderEditSurface(GuiGraphics graphics) {
        graphics.fill(0, 0, width, height, TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 86));
        int lightLine = TabletUiFactory.withAlpha(ModColors.BORDER_BASE, 62);
        int strongLine = TabletUiFactory.withAlpha(ModColors.BORDER_ACCENT, 78);
        for (int x = 0; x <= width; x += GRID_STEP) {
            graphics.fill(x, 0, x + 1, height, x % (GRID_STEP * 4) == 0 ? strongLine : lightLine);
        }
        for (int y = 0; y <= height; y += GRID_STEP) {
            graphics.fill(0, y, width, y + 1, y % (GRID_STEP * 4) == 0 ? strongLine : lightLine);
        }
        Minecraft minecraft = Minecraft.getInstance();
        String title = getTitle().getString();
        graphics.drawString(minecraft.font, title, width / 2 - minecraft.font.width(title) / 2, 10, TabletUiFactory.withAlpha(ModColors.TEXT_PRIMARY, 230), false);
    }

    private void renderHudPreviews(GuiGraphics graphics, int mouseX, int mouseY) {
        QuestHudLayout.HudBox completion = visualBoxFor(QuestHudLayout.Element.COMPLETION);
        QuestHudLayout.HudBox pinned = visualBoxFor(QuestHudLayout.Element.PINNED);
        QuestHudLayout.HudBox completionSlot = slotBoxFor(QuestHudLayout.Element.COMPLETION);
        QuestHudLayout.HudBox pinnedSlot = slotBoxFor(QuestHudLayout.Element.PINNED);
        QuestHudLayout.Element hovered = elementAt(mouseX, mouseY);
        boolean completionSelected = selected == QuestHudLayout.Element.COMPLETION || hovered == QuestHudLayout.Element.COMPLETION;
        boolean pinnedSelected = selected == QuestHudLayout.Element.PINNED || hovered == QuestHudLayout.Element.PINNED;
        QuestCompletionNotificationOverlay.renderPreview(graphics, completion.x(), completion.y(), completion.width(), completion.height(), false);
        PinnedQuestHudOverlay.renderPreview(graphics, pinned.x(), pinned.y(), pinned.width(), pinned.height(), false);
        if (completionSelected) {
            QuestHudLayout.HudBox selection = selectionBox(completionSlot);
            drawSelectionSlot(graphics, selection, QuestHudLayout.Element.COMPLETION);
            drawResizeHandle(graphics, selection);
        }
        if (pinnedSelected) {
            QuestHudLayout.HudBox selection = selectionBox(pinnedSlot);
            drawSelectionSlot(graphics, selection, QuestHudLayout.Element.PINNED);
            drawResizeHandle(graphics, selection);
        }
    }

    private void renderContextMenu(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = contextMenuX();
        int y = contextMenuY();
        int menuW = contextMenuW();
        List<ContextAction> actions = contextActions();
        ContextMenuSystem.drawVanillaPanel(graphics, x, y, menuW, contextMenuH(actions), ModColors.BORDER_ACCENT);
        int rowY = y + ContextMenuSystem.outerPad();
        int rowW = menuW - ContextMenuSystem.outerPad() * 2;
        for (int i = 0; i < actions.size(); i++) {
            ContextAction action = actions.get(i);
            boolean hovered = inside(mouseX, mouseY, x + ContextMenuSystem.outerPad(), rowY + i * ContextMenuSystem.rowHeight(), rowW, ContextMenuSystem.rowHeight());
            ContextMenuSystem.drawVanillaContextRow(graphics, x, rowY + i * ContextMenuSystem.rowHeight(), rowW, action.label(), action.icon(), action.accentColor(), hovered);
        }
    }

    private void drawResizeHandle(GuiGraphics graphics, QuestHudLayout.HudBox box) {
        QuestHudLayout.HudBox handle = resizeHandle(box);
        graphics.fill(handle.x(), handle.y(), handle.x() + handle.width(), handle.y() + handle.height(), TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 220));
        graphics.renderOutline(handle.x(), handle.y(), handle.width(), handle.height(), ModColors.SUCCESS);
    }

    private void drawSelectionSlot(GuiGraphics graphics, QuestHudLayout.HudBox box, QuestHudLayout.Element element) {
        float opacity = QuestHudLayout.opacityPercent(element) / 100.0f;
        int fillAlpha = Math.round(18.0f * opacity);
        int outlineAlpha = Math.round(185.0f * opacity);
        if (fillAlpha > 0) {
            graphics.fill(box.x(), box.y(), box.x() + box.width(), box.y() + box.height(), TabletUiFactory.withAlpha(ModColors.INTERACTIVE, fillAlpha));
        }
        if (outlineAlpha > 0) {
            graphics.renderOutline(box.x(), box.y(), box.width(), box.height(), TabletUiFactory.withAlpha(ModColors.SUCCESS, outlineAlpha));
        }
    }

    private boolean handleContextClick(double mouseX, double mouseY) {
        if (!insideContext(mouseX, mouseY)) {
            contextElement = null;
            return false;
        }
        int x = contextMenuX();
        int y = contextMenuY();
        List<ContextAction> actions = contextActions();
        int row = contextRowAt(mouseX, mouseY, x, y, contextMenuW(), actions.size());
        if (row >= 0 && row < actions.size()) {
            actions.get(row).action().run();
            if (actions.get(row).closeAfterClick()) {
                contextElement = null;
            }
            return true;
        }
        return true;
    }

    private QuestHudLayout.Element elementAt(double mouseX, double mouseY) {
        if (slotBoxFor(QuestHudLayout.Element.PINNED).contains(mouseX, mouseY)) {
            return QuestHudLayout.Element.PINNED;
        }
        if (slotBoxFor(QuestHudLayout.Element.COMPLETION).contains(mouseX, mouseY)) {
            return QuestHudLayout.Element.COMPLETION;
        }
        return null;
    }

    private QuestHudLayout.HudBox visualBoxFor(QuestHudLayout.Element element) {
        QuestHudLayout.HudBox raw = rawBoxFor(element);
        if (!QuestHudLayout.snapToGrid()) {
            return raw;
        }
        QuestHudLayout.HudBox slot = slotBox(raw);
        return visualBoxInSlot(slot);
    }

    private QuestHudLayout.HudBox slotBoxFor(QuestHudLayout.Element element) {
        QuestHudLayout.HudBox raw = rawBoxFor(element);
        return QuestHudLayout.snapToGrid() ? slotBox(raw) : raw;
    }

    private QuestHudLayout.HudBox rawBoxFor(QuestHudLayout.Element element) {
        return element == QuestHudLayout.Element.COMPLETION ? completionBox() : pinnedBox();
    }

    private QuestHudLayout.HudBox completionBox() {
        return QuestHudLayout.completionBox(
                width,
                height,
                QuestHudLayout.scaledSize(QuestHudLayout.Element.COMPLETION, QuestCompletionNotificationOverlay.width()),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.COMPLETION, QuestCompletionNotificationOverlay.height())
        );
    }

    private QuestHudLayout.HudBox pinnedBox() {
        return QuestHudLayout.pinnedBox(
                width,
                height,
                QuestHudLayout.scaledSize(QuestHudLayout.Element.PINNED, PinnedQuestHudOverlay.width()),
                QuestHudLayout.scaledHeight(QuestHudLayout.Element.PINNED, PinnedQuestHudOverlay.currentStackHeight())
        );
    }

    private QuestHudLayout.HudBox resizeHandle(QuestHudLayout.HudBox box) {
        return new QuestHudLayout.HudBox(box.x() + box.width() - HANDLE_SIZE, box.y() + box.height() - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
    }

    private int baseWidth(QuestHudLayout.Element element) {
        return element == QuestHudLayout.Element.COMPLETION ? QuestCompletionNotificationOverlay.width() : PinnedQuestHudOverlay.width();
    }

    private int baseHeight(QuestHudLayout.Element element) {
        return element == QuestHudLayout.Element.COMPLETION ? QuestCompletionNotificationOverlay.height() : PinnedQuestHudOverlay.currentStackHeight();
    }

    private int contextMenuX() {
        int menuW = contextMenuW();
        return Math.max(4, Math.min(contextX, width - menuW - 4));
    }

    private int contextMenuY() {
        int menuH = contextMenuH(contextActions());
        return Math.max(4, Math.min(contextY, height - menuH - 4));
    }

    private boolean insideContext(double mouseX, double mouseY) {
        return inside(mouseX, mouseY, contextMenuX(), contextMenuY(), contextMenuW(), contextMenuH(contextActions()));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private QuestHudLayout.HudBox slotBox(QuestHudLayout.HudBox visual) {
        QuestHudLayout.HudBox slot = clampedSlot(
                snapSlot(visual.x() - GRID_VISUAL_MARGIN),
                snapSlot(visual.y() - GRID_VISUAL_MARGIN),
                visual.width(),
                visual.height()
        );
        return slot;
    }

    private QuestHudLayout.HudBox clampedSlot(int slotX, int slotY, int visualWidth, int visualHeight) {
        int slotW = CanvasGeometry.slotSpanForVisualSize(visualWidth);
        int slotH = CanvasGeometry.slotSpanForVisualSize(visualHeight);
        int x = Math.max(0, Math.min(snapSlot(slotX), snapFloor(Math.max(0, width - slotW))));
        int y = Math.max(0, Math.min(snapSlot(slotY), snapFloor(Math.max(0, height - slotH))));
        return new QuestHudLayout.HudBox(x, y, slotW, slotH);
    }

    private static QuestHudLayout.HudBox visualBoxInSlot(QuestHudLayout.HudBox slot) {
        return new QuestHudLayout.HudBox(
                slot.x() + GRID_VISUAL_MARGIN,
                slot.y() + GRID_VISUAL_MARGIN,
                Math.max(1, slot.width() - GRID_VISUAL_MARGIN),
                Math.max(1, slot.height() - GRID_VISUAL_MARGIN)
        );
    }

    private static int snapSlot(int value) {
        return CanvasGeometry.snapValueToGrid(value, GRID_STEP);
    }

    private static int snapFloor(int value) {
        return Math.max(0, (value / GRID_STEP) * GRID_STEP);
    }

    private static QuestHudLayout.HudBox selectionBox(QuestHudLayout.HudBox box) {
        return QuestHudLayout.snapToGrid() ? visualBoxInSlot(box) : box;
    }

    private List<ContextAction> contextActions() {
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.action(Component.translatable("ui.questsandstuff.hud.change_background").getString(), "background", ModColors.INTERACTIVE, () -> {
            openingChild = true;
            if (!QuestHudAssetLibraryBridge.open(this, contextElement)) {
                openingChild = false;
            }
        }));
        actions.add(ContextActions.action(Component.translatable("ui.questsandstuff.hud.remove_background").getString(), "delete", ModColors.WARNING, () -> QuestHudLayout.setBackground(contextElement, "")));
        return actions;
    }

    private int contextMenuW() {
        List<String> labels = contextActions().stream().map(ContextAction::label).toList();
        return ContextMenuSystem.preferredMenuWidth(labels, 132, 172);
    }

    private static int contextMenuH(List<ContextAction> actions) {
        return ContextMenuSystem.menuHeightForRows(actions.size());
    }

    private static int contextRowAt(double mouseX, double mouseY, int x, int y, int menuW, int rows) {
        int rowX = x + ContextMenuSystem.outerPad();
        int rowY = y + ContextMenuSystem.outerPad();
        int rowW = menuW - ContextMenuSystem.outerPad() * 2;
        if (!inside(mouseX, mouseY, rowX, rowY, rowW, rows * ContextMenuSystem.rowHeight())) {
            return -1;
        }
        return ((int) Math.floor(mouseY) - rowY) / ContextMenuSystem.rowHeight();
    }

    private void snapAllElementsToGrid() {
        snapElementToGrid(QuestHudLayout.Element.COMPLETION);
        snapElementToGrid(QuestHudLayout.Element.PINNED);
    }

    private void snapElementToGrid(QuestHudLayout.Element element) {
        QuestHudLayout.HudBox raw = rawBoxFor(element);
        QuestHudLayout.HudBox slot = slotBox(raw);
        QuestHudLayout.HudBox visual = visualBoxInSlot(slot);
        applySizeFromVisual(element, visual.width(), visual.height());
        QuestHudLayout.setPosition(element, visual.x(), visual.y(), width, height, visual.width(), visual.height());
    }

    private void applySizeFromVisual(QuestHudLayout.Element element, int visualWidth, int visualHeight) {
        int baseWidth = baseWidth(element);
        int baseHeight = baseHeight(element);
        int widthPercent = Math.round(visualWidth * 100.0f / Math.max(1, baseWidth));
        int heightPercent = Math.round(visualHeight * 100.0f / Math.max(1, baseHeight));
        QuestHudLayout.setSizePercent(element, widthPercent, heightPercent);
    }

    private Component snapLabel() {
        return Component.translatable(QuestHudLayout.snapToGrid()
                ? "ui.questsandstuff.hud.layout.snap_on"
                : "ui.questsandstuff.hud.layout.snap_off");
    }

    private void updateSnapButton() {
        if (snapButton != null) {
            snapButton.setMessage(snapLabel());
        }
    }

    private void saveAndClose() {
        QuestHudLayout.save();
        closed = true;
        Minecraft.getInstance().setScreen(null);
    }

    private void cancelAndClose() {
        QuestHudLayout.restore(original);
        closed = true;
        Minecraft.getInstance().setScreen(null);
    }

    private enum DragMode {
        NONE,
        MOVE,
        RESIZE
    }
}

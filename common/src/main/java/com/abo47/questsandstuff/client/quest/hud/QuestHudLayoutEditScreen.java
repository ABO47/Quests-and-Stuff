package com.abo47.questsandstuff.client.quest.hud;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
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
    private static final int BUTTON_W = 64;
    private static final int BUTTON_H = 20;
    private static final int BUTTON_GAP = 8;

    private final QuestHudLayoutDragHandler dragHandler;

    public QuestHudLayoutEditScreen() {
        super(Component.translatable("ui.questsandstuff.hud.layout.title"));
        this.dragHandler = new QuestHudLayoutDragHandler();
    }

    void returnFromChild() {
        dragHandler.setOpeningChild(false);
    }

    @Override
    protected void init() {
        if (QuestHudLayoutManager.snapToGrid()) {
            dragHandler.snapAllElementsToGrid(width, height);
        }
        int totalW = BUTTON_W * 4 + BUTTON_GAP * 3;
        int startX = width / 2 - totalW / 2;
        int y = Math.max(8, height - 44);
        addRenderableWidget(Button.builder(Component.translatable("ui.questsandstuff.common.save"), button -> dragHandler.saveAndClose())
                .bounds(startX, y, BUTTON_W, BUTTON_H)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("ui.questsandstuff.hud.layout.reset"), button -> {
                    QuestHudLayoutManager.resetToDefaults();
                    dragHandler.snapAllElementsToGrid(width, height);
                    dragHandler.setSelected(null);
                    dragHandler.setContextElement(null);
                    dragHandler.updateSnapButton();
                })
                .bounds(startX + BUTTON_W + BUTTON_GAP, y, BUTTON_W, BUTTON_H)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("ui.questsandstuff.common.cancel"), button -> dragHandler.cancelAndClose())
                .bounds(startX + (BUTTON_W + BUTTON_GAP) * 2, y, BUTTON_W, BUTTON_H)
                .build());
        Button snapButton = addRenderableWidget(Button.builder(dragHandler.snapLabel(), button -> {
                    boolean enabled = !QuestHudLayoutManager.snapToGrid();
                    QuestHudLayoutManager.setSnapToGrid(enabled);
                    if (enabled) {
                        dragHandler.snapAllElementsToGrid(width, height);
                    }
                    dragHandler.updateSnapButton();
                })
                .bounds(startX + (BUTTON_W + BUTTON_GAP) * 3, y, BUTTON_W, BUTTON_H)
                .build());
        dragHandler.setSnapButton(snapButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        QuestHudOverlayRenderer.resetGuiState(graphics);
        renderEditSurface(graphics);
        renderHudPreviews(graphics, mouseX, mouseY);
        QuestHudOverlayRenderer.resetGuiState(graphics);
        if (dragHandler.contextElement() != null) {
            renderContextMenu(graphics, mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && dragHandler.contextElement() != null && handleContextClick(mouseX, mouseY)) {
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1) {
            QuestHudLayoutManager.Element target = dragHandler.elementAt(mouseX, mouseY, width, height);
            if (target != null) {
                dragHandler.setSelected(target);
                dragHandler.setContextElement(target);
                dragHandler.setContextPosition((int) Math.round(mouseX), (int) Math.round(mouseY));
                return true;
            }
            dragHandler.setContextElement(null);
            return false;
        }
        dragHandler.handleMouseClicked(mouseX, mouseY, button, width, height);
        return dragHandler.dragging() != null;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragHandler.dragging() != null && button == 0) {
            return dragHandler.handleMouseDragged(mouseX, mouseY, button, width, height);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragHandler.handleMouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        dragHandler.cancelAndClose();
    }

    @Override
    public void removed() {
        if (!dragHandler.isClosed() && !dragHandler.isOpeningChild()) {
            QuestHudLayoutManager.restore(dragHandler.original());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderEditSurface(GuiGraphics graphics) {
        SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 86)).draw(graphics, 0, 0, 0, 0, width, height);
        int lightLine = withAlpha(TabletColors.BORDER_BASE, 62);
        int strongLine = withAlpha(TabletColors.BORDER_ACCENT, 78);
        for (int x = 0; x <= width; x += 16) {
            SurfaceFactory.fill(x % (16 * 4) == 0 ? strongLine : lightLine).draw(graphics, 0, 0, x, 0, 1, height);
        }
        for (int y = 0; y <= height; y += 16) {
            SurfaceFactory.fill(y % (16 * 4) == 0 ? strongLine : lightLine).draw(graphics, 0, 0, 0, y, width, 1);
        }
        Minecraft minecraft = Minecraft.getInstance();
        String title = getTitle().getString();
        graphics.drawString(minecraft.font, title, width / 2 - minecraft.font.width(title) / 2, 10, withAlpha(TabletColors.TEXT_PRIMARY, 230), false);
    }

    private void renderHudPreviews(GuiGraphics graphics, int mouseX, int mouseY) {
        QuestHudLayoutManager.HudBox completion = dragHandler.visualBoxFor(QuestHudLayoutManager.Element.COMPLETION, width, height);
        QuestHudLayoutManager.HudBox pinned = dragHandler.visualBoxFor(QuestHudLayoutManager.Element.PINNED, width, height);
        QuestHudLayoutManager.HudBox completionSlot = dragHandler.slotBoxFor(QuestHudLayoutManager.Element.COMPLETION, width, height);
        QuestHudLayoutManager.HudBox pinnedSlot = dragHandler.slotBoxFor(QuestHudLayoutManager.Element.PINNED, width, height);
        QuestHudLayoutManager.Element hovered = dragHandler.elementAt(mouseX, mouseY, width, height);
        boolean completionSelected = dragHandler.selected() == QuestHudLayoutManager.Element.COMPLETION || hovered == QuestHudLayoutManager.Element.COMPLETION;
        boolean pinnedSelected = dragHandler.selected() == QuestHudLayoutManager.Element.PINNED || hovered == QuestHudLayoutManager.Element.PINNED;
        QuestCompletionNotificationOverlay.renderPreview(graphics, completion.x(), completion.y(), completion.width(), completion.height(), false);
        PinnedQuestHudOverlay.renderPreview(graphics, pinned.x(), pinned.y(), pinned.width(), pinned.height(), false);
        if (completionSelected) {
            QuestHudLayoutManager.HudBox selection = QuestHudLayoutDragHandler.selectionBox(completionSlot);
            drawSelectionSlot(graphics, selection, QuestHudLayoutManager.Element.COMPLETION);
            drawResizeHandle(graphics, selection);
        }
        if (pinnedSelected) {
            QuestHudLayoutManager.HudBox selection = QuestHudLayoutDragHandler.selectionBox(pinnedSlot);
            drawSelectionSlot(graphics, selection, QuestHudLayoutManager.Element.PINNED);
            drawResizeHandle(graphics, selection);
        }
    }

    private void renderContextMenu(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = contextMenuX();
        int y = contextMenuY();
        int menuW = contextMenuW();
        List<ContextAction> actions = contextActions();
        ContextMenuRenderer.drawVanillaPanel(graphics, x, y, menuW, contextMenuH(), TabletColors.BORDER_ACCENT);
        int rowY = y + ContextMenuRenderer.outerPad();
        int rowW = menuW - ContextMenuRenderer.outerPad() * 2;
        for (int i = 0; i < actions.size(); i++) {
            ContextAction action = actions.get(i);
            boolean hovered = inside(mouseX, mouseY, x + ContextMenuRenderer.outerPad(), rowY + i * ContextMenuRenderer.rowHeight(), rowW, ContextMenuRenderer.rowHeight());
            ContextMenuRenderer.drawVanillaContextRow(graphics, x, rowY + i * ContextMenuRenderer.rowHeight(), rowW, action.label(), action.icon(), action.accentColor(), hovered);
        }
    }

    private void drawResizeHandle(GuiGraphics graphics, QuestHudLayoutManager.HudBox box) {
        QuestHudLayoutManager.HudBox handle = dragHandler.resizeHandle(box);
        SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 220)).draw(graphics, 0, 0, handle.x(), handle.y(), handle.width(), handle.height());
        drawRectOutline(graphics, handle.x(), handle.y(), handle.width(), handle.height(), TabletColors.SUCCESS);
    }

    private void drawSelectionSlot(GuiGraphics graphics, QuestHudLayoutManager.HudBox box, QuestHudLayoutManager.Element element) {
        float opacity = QuestHudLayoutManager.opacityPercent(element) / 100.0f;
        int fillAlpha = Math.round(18.0f * opacity);
        int outlineAlpha = Math.round(185.0f * opacity);
        if (fillAlpha > 0) {
            SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, fillAlpha)).draw(graphics, 0, 0, box.x(), box.y(), box.width(), box.height());
        }
        if (outlineAlpha > 0) {
            drawRectOutline(graphics, box.x(), box.y(), box.width(), box.height(), withAlpha(TabletColors.SUCCESS, outlineAlpha));
        }
    }

    private boolean handleContextClick(double mouseX, double mouseY) {
        if (!insideContext(mouseX, mouseY)) {
            dragHandler.setContextElement(null);
            return false;
        }
        int x = contextMenuX();
        int y = contextMenuY();
        List<ContextAction> actions = contextActions();
        int row = contextRowAt(mouseX, mouseY, x, y, contextMenuW(), actions.size());
        if (row >= 0 && row < actions.size()) {
            actions.get(row).action().run();
            if (actions.get(row).closeAfterClick()) {
                dragHandler.setContextElement(null);
            }
            return true;
        }
        return true;
    }

    private int contextMenuX() {
        int menuW = contextMenuW();
        return Math.max(4, Math.min(dragHandler.contextX(), width - menuW - 4));
    }

    private int contextMenuY() {
        int menuH = contextMenuH();
        return Math.max(4, Math.min(dragHandler.contextY(), height - menuH - 4));
    }

    private boolean insideContext(double mouseX, double mouseY) {
        return inside(mouseX, mouseY, contextMenuX(), contextMenuY(), contextMenuW(), contextMenuH());
    }

    static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static int contextMenuW() {
        return ContextMenuRenderer.CONTEXT_MENU_WIDTH;
    }

    static int contextMenuH() {
        return ContextMenuRenderer.menuHeightForRows(2);
    }

    private static int contextRowAt(double mouseX, double mouseY, int x, int y, int menuW, int rows) {
        int rowX = x + ContextMenuRenderer.outerPad();
        int rowY = y + ContextMenuRenderer.outerPad();
        int rowW = menuW - ContextMenuRenderer.outerPad() * 2;
        if (!inside(mouseX, mouseY, rowX, rowY, rowW, rows * ContextMenuRenderer.rowHeight())) {
            return -1;
        }
        return ((int) Math.floor(mouseY) - rowY) / ContextMenuRenderer.rowHeight();
    }

    private List<ContextAction> contextActions() {
        List<ContextAction> actions = new ArrayList<>();
        QuestHudLayoutManager.Element contextElement = dragHandler.contextElement();
        actions.add(ContextActionFactory.action(Component.translatable("ui.questsandstuff.hud.change_background").getString(), "background", TabletColors.INTERACTIVE, () -> {
            dragHandler.setOpeningChild(true);
            if (!QuestHudAssetLibraryBridge.open(this, contextElement)) {
                dragHandler.setOpeningChild(false);
            }
        }));
        actions.add(ContextActionFactory.action(Component.translatable("ui.questsandstuff.hud.remove_background").getString(), "delete", TabletColors.WARNING, () -> QuestHudLayoutManager.setBackground(contextElement, "")));
        return actions;
    }
}

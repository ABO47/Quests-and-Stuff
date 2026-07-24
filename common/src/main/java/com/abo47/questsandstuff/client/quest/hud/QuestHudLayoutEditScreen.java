package com.abo47.questsandstuff.client.quest.hud;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.contextmenu.ActionTone;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class QuestHudLayoutEditScreen extends Screen {


    private final QuestHudLayoutDragHandler dragHandler;
    private WidgetGroup contextMenuWidget;
    private List<ContextAction> contextMenuActions;
    private QuestHudLayoutManager.Element contextElement;
    private ThemedButton snapButton;
    private String hudDeleteConfirmKey = "";

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
        int totalW = HudConstants.EDIT_BUTTON_W * 4 + HudConstants.EDIT_BUTTON_GAP * 3;
        int startX = width / 2 - totalW / 2;
        int y = Math.max(8, height - 44);
        addRenderableWidget(new ThemedButton(startX, y, HudConstants.EDIT_BUTTON_W, HudConstants.EDIT_BUTTON_H,
                Component.translatable("ui.questsandstuff.common.save"),
                "save", TabletColors.SUCCESS, button -> dragHandler.saveAndClose()));
        addRenderableWidget(new ThemedButton(startX + HudConstants.EDIT_BUTTON_W + HudConstants.EDIT_BUTTON_GAP, y, HudConstants.EDIT_BUTTON_W, HudConstants.EDIT_BUTTON_H,
                Component.translatable("ui.questsandstuff.hud.layout.reset"),
                "refresh", TabletColors.WARNING, button -> {
            QuestHudLayoutManager.resetToDefaults();
            dragHandler.snapAllElementsToGrid(width, height);
            dragHandler.setSelected(null);
            closeContextMenu();
            updateSnapButtonLabel();
        }));
        addRenderableWidget(new ThemedButton(startX + (HudConstants.EDIT_BUTTON_W + HudConstants.EDIT_BUTTON_GAP) * 2, y, HudConstants.EDIT_BUTTON_W, HudConstants.EDIT_BUTTON_H,
                Component.translatable("ui.questsandstuff.common.cancel"),
                "close", TabletColors.ERROR, button -> dragHandler.cancelAndClose()));
        snapButton = new ThemedButton(startX + (HudConstants.EDIT_BUTTON_W + HudConstants.EDIT_BUTTON_GAP) * 3, y, HudConstants.EDIT_BUTTON_W, HudConstants.EDIT_BUTTON_H,
                snapLabel(), "grid", TabletColors.INTERACTIVE, button -> {
            boolean enabled = !QuestHudLayoutManager.snapToGrid();
            QuestHudLayoutManager.setSnapToGrid(enabled);
            if (enabled) {
                dragHandler.snapAllElementsToGrid(width, height);
            }
            updateSnapButtonLabel();
        });
        addRenderableWidget(snapButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        QuestHudOverlayRenderer.resetGuiState(graphics);
        renderEditSurface(graphics);
        renderHudPreviews(graphics, mouseX, mouseY);
        QuestHudOverlayRenderer.resetGuiState(graphics);
        if (contextMenuWidget != null) {
            contextMenuWidget.drawInBackground(graphics, mouseX, mouseY, partialTick);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && contextMenuWidget != null && contextMenuActions != null) {
            if (ContextMenuPanel.click(contextMenuActions, 0, contextMenuActions.size(),
                    contextMenuWidget.getPositionX(), contextMenuWidget.getPositionY(),
                    contextMenuWidget.getSizeWidth(), (int) Math.round(mouseX), (int) Math.round(mouseY),
                    null, action -> closeContextMenu(), ContextMenuAnimationBridge.DEFAULT_KEY)) {
                return true;
            }
            closeContextMenu();
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1) {
            QuestHudLayoutManager.Element target = dragHandler.elementAt(mouseX, mouseY, width, height);
            if (target != null) {
                dragHandler.setSelected(target);
                openContextMenu(target, (int) Math.round(mouseX), (int) Math.round(mouseY));
                return true;
            }
            closeContextMenu();
            return false;
        }
        dragHandler.handleMouseClicked(mouseX, mouseY, button, width, height);
        return dragHandler.dragging() != null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (dragHandler.selected() != null) {
            int dx = 0;
            int dy = 0;
            if (keyCode == 262) {
                dx = 1;
            } else if (keyCode == 263) {
                dx = -1;
            } else if (keyCode == 264) {
                dy = 1;
            } else if (keyCode == 265) {
                dy = -1;
            }
            if (dx != 0 || dy != 0) {
                int step = hasShiftDown() ? 16 : 1;
                dx *= step;
                dy *= step;
                QuestHudLayoutManager.Element el = dragHandler.selected();
                QuestHudLayoutManager.HudBox raw = dragHandler.rawBoxFor(el, width, height);
                QuestHudLayoutManager.setPosition(el, raw.x() + dx, raw.y() + dy, width, height, raw.width(), raw.height());
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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

    private void openContextMenu(QuestHudLayoutManager.Element element, int mouseX, int mouseY) {
        contextElement = element;
        contextMenuActions = contextActions();
        if (contextMenuActions.isEmpty()) {
            contextMenuWidget = null;
            return;
        }
        int menuW = ContextMenuRenderer.CONTEXT_MENU_WIDTH;
        int menuH = ContextMenuPanel.heightFor(contextMenuActions, contextMenuActions.size());
        int menuX = Math.max(4, Math.min(mouseX, width - menuW - 4));
        int menuY = Math.max(4, Math.min(mouseY, height - menuH - 4));
        contextMenuWidget = ContextMenuPanel.build(menuX, menuY, menuW, contextMenuActions, 0, contextMenuActions.size(), TabletColors.BORDER_ACCENT, null, null, width, height);
    }

    private void closeContextMenu() {
        contextMenuWidget = null;
        contextMenuActions = null;
        contextElement = null;
        hudDeleteConfirmKey = "";
    }

    private List<ContextAction> contextActions() {
        List<ContextAction> actions = new ArrayList<>();
        QuestHudLayoutManager.Element target = contextElement;
        if (target == null) {
            return actions;
        }
        actions.add(ContextActionFactory.action(
                Component.translatable("ui.questsandstuff.hud.change_background").getString(),
                "background", ActionTone.PRIMARY, () -> {
            dragHandler.setOpeningChild(true);
            if (!QuestHudAssetLibraryBridge.open(this, target)) {
                dragHandler.setOpeningChild(false);
            }
        }));
        if (!QuestHudLayoutManager.background(target).isBlank()) {
            String deleteKey = "hud_remove_bg:" + target.name();
            boolean confirming = deleteKey.equals(hudDeleteConfirmKey);
            String label = confirming ? "Sure?" : Component.translatable("ui.questsandstuff.hud.remove_background").getString();
            actions.add(new ContextAction(label, "delete", ActionTone.WARNING, confirming, () -> {
                if (confirming) {
                    hudDeleteConfirmKey = "";
                    QuestHudLayoutManager.setBackground(target, "");
                } else {
                    hudDeleteConfirmKey = deleteKey;
                }
            }));
        }
        boolean bordersShown = QuestHudLayoutManager.showBorders(target);
        if (bordersShown) {
            actions.add(ContextActionFactory.action(
                    Component.translatable("ui.questsandstuff.hud.hide_borders").getString(),
                    "eye_off", ActionTone.NEUTRAL, () -> QuestHudLayoutManager.setShowBorders(target, false)));
        } else {
            actions.add(ContextActionFactory.action(
                    Component.translatable("ui.questsandstuff.hud.show_borders").getString(),
                    "eye", ActionTone.NEUTRAL, () -> QuestHudLayoutManager.setShowBorders(target, true)));
        }
        return actions;
    }

    private void updateSnapButtonLabel() {
        if (snapButton != null) {
            snapButton.setMessage(snapLabel());
        }
    }

    private Component snapLabel() {
        return Component.translatable(QuestHudLayoutManager.snapToGrid()
                ? "ui.questsandstuff.hud.layout.snap_on"
                : "ui.questsandstuff.hud.layout.snap_off");
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
        QuestHudLayoutManager.Element hovered = dragHandler.elementAt(mouseX, mouseY, width, height);
        boolean completionSelected = dragHandler.selected() == QuestHudLayoutManager.Element.COMPLETION || hovered == QuestHudLayoutManager.Element.COMPLETION;
        boolean pinnedSelected = dragHandler.selected() == QuestHudLayoutManager.Element.PINNED || hovered == QuestHudLayoutManager.Element.PINNED;
        QuestCompletionNotificationOverlay.renderPreview(graphics, completion.x(), completion.y(), completion.width(), completion.height(), false);
        PinnedQuestHudOverlay.renderPreview(graphics, pinned.x(), pinned.y(), pinned.width(), pinned.height(), false);
        if (completionSelected) {
            QuestHudLayoutManager.HudBox selection = QuestHudLayoutDragHandler.selectionBox(completion);
            drawSelectionSlot(graphics, selection, QuestHudLayoutManager.Element.COMPLETION);
            drawResizeHandle(graphics, selection);
        }
        if (pinnedSelected) {
            QuestHudLayoutManager.HudBox selection = QuestHudLayoutDragHandler.selectionBox(pinned);
            drawSelectionSlot(graphics, selection, QuestHudLayoutManager.Element.PINNED);
            drawResizeHandle(graphics, selection);
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

    private static class ThemedButton extends Button {
        private final String icon;
        private final int accentColor;

        ThemedButton(int x, int y, int width, int height, Component message, String icon, int accentColor, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.icon = icon;
            this.accentColor = accentColor;
        }

        @Override
        public void onPress() {
            super.onPress();
            setFocused(false);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = isHovered();
            SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_PANEL_ALT, 210)).draw(graphics, 0, 0, getX(), getY(), getWidth(), getHeight());
            drawRectOutline(graphics, getX(), getY(), getWidth(), getHeight(), hovered ? TabletColors.BORDER_ACCENT : TabletColors.subtleBorder());
            if (hovered) {
                GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, getX(), getY(), getWidth(), getHeight(), accentColor);
            }
            String text = getMessage().getString();
            Font font = Minecraft.getInstance().font;
            IGuiTexture iconTexture = IconAtlas.iconTexture(icon);
            int iconSize = 12;
            int iconGap = iconTexture != null ? 4 : 0;
            int textW = font.width(text);
            int contentW = (iconTexture != null ? iconSize + iconGap : 0) + textW;
            int contentX = getX() + (getWidth() - contentW) / 2;
            int centerY = getY() + getHeight() / 2;
            int textColor = hovered ? accentColor : TabletColors.TEXT_PRIMARY;
            if (iconTexture != null) {
                iconTexture.draw(graphics, 0, 0, contentX, centerY - iconSize / 2, iconSize, iconSize);
                contentX += iconSize + iconGap;
            }
            graphics.drawString(font, text, contentX, centerY - 9 / 2, textColor, false);
        }
    }
}

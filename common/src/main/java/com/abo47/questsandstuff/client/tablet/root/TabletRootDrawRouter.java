package com.abo47.questsandstuff.client.tablet.root;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class TabletRootDrawRouter {
    private static final int OFFSCREEN_MOUSE = Integer.MIN_VALUE / 4;

    private TabletRootDrawRouter() {
    }

    static void draw(
            WidgetGroup modalLayer,
            WidgetGroup frontWindowLayer,
            boolean modalOpen,
            boolean frontWindowOpen,
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTicks,
            LayerDraw layer,
            SelfLayerDraw selfDraw
    ) {
        if (!modalOpen || modalLayer == null) {
            drawWithFrontWindow(frontWindowLayer, frontWindowOpen, graphics, mouseX, mouseY, partialTicks, layer, selfDraw);
            return;
        }
        modalLayer.setVisible(false);
        selfDraw.draw(graphics, OFFSCREEN_MOUSE, OFFSCREEN_MOUSE, partialTicks);
        modalLayer.setVisible(true);
        drawGroupLayer(modalLayer, graphics, mouseX, mouseY, partialTicks, layer);
    }

    private static void drawWithFrontWindow(
            WidgetGroup frontWindowLayer,
            boolean frontWindowOpen,
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTicks,
            LayerDraw layer,
            SelfLayerDraw selfDraw
    ) {
        if (!frontWindowOpen || frontWindowLayer == null) {
            selfDraw.draw(graphics, mouseX, mouseY, partialTicks);
            return;
        }
        frontWindowLayer.setVisible(false);
        selfDraw.draw(graphics, OFFSCREEN_MOUSE, OFFSCREEN_MOUSE, partialTicks);
        frontWindowLayer.setVisible(true);
        drawGroupLayer(frontWindowLayer, graphics, mouseX, mouseY, partialTicks, layer);
    }

    private static void drawGroupLayer(WidgetGroup group, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, LayerDraw layer) {
        switch (layer) {
            case BACKGROUND -> group.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            case FOREGROUND -> group.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            case OVERLAY -> group.drawOverlay(graphics, mouseX, mouseY, partialTicks);
        }
    }

    enum LayerDraw {
        BACKGROUND,
        FOREGROUND,
        OVERLAY
    }

    @FunctionalInterface
    interface SelfLayerDraw {
        void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);
    }
}

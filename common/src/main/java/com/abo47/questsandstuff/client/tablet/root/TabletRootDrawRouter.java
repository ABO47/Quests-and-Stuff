package com.abo47.questsandstuff.client.tablet.root;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;

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
            boolean frontWindowMouse,
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTicks,
            LayerDraw layer,
            SelfLayerDraw selfDraw
    ) {
        if (!modalOpen && !frontWindowOpen) {
            selfDraw.draw(graphics, mouseX, mouseY, partialTicks);
            return;
        }
        if (modalLayer != null) {
            modalLayer.setVisible(false);
        }
        if (frontWindowLayer != null) {
            frontWindowLayer.setVisible(false);
        }
        selfDraw.draw(graphics, OFFSCREEN_MOUSE, OFFSCREEN_MOUSE, partialTicks);
        if (frontWindowOpen && frontWindowLayer != null) {
            flushBatchedDraws(graphics);
            frontWindowLayer.setVisible(true);
            drawGroupLayer(frontWindowLayer, graphics, layerMouse(frontWindowMouse, mouseX), layerMouse(frontWindowMouse, mouseY), partialTicks, layer);
        }
        if (!modalOpen || modalLayer == null) {
            return;
        }
        flushBatchedDraws(graphics);
        modalLayer.setVisible(true);
        drawGroupLayer(modalLayer, graphics, mouseX, mouseY, partialTicks, layer);
    }

    private static int layerMouse(boolean active, int mouse) {
        return active ? mouse : OFFSCREEN_MOUSE;
    }

    private static void flushBatchedDraws(GuiGraphics graphics) {
        graphics.bufferSource().endBatch();
        RenderSystem.depthMask(true);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
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

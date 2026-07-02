package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

public final class CanvasBackgroundOpacity {
    private CanvasBackgroundOpacity() {
    }

    public static int alpha(int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        return Math.max(0, Math.min(255, 255 * clamped / 100));
    }

    public static int color(int color, int percent) {
        return withAlpha(color, alpha(percent));
    }

    public static void drawFill(GuiGraphics graphics, int x, int y, int width, int height, int color, int percent) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int fill = color(color, percent);
        if ((fill >>> 24) == 0) {
            return;
        }
        SurfaceFactory.fill(fill).draw(graphics, 0, 0, x, y, width, height);
    }

    public static void drawTexture(GuiGraphics graphics, IGuiTexture texture, int mouseX, int mouseY, int x, int y, int width, int height, int percent) {
        if (texture == null || width <= 0 || height <= 0) {
            return;
        }
        int alpha = alpha(percent);
        if (alpha <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha / 255.0f);
        try {
            texture.draw(graphics, mouseX, mouseY, x, y, width, height);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}

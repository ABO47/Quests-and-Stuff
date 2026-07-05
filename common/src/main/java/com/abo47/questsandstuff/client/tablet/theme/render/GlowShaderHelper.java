package com.abo47.questsandstuff.client.tablet.theme.render;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.awt.Rectangle;
import java.util.List;

public final class GlowShaderHelper {
    private static final ResourceLocation GLOW_SHADER = new ResourceLocation("questsandstuff", "glow");
    private static ShaderTexture shader;

    private GlowShaderHelper() {
    }

    private static ShaderTexture shader() {
        if (shader == null) {
            shader = ShaderTexture.createShader(GLOW_SHADER);
        }
        return shader;
    }

    /** IGuiTexture that draws the glow shader in the default INTERACTIVE color. */
    public static IGuiTexture hoverGlow() {
        return hoverGlow(TabletColors.INTERACTIVE);
    }

    /** IGuiTexture that draws the glow shader in a custom ARGB color. */
    public static IGuiTexture hoverGlow(int glowColor) {
        return (graphics, mouseX, mouseY, x, y, w, h) ->
                drawGlow(graphics, mouseX, mouseY, (int) x, (int) y, w, h, glowColor);
    }

    /** Draw the glow shader over the given area in the default INTERACTIVE color. */
    public static void drawGlow(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h) {
        drawGlow(graphics, mouseX, mouseY, x, y, w, h, TabletColors.INTERACTIVE);
    }

    /** Draw the glow shader over the given area in a custom ARGB color. */
    public static void drawGlow(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h, int glowColor) {
        ShaderTexture s = shader();
        if (s == null) return;
        float r = FastColor.ARGB32.red(glowColor) / 255f;
        float g = FastColor.ARGB32.green(glowColor) / 255f;
        float b = FastColor.ARGB32.blue(glowColor) / 255f;
        s.setUniformCache(cache -> cache.glUniform4F("uGlowColor", r, g, b, 1f));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        s.draw(graphics, mouseX, mouseY, x, y, w, h);
    }

    /**
     * Draw the glow shader clipped to ancestor widget bounds.
     * @param ancestors list of ancestor bounds from outermost to innermost
     */
    public static void drawGlowClipped(GuiGraphics graphics, int mouseX, int mouseY,
                                       int wx, int wy, int ww, int wh,
                                       int glowColor, List<Rectangle> ancestors) {
        if (ww <= 0 || wh <= 0) return;
        Rectangle clip = ancestorClip(wx, wy, ww, wh, ancestors);
        if (clip == null || clip.width <= 0 || clip.height <= 0) return;
        graphics.enableScissor(clip.x, clip.y, clip.x + clip.width, clip.y + clip.height);
        drawGlow(graphics, mouseX, mouseY, wx, wy, ww, wh, glowColor);
        graphics.disableScissor();
    }

    private static Rectangle ancestorClip(int wx, int wy, int ww, int wh, List<Rectangle> ancestors) {
        int sx = wx;
        int sy = wy;
        int sw = ww;
        int sh = wh;
        for (Rectangle a : ancestors) {
            int ar = a.x + a.width;
            int ab = a.y + a.height;
            int r = sx + sw;
            int b = sy + sh;
            sx = Math.max(sx, a.x);
            sy = Math.max(sy, a.y);
            sw = Math.min(r, ar) - sx;
            sh = Math.min(b, ab) - sy;
            if (sw <= 0 || sh <= 0) return null;
        }
        return new Rectangle(sx, sy, sw, sh);
    }
}

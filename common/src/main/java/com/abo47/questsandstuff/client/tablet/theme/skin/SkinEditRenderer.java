package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.awt.Rectangle;
import java.util.List;

public final class SkinEditRenderer {
    private static final ResourceLocation GLOW_SHADER = new ResourceLocation("questsandstuff", "glow");
    private static final int DIM_COLOR = 0x60000000;
    private static final ColorRectTexture DIM_BG = new ColorRectTexture(DIM_COLOR);
    private static ShaderTexture glowShader;
    private static final int GLOW_COLOR = ModColors.INTERACTIVE;
    private static final int SELECTED_GLOW_COLOR = ModColors.SUCCESS;

    private SkinEditRenderer() {
    }

    public static void draw(GuiGraphics graphics, TabletRootWidget root, TabletUiState state, int mouseX, int mouseY, boolean frontWindowOpen) {
        if (ModalStateQueries.anyOpen(state)) return;
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        if (!frontWindowOpen) {
            DIM_BG.draw(graphics, 0, 0, 0, 0, screenW, screenH);
        }

        String selectedKey = state.root.skinEditSelectedTarget;
        if (!selectedKey.isBlank()) {
            drawGlowForKey(graphics, root, selectedKey, SELECTED_GLOW_COLOR, mouseX, mouseY);
        } else {
            String hoverKey = SkinEditTargetResolver.findTargetKeyAt(root, mouseX, mouseY);
            if (hoverKey != null) {
                drawGlowForKey(graphics, root, hoverKey, GLOW_COLOR, mouseX, mouseY);
            }
        }

        if (root.isContextMenuOpen()) {
            root.getContextMenuRoot().drawInBackground(graphics, mouseX, mouseY, mc.getFrameTime());
        }
    }

    private static void drawGlowForKey(GuiGraphics graphics, WidgetGroup root, String key, int glowColor, int mouseX, int mouseY) {
        Widget widget = SkinEditTargetResolver.widgetForKey(root, key);
        if (widget != null) {
            drawGlow(graphics, root, widget, glowColor, mouseX, mouseY);
        }
    }

    private static void drawGlow(GuiGraphics graphics, WidgetGroup root, Widget widget, int glowColor, int mouseX, int mouseY) {
        int wx = widget.getPositionX();
        int wy = widget.getPositionY();
        int ww = widget.getSizeWidth();
        int wh = widget.getSizeHeight();
        if (ww <= 0 || wh <= 0) return;

        List<Rectangle> ancestors = SkinEditTargetResolver.ancestorBounds(widget, root);
        Rectangle clip = ancestorClip(wx, wy, ww, wh, ancestors);
        if (clip == null || clip.width <= 0 || clip.height <= 0) return;

        if (glowShader == null) {
            glowShader = ShaderTexture.createShader(GLOW_SHADER);
        }
        float r = FastColor.ARGB32.red(glowColor) / 255f;
        float g = FastColor.ARGB32.green(glowColor) / 255f;
        float b = FastColor.ARGB32.blue(glowColor) / 255f;

        graphics.enableScissor(clip.x, clip.y, clip.x + clip.width, clip.y + clip.height);
        if (glowShader != null) {
            glowShader.setUniformCache(cache -> cache.glUniform4F("uGlowColor", r, g, b, 1f));
            glowShader.draw(graphics, mouseX, mouseY, wx, wy, ww, wh);
        }
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

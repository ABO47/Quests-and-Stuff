package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import javax.annotation.Nonnull;

public final class CanvasGlowEffect {
    private static final ResourceLocation GLOW_SHADER = new ResourceLocation("questsandstuff", "glow");

    public static WidgetGroup overlay(int x, int y, int w, int h) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int px = getPositionX();
                int py = getPositionY();
                if (mouseX < px || mouseX > px + w || mouseY < py || mouseY > py + h) {
                    return;
                }
                ShaderTexture shader = ShaderTexture.createShader(GLOW_SHADER);
                if (shader == null) {
                    return;
                }
                int glowColor = ModColors.INTERACTIVE;
                float r = FastColor.ARGB32.red(glowColor) / 255f;
                float g = FastColor.ARGB32.green(glowColor) / 255f;
                float b = FastColor.ARGB32.blue(glowColor) / 255f;
                shader.setUniformCache(cache -> {
                    cache.glUniform4F("uGlowColor", r, g, b, 1f);
                });
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                shader.draw(graphics, mouseX, mouseY, px, py, w, h);
            }
        };
    }
}

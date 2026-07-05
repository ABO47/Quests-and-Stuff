package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

public final class CanvasGlowEffect {
    public static WidgetGroup overlay(int x, int y, int w, int h) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int px = getPositionX();
                int py = getPositionY();
                if (mouseX < px || mouseX > px + w || mouseY < py || mouseY > py + h) {
                    return;
                }
                GlowShaderHelper.drawGlow(graphics, mouseX, mouseY, px, py, w, h);
            }
        };
    }
}

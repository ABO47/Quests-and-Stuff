package com.abo47.questsandstuff.client.tablet.controls;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public class TabletScissoredWidgetGroup extends WidgetGroup {
    public TabletScissoredWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    private void withScissor(GuiGraphics graphics, Runnable draw) {
        int x = getPositionX();
        int y = getPositionY();
        int w = getSizeWidth();
        int h = getSizeHeight();
        var trans = graphics.pose().last().pose();
        var realPos = trans.transform(new Vector4f(x, y, 0, 1));
        var realPos2 = trans.transform(new Vector4f(x + w, y + h, 0, 1));
        graphics.enableScissor((int) realPos.x, (int) realPos.y, (int) realPos2.x, (int) realPos2.y);
        draw.run();
        graphics.disableScissor();
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        withScissor(graphics, () -> {
            drawBackgroundTexture(graphics, mouseX, mouseY);
            drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
        });
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        withScissor(graphics, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        withScissor(graphics, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }
}

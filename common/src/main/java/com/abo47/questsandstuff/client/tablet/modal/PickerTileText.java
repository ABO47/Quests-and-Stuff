package com.abo47.questsandstuff.client.tablet.modal;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

public final class PickerTileText {
    private PickerTileText() {
    }

    public static WidgetGroup centeredLabel(int x, int y, int w, String text, int color) {
        String safeText = text == null ? "" : text;
        return new WidgetGroup(x, y, w, 10) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                var font = Minecraft.getInstance().font;
                String fitted = fitText(safeText, Math.max(1, getSizeWidth()));
                int drawX = getPositionX() + Math.max(0, (getSizeWidth() - font.width(fitted)) / 2);
                com.lowdragmc.lowdraglib.gui.texture.TextTexture txt = new com.lowdragmc.lowdraglib.gui.texture.TextTexture(fitted).setType(com.lowdragmc.lowdraglib.gui.texture.TextTexture.TextType.NORMAL).setColor(color);
                txt.draw(graphics, mouseX, mouseY, drawX, getPositionY(), 200, 20);
            }
        };
    }

    private static String fitText(String text, int width) {
        var font = Minecraft.getInstance().font;
        if (font.width(text) <= width) {
            return text;
        }
        String suffix = "..";
        return font.plainSubstrByWidth(text, Math.max(1, width - font.width(suffix))) + suffix;
    }
}

package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class Surfaces {
    private Surfaces() {
    }

    public static ColorRectTexture fill(int color) {
        return new ColorRectTexture(color);
    }

    public static GuiTextureGroup bordered(int fillColor, int borderColor) {
        return new GuiTextureGroup(new ColorRectTexture(fillColor), new ColorBorderTexture(1, borderColor));
    }

    public static GuiTextureGroup transparentBorder(int borderColor) {
        return bordered(0x00000000, borderColor);
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fillColor, int borderColor) {
        WidgetGroup panel = new WidgetGroup(x, y, w, h);
        panel.setBackground(bordered(fillColor, borderColor));
        return panel;
    }
}

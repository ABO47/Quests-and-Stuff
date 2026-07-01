package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class Surfaces {
    private Surfaces() {
    }

    public static ColorRectTexture fill(int color) {
        return new ColorRectTexture(color);
    }

    public static ColorRectTexture transparentFill() {
        return fill(0x00000000);
    }

    public static int withAlpha(int color, int alpha) {
        return UiThemeTokens.withAlpha(color, alpha);
    }

    public static GuiTextureGroup group(IGuiTexture... textures) {
        return new GuiTextureGroup(textures);
    }

    public static IGuiTexture transparent() {
        return IGuiTexture.EMPTY;
    }

    public static GuiTextureGroup bordered(int fillColor, int borderColor) {
        return group(fill(fillColor), new ColorBorderTexture(1, borderColor));
    }

    public static GuiTextureGroup transparentBorder(int borderColor) {
        return bordered(0x00000000, borderColor);
    }

    public static GuiTextureGroup panel() {
        return bordered(ModColors.SURFACE_PANEL, ModColors.BORDER_BASE);
    }

    public static GuiTextureGroup insetPanel() {
        return bordered(ModColors.recessedSurface(), ModColors.subtleBorder());
    }

    public static GuiTextureGroup raisedPanel() {
        return bordered(ModColors.elevatedSurface(), ModColors.BORDER_BASE);
    }

    public static GuiTextureGroup control() {
        return bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
    }

    public static GuiTextureGroup controlHover(int accentColor) {
        return bordered(ModColors.hoverFill(accentColor), ModColors.focusBorder());
    }

    public static GuiTextureGroup controlPressed(int accentColor) {
        return bordered(ModColors.pressedFill(accentColor), accentColor);
    }

    public static GuiTextureGroup card(boolean selected, int accentColor, boolean muted) {
        int fill = muted
                ? withAlpha(ModColors.TEXT_MUTED, 34)
                : (selected ? withAlpha(accentColor, 180) : ModColors.elevatedSurface());
        int border = muted ? ModColors.subtleBorder() : (selected ? accentColor : ModColors.subtleBorder());
        return bordered(fill, border);
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fillColor, int borderColor) {
        WidgetGroup panel = new WidgetGroup(x, y, w, h);
        panel.setBackground(bordered(fillColor, borderColor));
        return panel;
    }
}

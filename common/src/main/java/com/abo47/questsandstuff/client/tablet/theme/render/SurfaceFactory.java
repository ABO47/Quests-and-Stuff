package com.abo47.questsandstuff.client.tablet.theme.render;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class SurfaceFactory {
    private SurfaceFactory() {
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
        return bordered(TabletColors.SURFACE_PANEL, TabletColors.BORDER_BASE);
    }

    public static GuiTextureGroup insetPanel() {
        return bordered(TabletColors.recessedSurface(), TabletColors.subtleBorder());
    }

    public static GuiTextureGroup raisedPanel() {
        return bordered(TabletColors.elevatedSurface(), TabletColors.BORDER_BASE);
    }

    public static GuiTextureGroup control() {
        return bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE);
    }

    public static GuiTextureGroup controlHover(int accentColor) {
        return bordered(TabletColors.hoverFill(accentColor), TabletColors.focusBorder());
    }

    public static GuiTextureGroup controlPressed(int accentColor) {
        return bordered(TabletColors.pressedFill(accentColor), accentColor);
    }

    public static GuiTextureGroup card(boolean selected, int accentColor, boolean muted) {
        int fill = muted
                ? withAlpha(TabletColors.TEXT_MUTED, 34)
                : (selected ? withAlpha(accentColor, 180) : TabletColors.elevatedSurface());
        int border = muted ? TabletColors.subtleBorder() : (selected ? accentColor : TabletColors.subtleBorder());
        return bordered(fill, border);
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fillColor, int borderColor) {
        WidgetGroup panel = new WidgetGroup(x, y, w, h);
        panel.setBackground(bordered(fillColor, borderColor));
        return panel;
    }
}

package com.abo47.questsandstuff.client.tablet.theme.render;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;

import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

public final class ChromeFactory {
    private ChromeFactory() {
    }

    public static ButtonWidget closeIconButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return iconButton(x, y, w, h, "close", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_ERROR), callback);
    }

    public static ButtonWidget iconButton(int x, int y, int w, int h, String icon, int activeColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        IGuiTexture glyph = IconAtlas.iconTexture(icon);
        IGuiTexture iconTexture = (graphics, mouseX, mouseY, x0, y0, width, height) -> {
            SurfaceFactory.fill(TabletColors.elevatedSurface()).draw(graphics, mouseX, mouseY, x0, y0, width, height);
            drawBorder(graphics, (int) x0, (int) y0, width, height, TabletColors.BORDER_BASE);
            drawGlyph(graphics, mouseX, mouseY, x0, y0, width, height, glyph, activeColor);
        };

        ButtonWidget btn = new ButtonWidget(x, y, w, h, iconTexture, callback);
        btn.setClientSideWidget();
        btn.setHoverTexture(GlowShaderHelper.hoverGlow(activeColor));
        btn.setClickedTexture((graphics, mouseX, mouseY, x0, y0, width, height) -> {
            SurfaceFactory.fill(TabletColors.pressedFill(activeColor)).draw(graphics, mouseX, mouseY, x0, y0, width, height);
            drawBorder(graphics, (int) x0, (int) y0, width, height, activeColor);
            drawGlyph(graphics, mouseX, mouseY, x0, y0, width, height, glyph, activeColor);
        });
        return btn;
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        SurfaceFactory.fill(color).draw(graphics, 0, 0, x, y, width, 1);
        SurfaceFactory.fill(color).draw(graphics, 0, 0, x, y + height - 1, width, 1);
        SurfaceFactory.fill(color).draw(graphics, 0, 0, x, y, 1, height);
        SurfaceFactory.fill(color).draw(graphics, 0, 0, x + width - 1, y, 1, height);
    }

    private static void drawGlyph(GuiGraphics graphics, int mouseX, int mouseY, float x0, float y0, int width, int height, IGuiTexture glyph, int fallbackColor) {
        int glyphX = (int) x0 + 2;
        int glyphY = (int) y0 + 2;
        int glyphW = Math.max(1, width - 4);
        int glyphH = Math.max(1, height - 4);
        if (glyph != null) {
            glyph.draw(graphics, mouseX, mouseY, glyphX, glyphY, glyphW, glyphH);
            return;
        }
        int cx = (int) x0 + width / 2;
        int cy = (int) y0 + height / 2;
        SurfaceFactory.fill(fallbackColor).draw(graphics, 0, 0, cx - 3, cy, 7, 1);
        SurfaceFactory.fill(fallbackColor).draw(graphics, 0, 0, cx, cy - 3, 1, 7);
    }
}

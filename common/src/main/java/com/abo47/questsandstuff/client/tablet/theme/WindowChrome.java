package com.abo47.questsandstuff.client.tablet.theme;


import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import net.minecraft.client.gui.GuiGraphics;

public final class WindowChrome {
    private WindowChrome() {
    }

    public static ButtonWidget closeIconButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return iconButton(x, y, w, h, "close", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_ERROR), callback);
    }

    public static ButtonWidget iconButton(int x, int y, int w, int h, String icon, int activeColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        IGuiTexture glyph = UiIconAtlas.iconTexture(icon);
        IGuiTexture iconTexture = (graphics, mouseX, mouseY, x0, y0, width, height) -> {
            graphics.fill((int) x0, (int) y0, (int) x0 + width, (int) y0 + height, ModColors.elevatedSurface());
            drawBorder(graphics, (int) x0, (int) y0, width, height, ModColors.BORDER_BASE);
            drawGlyph(graphics, mouseX, mouseY, x0, y0, width, height, glyph, activeColor);
        };

        IGuiTexture hoverTexture = (graphics, mouseX, mouseY, x0, y0, width, height) -> {
            graphics.fill((int) x0, (int) y0, (int) x0 + width, (int) y0 + height, ModColors.hoverFill(activeColor));
            drawBorder(graphics, (int) x0, (int) y0, width, height, ModColors.focusBorder());
            drawGlyph(graphics, mouseX, mouseY, x0, y0, width, height, glyph, activeColor);
        };

        IGuiTexture clickedTexture = (graphics, mouseX, mouseY, x0, y0, width, height) -> {
            graphics.fill((int) x0, (int) y0, (int) x0 + width, (int) y0 + height, ModColors.pressedFill(activeColor));
            drawBorder(graphics, (int) x0, (int) y0, width, height, activeColor);
            drawGlyph(graphics, mouseX, mouseY, x0, y0, width, height, glyph, activeColor);
        };

        ButtonWidget btn = new ButtonWidget(x, y, w, h, Surfaces.group(iconTexture), callback);
        btn.setClientSideWidget();
        btn.setHoverTexture(Surfaces.group(hoverTexture));
        btn.setClickedTexture(Surfaces.group(clickedTexture));
        return btn;
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
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
        graphics.fill(cx - 3, cy, cx + 4, cy + 1, fallbackColor);
        graphics.fill(cx, cy - 3, cx + 1, cy + 4, fallbackColor);
    }
}

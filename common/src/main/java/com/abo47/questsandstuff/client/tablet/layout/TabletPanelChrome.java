package com.abo47.questsandstuff.client.tablet.layout;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

public final class TabletPanelChrome {
    private TabletPanelChrome() {
    }

    public static void drawWindowShadow(GuiGraphics graphics, WidgetGroup panel) {
        drawWindowShadow(graphics, panel.getPositionX(), panel.getPositionY(), panel.getSizeWidth(), panel.getSizeHeight());
    }

    public static void drawWindowShadow(GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int soft = withAlpha(ModColors.SURFACE_BASE, 82);
        int hard = withAlpha(ModColors.SURFACE_BASE, 120);
        Surfaces.fill(soft).draw(graphics, 0, 0, x + 4, y + 5, w, h);
        Surfaces.fill(hard).draw(graphics, 0, 0, x + 2, y + 3, w, h);
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, TabletUiState state) {
        drawCanvasPanelChrome(graphics, panel, state.canvas.canvasViewportX, state.canvas.canvasViewportY, state.canvas.canvasViewportW, state.canvas.canvasViewportH);
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        int innerLeft = x + 1;
        int innerTop = y + 1;
        int innerRight = x + Math.max(1, w - 1);
        int innerBottom = y + Math.max(1, h - 1);

        int holeLeft = x + Math.max(1, Math.min(w - 1, viewportX));
        int holeTop = y + Math.max(1, Math.min(h - 1, viewportY));
        int holeRight = x + Math.max(1, Math.min(w - 1, viewportX + viewportW));
        int holeBottom = y + Math.max(1, Math.min(h - 1, viewportY + viewportH));

        IGuiTexture fill = Surfaces.fill(ModColors.SURFACE_PANEL);
        if (holeRight <= holeLeft || holeBottom <= holeTop) {
            fillPanelRect(fill, graphics, innerLeft, innerTop, innerRight, innerBottom);
        } else {
            fillPanelRect(fill, graphics, innerLeft, innerTop, innerRight, holeTop);
            fillPanelRect(fill, graphics, innerLeft, holeBottom, innerRight, innerBottom);
            fillPanelRect(fill, graphics, innerLeft, holeTop, holeLeft, holeBottom);
            fillPanelRect(fill, graphics, holeRight, holeTop, innerRight, holeBottom);
        }
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, TabletUiState state) {
        drawCanvasPanelOutlines(graphics, panel, state.canvas.canvasViewportX, state.canvas.canvasViewportY, state.canvas.canvasViewportW, state.canvas.canvasViewportH, state.root.canEdit, state.canvas.gridEnabled, state.canvas.gridOpacityPercent, TabletGridControls.defaultGridColor(state));
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH, boolean canEdit, boolean gridEnabled, int gridOpacityPercent, int gridColor) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        int holeLeft = x + Math.max(1, Math.min(w - 1, viewportX));
        int holeTop = y + Math.max(1, Math.min(h - 1, viewportY));
        int holeRight = x + Math.max(1, Math.min(w - 1, viewportX + viewportW));
        int holeBottom = y + Math.max(1, Math.min(h - 1, viewportY + viewportH));

        if (holeRight > holeLeft && holeBottom > holeTop) {
            drawRectOutline(graphics, holeLeft - 1, holeTop - 1, holeRight - holeLeft + 2, holeBottom - holeTop + 2, ModColors.BORDER_BASE);
            if (canEdit && gridEnabled) {
                drawRectOutline(graphics, holeLeft, holeTop, holeRight - holeLeft, holeBottom - holeTop, gridLineColor(gridOpacityPercent, gridColor));
            }
        }
        drawPanelOutline(graphics, panel);
    }

    public static void drawPanelChrome(GuiGraphics graphics, WidgetGroup panel) {
        drawPanelChromeNoShadow(graphics, panel);
    }

    public static void drawRootChromeNoShadow(GuiGraphics graphics, WidgetGroup panel) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        fillRootRect(Surfaces.fill(ModColors.SURFACE_BASE), graphics, x, y, x + Math.max(1, w), y + Math.max(1, h));
    }

    public static void drawPanelChromeNoShadow(GuiGraphics graphics, WidgetGroup panel) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        int right = x + Math.max(1, w - 1);
        int bottom = y + Math.max(1, h - 1);
        fillPanelRect(Surfaces.fill(ModColors.SURFACE_PANEL), graphics, x + 1, y + 1, right, bottom);
    }

    public static void drawPanelOutline(GuiGraphics graphics, WidgetGroup panel) {
        drawRectOutline(graphics, panel.getPositionX(), panel.getPositionY(), panel.getSize().width, panel.getSize().height, ModColors.BORDER_BASE);
    }

    public static void drawRectOutline(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        IGuiTexture fill = Surfaces.fill(color);
        fill.draw(graphics, 0, 0, x, y, w, 1);
        fill.draw(graphics, 0, 0, x, y + h - 1, w, 1);
        fill.draw(graphics, 0, 0, x, y + 1, 1, Math.max(0, h - 2));
        fill.draw(graphics, 0, 0, x + w - 1, y + 1, 1, Math.max(0, h - 2));
    }

    private static int gridLineColor(int gridOpacityPercent, int gridColor) {
        int alphaPercent = Math.max(0, Math.min(100, gridOpacityPercent));
        int alpha = Math.max(20, Math.min(220, (255 * alphaPercent) / 100));
        return (alpha << 24) | (gridColor & 0x00FFFFFF);
    }

    private static void fillPanelRect(IGuiTexture fill, GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (right > left && bottom > top) {
            fill.draw(graphics, 0, 0, left, top, right - left, bottom - top);
        }
    }

    private static void fillRootRect(IGuiTexture fill, GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (right > left && bottom > top) {
            fill.draw(graphics, 0, 0, left, top, right - left, bottom - top);
        }
    }
}

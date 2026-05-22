package com.abo47.questsandstuff.client.tablet.layout;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeTokens;
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
        int soft = UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, 82);
        int hard = UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, 120);
        graphics.fill(x + 4, y + 5, x + w + 4, y + h + 5, soft);
        graphics.fill(x + 2, y + 3, x + w + 2, y + h + 3, hard);
    }

    public static void drawPanelLighting(GuiGraphics graphics, WidgetGroup panel) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSizeWidth();
        int h = panel.getSizeHeight();
        drawPanelLighting(graphics, x, y, w, h);
    }

    public static void drawPanelLighting(GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 2 || h <= 2) {
            return;
        }
        int highlight = UiThemeTokens.withAlpha(ModColors.TEXT_PRIMARY, 22);
        int shade = UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, 96);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, highlight);
        graphics.fill(x + 1, y + 2, x + 2, y + h - 1, UiThemeTokens.withAlpha(ModColors.TEXT_PRIMARY, 10));
        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, shade);
        graphics.fill(x + w - 2, y + 2, x + w - 1, y + h - 1, UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, 70));
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, TabletUiState state) {
        drawCanvasPanelChrome(graphics, panel, state.canvasViewportX, state.canvasViewportY, state.canvasViewportW, state.canvasViewportH);
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH) {
        drawWindowShadow(graphics, panel);
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

        if (holeRight <= holeLeft || holeBottom <= holeTop) {
            fillPanelRect(graphics, innerLeft, innerTop, innerRight, innerBottom);
        } else {
            fillPanelRect(graphics, innerLeft, innerTop, innerRight, holeTop);
            fillPanelRect(graphics, innerLeft, holeBottom, innerRight, innerBottom);
            fillPanelRect(graphics, innerLeft, holeTop, holeLeft, holeBottom);
            fillPanelRect(graphics, holeRight, holeTop, innerRight, holeBottom);
        }
        drawPanelLighting(graphics, x, y, w, h);
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, TabletUiState state) {
        drawCanvasPanelOutlines(graphics, panel, state.canvasViewportX, state.canvasViewportY, state.canvasViewportW, state.canvasViewportH, state.canEdit, state.gridEnabled, state.gridOpacityPercent);
    }

    public static void drawCanvasPanelOutlines(GuiGraphics graphics, WidgetGroup panel, int viewportX, int viewportY, int viewportW, int viewportH, boolean canEdit, boolean gridEnabled, int gridOpacityPercent) {
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        int holeLeft = x + Math.max(1, Math.min(w - 1, viewportX));
        int holeTop = y + Math.max(1, Math.min(h - 1, viewportY));
        int holeRight = x + Math.max(1, Math.min(w - 1, viewportX + viewportW));
        int holeBottom = y + Math.max(1, Math.min(h - 1, viewportY + viewportH));

        if (holeRight > holeLeft && holeBottom > holeTop) {
            graphics.renderOutline(holeLeft - 1, holeTop - 1, holeRight - holeLeft + 2, holeBottom - holeTop + 2, ModColors.BORDER_BASE);
            if (canEdit && gridEnabled) {
                graphics.renderOutline(holeLeft, holeTop, holeRight - holeLeft, holeBottom - holeTop, gridLineColor(gridOpacityPercent));
            }
        }
        drawPanelOutline(graphics, panel);
    }

    public static void drawPanelChrome(GuiGraphics graphics, WidgetGroup panel) {
        drawWindowShadow(graphics, panel);
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        int right = x + Math.max(1, w - 1);
        int bottom = y + Math.max(1, h - 1);
        fillPanelRect(graphics, x + 1, y + 1, right, bottom);
        drawPanelLighting(graphics, x, y, w, h);
    }

    public static void drawPanelOutline(GuiGraphics graphics, WidgetGroup panel) {
        graphics.renderOutline(panel.getPositionX(), panel.getPositionY(), panel.getSize().width, panel.getSize().height, ModColors.BORDER_BASE);
    }

    private static int gridLineColor(TabletUiState state) {
        return gridLineColor(state.gridOpacityPercent);
    }

    private static int gridLineColor(int gridOpacityPercent) {
        int alphaPercent = Math.max(0, Math.min(100, gridOpacityPercent));
        int alpha = Math.max(20, Math.min(220, (255 * alphaPercent) / 100));
        return (alpha << 24) | (ModColors.TEXT_PRIMARY & 0x00FFFFFF);
    }

    private static void fillPanelRect(GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (right > left && bottom > top) {
            graphics.fill(left, top, right, bottom, ModColors.SURFACE_PANEL);
        }
    }
}

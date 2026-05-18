package com.abo47.questsandstuff.client.tablet.layout;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

public final class TabletPanelChrome {
    private TabletPanelChrome() {
    }

    public static void drawCanvasPanelChrome(GuiGraphics graphics, WidgetGroup panel, TabletUiState state) {
        drawCanvasPanelChrome(graphics, panel, state.canvasViewportX, state.canvasViewportY, state.canvasViewportW, state.canvasViewportH);
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

        if (holeRight <= holeLeft || holeBottom <= holeTop) {
            fillPanelRect(graphics, innerLeft, innerTop, innerRight, innerBottom);
        } else {
            fillPanelRect(graphics, innerLeft, innerTop, innerRight, holeTop);
            fillPanelRect(graphics, innerLeft, holeBottom, innerRight, innerBottom);
            fillPanelRect(graphics, innerLeft, holeTop, holeLeft, holeBottom);
            fillPanelRect(graphics, holeRight, holeTop, innerRight, holeBottom);
        }
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
        int x = panel.getPositionX();
        int y = panel.getPositionY();
        int w = panel.getSize().width;
        int h = panel.getSize().height;
        fillPanelRect(graphics, x + 1, y + 1, x + Math.max(1, w - 1), y + Math.max(1, h - 1));
    }

    public static void drawPanelOutline(GuiGraphics graphics, WidgetGroup panel) {
        graphics.renderOutline(panel.getPositionX(), panel.getPositionY(), panel.getSize().width, panel.getSize().height, ModColors.BORDER_ACCENT);
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

package com.abo47.questsandstuff.client.tablet.layout;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelOutlines;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawPanelOutline;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;

public final class SplitPanelLayout {
    private SplitPanelLayout() {
    }

    public static int splitterX(int leftX, int leftWidth) {
        return leftX + leftWidth + Math.max(0, (GAP - SPLITTER_W) / 2);
    }

    public static int rightPanelX(int leftX, int leftWidth) {
        return leftX + leftWidth + GAP;
    }

    public static WidgetGroup leftPanel(int x, int y, int w, int h) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawPanelChrome(graphics, this);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawPanelOutline(graphics, this);
            }
        };
    }

    public static WidgetGroup rightPanel(int x, int y, int w, int h, TabletUiState state) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawCanvasPanelChrome(graphics, this, state);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawCanvasPanelOutlines(graphics, this, state);
            }
        };
    }

    public static WidgetGroup rightPanel(int x, int y, int w, int h,
                                          int viewportX, int viewportY, int viewportW, int viewportH,
                                          boolean canEdit, boolean showGrid,
                                          int gridOpacity, int gridColor) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawCanvasPanelChrome(graphics, this, viewportX, viewportY, viewportW, viewportH);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawCanvasPanelOutlines(graphics, this, viewportX, viewportY, viewportW, viewportH, canEdit, showGrid, gridOpacity, gridColor);
            }
        };
    }
}

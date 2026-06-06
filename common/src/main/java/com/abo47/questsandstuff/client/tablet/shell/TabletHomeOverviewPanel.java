package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

final class TabletHomeOverviewPanel extends WidgetGroup {
    TabletHomeOverviewPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        int inset = TabletHomeComposer.contentInset(width);
        int statusH = TabletHomeComposer.statusBarHeight(height);
        int contentY = inset + statusH + TabletHomeComposer.contentGap();
        int contentH = Math.max(1, height - contentY - inset);

        addWidget(new TabletHomeStatusBar(inset, inset, Math.max(1, width - inset * 2), statusH));
        WidgetGroup appSurface = new WidgetGroup(inset, contentY, Math.max(1, width - inset * 2), contentH);
        appSurface.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        addWidget(appSurface);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletPanelChrome.drawPanelChromeNoShadow(graphics, this);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
        TabletPanelChrome.drawPanelOutline(graphics, this);
    }
}

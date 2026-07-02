package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class TabletHomeStatusBar extends WidgetGroup {
    TabletHomeStatusBar(int x, int y, int width, int height) {
        super(x, y, width, height);
        setBackground(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.subtleBorder()));
    }
}

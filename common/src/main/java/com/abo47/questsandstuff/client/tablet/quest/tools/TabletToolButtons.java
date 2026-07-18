package com.abo47.questsandstuff.client.tablet.quest.tools;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

final class TabletToolButtons {
    private TabletToolButtons() {
    }

    static void addToggle(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, boolean positiveFill, Component[] tooltips, Runnable action) {
        int fill = positiveFill ? withAlpha(TabletColors.SUCCESS, 62) : withAlpha(TabletColors.ERROR, 62);
        menu.addWidget(TabletIconTextButton.icon(x, y, size, size, icon, visuals(fill, border, iconColor), click -> action.run())
                .tooltips(tooltips));
    }

    static void addAction(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, Component[] tooltips, Runnable action) {
        menu.addWidget(TabletIconTextButton.icon(x, y, size, size, icon, visuals(withAlpha(TabletColors.SURFACE_PANEL_ALT, 164), border, iconColor), click -> action.run())
                .tooltips(tooltips));
    }

    static void addOpacity(WidgetGroup menu, int x, int y, int size, int border, String icon, Component[] tooltips, java.util.function.Consumer<Boolean> action) {
        menu.addWidget(TabletIconTextButton.icon(x, y, size, size, icon, visuals(withAlpha(TabletColors.SURFACE_PANEL_ALT, 164), border, TabletColors.TEXT_PRIMARY), click -> action.accept(click.button == 1))
                .tooltips(tooltips));
    }

    private static TabletIconTextButton.Visuals visuals(int fill, int border, int iconColor) {
        return new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(fill, border, iconColor),
                TabletIconTextButton.State.of(withAlpha(TabletColors.INTERACTIVE, 66), TabletColors.BORDER_ACCENT, iconColor),
                TabletIconTextButton.State.of(withAlpha(TabletColors.INTERACTIVE, 90), TabletColors.BORDER_ACCENT, iconColor)
        );
    }
}

package com.abo47.questsandstuff.client.tablet.quest.tools;

import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class TabletToolButtons {
    private TabletToolButtons() {
    }

    static void addToggle(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, boolean positiveFill, Component[] tooltips, Runnable action) {
        int fill = positiveFill ? withAlpha(ModColors.SUCCESS, 62) : withAlpha(ModColors.ERROR, 62);
        menu.addWidget(TabletIconTextButton.icon(x, y, size, size, icon, visuals(fill, border, iconColor), click -> action.run())
                .tooltips(tooltips));
    }

    static void addAction(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, Component[] tooltips, Runnable action) {
        menu.addWidget(TabletIconTextButton.icon(x, y, size, size, icon, visuals(withAlpha(ModColors.SURFACE_PANEL_ALT, 164), border, iconColor), click -> action.run())
                .tooltips(tooltips));
    }

    static void addOpacity(WidgetGroup menu, int x, int y, int size, int border, String icon, Component[] tooltips, java.util.function.Consumer<Boolean> action) {
        menu.addWidget(TabletIconTextButton.icon(x, y, size, size, icon, visuals(withAlpha(ModColors.SURFACE_PANEL_ALT, 164), border, ModColors.TEXT_PRIMARY), click -> action.accept(click.button == 1))
                .tooltips(tooltips));
    }

    private static TabletIconTextButton.Visuals visuals(int fill, int border, int iconColor) {
        return new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(fill, border, iconColor),
                TabletIconTextButton.State.of(withAlpha(ModColors.INTERACTIVE, 66), ModColors.BORDER_ACCENT, iconColor),
                TabletIconTextButton.State.of(withAlpha(ModColors.INTERACTIVE, 90), ModColors.BORDER_ACCENT, iconColor)
        );
    }
}

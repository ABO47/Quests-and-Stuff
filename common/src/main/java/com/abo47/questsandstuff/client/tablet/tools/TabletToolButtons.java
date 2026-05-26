package com.abo47.questsandstuff.client.tablet.tools;

import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ACTION_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class TabletToolButtons {
    private TabletToolButtons() {
    }

    static void addToggle(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, boolean positiveFill, Component[] tooltips, Runnable action) {
        int fill = positiveFill ? withAlpha(ModColors.SUCCESS, 62) : withAlpha(ModColors.ERROR, 62);
        menu.addWidget(panel(x, y, size, size, fill, border));
        addIcon(menu, x, y, size, icon, iconColor);
        ButtonWidget hit = hit(x, y, size, tooltips, action);
        menu.addWidget(hit);
    }

    static void addAction(WidgetGroup menu, int x, int y, int size, int border, String icon, int iconColor, Component[] tooltips, Runnable action) {
        menu.addWidget(panel(x, y, size, size, withAlpha(ModColors.SURFACE_PANEL_ALT, 164), border));
        addIcon(menu, x, y, size, icon, iconColor);
        ButtonWidget hit = hit(x, y, size, tooltips, action);
        menu.addWidget(hit);
    }

    static void addOpacity(WidgetGroup menu, int x, int y, int size, int border, String icon, Component[] tooltips, java.util.function.Consumer<Boolean> action) {
        menu.addWidget(panel(x, y, size, size, withAlpha(ModColors.SURFACE_PANEL_ALT, 164), border));
        var texture = UiIconAtlas.iconTexture(icon);
        if (texture != null) {
            int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, size - 4));
            menu.addWidget(new ImageWidget(x + (size - iconSize) / 2, y + (size - iconSize) / 2, iconSize, iconSize, texture));
        }
        ButtonWidget hit = flatHitButton(x, y, size, size, click -> action.accept(click.button == 1));
        decorateHit(hit, tooltips);
        menu.addWidget(hit);
    }

    static void addIcon(WidgetGroup menu, int x, int y, int size, String icon, int color) {
        var iconId = UiIconAtlas.icon(icon);
        if (iconId == null) {
            return;
        }
        int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, size - 4));
        ResourceTexture texture = new SmoothResourceTexture(iconId).setColor(color);
        menu.addWidget(new ImageWidget(x + (size - iconSize) / 2, y + (size - iconSize) / 2, iconSize, iconSize, texture));
    }

    static ButtonWidget hit(int x, int y, int size, Component[] tooltips, Runnable action) {
        ButtonWidget hit = flatHitButton(x, y, size, size, click -> action.run());
        decorateHit(hit, tooltips);
        return hit;
    }

    private static void decorateHit(ButtonWidget hit, Component[] tooltips) {
        hit.setHoverTooltips(tooltips);
        hit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 66), ModColors.BORDER_ACCENT));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
    }
}

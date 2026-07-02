package com.abo47.questsandstuff.client.tablet.ui.widget;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.render.WindowChrome;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class TabletWidgets {
    private TabletWidgets() {
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fill, int border) {
        WidgetGroup panel = new WidgetGroup(x, y, w, h);
        panel.setBackground(Surfaces.bordered(fill, border));
        return panel;
    }

    public static LabelWidget label(int x, int y, String text, int color) {
        LabelWidget label = new LabelWidget(x, y, text);
        label.setColor(color);
        return label;
    }

    public static LabelWidget dynamicLabel(int x, int y, java.util.function.Supplier<String> supplier, int color) {
        LabelWidget label = new LabelWidget(x, y, supplier);
        label.setColor(color);
        return label;
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ContextMenuSystem.addWindowsContextRow(menu, y, width, text, icon, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ContextMenuSystem.addWindowsContextRow(menu, y, width, text, icon, submenu, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, int iconColor, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ContextMenuSystem.addWindowsContextRow(menu, y, width, text, icon, iconColor, submenu, callback);
    }

    public static String contextIconForLabel(String label) {
        return ContextMenuSystem.iconForLabel(label);
    }

    public static ButtonWidget button(int x, int y, int w, int h, String text, int baseColor, int activeColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        var base = Surfaces.group(
                Surfaces.bordered(baseColor, ModColors.subtleBorder()),
                new TextTexture(text)
        );
        var active = Surfaces.group(
                Surfaces.controlPressed(activeColor),
                new TextTexture(text)
        );
        ButtonWidget button = new ButtonWidget(x, y, w, h, base, callback);
        button.setHoverTexture(Surfaces.group(
                Surfaces.controlHover(activeColor),
                new TextTexture(text)
        ));
        button.setClickedTexture(active);
        button.setClientSideWidget();
        return button;
    }

    public static ButtonWidget closeIconButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return WindowChrome.closeIconButton(x, y, w, h, callback);
    }

    public static ButtonWidget flatHitButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ButtonWidget button = new ButtonWidget(x, y, w, h, Surfaces.transparentFill(), callback);
        button.setClientSideWidget();
        button.setHoverTexture(Surfaces.transparentFill());
        button.setClickedTexture(Surfaces.transparentFill());
        return button;
    }

    public static String pendingDeleteLabel(TabletUiState state, String key, String fallback) {
        return ContextMenuState.pendingDeleteLabel(state, key, fallback);
    }

    public static boolean confirmDeleteClick(TabletUiState state, String key) {
        return ContextMenuState.confirmDeleteClick(state, key);
    }
}

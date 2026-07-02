package com.abo47.questsandstuff.client.tablet.ui.widget;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.render.ChromeFactory;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class TabletWidgets {
    private TabletWidgets() {
    }

    public static WidgetGroup panel(int x, int y, int w, int h, int fill, int border) {
        WidgetGroup panel = new WidgetGroup(x, y, w, h);
        panel.setBackground(SurfaceFactory.bordered(fill, border));
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
        ContextMenuRenderer.addWindowsContextRow(menu, y, width, text, icon, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ContextMenuRenderer.addWindowsContextRow(menu, y, width, text, icon, submenu, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, int iconColor, boolean submenu, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ContextMenuRenderer.addWindowsContextRow(menu, y, width, text, icon, iconColor, submenu, callback);
    }

    public static String contextIconForLabel(String label) {
        return ContextMenuRenderer.iconForLabel(label);
    }

    public static ButtonWidget button(int x, int y, int w, int h, String text, int baseColor, int activeColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        var base = SurfaceFactory.group(
                SurfaceFactory.bordered(baseColor, TabletColors.subtleBorder()),
                new TextTexture(text)
        );
        var active = SurfaceFactory.group(
                SurfaceFactory.controlPressed(activeColor),
                new TextTexture(text)
        );
        ButtonWidget button = new ButtonWidget(x, y, w, h, base, callback);
        button.setHoverTexture(SurfaceFactory.group(
                SurfaceFactory.controlHover(activeColor),
                new TextTexture(text)
        ));
        button.setClickedTexture(active);
        button.setClientSideWidget();
        return button;
    }

    public static ButtonWidget closeIconButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return ChromeFactory.closeIconButton(x, y, w, h, callback);
    }

    public static ButtonWidget flatHitButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ButtonWidget button = new ButtonWidget(x, y, w, h, SurfaceFactory.transparentFill(), callback);
        button.setClientSideWidget();
        button.setHoverTexture(SurfaceFactory.transparentFill());
        button.setClickedTexture(SurfaceFactory.transparentFill());
        return button;
    }

    public static String pendingDeleteLabel(TabletUiState state, String key, String fallback) {
        return ContextMenuController.pendingDeleteLabel(state, key, fallback);
    }

    public static boolean confirmDeleteClick(TabletUiState state, String key) {
        return ContextMenuController.confirmDeleteClick(state, key);
    }
}

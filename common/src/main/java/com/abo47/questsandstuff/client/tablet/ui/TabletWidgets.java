package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuSystem;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeTokens;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class TabletWidgets {
    private TabletWidgets() {
    }

    static int withAlpha(int color, int alpha) {
        return UiThemeTokens.withAlpha(color, alpha);
    }

    static WidgetGroup panel(int x, int y, int w, int h, int fill, int border) {
        WidgetGroup panel = new WidgetGroup(x, y, w, h);
        panel.setBackground(Surfaces.bordered(fill, border));
        return panel;
    }

    static LabelWidget label(int x, int y, String text, int color) {
        LabelWidget label = new LabelWidget(x, y, text);
        label.setColor(color);
        return label;
    }

    static LabelWidget dynamicLabel(int x, int y, java.util.function.Supplier<String> supplier, int color) {
        LabelWidget label = new LabelWidget(x, y, supplier);
        label.setColor(color);
        return label;
    }

    static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ContextMenuSystem.addWindowsContextRow(menu, y, width, text, icon, callback);
    }

    static String contextIconForLabel(String label) {
        return ContextMenuSystem.iconForLabel(label);
    }

    static ButtonWidget button(int x, int y, int w, int h, String text, int baseColor, int activeColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        GuiTextureGroup base = new GuiTextureGroup(
                Surfaces.bordered(baseColor, ModColors.subtleBorder()),
                new TextTexture(text)
        );
        GuiTextureGroup active = new GuiTextureGroup(
                Surfaces.controlPressed(activeColor),
                new TextTexture(text)
        );
        ButtonWidget button = new ButtonWidget(x, y, w, h, base, callback);
        button.setHoverTexture(new GuiTextureGroup(
                Surfaces.controlHover(activeColor),
                new TextTexture(text)
        ));
        button.setClickedTexture(active);
        button.setClientSideWidget();
        return button;
    }

    static ButtonWidget closeIconButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return WindowChrome.closeIconButton(x, y, w, h, callback);
    }

    static ButtonWidget flatHitButton(int x, int y, int w, int h, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ButtonWidget button = new ButtonWidget(x, y, w, h, Surfaces.fill(0x00000000), callback);
        button.setClientSideWidget();
        button.setHoverTexture(Surfaces.fill(0x00000000));
        button.setClickedTexture(Surfaces.fill(0x00000000));
        return button;
    }

    static String pendingDeleteLabel(TabletUiState state, String key, String fallback) {
        return key != null && key.equals(state.contextDeleteConfirmKey) ? "Sure?" : fallback;
    }

    static boolean confirmDeleteClick(TabletUiState state, String key) {
        String safeKey = key == null ? "" : key;
        if (safeKey.equals(state.contextDeleteConfirmKey)) {
            state.contextDeleteConfirmKey = "";
            return true;
        }
        state.contextDeleteConfirmKey = safeKey;
        return false;
    }
}

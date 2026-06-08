package com.abo47.questsandstuff.client.tablet.modal;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerListPanel;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TabletThemePickerModal {
    private static final int PAD = 8;
    private static final int HEADER_H = 18;
    private static final int ROW_H = 26;
    private static final int ROW_GAP = 4;
    private static final int ROW_PAD = 4;
    private static final int SETTINGS_ROW_H = 34;
    private static final int SETTINGS_ROW_INSET = 4;

    private TabletThemePickerModal() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, "Themes", w, state, refresh);

        int listX = PAD;
        int listY = HEADER_H + PAD;
        int listW = Math.max(32, w - PAD * 2);
        int listH = Math.max(1, h - listY - PAD);
        addThemeList(modal, state, refresh, listX, listY, listW, listH, "No themes found");
    }

    public static void addThemeList(WidgetGroup modal, TabletUiState state, Runnable refresh, int listX, int listY, int listW, int listH, String emptyText) {
        List<UiThemeManager.ThemeInfo> themes = UiThemeManager.availableThemes();
        String active = UiThemeManager.activeThemeName();
        TiledPickerPanel.add(
                modal,
                listX,
                listY,
                listW,
                listH,
                listW - 2,
                ROW_H,
                ROW_GAP,
                1,
                ROW_PAD,
                themes,
                emptyText,
                ScrollState.bind(
                        () -> state.themeScroll,
                        value -> state.themeScroll = value,
                        () -> state.themeScrollDragging,
                        dragging -> state.themeScrollDragging = dragging
                ),
                null,
                refresh,
                (surface, theme, index, rowX, rowY, rowW, rowH, layout) -> addThemeRow(surface, state, refresh, theme, active.equals(theme.id()), rowX, rowY, rowW)
        );
    }

    public static void addSettingsThemeList(WidgetGroup modal, TabletUiState state, Runnable refresh, int listX, int listY, int listW, int listH, String emptyText) {
        List<UiThemeManager.ThemeInfo> themes = UiThemeManager.availableThemes();
        String active = UiThemeManager.activeThemeName();
        PickerListPanel.add(
                modal,
                listX,
                listY,
                listW,
                listH,
                SETTINGS_ROW_H,
                themes,
                emptyText,
                ScrollState.bind(
                        () -> state.themeScroll,
                        value -> state.themeScroll = value,
                        () -> state.themeScrollDragging,
                        dragging -> state.themeScrollDragging = dragging
                ),
                2,
                refresh,
                (list, theme, index, rowY, rowW) -> addSettingsThemeRow(list, state, refresh, theme, active.equals(theme.id()), rowY, rowW)
        );
    }

    private static void addThemeRow(WidgetGroup list, TabletUiState state, Runnable refresh, UiThemeManager.ThemeInfo theme, boolean active, int x, int y, int w) {
        int fill = active ? withAlpha(theme.success(), 66) : ModColors.SURFACE_PANEL_ALT;
        int border = active ? theme.success() : ModColors.BORDER_BASE;
        list.addWidget(panel(x, y, w, ROW_H, fill, border));

        int swatchX = x + 6;
        addSwatch(list, swatchX, y + 6, theme.panel());
        addSwatch(list, swatchX + 12, y + 6, theme.panelAlt());
        addSwatch(list, swatchX + 24, y + 6, theme.accent());
        addSwatch(list, swatchX + 36, y + 6, theme.success());

        LabelWidget name = new LabelWidget(x + 54, y + 8, theme.label());
        name.setColor(active ? ModColors.TEXT_PRIMARY : theme.text());
        list.addWidget(name);

        ButtonWidget hit = flatHitButton(x, y, w, ROW_H, click -> {
            if (UiThemeManager.setActiveTheme(theme.id())) {
                QuestsAndStuffMod.debugLog("[QnS:UI] theme selected id={}", theme.id());
                refresh.run();
            }
        });
        hit.setHoverTooltips(new Component[]{Component.literal(theme.label())});
        hit.setHoverTexture(Surfaces.bordered(withAlpha(theme.accent(), 54), theme.accent()));
        list.addWidget(hit);
    }

    private static void addSettingsThemeRow(WidgetGroup list, TabletUiState state, Runnable refresh, UiThemeManager.ThemeInfo theme, boolean active, int y, int rowW) {
        int x = SETTINGS_ROW_INSET;
        int h = SETTINGS_ROW_H - SETTINGS_ROW_INSET;
        int w = Math.max(1, rowW - SETTINGS_ROW_INSET * 2);
        int fill = active ? withAlpha(theme.success(), 66) : withAlpha(ModColors.SURFACE_PANEL_ALT, 180);
        int border = active ? theme.success() : ModColors.BORDER_BASE;
        list.addWidget(panel(x, y, w, h, fill, border));

        int swatchX = x + 8;
        int swatchY = y + 8;
        addSwatch(list, swatchX, swatchY, theme.panel());
        addSwatch(list, swatchX + 12, swatchY, theme.panelAlt());
        addSwatch(list, swatchX + 24, swatchY, theme.accent());
        addSwatch(list, swatchX + 36, swatchY, theme.success());

        int labelX = x + 58;
        int labelW = Math.max(16, w - 70);
        LabelWidget name = new LabelWidget(labelX, y + 10, SearchFilter.crop(theme.label(), Math.max(12, labelW / 6)));
        name.setColor(active ? ModColors.TEXT_PRIMARY : theme.text());
        list.addWidget(name);

        ButtonWidget hit = flatHitButton(x, y, w, h, click -> {
            if (UiThemeManager.setActiveTheme(theme.id())) {
                QuestsAndStuffMod.debugLog("[QnS:UI] theme selected id={}", theme.id());
                refresh.run();
            }
        });
        hit.setHoverTooltips(new Component[]{Component.literal(theme.label())});
        hit.setHoverTexture(Surfaces.transparentBorder(theme.accent()));
        hit.setClickedTexture(Surfaces.fill(withAlpha(theme.accent(), 64)));
        list.addWidget(hit);
    }

    private static void addSwatch(WidgetGroup parent, int x, int y, int color) {
        parent.addWidget(panel(x, y, 10, 14, color, ModColors.BORDER_BASE));
    }

}

package com.abo47.questsandstuff.client.tablet.modal;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerListPanel;
import com.abo47.questsandstuff.client.tablet.controls.picker.TiledPickerPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class TabletThemePickerModal {
    private static final int PAD = GRID_8;
    private static final int HEADER_H = GRID_18;
    private static final int ROW_H = ROW_H_26;
    private static final int ROW_GAP = GRID_4;
    private static final int ROW_PAD = GRID_4;
    private static final int SETTINGS_ROW_H = GRID_34;
    private static final int SETTINGS_ROW_INSET = GRID_4;

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
                        () -> state.modal.themeScroll,
                        value -> state.modal.themeScroll = value,
                        () -> state.modal.themeScrollDragging,
                        dragging -> state.modal.themeScrollDragging = dragging
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
                        () -> state.modal.themeScroll,
                        value -> state.modal.themeScroll = value,
                        () -> state.modal.themeScrollDragging,
                        dragging -> state.modal.themeScrollDragging = dragging
                ),
                2,
                refresh,
                (list, theme, index, rowY, rowW) -> addSettingsThemeRow(list, state, refresh, theme, active.equals(theme.id()), rowY, rowW)
        );
    }

    private static void addThemeRow(WidgetGroup list, TabletUiState state, Runnable refresh, UiThemeManager.ThemeInfo theme, boolean active, int x, int y, int w) {
        int fill = active ? withAlpha(theme.success(), 66) : TabletColors.SURFACE_PANEL_ALT;
        int border = active ? theme.success() : TabletColors.BORDER_BASE;
        list.addWidget(panel(x, y, w, ROW_H, fill, border));

        int swatchX = x + GRID_6;
        addSwatch(list, swatchX, y + GRID_6, theme.panel());
        addSwatch(list, swatchX + GRID_12, y + GRID_6, theme.panelAlt());
        addSwatch(list, swatchX + GRID_24, y + GRID_6, theme.accent());
        addSwatch(list, swatchX + 36, y + GRID_6, theme.success());

        LabelWidget name = new LabelWidget(x + 54, y + GRID_8, theme.label());
        name.setColor(active ? TabletColors.TEXT_PRIMARY : theme.text());
        list.addWidget(name);

        ButtonWidget hit = flatHitButton(x, y, w, ROW_H, click -> {
            if (UiThemeManager.setActiveTheme(theme.id())) {
                QuestsAndStuffMod.debugLog("[QnS:UI] theme selected id={}", theme.id());
                refresh.run();
            }
        });
        hit.setHoverTooltips(new Component[]{Component.literal(theme.label())});
        hit.setHoverTexture(GlowShaderHelper.hoverGlow(theme.accent()));
        list.addWidget(hit);
    }

    private static void addSettingsThemeRow(WidgetGroup list, TabletUiState state, Runnable refresh, UiThemeManager.ThemeInfo theme, boolean active, int y, int rowW) {
        int x = SETTINGS_ROW_INSET;
        int h = SETTINGS_ROW_H - SETTINGS_ROW_INSET;
        int w = Math.max(1, rowW - SETTINGS_ROW_INSET * 2);
        int fill = active ? withAlpha(theme.success(), 66) : withAlpha(TabletColors.SURFACE_PANEL_ALT, 180);
        int border = active ? theme.success() : TabletColors.BORDER_BASE;
        list.addWidget(panel(x, y, w, h, fill, border));

        int swatchX = x + GRID_8;
        int swatchY = y + GRID_8;
        addSwatch(list, swatchX, swatchY, theme.panel());
        addSwatch(list, swatchX + GRID_12, swatchY, theme.panelAlt());
        addSwatch(list, swatchX + GRID_24, swatchY, theme.accent());
        addSwatch(list, swatchX + 36, swatchY, theme.success());

        int labelX = x + 58;
        int labelW = Math.max(16, w - 70);
        LabelWidget name = new LabelWidget(labelX, y + GRID_10, SearchFilter.crop(theme.label(), Math.max(12, labelW / 6)));
        name.setColor(active ? TabletColors.TEXT_PRIMARY : theme.text());
        list.addWidget(name);

        ButtonWidget hit = flatHitButton(x, y, w, h, click -> {
            if (UiThemeManager.setActiveTheme(theme.id())) {
                QuestsAndStuffMod.debugLog("[QnS:UI] theme selected id={}", theme.id());
                refresh.run();
            }
        });
        hit.setHoverTooltips(new Component[]{Component.literal(theme.label())});
        hit.setHoverTexture(GlowShaderHelper.hoverGlow(theme.accent()));
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(theme.accent(), 64)));
        list.addWidget(hit);
    }

    private static void addSwatch(WidgetGroup parent, int x, int y, int color) {
        parent.addWidget(panel(x, y, 10, 14, color, TabletColors.BORDER_BASE));
    }

}

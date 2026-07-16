package com.abo47.questsandstuff.client.tablet.settings;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.modal.SettingsOptionDescriptor;
import com.abo47.questsandstuff.client.tablet.modal.SettingsOptionRowRenderer;
import com.abo47.questsandstuff.client.tablet.modal.SettingsTabDescriptor;
import com.abo47.questsandstuff.client.tablet.modal.SettingsTabDescriptors;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;

public final class SettingsOptionsPanelRenderer {
    private static final int ROW_H = SettingsOptionRowRenderer.ROW_H;
    private static final int THEME_ROW_H = GRID_34;
    private static final int THEME_ROW_INSET = GRID_4;

    private SettingsOptionsPanelRenderer() {
    }

    public static void rebuildOptions(WidgetGroup panel, TabletUiState state, Runnable refresh, int listX, int listY, int listW, int listH) {
        panel.clearAllWidgets();
        boolean skinEditMode = state.root.skinEditMode;
        String search = state.settings.search;
        if (!search.isBlank()) {
            List<SettingsOptionDescriptor> matches = SettingsTabDescriptors.search(state, search);
            buildList(panel, listX, listY, listW, listH, ROW_H, matches, I18n.get("ui.questsandstuff.settings.empty"),
                    ScrollState.bind(() -> state.settings.scroll, v -> state.settings.scroll = v, () -> state.settings.scrollDragging, d -> state.settings.scrollDragging = d),
                    refresh, skinEditMode, (list, option, rowY, rowW, mode) -> SettingsOptionRowRenderer.render(list, option, rowY, rowW, refresh, mode));
            return;
        }
        SettingsTabDescriptor tab = SettingsTabDescriptors.descriptor(state.settings.currentTab);
        if (tab.themePicker()) {
            buildThemes(panel, listX, listY, listW, listH, state, refresh, skinEditMode);
            return;
        }
        buildList(panel, listX, listY, listW, listH, ROW_H, tab.options(state), I18n.get("ui.questsandstuff.settings.empty"),
                ScrollState.bind(() -> state.settings.scroll, v -> state.settings.scroll = v, () -> state.settings.scrollDragging, d -> state.settings.scrollDragging = d),
                refresh, skinEditMode, (list, option, rowY, rowW, mode) -> SettingsOptionRowRenderer.render(list, option, rowY, rowW, refresh, mode));
    }

    private interface RowDrawer<T> {
        void draw(WidgetGroup list, T entry, int rowY, int rowW, boolean skinEditMode);
    }

    private static <T> void buildList(WidgetGroup panel, int x, int y, int w, int h, int rowH, List<T> entries,
                                      String emptyText, ScrollState scroll, Runnable refresh, boolean skinEditMode, RowDrawer<T> drawer) {
        int rows = Math.max(1, h / rowH);
        int maxStart = Math.max(0, entries.size() - rows);
        scroll.setValue(ScrollMath.clamp(scroll.value(), maxStart));
        boolean showScroll = maxStart > 0;
        int rowW = showScroll ? w - DragScrollBarWidget.RESERVED_WIDTH - 2 : w;
        WidgetGroup list = new WidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!showScroll) {
                    return false;
                }
                int max = Math.max(0, entries.size() - rows);
                int next = Math.max(0, Math.min(max, scroll.value() + (wheelDelta > 0 ? -1 : 1)));
                if (next == scroll.value()) {
                    return true;
                }
                scroll.setValue(next);
                refresh.run();
                return true;
            }
        };
        panel.addWidget(list);
        if (entries.isEmpty()) {
            list.addWidget(label(8, 8, emptyText, TabletColors.TEXT_MUTED));
            return;
        }
        int end = Math.min(entries.size(), scroll.value() + rows);
        int rowY = 4;
        for (int i = scroll.value(); i < end; i++) {
            drawer.draw(list, entries.get(i), rowY, rowW, skinEditMode);
            rowY += rowH;
        }
        if (showScroll) {
            int barH = Math.max(1, rows * rowH);
            int knobH = Math.max(12, Math.round((float) rows / (float) entries.size() * barH));
            int barX = x + w - DragScrollBarWidget.RESERVED_WIDTH - 1;
            int barY = y + GRID_4;
            panel.addWidget(new DragScrollBarWidget(
                    barX + 1,
                    barY,
                    DragScrollBarWidget.RESERVED_WIDTH,
                    barH,
                    scroll::value,
                    () -> maxStart,
                    () -> knobH,
                    scroll::setValue,
                    scroll::dragging,
                    scroll::setDragging,
                    refresh,
                    TabletColors.scrollTrack(scroll.dragging()),
                    TabletColors.scrollThumb(false),
                    TabletColors.scrollThumb(true),
                    DragScrollBarWidget.WIDTH
            ));
        }
    }

    private static void buildThemes(WidgetGroup panel, int x, int y, int w, int h, TabletUiState state, Runnable refresh, boolean skinEditMode) {
        List<UiThemeManager.ThemeInfo> themes = UiThemeManager.availableThemes();
        if (themes.isEmpty()) {
            panel.addWidget(label(x + 8, y + 8, I18n.get("ui.questsandstuff.settings.themes_empty"), TabletColors.TEXT_MUTED));
            return;
        }
        ScrollState scroll = ScrollState.bind(() -> state.modal.themeScroll, v -> state.modal.themeScroll = v, () -> state.modal.themeScrollDragging, d -> state.modal.themeScrollDragging = d);
        buildList(panel, x, y, w, h, THEME_ROW_H, themes, I18n.get("ui.questsandstuff.settings.themes_empty"), scroll,
                refresh, skinEditMode, (list, theme, rowY, rowW, mode) -> addThemeRow(list, state, refresh, theme, rowY, rowW, mode));
    }

    private static void addThemeRow(WidgetGroup list, TabletUiState state, Runnable refresh, UiThemeManager.ThemeInfo theme, int rowY, int rowW, boolean skinEditMode) {
        boolean active = theme.id().equals(UiThemeManager.activeThemeName());
        int x = 0;
        int h = THEME_ROW_H - THEME_ROW_INSET;
        int w = Math.max(1, rowW);
        int fill = active ? withAlpha(theme.success(), 66) : withAlpha(TabletColors.SURFACE_PANEL_ALT, 180);
        int border = active ? theme.success() : TabletColors.BORDER_BASE;
        list.addWidget(panel(x, rowY, w, h, fill, border));
        int swatchX = GRID_8;
        list.addWidget(panel(swatchX, rowY + GRID_6, 10, 14, theme.panel(), TabletColors.BORDER_BASE));
        list.addWidget(panel(swatchX + GRID_12, rowY + GRID_6, 10, 14, theme.panelAlt(), TabletColors.BORDER_BASE));
        list.addWidget(panel(swatchX + GRID_24, rowY + GRID_6, 10, 14, theme.accent(), TabletColors.BORDER_BASE));
        list.addWidget(panel(swatchX + 36, rowY + GRID_6, 10, 14, theme.success(), TabletColors.BORDER_BASE));
        int labelX = swatchX + 36 + GRID_16;
        list.addWidget(label(labelX, rowY + GRID_8, theme.label(), active ? TabletColors.TEXT_PRIMARY : theme.text()));
        if (!skinEditMode) {
            ButtonWidget hit = flatHitButton(x, rowY, w, h, click -> {
                if (UiThemeManager.setActiveTheme(theme.id())) {
                    QuestsAndStuffMod.debugLog("[QnS:UI] theme selected id={}", theme.id());
                    refresh.run();
                }
            });
            hit.setHoverTexture(GlowShaderHelper.hoverGlow(theme.accent()));
            hit.setHoverTooltips(Component.literal(theme.label()));
            list.addWidget(hit);
        }
    }
}

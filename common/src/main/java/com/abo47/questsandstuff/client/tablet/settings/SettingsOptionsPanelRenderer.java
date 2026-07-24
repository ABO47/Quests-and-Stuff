package com.abo47.questsandstuff.client.tablet.settings;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

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
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;

public final class SettingsOptionsPanelRenderer {
    private static final int ROW_H = SettingsOptionRowRenderer.ROW_H;
    private static final int THEME_ROW_H = ROW_H;
    private static final int THEME_ROW_INSET = GRID_4;
    private static final int LIST_V_PAD = GRID_4;

    private SettingsOptionsPanelRenderer() {
    }

    public static void rebuildOptions(WidgetGroup panel, TabletUiState state, Runnable refresh, int listX, int listY, int listW, int listH) {
        panel.clearAllWidgets();
        SkinAnchorRegistry.unregister("settings_options_list");
        boolean skinEditMode = state.root.skinEditMode;
        String search = state.settings.search;
        if (!search.isBlank()) {
            List<SettingsOptionDescriptor> matches = SettingsTabDescriptors.search(state, search);
            WidgetGroup list = buildList(panel, listX, listY, listW, listH, ROW_H, matches, I18n.get("ui.questsandstuff.settings.empty"),
                    ScrollState.bind(() -> state.settings.scroll, v -> state.settings.scroll = v, () -> state.settings.scrollDragging, d -> state.settings.scrollDragging = d),
                    refresh, skinEditMode, (list2, option, rowY, rowW, mode) -> SettingsOptionRowRenderer.render(list2, option, rowY, rowW, refresh, mode), LIST_V_PAD);
            registerOptionsList(list);
            return;
        }
        SettingsTabDescriptor tab = SettingsTabDescriptors.descriptor(state.settings.currentTab);
        if (tab.themePicker()) {
            buildThemes(panel, listX, listY, listW, listH, state, refresh, skinEditMode);
            return;
        }
        WidgetGroup list = buildList(panel, listX, listY, listW, listH, ROW_H, tab.options(state), I18n.get("ui.questsandstuff.settings.empty"),
                ScrollState.bind(() -> state.settings.scroll, v -> state.settings.scroll = v, () -> state.settings.scrollDragging, d -> state.settings.scrollDragging = d),
                refresh, skinEditMode, (list2, option, rowY, rowW, mode) -> SettingsOptionRowRenderer.render(list2, option, rowY, rowW, refresh, mode), LIST_V_PAD);
        registerOptionsList(list);
    }

    private static void registerOptionsList(WidgetGroup list) {
        if (list == null) {
            return;
        }
        SkinAnchorRegistry.register("settings_options_list", list);
    }

    private interface RowDrawer<T> {
        void draw(WidgetGroup list, T entry, int rowY, int rowW, boolean skinEditMode);
    }

    private static <T> WidgetGroup buildList(WidgetGroup panel, int x, int y, int w, int h, int rowH, List<T> entries,
                                      String emptyText, ScrollState scroll, Runnable refresh, boolean skinEditMode, RowDrawer<T> drawer, int vPad) {
        int rows = ScrollMath.listRows(h - vPad * 2, rowH, GRID_4);
        int maxStart = Math.max(0, entries.size() - rows);
        scroll.setValue(ScrollMath.clamp(scroll.value(), maxStart));
        boolean showScroll = maxStart > 0;
        int rowW = showScroll ? w - DragScrollBarWidget.RESERVED_WIDTH - GRID_8 : w;
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
            list.addWidget(label(8, vPad, emptyText, TabletColors.TEXT_MUTED));
            return list;
        }
        int end = Math.min(entries.size(), scroll.value() + rows);
        int rowY = vPad;
        for (int i = scroll.value(); i < end; i++) {
            drawer.draw(list, entries.get(i), rowY, rowW, skinEditMode);
            rowY += rowH;
        }
        if (showScroll) {
            int barH = Math.max(1, rows * rowH);
            int knobH = Math.max(12, Math.round((float) rows / (float) entries.size() * barH));
            int barX = x + w - DragScrollBarWidget.RESERVED_WIDTH;
            int barY = y + vPad;
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
        return list;
    }

    private static void buildThemes(WidgetGroup panel, int x, int y, int w, int h, TabletUiState state, Runnable refresh, boolean skinEditMode) {
        List<UiThemeManager.ThemeInfo> themes = UiThemeManager.availableThemes();
        if (themes.isEmpty()) {
            panel.addWidget(label(x + 8, y + 8, I18n.get("ui.questsandstuff.settings.themes_empty"), TabletColors.TEXT_MUTED));
            return;
        }
        ScrollState scroll = ScrollState.bind(() -> state.modal.themeScroll, v -> state.modal.themeScroll = v, () -> state.modal.themeScrollDragging, d -> state.modal.themeScrollDragging = d);
        WidgetGroup list = buildList(panel, x, y, w, h, THEME_ROW_H, themes, I18n.get("ui.questsandstuff.settings.themes_empty"), scroll,
                refresh, skinEditMode, (list2, theme, rowY, rowW, mode) -> addThemeRow(list2, state, refresh, theme, rowY, rowW, mode), LIST_V_PAD);
        registerOptionsList(list);
    }

    private static void addThemeRow(WidgetGroup list, TabletUiState state, Runnable refresh, UiThemeManager.ThemeInfo theme, int rowY, int rowW, boolean skinEditMode) {
        boolean active = theme.id().equals(UiThemeManager.activeThemeName());
        int x = 0;
        int h = THEME_ROW_H - THEME_ROW_INSET;
        int w = Math.max(1, rowW);
        int fill = active ? withAlpha(theme.success(), 66) : withAlpha(TabletColors.SURFACE_PANEL_ALT, 180);
        int border = active ? theme.success() : TabletColors.BORDER_BASE;
        list.addWidget(panel(x, rowY, w, h, fill, border));
        list.addWidget(hoverFill(x, rowY, w, h));
        int rowMid = rowY + h / 2;
        int swatchX = GRID_8;
        int swatchH = 12;
        int swatchY = rowMid - swatchH / 2;
        list.addWidget(panel(swatchX, swatchY, 10, swatchH, theme.panel(), TabletColors.BORDER_BASE));
        list.addWidget(panel(swatchX + GRID_12, swatchY, 10, swatchH, theme.panelAlt(), TabletColors.BORDER_BASE));
        list.addWidget(panel(swatchX + GRID_24, swatchY, 10, swatchH, theme.accent(), TabletColors.BORDER_BASE));
        list.addWidget(panel(swatchX + 36, swatchY, 10, swatchH, theme.success(), TabletColors.BORDER_BASE));
        int labelX = swatchX + 36 + GRID_16;
        list.addWidget(label(labelX, rowMid - 4, theme.label(), active ? TabletColors.TEXT_PRIMARY : theme.text()));
        if (!skinEditMode) {
            ButtonWidget hit = flatHitButton(x, rowY, w, h, click -> {
                if (UiThemeManager.setActiveTheme(theme.id())) {
                    QuestsAndStuffMod.debugLog("[QnS:UI] theme selected id={}", theme.id());
                    refresh.run();
                }
            });
            hit.setHoverTexture(GlowShaderHelper.hoverGlow());
            hit.setHoverTooltips(Component.literal(theme.label()));
            list.addWidget(hit);
        }
    }

    private static WidgetGroup hoverFill(int x, int y, int w, int h) {
        WidgetGroup fill = new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if (isMouseOverElement(mouseX, mouseY)) {
                    SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_PANEL_ALT, 26)).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
            }
        };
        return fill;
    }
}

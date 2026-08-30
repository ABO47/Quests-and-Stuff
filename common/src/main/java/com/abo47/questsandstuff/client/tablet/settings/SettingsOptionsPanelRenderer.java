package com.abo47.questsandstuff.client.tablet.settings;

import java.util.List;

import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.modal.SettingsOptionDescriptor;
import com.abo47.questsandstuff.client.tablet.modal.SettingsOptionRowRenderer;
import com.abo47.questsandstuff.client.tablet.modal.SettingsTabDescriptor;
import com.abo47.questsandstuff.client.tablet.modal.SettingsTabDescriptors;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

public final class SettingsOptionsPanelRenderer {
    private static final int ROW_H = SettingsOptionRowRenderer.ROW_H;
    private static final int LIST_V_PAD = GRID_4;

    private SettingsOptionsPanelRenderer() {
    }

    public static void rebuildOptions(WidgetGroup panel, TabletUiState state, Runnable refresh, int listX, int listY, int listW, int listH) {
        panel.clearAllWidgets();
        SkinAnchorRegistry.unregister("settings_option_cards");
        boolean skinEditMode = state.root.skinEditMode;
        String search = state.settings.search;
        RowDrawer<SettingsOptionDescriptor> drawer = (list2, option, rowY, rowW, mode, st) -> SettingsOptionRowRenderer.render(list2, option, rowY, rowW, refresh, mode, st);
        if (!search.isBlank()) {
            List<SettingsOptionDescriptor> matches = SettingsTabDescriptors.search(state, search);
            WidgetGroup list = buildList(panel, listX, listY, listW, listH, ROW_H, matches, I18n.get("ui.questsandstuff.settings.empty"),
                    ScrollState.bind(() -> state.settings.scroll, v -> state.settings.scroll = v, () -> state.settings.scrollDragging, d -> state.settings.scrollDragging = d),
                    refresh, skinEditMode, drawer, LIST_V_PAD, state);
            registerOptionsList(list);
            return;
        }
        SettingsTabDescriptor tab = SettingsTabDescriptors.descriptor(state.settings.currentTab);
        WidgetGroup list = buildList(panel, listX, listY, listW, listH, ROW_H, tab.options(state), I18n.get("ui.questsandstuff.settings.empty"),
                ScrollState.bind(() -> state.settings.scroll, v -> state.settings.scroll = v, () -> state.settings.scrollDragging, d -> state.settings.scrollDragging = d),
                refresh, skinEditMode, drawer, LIST_V_PAD, state);
        registerOptionsList(list);
    }

    private static void registerOptionsList(WidgetGroup list) {
        if (list == null) {
            return;
        }
        SkinAnchorRegistry.register("settings_option_cards", list);
    }

    private interface RowDrawer<T> {
        void draw(WidgetGroup list, T entry, int rowY, int rowW, boolean skinEditMode, TabletUiState state);
    }

    private static <T> WidgetGroup buildList(WidgetGroup panel, int x, int y, int w, int h, int rowH, List<T> entries,
                                      String emptyText, ScrollState scroll, Runnable refresh, boolean skinEditMode, RowDrawer<T> drawer, int vPad, TabletUiState state) {
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
            drawer.draw(list, entries.get(i), rowY, rowW, skinEditMode, state);
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
                    DragScrollBarWidget.WIDTH
            ));
        }
        return list;
    }
}

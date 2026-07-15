package com.abo47.questsandstuff.client.tablet.modal;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerListPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;

public final class TabletSettingsModal {
    private static final int PAD = GRID_8;
    private static final int TAB_Y = 22;
    private static final int TAB_H = GRID_20;
    private static final int TAB_GAP = GRID_4;
    private static final int LIST_Y = 50;

    private TabletSettingsModal() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr("ui.questsandstuff.settings.title"), w, state, refresh);
        state.modal.settingsTab = SettingsTabDescriptors.activeTab(state.modal.settingsTab);
        SettingsTabDescriptor activeTab = SettingsTabDescriptors.active(state);
        addTabs(modal, state, refresh, w);

        int listX = PAD;
        int listW = Math.max(32, w - PAD * 2);
        int listH = Math.max(1, h - LIST_Y - PAD);
        if (activeTab.themePicker()) {
            TabletThemePickerModal.addSettingsThemeList(
                    modal,
                    state,
                    refresh,
                    listX,
                    LIST_Y,
                    listW,
                    listH,
                    TabletModalPanel.tr("ui.questsandstuff.settings.themes_empty")
            );
            return;
        }
        PickerListPanel.add(
                modal,
                listX,
                LIST_Y,
                listW,
                listH,
                SettingsOptionRowRenderer.ROW_H,
                activeTab.options(state),
                TabletModalPanel.tr("ui.questsandstuff.settings.empty"),
                ScrollState.bind(
                        () -> state.modal.settingsScroll,
                        value -> state.modal.settingsScroll = value,
                        () -> state.modal.settingsScrollDragging,
                        dragging -> state.modal.settingsScrollDragging = dragging
                ),
                2,
                refresh,
                (list, option, index, rowY, rowW) -> SettingsOptionRowRenderer.render(list, option, rowY, rowW, refresh)
        );
    }

    private static void addTabs(WidgetGroup modal, TabletUiState state, Runnable refresh, int w) {
        int totalW = Math.max(1, w - PAD * 2);
        int count = Math.max(1, SettingsTabDescriptors.all().size());
        int available = Math.max(count, totalW - TAB_GAP * (count - 1));
        int tabW = Math.max(1, available / count);
        int remainder = Math.max(0, available - tabW * count);
        int tabX = PAD;
        for (int i = 0; i < count; i++) {
            SettingsTabDescriptor tab = SettingsTabDescriptors.all().get(i);
            int currentW = tabW + (i < remainder ? 1 : 0);
            addTab(modal, state, refresh, tabX, currentW, tab);
            tabX += currentW + TAB_GAP;
        }
    }

    private static void addTab(WidgetGroup modal, TabletUiState state, Runnable refresh, int x, int w, SettingsTabDescriptor tab) {
        boolean active = state.modal.settingsTab == tab.id();
        int tabY = active ? TAB_Y : TAB_Y + 3;
        int tabH = active ? TAB_H : TAB_H - 3;
        int fill = active ? withAlpha(TabletColors.SURFACE_BASE, 250) : withAlpha(TabletColors.SURFACE_PANEL_ALT, 142);
        int border = active ? TabletColors.BORDER_ACCENT : TabletColors.BORDER_BASE;
        addTabShadow(modal, x, tabY, w, tabH, active);
        modal.addWidget(panel(x, tabY, w, tabH, fill, border));
        modal.addWidget(label(
                x + 8,
                tabY + 6,
                SearchFilter.crop(TabletModalPanel.tr(tab.labelKey()), Math.max(8, (w - 16) / 6)),
                active ? TabletColors.TEXT_PRIMARY : TabletColors.TEXT_MUTED
        ));
        ButtonWidget hit = flatHitButton(x, tabY, w, tabH, click -> selectTab(state, tab, refresh));
        hit.setHoverTexture(GlowShaderHelper.hoverGlow());
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 82)));
        hit.setHoverTooltips(Component.translatable(tab.labelKey()));
        modal.addWidget(hit);
    }

    private static void addTabShadow(WidgetGroup modal, int x, int y, int w, int h, boolean active) {
        WidgetGroup cast = new WidgetGroup(x + 3, y + 4, w, h);
        cast.setBackground(SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, active ? 126 : 78)));
        modal.addWidget(cast);

        WidgetGroup soft = new WidgetGroup(x + 1, y + 2, w, h);
        soft.setBackground(SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_PANEL, active ? 58 : 34)));
        modal.addWidget(soft);
    }

    private static void selectTab(TabletUiState state, SettingsTabDescriptor tab, Runnable refresh) {
        if (tab == null || state.modal.settingsTab == tab.id()) {
            return;
        }
        state.modal.settingsTab = tab.id();
        state.modal.settingsScroll = 0;
        state.modal.settingsScrollDragging = false;
        state.modal.themeScroll = 0;
        state.modal.themeScrollDragging = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] settings tab selected tab={}", tab.logName());
        refresh.run();
    }
}

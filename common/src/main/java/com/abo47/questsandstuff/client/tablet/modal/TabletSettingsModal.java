package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.TabletSelector;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerListPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;

public final class TabletSettingsModal {
    private static final int PAD = 8;
    private static final int TAB_Y = 22;
    private static final int TAB_H = 20;
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
        TabletSelector.add(
                modal,
                PAD,
                TAB_Y,
                Math.max(64, w - PAD * 2),
                TAB_H,
                tabOptions(),
                () -> SettingsTabDescriptors.active(state),
                tab -> selectTab(state, tab, refresh),
                SettingsTabDescriptors.all().size()
        );
    }

    private static List<TabletSelector.Option<SettingsTabDescriptor>> tabOptions() {
        return SettingsTabDescriptors.all().stream()
                .map(tab -> TabletSelector.option(tab, TabletModalPanel.tr(tab.labelKey())))
                .toList();
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

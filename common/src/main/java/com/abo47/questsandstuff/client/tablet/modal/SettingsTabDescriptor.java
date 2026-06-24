package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.List;
import java.util.function.Function;

record SettingsTabDescriptor(
        int id,
        String logName,
        String labelKey,
        boolean themePicker,
        Function<TabletUiState, List<SettingsOptionDescriptor>> optionProvider
) {
    List<SettingsOptionDescriptor> options(TabletUiState state) {
        return optionProvider.apply(state);
    }
}

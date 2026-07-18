package com.abo47.questsandstuff.client.tablet.modal;

import java.util.List;
import java.util.function.Function;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public record SettingsTabDescriptor(
        int id,
        String logName,
        String labelKey,
        boolean themePicker,
        Function<TabletUiState, List<SettingsOptionDescriptor>> optionProvider
) {
    public List<SettingsOptionDescriptor> options(TabletUiState state) {
        return optionProvider.apply(state);
    }
}

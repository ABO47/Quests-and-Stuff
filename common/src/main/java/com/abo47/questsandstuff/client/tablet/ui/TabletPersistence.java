package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiStatePersistence;

final class TabletPersistence {
    private TabletPersistence() {
    }

    static void readPersistedUiState(TabletUiState state) {
        TabletUiStatePersistence.read(state);
    }

    static boolean readPersistedEditMode() {
        return TabletUiStatePersistence.readEditMode();
    }

    static void persistUiState(TabletUiState state) {
        TabletUiStatePersistence.write(state);
    }

    static void persistEditMode(boolean enabled) {
        TabletUiStatePersistence.writeEditMode(enabled);
    }
}

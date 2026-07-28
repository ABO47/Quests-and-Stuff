package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiStatePersistence;

public final class TabletPersistence {
    private TabletPersistence() {
    }

    public static void readPersistedUiState(TabletUiState state) {
        TabletUiStatePersistence.read(state);
    }

    public static boolean readPersistedEditMode() {
        return TabletUiStatePersistence.readEditMode();
    }

    public static void persistUiState(TabletUiState state) {
        TabletUiStatePersistence.write(state);
    }

    public static void persistEditMode(boolean enabled) {
        TabletUiStatePersistence.writeEditMode(enabled);
    }

    public static void readPersistedSkinState(TabletUiState state) {
        TabletUiStatePersistence.readSkinState(state);
    }

    public static void persistSkinState(TabletUiState state) {
        TabletUiStatePersistence.writeSkinState(state);
    }
}

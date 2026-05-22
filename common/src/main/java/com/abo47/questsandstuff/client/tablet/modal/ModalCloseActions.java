package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletModalState;

public final class ModalCloseActions {
    private ModalCloseActions() {
    }

    public static void closeAll(TabletUiState state) {
        TabletModalState.closeAllModals(state);
    }

    public static void closeAllImmediately(TabletUiState state) {
        TabletModalState.closeAllModalsImmediately(state);
    }

    public static void closeColorPicker(TabletUiState state) {
        TabletModalState.closeAllModals(state);
    }
}

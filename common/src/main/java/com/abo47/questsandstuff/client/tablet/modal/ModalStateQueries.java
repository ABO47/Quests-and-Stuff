package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ModalStateQueries {
    private ModalStateQueries() {
    }

    public static boolean anyOpen(TabletUiState state) {
        return state != null && (state.modalWindowClosing || activeType(state) != ModalWindowManager.ModalType.NONE);
    }

    public static ModalWindowManager.ModalType activeType(TabletUiState state) {
        if (state == null) {
            return ModalWindowManager.ModalType.NONE;
        }
        if (state.modalSession != null && state.modalSession.active()) {
            return state.modalSession.type();
        }
        return ModalWindowManager.typeFromFlags(state);
    }
}

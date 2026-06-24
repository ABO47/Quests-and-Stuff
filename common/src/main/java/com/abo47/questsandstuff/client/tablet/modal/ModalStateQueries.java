package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class ModalStateQueries {
    private ModalStateQueries() {
    }

    public static boolean anyOpen(TabletUiState state) {
        return state != null && (state.modal.modalWindowClosing || activeType(state) != ModalWindowManager.ModalType.NONE);
    }

    public static ModalWindowManager.ModalType activeType(TabletUiState state) {
        if (state == null) {
            return ModalWindowManager.ModalType.NONE;
        }
        if (state.modal.modalSession != null && state.modal.modalSession.active()) {
            return state.modal.modalSession.type();
        }
        return ModalWindowManager.ModalType.NONE;
    }

    public static boolean isOpen(TabletUiState state, ModalWindowManager.ModalType type) {
        ModalWindowManager.ModalType safeType = type == null ? ModalWindowManager.ModalType.NONE : type;
        return activeType(state) == safeType;
    }
}

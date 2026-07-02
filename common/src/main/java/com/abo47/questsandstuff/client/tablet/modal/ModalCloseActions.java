package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.quest.sound.QuestSoundPreview;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState;

public final class ModalCloseActions {
    private ModalCloseActions() {
    }

    public static void closeAll(TabletUiState state) {
        QuestSoundPreview.stop();
        clearTransformSessions(state);
        TabletModalState.closeAllModals(state);
    }

    public static void closeAllImmediately(TabletUiState state) {
        QuestSoundPreview.stop();
        clearTransformSessions(state);
        TabletModalState.closeAllModalsImmediately(state);
    }

    public static void closeColorPicker(TabletUiState state) {
        closeAll(state);
    }

    private static void clearTransformSessions(TabletUiState state) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        CanvasTransformSessions.clearQuestDetailsSession(state);
    }
}

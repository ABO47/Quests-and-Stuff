package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;

public final class QuestDetailsEditState {
    private QuestDetailsEditState() {
    }

    public static boolean editorAvailable(TabletUiState state) {
        return state != null && state.editorAvailable;
    }

    public static boolean canEdit(TabletUiState state) {
        return editorAvailable(state) && state.questDetailsEditMode;
    }

    public static boolean toggle(TabletUiState state) {
        if (!editorAvailable(state)) {
            return false;
        }
        state.questDetailsEditMode = !state.questDetailsEditMode;
        persistUiState(state);
        return true;
    }
}

package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;

public final class QuestDetailsEditState {
    private QuestDetailsEditState() {
    }

    public static boolean editorAvailable(TabletUiState state) {
        return state != null && state.root.editorAvailable;
    }

    public static boolean canEdit(TabletUiState state) {
        return editorAvailable(state) && state.questDetails.questDetailsEditMode;
    }

    public static boolean toggle(TabletUiState state) {
        if (!editorAvailable(state)) {
            return false;
        }
        state.questDetails.questDetailsEditMode = !state.questDetails.questDetailsEditMode;
        persistUiState(state);
        return true;
    }
}

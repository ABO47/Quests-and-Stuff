package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class PrerequisiteConnectionRemover {
    private PrerequisiteConnectionRemover() {
    }

    static boolean canRemove(PrerequisiteConnectionRow row) {
        return row != null
                && row.sourceId() != null
                && !row.sourceId().isBlank()
                && row.targetId() != null
                && !row.targetId().isBlank();
    }

    static void clearAfterRemove(TabletUiState state, PrerequisiteConnectionRow row) {
        if (state == null || row == null) {
            return;
        }
        if (row.key().equals(state.modal.prerequisitesManagerSelectedConnectionKey)) {
            state.modal.prerequisitesManagerSelectedConnectionKey = "";
        }
        if (row.key().equals(state.modal.prerequisitesManagerHoveredConnectionKey)) {
            state.modal.prerequisitesManagerHoveredConnectionKey = "";
        }
        state.modal.prerequisitesManagerContextOpen = false;
        state.modal.prerequisitesManagerContextPrerequisiteId = "";
        ContextMenuController.clearDeleteConfirm(state);
    }
}

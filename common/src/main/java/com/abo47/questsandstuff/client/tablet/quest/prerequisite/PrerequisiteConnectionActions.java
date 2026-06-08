package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class PrerequisiteConnectionActions {
    private PrerequisiteConnectionActions() {
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
        if (row.key().equals(state.prerequisitesManagerSelectedConnectionKey)) {
            state.prerequisitesManagerSelectedConnectionKey = "";
        }
        if (row.key().equals(state.prerequisitesManagerHoveredConnectionKey)) {
            state.prerequisitesManagerHoveredConnectionKey = "";
        }
        state.prerequisitesManagerContextOpen = false;
        state.prerequisitesManagerContextPrerequisiteId = "";
        state.contextDeleteConfirmKey = "";
    }
}

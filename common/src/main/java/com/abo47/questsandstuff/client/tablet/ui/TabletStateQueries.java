package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.util.QuestIdentity;

import java.util.List;

public final class TabletStateQueries {
    private TabletStateQueries() {
    }

    public static int rootWidth(TabletUiState state) {
        return TabletLayout.rootWidth(state);
    }

    public static int rootHeight(TabletUiState state) {
        return TabletLayout.rootHeight(state);
    }

    public static String selectedGroupName(TabletUiState state) {
        String selected = sanitizeGroupName(state == null ? "" : state.selectedGroup);
        if (state == null || state.canEdit || selected.isBlank() || ClientQuestCache.groupOpenablePreview(selected)) {
            return selected;
        }
        for (String group : ClientQuestCache.selectableGroupOrder(false)) {
            String sanitized = sanitizeGroupName(group);
            if (!sanitized.isBlank()) {
                return sanitized;
            }
        }
        return "";
    }

    public static String singleSelectedQuestId(TabletUiState state) {
        if (state == null || state.selectedQuestIds.size() != 1) {
            return "";
        }
        String questId = state.selectedQuestIds.iterator().next();
        return questId == null ? "" : questId;
    }

    public static List<String> selectedQuestIdsSnapshot(TabletUiState state) {
        return state == null ? List.of() : List.copyOf(state.selectedQuestIds);
    }

    public static boolean hasSelectedQuests(TabletUiState state) {
        return state != null && !state.selectedQuestIds.isEmpty();
    }

    public static String sanitizeGroupName(String value) {
        return QuestIdentity.uiGroupName(value);
    }
}

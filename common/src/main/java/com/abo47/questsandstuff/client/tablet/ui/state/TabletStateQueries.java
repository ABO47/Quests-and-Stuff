package com.abo47.questsandstuff.client.tablet.ui.state;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletLayout;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

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

    public static String selectedChapterName(TabletUiState state) {
        String selected = sanitizeChapterName(state == null ? "" : state.root.selectedChapter);
        if (state == null || state.root.canEdit || selected.isBlank() || ClientQuestStateFacade.chapterOpenablePreview(selected)) {
            return selected;
        }
        for (String group : ClientQuestStateFacade.selectableChapterOrder(false)) {
            String sanitized = sanitizeChapterName(group);
            if (!sanitized.isBlank()) {
                return sanitized;
            }
        }
        return "";
    }

    public static String singleSelectedQuestId(TabletUiState state) {
        if (state == null || state.canvas.canvasSelection.questIds().size() != 1) {
            return "";
        }
        String questId = state.canvas.canvasSelection.questIds().iterator().next();
        return questId == null ? "" : questId;
    }

    public static List<String> selectedQuestIdSnapshot(TabletUiState state) {
        return state == null ? List.of() : List.copyOf(state.canvas.canvasSelection.questIds());
    }

    public static boolean hasSelectedQuests(TabletUiState state) {
        return state != null && !state.canvas.canvasSelection.questIds().isEmpty();
    }

    public static String sanitizeChapterName(String value) {
        return QuestIdentity.uiChapterName(value);
    }
}

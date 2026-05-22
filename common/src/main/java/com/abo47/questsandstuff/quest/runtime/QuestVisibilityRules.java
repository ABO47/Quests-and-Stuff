package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;

final class QuestVisibilityRules {
    private QuestVisibilityRules() {
    }

    static boolean isVisibleFor(PlayerQuestState state, QuestDefinition definition) {
        QuestProgressState progress = state.quest(definition.id());
        if (progress.completed()) {
            return true;
        }
        return switch (definition.settings().hiddenMode()) {
            case LOCKED -> progress.unlocked();
            case IN_PROGRESS -> progress.unlocked();
            case COMPLETED -> progress.completed();
            case PREREQUISITES_VISIBLE -> progress.unlocked() || prerequisitesVisible(state, definition);
        };
    }

    private static boolean prerequisitesVisible(PlayerQuestState state, QuestDefinition definition) {
        for (String prerequisite : definition.prerequisites()) {
            QuestProgressState prerequisiteState = state.quest(prerequisite);
            if (prerequisiteState.completed() || prerequisiteState.unlocked()) {
                return true;
            }
        }
        return false;
    }
}

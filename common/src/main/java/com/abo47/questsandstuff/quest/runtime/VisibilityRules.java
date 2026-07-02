package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class VisibilityRules {
    private VisibilityRules() {
    }

    static boolean isVisibleFor(PlayerQuestState state, QuestDefinition definition) {
        return isVisibleFor(state, definition, Map.of());
    }

    static boolean isVisibleFor(PlayerQuestState state, QuestDefinition definition, Map<String, QuestDefinition> definitions) {
        return isVisibleFor(state, definition, definitions == null ? Map.of() : definitions, new HashSet<>());
    }

    private static boolean isVisibleFor(PlayerQuestState state, QuestDefinition definition, Map<String, QuestDefinition> definitions, Set<String> visiting) {
        if (definition == null) {
            return false;
        }
        if (!visiting.add(definition.id())) {
            return false;
        }

        try {
            QuestProgressState progress = state.quest(definition.id());
            if (progress.completed()) {
                return true;
            }
            return switch (definition.settings().hiddenMode()) {
                case LOCKED -> progress.unlocked();
                case IN_PROGRESS -> progress.unlocked();
                case COMPLETED -> progress.completed();
                case PREREQUISITES_VISIBLE -> progress.unlocked() || prerequisitesVisible(state, definition, definitions, visiting);
            };
        } finally {
            visiting.remove(definition.id());
        }
    }

    private static boolean prerequisitesVisible(PlayerQuestState state, QuestDefinition definition, Map<String, QuestDefinition> definitions, Set<String> visiting) {
        for (String prerequisite : definition.prerequisites()) {
            QuestDefinition prerequisiteDefinition = definitions.get(prerequisite);
            if (prerequisiteDefinition != null && isVisibleFor(state, prerequisiteDefinition, definitions, visiting)) {
                return true;
            }
            QuestProgressState prerequisiteState = state.quest(prerequisite);
            if (prerequisiteState.completed() || prerequisiteState.unlocked()) {
                return true;
            }
        }
        return false;
    }
}

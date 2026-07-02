package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisibilityRulesTest {
    @Test
    void completedQuestsStayVisibleForEveryHiddenMode() {
        PlayerQuestState state = new PlayerQuestState();
        state.quest("done").setCompleted(true, 42);
        QuestDefinition definition = quest("done", QuestVisibilityMode.COMPLETED, Set.of());

        assertTrue(VisibilityRules.isVisibleFor(state, definition, Map.of(definition.id(), definition)));
    }

    @Test
    void lockedModeOnlyShowsUnlockedQuests() {
        PlayerQuestState state = new PlayerQuestState();
        QuestDefinition definition = quest("locked", QuestVisibilityMode.LOCKED, Set.of());
        Map<String, QuestDefinition> definitions = Map.of(definition.id(), definition);

        assertFalse(VisibilityRules.isVisibleFor(state, definition, definitions));

        state.quest("locked").setUnlocked(true);
        assertTrue(VisibilityRules.isVisibleFor(state, definition, definitions));
    }

    @Test
    void prerequisitesVisibleModeShowsWhenAPrerequisiteIsKnown() {
        PlayerQuestState state = new PlayerQuestState();
        QuestDefinition parent = quest("parent", QuestVisibilityMode.LOCKED, Set.of());
        QuestDefinition definition = quest("child", QuestVisibilityMode.PREREQUISITES_VISIBLE, Set.of("parent"));
        Map<String, QuestDefinition> definitions = Map.of(parent.id(), parent, definition.id(), definition);

        assertFalse(VisibilityRules.isVisibleFor(state, definition, definitions));

        state.quest("parent").setUnlocked(true);
        assertTrue(VisibilityRules.isVisibleFor(state, definition, definitions));
    }

    private static QuestDefinition quest(String id, QuestVisibilityMode mode, Set<String> prerequisites) {
        QuestSettings settings = new QuestSettings(false, mode, false, false, false, true);
        return new QuestDefinition(QuestDefinition.CURRENT_SCHEMA, id, QuestDisplay.DEFAULT, settings, prerequisites, Map.of(), Map.of());
    }
}

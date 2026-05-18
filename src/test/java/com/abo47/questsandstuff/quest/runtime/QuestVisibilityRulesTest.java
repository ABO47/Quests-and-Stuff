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

class QuestVisibilityRulesTest {
    @Test
    void completedQuestsStayVisibleForEveryHiddenMode() {
        PlayerQuestState state = new PlayerQuestState();
        state.quest("done").setCompleted(true, 42);

        assertTrue(QuestVisibilityRules.isVisibleFor(state, quest("done", QuestVisibilityMode.COMPLETED, Set.of())));
    }

    @Test
    void lockedModeOnlyShowsUnlockedQuests() {
        PlayerQuestState state = new PlayerQuestState();
        QuestDefinition definition = quest("locked", QuestVisibilityMode.LOCKED, Set.of());

        assertFalse(QuestVisibilityRules.isVisibleFor(state, definition));

        state.quest("locked").setUnlocked(true);
        assertTrue(QuestVisibilityRules.isVisibleFor(state, definition));
    }

    @Test
    void prerequisitesVisibleModeShowsWhenAPrerequisiteIsKnown() {
        PlayerQuestState state = new PlayerQuestState();
        QuestDefinition definition = quest("child", QuestVisibilityMode.PREREQUISITES_VISIBLE, Set.of("parent"));

        assertFalse(QuestVisibilityRules.isVisibleFor(state, definition));

        state.quest("parent").setUnlocked(true);
        assertTrue(QuestVisibilityRules.isVisibleFor(state, definition));
    }

    private static QuestDefinition quest(String id, QuestVisibilityMode mode, Set<String> prerequisites) {
        QuestSettings settings = new QuestSettings(false, mode, false, false, false, true);
        return new QuestDefinition(QuestDefinition.CURRENT_SCHEMA, id, QuestDisplay.DEFAULT, settings, prerequisites, Map.of(), Map.of());
    }
}

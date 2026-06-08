package com.abo47.questsandstuff.client.sync.cache;

import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientQuestPreviewRulesTest {
    @BeforeEach
    void resetClientState() {
        ClientQuestCache.resetStateForTests();
    }

    @Test
    void lockedGroupIsNotSelectableUntilAQuestInsideIsUnlockedOrCompleted() {
        ClientQuestCache.createGroupLocal("locked");
        ClientQuestCache.createGroupLocal("open");
        ClientQuestCache.createEditorQuestLocal("quest/locked", "locked", 0, 0, "Locked");
        ClientQuestCache.createEditorQuestLocal("quest/open", "open", 0, 0, "Open");
        ClientQuestCache.setGroupLockUntilUnlockedLocal("locked", true);
        setQuestState("quest/locked", false, false);

        assertTrue(ClientQuestCache.groupLockedPreview("locked"));
        assertFalse(ClientQuestCache.groupOpenablePreview("locked"));
        assertEquals(List.of("open"), ClientQuestCache.selectableGroupOrder(false));
        assertEquals(List.of("locked", "open"), ClientQuestCache.selectableGroupOrder(true));

        setQuestState("quest/locked", true, false);

        assertFalse(ClientQuestCache.groupLockedPreview("locked"));
        assertTrue(ClientQuestCache.groupOpenablePreview("locked"));
        assertEquals(List.of("locked", "open"), ClientQuestCache.selectableGroupOrder(false));
    }

    @Test
    void hiddenGroupIsInvisibleUntilAQuestInsideIsUnlockedOrCompleted() {
        ClientQuestCache.createGroupLocal("hidden");
        ClientQuestCache.createGroupLocal("open");
        ClientQuestCache.createEditorQuestLocal("quest/hidden", "hidden", 0, 0, "Hidden");
        ClientQuestCache.createEditorQuestLocal("quest/open", "open", 0, 0, "Open");
        ClientQuestCache.setGroupHideUntilUnlockedLocal("hidden", true);
        setQuestState("quest/hidden", false, false);

        assertTrue(ClientQuestCache.groupHiddenPreview("hidden"));
        assertFalse(ClientQuestCache.groupOpenablePreview("hidden"));
        assertEquals(List.of("open"), ClientQuestCache.visibleGroupOrder(false));
        assertEquals(List.of("hidden", "open"), ClientQuestCache.visibleGroupOrder(true));

        setQuestState("quest/hidden", false, true);

        assertFalse(ClientQuestCache.groupHiddenPreview("hidden"));
        assertTrue(ClientQuestCache.groupOpenablePreview("hidden"));
        assertEquals(List.of("hidden", "open"), ClientQuestCache.visibleGroupOrder(false));
    }

    @Test
    void questPreviewRulesRespectUnlockedAndCompletedState() {
        CompoundTag quest = new CompoundTag();
        quest.putString(QuestSyncKeys.Quest.HIDDEN_MODE, "locked");
        quest.putBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN, true);
        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, false);
        quest.putBoolean(QuestSyncKeys.Quest.COMPLETED, false);

        assertTrue(ClientQuestCache.questLockedPreview(quest));
        assertTrue(ClientQuestCache.questHiddenPreview(quest));

        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);

        assertFalse(ClientQuestCache.questLockedPreview(quest));
        assertFalse(ClientQuestCache.questHiddenPreview(quest));
    }

    private static void setQuestState(String questId, boolean unlocked, boolean completed) {
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, unlocked);
        quest.putBoolean(QuestSyncKeys.Quest.COMPLETED, completed);
    }
}

package com.abo47.questsandstuff.client.sync.state;

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
        ClientQuestStateFacade.resetStateForTests();
    }

    @Test
    void lockedGroupIsNotSelectableUntilAQuestInsideIsUnlockedOrCompleted() {
        ClientQuestStateFacade.createGroupLocal("locked");
        ClientQuestStateFacade.createGroupLocal("open");
        ClientQuestStateFacade.createEditorQuestLocal("quest/locked", "locked", 0, 0, "Locked");
        ClientQuestStateFacade.createEditorQuestLocal("quest/open", "open", 0, 0, "Open");
        ClientQuestStateFacade.setGroupLockUntilUnlockedLocal("locked", true);
        setQuestState("quest/locked", false, false);

        assertTrue(ClientQuestStateFacade.groupLockedPreview("locked"));
        assertFalse(ClientQuestStateFacade.groupOpenablePreview("locked"));
        assertEquals(List.of("open"), ClientQuestStateFacade.selectableGroupOrder(false));
        assertEquals(List.of("locked", "open"), ClientQuestStateFacade.selectableGroupOrder(true));

        setQuestState("quest/locked", true, false);

        assertFalse(ClientQuestStateFacade.groupLockedPreview("locked"));
        assertTrue(ClientQuestStateFacade.groupOpenablePreview("locked"));
        assertEquals(List.of("locked", "open"), ClientQuestStateFacade.selectableGroupOrder(false));
    }

    @Test
    void hiddenGroupIsInvisibleUntilAQuestInsideIsUnlockedOrCompleted() {
        ClientQuestStateFacade.createGroupLocal("hidden");
        ClientQuestStateFacade.createGroupLocal("open");
        ClientQuestStateFacade.createEditorQuestLocal("quest/hidden", "hidden", 0, 0, "Hidden");
        ClientQuestStateFacade.createEditorQuestLocal("quest/open", "open", 0, 0, "Open");
        ClientQuestStateFacade.setGroupHideUntilUnlockedLocal("hidden", true);
        setQuestState("quest/hidden", false, false);

        assertTrue(ClientQuestStateFacade.groupHiddenPreview("hidden"));
        assertFalse(ClientQuestStateFacade.groupOpenablePreview("hidden"));
        assertEquals(List.of("open"), ClientQuestStateFacade.visibleGroupOrder(false));
        assertEquals(List.of("hidden", "open"), ClientQuestStateFacade.visibleGroupOrder(true));

        setQuestState("quest/hidden", false, true);

        assertFalse(ClientQuestStateFacade.groupHiddenPreview("hidden"));
        assertTrue(ClientQuestStateFacade.groupOpenablePreview("hidden"));
        assertEquals(List.of("hidden", "open"), ClientQuestStateFacade.visibleGroupOrder(false));
    }

    @Test
    void questPreviewRulesRespectUnlockedAndCompletedState() {
        CompoundTag quest = new CompoundTag();
        quest.putString(QuestSyncKeys.Quest.HIDDEN_MODE, "locked");
        quest.putBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN, true);
        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, false);
        quest.putBoolean(QuestSyncKeys.Quest.COMPLETED, false);

        assertTrue(ClientQuestStateFacade.questLockedPreview(quest));
        assertTrue(ClientQuestStateFacade.questHiddenPreview(quest));

        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);

        assertFalse(ClientQuestStateFacade.questLockedPreview(quest));
        assertFalse(ClientQuestStateFacade.questHiddenPreview(quest));
    }

    private static void setQuestState(String questId, boolean unlocked, boolean completed) {
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, unlocked);
        quest.putBoolean(QuestSyncKeys.Quest.COMPLETED, completed);
    }
}

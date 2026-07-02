package com.abo47.questsandstuff.client.sync.state;

import com.abo47.questsandstuff.quest.sync.SyncKeys;
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
    void lockedChapterIsNotSelectableUntilAQuestInsideIsUnlockedOrCompleted() {
        ClientQuestStateFacade.createChapterLocal("locked");
        ClientQuestStateFacade.createChapterLocal("open");
        ClientQuestStateFacade.createEditorQuestLocal("quest/locked", "locked", 0, 0, "Locked");
        ClientQuestStateFacade.createEditorQuestLocal("quest/open", "open", 0, 0, "Open");
        ClientQuestStateFacade.setChapterLockUntilUnlockedLocal("locked", true);
        setQuestState("quest/locked", false, false);

        assertTrue(ClientQuestStateFacade.chapterLockedPreview("locked"));
        assertFalse(ClientQuestStateFacade.chapterOpenablePreview("locked"));
        assertEquals(List.of("open"), ClientQuestStateFacade.selectableChapterOrder(false));
        assertEquals(List.of("locked", "open"), ClientQuestStateFacade.selectableChapterOrder(true));

        setQuestState("quest/locked", true, false);

        assertFalse(ClientQuestStateFacade.chapterLockedPreview("locked"));
        assertTrue(ClientQuestStateFacade.chapterOpenablePreview("locked"));
        assertEquals(List.of("locked", "open"), ClientQuestStateFacade.selectableChapterOrder(false));
    }

    @Test
    void hiddenChapterIsInvisibleUntilAQuestInsideIsUnlockedOrCompleted() {
        ClientQuestStateFacade.createChapterLocal("hidden");
        ClientQuestStateFacade.createChapterLocal("open");
        ClientQuestStateFacade.createEditorQuestLocal("quest/hidden", "hidden", 0, 0, "Hidden");
        ClientQuestStateFacade.createEditorQuestLocal("quest/open", "open", 0, 0, "Open");
        ClientQuestStateFacade.setChapterHideUntilUnlockedLocal("hidden", true);
        setQuestState("quest/hidden", false, false);

        assertTrue(ClientQuestStateFacade.chapterHiddenPreview("hidden"));
        assertFalse(ClientQuestStateFacade.chapterOpenablePreview("hidden"));
        assertEquals(List.of("open"), ClientQuestStateFacade.visibleChapterOrder(false));
        assertEquals(List.of("hidden", "open"), ClientQuestStateFacade.visibleChapterOrder(true));

        setQuestState("quest/hidden", false, true);

        assertFalse(ClientQuestStateFacade.chapterHiddenPreview("hidden"));
        assertTrue(ClientQuestStateFacade.chapterOpenablePreview("hidden"));
        assertEquals(List.of("hidden", "open"), ClientQuestStateFacade.visibleChapterOrder(false));
    }

    @Test
    void questPreviewRulesRespectUnlockedAndCompletedState() {
        CompoundTag quest = new CompoundTag();
        quest.putString(SyncKeys.Quest.HIDDEN_MODE, "locked");
        quest.putBoolean(SyncKeys.Quest.VISUAL_HIDDEN, true);
        quest.putBoolean(SyncKeys.Quest.UNLOCKED, false);
        quest.putBoolean(SyncKeys.Quest.COMPLETED, false);

        assertTrue(ClientQuestStateFacade.questLockedPreview(quest));
        assertTrue(ClientQuestStateFacade.questHiddenPreview(quest));

        quest.putBoolean(SyncKeys.Quest.UNLOCKED, true);

        assertFalse(ClientQuestStateFacade.questLockedPreview(quest));
        assertFalse(ClientQuestStateFacade.questHiddenPreview(quest));
    }

    private static void setQuestState(String questId, boolean unlocked, boolean completed) {
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        quest.putBoolean(SyncKeys.Quest.UNLOCKED, unlocked);
        quest.putBoolean(SyncKeys.Quest.COMPLETED, completed);
    }
}

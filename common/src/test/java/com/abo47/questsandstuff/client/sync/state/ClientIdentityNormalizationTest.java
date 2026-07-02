package com.abo47.questsandstuff.client.sync.state;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientIdentityNormalizationTest {
    @AfterEach
    void tearDown() {
        ClientQuestState.reset();
        ClientChapterState.reset();
    }

    @Test
    void clientQuestStateUsesSharedQuestIdNormalization() {
        CompoundTag quest = new CompoundTag();
        quest.putString("title", "Stored");

        ClientQuestState.putQuest(" \\Chapter//Quest ", quest);

        assertTrue(ClientQuestState.containsQuest("Chapter/Quest"));
        assertTrue(ClientQuestState.containsQuest("/Chapter/Quest"));
        assertEquals(Set.of("Chapter/Quest"), ClientQuestState.questIdsSnapshot());
    }

    @Test
    void blankClientQuestIdsStayRejected() {
        ClientQuestState.putQuest(" / ", new CompoundTag());

        assertFalse(ClientQuestState.containsQuest(""));
        assertTrue(ClientQuestState.questIdsSnapshot().isEmpty());
    }

    @Test
    void clientChapterStateUsesSharedGroupNameNormalization() {
        assertTrue(ClientChapterState.addGroup(" Main Chapter "));

        assertTrue(ClientChapterState.containsGroup("Main Chapter"));
        assertFalse(ClientChapterState.containsGroup("main chapter"));
        assertEquals("Main Chapter", ClientChapterState.groupOrderSnapshot().get(0));
    }
}

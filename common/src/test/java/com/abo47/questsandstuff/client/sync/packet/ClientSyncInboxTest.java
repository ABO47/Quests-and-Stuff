package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientSyncInboxTest {
    @BeforeEach
    void resetClientState() {
        ClientQuestStateFacade.resetStateForTests();
        ClientSyncUiBridge.resetForTests();
    }

    @Test
    void fullChunksApplyWhenArrivingOutOfOrderAndRefreshOnce() {
        AtomicInteger refreshes = new AtomicInteger();
        ClientSyncUiBridge.registerTabletCallbacks(null, refreshes::incrementAndGet, null, null);

        ClientQuestStateFacade.acceptFullChunk(10L, 1, 2, fullPart("quest/b", false));

        assertFalse(ClientQuestStateFacade.containsQuest("quest/b"));
        assertEquals(0, refreshes.get());

        ClientQuestStateFacade.acceptFullChunk(10L, 0, 2, fullPart("quest/a", true));

        assertTrue(ClientQuestStateFacade.containsQuest("quest/a"));
        assertTrue(ClientQuestStateFacade.containsQuest("quest/b"));
        assertEquals(List.of("main"), ClientQuestStateFacade.chapterOrder());
        assertEquals(1, refreshes.get());
    }

    @Test
    void duplicateChunkUsesLatestPayloadBeforeApply() {
        ClientQuestStateFacade.acceptFullChunk(20L, 0, 2, fullPart("quest/old", true));
        ClientQuestStateFacade.acceptFullChunk(20L, 0, 2, fullPart("quest/new", true));
        ClientQuestStateFacade.acceptFullChunk(20L, 1, 2, fullPart("quest/second", false));

        assertFalse(ClientQuestStateFacade.containsQuest("quest/old"));
        assertTrue(ClientQuestStateFacade.containsQuest("quest/new"));
        assertTrue(ClientQuestStateFacade.containsQuest("quest/second"));
    }

    @Test
    void malformedChunkMetadataDoesNotApplyOrRefresh() {
        AtomicInteger refreshes = new AtomicInteger();
        ClientSyncUiBridge.registerTabletCallbacks(null, refreshes::incrementAndGet, null, null);

        ClientQuestStateFacade.acceptFullChunk(30L, -1, 2, fullPart("quest/bad-index", true));
        ClientQuestStateFacade.acceptFullChunk(31L, 0, 0, fullPart("quest/bad-count", true));

        assertFalse(ClientQuestStateFacade.containsQuest("quest/bad-index"));
        assertFalse(ClientQuestStateFacade.containsQuest("quest/bad-count"));
        assertEquals(0, refreshes.get());
    }

    @Test
    void deltaRefreshesOnlyWhenChapterPayloadChanges() {
        AtomicInteger refreshes = new AtomicInteger();
        ClientSyncUiBridge.registerTabletCallbacks(null, refreshes::incrementAndGet, null, null);

        ClientQuestStateFacade.applyDeltaSync(deltaPart("quest/changed", false));

        assertTrue(ClientQuestStateFacade.containsQuest("quest/changed"));
        assertEquals(0, refreshes.get());

        ClientQuestStateFacade.applyDeltaSync(deltaPart("quest/chapter", true));

        assertTrue(ClientQuestStateFacade.containsQuest("quest/chapter"));
        assertEquals(1, refreshes.get());
    }

    @Test
    void syncCanApplyWithoutAnActiveTabletRefreshHandler() {
        ClientSyncUiBridge.resetForTests();

        assertDoesNotThrow(() -> ClientQuestStateFacade.applyFullSync(fullPart("quest/no-tablet", true)));

        assertTrue(ClientQuestStateFacade.containsQuest("quest/no-tablet"));
        assertEquals(List.of("main"), ClientQuestStateFacade.chapterOrder());
    }

    private static CompoundTag fullPart(String questId, boolean includeChapterPayload) {
        CompoundTag part = new CompoundTag();
        if (includeChapterPayload) {
            part.put(SyncKeys.CHAPTERS, groupList());
            part.put(SyncKeys.CHAPTER_PROPS, groupProps());
        }
        part.put(SyncKeys.QUESTS, keyedQuest(questId));
        return part;
    }

    private static CompoundTag deltaPart(String questId, boolean includeChapterPayload) {
        CompoundTag part = new CompoundTag();
        if (includeChapterPayload) {
            part.put(SyncKeys.CHAPTERS, groupList());
            part.put(SyncKeys.CHAPTER_PROPS, groupProps());
        }
        part.put(SyncKeys.CHANGED, keyedQuest(questId));
        part.put(SyncKeys.REMOVED, new CompoundTag());
        return part;
    }

    private static CompoundTag keyedQuest(String questId) {
        CompoundTag quests = new CompoundTag();
        quests.put(questId, questTag(questId));
        return quests;
    }

    private static CompoundTag questTag(String questId) {
        CompoundTag quest = new CompoundTag();
        quest.putString(SyncKeys.Quest.TITLE, questId);
        CompoundTag groups = new CompoundTag();
        groups.put("main", new CompoundTag());
        quest.put(SyncKeys.Quest.CHAPTERS, groups);
        return quest;
    }

    private static ListTag groupList() {
        ListTag groups = new ListTag();
        groups.add(StringTag.valueOf("main"));
        return groups;
    }

    private static CompoundTag groupProps() {
        CompoundTag groupProps = new CompoundTag();
        CompoundTag main = new CompoundTag();
        main.putBoolean(SyncKeys.ChapterProps.LOCK_UNTIL_UNLOCKED, false);
        main.putBoolean(SyncKeys.ChapterProps.HIDE_UNTIL_UNLOCKED, false);
        groupProps.put("main", main);
        return groupProps;
    }
}

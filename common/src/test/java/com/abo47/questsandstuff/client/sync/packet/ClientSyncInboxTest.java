package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
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
        ClientQuestCache.resetStateForTests();
        ClientSyncUiBridge.resetForTests();
    }

    @Test
    void fullChunksApplyWhenArrivingOutOfOrderAndRefreshOnce() {
        AtomicInteger refreshes = new AtomicInteger();
        ClientSyncUiBridge.registerTabletCallbacks(null, refreshes::incrementAndGet, null, null);

        ClientQuestCache.acceptFullChunk(10L, 1, 2, fullPart("quest/b", false));

        assertFalse(ClientQuestCache.containsQuest("quest/b"));
        assertEquals(0, refreshes.get());

        ClientQuestCache.acceptFullChunk(10L, 0, 2, fullPart("quest/a", true));

        assertTrue(ClientQuestCache.containsQuest("quest/a"));
        assertTrue(ClientQuestCache.containsQuest("quest/b"));
        assertEquals(List.of("main"), ClientQuestCache.groupOrder());
        assertEquals(1, refreshes.get());
    }

    @Test
    void duplicateChunkUsesLatestPayloadBeforeApply() {
        ClientQuestCache.acceptFullChunk(20L, 0, 2, fullPart("quest/old", true));
        ClientQuestCache.acceptFullChunk(20L, 0, 2, fullPart("quest/new", true));
        ClientQuestCache.acceptFullChunk(20L, 1, 2, fullPart("quest/second", false));

        assertFalse(ClientQuestCache.containsQuest("quest/old"));
        assertTrue(ClientQuestCache.containsQuest("quest/new"));
        assertTrue(ClientQuestCache.containsQuest("quest/second"));
    }

    @Test
    void malformedChunkMetadataDoesNotApplyOrRefresh() {
        AtomicInteger refreshes = new AtomicInteger();
        ClientSyncUiBridge.registerTabletCallbacks(null, refreshes::incrementAndGet, null, null);

        ClientQuestCache.acceptFullChunk(30L, -1, 2, fullPart("quest/bad-index", true));
        ClientQuestCache.acceptFullChunk(31L, 0, 0, fullPart("quest/bad-count", true));

        assertFalse(ClientQuestCache.containsQuest("quest/bad-index"));
        assertFalse(ClientQuestCache.containsQuest("quest/bad-count"));
        assertEquals(0, refreshes.get());
    }

    @Test
    void deltaRefreshesOnlyWhenChapterPayloadChanges() {
        AtomicInteger refreshes = new AtomicInteger();
        ClientSyncUiBridge.registerTabletCallbacks(null, refreshes::incrementAndGet, null, null);

        ClientQuestCache.applyDeltaSync(deltaPart("quest/changed", false));

        assertTrue(ClientQuestCache.containsQuest("quest/changed"));
        assertEquals(0, refreshes.get());

        ClientQuestCache.applyDeltaSync(deltaPart("quest/chapter", true));

        assertTrue(ClientQuestCache.containsQuest("quest/chapter"));
        assertEquals(1, refreshes.get());
    }

    @Test
    void syncCanApplyWithoutAnActiveTabletRefreshHandler() {
        ClientSyncUiBridge.resetForTests();

        assertDoesNotThrow(() -> ClientQuestCache.applyFullSync(fullPart("quest/no-tablet", true)));

        assertTrue(ClientQuestCache.containsQuest("quest/no-tablet"));
        assertEquals(List.of("main"), ClientQuestCache.groupOrder());
    }

    private static CompoundTag fullPart(String questId, boolean includeChapterPayload) {
        CompoundTag part = new CompoundTag();
        if (includeChapterPayload) {
            part.put(QuestSyncKeys.GROUPS, groupList());
            part.put(QuestSyncKeys.GROUP_PROPS, groupProps());
        }
        part.put(QuestSyncKeys.QUESTS, keyedQuest(questId));
        return part;
    }

    private static CompoundTag deltaPart(String questId, boolean includeChapterPayload) {
        CompoundTag part = new CompoundTag();
        if (includeChapterPayload) {
            part.put(QuestSyncKeys.GROUPS, groupList());
            part.put(QuestSyncKeys.GROUP_PROPS, groupProps());
        }
        part.put(QuestSyncKeys.CHANGED, keyedQuest(questId));
        part.put(QuestSyncKeys.REMOVED, new CompoundTag());
        return part;
    }

    private static CompoundTag keyedQuest(String questId) {
        CompoundTag quests = new CompoundTag();
        quests.put(questId, questTag(questId));
        return quests;
    }

    private static CompoundTag questTag(String questId) {
        CompoundTag quest = new CompoundTag();
        quest.putString(QuestSyncKeys.Quest.TITLE, questId);
        CompoundTag groups = new CompoundTag();
        groups.put("main", new CompoundTag());
        quest.put(QuestSyncKeys.Quest.GROUPS, groups);
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
        main.putBoolean(QuestSyncKeys.GroupProps.LOCK_UNTIL_UNLOCKED, false);
        main.putBoolean(QuestSyncKeys.GroupProps.HIDE_UNTIL_UNLOCKED, false);
        groupProps.put("main", main);
        return groupProps;
    }
}

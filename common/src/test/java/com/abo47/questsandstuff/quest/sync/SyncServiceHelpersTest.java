package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncServiceHelpersTest {
    @TempDir
    Path root;

    @Test
    void fullChunksSplitAtQuestLimitAndUseSharedSchema() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        try {
            for (int i = 0; i < 129; i++) {
                store.upsert(quest("quest_" + threeDigits(i), "main"));
            }
            store.setChapterOrder(List.of("main"));
            SyncChunker chunker = new SyncChunker(new SyncPayloadBuilder(store));

            List<SyncChunker.SyncChunk> chunks = chunker.fullChunks(new PlayerQuestState(), store.questIds(), true);

            assertEquals(2, chunks.size());
            assertEquals(0, chunks.get(0).chunkIndex());
            assertEquals(2, chunks.get(0).chunkCount());
            assertEquals(1, chunks.get(1).chunkIndex());
            assertEquals(2, chunks.get(1).chunkCount());
            assertEquals(QuestDefinition.CURRENT_SCHEMA, chunks.get(0).payload().getInt(SyncKeys.SCHEMA));
            assertEquals(128, chunks.get(0).payload().getCompound(SyncKeys.QUESTS).getAllKeys().size());
            assertEquals(1, chunks.get(1).payload().getCompound(SyncKeys.QUESTS).getAllKeys().size());
            assertTrue(containsString(chunks.get(0).payload().getList(SyncKeys.CHAPTERS, Tag.TAG_STRING), "main"));
        } finally {
            store.shutdown();
        }
    }

    @Test
    void deltaChunksPutMetadataAndRemovedIdsOnlyOnFirstChunk() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        try {
            for (int i = 0; i < 129; i++) {
                store.upsert(quest("quest_" + threeDigits(i), "main"));
            }
            store.setChapterOrder(List.of("main"));
            SyncChunker chunker = new SyncChunker(new SyncPayloadBuilder(store));

            List<SyncChunker.SyncChunk> chunks = chunker.deltaChunks(
                    new PlayerQuestState(),
                    store.questIds(),
                    Set.of("quest_removed"),
                    Set.of("main"),
                    true
            );

            assertEquals(2, chunks.size());
            CompoundTag first = chunks.get(0).payload();
            CompoundTag second = chunks.get(1).payload();
            assertTrue(first.contains(SyncKeys.CHAPTERS, Tag.TAG_LIST));
            assertTrue(first.contains(SyncKeys.CHAPTER_PROPS, Tag.TAG_COMPOUND));
            assertEquals(128, first.getCompound(SyncKeys.CHANGED).getAllKeys().size());
            assertTrue(first.getCompound(SyncKeys.REMOVED).getBoolean("quest_removed"));
            assertFalse(second.contains(SyncKeys.CHAPTERS));
            assertFalse(second.contains(SyncKeys.CHAPTER_PROPS));
            assertEquals(1, second.getCompound(SyncKeys.CHANGED).getAllKeys().size());
            assertTrue(second.getCompound(SyncKeys.REMOVED).isEmpty());
        } finally {
            store.shutdown();
        }
    }

    @Test
    void descriptionChunksSplitAndSkipMissingDefinitions() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        try {
            for (int i = 0; i < 65; i++) {
                store.upsert(quest("quest_" + threeDigits(i), "main", defaultSettings(), List.of("Line " + i)));
            }
            DescriptionSyncer syncer = new DescriptionSyncer(store);

            Set<String> questIds = new java.util.HashSet<>(store.questIds());
            questIds.add("quest_missing");
            List<DescriptionSyncer.DescriptionChunk> chunks = syncer.descriptionChunks(questIds);

            assertEquals(2, chunks.size());
            assertEquals(0, chunks.get(0).chunkIndex());
            assertEquals(2, chunks.get(0).chunkCount());
            assertEquals(64, chunks.get(0).payload().getCompound(SyncKeys.DESCRIPTIONS).getAllKeys().size());
            assertEquals(1, chunks.get(1).payload().getCompound(SyncKeys.DESCRIPTIONS).getAllKeys().size());
            assertEquals(
                    "Line 0",
                    chunks.get(0).payload()
                            .getCompound(SyncKeys.DESCRIPTIONS)
                            .getList("quest_000", Tag.TAG_STRING)
                            .getString(0)
            );
            assertEquals(
                    "Line 64",
                    chunks.get(1).payload()
                            .getCompound(SyncKeys.DESCRIPTIONS)
                            .getList("quest_064", Tag.TAG_STRING)
                            .getString(0)
            );
            assertFalse(chunks.get(1).payload().getCompound(SyncKeys.DESCRIPTIONS).contains("quest_missing"));
        } finally {
            store.shutdown();
        }
    }

    @Test
    void displayCachePayloadUsesSharedKeysAndSkipsNullEntries() {
        Map<String, String> advancements = new LinkedHashMap<>();
        advancements.put("minecraft:story/root", "Minecraft");
        advancements.put("minecraft:story/null", null);
        Map<String, String> lootTables = new LinkedHashMap<>();
        lootTables.put("minecraft:chests/simple_dungeon", "simple_dungeon");
        Map<String, String> biomes = new LinkedHashMap<>();
        biomes.put("minecraft:plains", "plains");
        biomes.put(null, "bad");

        CompoundTag payload = DisplayCacheSyncer.payload(advancements, lootTables, biomes);

        assertEquals("Minecraft", payload.getCompound(SyncKeys.DisplayCache.ADVANCEMENTS).getString("minecraft:story/root"));
        assertFalse(payload.getCompound(SyncKeys.DisplayCache.ADVANCEMENTS).contains("minecraft:story/null"));
        assertEquals("simple_dungeon", payload.getCompound(SyncKeys.DisplayCache.LOOT_TABLES).getString("minecraft:chests/simple_dungeon"));
        assertEquals("plains", payload.getCompound(SyncKeys.DisplayCache.BIOMES).getString("minecraft:plains"));
        assertFalse(payload.getCompound(SyncKeys.DisplayCache.BIOMES).contains("bad"));
    }

    @Test
    void visibilitySelectorSeparatesVisibleLockedPreviewAndRemovedDelta() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        try {
            store.upsert(quest("quest/visible", "main"));
            store.upsert(quest("quest/locked", "main", settings(QuestVisibilityMode.LOCKED), List.of()));
            store.upsert(quest("quest/hidden", "main", settings(QuestVisibilityMode.IN_PROGRESS), List.of()));
            VisibilitySelector selector = new VisibilitySelector(store);
            selector.setVisibilityFilter((state, definition) -> "quest/visible".equals(definition.id()));
            PlayerQuestState playerState = new PlayerQuestState();

            assertEquals(Set.of("quest/visible"), selector.visibleQuestIds(playerState, false));
            assertEquals(Set.of("quest/visible", "quest/locked"), selector.syncedQuestIds(playerState, false));
            assertEquals(Set.of("quest/visible", "quest/locked", "quest/hidden"), selector.visibleQuestIds(playerState, true));

            VisibilitySelector.DeltaVisibility delta = selector.deltaVisibility(
                    playerState,
                    false,
                    Set.of("quest/visible", "quest/locked", "quest/hidden", "quest/missing")
            );

            assertEquals(Set.of("quest/visible", "quest/locked"), delta.changedQuestIds());
            assertEquals(Set.of("quest/visible"), delta.descriptionQuestIds());
            assertEquals(Set.of("quest/hidden", "quest/missing"), delta.removedQuestIds());

            playerState.quest("quest/locked").setUnlocked(true);
            assertFalse(selector.shouldSyncLockedPreview(playerState, store.quest("quest/locked")));
        } finally {
            store.shutdown();
        }
    }

    private static QuestDefinition quest(String id, String group) {
        return quest(id, group, defaultSettings(), List.of());
    }

    private static QuestDefinition quest(String id, String group, QuestSettings settings, List<String> description) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(
                        id,
                        "",
                        description,
                        Map.of(group, ChapterDef.DEFAULT),
                        QuestDisplay.DEFAULT_ICON,
                        QuestDisplay.DEFAULT_ICON_BACKGROUND,
                        QuestDisplay.DEFAULT_COMPLETION_SOUND,
                        QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME,
                        QuestDisplay.DEFAULT_COMPLETION_HUD_BACKGROUND,
                        QuestDisplay.DEFAULT_VISUAL_HIDDEN,
                        QuestDisplay.DEFAULT_QUEST_BACKGROUND,
                        QuestDisplay.DEFAULT_QUEST_BACKGROUND_GRAYSCALE
                ),
                settings,
                Set.of(),
                Map.of(),
                Map.of()
        );
    }

    private static QuestSettings defaultSettings() {
        return QuestSettings.DEFAULT;
    }

    private static QuestSettings settings(QuestVisibilityMode mode) {
        return new QuestSettings(false, mode, false, false, false, true);
    }

    private static String threeDigits(int value) {
        return String.format(java.util.Locale.ROOT, "%03d", value);
    }

    private static boolean containsString(net.minecraft.nbt.ListTag tag, String value) {
        for (int i = 0; i < tag.size(); i++) {
            if (value.equals(tag.getString(i))) {
                return true;
            }
        }
        return false;
    }
}

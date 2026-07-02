package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SyncChunker {
    static final int MAX_QUESTS_PER_CHUNK = 128;

    private final SyncPayloadBuilder payloads;

    SyncChunker(SyncPayloadBuilder payloads) {
        this.payloads = payloads;
    }

    List<SyncChunk> fullChunks(PlayerQuestState playerState, Set<String> syncedQuestIds, boolean editorGraphVisible) {
        List<Set<String>> chunkIds = partitionQuestIds(syncedQuestIds);
        List<SyncChunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkIds.size(); i++) {
            CompoundTag payload = new CompoundTag();
            payload.putInt(SyncKeys.SCHEMA, QuestDefinition.CURRENT_SCHEMA);
            payload.put(SyncKeys.GROUPS, payloads.chaptersTag(syncedQuestIds, editorGraphVisible));
            payload.put(SyncKeys.CHAPTER_PROPS, payloads.chapterPropsTag(syncedQuestIds, editorGraphVisible));
            payload.put(SyncKeys.QUESTS, payloads.questPayload(playerState, chunkIds.get(i)));
            chunks.add(new SyncChunk(i, chunkIds.size(), payload));
        }
        return List.copyOf(chunks);
    }

    List<SyncChunk> deltaChunks(
            PlayerQuestState playerState,
            Set<String> changedQuestIds,
            Set<String> removedQuestIds,
            Set<String> changedChapters,
            boolean includeMetadata
    ) {
        List<Set<String>> chunkIds = partitionQuestIds(changedQuestIds);
        int chunkCount = Math.max(1, chunkIds.size());
        List<SyncChunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            CompoundTag payload = new CompoundTag();

            Set<String> changedIds = i < chunkIds.size() ? chunkIds.get(i) : Set.of();
            if (i == 0 && includeMetadata) {
                payload.put(SyncKeys.GROUPS, payloads.chaptersTag());
                payload.put(SyncKeys.CHAPTER_PROPS, payloads.chapterPropsTagForChapters(changedChapters));
            }
            payload.put(SyncKeys.CHANGED, payloads.questPayload(playerState, changedIds));

            CompoundTag removed = new CompoundTag();
            if (i == 0) {
                for (String removedId : safeSet(removedQuestIds)) {
                    removed.putBoolean(removedId, true);
                }
            }
            payload.put(SyncKeys.REMOVED, removed);
            chunks.add(new SyncChunk(i, chunkCount, payload));
        }
        return List.copyOf(chunks);
    }

    static List<Set<String>> partitionQuestIds(Set<String> questIds) {
        List<String> ids = new ArrayList<>(safeSet(questIds));
        ids.sort(String::compareTo);

        if (ids.isEmpty()) {
            return List.of(Set.of());
        }

        List<Set<String>> chunks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += MAX_QUESTS_PER_CHUNK) {
            int end = Math.min(ids.size(), i + MAX_QUESTS_PER_CHUNK);
            chunks.add(new HashSet<>(ids.subList(i, end)));
        }
        return List.copyOf(chunks);
    }

    private static Set<String> safeSet(Set<String> values) {
        return values == null ? Set.of() : values;
    }

    record SyncChunk(int chunkIndex, int chunkCount, CompoundTag payload) {
    }
}

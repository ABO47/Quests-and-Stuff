package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.sync.S2CDescriptionSyncPacket;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.LongSupplier;

final class DescriptionSyncer {
    static final int MAX_DESCRIPTIONS_PER_CHUNK = 64;

    private final QuestDefinitionStore definitionStore;

    DescriptionSyncer(QuestDefinitionStore definitionStore) {
        this.definitionStore = definitionStore;
    }

    void sync(ServerPlayer player, LongSupplier sequenceSupplier, Set<String> questIds) {
        List<DescriptionChunk> chunks = descriptionChunks(questIds);
        if (chunks.isEmpty()) {
            return;
        }

        long sequence = sequenceSupplier.getAsLong();
        for (DescriptionChunk chunk : chunks) {
            ModNetwork.sendToPlayer(new S2CDescriptionSyncPacket(sequence, chunk.chunkIndex(), chunk.chunkCount(), chunk.payload()), player);
        }
    }

    List<DescriptionChunk> descriptionChunks(Set<String> questIds) {
        if (questIds == null || questIds.isEmpty()) {
            return List.of();
        }

        List<String> ids = new ArrayList<>(questIds);
        ids.sort(String::compareTo);
        List<List<String>> chunkIds = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += MAX_DESCRIPTIONS_PER_CHUNK) {
            int end = Math.min(ids.size(), i + MAX_DESCRIPTIONS_PER_CHUNK);
            chunkIds.add(ids.subList(i, end));
        }

        List<DescriptionChunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkIds.size(); i++) {
            CompoundTag payload = new CompoundTag();
            CompoundTag descriptions = new CompoundTag();
            for (String questId : chunkIds.get(i)) {
                QuestDefinition definition = definitionStore.quest(questId);
                if (definition == null) {
                    continue;
                }
                descriptions.put(questId, descriptionTag(definition));
            }
            payload.put(SyncKeys.DESCRIPTIONS, descriptions);
            chunks.add(new DescriptionChunk(i, chunkIds.size(), payload));
        }
        return List.copyOf(chunks);
    }

    private static ListTag descriptionTag(QuestDefinition definition) {
        ListTag lines = new ListTag();
        for (String line : definition.display().description()) {
            lines.add(StringTag.valueOf(line));
        }
        return lines;
    }

    record DescriptionChunk(int chunkIndex, int chunkCount, CompoundTag payload) {
    }
}

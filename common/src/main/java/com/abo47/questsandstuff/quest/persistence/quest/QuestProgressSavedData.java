package com.abo47.questsandstuff.quest.persistence.quest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;

public final class QuestProgressSavedData extends SavedData {
    private final Map<UUID, PlayerQuestState> states = new HashMap<>();

    public PlayerQuestState state(UUID playerId) {
        return states.computeIfAbsent(playerId, ignored -> new PlayerQuestState());
    }

    public Map<UUID, PlayerQuestState> states() {
        return states;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        for (Map.Entry<UUID, PlayerQuestState> entry : states.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue().save());
        }
        return tag;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public static QuestProgressSavedData load(CompoundTag tag) {
        QuestProgressSavedData data = new QuestProgressSavedData();
        for (String key : tag.getAllKeys()) {
            try {
                data.states.put(UUID.fromString(key), PlayerQuestState.load(tag.getCompound(key)));
            } catch (Exception ignored) {
                
            }
        }
        return data;
    }

    public static QuestProgressSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            throw new IllegalStateException("Cannot initialize quest progress before overworld is available");
        }
        return overworld.getDataStorage().computeIfAbsent(
                QuestProgressSavedData::load,
                QuestProgressSavedData::new,
                "questsandstuff_progress"
        );
    }
}

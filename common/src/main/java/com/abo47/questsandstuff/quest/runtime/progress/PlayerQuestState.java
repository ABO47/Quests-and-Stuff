package com.abo47.questsandstuff.quest.runtime.progress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class PlayerQuestState {
    private final Map<String, QuestProgressState> quests = new HashMap<>();
    private final Set<String> pinnedQuests = new HashSet<>();

    public Map<String, QuestProgressState> quests() {
        return quests;
    }

    public Set<String> pinnedQuests() {
        return pinnedQuests;
    }

    public QuestProgressState quest(String questId) {
        return quests.computeIfAbsent(questId, ignored -> new QuestProgressState());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        CompoundTag questsTag = new CompoundTag();
        for (Map.Entry<String, QuestProgressState> entry : quests.entrySet()) {
            questsTag.put(entry.getKey(), entry.getValue().save());
        }
        tag.put("quests", questsTag);

        ListTag pinnedTag = new ListTag();
        for (String questId : pinnedQuests) {
            pinnedTag.add(StringTag.valueOf(questId));
        }
        tag.put("pinned", pinnedTag);
        return tag;
    }

    public static PlayerQuestState load(CompoundTag tag) {
        PlayerQuestState state = new PlayerQuestState();

        CompoundTag questsTag = tag.getCompound("quests");
        for (String questId : questsTag.getAllKeys()) {
            state.quests.put(questId, QuestProgressState.load(questsTag.getCompound(questId)));
        }

        ListTag pinnedTag = tag.getList("pinned", Tag.TAG_STRING);
        for (int i = 0; i < pinnedTag.size(); i++) {
            state.pinnedQuests.add(pinnedTag.getString(i));
        }

        return state;
    }
}

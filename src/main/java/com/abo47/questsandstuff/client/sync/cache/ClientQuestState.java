package com.abo47.questsandstuff.client.sync.cache;

import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class ClientQuestState {
    public static final Map<String, CompoundTag> QUESTS = new HashMap<>();
    public static final Set<String> PINNED = new TreeSet<>();

    private ClientQuestState() {
    }

    public static void reset() {
        QUESTS.clear();
        PINNED.clear();
    }

    public static Map<String, CompoundTag> questSnapshot() {
        Map<String, CompoundTag> copy = new HashMap<>();
        for (Map.Entry<String, CompoundTag> entry : QUESTS.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return Collections.unmodifiableMap(copy);
    }

    public static Set<String> pinnedSnapshot() {
        return Collections.unmodifiableSet(PINNED);
    }

    public static int completedCount() {
        int count = 0;
        for (CompoundTag quest : QUESTS.values()) {
            if (quest.getBoolean("completed")) {
                count++;
            }
        }
        return count;
    }

    public static int totalCount() {
        return QUESTS.size();
    }
}

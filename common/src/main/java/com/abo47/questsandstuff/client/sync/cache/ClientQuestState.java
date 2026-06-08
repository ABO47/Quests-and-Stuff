package com.abo47.questsandstuff.client.sync.cache;

import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ClientQuestState {
    private static final Map<String, CompoundTag> QUESTS = new HashMap<>();
    private static final Set<String> PINNED = new TreeSet<>();

    private ClientQuestState() {
    }

    public static void reset() {
        QUESTS.clear();
        PINNED.clear();
    }

    public static void clearQuests() {
        QUESTS.clear();
    }

    public static void putQuest(String questId, CompoundTag quest) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank() || quest == null) {
            return;
        }
        QUESTS.put(normalized, quest.copy());
    }

    public static CompoundTag questCopy(String questId) {
        CompoundTag quest = QUESTS.get(normalizeQuestId(questId));
        return quest == null ? new CompoundTag() : quest.copy();
    }

    public static CompoundTag questSectionCopy(String questId, String section) {
        CompoundTag quest = QUESTS.get(normalizeQuestId(questId));
        return quest == null || section == null ? new CompoundTag() : quest.getCompound(section).copy();
    }

    public static CompoundTag mutableQuest(String questId) {
        return QUESTS.get(normalizeQuestId(questId));
    }

    public static CompoundTag mutableQuestOrCreate(String questId) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank()) {
            return new CompoundTag();
        }
        return QUESTS.computeIfAbsent(normalized, ignored -> new CompoundTag());
    }

    public static boolean containsQuest(String questId) {
        return QUESTS.containsKey(normalizeQuestId(questId));
    }

    public static List<Map.Entry<String, CompoundTag>> questEntries() {
        List<Map.Entry<String, CompoundTag>> entries = new ArrayList<>(QUESTS.size());
        for (Map.Entry<String, CompoundTag> entry : QUESTS.entrySet()) {
            entries.add(Map.entry(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(entries);
    }

    public static Set<String> questIdsSnapshot() {
        return Collections.unmodifiableSet(new TreeSet<>(QUESTS.keySet()));
    }

    public static boolean removeQuest(String questId) {
        return QUESTS.remove(normalizeQuestId(questId)) != null;
    }

    public static void forEachQuest(Consumer<CompoundTag> consumer) {
        if (consumer == null) {
            return;
        }
        QUESTS.values().forEach(consumer);
    }

    public static void forEachQuestEntry(BiConsumer<String, CompoundTag> consumer) {
        if (consumer == null) {
            return;
        }
        QUESTS.forEach(consumer);
    }

    public static Map<String, CompoundTag> questSnapshot() {
        Map<String, CompoundTag> copy = new HashMap<>();
        for (Map.Entry<String, CompoundTag> entry : QUESTS.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return Collections.unmodifiableMap(copy);
    }

    public static Set<String> pinnedSnapshot() {
        return Collections.unmodifiableSet(new TreeSet<>(PINNED));
    }

    public static void setPinned(Iterable<String> questIds) {
        PINNED.clear();
        if (questIds == null) {
            return;
        }
        for (String questId : questIds) {
            String normalized = normalizeQuestId(questId);
            if (!normalized.isBlank()) {
                PINNED.add(normalized);
            }
        }
    }

    public static void removePinned(String questId) {
        PINNED.remove(normalizeQuestId(questId));
    }

    public static void togglePinned(String questId) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank()) {
            return;
        }
        if (!PINNED.add(normalized)) {
            PINNED.remove(normalized);
        }
    }

    public static int completedCount() {
        int count = 0;
        for (CompoundTag quest : QUESTS.values()) {
            if (quest.getBoolean(QuestSyncKeys.Quest.COMPLETED)) {
                count++;
            }
        }
        return count;
    }

    public static int totalCount() {
        return QUESTS.size();
    }

    private static String normalizeQuestId(String questId) {
        return questId == null ? "" : questId.trim();
    }
}

package com.abo47.questsandstuff.client.sync.cache;

import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientDisplayState {
    private static final Map<String, String> ADVANCEMENT_DISPLAYS = new HashMap<>();
    private static final Map<String, String> LOOT_TABLE_DISPLAYS = new HashMap<>();
    private static final Map<String, String> BIOME_DISPLAYS = new HashMap<>();
    private static final LinkedList<String> RECENT_EVENTS = new LinkedList<>();
    private static final Set<String> CHAPTER_COMPLETION_NOTICES = new HashSet<>();

    private ClientDisplayState() {
    }

    public static void reset() {
        ADVANCEMENT_DISPLAYS.clear();
        LOOT_TABLE_DISPLAYS.clear();
        BIOME_DISPLAYS.clear();
        RECENT_EVENTS.clear();
        CHAPTER_COMPLETION_NOTICES.clear();
    }

    public static void applyDisplayCacheSync(CompoundTag payload) {
        ADVANCEMENT_DISPLAYS.clear();
        LOOT_TABLE_DISPLAYS.clear();

        CompoundTag advancements = payload.getCompound(QuestSyncKeys.DisplayCache.ADVANCEMENTS);
        for (String key : advancements.getAllKeys()) {
            ADVANCEMENT_DISPLAYS.put(key, advancements.getString(key));
        }
        CompoundTag lootTables = payload.getCompound(QuestSyncKeys.DisplayCache.LOOT_TABLES);
        for (String key : lootTables.getAllKeys()) {
            LOOT_TABLE_DISPLAYS.put(key, lootTables.getString(key));
        }
        CompoundTag biomes = payload.getCompound(QuestSyncKeys.DisplayCache.BIOMES);
        for (String key : biomes.getAllKeys()) {
            BIOME_DISPLAYS.put(key, biomes.getString(key));
        }
    }

    public static void applyQuestEvent(String eventType, String questId, String rewardId) {
        String normalizedType = eventType == null ? "unknown" : eventType;
        String normalizedQuest = questId == null ? "" : questId;
        String normalizedReward = rewardId == null ? "" : rewardId;
        String message = normalizedType + ":" + normalizedQuest + ":" + normalizedReward;
        RECENT_EVENTS.addFirst(message);
        while (RECENT_EVENTS.size() > 12) {
            RECENT_EVENTS.removeLast();
        }
    }

    public static Map<String, String> advancementDisplays() {
        return Map.copyOf(ADVANCEMENT_DISPLAYS);
    }

    public static Map<String, String> lootTableDisplays() {
        return Map.copyOf(LOOT_TABLE_DISPLAYS);
    }

    public static Map<String, String> biomeDisplays() {
        return Map.copyOf(BIOME_DISPLAYS);
    }

    public static List<String> recentEvents() {
        return List.copyOf(RECENT_EVENTS);
    }

    public static void noteQuestCompleted(CompoundTag quest, String currentGroup) {
        if (quest == null || quest.isEmpty()) {
            return;
        }
        String selected = currentGroup == null ? "" : currentGroup;
        CompoundTag groups = quest.getCompound(QuestSyncKeys.Quest.GROUPS);
        for (String group : groups.getAllKeys()) {
            if (group == null || group.isBlank() || group.equals(selected)) {
                continue;
            }
            CHAPTER_COMPLETION_NOTICES.add(group);
        }
    }

    public static boolean chapterHasCompletionNotice(String group) {
        return group != null && CHAPTER_COMPLETION_NOTICES.contains(group);
    }

    public static void clearChapterCompletionNotice(String group) {
        if (group == null || group.isBlank()) {
            return;
        }
        CHAPTER_COMPLETION_NOTICES.remove(group);
    }
}

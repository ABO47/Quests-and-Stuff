package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ClientQuestCopyMutations {
    private ClientQuestCopyMutations() {
    }

    public static void copyQuestLocal(String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        String sourceId = sourceQuestId == null ? "" : sourceQuestId.trim();
        String targetId = newQuestId == null ? "" : newQuestId.trim();
        String normalizedGroup = normalizeGroup(group);
        if (sourceId.isBlank() || targetId.isBlank() || normalizedGroup.isBlank()) {
            return;
        }
        CompoundTag source = ClientQuestState.QUESTS.get(sourceId);
        if (source == null) {
            return;
        }

        CompoundTag quest = source.copy();
        quest.putBoolean("completed", false);
        quest.putBoolean("unlocked", false);
        quest.putBoolean("claimed", false);
        quest.putFloat("progress", 0.0f);

        ListTag prerequisites = quest.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        ListTag remappedPrerequisites = new ListTag();
        if (copiedIds != null && !copiedIds.isEmpty()) {
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisite = prerequisites.getString(i);
                String mapped = copiedIds.get(prerequisite);
                if (mapped != null && !mapped.isBlank()) {
                    remappedPrerequisites.add(net.minecraft.nbt.StringTag.valueOf(mapped));
                }
            }
        }
        quest.put(QuestDefinition.PREREQUISITES_FIELD, remappedPrerequisites);
        quest.put("connection_colors", remappedConnectionColors(quest.getCompound("connection_colors"), copiedIds, remappedPrerequisites));
        quest.put("connection_modes", remappedConnectionModes(quest.getCompound("connection_modes"), copiedIds, remappedPrerequisites));
        quest.put("hidden_connections", remappedHiddenConnections(quest.getList("hidden_connections", Tag.TAG_STRING), copiedIds, remappedPrerequisites));

        CompoundTag groups = new CompoundTag();
        CompoundTag groupTag = new CompoundTag();
        groupTag.putBoolean("visible", true);
        groupTag.putInt("x", x);
        groupTag.putInt("y", y);
        float normalizedScale = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat("scale", Math.max(0.5f, normalizedScale));
        groups.put(normalizedGroup, groupTag);
        quest.put("groups", groups);

        ClientQuestState.QUESTS.put(targetId, quest);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        String sourceId = sourceQuestId == null ? "" : sourceQuestId.trim();
        String targetId = newQuestId == null ? "" : newQuestId.trim();
        String normalizedGroup = normalizeGroup(group);
        if (targetId.isBlank() || normalizedGroup.isBlank()) {
            return;
        }
        CompoundTag quest = sourceSnapshot == null ? new CompoundTag() : sourceSnapshot.copy();
        quest.putBoolean("completed", false);
        quest.putBoolean("unlocked", false);
        quest.putBoolean("claimed", false);
        quest.putFloat("progress", 0.0f);

        ListTag prerequisites = quest.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        ListTag remappedPrerequisites = new ListTag();
        if (copiedIds != null && !copiedIds.isEmpty()) {
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisite = prerequisites.getString(i);
                String mapped = copiedIds.get(prerequisite);
                if (mapped != null && !mapped.isBlank() && !mapped.equals(targetId)) {
                    remappedPrerequisites.add(net.minecraft.nbt.StringTag.valueOf(mapped));
                }
            }
        }
        quest.put(QuestDefinition.PREREQUISITES_FIELD, remappedPrerequisites);
        quest.put("connection_colors", remappedConnectionColors(quest.getCompound("connection_colors"), copiedIds, remappedPrerequisites));
        quest.put("connection_modes", remappedConnectionModes(quest.getCompound("connection_modes"), copiedIds, remappedPrerequisites));
        quest.put("hidden_connections", remappedHiddenConnections(quest.getList("hidden_connections", Tag.TAG_STRING), copiedIds, remappedPrerequisites));

        CompoundTag groups = new CompoundTag();
        CompoundTag groupTag = new CompoundTag();
        groupTag.putBoolean("visible", true);
        groupTag.putInt("x", x);
        groupTag.putInt("y", y);
        float normalizedScale = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat("scale", Math.max(0.5f, normalizedScale));
        groups.put(normalizedGroup, groupTag);
        quest.put("groups", groups);

        ClientQuestState.QUESTS.put(targetId, quest);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        if (copiedIds == null || copiedIds.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : copiedIds.entrySet()) {
            String sourceId = entry.getKey() == null ? "" : entry.getKey().trim();
            String targetId = entry.getValue() == null ? "" : entry.getValue().trim();
            if (sourceId.isBlank() || targetId.isBlank()) {
                continue;
            }
            CompoundTag target = ClientQuestState.QUESTS.get(targetId);
            if (target == null) {
                continue;
            }
            CompoundTag source = snapshots == null ? null : snapshots.get(sourceId);
            if (source == null) {
                source = ClientQuestState.QUESTS.get(sourceId);
            }
            if (source == null) {
                continue;
            }
            target.put(QuestDefinition.PREREQUISITES_FIELD, remappedPrerequisiteList(source.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING), copiedIds, targetId));
            ListTag remappedPrerequisites = target.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            target.put("connection_colors", remappedConnectionColors(source.getCompound("connection_colors"), copiedIds, remappedPrerequisites));
            target.put("connection_modes", remappedConnectionModes(source.getCompound("connection_modes"), copiedIds, remappedPrerequisites));
            target.put("hidden_connections", remappedHiddenConnections(source.getList("hidden_connections", Tag.TAG_STRING), copiedIds, remappedPrerequisites));
        }
    }

    private static ListTag remappedPrerequisiteList(ListTag prerequisites, Map<String, String> copiedIds, String targetId) {
        ListTag remapped = new ListTag();
        if (prerequisites == null || prerequisites.isEmpty() || copiedIds == null || copiedIds.isEmpty()) {
            return remapped;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (int i = 0; i < prerequisites.size(); i++) {
            String prerequisite = prerequisites.getString(i);
            String mapped = copiedIds.get(prerequisite);
            if (mapped != null && !mapped.isBlank() && !mapped.equals(targetId) && seen.add(mapped)) {
                remapped.add(net.minecraft.nbt.StringTag.valueOf(mapped));
            }
        }
        return remapped;
    }

    private static CompoundTag remappedConnectionColors(CompoundTag colors, Map<String, String> copiedIds, ListTag remappedPrerequisites) {
        CompoundTag remapped = new CompoundTag();
        if (colors == null || colors.isEmpty() || copiedIds == null || copiedIds.isEmpty()) {
            return remapped;
        }
        Set<String> prerequisites = stringSet(remappedPrerequisites);
        for (String key : colors.getAllKeys()) {
            String mapped = copiedIds.get(key);
            if (mapped != null && prerequisites.contains(mapped) && colors.contains(key, Tag.TAG_INT)) {
                remapped.putInt(mapped, colors.getInt(key));
            }
        }
        return remapped;
    }

    private static CompoundTag remappedConnectionModes(CompoundTag modes, Map<String, String> copiedIds, ListTag remappedPrerequisites) {
        CompoundTag remapped = new CompoundTag();
        if (modes == null || modes.isEmpty() || copiedIds == null || copiedIds.isEmpty()) {
            return remapped;
        }
        Set<String> prerequisites = stringSet(remappedPrerequisites);
        for (String key : modes.getAllKeys()) {
            String mapped = copiedIds.get(key);
            if (mapped != null && prerequisites.contains(mapped) && "grid".equals(modes.getString(key))) {
                remapped.putString(mapped, "grid");
            }
        }
        return remapped;
    }

    private static ListTag remappedHiddenConnections(ListTag hiddenConnections, Map<String, String> copiedIds, ListTag remappedPrerequisites) {
        ListTag remapped = new ListTag();
        if (hiddenConnections == null || hiddenConnections.isEmpty() || copiedIds == null || copiedIds.isEmpty()) {
            return remapped;
        }
        Set<String> prerequisites = stringSet(remappedPrerequisites);
        Set<String> seen = new LinkedHashSet<>();
        for (int i = 0; i < hiddenConnections.size(); i++) {
            String mapped = copiedIds.get(hiddenConnections.getString(i));
            if (mapped != null && prerequisites.contains(mapped) && seen.add(mapped)) {
                remapped.add(net.minecraft.nbt.StringTag.valueOf(mapped));
            }
        }
        return remapped;
    }

    private static Set<String> stringSet(ListTag list) {
        Set<String> out = new LinkedHashSet<>();
        if (list == null) {
            return out;
        }
        for (int i = 0; i < list.size(); i++) {
            String value = list.getString(i);
            if (value != null && !value.isBlank()) {
                out.add(value);
            }
        }
        return out;
    }

    private static String normalizeGroup(String value) {
        return value == null ? "" : value.trim();
    }
}

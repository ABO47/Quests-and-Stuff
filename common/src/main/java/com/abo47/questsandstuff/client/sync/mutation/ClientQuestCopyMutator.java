package com.abo47.questsandstuff.client.sync.mutation;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

public final class ClientQuestCopyMutator {
    private ClientQuestCopyMutator() {
    }

    public static void copyQuestLocal(String sourceQuestId, String newQuestId, String chapter, int x, int y, float scale, Map<String, String> copiedIds) {
        String sourceId = QuestIdentity.questId(sourceQuestId);
        String targetId = QuestIdentity.questId(newQuestId);
        String normalizedGroup = normalizeChapter(chapter);
        if (sourceId.isBlank() || targetId.isBlank() || normalizedGroup.isBlank()) {
            return;
        }
        CompoundTag source = ClientQuestState.mutableQuest(sourceId);
        if (source == null) {
            return;
        }

        CompoundTag quest = source.copy();

        ListTag prerequisites = quest.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
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
        quest.put(SyncKeys.Quest.PREREQUISITES, remappedPrerequisites);
        quest.put(SyncKeys.Quest.CONNECTION_COLORS, remappedConnectionColors(quest.getCompound(SyncKeys.Quest.CONNECTION_COLORS), copiedIds, remappedPrerequisites));
        quest.put(SyncKeys.Quest.CONNECTION_MODES, remappedConnectionModes(quest.getCompound(SyncKeys.Quest.CONNECTION_MODES), copiedIds, remappedPrerequisites));
        quest.put(SyncKeys.Quest.HIDDEN_CONNECTIONS, remappedHiddenConnections(quest.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING), copiedIds, remappedPrerequisites));
        ClientQuestSnapshotBuilder.prepareCopiedQuest(quest, normalizedGroup, x, y, scale);

        ClientQuestState.putQuest(targetId, quest);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String chapter, int x, int y, float scale, Map<String, String> copiedIds) {
        String targetId = QuestIdentity.questId(newQuestId);
        String normalizedGroup = normalizeChapter(chapter);
        if (targetId.isBlank() || normalizedGroup.isBlank()) {
            return;
        }
        CompoundTag quest = sourceSnapshot == null ? new CompoundTag() : sourceSnapshot.copy();

        ListTag prerequisites = quest.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
        ListTag remappedPrerequisites = new ListTag();
        if (copiedIds != null && !copiedIds.isEmpty()) {
            for (int i = 0; i < prerequisites.size(); i++) {
            String prerequisite = prerequisites.getString(i);
                String mapped = mappedCopiedId(copiedIds, prerequisite);
            if (!mapped.isBlank() && !mapped.equals(targetId)) {
                remappedPrerequisites.add(net.minecraft.nbt.StringTag.valueOf(mapped));
            }
            }
        }
        quest.put(SyncKeys.Quest.PREREQUISITES, remappedPrerequisites);
        quest.put(SyncKeys.Quest.CONNECTION_COLORS, remappedConnectionColors(quest.getCompound(SyncKeys.Quest.CONNECTION_COLORS), copiedIds, remappedPrerequisites));
        quest.put(SyncKeys.Quest.CONNECTION_MODES, remappedConnectionModes(quest.getCompound(SyncKeys.Quest.CONNECTION_MODES), copiedIds, remappedPrerequisites));
        quest.put(SyncKeys.Quest.HIDDEN_CONNECTIONS, remappedHiddenConnections(quest.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING), copiedIds, remappedPrerequisites));
        ClientQuestSnapshotBuilder.prepareCopiedQuest(quest, normalizedGroup, x, y, scale);

        ClientQuestState.putQuest(targetId, quest);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        if (copiedIds == null || copiedIds.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : copiedIds.entrySet()) {
            String sourceId = QuestIdentity.questId(entry.getKey());
            String targetId = QuestIdentity.questId(entry.getValue());
            if (sourceId.isBlank() || targetId.isBlank()) {
                continue;
            }
            CompoundTag target = ClientQuestState.mutableQuest(targetId);
            if (target == null) {
                continue;
            }
            CompoundTag source = snapshots == null ? null : snapshots.get(sourceId);
            if (source == null) {
                source = ClientQuestState.mutableQuest(sourceId);
            }
            if (source == null) {
                continue;
            }
            target.put(SyncKeys.Quest.PREREQUISITES, remappedPrerequisiteList(source.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING), copiedIds, targetId));
            ListTag remappedPrerequisites = target.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
            target.put(SyncKeys.Quest.CONNECTION_COLORS, remappedConnectionColors(source.getCompound(SyncKeys.Quest.CONNECTION_COLORS), copiedIds, remappedPrerequisites));
            target.put(SyncKeys.Quest.CONNECTION_MODES, remappedConnectionModes(source.getCompound(SyncKeys.Quest.CONNECTION_MODES), copiedIds, remappedPrerequisites));
            target.put(SyncKeys.Quest.HIDDEN_CONNECTIONS, remappedHiddenConnections(source.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING), copiedIds, remappedPrerequisites));
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
            String mapped = mappedCopiedId(copiedIds, prerequisite);
            if (!mapped.isBlank() && !mapped.equals(targetId) && seen.add(mapped)) {
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
            String mapped = mappedCopiedId(copiedIds, key);
            if (!mapped.isBlank() && prerequisites.contains(mapped) && colors.contains(key, Tag.TAG_INT)) {
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
            String mapped = mappedCopiedId(copiedIds, key);
            QuestConnectionMode mode = QuestConnectionMode.fromSerializedName(modes.getString(key));
            if (!mapped.isBlank() && prerequisites.contains(mapped) && mode.storedInQuestMetadata()) {
                remapped.putString(mapped, mode.serializedName());
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
            String mapped = mappedCopiedId(copiedIds, hiddenConnections.getString(i));
            if (!mapped.isBlank() && prerequisites.contains(mapped) && seen.add(mapped)) {
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
                out.add(QuestConnectionMetadata.metadataKey(value));
            }
        }
        return out;
    }

    private static String mappedCopiedId(Map<String, String> copiedIds, String key) {
        if (copiedIds == null || copiedIds.isEmpty()) {
            return "";
        }
        String normalizedKey = QuestConnectionMetadata.metadataKey(key);
        String mapped = copiedIds.get(normalizedKey);
        if (mapped == null) {
            mapped = copiedIds.get(key);
        }
        return QuestConnectionMetadata.metadataKey(mapped);
    }

    private static String normalizeChapter(String value) {
        return QuestIdentity.chapterName(value);
    }
}

package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

final class ClientQuestConnectionMutations {
    private ClientQuestConnectionMutations() {
    }

    static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        String normalizedQuest = questId == null ? "" : questId.trim();
        String normalizedPrerequisite = prerequisiteId == null ? "" : prerequisiteId.trim();
        if (normalizedQuest.isBlank() || normalizedPrerequisite.isBlank() || normalizedQuest.equals(normalizedPrerequisite)) {
            return;
        }
        CompoundTag quest = ClientQuestState.QUESTS.get(normalizedQuest);
        if (quest == null || !ClientQuestState.QUESTS.containsKey(normalizedPrerequisite)) {
            return;
        }
        ListTag prerequisites = quest.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        ListTag next = new ListTag();
        boolean found = false;
        for (int i = 0; i < prerequisites.size(); i++) {
            String prerequisite = prerequisites.getString(i);
            if (normalizedPrerequisite.equals(prerequisite)) {
                found = true;
                if (!add) {
                    continue;
                }
            }
            next.add(prerequisites.get(i).copy());
        }
        if (add && !found) {
            next.add(StringTag.valueOf(normalizedPrerequisite));
        }
        quest.put(QuestDefinition.PREREQUISITES_FIELD, next);
        if (!add) {
            removeConnectionMetadata(quest, normalizedPrerequisite);
        }
        refreshLocalUnlockState(quest);
    }

    static void setConnectionColorLocal(String questId, String prerequisiteId, int color) {
        ConnectionTarget target = targetForConnection(questId, prerequisiteId);
        if (target == null) {
            return;
        }
        CompoundTag colors = target.quest().getCompound("connection_colors").copy();
        colors.putInt(target.prerequisiteId(), color);
        target.quest().put("connection_colors", colors);
    }

    static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ConnectionTarget target = targetForConnection(questId, prerequisiteId);
        if (target == null) {
            return;
        }
        CompoundTag modes = target.quest().getCompound("connection_modes").copy();
        if (gridMode) {
            modes.putString(target.prerequisiteId(), "grid");
        } else {
            modes.remove(target.prerequisiteId());
        }
        target.quest().put("connection_modes", modes);
    }

    static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ConnectionTarget target = targetForConnection(questId, prerequisiteId);
        if (target == null) {
            return;
        }
        ListTag next = removeString(target.quest().getList("hidden_connections", Tag.TAG_STRING), target.prerequisiteId());
        if (hidden) {
            next.add(StringTag.valueOf(target.prerequisiteId()));
        }
        target.quest().put("hidden_connections", next);
    }

    static void removeQuestReferences(String questId) {
        for (CompoundTag quest : ClientQuestState.QUESTS.values()) {
            ListTag prerequisites = quest.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            if (prerequisites.isEmpty()) {
                continue;
            }
            ListTag filtered = new ListTag();
            boolean changed = false;
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisite = prerequisites.getString(i);
                if (questId.equals(prerequisite)) {
                    changed = true;
                    continue;
                }
                filtered.add(prerequisites.get(i).copy());
            }
            if (changed) {
                quest.put(QuestDefinition.PREREQUISITES_FIELD, filtered);
                removeConnectionMetadata(quest, questId);
            }
        }
    }

    static void refreshLocalUnlockState(CompoundTag quest) {
        if (quest.getBoolean("completed")) {
            quest.putBoolean("unlocked", true);
            return;
        }

        ListTag prerequisites = quest.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        if (prerequisites.isEmpty()) {
            quest.putBoolean("unlocked", true);
            return;
        }

        for (int i = 0; i < prerequisites.size(); i++) {
            CompoundTag prerequisite = ClientQuestState.QUESTS.get(prerequisites.getString(i));
            if (prerequisite == null || !prerequisite.getBoolean("completed")) {
                quest.putBoolean("unlocked", false);
                return;
            }
        }
        quest.putBoolean("unlocked", true);
    }

    private static ConnectionTarget targetForConnection(String questId, String prerequisiteId) {
        String normalizedQuest = questId == null ? "" : questId.trim();
        String normalizedPrerequisite = prerequisiteId == null ? "" : prerequisiteId.trim();
        if (normalizedQuest.isBlank() || normalizedPrerequisite.isBlank()) {
            return null;
        }
        CompoundTag quest = ClientQuestState.QUESTS.get(normalizedQuest);
        return quest == null ? null : new ConnectionTarget(quest, normalizedPrerequisite);
    }

    private static void removeConnectionMetadata(CompoundTag quest, String prerequisiteId) {
        CompoundTag colors = quest.getCompound("connection_colors").copy();
        colors.remove(prerequisiteId);
        quest.put("connection_colors", colors);
        CompoundTag modes = quest.getCompound("connection_modes").copy();
        modes.remove(prerequisiteId);
        quest.put("connection_modes", modes);
        quest.put("hidden_connections", removeString(quest.getList("hidden_connections", Tag.TAG_STRING), prerequisiteId));
    }

    private static ListTag removeString(ListTag current, String value) {
        ListTag next = new ListTag();
        for (int i = 0; i < current.size(); i++) {
            String entry = current.getString(i);
            if (!value.equals(entry)) {
                next.add(current.get(i).copy());
            }
        }
        return next;
    }

    private record ConnectionTarget(CompoundTag quest, String prerequisiteId) {
    }
}

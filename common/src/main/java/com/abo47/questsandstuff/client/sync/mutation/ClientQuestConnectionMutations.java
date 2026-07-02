package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import static com.abo47.questsandstuff.quest.sync.QuestSyncKeys.Quest.CONNECTION_TEXTURES;
import static com.abo47.questsandstuff.quest.sync.QuestSyncKeys.Quest.CONNECTION_TEXTURE_SPACINGS;

final class ClientQuestConnectionMutations {
    private ClientQuestConnectionMutations() {
    }

    static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        String normalizedQuest = QuestConnectionMetadata.normalizeQuestId(questId);
        String normalizedPrerequisite = QuestConnectionMetadata.metadataKey(prerequisiteId);
        if (normalizedQuest.isBlank() || normalizedPrerequisite.isBlank() || normalizedQuest.equals(normalizedPrerequisite)) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalizedQuest);
        if (quest == null || !ClientQuestState.containsQuest(normalizedPrerequisite)) {
            return;
        }
        ListTag prerequisites = quest.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
        ListTag next = new ListTag();
        boolean found = false;
        for (int i = 0; i < prerequisites.size(); i++) {
            String prerequisite = prerequisites.getString(i);
            if (normalizedPrerequisite.equals(QuestConnectionMetadata.metadataKey(prerequisite))) {
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
        quest.put(QuestSyncKeys.Quest.PREREQUISITES, next);
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
        CompoundTag colors = target.quest().getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS).copy();
        colors.putInt(target.metadataKey(), color);
        target.quest().put(QuestSyncKeys.Quest.CONNECTION_COLORS, colors);
    }

    static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ConnectionTarget target = targetForConnection(questId, prerequisiteId);
        if (target == null) {
            return;
        }
        CompoundTag modes = target.quest().getCompound(QuestSyncKeys.Quest.CONNECTION_MODES).copy();
        if (gridMode) {
            modes.putString(target.metadataKey(), QuestConnectionMode.GRID.serializedName());
        } else {
            modes.remove(target.metadataKey());
        }
        target.quest().put(QuestSyncKeys.Quest.CONNECTION_MODES, modes);
    }

    static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ConnectionTarget target = targetForConnection(questId, prerequisiteId);
        if (target == null) {
            return;
        }
        ListTag next = removeString(target.quest().getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING), target.metadataKey());
        if (hidden) {
            next.add(StringTag.valueOf(target.metadataKey()));
        }
        target.quest().put(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, next);
    }

    static void removeQuestReferences(String questId) {
        ClientQuestState.forEachQuest(quest -> {
            ListTag prerequisites = quest.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
            if (prerequisites.isEmpty()) {
                return;
            }
            ListTag filtered = new ListTag();
            boolean changed = false;
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisite = prerequisites.getString(i);
                if (QuestConnectionMetadata.metadataKey(questId).equals(QuestConnectionMetadata.metadataKey(prerequisite))) {
                    changed = true;
                    continue;
                }
                filtered.add(prerequisites.get(i).copy());
            }
            if (changed) {
                quest.put(QuestSyncKeys.Quest.PREREQUISITES, filtered);
                removeConnectionMetadata(quest, questId);
            }
        });
    }

    static void refreshLocalUnlockState(CompoundTag quest) {
        if (quest.getBoolean(QuestSyncKeys.Quest.COMPLETED)) {
            quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);
            return;
        }

        ListTag prerequisites = quest.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
        if (prerequisites.isEmpty()) {
            quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);
            return;
        }

        for (int i = 0; i < prerequisites.size(); i++) {
            CompoundTag prerequisite = ClientQuestState.mutableQuest(prerequisites.getString(i));
            if (prerequisite == null || !prerequisite.getBoolean(QuestSyncKeys.Quest.COMPLETED)) {
                quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, false);
                return;
            }
        }
        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);
    }

    private static ConnectionTarget targetForConnection(String questId, String prerequisiteId) {
        String normalizedQuest = QuestConnectionMetadata.normalizeQuestId(questId);
        String normalizedPrerequisite = QuestConnectionMetadata.normalizeQuestId(prerequisiteId);
        if (normalizedQuest.isBlank() || normalizedPrerequisite.isBlank()) {
            return null;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalizedQuest);
        return quest == null ? null : new ConnectionTarget(quest, normalizedPrerequisite);
    }

    public static void setConnectionTextureLocal(String questId, String prerequisiteId, String texture) {
        ConnectionTarget target = targetForConnection(questId, prerequisiteId);
        if (target == null) {
            return;
        }
        CompoundTag textures = target.quest().getCompound(CONNECTION_TEXTURES).copy();
        if (texture == null || texture.isBlank()) {
            textures.remove(target.metadataKey());
        } else {
            textures.putString(target.metadataKey(), texture);
        }
        target.quest().put(CONNECTION_TEXTURES, textures);
    }

    public static void setConnectionTextureSpacingLocal(String questId, String prerequisiteId, int spacing) {
        ConnectionTarget target = targetForConnection(questId, prerequisiteId);
        if (target == null) {
            return;
        }
        CompoundTag spacings = target.quest().getCompound(CONNECTION_TEXTURE_SPACINGS).copy();
        if (spacing <= 0) {
            spacings.remove(target.metadataKey());
        } else {
            spacings.putInt(target.metadataKey(), spacing);
        }
        target.quest().put(CONNECTION_TEXTURE_SPACINGS, spacings);
    }

    static void removeConnectionMetadata(CompoundTag quest, String prerequisiteId) {
        String key = QuestConnectionMetadata.metadataKey(prerequisiteId);
        CompoundTag colors = quest.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS).copy();
        colors.remove(key);
        quest.put(QuestSyncKeys.Quest.CONNECTION_COLORS, colors);
        CompoundTag modes = quest.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES).copy();
        modes.remove(key);
        quest.put(QuestSyncKeys.Quest.CONNECTION_MODES, modes);
        CompoundTag textures = quest.getCompound(CONNECTION_TEXTURES).copy();
        textures.remove(key);
        quest.put(CONNECTION_TEXTURES, textures);
        CompoundTag spacings = quest.getCompound(CONNECTION_TEXTURE_SPACINGS).copy();
        spacings.remove(key);
        quest.put(CONNECTION_TEXTURE_SPACINGS, spacings);
        quest.put(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, removeString(quest.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING), key));
    }

    private static ListTag removeString(ListTag current, String value) {
        ListTag next = new ListTag();
        for (int i = 0; i < current.size(); i++) {
            String entry = current.getString(i);
            if (!QuestConnectionMetadata.metadataKey(value).equals(QuestConnectionMetadata.metadataKey(entry))) {
                next.add(current.get(i).copy());
            }
        }
        return next;
    }

    private record ConnectionTarget(CompoundTag quest, String prerequisiteId) {
        String metadataKey() {
            return QuestConnectionMetadata.metadataKey(prerequisiteId);
        }
    }
}

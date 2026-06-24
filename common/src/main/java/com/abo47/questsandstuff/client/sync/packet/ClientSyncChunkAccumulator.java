package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public final class ClientSyncChunkAccumulator {
    private final int expected;
    private final Map<Integer, CompoundTag> parts = new HashMap<>();

    public ClientSyncChunkAccumulator(int expected) {
        this.expected = Math.max(1, expected);
    }

    public int expected() {
        return expected;
    }

    public void add(int index, CompoundTag payload) {
        if (index < 0 || index >= expected) {
            return;
        }
        parts.put(index, payload == null ? new CompoundTag() : payload.copy());
    }

    public boolean complete() {
        return parts.size() >= expected;
    }

    public CompoundTag joinFullPayload() {
        CompoundTag full = new CompoundTag();
        full.putInt(QuestSyncKeys.SCHEMA, QuestDefinition.CURRENT_SCHEMA);
        CompoundTag quests = new CompoundTag();
        ListTag groups = new ListTag();
        CompoundTag groupProps = new CompoundTag();
        for (int i = 0; i < expected; i++) {
            CompoundTag part = parts.get(i);
            if (part == null) {
                continue;
            }
            if (groups.isEmpty()) {
                groups = (ListTag) part.getList(QuestSyncKeys.GROUPS, Tag.TAG_STRING).copy();
            }
            if (groupProps.isEmpty() && part.contains(QuestSyncKeys.GROUP_PROPS, Tag.TAG_COMPOUND)) {
                groupProps = part.getCompound(QuestSyncKeys.GROUP_PROPS).copy();
            }
            CompoundTag partQuests = part.getCompound(QuestSyncKeys.QUESTS);
            for (String key : partQuests.getAllKeys()) {
                quests.put(key, partQuests.getCompound(key).copy());
            }
        }
        full.put(QuestSyncKeys.GROUPS, groups);
        full.put(QuestSyncKeys.GROUP_PROPS, groupProps);
        full.put(QuestSyncKeys.QUESTS, quests);
        return full;
    }

    public CompoundTag joinDeltaPayload() {
        CompoundTag delta = new CompoundTag();
        CompoundTag changed = new CompoundTag();
        CompoundTag removed = new CompoundTag();
        ListTag groups = new ListTag();
        CompoundTag groupProps = new CompoundTag();

        for (int i = 0; i < expected; i++) {
            CompoundTag part = parts.get(i);
            if (part == null) {
                continue;
            }
            if (groups.isEmpty()) {
                groups = (ListTag) part.getList(QuestSyncKeys.GROUPS, Tag.TAG_STRING).copy();
            }
            if (groupProps.isEmpty() && part.contains(QuestSyncKeys.GROUP_PROPS, Tag.TAG_COMPOUND)) {
                groupProps = part.getCompound(QuestSyncKeys.GROUP_PROPS).copy();
            }
            CompoundTag partChanged = part.getCompound(QuestSyncKeys.CHANGED);
            for (String key : partChanged.getAllKeys()) {
                changed.put(key, partChanged.getCompound(key).copy());
            }

            CompoundTag partRemoved = part.getCompound(QuestSyncKeys.REMOVED);
            for (String key : partRemoved.getAllKeys()) {
                Tag entry = partRemoved.get(key);
                if (entry != null) {
                    removed.put(key, entry.copy());
                }
            }
        }

        if (!groups.isEmpty() || !groupProps.isEmpty()) {
            delta.put(QuestSyncKeys.GROUPS, groups);
            delta.put(QuestSyncKeys.GROUP_PROPS, groupProps);
        }
        delta.put(QuestSyncKeys.CHANGED, changed);
        delta.put(QuestSyncKeys.REMOVED, removed);
        return delta;
    }

    public CompoundTag joinDescriptionPayload() {
        CompoundTag combined = new CompoundTag();
        CompoundTag descriptions = new CompoundTag();
        for (int i = 0; i < expected; i++) {
            CompoundTag part = parts.get(i);
            if (part == null) {
                continue;
            }
            CompoundTag partDescriptions = part.getCompound(QuestSyncKeys.DESCRIPTIONS);
            for (String key : partDescriptions.getAllKeys()) {
                descriptions.put(key, partDescriptions.getList(key, Tag.TAG_STRING).copy());
            }
        }
        combined.put(QuestSyncKeys.DESCRIPTIONS, descriptions);
        return combined;
    }
}

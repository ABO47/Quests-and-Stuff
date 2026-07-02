package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
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
        full.putInt(SyncKeys.SCHEMA, QuestDefinition.CURRENT_SCHEMA);
        CompoundTag quests = new CompoundTag();
        ListTag groups = new ListTag();
        CompoundTag groupProps = new CompoundTag();
        for (int i = 0; i < expected; i++) {
            CompoundTag part = parts.get(i);
            if (part == null) {
                continue;
            }
            if (groups.isEmpty()) {
                groups = (ListTag) part.getList(SyncKeys.GROUPS, Tag.TAG_STRING).copy();
            }
            if (groupProps.isEmpty() && part.contains(SyncKeys.CHAPTER_PROPS, Tag.TAG_COMPOUND)) {
                groupProps = part.getCompound(SyncKeys.CHAPTER_PROPS).copy();
            }
            CompoundTag partQuests = part.getCompound(SyncKeys.QUESTS);
            for (String key : partQuests.getAllKeys()) {
                quests.put(key, partQuests.getCompound(key).copy());
            }
        }
        full.put(SyncKeys.GROUPS, groups);
        full.put(SyncKeys.CHAPTER_PROPS, groupProps);
        full.put(SyncKeys.QUESTS, quests);
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
                groups = (ListTag) part.getList(SyncKeys.GROUPS, Tag.TAG_STRING).copy();
            }
            if (groupProps.isEmpty() && part.contains(SyncKeys.CHAPTER_PROPS, Tag.TAG_COMPOUND)) {
                groupProps = part.getCompound(SyncKeys.CHAPTER_PROPS).copy();
            }
            CompoundTag partChanged = part.getCompound(SyncKeys.CHANGED);
            for (String key : partChanged.getAllKeys()) {
                changed.put(key, partChanged.getCompound(key).copy());
            }

            CompoundTag partRemoved = part.getCompound(SyncKeys.REMOVED);
            for (String key : partRemoved.getAllKeys()) {
                Tag entry = partRemoved.get(key);
                if (entry != null) {
                    removed.put(key, entry.copy());
                }
            }
        }

        if (!groups.isEmpty() || !groupProps.isEmpty()) {
            delta.put(SyncKeys.GROUPS, groups);
            delta.put(SyncKeys.CHAPTER_PROPS, groupProps);
        }
        delta.put(SyncKeys.CHANGED, changed);
        delta.put(SyncKeys.REMOVED, removed);
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
            CompoundTag partDescriptions = part.getCompound(SyncKeys.DESCRIPTIONS);
            for (String key : partDescriptions.getAllKeys()) {
                descriptions.put(key, partDescriptions.getList(key, Tag.TAG_STRING).copy());
            }
        }
        combined.put(SyncKeys.DESCRIPTIONS, descriptions);
        return combined;
    }
}

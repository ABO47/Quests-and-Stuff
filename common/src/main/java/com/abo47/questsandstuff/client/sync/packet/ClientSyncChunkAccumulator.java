package com.abo47.questsandstuff.client.sync.packet;

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

    public void add(int index, CompoundTag payload) {
        parts.put(index, payload.copy());
    }

    public boolean complete() {
        return parts.size() >= expected;
    }

    public CompoundTag joinFullPayload() {
        CompoundTag full = new CompoundTag();
        full.putInt("schema", 1);
        CompoundTag quests = new CompoundTag();
        ListTag groups = new ListTag();
        CompoundTag groupProps = new CompoundTag();
        for (int i = 0; i < expected; i++) {
            CompoundTag part = parts.get(i);
            if (part == null) {
                continue;
            }
            if (groups.isEmpty()) {
                groups = (ListTag) part.getList("groups", Tag.TAG_STRING).copy();
            }
            if (groupProps.isEmpty() && part.contains("group_props", Tag.TAG_COMPOUND)) {
                groupProps = part.getCompound("group_props").copy();
            }
            CompoundTag partQuests = part.getCompound("quests");
            for (String key : partQuests.getAllKeys()) {
                quests.put(key, partQuests.getCompound(key).copy());
            }
        }
        full.put("groups", groups);
        full.put("group_props", groupProps);
        full.put("quests", quests);
        return full;
    }

    public CompoundTag joinDeltaPayload() {
        CompoundTag delta = new CompoundTag();
        CompoundTag changed = new CompoundTag();
        CompoundTag removed = new CompoundTag();

        for (int i = 0; i < expected; i++) {
            CompoundTag part = parts.get(i);
            if (part == null) {
                continue;
            }
            CompoundTag partChanged = part.getCompound("changed");
            for (String key : partChanged.getAllKeys()) {
                changed.put(key, partChanged.getCompound(key).copy());
            }

            CompoundTag partRemoved = part.getCompound("removed");
            for (String key : partRemoved.getAllKeys()) {
                removed.put(key, partRemoved.getCompound(key).copy());
            }
        }

        delta.put("changed", changed);
        delta.put("removed", removed);
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
            CompoundTag partDescriptions = part.getCompound("descriptions");
            for (String key : partDescriptions.getAllKeys()) {
                descriptions.put(key, partDescriptions.getList(key, Tag.TAG_STRING).copy());
            }
        }
        combined.put("descriptions", descriptions);
        return combined;
    }
}

package com.abo47.questsandstuff.client.tablet.details.objective;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class QuestObjectiveEntries {
    private QuestObjectiveEntries() {
    }

    static List<QuestDetailsObjectiveEntry> entries(CompoundTag tag) {
        return entries(tag, new ListTag());
    }

    static List<QuestDetailsObjectiveEntry> entries(CompoundTag tag, ListTag order) {
        List<QuestDetailsObjectiveEntry> entries = new ArrayList<>();
        Set<String> added = new HashSet<>();
        for (int i = 0; i < order.size(); i++) {
            String key = order.getString(i);
            addEntry(entries, added, tag, key);
        }
        for (String key : tag.getAllKeys()) {
            addEntry(entries, added, tag, key);
        }
        return entries;
    }

    private static boolean addEntry(List<QuestDetailsObjectiveEntry> entries, Set<String> added, CompoundTag tag, String key) {
        if (key == null || key.isBlank() || added.contains(key) || !tag.contains(key, Tag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag entry = tag.getCompound(key);
        JsonObject json = QuestObjectiveJsons.read(entry.getString("json"));
        if (!json.has("id")) {
            json.addProperty("id", key);
        }
        entries.add(new QuestDetailsObjectiveEntry(key, entry, json));
        added.add(key);
        return true;
    }
}

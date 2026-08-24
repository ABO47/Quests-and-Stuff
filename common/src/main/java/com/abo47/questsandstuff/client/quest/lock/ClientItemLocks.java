package com.abo47.questsandstuff.client.quest.lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.model.task.QuestTaskItemLocks;
import com.abo47.questsandstuff.quest.sync.SyncKeys;

public final class ClientItemLocks {
    private static Set<String> lockedEntries;
    private static boolean anyLocksDefined;
    private static int lastPublishedFingerprint = Integer.MIN_VALUE;

    private ClientItemLocks() {
    }

    public static void invalidate() {
        lockedEntries = null;
    }

    public static boolean anyLocks() {
        ensure();
        return anyLocksDefined;
    }

    public static Set<String> entries() {
        ensure();
        return lockedEntries;
    }

    public static boolean isEntryLocked(String entry) {
        ensure();
        return lockedEntries.contains(entry.toLowerCase());
    }

    public static boolean isLocked(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !anyLocks()) {
            return false;
        }
        if (isEntryLocked(itemEntry(stack.getItem()))) {
            return true;
        }
        boolean[] found = {false};
        stack.getItem().builtInRegistryHolder().tags().forEach(tag -> {
            if (isEntryLocked("#" + tag.location())) {
                found[0] = true;
            }
        });
        return found[0];
    }

    public static String itemEntry(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static void ensure() {
        if (lockedEntries != null) {
            return;
        }
        compute();
    }

    private static void compute() {
        Set<String> entries = new HashSet<>();
        for (var questEntry : ClientQuestState.questEntries()) {
            CompoundTag quest = questEntry.getValue();
            CompoundTag tasks = quest.getCompound(SyncKeys.Quest.TASKS);
            for (String taskId : tasks.getAllKeys()) {
                CompoundTag task = tasks.getCompound(taskId);
                if (task.getBoolean(SyncKeys.Task.COMPLETE)) {
                    continue;
                }
                List<String> locks = locksOf(task.getString(SyncKeys.Task.JSON));
                entries.addAll(locks);
            }
        }
        lockedEntries = Collections.unmodifiableSet(entries);
        anyLocksDefined = !entries.isEmpty();
        int fingerprint = entries.hashCode();
        if (fingerprint != lastPublishedFingerprint) {
            lastPublishedFingerprint = fingerprint;
            ClientLockEvents.fire();
        }
    }

    private static List<String> locksOf(String taskJson) {
        try {
            JsonElement parsed = JsonParser.parseString(taskJson == null || taskJson.isBlank() ? "{}" : taskJson);
            if (!parsed.isJsonObject() || !parsed.getAsJsonObject().has(QuestTaskItemLocks.FIELD)) {
                return List.of();
            }
            JsonElement field = parsed.getAsJsonObject().get(QuestTaskItemLocks.FIELD);
            if (!field.isJsonArray()) {
                return List.of();
            }
            List<String> locks = new ArrayList<>();
            for (JsonElement element : field.getAsJsonArray()) {
                if (element.isJsonPrimitive()) {
                    locks.add(element.getAsString());
                }
            }
            return QuestTaskItemLocks.normalize(locks);
        } catch (Exception error) {
            return List.of();
        }
    }
}

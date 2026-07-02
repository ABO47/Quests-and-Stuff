package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import com.abo47.questsandstuff.util.naming.QuestIdentity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class ClientQuestTaskMutations {
    private ClientQuestTaskMutations() {
    }

    public static void putQuestTaskJsonLocal(String questId, String taskJson) {
        putObjectiveJsonLocal(questId, taskJson, SyncKeys.Quest.TASKS, SyncKeys.Quest.TASKS_ORDER);
    }

    public static void putQuestRewardJsonLocal(String questId, String rewardJson) {
        putObjectiveJsonLocal(questId, rewardJson, SyncKeys.Quest.REWARDS, SyncKeys.Quest.REWARDS_ORDER);
    }

    private static void putObjectiveJsonLocal(String questId, String jsonValue, String bucketName, String orderName) {
        String normalizedQuest = normalizeQuestId(questId);
        if (normalizedQuest.isBlank() || jsonValue == null || jsonValue.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalizedQuest);
        if (quest == null) {
            return;
        }
        String id = objectiveId(normalizedQuest, bucketName, jsonValue);
        if (id.isBlank()) {
            return;
        }
        CompoundTag bucket = quest.getCompound(bucketName);
        CompoundTag entry = bucket.getCompound(id);
        entry.putString(SyncKeys.Objective.JSON, jsonValue);
        entry.putString(SyncKeys.Objective.TYPE, objectiveType(normalizedQuest, id, bucketName, jsonValue, entry.getString(SyncKeys.Objective.TYPE)));
        bucket.put(id, entry);
        quest.put(bucketName, bucket);
        appendObjectiveOrder(quest, orderName, id);
    }

    private static void appendObjectiveOrder(CompoundTag quest, String orderName, String id) {
        if (quest == null || orderName == null || orderName.isBlank() || id == null || id.isBlank()) {
            return;
        }
        ListTag current = quest.getList(orderName, Tag.TAG_STRING);
        ListTag next = new ListTag();
        boolean found = false;
        for (int i = 0; i < current.size(); i++) {
            String value = current.getString(i);
            if (id.equals(value)) {
                found = true;
            }
            next.add(current.get(i).copy());
        }
        if (!found) {
            next.add(StringTag.valueOf(id));
        }
        quest.put(orderName, next);
    }

    private static String objectiveId(String questId, String bucketName, String jsonValue) {
        try {
            JsonObject json = JsonParser.parseString(jsonValue).getAsJsonObject();
            return json.has("id") && !json.get("id").isJsonNull() ? json.get("id").getAsString().trim() : "";
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Sync] Failed reading optimistic objective id quest={} bucket={} json={}",
                    questId,
                    bucketName,
                    abbreviateJson(jsonValue),
                    exception
            );
            return "";
        }
    }

    private static String objectiveType(String questId, String objectiveId, String bucketName, String jsonValue, String fallback) {
        try {
            JsonObject json = JsonParser.parseString(jsonValue).getAsJsonObject();
            return json.has("type") && !json.get("type").isJsonNull() ? json.get("type").getAsString().trim() : fallback;
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Sync] Failed reading optimistic objective type quest={} objective={} bucket={} json={}",
                    questId,
                    objectiveId,
                    bucketName,
                    abbreviateJson(jsonValue),
                    exception
            );
            return fallback == null ? "" : fallback;
        }
    }

    private static String abbreviateJson(String jsonValue) {
        String value = jsonValue == null ? "" : jsonValue.replace('\n', ' ').replace('\r', ' ').trim();
        return value.length() <= 120 ? value : value.substring(0, 117) + "...";
    }

    private static String normalizeQuestId(String value) {
        return QuestIdentity.questId(value);
    }
}

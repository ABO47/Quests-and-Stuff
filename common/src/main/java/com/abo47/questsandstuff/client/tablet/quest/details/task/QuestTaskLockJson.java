package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.ArrayList;
import java.util.List;

import com.abo47.questsandstuff.quest.model.task.QuestTaskItemLocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class QuestTaskLockJson {
    private QuestTaskLockJson() {
    }

    static List<String> locks(JsonObject taskJson) {
        if (taskJson == null || !taskJson.has(QuestTaskItemLocks.FIELD) || !taskJson.get(QuestTaskItemLocks.FIELD).isJsonArray()) {
            return List.of();
        }
        List<String> entries = new ArrayList<>();
        for (JsonElement element : taskJson.getAsJsonArray(QuestTaskItemLocks.FIELD)) {
            if (element.isJsonPrimitive()) {
                entries.add(element.getAsString());
            }
        }
        return QuestTaskItemLocks.normalize(entries);
    }

    static boolean hasLocks(JsonObject taskJson) {
        return !locks(taskJson).isEmpty();
    }

    static void add(JsonObject taskJson, String entry) {
        taskJson.add(QuestTaskItemLocks.FIELD, toJson(QuestTaskItemLocks.add(locks(taskJson), entry)));
    }

    static void remove(JsonObject taskJson, String entry) {
        taskJson.add(QuestTaskItemLocks.FIELD, toJson(QuestTaskItemLocks.remove(locks(taskJson), entry)));
    }

    private static JsonArray toJson(List<String> entries) {
        JsonArray array = new JsonArray();
        for (String entry : entries) {
            array.add(entry);
        }
        return array;
    }
}

package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;

record QuestDetailsObjectiveEntry(String id, CompoundTag tag, JsonObject json) {
    String type() {
        String tagType = tag.getString("type");
        return tagType == null || tagType.isBlank() ? jsonString(json, "type") : tagType;
    }

    private static String jsonString(JsonObject json, String key) {
        if (json == null || !json.has(key) || json.get(key).isJsonNull()) {
            return "";
        }
        try {
            return json.get(key).getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }
}

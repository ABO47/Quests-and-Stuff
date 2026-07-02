package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;

record QuestDetailsObjectiveEntry(String id, CompoundTag tag, JsonObject json) {
    String type() {
        String tagType = tag.getString("type");
        return tagType == null || tagType.isBlank() ? TaskJsonFactory.asString(json, "type", "") : tagType;
    }
}

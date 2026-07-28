package com.abo47.questsandstuff.client.tablet.quest.details.task;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;

record QuestDetailsTaskEntry(String id, CompoundTag tag, JsonObject json) {
    String type() {
        String tagType = tag.getString("type");
        return tagType == null || tagType.isBlank() ? TaskJsonFactory.asString(json, "type", "") : tagType;
    }
}

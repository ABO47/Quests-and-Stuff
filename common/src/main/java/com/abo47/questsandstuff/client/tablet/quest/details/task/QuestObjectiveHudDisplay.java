package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public final class QuestObjectiveHudDisplay {
    private QuestObjectiveHudDisplay() {
    }

    public static String title(CompoundTag taskTag) {
        if (taskTag == null) {
            return "";
        }
        String type = taskTag.getString("type");
        JsonObject json = jsonWithType(taskTag, type);
        return QuestObjectiveDisplayText.displayName(json, type);
    }

    public static String progressText(CompoundTag taskTag) {
        if (taskTag == null) {
            return "";
        }
        if (taskTag.getBoolean("complete")) {
            return Component.translatable("ui.questsandstuff.common.done").getString();
        }
        String type = taskTag.getString("type");
        JsonObject json = jsonWithType(taskTag, type);
        if (QuestObjectiveDisplayText.usesAmountField(json, true)) {
            int amount = QuestObjectiveDisplayText.amount(json);
            int count = Math.max(0, Math.min(amount, taskTag.getInt("count")));
            return count + "/" + amount;
        }
        if (taskTag.contains("progress")) {
            int percent = Math.max(0, Math.min(99, Math.round(taskTag.getFloat("progress") * 100.0f)));
            return percent <= 0 ? "" : percent + "%";
        }
        return "";
    }

    public static String icon(CompoundTag taskTag) {
        if (taskTag == null) {
            return "";
        }
        String type = taskTag.getString("type");
        return QuestObjectiveDisplayText.taskIcon(jsonWithType(taskTag, type));
    }

    private static JsonObject jsonWithType(CompoundTag taskTag, String type) {
        JsonObject json = TaskJsonFactory.read(taskTag == null ? "" : taskTag.getString("json"));
        if (!json.has("type") && type != null && !type.isBlank()) {
            json.addProperty("type", type);
        }
        return json;
    }
}

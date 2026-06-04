package com.abo47.questsandstuff.client.canvas.blueprint;

import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientQuestDefinitionSnapshots {
    private ClientQuestDefinitionSnapshots() {
    }

    public static QuestDefinition fromClientTag(String questId, CompoundTag tag) {
        if (questId == null || questId.isBlank() || tag == null || tag.isEmpty()) {
            return null;
        }
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                questId.trim(),
                display(tag),
                settings(tag),
                strings(tag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING)),
                intMap(tag.getCompound("connection_colors")),
                stringMap(tag.getCompound("connection_modes")),
                strings(tag.getList("hidden_connections", Tag.TAG_STRING)),
                stringList(tag.getList("tasks_order", Tag.TAG_STRING)),
                stringList(tag.getList("rewards_order", Tag.TAG_STRING)),
                tasks(tag.getCompound("tasks")),
                rewards(tag.getCompound("rewards"))
        );
    }

    private static QuestDisplay display(CompoundTag tag) {
        return new QuestDisplay(
                tag.getString("title"),
                tag.getString("subtitle"),
                stringList(tag.getList("description", Tag.TAG_STRING)),
                groups(tag.getCompound("groups")),
                tag.getString("icon"),
                tag.getString("icon_background"),
                tag.getString("completion_sound"),
                tag.contains("completion_sound_volume", Tag.TAG_INT)
                        ? tag.getInt("completion_sound_volume")
                        : QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME,
                tag.getString("completion_hud_background"),
                tag.getBoolean("visual_hidden"),
                tag.getString("quest_background"),
                tag.getBoolean("quest_background_grayscale")
        );
    }

    private static QuestSettings settings(CompoundTag tag) {
        String hiddenMode = tag.getString("hidden_mode");
        return new QuestSettings(
                false,
                hiddenMode.isBlank() ? QuestSettings.DEFAULT.hiddenMode() : QuestVisibilityMode.fromSerializedName(hiddenMode),
                tag.getBoolean("repeatable"),
                false,
                false,
                !tag.contains(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)
                        || tag.getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)
        );
    }

    private static Map<String, ChapterDefinition> groups(CompoundTag groupsTag) {
        Map<String, ChapterDefinition> groups = new LinkedHashMap<>();
        for (String group : groupsTag.getAllKeys()) {
            CompoundTag view = groupsTag.getCompound(group);
            groups.put(group, new ChapterDefinition(
                    !view.contains("visible") || view.getBoolean("visible"),
                    view.getInt("x"),
                    view.getInt("y"),
                    view.contains("scale", Tag.TAG_FLOAT) ? view.getFloat("scale") : 1.0f
            ));
        }
        return groups;
    }

    private static Map<String, QuestTaskDefinition> tasks(CompoundTag tasksTag) {
        Map<String, QuestTaskDefinition> tasks = new LinkedHashMap<>();
        for (String taskId : tasksTag.getAllKeys()) {
            QuestTaskDefinition task = QuestTaskDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(tasksTag.getCompound(taskId).getString("json")))
                    .result()
                    .orElse(null);
            if (task != null) {
                tasks.put(taskId, task);
            }
        }
        return tasks;
    }

    private static Map<String, QuestRewardDefinition> rewards(CompoundTag rewardsTag) {
        Map<String, QuestRewardDefinition> rewards = new LinkedHashMap<>();
        for (String rewardId : rewardsTag.getAllKeys()) {
            QuestRewardDefinition reward = QuestRewardDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(rewardsTag.getCompound(rewardId).getString("json")))
                    .result()
                    .orElse(null);
            if (reward != null) {
                rewards.put(rewardId, reward);
            }
        }
        return rewards;
    }

    private static Set<String> strings(ListTag tags) {
        return new LinkedHashSet<>(stringList(tags));
    }

    private static List<String> stringList(ListTag tags) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            String value = tags.getString(i);
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private static Map<String, Integer> intMap(CompoundTag tag) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_INT)) {
                values.put(key, tag.getInt(key));
            }
        }
        return values;
    }

    private static Map<String, String> stringMap(CompoundTag tag) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            String value = tag.getString(key);
            if (!value.isBlank()) {
                values.put(key, value);
            }
        }
        return values;
    }
}

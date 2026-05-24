package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class QuestSyncPayloadBuilder {
    private final QuestDefinitionStore definitionStore;

    QuestSyncPayloadBuilder(QuestDefinitionStore definitionStore) {
        this.definitionStore = definitionStore;
    }

    ListTag groupsTag() {
        ListTag groupsTag = new ListTag();
        for (String group : definitionStore.groupOrder()) {
            groupsTag.add(StringTag.valueOf(group));
        }
        return groupsTag;
    }

    CompoundTag groupPropsTag() {
        CompoundTag groupProps = new CompoundTag();
        for (String group : definitionStore.groupOrder()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("icon", definitionStore.groupIcon(group));
            entry.putString("background", definitionStore.groupBackground(group));
            entry.putString("canvas_background", definitionStore.groupCanvasBackground(group));
            entry.putString("text_align", definitionStore.groupTextAlign(group));
            entry.putInt("text_color", definitionStore.groupTextColor(group));
            entry.putString("text_style", definitionStore.groupTextStyle(group));
            entry.putInt("text_size", definitionStore.groupTextSize(group));
            entry.put("canvas_images", CanvasLayerNbt.imagesToListTag(definitionStore.canvasImages(group)));
            entry.put("canvas_texts", CanvasLayerNbt.textsToListTag(definitionStore.canvasTexts(group)));
            entry.put("canvas_layer_order", CanvasLayerNbt.stringsToListTag(definitionStore.canvasLayerOrder(group)));
            groupProps.put(group, entry);
        }
        return groupProps;
    }

    CompoundTag questPayload(PlayerQuestState playerState, Set<String> questIds) {
        CompoundTag questsTag = new CompoundTag();

        for (String questId : questIds) {
            QuestDefinition definition = definitionStore.quests().get(questId);
            if (definition == null) {
                continue;
            }
            QuestProgressState progress = playerState.quest(questId);
            questsTag.put(questId, questTag(definition, progress, false));
        }

        return questsTag;
    }

    CompoundTag editorQuestPayload(QuestDefinition definition) {
        return questTag(definition, null, true);
    }

    private static CompoundTag questTag(QuestDefinition definition, QuestProgressState progress, boolean includeDescription) {
        CompoundTag questTag = new CompoundTag();
        questTag.putString("title", definition.display().title());
        questTag.putString("subtitle", definition.display().subtitle());
        if (includeDescription) {
            questTag.put("description", descriptionTag(definition));
        }
        questTag.putString("icon", definition.display().icon());
        questTag.putString("icon_background", definition.display().iconBackground());
        questTag.putString("completion_sound", definition.display().completionSound());
        questTag.putBoolean("visual_hidden", definition.display().visualHidden());
        if (progress != null) {
            questTag.putBoolean("completed", progress.completed());
            questTag.putBoolean("unlocked", progress.unlocked());
            questTag.putBoolean("claimed", allRewardsClaimed(definition, progress));
            questTag.putFloat("progress", progressPercent(definition, progress));
        }
        questTag.putBoolean("repeatable", definition.settings().repeatable());
        questTag.putBoolean("auto_claim_rewards", definition.settings().autoClaimRewards());
        questTag.putString("hidden_mode", definition.settings().hiddenMode().serializedName());
        questTag.putBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD, definition.settings().showPrerequisiteArrow());
        questTag.put("tasks", taskDefinitionsTag(definition, progress));
        questTag.put("tasks_order", stringOrderTag(definition.tasksOrder()));
        questTag.put("rewards", rewardDefinitionsTag(definition));
        questTag.put("rewards_order", stringOrderTag(definition.rewardsOrder()));
        questTag.put(QuestDefinition.PREREQUISITES_FIELD, prerequisitesTag(definition));
        questTag.put("connection_colors", connectionColorsTag(definition));
        questTag.put("connection_modes", connectionModesTag(definition));
        questTag.put("hidden_connections", hiddenConnectionsTag(definition));
        questTag.put("groups", chapterViewsTag(definition));
        return questTag;
    }

    private static ListTag descriptionTag(QuestDefinition definition) {
        ListTag lines = new ListTag();
        for (String line : definition.display().description()) {
            lines.add(StringTag.valueOf(line));
        }
        return lines;
    }

    private static ListTag prerequisitesTag(QuestDefinition definition) {
        ListTag prerequisites = new ListTag();
        List<String> prerequisiteIds = new ArrayList<>(definition.prerequisites());
        prerequisiteIds.sort(String::compareTo);
        for (String prerequisiteId : prerequisiteIds) {
            prerequisites.add(StringTag.valueOf(prerequisiteId));
        }
        return prerequisites;
    }

    private static CompoundTag chapterViewsTag(QuestDefinition definition) {
        CompoundTag groups = new CompoundTag();
        for (Map.Entry<String, ChapterDefinition> groupEntry : definition.display().groups().entrySet()) {
            ChapterDefinition view = groupEntry.getValue();
            CompoundTag groupTag = new CompoundTag();
            groupTag.putBoolean("visible", view.visible());
            groupTag.putInt("x", view.x());
            groupTag.putInt("y", view.y());
            groupTag.putFloat("scale", view.scale());
            groups.put(groupEntry.getKey(), groupTag);
        }
        return groups;
    }

    private static float progressPercent(QuestDefinition definition, QuestProgressState progress) {
        if (definition.tasks().isEmpty()) {
            return 0.0f;
        }

        float sum = 0.0f;
        int count = 0;
        for (Map.Entry<String, QuestTaskDefinition> task : definition.tasks().entrySet()) {
            sum += task.getValue().getProgress(progress.getTaskProgress(task.getKey(), task.getValue()));
            count++;
        }

        return count == 0 ? 0.0f : sum / (float) count;
    }

    private static boolean allRewardsClaimed(QuestDefinition definition, QuestProgressState progress) {
        if (definition.rewards().isEmpty()) {
            return progress.completed();
        }
        return progress.claimedRewards().containsAll(definition.rewards().keySet());
    }

    private static CompoundTag taskDefinitionsTag(QuestDefinition definition, QuestProgressState progress) {
        CompoundTag tasksTag = new CompoundTag();
        for (Map.Entry<String, QuestTaskDefinition> entry : definition.tasks().entrySet()) {
            QuestTaskDefinition task = entry.getValue();
            CompoundTag taskTag = new CompoundTag();
            taskTag.putString("type", task.type().toString());
            taskTag.putString("json", encodeTask(task));
            if (progress != null) {
                var taskProgress = progress.getTaskProgress(entry.getKey(), task);
                taskTag.putFloat("progress", task.getProgress(taskProgress));
                taskTag.putBoolean("complete", task.isComplete(taskProgress));
                taskTag.putInt("count", progress.getTaskCount(entry.getKey()));
            }
            tasksTag.put(entry.getKey(), taskTag);
        }
        return tasksTag;
    }

    private static CompoundTag rewardDefinitionsTag(QuestDefinition definition) {
        CompoundTag rewardsTag = new CompoundTag();
        for (Map.Entry<String, QuestRewardDefinition> entry : definition.rewards().entrySet()) {
            QuestRewardDefinition reward = entry.getValue();
            CompoundTag rewardTag = new CompoundTag();
            rewardTag.putString("type", reward.type().toString());
            rewardTag.putString("json", encodeReward(reward));
            rewardTag.putBoolean("selectable", reward.selectable());
            rewardTag.putBoolean("mass_claimable", reward.canBeMassClaimed());
            rewardsTag.put(entry.getKey(), rewardTag);
        }
        return rewardsTag;
    }

    private static String encodeTask(QuestTaskDefinition task) {
        try {
            JsonElement encoded = QuestTaskDefinition.CODEC.encodeStart(JsonOps.INSTANCE, task)
                    .getOrThrow(false, ignored -> {});
            return encoded.toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }

    private static String encodeReward(QuestRewardDefinition reward) {
        try {
            JsonElement encoded = QuestRewardDefinition.CODEC.encodeStart(JsonOps.INSTANCE, reward)
                    .getOrThrow(false, ignored -> {});
            return encoded.toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }

    private static ListTag stringOrderTag(Iterable<String> values) {
        ListTag order = new ListTag();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                order.add(StringTag.valueOf(value));
            }
        }
        return order;
    }

    private static CompoundTag connectionColorsTag(QuestDefinition definition) {
        CompoundTag colors = new CompoundTag();
        for (Map.Entry<String, Integer> entry : definition.connectionColors().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                colors.putInt(entry.getKey(), entry.getValue());
            }
        }
        return colors;
    }

    private static CompoundTag connectionModesTag(QuestDefinition definition) {
        CompoundTag modes = new CompoundTag();
        for (Map.Entry<String, String> entry : definition.connectionModes().entrySet()) {
            if (entry.getKey() != null && "grid".equals(entry.getValue())) {
                modes.putString(entry.getKey(), entry.getValue());
            }
        }
        return modes;
    }

    private static ListTag hiddenConnectionsTag(QuestDefinition definition) {
        ListTag hidden = new ListTag();
        List<String> hiddenConnectionIds = new ArrayList<>(definition.hiddenConnections());
        hiddenConnectionIds.sort(String::compareTo);
        for (String hiddenConnectionId : hiddenConnectionIds) {
            hidden.add(StringTag.valueOf(hiddenConnectionId));
        }
        return hidden;
    }
}

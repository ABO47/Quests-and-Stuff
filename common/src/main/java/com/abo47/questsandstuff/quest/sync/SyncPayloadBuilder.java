package com.abo47.questsandstuff.quest.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;

import com.google.gson.JsonElement;

final class SyncPayloadBuilder {
    private final QuestDefinitionStore definitionStore;

    SyncPayloadBuilder(QuestDefinitionStore definitionStore) {
        this.definitionStore = definitionStore;
    }

    ListTag chaptersTag() {
        ListTag chaptersTag = new ListTag();
        for (String chapter : definitionStore.chapterOrder()) {
            chaptersTag.add(StringTag.valueOf(chapter));
        }
        return chaptersTag;
    }

    CompoundTag chapterPropsTag() {
        return chapterPropsTagForChapters(Set.copyOf(definitionStore.chapterOrder()));
    }

    CompoundTag chapterPropsTagForChapters(Set<String> chapters) {
        CompoundTag chapterProps = new CompoundTag();
        if (chapters == null || chapters.isEmpty()) {
            return chapterProps;
        }
        Set<String> pending = new HashSet<>(chapters);
        for (String chapter : definitionStore.chapterOrder()) {
            if (pending.remove(chapter)) {
                chapterProps.put(chapter, chapterPropsEntry(chapter));
            }
        }
        for (String chapter : pending) {
            if (chapter != null && !chapter.isBlank()) {
                chapterProps.put(chapter, chapterPropsEntry(chapter));
            }
        }
        return chapterProps;
    }

    private CompoundTag chapterPropsEntry(String chapter) {
        CompoundTag entry = new CompoundTag();
        entry.putString(SyncKeys.ChapterProps.ICON, definitionStore.chapterIcon(chapter));
        entry.putString(SyncKeys.ChapterProps.BACKGROUND, definitionStore.chapterBackground(chapter));
        entry.putString(SyncKeys.ChapterProps.CANVAS_BACKGROUND, definitionStore.chapterCanvasBackground(chapter));
        entry.putString(SyncKeys.ChapterProps.TEXT_ALIGN, definitionStore.chapterTextAlign(chapter));
        entry.putInt(SyncKeys.ChapterProps.TEXT_COLOR, definitionStore.chapterTextColor(chapter));
        entry.putString(SyncKeys.ChapterProps.TEXT_STYLE, definitionStore.chapterTextStyle(chapter));
        entry.putInt(SyncKeys.ChapterProps.TEXT_SIZE, definitionStore.chapterTextSize(chapter));
        entry.putBoolean(SyncKeys.ChapterProps.LOCK_UNTIL_UNLOCKED, definitionStore.chapterLockUntilUnlocked(chapter));
        entry.putBoolean(SyncKeys.ChapterProps.HIDE_UNTIL_UNLOCKED, definitionStore.chapterHideUntilUnlocked(chapter));
        entry.put(SyncKeys.ChapterProps.CANVAS_EXCLUSIVE_CHOICES, CanvasLayerNbtCodec.exclusiveChoicesToListTag(definitionStore.canvasExclusiveChoices(chapter)));
        entry.put(SyncKeys.ChapterProps.CANVAS_IMAGES, CanvasLayerNbtCodec.imagesToListTag(definitionStore.canvasImages(chapter)));
        entry.put(SyncKeys.ChapterProps.CANVAS_TEXTS, CanvasLayerNbtCodec.textsToListTag(definitionStore.canvasTexts(chapter)));
        entry.put(SyncKeys.ChapterProps.CANVAS_LAYER_ORDER, CanvasLayerNbtCodec.stringsToListTag(definitionStore.canvasLayerOrder(chapter)));
        return entry;
    }

    CompoundTag questPayload(PlayerQuestState playerState, Set<String> questIds) {
        CompoundTag questsTag = new CompoundTag();

        for (String questId : questIds) {
            QuestDefinition definition = definitionStore.quest(questId);
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

    CompoundTag editorQuestPayload(QuestDefinition definition, PlayerQuestState playerState) {
        QuestProgressState progress = playerState == null ? null : playerState.quest(definition.id());
        return questTag(definition, progress, true);
    }

    private static CompoundTag questTag(QuestDefinition definition, QuestProgressState progress, boolean includeDescription) {
        CompoundTag questTag = new CompoundTag();
        questTag.putString(SyncKeys.Quest.TITLE, definition.display().title());
        questTag.putString(SyncKeys.Quest.SUBTITLE, definition.display().subtitle());
        if (includeDescription) {
            questTag.put(SyncKeys.Quest.DESCRIPTION, descriptionTag(definition));
        }
        questTag.putString(SyncKeys.Quest.ICON, definition.display().icon());
        questTag.putString(SyncKeys.Quest.ICON_BACKGROUND, definition.display().iconBackground());
        questTag.putString(SyncKeys.Quest.COMPLETION_SOUND, definition.display().completionSound());
        questTag.putInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME, definition.display().completionSoundVolume());
        questTag.putString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND, definition.display().completionHudBackground());
        questTag.putBoolean(SyncKeys.Quest.VISUAL_HIDDEN, definition.display().visualHidden());
        questTag.putString(SyncKeys.Quest.QUEST_BACKGROUND, definition.display().questBackground());
        questTag.putBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, definition.display().questBackgroundGrayscale());
        if (progress != null) {
            questTag.putBoolean(SyncKeys.Quest.COMPLETED, progress.completed());
            questTag.putBoolean(SyncKeys.Quest.UNLOCKED, progress.unlocked());
            questTag.putBoolean(SyncKeys.Quest.CLAIMED, allRewardsClaimed(definition, progress));
            questTag.putFloat(SyncKeys.Quest.PROGRESS, progressPercent(definition, progress));
        }
        questTag.putBoolean(SyncKeys.Quest.REPEATABLE, definition.settings().repeatable());
        questTag.putString(SyncKeys.Quest.HIDDEN_MODE, definition.settings().hiddenMode().serializedName());
        questTag.putBoolean(SyncKeys.Quest.SHOW_PREREQUISITE_ARROW, definition.settings().showPrerequisiteArrow());
        questTag.put(SyncKeys.Quest.TASKS, taskDefinitionsTag(definition, progress));
        questTag.put(SyncKeys.Quest.TASKS_ORDER, stringOrderTag(definition.tasksOrder()));
        questTag.put(SyncKeys.Quest.REWARDS, rewardDefinitionsTag(definition));
        questTag.put(SyncKeys.Quest.REWARDS_ORDER, stringOrderTag(definition.rewardsOrder()));
        questTag.put(SyncKeys.Quest.PREREQUISITES, prerequisitesTag(definition));
        questTag.put(SyncKeys.Quest.CONNECTION_COLORS, connectionColorsTag(definition));
        questTag.put(SyncKeys.Quest.CONNECTION_MODES, connectionModesTag(definition));
        questTag.put(SyncKeys.Quest.HIDDEN_CONNECTIONS, hiddenConnectionsTag(definition));
        CompoundTag ctTag = connectionTexturesTag(definition);
        questTag.put(SyncKeys.Quest.CONNECTION_TEXTURES, ctTag);
        questTag.put(SyncKeys.Quest.CONNECTION_TEXTURE_SPACINGS, connectionTextureSpacingsTag(definition));
        questTag.put(SyncKeys.Quest.CHAPTERS, chapterViewsTag(definition));
        return questTag;
    }

    static ListTag descriptionTag(QuestDefinition definition) {
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
        CompoundTag chapters = new CompoundTag();
        for (Map.Entry<String, ChapterDef> chapterEntry : definition.display().chapters().entrySet()) {
            ChapterDef view = chapterEntry.getValue();
            CompoundTag chapterTag = new CompoundTag();
            chapterTag.putBoolean(SyncKeys.ChapterView.VISIBLE, view.visible());
            chapterTag.putInt(SyncKeys.ChapterView.X, view.x());
            chapterTag.putInt(SyncKeys.ChapterView.Y, view.y());
            chapterTag.putFloat(SyncKeys.ChapterView.SCALE, view.scale());
            chapters.put(chapterEntry.getKey(), chapterTag);
        }
        return chapters;
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
            taskTag.putString(SyncKeys.Task.TYPE, task.type().toString());
            taskTag.putString(SyncKeys.Task.JSON, encodeTask(definition.id(), entry.getKey(), task));
            if (progress != null) {
                var taskProgress = progress.getTaskProgress(entry.getKey(), task);
                taskTag.putFloat(SyncKeys.Task.PROGRESS, task.getProgress(taskProgress));
                taskTag.putBoolean(SyncKeys.Task.COMPLETE, task.isComplete(taskProgress));
                taskTag.putInt(SyncKeys.Task.COUNT, progress.getTaskCount(entry.getKey()));
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
            rewardTag.putString(SyncKeys.Task.TYPE, reward.type().toString());
            rewardTag.putString(SyncKeys.Task.JSON, encodeReward(definition.id(), entry.getKey(), reward));
            rewardTag.putBoolean(SyncKeys.Task.SELECTABLE, reward.selectable());
            rewardTag.putBoolean(SyncKeys.Task.MASS_CLAIMABLE, reward.canBeMassClaimed());
            rewardsTag.put(entry.getKey(), rewardTag);
        }
        return rewardsTag;
    }

    private static String encodeTask(String questId, String taskId, QuestTaskDefinition task) {
        try {
            return QuestTaskDefinition.CODEC.encodeStart(JsonOps.INSTANCE, task)
                    .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                            "[QnS:Sync] Failed encoding task JSON quest={} task={} type={} diagnostic={}",
                            questId,
                            taskId,
                            task == null ? "" : task.type(),
                            diagnostic
                    ))
                    .map(JsonElement::toString)
                    .orElse("{}");
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Sync] Failed encoding task JSON quest={} task={} type={}",
                    questId,
                    taskId,
                    task == null ? "" : task.type(),
                    exception
            );
            return "{}";
        }
    }

    private static String encodeReward(String questId, String rewardId, QuestRewardDefinition reward) {
        try {
            return QuestRewardDefinition.CODEC.encodeStart(JsonOps.INSTANCE, reward)
                    .resultOrPartial(diagnostic -> QuestsAndStuffMod.LOGGER.warn(
                            "[QnS:Sync] Failed encoding reward JSON quest={} reward={} type={} diagnostic={}",
                            questId,
                            rewardId,
                            reward == null ? "" : reward.type(),
                            diagnostic
                    ))
                    .map(JsonElement::toString)
                    .orElse("{}");
        } catch (RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn(
                    "[QnS:Sync] Failed encoding reward JSON quest={} reward={} type={}",
                    questId,
                    rewardId,
                    reward == null ? "" : reward.type(),
                    exception
            );
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
            String key = QuestConnectionMetadata.metadataKey(entry.getKey());
            if (!key.isBlank() && entry.getValue() != null) {
                colors.putInt(key, entry.getValue());
            }
        }
        return colors;
    }

    private static CompoundTag connectionModesTag(QuestDefinition definition) {
        CompoundTag modes = new CompoundTag();
        for (Map.Entry<String, String> entry : definition.connectionModes().entrySet()) {
            String key = QuestConnectionMetadata.metadataKey(entry.getKey());
            QuestConnectionMode mode = QuestConnectionMode.fromSerializedName(entry.getValue());
            if (!key.isBlank() && mode.storedInQuestMetadata()) {
                modes.putString(key, mode.serializedName());
            }
        }
        return modes;
    }

    private static ListTag hiddenConnectionsTag(QuestDefinition definition) {
        ListTag hidden = new ListTag();
        List<String> hiddenConnectionIds = new ArrayList<>(definition.hiddenConnections());
        hiddenConnectionIds.sort(String::compareTo);
        for (String hiddenConnectionId : hiddenConnectionIds) {
            String key = QuestConnectionMetadata.metadataKey(hiddenConnectionId);
            if (!key.isBlank()) {
                hidden.add(StringTag.valueOf(key));
            }
        }
        return hidden;
    }

    private static CompoundTag connectionTexturesTag(QuestDefinition definition) {
        CompoundTag textures = new CompoundTag();
        Map<String, String> connTextures = definition.connectionTextures();
        for (Map.Entry<String, String> entry : connTextures.entrySet()) {
            String key = QuestConnectionMetadata.metadataKey(entry.getKey());
            if (!key.isBlank() && entry.getValue() != null && !entry.getValue().isBlank()) {
                textures.putString(key, entry.getValue());
            }
        }
        return textures;
    }

    private static CompoundTag connectionTextureSpacingsTag(QuestDefinition definition) {
        CompoundTag spacings = new CompoundTag();
        for (Map.Entry<String, Integer> entry : definition.connectionTextureSpacings().entrySet()) {
            String key = QuestConnectionMetadata.metadataKey(entry.getKey());
            if (!key.isBlank() && entry.getValue() != null && entry.getValue() > 0) {
                spacings.putInt(key, entry.getValue());
            }
        }
        return spacings;
    }
}

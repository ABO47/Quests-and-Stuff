package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.Map;

final class ClientQuestSnapshotBuilder {
    private ClientQuestSnapshotBuilder() {
    }

    static CompoundTag newEditorQuest(String title, String group, int x, int y, QuestVisibilityMode hiddenMode) {
        QuestDisplay display = QuestDisplay.forNewQuest(title, Map.of(group, new ChapterDefinition(true, x, y, 1.0f)));
        CompoundTag quest = emptyEditorQuest(display, hiddenMode);
        setProgressDefaults(quest, true);
        return quest;
    }

    static void prepareCopiedQuest(CompoundTag quest, String group, int x, int y, float scale) {
        if (quest == null) {
            return;
        }
        ensureDisplayDefaults(quest);
        ensureDefinitionBuckets(quest);
        setProgressDefaults(quest, false);
        replaceSingleGroup(quest, group, x, y, scale);
    }

    private static CompoundTag emptyEditorQuest(QuestDisplay display, QuestVisibilityMode hiddenMode) {
        CompoundTag quest = new CompoundTag();
        putDisplay(quest, display);
        putSettingsDefaults(quest, hiddenMode);
        quest.put(QuestSyncKeys.Quest.PREREQUISITES, new ListTag());
        quest.put(QuestSyncKeys.Quest.CONNECTION_COLORS, new CompoundTag());
        quest.put(QuestSyncKeys.Quest.CONNECTION_MODES, new CompoundTag());
        quest.put(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, new ListTag());
        quest.put(QuestSyncKeys.Quest.TASKS, new CompoundTag());
        quest.put(QuestSyncKeys.Quest.TASKS_ORDER, new ListTag());
        quest.put(QuestSyncKeys.Quest.REWARDS, new CompoundTag());
        quest.put(QuestSyncKeys.Quest.REWARDS_ORDER, new ListTag());
        return quest;
    }

    private static void putDisplay(CompoundTag quest, QuestDisplay display) {
        QuestDisplay safe = display == null ? QuestDisplay.DEFAULT : display;
        quest.putString(QuestSyncKeys.Quest.TITLE, safe.title());
        quest.putString(QuestSyncKeys.Quest.SUBTITLE, safe.subtitle());
        quest.put(QuestSyncKeys.Quest.DESCRIPTION, stringListTag(safe.description()));
        quest.putString(QuestSyncKeys.Quest.ICON, safe.icon());
        quest.putString(QuestSyncKeys.Quest.ICON_BACKGROUND, safe.iconBackground());
        quest.putString(QuestSyncKeys.Quest.COMPLETION_SOUND, safe.completionSound());
        quest.putInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME, safe.completionSoundVolume());
        quest.putString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND, safe.completionHudBackground());
        quest.putBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN, safe.visualHidden());
        quest.putString(QuestSyncKeys.Quest.QUEST_BACKGROUND, safe.questBackground());
        quest.putBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, safe.questBackgroundGrayscale());
        quest.put(QuestSyncKeys.Quest.GROUPS, groupsTag(safe.groups()));
    }

    private static void putSettingsDefaults(CompoundTag quest, QuestVisibilityMode hiddenMode) {
        QuestVisibilityMode mode = hiddenMode == null ? QuestSettings.DEFAULT.hiddenMode() : hiddenMode;
        quest.putBoolean(QuestSyncKeys.Quest.REPEATABLE, QuestSettings.DEFAULT.repeatable());
        quest.putString(QuestSyncKeys.Quest.HIDDEN_MODE, mode.serializedName());
        quest.putBoolean(QuestSyncKeys.Quest.SHOW_PREREQUISITE_ARROW, QuestSettings.DEFAULT.showPrerequisiteArrow());
    }

    private static void setProgressDefaults(CompoundTag quest, boolean unlocked) {
        quest.putBoolean(QuestSyncKeys.Quest.COMPLETED, false);
        quest.putBoolean(QuestSyncKeys.Quest.UNLOCKED, unlocked);
        quest.putBoolean(QuestSyncKeys.Quest.CLAIMED, false);
        quest.putFloat(QuestSyncKeys.Quest.PROGRESS, 0.0f);

        CompoundTag tasks = quest.getCompound(QuestSyncKeys.Quest.TASKS);
        for (String taskId : tasks.getAllKeys()) {
            CompoundTag task = tasks.getCompound(taskId);
            task.putFloat(QuestSyncKeys.Objective.PROGRESS, 0.0f);
            task.putBoolean(QuestSyncKeys.Objective.COMPLETE, false);
            task.putInt(QuestSyncKeys.Objective.COUNT, 0);
            tasks.put(taskId, task);
        }
        quest.put(QuestSyncKeys.Quest.TASKS, tasks);
    }

    private static void ensureDisplayDefaults(CompoundTag quest) {
        if (!quest.contains(QuestSyncKeys.Quest.TITLE, Tag.TAG_STRING)) {
            quest.putString(QuestSyncKeys.Quest.TITLE, QuestDisplay.DEFAULT.title());
        }
        if (!quest.contains(QuestSyncKeys.Quest.SUBTITLE, Tag.TAG_STRING)) {
            quest.putString(QuestSyncKeys.Quest.SUBTITLE, QuestDisplay.DEFAULT_SUBTITLE);
        }
        if (!quest.contains(QuestSyncKeys.Quest.DESCRIPTION, Tag.TAG_LIST)) {
            quest.put(QuestSyncKeys.Quest.DESCRIPTION, new ListTag());
        }
        quest.putString(QuestSyncKeys.Quest.ICON, QuestDisplay.normalizeIcon(quest.getString(QuestSyncKeys.Quest.ICON)));
        quest.putString(QuestSyncKeys.Quest.ICON_BACKGROUND, QuestDisplay.normalizeIconBackground(quest.getString(QuestSyncKeys.Quest.ICON_BACKGROUND)));
        String completionSound = quest.getString(QuestSyncKeys.Quest.COMPLETION_SOUND);
        quest.putString(QuestSyncKeys.Quest.COMPLETION_SOUND, completionSound == null || completionSound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : completionSound.trim());
        if (!quest.contains(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME, Tag.TAG_INT)) {
            quest.putInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME, QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME);
        } else {
            quest.putInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME, QuestDisplay.normalizeCompletionSoundVolume(quest.getInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME)));
        }
        quest.putString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND, QuestDisplay.normalizeCompletionHudBackground(quest.getString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND)));
        if (!quest.contains(QuestSyncKeys.Quest.VISUAL_HIDDEN, Tag.TAG_BYTE)) {
            quest.putBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN, QuestDisplay.DEFAULT_VISUAL_HIDDEN);
        }
        quest.putString(QuestSyncKeys.Quest.QUEST_BACKGROUND, QuestDisplay.normalizeQuestBackground(quest.getString(QuestSyncKeys.Quest.QUEST_BACKGROUND)));
        if (!quest.contains(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, Tag.TAG_BYTE)) {
            quest.putBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, QuestDisplay.DEFAULT_QUEST_BACKGROUND_GRAYSCALE);
        }
    }

    private static void ensureDefinitionBuckets(CompoundTag quest) {
        if (!quest.contains(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_LIST)) {
            quest.put(QuestSyncKeys.Quest.PREREQUISITES, new ListTag());
        }
        if (!quest.contains(QuestSyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND)) {
            quest.put(QuestSyncKeys.Quest.CONNECTION_COLORS, new CompoundTag());
        }
        if (!quest.contains(QuestSyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND)) {
            quest.put(QuestSyncKeys.Quest.CONNECTION_MODES, new CompoundTag());
        }
        if (!quest.contains(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST)) {
            quest.put(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, new ListTag());
        }
        if (!quest.contains(QuestSyncKeys.Quest.TASKS, Tag.TAG_COMPOUND)) {
            quest.put(QuestSyncKeys.Quest.TASKS, new CompoundTag());
        }
        if (!quest.contains(QuestSyncKeys.Quest.TASKS_ORDER, Tag.TAG_LIST)) {
            quest.put(QuestSyncKeys.Quest.TASKS_ORDER, new ListTag());
        }
        if (!quest.contains(QuestSyncKeys.Quest.REWARDS, Tag.TAG_COMPOUND)) {
            quest.put(QuestSyncKeys.Quest.REWARDS, new CompoundTag());
        }
        if (!quest.contains(QuestSyncKeys.Quest.REWARDS_ORDER, Tag.TAG_LIST)) {
            quest.put(QuestSyncKeys.Quest.REWARDS_ORDER, new ListTag());
        }
    }

    private static void replaceSingleGroup(CompoundTag quest, String group, int x, int y, float scale) {
        CompoundTag groups = new CompoundTag();
        CompoundTag groupTag = new CompoundTag();
        groupTag.putBoolean(QuestSyncKeys.ChapterView.VISIBLE, true);
        groupTag.putInt(QuestSyncKeys.ChapterView.X, x);
        groupTag.putInt(QuestSyncKeys.ChapterView.Y, y);
        float normalizedScale = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat(QuestSyncKeys.ChapterView.SCALE, Math.max(0.5f, normalizedScale));
        groups.put(group, groupTag);
        quest.put(QuestSyncKeys.Quest.GROUPS, groups);
    }

    private static ListTag stringListTag(List<String> values) {
        ListTag out = new ListTag();
        if (values == null) {
            return out;
        }
        for (String value : values) {
            if (value != null) {
                out.add(StringTag.valueOf(value));
            }
        }
        return out;
    }

    private static CompoundTag groupsTag(Map<String, ChapterDefinition> groups) {
        CompoundTag out = new CompoundTag();
        if (groups == null || groups.isEmpty()) {
            return out;
        }
        for (Map.Entry<String, ChapterDefinition> entry : groups.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            ChapterDefinition view = entry.getValue();
            CompoundTag groupTag = new CompoundTag();
            groupTag.putBoolean(QuestSyncKeys.ChapterView.VISIBLE, view.visible());
            groupTag.putInt(QuestSyncKeys.ChapterView.X, view.x());
            groupTag.putInt(QuestSyncKeys.ChapterView.Y, view.y());
            groupTag.putFloat(QuestSyncKeys.ChapterView.SCALE, view.scale());
            out.put(entry.getKey(), groupTag);
        }
        return out;
    }
}
